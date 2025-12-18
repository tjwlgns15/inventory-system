package com.yhs.inventroysystem.application.delivery;

import com.yhs.inventroysystem.application.delivery.DeliveryDocumentCommands.DeliveryDocumentDeleteCommand;
import com.yhs.inventroysystem.application.delivery.DeliveryDocumentCommands.DeliveryDocumentUpdateCommand;
import com.yhs.inventroysystem.application.delivery.DeliveryDocumentCommands.DeliveryDocumentUploadCommand;
import com.yhs.inventroysystem.domain.delivery.entity.Delivery;
import com.yhs.inventroysystem.domain.delivery.entity.DeliveryDocument;
import com.yhs.inventroysystem.domain.delivery.service.DeliveryDocumentDomainService;
import com.yhs.inventroysystem.domain.delivery.service.DeliveryDomainService;
import com.yhs.inventroysystem.infrastructure.file.FileStorageFactory;
import com.yhs.inventroysystem.infrastructure.file.FileStorageService;
import com.yhs.inventroysystem.infrastructure.file.FileStorageType;
import com.yhs.inventroysystem.infrastructure.file.FileUploadResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Transactional(readOnly = true)
@Slf4j
public class DeliveryDocumentService {

    private final DeliveryDomainService deliveryDomainService;
    private final DeliveryDocumentDomainService deliveryDocumentDomainService;

    private final FileStorageService fileStorageService;

    public DeliveryDocumentService(
            DeliveryDomainService deliveryDomainService,
            DeliveryDocumentDomainService deliveryDocumentDomainService,
            FileStorageFactory fileStorageFactory) {
        this.deliveryDomainService = deliveryDomainService;
        this.deliveryDocumentDomainService = deliveryDocumentDomainService;
        this.fileStorageService = fileStorageFactory.getStorageService(FileStorageType.DELIVERY_DOCUMENT);
    }

    @Transactional
    public DeliveryDocument uploadDocument(DeliveryDocumentUploadCommand command) {
        Delivery delivery = deliveryDomainService.findById(command.deliveryId());

        MultipartFile uploadFile = command.uploadFile();
        validateDocumentFile(uploadFile);

        FileUploadResult result = fileStorageService.store(
                uploadFile,
                FileStorageType.DELIVERY_DOCUMENT.getDirectory()
        );

        DeliveryDocument document = new DeliveryDocument(
                delivery,
                result.getOriginalFileName(),
                result.getStoredFileName(),
                result.getFilePath(),
                result.getFileSize(),
                uploadFile.getContentType()
        );

        if (command.description() != null && !command.description().isEmpty()) {
            document.updateDescription(command.description());
        }

        DeliveryDocument savedDocument = deliveryDocumentDomainService.saveDeliveryDocument(document);
        delivery.getDocuments().add(savedDocument);

        log.info("출하 문서 업로드 완료 - deliveryId: {}, fileName: {}",
                delivery.getId(), result.getOriginalFileName());

        return savedDocument;
    }

    public DeliveryDocument getDocument(Long documentId) {
        return deliveryDocumentDomainService.findById(documentId);
    }

    public List<DeliveryDocument> getDocumentsByDeliveryId(Long deliveryId) {
        return deliveryDocumentDomainService.getDocumentsByDeliveryId(deliveryId);
    }

    public byte[] getDocumentFile(Long documentId) {
        DeliveryDocument document = deliveryDocumentDomainService.findById(documentId);
        return fileStorageService.load(document.getFilePath());
    }
    @Transactional
    public DeliveryDocument updateDocumentDescription(DeliveryDocumentUpdateCommand command) {
        DeliveryDocument document = deliveryDocumentDomainService.findById(command.documentId());

        document.updateDescription(command.description());
        log.info("수주 관련 문서 설명 업데이트 - Document ID: {}", document.getId());

        return document;
    }

    @Transactional
    public void deleteDocument(DeliveryDocumentDeleteCommand command) {
        Delivery delivery = deliveryDomainService.findById(command.deliveryId());
        DeliveryDocument document = deliveryDocumentDomainService.findById(command.documentId());

        fileStorageService.delete(document.getFilePath());

        delivery.getDocuments().remove(document);
        deliveryDocumentDomainService.deleteDocument(document);

        log.info("출하 문서 삭제 완료 - deliveryId: {}, documentId: {}", command.deliveryId(), command.documentId());
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
