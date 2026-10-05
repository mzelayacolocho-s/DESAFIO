package com.udb.eventos.service;

import com.udb.eventos.dto.EventDtos.EventRequest;
import com.udb.eventos.dto.EventDtos.EventResponse;
import com.udb.eventos.dto.PageResponse;
import com.udb.eventos.exception.BadRequestException;
import com.udb.eventos.exception.ResourceNotFoundException;
import com.udb.eventos.model.BookingStatus;
import com.udb.eventos.model.Event;
import com.udb.eventos.repository.BookingRepository;
import com.udb.eventos.repository.EventRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;

    public EventService(EventRepository eventRepository, BookingRepository bookingRepository) {
        this.eventRepository = eventRepository;
        this.bookingRepository = bookingRepository;
    }

    /** Listado paginado (Spring Data Pageable: ?page=0&size=10&sort=eventDate,asc). */
    @Transactional(readOnly = true)
    public PageResponse<EventResponse> findAll(Pageable pageable) {
        return PageResponse.from(eventRepository.findAll(pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public EventResponse findById(Integer id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public EventResponse create(EventRequest req) {
        Event event = new Event();
        apply(event, req);
        return toResponse(eventRepository.save(event));
    }

    @Transactional
    public EventResponse update(Integer id, EventRequest req) {
        Event event = getOrThrow(id);

        long reserved = bookingRepository.sumQuantityByEventAndStatus(id, BookingStatus.CONFIRMED);
        if (req.capacity() < reserved) {
            throw new BadRequestException("La capacidad no puede ser menor a las entradas ya reservadas ("
                    + reserved + ")");
        }

        apply(event, req);
        return toResponse(eventRepository.save(event));
    }

    @Transactional
    public void delete(Integer id) {
        Event event = getOrThrow(id);
        if (bookingRepository.existsByEvent_IdEvent(id)) {
            throw new BadRequestException("No se puede eliminar un evento que ya tiene reservas");
        }
        eventRepository.delete(event);
    }

    private Event getOrThrow(Integer id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id " + id));
    }

    private void apply(Event event, EventRequest req) {
        event.setTitle(req.title());
        event.setDescription(req.description());
        event.setEventDate(req.eventDate());
        event.setVenue(req.venue());
        event.setCapacity(req.capacity());
        event.setPricePerTicket(req.pricePerTicket());
    }

    private EventResponse toResponse(Event e) {
        long reserved = bookingRepository.sumQuantityByEventAndStatus(e.getIdEvent(), BookingStatus.CONFIRMED);
        return new EventResponse(e.getIdEvent(), e.getTitle(), e.getDescription(), e.getEventDate(),
                e.getVenue(), e.getCapacity(), e.getPricePerTicket(), e.getCapacity() - reserved);
    }
}