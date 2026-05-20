package com.innowise.userservice.integration;

import com.innowise.userservice.model.dto.card.CardCreateRequest;
import com.innowise.userservice.model.dto.card.CardResponse;
import com.innowise.userservice.model.dto.card.CardUpdateRequest;
import com.innowise.userservice.model.dto.card.UserWithCardsDTO;
import com.innowise.userservice.model.dto.user.UserCreateRequest;
import com.innowise.userservice.model.dto.user.UserResponse;
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

    private Long createTestUser() {
        UserCreateRequest userRequest = new UserCreateRequest("Bob", "Duck", "bob@email.com", LocalDate.of(2000, 1, 1));
        ResponseEntity<UserResponse> responseEntity = restTemplate.postForEntity(usersUrl(), userRequest, UserResponse.class);
        return responseEntity.getBody().id();
    }

    @Test
    void createCard_shouldReturnCreatedCard() {
        Long userId = createTestUser();
        CardCreateRequest createRequest = new CardCreateRequest(userId, "1111-2222", "Bob Duck", LocalDate.of(2030, 1, 1));

        ResponseEntity<CardResponse> createResponse = restTemplate.postForEntity(cardsUrl(), createRequest, CardResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody().id()).isPositive();
        assertThat(createResponse.getBody().number()).isEqualTo("1111-2222");
        assertThat(createResponse.getBody().isActive()).isTrue();
    }

    @Test
    void createCard_shouldReturnBadRequest_whenUserHasAlreadyFiveCards() {
        Long userId = createTestUser();
        for (int i = 1; i <= 5; i++) {
            CardCreateRequest cardRequest = new CardCreateRequest(userId, "card" + i, "Bob Duck", LocalDate.of(2030, 1, 1));
            restTemplate.postForEntity(cardsUrl(), cardRequest, CardResponse.class);
        }
        CardCreateRequest sixthRequest = new CardCreateRequest(userId, "sixth", "Bob Duck", LocalDate.of(2030, 1, 1));
        assertThatThrownBy(() -> restTemplate.postForEntity(cardsUrl(), sixthRequest, CardResponse.class))
                .isInstanceOf(HttpClientErrorException.BadRequest.class);
    }

    @Test
    void createCard_shouldReturnNotFound_whenUserDoesNotExist() {
        CardCreateRequest createRequest = new CardCreateRequest(10000L, "1111-2222", "Bob Duck", LocalDate.of(2030, 1, 1));
        assertThatThrownBy(() -> restTemplate.postForEntity(cardsUrl(), createRequest, CardResponse.class))
                .isInstanceOf(HttpClientErrorException.NotFound.class);
    }

    @Test
    void getCardById_shouldReturnCard_whenExists() {
        Long userId = createTestUser();
        CardCreateRequest createRequest = new CardCreateRequest(userId, "3333-4444", "Bob Duck", LocalDate.of(2030, 1, 1));
        ResponseEntity<CardResponse> createResponse = restTemplate.postForEntity(cardsUrl(), createRequest, CardResponse.class);
        Long cardId = createResponse.getBody().id();

        ResponseEntity<CardResponse> getResponse = restTemplate.getForEntity(cardsUrl() + "/" + cardId, CardResponse.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().number()).isEqualTo("3333-4444");
    }

    @Test
    void getCardById_shouldReturnNotFound_whenCardDoesNotExist() {
        assertThatThrownBy(() -> restTemplate.getForEntity(cardsUrl() + "/10000", CardResponse.class))
                .isInstanceOf(HttpClientErrorException.NotFound.class);
    }

    @Test
    void updateCard_shouldUpdateAndReturnCard() {
        Long userId = createTestUser();
        CardCreateRequest createRequest = new CardCreateRequest(userId, "1111-2222", "Bob Duck", LocalDate.of(2030, 1, 1));
        ResponseEntity<CardResponse> createResponse = restTemplate.postForEntity(cardsUrl(), createRequest, CardResponse.class);
        Long cardId = createResponse.getBody().id();

        CardUpdateRequest updateRequest = new CardUpdateRequest("3333-4444", "Bob Duck", LocalDate.of(2030, 1, 1));
        HttpEntity<CardUpdateRequest> entity = new HttpEntity<>(updateRequest);
        ResponseEntity<CardResponse> updateResponse = restTemplate.exchange(cardsUrl() + "/" + cardId, HttpMethod.PATCH, entity, CardResponse.class);

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody().number()).isEqualTo("3333-4444");
        assertThat(updateResponse.getBody().holder()).isEqualTo("Bob Duck");
    }

    @Test
    void deleteCard_shouldReturnNoContent() {
        Long userId = createTestUser();
        CardCreateRequest createRequest = new CardCreateRequest(userId, "1111-2222", "Bob Duck", LocalDate.of(2030, 1, 1));
        ResponseEntity<CardResponse> createResponse = restTemplate.postForEntity(cardsUrl(), createRequest, CardResponse.class);
        Long cardId = createResponse.getBody().id();

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(cardsUrl() + "/" + cardId, HttpMethod.DELETE, null, Void.class);

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThatThrownBy(() -> restTemplate.getForEntity(cardsUrl() + "/" + cardId, CardResponse.class))
                .isInstanceOf(HttpClientErrorException.NotFound.class);
    }

    @Test
    void activateCard_shouldActivateCard() {
        Long userId = createTestUser();
        CardCreateRequest createRequest = new CardCreateRequest(userId, "1111-2222", "Bob Duck", LocalDate.of(2030, 1, 1));
        ResponseEntity<CardResponse> createResponse = restTemplate.postForEntity(cardsUrl(), createRequest, CardResponse.class);
        Long cardId = createResponse.getBody().id();

        restTemplate.exchange(cardsUrl() + "/" + cardId + "/deactivate", HttpMethod.PATCH, null, Void.class);
        ResponseEntity<Void> activateResponse = restTemplate.exchange(cardsUrl() + "/" + cardId + "/activate", HttpMethod.PATCH, null, Void.class);

        assertThat(activateResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        ResponseEntity<CardResponse> getResponse = restTemplate.getForEntity(cardsUrl() + "/" + cardId, CardResponse.class);
        assertThat(getResponse.getBody().isActive()).isTrue();
    }

    @Test
    void getCardsByUser_shouldReturnPageOfCards() {
        Long userId = createTestUser();
        for (int i = 1; i <= 3; i++) {
            CardCreateRequest cardRequest = new CardCreateRequest(userId, "card" + i, "Bob Duck", LocalDate.of(2030, 1, 1));
            restTemplate.postForEntity(cardsUrl(), cardRequest, CardResponse.class);
        }

        ResponseEntity<String> pageResponse = restTemplate.getForEntity(
                cardsUrl() + "/user/" + userId + "?page=0&size=2",
                String.class);

        assertThat(pageResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(pageResponse.getBody()).contains("\"totalElements\":3");
        assertThat(pageResponse.getBody()).contains("\"numberOfElements\":2");
    }

    @Test
    void getActiveCardsByUser_shouldReturnListOfActiveCards() {
        Long userId = createTestUser();
        CardCreateRequest activeRequest = new CardCreateRequest(userId, "1111-2222", "Bob Duck", LocalDate.of(2030, 1, 1));
        restTemplate.postForEntity(cardsUrl(), activeRequest, CardResponse.class);

        CardCreateRequest inactiveRequest = new CardCreateRequest(userId, "3333-4444", "Bob Duck", LocalDate.of(2030, 1, 1));
        ResponseEntity<CardResponse> inactiveResponse = restTemplate.postForEntity(cardsUrl(), inactiveRequest, CardResponse.class);
        restTemplate.exchange(cardsUrl() + "/" + inactiveResponse.getBody().id() + "/deactivate", HttpMethod.PATCH, null, Void.class);

        ResponseEntity<CardResponse[]> getResponse = restTemplate.getForEntity(cardsUrl() + "/user/" + userId + "/active", CardResponse[].class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).hasSize(1);
        assertThat(getResponse.getBody()[0].isActive()).isTrue();
    }

    @Test
    void getUserWithCards_cacheShouldBeInvalidatedAfterCardCreation() {
        UserCreateRequest userRequest = new UserCreateRequest("Bob", "Duck", "bob@email.com", LocalDate.of(2000, 1, 1));
        ResponseEntity<UserResponse> userResponse = restTemplate.postForEntity(usersUrl(), userRequest, UserResponse.class);
        Long userId = userResponse.getBody().id();

        ResponseEntity<UserWithCardsDTO> responseBeforeCreation = restTemplate.getForEntity(usersUrl() + "/" + userId + "/with-cards", UserWithCardsDTO.class);
        assertThat(responseBeforeCreation.getBody().cards()).isEmpty();

        CardCreateRequest cardRequest = new CardCreateRequest(userId, "1111-2222", "Bob Duck", LocalDate.of(2030, 1, 1));
        restTemplate.postForEntity(cardsUrl(), cardRequest, CardResponse.class);

        ResponseEntity<UserWithCardsDTO> responseAfterCreation = restTemplate.getForEntity(usersUrl() + "/" + userId + "/with-cards", UserWithCardsDTO.class);
        assertThat(responseAfterCreation.getBody().cards()).hasSize(1);
        assertThat(responseAfterCreation.getBody().cards().get(0).number()).isEqualTo("1111-2222");
    }
}