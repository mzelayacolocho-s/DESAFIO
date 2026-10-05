package com.udb.eventos.security;

import com.udb.eventos.dto.AuthDtos.AuthResponse;
import com.udb.eventos.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * Se ejecuta cuando GitHub autentica al usuario. Crea el usuario en la BD si es
 * su primer acceso, genera el JWT propio de la aplicacion y redirige al
 * frontend con los tokens.
 */
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final String frontendUrl;

    public OAuth2SuccessHandler(AuthService authService,
                                @Value("${app.frontend-url}") String frontendUrl) {
        this.authService = authService;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        String login = oauthUser.getAttribute("login");   // usuario de GitHub
        String name = oauthUser.getAttribute("name");     // nombre completo (puede ser null)

        AuthResponse tokens = authService.loginWithGithub(login, name);

        String redirect = UriComponentsBuilder.fromUriString(frontendUrl)
                .queryParam("token", tokens.accessToken())
                .queryParam("refresh", tokens.refreshToken())
                .build().toUriString();

        response.sendRedirect(redirect);
    }
}