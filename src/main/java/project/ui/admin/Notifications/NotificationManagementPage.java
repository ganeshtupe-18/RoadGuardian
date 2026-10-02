package project.ui.admin.Notifications;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import project.controller.admin.NotificationController;
import project.model.Notification;
import project.ui.admin.DashBoard.AdminSectionPage;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NotificationManagementPage extends AdminSectionPage {

    private static final String BG = "#0F0F0F";
    private static final String CARD = "#1A1A1A";
    private static final String WHITE = "#1A1A1A";
    private static final String HEADING = "#F3F4F6";
    private static final String TEXT = "#A1A1AA";
    private static final String MUTED = "#A1A1AA";
    private static final String BORDER = "#F59E0B";
    private static final String BLUE = "#F59E0B";
    private static final String GREEN = "#22C55E";
    private static final String RED = "#EF4444";
    private static final String ORANGE = "#F59E0B";
    private static final String PURPLE = "#F59E0B";

    private final List<Notification> allNotifications = new ArrayList<>();

    private NotificationController controller;
    private VBox root;
    private VBox recordsContainer;

    private Label totalLabel;
    private Label broadcastLabel;
    private Label directLabel;
    private Label reminderLabel;
    private Label resultLabel;

    private ComboBox<String> recipientMode;
    private TextField recipientField;
    private ComboBox<String> typeField;
    private TextField titleField;
    private TextArea messageField;

    private TextField searchField;
    private ComboBox<String> filterField;

    @Override
    public VBox getView() {
        root = new VBox(20);
        root.setPadding(new Insets(28, 30, 34, 30));
        root.setStyle("-fx-background-color: " + BG + ";");
        root.setFillWidth(true);

        try {
            controller = new NotificationController();
        } catch (Exception e) {
            root.getChildren().add(createErrorView("Unable to connect to Firebase."));
            return root;
        }

        root.getChildren().addAll(
                createHeader(),
                createStats(),
                createComposer(),
                createToolbar(),
                createRecordsSection()
        );

        loadNotifications();
        return root;
    }

    private HBox createHeader() {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);

        VBox text = new VBox(4);
        Label title = new Label("Notification Management");
        title.setStyle(
                "-fx-text-fill: " + HEADING + ";" +
                "-fx-font-size: 28px;" +
                "-fx-font-weight: bold;"
        );

        Label subtitle = new Label(
                "Send real Firebase notifications to all customers or a specific customer."
        );
        subtitle.setStyle(
                "-fx-text-fill: " + TEXT + ";" +
                "-fx-font-size: 12px;"
        );
        text.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label live = new Label("● SYSTEM LIVE");
        live.setStyle(
                "-fx-text-fill: " + GREEN + ";" +
                "-fx-font-size: 10px;" +
                "-fx-font-weight: bold;"
        );

        box.getChildren().addAll(text, spacer, live);
        return box;
    }

    private GridPane createStats() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);

        totalLabel = valueLabel(BLUE);
        broadcastLabel = valueLabel(GREEN);
        directLabel = valueLabel(PURPLE);
        reminderLabel = valueLabel(ORANGE);

        grid.add(statCard("Total", totalLabel, "Saved notifications"), 0, 0);
        grid.add(statCard("Broadcast", broadcastLabel, "All customers"), 1, 0);
        grid.add(statCard("Direct", directLabel, "Specific customer"), 2, 0);
        grid.add(statCard("Reminders", reminderLabel, "Reminder type"), 3, 0);

        for (int i = 0; i < 4; i++) {
            javafx.scene.layout.ColumnConstraints column = new javafx.scene.layout.ColumnConstraints();
            column.setPercentWidth(25);
            column.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(column);
        }

        return grid;
    }

    private VBox statCard(String title, Label value, String subtitle) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(15));
        card.setStyle(
                "-fx-background-color: " + CARD + ";" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 11;" +
                "-fx-background-radius: 11;"
        );

        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-text-fill: #A1A1AA;" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;"
        );

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle(
                "-fx-text-fill: " + MUTED + ";" +
                "-fx-font-size: 9px;"
        );

        card.getChildren().addAll(titleLabel, value, subtitleLabel);
        return card;
    }

    private Label valueLabel(String color) {
        Label label = new Label("0");
        label.setStyle(
                "-fx-text-fill: " + color + ";" +
                "-fx-font-size: 24px;" +
                "-fx-font-weight: bold;"
        );
        return label;
    }

    private VBox createComposer() {
        VBox card = new VBox(13);
        card.setPadding(new Insets(18));
        card.setStyle(
                "-fx-background-color: " + CARD + ";" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 12;" +
                "-fx-background-radius: 12;"
        );

        Label heading = new Label("Send Notification");
        heading.setStyle(
                "-fx-text-fill: " + HEADING + ";" +
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;"
        );

        Label info = new Label(
                "Broadcast is visible to every customer. For a direct message, enter the customer's login email or customer ID."
        );
        info.setWrapText(true);
        info.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 11px;");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);

        recipientMode = new ComboBox<>();
        recipientMode.getItems().addAll("All Customers", "Specific Customer");
        recipientMode.setValue("All Customers");
        recipientMode.setMaxWidth(Double.MAX_VALUE);
        styleCombo(recipientMode);

        recipientField = new TextField();
        recipientField.setPromptText("Customer email or customer ID");
        recipientField.setDisable(true);
        styleTextField(recipientField);

        typeField = new ComboBox<>();
        typeField.getItems().addAll("General", "Service", "SOS", "Reminder", "Safety");
        typeField.setValue("General");
        typeField.setMaxWidth(Double.MAX_VALUE);
        styleCombo(typeField);

        titleField = new TextField();
        titleField.setPromptText("Notification title");
        styleTextField(titleField);

        messageField = new TextArea();
        messageField.setPromptText("Write the notification message...");
        messageField.setWrapText(true);
        messageField.setPrefRowCount(3);
        messageField.setStyle(
                "-fx-control-inner-background: " + WHITE + ";" +
                "-fx-text-fill: " + HEADING + ";" +
                "-fx-prompt-text-fill: " + MUTED + ";" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-font-size: 11px;"
        );

        recipientMode.valueProperty().addListener((obs, oldValue, newValue) -> {
            boolean direct = "Specific Customer".equals(newValue);
            recipientField.setDisable(!direct);
            if (!direct) {
                recipientField.clear();
            }
        });

        form.add(fieldLabel("Recipient"), 0, 0);
        form.add(recipientMode, 1, 0);
        form.add(fieldLabel("Customer"), 2, 0);
        form.add(recipientField, 3, 0);
        form.add(fieldLabel("Type"), 0, 1);
        form.add(typeField, 1, 1);
        form.add(fieldLabel("Title"), 2, 1);
        form.add(titleField, 3, 1);
        form.add(fieldLabel("Message"), 0, 2);
        form.add(messageField, 1, 2, 3, 1);

        for (int i = 0; i < 4; i++) {
            javafx.scene.layout.ColumnConstraints column = new javafx.scene.layout.ColumnConstraints();
            if (i == 0 || i == 2) {
                column.setMinWidth(70);
                column.setPrefWidth(80);
            } else {
                column.setHgrow(Priority.ALWAYS);
            }
            form.getColumnConstraints().add(column);
        }

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button clear = secondaryButton("Clear");
        clear.setOnAction(event -> clearComposer());

        Button send = primaryButton("Send Notification");
        send.setOnAction(event -> sendNotification());

        actions.getChildren().addAll(clear, send);

        card.getChildren().addAll(heading, info, new Separator(), form, actions);
        return card;
    }

    private HBox createToolbar() {
        HBox bar = new HBox(10);
        bar.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("Search title, message or recipient...");
        searchField.setPrefWidth(360);
        styleTextField(searchField);

        filterField = new ComboBox<>();
        filterField.getItems().addAll("All", "General", "Service", "SOS", "Reminder", "Safety");
        filterField.setValue("All");
        styleCombo(filterField);

        Button refresh = secondaryButton("Refresh");
        refresh.setOnAction(event -> loadNotifications());

        searchField.textProperty().addListener((obs, oldValue, newValue) -> renderRecords());
        filterField.valueProperty().addListener((obs, oldValue, newValue) -> renderRecords());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        resultLabel = new Label("0 notifications");
        resultLabel.setStyle(
                "-fx-text-fill: " + TEXT + ";" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;"
        );

        bar.getChildren().addAll(searchField, filterField, refresh, spacer, resultLabel);
        return bar;
    }

    private VBox createRecordsSection() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(18));
        card.setStyle(
                "-fx-background-color: " + CARD + ";" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 12;" +
                "-fx-background-radius: 12;"
        );

        Label heading = new Label("Notification Records");
        heading.setStyle(
                "-fx-text-fill: " + HEADING + ";" +
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;"
        );

        Label note = new Label("Only real Firebase records are shown. No demo notifications are created.");
        note.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 10px;");

        recordsContainer = new VBox(10);
        recordsContainer.setFillWidth(true);

        card.getChildren().addAll(heading, note, new Separator(), recordsContainer);
        return card;
    }

    private void loadNotifications() {
        if (controller == null) {
            return;
        }

        List<Notification> loaded = controller.getAllNotifications();
        allNotifications.clear();
        if (loaded != null) {
            allNotifications.addAll(loaded);
        }

        updateStats();
        renderRecords();

        if (controller.getLastError() != null && allNotifications.isEmpty()) {
            showError("Unable to load notifications", controller.getLastError());
        }
    }

    private void updateStats() {
        int broadcasts = 0;
        int direct = 0;
        int reminders = 0;

        for (Notification notification : allNotifications) {
            if (notification == null) {
                continue;
            }
            if (notification.isBroadcast()) {
                broadcasts++;
            } else {
                direct++;
            }
            if (notification.isReminder()
                    || "Reminder".equalsIgnoreCase(notification.getType())) {
                reminders++;
            }
        }

        totalLabel.setText(String.valueOf(allNotifications.size()));
        broadcastLabel.setText(String.valueOf(broadcasts));
        directLabel.setText(String.valueOf(direct));
        reminderLabel.setText(String.valueOf(reminders));
    }

    private void renderRecords() {
        if (recordsContainer == null) {
            return;
        }

        recordsContainer.getChildren().clear();

        String search = searchField == null || searchField.getText() == null
                ? ""
                : searchField.getText().trim().toLowerCase();
        String filter = filterField == null ? "All" : filterField.getValue();

        int visible = 0;

        for (Notification notification : allNotifications) {
            if (notification == null) {
                continue;
            }

            if (filter != null && !"All".equalsIgnoreCase(filter)
                    && !filter.equalsIgnoreCase(notification.getType())) {
                continue;
            }

            if (!search.isEmpty()) {
                String haystack = (
                        firstNonBlank(notification.getTitle(), "") + " " +
                        firstNonBlank(notification.getMessage(), "") + " " +
                        firstNonBlank(notification.getRecipientId(), "") + " " +
                        firstNonBlank(notification.getType(), "")
                ).toLowerCase();

                if (!haystack.contains(search)) {
                    continue;
                }
            }

            recordsContainer.getChildren().add(createRecordCard(notification));
            visible++;
        }

        resultLabel.setText(visible == 1 ? "1 notification" : visible + " notifications");

        if (visible == 0) {
            VBox empty = new VBox(6);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(30));

            Label title = new Label("No notification records");
            title.setStyle(
                    "-fx-text-fill: " + HEADING + ";" +
                    "-fx-font-size: 14px;" +
                    "-fx-font-weight: bold;"
            );

            Label subtitle = new Label("Send a notification above and it will appear here.");
            subtitle.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 11px;");

            empty.getChildren().addAll(title, subtitle);
            recordsContainer.getChildren().add(empty);
        }
    }

    private VBox createRecordCard(Notification notification) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle(
                "-fx-background-color: " + WHITE + ";" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;"
        );

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);

        VBox text = new VBox(3);

        Label title = new Label(firstNonBlank(notification.getTitle(), "Notification"));
        title.setWrapText(true);
        title.setStyle(
                "-fx-text-fill: " + HEADING + ";" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;"
        );

        Label recipient = new Label(
                notification.isBroadcast()
                        ? "Recipient: All Customers"
                        : "Recipient: " + firstNonBlank(notification.getRecipientId(), "Unknown Customer")
        );
        recipient.setStyle(
                "-fx-text-fill: #A1A1AA;" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;"
        );

        text.getChildren().addAll(title, recipient);
        HBox.setHgrow(text, Priority.ALWAYS);

        Label type = badge(firstNonBlank(notification.getType(), "General"), colorFor(notification));

        Button delete = secondaryButton("Delete");
        delete.setStyle(delete.getStyle() + "-fx-text-fill: " + RED + "; -fx-border-color: " + RED + ";");
        delete.setOnAction(event -> deleteNotification(notification));

        top.getChildren().addAll(text, type, delete);

        Label message = new Label(firstNonBlank(notification.getMessage(), "No message."));
        message.setWrapText(true);
        message.setStyle(
                "-fx-text-fill: " + HEADING + ";" +
                "-fx-font-size: 12px;"
        );

        HBox meta = new HBox(14);
        Label source = metaLabel("Source: " + firstNonBlank(notification.getSource(), "Admin"));
        Label date = metaLabel("Sent: " + formatDate(notification.getCreatedAt()));
        Label id = metaLabel("ID: " + firstNonBlank(notification.getNotificationId(), "-"));
        meta.getChildren().addAll(source, date, id);

        card.getChildren().addAll(top, message, meta);
        return card;
    }

    private void sendNotification() {
        boolean broadcast = "All Customers".equals(recipientMode.getValue());
        String recipient = recipientField.getText();
        String type = typeField.getValue();
        String title = titleField.getText();
        String message = messageField.getText();

        if (!broadcast && (recipient == null || recipient.trim().isEmpty())) {
            showError("Customer required", "Enter the customer's login email or customer ID.");
            return;
        }

        if (title == null || title.trim().isEmpty()) {
            showError("Title required", "Enter a notification title.");
            return;
        }

        if (message == null || message.trim().isEmpty()) {
            showError("Message required", "Enter a notification message.");
            return;
        }

        String id = controller.sendNotification(recipient, title, message, type, broadcast);
        if (id == null) {
            showError("Send failed", firstNonBlank(controller.getLastError(), "Unable to send notification."));
            return;
        }

        showInfo("Notification sent", "Firebase notification created successfully.\nID: " + id);
        clearComposer();
        loadNotifications();
    }

    private void deleteNotification(Notification notification) {
        if (notification == null) {
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("RoadGuardian");
        confirm.setHeaderText("Delete notification?");
        confirm.setContentText(firstNonBlank(notification.getTitle(), "Notification"));

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        if (!controller.deleteNotification(notification.getNotificationId())) {
            showError("Delete failed", firstNonBlank(controller.getLastError(), "Unable to delete notification."));
            return;
        }

        loadNotifications();
    }

    private void clearComposer() {
        recipientMode.setValue("All Customers");
        recipientField.clear();
        recipientField.setDisable(true);
        typeField.setValue("General");
        titleField.clear();
        messageField.clear();
    }

    private Label fieldLabel(String text) {
        Label label = new Label(text);
        label.setStyle(
                "-fx-text-fill: #A1A1AA;" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;"
        );
        return label;
    }

    private Label metaLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: " + MUTED + "; -fx-font-size: 9px;");
        return label;
    }

    private Label badge(String text, String color) {
        Label label = new Label(text);
        label.setStyle(
                "-fx-text-fill: " + color + ";" +
                "-fx-background-color: " + color + "18;" +
                "-fx-border-color: " + color + "44;" +
                "-fx-border-radius: 12;" +
                "-fx-background-radius: 12;" +
                "-fx-font-size: 9px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 4 8 4 8;"
        );
        return label;
    }

    private String colorFor(Notification notification) {
        String type = firstNonBlank(notification.getType(), "General");
        if ("SOS".equalsIgnoreCase(type)) {
            return RED;
        }
        if ("Service".equalsIgnoreCase(type)) {
            return BLUE;
        }
        if ("Reminder".equalsIgnoreCase(type)) {
            return ORANGE;
        }
        if ("Safety".equalsIgnoreCase(type)) {
            return PURPLE;
        }
        return GREEN;
    }

    private Button primaryButton(String text) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: " + BLUE + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 8;" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 9 15 9 15;" +
                "-fx-cursor: hand;"
        );
        return button;
    }

    private Button secondaryButton(String text) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: " + WHITE + ";" +
                "-fx-text-fill: " + BLUE + ";" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 9 14 9 14;" +
                "-fx-cursor: hand;"
        );
        return button;
    }

    private void styleTextField(TextField field) {
        field.setPrefHeight(40);
        field.setStyle(
                "-fx-background-color: " + WHITE + ";" +
                "-fx-text-fill: " + HEADING + ";" +
                "-fx-prompt-text-fill: " + MUTED + ";" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-font-size: 11px;" +
                "-fx-padding: 0 11 0 11;"
        );
    }

    private void styleCombo(ComboBox<String> combo) {
        combo.setPrefHeight(40);
        combo.setStyle(
                "-fx-background-color: " + WHITE + ";" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-font-size: 11px;"
        );

        combo.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 11px;");
            }
        });
    }

    private VBox createErrorView(String message) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));

        Label title = new Label("Notification module unavailable");
        title.setStyle(
                "-fx-text-fill: " + RED + ";" +
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;"
        );

        Label detail = new Label(firstNonBlank(message, "Unknown error."));
        detail.setWrapText(true);
        detail.setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 11px;");

        box.getChildren().addAll(title, detail);
        return box;
    }

    private String formatDate(long millis) {
        if (millis <= 0L) {
            return "Date unavailable";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")
                .withZone(ZoneId.systemDefault());
        return formatter.format(Instant.ofEpochMilli(millis));
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
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
}
