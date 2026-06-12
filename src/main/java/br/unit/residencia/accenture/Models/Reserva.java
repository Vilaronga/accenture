package br.unit.residencia.accenture.Models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reservas")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "idReserva")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva")
    private Long idReserva;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoReserva tipoReserva;

    // Esse campo só é preenchido se o tipo de reserva for INDIVIDUAL
    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    // Esse campo só é preenchido se o tipo de reserva for EQUIPE
    @ManyToOne
    @JoinColumn(name = "id_equipe")
    private Equipe equipe;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_sala")
    private Sala sala;

    @Column(name = "data_hora_inicio")
    private LocalDateTime dataHoraInicio;

    @Column(name = "data_hora_fim")
    private LocalDateTime dataHoraFim;

    @Column(name = "data_hora_criacao")
    private LocalDateTime dataHoraCriacao;

    @Builder.Default
    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservaLocal> locais = new ArrayList<>();
}