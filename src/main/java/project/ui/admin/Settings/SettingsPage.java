package project.ui.admin.Settings;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class SettingsPage {

    // =====================================================
    // THEME
    // =====================================================

    private static final String BG = "#0F0F0F";
    private static final String CARD = "#1A1A1A";
    private static final String SOFT = "#1A1A1A";
    private static final String BLUE = "#F59E0B";
    private static final String BLUE_HOVER = "#F59E0B";
    private static final String GREEN = "#22C55E";
    private static final String ORANGE = "#F59E0B";
    private static final String RED = "#EF4444";

    private static final String HEADING = "#F3F4F6";
    private static final String TEXT = "#A1A1AA";
    private static final String MUTED = "#A1A1AA";
    private static final String BORDER = "#F59E0B";

    // =====================================================
    // MAIN VIEW
    // =====================================================

    public VBox getView() {

        VBox root = new VBox(18);

        root.setPadding(
                new Insets(28, 30, 30, 30)
        );

        root.setStyle(
                "-fx-background-color: " + BG + ";"
        );

        Button backButton = new Button("←  Back to Home");

        backButton.setPrefHeight(40);
        backButton.setPadding(
                new Insets(0, 18, 0, 18)
        );

        backButton.setTextFill(
                Color.web(HEADING)
        );

        backButton.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        13
                )
        );

        backButton.setCursor(Cursor.HAND);

        backButton.setStyle(
                "-fx-background-color: #1A1A1A;" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-radius: 9;" +
                        "-fx-background-radius: 9;" +
                        "-fx-cursor: hand;"
        );

        backButton.setOnMouseEntered(
                event ->
                        backButton.setStyle(
                                "-fx-background-color: " + SOFT + ";" +
                                        "-fx-border-color: " + BLUE + ";" +
                                        "-fx-border-radius: 9;" +
                                        "-fx-background-radius: 9;" +
                                        "-fx-cursor: hand;"
                        )
        );

        backButton.setOnMouseExited(
                event ->
                        backButton.setStyle(
                                "-fx-background-color: #1A1A1A;" +
                                        "-fx-border-color: " + BORDER + ";" +
                                        "-fx-border-radius: 9;" +
                                        "-fx-background-radius: 9;" +
                                        "-fx-cursor: hand;"
                        )
        );

        // Only show a message for now.
        // Actual Home/Login navigation is intentionally not implemented yet.
        backButton.setOnAction(
                event ->
                        showMessage(
                                "Navigation",
                                "Going to Home Page..."
                        )
        );

        VBox header = createHeader();

        HBox topRow = new HBox(16);

        VBox profileCard = createProfileSection();
        VBox applicationCard = createApplicationSection();

        HBox.setHgrow(profileCard, Priority.ALWAYS);
        HBox.setHgrow(applicationCard, Priority.ALWAYS);

        topRow.getChildren().addAll(
                profileCard,
                applicationCard
        );

        HBox bottomRow = new HBox(16);

        VBox notificationCard = createNotificationSection();
        VBox securityCard = createSecuritySection();

        HBox.setHgrow(notificationCard, Priority.ALWAYS);
        HBox.setHgrow(securityCard, Priority.ALWAYS);

        bottomRow.getChildren().addAll(
                notificationCard,
                securityCard
        );

        root.getChildren().addAll(
                backButton,
                header,
                topRow,
                bottomRow
        );

        return root;
    }

    // =====================================================
    // HEADER
    // =====================================================

    private VBox createHeader() {

        VBox header = new VBox(8);

        HBox titleRow = new HBox(12);

        titleRow.setAlignment(Pos.CENTER_LEFT);

        VBox icon = createHeaderIcon();

        VBox titleBox = new VBox(3);

        Label title = new Label("Settings");

        title.setTextFill(Color.web(HEADING));
        title.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        31
                )
        );

        Label subtitle = new Label(
                "Manage your RoadGuardian admin profile, application preferences and security."
        );

        subtitle.setTextFill(Color.web(TEXT));
        subtitle.setFont(Font.font("Arial", 14));

        titleBox.getChildren().addAll(
                title,
                subtitle
        );

        Region spacer = new Region();

        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label status = new Label(
                "●  SYSTEM ACTIVE"
        );

        status.setTextFill(Color.web(GREEN));
        status.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        10
                )
        );

        status.setPadding(
                new Insets(8, 12, 8, 12)
        );

        status.setStyle(
                "-fx-background-color: #14251B;" +
                        "-fx-background-radius: 20;"
        );

        titleRow.getChildren().addAll(
                icon,
                titleBox,
                spacer,
                status
        );

        header.getChildren().add(titleRow);

        return header;
    }

    private VBox createHeaderIcon() {

        VBox box = new VBox();

        box.setAlignment(Pos.CENTER);

        box.setPrefSize(48, 48);
        box.setMinSize(48, 48);
        box.setMaxSize(48, 48);

        box.setStyle(
                "-fx-background-color: " + BLUE + ";" +
                        "-fx-background-radius: 13;"
        );

        Label icon = new Label("⚙");

        icon.setTextFill(Color.WHITE);
        icon.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        21
                )
        );

        box.getChildren().add(icon);

        return box;
    }

    // =====================================================
    // PROFILE
    // =====================================================

    private VBox createProfileSection() {

        VBox card = createCard();

        card.getChildren().add(
                createSectionHeader(
                        "Admin Profile",
                        "Personal information used for administration.",
                        BLUE,
                        "A"
                )
        );

        GridPane grid = createGrid();

        TextField nameField =
                createTextField("RoadGuardian Admin");

        TextField emailField =
                createTextField("admin@roadguardian.com");

        TextField phoneField =
                createTextField("+91 98765 43210");

        addRow(
                grid,
                "Admin Name",
                nameField,
                0
        );

        addRow(
                grid,
                "Email",
                emailField,
                1
        );

        addRow(
                grid,
                "Phone",
                phoneField,
                2
        );

        Button save = createPrimaryButton(
                "Save Profile"
        );

        save.setOnAction(
                event ->
                        showMessage(
                                "Profile Updated",
                                "Admin profile saved successfully."
                        )
        );

        HBox buttonRow = new HBox(save);

        buttonRow.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(
                grid,
                buttonRow
        );

        return card;
    }

    // =====================================================
    // APPLICATION
    // =====================================================

    private VBox createApplicationSection() {

        VBox card = createCard();

        card.getChildren().add(
                createSectionHeader(
                        "Application",
                        "Configure language, timezone and system behavior.",
                        GREEN,
                        "⌘"
                )
        );

        GridPane grid = createGrid();

        ComboBox<String> language =
                new ComboBox<>();

        language.getItems().addAll(
                "English",
                "Marathi",
                "Hindi"
        );

        language.setValue("English");
        styleCombo(language);

        ComboBox<String> timezone =
                new ComboBox<>();

        timezone.getItems().addAll(
                "Asia/Kolkata",
                "UTC"
        );

        timezone.setValue("Asia/Kolkata");
        styleCombo(timezone);

        addRow(
                grid,
                "Language",
                language,
                0
        );

        addRow(
                grid,
                "Timezone",
                timezone,
                1
        );

        ToggleButton maintenance =
                createToggle(
                        "Maintenance Mode",
                        "Temporarily restrict application access."
                );

        grid.add(
                maintenance,
                1,
                2
        );

        Button save = createPrimaryButton(
                "Save Settings"
        );

        save.setOnAction(
                event ->
                        showMessage(
                                "Settings Updated",
                                "Application settings saved successfully."
                        )
        );

        HBox buttonRow = new HBox(save);

        buttonRow.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(
                grid,
                buttonRow
        );

        return card;
    }

    // =====================================================
    // NOTIFICATIONS
    // =====================================================

    private VBox createNotificationSection() {

        VBox card = createCard();

        card.getChildren().add(
                createSectionHeader(
                        "Notification Preferences",
                        "Choose which events should appear in the admin panel.",
                        ORANGE,
                        "!"
                )
        );

        VBox options = new VBox(9);

        options.getChildren().addAll(
                createPreferenceRow(
                        "Service Requests",
                        "New roadside service requests",
                        true
                ),
                createPreferenceRow(
                        "SOS Alerts",
                        "Emergency assistance requests",
                        true
                ),
                createPreferenceRow(
                        "Complaints",
                        "Customer complaint updates",
                        true
                ),
                createPreferenceRow(
                        "Reviews",
                        "New customer reviews",
                        true
                )
        );

        Button save = createPrimaryButton(
                "Save Preferences"
        );

        save.setOnAction(
                event ->
                        showMessage(
                                "Notifications Updated",
                                "Notification preferences saved."
                        )
        );

        HBox buttonRow = new HBox(save);

        buttonRow.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(
                options,
                buttonRow
        );

        return card;
    }

    // =====================================================
    // SECURITY
    // =====================================================

    private VBox createSecuritySection() {

        VBox card = createCard();

        card.getChildren().add(
                createSectionHeader(
                        "Security",
                        "Update your administrator password securely.",
                        RED,
                        "✓"
                )
        );

        VBox fields = new VBox(10);

        PasswordField currentPassword =
                createPasswordField(
                        "Current password"
                );

        PasswordField newPassword =
                createPasswordField(
                        "New password"
                );

        PasswordField confirmPassword =
                createPasswordField(
                        "Confirm new password"
                );

        fields.getChildren().addAll(
                currentPassword,
                newPassword,
                confirmPassword
        );

        Button changeButton =
                createPrimaryButton(
                        "Change Password"
                );

        changeButton.setOnAction(
                event -> {

                    if (newPassword.getText().isEmpty()) {

                        showMessage(
                                "Security",
                                "Please enter a new password."
                        );

                        return;
                    }

                    if (!newPassword.getText().equals(
                            confirmPassword.getText()
                    )) {

                        showMessage(
                                "Security",
                                "New password and confirm password do not match."
                        );

                        return;
                    }

                    showMessage(
                            "Security Updated",
                            "Password change request completed."
                    );
                }
        );

        HBox buttonRow = new HBox(changeButton);

        buttonRow.setAlignment(Pos.CENTER_RIGHT);

        Label hint = new Label(
                "Use a strong password with a combination of letters, numbers and symbols."
        );

        hint.setTextFill(Color.web(MUTED));
        hint.setFont(Font.font("Arial", 11));
        hint.setWrapText(true);

        card.getChildren().addAll(
                fields,
                hint,
                buttonRow
        );

        return card;
    }

    // =====================================================
    // CARD
    // =====================================================

    private VBox createCard() {

        VBox card = new VBox(15);

        card.setPadding(
                new Insets(20)
        );

        card.setStyle(
                "-fx-background-color: " + CARD + ";" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-radius: 15;" +
                        "-fx-background-radius: 15;" +
                        "-fx-effect: dropshadow(gaussian, rgba(23,32,51,0.07), 12, 0.08, 0, 3);"
        );

        return card;
    }

    // =====================================================
    // SECTION HEADER
    // =====================================================

    private HBox createSectionHeader(
            String titleText,
            String subtitleText,
            String color,
            String iconText
    ) {

        HBox header = new HBox(11);

        header.setAlignment(Pos.CENTER_LEFT);

        StackPane icon = new StackPane();

        icon.setPrefSize(38, 38);
        icon.setMinSize(38, 38);
        icon.setMaxSize(38, 38);

        icon.setStyle(
                "-fx-background-color: " + color + "18;" +
                        "-fx-background-radius: 11;"
        );

        Label iconLabel = new Label(iconText);

        iconLabel.setTextFill(Color.web(color));
        iconLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        16
                )
        );

        icon.getChildren().add(iconLabel);

        VBox textBox = new VBox(2);

        Label title = new Label(titleText);

        title.setTextFill(Color.web(HEADING));
        title.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        18
                )
        );

        Label subtitle = new Label(subtitleText);

        subtitle.setTextFill(Color.web(MUTED));
        subtitle.setFont(Font.font("Arial", 11));
        subtitle.setWrapText(true);

        textBox.getChildren().addAll(
                title,
                subtitle
        );

        header.getChildren().addAll(
                icon,
                textBox
        );

        return header;
    }

    // =====================================================
    // PREFERENCE ROW
    // =====================================================

    private HBox createPreferenceRow(
            String titleText,
            String subtitleText,
            boolean selected
    ) {

        HBox row = new HBox(12);

        row.setAlignment(Pos.CENTER_LEFT);

        row.setPadding(
                new Insets(10, 12, 10, 12)
        );

        row.setStyle(
                "-fx-background-color: " + SOFT + ";" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;"
        );

        VBox textBox = new VBox(2);

        Label title = new Label(titleText);

        title.setTextFill(Color.web(HEADING));
        title.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        13
                )
        );

        Label subtitle = new Label(subtitleText);

        subtitle.setTextFill(Color.web(MUTED));
        subtitle.setFont(Font.font("Arial", 10));
        subtitle.setWrapText(true);

        textBox.getChildren().addAll(
                title,
                subtitle
        );

        Region spacer = new Region();

        HBox.setHgrow(spacer, Priority.ALWAYS);

        CheckBox check = new CheckBox();

        check.setSelected(selected);

        check.setCursor(Cursor.HAND);

        row.getChildren().addAll(
                textBox,
                spacer,
                check
        );

        return row;
    }

    // =====================================================
    // TOGGLE
    // =====================================================

    private ToggleButton createToggle(
            String titleText,
            String subtitleText
    ) {

        ToggleButton toggle =
                new ToggleButton();

        toggle.setPrefWidth(46);
        toggle.setPrefHeight(24);

        toggle.setCursor(Cursor.HAND);

        toggle.setStyle(
                "-fx-background-color: #F59E0B;" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-radius: 20;"
        );

        toggle.selectedProperty().addListener(
                (obs, oldValue, selected) -> {

                    if (selected) {

                        toggle.setStyle(
                                "-fx-background-color: " + GREEN + ";" +
                                        "-fx-background-radius: 20;" +
                                        "-fx-border-radius: 20;"
                        );

                    } else {

                        toggle.setStyle(
                                "-fx-background-color: #F59E0B;" +
                                        "-fx-background-radius: 20;" +
                                        "-fx-border-radius: 20;"
                        );
                    }
                }
        );

        VBox textBox = new VBox(2);

        Label title = new Label(titleText);

        title.setTextFill(Color.web(HEADING));
        title.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        13
                )
        );

        Label subtitle = new Label(subtitleText);

        subtitle.setTextFill(Color.web(MUTED));
        subtitle.setFont(Font.font("Arial", 10));
        subtitle.setWrapText(true);

        textBox.getChildren().addAll(
                title,
                subtitle
        );

        HBox wrapper = new HBox(12);

        wrapper.setAlignment(Pos.CENTER_LEFT);

        wrapper.setPadding(
                new Insets(5, 0, 5, 0)
        );

        Region spacer = new Region();

        HBox.setHgrow(spacer, Priority.ALWAYS);

        wrapper.getChildren().addAll(
                textBox,
                spacer,
                toggle
        );

        return toggle;
    }

    // =====================================================
    // GRID / FORM
    // =====================================================

    private GridPane createGrid() {

        GridPane grid = new GridPane();

        grid.setHgap(13);
        grid.setVgap(12);

        ColumnConstraints labelColumn =
                new ColumnConstraints();

        labelColumn.setMinWidth(105);
        labelColumn.setPrefWidth(105);

        ColumnConstraints valueColumn =
                new ColumnConstraints();

        valueColumn.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().addAll(
                labelColumn,
                valueColumn
        );

        return grid;
    }

    private void addRow(
            GridPane grid,
            String labelText,
            Control control,
            int row
    ) {

        Label label = new Label(labelText);

        label.setTextFill(Color.web(TEXT));
        label.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        12
                )
        );

        grid.add(
                label,
                0,
                row
        );

        grid.add(
                control,
                1,
                row
        );

        GridPane.setHgrow(
                control,
                Priority.ALWAYS
        );
    }

    private TextField createTextField(
            String value
    ) {

        TextField field =
                new TextField(value);

        field.setPrefHeight(40);

        styleField(field);

        return field;
    }

    private PasswordField createPasswordField(
            String prompt
    ) {

        PasswordField field =
                new PasswordField();

        field.setPromptText(prompt);
        field.setPrefHeight(40);

        styleField(field);

        return field;
    }

    // =====================================================
    // STYLING
    // =====================================================

    private void styleField(
            TextField field
    ) {

        field.setStyle(
                "-fx-background-color: #1A1A1A;" +
                        "-fx-text-fill: " + HEADING + ";" +
                        "-fx-prompt-text-fill: " + MUTED + ";" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-radius: 9;" +
                        "-fx-background-radius: 9;" +
                        "-fx-padding: 0 12 0 12;" +
                        "-fx-font-size: 12px;"
        );
    }

    private void styleCombo(
            ComboBox<String> combo
    ) {

        combo.setPrefHeight(40);

        combo.setStyle(
                "-fx-background-color: #1A1A1A;" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-radius: 9;" +
                        "-fx-background-radius: 9;" +
                        "-fx-font-size: 12px;"
        );
    }

    private Button createPrimaryButton(
            String text
    ) {

        Button button = new Button(text);

        button.setPrefHeight(40);

        button.setPadding(
                new Insets(0, 17, 0, 17)
        );

        button.setTextFill(Color.WHITE);

        button.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        12
                )
        );

        button.setCursor(Cursor.HAND);

        setButtonStyle(
                button,
                BLUE,
                BLUE_HOVER
        );

        return button;
    }

    private void setButtonStyle(
            Button button,
            String normal,
            String hover
    ) {

        button.setStyle(
                "-fx-background-color: " + normal + ";" +
                        "-fx-background-radius: 9;"
        );

        button.setOnMouseEntered(
                event ->
                        button.setStyle(
                                "-fx-background-color: " + hover + ";" +
                                        "-fx-background-radius: 9;"
                        )
        );

        button.setOnMouseExited(
                event ->
                        button.setStyle(
                                "-fx-background-color: " + normal + ";" +
                                        "-fx-background-radius: 9;"
                        )
        );
    }

    // =====================================================
    // MESSAGE
    // =====================================================

    private void showMessage(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}
