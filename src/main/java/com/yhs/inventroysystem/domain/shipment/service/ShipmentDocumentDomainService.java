package com.yhs.inventroysystem.domain.shipment.service;

import com.yhs.inventroysystem.domain.exception.ResourceNotFoundException;
import com.yhs.inventroysystem.domain.shipment.entity.ShipmentDocument;
import com.yhs.inventroysystem.domain.shipment.repository.ShipmentDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ShipmentDocumentDomainService {

    private final ShipmentDocumentRepository shipmentDocumentRepository;

    @Transactional
    public ShipmentDocument saveShipmentDocument(ShipmentDocument document) {
        return shipmentDocumentRepository.save(document);
    }

    public ShipmentDocument findById(Long documentId) {
        return shipmentDocumentRepository.findById(documentId)
                .orElseThrow(() -> ResourceNotFoundException.document(documentId));
    }

    public List<ShipmentDocument> getDocumentsByShipmentId(Long shipmentId) {
        return shipmentDocumentRepository.findByShipmentId(shipmentId);
    }

    public void deleteDocument(ShipmentDocument document) {
        shipmentDocumentRepository.delete(document);
    }
}
