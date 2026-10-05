package com.udb.eventos.controller;

import com.udb.eventos.dto.EventDtos.EventRequest;
import com.udb.eventos.dto.EventDtos.EventResponse;
import com.udb.eventos.dto.PageResponse;
import com.udb.eventos.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Eventos", description = "Lectura: cualquier usuario autenticado. Crear/editar/eliminar: solo ADMIN")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @Operation(summary = "Listar eventos (paginado: page, size, sort)")
    @GetMapping
    public PageResponse<EventResponse> list(
            @ParameterObject @PageableDefault(size = 10, sort = "eventDate") Pageable pageable) {
        return eventService.findAll(pageable);
    }

    @Operation(summary = "Obtener evento por ID")
    @GetMapping("/{id}")
    public EventResponse getById(@PathVariable Integer id) {
        return eventService.findById(id);
    }

    @Operation(summary = "Crear nuevo evento (ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<EventResponse> create(@Valid @RequestBody EventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.create(request));
    }

    @Operation(summary = "Actualizar un evento (ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public EventResponse update(@PathVariable Integer id, @Valid @RequestBody EventRequest request) {
        return eventService.update(id, request);
    }

    @Operation(summary = "Eliminar un evento (ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }
}