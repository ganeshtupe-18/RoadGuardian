package project.ui.user;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import project.controller.user.ComplaintController;
import project.dao.user.ServiceRequestDAO;
import project.model.Complaint;
import project.model.ServiceRequest;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ComplaintsPage {

    private static final String BG      = "#0F0F0F";
    private static final String CARD    = "#1A1A1A";
    private static final String BORDER  = "#F59E0B";
    private static final String HEADING = "#F3F4F6";
    private static final String TEXT    = "#A1A1AA";
    private static final String BLUE    = "#F59E0B";
    private static final String GREEN   = "#22C55E";
    private static final String ORANGE  = "#F59E0B";
    private static final String RED     = "#EF4444";
    private static final String PURPLE  = "#A78BFA";

    private final ComplaintController controller = new ComplaintController();

    private TextField subjectField;
    private ComboBox<String> categoryBox;
    private ComboBox<String> priorityBox;
    private ComboBox<String> relatedRequestBox;
    private TextArea descriptionArea;
    private VBox recordsBox;
    private Label countLabel;
    private Label pendingValue;
    private Label progressValue;
    private Label resolvedValue;
    private ScrollPane recordsScroll;

    public Scene getComplaintsScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG + ";");

        try {
            root.setTop(UserHeader.createHeader());
        } catch (Exception ignored) {
        }

        root.setLeft(UserSideBar.createSidebar("Complaints"));

        VBox content = createContent();
        ScrollPane pageScroll = new ScrollPane(content);
        pageScroll.setFitToWidth(true);
        pageScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        pageScroll.setStyle(
                "-fx-background-color: " + BG + ";" +
                "-fx-background: " + BG + ";" +
                "-fx-border-color: transparent;"
        );
        root.setCenter(pageScroll);

        double width = 1200;
        double height = 700;
        if (UserDashboard.dashboardStage != null) {
            width = Math.max(1000, UserDashboard.dashboardStage.getWidth());
            height = Math.max(650, UserDashboard.dashboardStage.getHeight());
        }

        Scene scene = new Scene(root, width, height);
        loadRelatedRequests();
        loadComplaints(false);
        return scene;
    }

    private VBox createContent() {
        VBox content = new VBox(22);
        content.setPadding(new Insets(30, 35, 40, 35));
        content.setStyle("-fx-background-color: " + BG + ";");

        content.getChildren().addAll(
                createHeader(),
                createStats(),
                createSubmitCard(),
                createRecordsCard()
        );
        return content;
    }

    private HBox createHeader() {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox text = new VBox(4);
        Label title = new Label("Complaints");
        title.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 28px; -fx-font-weight: bold;");

        Label subtitle = new Label("Submit a complaint and track the real status updated by RoadGuardian Admin.");
        subtitle.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 13px;");
        text.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refresh = button("Refresh", BLUE);
        refresh.setOnAction(e -> loadComplaints(true));

        row.getChildren().addAll(text, spacer, refresh);
        return row;
    }

    private GridPane createStats() {
        GridPane grid = new GridPane();
        grid.setHgap(12);

        pendingValue = statValue(ORANGE);
        progressValue = statValue(PURPLE);
        resolvedValue = statValue(GREEN);

        grid.add(statCard("Pending", pendingValue), 0, 0);
        grid.add(statCard("In Progress", progressValue), 1, 0);
        grid.add(statCard("Resolved", resolvedValue), 2, 0);

        for (int i = 0; i < 3; i++) {
            javafx.scene.layout.ColumnConstraints c = new javafx.scene.layout.ColumnConstraints();
            c.setPercentWidth(33.33);
            c.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(c);
        }
        return grid;
    }

    private VBox statCard(String title, Label value) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(15));
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle(cardStyle());

        Label t = new Label(title);
        t.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        card.getChildren().addAll(t, value);
        return card;
    }

    private Label statValue(String color) {
        Label label = new Label("0");
        label.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 24px; -fx-font-weight: bold;");
        return label;
    }

    private VBox createSubmitCard() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(20));
        card.setStyle(cardStyle());

        Label title = new Label("Submit New Complaint");
        title.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 18px; -fx-font-weight: bold;");

        subjectField = new TextField();
        subjectField.setPromptText("Short complaint subject");
        styleInput(subjectField);

        categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll(
                "Roadside Assistance",
                "Mechanic Behaviour",
                "Service Delay",
                "Billing / Charge",
                "Tow Truck",
                "Emergency / SOS",
                "App / Technical",
                "Other"
        );
        categoryBox.setValue("Roadside Assistance");
        styleCombo(categoryBox);

        priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll("Low", "Medium", "High");
        priorityBox.setValue("Medium");
        styleCombo(priorityBox);

        relatedRequestBox = new ComboBox<>();
        relatedRequestBox.getItems().add("None");
        relatedRequestBox.setValue("None");
        styleCombo(relatedRequestBox);

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.add(fieldBox("Subject", subjectField), 0, 0, 2, 1);
        form.add(fieldBox("Category", categoryBox), 0, 1);
        form.add(fieldBox("Priority", priorityBox), 1, 1);
        form.add(fieldBox("Related Service Request (Optional)", relatedRequestBox), 0, 2, 2, 1);

        javafx.scene.layout.ColumnConstraints c1 = new javafx.scene.layout.ColumnConstraints();
        c1.setPercentWidth(50);
        c1.setHgrow(Priority.ALWAYS);
        javafx.scene.layout.ColumnConstraints c2 = new javafx.scene.layout.ColumnConstraints();
        c2.setPercentWidth(50);
        c2.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(c1, c2);

        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Explain the issue clearly...");
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefRowCount(4);
        descriptionArea.setStyle(
                "-fx-control-inner-background: #242424;" +
                "-fx-text-fill: " + HEADING + ";" +
                "-fx-prompt-text-fill: #9A9AA3;" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-font-size: 12px;"
        );

        VBox descriptionBox = new VBox(6);
        Label dl = new Label("Description");
        dl.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 11px; -fx-font-weight: bold;");
        descriptionBox.getChildren().addAll(dl, descriptionArea);

        HBox actions = new HBox();
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button submit = button("Submit Complaint", BLUE);
        submit.setOnAction(e -> submitComplaint());
        actions.getChildren().add(submit);

        card.getChildren().addAll(title, new Separator(), form, descriptionBox, actions);
        return card;
    }

    private VBox createRecordsCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle(cardStyle());

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("My Complaint History");
        title.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 18px; -fx-font-weight: bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        countLabel = new Label("0 complaints");
        countLabel.setStyle("-fx-text-fill: " + BLUE + "; -fx-font-size: 11px; -fx-font-weight: bold;");
        header.getChildren().addAll(title, spacer, countLabel);

        recordsBox = new VBox(10);
        recordsScroll = new ScrollPane(recordsBox);
        recordsScroll.setFitToWidth(true);
        recordsScroll.setPrefHeight(480);
        recordsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        recordsScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");

        card.getChildren().addAll(header, new Separator(), recordsScroll);
        return card;
    }

    private void submitComplaint() {
        String subject = clean(subjectField.getText());
        String description = clean(descriptionArea.getText());

        if (subject == null || description == null) {
            show(Alert.AlertType.WARNING, "Missing Information", "Subject and description are required.");
            return;
        }

        String related = relatedRequestBox.getValue();
        if ("None".equalsIgnoreCase(related)) {
            related = null;
        }

        boolean success = controller.submitComplaint(
                subject,
                categoryBox.getValue(),
                description,
                priorityBox.getValue(),
                related
        );

        if (!success) {
            show(Alert.AlertType.ERROR, "Complaint Not Submitted", "Unable to save complaint in Firebase.");
            return;
        }

        subjectField.clear();
        descriptionArea.clear();
        categoryBox.setValue("Roadside Assistance");
        priorityBox.setValue("Medium");
        relatedRequestBox.setValue("None");

        show(Alert.AlertType.INFORMATION, "Complaint Submitted", "Your complaint was saved and is now visible to Admin.");
        loadComplaints(false);
    }

    private void loadComplaints(boolean preserveScroll) {
        double old = recordsScroll == null ? 0.0 : recordsScroll.getVvalue();
        List<Complaint> complaints = controller.getMyComplaints();

        if (recordsBox == null) {
            return;
        }

        recordsBox.getChildren().clear();

        int pending = 0;
        int progress = 0;
        int resolved = 0;

        for (Complaint complaint : complaints) {
            String status = normalizeStatus(complaint.getStatus());
            if ("Pending".equalsIgnoreCase(status)) pending++;
            if ("In Progress".equalsIgnoreCase(status)) progress++;
            if ("Resolved".equalsIgnoreCase(status)) resolved++;
            recordsBox.getChildren().add(createComplaintCard(complaint));
        }

        if (complaints.isEmpty()) {
            Label empty = new Label("No complaints submitted yet.");
            empty.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 12px;");
            VBox emptyBox = new VBox(empty);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(35));
            recordsBox.getChildren().add(emptyBox);
        }

        pendingValue.setText(String.valueOf(pending));
        progressValue.setText(String.valueOf(progress));
        resolvedValue.setText(String.valueOf(resolved));
        countLabel.setText(complaints.size() == 1 ? "1 complaint" : complaints.size() + " complaints");

        if (preserveScroll && recordsScroll != null) {
            Platform.runLater(() -> recordsScroll.setVvalue(old));
        }
    }

    private VBox createComplaintCard(Complaint complaint) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle(
                "-fx-background-color: #F8FBFC;" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;"
        );

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);

        VBox identity = new VBox(3);
        Label subject = new Label(firstNonBlank(complaint.getSubject(), "Complaint"));
        subject.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label id = new Label("ID: " + firstNonBlank(complaint.getComplaintId(), "-"));
        id.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 10px;");
        identity.getChildren().addAll(subject, id);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label status = badge(normalizeStatus(complaint.getStatus()), statusColor(complaint.getStatus()));
        top.getChildren().addAll(identity, spacer, status);

        HBox info = new HBox(8);
        VBox category = infoBox("CATEGORY", firstNonBlank(complaint.getCategory(), "Other"));
        VBox priority = infoBox("PRIORITY", firstNonBlank(complaint.getPriority(), "Medium"));
        VBox request = infoBox("REQUEST", firstNonBlank(complaint.getRelatedRequestId(), "Not linked"));
        VBox date = infoBox("SUBMITTED", formatTime(complaint.getCreatedAt()));
        for (VBox box : new VBox[]{category, priority, request, date}) {
            HBox.setHgrow(box, Priority.ALWAYS);
        }
        info.getChildren().addAll(category, priority, request, date);

        Label description = new Label(firstNonBlank(complaint.getDescription(), "No description provided."));
        description.setWrapText(true);
        description.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 11px;");

        card.getChildren().addAll(top, info, description);

        if (clean(complaint.getAdminNote()) != null) {
            Label note = new Label("Admin note: " + complaint.getAdminNote());
            note.setWrapText(true);
            note.setStyle(
                    "-fx-text-fill: " + HEADING + ";" +
                    "-fx-background-color: #242424;" +
                    "-fx-padding: 9;" +
                    "-fx-background-radius: 7;" +
                    "-fx-font-size: 11px;"
            );
            card.getChildren().add(note);
        }

        return card;
    }

    private VBox fieldBox(String title, javafx.scene.Node control) {
        VBox box = new VBox(6);
        Label label = new Label(title);
        label.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 11px; -fx-font-weight: bold;");
        box.getChildren().addAll(label, control);
        return box;
    }

    private VBox infoBox(String title, String value) {
        VBox box = new VBox(3);
        box.setPadding(new Insets(8));
        box.setMinWidth(120);
        box.setStyle("-fx-background-color: " + CARD + "; -fx-border-color: " + BORDER + "; -fx-border-radius: 7; -fx-background-radius: 7;");

        Label t = new Label(title);
        t.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 8px; -fx-font-weight: bold;");
        Label v = new Label(value);
        v.setWrapText(true);
        v.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 10px; -fx-font-weight: bold;");
        box.getChildren().addAll(t, v);
        return box;
    }

    private Label badge(String text, String color) {
        Label label = new Label(text);
        label.setStyle(
                "-fx-text-fill: " + color + ";" +
                "-fx-background-color: " + color + "18;" +
                "-fx-border-color: " + color + "55;" +
                "-fx-border-radius: 14;" +
                "-fx-background-radius: 14;" +
                "-fx-padding: 5 10 5 10;" +
                "-fx-font-size: 9px;" +
                "-fx-font-weight: bold;"
        );
        return label;
    }

    private void loadRelatedRequests() {
        try {
            ServiceRequestDAO dao = new ServiceRequestDAO();
            String customerId = firstNonBlank(UserSession.getUserEmail(), UserSession.getUserId());
            List<ServiceRequest> requests = dao.getRequestsByCustomerId(customerId);
            List<String> ids = new ArrayList<>();
            for (ServiceRequest request : requests) {
                if (request != null && clean(request.getRequestId()) != null) {
                    ids.add(request.getRequestId());
                }
            }
            if (relatedRequestBox != null) {
                relatedRequestBox.getItems().setAll("None");
                relatedRequestBox.getItems().addAll(ids);
                relatedRequestBox.setValue("None");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Button button(String text, String color) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 11px;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 9 16 9 16;" +
                "-fx-cursor: hand;"
        );
        return button;
    }

    private void styleInput(TextField field) {
        field.setPrefHeight(42);
        field.setStyle(
                "-fx-background-color: " + CARD + ";" +
                "-fx-text-fill: " + HEADING + ";" +
                "-fx-prompt-text-fill: #9A9AA3;" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-font-size: 12px;"
        );
    }

    private void styleCombo(ComboBox<String> combo) {
        combo.setPrefHeight(42);
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.setStyle(
                "-fx-background-color: " + CARD + ";" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-font-size: 11px;"
        );
    }

    private String cardStyle() {
        return "-fx-background-color: " + CARD + ";" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 12;" +
                "-fx-background-radius: 12;";
    }

    private String normalizeStatus(String status) {
        status = clean(status);
        if (status == null) return "Pending";
        if (status.equalsIgnoreCase("InProgress") || status.equalsIgnoreCase("Processing")) return "In Progress";
        if (status.equalsIgnoreCase("Closed") || status.equalsIgnoreCase("Completed")) return "Resolved";
        return status;
    }

    private String statusColor(String status) {
        status = normalizeStatus(status);
        if ("Pending".equalsIgnoreCase(status)) return ORANGE;
        if ("In Progress".equalsIgnoreCase(status)) return PURPLE;
        if ("Resolved".equalsIgnoreCase(status)) return GREEN;
        if ("Rejected".equalsIgnoreCase(status)) return RED;
        return BLUE;
    }

    private String formatTime(long value) {
        if (value <= 0) return "-";
        return DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")
                .withZone(ZoneId.systemDefault())
                .format(Instant.ofEpochMilli(value));
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
