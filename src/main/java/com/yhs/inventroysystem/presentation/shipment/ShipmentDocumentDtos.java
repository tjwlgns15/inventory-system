package com.yhs.inventroysystem.presentation.shipment;

import com.yhs.inventroysystem.domain.shipment.entity.ShipmentDocument;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public class ShipmentDocumentDtos {

    public record ShipmentDocumentDescriptionUpdateRequest(
            @Size(max = 500, message = "설명은 500자 이내여야 합니다")
            String description
    ) {}

    public record ShipmentDocumentResponse(
            Long id,
            Long shipmentId,
            String originalFileName,
            String storedFileName,
            Long fileSize,
            String contentType,
            String description,
            LocalDateTime createdAt
    ) {
        public static ShipmentDocumentResponse from(ShipmentDocument document) {
            return new ShipmentDocumentResponse(
                    document.getId(),
                    document.getShipment().getId(),
                    document.getOriginalFileName(),
                    document.getStoredFileName(),
                    document.getFileSize(),
                    document.getContentType(),
                    document.getDescription(),
                    document.getCreatedAt()
            );
        }
    }

    public record ShipmentDocumentListResponse(
            List<ShipmentDocumentResponse> documents
    ) {
        public static ShipmentDocumentListResponse from(List<ShipmentDocument> documents) {
            return new ShipmentDocumentListResponse(
                    documents.stream()
                            .map(ShipmentDocumentResponse::from)
                            .toList()
            );
        }
    }
}
