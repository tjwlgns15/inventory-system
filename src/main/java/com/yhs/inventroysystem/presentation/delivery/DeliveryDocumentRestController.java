package com.yhs.inventroysystem.presentation.delivery;

import com.yhs.inventroysystem.application.delivery.DeliveryDocumentCommands;
import com.yhs.inventroysystem.application.delivery.DeliveryDocumentService;
import com.yhs.inventroysystem.domain.delivery.DeliveryDocument;
import com.yhs.inventroysystem.infrastructure.file.FileDownloadUtils;
import com.yhs.inventroysystem.presentation.delivery.DocumentDtos.DocumentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;

import static com.yhs.inventroysystem.application.delivery.DeliveryDocumentCommands.*;
import static com.yhs.inventroysystem.presentation.delivery.DocumentDtos.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/deliveries/{deliveryId}/documents")
public class DeliveryDocumentRestController {

    private final DeliveryDocumentService deliveryDocumentService;


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> uploadDocument(
            @PathVariable Long deliveryId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description) {

        DocumentUploadCommand command = new DocumentUploadCommand(
                deliveryId,
                file,
                description
        );

        DeliveryDocument document = deliveryDocumentService.uploadDocument(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(DocumentResponse.from(document));
    }

    @GetMapping
    public ResponseEntity<DocumentListResponse> getAllDocuments(@PathVariable Long deliveryId) {
        List<DeliveryDocument> documents = deliveryDocumentService.getDocumentsByDeliveryId(deliveryId);
        return ResponseEntity.ok(DocumentListResponse.from(documents));
    }

    @PatchMapping("/{documentId}/description")
    public ResponseEntity<DocumentResponse> updateDocumentDescription(
            @PathVariable Long deliveryId,
            @PathVariable Long documentId,
            @Valid @RequestBody DocumentDescriptionUpdateRequest request) {

        DocumentUpdateCommand command = new DocumentUpdateCommand(
                documentId,
                request.description()
        );

        DeliveryDocument document = deliveryDocumentService.updateDocumentDescription(command);
        return ResponseEntity.ok(DocumentResponse.from(document));
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long deliveryId,
            @PathVariable Long documentId) {

        DocumentDeleteCommand command = new DocumentDeleteCommand(
                deliveryId,
                documentId
        );
        deliveryDocumentService.deleteDocument(command);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long deliveryId,
            @PathVariable Long documentId,
            @RequestHeader(value = "User-Agent", required = false) String userAgent) {

        DeliveryDocument document = deliveryDocumentService.getDocument(documentId);
        byte[] fileData = deliveryDocumentService.getDocumentFile(documentId);

        ByteArrayResource resource = new ByteArrayResource(fileData);

        String contentDisposition = FileDownloadUtils.createContentDisposition(
                document.getOriginalFileName(),
                userAgent
        );

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        document.getContentType() != null
                                ? document.getContentType()
                                : MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .contentLength(fileData.length)
                .body(resource);
    }
}

