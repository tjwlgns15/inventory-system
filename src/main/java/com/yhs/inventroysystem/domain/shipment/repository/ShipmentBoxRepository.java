package com.yhs.inventroysystem.domain.shipment.repository;

import com.yhs.inventroysystem.domain.shipment.entity.Shipment;
import com.yhs.inventroysystem.domain.shipment.entity.ShipmentBox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentBoxRepository extends JpaRepository<ShipmentBox, Long> {

    @Query("SELECT sb FROM ShipmentBox sb " +
            "WHERE sb.isActive IS true")
    List<ShipmentBox> findByIsActive();

    @Query("SELECT sb FROM ShipmentBox sb " +
            "WHERE sb.id = :id " +
            "AND sb.isActive IS true")
    Optional<ShipmentBox> findByIdAndIsActive(@Param("id") Long id);
}
