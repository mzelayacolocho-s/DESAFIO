package com.udb.eventos.service;

import com.udb.eventos.dto.UserResponse;
import com.udb.eventos.model.User;
import com.udb.eventos.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(UserService::toResponse).toList();
    }

    public static UserResponse toResponse(User u) {
        return new UserResponse(u.getIdUser(), u.getUsername(), u.getFirstname(),
                u.getLastname(), u.getAge(), u.getRole().name());
    }
}