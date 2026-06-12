package br.unit.residencia.accenture.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AtribuicaoMembroDTO(

        @JsonProperty("idUsuario")
        Long idUsuario,

        @JsonProperty("nomeUsuario")
        String nomeUsuario,

        @JsonProperty("especialidade")
        String especialidade,

        @JsonProperty("idLocalDeTrabalho")
        Long idLocalDeTrabalho,

        @JsonProperty("posX")
        Double posX,

        @JsonProperty("posY")
        Double posY,

        @JsonProperty("tipoCadeira")
        String tipoCadeira
) {}