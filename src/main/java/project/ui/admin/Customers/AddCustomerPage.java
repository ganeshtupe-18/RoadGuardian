package project.ui.admin.Customers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import project.controller.admin.CustomerController;
import project.model.Customer;
import project.ui.admin.DashBoard.AdminSectionPage;

public class AddCustomerPage extends AdminSectionPage {

    // =========================================================
    // THEME
    // Same theme as Customer Management Page
    // =========================================================

    private static final String BG = "#0F0F0F";
    private static final String SURFACE = "#1A1A1A";
    private static final String SECONDARY = "#242424";

    private static final String ORANGE = "#F59E0B";
    private static final String ORANGE_HOVER = "#D97706";

    private static final String BLUE = "#F59E0B";
    private static final String GREEN = "#22C55E";
    private static final String RED = "#EF4444";

    private static final String HEADING = "#F3F4F6";
    private static final String TEXT = "#A1A1AA";
    private static final String BORDER = "#F59E0B";

    // =========================================================
    // CONTROLLER
    // =========================================================

    private final CustomerController controller;

    // =========================================================
    // CALLBACK
    // =========================================================

    private final Runnable backAction;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public AddCustomerPage(Runnable backAction) {

        this.backAction = backAction;

        CustomerController tempController;

        try {

            tempController = new CustomerController();

        } catch (Exception e) {

            tempController = null;
        }

        this.controller = tempController;
    }

    // =========================================================
    // GET VIEW
    // =========================================================

    @Override
    public VBox getView() {

        VBox root = new VBox();

        root.setStyle(
                "-fx-background-color: " + BG + ";"
        );

        VBox content = createContent();

        root.getChildren().add(content);

        return root;
    }

    // =========================================================
    // MAIN CONTENT
    // =========================================================

    private VBox createContent() {

        VBox content = new VBox(20);

        content.setPadding(
                new Insets(
                        30,
                        35,
                        35,
                        35
                )
        );

        HBox header = createHeader();

        VBox formCard = createFormCard();

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

        HBox header = new HBox(15);

        header.setAlignment(
                Pos.CENTER_LEFT
        );

        Button backButton = createButton(
                "← Back",
                BLUE
        );

        backButton.setOnAction(
                event -> {

                    if (backAction != null) {
                        backAction.run();
                    }

                }
        );

        VBox titleBox = new VBox(4);

        Label title = new Label(
                "Add Customer"
        );

        title.setTextFill(
                Color.web(HEADING)
        );

        title.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        30
                )
        );

        Label subtitle = new Label(
                "Create a new customer account in RoadGuardian."
        );

        subtitle.setTextFill(
                Color.web(TEXT)
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

        header.getChildren().addAll(
                backButton,
                titleBox
        );

        return header;
    }

    // =========================================================
    // FORM CARD
    // =========================================================

    private VBox createFormCard() {

        VBox card = new VBox(20);

        card.setPadding(
                new Insets(25)
        );

        card.setStyle(
                "-fx-background-color: " + SURFACE + ";" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 14;" +
                "-fx-background-radius: 14;"
        );

        Label heading = new Label(
                "Customer Information"
        );

        heading.setTextFill(
                Color.web(HEADING)
        );

        heading.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        21
                )
        );

        Label helper = new Label(
                "Enter the customer's basic account information."
        );

        helper.setTextFill(
                Color.web(TEXT)
        );

        helper.setFont(
                Font.font(
                        "Arial",
                        14
                )
        );

        GridPane form = createForm();

        // -----------------------------------------------------
        // FIELDS
        // -----------------------------------------------------

        TextField name = createTextField(
                "Enter customer full name"
        );

        TextField email = createTextField(
                "Enter email address"
        );

        TextField phone = createTextField(
                "Enter phone number"
        );

        TextField address = createTextField(
                "Enter address"
        );

        TextField city = createTextField(
                "Enter city"
        );

        ComboBox<String> status = new ComboBox<>();

        status.getItems().addAll(
                "Active",
                "Inactive"
        );

        status.setValue(
                "Active"
        );

        status.setPrefWidth(320);

        status.setPrefHeight(42);

        status.setStyle(
                "-fx-background-color: " + SURFACE + ";" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-font-size: 14px;"
        );

        // -----------------------------------------------------
        // ADD TO FORM
        // -----------------------------------------------------

        form.add(
                createFormLabel("Full Name *"),
                0,
                0
        );

        form.add(
                name,
                1,
                0
        );

        form.add(
                createFormLabel("Email *"),
                0,
                1
        );

        form.add(
                email,
                1,
                1
        );

        form.add(
                createFormLabel("Phone *"),
                0,
                2
        );

        form.add(
                phone,
                1,
                2
        );

        form.add(
                createFormLabel("Address"),
                0,
                3
        );

        form.add(
                address,
                1,
                3
        );

        form.add(
                createFormLabel("City"),
                0,
                4
        );

        form.add(
                city,
                1,
                4
        );

        form.add(
                createFormLabel("Status"),
                0,
                5
        );

        form.add(
                status,
                1,
                5
        );

        // -----------------------------------------------------
        // BUTTONS
        // -----------------------------------------------------

        HBox buttons = new HBox(12);

        buttons.setAlignment(
                Pos.CENTER_RIGHT
        );

        Button cancelButton =
                createSecondaryButton("Cancel");

        Button saveButton =
                createButton(
                        "Save Customer",
                        ORANGE
                );

        cancelButton.setOnAction(
                event -> {

                    if (backAction != null) {
                        backAction.run();
                    }

                }
        );

        saveButton.setOnAction(
                event -> {

                    saveCustomer(
                            name,
                            email,
                            phone,
                            address,
                            city,
                            status
                    );

                }
        );

        Region spacer = new Region();

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
    // FORM GRID
    // =========================================================

    private GridPane createForm() {

        GridPane form = new GridPane();

        form.setHgap(18);

        form.setVgap(16);

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

        Label label = new Label(text);

        label.setTextFill(
                Color.web(HEADING)
        );

        label.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        14
                )
        );

        label.setMinWidth(120);

        return label;
    }

    // =========================================================
    // TEXT FIELD
    // =========================================================

    private TextField createTextField(
            String prompt
    ) {

        TextField field = new TextField();

        field.setPromptText(prompt);

        field.setPrefWidth(450);

        field.setPrefHeight(42);

        field.setStyle(
                "-fx-background-color: " + SURFACE + ";" +
                "-fx-text-fill: " + HEADING + ";" +
                "-fx-prompt-text-fill: " + TEXT + ";" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 0 12 0 12;" +
                "-fx-font-size: 14px;"
        );

        return field;
    }

    // =========================================================
    // SAVE CUSTOMER
    // =========================================================

    private void saveCustomer(
            TextField name,
            TextField email,
            TextField phone,
            TextField address,
            TextField city,
            ComboBox<String> status
    ) {

        String customerName =
                name.getText().trim();

        String customerEmail =
                email.getText().trim();

        String customerPhone =
                phone.getText().trim();

        String customerAddress =
                address.getText().trim();

        String customerCity =
                city.getText().trim();

        String customerStatus =
                status.getValue();

        // -----------------------------------------------------
        // VALIDATION
        // -----------------------------------------------------

        if (
                customerName.isBlank()
                ||
                customerEmail.isBlank()
                ||
                customerPhone.isBlank()
        ) {

            showError(
                    "Missing Information",
                    "Name, email and phone are required."
            );

            return;
        }

        // -----------------------------------------------------
        // EMAIL VALIDATION
        // -----------------------------------------------------

        if (
                !customerEmail.contains("@")
                ||
                !customerEmail.contains(".")
        ) {

            showError(
                    "Invalid Email",
                    "Please enter a valid email address."
            );

            return;
        }

        // -----------------------------------------------------
        // PHONE VALIDATION
        // -----------------------------------------------------

        if (
                customerPhone.length() < 10
        ) {

            showError(
                    "Invalid Phone",
                    "Please enter a valid phone number."
            );

            return;
        }

        // -----------------------------------------------------
        // CONTROLLER CHECK
        // -----------------------------------------------------

        if (controller == null) {

            showError(
                    "Controller Error",
                    "Customer controller could not be initialized."
            );

            return;
        }

        // -----------------------------------------------------
        // CREATE CUSTOMER
        // -----------------------------------------------------

        Customer customer =
                new Customer(
                        "",
                        customerName,
                        customerEmail,
                        customerPhone,
                        customerAddress,
                        customerCity,
                        "",
                        customerStatus,
                        String.valueOf(
                                System.currentTimeMillis()
                        )
                );

        // -----------------------------------------------------
        // SAVE TO FIREBASE
        // -----------------------------------------------------

        try {

            boolean success =
                    controller.addCustomer(
                            customer
                    );

            if (success) {

                showInformation(
                        "Customer Added",
                        "Customer added successfully."
                );

                if (backAction != null) {
                    backAction.run();
                }

            } else {

                showError(
                        "Add Customer Failed",
                        "Customer could not be added."
                );
            }

        } catch (Exception e) {

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

        Button button = new Button(text);

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

        button.setPrefHeight(42);

        button.setPadding(
                new Insets(
                        0,
                        18,
                        0,
                        18
                )
        );

        button.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-background-radius: 9;" +
                "-fx-cursor: hand;"
        );

        button.setOnMouseEntered(
                event -> {

                    String hoverColor =
                            color.equals(ORANGE)
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

        Button button = new Button(text);

        button.setTextFill(
                Color.web(HEADING)
        );

        button.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        14
                )
        );

        button.setPrefHeight(42);

        button.setPadding(
                new Insets(
                        0,
                        18,
                        0,
                        18
                )
        );

        button.setStyle(
                "-fx-background-color: " + SURFACE + ";" +
                "-fx-border-color: " + BORDER + ";" +
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

        alert.setTitle(title);

        alert.setHeaderText(null);

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

        alert.setTitle(title);

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }
}