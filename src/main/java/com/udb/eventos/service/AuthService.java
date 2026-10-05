package com.udb.eventos.service;

import com.udb.eventos.dto.AuthDtos.AuthResponse;
import com.udb.eventos.dto.AuthDtos.LoginRequest;
import com.udb.eventos.dto.AuthDtos.RefreshRequest;
import com.udb.eventos.dto.AuthDtos.RegisterRequest;
import com.udb.eventos.dto.UserResponse;
import com.udb.eventos.exception.BadRequestException;
import com.udb.eventos.model.RefreshToken;
import com.udb.eventos.model.Role;
import com.udb.eventos.model.User;
import com.udb.eventos.repository.RefreshTokenRepository;
import com.udb.eventos.repository.UserRepository;
import com.udb.eventos.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final long refreshExpirationMs;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       @Value("${app.jwt.refresh-expiration-ms}") long refreshExpirationMs) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    /** Registro publico: siempre crea usuarios con rol USER. */
    @Transactional
    public UserResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new BadRequestException("El username '" + req.username() + "' ya está en uso");
        }

        User user = new User();
        user.setUsername(req.username());
        user.setPassword(passwordEncoder.encode(req.password())); // BCrypt
        user.setFirstname(req.firstname());
        user.setLastname(req.lastname());
        user.setAge(req.age());
        user.setRole(Role.USER);

        return UserService.toResponse(userRepository.save(user));
    }

    /** Valida credenciales (lanza BadCredentialsException si son incorrectas) y emite tokens. */
    @Transactional
    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password()));

        User user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado"));
        return issueTokens(user);
    }

    /** Intercambia un refresh token valido por un nuevo access token (y rota el refresh token). */
    @Transactional
    public AuthResponse refresh(RefreshRequest req) {
        RefreshToken stored = refreshTokenRepository.findByToken(req.refreshToken())
                .orElseThrow(() -> new BadRequestException("Refresh token inválido"));

        if (stored.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(stored);
            throw new BadRequestException("El refresh token expiró. Inicia sesión nuevamente.");
        }

        User user = stored.getUser();
        refreshTokenRepository.delete(stored); // rotacion: el token usado ya no sirve
        return issueTokens(user);
    }

    /** Primer acceso con GitHub: se crea el usuario; despues solo se reutiliza. */
    @Transactional
    public AuthResponse loginWithGithub(String githubLogin, String githubName) {
        String username = "github_" + githubLogin;

        User user = userRepository.findByUsername(username).orElseGet(() -> {
            User u = new User();
            u.setUsername(username);
            u.setFirstname(githubName != null && !githubName.isBlank() ? githubName : githubLogin);
            u.setLastname("(GitHub)");
            u.setAge(0);
            // No tiene contraseña real: se guarda una aleatoria cifrada.
            u.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            u.setRole(Role.USER);
            return userRepository.save(u);
        });

        return issueTokens(user);
    }

    private AuthResponse issueTokens(User user) {
        String access = jwtService.generateToken(user.getUsername(), user.getRole().name());

        RefreshToken refresh = new RefreshToken();
        refresh.setToken(UUID.randomUUID().toString());
        refresh.setUser(user);
        refresh.setExpiryDate(Instant.now().plusMillis(refreshExpirationMs));
        refreshTokenRepository.save(refresh);

        return new AuthResponse(access, refresh.getToken(), "Bearer",
                user.getUsername(), user.getRole().name());
    }
}