package com.udb.eventos.dto;

/** Nunca se expone la contraseña. */
public record UserResponse(
        Integer idUser,
        String username,
        String firstname,
        String lastname,
        Integer age,
        String role) {
}