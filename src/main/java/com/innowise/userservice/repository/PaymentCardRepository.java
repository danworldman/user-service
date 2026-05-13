package com.innowise.userservice.repository;

import com.innowise.userservice.model.entity.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface PaymentCardRepository extends JpaRepository<PaymentCard, Long> {
    List<PaymentCard> findByUserId(long userId);

    @Query("SELECT c FROM PaymentCard c WHERE c.user.id = :userId AND c.isActive = true")
    List<PaymentCard> findActiveCardsByUserId(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE payment_cards SET active = :active WHERE id = :id", nativeQuery = true)
    int updateCardActiveStatus(@Param("id") Long id, @Param("active") boolean active);

    int countByUserId(Long userId);
}