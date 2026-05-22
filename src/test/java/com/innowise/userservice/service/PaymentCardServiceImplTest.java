package com.innowise.userservice.service;

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
import com.innowise.userservice.service.impl.PaymentCardServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentCardServiceImplTest {

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @Mock
    private PaymentCardMapper paymentCardMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CacheManager cacheManager;

    @InjectMocks
    private PaymentCardServiceImpl paymentCardService;

    private User createUser(Long id, String name, String surname, String email, boolean isActive,
                            LocalDate birthDate, LocalDateTime createdAt, LocalDateTime updatedAt) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setSurname(surname);
        user.setEmail(email);
        user.setActive(isActive);
        user.setBirthDate(birthDate);
        user.setCreatedAt(createdAt);
        user.setUpdatedAt(updatedAt);

        return user;
    }

    private PaymentCard createPaymentCard(Long id, String number, String holder, LocalDate expirationDate,
                                          boolean isActive, LocalDateTime createdAt, LocalDateTime updateAt, User user) {
        PaymentCard paymentCard = new PaymentCard();
        paymentCard.setId(id);
        paymentCard.setNumber(number);
        paymentCard.setHolder(holder);
        paymentCard.setExpirationDate(expirationDate);
        paymentCard.setActive(isActive);
        paymentCard.setCreatedAt(createdAt);
        paymentCard.setUpdatedAt(updateAt);
        paymentCard.setUser(user);

        return paymentCard;
    }

    private CardResponse createCardResponse(Long id, Long userId, String number, String holder, LocalDate expirationDate,
                                            boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new CardResponse(
                id,
                userId,
                number,
                holder,
                expirationDate,
                isActive,
                createdAt,
                updatedAt
        );
    }

    private CardCreateRequest cardCreateRequest(Long userId, String number, String holder, LocalDate expirationDate) {
        return new CardCreateRequest(userId, number, holder, expirationDate);
    }

    @Test
    void getById_shouldReturnCardResponse_whenCardExists() {
        Long cardId = 1L;
        User user = createUser(1L, "Bob", "Duck", "bob@email.com",
                true, LocalDate.of(2000, 1, 1),
                LocalDateTime.of(2026, 5, 19, 3, 1),
                LocalDateTime.of(2026, 5, 19, 3, 1)
        );

        PaymentCard paymentCard = createPaymentCard(cardId, "1111-2222", "Bob Duck",
                LocalDate.of(2030, 1, 1), true,
                LocalDateTime.of(2026, 1, 5, 2, 3),
                LocalDateTime.of(2026, 1, 5, 2, 3), user
        );

        CardResponse cardResponse = createCardResponse(cardId, 1L, "1111-2222", "Bob Duck",
                LocalDate.of(2030, 1, 1), true,
                LocalDateTime.of(2026, 1, 5, 2, 3),
                LocalDateTime.of(2026, 1, 5, 2, 3)
        );

        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(paymentCard));
        when(paymentCardMapper.toDto(paymentCard)).thenReturn(cardResponse);

        CardResponse result = paymentCardService.getById(cardId);

        assertEquals(result, cardResponse);
        verify(paymentCardRepository).findById(cardId);
        verify(paymentCardMapper).toDto(paymentCard);
    }

    @Test
    void getById_shouldThrowResourceNotFoundException_whenCardDoesNotExist() {
        Long cardId = 1000L;

        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentCardService.getById(cardId));
        verify(paymentCardMapper, never()).toDto(any());
    }

    @Test
    void create_shouldReturnCardResponse_whenUserExists() {
        Long userId = 1L;
        User user = createUser(userId, "Bob", "Duck", "bob@email.com",
                true, LocalDate.of(2000, 1, 1),
                LocalDateTime.of(2026, 5, 19, 3, 1),
                LocalDateTime.of(2026, 5, 19, 3, 1)
        );

        CardCreateRequest cardCreateRequest = cardCreateRequest(userId, "1111-2222", "Bob Duck",
                LocalDate.of(2030, 1, 1)
        );

        PaymentCard paymentCard = createPaymentCard(1L, "1111-2222", "Bob Duck",
                LocalDate.of(2030, 1, 1), true,
                LocalDateTime.of(2026, 1, 5, 2, 3),
                LocalDateTime.of(2026, 1, 5, 2, 3), user
        );

        PaymentCard savedPaymentCard = createPaymentCard(1L, "1111-2222", "Bob Duck",
                LocalDate.of(2030, 1, 1), true,
                LocalDateTime.of(2026, 1, 5, 2, 3),
                LocalDateTime.of(2026, 1, 5, 2, 3), user
        );

        CardResponse cardResponse = createCardResponse(1L, 1L, "1111-2222", "Bob Duck",
                LocalDate.of(2030, 1, 1), true,
                LocalDateTime.of(2026, 1, 5, 2, 3),
                LocalDateTime.of(2026, 1, 5, 2, 3)
        );

        when(userRepository.findUsersWithPaymentCards(userId)).thenReturn(Optional.of(user));
        when(paymentCardRepository.countByUserId(userId)).thenReturn(3);
        when(paymentCardMapper.toEntity(cardCreateRequest)).thenReturn(paymentCard);
        when(paymentCardRepository.save(paymentCard)).thenReturn(savedPaymentCard);
        when(paymentCardMapper.toDto(savedPaymentCard)).thenReturn(cardResponse);

        CardResponse result = paymentCardService.create(cardCreateRequest);

        assertEquals(result, cardResponse);
        verify(userRepository).findUsersWithPaymentCards(userId);
        verify(paymentCardRepository).countByUserId(userId);
        verify(paymentCardMapper).toEntity(cardCreateRequest);
        verify(paymentCardRepository).save(paymentCard);
        verify(paymentCardMapper).toDto(savedPaymentCard);
    }

    @Test
    void create_shouldThrowMaxNumberOfPaymentCardException_whenUserHasAlreadyFiveCards() {
        Long userId = 1L;
        User user = createUser(userId, "Bob", "Duck", "bob@email.com",
                true, LocalDate.of(2000, 1, 1),
                LocalDateTime.of(2026, 5, 19, 3, 1),
                LocalDateTime.of(2026, 5, 19, 3, 1)
        );

        CardCreateRequest cardCreateRequest = cardCreateRequest(userId, "1111-2222", "Bob Duck",
                LocalDate.of(2030, 1, 1)
        );

        when(userRepository.findUsersWithPaymentCards(userId)).thenReturn(Optional.of(user));
        when(paymentCardRepository.countByUserId(userId)).thenReturn(5);

        assertThrows(MaxNumberOfPaymentCardException.class, () -> paymentCardService.create(cardCreateRequest));

        verify(userRepository).findUsersWithPaymentCards(userId);
        verify(paymentCardRepository).countByUserId(userId);
        verifyNoInteractions(paymentCardMapper);
        verify(paymentCardRepository, never()).save(any());
    }

    @Test
    void update_shouldReturnCardResponse_whenCardExists() {
        Long cardId = 1L;
        CardUpdateRequest cardUpdateRequest = new CardUpdateRequest("2222-3333", "Bob Duck",
                LocalDate.of(2030, 1, 1)
        );

        User user = createUser(1L, "Bob", "Duck", "bob@email.com",
                true, LocalDate.of(2000, 1, 1),
                LocalDateTime.of(2026, 5, 19, 3, 1),
                LocalDateTime.of(2026, 5, 19, 3, 1)
        );

        PaymentCard oldPaymentCard = createPaymentCard(1L, "1111-2222", "Bob Duck",
                LocalDate.of(2030, 1, 1), true,
                LocalDateTime.of(2026, 1, 5, 2, 3),
                LocalDateTime.of(2026, 1, 5, 2, 3), user
        );

        CardResponse newCardResponse = createCardResponse(1L, 1L, "2222-3333", "Bob Duck",
                LocalDate.of(2030, 1, 1), true,
                LocalDateTime.of(2026, 1, 5, 2, 3),
                LocalDateTime.of(2026, 1, 5, 2, 3)
        );

        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(oldPaymentCard));
        when(paymentCardMapper.toDto(oldPaymentCard)).thenReturn(newCardResponse);

        CardResponse result = paymentCardService.update(cardId, cardUpdateRequest);

        assertEquals(result, newCardResponse);
        verify(paymentCardRepository).findById(cardId);
        verify(paymentCardMapper).updateEntity(cardUpdateRequest, oldPaymentCard);
        verify(paymentCardMapper).toDto(oldPaymentCard);
    }

    @Test
    void update_shouldThrowResourceNotFoundException_whenCardDoesNotExist() {
        Long cardId = 10000L;
        CardUpdateRequest cardUpdateRequest = new CardUpdateRequest("2222-3333", "Bob Duck",
                LocalDate.of(2030, 1, 1));

        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentCardService.update(cardId, cardUpdateRequest));
    }

    @Test
    void delete_shouldDeleteCard_whenCardExists() {
        Long cardId = 1L;
        User user = createUser(1L, "Bob", "Duck", "bob@email.com",
                true, LocalDate.of(2000, 1, 1),
                LocalDateTime.of(2026, 5, 19, 3, 1),
                LocalDateTime.of(2026, 5, 19, 3, 1)
        );

        PaymentCard paymentCard = createPaymentCard(1L, "1111-2222", "Bob Duck",
                LocalDate.of(2030, 1, 1), true,
                LocalDateTime.of(2026, 1, 5, 2, 3),
                LocalDateTime.of(2026, 1, 5, 2, 3), user
        );


        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(paymentCard));
        Cache mockCache = mock(Cache.class);
        when(cacheManager.getCache("userWithCards")).thenReturn(mockCache);

        paymentCardService.delete(cardId);

        verify(paymentCardRepository).findById(cardId);
        verify(paymentCardRepository).delete(paymentCard);
        verify(mockCache).evict(user.getId());
    }

    @Test
    void delete_shouldThrowResourceNotFoundException_whenCardDoesNotExist() {
        Long cardId = 1000L;

        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentCardService.delete(cardId));

        verify(paymentCardRepository, never()).delete(any());
    }

    @Test
    void activatePaymentCardStatus_shouldActivate_whenCardExists() {
        Long cardId = 1L;
        User user = createUser(1L, "Bob", "Duck", "bob@email.com",
                true, LocalDate.of(2000, 1, 1),
                LocalDateTime.of(2026, 5, 19, 3, 1),
                LocalDateTime.of(2026, 5, 19, 3, 1)
        );

        PaymentCard paymentCard = createPaymentCard(1L, "1111-2222", "Bob Duck",
                LocalDate.of(2030, 1, 1), true,
                LocalDateTime.of(2026, 1, 5, 2, 3),
                LocalDateTime.of(2026, 1, 5, 2, 3), user
        );

        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(paymentCard));
        when(paymentCardRepository.updateCardActiveStatus(cardId, true)).thenReturn(1);
        Cache mockCache = mock(Cache.class);
        when(cacheManager.getCache("userWithCards")).thenReturn(mockCache);

        paymentCardService.activatePaymentCardStatus(cardId);

        verify(paymentCardRepository).findById(cardId);
        verify(paymentCardRepository).updateCardActiveStatus(cardId, true);
        verify(mockCache).evict(user.getId());
    }

    @Test
    void activatePaymentCardStatus_shouldThrowResourceNotFoundException_whenCardNotFound() {
        Long cardId = 1000L;

        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentCardService.activatePaymentCardStatus(cardId));
    }

    @Test
    void deactivatePaymentCardStatus_shouldDeactivate_whenCardExists() {
        Long cardId = 1L;
        User user = createUser(1L, "Bob", "Duck", "bob@email.com",
                true, LocalDate.of(2000, 1, 1),
                LocalDateTime.of(2026, 5, 19, 3, 1),
                LocalDateTime.of(2026, 5, 19, 3, 1)
        );

        PaymentCard paymentCard = createPaymentCard(1L, "1111-2222", "Bob Duck",
                LocalDate.of(2030, 1, 1), true,
                LocalDateTime.of(2026, 1, 5, 2, 3),
                LocalDateTime.of(2026, 1, 5, 2, 3), user
        );

        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(paymentCard));
        when(paymentCardRepository.updateCardActiveStatus(cardId, false)).thenReturn(1);
        Cache mockCache = mock(Cache.class);
        when(cacheManager.getCache("userWithCards")).thenReturn(mockCache);

        paymentCardService.deactivatePaymentCardStatus(cardId);

        verify(paymentCardRepository).findById(cardId);
        verify(paymentCardRepository).updateCardActiveStatus(cardId, false);
        verify(mockCache).evict(user.getId());
    }

    @Test
    void deactivatePaymentCardStatus_shouldThrowResourceNotFoundException_whenCardNotFound() {
        Long cardId = 1000L;

        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentCardService.deactivatePaymentCardStatus(cardId));
    }

    @Test
    void getCardsByUserId_shouldPageOfCardResponse_whenCardExists() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        User user = createUser(1L, "Bob", "Duck", "bob@email.com",
                true, LocalDate.of(2000, 1, 1),
                LocalDateTime.of(2026, 5, 19, 3, 1),
                LocalDateTime.of(2026, 5, 19, 3, 1)
        );

        PaymentCard PaymentCardOne = createPaymentCard(1L, "1111-2222", "Bob Duck",
                LocalDate.of(2030, 1, 1), true,
                LocalDateTime.of(2026, 1, 5, 2, 3),
                LocalDateTime.of(2026, 1, 5, 2, 3), user
        );

        PaymentCard PaymentCardTwo = createPaymentCard(2L, "3333-4444", "Bob Duck",
                LocalDate.of(2031, 1, 1), true,
                LocalDateTime.of(2026, 1, 5, 2, 3),
                LocalDateTime.of(2026, 1, 5, 2, 3), user
        );

        List<PaymentCard> cards = List.of(PaymentCardOne, PaymentCardTwo);
        Page<PaymentCard> cardPage = new PageImpl<>(cards, pageable, 2);

        CardResponse CardResponseNumberOne = createCardResponse(1L, userId, "1111-2222", "Bob Duck",
                LocalDate.of(2030, 1, 1), true,
                LocalDateTime.of(2026, 1, 5, 2, 3),
                LocalDateTime.of(2026, 1, 5, 2, 3)
        );

        CardResponse CardResponseNumberTwo = createCardResponse(2L, userId, "3333-4444", "Bob Duck",
                LocalDate.of(2031, 1, 1), true,
                LocalDateTime.of(2026, 1, 5, 2, 3),
                LocalDateTime.of(2026, 1, 5, 2, 3)
        );

        when(userRepository.existsById(userId)).thenReturn(true);
        when(paymentCardRepository.findByUserId(userId, pageable)).thenReturn(cardPage);
        when(paymentCardMapper.toDto(PaymentCardOne)).thenReturn(CardResponseNumberOne);
        when(paymentCardMapper.toDto(PaymentCardTwo)).thenReturn(CardResponseNumberTwo);

        Page<CardResponse> result = paymentCardService.getCardsByUserId(userId, pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(CardResponseNumberOne, result.getContent().get(0));
        assertEquals(CardResponseNumberTwo, result.getContent().get(1));
        verify(userRepository).existsById(userId);
        verify(paymentCardRepository).findByUserId(userId, pageable);
        verify(paymentCardMapper).toDto(PaymentCardOne);
        verify(paymentCardMapper).toDto(PaymentCardTwo);
    }

    @Test
    void getCardsByUserId_shouldReturnEmptyPage_whenNoCardsFound() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<PaymentCard> emptyPage = Page.empty(pageable);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(paymentCardRepository.findByUserId(userId, pageable)).thenReturn(emptyPage);

        Page<CardResponse> result = paymentCardService.getCardsByUserId(userId, pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository).existsById(userId);
        verify(paymentCardRepository).findByUserId(userId, pageable);
        verify(paymentCardMapper, never()).toDto(any());
    }

    @Test
    void getActiveCardsByUserId_shouldReturnListOfCardResponse() {
        Long userId = 1L;
        User user = createUser(userId, "Bob", "Duck", "bob@email.com",
                true, LocalDate.of(2000, 1, 1),
                LocalDateTime.of(2026, 5, 19, 3, 1),
                LocalDateTime.of(2026, 5, 19, 3, 1)
        );

        PaymentCard paymentCardOne = createPaymentCard(1L, "1111-2222", "Bob Duck",
                LocalDate.of(2030, 1, 1), true,
                LocalDateTime.of(2026, 1, 5, 2, 3),
                LocalDateTime.of(2026, 1, 5, 2, 3), user
        );

        PaymentCard paymentCardTwo = createPaymentCard(2L, "3333-4444", "Bob Duck",
                LocalDate.of(2031, 1, 1), true,
                LocalDateTime.of(2026, 1, 5, 2, 3),
                LocalDateTime.of(2026, 1, 5, 2, 3), user
        );

        List<PaymentCard> activeCards = List.of(paymentCardOne, paymentCardTwo);

        CardResponse cardResponseNumberOne = createCardResponse(1L, userId, "1111-2222", "Bob Duck",
                LocalDate.of(2030, 1, 1), true,
                LocalDateTime.of(2026, 1, 5, 2, 3),
                LocalDateTime.of(2026, 1, 5, 2, 3)
        );

        CardResponse cardResponseNumberTwo = createCardResponse(2L, userId, "3333-4444", "Bob Duck",
                LocalDate.of(2031, 1, 1), true,
                LocalDateTime.of(2026, 1, 5, 2, 3),
                LocalDateTime.of(2026, 1, 5, 2, 3)
        );

        when(userRepository.existsById(userId)).thenReturn(true);
        when(paymentCardRepository.findActivePaymentCardsByUserId(userId)).thenReturn(activeCards);
        when(paymentCardMapper.toDto(paymentCardOne)).thenReturn(cardResponseNumberOne);
        when(paymentCardMapper.toDto(paymentCardTwo)).thenReturn(cardResponseNumberTwo);

        List<CardResponse> result = paymentCardService.getActivePaymentCardsByUserId(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(cardResponseNumberOne, result.get(0));
        assertEquals(cardResponseNumberTwo, result.get(1));
        verify(userRepository).existsById(userId);
        verify(paymentCardRepository).findActivePaymentCardsByUserId(userId);
        verify(paymentCardMapper, times(2)).toDto(any(PaymentCard.class));
    }

    @Test
    void getActiveCardsByUserId_shouldThrowResourceNotFoundException_whenUserNotFound() {
        Long userId = 1000L;
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> paymentCardService.getActivePaymentCardsByUserId(userId));

        verify(userRepository).existsById(userId);
        verify(paymentCardRepository, never()).findActivePaymentCardsByUserId(any());
        verifyNoInteractions(paymentCardMapper);
    }
}