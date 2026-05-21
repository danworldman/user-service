package com.innowise.userservice.model.dto.card;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record CardUpdateRequest(
        @Pattern(regexp = "^[0-9]{16}$", message = "Card number must be exactly 16 digits")
        String number,

        String holder,

        @Future(message = "Expiration date must be in the future")
        LocalDate expirationDate
) {}