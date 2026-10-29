package org.angelml.system.utils;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.stage.Window;
import org.angelml.system.model.Dispositivo;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class TicketUtils {

    public static final double TICKET_WIDTH = 396;
    public static final double TICKET_HEIGHT = 612;

    private static final String STYLESHEET = "/org/angelml/system/resources/styles/styles.css";
    private static final String LOGO = "/org/angelml/system/resources/media/logos/01_logo_horizontal_transparente.png";
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String EMPTY = "—";

    private static final int START_B = 104;
    private static final int STOP = 106;
    private static final String[] CODE128 = {
        "212222", "222122", "222221", "121223", "121322", "131222", "122213", "122312", "132212", "221213",
        "221312", "231212", "112232", "122132", "122231", "113222", "123122", "123221", "223211", "221132",
        "221231", "213212", "223112", "312131", "311222", "321122", "321221", "312212", "322112", "322211",
        "212123", "212321", "232121", "111323", "131123", "131321", "112313", "132113", "132311", "211313",
        "231113", "231311", "112133", "112331", "132131", "113123", "113321", "133121", "313121", "211331",
        "231131", "213113", "213311", "213131", "311123", "311321", "331121", "312113", "312311", "332111",
        "314111", "221411", "431111", "111224", "111422", "121124", "121421", "141122", "141221", "112214",
        "112412", "122114", "122411", "142112", "142211", "241211", "221114", "413111", "241112", "134111",
        "111242", "121142", "121241", "114212", "124112", "124211", "411212", "421112", "421211", "212141",
        "214121", "412121", "111143", "111341", "131141", "114113", "114311", "411113", "411311", "113141",
        "114131", "311141", "411131", "211412", "211214", "211232", "2331112"
    };

    private TicketUtils() {
    }

    public static Node generarTicketEquipo(Dispositivo dispositivo) {
        VBox ticket = baseTicket("ticket-equipo");
        ticket.setSpacing(14);

        Label title = text("TICKET DE EQUIPO", "ticket-titulo");

        VBox internalCode = new VBox(2, text("CÓDIGO INTERNO", "ticket-label"),
                text(dispositivo.getCodigoInterno(), "ticket-codigo"));

        VBox barcode = new VBox(4, barcode(dispositivo.getFolio(), 330, 74),
                text(orDash(dispositivo.getFolio()), "ticket-valor"), text("Número de seguimiento", "ticket-label"));
        barcode.setAlignment(Pos.CENTER);

        GridPane details = grid();
        details.add(field("FECHA DE INGRESO", dispositivo.getFechaIngreso() == null ? EMPTY : DATE_TIME.format(dispositivo.getFechaIngreso())), 0, 0);
        details.add(field("ESTADO", dispositivo.getEstado().toString()), 1, 0);
        details.add(field("CLIENTE", shortName(dispositivo.getCliente())), 0, 1);
        details.add(field("TELÉFONO", dispositivo.getTelefono()), 1, 1);
        details.add(field("EQUIPO", dispositivo.getTipo() + " " + dispositivo.getEquipo()), 0, 2, 2, 1);

        ticket.getChildren().addAll(title, internalCode, barcode, details);
        return ticket;
    }

    public static Node generarTicketCliente(Dispositivo dispositivo) {
        VBox ticket = baseTicket("ticket-cliente");
        ticket.setSpacing(9);

        ImageView logo = new ImageView(new Image(TicketUtils.class.getResource(LOGO).toExternalForm()));
        logo.setFitWidth(132);
        logo.setPreserveRatio(true);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        VBox folio = new VBox(2, text("FOLIO", "ticket-label"), text(orDash(dispositivo.getFolio()), "ticket-valor"));
        folio.setAlignment(Pos.CENTER_RIGHT);
        HBox header = new HBox(logo, spacer, folio);
        header.setAlignment(Pos.CENTER_LEFT);

        GridPane details = grid();
        details.add(field("FECHA Y HORA DE INGRESO", dispositivo.getFechaIngreso() == null ? EMPTY : DATE_TIME.format(dispositivo.getFechaIngreso())), 0, 0);
        details.add(field("FECHA ESTIMADA DE ENTREGA", dispositivo.getFechaEstimada() == null ? "Por confirmar" : DATE.format(dispositivo.getFechaEstimada())), 1, 0);
        details.add(field("CLIENTE", dispositivo.getCliente()), 0, 1, 2, 1);
        details.add(field("TELÉFONO", dispositivo.getTelefono()), 0, 2);
        details.add(field("CORREO", dispositivo.getCorreo()), 1, 2);
        details.add(field("EQUIPO", dispositivo.getTipo() + " " + dispositivo.getEquipo()), 0, 3);
        details.add(field("NÚMERO DE SERIE", dispositivo.getSerie()), 1, 3);
        details.add(field("MOTIVO DE LA FALLA", dispositivo.getProblematica()), 0, 4, 2, 1);
        details.add(field("ACCESORIOS DEJADOS", dispositivo.getAccesorios()), 0, 5, 2, 1);
        details.add(field("DIAGNÓSTICO INICIAL", dispositivo.getDiagnostico()), 0, 6, 2, 1);
        details.add(field("TÉCNICO QUE RECIBE", dispositivo.getTecnicoNombre()), 0, 7, 2, 1);
        details.add(field("NOTAS ADICIONALES", dispositivo.getNotas()), 0, 8, 2, 1);

        Label footer = text("Conserve este comprobante: lo necesitará para retirar su equipo.", "ticket-label");
        footer.setWrapText(true);

        ticket.getChildren().addAll(header, text("COMPROBANTE DE INGRESO", "ticket-titulo"), details, footer);
        return ticket;
    }

    public static Node preview(Node ticket, double scale) {
        ticket.getTransforms().add(new Scale(scale, scale));
        return new Group(ticket);
    }

    public static boolean hasPrinters() {
        return !Printer.getAllPrinters().isEmpty();
    }

    public static PrinterJob createPrintJob(Window owner) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            return null;
        }
        if (!job.showPrintDialog(owner)) {
            job.cancelJob();
            return null;
        }
        return job;
    }

    public static boolean imprimirTicketsEnHoja(Node ticketEquipo, Node ticketCliente) {
        PrinterJob job = createPrintJob(null);
        return job != null && imprimirTicketsEnHoja(job, ticketEquipo, ticketCliente);
    }

    public static boolean imprimirTicketsEnHoja(PrinterJob job, Node ticketEquipo, Node ticketCliente) {
        HBox sheet = new HBox(ticketEquipo, ticketCliente);
        sheet.setStyle("-fx-background-color: white;");
        Scene scene = new Scene(sheet, TICKET_WIDTH * 2, TICKET_HEIGHT, Color.WHITE);
        scene.getStylesheets().add(TicketUtils.class.getResource(STYLESHEET).toExternalForm());

        PageLayout layout = job.getPrinter().createPageLayout(Paper.NA_LETTER, PageOrientation.LANDSCAPE, Printer.MarginType.HARDWARE_MINIMUM);
        job.getJobSettings().setPageLayout(layout);
        double scale = Math.min(1.0, Math.min(layout.getPrintableWidth() / (TICKET_WIDTH * 2), layout.getPrintableHeight() / TICKET_HEIGHT));
        sheet.getTransforms().add(new Scale(scale, scale));
        sheet.applyCss();
        sheet.layout();

        boolean printed = job.printPage(layout, sheet);
        if (printed) {
            job.endJob();
        } else {
            job.cancelJob();
        }
        return printed;
    }

    static List<Boolean> code128Modules(String content) {
        List<Integer> symbols = new ArrayList<>();
        symbols.add(START_B);
        int checksum = START_B;
        for (int position = 0; position < content.length(); position++) {
            int value = content.charAt(position) - 32;
            if (value < 0 || value > 94) {
                throw new IllegalArgumentException("Carácter no válido para el código de barras: " + content.charAt(position));
            }
            symbols.add(value);
            checksum += value * (position + 1);
        }
        symbols.add(checksum % 103);
        symbols.add(STOP);

        List<Boolean> modules = new ArrayList<>();
        for (int symbol : symbols) {
            boolean bar = true;
            for (char width : CODE128[symbol].toCharArray()) {
                for (int count = 0; count < width - '0'; count++) {
                    modules.add(bar);
                }
                bar = !bar;
            }
        }
        return modules;
    }

    private static Node barcode(String content, double width, double height) {
        Pane pane = new Pane();
        pane.setPrefSize(width, height);
        pane.setMinSize(width, height);
        pane.setMaxSize(width, height);
        pane.setSnapToPixel(false);
        if (ValidationsUtils.isBlank(content)) {
            return pane;
        }
        List<Boolean> modules = code128Modules(content);
        int quietZone = 10;
        double moduleWidth = width / (modules.size() + quietZone * 2);
        int index = 0;
        while (index < modules.size()) {
            if (!modules.get(index)) {
                index++;
                continue;
            }
            int start = index;
            while (index < modules.size() && modules.get(index)) {
                index++;
            }
            Rectangle bar = new Rectangle((quietZone + start) * moduleWidth, 0, (index - start) * moduleWidth, height);
            bar.setFill(Color.BLACK);
            bar.setSmooth(false);
            pane.getChildren().add(bar);
        }
        return pane;
    }

    private static VBox baseTicket(String variantClass) {
        VBox ticket = new VBox();
        ticket.getStyleClass().addAll("ticket-container", variantClass);
        ticket.setPrefSize(TICKET_WIDTH, TICKET_HEIGHT);
        ticket.setMinSize(TICKET_WIDTH, TICKET_HEIGHT);
        ticket.setMaxSize(TICKET_WIDTH, TICKET_HEIGHT);
        ticket.setPadding(new Insets(15));
        return ticket;
    }

    private static GridPane grid() {
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(9);
        ColumnConstraints left = new ColumnConstraints();
        left.setPercentWidth(50);
        ColumnConstraints right = new ColumnConstraints();
        right.setPercentWidth(50);
        grid.getColumnConstraints().addAll(left, right);
        return grid;
    }

    private static VBox field(String caption, String value) {
        Label valueLabel = text(orDash(value), "ticket-valor");
        valueLabel.setWrapText(true);
        VBox field = new VBox(1, text(caption, "ticket-label"), valueLabel);
        GridPane.setHalignment(field, HPos.LEFT);
        return field;
    }

    private static Label text(String content, String styleClass) {
        Label label = new Label(content);
        label.getStyleClass().add(styleClass);
        return label;
    }

    private static String orDash(String value) {
        return ValidationsUtils.isBlank(value) ? EMPTY : value.trim();
    }

    private static String shortName(String fullName) {
        if (ValidationsUtils.isBlank(fullName)) {
            return EMPTY;
        }
        String[] words = fullName.trim().split("\\s+");
        return words.length <= 2 ? fullName.trim() : words[0] + " " + words[1];
    }
}
