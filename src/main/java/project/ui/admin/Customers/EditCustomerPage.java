package project.ui.admin.Customers;

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

import project.controller.admin.CustomerController;
import project.model.Customer;
import project.util.IconUtil;
import project.ui.admin.DashBoard.AdminSectionPage;

public class EditCustomerPage extends AdminSectionPage {

    // =========================================================
    // AQUA MIST THEME
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
    // DATA
    // =========================================================

    private final Customer customer;

    private final CustomerController controller;

    private final Runnable backAction;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public EditCustomerPage(
            Customer customer,
            Runnable backAction
    ) {

        this.customer = customer;

        this.backAction = backAction;

        CustomerController tempController;

        try {

            tempController =
                    new CustomerController();

        } catch (Exception e) {

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
    // MAIN CONTENT
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
                        "Edit Customer"
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
                        "Update customer information and account status."
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

        var customerIcon =
                IconUtil.createUserIcon(
                        0.85,
                        Color.WHITE.toString()
                );

        javafx.scene.layout.StackPane customerAvatar =
                new javafx.scene.layout.StackPane();

        customerAvatar.setPrefSize(54, 54);
        customerAvatar.setMinSize(54, 54);
        customerAvatar.setMaxSize(54, 54);
        customerAvatar.setStyle(
                "-fx-background-color: " + BLUE + ";" +
                "-fx-background-radius: 27;"
        );
        customerAvatar.getChildren().add(customerIcon);

        titleBox.getChildren().addAll(
                title,
                subtitle
        );

        header.getChildren().addAll(
                backButton,
                customerAvatar,
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
                        "Customer Information"
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
                        "Modify the customer's registered information."
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
                        safe(
                                customer.getName()
                        )
                );

        TextField email =
                createTextField(
                        safe(
                                customer.getEmail()
                        )
                );

        TextField phone =
                createTextField(
                        safe(
                                customer.getPhone()
                        )
                );

        TextField address =
                createTextField(
                        safe(
                                customer.getAddress()
                        )
                );

        TextField city =
                createTextField(
                        safe(
                                customer.getCity()
                        )
                );

        ComboBox<String> status =
                new ComboBox<>();

        status.getItems().addAll(
                "Active",
                "Inactive"
        );

        String currentStatus =
                customer.getStatus();

        if (
                currentStatus == null
                ||
                currentStatus.isBlank()
                ||
                currentStatus.equals("-")
        ) {

            status.setValue(
                    "Active"
            );

        } else {

            status.setValue(
                    currentStatus
            );
        }

        status.setPrefWidth(
                320
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
                        "Status"
                ),
                0,
                5
        );

        form.add(
                status,
                1,
                5
        );

        // =====================================================
        // CUSTOMER ID
        // =====================================================

        Label idLabel =
                new Label(
                        "Customer ID: " +
                        safe(
                                customer.getCustomerId()
                        )
                );

        idLabel.setTextFill(
                Color.web(
                        TEXT
                )
        );

        idLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        13
                )
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

        Button updateButton =
                createButton(
                        "Update Customer",
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

        updateButton.setOnAction(
                event -> {

                    updateCustomer(
                            name,
                            email,
                            phone,
                            address,
                            city,
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
                updateButton
        );

        card.getChildren().addAll(
                heading,
                helper,
                form,
                idLabel,
                buttons
        );

        return card;
    }

    // =========================================================
    // FORM GRID
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
                120
        );

        return label;
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
                value != null
                &&
                !value.equals("-")
        ) {

            field.setText(
                    value
            );
        }

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
    // UPDATE CUSTOMER
    // =========================================================

    private void updateCustomer(
            TextField name,
            TextField email,
            TextField phone,
            TextField address,
            TextField city,
            ComboBox<String> status
    ) {

        String customerName =
                name.getText()
                        .trim();

        String customerEmail =
                email.getText()
                        .trim();

        String customerPhone =
                phone.getText()
                        .trim();

        String customerAddress =
                address.getText()
                        .trim();

        String customerCity =
                city.getText()
                        .trim();

        String customerStatus =
                status.getValue();

        // =====================================================
        // VALIDATION
        // =====================================================

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

        // =====================================================
        // EMAIL VALIDATION
        // =====================================================

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

        // =====================================================
        // PHONE VALIDATION
        // =====================================================

        if (
                customerPhone.length() < 10
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
                    "Customer controller could not be initialized."
            );

            return;
        }

        // =====================================================
        // UPDATE EXISTING CUSTOMER OBJECT
        // =====================================================

        customer.setName(
                customerName
        );

        customer.setEmail(
                customerEmail
        );

        customer.setPhone(
                customerPhone
        );

        customer.setAddress(
                customerAddress
        );

        customer.setCity(
                customerCity
        );

        customer.setStatus(
                customerStatus
        );

        // =====================================================
        // FIREBASE UPDATE
        // =====================================================

        try {

            boolean success =
                    controller.updateCustomer(
                            customer
                    );

            if (
                    success
            ) {

                showInformation(
                        "Customer Updated",
                        "Customer information updated successfully."
                );

                if (
                        backAction != null
                ) {

                    backAction.run();
                }

            } else {

                showError(
                        "Update Failed",
                        "Customer information could not be updated."
                );
            }

        } catch (
                Exception e
        ) {

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
    // SAFE STRING
    // =========================================================

    private String safe(
            String value
    ) {

        return value == null
                ||
                value.isBlank()
                ? "-"
                : value;
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