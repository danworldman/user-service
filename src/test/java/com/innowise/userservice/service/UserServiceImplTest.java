package com.innowise.userservice.service;

import com.innowise.userservice.exception.DuplicateEmailException;
import com.innowise.userservice.exception.ResourceNotFoundException;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.model.dto.card.CardInfoDTO;
import com.innowise.userservice.model.dto.card.UserWithCardsDTO;
import com.innowise.userservice.model.dto.user.UserCreateRequest;
import com.innowise.userservice.model.dto.user.UserResponse;
import com.innowise.userservice.model.dto.user.UserUpdateRequest;
import com.innowise.userservice.model.entity.PaymentCard;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    public void getUserById_shouldReturnUserResponse_whenUserExists() {
        Long userId = 1L;
        User user = createUser(userId, "Bob", "Duck", "bob@email.com", true,
                LocalDate.of(2000, 1, 1),
                LocalDateTime.of(2026, 5, 19, 3, 1),
                LocalDateTime.of(2026, 5, 19, 3, 1)
        );

        UserResponse userResponse = new UserResponse(userId, "Bob", "Duck", "bob@email.com",
                true, LocalDate.of(2000, 1, 1),
                LocalDateTime.of(2026, 5, 19, 3, 1),
                LocalDateTime.of(2026, 5, 19, 3, 1)
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userResponse);

        UserResponse result = userService.getUserById(userId);

        assertEquals(userResponse, result);
        verify(userRepository).findById(userId);
        verify(userMapper).toDto(user);
    }

    @Test
    public void getUserById_shouldThrowResourceNotFoundException_whenUserDoesNotExist() {
        Long userId = 10000L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(userId));
        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository);
        verify(userMapper, never()).toDto(any());
    }

    @Test
    public void createUser_shouldReturnUserResponse() {
        Long userId = 1L;
        User user = createUser(userId, "Bob", "Duck", "bob@email.com", true,
                LocalDate.of(2000, 1, 1),
                LocalDateTime.of(2026, 5, 19, 3, 1),
                LocalDateTime.of(2026, 5, 19, 3, 1)
        );

        UserCreateRequest userCreateRequest = new UserCreateRequest("Bob", "Duck", "bob@email.com",
                LocalDate.of(2000, 1, 1)
        );

        UserResponse userResponse = new UserResponse(userId, "Bob", "Duck", "bob@email.com",
                true, LocalDate.of(2000, 1, 1),
                LocalDateTime.of(2026, 5, 19, 3, 1),
                LocalDateTime.of(2026, 5, 19, 3, 1)
        );

        when(userRepository.existsByEmail(userCreateRequest.email())).thenReturn(false);
        when(userMapper.toEntity(userCreateRequest)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userResponse);

        UserResponse result = userService.createUser(userCreateRequest);

        assertEquals(result, userResponse);
        verify(userRepository).existsByEmail(userCreateRequest.email());
        verify(userMapper).toEntity(userCreateRequest);
        verify(userRepository).save(user);
        verify(userMapper).toDto(user);
    }

    @Test
    public void createUser_shouldThrowDuplicateEmailException_whenEmailExists() {
        UserCreateRequest userCreateRequest = new UserCreateRequest("Bob", "Duck", "bob@email.com",
                LocalDate.of(2000, 1, 1)
        );

        when(userRepository.existsByEmail(userCreateRequest.email())).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> userService.createUser(userCreateRequest));

        verify(userRepository).existsByEmail(userCreateRequest.email());
        verify(userMapper, never()).toEntity(any());
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).toDto(any());

    }

    @Test
    public void updateUser_shouldReturnUserResponse_whenUserExists() {
        Long userId = 1L;
        User oldUser = createUser(userId, "Bob", "Duck", "bob@email.com",
                true, LocalDate.of(2000, 1, 1),
                LocalDateTime.of(2026, 5, 19, 3, 1),
                LocalDateTime.of(2026, 5, 19, 3, 1)
        );

        UserUpdateRequest userUpdateRequest = new UserUpdateRequest("Bob", "Duck", "bob@email.com",
                LocalDate.of(2020, 1, 1)
        );

        UserResponse newUserResponse = new UserResponse(userId, "Bob", "Duck", "bob@email.com",
                true, LocalDate.of(2020, 1, 1),
                LocalDateTime.of(2026, 5, 19, 3, 1),
                LocalDateTime.of(2026, 5, 19, 3, 1)
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(oldUser));
        when(userMapper.toDto(oldUser)).thenReturn(newUserResponse);

        UserResponse result = userService.updateUser(userId, userUpdateRequest);

        assertEquals(result, newUserResponse);
        verify(userRepository).findById(userId);
        verify(userMapper).updateEntity(userUpdateRequest, oldUser);
        verify(userMapper).toDto(oldUser);
        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void updateUser_shouldThrowResourceNotFoundException() {
        Long userId = 10000L;
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest("Bob", "Duck", "bob@email.com",
                LocalDate.of(2020, 1, 1)
        );

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(userId, userUpdateRequest));
        verify(userRepository).findById(userId);
    }

    @Test
    public void deleteUser_shouldReturnVoid() {
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(true);

        userService.deleteUser(userId);

        verify(userRepository).existsById(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    public void deleteUser_shouldThrowResourceNotFoundException() {
        Long userId = 10000L;

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(userId));

        verify(userRepository).existsById(userId);
        verify(userRepository, never()).deleteById(userId);
    }

    @Test
    public void activateUserStatus_shouldReturnVoid() {
        Long userId = 1L;

        when(userRepository.updateStatus(userId, true)).thenReturn(1);

        userService.activateUserStatus(userId);

        verify(userRepository).updateStatus(userId, true);
    }

    @Test
    public void activateUserStatus_shouldThrowResourceNotFoundException() {
        Long userId = 10000L;

        when(userRepository.updateStatus(userId, true)).thenReturn(0);

        assertThrows(ResourceNotFoundException.class, () -> userService.activateUserStatus(userId));

        verify(userRepository).updateStatus(userId, true);
    }

    @Test
    public void deactivateUserStatus_shouldReturnVoid() {
        Long userId = 1L;

        when(userRepository.updateStatus(userId, false)).thenReturn(1);

        userService.deactivateUserStatus(userId);

        verify(userRepository).updateStatus(userId, false);
    }

    @Test
    public void deactivateUserStatus_shouldThrowResourceNotFoundException() {
        Long userId = 10000L;

        when(userRepository.updateStatus(userId, false)).thenReturn(0);

        assertThrows(ResourceNotFoundException.class, () -> userService.deactivateUserStatus(userId));

        verify(userRepository).updateStatus(userId, false);
    }

    @Test
    public void getAllUsers_shouldReturnPageOfUserResponse() {
        List<User> users = List.of(
                createUser(1L, "Bob", "Duck", "bob@email.com", true,
                        LocalDate.of(2000, 1, 1),
                        LocalDateTime.of(2026, 5, 19, 3, 1),
                        LocalDateTime.of(2026, 5, 19, 3, 1)),
                createUser(2L, "Sam", "Hock", "sam@email.com", true,
                        LocalDate.of(2010, 1, 1),
                        LocalDateTime.of(2026, 5, 20, 3, 1),
                        LocalDateTime.of(2026, 5, 20, 3, 1))
        );

        Pageable pageable = PageRequest.of(0, 10);

        Page<User> userPage = new PageImpl<>(users, pageable, 2);

        UserResponse userResponseNumberOne = new UserResponse(1L, "Bob", "Duck", "bob@email.com",
                true, LocalDate.of(2000, 1, 1),
                LocalDateTime.of(2026, 5, 19, 3, 1),
                LocalDateTime.of(2026, 5, 19, 3, 1)
        );

        UserResponse userResponseNumberTwo = new UserResponse(2L, "Sam", "Hock", "sam@email.com",
                true, LocalDate.of(2010, 1, 1),
                LocalDateTime.of(2026, 5, 20, 3, 1),
                LocalDateTime.of(2026, 5, 20, 3, 1)
        );

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
        when(userMapper.toDto(users.get(0))).thenReturn(userResponseNumberOne);
        when(userMapper.toDto(users.get(1))).thenReturn(userResponseNumberTwo);

        Page<UserResponse> result = userService.getAllUsers("Bob", "Duck", pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(userResponseNumberOne, result.getContent().get(0));
        assertEquals(userResponseNumberTwo, result.getContent().get(1));
        verify(userRepository).findAll(any(Specification.class), eq(pageable));
        verify(userMapper).toDto(users.get(0));
        verify(userMapper).toDto(users.get(1));
    }

    @Test
    public void getAllUsers_shouldReturnEmptyPage_whenNoUsersFound() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> emtyPage = Page.empty(pageable);

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emtyPage);

        Page<UserResponse> result = userService.getAllUsers("Bob", "Duck", pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository).findAll(any(Specification.class), eq(pageable));
        verify(userMapper, never()).toDto(any());
    }

    @Test
    public void getUserWithCards_shouldReturnUserWithCards_whenUserExists() {
        Long userId = 1L;
        User user = createUser(userId, "Bob", "Duck", "bob@email.com",
                true, LocalDate.of(2000, 1, 1),
                LocalDateTime.of(2026, 5, 19, 3, 1),
                LocalDateTime.of(2026, 5, 19, 3, 1)
        );

        PaymentCard paymentCard = new PaymentCard();
        paymentCard.setId(1L);
        paymentCard.setNumber("1111-2222");
        paymentCard.setHolder("Bob Duck");
        paymentCard.setExpirationDate(LocalDate.of(2030, 1, 1));
        paymentCard.setActive(true);
        paymentCard.setUser(user);
        user.setPaymentCards(List.of(paymentCard));

        CardInfoDTO cardInfoDTO = new CardInfoDTO(1L, "1111-2222", "Bob Duck", true);

        UserWithCardsDTO userWithCardsDTO = new UserWithCardsDTO(
                userId, "Bob", "Duck", "bob@email.com", true, List.of(cardInfoDTO)
        );

        when(userRepository.findUsersWithPaymentCards(userId)).thenReturn(Optional.of(user));

        UserWithCardsDTO result = userService.getUserWithCards(userId);

        assertEquals(userWithCardsDTO, result);
        verify(userRepository).findUsersWithPaymentCards(userId);
    }

    @Test
    public void getUserWithCards_shouldThrowResourceNotFoundException() {
        Long userId = 10000L;

        when(userRepository.findUsersWithPaymentCards(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserWithCards(userId));

        verify(userRepository).findUsersWithPaymentCards(userId);
        verifyNoMoreInteractions(userRepository);
    }

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
}