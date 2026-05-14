package com.innowise.userservice.model.dto.user;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserResponse(
        long id,
        String name,
        String surname,
        String email,
        boolean isActive,
        LocalDate birthdate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}