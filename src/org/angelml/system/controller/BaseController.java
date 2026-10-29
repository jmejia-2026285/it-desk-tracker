package org.angelml.system.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputControl;
import org.angelml.system.model.EstadoServicio;
import org.angelml.system.utils.AlertUtils;

import javafx.util.StringConverter;
import org.angelml.system.utils.ValidationsUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class BaseController {

    protected static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");

    protected <T> void runTask(Callable<T> work, Consumer<T> onSuccess) {
        runTask(work, onSuccess, this::showFailure);
    }

    protected <T> void runTask(Callable<T> work, Consumer<T> onSuccess, Consumer<Throwable> onFailure) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                T result = work.call();
                Platform.runLater(() -> onSuccess.accept(result));
                return result;
            }
        };
        task.setOnFailed(event -> onFailure.accept(task.getException()));
        Thread thread = new Thread(task, "db-task");
        thread.setDaemon(true);
        thread.start();
    }

    protected void runAction(Runnable work, Runnable onSuccess) {
        runTask(() -> {
            work.run();
            return Boolean.TRUE;
        }, done -> onSuccess.run());
    }

    protected void showFailure(Throwable error) {
        if (error instanceof IllegalArgumentException) {
            AlertUtils.warning("Revisa los datos", error.getMessage());
            return;
        }
        error.printStackTrace();
        AlertUtils.error("Ocurrió un problema", messageOf(error));
    }

    protected String messageOf(Throwable error) {
        if (error instanceof IllegalArgumentException || error instanceof IllegalStateException) {
            return error.getMessage();
        }
        return "Ocurrió un error inesperado. Intenta de nuevo.";
    }

    protected static void dateFormat(DatePicker picker) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        picker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date == null ? "" : formatter.format(date);
            }

            @Override
            public LocalDate fromString(String text) {
                try {
                    return ValidationsUtils.isBlank(text) ? null : LocalDate.parse(text.trim(), formatter);
                } catch (DateTimeParseException e) {
                    return null;
                }
            }
        });
        picker.getEditor().setPromptText("dd/mm/aaaa");
    }

    protected static void limit(TextInputControl control, int maxLength) {
        control.setTextFormatter(new TextFormatter<String>(change -> change.getControlNewText().length() <= maxLength ? change : null));
    }

    protected static <S> void textColumn(TableColumn<S, String> column, Function<S, String> getter) {
        column.setCellValueFactory(cell -> new SimpleStringProperty(getter.apply(cell.getValue())));
    }

    protected static <S> void dateTimeColumn(TableColumn<S, LocalDateTime> column, Function<S, LocalDateTime> getter) {
        column.setCellValueFactory(cell -> new SimpleObjectProperty<>(getter.apply(cell.getValue())));
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty ? null : value == null ? "—" : DATE_TIME.format(value));
            }
        });
    }

    protected static <S> void statusColumn(TableColumn<S, EstadoServicio> column, Function<S, EstadoServicio> getter) {
        column.setCellValueFactory(cell -> new SimpleObjectProperty<>(getter.apply(cell.getValue())));
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(EstadoServicio value, boolean empty) {
                super.updateItem(value, empty);
                getStyleClass().removeAll("estado-pendiente", "estado-proceso", "estado-completado");
                if (empty || value == null) {
                    setText(null);
                    return;
                }
                setText(value.toString());
                getStyleClass().add(switch (value) {
                    case PENDIENTE -> "estado-pendiente";
                    case EN_PROCESO -> "estado-proceso";
                    case COMPLETADO -> "estado-completado";
                });
            }
        });
    }
}
