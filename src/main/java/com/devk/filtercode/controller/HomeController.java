package com.devk.filtercode.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class HomeController {
    @FXML
    private StackPane contentArea;

    @FXML
    private void initialize(){
        loadView("filtercode.fxml");
    }

    @FXML
    private void btnEditor() {
        loadView("editor.fxml");
    }

    @FXML
    private void btnFilterCode() {
        loadView("filtercode.fxml");
    }

    @FXML
    private void btnAbout() {
        loadView("about.fxml");
    }

    @FXML
    private void btnTrainingData(){
        loadView("trainingdata.fxml");
    }

    private void loadView(String fxmlFile) {
        try {
            Node node = FXMLLoader.load(getClass().getResource("/com/devk/filtercode/ui/" + fxmlFile));
            contentArea.getChildren().setAll(node);
        } catch (IOException e) {
            showAlert("Lỗi", "Không thể tải trang " + fxmlFile, Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
