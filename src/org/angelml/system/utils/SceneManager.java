package org.angelml.system.utils;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.angelml.system.config.AppConfig;

import java.util.function.Consumer;

public final class SceneManager {

    public static final String LOGIN = "LoginView";
    public static final String DASHBOARD = "DashboardView";
    public static final String DISPOSITIVOS = "DispositivoView";
    public static final String REG_DISPOSITIVO = "RegDispositivoView";
    public static final String EDITAR_DISPOSITIVO = "EditarDispositivoView";
    public static final String TICKETS = "TicketView";
    public static final String REG_TICKET = "RegTicketView";
    public static final String IMPRIMIR_TICKET = "ImprimirTicketView";
    public static final String CALENDARIO = "CalendarioView";
    public static final String PERSONAL = "PersonalView";

    private static Stage primaryStage;
    private static StackPane contentHost;

    private SceneManager() {
    }

    public static void init(Stage stage) {
        primaryStage = stage;
        stage.setTitle(AppConfig.APP_NAME);
        stage.getIcons().add(new Image(SceneManager.class.getResource(
                "/org/angelml/system/resources/media/logos/08_icono_azul_transparente.png").toExternalForm()));
    }

    public static Stage getStage() {
        return primaryStage;
    }

    public static void showLogin() {
        contentHost = null;
        ViewFactory.Loaded<Object> view = ViewFactory.load(LOGIN);
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(BackgroundUtils.withBackground(view.root())));
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void showDashboard() {
        ViewFactory.Loaded<Object> view = ViewFactory.load(DASHBOARD);
        primaryStage.setScene(new Scene(BackgroundUtils.withBackground(view.root())));
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(720);
        primaryStage.setWidth(1280);
        primaryStage.setHeight(780);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void setContentHost(StackPane host) {
        contentHost = host;
    }

    public static <T> T navigate(String viewName) {
        if (contentHost == null) {
            throw new IllegalStateException("No hay una ventana principal abierta.");
        }
        ViewFactory.Loaded<T> view = ViewFactory.load(viewName);
        contentHost.getChildren().setAll(view.root());
        AnimationUtils.fadeIn(view.root());
        return view.controller();
    }

    public static <T> void showDialog(String viewName, String title, Consumer<T> setup) {
        ViewFactory.Loaded<T> view = ViewFactory.load(viewName);
        Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(title);
        dialog.getIcons().setAll(primaryStage.getIcons());
        dialog.setScene(new Scene(BackgroundUtils.withBackground(view.root())));
        if (setup != null) {
            setup.accept(view.controller());
        }
        dialog.setResizable(false);
        dialog.showAndWait();
    }

    public static void closeDialog(javafx.scene.Node anyNodeOfDialog) {
        ((Stage) anyNodeOfDialog.getScene().getWindow()).close();
    }
}
