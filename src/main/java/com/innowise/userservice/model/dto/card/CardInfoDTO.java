package com.innowise.userservice.model.dto.card;

public record CardInfoDTO(
        Long id,
        String number,
        String holder,
        boolean isActive
) {}