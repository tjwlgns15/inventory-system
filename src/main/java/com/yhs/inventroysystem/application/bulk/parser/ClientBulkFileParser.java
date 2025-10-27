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

import static com.yhs.inventroysystem.application.bulk.command.ClientBulkRegisterCommand.*;

@Service
@RequiredArgsConstructor
public class ClientBulkFileParser {

    public List<BulkClientData> parseFile(MultipartFile file) throws IOException {
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

    private List<BulkClientData> parseCsv(MultipartFile file) throws IOException {
        List<BulkClientData> clients = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {

            for (CSVRecord record : csvParser) {
                BulkClientData clientData = BulkClientData.builder()
                        .clientCode(record.get("clientCode"))
                        .countryCode(record.get("countryCode"))
                        .name(record.get("name"))
                        .address(getOptionalField(record, "address"))
                        .contactNumber(getOptionalField(record, "contactNumber"))
                        .email(getOptionalField(record, "email"))
                        .currency(record.get("currency"))
                        .parentClientCode(getOptionalField(record, "parentClientCode"))
                        .build();

                clients.add(clientData);
            }
        }

        return clients;
    }

    private List<BulkClientData> parseExcel(MultipartFile file) throws IOException {
        List<BulkClientData> clients = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // 첫 번째 행은 헤더로 가정
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalArgumentException("Excel 파일에 헤더가 없습니다");
            }

            int clientCodeIdx = findColumnIndex(headerRow, "clientCode");
            int countryCodeIdx = findColumnIndex(headerRow, "countryCode");
            int nameIdx = findColumnIndex(headerRow, "name");
            int addressIdx = findColumnIndexOptional(headerRow, "address");
            int contactNumberIdx = findColumnIndexOptional(headerRow, "contactNumber");
            int emailIdx = findColumnIndexOptional(headerRow, "email");
            int currencyIdx = findColumnIndex(headerRow, "currency");
            int parentClientCodeIdx = findColumnIndexOptional(headerRow, "parentClientCode");

            // 데이터 행 읽기 (헤더 다음부터)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isEmptyRow(row)) {
                    continue;
                }

                BulkClientData clientData = BulkClientData.builder()
                        .clientCode(getCellValueAsString(row.getCell(clientCodeIdx)))
                        .countryCode(getCellValueAsString(row.getCell(countryCodeIdx)))
                        .name(getCellValueAsString(row.getCell(nameIdx)))
                        .address(addressIdx >= 0 ? getCellValueAsString(row.getCell(addressIdx)) : "")
                        .contactNumber(contactNumberIdx >= 0 ? getCellValueAsString(row.getCell(contactNumberIdx)) : "")
                        .email(emailIdx >= 0 ? getCellValueAsString(row.getCell(emailIdx)) : "")
                        .currency(getCellValueAsString(row.getCell(currencyIdx)))
                        .parentClientCode(parentClientCodeIdx >= 0 ? getCellValueAsString(row.getCell(parentClientCodeIdx)) : "")
                        .build();

                clients.add(clientData);
            }
        }

        return clients;
    }

    private String getOptionalField(CSVRecord record, String fieldName) {
        try {
            return record.get(fieldName);
        } catch (IllegalArgumentException e) {
            return "";
        }
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
        return -1; // 선택 컬럼은 없으면 -1 반환
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
}