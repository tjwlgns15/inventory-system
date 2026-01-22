package com.yhs.inventroysystem.application.quotation;

import com.yhs.inventroysystem.domain.quotation.entity.Quotation;
import com.yhs.inventroysystem.domain.quotation.entity.QuotationDocument;
import com.yhs.inventroysystem.domain.quotation.service.QuotationDocumentDomainService;
import com.yhs.inventroysystem.domain.quotation.service.QuotationDomainService;
import com.yhs.inventroysystem.domain.shipment.entity.ShipmentDocument;
import com.yhs.inventroysystem.infrastructure.file.FileStorageFactory;
import com.yhs.inventroysystem.infrastructure.file.FileStorageService;
import com.yhs.inventroysystem.infrastructure.file.FileStorageType;
import com.yhs.inventroysystem.infrastructure.file.FileUploadResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

import static com.yhs.inventroysystem.application.quotation.QuotationDocumentCommands.*;

@Service
@Transactional(readOnly = true)
@Slf4j
public class QuotationDocumentService {

    private final QuotationDomainService quotationDomainService;
    private final QuotationDocumentDomainService quotationDocumentDomainService;

    private final FileStorageService fileStorageService;

    public QuotationDocumentService(QuotationDomainService quotationDomainService,
                                    QuotationDocumentDomainService quotationDocumentDomainService,
                                    FileStorageFactory fileStorageFactory) {
        this.quotationDomainService = quotationDomainService;
        this.quotationDocumentDomainService = quotationDocumentDomainService;
        this.fileStorageService = fileStorageFactory.getStorageService(FileStorageType.QUOTATION_DOCUMENT);
    }

    @Transactional
    public QuotationDocument uploadDocument(QuotationDocumentUploadCommand command) {
        Quotation quotation = quotationDomainService.findById(command.quotationId());

        MultipartFile uploadFile = command.uploadFile();
        validateDocumentFile(uploadFile);

        FileUploadResult result = fileStorageService.store(
                uploadFile,
                FileStorageType.QUOTATION_DOCUMENT.getDirectory()
        );

        QuotationDocument document = quotationDocumentDomainService.createDocument(
                quotation,
                result.getOriginalFileName(),
                result.getStoredFileName(),
                result.getFilePath(),
                result.getFileSize(),
                uploadFile.getContentType(),
                command.description()
        );

        quotation.addDocument(document);

        log.info("견적서 관련 문서 업로드 완료 - quotation: {}, fileName: {}",
                quotation.getId(), result.getOriginalFileName());

        return document;
    }

    public List<QuotationDocument> getDocumentByQuotationId(Long quotationId) {
        return quotationDocumentDomainService.findByQuotationId(quotationId);
    }

    @Transactional
    public QuotationDocument updateDocumentDescription(QuotationDocumentUpdateCommand command) {
        QuotationDocument document = quotationDocumentDomainService.findById(command.documentId());

        document.updateDescription(command.description());
        log.info("견적서 관련 문서 설명 업데이트 - Document ID: {}", document.getId());

        return document;
    }

    @Transactional
    public void deleteDocument(QuotationDocumentDeleteCommand command) {
        Quotation quotation = quotationDomainService.findById(command.quotationId());
        QuotationDocument document = quotationDocumentDomainService.findById(command.documentId());

        fileStorageService.delete(document.getFilePath());

        quotation.removeDocument(document);
        quotationDocumentDomainService.deleteById(document.getId());

        log.info("견적서 문서 삭제 완료 - quotationId: {}, documentId: {}", command.quotationId(), command.documentId());
    }

    public QuotationDocument getDocument(Long documentId) {
        return quotationDocumentDomainService.findById(documentId);
    }

//    public byte[] getDocumentFile(Long documentId) {
//        QuotationDocument document = quotationDocumentDomainService.findById(documentId);
//        return fileStorageService.load(document.getFilePath());
//    }

    public InputStream getDocumentFileStream(Long documentId) {
        QuotationDocument document = quotationDocumentDomainService.findById(documentId);
        return fileStorageService.loadAsStream(document.getFilePath());
    }

    private void validateDocumentFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다");
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            throw new IllegalArgumentException("파일 형식을 확인할 수 없습니다");
        }

        // 허용되는 문서 타입 (PDF, Word, Excel, 이미지 등)
        boolean isValidType = contentType.equals("application/pdf")
                || contentType.equals("application/msword")
                || contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                || contentType.equals("application/vnd.ms-excel")
                || contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                || contentType.startsWith("image/");

        if (!isValidType) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다");
        }

        long maxSize = 50 * 1024 * 1024; // 50MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("파일 크기는 50MB를 초과할 수 없습니다");
        }
    }
}
