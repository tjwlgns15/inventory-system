package com.yhs.inventroysystem.application.delivery;

import org.springframework.web.multipart.MultipartFile;

public class DeliveryDocumentCommands {

    public record DeliveryDocumentUploadCommand(
            Long deliveryId,
            MultipartFile uploadFile,
            String description
    ) {}

    public record DeliveryDocumentDeleteCommand(
            Long deliveryId,
            Long documentId
    ) {}

    public record DeliveryDocumentUpdateCommand(
            Long documentId,
            String description
    ) {}

}
