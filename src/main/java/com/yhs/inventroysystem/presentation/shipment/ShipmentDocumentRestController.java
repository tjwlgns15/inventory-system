package com.yhs.inventroysystem.presentation.shipment;

import com.yhs.inventroysystem.application.shipment.ShipmentDocumentService;
import com.yhs.inventroysystem.domain.shipment.entity.ShipmentDocument;
import com.yhs.inventroysystem.infrastructure.file.FileDownloadUtils;
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

import java.util.List;

import static com.yhs.inventroysystem.application.shipment.ShipmentDocumentCommands.*;
import static com.yhs.inventroysystem.presentation.shipment.ShipmentDocumentDtos.*;

@RestController
@RequestMapping("/api/shipments/{shipmentId}/documents")
@RequiredArgsConstructor
public class ShipmentDocumentRestController {
    private final ShipmentDocumentService shipmentDocumentService;


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ShipmentDocumentResponse> uploadDocument(
            @PathVariable Long shipmentId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description) {

        ShipmentDocumentUploadCommand command = new ShipmentDocumentUploadCommand(
                shipmentId,
                file,
                description
        );

        ShipmentDocument document = shipmentDocumentService.uploadDocument(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ShipmentDocumentResponse.from(document));
    }

    @GetMapping
    public ResponseEntity<ShipmentDocumentListResponse> getAllDocuments(@PathVariable Long shipmentId) {
        List<ShipmentDocument> documents = shipmentDocumentService.getDocumentsByShipmentId(shipmentId);
        return ResponseEntity.ok(ShipmentDocumentListResponse.from(documents));
    }

    @PatchMapping("/{documentId}/description")
    public ResponseEntity<ShipmentDocumentResponse> updateDocumentDescription(
            @PathVariable Long shipmentId,
            @PathVariable Long documentId,
            @Valid @RequestBody ShipmentDocumentDescriptionUpdateRequest request) {

        ShipmentDocumentUpdateCommand command = new ShipmentDocumentUpdateCommand(
                documentId,
                request.description()
        );

        ShipmentDocument document = shipmentDocumentService.updateDocumentDescription(command);
        return ResponseEntity.ok(ShipmentDocumentResponse.from(document));
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long shipmentId,
            @PathVariable Long documentId) {

        ShipmentDocumentDeleteCommand command = new ShipmentDocumentDeleteCommand(
                shipmentId,
                documentId
        );
        shipmentDocumentService.deleteDocument(command);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long shipmentId,
            @PathVariable Long documentId) {

        ShipmentDocument document = shipmentDocumentService.getDocument(documentId);
        byte[] fileData = shipmentDocumentService.getDocumentFile(documentId);

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
