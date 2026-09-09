package com.yhs.inventroysystem.application.contract;

import org.springframework.web.multipart.MultipartFile;

public class ContractDocumentCommands {

    public record ContractDocumentUploadCommand(
            Long contractId,
            MultipartFile uploadFile,
            String description
    ) {}

    public record ContractDocumentUpdateCommand(
            Long documentId,
            String description
    ) {}

    public record ContractDocumentDeleteCommand(
            Long contractId,
            Long documentId
    ) {}
}
