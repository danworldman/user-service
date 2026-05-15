package com.innowise.userservice.service;

import com.innowise.userservice.model.dto.card.CardCreateRequest;
import com.innowise.userservice.model.dto.card.CardResponse;
import com.innowise.userservice.model.dto.card.CardUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PaymentCardService {
    CardResponse create(CardCreateRequest request);

    CardResponse getById(Long id);

    CardResponse update(Long id, CardUpdateRequest request);

    void delete(Long id);

    void activatePaymentCardStatus(Long id);

    void deactivatePaymentCardStatus(Long id);

    Page<CardResponse> getCardsByUserId(Long userId, Pageable pageable);

    List<CardResponse> getActivePaymentCardsByUserId(Long userId);
}