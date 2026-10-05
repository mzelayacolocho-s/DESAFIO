package com.udb.eventos.repository;

import com.udb.eventos.model.Event;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Integer> {

    /**
     * Bloquea la fila del evento mientras dura la transaccion. Asi dos
     * reservas simultaneas no pueden pasar la validacion de cupos a la vez
     * (evita sobreventa).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Event e where e.idEvent = :id")
    Optional<Event> findByIdForUpdate(@Param("id") Integer id);
}