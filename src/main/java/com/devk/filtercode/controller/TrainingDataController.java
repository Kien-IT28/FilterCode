package com.devk.filtercode.controller;

import com.devk.filtercode.util.DatabaseConnection;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class TrainingDataController {

    @FXML
    private Button btnSelectFile;

    @FXML
    private Button btnInsert;

    @FXML
    private Button btnCancel;

    @FXML
    private Label lblFileName;

    private File selectedFile;

    @FXML
    private void initialize() {
        lblFileName.setText("No file selected");
    }

    @FXML
    private void btnSelectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Excel File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls")
        );

        Stage stage = (Stage) btnSelectFile.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            selectedFile = file;
            lblFileName.setText(file.getName());
        }
    }

    @FXML
    private void btnInsert() {
        if (selectedFile == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText("Please select an Excel file first!");
            alert.showAndWait();
            return;
        }

        // Alert đang xử lý
        Alert progressAlert = new Alert(Alert.AlertType.INFORMATION);
        progressAlert.setTitle("Processing");
        progressAlert.setHeaderText(null);
        progressAlert.setContentText("Inserting data, please wait...");
        progressAlert.show();

        // Task chạy nền
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {

                try (Connection conn = DatabaseConnection.getConnection();
                     FileInputStream fis = new FileInputStream(selectedFile)) {

                    Workbook workbook = WorkbookFactory.create(fis);
                    Sheet sheet = workbook.getSheetAt(0);

                    // SQL đúng số cột
                    String sql = "INSERT INTO PartNoP1 (PartNo, GroupName, TestMethod, Classify, Factory) VALUES (?, ?, ?, ?, ?)";

                    conn.setAutoCommit(false); // Tăng tốc độ insert
                    PreparedStatement ps = conn.prepareStatement(sql);

                    for (Row row : sheet) {
                        if (row.getRowNum() == 0) continue; // Skip header

                        String partNo = getCellValue(row, 0);
                        String groupName = getCellValue(row, 1);
                        String testMethod = getCellValue(row, 2);
                        String classify = getCellValue(row, 3);
                        String factory = getCellValue(row, 4);

                        ps.setString(1, partNo);
                        ps.setString(2, groupName);
                        ps.setString(3, testMethod);
                        ps.setString(4, classify);
                        ps.setString(5, factory);

                        ps.addBatch();

                        // Thực thi batch mỗi 200 dòng
                        if (row.getRowNum() % 200 == 0) {
                            ps.executeBatch();
                        }
                    }

                    ps.executeBatch();  // chạy phần còn lại
                    conn.commit();

                    workbook.close();
                }
                return null;
            }
        };

        task.setOnSucceeded(event -> {
            progressAlert.close();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Data inserted successfully!");
            alert.showAndWait();
        });

        task.setOnFailed(event -> {
            progressAlert.close();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to insert data!");
            alert.showAndWait();
        });

        new Thread(task).start();
    }

    private String getCellValue(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) return "";  // tránh lỗi

        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }

    @FXML
    private void btnCancel() {
        // Xóa file đã chọn
        selectedFile = null;

        // Nếu có Label hiển thị tên file, cũng nên xóa
        lblFileName.setText("No file selected");
    }
}
