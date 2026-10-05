package com.udb.eventos.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank(message = "El username es obligatorio")
            @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
            String username,

            @NotBlank(message = "La contraseña es obligatoria")
            @Size(min = 6, max = 100, message = "La contraseña debe tener al menos 6 caracteres")
            String password,

            @NotBlank(message = "El nombre es obligatorio")
            String firstname,

            @NotBlank(message = "El apellido es obligatorio")
            String lastname,

            @NotNull(message = "La edad es obligatoria")
            @Min(value = 1, message = "La edad debe ser mayor a 0")
            @Max(value = 120, message = "La edad no es válida")
            Integer age) {
    }

    public record LoginRequest(
            @NotBlank(message = "El username es obligatorio") String username,
            @NotBlank(message = "La contraseña es obligatoria") String password) {
    }

    public record RefreshRequest(
            @NotBlank(message = "El refresh token es obligatorio") String refreshToken) {
    }

    public record AuthResponse(
            String accessToken,
            String refreshToken,
            String tokenType,
            String username,
            String role) {
    }
}