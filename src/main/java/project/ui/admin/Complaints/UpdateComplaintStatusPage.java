package project.ui.admin.Complaints;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import project.controller.admin.ComplaintController;
import project.model.Complaint;
import project.ui.admin.DashBoard.AdminSectionPage;

public class UpdateComplaintStatusPage extends AdminSectionPage {

    private static final String BG = "#0F0F0F";
    private static final String SURFACE = "#1A1A1A";
    private static final String CARD = "#1A1A1A";
    private static final String BORDER = "#F59E0B";
    private static final String HEADING = "#F3F4F6";
    private static final String TEXT = "#A1A1AA";
    private static final String BLUE = "#F59E0B";
    private static final String RED = "#EF4444";

    private final Complaint complaint;
    private final Runnable onBack;
    private final Runnable onUpdated;
    private final ComplaintController controller = new ComplaintController();

    private ComboBox<String> statusBox;
    private TextArea adminNoteArea;
    private Button updateButton;

    public UpdateComplaintStatusPage(
            Complaint complaint,
            Runnable onBack,
            Runnable onUpdated
    ) {
        this.complaint = complaint;
        this.onBack = onBack == null ? () -> {} : onBack;
        this.onUpdated = onUpdated == null ? () -> {} : onUpdated;
    }

    @Override
    public VBox getView() {
        VBox root = new VBox(18);
        root.setPadding(new Insets(28, 32, 32, 32));
        root.setStyle("-fx-background-color: " + BG + ";");

        Button back = outlineButton("← Back to Complaint Details");
        back.setOnAction(e -> onBack.run());

        Label title = new Label("Update Complaint Status");
        title.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 28px; -fx-font-weight: bold;");
        Label subtitle = new Label("Update the same Firebase complaint record. Resolved and Rejected are final states.");
        subtitle.setWrapText(true);
        subtitle.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 12px;");

        VBox header = new VBox(4, title, subtitle);
        VBox form = createForm();

        root.getChildren().addAll(back, header, form);
        return root;
    }

    private VBox createForm() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(20));
        card.setMaxWidth(820);
        card.setStyle("-fx-background-color: " + SURFACE + "; -fx-border-color: " + BORDER + "; -fx-border-radius: 12; -fx-background-radius: 12;");

        Label heading = new Label("Complaint Action");
        heading.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 18px; -fx-font-weight: bold;");

        VBox summary = new VBox(5);
        summary.setPadding(new Insets(12));
        summary.setStyle("-fx-background-color: " + CARD + "; -fx-border-color: " + BORDER + "; -fx-border-radius: 8; -fx-background-radius: 8;");

        Label id = dark("Complaint ID: " + firstNonBlank(complaint == null ? null : complaint.getComplaintId(), "-"), true);
        Label subject = dark("Subject: " + firstNonBlank(complaint == null ? null : complaint.getSubject(), "Complaint"), false);
        Label current = dark("Current Status: " + normalizeStatus(complaint == null ? null : complaint.getStatus()), false);
        summary.getChildren().addAll(id, subject, current);

        Label statusLabel = dark("New Status", true);
        statusBox = new ComboBox<>();
        statusBox.setMaxWidth(Double.MAX_VALUE);
        statusBox.setPrefHeight(42);
        statusBox.setStyle("-fx-background-color: #1A1A1A; -fx-border-color: " + BORDER + "; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 11px;");
        configureStatuses();

        Label noteLabel = dark("Admin Note", true);
        adminNoteArea = new TextArea(firstNonBlank(complaint == null ? null : complaint.getAdminNote(), ""));
        adminNoteArea.setPromptText("Optional for In Progress/Resolved. Required when rejecting a complaint.");
        adminNoteArea.setWrapText(true);
        adminNoteArea.setPrefRowCount(4);
        adminNoteArea.setStyle("-fx-control-inner-background: white; -fx-text-fill: " + HEADING + "; -fx-prompt-text-fill: #A1A1AA; -fx-border-color: " + BORDER + "; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 11px;");

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button cancel = outlineButton("Cancel");
        cancel.setOnAction(e -> onBack.run());
        updateButton = primaryButton("Update Status");
        updateButton.setOnAction(e -> updateStatus());
        updateButton.setDisable(complaint == null || complaint.isFinalStatus());
        actions.getChildren().addAll(cancel, updateButton);

        card.getChildren().addAll(
                heading,
                new Separator(),
                summary,
                statusLabel,
                statusBox,
                noteLabel,
                adminNoteArea,
                actions
        );
        return card;
    }

    private void configureStatuses() {
        statusBox.getItems().clear();
        if (complaint == null) {
            statusBox.setDisable(true);
            return;
        }

        String current = normalizeStatus(complaint.getStatus());
        if ("Resolved".equalsIgnoreCase(current) || "Rejected".equalsIgnoreCase(current)) {
            statusBox.getItems().add(current);
            statusBox.setValue(current);
            statusBox.setDisable(true);
            return;
        }

        if ("Pending".equalsIgnoreCase(current)) {
            statusBox.getItems().addAll("In Progress", "Resolved", "Rejected");
        } else if ("In Progress".equalsIgnoreCase(current)) {
            statusBox.getItems().addAll("Resolved", "Rejected");
        } else {
            statusBox.getItems().addAll("In Progress", "Resolved", "Rejected");
        }

        if (!statusBox.getItems().isEmpty()) {
            statusBox.setValue(statusBox.getItems().get(0));
        }
    }

    private void updateStatus() {
        if (complaint == null) {
            show(Alert.AlertType.ERROR, "Complaint Missing", "Complaint data is not available.");
            return;
        }

        String newStatus = statusBox.getValue();
        String note = clean(adminNoteArea.getText());

        if (newStatus == null) {
            show(Alert.AlertType.WARNING, "Status Required", "Select a new complaint status.");
            return;
        }

        if ("Rejected".equalsIgnoreCase(newStatus) && note == null) {
            show(Alert.AlertType.WARNING, "Admin Note Required", "Please enter a reason before rejecting this complaint.");
            return;
        }

        updateButton.setDisable(true);
        boolean success = controller.updateStatus(
                complaint.getComplaintId(),
                newStatus,
                note
        );

        if (!success) {
            updateButton.setDisable(false);
            show(Alert.AlertType.ERROR, "Update Failed", "Complaint status could not be updated. The status may already be final.");
            return;
        }

        show(Alert.AlertType.INFORMATION, "Complaint Updated", "The complaint status was updated in Firebase.");
        onUpdated.run();
    }

    private Label dark(String text, boolean bold) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 11px;" + (bold ? " -fx-font-weight: bold;" : ""));
        return label;
    }

    private Button primaryButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + BLUE + "; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 9 15 9 15; -fx-cursor: hand;");
        return button;
    }

    private Button outlineButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: #1A1A1A; -fx-text-fill: " + BLUE + "; -fx-border-color: " + BLUE + "; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 8 13 8 13; -fx-cursor: hand;");
        return button;
    }

    private String normalizeStatus(String status) {
        status = clean(status);
        if (status == null) return "Pending";
        if (status.equalsIgnoreCase("InProgress") || status.equalsIgnoreCase("Processing")) return "In Progress";
        if (status.equalsIgnoreCase("Closed") || status.equalsIgnoreCase("Completed")) return "Resolved";
        return status;
    }

    private String firstNonBlank(String... values) {
        if (values == null) return null;
        for (String value : values) {
            String cleaned = clean(value);
            if (cleaned != null) return cleaned;
        }
        return null;
    }

    private String clean(String value) {
        if (value == null) return null;
        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private void show(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("RoadGuardian");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
