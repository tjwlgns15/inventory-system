package com.yhs.inventroysystem.domain.quotation.service;

import com.yhs.inventroysystem.domain.exception.ResourceNotFoundException;
import com.yhs.inventroysystem.domain.quotation.entity.Quotation;
import com.yhs.inventroysystem.domain.quotation.entity.QuotationDocument;
import com.yhs.inventroysystem.domain.quotation.repository.QuotationDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class QuotationDocumentDomainService {

    private final QuotationDocumentRepository quotationDocumentRepository;


    @Transactional
    public QuotationDocument createDocument(
            Quotation quotation,
            String originalFileName,
            String storedFileName,
            String filePath,
            Long fileSize,
            String contentType,
            String description) {

        validateFileInfo(originalFileName, storedFileName, filePath, fileSize);

        QuotationDocument document = new QuotationDocument(
                quotation,
                originalFileName,
                storedFileName,
                filePath,
                fileSize,
                contentType
        );

        if (description != null && !description.trim().isEmpty()) {
            document.updateDescription(description);
        }

        return quotationDocumentRepository.save(document);
    }

    public QuotationDocument findById(Long documentId) {
        return quotationDocumentRepository.findById(documentId)
                .orElseThrow(() -> ResourceNotFoundException.document(documentId));
    }

    public List<QuotationDocument> findByQuotationId(Long quotationId) {
        return quotationDocumentRepository.findByQuotationId(quotationId);
    }

    @Transactional
    public void deleteById(Long documentId) {
        quotationDocumentRepository.deleteById(documentId);
    }

    private void validateFileInfo(String originalFileName, String storedFileName,
                                         String filePath, Long fileSize) {
        if (originalFileName == null || originalFileName.trim().isEmpty()) {
            throw new IllegalArgumentException("원본 파일명은 필수입니다.");
        }
        if (storedFileName == null || storedFileName.trim().isEmpty()) {
            throw new IllegalArgumentException("저장된 파일명은 필수입니다.");
        }
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("파일 경로는 필수입니다.");
        }
        if (fileSize == null || fileSize <= 0) {
            throw new IllegalArgumentException("파일 크기는 0보다 커야 합니다.");
        }
    }
}
