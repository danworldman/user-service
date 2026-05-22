package com.innowise.userservice.model.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;

public record UserUpdateRequest(
        String name,

        String surname,

        @Email(message = "Invalid email format")
        String email,

        @Past(message = "Birth date must be in the past")
        LocalDate birthDate
) {}