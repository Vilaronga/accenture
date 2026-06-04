package br.unit.residencia.accenture.Services;

import br.unit.residencia.accenture.DTOs.SugestaoEquipeResponseDTO;
import br.unit.residencia.accenture.Models.Equipe;
import br.unit.residencia.accenture.Models.LocalDeTrabalho;
import br.unit.residencia.accenture.Models.Sala;
import br.unit.residencia.accenture.Models.TipoCadeira;
import br.unit.residencia.accenture.Models.Usuario;
import br.unit.residencia.accenture.Repositories.EquipeRepository;
import br.unit.residencia.accenture.Repositories.SalaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SugestaoService {

    private final EquipeRepository equipeRepository;
    private final SalaRepository salaRepository;
    private final GeminiService geminiService;

    /** Uma estação de trabalho (cadeira tipo PC) posicionada na planta. */
    private record Estacao(Long id, double x, double y) {
    }

    /** Grupo de estações mais próximas entre si dentro de uma sala. */
    private record Agrupamento(Sala sala, List<Estacao> estacoes, double compacidade) {
    }

    public SugestaoEquipeResponseDTO sugerirParaEquipe(Long idEquipe) {
        Equipe equipe = equipeRepository.findByIdComMembros(idEquipe)
                .orElseThrow(() -> new IllegalArgumentException("Equipe não encontrada: " + idEquipe));

        List<Usuario> membros = equipe.getMembros();
        if (membros == null || membros.isEmpty()) {
            throw new IllegalArgumentException("A equipe '" + equipe.getNome() + "' não possui membros.");
        }
        int tamanho = membros.size();

        List<Sala> salas = salaRepository.findAllComLocais();

        // Para cada sala, encontra o grupo de estações mais próximas entre si do tamanho da equipe.
        List<Agrupamento> candidatas = new ArrayList<>();
        for (Sala sala : salas) {
            List<Estacao> estacoes = estacoesDeTrabalho(sala);
            if (estacoes.size() >= tamanho) {
                List<Estacao> grupo = grupoMaisProximo(estacoes, tamanho);
                candidatas.add(new Agrupamento(sala, grupo, compacidade(grupo)));
            }
        }
        candidatas.sort(Comparator.comparingDouble(Agrupamento::compacidade));

        String salaSugerida = candidatas.isEmpty() ? null : candidatas.getFirst().sala().getNomeSala();

        String prompt = montarPrompt(equipe, membros, salas, candidatas);
        String sugestaoIa = geminiService.gerarTexto(prompt);

        return new SugestaoEquipeResponseDTO(
                equipe.getIdEquipe(),
                equipe.getNome(),
                tamanho,
                membros.stream().map(this::descreverMembro).toList(),
                salaSugerida,
                sugestaoIa
        );
    }

    private String descreverMembro(Usuario u) {
        String especialidade = u.getEspecialidade() == null ? "SEM_ESPECIALIDADE" : u.getEspecialidade().name();
        return u.getNome() + " (" + especialidade + ")";
    }

    /** Estações de trabalho (cadeiras tipo PC) com coordenadas válidas. */
    private List<Estacao> estacoesDeTrabalho(Sala sala) {
        List<Estacao> estacoes = new ArrayList<>();
        for (LocalDeTrabalho local : sala.getLocaisDeTrabalho()) {
            if (local.getTipoCadeira() == TipoCadeira.PC
                    && local.getPosX() != null && local.getPosY() != null) {
                estacoes.add(new Estacao(local.getIdLocalDeTrabalho(), local.getPosX(), local.getPosY()));
            }
        }
        return estacoes;
    }

    /**
     * Encontra o subconjunto de {@code n} estações mais próximas entre si.
     * Estratégia gulosa: usa cada estação como semente, pega as n mais próximas dela
     * e escolhe o grupo de menor dispersão.
     */
    private List<Estacao> grupoMaisProximo(List<Estacao> estacoes, int n) {
        List<Estacao> melhor = null;
        double melhorScore = Double.MAX_VALUE;

        for (Estacao semente : estacoes) {
            List<Estacao> ordenadas = new ArrayList<>(estacoes);
            ordenadas.sort(Comparator.comparingDouble(e -> distancia(e, semente)));
            List<Estacao> grupo = new ArrayList<>(ordenadas.subList(0, n));
            double score = compacidade(grupo);
            if (score < melhorScore) {
                melhorScore = score;
                melhor = grupo;
            }
        }
        return melhor;
    }

    /** Soma das distâncias de cada estação ao centroide do grupo (quanto menor, mais juntas). */
    private double compacidade(List<Estacao> grupo) {
        double cx = grupo.stream().mapToDouble(Estacao::x).average().orElse(0);
        double cy = grupo.stream().mapToDouble(Estacao::y).average().orElse(0);
        double soma = 0;
        for (Estacao e : grupo) {
            soma += Math.hypot(e.x() - cx, e.y() - cy);
        }
        return soma;
    }

    private double distancia(Estacao a, Estacao b) {
        return Math.hypot(a.x() - b.x(), a.y() - b.y());
    }

    private String montarPrompt(Equipe equipe, List<Usuario> membros, List<Sala> salas, List<Agrupamento> candidatas) {
        StringBuilder sb = new StringBuilder();
        sb.append("Você é um assistente de alocação de espaços de trabalho (workplace).\n");
        sb.append("Sua tarefa é sugerir onde uma equipe deve sentar, agrupando pessoas da MESMA ");
        sb.append("especialidade em cadeiras vizinhas e mantendo todo o time o mais próximo possível.\n\n");

        sb.append("EQUIPE: ").append(equipe.getNome())
                .append(" (").append(membros.size()).append(" membros)\n");
        sb.append("Membros e especialidades:\n");
        for (Usuario u : membros) {
            sb.append("- ").append(descreverMembro(u)).append('\n');
        }
        sb.append('\n');

        sb.append("SALAS QUE COMPORTAM A EQUIPE (estações de trabalho = cadeiras tipo PC).\n");
        sb.append("Para cada sala, listamos o grupo de cadeiras mais próximas entre si (recomendado):\n\n");
        if (candidatas.isEmpty()) {
            sb.append("Nenhuma sala possui estações suficientes para toda a equipe.\n\n");
        } else {
            for (Agrupamento ag : candidatas) {
                sb.append("Sala \"").append(ag.sala().getNomeSala()).append("\" (compacidade ")
                        .append(formatar(ag.compacidade())).append(" — quanto menor, mais juntas):\n");
                for (Estacao e : ag.estacoes()) {
                    sb.append("   - Estação #").append(e.id())
                            .append(" em (").append(formatar(e.x())).append(", ").append(formatar(e.y())).append(")\n");
                }
                sb.append('\n');
            }
        }

        // Salas que não comportam a equipe (apenas para contexto).
        List<String> insuficientes = new ArrayList<>();
        for (Sala sala : salas) {
            int qtd = estacoesDeTrabalho(sala).size();
            if (qtd < membros.size()) {
                insuficientes.add("\"" + sala.getNomeSala() + "\" (" + qtd + " estações)");
            }
        }
        if (!insuficientes.isEmpty()) {
            sb.append("Salas sem estações suficientes: ").append(String.join(", ", insuficientes)).append("\n\n");
        }

        sb.append("INSTRUÇÕES:\n");
        sb.append("1. Escolha UMA sala que comporte toda a equipe, preferindo a de menor compacidade ");
        sb.append("(cadeiras mais juntas).\n");
        sb.append("2. Atribua cada membro a uma estação específica (use o #id e as coordenadas), de modo ");
        sb.append("que pessoas da mesma especialidade fiquem em cadeiras vizinhas.\n");
        sb.append("3. Responda em português, de forma objetiva, contendo:\n");
        sb.append("   - Sala escolhida e o motivo.\n");
        sb.append("   - Lista no formato \"Membro -> Estação #id (x, y)\".\n");
        sb.append("   - Observações relevantes (por exemplo, se nenhuma sala comporta todos).\n");
        sb.append("Não invente cadeiras ou salas que não estejam na lista acima.\n");

        return sb.toString();
    }

    private String formatar(double v) {
        return String.format(Locale.US, "%.1f", v);
    }
}
