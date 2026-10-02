package project.ui.user;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.Cursor;
import javafx.stage.Stage;

import project.controller.user.TowTruckController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TowTruckPage {

    // ============================================================
    // COLORS
    // ============================================================

    private static final String BG     = "#0F0F0F";
    private static final String WHITE  = "#1A1A1A";
    private static final String CARD   = "#1A1A1A";
    private static final String BLUE   = "#F59E0B";
    private static final String DARK   = "#F3F4F6";
    private static final String GREY   = "#A1A1AA";
    private static final String BORDER = "#F59E0B";
    private static final String ORANGE = "#F59E0B";

    // ============================================================
    // CONTROLLER
    // ============================================================

    private final TowTruckController controller;

    // ============================================================
    // SCENE
    // ============================================================

    private Scene scene;

    // ============================================================
    // FIREBASE DATA
    // ============================================================

    private List<Map<String, Object>> towTrucks =
            new ArrayList<>();

    private List<Map<String, Object>> customerVehicles =
            new ArrayList<>();

    // ============================================================
    // TOW REQUEST INPUTS
    // ============================================================

    private ComboBox<String> vehicleComboBox;
    private TextField pickupLocationField;
    private TextField destinationField;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public TowTruckPage() {

        controller =
                new TowTruckController();

        buildUI();
    }

    // ============================================================
    // PUBLIC SCENE
    // ============================================================

    public Scene getTowTruckScene() {

        return scene;
    }

    // ============================================================
    // BUILD UI
    // ============================================================

    private void buildUI() {

        // ========================================================
        // HEADER
        // ========================================================

        HBox header =
                UserHeader.createHeader();

        // ========================================================
        // SIDEBAR
        // ========================================================

        ScrollPane sidebar =
                UserSideBar.createSidebar(
                        "Tow Truck"
                );

        // ========================================================
        // CONTENT
        // ========================================================

        VBox content =
                new VBox(18);

        content.setPadding(
                new Insets(
                        24,
                        26,
                        30,
                        26
                )
        );

        content.setFillWidth(true);

        content.setBackground(
                bg(
                        BG,
                        CornerRadii.EMPTY
                )
        );

        // ========================================================
        // HEADING
        // ========================================================

        Label title =
                new Label(
                        "Tow Truck Assistance"
                );

        title.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        34
                )
        );

        title.setTextFill(
                Color.web(DARK)
        );

        Label subtitle =
                new Label(
                        "Book an available RoadGuardian tow partner using your real vehicle and location details"
                );

        subtitle.setFont(
                Font.font(
                        "Arial",
                        16
                )
        );

        subtitle.setTextFill(
                Color.web(GREY)
        );

        VBox heading =
                new VBox(
                        4,
                        title,
                        subtitle
                );

        // ========================================================
        // MAIN
        // ========================================================

        HBox main =
                new HBox(30);

        main.setMaxWidth(
                Double.MAX_VALUE
        );

        main.setAlignment(
                Pos.TOP_LEFT
        );

        main.setFillHeight(true);

        // ========================================================
        // LEFT COLUMN
        // ========================================================

        VBox leftColumn =
                new VBox(18);

        leftColumn.setPrefWidth(850);
        leftColumn.setMinWidth(850);
        leftColumn.setMaxWidth(850);

        // ========================================================
        // MAP
        // ========================================================

        VBox mapBox =
                createMapBox();

        mapBox.setPrefWidth(850);
        mapBox.setMinWidth(850);
        mapBox.setMaxWidth(850);

        // ========================================================
        // CUSTOMER VEHICLES
        // ========================================================

        loadCustomerVehicles();

        // ========================================================
        // TOW DETAILS / DESTINATION
        // ========================================================

        VBox destinationBox =
                createDestinationBox();

        destinationBox.setPrefWidth(850);
        destinationBox.setMinWidth(850);
        destinationBox.setMaxWidth(850);

        leftColumn.getChildren().addAll(
                mapBox,
                destinationBox
        );

        // ========================================================
        // TRUCK CARDS
        // ========================================================

        VBox truckCards =
                new VBox(14);

        truckCards.setPrefWidth(370);
        truckCards.setMinWidth(370);
        truckCards.setMaxWidth(370);

        truckCards.setFillWidth(true);

        // ========================================================
        // LOAD FIREBASE DATA
        // ========================================================

        try {

            towTrucks =
                    controller
                            .getAvailableTowTrucks();

            if (towTrucks.isEmpty()) {

                Label noTruck =
                        new Label(
                                "No tow trucks are currently available."
                        );

                noTruck.setWrapText(true);

                noTruck.setFont(
                        Font.font(
                                "Arial",
                                FontWeight.BOLD,
                                15
                        )
                );

                noTruck.setTextFill(
                        Color.web(DARK)
                );

                noTruck.setPadding(
                        new Insets(20)
                );

                truckCards
                        .getChildren()
                        .add(noTruck);

            } else {

                for (
                        Map<String, Object> truck :
                        towTrucks
                ) {

                    truckCards
                            .getChildren()
                            .add(
                                    createTruckCard(
                                            truck
                                    )
                            );
                }
            }

        } catch (Exception exception) {

            exception.printStackTrace();

            Label error =
                    new Label(
                            "Unable to load tow truck data."
                    );

            error.setWrapText(true);

            error.setFont(
                    Font.font(
                            "Arial",
                            FontWeight.BOLD,
                            15
                    )
            );

            error.setTextFill(
                    Color.web("#EF4444")
            );

            error.setPadding(
                    new Insets(20)
            );

            truckCards
                    .getChildren()
                    .add(error);
        }

        // ========================================================
        // MAIN
        // ========================================================

        main.getChildren().addAll(
                leftColumn,
                truckCards
        );

        content.getChildren().addAll(
                heading,
                main
        );

        // ========================================================
        // SCROLL
        // ========================================================

        ScrollPane dashboardScroll =
                new ScrollPane(content);

        dashboardScroll.setFitToWidth(true);

        dashboardScroll.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        dashboardScroll.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.ALWAYS
        );

        dashboardScroll.setStyle(
                "-fx-background-color: " + BG + ";" +
                "-fx-background: " + BG + ";" +
                "-fx-control-inner-background: " + BG + ";" +
                "-fx-border-color: transparent;" +
                "-fx-padding: 0;"
        );

        HBox.setHgrow(
                dashboardScroll,
                Priority.ALWAYS
        );

        // ========================================================
        // BODY
        // ========================================================

        HBox body =
                new HBox();

        body.setFillHeight(true);

        body.setBackground(
                bg(
                        BG,
                        CornerRadii.EMPTY
                )
        );

        body.getChildren().addAll(
                sidebar,
                dashboardScroll
        );

        HBox.setHgrow(
                dashboardScroll,
                Priority.ALWAYS
        );

        // ========================================================
        // ROOT
        // ========================================================

        VBox root =
                new VBox(
                        header,
                        body
                );

        VBox.setVgrow(
                body,
                Priority.ALWAYS
        );

        scene =
                new Scene(
                        root,
                        UserDashboard.dashboardStage.getWidth(),
                        UserDashboard.dashboardStage.getHeight()
                );
    }

    // ============================================================
    // MAP
    // ============================================================

    private VBox createMapBox() {

        VBox box =
                new VBox(16);

        box.setPrefHeight(210);
        box.setMinHeight(210);
        box.setMaxHeight(210);

        box.setPrefWidth(850);
        box.setMinWidth(850);
        box.setMaxWidth(850);

        box.setPadding(
                new Insets(24)
        );

        box.setBackground(
                bg(
                        CARD,
                        new CornerRadii(25)
                )
        );

        box.setBorder(
                border(
                        BORDER,
                        25,
                        1
                )
        );

        HBox top =
                new HBox(14);

        top.setAlignment(
                Pos.CENTER_LEFT
        );

        Circle iconCircle =
                new Circle(
                        24,
                        Color.web("#FFF0E7")
                );

        Label icon =
                new Label("▱");

        icon.setStyle(
                "-fx-text-fill: " + ORANGE + ";" +
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;"
        );

        StackPane iconBox =
                new StackPane(
                        iconCircle,
                        icon
                );

        VBox titleBox =
                new VBox(4);

        Label title =
                new Label(
                        "Tow Truck Service Flow"
                );

        title.setStyle(
                "-fx-text-fill: " + DARK + ";" +
                "-fx-font-size: 20px;" +
                "-fx-font-weight: bold;"
        );

        Label subtitle =
                new Label(
                        "No fake map position or ETA is generated. Truck cards below show only values stored in Firebase."
                );

        subtitle.setWrapText(true);
        subtitle.setStyle(
                "-fx-text-fill: " + GREY + ";" +
                "-fx-font-size: 12px;"
        );

        titleBox.getChildren().addAll(
                title,
                subtitle
        );

        top.getChildren().addAll(
                iconBox,
                titleBox
        );

        Label flow =
                new Label(
                        "Customer booking  →  serviceRequests  →  Admin  →  Mechanic  →  Completed / Cancelled  →  History"
                );

        flow.setWrapText(true);
        flow.setStyle(
                "-fx-background-color: white;" +
                "-fx-text-fill: #F3F4F6;" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 12;" +
                "-fx-background-radius: 12;" +
                "-fx-padding: 14;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;"
        );

        Label note =
                new Label(
                        "For actual distance and live ETA, GPS / map integration is required later. This page does not invent those values."
                );

        note.setWrapText(true);
        note.setStyle(
                "-fx-text-fill: " + GREY + ";" +
                "-fx-font-size: 11px;"
        );

        box.getChildren().addAll(
                top,
                flow,
                note
        );

        return box;
    }

    // ============================================================
    // TRUCK MARKERS
    // ============================================================

    private void addTruckMarkers(
            StackPane map
    ) {

        if (towTrucks.isEmpty()) {
            return;
        }

        int count =
                Math.min(
                        towTrucks.size(),
                        3
                );

        for (
                int i = 0;
                i < count;
                i++
        ) {

            StackPane truck =
                    truckMarker();

            if (i == 0) {

                StackPane.setAlignment(
                        truck,
                        Pos.TOP_CENTER
                );

                StackPane.setMargin(
                        truck,
                        new Insets(
                                55,
                                0,
                                0,
                                0
                        )
                );

            } else if (i == 1) {

                StackPane.setAlignment(
                        truck,
                        Pos.CENTER_LEFT
                );

                StackPane.setMargin(
                        truck,
                        new Insets(
                                0,
                                155,
                                0,
                                0
                        )
                );

            } else {

                StackPane.setAlignment(
                        truck,
                        Pos.CENTER_RIGHT
                );

                StackPane.setMargin(
                        truck,
                        new Insets(
                                0,
                                205,
                                0,
                                0
                        )
                );
            }

            map.getChildren().add(
                    truck
            );

            String eta =
                    getString(
                            towTrucks.get(i),
                            "eta",
                            "--"
                    );

            Label time =
                    mapLabel(eta);

            if (i == 0) {

                StackPane.setAlignment(
                        time,
                        Pos.TOP_CENTER
                );

                StackPane.setMargin(
                        time,
                        new Insets(
                                105,
                                0,
                                0,
                                0
                        )
                );

            } else if (i == 1) {

                StackPane.setAlignment(
                        time,
                        Pos.CENTER_LEFT
                );

                StackPane.setMargin(
                        time,
                        new Insets(
                                80,
                                0,
                                0,
                                140
                        )
                );

            } else {

                StackPane.setAlignment(
                        time,
                        Pos.CENTER_RIGHT
                );

                StackPane.setMargin(
                        time,
                        new Insets(
                                80,
                                165,
                                0,
                                0
                        )
                );
            }

            map.getChildren().add(
                    time
            );
        }
    }

    private StackPane truckMarker() {

        Circle outer =
                new Circle(
                        27
                );

        outer.setFill(
                Color.TRANSPARENT
        );

        outer.setStroke(
                Color.WHITE
        );

        outer.setStrokeWidth(4);

        Circle inner =
                new Circle(
                        22,
                        Color.web(ORANGE)
                );

        Label icon =
                new Label("▱");

        icon.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        17
                )
        );

        icon.setTextFill(
                Color.WHITE
        );

        return new StackPane(
                outer,
                inner,
                icon
        );
    }

    private Label mapLabel(
            String text
    ) {

        Label label =
                new Label(text);

        label.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        13
                )
        );

        label.setTextFill(
                Color.web(DARK)
        );

        label.setPadding(
                new Insets(
                        7,
                        13,
                        7,
                        13
                )
        );

        label.setBackground(
                bg(
                        WHITE,
                        new CornerRadii(15)
                )
        );

        return label;
    }

    // ============================================================
    // DESTINATION
    // ============================================================

    private VBox createDestinationBox() {

        VBox outer =
                new VBox(14);

        outer.setPadding(
                new Insets(20)
        );

        outer.setPrefHeight(320);
        outer.setMinHeight(320);

        outer.setBackground(
                bg(
                        CARD,
                        new CornerRadii(22)
                )
        );

        outer.setBorder(
                border(
                        BORDER,
                        22,
                        1
                )
        );

        Label title =
                new Label(
                        "Tow request details"
                );

        title.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        21
                )
        );

        title.setTextFill(
                Color.web(DARK)
        );

        Label note =
                new Label(
                        "Select the vehicle and enter the real pickup and drop-off locations. "
                                + "The same request will be visible to Admin and the assigned Mechanic."
                );

        note.setWrapText(true);
        note.setTextFill(Color.web(GREY));
        note.setFont(Font.font("Arial", 12));

        // ========================================================
        // VEHICLE
        // ========================================================

        Label vehicleLabel =
                formLabel(
                        "Vehicle"
                );

        vehicleComboBox =
                new ComboBox<>();

        vehicleComboBox.setMaxWidth(
                Double.MAX_VALUE
        );

        vehicleComboBox.setPrefHeight(42);

        vehicleComboBox.setPromptText(
                customerVehicles.isEmpty()
                        ? "No registered vehicle found"
                        : "Select your vehicle"
        );

        vehicleComboBox.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;" +
                "-fx-font-size: 13px;"
        );

        for (Map<String, Object> vehicle : customerVehicles) {

            vehicleComboBox
                    .getItems()
                    .add(
                            buildVehicleLabel(vehicle)
                    );
        }

        if (!vehicleComboBox.getItems().isEmpty()) {

            vehicleComboBox
                    .getSelectionModel()
                    .selectFirst();
        }

        // ========================================================
        // PICKUP LOCATION
        // ========================================================

        Label pickupLabel =
                formLabel(
                        "Pickup / breakdown location"
                );

        pickupLocationField =
                createTowTextField(
                        "Example: NH-48, Exit 12B, Pune"
                );

        // ========================================================
        // DESTINATION
        // ========================================================

        Label destinationLabel =
                formLabel(
                        "Drop-off destination"
                );

        destinationField =
                createTowTextField(
                        "Example: RoadGuardian Service Center, Kothrud"
                );

        outer.getChildren().addAll(
                title,
                note,
                vehicleLabel,
                vehicleComboBox,
                pickupLabel,
                pickupLocationField,
                destinationLabel,
                destinationField
        );

        return outer;
    }

    // ============================================================
    // FORM LABEL
    // ============================================================

    private Label formLabel(
            String text
    ) {

        Label label =
                new Label(text);

        label.setStyle(
                "-fx-text-fill: " + DARK + ";" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;"
        );

        return label;
    }

    // ============================================================
    // TOW TEXT FIELD
    // ============================================================

    private TextField createTowTextField(
            String prompt
    ) {

        TextField field =
                new TextField();

        field.setPromptText(prompt);
        field.setPrefHeight(42);
        field.setMaxWidth(Double.MAX_VALUE);

        field.setStyle(
                "-fx-background-color: white;" +
                "-fx-text-fill: #F3F4F6;" +
                "-fx-prompt-text-fill: #9A9AA3;" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;" +
                "-fx-font-size: 13px;" +
                "-fx-padding: 0 12 0 12;"
        );

        return field;
    }

    // ============================================================
    // TRUCK CARD
    // ============================================================

    private VBox createTruckCard(
            Map<String, Object> truck
    ) {

        String truckId =
                getString(
                        truck,
                        "truckId",
                        ""
                );

        String name =
                getString(
                        truck,
                        "name",
                        "Tow Truck"
                );

        String company =
                getString(
                        truck,
                        "company",
                        "RoadGuardian Partner"
                );

        String eta =
                getString(
                        truck,
                        "eta",
                        "--"
                );

        String distance =
                getString(
                        truck,
                        "distance",
                        "--"
                );

        String charge =
                getString(
                        truck,
                        "charge",
                        "--"
                );

        String rating =
                getString(
                        truck,
                        "rating",
                        "--"
                );

        String suitable =
                getString(
                        truck,
                        "suitable",
                        "All vehicles"
                );

        VBox card =
                new VBox(12);

        card.setPadding(
                new Insets(20)
        );

        card.setPrefHeight(220);
        card.setMinHeight(220);
        card.setMaxHeight(220);

        card.setBackground(
                bg(
                        CARD,
                        new CornerRadii(22)
                )
        );

        card.setBorder(
                border(
                        BORDER,
                        22,
                        1
                )
        );

        // ========================================================
        // TOP
        // ========================================================

        HBox top =
                new HBox(12);

        top.setAlignment(
                Pos.CENTER_LEFT
        );

        Circle iconCircle =
                new Circle(
                        22,
                        Color.web("#FFF0E7")
                );

        Label truckIcon =
                new Label("▱");

        truckIcon.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        16
                )
        );

        truckIcon.setTextFill(
                Color.web(ORANGE)
        );

        StackPane icon =
                new StackPane(
                        iconCircle,
                        truckIcon
                );

        Label nameLabel =
                new Label(name);

        nameLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        18
                )
        );

        nameLabel.setTextFill(
                Color.web(DARK)
        );

        Label companyLabel =
                new Label(company);

        companyLabel.setFont(
                Font.font(
                        "Arial",
                        13
                )
        );

        companyLabel.setTextFill(
                Color.web(GREY)
        );

        VBox nameBox =
                new VBox(
                        3,
                        nameLabel,
                        companyLabel
                );

        Region space =
                new Region();

        HBox.setHgrow(
                space,
                Priority.ALWAYS
        );

        Label star =
                new Label(
                        "★ " + rating
                );

        star.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        15
                )
        );

        star.setTextFill(
                Color.web(ORANGE)
        );

        top.getChildren().addAll(
                icon,
                nameBox,
                space,
                star
        );

        // ========================================================
        // STATS
        // ========================================================

        HBox stats =
                new HBox(10);

        stats.getChildren().add(
                statBox(
                        "◷",
                        eta,
                        "ETA"
                )
        );

        stats.getChildren().add(
                statBox(
                        "⌖",
                        distance,
                        "Distance"
                )
        );

        stats.getChildren().add(
                statBox(
                        "₹",
                        charge,
                        "Charge"
                )
        );

        // ========================================================
        // SUITABLE
        // ========================================================

        Label suitableLabel =
                new Label(
                        "Suitable for: " + suitable
                );

        suitableLabel.setFont(
                Font.font(
                        "Arial",
                        13
                )
        );

        suitableLabel.setTextFill(
                Color.web(GREY)
        );

        // ========================================================
        // BOOK BUTTON
        // ========================================================

        Button book =
                new Button(
                        "Book now"
                );

        book.setMaxWidth(
                Double.MAX_VALUE
        );

        book.setPrefHeight(40);

        book.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        14
                )
        );

        book.setTextFill(
                Color.WHITE
        );

        book.setStyle(
                "-fx-background-color: " + BLUE + ";" +
                "-fx-background-radius: 22;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-cursor: hand;"
        );

        book.setCursor(
                Cursor.HAND
        );

        book.setOnMouseEntered(
                e -> book.setStyle(
                        "-fx-background-color: #1D4ED8;" +
                        "-fx-background-radius: 22;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;"
                )
        );

        book.setOnMouseExited(
                e -> book.setStyle(
                        "-fx-background-color: " + BLUE + ";" +
                        "-fx-background-radius: 22;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;"
                )
        );

        book.setOnAction(
                e -> handleBookTruck(
                        truck,
                        book
                )
        );

        card.getChildren().addAll(
                top,
                stats,
                suitableLabel,
                book
        );

        return card;
    }

    // ============================================================
    // BOOK TOW TRUCK
    // ============================================================

    private void handleBookTruck(
            Map<String, Object> truck,
            Button bookButton
    ) {

        String truckId =
                getString(
                        truck,
                        "truckId",
                        ""
                );

        String truckName =
                getString(
                        truck,
                        "name",
                        "Tow Truck"
                );

        String company =
                getString(
                        truck,
                        "company",
                        "RoadGuardian Partner"
                );

        String charge =
                getString(
                        truck,
                        "charge",
                        ""
                );

        String mechanicId =
                firstMapString(
                        truck,
                        "mechanicId",
                        "driverId",
                        "assignedMechanicId"
                );

        String mechanicName =
                firstMapString(
                        truck,
                        "mechanicName",
                        "driverName",
                        "assignedMechanicName"
                );

        // ========================================================
        // SELECTED VEHICLE
        // ========================================================

        int selectedVehicleIndex =
                vehicleComboBox == null
                        ? -1
                        : vehicleComboBox
                                .getSelectionModel()
                                .getSelectedIndex();

        if (selectedVehicleIndex < 0
                || selectedVehicleIndex >= customerVehicles.size()) {

            showError(
                    "Please select a registered vehicle before booking a tow truck."
            );

            return;
        }

        Map<String, Object> selectedVehicle =
                customerVehicles.get(
                        selectedVehicleIndex
                );

        String vehicleId =
                firstMapString(
                        selectedVehicle,
                        "vehicleId",
                        "id"
                );

        String vehicleNumber =
                firstMapString(
                        selectedVehicle,
                        "vehicleNumber",
                        "registrationNumber"
                );

        String pickupLocation =
                clean(
                        pickupLocationField == null
                                ? null
                                : pickupLocationField.getText()
                );

        String destination =
                clean(
                        destinationField == null
                                ? null
                                : destinationField.getText()
                );

        if (pickupLocation == null) {

            showError(
                    "Please enter your pickup / breakdown location."
            );

            return;
        }

        if (destination == null) {

            showError(
                    "Please enter the drop-off destination."
            );

            return;
        }

        Alert confirmation =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        confirmation.setTitle(
                "Confirm Tow Truck"
        );

        confirmation.setHeaderText(
                "Book " + truckName + "?"
        );

        confirmation.setContentText(
                "Vehicle: " + firstNonBlank(vehicleNumber, vehicleId, "Selected vehicle") +
                "\nCompany: " + company +
                "\nPickup: " + pickupLocation +
                "\nDestination: " + destination +
                "\nEstimated charge: ₹" + firstNonBlank(charge, "Not provided") +
                "\n\nThis creates one canonical service request visible to Customer, Admin and Mechanic."
        );

        if (
                confirmation.showAndWait()
                        .orElse(
                                ButtonType.CANCEL
                        )
                        != ButtonType.OK
        ) {

            return;
        }

        try {

            String userId =
                    UserSession.getUserId();

            String userName =
                    UserSession.getUserName();

            String userEmail =
                    UserSession.getUserEmail();

            if (clean(userId) == null
                    && clean(userEmail) == null) {

                showError(
                        "User session not found. Please login again."
                );

                return;
            }

            String requestId =
                    controller.bookTowTruck(
                            userId,
                            userName,
                            userEmail,
                            vehicleId,
                            vehicleNumber,
                            truckId,
                            truckName,
                            company,
                            pickupLocation,
                            destination,
                            charge,
                            mechanicId,
                            mechanicName
                    );

            bookButton.setText(
                    "Booked"
            );

            bookButton.setDisable(true);

            Alert success =
                    new Alert(
                            Alert.AlertType.INFORMATION
                    );

            success.setTitle(
                    "Tow Truck Booked"
            );

            success.setHeaderText(
                    "Tow service request created"
            );

            success.setContentText(
                    "The same request is now available in the RoadGuardian service flow."
                            + "\n\nRequest ID: "
                            + requestId
                            + "\nStatus: "
                            + (clean(mechanicId) == null ? "Pending" : "Assigned")
            );

            success.showAndWait();

        } catch (Exception exception) {

            exception.printStackTrace();

            showError(
                    "Unable to book tow truck.\n\n"
                            + exception.getMessage()
            );
        }
    }

    // ============================================================
    // LOAD CUSTOMER VEHICLES
    // ============================================================

    private void loadCustomerVehicles() {

        customerVehicles.clear();

        try {

            String email =
                    clean(
                            UserSession.getUserEmail()
                    );

            if (email == null) {
                return;
            }

            List<Map<String, Object>> vehicles =
                    controller
                            .getCustomerVehicles(email);

            if (vehicles != null) {

                for (Map<String, Object> vehicle : vehicles) {

                    if (vehicle != null) {
                        customerVehicles.add(vehicle);
                    }
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // ============================================================
    // VEHICLE LABEL
    // ============================================================

    private String buildVehicleLabel(
            Map<String, Object> vehicle
    ) {

        String number =
                firstMapString(
                        vehicle,
                        "vehicleNumber",
                        "registrationNumber"
                );

        String brand =
                firstMapString(
                        vehicle,
                        "brand",
                        "name",
                        "make"
                );

        String model =
                firstMapString(
                        vehicle,
                        "model"
                );

        StringBuilder label =
                new StringBuilder();

        label.append(
                firstNonBlank(
                        number,
                        "Vehicle"
                )
        );

        String details =
                clean(
                        (brand == null ? "" : brand)
                                + " "
                                + (model == null ? "" : model)
                );

        if (details != null) {
            label.append(" • ").append(details);
        }

        return label.toString();
    }

    // ============================================================
    // FIRST MAP STRING
    // ============================================================

    private String firstMapString(
            Map<String, Object> data,
            String... keys
    ) {

        if (data == null || keys == null) {
            return null;
        }

        for (String key : keys) {

            Object value =
                    data.get(key);

            String text =
                    clean(
                            value == null
                                    ? null
                                    : String.valueOf(value)
                    );

            if (text != null) {
                return text;
            }
        }

        return null;
    }

    // ============================================================
    // FIRST NON BLANK
    // ============================================================

    private String firstNonBlank(
            String... values
    ) {

        if (values == null) {
            return null;
        }

        for (String value : values) {

            String text = clean(value);

            if (text != null) {
                return text;
            }
        }

        return null;
    }

    // ============================================================
    // CLEAN
    // ============================================================

    private String clean(
            String value
    ) {

        if (value == null) {
            return null;
        }

        String cleaned =
                value.trim();

        return cleaned.isEmpty()
                ? null
                : cleaned;
    }

    // ============================================================
    // GET STRING FROM FIREBASE DATA
    // ============================================================

    private String getString(
            Map<String, Object> data,
            String key,
            String defaultValue
    ) {

        if (data == null) {
            return defaultValue;
        }

        Object value =
                data.get(key);

        if (value == null) {
            return defaultValue;
        }

        return String.valueOf(value);
    }

    // ============================================================
    // STAT BOX
    // ============================================================

    private VBox statBox(
            String icon,
            String value,
            String title
    ) {

        VBox box =
                new VBox(3);

        box.setAlignment(
                Pos.CENTER
        );

        box.setPrefWidth(120);
        box.setPrefHeight(72);
        box.setMinWidth(0);

        box.setBackground(
                bg(
                        "#F0F5F8",
                        new CornerRadii(22)
                )
        );

        Label iconLabel =
                new Label(icon);

        iconLabel.setFont(
                Font.font(
                        "Arial",
                        18
                )
        );

        iconLabel.setTextFill(
                Color.web(GREY)
        );

        Label valueLabel =
                new Label(value);

        valueLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        15
                )
        );

        valueLabel.setTextFill(
                Color.web(DARK)
        );

        Label titleLabel =
                new Label(title);

        titleLabel.setFont(
                Font.font(
                        "Arial",
                        12
                )
        );

        titleLabel.setTextFill(
                Color.web(GREY)
        );

        box.getChildren().addAll(
                iconLabel,
                valueLabel,
                titleLabel
        );

        return box;
    }

    // ============================================================
    // ERROR
    // ============================================================

    private void showError(
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle(
                "Tow Truck"
        );

        alert.setHeaderText(null);

        alert.setContentText(
                message
        );

        alert.showAndWait();
    }

    // ============================================================
    // BACKGROUND HELPER
    // ============================================================

    private Background bg(
            String color,
            CornerRadii radius
    ) {

        return new Background(
                new BackgroundFill(
                        Color.web(color),
                        radius,
                        Insets.EMPTY
                )
        );
    }

    // ============================================================
    // BORDER HELPER
    // ============================================================

    private Border border(
            String color,
            double radius,
            double width
    ) {

        return new Border(
                new BorderStroke(
                        Color.web(color),
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(radius),
                        new BorderWidths(width)
                )
        );
    }
}