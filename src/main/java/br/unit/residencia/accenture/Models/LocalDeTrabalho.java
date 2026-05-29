package br.unit.residencia.accenture.Models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "local_de_trabalho")
@Getter @Setter
@EqualsAndHashCode(of = "idLocalDeTrabalho")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocalDeTrabalho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id_local_de_trabalho")
    private Long idLocalDeTrabalho;

    @ManyToOne
    @JoinColumn(name="id_sala")
    private Sala sala;

    @Enumerated(EnumType.STRING)
    @Column(name="tipo_cadeira")
    private TipoCadeira tipoCadeira;

    private Double posX;
    private Double posY;
}
