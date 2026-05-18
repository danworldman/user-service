package com.innowise.userservice.model.dto.card;

import java.util.List;

public record UserWithCardsDTO(
        Long id,
        String name,
        String surname,
        String email,
        boolean isActive,
        List<CardInfoDTO> cards
) {}