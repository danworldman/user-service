package com.innowise.userservice.controller;

import com.innowise.userservice.model.dto.card.CardCreateRequest;
import com.innowise.userservice.model.dto.card.CardResponse;
import com.innowise.userservice.model.dto.card.CardUpdateRequest;
import com.innowise.userservice.service.PaymentCardService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class PaymentCardController {

    private final PaymentCardService paymentCardService;

    @GetMapping("/{id}")
    public ResponseEntity<CardResponse> getById(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(paymentCardService.getById(id));
    }

    @PostMapping
    public ResponseEntity<CardResponse> create(@Valid @RequestBody CardCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentCardService.create(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CardResponse> update(@PathVariable @Positive Long id,
                                               @Valid @RequestBody CardUpdateRequest request) {
        return ResponseEntity.ok(paymentCardService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long id) {
        paymentCardService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable @Positive Long id) {
        paymentCardService.activatePaymentCardStatus(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable @Positive Long id) {
        paymentCardService.deactivatePaymentCardStatus(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<CardResponse>> getCardsByUser(
            @PathVariable @Positive Long userId,
            @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(paymentCardService.getCardsByUserId(userId, pageable));
    }

    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<CardResponse>> getActiveCardsByUser(@PathVariable @Positive Long userId) {
        return ResponseEntity.ok(paymentCardService.getActivePaymentCardsByUserId(userId));
    }
}