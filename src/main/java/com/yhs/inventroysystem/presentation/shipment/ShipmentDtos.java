package com.yhs.inventroysystem.presentation.shipment;

import com.yhs.inventroysystem.application.shipment.ShipmentCommand.ShipmentBoxItemCommand;
import com.yhs.inventroysystem.application.shipment.ShipmentCommand.ShipmentCreateCommand;
import com.yhs.inventroysystem.application.shipment.ShipmentCommand.ShipmentItemCommand;
import com.yhs.inventroysystem.application.shipment.ShipmentCommand.ShipmentUpdateCommand;
import com.yhs.inventroysystem.domain.shipment.entity.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ShipmentDtos {

    public record ShipmentResponse(
            Long id,
            String invoiceNumber,
            LocalDate invoiceDate,
            Integer year,
            Integer sequence,

            String shipperCompanyName,
            String shipperAddress,
            String shipperContactPerson,
            String shipperPhone,

            Long clientId,
            String soldToCompanyName,
            String soldToAddress,
            String soldToContactPerson,
            String soldToPhone,

            String shipToCompanyName,
            String shipToAddress,
            String shipToContactPerson,
            String shipToPhone,


            String portOfLoading,
            String finalDestination,
            Long carrierId,
            String carrierName,
            LocalDate freightDate,
            String trackingNumber,
            String exportLicenseNumber,

            String lcNo,
            LocalDate lcDate,
            String lcIssuingBank,

            ShipmentType shipmentType,
            String shipmentTypeDisplay,
            String shipmentTypeDisplayEn,
            TradeTerms tradeTerms,
            String tradeTermsDisplay,
            String tradeTermsDisplayEn,
            String originDescription,
            String additionalRemarks,

            List<ShipmentBoxResponse> boxes,
            Integer totalBoxCount,
            List<ShipmentItemResponse> items,
            Integer totalQuantity,
            BigDecimal totalAmount,
            String currency,


            BigDecimal totalNetWeight,
            BigDecimal totalGrossWeight,
            BigDecimal totalCbm,

            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public static ShipmentResponse from(Shipment shipment) {
            return new ShipmentResponse(
                    shipment.getId(),
                    shipment.getInvoiceNumber(),
                    shipment.getInvoiceDate(),
                    shipment.getYear(),
                    shipment.getSequence(),
                    shipment.getShipperCompanyName(),
                    shipment.getShipperAddress(),
                    shipment.getShipperContactPerson(),
                    shipment.getShipperPhone(),
                    shipment.getClientId(),
                    shipment.getSoldToCompanyName(),
                    shipment.getSoldToAddress(),
                    shipment.getSoldToContactPerson(),
                    shipment.getSoldToPhone(),
                    shipment.getShipToCompanyName(),
                    shipment.getShipToAddress(),
                    shipment.getShipToContactPerson(),
                    shipment.getShipToPhone(),
                    shipment.getPortOfLoading(),
                    shipment.getFinalDestination(),
                    shipment.getCarrier() != null ? shipment.getCarrier().getId() : null,
                    shipment.getCarrierName(),
                    shipment.getFreightDate(),
                    shipment.getTrackingNumber(),
                    shipment.getExportLicenseNumber(),
                    shipment.getLcNo(),
                    shipment.getLcDate(),
                    shipment.getLcIssuingBank(),
                    shipment.getShipmentType(),
                    shipment.getShipmentType().getKorean(),
                    shipment.getShipmentType().getEnglish(),
                    shipment.getTradeTerms(),
                    shipment.getTradeTerms().getKorean(),
                    shipment.getTradeTerms().getEnglish(),
                    shipment.getOriginDescription(),
                    shipment.getAdditionalRemarks(),
                    shipment.getBoxItems().stream()
                            .map(ShipmentBoxResponse::from)
                            .toList(),
                    shipment.getTotalBoxCount(),
                    shipment.getItems().stream()
                            .map(ShipmentItemResponse::from)
                            .toList(),
                    shipment.getTotalQuantity(),
                    shipment.getTotalAmount(),
                    shipment.getCurrency(),
                    shipment.getTotalNetWeight(),
                    shipment.getTotalGrossWeight(),
                    shipment.getTotalCbm(),
                    shipment.getCreatedAt(),
                    shipment.getLastModifiedAt()
            );
        }
    }

    /**
     * 목록 조회용 요약 정보 DTO
     */
    public record ShipmentSummary(
            Long id,
            String invoiceNumber,
            LocalDate invoiceDate,
            Integer year,
            Integer sequence,

            // 고객사 정보 (핵심)
            Long clientId,
            String soldToCompanyName,

            // 발송지 정보
            String finalDestination,
            String carrierName,
            LocalDate freightDate,

            // 거래 정보
            ShipmentType shipmentType,
            String shipmentTypeDisplay,

            String trackingNumber,
            String exportLicenseNumber,

            // 금액 정보
            Integer totalQuantity,
            BigDecimal totalAmount,
            String currency,

            // 제품 정보
            List<ShipmentItemResponse> items,
            // 생성/수정 시간
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public static ShipmentSummary from(Shipment shipment) {
            return new ShipmentSummary(
                    shipment.getId(),
                    shipment.getInvoiceNumber(),
                    shipment.getInvoiceDate(),
                    shipment.getYear(),
                    shipment.getSequence(),
                    shipment.getClientId(),
                    shipment.getSoldToCompanyName(),
                    shipment.getFinalDestination(),
                    shipment.getCarrierName(),
                    shipment.getFreightDate(),
                    shipment.getShipmentType(),
                    shipment.getShipmentType().getKorean(),
                    shipment.getTrackingNumber(),
                    shipment.getExportLicenseNumber(),
                    shipment.getTotalQuantity(),
                    shipment.getTotalAmount(),
                    shipment.getCurrency(),
                    shipment.getItems().stream()
                            .map(ShipmentItemResponse::from)
                            .toList(),
                    shipment.getCreatedAt(),
                    shipment.getLastModifiedAt()
            );
        }
    }

    public record PageShipmentResponse(
            List<ShipmentSummary> content,
            int pageNumber,
            int pageSize,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last,
            boolean empty
    ) {
        public static PageShipmentResponse from(Page<Shipment> page) {
            List<ShipmentSummary> content = page.getContent().stream()
                    .map(ShipmentSummary::from)
                    .toList();

            return new PageShipmentResponse(
                    content,
                    page.getNumber(),
                    page.getSize(),
                    page.getTotalElements(),
                    page.getTotalPages(),
                    page.isFirst(),
                    page.isLast(),
                    page.isEmpty()
            );
        }
    }
    /**
     * 박스 정보 응답 DTO
     */
    public record ShipmentBoxResponse(
            Long id,
            Integer sequence,
            Long boxTemplateId,
            String title,
            BigDecimal width,
            BigDecimal length,
            BigDecimal height,
            Integer quantity,
            String dimensionString
    ) {
        public static ShipmentBoxResponse from(ShipmentBoxItem box) {
            return new ShipmentBoxResponse(
                    box.getId(),
                    box.getSequence(),
                    box.getBoxTemplateId(),
                    box.getTitle(),
                    box.getWidth(),
                    box.getLength(),
                    box.getHeight(),
                    box.getQuantity(),
                    box.getDimensionString()
            );
        }
    }

    /**
     * 제품 정보 응답 DTO
     */
    public record ShipmentItemResponse(
            Long id,
            Integer sequence,
            Long productId,
            String productCode,
            String productDescription,
            String hsCode,
            String unit,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal amount,
            BigDecimal netWeight,
            BigDecimal grossWeight,
            BigDecimal cbm
    ) {
        public static ShipmentItemResponse from(ShipmentItem item) {
            return new ShipmentItemResponse(
                    item.getId(),
                    item.getSequence(),
                    item.getProductId(),
                    item.getProductCode(),
                    item.getProductDescription(),
                    item.getHsCode(),
                    item.getUnit(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getAmount(),
                    item.getNetWeight(),
                    item.getGrossWeight(),
                    item.getCbm()
            );
        }
    }

    /**
     * Shipment 생성 요청 DTO
     */
    public record ShipmentCreate(
            // ========== Invoice 기본 정보 ==========

            @NotNull(message = "Invoice 작성일은 필수입니다")
            LocalDate invoiceDate,

            @NotNull(message = "제품 발송일은 필수입니다")
            LocalDate freightDate,

            // ========== Shipper / Exporter 정보 ==========

            @NotBlank(message = "판매자 회사명은 필수입니다")
            @Size(max = 200)
            String shipperCompanyName,

            @NotBlank(message = "판매자 주소는 필수입니다")
            @Size(max = 500)
            String shipperAddress,

            @Size(max = 100)
            String shipperContactPerson,

            @Size(max = 50)
            String shipperPhone,

            // ========== Sold To 정보 ==========

            Long clientId,

            @NotBlank(message = "고객사 회사명은 필수입니다")
            @Size(max = 200)
            String soldToCompanyName,

            @NotBlank(message = "고객사 주소는 필수입니다")
            @Size(max = 500)
            String soldToAddress,

            @Size(max = 100)
            String soldToContactPerson,

            @Size(max = 50)
            String soldToPhone,

            // ========== Ship To 정보 ==========

            @NotBlank(message = "발송처 회사명은 필수입니다")
            @Size(max = 200)
            String shipToCompanyName,

            @NotBlank(message = "발송처 주소는 필수입니다")
            @Size(max = 500)
            String shipToAddress,

            @Size(max = 100)
            String shipToContactPerson,

            @Size(max = 50)
            String shipToPhone,

            // ========== 운송 정보 ==========

            @NotBlank(message = "출발지는 필수입니다")
            @Size(max = 200)
            String portOfLoading,

            @NotBlank(message = "최종 목적지는 필수입니다")
            @Size(max = 200)
            String finalDestination,

            Long carrierId,

            @Size(max = 200)
            String carrierName,

            @Size(max = 200)
            String trackingNumber,

            @Size(max = 200)
            String exportLicenseNumber,

            // ========== 신용장 정보 ==========

            @Size(max = 100)
            String lcNo,

            LocalDate lcDate,

            @Size(max = 200)
            String lcIssuingBank,

            // ========== Remark 정보 ==========

            @NotNull(message = "거래 유형은 필수입니다")
            ShipmentType shipmentType,

            @NotNull(message = "거래 조건은 필수입니다")
            TradeTerms tradeTerms,

            @Size(max = 500)
            String originDescription,

            @Size(max = 1000)
            String additionalRemarks,

            // ========== 통화 ==========

            @NotBlank(message = "통화는 필수입니다")
            @Size(max = 10)
            String currency,

            // ========== 박스 및 제품 정보 ==========

            @Valid
            List<ShipmentBoxRequest> boxes,

            @Valid
            @NotEmpty(message = "제품 정보는 최소 1개 이상 필요합니다")
            List<ShipmentItemRequest> items
    ) {
        public ShipmentCreateCommand toCommand() {
            return new ShipmentCreateCommand(
                    invoiceDate,
                    freightDate,
                    shipperCompanyName,
                    shipperAddress,
                    shipperContactPerson,
                    shipperPhone,
                    clientId,
                    soldToCompanyName,
                    soldToAddress,
                    soldToContactPerson,
                    soldToPhone,
                    shipToCompanyName,
                    shipToAddress,
                    shipToContactPerson,
                    shipToPhone,
                    portOfLoading,
                    finalDestination,
                    carrierId,
                    carrierName,
                    trackingNumber,
                    exportLicenseNumber,
                    lcNo,
                    lcDate,
                    lcIssuingBank,
                    shipmentType,
                    tradeTerms,
                    originDescription,
                    additionalRemarks,
                    currency,
                    boxes != null ? boxes.stream()
                            .map(ShipmentBoxRequest::toCommand)
                            .toList() : null,
                    items.stream()
                            .map(ShipmentItemRequest::toCommand)
                            .toList()
            );
        }
    }

    /**
     * 박스 정보 DTO
     */
    public record ShipmentBoxRequest(

            @NotNull(message = "박스 순서는 필수입니다")
            @Positive(message = "박스 순서는 양수여야 합니다")
            Integer sequence,

            Long boxTemplateId,

            @NotBlank(message = "이름은 필수입니다")
            @Size(max = 200)
            String title,

            @NotNull(message = "박스 가로는 필수입니다")
            @DecimalMin(value = "0.0", inclusive = false)
            BigDecimal width,

            @NotNull(message = "박스 세로는 필수입니다")
            @DecimalMin(value = "0.0", inclusive = false)
            BigDecimal length,

            @NotNull(message = "박스 높이는 필수입니다")
            @DecimalMin(value = "0.0", inclusive = false)
            BigDecimal height,

            @NotNull(message = "박스 수량은 필수입니다")
            @Positive(message = "박스 수량은 양수여야 합니다")
            Integer quantity,

            @Size(max = 200)
            String remarks
    ) {
        public ShipmentBoxItemCommand toCommand() {
            return new ShipmentBoxItemCommand(
                    sequence,
                    boxTemplateId,
                    title,
                    width,
                    length,
                    height,
                    quantity
            );
        }
    }

    /**
     * 제품 정보 DTO
     */
    public record ShipmentItemRequest(

            @NotNull(message = "제품 순서는 필수입니다")
            @Positive(message = "제품 순서는 양수여야 합니다")
            Integer sequence,

            Long productId,

            @NotBlank(message = "제품명은 필수입니다")
            @Size(max = 300)
            String productCode,

            @Size(max = 500)
            String productDescription,

            @Size(max = 50)
            String hsCode,

            @NotBlank(message = "단위는 필수입니다")
            @Size(max = 20)
            String unit,

            @NotNull(message = "수량은 필수입니다")
            @Positive(message = "수량은 양수여야 합니다")
            Integer quantity,

            @NotNull(message = "단가는 필수입니다")
            @DecimalMin(value = "0.0", inclusive = false, message = "단가는 0보다 커야 합니다")
            BigDecimal unitPrice,

            @DecimalMin(value = "0.0", inclusive = false, message = "순중량은 0보다 커야 합니다")
            BigDecimal netWeight,

            @DecimalMin(value = "0.0", inclusive = false, message = "총중량은 0보다 커야 합니다")
            BigDecimal grossWeight,

            @DecimalMin(value = "0.0", inclusive = false, message = "CBM은 0보다 커야 합니다")
            BigDecimal cbm
    ) {

        public ShipmentItemCommand toCommand() {
            return new ShipmentItemCommand(
                    sequence,
                    productId,
                    productCode,
                    productDescription,
                    hsCode,
                    unit,
                    quantity,
                    unitPrice,
                    netWeight,
                    grossWeight,
                    cbm
            );
        }
    }

    public record ShipmentUpdate(
            // ========== Invoice 기본 정보 ==========

            @NotNull(message = "Invoice 작성일은 필수입니다")
            LocalDate invoiceDate,

            @NotNull(message = "제품 발송일은 필수입니다")
            LocalDate freightDate,

            // ========== Shipper / Exporter 정보 ==========

            @NotBlank(message = "판매자 회사명은 필수입니다")
            @Size(max = 200)
            String shipperCompanyName,

            @NotBlank(message = "판매자 주소는 필수입니다")
            @Size(max = 500)
            String shipperAddress,

            @Size(max = 100)
            String shipperContactPerson,

            @Size(max = 50)
            String shipperPhone,

            // ========== Sold To 정보 ==========

            Long clientId,

            @NotBlank(message = "고객사 회사명은 필수입니다")
            @Size(max = 200)
            String soldToCompanyName,

            @NotBlank(message = "고객사 주소는 필수입니다")
            @Size(max = 500)
            String soldToAddress,

            @Size(max = 100)
            String soldToContactPerson,

            @Size(max = 50)
            String soldToPhone,

            // ========== Ship To 정보 ==========

            @NotBlank(message = "발송처 회사명은 필수입니다")
            @Size(max = 200)
            String shipToCompanyName,

            @NotBlank(message = "발송처 주소는 필수입니다")
            @Size(max = 500)
            String shipToAddress,

            @Size(max = 100)
            String shipToContactPerson,

            @Size(max = 50)
            String shipToPhone,

            // ========== 운송 정보 ==========

            @NotBlank(message = "출발지는 필수입니다")
            @Size(max = 200)
            String portOfLoading,

            @NotBlank(message = "최종 목적지는 필수입니다")
            @Size(max = 200)
            String finalDestination,

            Long carrierId,

            @Size(max = 200)
            String carrierName,

            @Size(max = 200)
            String trackingNumber,

            @Size(max = 200)
            String exportLicenseNumber,

            // ========== 신용장 정보 ==========

            @Size(max = 100)
            String lcNo,

            LocalDate lcDate,

            @Size(max = 200)
            String lcIssuingBank,

            // ========== Remark 정보 ==========

            @NotNull(message = "거래 유형은 필수입니다")
            ShipmentType shipmentType,

            @NotNull(message = "거래 조건은 필수입니다")
            TradeTerms tradeTerms,

            @Size(max = 500)
            String originDescription,

            @Size(max = 1000)
            String additionalRemarks,

            // ========== 통화 ==========

            @NotBlank(message = "통화는 필수입니다")
            @Size(max = 10)
            String currency,

            // ========== 박스 및 제품 정보 ==========

            @Valid
            List<ShipmentBoxRequest> boxes,

            @Valid
            @NotEmpty(message = "제품 정보는 최소 1개 이상 필요합니다")
            List<ShipmentItemRequest> items
    ) {
        public ShipmentUpdateCommand toCommand() {
            return new ShipmentUpdateCommand(
                    invoiceDate,
                    freightDate,
                    shipperCompanyName,
                    shipperAddress,
                    shipperContactPerson,
                    shipperPhone,
                    clientId,
                    soldToCompanyName,
                    soldToAddress,
                    soldToContactPerson,
                    soldToPhone,
                    shipToCompanyName,
                    shipToAddress,
                    shipToContactPerson,
                    shipToPhone,
                    portOfLoading,
                    finalDestination,
                    carrierId,
                    carrierName,
                    trackingNumber,
                    exportLicenseNumber,
                    lcNo,
                    lcDate,
                    lcIssuingBank,
                    shipmentType,
                    tradeTerms,
                    originDescription,
                    additionalRemarks,
                    currency,
                    boxes != null ? boxes.stream()
                            .map(ShipmentBoxRequest::toCommand)
                            .toList() : null,
                    items.stream()
                            .map(ShipmentItemRequest::toCommand)
                            .toList()
            );
        }
    }
}

