package com.udb.eventos.service;

import com.udb.eventos.dto.BookingDtos.BookingRequest;
import com.udb.eventos.dto.BookingDtos.BookingResponse;
import com.udb.eventos.exception.BadRequestException;
import com.udb.eventos.exception.ResourceNotFoundException;
import com.udb.eventos.model.Booking;
import com.udb.eventos.model.BookingStatus;
import com.udb.eventos.model.Event;
import com.udb.eventos.model.User;
import com.udb.eventos.repository.BookingRepository;
import com.udb.eventos.repository.EventRepository;
import com.udb.eventos.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository,
                          EventRepository eventRepository,
                          UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    /**
     * Crea una reserva:
     *  1) toma al usuario del JWT (SecurityContext),
     *  2) bloquea el evento para evitar sobreventa,
     *  3) valida cupos disponibles,
     *  4) calcula total_amount = quantity * price_per_ticket.
     */
    @Transactional
    public BookingResponse create(BookingRequest req) {
        User user = currentUser();

        Event event = eventRepository.findByIdForUpdate(req.eventId())
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id " + req.eventId()));

        // Solo las reservas CONFIRMED ocupan cupo; las canceladas liberan lugares.
        long reserved = bookingRepository.sumQuantityByEventAndStatus(event.getIdEvent(), BookingStatus.CONFIRMED);
        long available = event.getCapacity() - reserved;

        if (req.quantity() > available) {
            throw new BadRequestException("No hay cupos suficientes. Entradas disponibles: " + available);
        }

        Booking booking = new Booking();
        booking.setEvent(event);
        booking.setUser(user);
        booking.setQuantity(req.quantity());
        booking.setTotalAmount(event.getPricePerTicket().multiply(BigDecimal.valueOf(req.quantity())));
        booking.setBookingDate(LocalDateTime.now());
        booking.setStatus(BookingStatus.CONFIRMED);

        return toResponse(bookingRepository.save(booking));
    }

    /** Solo las reservas del usuario autenticado (se obtiene del SecurityContext). */
    @Transactional(readOnly = true)
    public List<BookingResponse> findMine() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return bookingRepository.findByUserUsernameOrderByBookingDateDesc(username)
                .stream().map(this::toResponse).toList();
    }

    /** No borra el registro: cambia el status a CANCELLED. */
    @Transactional
    public BookingResponse cancel(Integer id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada con id " + id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isOwner = booking.getUser().getUsername().equals(auth.getName());
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Solo puedes cancelar tus propias reservas");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("La reserva ya está cancelada");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        return toResponse(bookingRepository.save(booking));
    }

    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
    }

    private BookingResponse toResponse(Booking b) {
        return new BookingResponse(b.getIdBooking(), b.getEvent().getIdEvent(), b.getEvent().getTitle(),
                b.getEvent().getEventDate(), b.getQuantity(), b.getTotalAmount(),
                b.getBookingDate(), b.getStatus().name());
    }
}