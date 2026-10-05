package com.udb.eventos.controller;

import com.udb.eventos.dto.BookingDtos.BookingRequest;
import com.udb.eventos.dto.BookingDtos.BookingResponse;
import com.udb.eventos.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Reservas", description = "Reservas del usuario autenticado")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Operation(summary = "Crear reserva (el total se calcula automáticamente)")
    @PostMapping
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody BookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.create(request));
    }

    @Operation(summary = "Listar las reservas del usuario autenticado")
    @GetMapping("/my")
    public List<BookingResponse> myBookings() {
        return bookingService.findMine();
    }

    @Operation(summary = "Cancelar una reserva (cambia el status a CANCELLED)")
    @DeleteMapping("/{id}")
    public BookingResponse cancel(@PathVariable Integer id) {
        return bookingService.cancel(id);
    }
}