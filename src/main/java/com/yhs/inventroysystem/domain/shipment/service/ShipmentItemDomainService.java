package com.yhs.inventroysystem.domain.shipment.service;

import com.yhs.inventroysystem.domain.shipment.repository.ShipmentBoxRepository;
import com.yhs.inventroysystem.domain.shipment.repository.ShipmentItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ShipmentItemDomainService {

    private final ShipmentItemRepository shipmentItemRepository;
}
