package com.innowise.userservice.model.dto.card;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Future;

import java.time.LocalDate;

public record CardUpdateRequest(
        @Nullable
        String number,
        @Nullable
        String holder,
        @Nullable
        @Future
        LocalDate expirationDate
) {}