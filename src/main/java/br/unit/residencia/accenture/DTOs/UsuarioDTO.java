package br.unit.residencia.accenture.DTOs;

import br.unit.residencia.accenture.Models.Perfil;

import java.time.LocalDate;

public record UsuarioDTO (
        Long id,
        String nome,
        String email,
        String microsoftId,
        Perfil perfil,
        LocalDate dataCriacao,
        LocalDate dataAtualizacao
){
}
