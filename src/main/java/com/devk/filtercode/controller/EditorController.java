package com.devk.filtercode.controller;

import com.devk.filtercode.model.PartNo;
import com.devk.filtercode.util.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Optional;

public class EditorController {
    private ObservableList<PartNo> data = FXCollections.observableArrayList();
    @FXML private TableView<PartNo> tableData;
    @FXML private TableColumn<PartNo, String> colPartNo;
    @FXML private TableColumn<PartNo, String> colId;
    @FXML private TableColumn<PartNo, String> colGroupName;
    @FXML private TableColumn<PartNo, String> colTestMethod;
    @FXML private TableColumn<PartNo, String> colClassify;
    @FXML private TableColumn<PartNo, String> colFactory;
    @FXML private TextField txtPartNo;
    @FXML private TextField txtGroupName;
    @FXML private TextField txtClassify;
    @FXML private TextField txtTestMethod;
    @FXML private TextField txtFactory;

    @FXML
    private void initialize(){
        // Gán cell value factory
        colId.setCellValueFactory(new PropertyValueFactory<>("Id"));
        colPartNo.setCellValueFactory(new PropertyValueFactory<>("partNo"));
        colGroupName.setCellValueFactory(new PropertyValueFactory<>("groupName"));
        colTestMethod.setCellValueFactory(new PropertyValueFactory<>("testMethod"));
        colClassify.setCellValueFactory(new PropertyValueFactory<>("classify"));
        colFactory.setCellValueFactory(new PropertyValueFactory<>("factory"));

        loadData();
        // Khi chọn 1 hàng trong TableView, hiển thị lên TextField
        tableData.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                txtPartNo.setText(newSelection.getPartNo());
                txtGroupName.setText(newSelection.getGroupName());
                txtTestMethod.setText(newSelection.getTestMethod());
                txtClassify.setText(newSelection.getClassify());
                txtFactory.setText(newSelection.getFactory());
            }
        });
    }

    private void loadData() {
        String sql = "SELECT Id, PartNo, GroupName, TestMethod, Classify, Factory FROM PartNoP1";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            data.clear();

            while (resultSet.next()) {
                data.add(new PartNo(
                        resultSet.getInt("id"),
                        resultSet.getString("PartNo"),
                        resultSet.getString("GroupName"),
                        resultSet.getString("TestMethod"),
                        resultSet.getString("Classify"),
                        resultSet.getString("Factory")
                ));
            }

            tableData.setItems(data);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void btnSearch() {
        // Lấy từ khóa, chuyển về lowercase
        String partNoText = txtPartNo.getText() != null ? txtPartNo.getText().trim().toLowerCase() : "";
        String groupNameText = txtGroupName.getText() != null ? txtGroupName.getText().trim().toLowerCase() : "";
        String testMethodText = txtTestMethod.getText() != null ? txtTestMethod.getText().trim().toLowerCase() : "";
        String classifyText = txtClassify.getText() != null ? txtClassify.getText().trim().toLowerCase() : "";
        String factoryText = txtFactory.getText() != null ? txtFactory.getText().trim().toLowerCase() : "";

        if (partNoText.isEmpty() && groupNameText.isEmpty() && testMethodText.isEmpty() && classifyText.isEmpty() && factoryText.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Thông báo");
            alert.setHeaderText(null);
            alert.setContentText("Vui lòng nhập dữ liệu cần tìm!");
            alert.showAndWait();
            return;
        }

        // Dùng FilteredList để tối ưu
        FilteredList<PartNo> filteredList = new FilteredList<>(data, p -> true);
        filteredList.setPredicate(item -> {
            boolean matches = true;

            // Chuyển về lowercase để không phân biệt chữ hoa chữ thường
            String itemPartNo = item.getPartNo() != null ? item.getPartNo().toLowerCase() : "";
            String itemGroupName = item.getGroupName() != null ? item.getGroupName().toLowerCase() : "";
            String itemTestMethod = item.getTestMethod() != null ? item.getTestMethod().toLowerCase() : "";
            String itemClassify = item.getClassify() != null ? item.getClassify().toLowerCase() : "";
            String itemFactory = item.getFactory() != null ? item.getFactory().toLowerCase() : "";

            if (!partNoText.isEmpty() && !itemPartNo.contains(partNoText)) matches = false;
            if (!groupNameText.isEmpty() && !itemGroupName.contains(groupNameText)) matches = false;
            if (!testMethodText.isEmpty() && !itemTestMethod.contains(testMethodText)) matches = false;
            if (!classifyText.isEmpty() && !itemClassify.contains(classifyText)) matches = false;
            if (!factoryText.isEmpty() && !itemFactory.contains(factoryText)) matches = false;

            return matches;
        });

        // Gán cho TableView
        tableData.setItems(filteredList);
    }

    @FXML
    private void btnRefresh(){
        txtPartNo.clear();
        txtGroupName.clear();
        txtTestMethod.clear();
        txtClassify.clear();
        txtFactory.clear();

        loadData();
    }

    @FXML
    private void btnAdd() {
        try (Connection conn = DatabaseConnection.getConnection()) {

            String partNo = txtPartNo.getText().trim();
            String groupName = txtGroupName.getText().trim();
            String testMethod = txtTestMethod.getText().trim();
            String classify = txtClassify.getText().trim();
            String factory = txtFactory.getText().trim();

            if (partNo.isEmpty() || groupName.isEmpty() || testMethod.isEmpty() || classify.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Thông báo");
                alert.setHeaderText(null);
                alert.setContentText("Vui lòng nhập đủ thông tin!");
                alert.showAndWait();
                return;
            }

            // ======== CHECK DỮ LIỆU TRÙNG ========
            String checkSql = "SELECT COUNT(*) FROM PartNoP1 WHERE PartNo=? AND GroupName=? AND TestMethod=? AND Classify=? AND Factory=?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, partNo);
            checkStmt.setString(2, groupName);
            checkStmt.setString(3, testMethod);
            checkStmt.setString(4, classify);
            checkStmt.setString(5, factory);

            ResultSet rs = checkStmt.executeQuery();
            rs.next();

            if (rs.getInt(1) > 0) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Thông báo");
                alert.setHeaderText(null);
                alert.setContentText("Dữ liệu đã tồn tại trong hệ thống!");
                alert.showAndWait();
                return;
            }

            // ======== INSERT DỮ LIỆU ========
            String insertSql = "INSERT INTO PartNoP1 (PartNo, GroupName, TestMethod, Classify, Factory) VALUES (?, ?, ?, ?, ?)";

            PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            insertStmt.setString(1, partNo);
            insertStmt.setString(2, groupName);
            insertStmt.setString(3, testMethod);
            insertStmt.setString(4, classify);
            insertStmt.setString(5, factory);

            insertStmt.executeUpdate();

            // ======== LẤY ID TỰ TĂNG TRẢ VỀ ========
            ResultSet generatedKeys = insertStmt.getGeneratedKeys();
            int newId = 0;
            if (generatedKeys.next()) {
                newId = generatedKeys.getInt(1);
            }

            // ======== THÊM VÀO TABLEVIEW ========
            data.add(new PartNo(newId, partNo, groupName, testMethod, classify, factory));
            tableData.setItems(data);

            // ======== XOÁ INPUT FORM ========
            txtPartNo.clear();
            txtGroupName.clear();
            txtTestMethod.clear();
            txtClassify.clear();
            txtFactory.clear();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void btnEdit() {
        PartNo selected = tableData.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Thông báo");
            alert.setHeaderText(null);
            alert.setContentText("Vui lòng chọn mã cần sửa!");
            alert.showAndWait();
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {

            String partNo = txtPartNo.getText().trim();
            String groupName = txtGroupName.getText().trim();
            String testMethod = txtTestMethod.getText().trim();
            String classify = txtClassify.getText().trim();
            String factory = txtFactory.getText().trim();

            if (partNo.isEmpty() || groupName.isEmpty() || testMethod.isEmpty() || classify.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Thông báo");
                alert.setHeaderText(null);
                alert.setContentText("Vui lòng nhập đủ thông tin!");
                alert.showAndWait();
                return;
            }

            String sql = """
            UPDATE PartNoP1
            SET PartNo = ?, GroupName = ?, TestMethod = ?, Classify = ?, Factory = ?
            WHERE id = ?
        """;

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, partNo);
            ps.setString(2, groupName);
            ps.setString(3, testMethod);
            ps.setString(4, classify);
            ps.setString(5, factory);
            ps.setInt(6, selected.getId());   // UPDATE đúng theo ID

            // ====== QUAN TRỌNG: chạy UPDATE vào database ======
            ps.executeUpdate();

            // Cập nhật lại object trong TableView
            selected.setPartNo(partNo);
            selected.setGroupName(groupName);
            selected.setTestMethod(testMethod);
            selected.setClassify(classify);
            selected.setFactory(factory);
            tableData.refresh();

            // Clear input
            txtPartNo.clear();
            txtGroupName.clear();
            txtTestMethod.clear();
            txtClassify.clear();
            txtFactory.clear();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void btnDelete() {
        PartNo selected = tableData.getSelectionModel().getSelectedItem();

        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Thông báo");
            alert.setHeaderText(null);
            alert.setContentText("Vui lòng chọn hàng cần xóa!");
            alert.showAndWait();
            return;
        }

        // Hộp thoại xác nhận
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận xóa");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Bạn có chắc chắn muốn xóa PartNo: " + selected.getPartNo() + "?");

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DatabaseConnection.getConnection()) {

                String sql = "DELETE FROM PartNoP1 WHERE id = ?";

                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, selected.getId());  // ← Xóa theo ID

                ps.executeUpdate();

                // Xóa hỏi TableView
                data.remove(selected);
                tableData.refresh();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
