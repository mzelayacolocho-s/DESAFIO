package com.udb.eventos.model;

/**
 * Roles de la aplicacion. En Spring Security la autoridad se escribe con el
 * prefijo ROLE_ (ROLE_USER, ROLE_ADMIN); asi hasRole('ADMIN') funciona.
 */
public enum Role {
    USER,
    ADMIN
}