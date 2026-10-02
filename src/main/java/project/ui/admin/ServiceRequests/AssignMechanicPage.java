package project.ui.admin.ServiceRequests;

import com.google.cloud.firestore.Firestore;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import project.controller.admin.MechanicController;
import project.controller.admin.ServiceRequestController;
import project.firebase.FirebaseConfig;
import project.model.Mechanic;
import project.model.ServiceRequest;
import project.util.IconUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AssignMechanicPage {

    private static final String BG = "#0F0F0F";
    private static final String SURFACE = "#1A1A1A";
    private static final String WHITE = "#1A1A1A";
    private static final String HEADING = "#F3F4F6";
    private static final String TEXT = "#A1A1AA";
    private static final String BORDER = "#F59E0B";
    private static final String BLUE = "#F59E0B";
    private static final String GREEN = "#22C55E";
    private static final String RED = "#EF4444";

    private final ServiceRequest request;
    private final Runnable backAction;

    private ServiceRequestController requestController;
    private MechanicController mechanicController;

    private ComboBox<Mechanic> mechanicComboBox;
    private Label selectedMechanicLabel;
    private Button assignButton;

    public AssignMechanicPage(ServiceRequest request, Runnable backAction) {
        this.request = request;
        this.backAction = backAction == null ? () -> {} : backAction;
    }

    public AssignMechanicPage(ServiceRequest request) {
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
            mechanicController = new MechanicController(firestore);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorView("Unable to connect to Firebase.");
        }

        ServiceRequest fresh = requestController.getRequestById(request.getRequestId());
        if (fresh != null) copyState(fresh, request);

        String status = normalizeStatus(request.getStatus());
        if (!status.equals("Pending") && !status.equals("Assigned")) {
            return createErrorView("Mechanic assignment is allowed only for Pending or Assigned requests. Current status: " + status);
        }

        root.getChildren().addAll(createHeader(), createRequestCard(), createMechanicCard(), createActions());
        loadMechanics();
        return root;
    }

    private VBox createHeader() {
        VBox box = new VBox(5);
        Label title = new Label(request.hasMechanic() ? "Reassign Mechanic" : "Assign Mechanic");
        title.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 25px; -fx-font-weight: bold;");
        Label subtitle = new Label("Choose an active mechanic. Saving this action keeps status = Assigned until that mechanic accepts it.");
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
                detailRow("Current Status", normalizeStatus(request.getStatus())),
                detailRow("Current Mechanic", request.hasMechanic() ? firstNonBlank(request.getMechanicName(), request.getMechanicId(), "Assigned") : "Not Assigned")
        );
        return card;
    }

    private VBox createMechanicCard() {
        VBox card = createCard();
        Label instruction = new Label("Only active mechanics are shown.");
        instruction.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 11px;");

        mechanicComboBox = new ComboBox<>();
        mechanicComboBox.setPromptText("Select active mechanic");
        mechanicComboBox.setPrefHeight(44);
        mechanicComboBox.setMaxWidth(Double.MAX_VALUE);
        mechanicComboBox.setStyle("-fx-background-color: " + WHITE + "; -fx-border-color: " + BORDER + ";"
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 12px;");

        mechanicComboBox.setCellFactory(list -> new ListCell<>() {
            @Override protected void updateItem(Mechanic item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(displayMechanic(item));
                    // UI-only: use a wrench icon for mechanic representations.
                    setGraphic(IconUtil.createMechanicIcon(0.50, GREEN));
                    setGraphicTextGap(8);
                    setStyle("-fx-text-fill: " + HEADING + "; -fx-background-color: #1A1A1A; -fx-font-size: 11px;");
                }
            }
        });

        mechanicComboBox.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Mechanic item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Select active mechanic");
                    setGraphic(null);
                } else {
                    setText(displayMechanic(item));
                    // UI-only: use a wrench icon for the selected mechanic.
                    setGraphic(IconUtil.createMechanicIcon(0.50, GREEN));
                    setGraphicTextGap(8);
                }
                setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 11px;");
            }
        });

        selectedMechanicLabel = new Label("No mechanic selected");
        selectedMechanicLabel.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 11px; -fx-font-weight: bold;");

        mechanicComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) {
                selectedMechanicLabel.setText("No mechanic selected");
                selectedMechanicLabel.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 11px; -fx-font-weight: bold;");
            } else {
                selectedMechanicLabel.setText("Selected: " + displayMechanic(newValue));
                selectedMechanicLabel.setStyle("-fx-text-fill: " + GREEN + "; -fx-font-size: 11px; -fx-font-weight: bold;");
            }
        });

        card.getChildren().addAll(heading("Select Mechanic"), instruction, mechanicComboBox, selectedMechanicLabel);
        return card;
    }

    private HBox createActions() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_RIGHT);
        Button back = button("Back", WHITE, HEADING, BORDER);
        back.setOnAction(e -> backAction.run());
        assignButton = button(request.hasMechanic() ? "Reassign Mechanic" : "Assign Mechanic", BLUE, "#FFFFFF", BLUE);
        assignButton.setOnAction(e -> assignSelectedMechanic());
        box.getChildren().addAll(back, assignButton);
        return box;
    }

    private void loadMechanics() {
        List<Mechanic> active = new ArrayList<>();
        try {
            List<Mechanic> mechanics = mechanicController.getAllMechanics();
            if (mechanics != null) {
                for (Mechanic mechanic : mechanics) {
                    if (mechanic == null) continue;
                    String status = clean(mechanic.getStatus());
                    if (status == null || status.equalsIgnoreCase("Active")) active.add(mechanic);
                }
            }
            mechanicComboBox.getItems().setAll(active);
            if (active.isEmpty()) {
                selectedMechanicLabel.setText("No active mechanics available.");
                selectedMechanicLabel.setStyle("-fx-text-fill: " + RED + "; -fx-font-size: 11px; -fx-font-weight: bold;");
                assignButton.setDisable(true);
                return;
            }

            if (request.hasMechanic()) {
                for (Mechanic mechanic : active) {
                    if (sameId(resolveMechanicId(mechanic), request.getMechanicId())) {
                        mechanicComboBox.setValue(mechanic);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Unable to Load Mechanics", e.getMessage());
        }
    }

    private void assignSelectedMechanic() {
        Mechanic mechanic = mechanicComboBox.getValue();
        if (mechanic == null) {
            showError("No Mechanic Selected", "Please select an active mechanic first.");
            return;
        }

        String mechanicId = resolveMechanicId(mechanic);
        if (mechanicId == null) {
            showError("Invalid Mechanic", "Selected mechanic does not have a usable mechanic ID/email.");
            return;
        }

        ServiceRequest fresh = requestController.getRequestById(request.getRequestId());
        if (fresh == null) {
            showError("Request Not Found", "This service request no longer exists in Firebase.");
            return;
        }
        String status = normalizeStatus(fresh.getStatus());
        if (!status.equals("Pending") && !status.equals("Assigned")) {
            showError("Assignment Blocked", "Request status changed to " + status + ". A mechanic can no longer be assigned/reassigned.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("RoadGuardian");
        confirmation.setHeaderText(request.hasMechanic() ? "Reassign Mechanic?" : "Assign Mechanic?");
        confirmation.setContentText("Request: " + safe(request.getRequestId())
                + "\nMechanic: " + safe(mechanic.getName())
                + "\nMechanic ID: " + mechanicId
                + "\n\nStatus will be Assigned until the mechanic accepts the request.");
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        assignButton.setDisable(true);
        boolean success = requestController.assignMechanic(request.getRequestId(), mechanicId, firstNonBlank(mechanic.getName(), mechanic.getEmail(), mechanicId));
        if (!success) {
            assignButton.setDisable(false);
            showError("Assignment Failed", "The request may have changed status. Refresh and try again.");
            return;
        }

        request.setMechanicId(mechanicId);
        request.setMechanicName(firstNonBlank(mechanic.getName(), mechanic.getEmail(), mechanicId));
        request.setStatus("Assigned");
        showInfo("Mechanic Assigned", "The mechanic was assigned successfully. The request is now visible to that mechanic as Assigned.");
        backAction.run();
    }

    private String resolveMechanicId(Mechanic mechanic) {
        if (mechanic == null) return null;

        /*
         * IMPORTANT FIX:
         * Assignment must store mechanic email when available.
         * Mechanic login session uses email, so random Firestore
         * document IDs must not be used for active assignment.
         */
        return normalizeId(firstNonBlank(mechanic.getEmail(), mechanic.getMechanicId()));
    }

    private String displayMechanic(Mechanic mechanic) {
        if (mechanic == null) return "Mechanic";
        String name = firstNonBlank(mechanic.getName(), mechanic.getEmail(), "Mechanic");
        String specialization = firstNonBlank(mechanic.getSpecialization(), "Automobile Service");
        return name + " • " + specialization;
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

        // UI-only: use shared vector icons for customer, vehicle and mechanic representations.
        if ("CUSTOMER".equalsIgnoreCase(title)) {
            v.setGraphic(IconUtil.createUserIcon(0.55, BLUE));
            v.setGraphicTextGap(7);
        } else if ("VEHICLE".equalsIgnoreCase(title)) {
            v.setGraphic(IconUtil.createVehicleIcon(0.55, BLUE));
            v.setGraphicTextGap(7);
        } else if ("CURRENT MECHANIC".equalsIgnoreCase(title)) {
            v.setGraphic(IconUtil.createMechanicIcon(0.55, GREEN));
            v.setGraphicTextGap(7);
        }

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
        Label title = new Label("Assign Mechanic");
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
        target.setUpdatedAt(source.getUpdatedAt());
    }

    private String normalizeStatus(String status) {
        status = clean(status);
        if (status == null) return "Pending";
        if (status.equalsIgnoreCase("Assigned")) return "Assigned";
        if (status.equalsIgnoreCase("Accepted")) return "Accepted";
        if (status.equalsIgnoreCase("In Progress") || status.equalsIgnoreCase("InProgress") || status.equalsIgnoreCase("Active")) return "In Progress";
        if (status.equalsIgnoreCase("Completed") || status.equalsIgnoreCase("Done")) return "Completed";
        if (status.equalsIgnoreCase("Cancelled") || status.equalsIgnoreCase("Canceled")) return "Cancelled";
        return "Pending";
    }

    private boolean sameId(String first, String second) {
        first = normalizeId(first);
        second = normalizeId(second);
        return first != null && second != null && first.equals(second);
    }

    private String normalizeId(String value) {
        value = clean(value);
        if (value == null) return null;
        return value.contains("@") ? value.toLowerCase() : value;
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