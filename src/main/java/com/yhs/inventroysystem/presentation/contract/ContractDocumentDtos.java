package com.yhs.inventroysystem.presentation.contract;

import com.yhs.inventroysystem.domain.contract.entity.ContractDocument;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public class ContractDocumentDtos {

    public record ContractDocumentDescriptionUpdateRequest(
            @Size(max = 500, message = "설명은 500자 이내여야 합니다")
            String description
    ) {}

    public record ContractDocumentResponse(
            Long id,
            Long contractId,
            String originalFileName,
            String storedFileName,
            Long fileSize,
            String contentType,
            String description,
            LocalDateTime createdAt
    ) {
        public static ContractDocumentResponse from(ContractDocument document) {
            return new ContractDocumentResponse(
                    document.getId(),
                    document.getContract().getId(),
                    document.getOriginalFileName(),
                    document.getStoredFileName(),
                    document.getFileSize(),
                    document.getContentType(),
                    document.getDescription(),
                    document.getCreatedAt()
            );
        }
    }

    public record ContractDocumentListResponse(
            List<ContractDocumentResponse> documents
    ) {
        public static ContractDocumentListResponse from(List<ContractDocument> documents) {
            return new ContractDocumentListResponse(
                    documents.stream()
                            .map(ContractDocumentResponse::from)
                            .toList()
            );
        }
    }
}