package br.unit.residencia.accenture.Services;

import br.unit.residencia.accenture.DTOs.AtribuicaoMembroDTO;
import br.unit.residencia.accenture.DTOs.SugestaoEquipeRequestDTO;
import br.unit.residencia.accenture.DTOs.SugestaoEquipeResponseDTO;
import br.unit.residencia.accenture.Models.*;
import br.unit.residencia.accenture.Repositories.EquipeRepository;
import br.unit.residencia.accenture.Repositories.ReservaRepository;
import br.unit.residencia.accenture.Repositories.ReservaLocalRepository;
import br.unit.residencia.accenture.Repositories.SalaRepository;
import br.unit.residencia.accenture.Repositories.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SugestaoService {

    private final EquipeRepository equipeRepository;
    private final SalaRepository salaRepository;
    private final ReservaRepository reservaRepository;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;
    private final UsuarioRepository usuarioRepository;
    private final ReservaLocalRepository reservaLocalRepository;

    private final Map<String, SugestaoEquipeResponseDTO> cacheTokenSugestao = new ConcurrentHashMap<>();

    /*
     * Tipos de cadeira para cada especialidade
     */
    private static final Set<Especialidade> ESPECIALIDADES_PC = Set.of(
            Especialidade.BACKEND,
            Especialidade.FRONTEND,
            Especialidade.DESIGNER
    );

    /*
     * Recursos definidos para cada especialidade
     */

    private static final Map<Especialidade, List<TipoRecurso>> RECURSOS_POR_ESPECIALIDADE = Map.of(
            Especialidade.FRONTEND, List.of(),
            Especialidade.DESIGNER, List.of(TipoRecurso.TV, TipoRecurso.PAINELLED),
            Especialidade.UI_UX, List.of(TipoRecurso.TV, TipoRecurso.PAINELLED),
            Especialidade.DEVOPS, List.of(TipoRecurso.PROJETOR, TipoRecurso.PAINELLED, TipoRecurso.IMPRESSORA),
            Especialidade.BACKEND, List.of()
    );

    /*
     * Recebe a requisição
     */
    public SugestaoEquipeResponseDTO sugerirParaEquipe(SugestaoEquipeRequestDTO request) {

        // 1. Busca equipe com membros
        Equipe equipe = equipeRepository.findByIdComMembros(request.idEquipe())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Equipe não encontrada: " + request.idEquipe()));

        List<Usuario> membros = equipe.getMembros();
        if (membros == null || membros.isEmpty()) {
            throw new IllegalArgumentException(
                    "A equipe '" + equipe.getNome() + "' não possui membros.");
        }

        // 2. Calcula necessidades da equipe
        long qtdPC    = membros.stream().filter(m -> precisaPC(m.getEspecialidade())).count();
        long qtdGeral = membros.size() - qtdPC;

        Set<TipoRecurso> recursosDesejados = membros.stream()
                .filter(m -> m.getEspecialidade() != null)
                .flatMap(m -> RECURSOS_POR_ESPECIALIDADE
                        .getOrDefault(m.getEspecialidade(), List.of()).stream())
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(TipoRecurso.class)));

        // 3. Busca todas as salas com seus locais de trabalho
        List<Sala> todasSalas = salaRepository.findAllComLocais();

        // 4. Para cada sala, calcula locais livres no período solicitado
        List<SalaDisponivel> salasDisponiveis = new ArrayList<>();

        for (Sala sala : todasSalas) {
            Set<Long> ocupados = new HashSet<>(
                    reservaLocalRepository.findLocaisOcupados(
                            sala.getIdSala(),
                            request.dataHoraInicio(),
                            request.dataHoraFim()
                    )
            );

            List<LocalDisponivel> livresPC = sala.getLocaisDeTrabalho().stream()
                    .filter(l -> l.getTipoCadeira() == TipoCadeira.PC)
                    .filter(l -> !ocupados.contains(l.getIdLocalDeTrabalho()))
                    .filter(l -> l.getPosX() != null && l.getPosY() != null)
                    .map(l -> new LocalDisponivel(l.getIdLocalDeTrabalho(), "PC", l.getPosX(), l.getPosY()))
                    .toList();

            List<LocalDisponivel> livresGeral = sala.getLocaisDeTrabalho().stream()
                    .filter(l -> l.getTipoCadeira() == TipoCadeira.GERAL)
                    .filter(l -> !ocupados.contains(l.getIdLocalDeTrabalho()))
                    .filter(l -> l.getPosX() != null && l.getPosY() != null)
                    .map(l -> new LocalDisponivel(l.getIdLocalDeTrabalho(), "GERAL", l.getPosX(), l.getPosY()))
                    .toList();

            // Só inclui salas que atendam às necessidades da equipe
            if (livresPC.size() >= qtdPC && livresGeral.size() >= qtdGeral) {
                List<TipoRecurso> recursosSala = sala.getRecursos().stream()
                        .map(Recurso::getTipoRecurso)
                        .distinct()
                        .toList();

                double compacidade = calcularCompacidade(livresPC, livresGeral, (int) qtdPC, (int) qtdGeral);

                salasDisponiveis.add(new SalaDisponivel(
                        sala.getIdSala(),
                        sala.getNomeSala(),
                        livresPC,
                        livresGeral,
                        recursosSala,
                        compacidade
                ));
            }
        }

        // Ordena por compacidade (cadeiras mais juntas primeiro)
        salasDisponiveis.sort(Comparator.comparingDouble(SalaDisponivel::compacidade));

        // 5. Montar o prompt e envia para o service pra chamar o Gemini
        String prompt = montarPrompt(equipe, membros, request, qtdPC, qtdGeral,
                recursosDesejados, salasDisponiveis, todasSalas);

        String respostaGemini = geminiService.gerarTextoComSystemPrompt(prompt);

        // 6. Paresear resposta do gemini
        SugestaoEquipeResponseDTO resposta = parsearResposta(respostaGemini);

        // 7. Cria o token que armazena a sugestão
        String token = UUID.randomUUID().toString();

        SugestaoEquipeResponseDTO respostaComToken = new SugestaoEquipeResponseDTO(
                token, request.idEquipe(), resposta.idSala(), resposta.nomeSala(),
                resposta.justificativa(), resposta.atribuicoes(), resposta.observacoes(),
                request.dataHoraInicio(), request.dataHoraFim()
        );

        // 8. Armazena o token em cache
        cacheTokenSugestao.put(token, respostaComToken);
        return respostaComToken;
    }

    /*
     * Aceite a sugestão (Recebe o token)
     */
    @Transactional
    public void aceitarSugestao(String token) {
        SugestaoEquipeResponseDTO sugestao = cacheTokenSugestao.get(token);
        if (sugestao == null) {
            throw new IllegalArgumentException("Sugestão expirada ou inválida.");
        }

        Equipe equipe = equipeRepository.findById(sugestao.idEquipe()).orElseThrow();
        Sala sala = salaRepository.findById(sugestao.idSala()).orElseThrow();

        // Mapa rápido: idLocalDeTrabalho → entidade (evita N queries)
        Map<Long, LocalDeTrabalho> locaisMap = sala.getLocaisDeTrabalho().stream()
                .collect(java.util.stream.Collectors.toMap(
                        LocalDeTrabalho::getIdLocalDeTrabalho, l -> l));

        //Verificação de conflito (caso uma reserva seja realizada no meio-termo)
        List<Long> idsLocaisAReservar = sugestao.atribuicoes().stream()
                .map(AtribuicaoMembroDTO::idLocalDeTrabalho)
                .toList();

        //obtém uma lista de lugares atualmente reservados
        List<Long> ocupados = reservaLocalRepository.findLocaisOcupados(
                sugestao.idSala(), sugestao.dataHoraInicio(), sugestao.dataHoraFim());

        //Verifica se os locais a serem reservados batem com os ocupados
        List<Long> conflitos = idsLocaisAReservar.stream()
                .filter(ocupados::contains)
                .toList();

        // Verifica a lista de conflitos, se houver conflito deleta o token e lança exceção.
        if (!conflitos.isEmpty()) {
            cacheTokenSugestao.remove(token);
            throw new IllegalStateException(
                    "Não foi possível confirmar a reserva pois os locais " + conflitos +
                            " já foram ocupados. Solicite uma nova sugestão.");
        }

        // Uma única reserva para a equipe com todos os locais vinculados
        Reserva reserva = Reserva.builder()
                .tipoReserva(TipoReserva.EQUIPE)
                .equipe(equipe)
                .sala(sala)
                .dataHoraInicio(sugestao.dataHoraInicio())
                .dataHoraFim(sugestao.dataHoraFim())
                .dataHoraCriacao(LocalDateTime.now())
                .build();

        reservaRepository.save(reserva);

        for (AtribuicaoMembroDTO atrib : sugestao.atribuicoes()) {
            Usuario usuario = usuarioRepository.findById(atrib.idUsuario()).orElseThrow();
            LocalDeTrabalho local = locaisMap.get(atrib.idLocalDeTrabalho());

            ReservaLocal reservaLocal = ReservaLocal.builder()
                    .reserva(reserva)
                    .localDeTrabalho(local)
                    .usuario(usuario)
                    .build();

            reserva.getLocais().add(reservaLocalRepository.save(reservaLocal));
        }

        cacheTokenSugestao.remove(token);
    }

    /*
     * Recusa uma sugestão (Recebe o token)
     */
    public void recusarSugestao(String token) {
        if (cacheTokenSugestao.remove(token) == null) {
            throw new IllegalArgumentException("Sugestão expirada ou inválida.");
        }
    }

    /*
     * Monta o prompt
     */
    private String montarPrompt(
            Equipe equipe,
            List<Usuario> membros,
            SugestaoEquipeRequestDTO request,
            long qtdPC,
            long qtdGeral,
            Set<TipoRecurso> recursosDesejados,
            List<SalaDisponivel> salasDisponiveis,
            List<Sala> todasSalas
    ) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        StringBuilder sb = new StringBuilder();

        // Contexto da solicitação
        sb.append("Dados da solicitação de reserva:\n");
        sb.append("- Equipe: \"").append(equipe.getNome()).append("\" (")
                .append(membros.size()).append(" membros)\n");
        sb.append("- Período: ")
                .append(request.dataHoraInicio().format(fmt))
                .append(" até ")
                .append(request.dataHoraFim().format(fmt))
                .append("\n\n");

        // Membros e suas necessidades
        sb.append("Membros e necessidades (já filtradas para o período solicitado):\n");
        sb.append("[\n");
        for (Usuario u : membros) {
            String esp = u.getEspecialidade() == null ? "SEM_ESPECIALIDADE" : u.getEspecialidade().name();
            String cadeira = precisaPC(u.getEspecialidade()) ? "CADEIRA_PC" : "CADEIRA_GERAL";
            sb.append("  { \"idUsuario\": ").append(u.getIdUsuario())
                    .append(", \"nome\": \"").append(u.getNome())
                    .append("\", \"especialidade\": \"").append(esp)
                    .append("\", \"precisaDe\": \"").append(cadeira)
                    .append("\" },\n");
        }
        // Remove a última vírgula e fecha
        if (!membros.isEmpty()) sb.setLength(sb.length() - 2);
        sb.append("\n]\n\n");

        // Resumo de necessidades
        sb.append("Resumo das necessidades da equipe:\n");
        sb.append("- Cadeiras PC necessárias: ").append(qtdPC).append("\n");
        sb.append("- Cadeiras Gerais necessárias: ").append(qtdGeral).append("\n");
        if (recursosDesejados.isEmpty()) {
            sb.append("- Recursos desejáveis: nenhum\n\n");
        } else {
            String recursos = recursosDesejados.stream()
                    .map(Enum::name).collect(Collectors.joining(", "));
            sb.append("- Recursos desejáveis (baseado nas especialidades): ").append(recursos).append("\n\n");
        }

        // Salas disponíveis
        sb.append("Salas com locais de trabalho LIVRES no período informado ");
        sb.append("(apenas locais não reservados são listados):\n");

        if (salasDisponiveis.isEmpty()) {
            sb.append("Nenhuma sala possui cadeiras livres suficientes para toda a equipe no período informado.\n\n");
        } else {
            sb.append("[\n");
            for (SalaDisponivel sd : salasDisponiveis) {
                sb.append("  {\n");
                sb.append("    \"idSala\": ").append(sd.idSala()).append(",\n");
                sb.append("    \"nomeSala\": \"").append(sd.nomeSala()).append("\",\n");
                sb.append("    \"compacidade\": ").append(String.format(Locale.US, "%.2f", sd.compacidade()))
                        .append("  /* quanto menor, mais próximas as cadeiras */,\n");

                // Recursos da sala
                sb.append("    \"recursosDisponíveis\": [");
                sb.append(sd.recursos().stream()
                        .map(r -> "\"" + r.name() + "\"")
                        .collect(Collectors.joining(", ")));
                sb.append("],\n");

                // Locais PC livres
                sb.append("    \"locaisPCLivres\": [\n");
                for (LocalDisponivel ld : sd.livresPC()) {
                    sb.append("      { \"id\": ").append(ld.id())
                            .append(", \"tipo\": \"PC\"")
                            .append(", \"posX\": ").append(String.format(Locale.US, "%.1f", ld.posX()))
                            .append(", \"posY\": ").append(String.format(Locale.US, "%.1f", ld.posY()))
                            .append(" },\n");
                }
                if (!sd.livresPC().isEmpty()) sb.setLength(sb.length() - 2);
                sb.append("\n    ],\n");

                // Locais GERAL livres
                sb.append("    \"locaisGERALLivres\": [\n");
                for (LocalDisponivel ld : sd.livresGeral()) {
                    sb.append("      { \"id\": ").append(ld.id())
                            .append(", \"tipo\": \"GERAL\"")
                            .append(", \"posX\": ").append(String.format(Locale.US, "%.1f", ld.posX()))
                            .append(", \"posY\": ").append(String.format(Locale.US, "%.1f", ld.posY()))
                            .append(" },\n");
                }
                if (!sd.livresGeral().isEmpty()) sb.setLength(sb.length() - 2);
                sb.append("\n    ]\n");

                sb.append("  },\n");
            }
            sb.setLength(sb.length() - 2);
            sb.append("\n]\n\n");
        }

        // Salas insuficientes
        List<String> insuficientes = todasSalas.stream()
                .filter(s -> salasDisponiveis.stream().noneMatch(sd -> sd.idSala().equals(s.getIdSala())))
                .map(s -> "\"" + s.getNomeSala() + "\"")
                .toList();
        if (!insuficientes.isEmpty()) {
            sb.append("Salas sem disponibilidade suficiente no período (não devem ser sugeridas): ")
                    .append(String.join(", ", insuficientes)).append("\n\n");
        }

        // ---- Regras de alocação ----
        sb.append("Regras de alocação (siga OBRIGATORIAMENTE):\n");
        sb.append("1. Membros que precisam de CADEIRA_PC devem receber SOMENTE um local do tipo \"PC\".\n");
        sb.append("2. Membros que precisam de CADEIRA_GERAL devem receber SOMENTE um local do tipo \"GERAL\".\n");
        sb.append("3. Cada local de trabalho só pode ser atribuído a UM membro. Sem repetições de \"idLocalDeTrabalho\".\n");
        sb.append("4. Prefira a sala com MENOR compacidade (cadeiras mais próximas entre si).\n");
        sb.append("5. Dentre as salas de menor compacidade, prefira a que tiver mais recursos desejáveis da equipe.\n");
        sb.append("6. Agrupe membros da MESMA especialidade em locais vizinhos (menores distâncias entre si).\n");
        sb.append("7. Use SOMENTE IDs de locais presentes nas listas acima. NUNCA invente IDs.\n");
        sb.append("8. Se nenhuma sala atender, retorne \"idSala\": null, \"atribuicoes\": [] e explique em \"observacoes\".\n\n");
        sb.append("9. Pode ter mais de uma equipe por sala desde que caibam todos os membros e atenda a todos os requisitos.\n");

        // ---- Formato de resposta ----
        sb.append("Responda APENAS com o seguinte JSON (sem nenhum texto fora dele):\n");
        sb.append("{\n");
        sb.append("  \"idSala\": <Long ou null>,\n");
        sb.append("  \"nomeSala\": \"<string ou null>\",\n");
        sb.append("  \"justificativa\": \"<por que esta sala foi escolhida, mencionando compacidade e recursos>\",\n");
        sb.append("  \"atribuicoes\": [\n");
        sb.append("    {\n");
        sb.append("      \"idUsuario\": <Long>,\n");
        sb.append("      \"nomeUsuario\": \"<string>\",\n");
        sb.append("      \"especialidade\": \"<string>\",\n");
        sb.append("      \"idLocalDeTrabalho\": <Long>,\n");
        sb.append("      \"posX\": <Double>,\n");
        sb.append("      \"posY\": <Double>,\n");
        sb.append("      \"tipoCadeira\": \"<PC ou GERAL>\"\n");
        sb.append("    }\n");
        sb.append("  ],\n");
        sb.append("  \"observacoes\": [\"<string>\", ...]\n");
        sb.append("}\n");

        return sb.toString();
    }

    /*
     * Perse da resposta
     */
    private SugestaoEquipeResponseDTO parsearResposta(String textoGemini) {
        // Remove blocos markdown se o Gemini os incluir mesmo com a instrução
        String json = textoGemini
                .replaceAll("(?s)```json\\s*", "")
                .replaceAll("(?s)```\\s*", "")
                .trim();

        // Garante que começa em '{' (remove qualquer texto antes)
        int inicio = json.indexOf('{');
        if (inicio > 0) {
            json = json.substring(inicio);
        }

        try {
            return objectMapper.readValue(json, SugestaoEquipeResponseDTO.class);
        } catch (Exception e) {
            throw new RuntimeException(
                    "O Gemini retornou uma resposta que não pôde ser interpretada como JSON. " +
                            "Resposta recebida:\n" + textoGemini, e);
        }
    }

    /*
     * Verificar se precisa de cadeira PC conforme a especialidade.
     */
    private boolean precisaPC(Especialidade especialidade) {
        return especialidade != null && ESPECIALIDADES_PC.contains(especialidade);
    }

    /*
     * Calcula a compacidade do melhor grupo possível dentro da sala,
     * considerando as necessidades de cadeira PC e GERAL.
     */
    private double calcularCompacidade(
            List<LocalDisponivel> livresPC,
            List<LocalDisponivel> livresGeral,
            int nPC,
            int nGeral
    ) {
        if (nPC == 0 && nGeral == 0) return 0;

        // Pega os nPC locais PC mais próximos do centroide de todos os locais PC
        List<LocalDisponivel> grupoPC    = nPC    > 0 ? grupoMaisProximo(livresPC, nPC)       : List.of();
        List<LocalDisponivel> grupoGeral = nGeral > 0 ? grupoMaisProximo(livresGeral, nGeral) : List.of();

        List<LocalDisponivel> grupoTotal = new ArrayList<>();
        grupoTotal.addAll(grupoPC);
        grupoTotal.addAll(grupoGeral);

        double cx = grupoTotal.stream().mapToDouble(LocalDisponivel::posX).average().orElse(0);
        double cy = grupoTotal.stream().mapToDouble(LocalDisponivel::posY).average().orElse(0);

        return grupoTotal.stream()
                .mapToDouble(l -> Math.hypot(l.posX() - cx, l.posY() - cy))
                .sum();
    }

    /*
     * Estratégia gulosa: para cada ponto semente, pega os n mais próximos e escolhe o melhor grupo.
     */
    private List<LocalDisponivel> grupoMaisProximo(List<LocalDisponivel> locais, int n) {
        if (locais.size() <= n) return new ArrayList<>(locais);

        List<LocalDisponivel> melhor = null;
        double melhorScore = Double.MAX_VALUE;

        for (LocalDisponivel semente : locais) {
            List<LocalDisponivel> ordenados = new ArrayList<>(locais);
            ordenados.sort(Comparator.comparingDouble(l -> distancia(l, semente)));
            List<LocalDisponivel> grupo = new ArrayList<>(ordenados.subList(0, n));
            double score = compacidadeSimples(grupo);
            if (score < melhorScore) {
                melhorScore = score;
                melhor = grupo;
            }
        }
        return melhor;
    }

    private double compacidadeSimples(List<LocalDisponivel> grupo) {
        double cx = grupo.stream().mapToDouble(LocalDisponivel::posX).average().orElse(0);
        double cy = grupo.stream().mapToDouble(LocalDisponivel::posY).average().orElse(0);
        return grupo.stream().mapToDouble(l -> Math.hypot(l.posX() - cx, l.posY() - cy)).sum();
    }

    private double distancia(LocalDisponivel a, LocalDisponivel b) {
        return Math.hypot(a.posX() - b.posX(), a.posY() - b.posY());
    }

    /*
     * Records auxiliares
     */
    private record LocalDisponivel(
            Long id,
            String tipo,
            double posX,
            double posY
    ) {}

    private record SalaDisponivel(
            Long idSala,
            String nomeSala,
            List<LocalDisponivel> livresPC,
            List<LocalDisponivel> livresGeral,
            List<TipoRecurso> recursos,
            double compacidade
    ) {}
}