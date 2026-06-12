package br.unit.residencia.accenture.DTOs;

import br.unit.residencia.accenture.Models.TipoReserva;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record ReservaRequestDTO(

        @NotNull(message = "O tipo de reserva é obrigatório.")
        TipoReserva tipoReserva,

        // Para reserva INDIVIDUAL
        Long idUsuario,

        // Para reserva de EQUIPE
        Long idEquipe,

        @NotNull(message = "O id da sala é obrigatório.")
        Long idSala,

        List<LocalReservadoDTO> locais,

        @NotNull(message = "A data/hora de início é obrigatória.")
        LocalDateTime dataHoraInicio,

        @NotNull(message = "A data/hora de fim é obrigatória.")
        LocalDateTime dataHoraFim
) {
        public record LocalReservadoDTO(
                @NotNull Long idLocalDeTrabalho,
                Long idUsuario
        ) {}
}