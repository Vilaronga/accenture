package br.unit.residencia.accenture.Models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Column(unique = true)
    private String email;

    private String microsoftId;

    @Enumerated(EnumType.STRING)
    private Perfil perfil;

    @Column(name="data_criacao")
    private LocalDate dataCriacao;

    @Column(name="data_atualizacao")
    private LocalDate dataAtualizacao;
}