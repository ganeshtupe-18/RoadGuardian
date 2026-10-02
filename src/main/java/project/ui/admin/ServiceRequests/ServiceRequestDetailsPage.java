package project.ui.admin.ServiceRequests;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import project.model.ServiceRequest;
import project.util.IconUtil;

public class ServiceRequestDetailsPage {

    private static final String BG = "#0F0F0F";
    private static final String SURFACE = "#1A1A1A";
    private static final String WHITE = "#1A1A1A";
    private static final String HEADING = "#F3F4F6";
    private static final String TEXT = "#A1A1AA";
    private static final String BORDER = "#F59E0B";
    private static final String BLUE = "#F59E0B";
    private static final String ORANGE = "#F59E0B";
    private static final String GREEN = "#22C55E";
    private static final String RED = "#EF4444";
    private static final String PURPLE = "#F59E0B";
    private static final String CYAN = "#F59E0B";

    private final ServiceRequest request;
    private final Runnable backAction;
    private final Runnable assignAction;
    private final Runnable statusAction;

    public ServiceRequestDetailsPage(
            ServiceRequest request,
            Runnable backAction,
            Runnable assignAction,
            Runnable statusAction
    ) {
        this.request = request;
        this.backAction = backAction == null ? () -> {} : backAction;
        this.assignAction = assignAction == null ? () -> {} : assignAction;
        this.statusAction = statusAction == null ? () -> {} : statusAction;
    }

    public ServiceRequestDetailsPage(ServiceRequest request) {
        this(request, () -> {}, () -> {}, () -> {});
    }

    public VBox getView() {
        VBox root = new VBox(18);
        root.setPadding(new Insets(22, 24, 28, 24));
        root.setStyle("-fx-background-color: " + BG + ";");

        if (request == null) {
            Label error = new Label("Service request is not available.");
            error.setStyle("-fx-text-fill: " + RED + "; -fx-font-size: 16px; -fx-font-weight: bold;");
            root.getChildren().add(error);
            return root;
        }

        root.getChildren().addAll(
                createHeader(),
                createLifecycleCard(),
                createRequestInformation(),
                createMechanicWorkCard(),
                createCostCard(),
                createActions()
        );
        return root;
    }

    private VBox createHeader() {
        VBox box = new VBox(7);

        Label title = new Label("Service Request Details");
        title.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 25px; -fx-font-weight: bold;");

        Label id = new Label("Request ID: " + safe(request.getRequestId()));
        id.setStyle("-fx-text-fill: " + BLUE + "; -fx-font-size: 13px; -fx-font-weight: bold;");

        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        Label customer = new Label(firstNonBlank(request.getCustomerName(), request.getCustomerId(), "Unknown Customer"));
        customer.setStyle("-fx-text-fill: #A1A1AA; -fx-font-size: 13px; -fx-font-weight: bold;");
        // UI-only: show a user silhouette for the customer representation.
        customer.setGraphic(IconUtil.createUserIcon(0.65, TEXT));
        customer.setGraphicTextGap(8);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        row.getChildren().addAll(customer, spacer, createStatusBadge(normalizeStatus(request.getStatus())));

        box.getChildren().addAll(title, id, row);
        return box;
    }

    private VBox createLifecycleCard() {
        VBox card = createCard();
        Label heading = sectionHeading("Lifecycle");
        Label next = new Label(getNextAction());
        next.setWrapText(true);
        next.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 11px; -fx-font-weight: bold;"
                + "-fx-background-color: #1A1A1A; -fx-border-color: " + BORDER + ";"
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10;");

        HBox flow = new HBox(7);
        flow.setAlignment(Pos.CENTER_LEFT);
        String[] statuses = {"Pending", "Assigned", "Accepted", "In Progress", "Completed"};
        for (int i = 0; i < statuses.length; i++) {
            flow.getChildren().add(lifecycleStep(statuses[i]));
            if (i < statuses.length - 1) {
                Label arrow = new Label("→");
                arrow.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 14px; -fx-font-weight: bold;");
                flow.getChildren().add(arrow);
            }
        }
        if (request.isCancelled()) {
            Label cancelled = createStatusBadge("Cancelled");
            flow.getChildren().addAll(new Label("   "), cancelled);
        }

        card.getChildren().addAll(heading, new Separator(), flow, next);
        return card;
    }

    private VBox lifecycleStep(String status) {
        boolean active = normalizeStatus(request.getStatus()).equalsIgnoreCase(status);
        String color = getStatusColor(status);
        VBox box = new VBox();
        box.setAlignment(Pos.CENTER);
        box.setMinWidth(status.equals("In Progress") ? 90 : 75);
        box.setPadding(new Insets(8));
        box.setStyle("-fx-background-color: " + (active ? color + "20" : WHITE) + ";"
                + "-fx-border-color: " + (active ? color : BORDER) + ";"
                + "-fx-border-width: " + (active ? "2" : "1") + ";"
                + "-fx-border-radius: 8; -fx-background-radius: 8;");
        Label label = new Label(status);
        label.setStyle("-fx-text-fill: " + (active ? color : HEADING) + "; -fx-font-size: 9px; -fx-font-weight: bold;");
        box.getChildren().add(label);
        return box;
    }

    private VBox createRequestInformation() {
        VBox card = createCard();
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(infoBox("CUSTOMER", firstNonBlank(request.getCustomerName(), request.getCustomerId(), "Unknown Customer")), 0, 0);
        grid.add(infoBox("CUSTOMER ID", request.getCustomerId()), 1, 0);
        grid.add(infoBox("VEHICLE", firstNonBlank(request.getVehicleNumber(), request.getVehicleId(), "Not provided")), 2, 0);
        grid.add(infoBox("SERVICE", firstNonBlank(request.getServiceType(), "Service Request")), 0, 1);
        grid.add(infoBox("LOCATION", firstNonBlank(request.getLocation(), "Not provided")), 1, 1);
        grid.add(infoBox("MECHANIC", getMechanicDisplay()), 2, 1);
        grid.add(infoBox("REQUESTED", request.getRequestDate()), 0, 2);
        grid.add(infoBox("ASSIGNED", request.getAssignedDate()), 1, 2);
        grid.add(infoBox("UPDATED", request.getUpdatedAt()), 2, 2);

        for (int i = 0; i < 3; i++) {
            javafx.scene.layout.ColumnConstraints c = new javafx.scene.layout.ColumnConstraints();
            c.setPercentWidth(33.33);
            c.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(c);
        }

        Label description = new Label(firstNonBlank(request.getDescription(), "No description provided."));
        description.setWrapText(true);
        description.setStyle("-fx-text-fill: #F3F4F6; -fx-font-size: 12px;"
                + "-fx-background-color: " + WHITE + "; -fx-border-color: " + BORDER + ";"
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 11;");

        card.getChildren().addAll(sectionHeading("Request Information"), new Separator(), grid,
                miniHeading("PROBLEM DESCRIPTION"), description);
        return card;
    }

    private VBox createMechanicWorkCard() {
        VBox card = createCard();
        HBox row = new HBox(10);
        VBox diagnosis = infoBox("DIAGNOSIS", firstNonBlank(request.getDiagnosis(), "Not added yet"));
        VBox repair = infoBox("REPAIR DETAILS", firstNonBlank(request.getRepairDetails(), "Not added yet"));
        HBox.setHgrow(diagnosis, Priority.ALWAYS);
        HBox.setHgrow(repair, Priority.ALWAYS);
        diagnosis.setMaxWidth(Double.MAX_VALUE);
        repair.setMaxWidth(Double.MAX_VALUE);
        row.getChildren().addAll(diagnosis, repair);
        card.getChildren().addAll(sectionHeading("Mechanic Work"), new Separator(), row);
        return card;
    }

    private VBox createCostCard() {
        VBox card = createCard();
        HBox row = new HBox(10);
        VBox estimated = infoBox("ESTIMATED", money(request.getEstimatedCost()));
        VBox parts = infoBox("PARTS", money(request.getPartsCost()));
        VBox labour = infoBox("LABOUR", money(request.getLabourCost()));
        VBox total = infoBox("FINAL", request.isCancelled() ? "₹0" : money(resolveFinalCost()));
        for (VBox box : new VBox[]{estimated, parts, labour, total}) {
            HBox.setHgrow(box, Priority.ALWAYS);
            box.setMaxWidth(Double.MAX_VALUE);
        }
        row.getChildren().addAll(estimated, parts, labour, total);
        card.getChildren().addAll(sectionHeading("Cost Details"), new Separator(), row);
        return card;
    }

    private HBox createActions() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_RIGHT);

        Button back = button("Back", WHITE, HEADING, BORDER);
        back.setOnAction(e -> backAction.run());

        String status = normalizeStatus(request.getStatus());

        if (status.equals("Pending") || status.equals("Assigned")) {
            Button assign = button(status.equals("Assigned") ? "Reassign Mechanic" : "Assign Mechanic", BLUE, "#FFFFFF", BLUE);
            assign.setOnAction(e -> assignAction.run());
            box.getChildren().add(assign);
        }

        if (!status.equals("Completed") && !status.equals("Cancelled")) {
            Button cancel = button("Cancel Request", RED, "#FFFFFF", RED);
            cancel.setOnAction(e -> statusAction.run());
            box.getChildren().add(cancel);
        }

        box.getChildren().add(back);
        return box;
    }

    private VBox createCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(17));
        card.setStyle("-fx-background-color: " + SURFACE + "; -fx-border-color: " + BORDER + ";"
                + "-fx-border-radius: 12; -fx-background-radius: 12;");
        return card;
    }

    private VBox infoBox(String title, String value) {
        VBox box = new VBox(5);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: " + WHITE + "; -fx-border-color: " + BORDER + ";"
                + "-fx-border-radius: 8; -fx-background-radius: 8;");
        Label t = new Label(title);
        t.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 8px; -fx-font-weight: bold;");
        Label v = new Label(safe(value));
        v.setWrapText(true);
        v.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 11px; -fx-font-weight: bold;");

        // UI-only: use shared vector icons for customer, vehicle and mechanic representations.
        if ("CUSTOMER".equalsIgnoreCase(title)) {
            v.setGraphic(IconUtil.createUserIcon(0.55, TEXT));
            v.setGraphicTextGap(7);
        } else if ("VEHICLE".equalsIgnoreCase(title)) {
            v.setGraphic(IconUtil.createVehicleIcon(0.55, BLUE));
            v.setGraphicTextGap(7);
        } else if ("MECHANIC".equalsIgnoreCase(title)) {
            v.setGraphic(IconUtil.createMechanicIcon(0.55, ORANGE));
            v.setGraphicTextGap(7);
        }

        box.getChildren().addAll(t, v);
        return box;
    }

    private Label sectionHeading(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 17px; -fx-font-weight: bold;");
        return label;
    }

    private Label miniHeading(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 8px; -fx-font-weight: bold;");
        return label;
    }

    private Label createStatusBadge(String status) {
        String normalized = normalizeStatus(status);
        String color = getStatusColor(normalized);
        Label badge = new Label(normalized);
        badge.setStyle("-fx-text-fill: " + color + "; -fx-background-color: " + color + "18;"
                + "-fx-border-color: " + color + "55; -fx-border-radius: 14; -fx-background-radius: 14;"
                + "-fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 6 11 6 11;");
        return badge;
    }

    private Button button(String text, String background, String textColor, String border) {
        Button button = new Button(text);
        button.setCursor(Cursor.HAND);
        button.setStyle("-fx-background-color: " + background + "; -fx-text-fill: " + textColor + ";"
                + "-fx-border-color: " + border + "; -fx-border-radius: 8; -fx-background-radius: 8;"
                + "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 9 15 9 15;");
        return button;
    }

    private String getMechanicDisplay() {
        if (!request.hasMechanic()) return "Not Assigned";
        return firstNonBlank(request.getMechanicName(), request.getMechanicId(), "Assigned");
    }

    private String getNextAction() {
        switch (normalizeStatus(request.getStatus())) {
            case "Pending": return "Next: Admin assigns a mechanic.";
            case "Assigned": return "Next: Assigned mechanic accepts the request.";
            case "Accepted": return "Next: Mechanic starts repair.";
            case "In Progress": return "Next: Mechanic saves work and completes the job.";
            case "Completed": return "Lifecycle complete. This record is now in service history.";
            case "Cancelled": return "This request has been cancelled.";
            default: return "Current status is being monitored.";
        }
    }

    private String resolveFinalCost() {
        String finalCost = clean(request.getFinalCost());
        if (finalCost != null) return finalCost;
        double parts = parseMoney(request.getPartsCost());
        double labour = parseMoney(request.getLabourCost());
        if (parts + labour > 0) return String.valueOf(parts + labour);
        return firstNonBlank(request.getEstimatedCost(), "0");
    }

    private String money(String value) {
        String cleaned = clean(value);
        if (cleaned == null) return "₹0";
        return cleaned.startsWith("₹") ? cleaned : "₹" + cleaned;
    }

    private double parseMoney(String value) {
        try {
            String cleaned = clean(value);
            if (cleaned == null) return 0.0;
            return Math.max(0.0, Double.parseDouble(cleaned.replace("₹", "").replace(",", "")));
        } catch (Exception e) {
            return 0.0;
        }
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