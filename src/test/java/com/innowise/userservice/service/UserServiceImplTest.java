package com.innowise.userservice.service;

import com.innowise.userservice.exception.DuplicateEmailException;
import com.innowise.userservice.exception.ResourceNotFoundException;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.model.dto.card.CardInfoDTO;
import com.innowise.userservice.model.dto.user.UserWithCardsDTO;
import com.innowise.userservice.model.dto.user.UserCreateRequest;
import com.innowise.userservice.model.dto.user.UserResponse;
import com.innowise.userservice.model.dto.user.UserUpdateRequest;
import com.innowise.userservice.model.entity.PaymentCard;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.dao.UserDAO;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserDAO userDAO;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getUserById_shouldReturnUserResponse_whenUserExists() {
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

        when(userDAO.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userResponse);

        UserResponse result = userService.getUserById(userId);

        assertEquals(userResponse, result);
        verify(userDAO).findById(userId);
        verify(userMapper).toDto(user);
    }

    @Test
    void getUserById_shouldThrowResourceNotFoundException_whenUserDoesNotExist() {
        Long userId = 10000L;

        when(userDAO.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(userId));

        verify(userDAO).findById(userId);
        verify(userMapper, never()).toDto(any());
    }

    @Test
    void getUserById_shouldThrowIllegalArgumentException_whenIdIsNull() {
        Long userId = null;

        assertThrows(IllegalArgumentException.class, () -> userService.getUserById(userId));

        verify(userDAO, never()).findById(any());
    }

    @Test
    void createUser_shouldReturnUserResponse() {
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

        when(userDAO.existsByEmail(userCreateRequest.email())).thenReturn(false);
        when(userMapper.toEntity(userCreateRequest)).thenReturn(user);
        when(userDAO.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userResponse);

        UserResponse result = userService.createUser(userCreateRequest);

        assertEquals(result, userResponse);
        verify(userDAO).existsByEmail(userCreateRequest.email());
        verify(userMapper).toEntity(userCreateRequest);
        verify(userDAO).save(user);
        verify(userMapper).toDto(user);
    }

    @Test
    void createUser_shouldThrowDuplicateEmailException_whenEmailExists() {
        UserCreateRequest userCreateRequest = new UserCreateRequest("Bob", "Duck", "bob@email.com",
                LocalDate.of(2000, 1, 1)
        );

        when(userDAO.existsByEmail(userCreateRequest.email())).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> userService.createUser(userCreateRequest));

        verify(userDAO).existsByEmail(userCreateRequest.email());
        verify(userMapper, never()).toEntity(any());
        verify(userDAO, never()).save(any());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    void createUser_shouldThrowIllegalArgumentException_whenRequestIsNull() {
        UserCreateRequest userCreateRequest = null;

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(userCreateRequest));

        verify(userDAO, never()).existsByEmail(any());
        verify(userMapper, never()).toEntity(any());
    }

    @Test
    void updateUser_shouldReturnUserResponse_whenUserExists() {
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

        when(userDAO.findById(userId)).thenReturn(Optional.of(oldUser));
        when(userMapper.toDto(oldUser)).thenReturn(newUserResponse);

        UserResponse result = userService.updateUser(userId, userUpdateRequest);

        assertEquals(result, newUserResponse);
        verify(userDAO).findById(userId);
        verify(userMapper).updateEntity(userUpdateRequest, oldUser);
        verify(userMapper).toDto(oldUser);
        verify(userDAO, never()).existsByEmail(any());
        verify(userDAO, never()).save(any());
    }

    @Test
    void updateUser_shouldThrowResourceNotFoundException() {
        Long userId = 10000L;
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest("Bob", "Duck", "bob@email.com",
                LocalDate.of(2020, 1, 1)
        );

        when(userDAO.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(userId, userUpdateRequest));

        verify(userDAO).findById(userId);
    }

    @Test
    void deleteUser_shouldReturnVoid() {
        Long userId = 1L;

        when(userDAO.existsById(userId)).thenReturn(true);

        userService.deleteUser(userId);

        verify(userDAO).existsById(userId);
        verify(userDAO).deleteById(userId);
    }

    @Test
    void deleteUser_shouldThrowResourceNotFoundException() {
        Long userId = 10000L;

        when(userDAO.existsById(userId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(userId));

        verify(userDAO).existsById(userId);
        verify(userDAO, never()).deleteById(userId);
    }

    @Test
    public void activateUserStatus_shouldReturnVoid() {
        Long userId = 1L;
        User user = createUser(userId, "Bob", "Duck", "bob@email.com", false,
                LocalDate.of(2000, 1, 1),
                LocalDateTime.of(2026, 5, 19, 3, 1),
                LocalDateTime.of(2026, 5, 19, 3, 1)
        );

        when(userDAO.findById(userId)).thenReturn(Optional.of(user));
        when(userDAO.save(user)).thenReturn(user);

        userService.activateUserStatus(userId);

        assertTrue(user.isActive());
        verify(userDAO).findById(userId);
        verify(userDAO).save(user);
    }

    @Test
    void activateUserStatus_shouldThrowResourceNotFoundException() {
        Long userId = 10000L;

        when(userDAO.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.activateUserStatus(userId));

        verify(userDAO).findById(userId);
        verify(userDAO, never()).save(any());
    }

    @Test
    void deactivateUserStatus_shouldReturnVoid() {
        Long userId = 1L;
        User user = createUser(userId, "Bob", "Duck", "bob@email.com", true,
                LocalDate.of(2000, 1, 1),
                LocalDateTime.of(2026, 5, 19, 3, 1),
                LocalDateTime.of(2026, 5, 19, 3, 1)
        );

        when(userDAO.findById(userId)).thenReturn(Optional.of(user));
        when(userDAO.save(user)).thenReturn(user);

        userService.deactivateUserStatus(userId);

        assertFalse(user.isActive());
        verify(userDAO).findById(userId);
        verify(userDAO).save(user);
    }

    @Test
    void deactivateUserStatus_shouldThrowResourceNotFoundException() {
        Long userId = 10000L;

        when(userDAO.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.deactivateUserStatus(userId));

        verify(userDAO).findById(userId);
        verify(userDAO, never()).save(any());
    }

    @Test
    void getAllUsers_shouldReturnPageOfUserResponse() {
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

        when(userDAO.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
        when(userMapper.toDto(users.get(0))).thenReturn(userResponseNumberOne);
        when(userMapper.toDto(users.get(1))).thenReturn(userResponseNumberTwo);

        Page<UserResponse> result = userService.getAllUsers("Bob", "Duck", pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(userResponseNumberOne, result.getContent().get(0));
        assertEquals(userResponseNumberTwo, result.getContent().get(1));
        verify(userDAO).findAll(any(Specification.class), eq(pageable));
        verify(userMapper).toDto(users.get(0));
        verify(userMapper).toDto(users.get(1));
    }

    @Test
    void getAllUsers_shouldReturnEmptyPage_whenNoUsersFound() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> emtyPage = Page.empty(pageable);

        when(userDAO.findAll(any(Specification.class), eq(pageable))).thenReturn(emtyPage);

        Page<UserResponse> result = userService.getAllUsers("Bob", "Duck", pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userDAO).findAll(any(Specification.class), eq(pageable));
        verify(userMapper, never()).toDto(any());
    }

    @Test
    void getUserWithCards_shouldReturnUserWithCards_whenUserExists() {
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

        when(userDAO.findUsersWithPaymentCards(userId)).thenReturn(Optional.of(user));
        when(userMapper.toUserWithCardsDTO(user)).thenReturn(userWithCardsDTO);

        UserWithCardsDTO result = userService.getUserWithCards(userId);

        assertEquals(userWithCardsDTO, result);
        verify(userDAO).findUsersWithPaymentCards(userId);
        verify(userMapper).toUserWithCardsDTO(user);
    }

    @Test
    void getUserWithCards_shouldThrowResourceNotFoundException() {
        Long userId = 10000L;

        when(userDAO.findUsersWithPaymentCards(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserWithCards(userId));

        verify(userDAO).findUsersWithPaymentCards(userId);
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