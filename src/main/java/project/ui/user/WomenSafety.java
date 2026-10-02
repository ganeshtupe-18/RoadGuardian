package project.ui.user;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import project.dao.user.WomenSafetyDAO;
import project.model.SOSRequest;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Customer Women Safety page.
 *
 * The SOS created here is the SAME canonical SOSRequests document used by
 * Customer SOS, Admin SOS and Mechanic SOS.
 */
public class WomenSafety {

    private static final String BG     = "#0F0F0F";
    private static final String CARD   = "#1A1A1A";
    private static final String WHITE  = "#242424";
    private static final String BORDER = "#F59E0B";
    private static final String HEADING= "#F3F4F6";
    private static final String TEXT   = "#A1A1AA";
    private static final String BLUE   = "#F59E0B";
    private static final String GREEN  = "#22C55E";
    private static final String RED    = "#EF4444";
    private static final String ORANGE = "#F59E0B";

    private Scene womenSafetyScene;
    private final WomenSafetyDAO womenSafetyDAO;
    private final String userEmail;

    private Label safetyStatusLabel;
    private Label sosStatusLabel;
    private Label sosIdLabel;
    private Button sosButton;
    private Button cancelButton;

    private String activeSOSId;

    // ============================================================
    // CONSTRUCTORS
    // ============================================================

    /**
     * IMPORTANT FIX:
     * AppNavigator opens WomenSafety with the no-arg constructor.
     * The old code passed an empty email, so the page always behaved as if
     * nobody was logged in. Now it uses the actual logged-in UserSession.
     */
    public WomenSafety() throws IOException {
        this(UserSession.getUserEmail());
    }

    public WomenSafety(String userEmail) throws IOException {
        this.userEmail = normalizeEmail(userEmail);
        this.womenSafetyDAO = new WomenSafetyDAO();
    }

    // ============================================================
    // PUBLIC SCENE
    // ============================================================

    public Scene getWomenSafetyScene() {

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG + ";");

        root.setTop(UserHeader.createHeader());
        root.setLeft(UserSideBar.createSidebar("Women Safety"));

        VBox content = createContent();

        ScrollPane contentScroll = new ScrollPane(content);
        contentScroll.setFitToWidth(true);
        contentScroll.setPannable(true);
        contentScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        contentScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        contentScroll.setStyle(
                "-fx-background-color: " + BG + ";" +
                "-fx-background: " + BG + ";" +
                "-fx-border-color: transparent;"
        );

        root.setCenter(contentScroll);

        double width = 1280;
        double height = 760;

        if (UserDashboard.dashboardStage != null) {
            if (UserDashboard.dashboardStage.getWidth() > 0) {
                width = UserDashboard.dashboardStage.getWidth();
            }
            if (UserDashboard.dashboardStage.getHeight() > 0) {
                height = UserDashboard.dashboardStage.getHeight();
            }
        }

        womenSafetyScene = new Scene(root, width, height);

        loadCurrentState();

        return womenSafetyScene;
    }

    // ============================================================
    // CONTENT
    // ============================================================

    private VBox createContent() {

        VBox content = new VBox(20);
        content.setPadding(new Insets(30, 35, 35, 35));
        content.setStyle("-fx-background-color: " + BG + ";");

        content.getChildren().addAll(
                createHeading(),
                createSafetyCard(),
                createLowerSection()
        );

        return content;
    }

    // ============================================================
    // HEADING
    // ============================================================

    private HBox createHeading() {

        HBox heading = new HBox();
        heading.setAlignment(Pos.CENTER_LEFT);

        VBox text = new VBox(4);

        Label title = new Label("Women Safety");
        title.setStyle(
                "-fx-font-size: 28px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + HEADING + ";"
        );

        Label subtitle = new Label(
                "Create a priority emergency request that is visible to Admin and the assigned Mechanic."
        );
        subtitle.setWrapText(true);
        subtitle.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-text-fill: " + TEXT + ";"
        );

        text.getChildren().addAll(title, subtitle);
        heading.getChildren().add(text);

        return heading;
    }

    // ============================================================
    // MAIN SAFETY CARD
    // ============================================================

    private HBox createSafetyCard() {

        HBox card = new HBox(24);
        card.setPadding(new Insets(24));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(cardStyle());

        VBox left = new VBox(11);
        HBox.setHgrow(left, Priority.ALWAYS);

        Label modeTitle = new Label("Women Safety Mode");
        modeTitle.setStyle(
                "-fx-font-size: 20px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + HEADING + ";"
        );

        Label description = new Label(
                "The emergency request follows the same SOS lifecycle used across RoadGuardian."
        );
        description.setWrapText(true);
        description.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: " + TEXT + ";"
        );

        safetyStatusLabel = createBadge("Checking safety mode...", TEXT, "#EEF2F7");

        VBox lifecycle = createLifecycleInfo();

        left.getChildren().addAll(
                modeTitle,
                description,
                safetyStatusLabel,
                lifecycle
        );

        VBox right = new VBox(11);
        right.setPrefWidth(320);
        right.setPadding(new Insets(20));
        right.setStyle(
                "-fx-background-color: " + WHITE + ";" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 16;" +
                "-fx-background-radius: 16;"
        );

        Label sosTitle = new Label("Emergency SOS");
        sosTitle.setStyle(
                "-fx-font-size: 17px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + HEADING + ";"
        );

        sosStatusLabel = new Label("Checking active request...");
        sosStatusLabel.setWrapText(true);
        sosStatusLabel.setStyle(
                "-fx-font-size: 11px;" +
                "-fx-text-fill: " + TEXT + ";"
        );

        sosIdLabel = new Label("");
        sosIdLabel.setWrapText(true);
        sosIdLabel.setStyle(
                "-fx-font-size: 10px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + HEADING + ";"
        );

        sosButton = new Button("Activate Women Safety SOS");
        sosButton.setMaxWidth(Double.MAX_VALUE);
        sosButton.setPrefHeight(42);
        stylePrimaryButton(sosButton);
        sosButton.setOnAction(event -> handleEmergencySOS());

        cancelButton = new Button("Cancel Active SOS");
        cancelButton.setMaxWidth(Double.MAX_VALUE);
        cancelButton.setPrefHeight(38);
        styleDangerOutlineButton(cancelButton);
        cancelButton.setDisable(true);
        cancelButton.setVisible(false);
        cancelButton.setManaged(false);
        cancelButton.setOnAction(event -> handleCancelSOS());

        Button refreshButton = new Button("Refresh Status");
        refreshButton.setMaxWidth(Double.MAX_VALUE);
        refreshButton.setPrefHeight(36);
        styleSecondaryButton(refreshButton);
        refreshButton.setOnAction(event -> loadCurrentState());

        right.getChildren().addAll(
                sosTitle,
                sosStatusLabel,
                sosIdLabel,
                sosButton,
                cancelButton,
                refreshButton
        );

        card.getChildren().addAll(left, right);
        return card;
    }

    private VBox createLifecycleInfo() {

        VBox box = new VBox(6);
        box.setPadding(new Insets(11));
        box.setStyle(
                "-fx-background-color: #1A1A1A;" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;"
        );

        Label title = new Label("SOS Lifecycle");
        title.setStyle(
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + HEADING + ";"
        );

        Label flow = new Label(
                "Customer → Pending → Admin Assign → Assigned → Mechanic Accept → Accepted → In Progress → Resolved"
        );
        flow.setWrapText(true);
        flow.setStyle(
                "-fx-font-size: 10px;" +
                "-fx-text-fill: " + TEXT + ";"
        );

        box.getChildren().addAll(title, flow);
        return box;
    }

    // ============================================================
    // CURRENT FIREBASE STATE
    // ============================================================

    private void loadCurrentState() {

        if (safetyStatusLabel == null || sosStatusLabel == null || sosButton == null) {
            return;
        }

        if (userEmail.isEmpty()) {
            safetyStatusLabel.setText("Login required");
            safetyStatusLabel.setStyle(badgeStyle(ORANGE, "#FFF4E5"));
            sosStatusLabel.setText("Please login before using Women Safety SOS.");
            sosButton.setDisable(true);
            setCancelVisible(false);
            return;
        }

        try {
            boolean enabled = womenSafetyDAO.isSafetyModeEnabled(userEmail);

            safetyStatusLabel.setText(
                    enabled ? "Safety mode is ON" : "Safety mode is ready"
            );
            safetyStatusLabel.setStyle(
                    enabled
                            ? badgeStyle(GREEN, "#E8F7EF")
                            : badgeStyle(BLUE, "#EAF2FF")
            );

            SOSRequest active = womenSafetyDAO.getActiveSOSRequest(userEmail);

            if (active == null) {
                activeSOSId = null;
                sosStatusLabel.setText("No active SOS request.");
                sosIdLabel.setText("");
                sosButton.setText("Activate Women Safety SOS");
                sosButton.setDisable(false);
                setCancelVisible(false);
                return;
            }

            activeSOSId = clean(active.getSosId());

            String type = firstNonBlank(
                    active.getEmergencyType(),
                    "Emergency SOS"
            );

            String status = firstNonBlank(
                    active.getStatus(),
                    "Pending"
            );

            sosStatusLabel.setText(type + " is active • Status: " + status);
            sosIdLabel.setText(
                    activeSOSId == null ? "" : "Request ID: " + activeSOSId
            );
            sosButton.setText("SOS Already Active");
            sosButton.setDisable(true);

            boolean finalStatus = isFinalStatus(status);
            setCancelVisible(!finalStatus);

        } catch (Exception exception) {
            exception.printStackTrace();
            safetyStatusLabel.setText("Unable to load safety status");
            safetyStatusLabel.setStyle(badgeStyle(RED, "#FDECEC"));
            sosStatusLabel.setText("Firebase status could not be loaded.");
        }
    }

    // ============================================================
    // CREATE SOS
    // ============================================================

    private void handleEmergencySOS() {

        if (userEmail.isEmpty()) {
            showAlert(
                    Alert.AlertType.WARNING,
                    "Login Required",
                    "Please login before activating Women Safety SOS."
            );
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("RoadGuardian");
        confirmation.setHeaderText("Activate Women Safety SOS?");
        confirmation.setContentText(
                "This creates one real SOS request in SOSRequests. " +
                "Admin will see it as Pending and can assign a mechanic."
        );

        if (sosButton.getScene() != null) {
            confirmation.initOwner(sosButton.getScene().getWindow());
        }

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            sosButton.setDisable(true);
            sosStatusLabel.setText("Creating emergency request...");

            String sosId = womenSafetyDAO.createSOSRequest(userEmail);
            activeSOSId = sosId;

            showAlert(
                    Alert.AlertType.INFORMATION,
                    "SOS Activated",
                    "Women Safety SOS created successfully.\n\n" +
                    "Request ID: " + sosId + "\n" +
                    "Status: Pending\n\n" +
                    "The same request is now available to Admin."
            );

            loadCurrentState();

        } catch (Exception exception) {
            exception.printStackTrace();
            sosButton.setDisable(false);
            sosStatusLabel.setText("Unable to activate SOS.");
            showAlert(
                    Alert.AlertType.ERROR,
                    "SOS Failed",
                    firstNonBlank(exception.getMessage(), "Unable to activate Women Safety SOS.")
            );
        }
    }

    // ============================================================
    // CANCEL ACTIVE SOS
    // ============================================================

    private void handleCancelSOS() {

        if (activeSOSId == null) {
            loadCurrentState();
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("RoadGuardian");
        confirmation.setHeaderText("Cancel active SOS?");
        confirmation.setContentText(
                "Request ID: " + activeSOSId + "\n\n" +
                "The status will become Cancelled for Customer, Admin and Mechanic."
        );

        if (cancelButton.getScene() != null) {
            confirmation.initOwner(cancelButton.getScene().getWindow());
        }

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            boolean cancelled = womenSafetyDAO.cancelSOS(activeSOSId);

            if (!cancelled) {
                showAlert(
                        Alert.AlertType.WARNING,
                        "Cannot Cancel",
                        "This SOS may already be resolved or cancelled."
                );
            } else {
                showAlert(
                        Alert.AlertType.INFORMATION,
                        "SOS Cancelled",
                        "The SOS request has been cancelled."
                );
            }

            loadCurrentState();

        } catch (Exception exception) {
            exception.printStackTrace();
            showAlert(
                    Alert.AlertType.ERROR,
                    "Cancel Failed",
                    firstNonBlank(exception.getMessage(), "Unable to cancel SOS.")
            );
        }
    }

    // ============================================================
    // LOWER SECTION
    // ============================================================

    private HBox createLowerSection() {

        HBox section = new HBox(18);

        VBox contacts = createEmergencyContacts();
        VBox features = createSafetyFeatures();

        HBox.setHgrow(contacts, Priority.ALWAYS);
        HBox.setHgrow(features, Priority.ALWAYS);

        section.getChildren().addAll(contacts, features);
        return section;
    }

    // ============================================================
    // EMERGENCY CONTACTS
    // ============================================================

    private VBox createEmergencyContacts() {

        VBox card = new VBox(13);
        card.setPadding(new Insets(20));
        card.setStyle(cardStyle());

        Label title = sectionTitle("Emergency Contacts");
        Label subtitle = sectionSubtitle(
                "Contacts saved in your profile. Actions are recorded in Firebase."
        );

        card.getChildren().addAll(title, subtitle);

        if (userEmail.isEmpty()) {
            card.getChildren().add(createInfoRow("Login required to load contacts."));
            return card;
        }

        try {
            List<Map<String, Object>> contacts = womenSafetyDAO.getEmergencyContacts(userEmail);

            if (contacts == null || contacts.isEmpty()) {
                card.getChildren().add(createInfoRow("No emergency contacts added."));
                return card;
            }

            for (Map<String, Object> contact : contacts) {
                String name = getValue(contact, "name", "Emergency Contact");
                String relation = firstNonBlank(
                        getNullableValue(contact, "relation"),
                        getNullableValue(contact, "description"),
                        getNullableValue(contact, "phone"),
                        "Trusted contact"
                );
                String action = getValue(contact, "action", "Record Contact");

                card.getChildren().add(createContactRow(name, relation, action));
            }

        } catch (Exception exception) {
            exception.printStackTrace();
            card.getChildren().add(createInfoRow("Unable to load emergency contacts."));
        }

        return card;
    }

    private HBox createContactRow(
            String name,
            String description,
            String action
    ) {

        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(11));
        row.setStyle(
                "-fx-background-color: #1A1A1A;" +
                "-fx-border-color: #E1E8EE;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;"
        );

        VBox text = new VBox(3);
        HBox.setHgrow(text, Priority.ALWAYS);

        Label nameLabel = new Label(firstNonBlank(name, "Emergency Contact"));
        nameLabel.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + HEADING + ";"
        );

        Label descriptionLabel = new Label(firstNonBlank(description, "Trusted contact"));
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle(
                "-fx-font-size: 10px;" +
                "-fx-text-fill: " + TEXT + ";"
        );

        text.getChildren().addAll(nameLabel, descriptionLabel);

        Button actionButton = new Button(firstNonBlank(action, "Record Contact"));
        styleSecondaryButton(actionButton);
        actionButton.setOnAction(event -> handleContactAction(name, action, actionButton));

        row.getChildren().addAll(text, actionButton);
        return row;
    }

    private HBox createInfoRow(String message) {
        HBox row = new HBox();
        row.setPadding(new Insets(12));
        row.setStyle(
                "-fx-background-color: #1A1A1A;" +
                "-fx-border-color: #E1E8EE;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;"
        );

        Label label = new Label(message);
        label.setWrapText(true);
        label.setStyle(
                "-fx-font-size: 11px;" +
                "-fx-text-fill: " + TEXT + ";"
        );

        row.getChildren().add(label);
        return row;
    }

    private void handleContactAction(
            String contactName,
            String action,
            Button button
    ) {

        if (userEmail.isEmpty()) {
            showAlert(
                    Alert.AlertType.WARNING,
                    "Login Required",
                    "Please login before recording a contact action."
            );
            return;
        }

        try {
            womenSafetyDAO.logContactAction(
                    userEmail,
                    contactName,
                    firstNonBlank(action, "Contact")
            );

            button.setText("Recorded");
            button.setDisable(true);

            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Action Recorded",
                    "The contact action for " + contactName + " was recorded in RoadGuardian."
            );

        } catch (Exception exception) {
            exception.printStackTrace();
            showAlert(
                    Alert.AlertType.ERROR,
                    "Action Failed",
                    firstNonBlank(exception.getMessage(), "Unable to record contact action.")
            );
        }
    }

    // ============================================================
    // REAL IMPLEMENTED SAFETY FEATURES
    // ============================================================

    private VBox createSafetyFeatures() {

        VBox card = new VBox(13);
        card.setPadding(new Insets(20));
        card.setStyle(cardStyle());

        card.getChildren().addAll(
                sectionTitle("RoadGuardian Safety Flow"),
                sectionSubtitle("Only implemented data-flow features are shown here."),
                feature("Canonical SOS", "Women Safety writes to SOSRequests, not a separate legacy collection."),
                feature("Admin Visibility", "A new Women Safety SOS appears to Admin with status Pending."),
                feature("Mechanic Response", "After Admin assignment, the selected mechanic can Accept, Start and Resolve the same request."),
                feature("Customer Status", "Reopening this page reads the current status from the same Firebase SOS document.")
        );

        return card;
    }

    private HBox feature(String title, String description) {

        HBox row = new HBox(10);
        row.setAlignment(Pos.TOP_LEFT);

        Label icon = new Label("✓");
        icon.setMinSize(28, 28);
        icon.setAlignment(Pos.CENTER);
        icon.setStyle(
                "-fx-background-color: #E8F7EF;" +
                "-fx-background-radius: 20;" +
                "-fx-text-fill: " + GREEN + ";" +
                "-fx-font-weight: bold;"
        );

        VBox text = new VBox(3);

        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + HEADING + ";"
        );

        Label descriptionLabel = new Label(description);
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle(
                "-fx-font-size: 10px;" +
                "-fx-text-fill: " + TEXT + ";"
        );

        text.getChildren().addAll(titleLabel, descriptionLabel);
        row.getChildren().addAll(icon, text);
        return row;
    }

    // ============================================================
    // UI HELPERS
    // ============================================================

    private Label sectionTitle(String text) {
        Label label = new Label(text);
        label.setStyle(
                "-fx-font-size: 17px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + HEADING + ";"
        );
        return label;
    }

    private Label sectionSubtitle(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setStyle(
                "-fx-font-size: 10px;" +
                "-fx-text-fill: " + TEXT + ";"
        );
        return label;
    }

    private Label createBadge(String text, String color, String background) {
        Label label = new Label(text);
        label.setPadding(new Insets(6, 11, 6, 11));
        label.setStyle(badgeStyle(color, background));
        return label;
    }

    private String badgeStyle(String color, String background) {
        return "-fx-background-color: " + background + ";" +
                "-fx-background-radius: 20;" +
                "-fx-text-fill: " + color + ";" +
                "-fx-font-size: 10px;" +
                "-fx-font-weight: bold;";
    }

    private void stylePrimaryButton(Button button) {
        button.setStyle(
                "-fx-background-color: " + BLUE + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 9;" +
                "-fx-cursor: hand;"
        );
    }

    private void styleSecondaryButton(Button button) {
        button.setStyle(
                "-fx-background-color: white;" +
                "-fx-text-fill: " + HEADING + ";" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-font-size: 10px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 8 12 8 12;" +
                "-fx-cursor: hand;"
        );
    }

    private void styleDangerOutlineButton(Button button) {
        button.setStyle(
                "-fx-background-color: white;" +
                "-fx-text-fill: " + RED + ";" +
                "-fx-border-color: " + RED + ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-font-size: 10px;" +
                "-fx-font-weight: bold;" +
                "-fx-cursor: hand;"
        );
    }

    private void setCancelVisible(boolean visible) {
        if (cancelButton == null) {
            return;
        }
        cancelButton.setVisible(visible);
        cancelButton.setManaged(visible);
        cancelButton.setDisable(!visible);
    }

    private String cardStyle() {
        return "-fx-background-color: " + CARD + ";" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 18;" +
                "-fx-background-radius: 18;";
    }

    // ============================================================
    // DATA HELPERS
    // ============================================================

    private boolean isFinalStatus(String status) {
        String value = clean(status);
        return value != null && (
                value.equalsIgnoreCase("Resolved") ||
                value.equalsIgnoreCase("Completed") ||
                value.equalsIgnoreCase("Cancelled") ||
                value.equalsIgnoreCase("Canceled")
        );
    }

    private String getValue(
            Map<String, Object> data,
            String key,
            String defaultValue
    ) {
        String value = getNullableValue(data, key);
        return value == null ? defaultValue : value;
    }

    private String getNullableValue(
            Map<String, Object> data,
            String key
    ) {
        if (data == null || key == null) {
            return null;
        }
        Object value = data.get(key);
        return value == null ? null : clean(String.valueOf(value));
    }

    private String normalizeEmail(String value) {
        String cleaned = clean(value);
        return cleaned == null ? "" : cleaned.toLowerCase();
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }

        for (String value : values) {
            String cleaned = clean(value);
            if (cleaned != null) {
                return cleaned;
            }
        }

        return null;
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    // ============================================================
    // ALERT
    // ============================================================

    private void showAlert(
            Alert.AlertType type,
            String title,
            String message
    ) {
        Alert alert = new Alert(type);
        alert.setTitle("RoadGuardian");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
