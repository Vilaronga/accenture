package br.unit.residencia.accenture.Repositories;

import br.unit.residencia.accenture.Models.ReservaLocal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservaLocalRepository extends JpaRepository<ReservaLocal, Long> {

    /*
     * Retorna os IDs dos locais de trabalho já ocupados em uma sala
     * que conflitam com o intervalo [inicio, fim).
     */
    @Query("""
            SELECT rl.localDeTrabalho.idLocalDeTrabalho
            FROM ReservaLocal rl
            WHERE rl.reserva.sala.idSala = :idSala
              AND rl.reserva.dataHoraInicio < :fim
              AND rl.reserva.dataHoraFim    > :inicio
            """)
    List<Long> findLocaisOcupados(
            @Param("idSala")  Long          idSala,
            @Param("inicio")  LocalDateTime inicio,
            @Param("fim")     LocalDateTime fim
    );
}