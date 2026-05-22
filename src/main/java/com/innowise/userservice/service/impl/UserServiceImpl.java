package com.innowise.userservice.service.impl;

import com.innowise.userservice.exception.DuplicateEmailException;
import com.innowise.userservice.exception.ResourceNotFoundException;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.model.dto.card.CardInfoDTO;
import com.innowise.userservice.model.dto.card.UserWithCardsDTO;
import com.innowise.userservice.model.dto.user.UserCreateRequest;
import com.innowise.userservice.model.dto.user.UserResponse;
import com.innowise.userservice.model.dto.user.UserUpdateRequest;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.repository.specification.UserSpecification;
import com.innowise.userservice.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String USER_NOT_FOUND_MESSAGE = "User not found with id: ";

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        validateID(id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MESSAGE + id));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException("This email already exists: " + request.email());
        }

        User user = userMapper.toEntity(request);
        User saveUser = userRepository.save(user);
        return userMapper.toDto(saveUser);
    }

    @Override
    @CacheEvict(value = "userWithCards", key = "#id")
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        validateID(id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MESSAGE + id));

        if (request.email() != null && !request.email().equals(user.getEmail())
                && userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException("This email already exists: " + request.email());
        }

        if (request.name() != null && request.name().isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }
        if (request.surname() != null && request.surname().isBlank()) {
            throw new IllegalArgumentException("Surname cannot be blank");
        }

        userMapper.updateEntity(request, user);
        return userMapper.toDto(user);
    }

    @Override
    @CacheEvict(value = "userWithCards", key = "#id")
    @Transactional
    public void deleteUser(Long id) {
        validateID(id);

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException(USER_NOT_FOUND_MESSAGE + id);
        }

        userRepository.deleteById(id);
    }

    @Override
    @CacheEvict(value = "userWithCards", key = "#id")
    @Transactional
    public void activateUserStatus(Long id) {
        validateID(id);

        int resultOfUpdate = userRepository.updateStatus(id, true);
        if (resultOfUpdate == 0) {
            throw new ResourceNotFoundException(USER_NOT_FOUND_MESSAGE + id);
        }
    }

    @Override
    @CacheEvict(value = "userWithCards", key = "#id")
    @Transactional
    public void deactivateUserStatus(Long id) {
        validateID(id);

        int resultOfUpdate = userRepository.updateStatus(id, false);
        if (resultOfUpdate == 0) {
            throw new ResourceNotFoundException(USER_NOT_FOUND_MESSAGE + id);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(String name, String surname, Pageable pageable) {
        Specification<User> specification = Specification
                .where(UserSpecification.hasName(name))
                .and(UserSpecification.hasSurname(surname));

        Page<User> userPage = userRepository.findAll(specification, pageable);

        return userPage.map(userMapper::toDto);
    }

    @Override
    @Cacheable(value = "userWithCards", key = "#id")
    @Transactional(readOnly = true)
    public UserWithCardsDTO getUserWithCards(Long id) {
        validateID(id);

        User user = userRepository.findUsersWithPaymentCards(id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MESSAGE + id));

        List<CardInfoDTO> cards = user.getPaymentCards().stream()
                .map(card -> new CardInfoDTO(
                        card.getId(),
                        card.getNumber(),
                        card.getHolder(),
                        card.isActive()
                ))
                .toList();

        return new UserWithCardsDTO(
                user.getId(), user.getName(), user.getSurname(),
                user.getEmail(), user.isActive(), cards
        );
    }

    private void validateID(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("User's id cannot be null");
        }

        if (id <= 0) {
            throw new IllegalArgumentException("User id must be positive");
        }
    }
}