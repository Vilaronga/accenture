package br.unit.residencia.accenture.DTOs;

import br.unit.residencia.accenture.Models.ReservaLocal;

public record ReservaLocalDTO(
        Long idReservaLocal,
        Long idLocalDeTrabalho,
        String tipoCadeira,
        Double posX,
        Double posY,
        Long idUsuario,
        String nomeUsuario
) {
    public static ReservaLocalDTO from(ReservaLocal rl) {
        return new ReservaLocalDTO(
                rl.getIdReservaLocal(),
                rl.getLocalDeTrabalho().getIdLocalDeTrabalho(),
                rl.getLocalDeTrabalho().getTipoCadeira() == null
                        ? null : rl.getLocalDeTrabalho().getTipoCadeira().name(),
                rl.getLocalDeTrabalho().getPosX(),
                rl.getLocalDeTrabalho().getPosY(),
                rl.getUsuario() == null ? null : rl.getUsuario().getIdUsuario(),
                rl.getUsuario() == null ? null : rl.getUsuario().getNome()
        );
    }
}