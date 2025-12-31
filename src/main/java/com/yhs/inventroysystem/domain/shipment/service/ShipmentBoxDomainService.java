package com.yhs.inventroysystem.domain.shipment.service;

import com.yhs.inventroysystem.domain.exception.ResourceNotFoundException;
import com.yhs.inventroysystem.domain.shipment.entity.Shipment;
import com.yhs.inventroysystem.domain.shipment.entity.ShipmentBox;
import com.yhs.inventroysystem.domain.shipment.entity.ShipmentType;
import com.yhs.inventroysystem.domain.shipment.repository.ShipmentBoxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ShipmentBoxDomainService {

    private final ShipmentBoxRepository shipmentBoxRepository;

    public ShipmentBox getShipmentBox(Long id){
        return shipmentBoxRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.box(id));
    }
    public ShipmentBox getShipmentBoxActivate(Long id){
        return shipmentBoxRepository.findByIdAndIsActive(id)
                .orElseThrow(() -> ResourceNotFoundException.box(id));
    }

    public List<ShipmentBox> findByIsActiveTrue() {
        return shipmentBoxRepository.findByIsActive();
    }
    public List<ShipmentBox> findAll() {
        return shipmentBoxRepository.findAll();
    }

    @Transactional
    public ShipmentBox save(ShipmentBox shipmentBox) {
        return shipmentBoxRepository.save(shipmentBox);
    }

    @Transactional
    public void delete(ShipmentBox shipmentBox) {
        shipmentBoxRepository.delete(shipmentBox);
    }
}
