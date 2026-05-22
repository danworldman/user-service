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
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class PaymentCardServiceImpl implements PaymentCardService {

    private static final String CARD_NOT_FOUND_MESSAGE = "Payment card not found with id: ";

    private final PaymentCardRepository paymentCardRepository;
    private final PaymentCardMapper paymentCardMapper;
    private final UserRepository userRepository;
    private final CacheManager cacheManager;

    @Override
    @Transactional(readOnly = true)
    public CardResponse getById(Long id) {
        validateId(id);

        PaymentCard paymentCard = paymentCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CARD_NOT_FOUND_MESSAGE + id));

        return paymentCardMapper.toDto(paymentCard);
    }

    @Override
    @CacheEvict(value = "userWithCards", key = "#result.userId()")
    @Transactional
    public CardResponse create(CardCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (request.userId() == null) {
            throw new IllegalArgumentException("User's id cannot be null");
        }

        User user = userRepository.findUsersWithPaymentCards(request.userId())
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
    @CacheEvict(value = "userWithCards", key = "#result.userId()")
    @Transactional
    public CardResponse update(Long id, CardUpdateRequest request) {
        validateId(id);

        PaymentCard paymentCard = paymentCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CARD_NOT_FOUND_MESSAGE + id));

        paymentCardMapper.updateEntity(request, paymentCard);
        paymentCardRepository.flush();

        return paymentCardMapper.toDto(paymentCard);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        validateId(id);

        PaymentCard paymentCard = paymentCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CARD_NOT_FOUND_MESSAGE + id));

        Long userId = paymentCard.getUser().getId();
        paymentCardRepository.delete(paymentCard);
        evictUserCache(userId);
    }

    @Override
    @Transactional
    public void activatePaymentCardStatus(Long id) {
        validateId(id);

        PaymentCard paymentCard = paymentCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CARD_NOT_FOUND_MESSAGE + id));

        Long userId = paymentCard.getUser().getId();
        int updated = paymentCardRepository.updateCardActiveStatus(id, true);
        if (updated == 0) {
            throw new ResourceNotFoundException(CARD_NOT_FOUND_MESSAGE + id);
        }
        evictUserCache(userId);
    }

    @Override
    @Transactional
    public void deactivatePaymentCardStatus(Long id) {
        validateId(id);

        PaymentCard paymentCard = paymentCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CARD_NOT_FOUND_MESSAGE + id));

        Long userId = paymentCard.getUser().getId();
        int updated = paymentCardRepository.updateCardActiveStatus(id, false);
        if (updated == 0) {
            throw new ResourceNotFoundException(CARD_NOT_FOUND_MESSAGE + id);
        }
        evictUserCache(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardResponse> getCardsByUserId(Long userId, Pageable pageable) {
        validateId(userId);

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        Page<PaymentCard> paymentCardPage = paymentCardRepository.findByUserId(userId, pageable);
        return paymentCardPage.map(paymentCardMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardResponse> getActivePaymentCardsByUserId(Long userId) {
        validateId(userId);
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return paymentCardRepository.findActivePaymentCardsByUserId(userId)
                .stream()
                .map(paymentCardMapper::toDto)
                .toList();
    }

    private void validateId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
        if (id <= 0) {
            throw new IllegalArgumentException("Id must be positive");
        }
    }

    private void evictUserCache(Long userId) {
        Cache cache = cacheManager.getCache("userWithCards");
        if (cache != null) {
            cache.evict(userId);
        }
    }
}