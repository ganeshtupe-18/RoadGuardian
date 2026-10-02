package project.ui.admin.Vehicles;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.control.Control;

import com.google.cloud.firestore.Firestore;

import project.controller.admin.VehicleController;
import project.firebase.FirebaseConfig;
import project.model.Vehicle;
import project.ui.admin.DashBoard.AdminSectionPage;

public class EditVehiclePage extends AdminSectionPage {

    // =========================================================
    // THEME
    // =========================================================

    private static final String BG = "#0F0F0F";
    private static final String SURFACE = "#1A1A1A";
    private static final String SECONDARY = "#242424";

    private static final String ORANGE = "#F59E0B";
    private static final String ORANGE_HOVER = "#D97706";

    private static final String BLUE = "#F59E0B";
    private static final String BLUE_HOVER = "#F59E0B";

    private static final String GREEN = "#22C55E";
    private static final String RED = "#EF4444";

    private static final String HEADING = "#F3F4F6";
    private static final String TEXT = "#A1A1AA";
    private static final String MUTED = "#A1A1AA";
    private static final String WHITE = "#1A1A1A";
    private static final String BORDER = "#F59E0B";

    // =========================================================
    // DATA
    // =========================================================

    private final Vehicle vehicle;
    private final Runnable onBack;

    private VehicleController controller;

    // =========================================================
    // FIELDS
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

    public EditVehiclePage(
            Vehicle vehicle,
            Runnable onBack
    ) {

        this.vehicle = vehicle;
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

        HBox header =
                createHeader();

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
                        "Edit Vehicle"
                );

        title.setStyle(
                "-fx-text-fill: " + HEADING + ";" +
                "-fx-font-size: 32px;" +
                "-fx-font-weight: bold;"
        );

        Label subtitle =
                new Label(
                        "Update the registered vehicle information."
                );

        subtitle.setStyle(
                "-fx-text-fill: " + TEXT + ";" +
                "-fx-font-size: 16px;"
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

        // =====================================================
        // HEADER
        // =====================================================

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
                        "Modify the vehicle information and save the updated details."
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

        // =====================================================
        // FORM
        // =====================================================

        GridPane form =
                createFormGrid();

        customerIdField =
                createTextField(
                        safe(
                                vehicle.getCustomerId()
                        )
                );

        ownerNameField =
                createTextField(
                        safe(
                                vehicle.getOwnerName()
                        )
                );

        vehicleNumberField =
                createTextField(
                        safe(
                                vehicle.getVehicleNumber()
                        )
                );

        brandField =
                createTextField(
                        safe(
                                vehicle.getBrand()
                        )
                );

        modelField =
                createTextField(
                        safe(
                                vehicle.getModel()
                        )
                );

        yearField =
                createTextField(
                        safe(
                                vehicle.getYear()
                        )
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

        // Set existing values

        setComboValue(
                vehicleTypeCombo,
                vehicle.getVehicleType()
        );

        setComboValue(
                fuelTypeCombo,
                vehicle.getFuelType()
        );

        setComboValue(
                statusCombo,
                vehicle.getStatus()
        );

        // =====================================================
        // FORM ROWS
        // =====================================================

        addFormRow(
                form,
                "Customer ID",
                customerIdField,
                0
        );

        addFormRow(
                form,
                "Owner Name *",
                ownerNameField,
                1
        );

        addFormRow(
                form,
                "Vehicle Number *",
                vehicleNumberField,
                2
        );

        addFormRow(
                form,
                "Brand",
                brandField,
                3
        );

        addFormRow(
                form,
                "Model",
                modelField,
                4
        );

        addFormRow(
                form,
                "Vehicle Type",
                vehicleTypeCombo,
                5
        );

        addFormRow(
                form,
                "Fuel Type",
                fuelTypeCombo,
                6
        );

        addFormRow(
                form,
                "Year",
                yearField,
                7
        );

        addFormRow(
                form,
                "Status",
                statusCombo,
                8
        );

        Separator separator =
                new Separator();

        // =====================================================
        // BUTTONS
        // =====================================================

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

        Button updateButton =
                new Button(
                        "Update Vehicle"
                );

        styleUpdateButton(
                updateButton
        );

        updateButton.setOnAction(
                event ->
                        updateVehicle()
        );

        buttonBox.getChildren().addAll(
                cancelButton,
                updateButton
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
    // ADD FORM ROW
    // =========================================================

    private void addFormRow(
            GridPane form,
            String labelText,
            Control control,
            int row
    ) {

        Label label =
                new Label(
                        labelText
                );

        label.setStyle(
                "-fx-text-fill: " + HEADING + ";" +
                "-fx-font-size: 15px;" +
                "-fx-font-weight: bold;"
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
            String value
    ) {

        TextField field =
                new TextField();

        if (
                value != null &&
                !value.equals("-")
        ) {

            field.setText(
                    value
            );
        }

        field.setPrefHeight(
                43
        );

        field.setMaxWidth(
                Double.MAX_VALUE
        );

        field.setStyle(
                "-fx-background-color: " +
                WHITE +
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

        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll(values);
        combo.setPrefHeight(43);
        combo.setMaxWidth(Double.MAX_VALUE);

        combo.setStyle(
                "-fx-background-color: " + WHITE + ";" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 9;" +
                "-fx-background-radius: 9;" +
                "-fx-font-size: 14px;"
        );

        combo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setStyle("-fx-text-fill: " + HEADING + "; -fx-font-size: 14px;");
            }
        });

        combo.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setStyle("-fx-text-fill: " + HEADING + "; -fx-background-color: #1A1A1A; -fx-font-size: 14px;");
            }
        });

        return combo;
    }

    // =========================================================
    // SET COMBO VALUE
    // =========================================================

    private void setComboValue(
            ComboBox<String> combo,
            String value
    ) {

        if (
                value == null ||
                value.isBlank()
        ) {

            if (
                    !combo.getItems().isEmpty()
            ) {

                combo.setValue(
                        combo.getItems().get(0)
                );
            }

            return;
        }

        if (
                combo.getItems().contains(
                        value
                )
        ) {

            combo.setValue(
                    value
            );

        } else {

            combo.getItems().add(
                    value
            );

            combo.setValue(
                    value
            );
        }
    }

    // =========================================================
    // UPDATE VEHICLE
    // =========================================================

    private void updateVehicle() {

        String ownerName =
                ownerNameField
                        .getText()
                        .trim();

        String vehicleNumber =
                vehicleNumberField
                        .getText()
                        .trim();

        // =====================================================
        // VALIDATION
        // =====================================================

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

        // =====================================================
        // YEAR VALIDATION
        // =====================================================

        String year =
                yearField
                        .getText()
                        .trim();

        if (
                !year.isBlank() &&
                !year.equals("-")
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

            } catch (
                    NumberFormatException e
            ) {

                showError(
                        "Invalid Year",
                        "Year must contain numbers only."
                );

                yearField.requestFocus();

                return;
            }
        }

        // =====================================================
        // CUSTOMER VALIDATION + OWNER RESOLUTION
        // =====================================================

        String customerId = customerIdField.getText().trim();

        if (customerId.isBlank()) {
            showError("Validation Error", "Customer ID is required.");
            customerIdField.requestFocus();
            return;
        }

        if (!controller.customerExists(customerId)) {
            showError(
                    "Customer Not Found",
                    "Enter a valid registered Customer ID / customer email."
            );
            customerIdField.requestFocus();
            return;
        }

        if (ownerName.isBlank()) {
            ownerName = controller.getCustomerName(customerId);
            ownerNameField.setText(ownerName);
        }

        // =====================================================
        // UPDATE EXISTING OBJECT
        // =====================================================

        vehicle.setCustomerId(
                customerIdField
                        .getText()
                        .trim()
        );

        vehicle.setOwnerName(
                ownerName
        );

        vehicle.setVehicleNumber(
                vehicleNumber
        );

        vehicle.setBrand(
                brandField
                        .getText()
                        .trim()
        );

        vehicle.setModel(
                modelField
                        .getText()
                        .trim()
        );

        vehicle.setVehicleType(
                vehicleTypeCombo
                        .getValue()
        );

        vehicle.setFuelType(
                fuelTypeCombo
                        .getValue()
        );

        vehicle.setYear(
                year
        );

        vehicle.setStatus(
                statusCombo
                        .getValue()
        );

        // =====================================================
        // FIREBASE UPDATE
        // =====================================================

        try {

            boolean success =
                    controller.updateVehicle(
                            vehicle
                    );

            if (
                    success
            ) {

                showSuccess(
                        "Vehicle Updated",
                        "Vehicle information updated successfully."
                );

                goBack();

            } else {

                showError(
                        "Update Failed",
                        controller.getLastError() == null
                                ? "Vehicle could not be updated."
                                : controller.getLastError()
                );
            }

        } catch (
                Exception e
        ) {

            e.printStackTrace();

            showError(
                    "Update Failed",
                    e.getMessage()
            );
        }
    }

    // =========================================================
    // BACK
    // =========================================================

    private void goBack() {

        if (
                onBack != null
        ) {

            onBack.run();
        }
    }

    // =========================================================
    // BACK BUTTON
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
                WHITE +
                ";" +
                "-fx-text-fill: " + HEADING + ";" +
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
    // UPDATE BUTTON
    // =========================================================

    private void styleUpdateButton(
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
                WHITE +
                ";" +
                "-fx-text-fill: " + HEADING + ";" +
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
    // SAFE
    // =========================================================

    private String safe(
            String value
    ) {

        return value == null ||
               value.isBlank()
                ? "-"
                : value;
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
                        "Edit Vehicle Error"
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