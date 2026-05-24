package br.unit.residencia.accenture.Models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "recursos")
@Getter @Setter
@EqualsAndHashCode(of = "idRecurso")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id_recurso")
    private Long idRecurso;

    @ManyToOne
    @JoinColumn(name="id_sala")
    private Sala sala;

    @Column(name = "tipo_recurso")
    @Enumerated(EnumType.STRING)
    private TipoRecurso tipoRecurso;

    private Double posX;
    private Double posY;
}
