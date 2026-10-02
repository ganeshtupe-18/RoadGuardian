package project.ui.user;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import project.controller.user.NotificationController;
import project.model.Notification;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class NotificationsPage {

    private static final String BG     = "#0F0F0F";
    private static final String CARD   = "#1A1A1A";
    private static final String WHITE  = "#242424";
    private static final String SECONDARY_SURFACE = "#242424";
    private static final String HEADING= "#F3F4F6";
    private static final String TEXT   = "#A1A1AA";
    private static final String MUTED  = "#71717A";
    private static final String BORDER = "#F59E0B";
    private static final String BLUE   = "#F59E0B";
    private static final String GREEN  = "#22C55E";
    private static final String RED    = "#EF4444";
    private static final String ORANGE = "#F59E0B";
    private static final String PURPLE = "#A78BFA";

    private final NotificationController controller;
    private final List<Notification> notifications = new ArrayList<>();

    private String selectedFilter = "All";
    private Scene notificationsScene;
    private VBox notificationContainer;
    private Label unreadLabel;
    private Label resultLabel;
    private HBox filterBar;

    public NotificationsPage() {
        controller = new NotificationController();
    }

    public Scene getNotificationsScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG + ";");

        root.setTop(UserHeader.createHeader());
        root.setLeft(UserSideBar.createSidebar("Notifications"));

        VBox content = createContent();
        ScrollPane contentScroll = new ScrollPane(content);
        contentScroll.setFitToWidth(true);
        contentScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        contentScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        contentScroll.setStyle(
                "-fx-background-color: " + BG + ";" +
                "-fx-background: " + BG + ";" +
                "-fx-border-color: transparent;"
        );

        root.setCenter(contentScroll);

        double width = 1280;
        double height = 820;

        if (UserDashboard.dashboardStage != null) {
            if (UserDashboard.dashboardStage.getWidth() > 100) {
                width = UserDashboard.dashboardStage.getWidth();
            }
            if (UserDashboard.dashboardStage.getHeight() > 100) {
                height = UserDashboard.dashboardStage.getHeight();
            }
        }

        notificationsScene = new Scene(root, width, height);
        refreshData();
        return notificationsScene;
    }

    private VBox createContent() {
        VBox content = new VBox(18);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + BG + ";");

        content.getChildren().addAll(
                createHeading(),
                createFilterBar(),
                createNotificationPanel()
        );

        return content;
    }

    private HBox createHeading() {
        HBox heading = new HBox(12);
        heading.setAlignment(Pos.CENTER_LEFT);

        VBox left = new VBox(4);

        Label title = new Label("Notifications");
        title.setStyle(
                "-fx-text-fill: " + HEADING + ";" +
                "-fx-font-size: 28px;" +
                "-fx-font-weight: bold;"
        );

        unreadLabel = new Label("Loading updates...");
        unreadLabel.setStyle(
                "-fx-text-fill: " + TEXT + ";" +
                "-fx-font-size: 13px;"
        );

        left.getChildren().addAll(title, unreadLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refresh = actionButton("Refresh", WHITE, BLUE, BLUE);
        refresh.setOnAction(event -> refreshData());

        Button markAll = actionButton("Mark all as read", BLUE, WHITE, BLUE);
        markAll.setOnAction(event -> markAllAsRead());

        heading.getChildren().addAll(left, spacer, refresh, markAll);
        return heading;
    }

    private HBox createFilterBar() {
        HBox wrapper = new HBox(12);
        wrapper.setAlignment(Pos.CENTER_LEFT);

        filterBar = new HBox(5);
        filterBar.setPadding(new Insets(4));
        filterBar.setStyle(
                "-fx-background-color: " + CARD + ";" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 22;" +
                "-fx-background-radius: 22;"
        );

        rebuildFilterButtons();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        resultLabel = new Label("0 notifications");
        resultLabel.setStyle(
                "-fx-text-fill: " + TEXT + ";" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;"
        );

        wrapper.getChildren().addAll(filterBar, spacer, resultLabel);
        return wrapper;
    }

    private void rebuildFilterButtons() {
        if (filterBar == null) {
            return;
        }

        filterBar.getChildren().clear();

        String[] filters = {"All", "Unread", "Service", "SOS", "Admin", "Reminders"};
        for (String filter : filters) {
            Button button = filterButton(filter, filter.equals(selectedFilter));
            button.setOnAction(event -> {
                selectedFilter = filter;
                rebuildFilterButtons();
                renderNotifications();
            });
            filterBar.getChildren().add(button);
        }
    }

    private Button filterButton(String text, boolean active) {
        Button button = new Button(text);
        button.setPadding(new Insets(7, 14, 7, 14));
        button.setStyle(
                active
                        ? "-fx-background-color: " + WHITE + ";" +
                          "-fx-text-fill: " + HEADING + ";" +
                          "-fx-border-color: " + BORDER + ";" +
                          "-fx-border-radius: 18;" +
                          "-fx-background-radius: 18;" +
                          "-fx-font-size: 12px;" +
                          "-fx-font-weight: bold;" +
                          "-fx-cursor: hand;"
                        : "-fx-background-color: transparent;" +
                          "-fx-text-fill: " + TEXT + ";" +
                          "-fx-font-size: 12px;" +
                          "-fx-font-weight: bold;" +
                          "-fx-cursor: hand;"
        );
        return button;
    }

    private VBox createNotificationPanel() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(18));
        panel.setStyle(
                "-fx-background-color: " + CARD + ";" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 14;" +
                "-fx-background-radius: 14;"
        );

        Label heading = new Label("Live RoadGuardian Updates");
        heading.setStyle(
                "-fx-text-fill: " + HEADING + ";" +
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;"
        );

        Label note = new Label(
                "Service Request, SOS and Admin notifications are loaded from Firebase."
        );
        note.setStyle(
                "-fx-text-fill: " + TEXT + ";" +
                "-fx-font-size: 11px;"
        );

        notificationContainer = new VBox(10);
        notificationContainer.setFillWidth(true);

        panel.getChildren().addAll(heading, note, notificationContainer);
        return panel;
    }

    private void refreshData() {
        if (!controller.isLoggedIn()) {
            notifications.clear();
            updateHeaderCounts();
            renderLoginRequired();
            return;
        }

        List<Notification> loaded = controller.getNotifications();
        notifications.clear();
        if (loaded != null) {
            notifications.addAll(loaded);
        }

        updateHeaderCounts();
        renderNotifications();

        if (controller.getLastError() != null && notifications.isEmpty()) {
            showError("Unable to load notifications", controller.getLastError());
        }
    }

    private void renderNotifications() {
        if (notificationContainer == null) {
            return;
        }

        notificationContainer.getChildren().clear();
        int visible = 0;

        for (Notification notification : notifications) {
            if (notification == null || !matchesFilter(notification)) {
                continue;
            }
            notificationContainer.getChildren().add(createNotificationCard(notification));
            visible++;
        }

        if (resultLabel != null) {
            resultLabel.setText(visible == 1 ? "1 notification" : visible + " notifications");
        }

        if (visible == 0) {
            VBox empty = new VBox(6);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(34));

            Label title = new Label("No notifications found");
            title.setStyle(
                    "-fx-text-fill: " + HEADING + ";" +
                    "-fx-font-size: 14px;" +
                    "-fx-font-weight: bold;"
            );

            Label subtitle = new Label(
                    "Updates will appear here when your Service Request, SOS or Admin notification changes."
            );
            subtitle.setWrapText(true);
            subtitle.setStyle(
                    "-fx-text-fill: " + TEXT + ";" +
                    "-fx-font-size: 11px;"
            );

            empty.getChildren().addAll(title, subtitle);
            notificationContainer.getChildren().add(empty);
        }
    }

    private HBox createNotificationCard(Notification notification) {
        HBox card = new HBox(14);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(15, 17, 15, 17));

        String background = notification.isRead() ? CARD : SECONDARY_SURFACE;
        String border = notification.isRead() ? BORDER : BLUE;

        card.setStyle(
                "-fx-background-color: " + background + ";" +
                "-fx-border-color: " + border + ";" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 11;" +
                "-fx-background-radius: 11;"
        );

        Label icon = new Label(iconFor(notification));
        icon.setAlignment(Pos.CENTER);
        icon.setMinSize(42, 42);
        icon.setMaxSize(42, 42);
        icon.setStyle(
                "-fx-background-color: " + colorFor(notification) + "18;" +
                "-fx-text-fill: " + colorFor(notification) + ";" +
                "-fx-background-radius: 21;" +
                "-fx-font-size: 17px;" +
                "-fx-font-weight: bold;"
        );

        VBox body = new VBox(5);
        HBox titleRow = new HBox(7);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(firstNonBlank(notification.getTitle(), "RoadGuardian Update"));
        title.setStyle(
                "-fx-text-fill: " + HEADING + ";" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;"
        );

        titleRow.getChildren().add(title);

        if (!notification.isRead()) {
            Label dot = new Label("●");
            dot.setStyle("-fx-text-fill: " + RED + "; -fx-font-size: 9px;");
            titleRow.getChildren().add(dot);
        }

        Label message = new Label(firstNonBlank(notification.getMessage(), "No details available."));
        message.setWrapText(true);
        message.setStyle(
                "-fx-text-fill: " + TEXT + ";" +
                "-fx-font-size: 12px;"
        );

        HBox meta = new HBox(10);
        meta.setAlignment(Pos.CENTER_LEFT);

        Label type = badge(firstNonBlank(notification.getType(), "General"), colorFor(notification));

        Label source = new Label("Source: " + firstNonBlank(notification.getSource(), "RoadGuardian"));
        source.setStyle(
                "-fx-text-fill: " + MUTED + ";" +
                "-fx-font-size: 10px;"
        );

        if (notification.getRelatedId() != null && !notification.getRelatedId().isBlank()) {
            Label related = new Label("ID: " + notification.getRelatedId());
            related.setStyle(
                    "-fx-text-fill: " + MUTED + ";" +
                    "-fx-font-size: 10px;"
            );
            meta.getChildren().addAll(type, source, related);
        } else {
            meta.getChildren().addAll(type, source);
        }

        body.getChildren().addAll(titleRow, message, meta);
        HBox.setHgrow(body, Priority.ALWAYS);

        VBox right = new VBox(7);
        right.setAlignment(Pos.CENTER_RIGHT);

        Label time = new Label(formatTime(notification.getCreatedAt()));
        time.setStyle(
                "-fx-text-fill: " + MUTED + ";" +
                "-fx-font-size: 10px;"
        );

        right.getChildren().add(time);

        if (!notification.isRead()) {
            Button markRead = actionButton("Mark read", WHITE, BLUE, BLUE);
            markRead.setStyle(markRead.getStyle() + "-fx-font-size: 10px; -fx-padding: 6 10 6 10;");
            markRead.setOnAction(event -> {
                if (controller.markRead(notification)) {
                    updateHeaderCounts();
                    renderNotifications();
                } else {
                    showError("Unable to update notification", controller.getLastError());
                }
            });
            right.getChildren().add(markRead);
        }

        card.getChildren().addAll(icon, body, right);
        return card;
    }

    private void markAllAsRead() {
        if (notifications.isEmpty()) {
            return;
        }

        if (controller.markAllRead(notifications)) {
            updateHeaderCounts();
            renderNotifications();
        } else {
            showError("Unable to mark notifications as read", controller.getLastError());
        }
    }

    private void updateHeaderCounts() {
        if (unreadLabel == null) {
            return;
        }

        int unread = controller.unreadCount(notifications);
        unreadLabel.setText(
                unread == 0
                        ? "You're all caught up"
                        : unread == 1
                        ? "1 unread update"
                        : unread + " unread updates"
        );
    }

    private boolean matchesFilter(Notification notification) {
        if ("All".equals(selectedFilter)) {
            return true;
        }
        if ("Unread".equals(selectedFilter)) {
            return !notification.isRead();
        }
        if ("Reminders".equals(selectedFilter)) {
            return notification.isReminder()
                    || "Reminder".equalsIgnoreCase(notification.getType());
        }
        if ("Admin".equals(selectedFilter)) {
            return "Admin".equalsIgnoreCase(notification.getSource())
                    || notification.getNotificationId().startsWith("admin_");
        }
        return selectedFilter.equalsIgnoreCase(notification.getType());
    }

    private void renderLoginRequired() {
        if (notificationContainer == null) {
            return;
        }

        notificationContainer.getChildren().clear();

        VBox box = new VBox(7);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(35));

        Label title = new Label("Login required");
        title.setStyle(
                "-fx-text-fill: " + HEADING + ";" +
                "-fx-font-size: 15px;" +
                "-fx-font-weight: bold;"
        );

        Label text = new Label("Please login again to view your RoadGuardian notifications.");
        text.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 11px;");

        box.getChildren().addAll(title, text);
        notificationContainer.getChildren().add(box);
    }

    private Button actionButton(String text, String background, String textColor, String borderColor) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: " + background + ";" +
                "-fx-text-fill: " + textColor + ";" +
                "-fx-border-color: " + borderColor + ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 9 14 9 14;" +
                "-fx-cursor: hand;"
        );
        return button;
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
        if ("Reminder".equalsIgnoreCase(type) || notification.isReminder()) {
            return ORANGE;
        }
        if ("Safety".equalsIgnoreCase(type)) {
            return PURPLE;
        }
        if ("Admin".equalsIgnoreCase(notification.getSource())) {
            return GREEN;
        }
        return BLUE;
    }

    private String iconFor(Notification notification) {
        String type = firstNonBlank(notification.getType(), "General");
        if ("SOS".equalsIgnoreCase(type)) {
            return "!";
        }
        if ("Service".equalsIgnoreCase(type)) {
            return "S";
        }
        if ("Reminder".equalsIgnoreCase(type) || notification.isReminder()) {
            return "R";
        }
        if ("Safety".equalsIgnoreCase(type)) {
            return "W";
        }
        if ("Admin".equalsIgnoreCase(notification.getSource())) {
            return "A";
        }
        return "N";
    }

    private String formatTime(long millis) {
        if (millis <= 0L) {
            return "Date unavailable";
        }

        long diff = Math.max(0L, System.currentTimeMillis() - millis);
        long minute = 60_000L;
        long hour = 60 * minute;
        long day = 24 * hour;

        if (diff < minute) {
            return "Just now";
        }
        if (diff < hour) {
            long value = diff / minute;
            return value + (value == 1 ? " min ago" : " mins ago");
        }
        if (diff < day) {
            long value = diff / hour;
            return value + (value == 1 ? " hr ago" : " hrs ago");
        }
        if (diff < 7 * day) {
            long value = diff / day;
            return value + (value == 1 ? " day ago" : " days ago");
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

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("RoadGuardian");
        alert.setHeaderText(title);
        alert.setContentText(firstNonBlank(message, "Unknown error."));
        alert.showAndWait();
    }
}
