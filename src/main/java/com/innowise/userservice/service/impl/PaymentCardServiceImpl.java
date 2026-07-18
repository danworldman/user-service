package com.innowise.userservice.service.impl;

import com.innowise.userservice.exception.MaxNumberOfPaymentCardException;
import com.innowise.userservice.exception.ResourceNotFoundException;
import com.innowise.userservice.mapper.PaymentCardMapper;
import com.innowise.userservice.model.dto.card.CardCreateRequest;
import com.innowise.userservice.model.dto.card.CardResponse;
import com.innowise.userservice.model.dto.card.CardUpdateRequest;
import com.innowise.userservice.model.entity.PaymentCard;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.dao.PaymentCardDAO;
import com.innowise.userservice.dao.UserDAO;
import com.innowise.userservice.dao.specification.PaymentCardSpecification;
import com.innowise.userservice.service.PaymentCardService;
import lombok.AllArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class PaymentCardServiceImpl implements PaymentCardService {

    private static final String CARD_NOT_FOUND_MESSAGE = "Payment card not found with id: ";

    private final PaymentCardDAO paymentCardDAO;
    private final PaymentCardMapper paymentCardMapper;
    private final UserDAO userDAO;
    private final CacheManager cacheManager;

    @Override
    @Transactional(readOnly = true)
    public CardResponse getById(Long id) {
        validateId(id);

        PaymentCard paymentCard = paymentCardDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CARD_NOT_FOUND_MESSAGE + id));

        return paymentCardMapper.toDto(paymentCard);
    }

    @Override
    @CacheEvict(value = "userWithCards", key = "#result.userId()")
    public CardResponse create(CardCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (request.userId() == null) {
            throw new IllegalArgumentException("User's id cannot be null");
        }

        User user = userDAO.findUsersWithPaymentCards(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.userId()));

        if (paymentCardDAO.countByUserId(user.getId()) >= 5) {
            throw new MaxNumberOfPaymentCardException("User already has 5 payment cards");
        }

        PaymentCard paymentCard = paymentCardMapper.toEntity(request);
        paymentCard.setUser(user);
        PaymentCard savedPaymentCard = paymentCardDAO.save(paymentCard);
        return paymentCardMapper.toDto(savedPaymentCard);
    }

    @Override
    @CacheEvict(value = "userWithCards", key = "#result.userId()")
    @Transactional
    public CardResponse update(Long id, CardUpdateRequest request) {
        validateId(id);

        PaymentCard paymentCard = paymentCardDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CARD_NOT_FOUND_MESSAGE + id));

        paymentCardMapper.updateEntity(request, paymentCard);
        return paymentCardMapper.toDto(paymentCard);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        validateId(id);

        PaymentCard paymentCard = paymentCardDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CARD_NOT_FOUND_MESSAGE + id));

        Long userId = paymentCard.getUser().getId();
        paymentCardDAO.delete(paymentCard);
        evictUserCache(userId);
    }

    @Override
    @Transactional
    public void activatePaymentCardStatus(Long id) {
        validateId(id);

        PaymentCard paymentCard = paymentCardDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CARD_NOT_FOUND_MESSAGE + id));

        Long userId = paymentCard.getUser().getId();
        paymentCard.setActive(true);
        paymentCardDAO.save(paymentCard);
        evictUserCache(userId);
    }

    @Override
    @Transactional
    public void deactivatePaymentCardStatus(Long id) {
        validateId(id);

        PaymentCard paymentCard = paymentCardDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CARD_NOT_FOUND_MESSAGE + id));

        Long userId = paymentCard.getUser().getId();
        paymentCard.setActive(false);
        paymentCardDAO.save(paymentCard);
        evictUserCache(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardResponse> getCardsByUserId(Long userId, Pageable pageable) {
        validateId(userId);

        if (!userDAO.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        Page<PaymentCard> paymentCardPage = paymentCardDAO.findByUserId(userId, pageable);
        return paymentCardPage.map(paymentCardMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardResponse> getActivePaymentCardsByUserId(Long userId) {
        validateId(userId);
        if (!userDAO.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return paymentCardDAO.findActivePaymentCardsByUserId(userId)
                .stream()
                .map(paymentCardMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardResponse> getAllCards(Long userId, Boolean active, String holder, String name, String surname,
                                          Pageable pageable) {
        Specification<PaymentCard> specification = Specification
                .where(PaymentCardSpecification.hasUserId(userId))
                .and(PaymentCardSpecification.isActive(active))
                .and(PaymentCardSpecification.hasHolder(holder))
                .and(PaymentCardSpecification.hasUserName(name))
                .and(PaymentCardSpecification.hasUserSurName(surname));

        Page<PaymentCard> paymentCardPagePage = paymentCardDAO.findAll(specification, pageable);

        return paymentCardPagePage.map(paymentCardMapper::toDto);
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