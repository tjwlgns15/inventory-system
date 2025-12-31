package com.yhs.inventroysystem.domain.shipment.repository;

import com.yhs.inventroysystem.domain.shipment.entity.ShipmentBox;
import com.yhs.inventroysystem.domain.shipment.entity.ShipmentItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShipmentItemRepository extends JpaRepository<ShipmentItem, Long> {

}
