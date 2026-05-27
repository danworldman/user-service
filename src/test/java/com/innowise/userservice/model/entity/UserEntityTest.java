package com.innowise.userservice.model.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserEntityTest {

    @Test
    void testEqualsAndHashCode() {
        User firstUser = new User();
        firstUser.setId(1L);
        firstUser.setEmail("bob@email.com");

        User secondUser = new User();
        secondUser.setId(1L);
        secondUser.setEmail("sam@email.com");

        User thirdUser = new User();
        thirdUser.setId(2L);
        thirdUser.setEmail("bob@email.com");
        
        assertThat(firstUser).isEqualTo(firstUser);
        assertThat(firstUser).isEqualTo(secondUser);
        assertThat(secondUser).isEqualTo(firstUser);

        User fourthUser = new User();
        fourthUser.setId(1L);
        fourthUser.setEmail("bob@email.com");

        assertThat(firstUser).isEqualTo(secondUser);
        assertThat(secondUser).isEqualTo(fourthUser);
        assertThat(firstUser).isEqualTo(fourthUser);
        assertThat(firstUser).isNotEqualTo(null);
        assertThat(firstUser).isNotEqualTo(new Object());
        assertThat(firstUser).isNotEqualTo(thirdUser);
        assertThat(thirdUser).isNotEqualTo(firstUser);
        assertThat(firstUser.hashCode()).isEqualTo(secondUser.hashCode());
        assertThat(firstUser.hashCode()).isNotEqualTo(thirdUser.hashCode());

        Set<User> set = new HashSet<>();
        set.add(firstUser);

        assertThat(set).contains(secondUser);
        assertThat(set).doesNotContain(thirdUser);
    }

    @Test
    void testEqualsWithNullEmail() {
        User firstUser = new User();
        firstUser.setEmail(null);

        User secondUser = new User();
        secondUser.setEmail(null);

        User thirdUser = new User();
        thirdUser.setEmail("bob@email.com");

        assertThat(firstUser).isNotEqualTo(secondUser);
        assertThat(firstUser).isNotEqualTo(thirdUser);
    }

    @Test
    void testEqualsWithMixedNullEmail() {
        User user = new User();
        user.setEmail("bob@email.com");

        User userWithNullEmail = new User();
        userWithNullEmail.setEmail(null);

        assertThat(user).isNotEqualTo(userWithNullEmail);
        assertThat(userWithNullEmail).isNotEqualTo(user);
    }

    @Test
    void testEqualsShouldIgnoreNonBusinessFields() {
        User user = new User();
        user.setEmail("bob@email.com");
        user.setId(1L);
        user.setName("Bob");
        user.setSurname("Duck");
        user.setBirthDate(LocalDate.of(2000, 1, 1));
        user.setActive(true);

        User anotherUser = new User();
        anotherUser.setEmail("sam@email.com");
        anotherUser.setId(1L);
        anotherUser.setName("Sam");
        anotherUser.setSurname("Hock");
        anotherUser.setBirthDate(LocalDate.of(2000, 1, 1));
        anotherUser.setActive(false);

        assertThat(user).isEqualTo(anotherUser);
        assertThat(user.hashCode()).isEqualTo(anotherUser.hashCode());
    }

    @Test
    void testHashCodeStability() {
        User user = new User();
        user.setId(1L);
        user.setEmail("bob@email.com");
        int first = user.hashCode();
        int second = user.hashCode();

        assertThat(first).isEqualTo(second);
    }

    @Test
    void testSetContainsAfterChangingNonBusinessFields() {
        User oldUser = new User();
        oldUser.setId(1L);
        oldUser.setEmail("bob@email.com");
        oldUser.setName("Bob");

        User newUser = new User();
        newUser.setId(1L);
        newUser.setEmail("sam@email.com");
        newUser.setName("Sam");

        Set<User> set = new HashSet<>();
        set.add(oldUser);

        assertThat(set).contains(newUser);
    }
}