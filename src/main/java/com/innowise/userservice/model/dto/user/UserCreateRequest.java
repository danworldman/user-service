package com.innowise.userservice.model.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;

public record UserCreateRequest(
        @NotBlank(message = "Name is required and cannot be blank")
        String name,

        @NotBlank(message = "Surname is required and cannot be blank")
        String surname,

        @NotBlank(message = "Email is required and cannot be blank")
        @Email(message = "Invalid email format")
        String email,

        @NotNull(message = "Birth date is required")
        @Past(message = "Birth date must be in the past")
        LocalDate birthDate
) {}