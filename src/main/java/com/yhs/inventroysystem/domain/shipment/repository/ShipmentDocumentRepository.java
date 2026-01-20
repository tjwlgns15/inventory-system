package com.yhs.inventroysystem.domain.shipment.repository;

import com.yhs.inventroysystem.domain.shipment.entity.ShipmentDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentDocumentRepository extends JpaRepository<ShipmentDocument, Long> {

    @Query("SELECT sd FROM ShipmentDocument sd " +
            "WHERE sd.shipment.id = :shipmentId " +
            "ORDER BY sd.createdAt DESC")
    List<ShipmentDocument> findByShipmentId(@Param("shipmentId") Long shipmentId);

    @Query("SELECT sd FROM ShipmentDocument sd " +
            "WHERE sd.id = :documentId " +
            "AND sd.shipment.id = :shipmentId")
    Optional<ShipmentDocument> findByIdAndShipmentId(
            @Param("documentId") Long documentId,
            @Param("shipmentId") Long shipmentId
    );
}
