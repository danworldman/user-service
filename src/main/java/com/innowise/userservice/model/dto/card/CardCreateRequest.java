package com.innowise.userservice.model.dto.card;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record CardCreateRequest(
        @NotNull(message = "User ID is required")
        Long userId,

        @NotBlank(message = "Card number is required")
        @Pattern(regexp = "^[0-9]{16}$", message = "Card number must be exactly 16 digits")
        String number,

        @NotBlank(message = "Card holder name is required")
        String holder,

        @NotNull(message = "Expiration date is required")
        @Future(message = "Expiration date must be in the future")
        LocalDate expirationDate
) {}