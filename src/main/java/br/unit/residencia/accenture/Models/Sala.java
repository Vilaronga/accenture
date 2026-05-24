package br.unit.residencia.accenture.Models;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="sala")
@Getter @Setter
@EqualsAndHashCode(of = "idSala")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sala {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id_sala")
    private Long idSala;

    @Column(name="nome_sala")
    private String nomeSala;

    @Column(name="caminho_planta")
    private String caminhoPlanta;

    @Builder.Default
    @OneToMany(mappedBy = "sala", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Recurso> recursos = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "sala", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LocalDeTrabalho> locaisDeTrabalho = new ArrayList<>();

    //Métodos adicionais
    public void adicionarRecurso(Recurso recurso) {
        recursos.add(recurso);
        recurso.setSala(this);
    }

    public void removerRecurso(Recurso recurso) {
        if (this.recursos.contains(recurso)) {
            this.recursos.remove(recurso);
            recurso.setSala(null);
        }
    }

    public void adicionarLocal(LocalDeTrabalho local) {
        locaisDeTrabalho.add(local);
        local.setSala(this);
    }
}
