package org.angelml.system;

import javafx.application.Application;
import javafx.stage.Stage;
import org.angelml.system.utils.SceneManager;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        SceneManager.init(stage);
        SceneManager.showLogin();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
