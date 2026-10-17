package dev.dixmk.language.minecraft.app;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("main.fxml"));
        stage.setTitle("MC Language Tools");
        stage.setScene(new Scene(loader.load()));
        
        MainController controller = loader.getController();
        controller.setHostServices(getHostServices());
        
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
