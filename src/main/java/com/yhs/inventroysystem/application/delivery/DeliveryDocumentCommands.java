package com.yhs.inventroysystem.application.delivery;

import org.springframework.web.multipart.MultipartFile;

public class DeliveryDocumentCommands {

    public record DocumentUploadCommand(
            Long deliveryId,
            MultipartFile uploadFile,
            String description
    ) {}

    public record DocumentDeleteCommand(
            Long deliveryId,
            Long documentId
    ) {}

    public record DocumentUpdateCommand(
            Long documentId,
            String description
    ) {}

}
