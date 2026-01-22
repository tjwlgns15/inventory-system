package com.yhs.inventroysystem.presentation.delivery;

import com.yhs.inventroysystem.application.delivery.DeliveryDocumentService;
import com.yhs.inventroysystem.domain.delivery.entity.DeliveryDocument;
import com.yhs.inventroysystem.infrastructure.file.FileDownloadUtils;
import com.yhs.inventroysystem.presentation.delivery.DeliveryDocumentDtos.DeliveryDocumentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
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

import static com.yhs.inventroysystem.application.delivery.DeliveryDocumentCommands.*;
import static com.yhs.inventroysystem.presentation.delivery.DeliveryDocumentDtos.DeliveryDocumentDescriptionUpdateRequest;
import static com.yhs.inventroysystem.presentation.delivery.DeliveryDocumentDtos.DeliveryDocumentListResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/deliveries/{deliveryId}/documents")
public class DeliveryDocumentRestController {

    private final DeliveryDocumentService deliveryDocumentService;


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DeliveryDocumentResponse> uploadDocument(
            @PathVariable Long deliveryId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description) {

        DeliveryDocumentUploadCommand command = new DeliveryDocumentUploadCommand(
                deliveryId,
                file,
                description
        );

        DeliveryDocument document = deliveryDocumentService.uploadDocument(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(DeliveryDocumentResponse.from(document));
    }

    @GetMapping
    public ResponseEntity<DeliveryDocumentListResponse> getAllDocuments(@PathVariable Long deliveryId) {
        List<DeliveryDocument> documents = deliveryDocumentService.getDocumentsByDeliveryId(deliveryId);
        return ResponseEntity.ok(DeliveryDocumentListResponse.from(documents));
    }

    @PatchMapping("/{documentId}/description")
    public ResponseEntity<DeliveryDocumentResponse> updateDocumentDescription(
            @PathVariable Long deliveryId,
            @PathVariable Long documentId,
            @Valid @RequestBody DeliveryDocumentDescriptionUpdateRequest request) {

        DeliveryDocumentUpdateCommand command = new DeliveryDocumentUpdateCommand(
                documentId,
                request.description()
        );

        DeliveryDocument document = deliveryDocumentService.updateDocumentDescription(command);
        return ResponseEntity.ok(DeliveryDocumentResponse.from(document));
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long deliveryId,
            @PathVariable Long documentId) {

        DeliveryDocumentDeleteCommand command = new DeliveryDocumentDeleteCommand(
                deliveryId,
                documentId
        );
        deliveryDocumentService.deleteDocument(command);

        return ResponseEntity.noContent().build();
    }

//    @GetMapping("/{documentId}/download")
//    public ResponseEntity<Resource> downloadDocument(
//            @PathVariable Long deliveryId,
//            @PathVariable Long documentId) {
//
//        DeliveryDocument document = deliveryDocumentService.getDocument(documentId);
//        byte[] fileData = deliveryDocumentService.getDocumentFile(documentId);
//
//        ByteArrayResource resource = new ByteArrayResource(fileData);
//
//        String contentType = document.getContentType() != null
//                ? document.getContentType()
//                : MediaType.APPLICATION_OCTET_STREAM_VALUE;
//
//        boolean shouldDisplayInline = FileDownloadUtils.shouldDisplayInline(contentType);
//
//        String contentDisposition = FileDownloadUtils.createContentDisposition(
//                document.getOriginalFileName(),
//                shouldDisplayInline
//        );
//
//        return ResponseEntity.ok()
//                .contentType(MediaType.parseMediaType(contentType))
//                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
//                .contentLength(fileData.length)
//                .body(resource);
//    }

    @GetMapping("/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long deliveryId,
            @PathVariable Long documentId) {

        DeliveryDocument document = deliveryDocumentService.getDocument(documentId);

        InputStream inputStream = deliveryDocumentService.getDocumentFileStream(documentId);
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

