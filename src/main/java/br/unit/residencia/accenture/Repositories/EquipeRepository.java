package br.unit.residencia.accenture.Repositories;

import br.unit.residencia.accenture.Models.Equipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EquipeRepository extends JpaRepository<Equipe, Long> {

    @Query("""
            select distinct e from Equipe e
            left join fetch e.membros
            where e.idEquipe = :id
            """)
    Optional<Equipe> findByIdComMembros(@Param("id") Long id);
}
