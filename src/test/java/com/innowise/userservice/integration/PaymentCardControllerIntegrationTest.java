package com.innowise.userservice.integration;

import com.innowise.userservice.model.dto.card.CardCreateRequest;
import com.innowise.userservice.model.dto.card.CardResponse;
import com.innowise.userservice.model.dto.card.CardUpdateRequest;
import com.innowise.userservice.model.dto.user.UserCreateRequest;
import com.innowise.userservice.model.dto.user.UserResponse;
import com.innowise.userservice.model.dto.user.UserWithCardsDTO;
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

public class PaymentCardControllerIntegrationTest extends BaseIntegrationTest {

    private RestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        restTemplate = createRestTemplate();

        jdbcTemplate.execute("TRUNCATE TABLE payment_cards RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE users RESTART IDENTITY CASCADE");

    }

    private String cardsUrl() {
        return "http://localhost:" + port + "/api/cards";
    }

    private String usersUrl() {
        return "http://localhost:" + port + "/api/users";
    }

    @Test
    void getCardById_shouldReturnCard_whenExists() {
        Long userId = createTestUser();
        CardCreateRequest createRequest = createCardRequestWithNumber(userId, "3333444455556666");
        ResponseEntity<CardResponse> createResponse = restTemplate.postForEntity(
                cardsUrl(),
                createRequest,
                CardResponse.class
        );

        assertThat(createResponse.getBody()).isNotNull();
        Long cardId = createResponse.getBody().id();

        ResponseEntity<CardResponse> getResponse = restTemplate.getForEntity(
                cardsUrl() + "/" + cardId,
                CardResponse.class
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().number()).isEqualTo("3333444455556666");
    }

    @Test
    void getCardById_shouldReturnNotFound_whenCardDoesNotExist() {
        assertThatThrownBy(() -> restTemplate.getForEntity(cardsUrl() + "/10000", CardResponse.class))
                .isInstanceOf(HttpClientErrorException.NotFound.class);
    }

    @Test
    void createCard_shouldReturnCreatedCard() {
        Long userId = createTestUser();
        CardCreateRequest createRequest = createCardRequest(userId);
        ResponseEntity<CardResponse> createResponse = restTemplate.postForEntity(
                cardsUrl(),
                createRequest,
                CardResponse.class
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().id()).isPositive();
        assertThat(createResponse.getBody().number()).isEqualTo("1111222233334444");
        assertThat(createResponse.getBody().isActive()).isTrue();
    }

    @Test
    void createCard_shouldReturnBadRequest_whenUserHasAlreadyFiveCards() {
        Long userId = createTestUser();

        for (int i = 1; i <= 5; i++) {
            CardCreateRequest cardRequest = createCardRequestWithNumber(userId, "111122223333444" + i);
            ResponseEntity<CardResponse> response = restTemplate.postForEntity(
                    cardsUrl(),
                    cardRequest,
                    CardResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }

        CardCreateRequest sixthRequest = createCardRequestWithNumber(userId, "6666666666666666");

        assertThatThrownBy(() -> restTemplate.postForEntity(cardsUrl(), sixthRequest, CardResponse.class))
                .isInstanceOf(HttpClientErrorException.BadRequest.class);
    }

    @Test
    void createCard_shouldReturnNotFound_whenUserDoesNotExist() {
        CardCreateRequest createRequest = createCardRequest(10000L);

        assertThatThrownBy(() -> restTemplate.postForEntity(cardsUrl(), createRequest, CardResponse.class))
                .isInstanceOf(HttpClientErrorException.NotFound.class);
    }

    @Test
    void createCard_shouldReturnBadRequest_withInvalidNumberFormat() {
        Long userId = createTestUser();
        CardCreateRequest createRequest = new CardCreateRequest(
                userId,
                "1234",
                "Bob Duck",
                LocalDate.of(2030, 1, 1)
        );

        assertThatThrownBy(() -> restTemplate.postForEntity(cardsUrl(), createRequest, CardResponse.class))
                .isInstanceOf(HttpClientErrorException.BadRequest.class);
    }

    @Test
    void createCard_shouldReturnBadRequest_withEmptyHolder() {
        Long userId = createTestUser();
        CardCreateRequest createRequest = new CardCreateRequest(
                userId,
                "1111222233334444",
                "",
                LocalDate.of(2030, 1, 1)
        );

        assertThatThrownBy(() -> restTemplate.postForEntity(cardsUrl(), createRequest, CardResponse.class))
                .isInstanceOf(HttpClientErrorException.BadRequest.class);
    }

    @Test
    void createCard_shouldReturnBadRequest_withPastExpirationDate() {
        Long userId = createTestUser();
        CardCreateRequest createRequest = new CardCreateRequest(
                userId,
                "1111222233334444",
                "Bob Duck",
                LocalDate.of(2000, 1, 1)
        );

        assertThatThrownBy(() -> restTemplate.postForEntity(cardsUrl(), createRequest, CardResponse.class))
                .isInstanceOf(HttpClientErrorException.BadRequest.class);
    }

    @Test
    void updateCard_shouldUpdateAndReturnCard() {
        Long userId = createTestUser();
        CardCreateRequest createRequest = createCardRequest(userId);
        ResponseEntity<CardResponse> createResponse = restTemplate.postForEntity(
                cardsUrl(),
                createRequest,
                CardResponse.class
        );

        assertThat(createResponse.getBody()).isNotNull();
        Long cardId = createResponse.getBody().id();

        CardUpdateRequest updateRequest = new CardUpdateRequest(
                "3333444455556666",
                "Bob Duck",
                LocalDate.of(2030, 1, 1)
        );

        HttpEntity<CardUpdateRequest> entity = new HttpEntity<>(updateRequest);
        ResponseEntity<CardResponse> updateResponse = restTemplate.exchange(
                cardsUrl() + "/" + cardId,
                HttpMethod.PATCH,
                entity,
                CardResponse.class
        );

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody()).isNotNull();
        assertThat(updateResponse.getBody().number()).isEqualTo("3333444455556666");
        assertThat(updateResponse.getBody().holder()).isEqualTo("Bob Duck");
    }

    @Test
    void updateCard_shouldReturnBadRequest_withPastExpirationDate() {
        Long userId = createTestUser();
        CardCreateRequest createRequest = createCardRequest(userId);
        ResponseEntity<CardResponse> createResponse = restTemplate.postForEntity(
                cardsUrl(),
                createRequest,
                CardResponse.class
        );

        assertThat(createResponse.getBody()).isNotNull();
        Long cardId = createResponse.getBody().id();

        CardUpdateRequest update = new CardUpdateRequest(
                null,
                null,
                LocalDate.of(2000, 1, 1)
        );

        HttpEntity<CardUpdateRequest> entity = new HttpEntity<>(update);

        assertThatThrownBy(() -> restTemplate.exchange(
                cardsUrl() + "/" + cardId,
                HttpMethod.PATCH,
                entity,
                Void.class
        )).isInstanceOf(HttpClientErrorException.BadRequest.class);
    }

    @Test
    void deleteCard_shouldReturnNoContent() {
        Long userId = createTestUser();
        CardCreateRequest createRequest = createCardRequest(userId);
        ResponseEntity<CardResponse> createResponse = restTemplate.postForEntity(
                cardsUrl(),
                createRequest,
                CardResponse.class
        );

        assertThat(createResponse.getBody()).isNotNull();
        Long cardId = createResponse.getBody().id();

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                cardsUrl() + "/" + cardId,
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThatThrownBy(() -> restTemplate.getForEntity(cardsUrl() + "/" + cardId, CardResponse.class))
                .isInstanceOf(HttpClientErrorException.NotFound.class);
    }

    @Test
    void activateCard_shouldActivateCard() {
        Long userId = createTestUser();
        CardCreateRequest createRequest = createCardRequest(userId);
        ResponseEntity<CardResponse> createResponse = restTemplate.postForEntity(
                cardsUrl(),
                createRequest,
                CardResponse.class
        );

        assertThat(createResponse.getBody()).isNotNull();
        Long cardId = createResponse.getBody().id();

        restTemplate.exchange(
                cardsUrl() + "/" + cardId + "/deactivate",
                HttpMethod.PATCH,
                null,
                Void.class
        );

        ResponseEntity<Void> activateResponse = restTemplate.exchange(
                cardsUrl() + "/" + cardId + "/activate",
                HttpMethod.PATCH,
                null,
                Void.class
        );

        assertThat(activateResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        ResponseEntity<CardResponse> getResponse = restTemplate.getForEntity(
                cardsUrl() + "/" + cardId,
                CardResponse.class
        );

        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().isActive()).isTrue();
    }

    @Test
    void getUserWithCards_cacheShouldBeInvalidatedAfterCardCreation() {
        Long userId = createTestUser();
        ResponseEntity<UserWithCardsDTO> responseBeforeCreation = restTemplate.getForEntity(
                usersUrl() + "/" + userId + "/with-cards",
                UserWithCardsDTO.class
        );

        assertThat(responseBeforeCreation.getBody()).isNotNull();
        assertThat(responseBeforeCreation.getBody().cards()).isEmpty();

        CardCreateRequest cardRequest = createCardRequest(userId);
        restTemplate.postForEntity(cardsUrl(), cardRequest, CardResponse.class);

        ResponseEntity<UserWithCardsDTO> responseAfterCreation = restTemplate.getForEntity(
                usersUrl() + "/" + userId + "/with-cards",
                UserWithCardsDTO.class
        );

        assertThat(responseAfterCreation.getBody()).isNotNull();
        assertThat(responseAfterCreation.getBody().cards()).hasSize(1);
        assertThat(responseAfterCreation.getBody().cards().getFirst().number()).isEqualTo("1111222233334444");
    }

    @Test
    void getAllCards_shouldFilterByHolderAndUserNames() {
        Long userId = createTestUser();

        CardCreateRequest cardCreateRequest = new CardCreateRequest(
                userId, "1111222233334444", "Bob Duck", LocalDate.of(2030, 1, 1));
        restTemplate.postForEntity(cardsUrl(), cardCreateRequest, CardResponse.class
        );

        CardCreateRequest wrongCardCreateRequest = new CardCreateRequest(
                userId, "3333444455556666", "Sam Hock", LocalDate.of(2030, 1, 1));
        restTemplate.postForEntity(cardsUrl(), wrongCardCreateRequest, CardResponse.class
        );

        ResponseEntity<String> response = restTemplate.getForEntity(
                cardsUrl() + "?holder=Bob&name=Bob&surname=Duck&page=0&size=10",
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"number\":\"1111222233334444\"");
        assertThat(response.getBody()).doesNotContain("\"number\":\"3333444455556666\"");
        assertThat(response.getBody()).contains("\"totalElements\":1");
    }

    private Long createTestUser() {
        UserCreateRequest userRequest = new UserCreateRequest(
                "Bob",
                "Duck",
                "bob@email.com",
                LocalDate.of(2000, 1, 1)
        );

        ResponseEntity<UserResponse> responseEntity = restTemplate.postForEntity(
                usersUrl(),
                userRequest,
                UserResponse.class
        );

        assertThat(responseEntity.getBody()).isNotNull();
        return responseEntity.getBody().id();
    }

    private CardCreateRequest createCardRequest(Long userId) {
        return new CardCreateRequest(
                userId,
                "1111222233334444",
                "Bob Duck",
                LocalDate.of(2030, 1, 1)
        );
    }

    private CardCreateRequest createCardRequestWithNumber(Long userId, String number) {
        return new CardCreateRequest(
                userId,
                number,
                "Bob Duck",
                LocalDate.of(2030, 1, 1)
        );
    }
}