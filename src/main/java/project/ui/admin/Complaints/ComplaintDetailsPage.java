package project.ui.admin.Complaints;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import project.model.Complaint;
import project.ui.admin.DashBoard.AdminSectionPage;

public class ComplaintDetailsPage extends AdminSectionPage {

    private static final String BG = "#0F0F0F";
    private static final String SURFACE = "#1A1A1A";
    private static final String CARD = "#1A1A1A";
    private static final String BORDER = "#F59E0B";
    private static final String HEADING = "#F3F4F6";
    private static final String TEXT = "#A1A1AA";
    private static final String BLUE = "#F59E0B";
    private static final String ORANGE = "#F59E0B";
    private static final String GREEN = "#22C55E";
    private static final String RED = "#EF4444";
    private static final String PURPLE = "#F59E0B";

    private final Complaint complaint;
    private final Runnable onBack;
    private final Runnable onUpdateStatus;

    public ComplaintDetailsPage(
            Complaint complaint,
            Runnable onBack,
            Runnable onUpdateStatus
    ) {
        this.complaint = complaint;
        this.onBack = onBack == null ? () -> {} : onBack;
        this.onUpdateStatus = onUpdateStatus == null ? () -> {} : onUpdateStatus;
    }

    @Override
    public VBox getView() {
        VBox root = new VBox(18);
        root.setPadding(new Insets(28, 32, 32, 32));
        root.setStyle("-fx-background-color: " + BG + ";");

        if (complaint == null) {
            Label error = new Label("Complaint is not available.");
            error.setStyle("-fx-text-fill: " + RED + "; -fx-font-size: 15px; -fx-font-weight: bold;");
            root.getChildren().add(error);
            return root;
        }

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);
        Button back = outlineButton("← Back to Complaints");
        back.setOnAction(e -> onBack.run());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label status = badge(normalizeStatus(complaint.getStatus()), statusColor(complaint.getStatus()));
        top.getChildren().addAll(back, spacer, status);

        VBox header = new VBox(4);
        Label title = new Label(firstNonBlank(complaint.getSubject(), "Complaint Details"));
        title.setWrapText(true);
        title.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 28px; -fx-font-weight: bold;");
        Label subtitle = new Label("Complaint ID: " + firstNonBlank(complaint.getComplaintId(), "-"));
        subtitle.setStyle("-fx-text-fill: " + BLUE + "; -fx-font-size: 11px; -fx-font-weight: bold;");
        header.getChildren().addAll(title, subtitle);

        VBox detailsCard = createDetailsCard();
        root.getChildren().addAll(top, header, detailsCard);
        return root;
    }

    private VBox createDetailsCard() {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle(
                "-fx-background-color: " + SURFACE + ";" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 12;" +
                "-fx-background-radius: 12;"
        );

        Label heading = new Label("Complaint Information");
        heading.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 18px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(infoBox("CUSTOMER", firstNonBlank(complaint.getCustomerName(), "Customer")), 0, 0);
        grid.add(infoBox("CUSTOMER EMAIL / ID", firstNonBlank(complaint.getCustomerEmail(), complaint.getCustomerId(), "-")), 1, 0);
        grid.add(infoBox("CATEGORY", firstNonBlank(complaint.getCategory(), "Other")), 0, 1);
        grid.add(infoBox("PRIORITY", firstNonBlank(complaint.getPriority(), "Medium")), 1, 1);
        grid.add(infoBox("RELATED REQUEST", firstNonBlank(complaint.getRelatedRequestId(), "Not linked")), 0, 2);
        grid.add(infoBox("CURRENT STATUS", normalizeStatus(complaint.getStatus())), 1, 2);
        grid.add(infoBox("SUBMITTED", formatTime(complaint.getCreatedAt())), 0, 3);
        grid.add(infoBox("LAST UPDATED", formatTime(complaint.getUpdatedAt())), 1, 3);

        javafx.scene.layout.ColumnConstraints c1 = new javafx.scene.layout.ColumnConstraints();
        c1.setPercentWidth(50);
        c1.setHgrow(Priority.ALWAYS);
        javafx.scene.layout.ColumnConstraints c2 = new javafx.scene.layout.ColumnConstraints();
        c2.setPercentWidth(50);
        c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);

        VBox descriptionBox = textBox("DESCRIPTION", firstNonBlank(complaint.getDescription(), "No description provided."));
        VBox noteBox = textBox("ADMIN NOTE", firstNonBlank(complaint.getAdminNote(), "No admin note yet."));

        HBox actions = new HBox();
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button update = primaryButton("Update Status");
        update.setDisable(complaint.isFinalStatus());
        update.setOnAction(e -> onUpdateStatus.run());
        actions.getChildren().add(update);

        card.getChildren().addAll(heading, new Separator(), grid, descriptionBox, noteBox, actions);
        return card;
    }

    private VBox infoBox(String title, String value) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(11));
        box.setMaxWidth(Double.MAX_VALUE);
        box.setStyle("-fx-background-color: " + CARD + "; -fx-border-color: " + BORDER + "; -fx-border-radius: 8; -fx-background-radius: 8;");

        Label t = new Label(title);
        t.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 8px; -fx-font-weight: bold;");
        Label v = new Label(value);
        v.setWrapText(true);
        v.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 11px; -fx-font-weight: bold;");
        box.getChildren().addAll(t, v);
        return box;
    }

    private VBox textBox(String title, String value) {
        VBox box = new VBox(5);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color: " + CARD + "; -fx-border-color: " + BORDER + "; -fx-border-radius: 8; -fx-background-radius: 8;");
        Label t = new Label(title);
        t.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 8px; -fx-font-weight: bold;");
        Label v = new Label(value);
        v.setWrapText(true);
        v.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 11px;");
        box.getChildren().addAll(t, v);
        return box;
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

    private Label badge(String text, String color) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: " + color + "; -fx-background-color: " + color + "18; -fx-border-color: " + color + "55; -fx-border-radius: 14; -fx-background-radius: 14; -fx-padding: 6 10 6 10; -fx-font-size: 9px; -fx-font-weight: bold;");
        return label;
    }

    private String statusColor(String status) {
        status = normalizeStatus(status);
        if ("Pending".equalsIgnoreCase(status)) return ORANGE;
        if ("In Progress".equalsIgnoreCase(status)) return PURPLE;
        if ("Resolved".equalsIgnoreCase(status)) return GREEN;
        if ("Rejected".equalsIgnoreCase(status)) return RED;
        return BLUE;
    }

    private String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) return "Pending";
        if (status.equalsIgnoreCase("InProgress") || status.equalsIgnoreCase("Processing")) return "In Progress";
        if (status.equalsIgnoreCase("Closed") || status.equalsIgnoreCase("Completed")) return "Resolved";
        return status.trim();
    }

    private String formatTime(long value) {
        if (value <= 0) return "-";
        return java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")
                .withZone(java.time.ZoneId.systemDefault())
                .format(java.time.Instant.ofEpochMilli(value));
    }

    private String firstNonBlank(String... values) {
        if (values == null) return null;
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) return value.trim();
        }
        return null;
    }
}
