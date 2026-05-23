package br.unit.residencia.accenture.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "local_de_trabalho")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocalDeTrabalho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id_local_de_trabalho")
    private Long idLocalDeTrabalho;

    @Enumerated(EnumType.STRING)
    @Column(name="tipo_cadeira")
    private TipoCadeira tipoCadeira;

    private float posX;
    private float posY;
}
