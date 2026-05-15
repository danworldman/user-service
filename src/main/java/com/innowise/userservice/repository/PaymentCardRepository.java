package com.innowise.userservice.repository;

import com.innowise.userservice.model.entity.PaymentCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long> {
    @Query("SELECT c FROM PaymentCard c WHERE c.user.id = :userId AND c.isActive = true")
    List<PaymentCard> findActivePaymentCardsByUserId(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE payment_cards SET active = :active WHERE id = :id", nativeQuery = true)
    int updateCardActiveStatus(@Param("id") Long id, @Param("active") boolean active);

    int countByUserId(Long id);

    Page<PaymentCard> findByUserId(Long userId, Pageable pageable);
}