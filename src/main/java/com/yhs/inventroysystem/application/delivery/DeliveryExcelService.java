package com.yhs.inventroysystem.application.delivery;

import com.yhs.inventroysystem.domain.delivery.Delivery;
import com.yhs.inventroysystem.domain.delivery.DeliveryItem;
import com.yhs.inventroysystem.domain.delivery.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryExcelService {

    private final DeliveryRepository deliveryRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * 모든 납품 데이터를 엑셀로 내보내는 메서드
     */
    public byte[] exportAllDeliveriesToExcel() {
        List<Delivery> deliveries = deliveryRepository.findAllWithClientAndItem();
        return generateExcel(deliveries);
    }

    /**
     * 특정 납품 ID 데이터만 엑셀로 내보내는 메서드
     */
    public byte[] exportDeliveryByIdToExcel(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("납품을 찾을 수 없습니다: " + deliveryId));
        return generateExcel(List.of(delivery));
    }

    /**
     * 납품 데이터를 기반으로 실제 엑셀 파일 생성
     */
    private byte[] generateExcel(List<Delivery> deliveries) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("납품 목록");

            // 스타일 정의
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle mergedStyle = createMergedCellStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);

            // 헤더 생성
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "수주일", "영업접수번호", "거래처명", "통화",
                    "제품명", "수량", "기준단가", "실제단가", "할인액", "가격조정사유", "품목금액",
                    "소계", "환율", "원화환산금액", "출하요청일", "실출하일", "상태", "메모"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 데이터 입력
            int rowNum = 1;
            for (Delivery delivery : deliveries) {
                int itemCount = delivery.getItems().size();
                int startRow = rowNum;

                for (int i = 0; i < itemCount; i++) {
                    DeliveryItem item = delivery.getItems().get(i);
                    Row row = sheet.createRow(rowNum++);
                    int colNum = 0;

                    // 첫 행만 공통 정보 입력 (수주일~통화)
                    if (i == 0) {
                        createCell(row, colNum++, formatDate(delivery.getOrderedAt()), mergedStyle);
                        createCell(row, colNum++, delivery.getDeliveryNumber(), mergedStyle);
                        createCell(row, colNum++, delivery.getClient().getName(), mergedStyle);
                        createCell(row, colNum++, delivery.getClient().getCurrency().name() +
                                "(" + delivery.getClient().getCurrency().getSymbol() + ")", mergedStyle);
                    } else {
                        colNum += 4;
                    }

                    // 품목별 데이터
                    createCell(row, colNum++, item.getProduct().getName(), dataStyle);
                    createCell(row, colNum++, item.getQuantity(), numberStyle);
                    createCell(row, colNum++, item.getBaseUnitPrice(), numberStyle);
                    createCell(row, colNum++, item.getActualUnitPrice(), numberStyle);
                    createCell(row, colNum++, item.getDiscountAmount(), numberStyle);
                    createCell(row, colNum++, item.getPriceNote(), dataStyle);
                    createCell(row, colNum++, item.getTotalPrice(), numberStyle);

                    // 첫 행에만 납품 합계 등 입력
                    if (i == 0) {
                        createCell(row, colNum++, delivery.getSubtotalAmount() != null ? delivery.getSubtotalAmount() : delivery.getTotalAmount(), numberStyle);
                        createCell(row, colNum++, delivery.getExchangeRate(), numberStyle);
                        createCell(row, colNum++, delivery.getTotalAmountKRW(), numberStyle);
                        createCell(row, colNum++, formatDate(delivery.getRequestedAt()), mergedStyle);
                        createCell(row, colNum++, formatDateTime(delivery.getDeliveredAt()), mergedStyle);
                        createCell(row, colNum++, getStatusText(delivery.getStatus().name()), mergedStyle);
                        createCell(row, colNum++, delivery.getMemo(), mergedStyle);
                    }
                }

                // 품목 여러 개면 병합
                if (itemCount > 1) {
                    int endRow = rowNum - 1;
                    int[] mergeCols = {0, 1, 2, 3, 11, 12, 13, 14, 15, 16, 17};

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

    private String getStatusText(String status) {
        return switch (status) {
            case "PENDING" -> "대기";
            case "COMPLETED" -> "완료";
            case "CANCELLED" -> "취소";
            default -> status;
        };
    }
}
