package project.ui.admin.SOSRequests;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import project.model.SOSRequest;
import project.ui.admin.DashBoard.AdminSectionPage;

public class SOSRequestDetailsPage extends AdminSectionPage {

    // =====================================================
    // THEME
    // =====================================================

    private static final String BG = "#0F0F0F";
    private static final String SURFACE = "#1A1A1A";
    private static final String SECONDARY = "#242424";

    private static final String HEADING = "#F3F4F6";
    private static final String TEXT = "#A1A1AA";

    private static final String BLUE = "#F59E0B";
    private static final String ORANGE = "#F59E0B";
    private static final String GREEN = "#22C55E";
    private static final String RED = "#EF4444";

    private static final String BORDER = "#F59E0B";

    // =====================================================
    // DATA
    // =====================================================

    private final SOSRequest request;

    private Runnable onBack;
    private Runnable onAssignMechanic;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public SOSRequestDetailsPage(SOSRequest request) {

        this.request = request;

        this.onBack = null;
        this.onAssignMechanic = null;
    }

    // =====================================================
    // CONSTRUCTOR WITH NAVIGATION
    // =====================================================

    public SOSRequestDetailsPage(
            SOSRequest request,
            Runnable onBack,
            Runnable onAssignMechanic
    ) {

        this.request = request;
        this.onBack = onBack;
        this.onAssignMechanic = onAssignMechanic;
    }

    // =====================================================
    // VIEW
    // =====================================================

    @Override
    public VBox getView() {

        VBox root = new VBox(20);

        root.setPadding(
                new Insets(30, 32, 32, 32)
        );

        root.setStyle(
                "-fx-background-color: " + BG + ";"
        );

        // -------------------------------------------------
        // BACK BUTTON
        // -------------------------------------------------

        Button backButton =
                new Button("← Back to SOS Requests");

        styleOutlineButton(backButton);

        backButton.setOnAction(
                event -> {

                    if (onBack != null) {
                        onBack.run();
                    }
                }
        );

        // -------------------------------------------------
        // HEADER
        // -------------------------------------------------

        VBox header = createHeader();

        // -------------------------------------------------
        // MAIN CONTENT
        // -------------------------------------------------

        VBox content = createDetailsContent();

        VBox.setVgrow(
                content,
                Priority.ALWAYS
        );

        root.getChildren().addAll(
                backButton,
                header,
                content
        );

        return root;
    }

    // =====================================================
    // HEADER
    // =====================================================

    private VBox createHeader() {

        VBox header = new VBox(7);

        Label title =
                new Label("SOS Emergency Details");

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
                        "View complete information about this emergency roadside assistance request."
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

        header.getChildren().addAll(
                title,
                subtitle
        );

        return header;
    }

    // =====================================================
    // DETAILS CONTENT
    // =====================================================

    private VBox createDetailsContent() {

        VBox main = new VBox(18);

        // -------------------------------------------------
        // REQUEST HEADER CARD
        // -------------------------------------------------

        VBox requestHeader =
                createRequestHeader();

        // -------------------------------------------------
        // REQUEST INFORMATION
        // -------------------------------------------------

        VBox requestInfo =
                createRequestInformation();

        // -------------------------------------------------
        // PROBLEM DESCRIPTION
        // -------------------------------------------------

        VBox description =
                createDescriptionCard();

        // -------------------------------------------------
        // LOCATION INFORMATION
        // -------------------------------------------------

        VBox location =
                createLocationCard();

        // -------------------------------------------------
        // MECHANIC INFORMATION
        // -------------------------------------------------

        VBox mechanic =
                createMechanicCard();

        // -------------------------------------------------
        // ACTIONS
        // -------------------------------------------------

        HBox actions =
                createActions();

        main.getChildren().addAll(
                requestHeader,
                requestInfo,
                description,
                location,
                mechanic,
                actions
        );

        ScrollPane scrollPane =
                new ScrollPane(main);

        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);

        scrollPane.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        scrollPane.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED
        );

        scrollPane.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-background: transparent;"
        );

        VBox wrapper = new VBox();

        wrapper.setStyle(
                "-fx-background-color: transparent;"
        );

        VBox.setVgrow(
                scrollPane,
                Priority.ALWAYS
        );

        wrapper.getChildren().add(
                scrollPane
        );

        return wrapper;
    }

    // =====================================================
    // REQUEST HEADER
    // =====================================================

    private VBox createRequestHeader() {

        VBox card = createCard();

        HBox row = new HBox(15);

        row.setAlignment(
                Pos.CENTER_LEFT
        );

        VBox icon =
                createSOSIcon();

        VBox information =
                new VBox(5);

        Label id =
                new Label(
                        safe(request.getSosId())
                );

        id.setTextFill(
                Color.web(HEADING)
        );

        id.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        24
                )
        );

        Label customer =
                new Label(
                        "Customer: " +
                        safe(request.getCustomerName())
                );

        customer.setTextFill(
                Color.web(TEXT)
        );

        customer.setFont(
                Font.font(
                        "Arial",
                        14
                )
        );

        information.getChildren().addAll(
                id,
                customer
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Label status =
                createStatusBadge(
                        request.getStatus()
                );

        row.getChildren().addAll(
                icon,
                information,
                spacer,
                status
        );

        card.getChildren().add(row);

        return card;
    }

    // =====================================================
    // SOS ICON
    // =====================================================

    private VBox createSOSIcon() {

        VBox box = new VBox();

        box.setAlignment(
                Pos.CENTER
        );

        box.setMinSize(58, 58);
        box.setPrefSize(58, 58);
        box.setMaxSize(58, 58);

        box.setStyle(
                "-fx-background-color: " +
                RED +
                ";" +
                "-fx-background-radius: 14;"
        );

        Label text =
                new Label("SOS");

        text.setTextFill(
                Color.WHITE
        );

        text.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        16
                )
        );

        box.getChildren().add(text);

        return box;
    }

    // =====================================================
    // REQUEST INFORMATION
    // =====================================================

    private VBox createRequestInformation() {

        VBox card = createCard();

        Label title =
                createSectionTitle(
                        "Request Information"
                );

        VBox grid = new VBox(12);

        HBox row1 = new HBox(15);

        VBox customer =
                createInfoBox(
                        "CUSTOMER",
                        request.getCustomerName()
                );

        VBox vehicle =
                createInfoBox(
                        "VEHICLE",
                        request.getVehicleNumber()
                );

        HBox.setHgrow(
                customer,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                vehicle,
                Priority.ALWAYS
        );

        row1.getChildren().addAll(
                customer,
                vehicle
        );

        HBox row2 = new HBox(15);

        VBox emergency =
                createInfoBox(
                        "EMERGENCY TYPE",
                        request.getEmergencyType()
                );

        VBox location =
                createInfoBox(
                        "LOCATION",
                        request.getLocation()
                );

        HBox.setHgrow(
                emergency,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                location,
                Priority.ALWAYS
        );

        row2.getChildren().addAll(
                emergency,
                location
        );

        grid.getChildren().addAll(
                row1,
                row2
        );

        card.getChildren().addAll(
                title,
                new Separator(),
                grid
        );

        return card;
    }

    // =====================================================
    // DESCRIPTION
    // =====================================================

    private VBox createDescriptionCard() {

        VBox card = createCard();

        Label title =
                createSectionTitle(
                        "Emergency Description"
                );

        Label description =
                new Label(
                        safe(
                                request.getDescription()
                        )
                );

        description.setTextFill(
                Color.web(TEXT)
        );

        description.setFont(
                Font.font(
                        "Arial",
                        15
                )
        );

        description.setWrapText(true);

        description.setPadding(
                new Insets(5, 0, 5, 0)
        );

        card.getChildren().addAll(
                title,
                new Separator(),
                description
        );

        return card;
    }

    // =====================================================
    // LOCATION
    // =====================================================

    private VBox createLocationCard() {

        VBox card = createCard();

        Label title =
                createSectionTitle(
                        "Location Information"
                );

        HBox row =
                new HBox(15);

        VBox location =
                createInfoBox(
                        "LOCATION",
                        request.getLocation()
                );

        VBox latitude =
                createInfoBox(
                        "LATITUDE",
                        request.getLatitude()
                );

        VBox longitude =
                createInfoBox(
                        "LONGITUDE",
                        request.getLongitude()
                );

        HBox.setHgrow(
                location,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                latitude,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                longitude,
                Priority.ALWAYS
        );

        row.getChildren().addAll(
                location,
                latitude,
                longitude
        );

        card.getChildren().addAll(
                title,
                new Separator(),
                row
        );

        return card;
    }

    // =====================================================
    // MECHANIC
    // =====================================================

    private VBox createMechanicCard() {

        VBox card = createCard();

        Label title =
                createSectionTitle(
                        "Mechanic Assignment"
                );

        String mechanicName =
                safe(
                        request.getMechanicName()
                );

        if (mechanicName.equals("-")) {
            mechanicName = "Unassigned";
        }

        Label mechanic =
                new Label(
                        mechanicName
                );

        mechanic.setTextFill(
                mechanicName.equals("Unassigned")
                        ? Color.web(ORANGE)
                        : Color.web(GREEN)
        );

        mechanic.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        18
                )
        );

        mechanic.setPadding(
                new Insets(8, 0, 8, 0)
        );

        Label message =
                new Label();

        if (
                mechanicName.equals("Unassigned")
        ) {

            message.setText(
                    "No mechanic has been assigned to this emergency request."
            );

        } else {

            message.setText(
                    "A mechanic has been assigned to this emergency request."
            );
        }

        message.setTextFill(
                Color.web(TEXT)
        );

        message.setFont(
                Font.font(
                        "Arial",
                        14
                )
        );

        card.getChildren().addAll(
                title,
                new Separator(),
                mechanic,
                message
        );

        return card;
    }

    // =====================================================
    // ACTIONS
    // =====================================================

    private HBox createActions() {

        HBox actions =
                new HBox(12);

        actions.setAlignment(
                Pos.CENTER_RIGHT
        );

        Button back =
                new Button(
                        "Back to SOS Requests"
                );

        styleOutlineButton(back);

        back.setOnAction(
                event -> {

                    if (onBack != null) {
                        onBack.run();
                    }
                }
        );

        Button assign =
                new Button(
                        "Assign SOS Mechanic"
                );

        stylePrimaryButton(
                assign,
                BLUE
        );

        assign.setOnAction(
                event -> {

                    if (onAssignMechanic != null) {
                        onAssignMechanic.run();
                    }
                }
        );

        actions.getChildren().addAll(
                back,
                assign
        );

        return actions;
    }

    // =====================================================
    // CARD
    // =====================================================

    private VBox createCard() {

        VBox card =
                new VBox(13);

        card.setPadding(
                new Insets(20)
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

        return card;
    }

    // =====================================================
    // SECTION TITLE
    // =====================================================

    private Label createSectionTitle(
            String text
    ) {

        Label label =
                new Label(text);

        label.setTextFill(
                Color.web(HEADING)
        );

        label.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        20
                )
        );

        return label;
    }

    // =====================================================
    // INFO BOX
    // =====================================================

    private VBox createInfoBox(
            String title,
            String value
    ) {

        VBox box =
                new VBox(5);

        box.setPadding(
                new Insets(12)
        );

        box.setStyle(
                "-fx-background-color: " +
                SECONDARY +
                ";" +
                "-fx-background-radius: 9;" +
                "-fx-border-color: " +
                BORDER +
                ";" +
                "-fx-border-radius: 9;"
        );

        Label titleLabel =
                new Label(title);

        titleLabel.setTextFill(
                Color.web(TEXT)
        );

        titleLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        11
                )
        );

        Label valueLabel =
                new Label(
                        safe(value)
                );

        valueLabel.setTextFill(
                Color.web(HEADING)
        );

        valueLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        14
                )
        );

        valueLabel.setWrapText(true);

        box.getChildren().addAll(
                titleLabel,
                valueLabel
        );

        return box;
    }

    // =====================================================
    // STATUS
    // =====================================================

    private Label createStatusBadge(
            String status
    ) {

        String actualStatus =
                safe(status);

        if (actualStatus.equals("-")) {
            actualStatus = "Unknown";
        }

        String color =
                getStatusColor(
                        actualStatus
                );

        Label badge =
                new Label(
                        actualStatus
                );

        badge.setTextFill(
                Color.web(color)
        );

        badge.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        13
                )
        );

        badge.setPadding(
                new Insets(8, 14, 8, 14)
        );

        badge.setStyle(
                "-fx-background-color: " +
                color +
                "18;" +
                "-fx-background-radius: 20;"
        );

        return badge;
    }

    // =====================================================
    // STATUS COLOR
    // =====================================================

    private String getStatusColor(
            String status
    ) {

        if (
                status.equalsIgnoreCase(
                        "Resolved"
                )
        ) {

            return GREEN;
        }

        if (
                status.equalsIgnoreCase(
                        "Accepted"
                )
                ||
                status.equalsIgnoreCase(
                        "In Progress"
                )
        ) {

            return BLUE;
        }

        if (
                status.equalsIgnoreCase(
                        "Cancelled"
                )
        ) {

            return RED;
        }

        return ORANGE;
    }

    // =====================================================
    // OUTLINE BUTTON
    // =====================================================

    private void styleOutlineButton(
            Button button
    ) {

        button.setTextFill(
                Color.web(BLUE)
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
                        9,
                        16,
                        9,
                        16
                )
        );

        button.setCursor(
                Cursor.HAND
        );

        button.setStyle(
                "-fx-background-color: " +
                SURFACE +
                ";" +
                "-fx-border-color: " +
                BLUE +
                ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;"
        );

        button.setOnMouseEntered(
                event ->
                        button.setStyle(
                                "-fx-background-color: #172033;" +
                                "-fx-border-color: " +
                                BLUE +
                                ";" +
                                "-fx-border-radius: 8;" +
                                "-fx-background-radius: 8;"
                        )
        );

        button.setOnMouseExited(
                event ->
                        button.setStyle(
                                "-fx-background-color: " +
                                SURFACE +
                                ";" +
                                "-fx-border-color: " +
                                BLUE +
                                ";" +
                                "-fx-border-radius: 8;" +
                                "-fx-background-radius: 8;"
                        )
        );
    }

    // =====================================================
    // PRIMARY BUTTON
    // =====================================================

    private void stylePrimaryButton(
            Button button,
            String color
    ) {

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
                        9,
                        17,
                        9,
                        17
                )
        );

        button.setCursor(
                Cursor.HAND
        );

        button.setStyle(
                "-fx-background-color: " +
                color +
                ";" +
                "-fx-background-radius: 8;"
        );

        button.setOnMouseEntered(
                event ->
                        button.setStyle(
                                "-fx-background-color: #F59E0B;" +
                                "-fx-background-radius: 8;"
                        )
        );

        button.setOnMouseExited(
                event ->
                        button.setStyle(
                                "-fx-background-color: " +
                                color +
                                ";" +
                                "-fx-background-radius: 8;"
                        )
        );
    }

    // =====================================================
    // SAFE VALUE
    // =====================================================

    private String safe(
            String value
    ) {

        if (
                value == null ||
                value.isBlank()
        ) {

            return "-";
        }

        return value;
    }
}