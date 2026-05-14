package com.innowise.userservice.controller;

import com.innowise.userservice.model.dto.user.UserCreateRequest;
import com.innowise.userservice.model.dto.user.UserResponse;
import com.innowise.userservice.model.dto.user.UserUpdateRequest;
import com.innowise.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public UserResponse createUser(@Valid @RequestBody UserCreateRequest request) {
        return userService.createUser(request);
    }

    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return userService.updateUser(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @PatchMapping("/{id}/activate")
    public void activateUserStatus(@PathVariable Long id) {
        userService.activateUserStatus(id);
    }

    @PatchMapping("/{id}/deactivate")
    public void deactivateUserStatus(@PathVariable Long id) {
        userService.deactivateUserStatus(id);
    }

    public Page<UserResponse> getAllUsers(@RequestParam(required = false) String name,
                                          @RequestParam(required = false) String surname,
                                          @PageableDefault(size = 10) Pageable pageable) {
        return userService.getAllUsers(name, surname, pageable);
    }

}
