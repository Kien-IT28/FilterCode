package com.devk.filtercode.controller;

import com.devk.filtercode.util.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

public class FilterCodeController {

    @FXML
    private Button btnSelectFile;
    @FXML
    private Label lblStatus;

    /**
     * Cấu trúc:
     * classifyMap[partNo][groupName][testMethod] = classify
     */
    private final Map<String, Map<String, Map<String, PartInfo>>> classifyMap = new HashMap<>();

    // class chứa classify + factory
    private static class PartInfo {
        String classify;
        String factory;

        PartInfo(String classify, String factory) {
            this.classify = classify;
            this.factory = factory;
        }
    }

    // Ham khoi tao
    @FXML
    public void initialize() {
        loadPartNosFromDatabase();
        btnSelectFile.setOnAction(e -> selectAndFilterExcel());
    }

    // 1. Load dữ liệu phân loại từ database
    private void loadPartNosFromDatabase() {
        String sql = "SELECT partNo, groupName, testMethod, classify, factory FROM PartNoP1";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String part = rs.getString("partNo").trim();
                String group = rs.getString("groupName").trim();
                String test = rs.getString("testMethod").trim();
                String classify = rs.getString("classify").trim();
                String factory = rs.getString("factory").trim();

                classifyMap
                        .computeIfAbsent(part, k -> new HashMap<>())
                        .computeIfAbsent(group, k -> new HashMap<>())
                        .put(test, new PartInfo(classify, factory));
            }

            lblStatus.setText("Đã load PartNoP1 (" + classifyMap.size() + " PartNo).");

        } catch (Exception e) {
            e.printStackTrace();
            lblStatus.setText("Lỗi kết nối database!");
        }
    }

    // 2. Chọn file Excel
    private void selectAndFilterExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn file Excel cần lọc");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
        );

        Stage stage = new Stage();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                File filteredFile = filterExcelFile(file);
                lblStatus.setText("Đã lọc xong → " + filteredFile.getAbsolutePath());
            } catch (Exception ex) {
                ex.printStackTrace();
                lblStatus.setText("Lỗi khi lọc file!");
            }
        }
    }

    // 3. Lọc dữ liệu Excel
    private File filterExcelFile(File file) throws IOException {

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            // Workbook mới
            Workbook newWorkbook = new XSSFWorkbook();
            Sheet sheetP1 = newWorkbook.createSheet("P1");
            Sheet sheetP2 = newWorkbook.createSheet("P2");
            Sheet sheetDataNew = newWorkbook.createSheet("DataNew");

            Row headerRow = sheet.getRow(0);

            // --- Map để nhóm theo factory + classify ---
            Map<String, List<Row>> p1Groups = new LinkedHashMap<>();
            Map<String, List<Row>> p2Groups = new LinkedHashMap<>();
            List<Row> dataNewList = new ArrayList<>();   // <<< NEW

            // ---- Duyệt từng dòng Excel input ----
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                String partNo  = formatter.formatCellValue(row.getCell(0)).trim();
                String group   = formatter.formatCellValue(row.getCell(1)).trim();
                String test    = formatter.formatCellValue(row.getCell(2)).trim();

                // NẾU KHÔNG TÌM THẤY TRONG DATABASE → ĐƯA VÀO SHEET DATA NEW
                if (!classifyMap.containsKey(partNo) ||
                        !classifyMap.get(partNo).containsKey(group) ||
                        !classifyMap.get(partNo).get(group).containsKey(test)) {

                    dataNewList.add(row);
                    continue;
                }

                // Có trong DB → phân loại vào P1/P2
                PartInfo info = classifyMap.get(partNo).get(group).get(test);
                String classifyKey = info.classify;

                if ("P1".equalsIgnoreCase(info.factory)) {
                    p1Groups.computeIfAbsent(classifyKey, k -> new ArrayList<>()).add(row);
                } else if ("P2".equalsIgnoreCase(info.factory)) {
                    p2Groups.computeIfAbsent(classifyKey, k -> new ArrayList<>()).add(row);
                }
            }

            // --- Viết dữ liệu ra Sheet P1 ---
            writeGroupToSheet(sheetP1, headerRow, p1Groups);

            // --- Viết dữ liệu ra Sheet P2 ---
            writeGroupToSheet(sheetP2, headerRow, p2Groups);

            // --- Viết dữ liệu ra Sheet DATANEW ---
            writeDataNew(sheetDataNew, headerRow, dataNewList);

            // Export file
            File outputFile = new File(file.getParent(), "Filtered_" + file.getName());
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                newWorkbook.write(fos);
            }

            return outputFile;
        }
    }

    // 4. Ham xet thu tu uu tien
    private int getPriority(String classify) {
        switch (classify.toUpperCase()) {
            case "PT RF":   return 1;
            case "PT WIRE": return 2;
            case "FT RF":   return 3;
            case "FT WIRE": return 4;
            default:        return 999;
        }
    }

    // 5. Ham viet luu trinh vao trang tinh
    private void writeGroupToSheet(Sheet sheet, Row headerRow, Map<String, List<Row>> groups) {

        // Sắp xếp lại key classify theo thứ tự ưu tiên
        List<String> orderedKeys = new ArrayList<>(groups.keySet());
        orderedKeys.sort(Comparator.comparingInt(this::getPriority));

        int rowIndex = 0;

        for (String classify : orderedKeys) {

            // Dòng tiêu đề nhóm (PT RF, PT WIRE…)
            Row groupTitle = sheet.createRow(rowIndex++);
            groupTitle.createCell(0).setCellValue(classify);

            // Dòng header
            Row newHeader = sheet.createRow(rowIndex++);
            copyRow(headerRow, newHeader);

            // Copy dòng data
            for (Row oldRow : groups.get(classify)) {
                Row newRow = sheet.createRow(rowIndex++);
                copyRow(oldRow, newRow);
            }

            // Dòng trống dưới mỗi nhóm
            rowIndex++;
        }
    }

    // 6. Ham viet data moi
    private void writeDataNew(Sheet sheet, Row headerRow, List<Row> list) {
        int rowIndex = 0;

        // Header
        Row newHeader = sheet.createRow(rowIndex++);
        copyRow(headerRow, newHeader);

        // Data
        for (Row oldRow : list) {
            Row newRow = sheet.createRow(rowIndex++);
            copyRow(oldRow, newRow);
        }
    }

    // 7. Hàm copy toàn bộ cell từ dòng cũ sang dòng mới
    private void copyRow(Row sourceRow, Row targetRow) {
        DataFormatter formatter = new DataFormatter();

        for (int i = 0; i < sourceRow.getLastCellNum(); i++) {
            Cell oldCell = sourceRow.getCell(i);
            Cell newCell = targetRow.createCell(i);

            if (oldCell == null) {
                newCell.setCellValue("");
                continue;
            }

            switch (oldCell.getCellType()) {
                case STRING:
                    newCell.setCellValue(oldCell.getStringCellValue());
                    break;
                case NUMERIC:
                    newCell.setCellValue(oldCell.getNumericCellValue());
                    break;
                case BOOLEAN:
                    newCell.setCellValue(oldCell.getBooleanCellValue());
                    break;
                case FORMULA:
                    try {
                        newCell.setCellFormula(oldCell.getCellFormula());
                    } catch (Exception e) {
                        newCell.setCellValue(formatter.formatCellValue(oldCell));
                    }
                    break;
                default:
                    newCell.setCellValue(formatter.formatCellValue(oldCell));
            }
        }
    }
}
