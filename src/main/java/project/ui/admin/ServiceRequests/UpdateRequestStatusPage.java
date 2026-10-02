package project.ui.admin.ServiceRequests;

import com.google.cloud.firestore.Firestore;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import project.controller.admin.ServiceRequestController;
import project.firebase.FirebaseConfig;
import project.model.ServiceRequest;

import java.util.Optional;

public class UpdateRequestStatusPage {

    private static final String BG = "#0F0F0F";
    private static final String SURFACE = "#1A1A1A";
    private static final String WHITE = "#1A1A1A";
    private static final String HEADING = "#F3F4F6";
    private static final String TEXT = "#A1A1AA";
    private static final String BORDER = "#F59E0B";
    private static final String BLUE = "#F59E0B";
    private static final String GREEN = "#22C55E";
    private static final String ORANGE = "#F59E0B";
    private static final String RED = "#EF4444";
    private static final String PURPLE = "#F59E0B";
    private static final String CYAN = "#F59E0B";

    private final ServiceRequest request;
    private final Runnable backAction;
    private ServiceRequestController requestController;
    private Button cancelButton;

    public UpdateRequestStatusPage(ServiceRequest request, Runnable backAction) {
        this.request = request;
        this.backAction = backAction == null ? () -> {} : backAction;
    }

    public UpdateRequestStatusPage(ServiceRequest request) {
        this(request, () -> {});
    }

    public VBox getView() {
        VBox root = new VBox(18);
        root.setPadding(new Insets(24, 26, 28, 26));
        root.setPrefWidth(620);
        root.setStyle("-fx-background-color: " + BG + ";");

        if (request == null) return createErrorView("Service request is not available.");

        try {
            Firestore firestore = FirebaseConfig.getFirestore();
            requestController = new ServiceRequestController(firestore);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorView("Unable to connect to Firebase.");
        }

        ServiceRequest fresh = requestController.getRequestById(request.getRequestId());
        if (fresh != null) copyState(fresh, request);

        root.getChildren().addAll(createHeader(), createRequestCard(), createLifecycleCard(), createAdminActionCard(), createActions());
        return root;
    }

    private VBox createHeader() {
        VBox box = new VBox(5);
        Label title = new Label("Admin Request Control");
        title.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 25px; -fx-font-weight: bold;");
        Label subtitle = new Label("Mechanic lifecycle statuses are read-only here. Admin can cancel an active request when necessary.");
        subtitle.setWrapText(true);
        subtitle.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 12px;");
        box.getChildren().addAll(title, subtitle);
        return box;
    }

    private VBox createRequestCard() {
        VBox card = createCard();
        card.getChildren().addAll(
                heading("Request Information"),
                new Separator(),
                detailRow("Request ID", request.getRequestId()),
                detailRow("Customer", firstNonBlank(request.getCustomerName(), request.getCustomerId(), "Unknown Customer")),
                detailRow("Vehicle", firstNonBlank(request.getVehicleNumber(), request.getVehicleId(), "Not provided")),
                detailRow("Service", firstNonBlank(request.getServiceType(), "Service Request")),
                detailRow("Mechanic", request.hasMechanic() ? firstNonBlank(request.getMechanicName(), request.getMechanicId(), "Assigned") : "Not Assigned")
        );
        return card;
    }

    private VBox createLifecycleCard() {
        VBox card = createCard();
        String current = normalizeStatus(request.getStatus());

        HBox currentRow = new HBox(12);
        currentRow.setAlignment(Pos.CENTER_LEFT);
        Label label = new Label("Current Status");
        label.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 11px; -fx-font-weight: bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        currentRow.getChildren().addAll(label, spacer, statusBadge(current));

        Label next = new Label(nextAction(current));
        next.setWrapText(true);
        next.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 11px; -fx-font-weight: bold;"
                + "-fx-background-color: " + WHITE + "; -fx-border-color: " + BORDER + ";"
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10;");

        HBox flow = new HBox(7);
        flow.setAlignment(Pos.CENTER_LEFT);
        String[] statuses = {"Pending", "Assigned", "Accepted", "In Progress", "Completed"};
        for (int i = 0; i < statuses.length; i++) {
            flow.getChildren().add(flowStep(statuses[i], current));
            if (i < statuses.length - 1) {
                Label arrow = new Label("→");
                arrow.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-weight: bold;");
                flow.getChildren().add(arrow);
            }
        }
        if (current.equals("Cancelled")) flow.getChildren().addAll(new Label("   "), statusBadge("Cancelled"));

        card.getChildren().addAll(heading("Lifecycle"), new Separator(), currentRow, flow, next);
        return card;
    }

    private VBox createAdminActionCard() {
        VBox card = createCard();
        String current = normalizeStatus(request.getStatus());

        Label rule = new Label(adminRule(current));
        rule.setWrapText(true);
        rule.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 11px;"
                + "-fx-background-color: " + WHITE + "; -fx-border-color: " + BORDER + ";"
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 11;");

        Label warning = new Label("Admin cannot manually set Accepted, In Progress or Completed. Those transitions are performed by the assigned mechanic.");
        warning.setWrapText(true);
        warning.setStyle("-fx-text-fill: " + ORANGE + "; -fx-font-size: 10px; -fx-font-weight: bold;");

        card.getChildren().addAll(heading("Admin Permission"), new Separator(), rule, warning);
        return card;
    }

    private HBox createActions() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_RIGHT);

        Button back = button("Back", WHITE, HEADING, BORDER);
        back.setOnAction(e -> backAction.run());

        String status = normalizeStatus(request.getStatus());
        cancelButton = button("Cancel Request", RED, "#FFFFFF", RED);
        cancelButton.setDisable(status.equals("Completed") || status.equals("Cancelled"));
        cancelButton.setOnAction(e -> cancelRequest());

        box.getChildren().addAll(back, cancelButton);
        return box;
    }

    private void cancelRequest() {
        ServiceRequest fresh = requestController.getRequestById(request.getRequestId());
        if (fresh == null) {
            showError("Request Not Found", "This request no longer exists in Firebase.");
            return;
        }

        String current = normalizeStatus(fresh.getStatus());
        if (current.equals("Completed")) {
            showError("Already Completed", "Completed requests cannot be cancelled.");
            return;
        }
        if (current.equals("Cancelled")) {
            showInfo("Already Cancelled", "This request is already cancelled.");
            backAction.run();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("RoadGuardian");
        confirm.setHeaderText("Cancel Service Request?");
        confirm.setContentText("Request: " + safe(fresh.getRequestId())
                + "\nCustomer: " + firstNonBlank(fresh.getCustomerName(), fresh.getCustomerId(), "Customer")
                + "\nCurrent Status: " + current
                + "\n\nCustomer, Admin and Mechanic will all see status = Cancelled.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        cancelButton.setDisable(true);
        boolean success = requestController.cancelRequest(fresh.getRequestId());
        if (!success) {
            cancelButton.setDisable(false);
            showError("Cancellation Failed", "The request may have changed. Refresh and try again.");
            return;
        }

        request.setStatus("Cancelled");
        showInfo("Request Cancelled", "The same serviceRequests record is now Cancelled.");
        backAction.run();
    }

    private VBox flowStep(String status, String current) {
        boolean active = current.equalsIgnoreCase(status);
        String color = getStatusColor(status);
        VBox box = new VBox();
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(8));
        box.setMinWidth(status.equals("In Progress") ? 90 : 75);
        box.setStyle("-fx-background-color: " + (active ? color + "20" : WHITE) + ";"
                + "-fx-border-color: " + (active ? color : BORDER) + "; -fx-border-width: " + (active ? "2" : "1") + ";"
                + "-fx-border-radius: 8; -fx-background-radius: 8;");
        Label label = new Label(status);
        label.setStyle("-fx-text-fill: " + (active ? color : HEADING) + "; -fx-font-size: 9px; -fx-font-weight: bold;");
        box.getChildren().add(label);
        return box;
    }

    private Label statusBadge(String status) {
        String normalized = normalizeStatus(status);
        String color = getStatusColor(normalized);
        Label badge = new Label(normalized);
        badge.setStyle("-fx-text-fill: " + color + "; -fx-background-color: " + color + "18;"
                + "-fx-border-color: " + color + "55; -fx-border-radius: 14; -fx-background-radius: 14;"
                + "-fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 6 11 6 11;");
        return badge;
    }

    private String nextAction(String status) {
        switch (normalizeStatus(status)) {
            case "Pending": return "Next: Admin assigns a mechanic → Assigned.";
            case "Assigned": return "Next: Assigned mechanic accepts → Accepted.";
            case "Accepted": return "Next: Mechanic starts repair → In Progress.";
            case "In Progress": return "Next: Mechanic completes repair → Completed.";
            case "Completed": return "Lifecycle complete.";
            case "Cancelled": return "Lifecycle stopped because the request was cancelled.";
            default: return "Current status is being monitored.";
        }
    }

    private String adminRule(String status) {
        switch (normalizeStatus(status)) {
            case "Pending": return "Use Assign Mechanic on the request details page. Admin may cancel if the request should not continue.";
            case "Assigned": return "Wait for the selected mechanic to accept. Admin may reassign before acceptance or cancel the request.";
            case "Accepted": return "The mechanic accepted the request. Only the mechanic can start repair. Admin may cancel if necessary.";
            case "In Progress": return "Repair is active. Only the mechanic can complete the job. Admin may cancel only if necessary.";
            case "Completed": return "Final state. No admin status change is allowed.";
            case "Cancelled": return "Final state. No further status change is allowed.";
            default: return "Follow the RoadGuardian service lifecycle.";
        }
    }

    private VBox createCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(17));
        card.setStyle("-fx-background-color: " + SURFACE + "; -fx-border-color: " + BORDER + "; -fx-border-radius: 12; -fx-background-radius: 12;");
        return card;
    }

    private Label heading(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 17px; -fx-font-weight: bold;");
        return label;
    }

    private HBox detailRow(String title, String value) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        Label t = new Label(title);
        t.setMinWidth(110);
        t.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 11px; -fx-font-weight: bold;");
        Label v = new Label(safe(value));
        v.setWrapText(true);
        v.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        HBox.setHgrow(v, Priority.ALWAYS);
        row.getChildren().addAll(t, v);
        return row;
    }

    private Button button(String text, String bg, String fg, String border) {
        Button button = new Button(text);
        button.setCursor(Cursor.HAND);
        button.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + "; -fx-border-color: " + border + ";"
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 9 16 9 16;");
        return button;
    }

    private VBox createErrorView(String message) {
        VBox box = new VBox(12);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(35));
        box.setStyle("-fx-background-color: " + BG + ";");
        Label title = new Label("Request Control Error");
        title.setStyle("-fx-text-fill: " + RED + "; -fx-font-size: 18px; -fx-font-weight: bold;");
        Label text = new Label(message);
        text.setWrapText(true);
        text.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 12px;");
        Button back = button("Back", WHITE, HEADING, BORDER);
        back.setOnAction(e -> backAction.run());
        box.getChildren().addAll(title, text, back);
        return box;
    }

    private void copyState(ServiceRequest source, ServiceRequest target) {
        target.setStatus(source.getStatus());
        target.setMechanicId(source.getMechanicId());
        target.setMechanicName(source.getMechanicName());
        target.setAssignedDate(source.getAssignedDate());
        target.setAcceptedDate(source.getAcceptedDate());
        target.setStartedDate(source.getStartedDate());
        target.setCompletedDate(source.getCompletedDate());
        target.setCancelledDate(source.getCancelledDate());
        target.setUpdatedAt(source.getUpdatedAt());
    }

    private String normalizeStatus(String status) {
        status = clean(status);
        if (status == null) return "Pending";
        if (status.equalsIgnoreCase("Assigned")) return "Assigned";
        if (status.equalsIgnoreCase("Accepted")) return "Accepted";
        if (status.equalsIgnoreCase("In Progress") || status.equalsIgnoreCase("InProgress") || status.equalsIgnoreCase("Active") || status.equalsIgnoreCase("Started")) return "In Progress";
        if (status.equalsIgnoreCase("Completed") || status.equalsIgnoreCase("Complete") || status.equalsIgnoreCase("Done")) return "Completed";
        if (status.equalsIgnoreCase("Cancelled") || status.equalsIgnoreCase("Canceled")) return "Cancelled";
        return "Pending";
    }

    private String getStatusColor(String status) {
        switch (normalizeStatus(status)) {
            case "Pending": return ORANGE;
            case "Assigned": return BLUE;
            case "Accepted": return PURPLE;
            case "In Progress": return CYAN;
            case "Completed": return GREEN;
            case "Cancelled": return RED;
            default: return TEXT;
        }
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("RoadGuardian");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("RoadGuardian");
        alert.setHeaderText(title);
        alert.setContentText(firstNonBlank(message, "Unknown error."));
        alert.showAndWait();
    }

    private String firstNonBlank(String... values) {
        if (values == null) return null;
        for (String value : values) {
            String cleaned = clean(value);
            if (cleaned != null) return cleaned;
        }
        return null;
    }

    private String safe(String value) {
        String cleaned = clean(value);
        return cleaned == null ? "-" : cleaned;
    }

    private String clean(String value) {
        if (value == null) return null;
        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }
}