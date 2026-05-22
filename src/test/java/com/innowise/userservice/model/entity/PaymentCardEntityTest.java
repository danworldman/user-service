package com.innowise.userservice.model.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentCardEntityTest {

    @Test
    void testEqualsAndHashCode() {
        PaymentCard firstPaymentCard = new PaymentCard();
        firstPaymentCard.setNumber("1111222233334444");

        PaymentCard secondPaymentCard = new PaymentCard();
        secondPaymentCard.setNumber("1111222233334444");

        PaymentCard therdPaymentCard = new PaymentCard();
        therdPaymentCard.setNumber("6666666666666666");

        assertThat(firstPaymentCard).isEqualTo(firstPaymentCard);
        assertThat(firstPaymentCard).isEqualTo(secondPaymentCard);
        assertThat(secondPaymentCard).isEqualTo(firstPaymentCard);

        PaymentCard fourthPaymentCard = new PaymentCard();
        fourthPaymentCard.setNumber("1111222233334444");

        assertThat(firstPaymentCard).isEqualTo(secondPaymentCard);
        assertThat(secondPaymentCard).isEqualTo(fourthPaymentCard);
        assertThat(firstPaymentCard).isEqualTo(fourthPaymentCard);
        assertThat(firstPaymentCard).isNotEqualTo(null);
        assertThat(firstPaymentCard).isNotEqualTo(new Object());
        assertThat(firstPaymentCard).isNotEqualTo(therdPaymentCard);
        assertThat(therdPaymentCard).isNotEqualTo(firstPaymentCard);
        assertThat(firstPaymentCard.hashCode()).isEqualTo(secondPaymentCard.hashCode());
        assertThat(firstPaymentCard.hashCode()).isNotEqualTo(therdPaymentCard.hashCode());

        Set<PaymentCard> set = new HashSet<>();
        set.add(firstPaymentCard);

        assertThat(set).contains(secondPaymentCard);
        assertThat(set).doesNotContain(therdPaymentCard);
    }

    @Test
    void testEqualsWithNullNumber() {
        PaymentCard firstPaymentCard = new PaymentCard();
        firstPaymentCard.setNumber(null);

        PaymentCard secondPaymentCard = new PaymentCard();
        secondPaymentCard.setNumber(null);

        PaymentCard therdPaymentCard = new PaymentCard();
        therdPaymentCard.setNumber("1111222233334444");

        assertThat(firstPaymentCard).isNotEqualTo(secondPaymentCard);
        assertThat(firstPaymentCard).isNotEqualTo(therdPaymentCard);
    }

    @Test
    void testEqualsWithMixedNullNumber() {
        PaymentCard paymentCard = new PaymentCard();
        paymentCard.setNumber("1111222233334444");

        PaymentCard paymentCardWithNullNumber = new PaymentCard();
        paymentCardWithNullNumber.setNumber(null);

        assertThat(paymentCard).isNotEqualTo(paymentCardWithNullNumber);
        assertThat(paymentCardWithNullNumber).isNotEqualTo(paymentCard);
    }

    @Test
    void testEqualsShouldIgnoreNonBusinessFields() {
        PaymentCard paymentCard = new PaymentCard();
        paymentCard.setNumber("1111222233334444");
        paymentCard.setId(1L);
        paymentCard.setHolder("Bob Duck");
        paymentCard.setExpirationDate(LocalDate.of(2030, 1, 1));
        paymentCard.setActive(true);

        PaymentCard anotherPaymentCard = new PaymentCard();
        anotherPaymentCard.setNumber("1111222233334444");
        anotherPaymentCard.setId(999L);
        anotherPaymentCard.setHolder("Bob Duck");
        anotherPaymentCard.setExpirationDate(LocalDate.of(2030, 1, 1));
        anotherPaymentCard.setActive(false);

        assertThat(paymentCard).isEqualTo(anotherPaymentCard);
        assertThat(paymentCard.hashCode()).isEqualTo(anotherPaymentCard.hashCode());
    }

    @Test
    void testHashCodeStability() {
        PaymentCard paymentCard = new PaymentCard();
        paymentCard.setNumber("1111222233334444");
        int first = paymentCard.hashCode();
        int second = paymentCard.hashCode();

        assertThat(first).isEqualTo(second);
    }

    @Test
    void testSetContainsAfterChangingNonBusinessFields() {
        PaymentCard oldPaymentCard = new PaymentCard();
        oldPaymentCard.setNumber("1111222233334444");
        oldPaymentCard.setHolder("Bob Duck");

        PaymentCard newPaymentCard = new PaymentCard();
        newPaymentCard.setNumber("1111222233334444");
        newPaymentCard.setHolder("Bob Duck");

        Set<PaymentCard> set = new HashSet<>();
        set.add(oldPaymentCard);

        assertThat(set).contains(newPaymentCard);
    }
}