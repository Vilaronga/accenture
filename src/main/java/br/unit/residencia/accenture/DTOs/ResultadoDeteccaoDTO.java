package br.unit.residencia.accenture.DTOs;

import java.util.List;

public record ResultadoDeteccaoDTO(

        List<ObjetoDetectadoDTO> objetos

) {
}