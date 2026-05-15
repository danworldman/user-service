package com.innowise.userservice.model.dto.card;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CardCreateRequest(
        @NotNull
        Long userId,
        @NotBlank
        String number,
        @NotBlank
        String holder,
        @NotNull
        @Future
        LocalDate expirationDate
) {}