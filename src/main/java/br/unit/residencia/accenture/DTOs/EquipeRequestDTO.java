package br.unit.residencia.accenture.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record EquipeRequestDTO(

        @NotBlank(message = "O nome da equipe é obrigatório.")
        String nome,

        @NotNull(message = "O id do líder é obrigatório.")
        Long idLider,

        // IDs dos membros pode ser vazio, mas não nulo
        List<Long> idsMembros
) {}