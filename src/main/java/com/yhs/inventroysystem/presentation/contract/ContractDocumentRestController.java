package com.yhs.inventroysystem.presentation.contract;

import com.yhs.inventroysystem.application.contract.ContractDocumentService;
import com.yhs.inventroysystem.domain.contract.entity.ContractDocument;
import com.yhs.inventroysystem.infrastructure.file.FileDownloadUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

import static com.yhs.inventroysystem.application.contract.ContractDocumentCommands.*;
import static com.yhs.inventroysystem.presentation.contract.ContractDocumentDtos.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/contracts/{contractId}/documents")
public class ContractDocumentRestController {

    private final ContractDocumentService contractDocumentService;


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ContractDocumentResponse> uploadDocument(
            @PathVariable Long contractId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description) {

        ContractDocumentUploadCommand command = new ContractDocumentUploadCommand(
                contractId,
                file,
                description
        );

        ContractDocument document = contractDocumentService.uploadDocument(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(ContractDocumentResponse.from(document));
    }

    @GetMapping
    public ResponseEntity<ContractDocumentListResponse> getAllDocuments(@PathVariable Long contractId) {
        List<ContractDocument> documents = contractDocumentService.getDocumentsByContractId(contractId);

        return ResponseEntity.ok(ContractDocumentListResponse.from(documents));
    }

    @PatchMapping("/{documentId}/description")
    public ResponseEntity<ContractDocumentResponse> updateDocumentDescription(
            @PathVariable Long contractId,
            @PathVariable Long documentId,
            @Valid @RequestBody ContractDocumentDescriptionUpdateRequest request) {

        ContractDocumentUpdateCommand command = new ContractDocumentUpdateCommand(
                documentId,
                request.description()
        );

        ContractDocument document = contractDocumentService.updateDocumentDescription(command);
        return ResponseEntity.ok(ContractDocumentResponse.from(document));
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long contractId,
            @PathVariable Long documentId) {

        ContractDocumentDeleteCommand command = new ContractDocumentDeleteCommand(
                contractId,
                documentId
        );

        contractDocumentService.deleteDocument(command);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long contractId,
            @PathVariable Long documentId) {

        ContractDocument document = contractDocumentService.getDocument(documentId);
        InputStream inputStream = contractDocumentService.getDocumentFileStream(documentId);
        InputStreamResource resource = new InputStreamResource(inputStream);

        String contentType = document.getContentType() != null
                ? document.getContentType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        boolean shouldDisplayInline = FileDownloadUtils.shouldDisplayInline(contentType);

        String contentDisposition = FileDownloadUtils.createContentDisposition(
                document.getOriginalFileName(),
                shouldDisplayInline
        );

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .contentLength(document.getFileSize())
                .body(resource);
    }
}