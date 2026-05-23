package br.unit.residencia.accenture.Models;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name="sala")
public class Sala {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id_sala")
    private Long idSala;

    @ElementCollection
    @CollectionTable(name = "sala_recursos", joinColumns = @JoinColumn(name = "sala_id"))
    @Column(name = "recurso", length = 100)
    private List<String> recursos;
}
