package com.innowise.userservice.dao;

import com.innowise.userservice.model.entity.PaymentCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentCardDAO extends JpaRepository<PaymentCard, Long>, JpaSpecificationExecutor<PaymentCard> {

    @Query(value = "SELECT * FROM payment_cards WHERE user_id = :userId AND active = true", nativeQuery = true)
    List<PaymentCard> findActivePaymentCardsByUserId(@Param("userId") Long userId);

    int countByUserId(Long id);

    Page<PaymentCard> findByUserId(Long userId, Pageable pageable);
}