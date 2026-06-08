package com.mall.common.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
public class ExcelExportUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public byte[] exportSalesReport(Map<String, Object> data) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("销售报表");

            Row headerRow = sheet.createRow(0);
            createHeaderCell(headerRow, 0, "指标");
            createHeaderCell(headerRow, 1, "数值");

            Map<String, Object> salesStats = (Map<String, Object>) data.get("salesStats");
            int rowNum = 1;
            
            addDataRow(sheet, rowNum++, "销售总额", salesStats.get("totalSales"));
            addDataRow(sheet, rowNum++, "订单总数", salesStats.get("orderCount"));
            addDataRow(sheet, rowNum++, "完成订单", salesStats.get("completedCount"));
            addDataRow(sheet, rowNum++, "客单价", salesStats.get("avgOrderAmount"));
            addDataRow(sheet, rowNum++, "佣金支出", salesStats.get("totalCommission"));
            addDataRow(sheet, rowNum++, "实际收入", salesStats.get("actualIncome"));

            autoSizeColumns(sheet);
            return writeWorkbookToBytes(workbook);
        }
    }

    public byte[] exportProductRanking(List<Map<String, Object>> products) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("商品排行");

            Row headerRow = sheet.createRow(0);
            createHeaderCell(headerRow, 0, "排名");
            createHeaderCell(headerRow, 1, "商品名称");
            createHeaderCell(headerRow, 2, "价格");
            createHeaderCell(headerRow, 3, "销量");
            createHeaderCell(headerRow, 4, "库存");

            for (int i = 0; i < products.size(); i++) {
                Map<String, Object> product = products.get(i);
                Row row = sheet.createRow(i + 1);
                
                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue((String) product.get("name"));
                row.createCell(2).setCellValue(formatMoney(product.get("price")));
                row.createCell(3).setCellValue(((Number) product.get("sales")).doubleValue());
                row.createCell(4).setCellValue(((Number) product.get("stock")).doubleValue());
            }

            autoSizeColumns(sheet);
            return writeWorkbookToBytes(workbook);
        }
    }

    public byte[] exportOrderReport(List<Map<String, Object>> orders) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("订单报表");

            Row headerRow = sheet.createRow(0);
            createHeaderCell(headerRow, 0, "订单编号");
            createHeaderCell(headerRow, 1, "下单时间");
            createHeaderCell(headerRow, 2, "收货人");
            createHeaderCell(headerRow, 3, "联系电话");
            createHeaderCell(headerRow, 4, "收货地址");
            createHeaderCell(headerRow, 5, "订单金额");
            createHeaderCell(headerRow, 6, "订单状态");

            for (int i = 0; i < orders.size(); i++) {
                Map<String, Object> order = orders.get(i);
                Row row = sheet.createRow(i + 1);
                
                row.createCell(0).setCellValue((String) order.get("orderNo"));
                row.createCell(1).setCellValue(formatDateTime(order.get("createTime")));
                row.createCell(2).setCellValue((String) order.get("receiverName"));
                row.createCell(3).setCellValue((String) order.get("receiverPhone"));
                row.createCell(4).setCellValue((String) order.get("receiverAddress"));
                row.createCell(5).setCellValue(formatMoney(order.get("totalAmount")));
                row.createCell(6).setCellValue(getOrderStatusText((Integer) order.get("status")));
            }

            autoSizeColumns(sheet);
            return writeWorkbookToBytes(workbook);
        }
    }

    private void createHeaderCell(Row row, int column, String value) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        
        CellStyle style = row.getSheet().getWorkbook().createCellStyle();
        Font font = row.getSheet().getWorkbook().createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        cell.setCellStyle(style);
    }

    private void addDataRow(Sheet sheet, int rowNum, String label, Object value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(formatValue(value));
    }

    private String formatValue(Object value) {
        if (value == null) return "0";
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).setScale(2, RoundingMode.HALF_UP).toString();
        }
        if (value instanceof Number) {
            return String.valueOf(value);
        }
        return String.valueOf(value);
    }

    private String formatMoney(Object value) {
        if (value == null) return "¥0.00";
        if (value instanceof BigDecimal) {
            return "¥" + ((BigDecimal) value).setScale(2, RoundingMode.HALF_UP).toString();
        }
        if (value instanceof Number) {
            return "¥" + String.format("%.2f", ((Number) value).doubleValue());
        }
        return "¥0.00";
    }

    private String formatDateTime(Object value) {
        if (value == null) return "";
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).format(DATE_FORMATTER);
        }
        return String.valueOf(value);
    }

    private String getOrderStatusText(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "待支付";
            case 1 -> "待发货";
            case 2 -> "待收货";
            case 3 -> "已完成";
            case 4 -> "已取消";
            default -> "未知";
        };
    }

    private void autoSizeColumns(Sheet sheet) {
        if (sheet.getRow(0) != null) {
            int lastCellNum = sheet.getRow(0).getLastCellNum();
            for (int i = 0; i < lastCellNum; i++) {
                sheet.autoSizeColumn(i);
            }
        }
    }

    private byte[] writeWorkbookToBytes(Workbook workbook) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}