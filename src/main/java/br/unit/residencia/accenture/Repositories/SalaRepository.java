package br.unit.residencia.accenture.Repositories;

import br.unit.residencia.accenture.Models.Sala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SalaRepository extends JpaRepository<Sala, Long> {
    Optional<Sala> findByNomeSala(String nomeSala);
}