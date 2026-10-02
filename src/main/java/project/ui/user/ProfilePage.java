package project.ui.user;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import project.controller.user.ProfileController;

import java.util.Map;

public class ProfilePage {

    // ============================================================
    // COLORS
    // ============================================================

    private static final String BACKGROUND    = "#0F0F0F";
    private static final String CARD          = "#1A1A1A";

    private static final String HEADING       = "#F3F4F6";
    private static final String SECONDARY_TEXT= "#A1A1AA";

    private static final String NAV_BLUE = "#F59E0B";
    private static final String BORDER   = "#F59E0B";

    private static final String SUCCESS = "#22C55E";
    private static final String ERROR   = "#EF4444";

    // ============================================================
    // CONTROLLER
    // ============================================================

    private final ProfileController controller;

    // ============================================================
    // USER EMAIL
    // ============================================================

    private String userEmail;

    // ============================================================
    // UI FIELDS
    // ============================================================

    private TextField nameField;
    private TextField emailField;
    private TextField phoneField;
    private TextField roleField;

    private Label statusLabel;

    private Scene profileScene;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public ProfilePage() {

        controller = new ProfileController();

        userEmail = UserSession.getUserEmail();

        if (userEmail == null) {
            userEmail = "";
        }

        userEmail = userEmail.trim().toLowerCase();

        loadProfile();
    }

    // ============================================================
    // LOAD PROFILE
    // ============================================================

    private void loadProfile() {

        Map<String, Object> profile =
                controller.getProfile(userEmail);

        buildScene(profile);
    }

    // ============================================================
    // BUILD SCENE
    // ============================================================

    private void buildScene(
            Map<String, Object> profile
    ) {

        BorderPane root = new BorderPane();

        root.setStyle(
                "-fx-background-color: "
                        + BACKGROUND
                        + ";"
        );

        // ========================================================
        // HEADER
        // ========================================================

        try {

            root.setTop(
                    UserHeader.createHeader()
            );

        } catch (Exception e) {

            root.setTop(
                    createFallbackHeader()
            );
        }

        // ========================================================
        // SIDEBAR
        // ========================================================

        root.setLeft(
                UserSideBar.createSidebar("Profile")
        );

        // ========================================================
        // SCROLL PANE
        // ========================================================

        ScrollPane scrollPane =
                new ScrollPane();

        scrollPane.setFitToWidth(true);

        scrollPane.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        scrollPane.setStyle(
                "-fx-background-color: "
                        + BACKGROUND
                        + ";"
                        + "-fx-background: "
                        + BACKGROUND
                        + ";"
                        + "-fx-border-color: transparent;"
        );

        VBox content =
                createContent(profile);

        scrollPane.setContent(content);

        root.setCenter(scrollPane);

        // ========================================================
        // SCENE SIZE
        // ========================================================

        double width = 1200;
        double height = 700;

        try {

            if (UserDashboard.dashboardStage != null) {

                width =
                        UserDashboard.dashboardStage.getWidth();

                height =
                        UserDashboard.dashboardStage.getHeight();

                if (width <= 0) {
                    width = 1200;
                }

                if (height <= 0) {
                    height = 700;
                }
            }

        } catch (Exception ignored) {
        }

        profileScene =
                new Scene(
                        root,
                        width,
                        height
                );
    }

    // ============================================================
    // CREATE CONTENT
    // ============================================================

    private VBox createContent(
            Map<String, Object> profile
    ) {

        VBox content =
                new VBox();

        content.setPadding(
                new Insets(
                        35,
                        45,
                        45,
                        45
                )
        );

        content.setSpacing(25);

        // ========================================================
        // TITLE
        // ========================================================

        Label title =
                new Label("My Profile");

        title.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        30
                )
        );

        title.setTextFill(
                Color.web(HEADING)
        );

        Label subtitle =
                new Label(
                        "Manage your RoadGuardian account information."
                );

        subtitle.setFont(
                Font.font(
                        "Arial",
                        15
                )
        );

        subtitle.setTextFill(
                Color.web(SECONDARY_TEXT)
        );

        VBox titleBox =
                new VBox(
                        6,
                        title,
                        subtitle
                );

        // ========================================================
        // PROFILE CARD
        // ========================================================

        VBox card =
                new VBox();

        card.setPadding(
                new Insets(30)
        );

        card.setSpacing(18);

        card.setMaxWidth(800);

        card.setStyle(
                "-fx-background-color: "
                        + CARD
                        + ";"
                        + "-fx-background-radius: 18;"
                        + "-fx-border-color: "
                        + BORDER
                        + ";"
                        + "-fx-border-radius: 18;"
        );

        // ========================================================
        // USER NAME
        // ========================================================

        String name =
                getString(
                        profile,
                        "name",
                        UserSession.getUserName()
                );

        if (name == null ||
                name.trim().isEmpty()) {

            name = "User";
        }

        // ========================================================
        // AVATAR
        // ========================================================

        String initial =
                name.trim()
                        .substring(0, 1)
                        .toUpperCase();

        Label avatar =
                new Label(initial);

        avatar.setAlignment(
                Pos.CENTER
        );

        avatar.setPrefSize(
                70,
                70
        );

        avatar.setMinSize(
                70,
                70
        );

        avatar.setMaxSize(
                70,
                70
        );

        avatar.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        27
                )
        );

        avatar.setTextFill(
                Color.WHITE
        );

        avatar.setStyle(
                "-fx-background-color: "
                        + NAV_BLUE
                        + ";"
                        + "-fx-background-radius: 35;"
        );

        // ========================================================
        // DISPLAY NAME
        // ========================================================

        Label displayName =
                new Label(name);

        displayName.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        21
                )
        );

        displayName.setTextFill(
                Color.web(HEADING)
        );

        // ========================================================
        // DISPLAY EMAIL
        // ========================================================

        Label displayEmail =
                new Label(userEmail);

        displayEmail.setFont(
                Font.font(
                        "Arial",
                        14
                )
        );

        displayEmail.setTextFill(
                Color.web(SECONDARY_TEXT)
        );

        VBox identity =
                new VBox(
                        5,
                        displayName,
                        displayEmail
                );

        HBox profileHeader =
                new HBox(
                        18,
                        avatar,
                        identity
                );

        profileHeader.setAlignment(
                Pos.CENTER_LEFT
        );

        card.getChildren().add(
                profileHeader
        );

        // ========================================================
        // SEPARATOR
        // ========================================================

        Region separator =
                new Region();

        separator.setPrefHeight(1);

        separator.setMaxWidth(
                Double.MAX_VALUE
        );

        separator.setStyle(
                "-fx-background-color: "
                        + BORDER
                        + ";"
        );

        card.getChildren().add(
                separator
        );

        // ========================================================
        // NAME
        // ========================================================

        card.getChildren().add(
                createFieldLabel("Full Name")
        );

        nameField =
                createTextField(
                        getString(
                                profile,
                                "name",
                                UserSession.getUserName()
                        )
                );

        card.getChildren().add(
                nameField
        );

        // ========================================================
        // EMAIL
        // ========================================================

        card.getChildren().add(
                createFieldLabel("Email Address")
        );

        emailField =
                createTextField(
                        getString(
                                profile,
                                "email",
                                userEmail
                        )
                );

        emailField.setEditable(false);

        applyReadOnlyStyle(
                emailField
        );

        card.getChildren().add(
                emailField
        );

        // ========================================================
        // PHONE
        // ========================================================

        card.getChildren().add(
                createFieldLabel("Phone Number")
        );

        phoneField =
                createTextField(
                        getString(
                                profile,
                                "phone",
                                ""
                        )
                );

        card.getChildren().add(
                phoneField
        );

        // ========================================================
        // ROLE
        // ========================================================

        card.getChildren().add(
                createFieldLabel("Account Type")
        );

        roleField =
                createTextField(
                        getString(
                                profile,
                                "role",
                                UserSession.getUserRole()
                        )
                );

        roleField.setEditable(false);

        applyReadOnlyStyle(
                roleField
        );

        card.getChildren().add(
                roleField
        );

        // ========================================================
        // STATUS
        // ========================================================

        statusLabel =
                new Label();

        statusLabel.setFont(
                Font.font(
                        "Arial",
                        14
                )
        );

        statusLabel.setWrapText(true);

        card.getChildren().add(
                statusLabel
        );

        // ========================================================
        // RESET BUTTON
        // ========================================================

        Button resetButton =
                new Button("Reset");

        resetButton.setPrefWidth(100);

        resetButton.setPrefHeight(44);

        resetButton.setFont(
                Font.font(
                        "Arial",
                        14
                )
        );

        resetButton.setCursor(
                javafx.scene.Cursor.HAND
        );

        resetButton.setStyle(
                "-fx-background-color: transparent;"
                        + "-fx-text-fill: "
                        + HEADING
                        + ";"
                        + "-fx-border-color: "
                        + BORDER
                        + ";"
                        + "-fx-border-radius: 22;"
                        + "-fx-background-radius: 22;"
        );

        resetButton.setOnAction(
                e -> resetFields()
        );

        // ========================================================
        // SAVE BUTTON
        // ========================================================

        Button saveButton =
                new Button("Save Changes");

        saveButton.setPrefWidth(150);

        saveButton.setPrefHeight(44);

        saveButton.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        14
                )
        );

        saveButton.setTextFill(
                Color.WHITE
        );

        saveButton.setCursor(
                javafx.scene.Cursor.HAND
        );

        saveButton.setStyle(
                "-fx-background-color: "
                        + NAV_BLUE
                        + ";"
                        + "-fx-background-radius: 22;"
        );

        saveButton.setOnAction(
                e -> saveProfile()
        );

        HBox buttons =
                new HBox(
                        12,
                        resetButton,
                        saveButton
                );

        buttons.setAlignment(
                Pos.CENTER_RIGHT
        );

        card.getChildren().add(
                buttons
        );

        content.getChildren().addAll(
                titleBox,
                card
        );

        return content;
    }

    // ============================================================
    // SAVE PROFILE
    // ============================================================

    private void saveProfile() {

        if (userEmail.isEmpty()) {

            showError(
                    "No logged-in user found."
            );

            return;
        }

        String name =
                nameField.getText();

        String phone =
                phoneField.getText();

        // ========================================================
        // VALIDATION
        // ========================================================

        String validation =
                controller.validateProfile(
                        name,
                        phone
                );

        if (validation != null) {

            showError(
                    validation
            );

            return;
        }

        // ========================================================
        // FIRESTORE UPDATE
        // ========================================================

        boolean success =
                controller.updateProfile(
                        userEmail,
                        name,
                        phone
                );

        if (!success) {

            showError(
                    "Profile update failed. Please try again."
            );

            return;
        }

        // ========================================================
        // UPDATE SESSION
        // ========================================================

        UserSession.setUser(
                UserSession.getUserId(),
                name.trim(),
                UserSession.getUserEmail(),
                UserSession.getUserRole()
        );

        showSuccess(
                "Profile updated successfully."
        );
    }

    // ============================================================
    // RESET
    // ============================================================

    private void resetFields() {

        Map<String, Object> profile =
                controller.getProfile(
                        userEmail
                );

        nameField.setText(
                getString(
                        profile,
                        "name",
                        UserSession.getUserName()
                )
        );

        phoneField.setText(
                getString(
                        profile,
                        "phone",
                        ""
                )
        );

        clearStatus();
    }

    // ============================================================
    // FIELD LABEL
    // ============================================================

    private Label createFieldLabel(
            String text
    ) {

        Label label =
                new Label(text);

        label.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        14
                )
        );

        label.setTextFill(
                Color.web(HEADING)
        );

        return label;
    }

    // ============================================================
    // TEXT FIELD
    // ============================================================

    private TextField createTextField(
            String value
    ) {

        TextField field =
                new TextField(
                        value == null
                                ? ""
                                : value
                );

        field.setPrefHeight(44);

        field.setMaxWidth(
                Double.MAX_VALUE
        );

        field.setFont(
                Font.font(
                        "Arial",
                        14
                )
        );

        field.setStyle(
                "-fx-background-color: #242424;"
                        + "-fx-text-fill: "
                        + HEADING
                        + ";"
                        + "-fx-border-color: "
                        + BORDER
                        + ";"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
                        + "-fx-padding: 11;"
        );

        return field;
    }

    // ============================================================
    // READ ONLY STYLE
    // ============================================================

    private void applyReadOnlyStyle(
            TextField field
    ) {

        field.setStyle(
                "-fx-background-color: #242424;"
                        + "-fx-text-fill: "
                        + SECONDARY_TEXT
                        + ";"
                        + "-fx-border-color: "
                        + BORDER
                        + ";"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
                        + "-fx-padding: 11;"
        );
    }

    // ============================================================
    // GET STRING
    // ============================================================

    private String getString(
            Map<String, Object> data,
            String key,
            String fallback
    ) {

        if (data == null) {

            return fallback == null
                    ? ""
                    : fallback;
        }

        Object value =
                data.get(key);

        if (value == null) {

            return fallback == null
                    ? ""
                    : fallback;
        }

        String result =
                String.valueOf(value);

        if (result.trim().isEmpty()) {

            return fallback == null
                    ? ""
                    : fallback;
        }

        return result;
    }

    // ============================================================
    // SUCCESS MESSAGE
    // ============================================================

    private void showSuccess(
            String message
    ) {

        statusLabel.setText(
                message
        );

        statusLabel.setTextFill(
                Color.web(SUCCESS)
        );
    }

    // ============================================================
    // ERROR MESSAGE
    // ============================================================

    private void showError(
            String message
    ) {

        statusLabel.setText(
                message
        );

        statusLabel.setTextFill(
                Color.web(ERROR)
        );
    }

    // ============================================================
    // CLEAR STATUS
    // ============================================================

    private void clearStatus() {

        statusLabel.setText("");
    }

    // ============================================================
    // FALLBACK HEADER
    // ============================================================

    private HBox createFallbackHeader() {

        HBox header =
                new HBox();

        header.setPadding(
                new Insets(
                        15,
                        25,
                        15,
                        25
                )
        );

        header.setAlignment(
                Pos.CENTER_LEFT
        );

        header.setStyle(
                "-fx-background-color: #242424;"
                        + "-fx-border-color: "
                        + BORDER
                        + ";"
        );

        Label logo =
                new Label(
                        "RoadGuardian"
                );

        logo.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        22
                )
        );

        logo.setTextFill(
                Color.web(HEADING)
        );

        header.getChildren().add(
                logo
        );

        return header;
    }

    // ============================================================
    // GET PROFILE SCENE
    // ============================================================

    public Scene getProfileScene() {

        return profileScene;
    }
}