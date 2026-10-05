package com.udb.eventos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class BookingDtos {

    private BookingDtos() {
    }

    /** El cliente NO envia el total: lo calcula el service. */
    public record BookingRequest(
            @NotNull(message = "El evento es obligatorio")
            Integer eventId,

            @NotNull(message = "La cantidad es obligatoria")
            @Min(value = 1, message = "La cantidad mínima es 1")
            Integer quantity) {
    }

    public record BookingResponse(
            Integer idBooking,
            Integer eventId,
            String eventTitle,
            LocalDateTime eventDate,
            Integer quantity,
            BigDecimal totalAmount,
            LocalDateTime bookingDate,
            String status) {
    }
}