package com.yhs.inventroysystem.application.bulk.parser;

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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.yhs.inventroysystem.application.bulk.command.PartBulkRegisterCommand.*;

@Service
@RequiredArgsConstructor
public class PartBulkFileParser {

    public List<BulkPartData> parseFile(MultipartFile file) throws IOException {
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

    private List<BulkPartData> parseCsv(MultipartFile file) throws IOException {
        List<BulkPartData> parts = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {

            for (CSVRecord record : csvParser) {
                BulkPartData partData = BulkPartData.builder()
                        .partCode(record.get("partCode"))
                        .name(record.get("name"))
                        .specification(record.get("specification"))
                        .initialStock(parseInteger(record.get("stockQuantity")))
                        .unit(record.get("unit"))
                        .build();

                parts.add(partData);
            }
        }

        return parts;
    }

    private List<BulkPartData> parseExcel(MultipartFile file) throws IOException {
        List<BulkPartData> parts = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // 첫 번째 행은 헤더로 가정
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalArgumentException("Excel 파일에 헤더가 없습니다");
            }

            int partCodeIdx = findColumnIndex(headerRow, "partCode");
            int nameIdx = findColumnIndex(headerRow, "name");
            int specificationIdx = findColumnIndex(headerRow, "specification");
            int stockQuantityIdx = findColumnIndex(headerRow, "stockQuantity");
            int unitIdx = findColumnIndex(headerRow, "unit");

            // 데이터 행 읽기 (헤더 다음부터)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isEmptyRow(row)) {
                    continue;
                }

                BulkPartData partData = BulkPartData.builder()
                        .partCode(getCellValueAsString(row.getCell(partCodeIdx)))
                        .name(getCellValueAsString(row.getCell(nameIdx)))
                        .specification(getCellValueAsString(row.getCell(specificationIdx)))
                        .initialStock(getCellValueAsInteger(row.getCell(stockQuantityIdx)))
                        .unit(getCellValueAsString(row.getCell(unitIdx)))
                        .build();

                parts.add(partData);
            }
        }

        return parts;
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
}