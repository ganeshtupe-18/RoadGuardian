package project.ui.admin.Services;

import com.google.cloud.firestore.Firestore;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import project.controller.admin.ServiceController;
import project.firebase.FirebaseConfig;
import project.model.RoadService;
import project.ui.admin.DashBoard.AdminSectionPage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ServiceManagementPage extends AdminSectionPage {

    private static final String BG = "#0F0F0F";
    private static final String SURFACE = "#1A1A1A";
    private static final String WHITE = "#1A1A1A";
    private static final String DARK = "#F3F4F6";
    private static final String TEXT = "#A1A1AA";
    private static final String MUTED = "#A1A1AA";
    private static final String BORDER = "#F59E0B";
    private static final String BLUE = "#F59E0B";
    private static final String GREEN = "#22C55E";
    private static final String RED = "#EF4444";
    private static final String ORANGE = "#F59E0B";

    private ServiceController controller;
    private final List<RoadService> allServices = new ArrayList<>();

    private VBox root;
    private VBox listBox;
    private ScrollPane recordsScroll;
    private TextField searchField;
    private ComboBox<String> statusFilter;
    private ComboBox<String> sortFilter;
    private Label resultLabel;
    private Label totalLabel;
    private Label activeLabel;
    private Label inactiveLabel;
    private double savedScroll = 0.0;

    @Override
    public VBox getView() {
        root = new VBox();
        root.setStyle("-fx-background-color: " + BG + ";");

        try {
            Firestore firestore = FirebaseConfig.getFirestore();
            controller = new ServiceController(firestore);
        } catch (Exception e) {
            e.printStackTrace();
            root.getChildren().setAll(errorView("Unable to connect to Firebase."));
            return root;
        }

        showManagementPage();
        return root;
    }

    private void showManagementPage() {
        root.getChildren().clear();
        root.setPadding(new Insets(28, 32, 32, 32));
        root.setStyle("-fx-background-color: " + BG + ";");

        HBox header = createHeader();
        GridPane stats = createStatistics();
        HBox toolbar = createToolbar();
        VBox records = createRecordsCard();

        root.setSpacing(20);
        root.getChildren().addAll(header, stats, toolbar, records);
        VBox.setVgrow(records, Priority.ALWAYS);

        loadServices();
        Platform.runLater(() -> {
            if (recordsScroll != null) {
                recordsScroll.setVvalue(savedScroll);
            }
        });
    }

    private HBox createHeader() {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);

        VBox text = new VBox(4);
        Label title = label("Services", 29, true, DARK);
        Label subtitle = label(
                "Manage the Firebase service catalog used by customers when requesting a mechanic.",
                12,
                false,
                TEXT
        );
        subtitle.setWrapText(true);
        text.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label live = label("● SYSTEM LIVE", 10, true, GREEN);
        Button add = primaryButton("+ Add Service");
        add.setOnAction(e -> openAddPage());

        box.getChildren().addAll(text, spacer, live, add);
        return box;
    }

    private GridPane createStatistics() {
        GridPane grid = new GridPane();
        grid.setHgap(14);

        for (int i = 0; i < 3; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(33.333);
            column.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(column);
        }

        totalLabel = statValue(BLUE);
        activeLabel = statValue(GREEN);
        inactiveLabel = statValue(RED);

        grid.add(statCard("Total Services", "All catalog records", totalLabel), 0, 0);
        grid.add(statCard("Active", "Visible to customers", activeLabel), 1, 0);
        grid.add(statCard("Inactive", "Hidden from customer selection", inactiveLabel), 2, 0);
        return grid;
    }

    private VBox statCard(String title, String subtitle, Label value) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(16));
        card.setStyle(cardStyle());
        card.setMaxWidth(Double.MAX_VALUE);
        card.getChildren().addAll(
                label(title, 11, true, DARK),
                value,
                label(subtitle, 9, false, MUTED)
        );
        return card;
    }

    private Label statValue(String color) {
        return label("0", 25, true, color);
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("Search service name, category or description...");
        searchField.setPrefWidth(360);
        searchField.setPrefHeight(42);
        searchField.setStyle(inputStyle());

        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All", "Active", "Inactive");
        statusFilter.setValue("All");
        statusFilter.setPrefWidth(140);
        statusFilter.setPrefHeight(42);
        styleCombo(statusFilter);

        sortFilter = new ComboBox<>();
        sortFilter.getItems().addAll("Name A-Z", "Name Z-A", "Lowest Price", "Highest Price");
        sortFilter.setValue("Name A-Z");
        sortFilter.setPrefWidth(150);
        sortFilter.setPrefHeight(42);
        styleCombo(sortFilter);

        Button refresh = primaryButton("Refresh");
        refresh.setOnAction(e -> refreshPreservingScroll());

        searchField.textProperty().addListener((o, a, b) -> applyFilters());
        statusFilter.valueProperty().addListener((o, a, b) -> applyFilters());
        sortFilter.valueProperty().addListener((o, a, b) -> applyFilters());

        toolbar.getChildren().addAll(searchField, statusFilter, sortFilter, refresh);
        return toolbar;
    }

    private VBox createRecordsCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(18));
        card.setStyle(cardStyle());
        VBox.setVgrow(card, Priority.ALWAYS);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox titleBox = new VBox(3);
        titleBox.getChildren().addAll(
                label("Service Catalog", 18, true, DARK),
                label("No demo rows. Every record below comes from services collection.", 10, false, TEXT)
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        resultLabel = label("0 services", 10, true, BLUE);
        header.getChildren().addAll(titleBox, spacer, resultLabel);

        listBox = new VBox(10);
        listBox.setFillWidth(true);

        recordsScroll = new ScrollPane(listBox);
        recordsScroll.setFitToWidth(true);
        recordsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        recordsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        recordsScroll.setStyle("-fx-background-color: transparent;-fx-background: transparent;-fx-border-color: transparent;");
        recordsScroll.setPrefHeight(500);

        card.getChildren().addAll(header, recordsScroll);
        VBox.setVgrow(recordsScroll, Priority.ALWAYS);
        return card;
    }

    private void loadServices() {
        allServices.clear();
        if (controller != null) {
            allServices.addAll(controller.getAllServices());
        }

        totalLabel.setText(String.valueOf(allServices.size()));
        int active = 0;
        for (RoadService service : allServices) {
            if (service != null && service.isActive()) active++;
        }
        activeLabel.setText(String.valueOf(active));
        inactiveLabel.setText(String.valueOf(allServices.size() - active));
        applyFilters();
    }

    private void refreshPreservingScroll() {
        savedScroll = recordsScroll == null ? 0.0 : recordsScroll.getVvalue();
        loadServices();
        Platform.runLater(() -> {
            if (recordsScroll != null) recordsScroll.setVvalue(savedScroll);
        });
    }

    private void applyFilters() {
        if (listBox == null) return;

        String search = clean(searchField == null ? null : searchField.getText());
        String status = statusFilter == null ? "All" : statusFilter.getValue();
        String sort = sortFilter == null ? "Name A-Z" : sortFilter.getValue();

        List<RoadService> filtered = new ArrayList<>();
        for (RoadService service : allServices) {
            if (service == null) continue;

            if (search != null) {
                String q = search.toLowerCase();
                if (!contains(service.getName(), q)
                        && !contains(service.getCategory(), q)
                        && !contains(service.getDescription(), q)) {
                    continue;
                }
            }

            if (status != null && !"All".equalsIgnoreCase(status)) {
                String current = service.isActive() ? "Active" : "Inactive";
                if (!current.equalsIgnoreCase(status)) continue;
            }

            filtered.add(service);
        }

        if ("Name Z-A".equals(sort)) {
            filtered.sort(Comparator.comparing((RoadService s) -> safe(s.getName()).toLowerCase()).reversed());
        } else if ("Lowest Price".equals(sort)) {
            filtered.sort(Comparator.comparingDouble(RoadService::getBasePrice));
        } else if ("Highest Price".equals(sort)) {
            filtered.sort(Comparator.comparingDouble(RoadService::getBasePrice).reversed());
        } else {
            filtered.sort(Comparator.comparing(s -> safe(s.getName()).toLowerCase()));
        }

        render(filtered);
    }

    private void render(List<RoadService> services) {
        listBox.getChildren().clear();

        if (services == null || services.isEmpty()) {
            VBox empty = new VBox(6);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(40));
            empty.getChildren().addAll(
                    label("No Services Found", 15, true, DARK),
                    label("Add a service from Admin. Active services will then appear on the customer mechanic request flow.", 11, false, TEXT)
            );
            listBox.getChildren().add(empty);
            resultLabel.setText("0 services");
            return;
        }

        for (RoadService service : services) {
            listBox.getChildren().add(serviceCard(service));
        }
        resultLabel.setText(services.size() == 1 ? "1 service" : services.size() + " services");
    }

    private VBox serviceCard(RoadService service) {
        VBox card = new VBox(11);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: " + WHITE + ";-fx-border-color: " + BORDER + ";-fx-border-radius: 10;-fx-background-radius: 10;");

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);

        VBox identity = new VBox(3);
        Label name = label(safe(service.getName()), 15, true, DARK);
        Label id = label("ID: " + safe(service.getServiceId()), 9, false, MUTED);
        identity.getChildren().addAll(name, id);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String statusText = service.isActive() ? "Active" : "Inactive";
        String statusColor = service.isActive() ? GREEN : RED;
        Label badge = label(statusText, 9, true, statusColor);
        badge.setStyle("-fx-text-fill: " + statusColor + ";-fx-font-size: 9px;-fx-font-weight: bold;-fx-background-color: " + statusColor + "18;-fx-border-color: " + statusColor + "55;-fx-border-radius: 14;-fx-background-radius: 14;-fx-padding: 5 10 5 10;");
        top.getChildren().addAll(identity, spacer, badge);

        GridPane info = new GridPane();
        info.setHgap(10);
        info.setVgap(10);
        for (int i = 0; i < 3; i++) {
            ColumnConstraints c = new ColumnConstraints();
            c.setPercentWidth(33.333);
            c.setHgrow(Priority.ALWAYS);
            info.getColumnConstraints().add(c);
        }
        info.add(infoBox("CATEGORY", safe(service.getCategory())), 0, 0);
        info.add(infoBox("BASE PRICE", service.getPriceDisplay()), 1, 0);
        info.add(infoBox("DURATION", safe(service.getEstimatedDuration())), 2, 0);

        Label description = label(safe(service.getDescription()), 11, false, DARK);
        description.setWrapText(true);

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button details = outlineButton("View Details");
        Button edit = primaryButton("Edit");
        Button delete = dangerButton("Delete");
        details.setOnAction(e -> openDetailsPage(service));
        edit.setOnAction(e -> openEditPage(service));
        delete.setOnAction(e -> deleteService(service));
        actions.getChildren().addAll(details, edit, delete);

        card.getChildren().addAll(top, info, description, actions);
        return card;
    }

    private VBox infoBox(String title, String value) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: #1A1A1A;-fx-border-color: #F59E0B;-fx-border-radius: 8;-fx-background-radius: 8;");
        box.getChildren().addAll(
                label(title, 8, true, TEXT),
                label(safe(value), 11, true, DARK)
        );
        return box;
    }

    private void openAddPage() {
        savedScroll = recordsScroll == null ? 0 : recordsScroll.getVvalue();
        AddServicePage page = new AddServicePage(
                this::showManagementPage,
                service -> {
                    if (controller.addService(service)) {
                        info("Service Added", "Service saved to Firebase successfully.");
                        showManagementPage();
                    } else {
                        error("Unable to Add Service", controller.getLastError());
                    }
                }
        );
        navigateTo(page.getView());
    }

    private void openDetailsPage(RoadService service) {
        savedScroll = recordsScroll == null ? 0 : recordsScroll.getVvalue();
        RoadService fresh = controller.getServiceById(service.getServiceId());
        if (fresh == null) fresh = service;
        RoadService current = fresh;

        ServiceDetailsPage page = new ServiceDetailsPage(
                current,
                this::showManagementPage,
                () -> openEditPage(current)
        );
        navigateTo(page.getView());
    }

    private void openEditPage(RoadService service) {
        RoadService fresh = controller.getServiceById(service.getServiceId());
        if (fresh == null) fresh = service;

        RoadService current = fresh;
        EditServicePage page = new EditServicePage(
                current,
                () -> openDetailsPage(current),
                updated -> {
                    if (controller.updateService(updated)) {
                        info("Service Updated", "The same Firebase service record was updated.");
                        openDetailsPage(updated);
                    } else {
                        error("Unable to Update Service", controller.getLastError());
                    }
                }
        );
        navigateTo(page.getView());
    }

    private void deleteService(RoadService service) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("RoadGuardian");
        alert.setHeaderText("Delete Service?");
        alert.setContentText(
                "Service: " + safe(service.getName())
                        + "\n\nIf this service is already used by a service request, deletion will be blocked. Use Inactive instead."
        );
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        if (controller.deleteService(service)) {
            info("Service Deleted", "Service removed from Firebase.");
            refreshPreservingScroll();
        } else {
            error("Unable to Delete Service", controller.getLastError());
        }
    }

    private void navigateTo(VBox page) {
        root.getChildren().clear();
        root.setPadding(Insets.EMPTY);
        root.getChildren().add(page);
        VBox.setVgrow(page, Priority.ALWAYS);
    }

    private VBox errorView(String message) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));
        box.getChildren().addAll(
                label("Services Unavailable", 18, true, RED),
                label(message, 11, false, DARK)
        );
        return box;
    }

    private String cardStyle() {
        return "-fx-background-color: " + SURFACE + ";-fx-border-color: " + BORDER + ";-fx-border-radius: 12;-fx-background-radius: 12;";
    }

    private String inputStyle() {
        return "-fx-background-color: " + WHITE + ";-fx-text-fill: " + DARK + ";-fx-prompt-text-fill: " + MUTED + ";-fx-border-color: " + BORDER + ";-fx-border-radius: 8;-fx-background-radius: 8;-fx-font-size: 11px;";
    }

    private void styleCombo(ComboBox<String> combo) {
        combo.setStyle(inputStyle());
        combo.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setStyle("-fx-text-fill: " + DARK + ";-fx-font-size: 11px;");
            }
        });
        combo.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setStyle("-fx-text-fill: " + DARK + ";-fx-background-color: #1A1A1A;-fx-font-size: 11px;");
            }
        });
    }

    private Button primaryButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + BLUE + ";-fx-text-fill: white;-fx-font-weight: bold;-fx-background-radius: 8;-fx-padding: 9 15 9 15;-fx-cursor: hand;");
        return button;
    }

    private Button outlineButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + WHITE + ";-fx-text-fill: " + DARK + ";-fx-border-color: " + BORDER + ";-fx-border-radius: 8;-fx-background-radius: 8;-fx-padding: 8 13 8 13;-fx-cursor: hand;");
        return button;
    }

    private Button dangerButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + RED + ";-fx-text-fill: white;-fx-font-weight: bold;-fx-background-radius: 8;-fx-padding: 8 13 8 13;-fx-cursor: hand;");
        return button;
    }

    private Label label(String text, int size, boolean bold, String color) {
        Label label = new Label(text == null ? "" : text);
        label.setStyle("-fx-text-fill: " + color + ";-fx-font-size: " + size + "px;" + (bold ? "-fx-font-weight: bold;" : ""));
        return label;
    }

    private void info(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("RoadGuardian");
        alert.setHeaderText(title);
        alert.setContentText(message == null ? "" : message);
        alert.showAndWait();
    }

    private void error(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("RoadGuardian");
        alert.setHeaderText(title);
        alert.setContentText(message == null || message.isBlank() ? "Unknown Firebase error." : message);
        alert.showAndWait();
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase().contains(query);
    }

    private String clean(String value) {
        if (value == null) return null;
        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
