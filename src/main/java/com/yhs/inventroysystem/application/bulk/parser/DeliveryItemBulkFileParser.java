package com.yhs.inventroysystem.application.bulk.parser;

import com.yhs.inventroysystem.application.bulk.command.DeliveryItemBulkRegisterCommand;
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
import java.util.ArrayList;
import java.util.List;

import static com.yhs.inventroysystem.application.bulk.command.DeliveryItemBulkRegisterCommand.*;

@Service
@RequiredArgsConstructor
public class DeliveryItemBulkFileParser {

    public List<BulkDeliveryItemData> parseFile(MultipartFile file) throws IOException {
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

    private List<BulkDeliveryItemData> parseCsv(MultipartFile file) throws IOException {
        List<BulkDeliveryItemData> items = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {

            for (CSVRecord record : csvParser) {
                BulkDeliveryItemData itemData = BulkDeliveryItemData.builder()
                        .deliveryNumber(record.get("deliveryNumber"))
                        .productCode(record.get("productCode"))
                        .quantity(parseInteger(record.get("quantity")))
                        .actualUnitPrice(parseBigDecimalOptional(getOptionalValue(record, "actualUnitPrice")))
                        .priceNote(getOptionalValue(record, "priceNote"))
                        .isFreeItem(parseBooleanOptional(getOptionalValue(record, "isFreeItem")))
                        .build();

                items.add(itemData);
            }
        }

        return items;
    }

    private List<BulkDeliveryItemData> parseExcel(MultipartFile file) throws IOException {
        List<BulkDeliveryItemData> items = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalArgumentException("Excel 파일에 헤더가 없습니다");
            }

            int deliveryNumberIdx = findColumnIndex(headerRow, "deliveryNumber");
            int productCodeIdx = findColumnIndex(headerRow, "productCode");
            int quantityIdx = findColumnIndex(headerRow, "quantity");
            int actualUnitPriceIdx = findColumnIndexOptional(headerRow, "actualUnitPrice");
            int priceNoteIdx = findColumnIndexOptional(headerRow, "priceNote");
            int isFreeItemIdx = findColumnIndexOptional(headerRow, "isFreeItem");

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isEmptyRow(row)) {
                    continue;
                }

                BulkDeliveryItemData itemData = BulkDeliveryItemData.builder()
                        .deliveryNumber(getCellValueAsString(row.getCell(deliveryNumberIdx)))
                        .productCode(getCellValueAsString(row.getCell(productCodeIdx)))
                        .quantity(getCellValueAsInteger(row.getCell(quantityIdx)))
                        .actualUnitPrice(actualUnitPriceIdx >= 0 ?
                                getCellValueAsBigDecimal(row.getCell(actualUnitPriceIdx)) : null)
                        .priceNote(priceNoteIdx >= 0 ?
                                getCellValueAsString(row.getCell(priceNoteIdx)) : null)
                        .isFreeItem(isFreeItemIdx >= 0 ?
                                getCellValueAsBoolean(row.getCell(isFreeItemIdx)) : null)
                        .build();

                items.add(itemData);
            }
        }

        return items;
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
        return -1;
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
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    private Integer getCellValueAsInteger(Cell cell) {
        if (cell == null) {
            return 0;
        }

        return switch (cell.getCellType()) {
            case NUMERIC -> (int) cell.getNumericCellValue();
            case STRING -> parseInteger(cell.getStringCellValue().trim());
            default -> 0;
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

    private Boolean getCellValueAsBoolean(Cell cell) {
        if (cell == null) {
            return null;
        }

        return switch (cell.getCellType()) {
            case BOOLEAN -> cell.getBooleanCellValue();
            case STRING -> parseBooleanOptional(cell.getStringCellValue().trim());
            case NUMERIC -> cell.getNumericCellValue() != 0;
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

    private Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
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

    private Boolean parseBooleanOptional(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String lower = value.trim().toLowerCase();
        if (lower.equals("true") || lower.equals("1") || lower.equals("yes") || lower.equals("y")) {
            return true;
        } else if (lower.equals("false") || lower.equals("0") || lower.equals("no") || lower.equals("n")) {
            return false;
        }
        return null;
    }
}