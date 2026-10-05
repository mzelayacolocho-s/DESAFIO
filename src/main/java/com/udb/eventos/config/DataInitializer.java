package com.udb.eventos.config;

import com.udb.eventos.model.Event;
import com.udb.eventos.model.Role;
import com.udb.eventos.model.User;
import com.udb.eventos.repository.EventRepository;
import com.udb.eventos.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Datos de arranque: un administrador y algunos eventos de ejemplo. */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedData(UserRepository userRepository,
                               EventRepository eventRepository,
                               PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setFirstname("Administrador");
                admin.setLastname("Sistema");
                admin.setAge(30);
                admin.setRole(Role.ADMIN);
                userRepository.save(admin);
            }

            if (eventRepository.count() == 0) {
                eventRepository.save(event("Concierto de Rock", "Noche de rock con bandas locales",
                        LocalDateTime.now().plusDays(30), "Estadio Nacional", 100, "25.00"));
                eventRepository.save(event("Conferencia de Tecnología", "Charlas sobre desarrollo web e IA",
                        LocalDateTime.now().plusDays(45), "Universidad Don Bosco", 50, "10.50"));
                eventRepository.save(event("Feria Gastronómica", "Comida típica y food trucks",
                        LocalDateTime.now().plusDays(60), "Plaza Gerardo Barrios", 200, "0.00"));
            }
        };
    }

    private Event event(String title, String description, LocalDateTime date,
                        String venue, int capacity, String price) {
        Event e = new Event();
        e.setTitle(title);
        e.setDescription(description);
        e.setEventDate(date.withNano(0));
        e.setVenue(venue);
        e.setCapacity(capacity);
        e.setPricePerTicket(new BigDecimal(price));
        return e;
    }
}