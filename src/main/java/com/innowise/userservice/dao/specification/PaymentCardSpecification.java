package com.innowise.userservice.dao.specification;

import com.innowise.userservice.model.entity.PaymentCard;
import org.springframework.data.jpa.domain.Specification;

public class PaymentCardSpecification {

    private PaymentCardSpecification() {
    }

    public static Specification<PaymentCard> hasUserId(Long userId) {
        return (root, query, cb) -> {
            if (userId == null) return cb.conjunction();
            return cb.equal(root.get("user").get("id"), userId);
        };
    }

    public static Specification<PaymentCard> isActive(Boolean active) {
        return (root, query, cb) -> {
            if (active == null) return cb.conjunction();
            return cb.equal(root.get("isActive"), active);
        };
    }

    public static Specification<PaymentCard> hasHolder(String holder) {
        return (root, query, cb) -> {
            if (holder == null || holder.isBlank()) return cb.conjunction();
            return cb.like(cb.lower(root.get("holder")), "%" + holder.toLowerCase() + "%");
        };
    }

    public static Specification<PaymentCard> hasUserName(String firstName) {
        return (root, query, cb) -> {
            if (firstName == null || firstName.isBlank()) return cb.conjunction();
            return cb.like(cb.lower(root.get("user").get("name")), "%" + firstName.toLowerCase() + "%");
        };
    }

    public static Specification<PaymentCard> hasUserSurName(String lastName) {
        return (root, query, cb) -> {
            if (lastName == null || lastName.isBlank()) return cb.conjunction();
            return cb.like(cb.lower(root.get("user").get("surname")), "%" + lastName.toLowerCase() + "%");
        };
    }
}