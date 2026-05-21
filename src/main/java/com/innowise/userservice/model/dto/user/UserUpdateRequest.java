package com.innowise.userservice.model.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

public record UserUpdateRequest(
        @Pattern(regexp = ".*\\S.*", message = "Name cannot be empty or consist only of spaces")
        String name,

        @Pattern(regexp = ".*\\S.*", message = "Surname cannot be empty or consist only of spaces")
        String surname,

        @Email(message = "Invalid email format")
        @Pattern(regexp = ".*\\S.*", message = "Email cannot be empty or consist only of spaces")
        String email,

        @Past(message = "Birth date must be in the past")
        LocalDate birthDate
) {}