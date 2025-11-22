package com.devk.filtercode;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/devk/filtercode/ui/home.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1200, 500); // Tạo Scene với Parent
        primaryStage.setTitle("DEV K");
        primaryStage.getIcons().add(new Image(
                getClass().getResourceAsStream("/com/devk/filtercode/icons/code.png")
        ));
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
