package project.ui.admin.Complaints;

import com.google.cloud.firestore.Firestore;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import project.controller.admin.ComplaintController;
import project.firebase.FirebaseConfig;
import project.model.Complaint;
import project.ui.admin.DashBoard.AdminSectionPage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ComplaintManagementPage extends AdminSectionPage {

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

    private ComplaintController controller;
    private final List<Complaint> allComplaints = new ArrayList<>();

    private VBox root;
    private VBox managementView;
    private VBox recordsBox;
    private ScrollPane recordsScroll;

    private TextField searchField;
    private ComboBox<String> statusFilter;
    private ComboBox<String> priorityFilter;
    private ComboBox<String> sortFilter;

    private Label countLabel;
    private Label totalValue;
    private Label pendingValue;
    private Label progressValue;
    private Label resolvedValue;
    private Label highValue;

    private double savedScroll = 0.0;

    @Override
    public VBox getView() {
        if (root == null) {
            root = new VBox();
            root.setStyle("-fx-background-color: " + BG + ";");
        }

        try {
            Firestore firestore = FirebaseConfig.getFirestore();
            controller = new ComplaintController(firestore);
        } catch (Exception e) {
            e.printStackTrace();
            root.getChildren().setAll(createErrorView("Unable to connect to Firebase."));
            return root;
        }

        managementView = createManagementView();
        root.getChildren().setAll(managementView);
        VBox.setVgrow(managementView, Priority.ALWAYS);
        loadComplaints(false);
        return root;
    }

    private VBox createManagementView() {
        VBox view = new VBox(20);
        view.setPadding(new Insets(30, 32, 35, 32));
        view.setStyle("-fx-background-color: " + BG + ";");
        view.getChildren().addAll(
                createHeader(),
                createStats(),
                createToolbar(),
                createRecordsCard()
        );
        return view;
    }

    private HBox createHeader() {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox text = new VBox(4);
        Label title = new Label("Complaints");
        title.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 29px; -fx-font-weight: bold;");
        Label subtitle = new Label("Review real customer complaints stored in Firebase and move them toward resolution.");
        subtitle.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 13px;");
        text.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label live = new Label("● SYSTEM LIVE");
        live.setStyle("-fx-text-fill: " + GREEN + "; -fx-font-size: 10px; -fx-font-weight: bold;");

        row.getChildren().addAll(text, spacer, live);
        return row;
    }

    private GridPane createStats() {
        GridPane grid = new GridPane();
        grid.setHgap(12);

        totalValue = statValue(BLUE);
        pendingValue = statValue(ORANGE);
        progressValue = statValue(PURPLE);
        resolvedValue = statValue(GREEN);
        highValue = statValue(RED);

        grid.add(statCard("Total", totalValue, "All customer complaints"), 0, 0);
        grid.add(statCard("Pending", pendingValue, "Waiting for Admin action"), 1, 0);
        grid.add(statCard("In Progress", progressValue, "Currently being handled"), 2, 0);
        grid.add(statCard("Resolved", resolvedValue, "Closed successfully"), 3, 0);
        grid.add(statCard("High Priority", highValue, "Active urgent complaints"), 4, 0);

        for (int i = 0; i < 5; i++) {
            ColumnConstraints c = new ColumnConstraints();
            c.setPercentWidth(20);
            c.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(c);
        }
        return grid;
    }

    private VBox statCard(String title, Label value, String subtitle) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(14));
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle(
                "-fx-background-color: " + SURFACE + ";" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;"
        );

        Label t = new Label(title);
        t.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 11px; -fx-font-weight: bold;");
        Label s = new Label(subtitle);
        s.setWrapText(true);
        s.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 8px;");
        card.getChildren().addAll(t, value, s);
        return card;
    }

    private Label statValue(String color) {
        Label label = new Label("0");
        label.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 23px; -fx-font-weight: bold;");
        return label;
    }

    private HBox createToolbar() {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("Search complaint, customer, subject, request...");
        searchField.setPrefWidth(320);
        searchField.setPrefHeight(42);
        styleTextField(searchField);

        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All", "Pending", "In Progress", "Resolved", "Rejected");
        statusFilter.setValue("All");
        statusFilter.setPrefWidth(135);
        styleCombo(statusFilter);

        priorityFilter = new ComboBox<>();
        priorityFilter.getItems().addAll("All Priority", "High", "Medium", "Low");
        priorityFilter.setValue("All Priority");
        priorityFilter.setPrefWidth(130);
        styleCombo(priorityFilter);

        sortFilter = new ComboBox<>();
        sortFilter.getItems().addAll("Newest First", "Oldest First", "High Priority First");
        sortFilter.setValue("Newest First");
        sortFilter.setPrefWidth(155);
        styleCombo(sortFilter);

        Button refresh = button("Refresh", BLUE);
        refresh.setOnAction(e -> loadComplaints(true));

        searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        statusFilter.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        priorityFilter.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        sortFilter.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());

        row.getChildren().addAll(searchField, statusFilter, priorityFilter, sortFilter, refresh);
        return row;
    }

    private VBox createRecordsCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(18));
        card.setStyle(
                "-fx-background-color: " + SURFACE + ";" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 12;" +
                "-fx-background-radius: 12;"
        );

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox titleBox = new VBox(3);
        Label title = new Label("Complaint Records");
        title.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 18px; -fx-font-weight: bold;");
        Label subtitle = new Label("Actual records from complaints collection");
        subtitle.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 10px;");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        countLabel = new Label("0 complaints");
        countLabel.setStyle("-fx-text-fill: " + BLUE + "; -fx-font-size: 10px; -fx-font-weight: bold;");
        header.getChildren().addAll(titleBox, spacer, countLabel);

        recordsBox = new VBox(10);
        recordsScroll = new ScrollPane(recordsBox);
        recordsScroll.setFitToWidth(true);
        recordsScroll.setPrefHeight(550);
        recordsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        recordsScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");

        card.getChildren().addAll(header, new Separator(), recordsScroll);
        VBox.setVgrow(recordsScroll, Priority.ALWAYS);
        return card;
    }

    private void loadComplaints(boolean preserveScroll) {
        double old = recordsScroll == null ? 0.0 : recordsScroll.getVvalue();
        allComplaints.clear();
        allComplaints.addAll(controller.getAllComplaints());
        updateStats();
        applyFilters();

        if (preserveScroll && recordsScroll != null) {
            Platform.runLater(() -> recordsScroll.setVvalue(old));
        }
    }

    private void updateStats() {
        int pending = 0;
        int progress = 0;
        int resolved = 0;
        int high = 0;

        for (Complaint complaint : allComplaints) {
            String status = normalizeStatus(complaint.getStatus());
            if ("Pending".equalsIgnoreCase(status)) pending++;
            if ("In Progress".equalsIgnoreCase(status)) progress++;
            if ("Resolved".equalsIgnoreCase(status)) resolved++;
            if ("High".equalsIgnoreCase(complaint.getPriority()) && !complaint.isFinalStatus()) high++;
        }

        totalValue.setText(String.valueOf(allComplaints.size()));
        pendingValue.setText(String.valueOf(pending));
        progressValue.setText(String.valueOf(progress));
        resolvedValue.setText(String.valueOf(resolved));
        highValue.setText(String.valueOf(high));
    }

    private void applyFilters() {
        if (recordsBox == null) return;

        String query = clean(searchField == null ? null : searchField.getText());
        if (query != null) query = query.toLowerCase();
        String status = statusFilter == null ? "All" : statusFilter.getValue();
        String priority = priorityFilter == null ? "All Priority" : priorityFilter.getValue();
        String sort = sortFilter == null ? "Newest First" : sortFilter.getValue();

        List<Complaint> filtered = new ArrayList<>();
        for (Complaint complaint : allComplaints) {
            if (complaint == null) continue;

            if (query != null && !matchesSearch(complaint, query)) continue;
            if (!"All".equalsIgnoreCase(status)
                    && !normalizeStatus(complaint.getStatus()).equalsIgnoreCase(status)) continue;
            if (!"All Priority".equalsIgnoreCase(priority)
                    && !firstNonBlank(complaint.getPriority(), "Medium").equalsIgnoreCase(priority)) continue;

            filtered.add(complaint);
        }

        if ("Oldest First".equals(sort)) {
            filtered.sort(Comparator.comparingLong(Complaint::getCreatedAt));
        } else if ("High Priority First".equals(sort)) {
            filtered.sort((a, b) -> {
                int p = Integer.compare(priorityValue(b.getPriority()), priorityValue(a.getPriority()));
                if (p != 0) return p;
                return Long.compare(b.getCreatedAt(), a.getCreatedAt());
            });
        } else {
            filtered.sort(Comparator.comparingLong(Complaint::getCreatedAt).reversed());
        }

        render(filtered);
    }

    private void render(List<Complaint> complaints) {
        recordsBox.getChildren().clear();

        if (complaints.isEmpty()) {
            VBox empty = new VBox(6);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(35));
            Label title = new Label("No complaints found");
            title.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 14px; -fx-font-weight: bold;");
            Label text = new Label("Customer complaints will appear here after they are submitted.");
            text.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 11px;");
            empty.getChildren().addAll(title, text);
            recordsBox.getChildren().add(empty);
        } else {
            for (Complaint complaint : complaints) {
                recordsBox.getChildren().add(createComplaintCard(complaint));
            }
        }

        countLabel.setText(complaints.size() == 1 ? "1 complaint" : complaints.size() + " complaints");
    }

    private VBox createComplaintCard(Complaint complaint) {
        VBox card = new VBox(11);
        card.setPadding(new Insets(15));
        card.setStyle(
                "-fx-background-color: " + CARD + ";" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;"
        );

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);
        VBox identity = new VBox(3);
        Label subject = new Label(firstNonBlank(complaint.getSubject(), "Complaint"));
        subject.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 14px; -fx-font-weight: bold;");
        Label customer = new Label("Customer: " + firstNonBlank(complaint.getCustomerName(), complaint.getCustomerEmail(), complaint.getCustomerId(), "Customer"));
        customer.setStyle("-fx-text-fill: #A1A1AA; -fx-font-size: 11px; -fx-font-weight: bold;");
        identity.getChildren().addAll(subject, customer);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label priority = badge(firstNonBlank(complaint.getPriority(), "Medium"), priorityColor(complaint.getPriority()));
        Label status = badge(normalizeStatus(complaint.getStatus()), statusColor(complaint.getStatus()));
        top.getChildren().addAll(identity, spacer, priority, status);

        HBox info = new HBox(8);
        VBox id = infoBox("COMPLAINT ID", firstNonBlank(complaint.getComplaintId(), "-"));
        VBox category = infoBox("CATEGORY", firstNonBlank(complaint.getCategory(), "Other"));
        VBox request = infoBox("RELATED REQUEST", firstNonBlank(complaint.getRelatedRequestId(), "Not linked"));
        VBox date = infoBox("SUBMITTED", formatTime(complaint.getCreatedAt()));
        for (VBox box : new VBox[]{id, category, request, date}) HBox.setHgrow(box, Priority.ALWAYS);
        info.getChildren().addAll(id, category, request, date);

        Label description = new Label(shorten(complaint.getDescription(), 160));
        description.setWrapText(true);
        description.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 11px;");

        HBox actions = new HBox();
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button details = button("View Details →", BLUE);
        details.setOnAction(e -> openDetails(complaint));
        actions.getChildren().add(details);

        card.getChildren().addAll(top, info, description, actions);
        return card;
    }

    private void openDetails(Complaint complaint) {
        savedScroll = recordsScroll == null ? 0.0 : recordsScroll.getVvalue();
        Complaint fresh = controller.getComplaintById(complaint.getComplaintId());
        if (fresh == null) fresh = complaint;
        final Complaint selected = fresh;

        ComplaintDetailsPage page = new ComplaintDetailsPage(
                selected,
                this::showManagement,
                () -> openUpdateStatus(selected)
        );
        root.getChildren().setAll(page.getView());
    }

    private void openUpdateStatus(Complaint complaint) {
        Complaint fresh = controller.getComplaintById(complaint.getComplaintId());
        if (fresh == null) fresh = complaint;
        final Complaint selected = fresh;

        UpdateComplaintStatusPage page = new UpdateComplaintStatusPage(
                selected,
                () -> openDetails(selected),
                () -> {
                    loadComplaints(false);
                    Complaint updated = controller.getComplaintById(selected.getComplaintId());
                    openDetails(updated == null ? selected : updated);
                }
        );
        root.getChildren().setAll(page.getView());
    }

    private void showManagement() {
        root.getChildren().setAll(managementView);
        loadComplaints(false);
        Platform.runLater(() -> {
            if (recordsScroll != null) recordsScroll.setVvalue(savedScroll);
        });
    }

    private boolean matchesSearch(Complaint complaint, String query) {
        return contains(complaint.getComplaintId(), query)
                || contains(complaint.getCustomerName(), query)
                || contains(complaint.getCustomerEmail(), query)
                || contains(complaint.getCustomerId(), query)
                || contains(complaint.getSubject(), query)
                || contains(complaint.getCategory(), query)
                || contains(complaint.getDescription(), query)
                || contains(complaint.getRelatedRequestId(), query)
                || contains(complaint.getStatus(), query);
    }

    private VBox infoBox(String title, String value) {
        VBox box = new VBox(3);
        box.setPadding(new Insets(8));
        box.setMinWidth(120);
        box.setStyle("-fx-background-color: #1A1A1A; -fx-border-color: " + BORDER + "; -fx-border-radius: 7; -fx-background-radius: 7;");
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
                "-fx-padding: 5 9 5 9;" +
                "-fx-font-size: 9px; -fx-font-weight: bold;"
        );
        return label;
    }

    private Button button(String text, String color) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 10px; -fx-font-weight: bold;" +
                "-fx-background-radius: 7; -fx-padding: 8 13 8 13; -fx-cursor: hand;"
        );
        return button;
    }

    private void styleTextField(TextField field) {
        field.setStyle(
                "-fx-background-color: #1A1A1A; -fx-text-fill: " + HEADING + ";" +
                "-fx-prompt-text-fill: #A1A1AA; -fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 11px;"
        );
    }

    private void styleCombo(ComboBox<String> combo) {
        combo.setPrefHeight(42);
        combo.setStyle(
                "-fx-background-color: #1A1A1A; -fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 10px;"
        );
    }

    private VBox createErrorView(String message) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(50));
        box.setStyle("-fx-background-color: " + BG + ";");
        Label title = new Label("Complaint Data Error");
        title.setStyle("-fx-text-fill: " + RED + "; -fx-font-size: 16px; -fx-font-weight: bold;");
        Label text = new Label(message);
        text.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 11px;");
        box.getChildren().addAll(title, text);
        return box;
    }

    private int priorityValue(String priority) {
        if ("High".equalsIgnoreCase(priority)) return 3;
        if ("Medium".equalsIgnoreCase(priority)) return 2;
        return 1;
    }

    private String priorityColor(String priority) {
        if ("High".equalsIgnoreCase(priority)) return RED;
        if ("Low".equalsIgnoreCase(priority)) return GREEN;
        return ORANGE;
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
        status = clean(status);
        if (status == null) return "Pending";
        if (status.equalsIgnoreCase("InProgress") || status.equalsIgnoreCase("Processing")) return "In Progress";
        if (status.equalsIgnoreCase("Closed") || status.equalsIgnoreCase("Completed")) return "Resolved";
        return status;
    }

    private String formatTime(long value) {
        if (value <= 0) return "-";
        return java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")
                .withZone(java.time.ZoneId.systemDefault())
                .format(java.time.Instant.ofEpochMilli(value));
    }

    private String shorten(String value, int max) {
        value = clean(value);
        if (value == null) return "No description provided.";
        return value.length() <= max ? value : value.substring(0, max) + "...";
    }

    private boolean contains(String value, String query) {
        return value != null && query != null && value.toLowerCase().contains(query);
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

    @SuppressWarnings("unused")
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("RoadGuardian");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
