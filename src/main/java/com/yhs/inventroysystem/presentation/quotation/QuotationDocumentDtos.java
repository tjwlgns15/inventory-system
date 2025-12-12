package com.yhs.inventroysystem.presentation.quotation;

import com.yhs.inventroysystem.domain.quotation.QuotationDocument;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public class QuotationDocumentDtos {

    public record QuotationDocumentDescriptionUpdateRequest(
            @Size(max = 500, message = "설명은 500자 이내여야 합니다")
            String description
    ) {}

    public record QuotationDocumentResponse(
            Long id,
            Long quotationId,
            String originalFileName,
            String storedFileName,
            Long fileSize,
            String contentType,
            String description,
            LocalDateTime createdAt
    ) {
        public static QuotationDocumentResponse from(QuotationDocument document) {
            return new QuotationDocumentResponse(
                    document.getId(),
                    document.getQuotation().getId(),
                    document.getOriginalFileName(),
                    document.getStoredFileName(),
                    document.getFileSize(),
                    document.getContentType(),
                    document.getDescription(),
                    document.getCreatedAt()
            );
        }
    }

    public record QuotationDocumentListResponse(
            List<QuotationDocumentResponse> documents
    ) {
        public static QuotationDocumentListResponse from(List<QuotationDocument> documents) {
            return new QuotationDocumentListResponse(
                    documents.stream()
                            .map(QuotationDocumentResponse::from)
                            .toList()
            );
        }
    }
}
