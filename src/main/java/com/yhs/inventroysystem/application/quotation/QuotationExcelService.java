package com.yhs.inventroysystem.application.quotation;

import com.yhs.inventroysystem.domain.delivery.Delivery;
import com.yhs.inventroysystem.domain.delivery.DeliveryItem;
import com.yhs.inventroysystem.domain.quotation.Quotation;
import com.yhs.inventroysystem.domain.quotation.QuotationItem;
import com.yhs.inventroysystem.domain.quotation.QuotationRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuotationExcelService {

    private final QuotationRepository quotationRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public byte[] exportAllQuotationsToExcel(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            startDate = LocalDate.now().minusYears(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        List<Quotation> quotations = quotationRepository.findQuotationsByPeriod(startDate, endDate);


        return generateExcel(quotations);
    }

    /**
     * 견적서 데이터를 기반으로 실제 엑셀 파일 생성
     */
    private byte[] generateExcel(List<Quotation> quotations) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("견적서 목록");

            // 스타일 정의
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle mergedStyle = createMergedCellStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);

            // 헤더 생성
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "발행일", "견적서번호", "구분", "거래처명", "담당자", "통화",
                    "품목명", "수량", "단가", "품목금액",
                    "소계", "부가세액", "총액", "부가세 포함 여부", "비고"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 데이터 입력
            int rowNum = 1;
            for (Quotation quotation : quotations) {
                int itemCount = quotation.getItems().size();
                int startRow = rowNum;

                for (int i = 0; i < itemCount; i++) {
                    QuotationItem item = quotation.getItems().get(i);
                    Row row = sheet.createRow(rowNum++);
                    int colNum = 0;

                    // 첫 행만 공통 정보 입력 (발행일~통화)
                    if (i == 0) {
                        createCell(row, colNum++, formatDate(quotation.getOrderedAt()), mergedStyle);
                        createCell(row, colNum++, quotation.getQuotationNumber(), mergedStyle);
                        createCell(row, colNum++, getQuotationTypeText(quotation.getQuotationType().name()), mergedStyle);
                        createCell(row, colNum++, quotation.getCompanyName(), mergedStyle);
                        createCell(row, colNum++, quotation.getRepresentativeName() != null ? quotation.getRepresentativeName() : "-", mergedStyle);
                        createCell(row, colNum++, quotation.getCurrency().name() + "(" + quotation.getCurrency().getSymbol() + ")", mergedStyle);
                    } else {
                        colNum += 6;
                    }

                    // 품목별 데이터
                    createCell(row, colNum++, item.getProductName(), dataStyle);
                    createCell(row, colNum++, item.getQuantity(), numberStyle);
                    createCell(row, colNum++, item.getUnitPrice(), numberStyle);
                    createCell(row, colNum++, item.getTotalPrice(), numberStyle);

                    // 첫 행에만 견적서 합계 등 입력
                    if (i == 0) {
                        createCell(row, colNum++, quotation.getTotalAmount(), numberStyle);
                        createCell(row, colNum++, quotation.getTaxAmount(), numberStyle);
                        createCell(row, colNum++, quotation.getTotalAfterTaxAmount(), numberStyle);
                        createCell(row, colNum++, quotation.isTax() ? "포함" : "미포함", mergedStyle);
                        createCell(row, colNum++, quotation.getNote() != null ? quotation.getNote() : "-", mergedStyle);
                    }
                }

                // 품목 여러 개면 병합
                if (itemCount > 1) {
                    int endRow = rowNum - 1;
                    int[] mergeCols = {0, 1, 2, 3, 4, 5, 10, 11, 12, 13, 14};

                    for (int col : mergeCols) {
                        CellRangeAddress region = new CellRangeAddress(startRow, endRow, col, col);
                        sheet.addMergedRegion(region);

                        // 병합된 영역 전체에 테두리 적용
                        RegionUtil.setBorderTop(BorderStyle.THIN, region, sheet);
                        RegionUtil.setBorderBottom(BorderStyle.THIN, region, sheet);
                        RegionUtil.setBorderLeft(BorderStyle.THIN, region, sheet);
                        RegionUtil.setBorderRight(BorderStyle.THIN, region, sheet);
                    }
                }
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
     * 병합 셀용 스타일
     * - 일반 데이터 셀과 동일한 디자인 (왼쪽 정렬, 흰 배경)
     * - 병합된 셀이라도 통일감 유지
     */
    private CellStyle createMergedCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        style.setFillPattern(FillPatternType.NO_FILL); // 흰 배경

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

    private String formatDate(java.time.LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "-";
    }

    private String formatDateTime(java.time.LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : "-";
    }

    private String getQuotationTypeText(String type) {
        return switch (type) {
            case "RECEIPT" -> "수령";
            case "ISSUANCE" -> "발행";
            default -> type;
        };
    }

}