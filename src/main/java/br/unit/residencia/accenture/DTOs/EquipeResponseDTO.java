package br.unit.residencia.accenture.DTOs;

import br.unit.residencia.accenture.Models.Equipe;

import java.time.LocalDateTime;
import java.util.List;

public record EquipeResponseDTO(
        Long idEquipe,
        String nome,
        MembroDTO lider,
        List<MembroDTO> membros,
        LocalDateTime dataCriacao
) {
    public record MembroDTO(
            Long idUsuario,
            String nome,
            String email,
            String especialidade
    ) {}

    public static EquipeResponseDTO from(Equipe equipe) {
        MembroDTO liderDTO = equipe.getLider() == null ? null : new MembroDTO(
                equipe.getLider().getIdUsuario(),
                equipe.getLider().getNome(),
                equipe.getLider().getEmail(),
                equipe.getLider().getEspecialidade() == null ? null : equipe.getLider().getEspecialidade().name()
        );

        List<MembroDTO> membrosDTO = equipe.getMembros() == null ? List.of() :
                equipe.getMembros().stream()
                        .map(u -> new MembroDTO(
                                u.getIdUsuario(),
                                u.getNome(),
                                u.getEmail(),
                                u.getEspecialidade() == null ? null : u.getEspecialidade().name()
                        ))
                        .toList();

        return new EquipeResponseDTO(
                equipe.getIdEquipe(),
                equipe.getNome(),
                liderDTO,
                membrosDTO,
                equipe.getDataCriacao()
        );
    }
}