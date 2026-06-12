package br.unit.residencia.accenture.Repositories;

import br.unit.residencia.accenture.Models.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    /*
     * Busca as reservas mais os locais de trabalho referentes àquela reserva
     */
    @Query("""
            SELECT DISTINCT r FROM Reserva r
            LEFT JOIN FETCH r.locais rl
            LEFT JOIN FETCH rl.localDeTrabalho
            LEFT JOIN FETCH rl.usuario
            WHERE r.idReserva = :id
            """)
    Optional<Reserva> findByIdComLocais(@Param("id") Long id);
}