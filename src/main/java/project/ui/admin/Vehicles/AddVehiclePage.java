package project.ui.admin.Vehicles;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import com.google.cloud.firestore.Firestore;

import project.controller.admin.VehicleController;
import project.firebase.FirebaseConfig;
import project.model.Vehicle;
import project.ui.admin.DashBoard.AdminSectionPage;

public class AddVehiclePage extends AdminSectionPage {

    // =========================================================
    // THEME
    // =========================================================

    private static final String BG = "#0F0F0F";
    private static final String SURFACE = "#1A1A1A";
    private static final String SECONDARY = "#242424";

    private static final String ORANGE = "#F59E0B";
    private static final String ORANGE_HOVER = "#D97706";

    private static final String HEADING = "#F3F4F6";
    private static final String TEXT = "#A1A1AA";

    private static final String BLUE = "#F59E0B";
    private static final String GREEN = "#22C55E";
    private static final String RED = "#EF4444";

    private static final String BORDER = "#F59E0B";

    // =========================================================
    // FIELDS
    // =========================================================

    private VehicleController controller;

    private final Runnable onBack;

    // =========================================================
    // FORM FIELDS
    // =========================================================

    private TextField customerIdField;
    private TextField ownerNameField;
    private TextField vehicleNumberField;
    private TextField brandField;
    private TextField modelField;
    private TextField yearField;

    private ComboBox<String> vehicleTypeCombo;
    private ComboBox<String> fuelTypeCombo;
    private ComboBox<String> statusCombo;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public AddVehiclePage(
            Runnable onBack
    ) {

        this.onBack = onBack;
    }

    // =========================================================
    // GET VIEW
    // =========================================================

    @Override
    public VBox getView() {

        VBox root =
                new VBox();

        root.setFillWidth(true);

        root.setStyle(
                "-fx-background-color: " +
                BG +
                ";"
        );

        try {

            Firestore firestore =
                    FirebaseConfig.getFirestore();

            controller =
                    new VehicleController(
                            firestore
                    );

        } catch (Exception e) {

            e.printStackTrace();

            root.getChildren().add(
                    createErrorView(
                            "Unable to connect to Firestore."
                    )
            );

            return root;
        }

        VBox content =
                createPageContent();

        root.getChildren().add(
                content
        );

        VBox.setVgrow(
                content,
                Priority.ALWAYS
        );

        return root;
    }

    // =========================================================
    // PAGE CONTENT
    // =========================================================

    private VBox createPageContent() {

        VBox page =
                new VBox(22);

        page.setPadding(
                new Insets(
                        30,
                        32,
                        32,
                        32
                )
        );

        page.setFillWidth(true);

        // -----------------------------------------------------
        // HEADER
        // -----------------------------------------------------

        HBox header =
                createHeader();

        // -----------------------------------------------------
        // FORM CARD
        // -----------------------------------------------------

        VBox formCard =
                createFormCard();

        VBox.setVgrow(
                formCard,
                Priority.ALWAYS
        );

        page.getChildren().addAll(
                header,
                formCard
        );

        return page;
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
                new Button(
                        "← Back"
                );

        styleBackButton(
                backButton
        );

        backButton.setOnAction(
                event -> goBack()
        );

        VBox titleBox =
                new VBox(5);

        Label title =
                new Label(
                        "Add Vehicle"
                );

        title.setTextFill(
                Color.web(HEADING)
        );

        title.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        32
                )
        );

        Label subtitle =
                new Label(
                        "Register a new customer vehicle in RoadGuardian."
                );

        subtitle.setTextFill(
                Color.web(TEXT)
        );

        subtitle.setFont(
                Font.font(
                        "Arial",
                        16
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

        VBox card =
                new VBox(20);

        card.setPadding(
                new Insets(25)
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

        // -----------------------------------------------------
        // CARD HEADER
        // -----------------------------------------------------

        VBox cardHeader =
                new VBox(5);

        Label heading =
                new Label(
                        "Vehicle Information"
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

        Label helper =
                new Label(
                        "Enter the vehicle details below. Fields marked with * are required."
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

        cardHeader.getChildren().addAll(
                heading,
                helper
        );

        // -----------------------------------------------------
        // FORM
        // -----------------------------------------------------

        GridPane form =
                createFormGrid();

        customerIdField =
                createTextField(
                        "Enter customer ID"
                );

        ownerNameField =
                createTextField(
                        "Enter vehicle owner name"
                );

        vehicleNumberField =
                createTextField(
                        "e.g. MH12AB1234"
                );

        brandField =
                createTextField(
                        "e.g. Tata"
                );

        modelField =
                createTextField(
                        "e.g. Nexon"
                );

        yearField =
                createTextField(
                        "e.g. 2024"
                );

        vehicleTypeCombo =
                createComboBox(
                        "Car",
                        "Bike",
                        "Scooter",
                        "SUV",
                        "Truck",
                        "Other"
                );

        fuelTypeCombo =
                createComboBox(
                        "Petrol",
                        "Diesel",
                        "CNG",
                        "Electric",
                        "Hybrid"
                );

        statusCombo =
                createComboBox(
                        "Active",
                        "Inactive"
                );

        addFormRow(
                form,
                "Customer ID",
                customerIdField,
                0,
                false
        );

        addFormRow(
                form,
                "Owner Name *",
                ownerNameField,
                1,
                true
        );

        addFormRow(
                form,
                "Vehicle Number *",
                vehicleNumberField,
                2,
                true
        );

        addFormRow(
                form,
                "Brand",
                brandField,
                3,
                false
        );

        addFormRow(
                form,
                "Model",
                modelField,
                4,
                false
        );

        addFormRow(
                form,
                "Vehicle Type",
                vehicleTypeCombo,
                5,
                false
        );

        addFormRow(
                form,
                "Fuel Type",
                fuelTypeCombo,
                6,
                false
        );

        addFormRow(
                form,
                "Year",
                yearField,
                7,
                false
        );

        addFormRow(
                form,
                "Status",
                statusCombo,
                8,
                false
        );

        // -----------------------------------------------------
        // SEPARATOR
        // -----------------------------------------------------

        Separator separator =
                new Separator();

        // -----------------------------------------------------
        // BUTTONS
        // -----------------------------------------------------

        HBox buttonBox =
                new HBox(12);

        buttonBox.setAlignment(
                Pos.CENTER_RIGHT
        );

        Button cancelButton =
                new Button(
                        "Cancel"
                );

        styleCancelButton(
                cancelButton
        );

        cancelButton.setOnAction(
                event -> goBack()
        );

        Button saveButton =
                new Button(
                        "Save Vehicle"
                );

        styleSaveButton(
                saveButton
        );

        saveButton.setOnAction(
                event ->
                        saveVehicle()
        );

        buttonBox.getChildren().addAll(
                cancelButton,
                saveButton
        );

        card.getChildren().addAll(
                cardHeader,
                form,
                separator,
                buttonBox
        );

        return card;
    }

    // =========================================================
    // FORM GRID
    // =========================================================

    private GridPane createFormGrid() {

        GridPane form =
                new GridPane();

        form.setHgap(20);
        form.setVgap(16);

        ColumnConstraints labelColumn =
                new ColumnConstraints();

        labelColumn.setPrefWidth(180);

        ColumnConstraints fieldColumn =
                new ColumnConstraints();

        fieldColumn.setPrefWidth(420);

        fieldColumn.setHgrow(
                Priority.ALWAYS
        );

        form.getColumnConstraints()
                .addAll(
                        labelColumn,
                        fieldColumn
                );

        return form;
    }

    // =========================================================
    // FORM ROW
    // =========================================================

    private void addFormRow(
            GridPane form,
            String labelText,
            Control control,
            int row,
            boolean required
    ) {

        Label label =
                new Label(
                        labelText
                );

        label.setTextFill(
                Color.web(HEADING)
        );

        label.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        15
                )
        );

        form.add(
                label,
                0,
                row
        );

        form.add(
                control,
                1,
                row
        );
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

        field.setPrefHeight(
                43
        );

        field.setMaxWidth(
                Double.MAX_VALUE
        );

        field.setStyle(
                "-fx-background-color: " +
                SECONDARY +
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
                "-fx-border-radius: 9;" +
                "-fx-background-radius: 9;" +
                "-fx-padding: 0 14 0 14;" +
                "-fx-font-size: 15px;"
        );

        return field;
    }

    // =========================================================
    // COMBO BOX
    // =========================================================

    private ComboBox<String> createComboBox(
            String... values
    ) {

        ComboBox<String> combo =
                new ComboBox<>();

        combo.getItems().addAll(
                values
        );

        if (
                values.length > 0
        ) {

            combo.setValue(
                    values[0]
            );
        }

        combo.setPrefHeight(
                43
        );

        combo.setMaxWidth(
                Double.MAX_VALUE
        );

        combo.setStyle(
                "-fx-background-color: " +
                SECONDARY +
                ";" +
                "-fx-border-color: " +
                BORDER +
                ";" +
                "-fx-border-radius: 9;" +
                "-fx-background-radius: 9;" +
                "-fx-font-size: 14px;"
        );

        return combo;
    }

    // =========================================================
    // SAVE VEHICLE
    // =========================================================

    private void saveVehicle() {

        // -----------------------------------------------------
        // VALIDATION
        // -----------------------------------------------------

        String ownerName =
                ownerNameField
                        .getText()
                        .trim();

        String vehicleNumber =
                vehicleNumberField
                        .getText()
                        .trim();

        if (
                ownerName.isBlank()
        ) {

            showError(
                    "Validation Error",
                    "Owner name is required."
            );

            ownerNameField.requestFocus();

            return;
        }

        if (
                vehicleNumber.isBlank()
        ) {

            showError(
                    "Validation Error",
                    "Vehicle number is required."
            );

            vehicleNumberField.requestFocus();

            return;
        }

        // -----------------------------------------------------
        // YEAR VALIDATION
        // -----------------------------------------------------

        String year =
                yearField
                        .getText()
                        .trim();

        if (
                !year.isBlank()
        ) {

            try {

                int yearValue =
                        Integer.parseInt(
                                year
                        );

                if (
                        yearValue < 1900 ||
                        yearValue > 2100
                ) {

                    showError(
                            "Invalid Year",
                            "Please enter a valid vehicle year."
                    );

                    yearField.requestFocus();

                    return;
                }

            } catch (NumberFormatException e) {

                showError(
                        "Invalid Year",
                        "Year must contain numbers only."
                );

                yearField.requestFocus();

                return;
            }
        }

        // -----------------------------------------------------
        // CREATE VEHICLE
        // -----------------------------------------------------

        Vehicle vehicle =
                new Vehicle(
                        "",
                        customerIdField
                                .getText()
                                .trim(),

                        ownerName,

                        vehicleNumber,

                        brandField
                                .getText()
                                .trim(),

                        modelField
                                .getText()
                                .trim(),

                        vehicleTypeCombo
                                .getValue(),

                        fuelTypeCombo
                                .getValue(),

                        year,

                        statusCombo
                                .getValue(),

                        String.valueOf(
                                System.currentTimeMillis()
                        )
                );

        // -----------------------------------------------------
        // SAVE TO FIRESTORE
        // -----------------------------------------------------

        try {

            boolean success =
                    controller.addVehicle(
                            vehicle
                    );

            if (
                    success
            ) {

                showSuccess(
                        "Vehicle Added",
                        "Vehicle has been registered successfully."
                );

                goBack();

            } else {

                showError(
                        "Save Failed",
                        "Vehicle could not be added."
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    "Save Failed",
                    e.getMessage()
            );
        }
    }

    // =========================================================
    // BACK NAVIGATION
    // =========================================================

    private void goBack() {

        if (
                onBack != null
        ) {

            onBack.run();
        }
    }

    // =========================================================
    // BACK BUTTON STYLE
    // =========================================================

    private void styleBackButton(
            Button button
    ) {

        button.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        14
                )
        );

        button.setTextFill(
                Color.web(HEADING)
        );

        button.setPadding(
                new Insets(
                        10,
                        15,
                        10,
                        15
                )
        );

        button.setCursor(
                Cursor.HAND
        );

        String normal =
                "-fx-background-color: " +
                SURFACE +
                ";" +
                "-fx-border-color: " +
                BORDER +
                ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;";

        String hover =
                "-fx-background-color: " +
                SECONDARY +
                ";" +
                "-fx-border-color: " +
                BLUE +
                ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;";

        button.setStyle(
                normal
        );

        button.setOnMouseEntered(
                event ->
                        button.setStyle(
                                hover
                        )
        );

        button.setOnMouseExited(
                event ->
                        button.setStyle(
                                normal
                        )
        );
    }

    // =========================================================
    // SAVE BUTTON STYLE
    // =========================================================

    private void styleSaveButton(
            Button button
    ) {

        button.setTextFill(
                Color.WHITE
        );

        button.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        15
                )
        );

        button.setPadding(
                new Insets(
                        11,
                        20,
                        11,
                        20
                )
        );

        button.setCursor(
                Cursor.HAND
        );

        button.setStyle(
                "-fx-background-color: " +
                ORANGE +
                ";" +
                "-fx-background-radius: 9;"
        );

        button.setOnMouseEntered(
                event ->
                        button.setStyle(
                                "-fx-background-color: " +
                                ORANGE_HOVER +
                                ";" +
                                "-fx-background-radius: 9;"
                        )
        );

        button.setOnMouseExited(
                event ->
                        button.setStyle(
                                "-fx-background-color: " +
                                ORANGE +
                                ";" +
                                "-fx-background-radius: 9;"
                        )
        );
    }

    // =========================================================
    // CANCEL BUTTON
    // =========================================================

    private void styleCancelButton(
            Button button
    ) {

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

        button.setPadding(
                new Insets(
                        11,
                        20,
                        11,
                        20
                )
        );

        button.setCursor(
                Cursor.HAND
        );

        String normal =
                "-fx-background-color: " +
                SURFACE +
                ";" +
                "-fx-border-color: " +
                BORDER +
                ";" +
                "-fx-border-radius: 9;" +
                "-fx-background-radius: 9;";

        String hover =
                "-fx-background-color: " +
                SECONDARY +
                ";" +
                "-fx-border-color: " +
                BLUE +
                ";" +
                "-fx-border-radius: 9;" +
                "-fx-background-radius: 9;";

        button.setStyle(
                normal
        );

        button.setOnMouseEntered(
                event ->
                        button.setStyle(
                                hover
                        )
        );

        button.setOnMouseExited(
                event ->
                        button.setStyle(
                                normal
                        )
        );
    }

    // =========================================================
    // SUCCESS ALERT
    // =========================================================

    private void showSuccess(
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

    // =========================================================
    // ERROR ALERT
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
    // ERROR VIEW
    // =========================================================

    private VBox createErrorView(
            String message
    ) {

        VBox box =
                new VBox(12);

        box.setAlignment(
                Pos.CENTER
        );

        box.setPadding(
                new Insets(30)
        );

        Label icon =
                new Label("!");

        icon.setTextFill(
                Color.web(RED)
        );

        icon.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        32
                )
        );

        Label title =
                new Label(
                        "Add Vehicle Error"
                );

        title.setTextFill(
                Color.web(HEADING)
        );

        title.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        20
                )
        );

        Label text =
                new Label(
                        message
                );

        text.setTextFill(
                Color.web(TEXT)
        );

        text.setFont(
                Font.font(
                        "Arial",
                        15
                )
        );

        Button back =
                new Button(
                        "← Back"
                );

        styleBackButton(
                back
        );

        back.setOnAction(
                event ->
                        goBack()
        );

        box.getChildren().addAll(
                icon,
                title,
                text,
                back
        );

        return box;
    }
}