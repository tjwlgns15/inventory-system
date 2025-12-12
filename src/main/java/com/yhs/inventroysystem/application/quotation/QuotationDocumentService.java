package com.yhs.inventroysystem.application.quotation;

import com.yhs.inventroysystem.domain.exception.ResourceNotFoundException;
import com.yhs.inventroysystem.domain.quotation.Quotation;
import com.yhs.inventroysystem.domain.quotation.QuotationDocument;
import com.yhs.inventroysystem.domain.quotation.QuotationDocumentRepository;
import com.yhs.inventroysystem.domain.quotation.QuotationRepository;
import com.yhs.inventroysystem.infrastructure.file.FileStorageFactory;
import com.yhs.inventroysystem.infrastructure.file.FileStorageService;
import com.yhs.inventroysystem.infrastructure.file.FileStorageType;
import com.yhs.inventroysystem.infrastructure.file.FileUploadResult;
import com.yhs.inventroysystem.presentation.quotation.QuotationDocumentDtos;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.yhs.inventroysystem.application.quotation.QuotationDocumentCommands.*;

@Service
@Transactional(readOnly = true)
@Slf4j
public class QuotationDocumentService {

    private final QuotationRepository quotationRepository;
    private final QuotationDocumentRepository quotationDocumentRepository;

    private final FileStorageService fileStorageService;

    public QuotationDocumentService(QuotationRepository quotationRepository,
                                    QuotationDocumentRepository quotationDocumentRepository,
                                    FileStorageFactory fileStorageFactory) {
        this.quotationRepository = quotationRepository;
        this.quotationDocumentRepository = quotationDocumentRepository;
        this.fileStorageService = fileStorageFactory.getStorageService(FileStorageType.QUOTATION_DOCUMENT);
    }

    @Transactional
    public QuotationDocument uploadDocument(QuotationDocumentUploadCommand command) {
        Quotation quotation = findQuotation(command.quotationId());

        MultipartFile uploadFile = command.uploadFile();
        validateDocumentFile(uploadFile);

        FileUploadResult result = fileStorageService.store(
                uploadFile,
                FileStorageType.QUOTATION_DOCUMENT.getDirectory()
        );

        QuotationDocument document = new QuotationDocument(
                quotation,
                result.getOriginalFileName(),
                result.getStoredFileName(),
                result.getFilePath(),
                result.getFileSize(),
                uploadFile.getContentType()
        );

        if (command.description() != null && !command.description().isEmpty()) {
            document.updateDescription(command.description());
        }

        QuotationDocument savedDocument = quotationDocumentRepository.save(document);
        quotation.addDocument(savedDocument);

        log.info("견적서 관련 문서 업로드 완료 - quotation: {}, fileName: {}",
                quotation.getId(), result.getOriginalFileName());

        return savedDocument;
    }

    private Quotation findQuotation(Long quotationId) {
        return quotationRepository.findById(quotationId)
                .orElseThrow(() -> ResourceNotFoundException.quotation(quotationId));
    }

    private QuotationDocument findQuotationDocument(Long documentId) {
        return quotationDocumentRepository.findById(documentId)
                .orElseThrow(() -> ResourceNotFoundException.document(documentId));
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

    public List<QuotationDocument> getDocumentByQuotationId(Long quotationId) {
        return quotationDocumentRepository.findByQuotationId(quotationId);
    }

    @Transactional
    public QuotationDocument updateDocumentDescription(QuotationDocumentUpdateCommand command) {
        QuotationDocument document = findQuotationDocument(command.documentId());

        document.updateDescription(command.description());
        log.info("견적서 관련 문서 설명 업데이트 - Document ID: {}", document.getId());

        return document;
    }

    @Transactional
    public void deleteDocument(QuotationDocumentDeleteCommand command) {
        Quotation quotation = findQuotation(command.quotationId());
        QuotationDocument document = findQuotationDocument(command.documentId());

        fileStorageService.delete(document.getFilePath());

        quotation.removeDocument(document);
        quotationDocumentRepository.delete(document);

        log.info("견적서 문서 삭제 완료 - quotationId: {}, documentId: {}", command.quotationId(), command.documentId());
    }

    public QuotationDocument getDocument(Long documentId) {
        return findQuotationDocument(documentId);
    }

    public byte[] getDocumentFile(Long documentId) {
        QuotationDocument document = findQuotationDocument(documentId);
        return fileStorageService.load(document.getFilePath());
    }
}
