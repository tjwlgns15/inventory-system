package com.yhs.inventroysystem.presentation.quotation;

import com.yhs.inventroysystem.application.quotation.QuotationCommands;
import com.yhs.inventroysystem.application.quotation.QuotationExcelService;
import com.yhs.inventroysystem.application.quotation.QuotationService;
import com.yhs.inventroysystem.domain.quotation.entity.Quotation;
import com.yhs.inventroysystem.domain.quotation.entity.QuotationType;
import com.yhs.inventroysystem.presentation.quotation.QuotationDtos.QuotationCreate;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static com.yhs.inventroysystem.presentation.quotation.QuotationDtos.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quotations")
public class QuotationRestController {

    private final QuotationService quotationService;
    private final QuotationExcelService quotationExcelService;


    @PostMapping
    public ResponseEntity<Void> createQuotation(@Valid @RequestBody QuotationCreate request) {

        QuotationCommands.CreateCommand command = request.toCommand();
        quotationService.createQuotation(command);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<PageQuotationResponse> getQuotationsPaged(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "orderedAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String direction,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) QuotationType quotationType) {

        Page<Quotation> quotationPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            quotationPage = quotationService.searchQuotations(keyword, quotationType, page, size, sortBy, direction);
        } else {
            quotationPage = quotationService.findAllQuotationsPaged(quotationType, page, size, sortBy, direction);
        }

        return ResponseEntity.ok(PageQuotationResponse.from(quotationPage));
    }

    @GetMapping("/{quotationId}")
    public ResponseEntity<QuotationResponse> getQuotation(@PathVariable Long quotationId) {
        Quotation quotation = quotationService.findQuotationById(quotationId);

        return ResponseEntity.ok(QuotationResponse.from(quotation));
    }

    @PatchMapping("/{quotationId}")
    public ResponseEntity<QuotationResponse> updateQuotation(
            @PathVariable Long quotationId,
            @RequestBody QuotationUpdate request) {

        QuotationCommands.UpdateCommand command = request.toCommand();
        Quotation quotation = quotationService.updateQuotation(quotationId, command);

        return ResponseEntity.ok(QuotationResponse.from(quotation));
    }

    @DeleteMapping("/{quotationId}")
    public ResponseEntity<Void> deleteQuotation(@PathVariable Long quotationId) {
        quotationService.deleteQuotation(quotationId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportAllQuotationToExcel(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        byte[] excelData = quotationExcelService.exportAllQuotationsToExcel(startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("quotations_" + LocalDate.now() + ".xlsx", StandardCharsets.UTF_8)
                        .build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }
}
