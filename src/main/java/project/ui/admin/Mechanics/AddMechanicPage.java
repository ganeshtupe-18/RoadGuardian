package project.ui.admin.Mechanics;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import com.google.cloud.firestore.Firestore;

import project.controller.admin.MechanicController;
import project.firebase.FirebaseConfig;
import project.model.Mechanic;
import project.ui.admin.DashBoard.AdminSectionPage;
import project.util.IconUtil;

public class AddMechanicPage extends AdminSectionPage {

    // =========================================================
    // THEME
    // =========================================================

    private static final String BG = "#0F0F0F";
    private static final String SURFACE = "#1A1A1A";
    private static final String SECONDARY = "#242424";

    private static final String ORANGE = "#F59E0B";
    private static final String ORANGE_HOVER = "#D97706";

    private static final String BLUE = "#F59E0B";
    private static final String GREEN = "#22C55E";

    private static final String HEADING = "#F3F4F6";
    private static final String TEXT = "#A1A1AA";
    private static final String BORDER = "#F59E0B";

    // =========================================================
    // CONTROLLER
    // =========================================================

    private final MechanicController controller;

    // =========================================================
    // BACK ACTION
    // =========================================================

    private final Runnable backAction;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public AddMechanicPage(
            Runnable backAction
    ) {

        this.backAction = backAction;

        MechanicController tempController;

        try {

            Firestore firestore =
                    FirebaseConfig.getFirestore();

            tempController =
                    new MechanicController(
                            firestore
                    );

        } catch (Exception e) {

            e.printStackTrace();

            tempController = null;
        }

        this.controller =
                tempController;
    }

    // =========================================================
    // GET VIEW
    // =========================================================

    @Override
    public VBox getView() {

        VBox root =
                new VBox();

        root.setStyle(
                "-fx-background-color: " +
                BG +
                ";"
        );

        VBox content =
                createContent();

        root.getChildren().add(
                content
        );

        return root;
    }

    // =========================================================
    // CONTENT
    // =========================================================

    private VBox createContent() {

        VBox content =
                new VBox(20);

        content.setPadding(
                new Insets(
                        30,
                        35,
                        35,
                        35
                )
        );

        HBox header =
                createHeader();

        VBox formCard =
                createFormCard();

        VBox.setVgrow(
                formCard,
                Priority.ALWAYS
        );

        content.getChildren().addAll(
                header,
                formCard
        );

        return content;
    }

    // =========================================================
    // HEADER
    // =========================================================

    private HBox createHeader() {

        HBox header =
                new HBox(15);

        header.setAlignment(
                Pos.CENTER_LEFT
        );

        Button backButton =
                createButton(
                        "← Back",
                        BLUE
                );

        backButton.setOnAction(
                event -> {

                    if (
                            backAction != null
                    ) {

                        backAction.run();
                    }
                }
        );

        VBox titleBox =
                new VBox(4);

        Label title =
                new Label(
                        "Add Mechanic"
                );

        title.setTextFill(
                Color.web(
                        HEADING
                )
        );

        title.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        30
                )
        );

        Label subtitle =
                new Label(
                        "Register a new mechanic in RoadGuardian."
                );

        subtitle.setTextFill(
                Color.web(
                        TEXT
                )
        );

        subtitle.setFont(
                Font.font(
                        "Arial",
                        15
                )
        );

        titleBox.getChildren().addAll(
                title,
                subtitle
        );

        var mechanicIcon =
                IconUtil.createMechanicIcon(
                        0.85,
                        Color.WHITE.toString()
                );

        javafx.scene.layout.StackPane mechanicAvatar =
                new javafx.scene.layout.StackPane();

        mechanicAvatar.setPrefSize(54, 54);
        mechanicAvatar.setMinSize(54, 54);
        mechanicAvatar.setMaxSize(54, 54);
        mechanicAvatar.setStyle(
                "-fx-background-color: " +
                BLUE +
                ";" +
                "-fx-background-radius: 27;"
        );
        mechanicAvatar.getChildren().add(mechanicIcon);

        header.getChildren().addAll(
                backButton,
                mechanicAvatar,
                titleBox
        );

        return header;
    }

    // =========================================================
    // FORM CARD
    // =========================================================

    private VBox createFormCard() {

        VBox card =
                new VBox(20);

        card.setPadding(
                new Insets(
                        25
                )
        );

        card.setStyle(
                "-fx-background-color: " +
                SURFACE +
                ";" +
                "-fx-border-color: " +
                BORDER +
                ";" +
                "-fx-border-radius: 14;" +
                "-fx-background-radius: 14;"
        );

        Label heading =
                new Label(
                        "Mechanic Information"
                );

        heading.setTextFill(
                Color.web(
                        HEADING
                )
        );

        heading.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        21
                )
        );

        Label helper =
                new Label(
                        "Enter the mechanic's personal, professional and availability information."
                );

        helper.setTextFill(
                Color.web(
                        TEXT
                )
        );

        helper.setFont(
                Font.font(
                        "Arial",
                        14
                )
        );

        GridPane form =
                createForm();

        // =====================================================
        // FIELDS
        // =====================================================

        TextField name =
                createTextField(
                        "Enter mechanic full name"
                );

        TextField email =
                createTextField(
                        "Enter email address"
                );

        TextField phone =
                createTextField(
                        "Enter phone number"
                );

        TextField address =
                createTextField(
                        "Enter address"
                );

        TextField city =
                createTextField(
                        "Enter city"
                );

        TextField specialization =
                createTextField(
                        "e.g. Car Repair, Bike Repair, Electrical"
                );

        TextField experience =
                createTextField(
                        "e.g. 5 years"
                );

        ComboBox<String> status =
                new ComboBox<>();

        status.getItems().addAll(
                "Active",
                "Inactive"
        );

        status.setValue(
                "Active"
        );

        status.setPrefWidth(
                450
        );

        status.setPrefHeight(
                42
        );

        styleComboBox(
                status
        );

        // =====================================================
        // FORM ROWS
        // =====================================================

        form.add(
                createFormLabel(
                        "Full Name *"
                ),
                0,
                0
        );

        form.add(
                name,
                1,
                0
        );

        form.add(
                createFormLabel(
                        "Email *"
                ),
                0,
                1
        );

        form.add(
                email,
                1,
                1
        );

        form.add(
                createFormLabel(
                        "Phone *"
                ),
                0,
                2
        );

        form.add(
                phone,
                1,
                2
        );

        form.add(
                createFormLabel(
                        "Address"
                ),
                0,
                3
        );

        form.add(
                address,
                1,
                3
        );

        form.add(
                createFormLabel(
                        "City"
                ),
                0,
                4
        );

        form.add(
                city,
                1,
                4
        );

        form.add(
                createFormLabel(
                        "Specialization *"
                ),
                0,
                5
        );

        form.add(
                specialization,
                1,
                5
        );

        form.add(
                createFormLabel(
                        "Experience"
                ),
                0,
                6
        );

        form.add(
                experience,
                1,
                6
        );

        form.add(
                createFormLabel(
                        "Status"
                ),
                0,
                7
        );

        form.add(
                status,
                1,
                7
        );

        // =====================================================
        // BUTTONS
        // =====================================================

        HBox buttons =
                new HBox(12);

        buttons.setAlignment(
                Pos.CENTER_RIGHT
        );

        Button cancelButton =
                createSecondaryButton(
                        "Cancel"
                );

        Button saveButton =
                createButton(
                        "Save Mechanic",
                        ORANGE
                );

        cancelButton.setOnAction(
                event -> {

                    if (
                            backAction != null
                    ) {

                        backAction.run();
                    }
                }
        );

        saveButton.setOnAction(
                event -> {

                    saveMechanic(
                            name,
                            email,
                            phone,
                            address,
                            city,
                            specialization,
                            experience,
                            status
                    );
                }
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        buttons.getChildren().addAll(
                spacer,
                cancelButton,
                saveButton
        );

        card.getChildren().addAll(
                heading,
                helper,
                form,
                buttons
        );

        return card;
    }

    // =========================================================
    // FORM
    // =========================================================

    private GridPane createForm() {

        GridPane form =
                new GridPane();

        form.setHgap(
                18
        );

        form.setVgap(
                16
        );

        form.setPadding(
                new Insets(
                        15,
                        5,
                        15,
                        5
                )
        );

        return form;
    }

    // =========================================================
    // FORM LABEL
    // =========================================================

    private Label createFormLabel(
            String text
    ) {

        Label label =
                new Label(
                        text
                );

        label.setTextFill(
                Color.web(
                        HEADING
                )
        );

        label.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        14
                )
        );

        label.setMinWidth(
                150
        );

        return label;
    }

    // =========================================================
    // TEXT FIELD
    // =========================================================

    private TextField createTextField(
            String prompt
    ) {

        TextField field =
                new TextField();

        field.setPromptText(
                prompt
        );

        field.setPrefWidth(
                450
        );

        field.setPrefHeight(
                42
        );

        field.setStyle(
                "-fx-background-color: " +
                SURFACE +
                ";" +
                "-fx-text-fill: " +
                HEADING +
                ";" +
                "-fx-prompt-text-fill: " +
                TEXT +
                ";" +
                "-fx-border-color: " +
                BORDER +
                ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 0 12 0 12;" +
                "-fx-font-size: 14px;"
        );

        return field;
    }

    // =========================================================
    // COMBO BOX
    // =========================================================

    private void styleComboBox(
            ComboBox<String> comboBox
    ) {

        comboBox.setStyle(
                "-fx-background-color: " +
                SURFACE +
                ";" +
                "-fx-border-color: " +
                BORDER +
                ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-font-size: 14px;"
        );
    }

    // =========================================================
    // SAVE MECHANIC
    // =========================================================

    private void saveMechanic(
            TextField name,
            TextField email,
            TextField phone,
            TextField address,
            TextField city,
            TextField specialization,
            TextField experience,
            ComboBox<String> status
    ) {

        String mechanicName =
                name.getText()
                        .trim();

        String mechanicEmail =
                email.getText()
                        .trim();

        String mechanicPhone =
                phone.getText()
                        .trim();

        String mechanicAddress =
                address.getText()
                        .trim();

        String mechanicCity =
                city.getText()
                        .trim();

        String mechanicSpecialization =
                specialization.getText()
                        .trim();

        String mechanicExperience =
                experience.getText()
                        .trim();

        String mechanicStatus =
                status.getValue();

        // =====================================================
        // VALIDATION
        // =====================================================

        if (
                mechanicName.isBlank()
                ||
                mechanicEmail.isBlank()
                ||
                mechanicPhone.isBlank()
                ||
                mechanicSpecialization.isBlank()
        ) {

            showError(
                    "Missing Information",
                    "Name, email, phone and specialization are required."
            );

            return;
        }

        // =====================================================
        // EMAIL VALIDATION
        // =====================================================

        if (
                !mechanicEmail.contains("@")
                ||
                !mechanicEmail.contains(".")
        ) {

            showError(
                    "Invalid Email",
                    "Please enter a valid email address."
            );

            return;
        }

        // =====================================================
        // PHONE VALIDATION
        // =====================================================

        if (
                mechanicPhone.length() < 10
        ) {

            showError(
                    "Invalid Phone",
                    "Please enter a valid phone number."
            );

            return;
        }

        // =====================================================
        // CONTROLLER CHECK
        // =====================================================

        if (
                controller == null
        ) {

            showError(
                    "Controller Error",
                    "Mechanic controller could not be initialized."
            );

            return;
        }

        // =====================================================
        // CREATE MECHANIC
        // =====================================================

        Mechanic mechanic =
                new Mechanic(
                        "",
                        mechanicName,
                        mechanicEmail,
                        mechanicPhone,
                        mechanicAddress,
                        mechanicCity,
                        mechanicSpecialization,
                        mechanicStatus,
                        mechanicExperience,
                        String.valueOf(
                                System.currentTimeMillis()
                        )
                );

        // =====================================================
        // SAVE
        // =====================================================

        try {

            boolean success =
                    controller.addMechanic(
                            mechanic
                    );

            if (
                    success
            ) {

                showInformation(
                        "Mechanic Added",
                        "Mechanic added successfully."
                );

                if (
                        backAction != null
                ) {

                    backAction.run();
                }

            } else {

                showError(
                        "Add Mechanic Failed",
                        "Mechanic could not be added."
                );
            }

        } catch (
                Exception e
        ) {

            e.printStackTrace();

            showError(
                    "Firebase Error",
                    e.getMessage()
            );
        }
    }

    // =========================================================
    // PRIMARY BUTTON
    // =========================================================

    private Button createButton(
            String text,
            String color
    ) {

        Button button =
                new Button(
                        text
                );

        button.setTextFill(
                Color.WHITE
        );

        button.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        14
                )
        );

        button.setPrefHeight(
                42
        );

        button.setPadding(
                new Insets(
                        0,
                        18,
                        0,
                        18
                )
        );

        button.setStyle(
                "-fx-background-color: " +
                color +
                ";" +
                "-fx-background-radius: 9;" +
                "-fx-cursor: hand;"
        );

        button.setOnMouseEntered(
                event -> {

                    String hoverColor =
                            color.equals(
                                    ORANGE
                            )
                                    ? ORANGE_HOVER
                                    : "#F59E0B";

                    button.setStyle(
                            "-fx-background-color: " +
                            hoverColor +
                            ";" +
                            "-fx-background-radius: 9;" +
                            "-fx-cursor: hand;"
                    );
                }
        );

        button.setOnMouseExited(
                event -> {

                    button.setStyle(
                            "-fx-background-color: " +
                            color +
                            ";" +
                            "-fx-background-radius: 9;" +
                            "-fx-cursor: hand;"
                    );
                }
        );

        return button;
    }

    // =========================================================
    // SECONDARY BUTTON
    // =========================================================

    private Button createSecondaryButton(
            String text
    ) {

        Button button =
                new Button(
                        text
                );

        button.setTextFill(
                Color.web(
                        HEADING
                )
        );

        button.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        14
                )
        );

        button.setPrefHeight(
                42
        );

        button.setPadding(
                new Insets(
                        0,
                        18,
                        0,
                        18
                )
        );

        button.setStyle(
                "-fx-background-color: " +
                SURFACE +
                ";" +
                "-fx-border-color: " +
                BORDER +
                ";" +
                "-fx-border-radius: 9;" +
                "-fx-background-radius: 9;" +
                "-fx-cursor: hand;"
        );

        button.setOnMouseEntered(
                event -> {

                    button.setStyle(
                            "-fx-background-color: " +
                            SECONDARY +
                            ";" +
                            "-fx-border-color: " +
                            BLUE +
                            ";" +
                            "-fx-border-radius: 9;" +
                            "-fx-background-radius: 9;" +
                            "-fx-cursor: hand;"
                    );
                }
        );

        button.setOnMouseExited(
                event -> {

                    button.setStyle(
                            "-fx-background-color: " +
                            SURFACE +
                            ";" +
                            "-fx-border-color: " +
                            BORDER +
                            ";" +
                            "-fx-border-radius: 9;" +
                            "-fx-background-radius: 9;" +
                            "-fx-cursor: hand;"
                    );
                }
        );

        return button;
    }

    // =========================================================
    // ERROR
    // =========================================================

    private void showError(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle(
                title
        );

        alert.setHeaderText(
                null
        );

        alert.setContentText(
                message == null
                        ? "Unknown error."
                        : message
        );

        alert.showAndWait();
    }

    // =========================================================
    // INFORMATION
    // =========================================================

    private void showInformation(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(
                title
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