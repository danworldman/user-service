package com.innowise.userservice.service;

import com.innowise.userservice.model.dto.user.UserCreateRequest;
import com.innowise.userservice.model.dto.user.UserResponse;
import com.innowise.userservice.model.dto.user.UserUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponse createUser(UserCreateRequest request);

    UserResponse getUserById(long id);

    UserResponse updateUser(long id, UserUpdateRequest request);

    void deleteUser(long id);

    void activateUserStatus(long id);

    void deactivateUserStatus(long id);

    Page<UserResponse> getAllUsers(String name, String surname, Pageable pageable);
}