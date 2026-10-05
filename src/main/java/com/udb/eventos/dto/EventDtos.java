package com.udb.eventos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class EventDtos {

    private EventDtos() {
    }

    public record EventRequest(
            @NotBlank(message = "El título es obligatorio")
            @Size(max = 255, message = "El título no puede superar 255 caracteres")
            String title,

            String description,

            @NotNull(message = "La fecha del evento es obligatoria")
            LocalDateTime eventDate,

            @NotBlank(message = "El lugar es obligatorio")
            @Size(max = 255, message = "El lugar no puede superar 255 caracteres")
            String venue,

            @NotNull(message = "La capacidad es obligatoria")
            @Min(value = 1, message = "La capacidad debe ser mayor a 0")
            Integer capacity,

            @NotNull(message = "El precio por entrada es obligatorio")
            @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
            BigDecimal pricePerTicket) {
    }

    public record EventResponse(
            Integer idEvent,
            String title,
            String description,
            LocalDateTime eventDate,
            String venue,
            Integer capacity,
            BigDecimal pricePerTicket,
            long availableSeats) {
    }
}