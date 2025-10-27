package com.yhs.inventroysystem.application.bulk.parser;

import com.yhs.inventroysystem.application.bulk.command.ProductPartMappingBulkCommand;
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

import static com.yhs.inventroysystem.application.bulk.command.ProductPartMappingBulkCommand.*;

@Service
@RequiredArgsConstructor
public class ProductPartMappingFileParser {

    public List<BulkMappingData> parseFile(MultipartFile file) throws IOException {
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

    private List<BulkMappingData> parseCsv(MultipartFile file) throws IOException {
        List<BulkMappingData> mappings = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {

            for (CSVRecord record : csvParser) {
                BulkMappingData mappingData = BulkMappingData.builder()
                        .productCode(record.get("productCode"))
                        .partCode(record.get("partCode"))
                        .requiredQuantity(parseInteger(record.get("requiredQuantity")))
                        .build();

                mappings.add(mappingData);
            }
        }

        return mappings;
    }

    private List<BulkMappingData> parseExcel(MultipartFile file) throws IOException {
        List<BulkMappingData> mappings = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // 첫 번째 행은 헤더로 가정
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalArgumentException("Excel 파일에 헤더가 없습니다");
            }

            int productCodeIdx = findColumnIndex(headerRow, "productCode");
            int partCodeIdx = findColumnIndex(headerRow, "partCode");
            int requiredQuantityIdx = findColumnIndex(headerRow, "requiredQuantity");

            // 데이터 행 읽기 (헤더 다음부터)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isEmptyRow(row)) {
                    continue;
                }

                BulkMappingData mappingData = BulkMappingData.builder()
                        .productCode(getCellValueAsString(row.getCell(productCodeIdx)))
                        .partCode(getCellValueAsString(row.getCell(partCodeIdx)))
                        .requiredQuantity(getCellValueAsInteger(row.getCell(requiredQuantityIdx)))
                        .build();

                mappings.add(mappingData);
            }
        }

        return mappings;
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