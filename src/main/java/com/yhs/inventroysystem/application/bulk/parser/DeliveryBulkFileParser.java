package com.yhs.inventroysystem.application.bulk.parser;

import com.yhs.inventroysystem.domain.delivery.entity.DeliveryStatus;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.yhs.inventroysystem.application.bulk.command.DeliveryBulkRegisterCommand.*;

@Service
@RequiredArgsConstructor
public class DeliveryBulkFileParser {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public List<BulkDeliveryData> parseFile(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();

        if (filename == null) {
            throw new IllegalArgumentException("파일명을 확인할 수 없습니다");
        }

        if (filename.endsWith(".csv")) {
            return parseCsv(file);
        } else if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
            return parseExcel(file);
        } else {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. CSV 또는 Excel 파일을 업로드해주세요.");
        }
    }

    private List<BulkDeliveryData> parseCsv(MultipartFile file) throws IOException {
        List<BulkDeliveryData> deliveries = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {

            for (CSVRecord record : csvParser) {
                BulkDeliveryData deliveryData = BulkDeliveryData.builder()
                        .deliveryNumber(record.get("deliveryNumber"))
                        .clientCode(record.get("clientCode"))
                        .orderedAt(parseDate(record.get("orderedAt")))
                        .requestedAt(parseDate(record.get("requestedAt")))
                        .status(parseStatus(getOptionalValue(record, "status")))
                        .deliveredAt(parseDateTime(getOptionalValue(record, "deliveredAt")))
                        .totalDiscountAmount(parseBigDecimalOptional(record.get("totalDiscountAmount")))
                        .discountNote(getOptionalValue(record, "discountNote"))
                        .memo(getOptionalValue(record, "memo"))
                        .build();

                deliveries.add(deliveryData);
            }
        }

        return deliveries;
    }

    private List<BulkDeliveryData> parseExcel(MultipartFile file) throws IOException {
        List<BulkDeliveryData> deliveries = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalArgumentException("Excel 파일에 헤더가 없습니다");
            }

            int deliveryNumberIdx = findColumnIndex(headerRow, "deliveryNumber");
            int clientCodeIdx = findColumnIndex(headerRow, "clientCode");
            int orderedAtIdx = findColumnIndex(headerRow, "orderedAt");
            int requestedAtIdx = findColumnIndex(headerRow, "requestedAt");
            int statusIdx = findColumnIndexOptional(headerRow, "status");
            int deliveredAtIdx = findColumnIndexOptional(headerRow, "deliveredAt");
            int totalDiscountAmountIdx = findColumnIndexOptional(headerRow, "totalDiscountAmount");
            int discountNoteIdx = findColumnIndexOptional(headerRow, "discountNote");
            int memoIdx = findColumnIndexOptional(headerRow, "memo");

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isEmptyRow(row)) {
                    continue;
                }

                BulkDeliveryData deliveryData = BulkDeliveryData.builder()
                        .deliveryNumber(getCellValueAsString(row.getCell(deliveryNumberIdx)))
                        .clientCode(getCellValueAsString(row.getCell(clientCodeIdx)))
                        .orderedAt(getCellValueAsDate(row.getCell(orderedAtIdx)))
                        .requestedAt(getCellValueAsDate(row.getCell(requestedAtIdx)))
                        .status(statusIdx >= 0 ?
                                parseStatus(getCellValueAsString(row.getCell(statusIdx))) : null)
                        .deliveredAt(deliveredAtIdx >= 0 ?
                                getCellValueAsDateTime(row.getCell(deliveredAtIdx)) : null)
                        .totalDiscountAmount(totalDiscountAmountIdx >= 0 ?
                                getCellValueAsBigDecimal(row.getCell(totalDiscountAmountIdx)) : null)
                        .discountNote(discountNoteIdx >= 0 ?
                                getCellValueAsString(row.getCell(discountNoteIdx)) : null)
                        .memo(memoIdx >= 0 ?
                                getCellValueAsString(row.getCell(memoIdx)) : null)
                        .build();

                deliveries.add(deliveryData);
            }
        }

        return deliveries;
    }

    private int findColumnIndex(Row headerRow, String columnName) {
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null && columnName.equalsIgnoreCase(cell.getStringCellValue().trim())) {
                return i;
            }
        }
        throw new IllegalArgumentException("필수 컬럼을 찾을 수 없습니다: " + columnName);
    }

    private int findColumnIndexOptional(Row headerRow, String columnName) {
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null && columnName.equalsIgnoreCase(cell.getStringCellValue().trim())) {
                return i;
            }
        }
        return -1; // 선택적 컬럼이므로 -1 반환
    }

    private boolean isEmptyRow(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalDate().format(DATE_FORMATTER);
                } else {
                    yield String.valueOf((long) cell.getNumericCellValue());
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    private LocalDate getCellValueAsDate(Cell cell) {
        if (cell == null) {
            return null;
        }

        return switch (cell.getCellType()) {
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalDate();
                } else {
                    yield null;
                }
            }
            case STRING -> parseDate(cell.getStringCellValue().trim());
            default -> null;
        };
    }

    private LocalDateTime getCellValueAsDateTime(Cell cell) {
        if (cell == null) {
            return null;
        }

        return switch (cell.getCellType()) {
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue();
                } else {
                    yield null;
                }
            }
            case STRING -> parseDateTime(cell.getStringCellValue().trim());
            default -> null;
        };
    }

    private BigDecimal getCellValueAsBigDecimal(Cell cell) {
        if (cell == null) {
            return null;
        }

        return switch (cell.getCellType()) {
            case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue());
            case STRING -> parseBigDecimalOptional(cell.getStringCellValue().trim());
            default -> null;
        };
    }

    private String getOptionalValue(CSVRecord record, String columnName) {
        try {
            String value = record.get(columnName);
            return (value == null || value.trim().isEmpty()) ? null : value.trim();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim(), DATE_FORMATTER);
        } catch (Exception e) {
            try {
                // yyyy/MM/dd 형식도 시도
                return LocalDate.parse(value.trim(), DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            } catch (Exception ex) {
                return null;
            }
        }
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            // yyyy-MM-dd HH:mm:ss 형식
            return LocalDateTime.parse(value.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            try {
                // yyyy-MM-dd'T'HH:mm:ss 형식 (ISO)
                return LocalDateTime.parse(value.trim());
            } catch (Exception ex) {
                try {
                    // 날짜만 있는 경우 00:00:00으로 설정
                    LocalDate date = parseDate(value);
                    return date != null ? date.atStartOfDay() : null;
                } catch (Exception ex2) {
                    return null;
                }
            }
        }
    }

    private DeliveryStatus parseStatus(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return DeliveryStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            // PENDING, COMPLETED, CANCELLED 중 하나가 아니면 null
            return null;
        }
    }

    private BigDecimal parseBigDecimalOptional(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}