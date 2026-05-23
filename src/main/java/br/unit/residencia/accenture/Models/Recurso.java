package br.unit.residencia.accenture.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recursos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id_recurso")
    private Long idRecurso;

    @Column(name = "tipo_recurso")
    @Enumerated(EnumType.STRING)
    private TipoRecurso tipoRecurso;

    private int posX;
    private int posY;
}
