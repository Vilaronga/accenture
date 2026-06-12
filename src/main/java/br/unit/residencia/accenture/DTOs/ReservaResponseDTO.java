package br.unit.residencia.accenture.DTOs;

import br.unit.residencia.accenture.Models.Reserva;

import java.time.LocalDateTime;
import java.util.List;

public record ReservaResponseDTO(
        Long idReserva,
        String tipoReserva,
        Long idUsuario,
        String nomeUsuario,
        Long idEquipe,
        String nomeEquipe,
        Long idSala,
        String nomeSala,
        LocalDateTime dataHoraInicio,
        LocalDateTime dataHoraFim,
        LocalDateTime dataHoraCriacao,
        List<ReservaLocalDTO> locais
) {
    public static ReservaResponseDTO from(Reserva r) {
        List<ReservaLocalDTO> locaisDTO = r.getLocais() == null ? List.of() :
                r.getLocais().stream().map(ReservaLocalDTO::from).toList();

        return new ReservaResponseDTO(
                r.getIdReserva(),
                r.getTipoReserva() == null ? null : r.getTipoReserva().name(),
                r.getUsuario()  == null ? null : r.getUsuario().getIdUsuario(),
                r.getUsuario()  == null ? null : r.getUsuario().getNome(),
                r.getEquipe()   == null ? null : r.getEquipe().getIdEquipe(),
                r.getEquipe()   == null ? null : r.getEquipe().getNome(),
                r.getSala()     == null ? null : r.getSala().getIdSala(),
                r.getSala()     == null ? null : r.getSala().getNomeSala(),
                r.getDataHoraInicio(),
                r.getDataHoraFim(),
                r.getDataHoraCriacao(),
                locaisDTO
        );
    }
}