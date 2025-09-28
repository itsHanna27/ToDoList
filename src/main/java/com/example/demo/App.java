package com.example.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage primaryStage) {
        try {
            // Correctly load FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/ToDo.fxml"));
            Parent root = loader.load();

            // Create scene and apply stylesheet
            scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/example/demo/style.css").toExternalForm());

            // Get controller and pass the stage to it
            ToDoController controller = loader.getController();
            controller.setStage(primaryStage);

            // Remove window decorations
            primaryStage.initStyle(StageStyle.UNDECORATED);

            // Apply scene to stage
            primaryStage.setScene(scene);
            primaryStage.setTitle("To-Do List");
            primaryStage.setResizable(false);
            primaryStage.setWidth(529);
            primaryStage.setHeight(483);
            primaryStage.setFullScreen(false);
            primaryStage.setFullScreenExitHint("");

            // Add app icon
            primaryStage.getIcons().add(new javafx.scene.image.Image(
                    getClass().getResource("/com/example/demo/icon.jpg").toExternalForm()));

            // Show the app
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Scene getScene() {
        return scene;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
