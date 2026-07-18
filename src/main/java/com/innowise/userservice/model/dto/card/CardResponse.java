package com.innowise.userservice.model.dto.card;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CardResponse(
        Long id,
        Long userId,
        String number,
        String holder,
        LocalDate expirationDate,
        boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}