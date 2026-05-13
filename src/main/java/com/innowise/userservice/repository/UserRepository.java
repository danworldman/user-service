package com.innowise.userservice.repository;

import com.innowise.userservice.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.paymentCards WHERE u.id = :id")
    Optional<User> findUsersWithPaymentCards(@Param("id") long id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.isActive = :status, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id = :id")
    int updateStatus(@Param("id") long id, @Param("status") boolean status);
}