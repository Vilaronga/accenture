package br.unit.residencia.accenture.Models;

import jakarta.persistence.*;
import lombok.*;

/*
 * Tabela de relacionamento entre uma Reserva e os locais de trabalho alocados.
 */
@Entity
@Table(name = "reserva_locais")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "idReservaLocal")
public class ReservaLocal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva_local")
    private Long idReservaLocal;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_reserva")
    private Reserva reserva;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_local_de_trabalho")
    private LocalDeTrabalho localDeTrabalho;

    // Usuário alocado neste local dentro desta reserva.
    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;
}