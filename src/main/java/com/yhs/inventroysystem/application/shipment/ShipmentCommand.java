package com.yhs.inventroysystem.application.shipment;

import com.yhs.inventroysystem.domain.shipment.entity.ShipmentType;
import com.yhs.inventroysystem.domain.shipment.entity.TradeTerms;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ShipmentCommand {

    public record ShipmentCreateCommand(
            // ========== Invoice 기본 정보 ==========
            LocalDate invoiceDate,
            LocalDate freightDate,

            // ========== Shipper / Exporter 정보 ==========
            String shipperCompanyName,
            String shipperAddress,
            String shipperContactPerson,
            String shipperPhone,

            // ========== Sold To 정보 ==========
            Long clientId,
            String soldToCompanyName,
            String soldToAddress,
            String soldToContactPerson,
            String soldToPhone,

            // ========== Ship To 정보 ==========
            String shipToCompanyName,
            String shipToAddress,
            String shipToContactPerson,
            String shipToPhone,

            // ========== 운송 정보 ==========
            String portOfLoading,
            String finalDestination,
            Long carrierId,
            String carrierName,
            String trackingNumber,
            String exportLicenseNumber,

            // ========== 신용장 정보 ==========
            String lcNo,
            LocalDate lcDate,
            String lcIssuingBank,

            // ========== Remark 정보 ==========
            ShipmentType shipmentType,
            TradeTerms tradeTerms,
            String originDescription,
            String additionalRemarks,

            // ========== 통화 ==========
            String currency,

            // ========== 박스 및 제품 정보 ==========
            List<ShipmentBoxItemCommand> boxItems,
            List<ShipmentItemCommand> items
    ) {
    }

    public record ShipmentBoxItemCommand(
            Integer sequence,
            Long boxTemplateId,  // null이면 직접 입력
            String title,
            BigDecimal width,
            BigDecimal length,
            BigDecimal height,
            Integer quantity
    ) {
        public boolean isFromTemplate() {
            return boxTemplateId != null;
        }
    }

    public record ShipmentItemCommand(
            Integer sequence,
            Long productId,
            String productCode,
            String productDescription,
            String hsCode,
            String unit,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal netWeight,      // 순중량 (kg) - 개별 제품
            BigDecimal grossWeight,    // 총중량 (kg) - 개별 제품
            BigDecimal cbm             // CBM (Cubic Meter) - 개별 제품
    ) {
    }

    public record ShipmentUpdateCommand(
            // ========== Invoice 기본 정보 ==========
            LocalDate invoiceDate,
            LocalDate freightDate,

            // ========== Shipper / Exporter 정보 ==========
            String shipperCompanyName,
            String shipperAddress,
            String shipperContactPerson,
            String shipperPhone,

            // ========== Sold To 정보 ==========
            Long clientId,
            String soldToCompanyName,
            String soldToAddress,
            String soldToContactPerson,
            String soldToPhone,

            // ========== Ship To 정보 ==========
            String shipToCompanyName,
            String shipToAddress,
            String shipToContactPerson,
            String shipToPhone,

            // ========== 운송 정보 ==========
            String portOfLoading,
            String finalDestination,
            Long carrierId,
            String carrierName,
            String trackingNumber,
            String exportLicenseNumber,

            // ========== 신용장 정보 ==========
            String lcNo,
            LocalDate lcDate,
            String lcIssuingBank,

            // ========== Remark 정보 ==========
            ShipmentType shipmentType,
            TradeTerms tradeTerms,
            String originDescription,
            String additionalRemarks,

            // ========== 통화 ==========
            String currency,

            // ========== 박스 및 제품 정보 ==========
            List<ShipmentBoxItemCommand> boxItems,
            List<ShipmentItemCommand> items
    ) {
    }
}
