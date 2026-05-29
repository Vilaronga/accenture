package br.unit.residencia.accenture.Models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "equipes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "idEquipe")
public class Equipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_equipe")
    private Long idEquipe;

    @Column(nullable = false)
    private String nome;

    @ManyToOne
    @JoinColumn(name = "id_lider")
    private Usuario lider;

    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "equipe_membros",
            joinColumns = @JoinColumn(name = "id_equipe"),
            inverseJoinColumns = @JoinColumn(name = "id_usuario")
    )
    private List<Usuario> membros = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "equipe")
    private List<Reserva> reservas = new ArrayList<>();

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;
}