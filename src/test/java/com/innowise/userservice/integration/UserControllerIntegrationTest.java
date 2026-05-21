package com.innowise.userservice.integration;

import com.innowise.userservice.model.dto.card.CardCreateRequest;
import com.innowise.userservice.model.dto.card.CardResponse;
import com.innowise.userservice.model.dto.card.UserWithCardsDTO;
import com.innowise.userservice.model.dto.user.UserCreateRequest;
import com.innowise.userservice.model.dto.user.UserResponse;
import com.innowise.userservice.model.dto.user.UserUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UserControllerIntegrationTest extends BaseIntegrationTest {

    private RestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        restTemplate = createRestTemplate();

        jdbcTemplate.execute("TRUNCATE TABLE payment_cards RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE users RESTART IDENTITY CASCADE");
    }

    private String baseUrl() {
        return "http://localhost:" + port + "/api/users";
    }

    private UserCreateRequest createUserRequest() {
        return new UserCreateRequest(
                "Bob",
                "Duck",
                "bob@email.com",
                LocalDate.of(2000, 1, 1)
        );
    }

    private UserCreateRequest createAnotherUserRequest() {
        return new UserCreateRequest(
                "Sam",
                "Hock",
                "sam@email.com",
                LocalDate.of(2000, 1, 1)
        );
    }

    @Test
    void getUserById_shouldReturnUser_whenUserExists() {
        UserCreateRequest createRequest = createUserRequest();
        ResponseEntity<UserResponse> createResponse = restTemplate.postForEntity(
                baseUrl(),
                createRequest,
                UserResponse.class
        );

        assertThat(createResponse.getBody()).isNotNull();
        Long userId = createResponse.getBody().id();

        ResponseEntity<UserResponse> getResponse = restTemplate.getForEntity(
                baseUrl() + "/" + userId,
                UserResponse.class
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().name()).isEqualTo("Bob");
    }

    @Test
    void getUserById_shouldReturnNotFound_whenUserDoesNotExist() {
        assertThatThrownBy(() -> restTemplate.getForEntity(baseUrl() + "/10000", UserResponse.class))
                .isInstanceOf(HttpClientErrorException.NotFound.class);
    }

    @Test
    void createUser_shouldReturnCreatedUser() {
        UserCreateRequest createRequest = createUserRequest();
        ResponseEntity<UserResponse> createResponse = restTemplate.postForEntity(
                baseUrl(),
                createRequest,
                UserResponse.class
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().id()).isPositive();
        assertThat(createResponse.getBody().name()).isEqualTo("Bob");
        assertThat(createResponse.getBody().email()).isEqualTo("bob@email.com");
        assertThat(createResponse.getBody().isActive()).isTrue();
    }

    @Test
    void createUser_shouldReturnConflict_whenEmailAlreadyExists() {
        UserCreateRequest createRequest = createUserRequest();
        restTemplate.postForEntity(baseUrl(), createRequest, UserResponse.class);

        assertThatThrownBy(() -> restTemplate.postForEntity(baseUrl(), createRequest, UserResponse.class))
                .isInstanceOf(HttpClientErrorException.Conflict.class);
    }

    @Test
    void createUser_shouldReturnValidationErrors_withInvalidEmail() {
        UserCreateRequest createRequest = new UserCreateRequest(
                "Bob",
                "Duck",
                "badEmail",
                LocalDate.of(2000, 1, 1)
        );

        assertThatThrownBy(() -> restTemplate.postForEntity(baseUrl(), createRequest, String.class))
                .isInstanceOf(HttpClientErrorException.BadRequest.class)
                .satisfies(ex -> {
                    HttpClientErrorException.BadRequest exception = (HttpClientErrorException.BadRequest) ex;

                    assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(exception.getResponseBodyAsString()).contains("errors");
                });
    }

    @Test
    void updateUser_shouldUpdateAndReturnUpdatedUser() {
        UserCreateRequest createRequest = createUserRequest();
        ResponseEntity<UserResponse> createResponse = restTemplate.postForEntity(
                baseUrl(),
                createRequest,
                UserResponse.class
        );

        assertThat(createResponse.getBody()).isNotNull();
        Long userId = createResponse.getBody().id();

        UserUpdateRequest updateRequest = new UserUpdateRequest(
                "Bob",
                "Duck",
                "bob@email.com",
                LocalDate.of(2020, 1, 1)
        );

        HttpEntity<UserUpdateRequest> entity = new HttpEntity<>(updateRequest);
        ResponseEntity<UserResponse> updateResponse = restTemplate.exchange(
                baseUrl() + "/" + userId,
                HttpMethod.PUT,
                entity,
                UserResponse.class
        );

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody()).isNotNull();
        assertThat(updateResponse.getBody().birthDate()).isEqualTo(LocalDate.of(2020, 1, 1));
    }

    @Test
    void updateUser_shouldReturnConflict_whenNewEmailAlreadyExists() {
        UserCreateRequest firstCreateRequest = createUserRequest();
        restTemplate.postForEntity(baseUrl(), firstCreateRequest, UserResponse.class);

        UserCreateRequest secondCreateRequest = createAnotherUserRequest();
        ResponseEntity<UserResponse> secondCreateResponse = restTemplate.postForEntity(
                baseUrl(),
                secondCreateRequest,
                UserResponse.class
        );

        assertThat(secondCreateResponse.getBody()).isNotNull();
        Long secondUserId = secondCreateResponse.getBody().id();

        UserUpdateRequest updateRequest = new UserUpdateRequest(null, null, "bob@email.com", null);
        HttpEntity<UserUpdateRequest> entity = new HttpEntity<>(updateRequest);

        assertThatThrownBy(() -> restTemplate.exchange(
                baseUrl() + "/" + secondUserId,
                HttpMethod.PUT,
                entity,
                Void.class
        )).isInstanceOf(HttpClientErrorException.Conflict.class);
    }

    @Test
    void updateUser_shouldReturnBadRequest_withEmptyName() {
        UserCreateRequest create = createUserRequest();
        ResponseEntity<UserResponse> createResponse = restTemplate.postForEntity(baseUrl(), create, UserResponse.class);

        assertThat(createResponse.getBody()).isNotNull();
        Long userId = createResponse.getBody().id();

        UserUpdateRequest updateRequest = new UserUpdateRequest("", null, null, null);
        HttpEntity<UserUpdateRequest> entity = new HttpEntity<>(updateRequest);

        assertThatThrownBy(() -> restTemplate.exchange(
                baseUrl() + "/" + userId,
                HttpMethod.PUT,
                entity,
                Void.class
        )).isInstanceOf(HttpClientErrorException.BadRequest.class);
    }

    @Test
    void deleteUser_shouldReturnNoContent() {
        UserCreateRequest createRequest = createUserRequest();
        ResponseEntity<UserResponse> createResponse = restTemplate.postForEntity(
                baseUrl(),
                createRequest,
                UserResponse.class
        );

        assertThat(createResponse.getBody()).isNotNull();
        Long userId = createResponse.getBody().id();

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                baseUrl() + "/" + userId,
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThatThrownBy(() -> restTemplate.getForEntity(baseUrl() + "/" + userId, UserResponse.class))
                .isInstanceOf(HttpClientErrorException.NotFound.class);
    }

    @Test
    void activateUserStatus_shouldActivateUser() {
        UserCreateRequest createRequest = createUserRequest();
        ResponseEntity<UserResponse> createResponse = restTemplate.postForEntity(
                baseUrl(),
                createRequest,
                UserResponse.class
        );

        assertThat(createResponse.getBody()).isNotNull();
        Long userId = createResponse.getBody().id();

        restTemplate.exchange(
                baseUrl() + "/" + userId + "/deactivate",
                HttpMethod.PATCH, null,
                Void.class
        );

        ResponseEntity<Void> activateResponse = restTemplate.exchange(
                baseUrl() + "/" + userId + "/activate",
                HttpMethod.PATCH,
                null,
                Void.class
        );

        assertThat(activateResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        ResponseEntity<UserResponse> getResponse = restTemplate.getForEntity(
                baseUrl() + "/" + userId,
                UserResponse.class
        );

        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().isActive()).isTrue();
    }

    @Test
    void getAllUsers_shouldReturnPageOfUsers() {
        restTemplate.postForEntity(baseUrl(), createUserRequest(), UserResponse.class);
        restTemplate.postForEntity(baseUrl(), createAnotherUserRequest(), UserResponse.class);

        ResponseEntity<String> pageResponse = restTemplate.getForEntity(
                baseUrl() + "?page=0&size=10",
                String.class
        );

        assertThat(pageResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(pageResponse.getBody()).contains("\"totalElements\":2");
    }

    @Test
    void getAllUsers_shouldFilterByNameAndSurname() {
        restTemplate.postForEntity(baseUrl(), createUserRequest(), UserResponse.class);

        ResponseEntity<String> pageResponse = restTemplate.getForEntity(baseUrl()
                + "?name=Bob&surname=Duck&page=0&size=10", String.class);

        assertThat(pageResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(pageResponse.getBody()).contains("\"totalElements\":1");
        assertThat(pageResponse.getBody()).contains("\"name\":\"Bob\"");
    }

    @Test
    void getUserWithCards_shouldReturnUserWithCards() {
        UserCreateRequest createRequest = createUserRequest();
        ResponseEntity<UserResponse> createResponse = restTemplate.postForEntity(
                baseUrl(),
                createRequest,
                UserResponse.class
        );

        assertThat(createResponse.getBody()).isNotNull();
        Long userId = createResponse.getBody().id();

        CardCreateRequest cardRequest = new CardCreateRequest(
                userId,
                "1111222233334444",
                "Bob Duck",
                LocalDate.of(2030, 1, 1)
        );

        restTemplate.postForEntity("http://localhost:" + port + "/api/cards", cardRequest, CardResponse.class);

        ResponseEntity<UserWithCardsDTO> firstResponse = restTemplate.getForEntity(baseUrl() + "/"
                + userId + "/with-cards", UserWithCardsDTO.class);
        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(firstResponse.getBody()).isNotNull();
        assertThat(firstResponse.getBody().cards()).hasSize(1);

        ResponseEntity<UserWithCardsDTO> secondResponse = restTemplate.getForEntity(baseUrl() + "/"
                + userId + "/with-cards", UserWithCardsDTO.class);
        assertThat(secondResponse.getBody()).isNotNull();
        assertThat(secondResponse.getBody()).isEqualTo(firstResponse.getBody());
    }

    @Test
    void getUserWithCards_shouldReturnNotFound_whenUserDoesNotExist() {
        assertThatThrownBy(() -> restTemplate.getForEntity(baseUrl() + "/10000/with-cards", UserWithCardsDTO.class))
                .isInstanceOf(HttpClientErrorException.NotFound.class);
    }
}