package br.unit.residencia.accenture.Services;

import br.unit.residencia.accenture.DTOs.ReservaRequestDTO;
import br.unit.residencia.accenture.DTOs.ReservaResponseDTO;
import br.unit.residencia.accenture.Models.*;
import br.unit.residencia.accenture.Repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EquipeRepository equipeRepository;
    private final SalaRepository salaRepository;
    private final ReservaLocalRepository reservaLocalRepository;

    /*
     * Cria a reserva
     */
    @Transactional
    public ReservaResponseDTO criar(ReservaRequestDTO dto) {
        validarDatas(dto.dataHoraInicio(), dto.dataHoraFim());

        Sala sala = salaRepository.findById(dto.idSala())
                .orElseThrow(() -> new IllegalArgumentException("Sala não encontrada: " + dto.idSala()));

        Usuario usuario = null;
        Equipe equipe   = null;

        if (dto.tipoReserva() == TipoReserva.INDIVIDUAL) {
            if (dto.idUsuario() == null)
                throw new IllegalArgumentException("Para reserva INDIVIDUAL, idUsuario é obrigatório.");
            usuario = usuarioRepository.findById(dto.idUsuario())
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + dto.idUsuario()));

        } else if (dto.tipoReserva() == TipoReserva.EQUIPE) {
            if (dto.idEquipe() == null)
                throw new IllegalArgumentException("Para reserva EQUIPE, idEquipe é obrigatório.");
            equipe = equipeRepository.findById(dto.idEquipe())
                    .orElseThrow(() -> new IllegalArgumentException("Equipe não encontrada: " + dto.idEquipe()));
        }

        // Mapa rápido: idLocalDeTrabalho → entidade
        Map<Long, LocalDeTrabalho> locaisMap = sala.getLocaisDeTrabalho().stream()
                .collect(Collectors.toMap(LocalDeTrabalho::getIdLocalDeTrabalho, l -> l));

        // Verifica conflito de horário para os locais solicitados
        if (dto.locais() != null && !dto.locais().isEmpty()) {
            List<Long> ocupados = reservaLocalRepository.findLocaisOcupados(
                    dto.idSala(), dto.dataHoraInicio(), dto.dataHoraFim());

            List<Long> conflitos = dto.locais().stream()
                    .map(ReservaRequestDTO.LocalReservadoDTO::idLocalDeTrabalho)
                    .filter(ocupados::contains)
                    .toList();

            if (!conflitos.isEmpty()) {
                throw new IllegalStateException(
                        "Os seguintes locais já estão reservados no período: " + conflitos);
            }
        }

        // Cria a reserva principal
        Reserva reserva = Reserva.builder()
                .tipoReserva(dto.tipoReserva())
                .usuario(usuario)
                .equipe(equipe)
                .sala(sala)
                .dataHoraInicio(dto.dataHoraInicio())
                .dataHoraFim(dto.dataHoraFim())
                .dataHoraCriacao(LocalDateTime.now())
                .build();

        reservaRepository.save(reserva);

        // Cria os vínculos de local de trabalho
        if (dto.locais() != null) {
            final Usuario usuarioFinal = usuario;
            for (ReservaRequestDTO.LocalReservadoDTO localDto : dto.locais()) {
                LocalDeTrabalho local = locaisMap.get(localDto.idLocalDeTrabalho());
                if (local == null) {
                    throw new IllegalArgumentException(
                            "Local de trabalho " + localDto.idLocalDeTrabalho() +
                                    " não pertence à sala " + dto.idSala());
                }

                // Se idUsuario vier no DTO usa ele senão usa null
                Usuario usuarioLocal = null;
                if (localDto.idUsuario() != null) {
                    usuarioLocal = usuarioRepository.findById(localDto.idUsuario())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Usuário não encontrado: " + localDto.idUsuario()));
                } else if (usuarioFinal != null) {
                    usuarioLocal = usuarioFinal;
                }

                ReservaLocal reservaLocal = ReservaLocal.builder()
                        .reserva(reserva)
                        .localDeTrabalho(local)
                        .usuario(usuarioLocal)
                        .build();

                reserva.getLocais().add(reservaLocalRepository.save(reservaLocal));
            }
        }

        return ReservaResponseDTO.from(reserva);
    }

    /*
     * Lista todas as reservas
     */
    @Transactional(readOnly = true)
    public List<ReservaResponseDTO> listarTodas() {
        return reservaRepository.findAll().stream()
                .map(ReservaResponseDTO::from)
                .toList();
    }

    /*
     * Busca a reserva por id
     */
    @Transactional(readOnly = true)
    public ReservaResponseDTO buscarPorId(Long id) {
        // Usa a query com fetch dos locais para não ter N+1
        Reserva reserva = reservaRepository.findByIdComLocais(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva não encontrada: " + id));
        return ReservaResponseDTO.from(reserva);
    }

    /*
     * Lista reservas pelo usuário
     */
    @Transactional(readOnly = true)
    public List<ReservaResponseDTO> listarPorUsuario(Long idUsuario) {
        if (!usuarioRepository.existsById(idUsuario))
            throw new IllegalArgumentException("Usuário não encontrado: " + idUsuario);
        return reservaRepository.findAll().stream()
                .filter(r -> r.getUsuario() != null && r.getUsuario().getIdUsuario().equals(idUsuario))
                .map(ReservaResponseDTO::from)
                .toList();
    }

    /*
     * Lista as reservas por equipes
     */
    @Transactional(readOnly = true)
    public List<ReservaResponseDTO> listarPorEquipe(Long idEquipe) {
        if (!equipeRepository.existsById(idEquipe))
            throw new IllegalArgumentException("Equipe não encontrada: " + idEquipe);
        return reservaRepository.findAll().stream()
                .filter(r -> r.getEquipe() != null && r.getEquipe().getIdEquipe().equals(idEquipe))
                .map(ReservaResponseDTO::from)
                .toList();
    }

    /*
     * Listas as reservas por sala
     */
    @Transactional(readOnly = true)
    public List<ReservaResponseDTO> listarPorSala(Long idSala) {
        if (!salaRepository.existsById(idSala))
            throw new IllegalArgumentException("Sala não encontrada: " + idSala);
        return reservaRepository.findAll().stream()
                .filter(r -> r.getSala() != null && r.getSala().getIdSala().equals(idSala))
                .map(ReservaResponseDTO::from)
                .toList();
    }

    /*
     * Atualiza uma reserva
     */
    @Transactional
    public ReservaResponseDTO atualizar(Long id, ReservaRequestDTO dto) {
        validarDatas(dto.dataHoraInicio(), dto.dataHoraFim());

        Reserva reserva = reservaRepository.findByIdComLocais(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva não encontrada: " + id));

        Sala sala = salaRepository.findById(dto.idSala())
                .orElseThrow(() -> new IllegalArgumentException("Sala não encontrada: " + dto.idSala()));

        Usuario usuario = null;
        Equipe equipe   = null;

        if (dto.tipoReserva() == TipoReserva.INDIVIDUAL) {
            if (dto.idUsuario() == null)
                throw new IllegalArgumentException("Para reserva INDIVIDUAL, idUsuario é obrigatório.");
            usuario = usuarioRepository.findById(dto.idUsuario())
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + dto.idUsuario()));
        } else if (dto.tipoReserva() == TipoReserva.EQUIPE) {
            if (dto.idEquipe() == null)
                throw new IllegalArgumentException("Para reserva EQUIPE, idEquipe é obrigatório.");
            equipe = equipeRepository.findById(dto.idEquipe())
                    .orElseThrow(() -> new IllegalArgumentException("Equipe não encontrada: " + dto.idEquipe()));
        }

        // Verifica conflito excluindo os próprios locais desta reserva
        if (dto.locais() != null && !dto.locais().isEmpty()) {
            List<Long> idsAtuais = reserva.getLocais().stream()
                    .map(rl -> rl.getLocalDeTrabalho().getIdLocalDeTrabalho())
                    .toList();

            List<Long> ocupados = reservaLocalRepository.findLocaisOcupados(
                            dto.idSala(), dto.dataHoraInicio(), dto.dataHoraFim()).stream()
                    .filter(localId -> !idsAtuais.contains(localId))
                    .toList();

            List<Long> conflitos = dto.locais().stream()
                    .map(ReservaRequestDTO.LocalReservadoDTO::idLocalDeTrabalho)
                    .filter(ocupados::contains)
                    .toList();

            if (!conflitos.isEmpty())
                throw new IllegalStateException("Os seguintes locais já estão reservados: " + conflitos);
        }

        Map<Long, LocalDeTrabalho> locaisMap = sala.getLocaisDeTrabalho().stream()
                .collect(Collectors.toMap(LocalDeTrabalho::getIdLocalDeTrabalho, l -> l));

        // Atualiza campos da reserva e limpa os locais antigos
        reserva.setTipoReserva(dto.tipoReserva());
        reserva.setUsuario(usuario);
        reserva.setEquipe(equipe);
        reserva.setSala(sala);
        reserva.setDataHoraInicio(dto.dataHoraInicio());
        reserva.setDataHoraFim(dto.dataHoraFim());
        reserva.getLocais().clear();

        if (dto.locais() != null) {
            final Usuario usuarioFinal = usuario;
            for (ReservaRequestDTO.LocalReservadoDTO localDto : dto.locais()) {
                LocalDeTrabalho local = locaisMap.get(localDto.idLocalDeTrabalho());
                if (local == null)
                    throw new IllegalArgumentException(
                            "Local " + localDto.idLocalDeTrabalho() + " não pertence à sala " + dto.idSala());

                Usuario usuarioLocal = null;
                if (localDto.idUsuario() != null) {
                    usuarioLocal = usuarioRepository.findById(localDto.idUsuario())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Usuário não encontrado: " + localDto.idUsuario()));
                } else if (usuarioFinal != null) {
                    usuarioLocal = usuarioFinal;
                }

                reserva.getLocais().add(ReservaLocal.builder()
                        .reserva(reserva)
                        .localDeTrabalho(local)
                        .usuario(usuarioLocal)
                        .build());
            }
        }

        return ReservaResponseDTO.from(reservaRepository.save(reserva));
    }

    /*
     * Exclui uma reserva
     */
    @Transactional
    public void cancelar(Long id) {
        if (!reservaRepository.existsById(id))
            throw new IllegalArgumentException("Reserva não encontrada: " + id);
        reservaRepository.deleteById(id);
    }

    /*
     * Método auxiliar somente para validar que a data de final seja maior que a data final.
     */
    private void validarDatas(LocalDateTime inicio, LocalDateTime fim) {
        if (!fim.isAfter(inicio))
            throw new IllegalArgumentException("A data/hora de fim deve ser posterior à de início.");
    }
}