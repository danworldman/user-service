package com.innowise.userservice.model.dto.user;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        String surname,
        String email,
        boolean isActive,
        LocalDate birthDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}