package com.yhs.inventroysystem.domain.delivery.service;

import com.yhs.inventroysystem.application.delivery.DeliveryDocumentCommands.DeliveryDocumentDeleteCommand;
import com.yhs.inventroysystem.application.delivery.DeliveryDocumentCommands.DeliveryDocumentUpdateCommand;
import com.yhs.inventroysystem.application.delivery.DeliveryDocumentCommands.DeliveryDocumentUploadCommand;
import com.yhs.inventroysystem.domain.delivery.entity.Delivery;
import com.yhs.inventroysystem.domain.delivery.entity.DeliveryDocument;
import com.yhs.inventroysystem.domain.delivery.repository.DeliveryDocumentRepository;
import com.yhs.inventroysystem.domain.delivery.repository.DeliveryRepository;
import com.yhs.inventroysystem.domain.exception.ResourceNotFoundException;
import com.yhs.inventroysystem.infrastructure.file.FileStorageFactory;
import com.yhs.inventroysystem.infrastructure.file.FileStorageService;
import com.yhs.inventroysystem.infrastructure.file.FileStorageType;
import com.yhs.inventroysystem.infrastructure.file.FileUploadResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DeliveryDocumentDomainService {

    private final DeliveryDocumentRepository deliveryDocumentRepository;

    @Transactional
    public DeliveryDocument saveDeliveryDocument(DeliveryDocument document) {
        return deliveryDocumentRepository.save(document);
    }

    public DeliveryDocument findById(Long documentId) {
        return deliveryDocumentRepository.findById(documentId)
                .orElseThrow(() -> ResourceNotFoundException.document(documentId));
    }

    public List<DeliveryDocument> getDocumentsByDeliveryId(Long deliveryId) {
        return deliveryDocumentRepository.findByDeliveryId(deliveryId);
    }

    public void deleteDocument(DeliveryDocument document) {
        deliveryDocumentRepository.delete(document);
    }
}
