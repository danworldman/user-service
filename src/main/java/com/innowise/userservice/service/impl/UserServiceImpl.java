package com.innowise.userservice.service.impl;

import com.innowise.userservice.exception.DuplicateEmailException;
import com.innowise.userservice.exception.ResourceNotFoundException;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.model.dto.user.UserCreateRequest;
import com.innowise.userservice.model.dto.user.UserResponse;
import com.innowise.userservice.model.dto.user.UserUpdateRequest;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.repository.specification.UserSpecification;
import com.innowise.userservice.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

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
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("User's id cannot be null");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        if (id == null) {
            throw new IllegalArgumentException("User's id cannot be null");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (request.email() != null && !request.email().equals(user.getEmail())){
            if (userRepository.existsByEmail(request.email())){
                throw new DuplicateEmailException("This email already exists: " + request.email());
            }
        }

        userMapper.updateEntity(request, user);
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("User's id cannot be null");
        }

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }

        userRepository.deleteById(id);

    }

    @Override
    @Transactional
    public void activateUserStatus(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("User's id cannot be null");
        }

        int resultOfUpdate = userRepository.updateStatus(id, true);
        if (resultOfUpdate == 0) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
    }

    @Override
    @Transactional
    public void deactivateUserStatus(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("User's id cannot be null");
        }

        int resultOfUpdate = userRepository.updateStatus(id, false);
        if (resultOfUpdate == 0) {
            throw new ResourceNotFoundException("User not found with id: " + id);
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
    @Transactional(readOnly = true)
    public UserResponse getUserWithCards(Long id){
        User user = userRepository.findUsersWithPaymentCards(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        return userMapper.toDto(user);
    }

}