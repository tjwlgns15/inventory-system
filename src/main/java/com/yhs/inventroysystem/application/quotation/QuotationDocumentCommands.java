package com.yhs.inventroysystem.application.quotation;

import org.springframework.web.multipart.MultipartFile;

public class QuotationDocumentCommands {
    public record QuotationDocumentUploadCommand(
            Long quotationId,
            MultipartFile uploadFile,
            String description
    ) {}

    public record QuotationDocumentUpdateCommand(
            Long documentId,
            String description
    ) {}

    public record QuotationDocumentDeleteCommand(
            Long quotationId,
            Long documentId
    ) {}


}
