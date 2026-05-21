package com.innowise.userservice.model.dto.card;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

public record CardUpdateRequest(
        @Pattern(regexp = "^\\d{16}$", message = "Card number must be exactly 16 digits")
        String number,

        @Pattern(regexp = ".*\\S.*", message = "Holder name cannot be empty or consist only of spaces")
        String holder,

        @Future(message = "Expiration date must be in the future")
        LocalDate expirationDate
) {}