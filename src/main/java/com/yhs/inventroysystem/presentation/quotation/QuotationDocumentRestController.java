package com.yhs.inventroysystem.presentation.quotation;

import com.yhs.inventroysystem.application.quotation.QuotationDocumentCommands;
import com.yhs.inventroysystem.application.quotation.QuotationDocumentCommands.QuotationDocumentDeleteCommand;
import com.yhs.inventroysystem.application.quotation.QuotationDocumentCommands.QuotationDocumentUpdateCommand;
import com.yhs.inventroysystem.application.quotation.QuotationDocumentCommands.QuotationDocumentUploadCommand;
import com.yhs.inventroysystem.application.quotation.QuotationDocumentService;
import com.yhs.inventroysystem.domain.quotation.QuotationDocument;
import com.yhs.inventroysystem.infrastructure.file.FileDownloadUtils;
import com.yhs.inventroysystem.presentation.delivery.DeliveryDocumentDtos;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.yhs.inventroysystem.presentation.quotation.QuotationDocumentDtos.*;
import static com.yhs.inventroysystem.presentation.quotation.QuotationDocumentDtos.QuotationDocumentDescriptionUpdateRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quotations/{quotationId}/documents")
public class QuotationDocumentRestController {

    private final QuotationDocumentService quotationDocumentService;


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<QuotationDocumentResponse> uploadDocument(
            @PathVariable Long quotationId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description) {

        QuotationDocumentUploadCommand command = new QuotationDocumentUploadCommand(
                quotationId,
                file,
                description
        );

        QuotationDocument document = quotationDocumentService.uploadDocument(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(QuotationDocumentResponse.from(document));
    }

    @GetMapping
    public ResponseEntity<QuotationDocumentListResponse> getAllDocuments(@PathVariable Long quotationId) {
        List<QuotationDocument> documents = quotationDocumentService.getDocumentByQuotationId(quotationId);

        return ResponseEntity.ok(QuotationDocumentListResponse.from(documents));
    }

    @PatchMapping("/{documentId}/description")
    public ResponseEntity<QuotationDocumentResponse> updateDocumentDescription(
            @PathVariable Long quotationId,
            @PathVariable Long documentId,
            @Valid @RequestBody QuotationDocumentDescriptionUpdateRequest request) {

        QuotationDocumentUpdateCommand command = new QuotationDocumentUpdateCommand(
                documentId,
                request.description()
        );

        QuotationDocument document = quotationDocumentService.updateDocumentDescription(command);
        return ResponseEntity.ok(QuotationDocumentResponse.from(document));
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long quotationId,
            @PathVariable Long documentId) {

        QuotationDocumentDeleteCommand command = new QuotationDocumentDeleteCommand(
                quotationId,
                documentId
        );

        quotationDocumentService.deleteDocument(command);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(
        @PathVariable Long quotationId,
        @PathVariable Long documentId) {

        QuotationDocument document = quotationDocumentService.getDocument(documentId);
        byte[] fileData = quotationDocumentService.getDocumentFile(documentId);

        ByteArrayResource resource = new ByteArrayResource(fileData);

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
                .contentLength(fileData.length)
                .body(resource);
    }
}
