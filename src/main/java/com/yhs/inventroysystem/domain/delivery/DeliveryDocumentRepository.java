package com.yhs.inventroysystem.domain.delivery;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryDocumentRepository extends JpaRepository<DeliveryDocument, Long> {

    @Query("SELECT d FROM DeliveryDocument  d " +
            "WHERE d.delivery.id = :deliveryId " +
            "ORDER BY d.createdAt DESC")
    List<DeliveryDocument> findByDeliveryId(@Param("deliveryId") Long deliveryId);

    @Query("SELECT d FROM DeliveryDocument d " +
            "WHERE d.id = :documentId " +
            "AND d.delivery.id = :deliveryId")
    Optional<DeliveryDocument> findByIdAndDeliveryId(
            @Param("documentId") Long documentId,
            @Param("deliveryId") Long deliveryId
    );
}
