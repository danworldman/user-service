package com.innowise.userservice.service.impl;

import com.innowise.userservice.exception.MaxNumberOfPaymentCardException;
import com.innowise.userservice.exception.ResourceNotFoundException;
import com.innowise.userservice.mapper.PaymentCardMapper;
import com.innowise.userservice.model.dto.card.CardCreateRequest;
import com.innowise.userservice.model.dto.card.CardResponse;
import com.innowise.userservice.model.dto.card.CardUpdateRequest;
import com.innowise.userservice.model.entity.PaymentCard;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.PaymentCardService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class PaymentServiceImpl implements PaymentCardService {

    private final PaymentCardRepository paymentCardRepository;
    private final PaymentCardMapper paymentCardMapper;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CardResponse create(CardCreateRequest request) {
        if (request.userId() == null) {
            throw new IllegalArgumentException("User's id cannot be null");
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.userId()));

        if (paymentCardRepository.countByUserId(user.getId()) >= 5) {
            throw new MaxNumberOfPaymentCardException("User already has 5 payment cards");
        }

        PaymentCard paymentCard = paymentCardMapper.toEntity(request);
        paymentCard.setUser(user);
        PaymentCard savedPaymentCard = paymentCardRepository.save(paymentCard);
        return paymentCardMapper.toDto(savedPaymentCard);
    }

    @Override
    @Transactional(readOnly = true)
    public CardResponse getById(Long id) {
        PaymentCard paymentCard = paymentCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment card not found with id: " + id));

        return paymentCardMapper.toDto(paymentCard);
    }

    @Override
    @Transactional
    public CardResponse update(Long id, CardUpdateRequest request) {
        PaymentCard paymentCard = paymentCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment card not found with id: " + id));

        paymentCardMapper.updateEntity(request, paymentCard);

        return paymentCardMapper.toDto(paymentCard);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        PaymentCard paymentCard = paymentCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment card not found with id: " + id));

        paymentCardRepository.delete(paymentCard);
    }

    @Override
    @Transactional
    public void activatePaymentCardStatus(Long id) {
        int resultOfUpdate = paymentCardRepository.updateCardActiveStatus(id, true);
        if (resultOfUpdate == 0) {
            throw new ResourceNotFoundException("Payment card not found with id: " + id);
        }
    }

    @Override
    @Transactional
    public void deactivatePaymentCardStatus(Long id) {
        int resultOfUpdate = paymentCardRepository.updateCardActiveStatus(id, false);
        if (resultOfUpdate == 0) {
            throw new ResourceNotFoundException("Payment card not found with id: " + id);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardResponse> getCardsByUserId(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        Page<PaymentCard> paymentCardPage = paymentCardRepository.findByUserId(userId, pageable);

        return paymentCardPage.map(paymentCardMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardResponse> getActivePaymentCardsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return paymentCardRepository.findActivePaymentCardsByUserId(userId)
                .stream()
                .map(paymentCardMapper::toDto)
                .toList();
    }
}