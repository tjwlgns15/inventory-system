package com.yhs.inventroysystem.application.shipment;

import com.yhs.inventroysystem.domain.shipment.entity.Shipment;
import com.yhs.inventroysystem.domain.shipment.service.ShipmentDomainService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShipmentExcelService {

    private final ShipmentDomainService shipmentDomainService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public byte[] exportAllShipmentsToExcel(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            startDate = LocalDate.now().minusYears(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        List<Shipment> shipments = shipmentDomainService.findByDateRange(startDate, endDate);

        return generateExcel(shipments);
    }

    /**
     * 선적 데이터를 기반으로 실제 엑셀 파일 생성
     */
    private byte[] generateExcel(List<Shipment> shipments) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("선적 목록");

            // 스타일 정의
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            // 헤더 생성
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "작성일", "Invoice No.", "거래유형", "거래처", "제품명", "수량(총계)", "금액(화폐)", "목적지", "발송일", "운송장번호", "면장번호"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 데이터 입력
            int rowNum = 1;
            for (Shipment shipment : shipments) {
                Row row = sheet.createRow(rowNum++);
                BigDecimal totalAmount = shipment.getTotalAmount();
                String currency = shipment.getCurrency();

                // 작성일
                createCell(row, 0, formatDate(shipment.getInvoiceDate()), dateStyle);
                // Invoice No.
                createCell(row, 1, shipment.getInvoiceNumber(), dataStyle);
                // 거래유형
                createCell(row, 2, getShipmentTypeText(shipment.getShipmentType().name()), dataStyle);
                // 거래처 (Sold To Company Name)
                createCell(row, 3, shipment.getSoldToCompanyName(), dataStyle);
                // 제품명 (items의 productCode 리스트)
                createCell(row, 4, getProductNames(shipment), dataStyle);
                // 수량
                createCell(row, 5, shipment.getTotalQuantity(), numberStyle);
                // 금액 (totalAmount)
                createCell(row, 6, totalAmount + " (" + currency + ")", numberStyle);
                // 목적지 (Final Destination)
                createCell(row, 7, shipment.getFinalDestination(), dataStyle);
                // 발송일 (Freight Date)
                createCell(row, 8, formatDate(shipment.getFreightDate()), dateStyle);
                // 운송장 번호
                createCell(row, 9, shipment.getTrackingNumber(), dataStyle);
                // 면장 번호
                createCell(row, 10, shipment.getExportLicenseNumber(), dataStyle);

            }

            // 열 너비 자동 조정
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1024);
            }

            workbook.write(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Excel 생성 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 셀 생성 + 값 + 스타일 적용
     */
    private void createCell(Row row, int column, Object value, CellStyle style) {
        Cell cell = row.createCell(column);

        if (value == null) {
            cell.setCellValue("-");
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        }

        cell.setCellStyle(style);
    }

    /**
     * 헤더 셀 스타일 (회색 배경 + 굵은 글씨 + 가운데 정렬)
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.BLACK.getIndex());
        style.setFont(font);

        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    /**
     * 일반 데이터 셀 스타일 (왼쪽 정렬)
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    /**
     * 날짜 데이터용 스타일 (가운데 정렬)
     */
    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    /**
     * 숫자 데이터용 스타일 (오른쪽 정렬 + 천단위 콤마)
     */
    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0"));

        return style;
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "-";
    }

    private String getShipmentTypeText(String type) {
        return switch (type) {
            case "EXPORT" -> "정식수출";
            case "SAMPLE" -> "무상샘플";
            default -> type;
        };
    }

    /**
     * 제품명 리스트를 쉼표로 구분된 문자열로 반환
     */
    private String getProductNames(Shipment shipment) {
        if (shipment.getItems() == null || shipment.getItems().isEmpty()) {
            return "-";
        }

        return shipment.getItems().stream()
                .map(item -> item.getProductCode())
                .reduce((a, b) -> a + ", " + b)
                .orElse("-");
    }
}