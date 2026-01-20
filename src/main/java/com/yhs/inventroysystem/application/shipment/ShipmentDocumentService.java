package com.yhs.inventroysystem.application.shipment;

import com.yhs.inventroysystem.domain.shipment.entity.Shipment;
import com.yhs.inventroysystem.domain.shipment.entity.ShipmentDocument;
import com.yhs.inventroysystem.domain.shipment.service.ShipmentDocumentDomainService;
import com.yhs.inventroysystem.domain.shipment.service.ShipmentDomainService;
import com.yhs.inventroysystem.infrastructure.file.FileStorageFactory;
import com.yhs.inventroysystem.infrastructure.file.FileStorageService;
import com.yhs.inventroysystem.infrastructure.file.FileStorageType;
import com.yhs.inventroysystem.infrastructure.file.FileUploadResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.yhs.inventroysystem.application.shipment.ShipmentDocumentCommands.*;

@Service
@Transactional(readOnly = true)
@Slf4j
public class ShipmentDocumentService {

    private final ShipmentDomainService shipmentDomainService;
    private final ShipmentDocumentDomainService shipmentDocumentDomainService;

    private final FileStorageService fileStorageService;

    public ShipmentDocumentService(ShipmentDomainService shipmentDomainService,
                                   ShipmentDocumentDomainService shipmentDocumentDomainService,
                                   FileStorageFactory fileStorageFactory) {
        this.shipmentDomainService = shipmentDomainService;
        this.shipmentDocumentDomainService = shipmentDocumentDomainService;
        this.fileStorageService = fileStorageFactory.getStorageService(FileStorageType.SHIPMENT_DOCUMENT);
    }


    @Transactional
    public ShipmentDocument uploadDocument(ShipmentDocumentUploadCommand command) {
        Shipment shipment = shipmentDomainService.getShipment(command.shipmentId());

        MultipartFile uploadFile = command.uploadFile();
        validateDocumentFile(uploadFile);

        FileUploadResult result = fileStorageService.store(
                uploadFile,
                FileStorageType.SHIPMENT_DOCUMENT.getDirectory()
        );

        ShipmentDocument document = new ShipmentDocument(
                shipment,
                result.getOriginalFileName(),
                result.getStoredFileName(),
                result.getFilePath(),
                result.getFileSize(),
                uploadFile.getContentType()
        );

        if (command.description() != null && !command.description().isEmpty()) {
            document.updateDescription(command.description());
        }

        ShipmentDocument savedDocument = shipmentDocumentDomainService.saveShipmentDocument(document);
        shipment.getDocuments().add(savedDocument);

        log.info("선적 관련 문서 업로드 완료 - shipmentId: {}, fileName: {}",
                shipment.getId(), result.getOriginalFileName());

        return savedDocument;
    }

    public ShipmentDocument getDocument(Long documentId) {
        return shipmentDocumentDomainService.findById(documentId);
    }

    public List<ShipmentDocument> getDocumentsByShipmentId(Long shipmentId) {
        return shipmentDocumentDomainService.getDocumentsByShipmentId(shipmentId);
    }

    public byte[] getDocumentFile(Long documentId) {
        ShipmentDocument document = shipmentDocumentDomainService.findById(documentId);
        return fileStorageService.load(document.getFilePath());
    }
    @Transactional
    public ShipmentDocument updateDocumentDescription(ShipmentDocumentUpdateCommand command) {
        ShipmentDocument document = shipmentDocumentDomainService.findById(command.documentId());

        document.updateDescription(command.description());
        log.info("선적 관련 문서 설명 업데이트 - Document ID: {}", document.getId());

        return document;
    }

    @Transactional
    public void deleteDocument(ShipmentDocumentDeleteCommand command) {
        Shipment shipment = shipmentDomainService.getShipment(command.shipmentId());
        ShipmentDocument document = shipmentDocumentDomainService.findById(command.documentId());

        fileStorageService.delete(document.getFilePath());

        shipment.getDocuments().remove(document);
        shipmentDocumentDomainService.deleteDocument(document);

        log.info("선적 관련 문서 삭제 완료 - shipmentId: {}, documentId: {}", command.shipmentId(), command.documentId());
    }

    private void validateDocumentFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다");
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            throw new IllegalArgumentException("파일 형식을 확인할 수 없습니다");
        }

//        // 허용되는 문서 타입 (PDF, Word, Excel, 이미지 등)
//        boolean isValidType = contentType.equals("application/pdf")
//                || contentType.equals("application/msword")
//                || contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
//                || contentType.equals("application/vnd.ms-excel")
//                || contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
//                || contentType.startsWith("image/");
//
//        if (!isValidType) {
//            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다");
//        }
//
//        long maxSize = 50 * 1024 * 1024; // 50MB
//        if (file.getSize() > maxSize) {
//            throw new IllegalArgumentException("파일 크기는 50MB를 초과할 수 없습니다");
//        }
    }
}