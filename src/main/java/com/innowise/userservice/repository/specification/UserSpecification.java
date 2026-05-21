package com.innowise.userservice.repository.specification;

import com.innowise.userservice.model.entity.User;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class UserSpecification {

    public static Specification<User> hasName(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + name.toLowerCase() + "%"
            );
        };
    }

    public static Specification<User> hasSurname(String surname) {
        return (root, query, criteriaBuilder) -> {
            if (surname == null || surname.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("surname")),
                    "%" + surname.toLowerCase() + "%"
            );
        };
    }

    public static Specification<User> hasEmail(String email) {
        return (root, query, criteriaBuilder) -> {
            if (email == null || email.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    criteriaBuilder.lower(root.get("email")),
                    email.trim().toLowerCase()
            );
        };
    }

    public static Specification<User> hasStatus(Boolean isActive) {
        return (root, query, criteriaBuilder) -> {
            if (isActive == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(root.get("isActive"), isActive);
        };
    }

    public static Specification<User> berthDateBetween(LocalDate from, LocalDate to) {
        return (root, query, criteriaBuilder) -> {
            if (from == null && to == null) {
                return criteriaBuilder.conjunction();
            }
            if (from != null && to == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("birthDate"), from);
            }
            if (from == null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("birthDate"), to);
            }
            return criteriaBuilder.between(root.get("birthDate"), from, to);
        };
    }

    public static Specification<User> createdBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, criteriaBuilder) -> {
            if (from == null && to == null) {
                return criteriaBuilder.conjunction();
            }
            if (from != null && to == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), from);
            }
            if (from == null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), to);
            }
            return criteriaBuilder.between(root.get("createdAt"), from, to);
        };
    }

    public static Specification<User> updatedBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, criteriaBuilder) -> {
            if (from == null && to == null) {
                return criteriaBuilder.conjunction();
            }
            if (from != null && to == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("updatedAt"), from);
            }
            if (from == null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("updatedAt"), to);
            }
            return criteriaBuilder.between(root.get("updatedAt"), from, to);
        };
    }
}