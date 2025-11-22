module com.devk.filtercode {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires javafx.base;
    requires javafx.graphics;


    opens com.devk.filtercode to javafx.fxml;
    opens com.devk.filtercode.model to javafx.base; // nếu dùng PropertyValueFactory
    // Cho phép FXMLLoader truy cập controller
    opens com.devk.filtercode.controller to javafx.fxml;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml; // nếu dùng XSSFWorkbook


    exports com.devk.filtercode;
}