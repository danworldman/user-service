package com.innowise.userservice.controller;

import com.innowise.userservice.model.dto.card.CardCreateRequest;
import com.innowise.userservice.model.dto.card.CardResponse;
import com.innowise.userservice.model.dto.card.CardUpdateRequest;
import com.innowise.userservice.service.PaymentCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class PaymentCardController {

    private final PaymentCardService paymentCardService;

    @PostMapping
    public CardResponse create(@Valid @RequestBody CardCreateRequest request) {
        return paymentCardService.create(request);
    }

    @GetMapping("/{id}")
    public CardResponse getById(@PathVariable Long id) {
        return paymentCardService.getById(id);
    }

    @PatchMapping("/{id}")
    public CardResponse update(@PathVariable Long id, @Valid @RequestBody CardUpdateRequest request) {
        return paymentCardService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        paymentCardService.delete(id);
    }

    @PatchMapping("/{id}/activate")
    public void activate(@PathVariable Long id) {
        paymentCardService.activatePaymentCardStatus(id);
    }

    @PatchMapping("/{id}/deactivate")
    public void deactivate(@PathVariable Long id) {
        paymentCardService.deactivatePaymentCardStatus(id);
    }

    @GetMapping("/user/{userId}")
    public Page<CardResponse> getCardsByUser(
            @PathVariable Long userId,
            @PageableDefault(size = 10) Pageable pageable) {
        return paymentCardService.getCardsByUserId(userId, pageable);
    }

    @GetMapping("/user/{userId}/active")
    public List<CardResponse> getActiveCardsByUser(@PathVariable Long userId) {
        return paymentCardService.getActivePaymentCardsByUserId(userId);
    }
}