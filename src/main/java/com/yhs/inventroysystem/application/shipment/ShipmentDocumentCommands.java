package com.yhs.inventroysystem.application.shipment;

import org.springframework.web.multipart.MultipartFile;

public class ShipmentDocumentCommands {

    public record ShipmentDocumentUploadCommand(
            Long shipmentId,
            MultipartFile uploadFile,
            String description
    ) {}

    public record ShipmentDocumentDeleteCommand(
            Long shipmentId,
            Long documentId
    ) {}

    public record ShipmentDocumentUpdateCommand(
            Long documentId,
            String description
    ) {}

}
