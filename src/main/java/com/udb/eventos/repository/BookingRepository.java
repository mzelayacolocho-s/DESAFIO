package com.udb.eventos.repository;

import com.udb.eventos.model.Booking;
import com.udb.eventos.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {

    List<Booking> findByUserUsernameOrderByBookingDateDesc(String username);

    boolean existsByEvent_IdEvent(Integer eventId);

    /** Suma de entradas de un evento segun el estado (0 si no hay reservas). */
    @Query("select coalesce(sum(b.quantity), 0L) from Booking b "
            + "where b.event.idEvent = :eventId and b.status = :status")
    long sumQuantityByEventAndStatus(@Param("eventId") Integer eventId,
                                     @Param("status") BookingStatus status);
}