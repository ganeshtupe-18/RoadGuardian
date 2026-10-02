package project.ui.admin.Services;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import project.model.RoadService;
import project.ui.admin.DashBoard.AdminSectionPage;

import java.util.function.Consumer;

public class EditServicePage extends AdminSectionPage {

    private static final String BG = "#0F0F0F";
    private static final String WHITE = "#1A1A1A";
    private static final String DARK = "#F3F4F6";
    private static final String TEXT = "#A1A1AA";
    private static final String BORDER = "#F59E0B";
    private static final String BLUE = "#F59E0B";
    private static final String RED = "#EF4444";

    private final RoadService service;
    private final Runnable onBack;
    private final Consumer<RoadService> onSave;

    private TextField nameField;
    private TextArea descriptionField;
    private TextField categoryField;
    private TextField priceField;
    private TextField durationField;
    private ComboBox<String> statusBox;
    private Label messageLabel;

    public EditServicePage(RoadService service, Runnable onBack, Consumer<RoadService> onSave) {
        this.service = service;
        this.onBack = onBack == null ? () -> {} : onBack;
        this.onSave = onSave;
    }

    @Override
    public VBox getView() {
        VBox root = new VBox(18);
        root.setPadding(new Insets(28, 32, 32, 32));
        root.setStyle("-fx-background-color: " + BG + ";");

        Button back = secondaryButton("← Back to Service Details");
        back.setOnAction(e -> onBack.run());

        Label title = label("Edit Service", 28, true);
        Label subtitle = label("Changes are saved to the same services/{serviceId} Firebase record.", 12, false);

        VBox card = new VBox(14);
        card.setPadding(new Insets(22));
        card.setStyle(cardStyle());

        nameField = input(service == null ? "" : service.getName());
        descriptionField = new TextArea(service == null ? "" : safe(service.getDescription()));
        descriptionField.setWrapText(true);
        descriptionField.setPrefRowCount(4);
        descriptionField.setStyle(inputStyle());

        categoryField = input(service == null ? "" : safe(service.getCategory()));
        priceField = input(service == null ? "0" : priceText(service.getBasePrice()));
        durationField = input(service == null ? "" : safe(service.getEstimatedDuration()));

        statusBox = new ComboBox<>();
        statusBox.getItems().addAll("Active", "Inactive");
        statusBox.setValue(service != null && !service.isActive() ? "Inactive" : "Active");
        statusBox.setMaxWidth(Double.MAX_VALUE);
        styleStatusCombo(statusBox);

        card.getChildren().addAll(
                field("Service Name", nameField),
                field("Description", descriptionField),
                field("Category", categoryField),
                field("Base Price", priceField),
                field("Estimated Duration", durationField),
                field("Status", statusBox)
        );

        messageLabel = label("", 11, true);

        Button cancel = secondaryButton("Cancel");
        cancel.setOnAction(e -> onBack.run());
        Button save = primaryButton("Update Service");
        save.setOnAction(e -> save());
        HBox actions = new HBox(10, cancel, save);
        actions.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(back, title, subtitle, card, messageLabel, actions);
        return root;
    }

    private void save() {
        if (service == null) {
            error("Service is not available.");
            return;
        }

        String name = clean(nameField.getText());
        if (name == null) {
            error("Service name is required.");
            return;
        }

        double price;
        try {
            String raw = clean(priceField.getText());
            price = raw == null ? 0 : Double.parseDouble(raw.replace(",", ""));
            if (price < 0) throw new NumberFormatException();
        } catch (Exception e) {
            error("Base price must be a valid non-negative number.");
            return;
        }

        service.setName(name);
        service.setDescription(safe(clean(descriptionField.getText())));
        service.setCategory(defaultValue(categoryField.getText(), "General"));
        service.setBasePrice(price);
        service.setEstimatedDuration(safe(clean(durationField.getText())));
        service.setStatus(statusBox.getValue());

        if (onSave != null) {
            onSave.accept(service);
        }
    }

    private VBox field(String title, javafx.scene.Node node) {
        VBox box = new VBox(6);
        box.getChildren().addAll(label(title, 11, true), node);
        return box;
    }

    private TextField input(String value) {
        TextField field = new TextField(value == null ? "" : value);
        field.setStyle(inputStyle());
        field.setPrefHeight(42);
        field.setMaxWidth(Double.MAX_VALUE);
        return field;
    }

    private String inputStyle() {
        return "-fx-background-color: " + WHITE + ";-fx-text-fill: " + DARK + ";-fx-prompt-text-fill: #A1A1AA;-fx-border-color: " + BORDER + ";-fx-border-radius: 8;-fx-background-radius: 8;-fx-font-size: 12px;";
    }

    private void styleStatusCombo(ComboBox<String> combo) {
        combo.setStyle(inputStyle());
        combo.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setStyle("-fx-text-fill: " + DARK + ";-fx-font-size: 12px;");
            }
        });
        combo.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setStyle("-fx-text-fill: " + DARK + ";-fx-background-color: #1A1A1A;-fx-font-size: 12px;");
            }
        });
    }

    private String cardStyle() {
        return "-fx-background-color: #1A1A1A;-fx-border-color: " + BORDER + ";-fx-border-radius: 12;-fx-background-radius: 12;";
    }

    private Button primaryButton(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + BLUE + ";-fx-text-fill: white;-fx-font-weight: bold;-fx-background-radius: 8;-fx-padding: 10 18 10 18;-fx-cursor: hand;");
        return b;
    }

    private Button secondaryButton(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + WHITE + ";-fx-text-fill: " + DARK + ";-fx-border-color: " + BORDER + ";-fx-border-radius: 8;-fx-background-radius: 8;-fx-padding: 9 15 9 15;-fx-cursor: hand;");
        return b;
    }

    private Label label(String text, int size, boolean bold) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: " + (bold ? DARK : TEXT) + ";-fx-font-size: " + size + "px;" + (bold ? "-fx-font-weight: bold;" : ""));
        return label;
    }

    private void error(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: " + RED + ";-fx-font-size: 11px;-fx-font-weight: bold;");
    }

    private String defaultValue(String value, String fallback) {
        String cleaned = clean(value);
        return cleaned == null ? fallback : cleaned;
    }

    private String clean(String value) {
        if (value == null) return null;
        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String priceText(double value) {
        if (Math.floor(value) == value) return String.valueOf((long) value);
        return String.valueOf(value);
    }
}
