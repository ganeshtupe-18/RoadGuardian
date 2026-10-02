package project.ui.admin.Services;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import project.model.RoadService;
import project.ui.admin.DashBoard.AdminSectionPage;

import java.util.function.Consumer;

public class AddServicePage extends AdminSectionPage {

    private static final String BG = "#0F0F0F";
    private static final String WHITE = "#1A1A1A";
    private static final String DARK = "#F3F4F6";
    private static final String TEXT = "#A1A1AA";
    private static final String BORDER = "#F59E0B";
    private static final String BLUE = "#F59E0B";
    private static final String RED = "#EF4444";

    private final Runnable onBack;
    private final Consumer<RoadService> onSave;

    private TextField nameField;
    private TextArea descriptionField;
    private TextField categoryField;
    private TextField priceField;
    private TextField durationField;
    private ComboBox<String> statusBox;
    private Label messageLabel;

    public AddServicePage(Runnable onBack, Consumer<RoadService> onSave) {
        this.onBack = onBack == null ? () -> {} : onBack;
        this.onSave = onSave;
    }

    @Override
    public VBox getView() {
        VBox root = new VBox(18);
        root.setPadding(new Insets(28, 32, 32, 32));
        root.setStyle("-fx-background-color: " + BG + ";");

        Button back = secondaryButton("← Back to Services");
        back.setOnAction(e -> onBack.run());

        Label title = darkLabel("Add Service", 28, true);
        Label subtitle = darkLabel(
                "Create a real Firebase service that customers can select while requesting a mechanic.",
                12,
                false
        );
        subtitle.setWrapText(true);

        VBox card = new VBox(14);
        card.setPadding(new Insets(22));
        card.setStyle(cardStyle());

        nameField = input("Service name");
        descriptionField = new TextArea();
        descriptionField.setPromptText("Service description");
        descriptionField.setWrapText(true);
        descriptionField.setPrefRowCount(4);
        descriptionField.setStyle(inputStyle());

        categoryField = input("Category, e.g. Repair / Electrical / Roadside");
        priceField = input("Base price in rupees, e.g. 499");
        durationField = input("Estimated duration, e.g. 45 min");

        statusBox = new ComboBox<>();
        statusBox.getItems().addAll("Active", "Inactive");
        statusBox.setValue("Active");
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

        messageLabel = darkLabel("", 11, true);
        messageLabel.setWrapText(true);

        Button save = primaryButton("Save Service");
        save.setOnAction(e -> save());

        HBox actions = new HBox(10, secondaryButton("Cancel"), save);
        ((Button) actions.getChildren().get(0)).setOnAction(e -> onBack.run());
        actions.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(back, title, subtitle, card, messageLabel, actions);
        return root;
    }

    private void save() {
        String name = clean(nameField.getText());
        String description = clean(descriptionField.getText());
        String category = clean(categoryField.getText());
        String duration = clean(durationField.getText());

        if (name == null) {
            error("Service name is required.");
            return;
        }

        double price;
        try {
            String raw = clean(priceField.getText());
            price = raw == null ? 0 : Double.parseDouble(raw.replace(",", ""));
            if (price < 0) {
                throw new NumberFormatException();
            }
        } catch (Exception e) {
            error("Base price must be a valid non-negative number.");
            return;
        }

        RoadService service = new RoadService();
        service.setName(name);
        service.setDescription(description == null ? "" : description);
        service.setCategory(category == null ? "General" : category);
        service.setBasePrice(price);
        service.setEstimatedDuration(duration == null ? "" : duration);
        service.setStatus(statusBox.getValue());

        if (onSave != null) {
            onSave.accept(service);
        }
    }

    private VBox field(String title, javafx.scene.Node node) {
        VBox box = new VBox(6);
        Label label = darkLabel(title, 11, true);
        if (node instanceof javafx.scene.control.Control) {
            ((javafx.scene.control.Control) node).setMaxWidth(Double.MAX_VALUE);
        }
        box.getChildren().addAll(label, node);
        return box;
    }

    private TextField input(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setStyle(inputStyle());
        field.setPrefHeight(42);
        return field;
    }

    private String inputStyle() {
        return "-fx-background-color: " + WHITE + ";"
                + "-fx-text-fill: " + DARK + ";"
                + "-fx-prompt-text-fill: #A1A1AA;"
                + "-fx-border-color: " + BORDER + ";"
                + "-fx-border-radius: 8;"
                + "-fx-background-radius: 8;"
                + "-fx-font-size: 12px;";
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
        return "-fx-background-color: #1A1A1A;"
                + "-fx-border-color: " + BORDER + ";"
                + "-fx-border-radius: 12;"
                + "-fx-background-radius: 12;";
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

    private Label darkLabel(String text, int size, boolean bold) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: " + (bold ? DARK : TEXT) + ";-fx-font-size: " + size + "px;" + (bold ? "-fx-font-weight: bold;" : ""));
        return label;
    }

    private void error(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: " + RED + ";-fx-font-size: 11px;-fx-font-weight: bold;");
    }

    private String clean(String value) {
        if (value == null) return null;
        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }
}
