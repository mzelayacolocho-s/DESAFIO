package com.udb.eventos.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Define el esquema "bearerAuth" (HTTP Bearer / JWT). Con esto Swagger UI
 * muestra el boton Authorize para pegar el token. El requisito de seguridad
 * se aplica a toda la API; AuthController lo quita para los endpoints publicos.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API Sistema de Reservas de Eventos",
                version = "1.0",
                description = "Backend Spring Boot con JWT, refresh token, OAuth2 (GitHub) y roles USER/ADMIN"),
        security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER,
        description = "Pega aquí el accessToken obtenido en /api/auth/login (sin la palabra Bearer)")
public class SwaggerConfig {
}