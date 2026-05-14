package com.innowise.userservice.model.dto.user;

import jakarta.annotation.Nullable;

import java.time.LocalDate;

public record UserUpdateRequest(
        @Nullable
        String name,
        @Nullable
        String surname,
        @Nullable
        String email,
        @Nullable
        LocalDate birthdate
) {}