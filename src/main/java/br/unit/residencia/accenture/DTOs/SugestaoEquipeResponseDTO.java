package br.unit.residencia.accenture.DTOs;

import br.unit.residencia.accenture.Models.Equipe;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public record SugestaoEquipeResponseDTO(

        //remover caso erro
        @JsonProperty("token")
        String token,

        @JsonProperty("id_equipe")
        Long idEquipe,

        @JsonProperty("idSala")
        Long idSala,

        @JsonProperty("nomeSala")
        String nomeSala,

        @JsonProperty("justificativa")
        String justificativa,

        @JsonProperty("atribuicoes")
        List<AtribuicaoMembroDTO> atribuicoes,

        @JsonProperty("observacoes")
        List<String> observacoes,

        @JsonProperty("dataHoraInicio")
        LocalDateTime dataHoraInicio,

        @JsonProperty("dataHoraFim")
        LocalDateTime dataHoraFim
) {}