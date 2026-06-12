package br.unit.residencia.accenture.Repositories;

import br.unit.residencia.accenture.Models.Sala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalaRepository extends JpaRepository<Sala, Long> {
    Optional<Sala> findByNomeSala(String nomeSala);

    /*
     * Retorna sala com seus locais de trabalho
     */
    @Query("""
            select distinct s from Sala s
            left join fetch s.locaisDeTrabalho
            """)
    List<Sala> findAllComLocais();
}
