package com.innowise.userservice.service.impl;

import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.model.dto.user.UserCreateRequest;
import com.innowise.userservice.model.dto.user.UserResponse;
import com.innowise.userservice.model.dto.user.UserUpdateRequest;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponse createUser(UserCreateRequest request) {
        User user = userMapper.toEntity(request);
        User saveUser = userRepository.save(user);

        return userMapper.toDto(saveUser);
    }

    @Override
    public UserResponse getUserById(long id) {
        User user = userRepository.getById(id);
        return userMapper.toDto(user);
    }

    @Override
    public UserResponse updateUser(long id, UserUpdateRequest request) {
        return null;
    }

    @Override
    public void deleteUser(long id) {

    }

    @Override
    public void activateUserStatus(long id) {

    }

    @Override
    public void deactivateUserStatus(long id) {

    }

    @Override
    public Page<UserResponse> getAllUsers(String name, String surname, Pageable pageable) {
        return null;
    }
}