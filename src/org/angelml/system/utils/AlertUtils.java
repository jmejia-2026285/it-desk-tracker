package org.angelml.system.utils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public final class AlertUtils {

    private static final String MEDIA = "/org/angelml/system/resources/media/";
    private static final String STYLES = "/org/angelml/system/resources/styles/";

    private AlertUtils() {
    }

    public static void success(String title, String message) {
        show(MEDIA + "gif/check-animation.gif", title, message, "Aceptar", false);
    }

    public static void info(String title, String message) {
        show(MEDIA + "gif/information-animation.gif", title, message, "Aceptar", false);
    }

    public static void warning(String title, String message) {
        show(MEDIA + "gif/warning.gif", title, message, "Entendido", false);
    }

    public static void error(String title, String message) {
        show(MEDIA + "images/error-alert.png", title, message, "Cerrar", false);
    }

    public static void accessDenied(String message) {
        show(MEDIA + "gif/acceso-denegado.gif", "Acceso denegado", message, "Cerrar", false);
    }

    public static void printed(String message) {
        show(MEDIA + "gif/printed-icon.gif", "Tickets impresos", message, "Aceptar", false);
    }

    public static void printFailed(String message) {
        show(MEDIA + "images/print-fail-icon-415px.png", "No se pudo imprimir", message, "Cerrar", false);
    }

    public static boolean confirm(String title, String message) {
        return show(MEDIA + "gif/information-animation.gif", title, message, "Confirmar", true);
    }

    public static boolean confirmDelete(String message) {
        return show(MEDIA + "gif/deleted-100px.gif", "Confirmar eliminación", message, "Eliminar", true);
    }

    private static boolean show(String iconPath, String title, String message, String acceptText, boolean cancelable) {
        boolean[] accepted = {false};
        Stage stage = new Stage(StageStyle.TRANSPARENT);
        stage.initModality(Modality.APPLICATION_MODAL);
        if (SceneManager.getStage() != null) {
            stage.initOwner(SceneManager.getStage());
        }

        ImageView icon = new ImageView(new Image(AlertUtils.class.getResource(iconPath).toExternalForm()));
        icon.setFitHeight(84);
        icon.setFitWidth(84);
        icon.setPreserveRatio(true);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("alert-title");
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("alert-message");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(340);

        Button accept = new Button(acceptText);
        accept.getStyleClass().add("btn-primary");
        accept.setDefaultButton(true);
        accept.setOnAction(event -> {
            accepted[0] = true;
            stage.close();
        });
        HBox buttons = new HBox(12, accept);
        buttons.setAlignment(Pos.CENTER);
        if (cancelable) {
            Button cancel = new Button("Cancelar");
            cancel.getStyleClass().add("btn-secondary");
            cancel.setCancelButton(true);
            cancel.setOnAction(event -> stage.close());
            buttons.getChildren().add(0, cancel);
        }

        VBox card = new VBox(14, icon, titleLabel, messageLabel, buttons);
        card.getStyleClass().add("alert-card");
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(400);

        StackPane root = new StackPane(card);
        root.getStyleClass().add("alert-root");
        root.setPadding(new Insets(24));

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().addAll(
                AlertUtils.class.getResource(STYLES + "styles.css").toExternalForm(),
                AlertUtils.class.getResource(STYLES + "AlertStyles.css").toExternalForm());
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                stage.close();
            }
        });
        stage.setScene(scene);
        stage.showAndWait();
        return accepted[0];
    }
}
