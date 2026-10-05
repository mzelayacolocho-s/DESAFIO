package com.udb.eventos.controller;

import com.udb.eventos.dto.AuthDtos.AuthResponse;
import com.udb.eventos.dto.AuthDtos.LoginRequest;
import com.udb.eventos.dto.AuthDtos.RefreshRequest;
import com.udb.eventos.dto.AuthDtos.RegisterRequest;
import com.udb.eventos.dto.UserResponse;
import com.udb.eventos.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Registro, login y refresh token (públicos)")
@SecurityRequirements // sin candado en Swagger: estos endpoints no requieren token
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Registrar nuevo usuario")
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @Operation(summary = "Login: retorna JWT + refresh token")
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @Operation(summary = "Obtener un nuevo JWT usando el refresh token")
    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request);
    }
}