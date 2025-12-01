package com.yhs.inventroysystem.presentation.delivery;

import com.yhs.inventroysystem.domain.delivery.DeliveryDocument;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public class DocumentDtos {

    public record DocumentUploadRequest(
            @Size(max = 500, message = "설명은 500자 이내여야 합니다")
            String description
    ) {}

    public record DocumentDescriptionUpdateRequest(
            @Size(max = 500, message = "설명은 500자 이내여야 합니다")
            String description
    ) {}

    public record DocumentResponse(
            Long id,
            Long deliveryId,
            String originalFileName,
            String storedFileName,
            Long fileSize,
            String contentType,
            String description,
            LocalDateTime createdAt
    ) {
        public static DocumentResponse from(DeliveryDocument document) {
            return new DocumentResponse(
                    document.getId(),
                    document.getDelivery().getId(),
                    document.getOriginalFileName(),
                    document.getStoredFileName(),
                    document.getFileSize(),
                    document.getContentType(),
                    document.getDescription(),
                    document.getCreatedAt()
            );
        }
    }

    public record DocumentListResponse(
            List<DocumentResponse> documents
    ) {
        public static DocumentListResponse from(List<DeliveryDocument> documents) {
            return new DocumentListResponse(
                    documents.stream()
                            .map(DocumentResponse::from)
                            .toList()
            );
        }
    }
}
