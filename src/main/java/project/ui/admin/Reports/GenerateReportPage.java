package project.ui.admin.Reports;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import project.controller.admin.ReportController;
import project.model.Report;
import project.ui.admin.DashBoard.AdminSectionPage;

import java.util.function.Consumer;

public class GenerateReportPage extends AdminSectionPage {

    private static final String BG = "#0F0F0F";
    private static final String SURFACE = "#1A1A1A";
    private static final String BORDER = "#F59E0B";
    private static final String HEADING = "#F3F4F6";
    private static final String TEXT = "#A1A1AA";
    private static final String MUTED = "#A1A1AA";
    private static final String BLUE = "#F59E0B";
    private static final String RED = "#EF4444";

    private final ReportController controller;
    private final Runnable onBack;
    private final Consumer<Report> onGenerated;

    private TextField nameField;
    private ComboBox<String> typeBox;
    private Label errorLabel;
    private Button generateButton;

    public GenerateReportPage(
            ReportController controller,
            Runnable onBack,
            Consumer<Report> onGenerated
    ) {
        this.controller = controller;
        this.onBack = onBack;
        this.onGenerated = onGenerated;
    }

    @Override
    public VBox getView() {
        VBox root = new VBox(18);
        root.setPadding(new Insets(28, 32, 32, 32));
        root.setStyle("-fx-background-color: " + BG + ";");

        Button back = outlineButton("← Back to Reports");
        back.setOnAction(e -> {
            if (onBack != null) onBack.run();
        });

        Label title = new Label("Generate Report");
        title.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 29px; -fx-font-weight: bold;");
        Label subtitle = new Label("Take a live Firebase snapshot and save it in the reports collection.");
        subtitle.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 13px;");

        VBox form = createForm();
        VBox.setVgrow(form, Priority.ALWAYS);
        root.getChildren().addAll(back, title, subtitle, form);
        return root;
    }

    private VBox createForm() {
        VBox card = new VBox(15);
        card.setPadding(new Insets(22));
        card.setMaxWidth(760);
        card.setStyle("-fx-background-color: " + SURFACE + "; -fx-border-color: " + BORDER + "; -fx-border-radius: 12; -fx-background-radius: 12;");

        Label heading = new Label("Report Information");
        heading.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 19px; -fx-font-weight: bold;");

        Label nameLabel = fieldLabel("Report Name");
        nameField = new TextField();
        nameField.setPromptText("Example: September Service Summary");
        nameField.setPrefHeight(43);
        nameField.setStyle(inputStyle());

        Label typeLabel = fieldLabel("Report Type");
        typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Service", "SOS", "Customer", "Mechanic", "Financial", "System");
        typeBox.setValue("Service");
        typeBox.setPrefHeight(43);
        typeBox.setMaxWidth(Double.MAX_VALUE);
        typeBox.setStyle("-fx-background-color: #1A1A1A; -fx-border-color: " + BORDER + "; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 11px;");
        typeBox.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 11px;");
            }
        });

        Label note = new Label(
                "Service → serviceRequests\n" +
                "SOS → SOSRequests\n" +
                "Customer → users/customers + vehicles + complaints + reviews\n" +
                "Mechanic → mechanics + service jobs + reviews\n" +
                "Financial → completed service cost values\n" +
                "System → complete operational snapshot"
        );
        note.setWrapText(true);
        note.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 11px; -fx-background-color: #1A1A1A; -fx-border-color: " + BORDER + "; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 11;");

        errorLabel = new Label();
        errorLabel.setWrapText(true);
        errorLabel.setStyle("-fx-text-fill: " + RED + "; -fx-font-size: 11px; -fx-font-weight: bold;");

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button cancel = outlineButton("Cancel");
        cancel.setOnAction(e -> {
            if (onBack != null) onBack.run();
        });

        generateButton = primaryButton("Generate Firebase Report");
        generateButton.setOnAction(e -> generate());
        actions.getChildren().addAll(cancel, generateButton);

        card.getChildren().addAll(
                heading,
                nameLabel,
                nameField,
                typeLabel,
                typeBox,
                note,
                errorLabel,
                actions
        );
        return card;
    }

    private void generate() {
        String name = clean(nameField == null ? null : nameField.getText());
        String type = typeBox == null ? null : typeBox.getValue();

        if (name == null) {
            errorLabel.setText("Please enter a report name.");
            return;
        }
        if (controller == null) {
            errorLabel.setText("Report controller is not available.");
            return;
        }

        generateButton.setDisable(true);
        generateButton.setText("Generating...");
        errorLabel.setText("");

        Report report = controller.generateReport(name, type);

        generateButton.setDisable(false);
        generateButton.setText("Generate Firebase Report");

        if (report == null) {
            errorLabel.setText("Report generation failed. Check Firebase connection and console logs.");
            return;
        }

        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setTitle("RoadGuardian");
        success.setHeaderText("Report Generated");
        success.setContentText(
                "Report ID: " + report.getReportId() +
                "\nType: " + report.getType() +
                "\nSnapshot Records: " + report.getRecords()
        );
        success.showAndWait();

        if (onGenerated != null) {
            onGenerated.accept(report);
        } else if (onBack != null) {
            onBack.run();
        }
    }

    private Label fieldLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 11px; -fx-font-weight: bold;");
        return label;
    }

    private String inputStyle() {
        return "-fx-background-color: #1A1A1A; -fx-text-fill: " + HEADING + "; -fx-prompt-text-fill: " + MUTED + "; -fx-border-color: " + BORDER + "; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 0 11 0 11; -fx-font-size: 11px;";
    }

    private Button primaryButton(String text) {
        Button button = new Button(text);
        button.setCursor(Cursor.HAND);
        button.setStyle("-fx-background-color: " + BLUE + "; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 10 15 10 15; -fx-font-size: 11px; -fx-font-weight: bold;");
        return button;
    }

    private Button outlineButton(String text) {
        Button button = new Button(text);
        button.setCursor(Cursor.HAND);
        button.setStyle("-fx-background-color: #1A1A1A; -fx-text-fill: " + BLUE + "; -fx-border-color: " + BLUE + "; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 9 14 9 14; -fx-font-size: 10px; -fx-font-weight: bold;");
        return button;
    }

    private String clean(String value) {
        if (value == null) return null;
        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }
}
