package br.unit.residencia.accenture.DTOs;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record SugestaoEquipeRequestDTO(

        @NotNull(message = "O id da equipe é obrigatório.")
        Long idEquipe,

        @NotNull(message = "A data/hora de início é obrigatória.")
        LocalDateTime dataHoraInicio,

        @NotNull(message = "A data/hora de fim é obrigatória.")
        LocalDateTime dataHoraFim
) {}