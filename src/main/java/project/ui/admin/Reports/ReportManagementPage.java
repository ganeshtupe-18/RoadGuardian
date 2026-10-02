package project.ui.admin.Reports;

import com.google.cloud.firestore.Firestore;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import project.controller.admin.ReportController;
import project.firebase.FirebaseConfig;
import project.model.Report;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ReportManagementPage {

    private static final String BG = "#0F0F0F";
    private static final String SURFACE = "#1A1A1A";
    private static final String CARD = "#1A1A1A";
    private static final String BORDER = "#F59E0B";
    private static final String HEADING = "#F3F4F6";
    private static final String TEXT = "#A1A1AA";
    private static final String MUTED = "#A1A1AA";
    private static final String BLUE = "#F59E0B";
    private static final String GREEN = "#22C55E";
    private static final String ORANGE = "#F59E0B";
    private static final String RED = "#EF4444";
    private static final String PURPLE = "#F59E0B";

    private final StackPane root = new StackPane();
    private final List<Report> allReports = new ArrayList<>();

    private ReportController controller;
    private VBox managementView;
    private VBox reportCards;
    private ScrollPane recordsScroll;
    private TextField searchField;
    private ComboBox<String> typeFilter;
    private ComboBox<String> sortFilter;
    private Label resultCountLabel;
    private Label totalLabel;
    private Label serviceLabel;
    private Label sosLabel;
    private Label otherLabel;
    private double savedScroll = 0.0;

    public ReportManagementPage() {
        root.setStyle("-fx-background-color: " + BG + ";");

        try {
            Firestore firestore = FirebaseConfig.getFirestore();
            controller = new ReportController(firestore);
            showManagementPage(false);
        } catch (Exception e) {
            e.printStackTrace();
            root.getChildren().setAll(createFatalError());
        }
    }

    public VBox getView() {
        VBox wrapper = new VBox(root);
        wrapper.setStyle("-fx-background-color: " + BG + ";");
        VBox.setVgrow(root, Priority.ALWAYS);
        return wrapper;
    }

    private void showManagementPage(boolean restoreScroll) {
        double position = restoreScroll ? savedScroll : 0.0;
        managementView = createManagementView();
        root.getChildren().setAll(managementView);
        loadReports();

        Platform.runLater(() -> {
            if (recordsScroll != null) {
                recordsScroll.setVvalue(position);
            }
        });
    }

    private VBox createManagementView() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(28, 32, 32, 32));
        page.setStyle("-fx-background-color: " + BG + ";");
        page.getChildren().addAll(
                createHeader(),
                createStatistics(),
                createToolbar(),
                createRecordsCard()
        );
        return page;
    }

    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titles = new VBox(5);
        Label title = new Label("Reports");
        title.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 30px; -fx-font-weight: bold;");
        Label subtitle = new Label("Generate Firebase-backed operational reports from actual RoadGuardian records.");
        subtitle.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 13px;");
        titles.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button generate = primaryButton("+ Generate Report");
        generate.setOnAction(e -> showGeneratePage());

        header.getChildren().addAll(titles, spacer, generate);
        return header;
    }

    private HBox createStatistics() {
        HBox row = new HBox(12);

        totalLabel = statValue(BLUE);
        serviceLabel = statValue(GREEN);
        sosLabel = statValue(RED);
        otherLabel = statValue(PURPLE);

        VBox total = statCard("Total Reports", "Saved Firebase reports", totalLabel);
        VBox service = statCard("Service Reports", "Service lifecycle reports", serviceLabel);
        VBox sos = statCard("SOS Reports", "Emergency activity reports", sosLabel);
        VBox other = statCard("Other Reports", "Customer, mechanic, finance, system", otherLabel);

        for (VBox box : new VBox[]{total, service, sos, other}) {
            HBox.setHgrow(box, Priority.ALWAYS);
        }

        row.getChildren().addAll(total, service, sos, other);
        return row;
    }

    private VBox statCard(String title, String subtitle, Label value) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(16));
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle(
                "-fx-background-color: " + SURFACE + ";" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 11; -fx-background-radius: 11;"
        );

        Label t = new Label(title);
        t.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        Label s = new Label(subtitle);
        s.setStyle("-fx-text-fill: " + MUTED + "; -fx-font-size: 9px;");
        card.getChildren().addAll(t, value, s);
        return card;
    }

    private Label statValue(String color) {
        Label label = new Label("0");
        label.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 25px; -fx-font-weight: bold;");
        return label;
    }

    private HBox createToolbar() {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("Search report name, ID, type or admin...");
        searchField.setPrefWidth(360);
        searchField.setPrefHeight(42);
        searchField.setStyle(inputStyle());

        typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("All Reports", "Service", "SOS", "Customer", "Mechanic", "Financial", "System");
        typeFilter.setValue("All Reports");
        typeFilter.setPrefWidth(145);
        typeFilter.setPrefHeight(42);
        styleCombo(typeFilter);

        sortFilter = new ComboBox<>();
        sortFilter.getItems().addAll("Newest First", "Oldest First", "Report A-Z", "Report Z-A", "Most Records");
        sortFilter.setValue("Newest First");
        sortFilter.setPrefWidth(145);
        sortFilter.setPrefHeight(42);
        styleCombo(sortFilter);

        Button refresh = secondaryButton("Refresh");
        refresh.setPrefHeight(42);
        refresh.setOnAction(e -> refreshPreservingScroll());

        searchField.textProperty().addListener((o, a, b) -> applyFilters());
        typeFilter.valueProperty().addListener((o, a, b) -> applyFilters());
        sortFilter.valueProperty().addListener((o, a, b) -> applyFilters());

        row.getChildren().addAll(searchField, typeFilter, sortFilter, refresh);
        return row;
    }

    private VBox createRecordsCard() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(18));
        card.setStyle(
                "-fx-background-color: " + SURFACE + ";" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 12; -fx-background-radius: 12;"
        );

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);
        VBox titles = new VBox(4);
        Label title = new Label("Generated Report Records");
        title.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 19px; -fx-font-weight: bold;");
        Label sub = new Label("No sample records. Every item below is stored in Firestore reports.");
        sub.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 11px;");
        resultCountLabel = new Label("0 reports");
        resultCountLabel.setStyle("-fx-text-fill: " + BLUE + "; -fx-font-size: 11px; -fx-font-weight: bold;");
        titles.getChildren().addAll(title, sub, resultCountLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label live = new Label("● SYSTEM");
        live.setStyle("-fx-text-fill: " + GREEN + "; -fx-font-size: 10px; -fx-font-weight: bold;");
        top.getChildren().addAll(titles, spacer, live);

        reportCards = new VBox(11);
        reportCards.setPadding(new Insets(2, 2, 10, 2));

        recordsScroll = new ScrollPane(reportCards);
        recordsScroll.setFitToWidth(true);
        recordsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        recordsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        recordsScroll.setPrefHeight(520);
        recordsScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        card.getChildren().addAll(top, recordsScroll);
        return card;
    }

    private VBox createReportCard(Report report) {
        VBox card = new VBox(11);
        card.setPadding(new Insets(16));
        card.setCursor(Cursor.HAND);
        String normal = "-fx-background-color: " + CARD + "; -fx-border-color: " + BORDER + "; -fx-border-radius: 10; -fx-background-radius: 10;";
        String hover = "-fx-background-color: #1A1A1A; -fx-border-color: " + BLUE + "; -fx-border-radius: 10; -fx-background-radius: 10;";
        card.setStyle(normal);

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);
        VBox info = new VBox(3);
        Label name = new Label(nonBlank(report.getName(), "Unnamed Report"));
        name.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 15px; -fx-font-weight: bold;");
        Label id = new Label("Report ID: " + nonBlank(report.getReportId(), "-"));
        id.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 10px; -fx-font-weight: bold;");
        info.getChildren().addAll(name, id);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label type = badge(nonBlank(report.getType(), "Report"), typeColor(report.getType()));
        top.getChildren().addAll(info, spacer, type);

        HBox details = new HBox(10);
        VBox generatedBy = infoBox("GENERATED BY", nonBlank(report.getGeneratedBy(), "Admin"));
        VBox date = infoBox("DATE", nonBlank(report.getGeneratedDate(), "-"));
        VBox records = infoBox("SNAPSHOT RECORDS", String.valueOf(report.getRecords()));
        VBox status = infoBox("STATUS", nonBlank(report.getStatus(), "Generated"));
        for (VBox box : new VBox[]{generatedBy, date, records, status}) {
            HBox.setHgrow(box, Priority.ALWAYS);
        }
        details.getChildren().addAll(generatedBy, date, records, status);

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button view = smallButton("View Details", BLUE);
        Button delete = smallButton("Delete", RED);
        view.setOnAction(e -> {
            e.consume();
            savedScroll = recordsScroll == null ? 0.0 : recordsScroll.getVvalue();
            showDetails(report);
        });
        delete.setOnAction(e -> {
            e.consume();
            deleteReport(report);
        });
        actions.getChildren().addAll(view, delete);

        card.getChildren().addAll(top, details, actions);
        card.setOnMouseEntered(e -> card.setStyle(hover));
        card.setOnMouseExited(e -> card.setStyle(normal));
        card.setOnMouseClicked(e -> {
            savedScroll = recordsScroll == null ? 0.0 : recordsScroll.getVvalue();
            showDetails(report);
        });
        return card;
    }

    private VBox infoBox(String title, String value) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(9));
        box.setMaxWidth(Double.MAX_VALUE);
        box.setStyle("-fx-background-color: #1A1A1A; -fx-border-color: " + BORDER + "; -fx-border-radius: 7; -fx-background-radius: 7;");
        Label t = new Label(title);
        t.setStyle("-fx-text-fill: " + MUTED + "; -fx-font-size: 8px; -fx-font-weight: bold;");
        Label v = new Label(nonBlank(value, "-"));
        v.setWrapText(true);
        v.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 11px; -fx-font-weight: bold;");
        box.getChildren().addAll(t, v);
        return box;
    }

    private void loadReports() {
        allReports.clear();
        if (controller != null) {
            allReports.addAll(controller.getAllReports());
        }
        updateStatistics();
        applyFilters();
    }

    private void refreshPreservingScroll() {
        double old = recordsScroll == null ? 0.0 : recordsScroll.getVvalue();
        loadReports();
        Platform.runLater(() -> {
            if (recordsScroll != null) recordsScroll.setVvalue(old);
        });
    }

    private void updateStatistics() {
        int service = 0;
        int sos = 0;
        for (Report report : allReports) {
            if ("Service".equalsIgnoreCase(report.getType())) service++;
            if ("SOS".equalsIgnoreCase(report.getType())) sos++;
        }
        int other = Math.max(0, allReports.size() - service - sos);
        if (totalLabel != null) totalLabel.setText(String.valueOf(allReports.size()));
        if (serviceLabel != null) serviceLabel.setText(String.valueOf(service));
        if (sosLabel != null) sosLabel.setText(String.valueOf(sos));
        if (otherLabel != null) otherLabel.setText(String.valueOf(other));
    }

    private void applyFilters() {
        if (reportCards == null) return;

        String search = searchField == null ? "" : nonBlank(searchField.getText(), "").toLowerCase(Locale.ENGLISH);
        String type = typeFilter == null ? "All Reports" : typeFilter.getValue();
        String sort = sortFilter == null ? "Newest First" : sortFilter.getValue();

        List<Report> filtered = new ArrayList<>();
        for (Report report : allReports) {
            if (report == null) continue;
            if (!"All Reports".equalsIgnoreCase(type) && !type.equalsIgnoreCase(report.getType())) continue;

            String haystack = (
                    nonBlank(report.getReportId(), "") + " " +
                    nonBlank(report.getName(), "") + " " +
                    nonBlank(report.getType(), "") + " " +
                    nonBlank(report.getGeneratedBy(), "")
            ).toLowerCase(Locale.ENGLISH);
            if (!search.isBlank() && !haystack.contains(search)) continue;
            filtered.add(report);
        }

        if ("Oldest First".equals(sort)) {
            filtered.sort(Comparator.comparingLong(Report::getGeneratedAt));
        } else if ("Report A-Z".equals(sort)) {
            filtered.sort(Comparator.comparing(r -> nonBlank(r.getName(), "").toLowerCase(Locale.ENGLISH)));
        } else if ("Report Z-A".equals(sort)) {
            filtered.sort(Comparator.comparing((Report r) -> nonBlank(r.getName(), "").toLowerCase(Locale.ENGLISH)).reversed());
        } else if ("Most Records".equals(sort)) {
            filtered.sort(Comparator.comparingInt(Report::getRecords).reversed());
        } else {
            filtered.sort(Comparator.comparingLong(Report::getGeneratedAt).reversed());
        }

        render(filtered);
    }

    private void render(List<Report> reports) {
        reportCards.getChildren().clear();
        if (reports == null || reports.isEmpty()) {
            VBox empty = new VBox(6);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(40));
            Label t = new Label("No reports found");
            t.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 15px; -fx-font-weight: bold;");
            Label s = new Label("Generate a report to create the first Firebase-backed report record.");
            s.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 11px;");
            empty.getChildren().addAll(t, s);
            reportCards.getChildren().add(empty);
            resultCountLabel.setText("0 reports");
            return;
        }

        for (Report report : reports) {
            reportCards.getChildren().add(createReportCard(report));
        }
        resultCountLabel.setText(reports.size() == 1 ? "1 report" : reports.size() + " reports");
    }

    private void showGeneratePage() {
        GenerateReportPage page = new GenerateReportPage(
                controller,
                () -> showManagementPage(true),
                report -> {
                    if (report != null) {
                        showDetails(report);
                    } else {
                        showManagementPage(true);
                    }
                }
        );
        root.getChildren().setAll(page.getView());
    }

    private void showDetails(Report report) {
        Report fresh = report;
        if (controller != null && report != null && report.getReportId() != null) {
            Report loaded = controller.getReportById(report.getReportId());
            if (loaded != null) fresh = loaded;
        }
        final Report selected = fresh;
        ReportDetailsPage page = new ReportDetailsPage(
                selected,
                () -> showManagementPage(true),
                () -> showPreview(selected)
        );
        root.getChildren().setAll(page.getView());
    }

    private void showPreview(Report report) {
        ReportPreviewPage page = new ReportPreviewPage(report, () -> showDetails(report));
        root.getChildren().setAll(page.getView());
    }

    private void deleteReport(Report report) {
        if (report == null || report.getReportId() == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("RoadGuardian");
        confirm.setHeaderText("Delete Report?");
        confirm.setContentText(nonBlank(report.getName(), "Report") + "\n" + report.getReportId());

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        if (controller.deleteReport(report.getReportId())) {
            refreshPreservingScroll();
        } else {
            Alert error = new Alert(Alert.AlertType.ERROR, "Unable to delete this report.", ButtonType.OK);
            error.setHeaderText("Delete Failed");
            error.showAndWait();
        }
    }

    private VBox createFatalError() {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(50));
        box.setStyle("-fx-background-color: " + BG + ";");
        Label title = new Label("Unable to Load Reports");
        title.setStyle("-fx-text-fill: " + RED + "; -fx-font-size: 18px; -fx-font-weight: bold;");
        Label text = new Label("Firebase connection is not available.");
        text.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 12px;");
        box.getChildren().addAll(title, text);
        return box;
    }

    private Label badge(String text, String color) {
        Label label = new Label(text);
        label.setStyle(
                "-fx-text-fill: " + color + ";" +
                "-fx-background-color: " + color + "18;" +
                "-fx-border-color: " + color + "55;" +
                "-fx-border-radius: 14; -fx-background-radius: 14;" +
                "-fx-padding: 5 9 5 9; -fx-font-size: 9px; -fx-font-weight: bold;"
        );
        return label;
    }

    private String typeColor(String type) {
        if ("Service".equalsIgnoreCase(type)) return GREEN;
        if ("SOS".equalsIgnoreCase(type)) return RED;
        if ("Financial".equalsIgnoreCase(type)) return ORANGE;
        if ("Mechanic".equalsIgnoreCase(type)) return PURPLE;
        return BLUE;
    }

    private Button primaryButton(String text) {
        Button button = new Button(text);
        button.setCursor(Cursor.HAND);
        button.setStyle("-fx-background-color: " + BLUE + "; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 10 16 10 16; -fx-font-size: 11px; -fx-font-weight: bold;");
        return button;
    }

    private Button secondaryButton(String text) {
        Button button = new Button(text);
        button.setCursor(Cursor.HAND);
        button.setStyle("-fx-background-color: " + CARD + "; -fx-text-fill: " + HEADING + "; -fx-border-color: " + BORDER + "; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 9 14 9 14; -fx-font-size: 11px; -fx-font-weight: bold;");
        return button;
    }

    private Button smallButton(String text, String color) {
        Button button = new Button(text);
        button.setCursor(Cursor.HAND);
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 7; -fx-padding: 7 11 7 11; -fx-font-size: 9px; -fx-font-weight: bold;");
        return button;
    }

    private String inputStyle() {
        return "-fx-background-color: " + CARD + "; -fx-text-fill: " + HEADING + "; -fx-prompt-text-fill: " + MUTED + "; -fx-border-color: " + BORDER + "; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 0 11 0 11; -fx-font-size: 11px;";
    }

    private void styleCombo(ComboBox<String> combo) {
        combo.setStyle("-fx-background-color: " + CARD + "; -fx-border-color: " + BORDER + "; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 11px;");
        combo.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 11px;");
            }
        });
    }

    private String nonBlank(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }
}
