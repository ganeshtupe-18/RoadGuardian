package project.ui.user;

import project.controller.user.SettingsController;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class SettingsPage {

    // ============================================================
    // ROADGUARDIAN DARK THEME
    // ============================================================

    private static final String BG = "#0F0F0F";
    private static final String SIDEBAR = "#161616";
    private static final String CARD = "#1A1A1A";
    private static final String SURFACE = "#242424";
    private static final String HOVER = "#2B2B2B";

    private static final String PRIMARY = "#F59E0B";
    private static final String PRIMARY_LIGHT = "#FBBF24";

    private static final String HEADING = "#F3F4F6";
    private static final String TEXT = "#A1A1AA";
    private static final String MUTED = "#71717A";

    private static final String SUCCESS = "#22C55E";
    private static final String ERROR = "#EF4444";

    private static final String BORDER = "#F59E0B";

    // ============================================================
    // SETTINGS DATA
    // ============================================================

    private boolean darkModeEnabled = false;

    private boolean mechanicUpdates = true;
    private boolean documentReminders = true;
    private boolean serviceAlerts = true;
    private boolean offers = false;

    private String selectedLanguage = "English";

    /*
     * Kept because existing theme logic uses it.
     */
    private final Map<Node, String> lightStyles =
            new IdentityHashMap<>();

    private final SettingsController controller;

    private Scene settingsScene;

    private String userId;

    // ============================================================
    // UI REFERENCES
    // ============================================================

    private ComboBox<String> languageBox;

    private Button darkToggle;

    private Button mechanicToggle;
    private Button documentToggle;
    private Button serviceToggle;
    private Button offersToggle;

    private VBox contactsCard;

    private TextField makeModelField;
    private TextField registrationField;
    private TextField fuelField;
    private TextField yearField;

    private String currentVehicleId;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public SettingsPage() {

        controller =
                new SettingsController();

        String sessionEmail =
                UserSession.getUserEmail();

        userId =
                sessionEmail == null
                        || sessionEmail.trim().isEmpty()
                        ? UserSession.getUserId()
                        : sessionEmail.trim().toLowerCase();
    }

    // ============================================================
    // PUBLIC SCENE
    // ============================================================

    public Scene getSettingsScene() {

        BorderPane root =
                new BorderPane();

        root.setStyle(
                "-fx-background-color: " + BG + ";"
        );

        HBox header =
                UserHeader.createHeader();

        ScrollPane sidebar =
                UserSideBar.createSidebar(
                        "Settings"
                );

        VBox content =
                createContent();

        ScrollPane contentScroll =
                new ScrollPane(content);

        contentScroll.setFitToWidth(true);
        contentScroll.setFitToHeight(false);
        contentScroll.setPannable(true);

        contentScroll.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        contentScroll.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.ALWAYS
        );

        contentScroll.setStyle(
                "-fx-background-color: " + BG + ";" +
                "-fx-background: " + BG + ";" +
                "-fx-border-color: transparent;"
        );

        root.setTop(header);
        root.setLeft(sidebar);
        root.setCenter(contentScroll);

        settingsScene =
                new Scene(
                        root,
                        UserDashboard.dashboardStage.getWidth(),
                        UserDashboard.dashboardStage.getHeight()
                );

        // --------------------------------------------------------
        // LOAD FIREBASE DATA
        // --------------------------------------------------------

        if (
                userId != null
                        &&
                !userId.trim().isEmpty()
        ) {

            controller.loadSettings(
                    userId,
                    this
            );
        }

        return settingsScene;
    }

    // ============================================================
    // CONTENT
    // ============================================================

    private VBox createContent() {

        VBox content =
                new VBox(20);

        content.setPadding(
                new Insets(
                        28,
                        32,
                        35,
                        32
                )
        );

        content.setStyle(
                "-fx-background-color: " + BG + ";"
        );

        VBox heading =
                createHeading();

        HBox topCards =
                new HBox(18);

        VBox appearance =
                createAppearanceCard();

        VBox notifications =
                createNotificationCard();

        HBox.setHgrow(
                appearance,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                notifications,
                Priority.ALWAYS
        );

        topCards.getChildren().addAll(
                appearance,
                notifications
        );

        HBox bottomCards =
                new HBox(18);

        contactsCard =
                createEmergencyContactsCard();

        VBox vehicle =
                createVehicleDetailsCard();

        HBox.setHgrow(
                contactsCard,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                vehicle,
                Priority.ALWAYS
        );

        bottomCards.getChildren().addAll(
                contactsCard,
                vehicle
        );

        content.getChildren().addAll(
                heading,
                topCards,
                bottomCards
        );

        return content;
    }

    // ============================================================
    // HEADING
    // ============================================================

    private VBox createHeading() {

        VBox heading =
                new VBox(4);

        Label title =
                new Label("Settings");

        title.setStyle(
                "-fx-font-size: 29px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + HEADING + ";"
        );

        Label subtitle =
                new Label(
                        "Manage your RoadGuardian preferences"
                );

        subtitle.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-text-fill: " + TEXT + ";"
        );

        heading.getChildren().addAll(
                title,
                subtitle
        );

        return heading;
    }

    // ============================================================
    // APPEARANCE CARD
    // ============================================================

    private VBox createAppearanceCard() {

        VBox card =
                new VBox(14);

        card.setPrefHeight(315);
        card.setMinHeight(280);

        card.setPadding(
                new Insets(20)
        );

        card.setStyle(
                cardStyle()
        );

        Label title =
                new Label(
                        "Appearance & language"
                );

        title.setStyle(
                sectionTitleStyle()
        );

        // --------------------------------------------------------
        // DARK MODE
        // --------------------------------------------------------

        HBox darkMode =
                new HBox();

        darkMode.setPrefHeight(66);

        darkMode.setPadding(
                new Insets(
                        12,
                        15,
                        12,
                        15
                )
        );

        darkMode.setAlignment(
                Pos.CENTER_LEFT
        );

        darkMode.setStyle(
                innerRowStyle()
        );

        VBox darkText =
                new VBox(3);

        Label darkTitle =
                new Label("Dark mode");

        darkTitle.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + HEADING + ";"
        );

        Label darkDescription =
                new Label(
                        "Easier on the eyes during night-time assists."
                );

        darkDescription.setWrapText(true);

        darkDescription.setStyle(
                "-fx-font-size: 11px;" +
                "-fx-text-fill: " + TEXT + ";"
        );

        darkText.getChildren().addAll(
                darkTitle,
                darkDescription
        );

        Region space =
                new Region();

        HBox.setHgrow(
                space,
                Priority.ALWAYS
        );

        darkToggle =
                toggleButton(
                        darkModeEnabled
                );

        darkToggle.setOnAction(
                event -> {

                    darkModeEnabled =
                            !darkModeEnabled;

                    setToggleState(
                            darkToggle,
                            darkModeEnabled
                    );

                    if (
                            darkToggle.getScene() != null
                    ) {

                        applyTheme(
                                darkToggle
                                        .getScene()
                                        .getRoot()
                        );
                    }

                    savePreferences();
                }
        );

        darkMode.getChildren().addAll(
                darkText,
                space,
                darkToggle
        );

        // --------------------------------------------------------
        // LANGUAGE
        // --------------------------------------------------------

        Label languageTitle =
                new Label("Language");

        languageTitle.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + HEADING + ";"
        );

        languageBox =
                new ComboBox<>();

        languageBox.getItems().addAll(
                "English",
                "Hindi",
                "Marathi"
        );

        languageBox.setValue(
                selectedLanguage
        );

        languageBox.setMaxWidth(
                Double.MAX_VALUE
        );

        languageBox.setPrefHeight(42);

        styleComboBox(
                languageBox
        );

        languageBox.setOnAction(
                event -> {

                    if (
                            languageBox.getValue() != null
                    ) {

                        selectedLanguage =
                                languageBox.getValue();

                        savePreferences();
                    }
                }
        );

        card.getChildren().addAll(
                title,
                darkMode,
                languageTitle,
                languageBox
        );

        return card;
    }

    // ============================================================
    // NOTIFICATION CARD
    // ============================================================

    private VBox createNotificationCard() {

        VBox card =
                new VBox(10);

        card.setPrefHeight(315);
        card.setMinHeight(280);

        card.setPadding(
                new Insets(20)
        );

        card.setStyle(
                cardStyle()
        );

        Label title =
                new Label(
                        "Notification preferences"
                );

        title.setStyle(
                sectionTitleStyle()
        );

        card.getChildren().addAll(

                title,

                preferenceRow(
                        "Mechanic status updates",
                        mechanicUpdates
                ),

                preferenceRow(
                        "Document expiry reminders",
                        documentReminders
                ),

                preferenceRow(
                        "Service due alerts",
                        serviceAlerts
                ),

                preferenceRow(
                        "Offers and partner deals",
                        offers
                )
        );

        return card;
    }

    // ============================================================
    // PREFERENCE ROW
    // ============================================================

    private HBox preferenceRow(
            String text,
            boolean active
    ) {

        HBox row =
                new HBox();

        row.setPrefHeight(50);

        row.setPadding(
                new Insets(
                        0,
                        15,
                        0,
                        15
                )
        );

        row.setAlignment(
                Pos.CENTER_LEFT
        );

        row.setStyle(
                innerRowStyle()
        );

        Label label =
                new Label(text);

        label.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: " + HEADING + ";"
        );

        Region space =
                new Region();

        HBox.setHgrow(
                space,
                Priority.ALWAYS
        );

        Button toggle =
                toggleButton(active);

        toggle.setOnAction(
                event -> {

                    toggleState(toggle);

                    boolean value =
                            Boolean.TRUE.equals(
                                    toggle.getUserData()
                            );

                    updatePreference(
                            text,
                            value
                    );

                    savePreferences();
                }
        );

        switch (text) {

            case "Mechanic status updates" ->
                    mechanicToggle = toggle;

            case "Document expiry reminders" ->
                    documentToggle = toggle;

            case "Service due alerts" ->
                    serviceToggle = toggle;

            case "Offers and partner deals" ->
                    offersToggle = toggle;
        }

        row.getChildren().addAll(
                label,
                space,
                toggle
        );

        return row;
    }

    // ============================================================
    // UPDATE PREFERENCE
    // ============================================================

    private void updatePreference(
            String name,
            boolean value
    ) {

        switch (name) {

            case "Mechanic status updates" ->
                    mechanicUpdates = value;

            case "Document expiry reminders" ->
                    documentReminders = value;

            case "Service due alerts" ->
                    serviceAlerts = value;

            case "Offers and partner deals" ->
                    offers = value;
        }
    }

    // ============================================================
    // TOGGLE BUTTON
    // ============================================================

    private Button toggleButton(
            boolean active
    ) {

        Button toggle =
                new Button();

        toggle.setPrefWidth(42);
        toggle.setPrefHeight(24);

        toggle.setMinWidth(42);
        toggle.setMaxWidth(42);

        toggle.setPadding(
                Insets.EMPTY
        );

        setToggleState(
                toggle,
                active
        );

        return toggle;
    }

    private void toggleState(
            Button toggle
    ) {

        boolean active =
                !Boolean.TRUE.equals(
                        toggle.getUserData()
                );

        setToggleState(
                toggle,
                active
        );
    }

    private void setToggleState(
            Button toggle,
            boolean active
    ) {

        toggle.setUserData(active);

        toggle.setText(
                active
                        ? "●"
                        : "○"
        );

        toggle.setStyle(

                "-fx-background-color: " +

                        (
                                active
                                        ? PRIMARY
                                        : "#303030"
                        ) +

                        ";" +

                "-fx-background-radius: 15;" +

                "-fx-border-color: " +

                        (
                                active
                                        ? PRIMARY_LIGHT
                                        : BORDER
                        ) +

                        ";" +

                "-fx-border-radius: 15;" +

                "-fx-text-fill: white;" +

                "-fx-font-size: 14px;" +

                "-fx-font-weight: bold;" +

                "-fx-cursor: hand;"
        );
    }

    // ============================================================
    // EMERGENCY CONTACT CARD
    // ============================================================

    private VBox createEmergencyContactsCard() {

        VBox card =
                new VBox(11);

        card.setPadding(
                new Insets(20)
        );

        card.setStyle(
                cardStyle()
        );

        Label title =
                new Label(
                        "Emergency contacts"
                );

        title.setStyle(
                sectionTitleStyle()
        );

        Label description =
                new Label(
                        "People RoadGuardian can use as emergency contacts."
                );

        description.setWrapText(true);

        description.setStyle(
                "-fx-font-size: 11px;" +
                "-fx-text-fill: " + TEXT + ";"
        );

        Button addContact =
                new Button(
                        "+  Add contact"
                );

        addContact.setMaxWidth(
                Double.MAX_VALUE
        );

        addContact.setPrefHeight(40);

        addContact.setStyle(
                "-fx-background-color: " + SURFACE + ";" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 20;" +
                "-fx-background-radius: 20;" +
                "-fx-text-fill: " + HEADING + ";" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-cursor: hand;"
        );

        addContact.setOnAction(
                event ->
                        addContactRow(card)
        );

        card.getChildren().addAll(
                title,
                description,
                addContact
        );

        return card;
    }

    // ============================================================
    // ADD CONTACT
    // ============================================================

    private void addContactRow(
            VBox card
    ) {

        HBox row =
                contactRow(
                        null,
                        "",
                        ""
                );

        card.getChildren().add(
                card.getChildren().size() - 1,
                row
        );
    }

    // ============================================================
    // CONTACT ROW
    // ============================================================

    private HBox contactRow(
            String contactId,
            String name,
            String number
    ) {

        HBox row =
                new HBox(8);

        row.setPrefHeight(52);

        TextField nameField =
                new TextField(name);

        nameField.setPromptText(
                "Contact name"
        );

        nameField.setPrefHeight(38);

        nameField.setStyle(
                textFieldStyle()
        );

        HBox.setHgrow(
                nameField,
                Priority.ALWAYS
        );

        TextField numberField =
                new TextField(number);

        numberField.setPromptText(
                "Phone number"
        );

        numberField.setPrefHeight(38);

        numberField.setStyle(
                textFieldStyle()
        );

        HBox.setHgrow(
                numberField,
                Priority.ALWAYS
        );

        Button save =
                new Button("Save");

        save.setPrefHeight(38);

        save.setMinWidth(65);

        save.setStyle(
                "-fx-background-color: " + PRIMARY + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 20;" +
                "-fx-cursor: hand;"
        );

        save.setOnAction(
                event -> {

                    controller.saveContact(
                            userId,
                            contactId,
                            nameField.getText(),
                            numberField.getText(),
                            this
                    );
                }
        );

        Button delete =
                new Button("Delete");

        delete.setPrefHeight(38);

        delete.setStyle(
                "-fx-background-color: #2A1717;" +
                "-fx-text-fill: #F87171;" +
                "-fx-border-color: #4A2525;" +
                "-fx-border-radius: 20;" +
                "-fx-background-radius: 20;" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;" +
                "-fx-cursor: hand;"
        );

        boolean existingContact =
                contactId != null
                        && !contactId.trim().isEmpty();

        delete.setDisable(
                !existingContact
        );

        delete.setVisible(
                existingContact
        );

        delete.setManaged(
                existingContact
        );

        delete.setOnAction(
                event ->
                        controller.deleteContact(
                                userId,
                                contactId,
                                this
                        )
        );

        row.getChildren().addAll(
                nameField,
                numberField,
                save,
                delete
        );

        return row;
    }

    // ============================================================
    // VEHICLE DETAILS CARD
    // ============================================================

    private VBox createVehicleDetailsCard() {

        VBox card =
                new VBox(11);

        card.setPadding(
                new Insets(20)
        );

        card.setStyle(
                cardStyle()
        );

        Label title =
                new Label(
                        "Vehicle details"
                );

        title.setStyle(
                sectionTitleStyle()
        );

        Label description =
                new Label(
                        "Keep your registered vehicle information up to date."
                );

        description.setWrapText(true);

        description.setStyle(
                "-fx-font-size: 11px;" +
                "-fx-text-fill: " + TEXT + ";"
        );

        HBox row1 =
                new HBox(10);

        VBox make =
                vehicleField(
                        "Make & model",
                        ""
                );

        VBox registration =
                vehicleField(
                        "Registration",
                        ""
                );

        HBox.setHgrow(
                make,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                registration,
                Priority.ALWAYS
        );

        row1.getChildren().addAll(
                make,
                registration
        );

        HBox row2 =
                new HBox(10);

        VBox fuel =
                vehicleField(
                        "Fuel type",
                        ""
                );

        VBox year =
                vehicleField(
                        "Year",
                        ""
                );

        HBox.setHgrow(
                fuel,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                year,
                Priority.ALWAYS
        );

        row2.getChildren().addAll(
                fuel,
                year
        );

        Button save =
                new Button(
                        "Save vehicle changes"
                );

        save.setMaxWidth(
                Double.MAX_VALUE
        );

        save.setPrefHeight(40);

        save.setStyle(
                "-fx-background-color: " + PRIMARY + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 20;" +
                "-fx-cursor: hand;"
        );

        save.setOnAction(
                event ->
                        controller.saveVehicle(
                                userId,
                                currentVehicleId,
                                makeModelField.getText(),
                                registrationField.getText(),
                                fuelField.getText(),
                                yearField.getText(),
                                this
                        )
        );

        card.getChildren().addAll(
                title,
                description,
                row1,
                row2,
                save
        );

        return card;
    }

    // ============================================================
    // VEHICLE FIELD
    // ============================================================

    private VBox vehicleField(
            String title,
            String value
    ) {

        VBox box =
                new VBox(4);

        Label titleLabel =
                new Label(title);

        titleLabel.setStyle(
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + HEADING + ";"
        );

        TextField field =
                new TextField(value);

        field.setPrefHeight(40);

        field.setMaxWidth(
                Double.MAX_VALUE
        );

        field.setStyle(
                textFieldStyle()
        );

        switch (title) {

            case "Make & model" ->
                    makeModelField = field;

            case "Registration" ->
                    registrationField = field;

            case "Fuel type" ->
                    fuelField = field;

            case "Year" ->
                    yearField = field;
        }

        box.getChildren().addAll(
                titleLabel,
                field
        );

        return box;
    }

    // ============================================================
    // APPLY FIREBASE DATA
    // ============================================================

    public void applyLoadedData(
            Map<String, Object> settings,
            Map<String, Object> vehicle,
            List<Map<String, Object>> contacts
    ) {

        if (settings == null) {
            settings = Map.of();
        }

        if (vehicle == null) {
            vehicle = Map.of();
        }

        // --------------------------------------------------------
        // SETTINGS
        // --------------------------------------------------------

        darkModeEnabled =
                booleanValue(
                        settings.get("darkMode"),
                        false
                );

        selectedLanguage =
                stringValue(
                        settings.get("language"),
                        "English"
                );

        mechanicUpdates =
                booleanValue(
                        settings.get(
                                "mechanicStatusUpdates"
                        ),
                        true
                );

        documentReminders =
                booleanValue(
                        settings.get(
                                "documentExpiryReminders"
                        ),
                        true
                );

        serviceAlerts =
                booleanValue(
                        settings.get(
                                "serviceDueAlerts"
                        ),
                        true
                );

        offers =
                booleanValue(
                        settings.get(
                                "offersAndPartnerDeals"
                        ),
                        false
                );

        // --------------------------------------------------------
        // UPDATE UI
        // --------------------------------------------------------

        if (languageBox != null) {

            if (
                    languageBox
                            .getItems()
                            .contains(
                                    selectedLanguage
                            )
            ) {

                languageBox.setValue(
                        selectedLanguage
                );
            }
        }

        if (darkToggle != null) {

            setToggleState(
                    darkToggle,
                    darkModeEnabled
            );
        }

        if (mechanicToggle != null) {

            setToggleState(
                    mechanicToggle,
                    mechanicUpdates
            );
        }

        if (documentToggle != null) {

            setToggleState(
                    documentToggle,
                    documentReminders
            );
        }

        if (serviceToggle != null) {

            setToggleState(
                    serviceToggle,
                    serviceAlerts
            );
        }

        if (offersToggle != null) {

            setToggleState(
                    offersToggle,
                    offers
            );
        }

        // --------------------------------------------------------
        // VEHICLE
        // --------------------------------------------------------

        currentVehicleId =
                stringValue(
                        vehicle.get("vehicleId"),
                        ""
                );

        if (makeModelField != null) {

            makeModelField.setText(
                    stringValue(
                            vehicle.get("makeModel"),
                            ""
                    )
            );
        }

        if (registrationField != null) {

            registrationField.setText(
                    stringValue(
                            vehicle.get("registration"),
                            ""
                    )
            );
        }

        if (fuelField != null) {

            fuelField.setText(
                    stringValue(
                            vehicle.get("fuelType"),
                            ""
                    )
            );
        }

        if (yearField != null) {

            yearField.setText(
                    stringValue(
                            vehicle.get("year"),
                            ""
                    )
            );
        }

        // --------------------------------------------------------
        // CONTACTS
        // --------------------------------------------------------

        loadContacts(
                contacts
        );

        // --------------------------------------------------------
        // KEEP DARK VISUALS
        // --------------------------------------------------------

        if (
                settingsScene != null
                        &&
                darkModeEnabled
        ) {

            applyDarkTheme(
                    settingsScene.getRoot()
            );
        }
    }

    // ============================================================
    // LOAD CONTACTS
    // ============================================================

    private void loadContacts(
            List<Map<String, Object>> contacts
    ) {

        if (contactsCard == null) {
            return;
        }

        if (contacts == null) {
            return;
        }

        /*
         * Keep:
         * 0 = title
         * 1 = description
         * last = Add contact
         */
        while (
                contactsCard.getChildren().size() > 3
        ) {

            contactsCard
                    .getChildren()
                    .remove(2);
        }

        int insertIndex = 2;

        for (
                Map<String, Object> contact :
                contacts
        ) {

            if (contact == null) {
                continue;
            }

            String contactId =
                    stringValue(
                            contact.get("contactId"),
                            stringValue(
                                    contact.get("id"),
                                    ""
                            )
                    );

            String name =
                    stringValue(
                            contact.get("name"),
                            ""
                    );

            String phone =
                    stringValue(
                            contact.get("phone"),
                            ""
                    );

            HBox row =
                    contactRow(
                            contactId,
                            name,
                            phone
                    );

            contactsCard
                    .getChildren()
                    .add(
                            insertIndex,
                            row
                    );

            insertIndex++;
        }
    }

    // ============================================================
    // SAVE PREFERENCES
    // ============================================================

    private void savePreferences() {

        if (
                userId == null
                        ||
                userId.trim().isEmpty()
        ) {
            return;
        }

        controller.savePreferences(
                userId,
                darkModeEnabled,
                selectedLanguage,
                mechanicUpdates,
                documentReminders,
                serviceAlerts,
                offers,
                this
        );
    }

    // ============================================================
    // THEME
    // ============================================================

    private void applyTheme(
            Parent root
    ) {

        if (root == null) {
            return;
        }

        if (darkModeEnabled) {

            applyDarkTheme(root);

        } else {

            restoreLightTheme(root);
        }
    }

    private void applyDarkTheme(
            Node node
    ) {

        if (node == null) {
            return;
        }

        lightStyles.putIfAbsent(
                node,
                node.getStyle()
        );

        String existing =
                lightStyles.get(node);

        String converted =
                toDarkStyle(existing);

        if (
                converted != null
                        &&
                !converted.isEmpty()
        ) {

            node.setStyle(
                    converted
            );
        }

        if (node instanceof Parent parent) {

            for (
                    Node child :
                    parent.getChildrenUnmodifiable()
            ) {

                applyDarkTheme(
                        child
                );
            }
        }
    }

    private void restoreLightTheme(
            Node node
    ) {

        if (node == null) {
            return;
        }

        String original =
                lightStyles.get(node);

        if (original != null) {

            node.setStyle(
                    original
            );
        }

        if (node instanceof Parent parent) {

            for (
                    Node child :
                    parent.getChildrenUnmodifiable()
            ) {

                restoreLightTheme(
                        child
                );
            }
        }
    }

    private String toDarkStyle(
            String style
    ) {

        if (style == null) {
            return "";
        }

        String result =
                style;

        // --------------------------------------------------------
        // BACKGROUNDS
        // --------------------------------------------------------

        result =
                result.replace(
                        "#eef3f8",
                        CARD
                );

        result =
                result.replace(
                        "#f9fcfd",
                        CARD
                );

        result =
                result.replace(
                        "#ffffff",
                        SURFACE
                );

        result =
                result.replace(
                        "#FFFFFF",
                        SURFACE
                );

        result =
                result.replace(
                        "white",
                        SURFACE
                );

        // --------------------------------------------------------
        // TEXT
        // --------------------------------------------------------

        result =
                result.replace(
                        "#172033",
                        HEADING
                );

        result =
                result.replace(
                        "#263142",
                        HEADING
                );

        result =
                result.replace(
                        "#737d8d",
                        TEXT
                );

        result =
                result.replace(
                        "#65717d",
                        TEXT
                );

        result =
                result.replace(
                        "#d5e0e7",
                        BORDER
                );

        // --------------------------------------------------------
        // COMMON LIGHT BACKGROUND
        // --------------------------------------------------------

        result =
                result.replace(
                        "#f3f4f6",
                        SURFACE
                );

        return result;
    }

    // ============================================================
    // STYLE HELPERS
    // ============================================================

    private String cardStyle() {

        return

                "-fx-background-color: " +
                CARD +
                ";" +

                "-fx-border-color: " +
                BORDER +
                ";" +

                "-fx-border-width: 1;" +

                "-fx-border-radius: 16;" +

                "-fx-background-radius: 16;";
    }

    private String innerRowStyle() {

        return

                "-fx-background-color: " +
                SURFACE +
                ";" +

                "-fx-border-color: " +
                BORDER +
                ";" +

                "-fx-border-width: 1;" +

                "-fx-border-radius: 12;" +

                "-fx-background-radius: 12;";
    }

    private String sectionTitleStyle() {

        return

                "-fx-font-size: 17px;" +

                "-fx-font-weight: bold;" +

                "-fx-text-fill: " +
                HEADING +
                ";";
    }

    private String textFieldStyle() {

        return

                "-fx-background-color: " +
                SURFACE +
                ";" +

                "-fx-text-fill: " +
                HEADING +
                ";" +

                "-fx-prompt-text-fill: " +
                MUTED +
                ";" +

                "-fx-border-color: " +
                BORDER +
                ";" +

                "-fx-border-width: 1;" +

                "-fx-border-radius: 10;" +

                "-fx-background-radius: 10;" +

                "-fx-padding: 0 12 0 12;" +

                "-fx-font-size: 12px;";
    }

    // ============================================================
    // DARK COMBOBOX
    // ============================================================

    private void styleComboBox(
            ComboBox<String> combo
    ) {

        combo.setStyle(

                "-fx-background-color: " +
                SURFACE +
                ";" +

                "-fx-border-color: " +
                BORDER +
                ";" +

                "-fx-border-width: 1;" +

                "-fx-border-radius: 10;" +

                "-fx-background-radius: 10;" +

                "-fx-font-size: 12px;"
        );

        combo.setButtonCell(
                darkComboCell()
        );

        combo.setCellFactory(
                list ->
                        darkComboCell()
        );
    }

    private javafx.scene.control.ListCell<String>
    darkComboCell() {

        return new javafx.scene.control.ListCell<>() {

            @Override
            protected void updateItem(
                    String item,
                    boolean empty
            ) {

                super.updateItem(
                        item,
                        empty
                );

                if (
                        empty
                                ||
                        item == null
                ) {

                    setText(null);

                    setStyle(
                            "-fx-background-color: " +
                            SURFACE +
                            ";"
                    );

                } else {

                    setText(item);

                    setStyle(

                            "-fx-background-color: " +
                            SURFACE +
                            ";" +

                            "-fx-text-fill: " +
                            HEADING +
                            ";" +

                            "-fx-font-size: 12px;" +

                            "-fx-padding: 8 12 8 12;"
                    );
                }
            }
        };
    }

    // ============================================================
    // VALUE HELPERS
    // ============================================================

    private boolean booleanValue(
            Object value,
            boolean defaultValue
    ) {

        if (value == null) {

            return defaultValue;
        }

        if (value instanceof Boolean) {

            return (Boolean) value;
        }

        return Boolean.parseBoolean(
                String.valueOf(value)
        );
    }

    private String stringValue(
            Object value,
            String defaultValue
    ) {

        if (value == null) {

            return defaultValue;
        }

        String result =
                String.valueOf(value);

        if (
                result.trim().isEmpty()
        ) {

            return defaultValue;
        }

        return result;
    }

    // ============================================================
    // SUCCESS ALERT
    // ============================================================

    public void showSuccess(
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        if (
                settingsScene != null
                        &&
                settingsScene.getWindow() != null
        ) {

            alert.initOwner(
                    settingsScene.getWindow()
            );
        }

        alert.setTitle(
                "RoadGuardian"
        );

        alert.setHeaderText(
                null
        );

        alert.setContentText(
                message
        );

        alert.showAndWait();
    }

    // ============================================================
    // ERROR ALERT
    // ============================================================

    public void showError(
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        if (
                settingsScene != null
                        &&
                settingsScene.getWindow() != null
        ) {

            alert.initOwner(
                    settingsScene.getWindow()
            );
        }

        alert.setTitle(
                "Settings Error"
        );

        alert.setHeaderText(
                null
        );

        alert.setContentText(
                message
        );

        alert.showAndWait();
    }
}