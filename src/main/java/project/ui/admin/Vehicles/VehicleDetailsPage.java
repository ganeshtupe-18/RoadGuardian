package project.ui.admin.Vehicles;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import project.model.Vehicle;
import project.ui.admin.DashBoard.AdminSectionPage;

public class VehicleDetailsPage extends AdminSectionPage {

    // =========================================================
    // THEME
    // =========================================================

    private static final String BG = "#0F0F0F";
    private static final String SURFACE = "#1A1A1A";
    private static final String SECONDARY = "#242424";

    private static final String BLUE = "#F59E0B";
    private static final String BLUE_HOVER = "#F59E0B";

    private static final String GREEN = "#22C55E";
    private static final String RED = "#EF4444";
    private static final String ORANGE = "#F59E0B";

    private static final String HEADING = "#F3F4F6";
    private static final String TEXT = "#A1A1AA";
    private static final String WHITE = "#1A1A1A";
    private static final String BORDER = "#F59E0B";

    // =========================================================
    // DATA
    // =========================================================

    private final Vehicle vehicle;

    private final Runnable backAction;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public VehicleDetailsPage(
            Vehicle vehicle,
            Runnable backAction
    ) {

        this.vehicle = vehicle;
        this.backAction = backAction;
    }

    // =========================================================
    // GET VIEW
    // =========================================================

    @Override
    public VBox getView() {

        VBox root = new VBox();

        root.setPadding(
                new Insets(
                        30,
                        32,
                        32,
                        32
                )
        );

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

        HBox header =
                createHeader();

        VBox detailsCard =
                createDetailsCard();

        VBox.setVgrow(
                detailsCard,
                Priority.ALWAYS
        );

        content.getChildren().addAll(
                header,
                detailsCard
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
                        "Vehicle Details"
                );

        title.setStyle(
                "-fx-text-fill: " + HEADING + ";" +
                "-fx-font-size: 30px;" +
                "-fx-font-weight: bold;"
        );

        Label subtitle =
                new Label(
                        "Complete information about the registered vehicle."
                );

        subtitle.setStyle(
                "-fx-text-fill: " + TEXT + ";" +
                "-fx-font-size: 15px;"
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
    // DETAILS CARD
    // =========================================================

    private VBox createDetailsCard() {

        VBox card =
                new VBox(22);

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

        HBox profile =
                createProfile();

        Separator separator1 =
                new Separator();

        Label informationTitle =
                new Label(
                        "Vehicle Information"
                );

        informationTitle.setStyle(
                "-fx-text-fill: " + HEADING + ";" +
                "-fx-font-size: 20px;" +
                "-fx-font-weight: bold;"
        );

        HBox row1 =
                new HBox(15);

        VBox owner =
                createInfoBox(
                        "OWNER NAME",
                        vehicle.getOwnerName()
                );

        VBox customerId =
                createInfoBox(
                        "CUSTOMER ID",
                        vehicle.getCustomerId()
                );

        HBox.setHgrow(
                owner,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                customerId,
                Priority.ALWAYS
        );

        row1.getChildren().addAll(
                owner,
                customerId
        );

        HBox row2 =
                new HBox(15);

        VBox brand =
                createInfoBox(
                        "BRAND",
                        vehicle.getBrand()
                );

        VBox model =
                createInfoBox(
                        "MODEL",
                        vehicle.getModel()
                );

        HBox.setHgrow(
                brand,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                model,
                Priority.ALWAYS
        );

        row2.getChildren().addAll(
                brand,
                model
        );

        HBox row3 =
                new HBox(15);

        VBox vehicleType =
                createInfoBox(
                        "VEHICLE TYPE",
                        vehicle.getVehicleType()
                );

        VBox fuelType =
                createInfoBox(
                        "FUEL TYPE",
                        vehicle.getFuelType()
                );

        HBox.setHgrow(
                vehicleType,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                fuelType,
                Priority.ALWAYS
        );

        row3.getChildren().addAll(
                vehicleType,
                fuelType
        );

        HBox row4 =
                new HBox(15);

        VBox year =
                createInfoBox(
                        "MANUFACTURING YEAR",
                        vehicle.getYear()
                );

        VBox status =
                createStatusBox(
                        vehicle.getStatus()
                );

        HBox.setHgrow(
                year,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                status,
                Priority.ALWAYS
        );

        row4.getChildren().addAll(
                year,
                status
        );

        Separator separator2 =
                new Separator();

        HBox bottom =
                createBottom();

        card.getChildren().addAll(
                profile,
                separator1,
                informationTitle,
                row1,
                row2,
                row3,
                row4,
                separator2,
                bottom
        );

        return card;
    }

    // =========================================================
    // PROFILE
    // =========================================================

    private HBox createProfile() {

        HBox profile =
                new HBox(16);

        profile.setAlignment(
                Pos.CENTER_LEFT
        );

        StackPane avatar =
                createVehicleAvatar();

        VBox text =
                new VBox(5);

        Label vehicleNumber =
                new Label(
                        safe(
                                vehicle.getVehicleNumber()
                        )
                );

        vehicleNumber.setStyle(
                "-fx-text-fill: " + HEADING + ";" +
                "-fx-font-size: 25px;" +
                "-fx-font-weight: bold;"
        );

        Label vehicleName =
                new Label(
                        safe(
                                vehicle.getBrand()
                        )
                        +
                        " "
                        +
                        safe(
                                vehicle.getModel()
                        )
                );

        vehicleName.setStyle(
                "-fx-text-fill: #A1A1AA;" +
                "-fx-font-size: 15px;" +
                "-fx-font-weight: bold;"
        );

        text.getChildren().addAll(
                vehicleNumber,
                vehicleName
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Label status =
                createStatusBadge(
                        vehicle.getStatus()
                );

        profile.getChildren().addAll(
                avatar,
                text,
                spacer,
                status
        );

        return profile;
    }

    // =========================================================
    // VEHICLE AVATAR
    // =========================================================

    private StackPane createVehicleAvatar() {

        Circle circle =
                new Circle(
                        30,
                        Color.web(
                                ORANGE
                        )
                );

        Label icon =
                new Label(
                        "▰"
                );

        icon.setTextFill(
                Color.WHITE
        );

        icon.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        18
                )
        );

        StackPane avatar =
                new StackPane(
                        circle,
                        icon
                );

        avatar.setMinSize(
                60,
                60
        );

        avatar.setPrefSize(
                60,
                60
        );

        avatar.setMaxSize(
                60,
                60
        );

        return avatar;
    }

    // =========================================================
    // INFO BOX
    // =========================================================

    private VBox createInfoBox(
            String title,
            String value
    ) {

        VBox box = new VBox(7);
        box.setPadding(new Insets(15));
        box.setMinHeight(82);

        box.setStyle(
                "-fx-background-color: " + WHITE + ";" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;"
        );

        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-text-fill: " + TEXT + ";" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;"
        );

        Label valueLabel = new Label(safe(value));
        valueLabel.setWrapText(true);
        valueLabel.setStyle(
                "-fx-text-fill: " + HEADING + ";" +
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;"
        );

        box.getChildren().addAll(titleLabel, valueLabel);
        return box;
    }

    // =========================================================
    // STATUS BOX
    // =========================================================

    private VBox createStatusBox(
            String statusValue
    ) {

        VBox box =
                new VBox(7);

        box.setPadding(
                new Insets(
                        15
                )
        );

        box.setMinHeight(
                82
        );

        box.setStyle(
                "-fx-background-color: " +
                SECONDARY +
                ";" +
                "-fx-border-color: " +
                BORDER +
                ";" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;"
        );

        Label title =
                new Label(
                        "STATUS"
                );

        title.setStyle(
                "-fx-text-fill: " + TEXT + ";" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;"
        );

        Label status =
                createStatusBadge(
                        statusValue
                );

        box.getChildren().addAll(
                title,
                status
        );

        return box;
    }

    // =========================================================
    // STATUS BADGE
    // =========================================================

    private Label createStatusBadge(
            String status
    ) {

        String safeStatus =
                safe(status);

        Label badge =
                new Label(
                        safeStatus
                );

        badge.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        13
                )
        );

        badge.setPadding(
                new Insets(
                        7,
                        13,
                        7,
                        13
                )
        );

        if (
                "Active".equalsIgnoreCase(
                        safeStatus
                )
        ) {

            badge.setTextFill(
                    Color.web(
                            GREEN
                    )
            );

            badge.setStyle(
                    "-fx-text-fill: " + GREEN + ";" +
                    "-fx-background-color: " + GREEN + "18;" +
                    "-fx-background-radius: 20;"
            );

        } else {

            badge.setTextFill(
                    Color.web(
                            RED
                    )
            );

            badge.setStyle(
                    "-fx-text-fill: " + RED + ";" +
                    "-fx-background-color: " + RED + "18;" +
                    "-fx-background-radius: 20;"
            );
        }

        return badge;
    }

    // =========================================================
    // BOTTOM
    // =========================================================

    private HBox createBottom() {

        HBox bottom =
                new HBox(12);

        bottom.setAlignment(
                Pos.CENTER_LEFT
        );

        Label databaseId =
                new Label(
                        "Vehicle ID: " +
                        safe(
                                vehicle.getVehicleId()
                        )
                );

        databaseId.setStyle(
                "-fx-text-fill: #A1A1AA;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;"
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Button backButton =
                createButton(
                        "← Back to Vehicles",
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

        bottom.getChildren().addAll(
                databaseId,
                spacer,
                backButton
        );

        return bottom;
    }

    // =========================================================
    // BUTTON
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

        button.setPadding(
                new Insets(
                        10,
                        18,
                        10,
                        18
                )
        );

        button.setCursor(
                Cursor.HAND
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

                    button.setStyle(
                            "-fx-background-color: " +
                            BLUE_HOVER +
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
}