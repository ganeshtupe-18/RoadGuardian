package project.ui.user;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.shape.SVGPath;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import project.dao.user.VehicleDAO;
import project.util.IconUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VehiclePage {

    // ============================================================
    // COLORS
    // ============================================================

    private static final String MAIN_BACKGROUND   = "#0F0F0F";
    private static final String CARD_SURFACE       = "#1A1A1A";
    private static final String SECONDARY_SURFACE  = "#242424";

    private static final String PRIMARY_ORANGE = "#F59E0B";
    private static final String HEADING        = "#F3F4F6";
    private static final String SECONDARY_TEXT = "#A1A1AA";

    private static final String NAV_BLUE  = "#F59E0B";
    private static final String SUCCESS   = "#22C55E";
    private static final String EMERGENCY = "#EF4444";

    private static final String BORDER = "#F59E0B";

    // ============================================================
    // DATA
    // ============================================================

    private final VehicleDAO vehicleDAO;

    private final List<Vehicle> vehicles =
            new ArrayList<>();

    private final String userEmail;

    private int selectedVehicleIndex = 0;

    private Scene vehicleScene;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    /**
     * Default constructor.
     *
     * Uses the currently logged-in user's email
     * from UserSession.
     */
    public VehiclePage() {
        this(UserSession.getUserEmail());
    }

    /**
     * Constructor with explicit user email.
     *
     * Kept for compatibility with existing navigation/code.
     */
    public VehiclePage(String userEmail) {

        this.userEmail =
                userEmail == null
                        ? ""
                        : userEmail.trim().toLowerCase();

        vehicleDAO =
                new VehicleDAO();

        loadVehicles();
    }

    // ============================================================
    // LOAD VEHICLES FROM FIREBASE
    // ============================================================

    private void loadVehicles() {

        vehicles.clear();

        /*
         * If no user is logged in, do not attempt
         * to read another user's data.
         */
        if (userEmail.isEmpty()) {
            return;
        }

        try {

            List<Map<String, Object>> data =
                    vehicleDAO.getVehicles(userEmail);

            for (Map<String, Object> vehicleData : data) {

                if (vehicleData == null) {
                    continue;
                }

                vehicles.add(
                        Vehicle.fromMap(vehicleData)
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    "Unable to load vehicles",
                    "Vehicle data could not be loaded from Firebase."
            );
        }

        if (!vehicles.isEmpty()) {

            if (selectedVehicleIndex >= vehicles.size()) {
                selectedVehicleIndex = 0;
            }

        } else {

            selectedVehicleIndex = 0;
        }
    }

    // ============================================================
    // PUBLIC SCENE
    // ============================================================

    public Scene getMyVehiclesScene() {

        BorderPane root =
                new BorderPane();

        root.setStyle(
                "-fx-background-color: " +
                MAIN_BACKGROUND + ";"
        );

        HBox header =
                UserHeader.createHeader();

        ScrollPane sidebar =
                UserSideBar.createSidebar(
                        "My Vehicles"
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
                "-fx-background-color: " +
                MAIN_BACKGROUND + ";" +
                "-fx-border-color: transparent;"
        );

        root.setTop(header);
        root.setLeft(sidebar);
        root.setCenter(contentScroll);

        double width =
                UserDashboard.dashboardStage != null
                        ? UserDashboard.dashboardStage.getWidth()
                        : 1200;

        double height =
                UserDashboard.dashboardStage != null
                        ? UserDashboard.dashboardStage.getHeight()
                        : 800;

        vehicleScene =
                new Scene(
                        root,
                        width,
                        height
                );

        addScrollbarStyle(vehicleScene);

        return vehicleScene;
    }

    // ============================================================
    // MAIN CONTENT
    // ============================================================

    private VBox createContent() {

        VBox content =
                new VBox(20);

        content.setPadding(
                new Insets(
                        25,
                        30,
                        30,
                        30
                )
        );

        content.setStyle(
                "-fx-background-color: " +
                MAIN_BACKGROUND + ";"
        );

        content.getChildren().add(
                createHeading()
        );

        if (vehicles.isEmpty()) {

            content.getChildren().add(
                    createEmptyVehicleCard()
            );

            return content;
        }

        content.getChildren().add(
                createUpperSection()
        );

        content.getChildren().add(
                createLowerSection()
        );

        return content;
    }

    // ============================================================
    // HEADING
    // ============================================================

    private HBox createHeading() {

        HBox heading =
                new HBox();

        heading.setAlignment(
                Pos.CENTER_LEFT
        );

        VBox headingText =
                new VBox(2);

        Label title =
                new Label("My Vehicles");

        title.setStyle(
                "-fx-font-size: 28px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " +
                HEADING + ";"
        );

        String subtitleText =
                "Add your vehicle to manage its details";

        if (!vehicles.isEmpty()) {

            Vehicle v =
                    selectedVehicle();

            subtitleText =
                    safe(v.name) +
                    " " +
                    safe(v.model) +
                    " · " +
                    safe(v.registrationNumber);
        }

        Label subtitle =
                new Label(
                        subtitleText
                );

        subtitle.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-text-fill: #A1A1AA;"
        );

        headingText.getChildren().addAll(
                title,
                subtitle
        );

        HBox.setHgrow(
                headingText,
                Priority.ALWAYS
        );

        Button addVehicle =
                new Button(
                        "Add vehicle"
                );

        addVehicle.setPrefHeight(38);

        addVehicle.setPadding(
                new Insets(
                        0,
                        18,
                        0,
                        18
                )
        );

        addVehicle.setStyle(
                "-fx-background-color: #2869e8;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 25;"
        );

        addVehicle.setOnAction(
                e -> showAddVehicleDialog()
        );

        heading.getChildren().addAll(
                headingText,
                addVehicle
        );

        return heading;
    }

    // ============================================================
    // EMPTY VEHICLE
    // ============================================================

    private VBox createEmptyVehicleCard() {

        VBox card =
                new VBox(15);

        card.setAlignment(
                Pos.CENTER
        );

        card.setPadding(
                new Insets(50)
        );

        card.setStyle(
                cardStyle()
        );

        SVGPath icon =
                IconUtil.createVehicleIcon(
                        1.8,
                        PRIMARY_ORANGE
                );

        Label title =
                new Label(
                        "No vehicle added"
                );

        title.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        22
                ));

        title.setTextFill(
                Color.web(HEADING)
        );

        Label description;

        if (userEmail.isEmpty()) {

            description =
                    new Label(
                            "Please login to manage your vehicles."
                    );

        } else {

            description =
                    new Label(
                            "Add your vehicle details to start managing your vehicle."
                    );
        }

        description.setTextFill(
                Color.web(SECONDARY_TEXT)
        );

        Button add =
                new Button(
                        "Add Vehicle"
                );

        add.setStyle(
                "-fx-background-color: " +
                PRIMARY_ORANGE + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 22;" +
                "-fx-padding: 10 25;"
        );

        add.setDisable(
                userEmail.isEmpty()
        );

        add.setOnAction(
                e -> showAddVehicleDialog()
        );

        card.getChildren().addAll(
                icon,
                title,
                description,
                add
        );

        return card;
    }

    // ============================================================
    // UPPER SECTION
    // ============================================================

    private HBox createUpperSection() {

        HBox section =
                new HBox(18);

        VBox vehicleCard =
                createVehicleCard();

        VBox rightCards =
                createRightCards();

        HBox.setHgrow(
                vehicleCard,
                Priority.ALWAYS
        );

        section.getChildren().addAll(
                vehicleCard,
                rightCards
        );

        return section;
    }

    // ============================================================
    // VEHICLE CARD
    // ============================================================

    private VBox createVehicleCard() {

        Vehicle v =
                selectedVehicle();

        VBox card =
                new VBox();

        card.setStyle(
                "-fx-background-color: #1A1A1A;" +
                "-fx-background-radius: 22;" +
                "-fx-border-color: #F59E0B;" +
                "-fx-border-radius: 22;"
        );

        HBox.setHgrow(
                card,
                Priority.ALWAYS
        );

        StackPane imageBox =
                new StackPane();

        imageBox.setPrefHeight(205);
        imageBox.setMinHeight(205);

        imageBox.setStyle(
                "-fx-background-color: #242424;" +
                "-fx-background-radius: 20 20 0 0;"
        );

        SVGPath carIcon =
                IconUtil.createVehicleIcon(
                        2.25,
                        NAV_BLUE
                );

        Label carName =
                new Label(
                        safe(v.name) +
                        " " +
                        safe(v.model)
                );

        carName.setStyle(
                "-fx-font-size: 20px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #71717A;"
        );

        VBox imageContent =
                new VBox(
                        8,
                        carIcon,
                        carName
                );

        imageContent.setAlignment(
                Pos.CENTER
        );

        imageBox.getChildren().add(
                imageContent
        );

        card.getChildren().add(
                imageBox
        );

        VBox info =
                new VBox(13);

        info.setPadding(
                new Insets(18)
        );

        HBox nameRow =
                new HBox();

        nameRow.setAlignment(
                Pos.CENTER_LEFT
        );

        VBox nameBox =
                new VBox(2);

        Label name =
                new Label(
                        safe(v.name) +
                        " " +
                        safe(v.model)
                );

        name.setStyle(
                "-fx-font-size: 17px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " +
                HEADING + ";"
        );

        Label details =
                new Label(
                        safe(v.fuelType) +
                        " · " +
                        v.year +
                        " · " +
                        String.format(
                                "%,d",
                                v.mileage
                        ) +
                        " km"
                );

        details.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: #A1A1AA;"
        );

        nameBox.getChildren().addAll(
                name,
                details
        );

        HBox.setHgrow(
                nameBox,
                Priority.ALWAYS
        );

        Label health =
                new Label(
                        "Health " +
                        v.health +
                        " / 100"
                );

        health.setPadding(
                new Insets(
                        5,
                        11,
                        5,
                        11
                )
        );

        health.setStyle(
                "-fx-background-color: #1C3A2C;" +
                "-fx-background-radius: 15;" +
                "-fx-text-fill: #0c9d77;" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;"
        );

        nameRow.getChildren().addAll(
                nameBox,
                health
        );

        HBox documentCards =
                new HBox(8);

        documentCards.getChildren().addAll(

                smallInfoCard(
                        "▱",
                        "RC",
                        v.rcValidTill,
                        SUCCESS
                ),

                smallInfoCard(
                        "♧",
                        "INSURANCE",
                        v.insuranceExpiry,
                        EMERGENCY
                ),

                smallInfoCard(
                        "♨",
                        "PUC",
                        v.pucExpiry,
                        SUCCESS
                ),

                smallInfoCard(
                        "⚒",
                        "WARRANTY",
                        v.warranty,
                        NAV_BLUE
                )
        );

        info.getChildren().addAll(
                nameRow,
                documentCards
        );

        card.getChildren().add(
                info
        );

        return card;
    }

    // ============================================================
    // RIGHT CARDS
    // ============================================================

    private VBox createRightCards() {

        VBox box =
                new VBox(18);

        box.setPrefWidth(350);

        box.getChildren().addAll(
                createBatteryCard(),
                createTyreCard()
        );

        return box;
    }

    // ============================================================
    // BATTERY
    // ============================================================

    private VBox createBatteryCard() {

        Vehicle v =
                selectedVehicle();

        VBox card =
                new VBox(9);

        card.setPadding(
                new Insets(20)
        );

        card.setPrefHeight(140);

        card.setStyle(
                cardStyle()
        );

        HBox title =
                new HBox(8);

        title.setAlignment(
                Pos.CENTER_LEFT
        );

        Label icon =
                new Label("♧");

        icon.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-text-fill: " +
                NAV_BLUE + ";"
        );

        Label text =
                new Label("Battery");

        text.setStyle(
                "-fx-font-size: 17px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " +
                HEADING + ";"
        );

        title.getChildren().addAll(
                icon,
                text
        );

        Label percentage =
                new Label(
                        v.batteryPercentage + "%"
                );

        percentage.setStyle(
                "-fx-font-size: 27px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " +
                HEADING + ";"
        );

        Label details =
                new Label(
                        safe(v.batteryName) +
                        " · installed " +
                        safe(v.batteryInstallationDate)
                );

        details.setStyle(
                "-fx-font-size: 11px;" +
                "-fx-text-fill: #A1A1AA;"
        );

        StackPane progress =
                new StackPane();

        progress.setPrefHeight(7);
        progress.setMaxWidth(
                Double.MAX_VALUE
        );

        progress.setStyle(
                "-fx-background-color: #242424;" +
                "-fx-background-radius: 10;"
        );

        Region fill =
                new Region();

        fill.setStyle(
                "-fx-background-color: " +
                (v.batteryPercentage < 50
                        ? EMERGENCY
                        : SUCCESS) +
                ";" +
                "-fx-background-radius: 10;"
        );

        fill.prefWidthProperty().bind(
                progress.widthProperty()
                        .multiply(
                                Math.max(
                                        0,
                                        Math.min(
                                                100,
                                                v.batteryPercentage
                                        )
                                ) / 100.0
                        )
        );

        StackPane.setAlignment(
                fill,
                Pos.CENTER_LEFT
        );

        progress.getChildren().add(
                fill
        );

        card.getChildren().addAll(
                title,
                percentage,
                details,
                progress
        );

        return card;
    }

    // ============================================================
    // TYRES
    // ============================================================

    private VBox createTyreCard() {

        Vehicle v =
                selectedVehicle();

        VBox card =
                new VBox(14);

        card.setPadding(
                new Insets(20)
        );

        card.setPrefHeight(200);

        card.setStyle(
                cardStyle()
        );

        HBox title =
                new HBox(8);

        title.setAlignment(
                Pos.CENTER_LEFT
        );

        Label icon =
                new Label("◉");

        icon.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-text-fill: #ff7200;"
        );

        Label text =
                new Label("Tyres");

        text.setStyle(
                "-fx-font-size: 17px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " +
                HEADING + ";"
        );

        title.getChildren().addAll(
                icon,
                text
        );

        GridPane grid =
                new GridPane();

        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(
                tyreBox(
                        "Front left",
                        v.frontLeftPsi
                ),
                0,
                0
        );

        grid.add(
                tyreBox(
                        "Front right",
                        v.frontRightPsi
                ),
                1,
                0
        );

        grid.add(
                tyreBox(
                        "Rear left",
                        v.rearLeftPsi
                ),
                0,
                1
        );

        grid.add(
                tyreBox(
                        "Rear right",
                        v.rearRightPsi
                ),
                1,
                1
        );

        card.getChildren().addAll(
                title,
                grid
        );

        return card;
    }

    // ============================================================
    // LOWER SECTION
    // ============================================================

    private HBox createLowerSection() {

        HBox section =
                new HBox(18);

        VBox maintenance =
                createMaintenanceCard();

        VBox service =
                createServiceCard();

        HBox.setHgrow(
                maintenance,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                service,
                Priority.ALWAYS
        );

        section.getChildren().addAll(
                maintenance,
                service
        );

        return section;
    }

    // ============================================================
    // MAINTENANCE
    // ============================================================

    private VBox createMaintenanceCard() {

        Vehicle v =
                selectedVehicle();

        VBox card =
                createCard(
                        "Maintenance reminders"
                );

        if (!v.insuranceExpiry.isEmpty()) {

            card.getChildren().add(
                    reminder(
                            "Insurance",
                            v.insuranceExpiry,
                            "#3A1C1C",
                            EMERGENCY
                    )
            );
        }

        if (!v.lastServiceDate.isEmpty()) {

            card.getChildren().add(
                    reminder(
                            "Last service",
                            v.lastServiceDate,
                            "#3A3018",
                            "#e8a400"
                    )
            );
        }

        if (v.mileage > 0) {

            card.getChildren().add(
                    reminder(
                            "Current mileage",
                            String.format(
                                    "%,d km",
                                    v.mileage
                            ),
                            "#242424",
                            NAV_BLUE
                    )
            );
        }

        if (card.getChildren().size() == 1) {

            card.getChildren().add(
                    new Label(
                            "No maintenance reminders available."
                    )
            );
        }

        return card;
    }

    // ============================================================
    // SERVICE HISTORY
    // ============================================================

    private VBox createServiceCard() {

        Vehicle v =
                selectedVehicle();

        VBox card =
                createCard(
                        "Service history timeline"
                );

        if (v.id.isEmpty()) {

            card.getChildren().add(
                    new Label(
                            "Service history unavailable."
                    )
            );

            return card;
        }

        try {

            List<Map<String, Object>> history =
                    vehicleDAO.getServiceHistory(
                            userEmail,
                            v.id
                    );

            if (history.isEmpty()) {

                card.getChildren().add(
                        new Label(
                                "No service history available."
                        )
                );

            } else {

                for (Map<String, Object> item :
                        history) {

                    String title =
                            value(
                                    item,
                                    "title"
                            );

                    String date =
                            value(
                                    item,
                                    "date"
                            );

                    String garage =
                            value(
                                    item,
                                    "garage"
                            );

                    String amount =
                            value(
                                    item,
                                    "amount"
                            );

                    String details =
                            date +
                            " · " +
                            garage;

                    if (!amount.isEmpty()) {
                        details +=
                                " · ₹" +
                                amount;
                    }

                    card.getChildren().add(
                            serviceItem(
                                    title,
                                    details
                            )
                    );
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

            card.getChildren().add(
                    new Label(
                            "Unable to load service history."
                    )
            );
        }

        return card;
    }

    // ============================================================
    // GENERIC CARD
    // ============================================================

    private VBox createCard(
            String title
    ) {

        VBox card =
                new VBox(14);

        card.setPadding(
                new Insets(18)
        );

        card.setStyle(
                cardStyle()
        );

        Label titleLabel =
                new Label(title);

        titleLabel.setStyle(
                "-fx-font-size: 17px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " +
                HEADING + ";"
        );

        card.getChildren().add(
                titleLabel
        );

        return card;
    }

    // ============================================================
    // SMALL INFO CARD
    // ============================================================

    private VBox smallInfoCard(
            String icon,
            String title,
            String value,
            String color
    ) {

        VBox card =
                new VBox(4);

        card.setPadding(
                new Insets(10)
        );

        card.setPrefHeight(75);

        HBox.setHgrow(
                card,
                Priority.ALWAYS
        );

        card.setStyle(
                "-fx-background-color: #1A1A1A;" +
                "-fx-border-color: #F59E0B;" +
                "-fx-border-radius: 20;" +
                "-fx-background-radius: 20;"
        );

        Label iconLabel =
                new Label(icon);

        iconLabel.setStyle(
                "-fx-font-size: 15px;" +
                "-fx-text-fill: " +
                color + ";"
        );

        Label titleLabel =
                new Label(title);

        titleLabel.setStyle(
                "-fx-font-size: 10px;" +
                "-fx-text-fill: #A1A1AA;"
        );

        Label valueLabel =
                new Label(
                        value.isEmpty()
                                ? "Not available"
                                : value
                );

        valueLabel.setStyle(
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #F3F4F6;"
        );

        card.getChildren().addAll(
                iconLabel,
                titleLabel,
                valueLabel
        );

        return card;
    }

    // ============================================================
    // TYRE BOX
    // ============================================================

    private VBox tyreBox(
            String position,
            String psi
    ) {

        VBox box =
                new VBox(2);

        box.setPrefWidth(145);
        box.setPrefHeight(55);

        box.setPadding(
                new Insets(9)
        );

        box.setStyle(
                "-fx-background-color: #242424;" +
                "-fx-background-radius: 18;"
        );

        Label positionLabel =
                new Label(position);

        positionLabel.setStyle(
                "-fx-font-size: 10px;" +
                "-fx-text-fill: #A1A1AA;"
        );

        Label psiLabel =
                new Label(
                        psi.isEmpty()
                                ? "35 PSI"
                                : psi
                );

        psiLabel.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #F3F4F6;"
        );

        box.getChildren().addAll(
                positionLabel,
                psiLabel
        );

        return box;
    }

    // ============================================================
    // REMINDER
    // ============================================================

    private VBox reminder(
            String title,
            String description,
            String background,
            String color
    ) {

        VBox box =
                new VBox(3);

        box.setPadding(
                new Insets(
                        12,
                        15,
                        12,
                        15
                )
        );

        box.setStyle(
                "-fx-background-color: " +
                background +
                ";" +
                "-fx-background-radius: 20;"
        );

        Label titleLabel =
                new Label(title);

        titleLabel.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " +
                color + ";"
        );

        Label descriptionLabel =
                new Label(description);

        descriptionLabel.setWrapText(true);

        descriptionLabel.setStyle(
                "-fx-font-size: 11px;" +
                "-fx-text-fill: " +
                color + ";"
        );

        box.getChildren().addAll(
                titleLabel,
                descriptionLabel
        );

        return box;
    }

    // ============================================================
    // SERVICE ITEM
    // ============================================================

    private HBox serviceItem(
            String title,
            String details
    ) {

        Label icon =
                new Label("●");

        icon.setStyle(
                "-fx-font-size: 8px;" +
                "-fx-text-fill: #60A5FA;"
        );

        HBox iconBox =
                new HBox(icon);

        iconBox.setAlignment(
                Pos.CENTER
        );

        iconBox.setPrefWidth(28);

        VBox text =
                new VBox(2);

        Label titleLabel =
                new Label(
                        title.isEmpty()
                                ? "Service"
                                : title
                );

        titleLabel.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #F3F4F6;"
        );

        Label detailsLabel =
                new Label(details);

        detailsLabel.setStyle(
                "-fx-font-size: 10px;" +
                "-fx-text-fill: #A1A1AA;"
        );

        text.getChildren().addAll(
                titleLabel,
                detailsLabel
        );

        return new HBox(
                10,
                iconBox,
                text
        );
    }

    // ============================================================
    // ADD VEHICLE
    // ============================================================

    private void showAddVehicleDialog() {

        if (userEmail.isEmpty()) {

            showError(
                    "User not logged in",
                    "Vehicle cannot be added because no logged-in user was found."
            );

            return;
        }

        Dialog<ButtonType> dialog =
                new Dialog<>();

        dialog.setTitle(
                "Add Vehicle"
        );

        dialog.setHeaderText(
                "Enter your vehicle details"
        );

        TextField make =
                field("e.g. Honda");

        TextField model =
                field("e.g. City ZX");

        TextField registration =
                field("e.g. MH 12 AB 1234");

        TextField vin =
                field("VIN number");

        ComboBox<String> fuel =
                new ComboBox<>();

        fuel.getItems().addAll(
                "Petrol",
                "Diesel",
                "Electric",
                "CNG",
                "Hybrid"
        );

        fuel.setValue("Petrol");
        fuel.setMaxWidth(
                Double.MAX_VALUE
        );

        ComboBox<String> transmission =
                new ComboBox<>();

        transmission.getItems().addAll(
                "Manual",
                "Automatic",
                "AMT",
                "CVT"
        );

        transmission.setValue("Manual");

        transmission.setMaxWidth(
                Double.MAX_VALUE
        );

        TextField year =
                field("e.g. 2024");

        TextField mileage =
                field("e.g. 12000");

        TextField colour =
                field("e.g. Pearl White");

        TextField owner =
                field("Registered owner");

        TextField insurance =
                field("Policy number");

        TextField insuranceExpiry =
                field("e.g. 31 Dec 2026");

        TextField lastService =
                field("e.g. 15 Jul 2026");

        GridPane form =
                new GridPane();

        form.setHgap(18);
        form.setVgap(12);

        form.setPadding(
                new Insets(
                        15,
                        5,
                        5,
                        5
                )
        );

        addRow(
                form,
                0,
                "Make *",
                make,
                "Model *",
                model
        );

        addRow(
                form,
                1,
                "Registration *",
                registration,
                "VIN",
                vin
        );

        addRow(
                form,
                2,
                "Fuel type",
                fuel,
                "Transmission",
                transmission
        );

        addRow(
                form,
                3,
                "Manufacturing year *",
                year,
                "Current km *",
                mileage
        );

        addRow(
                form,
                4,
                "Colour",
                colour,
                "Registered owner",
                owner
        );

        addRow(
                form,
                5,
                "Insurance policy",
                insurance,
                "Insurance expiry",
                insuranceExpiry
        );

        form.addRow(
                6,
                new Label("Last service"),
                lastService
        );

        dialog.getDialogPane().setContent(
                form
        );

        dialog.getDialogPane().setPrefWidth(
                820
        );

        dialog.getDialogPane().getButtonTypes()
                .addAll(
                        ButtonType.CANCEL,
                        ButtonType.OK
                );

        dialog.showAndWait()
                .filter(
                        ButtonType.OK::equals
                )
                .ifPresent(
                        ignored ->
                                saveVehicle(
                                        make,
                                        model,
                                        registration,
                                        vin,
                                        fuel,
                                        transmission,
                                        year,
                                        mileage,
                                        colour,
                                        owner,
                                        insurance,
                                        insuranceExpiry,
                                        lastService
                                )
                );
    }

    // ============================================================
    // SAVE VEHICLE TO FIREBASE
    // ============================================================

    private void saveVehicle(
            TextField make,
            TextField model,
            TextField registration,
            TextField vin,
            ComboBox<String> fuel,
            ComboBox<String> transmission,
            TextField year,
            TextField mileage,
            TextField colour,
            TextField owner,
            TextField insurance,
            TextField insuranceExpiry,
            TextField lastService
    ) {

        if (userEmail.isEmpty()) {

            showError(
                    "User not logged in",
                    "Vehicle cannot be saved because no logged-in user was found."
            );

            return;
        }

        try {

            String makeText =
                    make.getText().trim();

            String modelText =
                    model.getText().trim();

            String registrationText =
                    registration.getText()
                            .trim()
                            .toUpperCase();

            int yearValue =
                    Integer.parseInt(
                            year.getText().trim()
                    );

            int mileageValue =
                    Integer.parseInt(
                            mileage.getText().trim()
                    );

            if (
                    makeText.isEmpty() ||
                    modelText.isEmpty() ||
                    registrationText.isEmpty() ||
                    yearValue < 1900 ||
                    yearValue > 2100 ||
                    mileageValue < 0
            ) {

                throw new IllegalArgumentException();
            }

            Map<String, Object> data =
                    new HashMap<>();

            data.put(
                    "name",
                    makeText
            );

            data.put(
                    "model",
                    modelText
            );

            data.put(
                    "registrationNumber",
                    registrationText
            );

            data.put(
                    "vin",
                    vin.getText().trim()
            );

            data.put(
                    "fuelType",
                    fuel.getValue()
            );

            data.put(
                    "transmission",
                    transmission.getValue()
            );

            data.put(
                    "year",
                    yearValue
            );

            data.put(
                    "mileage",
                    mileageValue
            );

            data.put(
                    "colour",
                    colour.getText().trim()
            );

            data.put(
                    "owner",
                    owner.getText().trim()
            );

            data.put(
                    "insurancePolicyNumber",
                    insurance.getText().trim()
            );

            data.put(
                    "insuranceExpiry",
                    insuranceExpiry.getText().trim()
            );

            data.put(
                    "lastServiceDate",
                    lastService.getText().trim()
            );

            /*
             * These are initial vehicle-health values.
             * They are not fake vehicle identity details.
             */
            data.put(
                    "health",
                    0
            );

            data.put(
                    "batteryPercentage",
                    0
            );

            data.put(
                    "batteryName",
                    ""
            );

            data.put(
                    "batteryInstallationDate",
                    ""
            );

            data.put(
                    "frontLeftPsi",
                    ""
            );

            data.put(
                    "frontRightPsi",
                    ""
            );

            data.put(
                    "rearLeftPsi",
                    ""
            );

            data.put(
                    "rearRightPsi",
                    ""
            );

            data.put(
                    "rcValidTill",
                    ""
            );

            data.put(
                    "pucExpiry",
                    ""
            );

            data.put(
                    "warranty",
                    ""
            );

            String vehicleId =
                    vehicleDAO.addVehicle(
                            userEmail,
                            data
                    );

            System.out.println(
                    "Vehicle saved with ID: " +
                    vehicleId
            );

            loadVehicles();

            refreshScene();

            showInfo(
                    "Vehicle Added",
                    "Vehicle has been saved successfully."
            );

        } catch (NumberFormatException e) {

            showError(
                    "Invalid details",
                    "Year and mileage must contain valid numbers."
            );

        } catch (IllegalArgumentException e) {

            showError(
                    "Invalid details",
                    "Please enter make, model, registration, valid year and mileage."
            );

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    "Firebase Error",
                    "Vehicle could not be saved."
            );
        }
    }

    // ============================================================
    // FIELD
    // ============================================================

    private TextField field(
            String prompt
    ) {

        TextField field =
                new TextField();

        field.setPromptText(prompt);

        return field;
    }

    // ============================================================
    // FORM ROW
    // ============================================================

    private void addRow(
            GridPane form,
            int row,
            String label1,
            javafx.scene.Node field1,
            String label2,
            javafx.scene.Node field2
    ) {

        form.addRow(
                row,
                new Label(label1),
                field1,
                new Label(label2),
                field2
        );
    }

    // ============================================================
    // REFRESH SCENE
    // ============================================================

    private void refreshScene() {

        if (vehicleScene == null) {
            return;
        }

        BorderPane root =
                new BorderPane();

        root.setStyle(
                "-fx-background-color: " +
                MAIN_BACKGROUND + ";"
        );

        HBox header =
                UserHeader.createHeader();

        ScrollPane sidebar =
                UserSideBar.createSidebar(
                        "My Vehicles"
                );

        VBox content =
                createContent();

        ScrollPane scroll =
                new ScrollPane(content);

        scroll.setFitToWidth(true);

        scroll.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        scroll.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.ALWAYS
        );

        scroll.setStyle(
                "-fx-background-color: " +
                MAIN_BACKGROUND + ";" +
                "-fx-border-color: transparent;"
        );

        root.setTop(header);
        root.setLeft(sidebar);
        root.setCenter(scroll);

        vehicleScene.setRoot(root);

        addScrollbarStyle(
                vehicleScene
        );
    }

    // ============================================================
    // SELECTED VEHICLE
    // ============================================================

    private Vehicle selectedVehicle() {

        if (vehicles.isEmpty()) {
            throw new IllegalStateException(
                    "No vehicle is available."
            );
        }

        if (selectedVehicleIndex < 0 ||
                selectedVehicleIndex >= vehicles.size()) {

            selectedVehicleIndex = 0;
        }

        return vehicles.get(
                selectedVehicleIndex
        );
    }

    // ============================================================
    // MAP VALUE
    // ============================================================

    private static String value(
            Map<String, Object> map,
            String key
    ) {

        if (map == null) {
            return "";
        }

        Object value =
                map.get(key);

        return value == null
                ? ""
                : String.valueOf(value);
    }

    private static String safe(
            String value
    ) {

        return value == null
                ? ""
                : value;
    }

    // ============================================================
    // ERROR
    // ============================================================

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
        alert.setContentText(message);

        if (vehicleScene != null &&
                vehicleScene.getWindow() != null) {

            alert.initOwner(
                    vehicleScene.getWindow()
            );
        }

        alert.showAndWait();
    }

    // ============================================================
    // INFORMATION
    // ============================================================

    private void showInfo(
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

        if (vehicleScene != null &&
                vehicleScene.getWindow() != null) {

            alert.initOwner(
                    vehicleScene.getWindow()
            );
        }

        alert.showAndWait();
    }

    // ============================================================
    // CARD STYLE
    // ============================================================

    private String cardStyle() {

        return
                "-fx-background-color: #1A1A1A;" +
                "-fx-border-color: #F59E0B;" +
                "-fx-border-radius: 20;" +
                "-fx-background-radius: 20;";
    }

    // ============================================================
    // SCROLLBAR
    // ============================================================

    private void addScrollbarStyle(
            Scene scene
    ) {

        String css =
                "data:text/css," +

                ".scroll-bar:vertical {" +
                "-fx-background-color: #242424;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 2;" +
                "}" +

                ".scroll-bar:vertical .track {" +
                "-fx-background-color: #242424;" +
                "-fx-background-radius: 8;" +
                "}" +

                ".scroll-bar:vertical .thumb {" +
                "-fx-background-color: #71717A;" +
                "-fx-background-radius: 8;" +
                "}" +

                ".scroll-bar:vertical .thumb:hover {" +
                "-fx-background-color: #2B2B2B;" +
                "-fx-background-radius: 8;" +
                "}" +

                ".scroll-bar:vertical .increment-button," +
                ".scroll-bar:vertical .decrement-button {" +
                "-fx-background-color: transparent;" +
                "-fx-padding: 0;" +
                "}" +

                ".scroll-bar:horizontal {" +
                "-fx-opacity: 0;" +
                "-fx-max-height: 0;" +
                "-fx-pref-height: 0;" +
                "}";

        scene.getStylesheets().add(css);
    }

    // ============================================================
    // VEHICLE MODEL
    // ============================================================

    private static class Vehicle {

        String id = "";

        String name = "";
        String model = "";
        String registrationNumber = "";

        String fuelType = "";
        String transmission = "";

        int year = 0;
        int mileage = 0;

        String colour = "";
        String vin = "";
        String owner = "";

        String insurancePolicyNumber = "";
        String insuranceExpiry = "";
        String lastServiceDate = "";

        int health = 55;

        int batteryPercentage = 67;

        String batteryName = "";
        String batteryInstallationDate = "";

        String frontLeftPsi = "";
        String frontRightPsi = "";
        String rearLeftPsi = "";
        String rearRightPsi = "";

        String rcValidTill = "";
        String pucExpiry = "";
        String warranty = "";

        // ========================================================
        // FIREBASE MAP -> VEHICLE
        // ========================================================

        static Vehicle fromMap(
                Map<String, Object> data
        ) {

            Vehicle v =
                    new Vehicle();

            if (data == null) {
                return v;
            }

            v.id =
                    string(
                            data.get("id")
                    );

            v.name =
                    string(
                            data.get("name")
                    );

            v.model =
                    string(
                            data.get("model")
                    );

            v.registrationNumber =
                    string(
                            data.get(
                                    "registrationNumber"
                            )
                    );

            v.fuelType =
                    string(
                            data.get("fuelType")
                    );

            v.transmission =
                    string(
                            data.get("transmission")
                    );

            v.year =
                    integer(
                            data.get("year")
                    );

            v.mileage =
                    integer(
                            data.get("mileage")
                    );

            v.colour =
                    string(
                            data.get("colour")
                    );

            v.vin =
                    string(
                            data.get("vin")
                    );

            v.owner =
                    string(
                            data.get("owner")
                    );

            v.insurancePolicyNumber =
                    string(
                            data.get(
                                    "insurancePolicyNumber"
                            )
                    );

            v.insuranceExpiry =
                    string(
                            data.get(
                                    "insuranceExpiry"
                            )
                    );

            v.lastServiceDate =
                    string(
                            data.get(
                                    "lastServiceDate"
                            )
                    );

            v.health =
                    integer(
                            data.get("health")
                    );

            v.batteryPercentage =
                    integer(
                            data.get(
                                    "batteryPercentage"
                            )
                    );

            v.batteryName =
                    string(
                            data.get(
                                    "batteryName"
                            )
                    );

            v.batteryInstallationDate =
                    string(
                            data.get(
                                    "batteryInstallationDate"
                            )
                    );

            v.frontLeftPsi =
                    string(
                            data.get(
                                    "frontLeftPsi"
                            )
                    );

            v.frontRightPsi =
                    string(
                            data.get(
                                    "frontRightPsi"
                            )
                    );

            v.rearLeftPsi =
                    string(
                            data.get(
                                    "rearLeftPsi"
                            )
                    );

            v.rearRightPsi =
                    string(
                            data.get(
                                    "rearRightPsi"
                            )
                    );

            v.rcValidTill =
                    string(
                            data.get(
                                    "rcValidTill"
                            )
                    );

            v.pucExpiry =
                    string(
                            data.get(
                                    "pucExpiry"
                            )
                    );

            v.warranty =
                    string(
                            data.get(
                                    "warranty"
                            )
                    );

            return v;
        }

        private static String string(
                Object value
        ) {

            return value == null
                    ? ""
                    : String.valueOf(value);
        }

        private static int integer(
                Object value
        ) {

            if (value instanceof Number) {

                return (
                        (Number) value
                ).intValue();
            }

            try {

                return Integer.parseInt(
                        String.valueOf(value)
                );

            } catch (Exception e) {

                return 0;
            }
        }
    }
}