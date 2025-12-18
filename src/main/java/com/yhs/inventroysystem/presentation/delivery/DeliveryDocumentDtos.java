package com.yhs.inventroysystem.presentation.delivery;

import com.yhs.inventroysystem.domain.delivery.entity.DeliveryDocument;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public class DeliveryDocumentDtos {

    public record DeliveryDocumentDescriptionUpdateRequest(
            @Size(max = 500, message = "설명은 500자 이내여야 합니다")
            String description
    ) {}

    public record DeliveryDocumentResponse(
            Long id,
            Long deliveryId,
            String originalFileName,
            String storedFileName,
            Long fileSize,
            String contentType,
            String description,
            LocalDateTime createdAt
    ) {
        public static DeliveryDocumentResponse from(DeliveryDocument document) {
            return new DeliveryDocumentResponse(
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

    public record DeliveryDocumentListResponse(
            List<DeliveryDocumentResponse> documents
    ) {
        public static DeliveryDocumentListResponse from(List<DeliveryDocument> documents) {
            return new DeliveryDocumentListResponse(
                    documents.stream()
                            .map(DeliveryDocumentResponse::from)
                            .toList()
            );
        }
    }
}
