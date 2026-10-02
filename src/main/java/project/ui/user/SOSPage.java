package project.ui.user;

import com.google.cloud.Timestamp;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.Scene;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import project.controller.user.SOSController;
import project.model.SOSRequest;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class SOSPage {

    // =====================================================
    // COLORS
    // =====================================================

    private static final String MAIN_BACKGROUND =
            "#0F0F0F";

    private static final String CARD_SURFACE =
            "#1A1A1A";

    private static final String SECONDARY_SURFACE =
            "#242424";

    private static final String HEADING =
            "#F3F4F6";

    private static final String SECONDARY_TEXT =
            "#A1A1AA";

    private static final String NAV_BLUE =
            "#F59E0B";

    private static final String SUCCESS =
            "#22C55E";

    private static final String WARNING =
            "#F59E0B";

    private static final String PURPLE =
            "#A78BFA";

    private static final String CYAN =
            "#38BDF8";

    private static final String EMERGENCY =
            "#EF4444";

    private static final String BORDER =
            "#F59E0B";

    // =====================================================
    // SCENE / ROOT
    // =====================================================

    private Scene sosScene;

    private BorderPane root;

    // =====================================================
    // CONTROLLER
    // =====================================================

    private final SOSController controller;

    // =====================================================
    // CALLBACK
    // =====================================================

    private Runnable dashboardAction;

    // =====================================================
    // CURRENT DATA
    // =====================================================

    private SOSRequest currentRequest;

    // =====================================================
    // LOCATION INPUT
    // =====================================================

    private TextField locationField;

    private TextField latitudeField;

    private TextField longitudeField;

    // =====================================================
    // CONSTRUCTORS
    // =====================================================

    public SOSPage() {

        this(null);
    }

    public SOSPage(
            Runnable dashboardAction
    ) {

        this.dashboardAction =
                dashboardAction;

        controller =
                new SOSController();
    }

    // =====================================================
    // PUBLIC SCENE
    //
    // REQUIRED BY AppNavigator
    // =====================================================

    public Scene getSOSScene() {

        root =
                new BorderPane();

        root.setStyle(
                "-fx-background-color: "
                        + MAIN_BACKGROUND
                        + ";"
        );

        root.setTop(
                UserHeader.createHeader()
        );

        root.setLeft(
                UserSideBar.createSidebar(
                        "Emergency SOS"
                )
        );

        refreshPage();

        double width =
                UserDashboard.dashboardStage != null
                        ? UserDashboard.dashboardStage.getWidth()
                        : 1280;

        double height =
                UserDashboard.dashboardStage != null
                        ? UserDashboard.dashboardStage.getHeight()
                        : 800;

        sosScene =
                new Scene(
                        root,
                        width,
                        height
                );

        addScrollbarStyle(
                sosScene
        );

        return sosScene;
    }

    // =====================================================
    // REFRESH PAGE
    // =====================================================

    private void refreshPage() {

        if (root == null) {

            return;
        }

        loadLatestSOS();

        VBox content =
                createSOSContent();

        ScrollPane scrollPane =
                new ScrollPane(
                        content
                );

        scrollPane.setFitToWidth(
                true
        );

        scrollPane.setPannable(
                true
        );

        scrollPane.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        scrollPane.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED
        );

        scrollPane.setStyle(
                "-fx-background-color: "
                        + MAIN_BACKGROUND
                        + ";"
                        + "-fx-background: "
                        + MAIN_BACKGROUND
                        + ";"
                        + "-fx-border-color: transparent;"
        );

        root.setCenter(
                scrollPane
        );
    }

    // =====================================================
    // LOAD ACTIVE OR LATEST SOS
    //
    // If active exists -> show it.
    //
    // Otherwise show latest resolved/cancelled request so
    // customer can still see final status.
    // =====================================================

    private void loadLatestSOS() {

        try {

            SOSRequest active =
                    controller.refreshActiveSOS();

            if (active != null) {

                currentRequest =
                        active;

                return;
            }

            List<SOSRequest> requests =
                    controller.getMySOSRequests();

            if (requests != null
                    &&
                    !requests.isEmpty()) {

                currentRequest =
                        requests.get(0);

            } else {

                currentRequest =
                        null;
            }

        } catch (Exception e) {

            currentRequest =
                    null;

            System.err.println(
                    "Unable to refresh SOS page: "
                            + e.getMessage()
            );
        }
    }

    // =====================================================
    // MAIN CONTENT
    // =====================================================

    private VBox createSOSContent() {

        VBox content =
                new VBox(20);

        content.setPadding(
                new Insets(
                        28,
                        30,
                        40,
                        30
                )
        );

        content.setStyle(
                "-fx-background-color: "
                        + MAIN_BACKGROUND
                        + ";"
        );

        // =================================================
        // HEADER
        // =================================================

        HBox header =
                createPageHeader();

        // =================================================
        // TOP ROW
        // =================================================

        HBox topRow =
                new HBox(20);

        VBox emergencyCard =
                createSOSCard();

        VBox locationCard =
                createLocationCard();

        HBox.setHgrow(
                emergencyCard,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                locationCard,
                Priority.ALWAYS
        );

        emergencyCard.setMaxWidth(
                Double.MAX_VALUE
        );

        locationCard.setMaxWidth(
                Double.MAX_VALUE
        );

        topRow.getChildren()
                .addAll(
                        emergencyCard,
                        locationCard
                );

        // =================================================
        // BOTTOM ROW
        // =================================================

        HBox bottomRow =
                new HBox(20);

        VBox timeline =
                createTimelineCard();

        VBox mechanic =
                createMechanicCard();

        VBox contacts =
                createContactsCard();

        HBox.setHgrow(
                timeline,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                mechanic,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                contacts,
                Priority.ALWAYS
        );

        timeline.setMaxWidth(
                Double.MAX_VALUE
        );

        mechanic.setMaxWidth(
                Double.MAX_VALUE
        );

        contacts.setMaxWidth(
                Double.MAX_VALUE
        );

        bottomRow
                .getChildren()
                .addAll(
                        timeline,
                        mechanic,
                        contacts
                );

        // =================================================
        // HISTORY
        // =================================================

        VBox history =
                createRecentSOSCard();

        content.getChildren()
                .addAll(
                        header,
                        topRow,
                        bottomRow,
                        history
                );

        return content;
    }

    // =====================================================
    // PAGE HEADER
    // =====================================================

    private HBox createPageHeader() {

        HBox row =
                new HBox(15);

        row.setAlignment(
                Pos.CENTER_LEFT
        );

        VBox textBox =
                new VBox(5);

        Label title =
                new Label(
                        "Emergency SOS"
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
                        31
                )
        );

        Label subtitle =
                new Label(
                        "Create an emergency request and track its status across RoadGuardian."
                );

        subtitle.setTextFill(
                Color.web(
                        SECONDARY_TEXT
                )
        );

        subtitle.setFont(
                Font.font(
                        "Arial",
                        14
                )
        );

        textBox
                .getChildren()
                .addAll(
                        title,
                        subtitle
                );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Label status =
                createStatusBadge(
                        getPageStatus()
                );

        Button refresh =
                createOutlineButton(
                        "Refresh",
                        NAV_BLUE
                );

        refresh.setOnAction(
                event ->
                        refreshPage()
        );

        Button dashboard =
                null;

        if (dashboardAction != null) {

            dashboard =
                    createOutlineButton(
                            "Dashboard",
                            HEADING
                    );

            dashboard.setOnAction(
                    event ->
                            dashboardAction.run()
            );
        }

        row.getChildren()
                .addAll(
                        textBox,
                        spacer,
                        status,
                        refresh
                );

        if (dashboard != null) {

            row.getChildren()
                    .add(
                            dashboard
                    );
        }

        return row;
    }

    // =====================================================
    // PAGE STATUS
    // =====================================================

    private String getPageStatus() {

        if (currentRequest == null) {

            return "No Active SOS";
        }

        if (currentRequest.isActive()) {

            return controller.getStatus(
                    currentRequest
            );
        }

        return controller.getStatus(
                currentRequest
        );
    }

    // =====================================================
    // SOS CARD
    // =====================================================

    private VBox createSOSCard() {

        VBox card =
                createCard();

        Label heading =
                new Label(
                        currentRequest != null
                                &&
                                currentRequest.isActive()
                                ? "Emergency request active"
                                : "Emergency assistance"
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
                        20
                )
        );

        Label description =
                new Label();

        description.setWrapText(
                true
        );

        description.setTextFill(
                Color.web(
                        SECONDARY_TEXT
                )
        );

        description.setFont(
                Font.font(
                        "Arial",
                        13
                )
        );

        if (currentRequest == null) {

            description.setText(
                    "Enter your current location and activate SOS when you need emergency roadside assistance."
            );

        } else if (currentRequest.isActive()) {

            description.setText(
                    "Your SOS request is active. Refresh this page to see Admin and mechanic status updates."
            );

        } else if (currentRequest.isResolved()) {

            description.setText(
                    "Your most recent SOS emergency has been resolved."
            );

        } else {

            description.setText(
                    "Your most recent SOS request is "
                            + controller.getStatus(
                            currentRequest
                    )
                            + "."
            );
        }

        // =================================================
        // SOS CIRCLE
        // =================================================

        StackPane circlePane =
                new StackPane();

        Circle outer =
                new Circle(
                        92
                );

        outer.setFill(
                Color.web(
                        "#FDEBEC"
                )
        );

        Circle inner =
                new Circle(
                        70
                );

        inner.setFill(
                Color.web(
                        EMERGENCY
                )
        );

        Label sos =
                new Label(
                        "SOS"
                );

        sos.setTextFill(
                Color.WHITE
        );

        sos.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        27
                )
        );

        circlePane
                .getChildren()
                .addAll(
                        outer,
                        inner,
                        sos
                );

        // =================================================
        // REQUEST INFO
        // =================================================

        if (currentRequest != null) {

            HBox requestInfo =
                    new HBox(10);

            VBox requestId =
                    createSmallInfoBox(
                            "SOS ID",
                            firstNonBlank(
                                    currentRequest.getSosId(),
                                    "-"
                            )
                    );

            VBox requestStatus =
                    createSmallInfoBox(
                            "STATUS",
                            controller.getStatus(
                                    currentRequest
                            )
                    );

            HBox.setHgrow(
                    requestId,
                    Priority.ALWAYS
            );

            HBox.setHgrow(
                    requestStatus,
                    Priority.ALWAYS
            );

            requestInfo
                    .getChildren()
                    .addAll(
                            requestId,
                            requestStatus
                    );

            card.getChildren()
                    .addAll(
                            heading,
                            description,
                            circlePane,
                            requestInfo
                    );

        } else {

            card.getChildren()
                    .addAll(
                            heading,
                            description,
                            circlePane
                    );
        }

        // =================================================
        // ACTIVE REQUEST
        // =================================================

        if (currentRequest != null
                &&
                currentRequest.isActive()) {

            Label currentStatus =
                    new Label(
                            getCustomerStatusMessage(
                                    currentRequest
                            )
                    );

            currentStatus.setWrapText(
                    true
            );

            currentStatus.setPadding(
                    new Insets(11)
            );

            currentStatus.setTextFill(
                    Color.web(
                            getStatusColor(
                                    currentRequest.getStatus()
                            )
                    )
            );

            currentStatus.setStyle(
                    "-fx-background-color: "
                            + SECONDARY_SURFACE
                            + ";"
                            + "-fx-background-radius: 9;"
                            + "-fx-font-weight: bold;"
            );

            Button refresh =
                    createPrimaryButton(
                            "Refresh SOS Status",
                            NAV_BLUE
                    );

            refresh.setOnAction(
                    event ->
                            refreshPage()
            );

            card.getChildren()
                    .addAll(
                            currentStatus,
                            refresh
                    );

            // =================================================
            // CANCEL
            // =================================================

            if (controller.canCancel(
                    currentRequest
            )) {

                Button cancel =
                        createOutlineButton(
                                "Cancel SOS Request",
                                EMERGENCY
                        );

                cancel.setMaxWidth(
                        Double.MAX_VALUE
                );

                cancel.setOnAction(
                        event ->
                                cancelCurrentSOS()
                );

                card.getChildren()
                        .add(
                                cancel
                        );
            }

            return card;
        }

        // =================================================
        // NO ACTIVE SOS
        // =================================================

        Label info =
                new Label(
                        "A new SOS will be saved in the shared SOSRequests collection and immediately become visible to Admin."
                );

        info.setWrapText(
                true
        );

        info.setTextFill(
                Color.web(
                        SECONDARY_TEXT
                )
        );

        info.setFont(
                Font.font(
                        "Arial",
                        11
                )
        );

        Button activate =
                createPrimaryButton(
                        "Activate Emergency SOS",
                        EMERGENCY
                );

        activate.setOnAction(
                event ->
                        activateSOS(
                                activate
                        )
        );

        card.getChildren()
                .addAll(
                        info,
                        activate
                );

        return card;
    }

    // =====================================================
    // ACTIVATE SOS
    // =====================================================

    private void activateSOS(
            Button button
    ) {

        String location =
                clean(
                        locationField == null
                                ? null
                                : locationField.getText()
                );

        String latitude =
                clean(
                        latitudeField == null
                                ? null
                                : latitudeField.getText()
                );

        String longitude =
                clean(
                        longitudeField == null
                                ? null
                                : longitudeField.getText()
                );

        // =================================================
        // LOCATION REQUIRED
        //
        // No fake Pune / NH-48 location anymore.
        // =================================================

        if (location == null) {

            showError(
                    "Location Required",
                    "Please enter your current location before activating SOS."
            );

            if (locationField != null) {

                locationField.requestFocus();
            }

            return;
        }

        // =================================================
        // COORDINATES OPTIONAL
        // =================================================

        if ((latitude == null)
                !=
                (longitude == null)) {

            showError(
                    "GPS Coordinates",
                    "Enter both latitude and longitude, or leave both empty."
            );

            return;
        }

        if (latitude != null
                &&
                !isValidLatitude(
                        latitude
                )) {

            showError(
                    "Invalid Latitude",
                    "Latitude must be a number between -90 and 90."
            );

            return;
        }

        if (longitude != null
                &&
                !isValidLongitude(
                        longitude
                )) {

            showError(
                    "Invalid Longitude",
                    "Longitude must be a number between -180 and 180."
            );

            return;
        }

        Alert confirmation =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        if (button.getScene() != null
                &&
                button.getScene().getWindow() != null) {

            confirmation.initOwner(
                    button
                            .getScene()
                            .getWindow()
            );
        }

        confirmation.setTitle(
                "RoadGuardian"
        );

        confirmation.setHeaderText(
                "Activate emergency SOS?"
        );

        confirmation.setContentText(
                "Location: "
                        + location
                        + "\n\n"
                        + "This request will be sent to RoadGuardian Admin for emergency assistance."
        );

        ButtonType result =
                confirmation
                        .showAndWait()
                        .orElse(
                                ButtonType.CANCEL
                        );

        if (result != ButtonType.OK) {

            return;
        }

        button.setDisable(
                true
        );

        boolean success =
                controller.activateSOS(
                        location,
                        latitude == null
                                ? ""
                                : latitude,
                        longitude == null
                                ? ""
                                : longitude
                );

        if (success) {

            showInformation(
                    "SOS Activated",
                    "Your emergency request was created successfully.\n\n"
                            + "Admin can now see the request and assign a mechanic."
            );

            refreshPage();

        } else {

            button.setDisable(
                    false
            );

            showError(
                    "Unable to Activate SOS",
                    "SOS could not be created. Please check your login and Firebase connection."
            );
        }
    }

    // =====================================================
    // CANCEL ACTIVE SOS
    // =====================================================

    private void cancelCurrentSOS() {

        if (currentRequest == null) {

            return;
        }

        Alert confirmation =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        if (sosScene != null
                &&
                sosScene.getWindow() != null) {

            confirmation.initOwner(
                    sosScene.getWindow()
            );
        }

        confirmation.setTitle(
                "RoadGuardian"
        );

        confirmation.setHeaderText(
                "Cancel SOS Request?"
        );

        confirmation.setContentText(
                "Current status: "
                        + controller.getStatus(
                                currentRequest
                        )
                        + "\n\n"
                        + "Do you want to cancel this emergency request?"
        );

        if (confirmation
                .showAndWait()
                .orElse(
                        ButtonType.CANCEL
                ) != ButtonType.OK) {

            return;
        }

        boolean success =
                controller.cancelSOS(
                        currentRequest.getSosId()
                );

        if (success) {

            showInformation(
                    "SOS Cancelled",
                    "Your SOS request has been cancelled."
            );

            refreshPage();

        } else {

            showError(
                    "Unable to Cancel",
                    "The SOS request can no longer be cancelled. Refresh the page to see its latest status."
            );

            refreshPage();
        }
    }

    // =====================================================
    // LOCATION CARD
    // =====================================================

    private VBox createLocationCard() {

        VBox card =
                createCard();

        Label title =
                new Label(
                        "SOS Location"
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
                        20
                )
        );

        Label description =
                new Label();

        description.setWrapText(
                true
        );

        description.setFont(
                Font.font(
                        "Arial",
                        12
                )
        );

        description.setTextFill(
                Color.web(
                        SECONDARY_TEXT
                )
        );

        // =================================================
        // ACTIVE / PREVIOUS REQUEST LOCATION
        // =================================================

        if (currentRequest != null
                &&
                currentRequest.isActive()) {

            description.setText(
                    "Location stored with the active SOS request."
            );

            VBox location =
                    createDetailBox(
                            "CURRENT LOCATION",
                            controller.getLocationDisplay(
                                    currentRequest
                            )
                    );

            VBox gps =
                    createDetailBox(
                            "GPS COORDINATES",
                            controller.getGPSDisplay(
                                    currentRequest
                            )
                    );

            Label note =
                    new Label(
                            "Location is taken directly from the SOS request stored in Firebase."
                    );

            note.setWrapText(
                    true
            );

            note.setTextFill(
                    Color.web(
                            SECONDARY_TEXT
                    )
            );

            note.setFont(
                    Font.font(
                            "Arial",
                            11
                    )
            );

            card.getChildren()
                    .addAll(
                            title,
                            description,
                            new Separator(),
                            location,
                            gps,
                            note
                    );

            return card;
        }

        // =================================================
        // NEW SOS LOCATION INPUT
        // =================================================

        description.setText(
                "Enter your actual current location. GPS coordinates are optional."
        );

        locationField =
                new TextField();

        locationField.setPromptText(
                "Example: Kothrud, Pune or highway / landmark"
        );

        styleTextField(
                locationField
        );

        latitudeField =
                new TextField();

        latitudeField.setPromptText(
                "Latitude (optional)"
        );

        styleTextField(
                latitudeField
        );

        longitudeField =
                new TextField();

        longitudeField.setPromptText(
                "Longitude (optional)"
        );

        styleTextField(
                longitudeField
        );

        // =================================================
        // KEEP LAST FINISHED LOCATION AS SUGGESTION
        // BUT DO NOT PRETEND IT IS CURRENT LOCATION.
        // =================================================

        if (currentRequest != null
                &&
                !currentRequest.isActive()) {

            Label previous =
                    new Label(
                            "Previous SOS location: "
                                    + controller.getLocationDisplay(
                                    currentRequest
                            )
                    );

            previous.setWrapText(
                    true
            );

            previous.setTextFill(
                    Color.web(
                            SECONDARY_TEXT
                    )
            );

            previous.setFont(
                    Font.font(
                            "Arial",
                            11
                    )
            );

            card.getChildren()
                    .addAll(
                            title,
                            description,
                            new Separator(),
                            createFieldLabel(
                                    "Current location"
                            ),
                            locationField,
                            createFieldLabel(
                                    "Latitude"
                            ),
                            latitudeField,
                            createFieldLabel(
                                    "Longitude"
                            ),
                            longitudeField,
                            previous
                    );

        } else {

            card.getChildren()
                    .addAll(
                            title,
                            description,
                            new Separator(),
                            createFieldLabel(
                                    "Current location"
                            ),
                            locationField,
                            createFieldLabel(
                                    "Latitude"
                            ),
                            latitudeField,
                            createFieldLabel(
                                    "Longitude"
                            ),
                            longitudeField
                    );
        }

        return card;
    }

    // =====================================================
    // TIMELINE CARD
    // =====================================================

    private VBox createTimelineCard() {

        VBox card =
                createCard();

        Label title =
                new Label(
                        "SOS Status Timeline"
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
                        20
                )
        );

        card.getChildren()
                .add(
                        title
                );

        // =================================================
        // NO SOS
        // =================================================

        if (currentRequest == null) {

            Label empty =
                    createEmptyLabel(
                            "No SOS request has been created yet."
                    );

            card.getChildren()
                    .add(
                            empty
                    );

            return card;
        }

        // =================================================
        // ACTUAL FIREBASE TIMELINE
        // =================================================

        List<Map<String, Object>> timeline =
                controller.getTimeline(
                        currentRequest
                );

        if (timeline != null
                &&
                !timeline.isEmpty()) {

            for (Map<String, Object> item :
                    timeline) {

                String itemTitle =
                        getString(
                                item,
                                "title",
                                "SOS update"
                        );

                String status =
                        getString(
                                item,
                                "status",
                                "Waiting"
                        );

                Object timestamp =
                        item.get(
                                "timestamp"
                        );

                card.getChildren()
                        .add(
                                createTimelineRow(
                                        itemTitle,
                                        status,
                                        formatDateObject(
                                                timestamp
                                        )
                                )
                        );
            }

        } else {

            // =================================================
            // OLD DOCUMENT FALLBACK
            // =================================================

            for (TimelineItem item :
                    createFallbackTimeline(
                            currentRequest
                    )) {

                card.getChildren()
                        .add(
                                createTimelineRow(
                                        item.title,
                                        item.status,
                                        item.time
                                )
                        );
            }
        }

        return card;
    }

    // =====================================================
    // TIMELINE ROW
    // =====================================================

    private HBox createTimelineRow(
            String title,
            String status,
            String time
    ) {

        HBox row =
                new HBox(11);

        row.setAlignment(
                Pos.CENTER_LEFT
        );

        boolean completed =
                status != null
                        &&
                        (
                                status.equalsIgnoreCase(
                                        "Completed"
                                )
                                        ||
                                status.equalsIgnoreCase(
                                        "Done"
                                )
                        );

        boolean waiting =
                status == null
                        ||
                        status.equalsIgnoreCase(
                                "Waiting"
                        );

        String color =
                completed
                        ? SUCCESS
                        : waiting
                        ? BORDER
                        : NAV_BLUE;

        Circle dot =
                new Circle(
                        7
                );

        dot.setFill(
                Color.web(
                        color
                )
        );

        VBox text =
                new VBox(2);

        Label titleLabel =
                new Label(
                        firstNonBlank(
                                title,
                                "SOS Update"
                        )
                );

        titleLabel.setWrapText(
                true
        );

        titleLabel.setTextFill(
                Color.web(
                        HEADING
                )
        );

        titleLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        12
                )
        );

        Label statusLabel =
                new Label(
                        firstNonBlank(
                                status,
                                "Waiting"
                        )
                );

        statusLabel.setTextFill(
                Color.web(
                        completed
                                ? SUCCESS
                                : waiting
                                ? SECONDARY_TEXT
                                : NAV_BLUE
                )
        );

        statusLabel.setFont(
                Font.font(
                        "Arial",
                        10
                )
        );

        text.getChildren()
                .addAll(
                        titleLabel,
                        statusLabel
                );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Label timeLabel =
                new Label(
                        firstNonBlank(
                                time,
                                "-"
                        )
                );

        timeLabel.setTextFill(
                Color.web(
                        SECONDARY_TEXT
                )
        );

        timeLabel.setFont(
                Font.font(
                        "Arial",
                        10
                )
        );

        row.getChildren()
                .addAll(
                        dot,
                        text,
                        spacer,
                        timeLabel
                );

        return row;
    }

    // =====================================================
    // FALLBACK TIMELINE
    // =====================================================

    private List<TimelineItem> createFallbackTimeline(
            SOSRequest request
    ) {

        List<TimelineItem> list =
                new ArrayList<>();

        boolean exists =
                request != null;

        boolean assigned =
                request != null
                        &&
                        (
                                request.isAssigned()
                                        ||
                                request.isAccepted()
                                        ||
                                request.isInProgress()
                                        ||
                                request.isResolved()
                        );

        boolean accepted =
                request != null
                        &&
                        (
                                request.isAccepted()
                                        ||
                                request.isInProgress()
                                        ||
                                request.isResolved()
                        );

        boolean progress =
                request != null
                        &&
                        (
                                request.isInProgress()
                                        ||
                                request.isResolved()
                        );

        boolean resolved =
                request != null
                        &&
                        request.isResolved();

        list.add(
                new TimelineItem(
                        "SOS request received",
                        exists
                                ? "Completed"
                                : "Waiting",
                        formatDate(
                                request == null
                                        ? null
                                        : request.getRequestDate()
                        )
                )
        );

        list.add(
                new TimelineItem(
                        "Mechanic assigned",
                        assigned
                                ? "Completed"
                                : "Waiting",
                        formatDate(
                                request == null
                                        ? null
                                        : request.getAssignedDate()
                        )
                )
        );

        list.add(
                new TimelineItem(
                        "Mechanic accepted",
                        accepted
                                ? "Completed"
                                : "Waiting",
                        formatDate(
                                request == null
                                        ? null
                                        : request.getAcceptedDate()
                        )
                )
        );

        list.add(
                new TimelineItem(
                        "Response started",
                        progress
                                ? "Completed"
                                : "Waiting",
                        formatDate(
                                request == null
                                        ? null
                                        : request.getStartedDate()
                        )
                )
        );

        list.add(
                new TimelineItem(
                        "Assistance completed",
                        resolved
                                ? "Completed"
                                : "Waiting",
                        formatDate(
                                request == null
                                        ? null
                                        : request.getResolvedDate()
                        )
                )
        );

        return list;
    }

    // =====================================================
    // MECHANIC CARD
    // =====================================================

    private VBox createMechanicCard() {

        VBox card =
                createCard();

        Label title =
                new Label(
                        "Assigned Mechanic"
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
                        20
                )
        );

        card.getChildren()
                .add(
                        title
                );

        // =================================================
        // NOTHING ACTIVE
        // =================================================

        if (currentRequest == null) {

            card.getChildren()
                    .add(
                            createEmptyLabel(
                                    "No active SOS request."
                            )
                    );

            return card;
        }

        boolean hasMechanic =
                currentRequest.hasMechanic();

        String mechanicName =
                controller.getMechanicDisplay(
                        currentRequest
                );

        StackPane avatar =
                new StackPane();

        Circle avatarCircle =
                new Circle(
                        34
                );

        avatarCircle.setFill(
                Color.web(
                        SECONDARY_SURFACE
                )
        );

        Label initials =
                new Label(
                        hasMechanic
                                ? createInitials(
                                mechanicName
                        )
                                : "?"
                );

        initials.setTextFill(
                Color.web(
                        NAV_BLUE
                )
        );

        initials.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        15
                )
        );

        avatar
                .getChildren()
                .addAll(
                        avatarCircle,
                        initials
                );

        Label name =
                new Label(
                        mechanicName
                );

        name.setWrapText(
                true
        );

        name.setTextFill(
                Color.web(
                        HEADING
                )
        );

        name.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        17
                )
        );

        Label responderStatus =
                new Label(
                        controller.getResponderStatusDisplay(
                                currentRequest
                        )
                );

        responderStatus.setWrapText(
                true
        );

        responderStatus.setTextFill(
                Color.web(
                        getStatusColor(
                                currentRequest.getStatus()
                        )
                )
        );

        responderStatus.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        12
                )
        );

        String mechanicId =
                firstNonBlank(
                        currentRequest.getMechanicId(),
                        currentRequest.getResponderId(),
                        ""
                );

        Label identifier =
                new Label(
                        hasMechanic
                                ? "Mechanic ID: "
                                + mechanicId
                                : "Admin has not assigned a mechanic yet."
                );

        identifier.setWrapText(
                true
        );

        identifier.setTextFill(
                Color.web(
                        SECONDARY_TEXT
                )
        );

        identifier.setFont(
                Font.font(
                        "Arial",
                        11
                )
        );

        card.getChildren()
                .addAll(
                        avatar,
                        name,
                        responderStatus,
                        identifier
                );

        if (hasMechanic) {

            Button details =
                    createOutlineButton(
                            "Responder Details",
                            NAV_BLUE
                    );

            details.setMaxWidth(
                    Double.MAX_VALUE
            );

            details.setOnAction(
                    event ->
                            showInformation(
                                    "Assigned Mechanic",
                                    "Name: "
                                            + mechanicName
                                            + "\n"
                                            + "Mechanic ID: "
                                            + mechanicId
                                            + "\n"
                                            + "Status: "
                                            + controller.getResponderStatusDisplay(
                                            currentRequest
                                    )
                                            + "\n\n"
                                            + "A phone number is not stored in this SOS document."
                            )
            );

            card.getChildren()
                    .add(
                            details
                    );
        }

        return card;
    }

    // =====================================================
    // CONTACTS CARD
    // =====================================================

    private VBox createContactsCard() {

        VBox card =
                createCard();

        Label title =
                new Label(
                        "Emergency Contacts"
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
                        20
                )
        );

        card.getChildren()
                .add(
                        title
                );

        List<Map<String, Object>> contacts =
                controller.getEmergencyContacts();

        if (contacts == null
                ||
                contacts.isEmpty()) {

            card.getChildren()
                    .add(
                            createEmptyLabel(
                                    "No emergency contacts are saved in your profile."
                            )
                    );

        } else {

            for (Map<String, Object> contact :
                    contacts) {

                String name =
                        getString(
                                contact,
                                "name",
                                "Emergency Contact"
                        );

                String relation =
                        getString(
                                contact,
                                "relation",
                                "Emergency Contact"
                        );

                String phone =
                        getString(
                                contact,
                                "phone",
                                ""
                        );

                card.getChildren()
                        .add(
                                createContactRow(
                                        name,
                                        relation,
                                        phone
                                )
                        );
            }
        }

        // =================================================
        // NATIONAL EMERGENCY
        // =================================================

        card.getChildren()
                .add(
                        createContactRow(
                                "National Emergency",
                                "Emergency Service",
                                "112"
                        )
                );

        return card;
    }

    // =====================================================
    // CONTACT ROW
    // =====================================================

    private HBox createContactRow(
            String name,
            String relation,
            String phone
    ) {

        HBox row =
                new HBox(10);

        row.setAlignment(
                Pos.CENTER_LEFT
        );

        StackPane avatar =
                new StackPane();

        Circle circle =
                new Circle(
                        20
                );

        circle.setFill(
                Color.web(
                        SECONDARY_SURFACE
                )
        );

        Label initials =
                new Label(
                        createInitials(
                                name
                        )
                );

        initials.setTextFill(
                Color.web(
                        NAV_BLUE
                )
        );

        initials.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        10
                )
        );

        avatar.getChildren()
                .addAll(
                        circle,
                        initials
                );

        VBox text =
                new VBox(2);

        Label nameLabel =
                new Label(
                        firstNonBlank(
                                name,
                                "Emergency Contact"
                        )
                );

        nameLabel.setTextFill(
                Color.web(
                        HEADING
                )
        );

        nameLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        12
                )
        );

        Label relationLabel =
                new Label(
                        firstNonBlank(
                                relation,
                                "Emergency Contact"
                        )
                );

        relationLabel.setTextFill(
                Color.web(
                        SECONDARY_TEXT
                )
        );

        relationLabel.setFont(
                Font.font(
                        "Arial",
                        10
                )
        );

        text.getChildren()
                .addAll(
                        nameLabel,
                        relationLabel
                );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Button phoneButton =
                createOutlineButton(
                        "Phone",
                        NAV_BLUE
                );

        phoneButton.setOnAction(
                event ->
                        showInformation(
                                name,
                                clean(phone) == null
                                        ? "Phone number is not available."
                                        : "Phone: "
                                        + phone
                        )
        );

        row.getChildren()
                .addAll(
                        avatar,
                        text,
                        spacer,
                        phoneButton
                );

        return row;
    }

    // =====================================================
    // RECENT SOS REQUESTS
    // =====================================================

    private VBox createRecentSOSCard() {

        VBox card =
                createCard();

        Label title =
                new Label(
                        "Recent SOS Requests"
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
                        20
                )
        );

        Label subtitle =
                new Label(
                        "Your latest SOS records from Firebase."
                );

        subtitle.setTextFill(
                Color.web(
                        SECONDARY_TEXT
                )
        );

        subtitle.setFont(
                Font.font(
                        "Arial",
                        11
                )
        );

        card.getChildren()
                .addAll(
                        title,
                        subtitle,
                        new Separator()
                );

        List<SOSRequest> requests =
                controller.getMySOSRequests();

        if (requests == null
                ||
                requests.isEmpty()) {

            card.getChildren()
                    .add(
                            createEmptyLabel(
                                    "No SOS history found."
                            )
                    );

            return card;
        }

        int limit =
                Math.min(
                        requests.size(),
                        5
                );

        for (int i = 0;
             i < limit;
             i++) {

            card.getChildren()
                    .add(
                            createHistoryRow(
                                    requests.get(i)
                            )
                    );
        }

        return card;
    }

    // =====================================================
    // HISTORY ROW
    // =====================================================

    private HBox createHistoryRow(
            SOSRequest request
    ) {

        HBox row =
                new HBox(12);

        row.setAlignment(
                Pos.CENTER_LEFT
        );

        row.setPadding(
                new Insets(
                        9,
                        4,
                        9,
                        4
                )
        );

        VBox info =
                new VBox(3);

        Label emergency =
                new Label(
                        controller.getEmergencyTypeDisplay(
                                request
                        )
                );

        emergency.setTextFill(
                Color.web(
                        HEADING
                )
        );

        emergency.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        12
                )
        );

        Label details =
                new Label(
                        controller.getLocationDisplay(
                                request
                        )
                                + " • "
                                + formatDate(
                                request.getRequestDate()
                        )
                );

        details.setTextFill(
                Color.web(
                        SECONDARY_TEXT
                )
        );

        details.setFont(
                Font.font(
                        "Arial",
                        10
                )
        );

        info.getChildren()
                .addAll(
                        emergency,
                        details
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

        row.getChildren()
                .addAll(
                        info,
                        spacer,
                        status
                );

        return row;
    }

    // =====================================================
    // CUSTOMER STATUS MESSAGE
    // =====================================================

    private String getCustomerStatusMessage(
            SOSRequest request
    ) {

        if (request == null) {

            return "No SOS request.";
        }

        if (request.isPending()) {

            return "SOS received. Waiting for Admin to assign a mechanic.";
        }

        if (request.isAssigned()) {

            return controller.getMechanicDisplay(
                    request
            )
                    + " has been assigned. Waiting for mechanic response.";
        }

        if (request.isAccepted()) {

            return controller.getMechanicDisplay(
                    request
            )
                    + " accepted your SOS request.";
        }

        if (request.isInProgress()) {

            return controller.getMechanicDisplay(
                    request
            )
                    + " is responding to your emergency.";
        }

        if (request.isResolved()) {

            return "Emergency assistance has been completed.";
        }

        if (request.isCancelled()) {

            return "This SOS request was cancelled.";
        }

        return "SOS status: "
                + controller.getStatus(
                request
        );
    }

    // =====================================================
    // SMALL INFO BOX
    // =====================================================

    private VBox createSmallInfoBox(
            String title,
            String value
    ) {

        VBox box =
                new VBox(4);

        box.setPadding(
                new Insets(
                        10
                )
        );

        box.setMaxWidth(
                Double.MAX_VALUE
        );

        box.setStyle(
                "-fx-background-color: "
                        + SECONDARY_SURFACE
                        + ";"
                        + "-fx-background-radius: 8;"
        );

        Label titleLabel =
                new Label(
                        title
                );

        titleLabel.setTextFill(
                Color.web(
                        SECONDARY_TEXT
                )
        );

        titleLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        9
                )
        );

        Label valueLabel =
                new Label(
                        firstNonBlank(
                                value,
                                "-"
                        )
                );

        valueLabel.setWrapText(
                true
        );

        valueLabel.setTextFill(
                Color.web(
                        HEADING
                )
        );

        valueLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        11
                )
        );

        box.getChildren()
                .addAll(
                        titleLabel,
                        valueLabel
                );

        return box;
    }

    // =====================================================
    // DETAIL BOX
    // =====================================================

    private VBox createDetailBox(
            String title,
            String value
    ) {

        VBox box =
                new VBox(5);

        box.setPadding(
                new Insets(
                        13
                )
        );

        box.setStyle(
                "-fx-background-color: "
                        + SECONDARY_SURFACE
                        + ";"
                        + "-fx-border-color: "
                        + BORDER
                        + ";"
                        + "-fx-border-radius: 9;"
                        + "-fx-background-radius: 9;"
        );

        Label heading =
                new Label(
                        title
                );

        heading.setTextFill(
                Color.web(
                        SECONDARY_TEXT
                )
        );

        heading.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        9
                )
        );

        Label content =
                new Label(
                        firstNonBlank(
                                value,
                                "-"
                        )
                );

        content.setWrapText(
                true
        );

        content.setTextFill(
                Color.web(
                        HEADING
                )
        );

        content.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        12
                )
        );

        box.getChildren()
                .addAll(
                        heading,
                        content
                );

        return box;
    }

    // =====================================================
    // FIELD LABEL
    // =====================================================

    private Label createFieldLabel(
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
                        11
                )
        );

        return label;
    }

    // =====================================================
    // TEXT FIELD STYLE
    // =====================================================

    private void styleTextField(
            TextField field
    ) {

        field.setPrefHeight(
                40
        );

        field.setStyle(
                "-fx-background-color: white;"
                        + "-fx-text-fill: "
                        + HEADING
                        + ";"
                        + "-fx-prompt-text-fill: #80909F;"
                        + "-fx-border-color: "
                        + BORDER
                        + ";"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
                        + "-fx-padding: 0 12 0 12;"
        );
    }

    // =====================================================
    // COMMON CARD
    // =====================================================

    private VBox createCard() {

        VBox card =
                new VBox(13);

        card.setPadding(
                new Insets(
                        20
                )
        );

        card.setMinWidth(
                0
        );

        card.setMaxWidth(
                Double.MAX_VALUE
        );

        card.setStyle(
                "-fx-background-color: "
                        + CARD_SURFACE
                        + ";"
                        + "-fx-border-color: "
                        + BORDER
                        + ";"
                        + "-fx-border-radius: 16;"
                        + "-fx-background-radius: 16;"
        );

        return card;
    }

    // =====================================================
    // PRIMARY BUTTON
    // =====================================================

    private Button createPrimaryButton(
            String text,
            String color
    ) {

        Button button =
                new Button(
                        text
                );

        button.setMaxWidth(
                Double.MAX_VALUE
        );

        button.setPrefHeight(
                44
        );

        button.setTextFill(
                Color.WHITE
        );

        button.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        12
                )
        );

        button.setStyle(
                "-fx-background-color: "
                        + color
                        + ";"
                        + "-fx-background-radius: 9;"
                        + "-fx-cursor: hand;"
        );

        return button;
    }

    // =====================================================
    // OUTLINE BUTTON
    // =====================================================

    private Button createOutlineButton(
            String text,
            String color
    ) {

        Button button =
                new Button(
                        text
                );

        button.setPadding(
                new Insets(
                        8,
                        14,
                        8,
                        14
                )
        );

        button.setTextFill(
                Color.web(
                        color
                )
        );

        button.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        11
                )
        );

        button.setStyle(
                "-fx-background-color: transparent;"
                        + "-fx-border-color: "
                        + color
                        + ";"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
                        + "-fx-cursor: hand;"
        );

        return button;
    }

    // =====================================================
    // STATUS BADGE
    // =====================================================

    private Label createStatusBadge(
            String status
    ) {

        String text =
                firstNonBlank(
                        status,
                        "No Active SOS"
                );

        String color =
                getStatusColor(
                        text
                );

        Label badge =
                new Label(
                        "●  "
                                + text
                );

        badge.setPadding(
                new Insets(
                        7,
                        12,
                        7,
                        12
                )
        );

        badge.setTextFill(
                Color.web(
                        color
                )
        );

        badge.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        11
                )
        );

        badge.setStyle(
                "-fx-background-color: "
                        + color
                        + "18;"
                        + "-fx-background-radius: 16;"
        );

        return badge;
    }

    // =====================================================
    // STATUS COLOR
    // =====================================================

    private String getStatusColor(
            String status
    ) {

        if (status == null) {

            return SECONDARY_TEXT;
        }

        if (status.equalsIgnoreCase(
                "Pending"
        )) {

            return WARNING;
        }

        if (status.equalsIgnoreCase(
                "Assigned"
        )) {

            return NAV_BLUE;
        }

        if (status.equalsIgnoreCase(
                "Accepted"
        )) {

            return PURPLE;
        }

        if (status.equalsIgnoreCase(
                "In Progress"
        )) {

            return CYAN;
        }

        if (status.equalsIgnoreCase(
                "Resolved"
        )) {

            return SUCCESS;
        }

        if (status.equalsIgnoreCase(
                "Cancelled"
        )) {

            return EMERGENCY;
        }

        return SECONDARY_TEXT;
    }

    // =====================================================
    // EMPTY LABEL
    // =====================================================

    private Label createEmptyLabel(
            String text
    ) {

        Label label =
                new Label(
                        text
                );

        label.setWrapText(
                true
        );

        label.setTextFill(
                Color.web(
                        SECONDARY_TEXT
                )
        );

        label.setFont(
                Font.font(
                        "Arial",
                        12
                )
        );

        label.setPadding(
                new Insets(
                        12,
                        0,
                        12,
                        0
                )
        );

        return label;
    }

    // =====================================================
    // VALID LATITUDE
    // =====================================================

    private boolean isValidLatitude(
            String value
    ) {

        try {

            double latitude =
                    Double.parseDouble(
                            value
                    );

            return latitude >= -90
                    &&
                    latitude <= 90;

        } catch (Exception e) {

            return false;
        }
    }

    // =====================================================
    // VALID LONGITUDE
    // =====================================================

    private boolean isValidLongitude(
            String value
    ) {

        try {

            double longitude =
                    Double.parseDouble(
                            value
                    );

            return longitude >= -180
                    &&
                    longitude <= 180;

        } catch (Exception e) {

            return false;
        }
    }

    // =====================================================
    // FORMAT STRING DATE
    // =====================================================

    private String formatDate(
            String value
    ) {

        String date =
                clean(
                        value
                );

        if (date == null) {

            return "-";
        }

        try {

            long millis =
                    Long.parseLong(
                            date
                    );

            if (millis > 0
                    &&
                    millis < 100000000000L) {

                millis *=
                        1000L;
            }

            return formatMillis(
                    millis
            );

        } catch (Exception e) {

            return date;
        }
    }

    // =====================================================
    // FORMAT OBJECT DATE
    // =====================================================

    private String formatDateObject(
            Object value
    ) {

        if (value == null) {

            return "-";
        }

        if (value instanceof Number) {

            long millis =
                    ((Number) value)
                            .longValue();

            if (millis > 0
                    &&
                    millis < 100000000000L) {

                millis *=
                        1000L;
            }

            return formatMillis(
                    millis
            );
        }

        if (value instanceof Date) {

            return formatMillis(
                    ((Date) value)
                            .getTime()
            );
        }

        if (value instanceof Timestamp) {

            return formatMillis(
                    ((Timestamp) value)
                            .toSqlTimestamp()
                            .getTime()
            );
        }

        return formatDate(
                String.valueOf(
                        value
                )
        );
    }

    // =====================================================
    // FORMAT MILLIS
    // =====================================================

    private String formatMillis(
            long millis
    ) {

        try {

            DateTimeFormatter formatter =
                    DateTimeFormatter
                            .ofPattern(
                                    "dd MMM yyyy, hh:mm a"
                            )
                            .withZone(
                                    ZoneId.systemDefault()
                            );

            return formatter.format(
                    Instant.ofEpochMilli(
                            millis
                    )
            );

        } catch (Exception e) {

            return "-";
        }
    }

    // =====================================================
    // FIREBASE MAP STRING
    // =====================================================

    private String getString(
            Map<String, Object> map,
            String key,
            String defaultValue
    ) {

        if (map == null) {

            return defaultValue;
        }

        Object value =
                map.get(
                        key
                );

        if (value == null) {

            return defaultValue;
        }

        String result =
                String.valueOf(
                        value
                ).trim();

        return result.isEmpty()
                ? defaultValue
                : result;
    }

    // =====================================================
    // INITIALS
    // =====================================================

    private String createInitials(
            String name
    ) {

        String value =
                clean(
                        name
                );

        if (value == null) {

            return "?";
        }

        String[] parts =
                value.split(
                        "\\s+"
                );

        if (parts.length == 1) {

            return parts[0]
                    .substring(
                            0,
                            Math.min(
                                    2,
                                    parts[0].length()
                            )
                    )
                    .toUpperCase();
        }

        return (
                String.valueOf(
                        parts[0].charAt(0)
                )
                        +
                parts[1].charAt(0)
        ).toUpperCase();
    }

    // =====================================================
    // FIRST NON BLANK
    // =====================================================

    private String firstNonBlank(
            String... values
    ) {

        if (values == null) {

            return null;
        }

        for (String value :
                values) {

            String cleaned =
                    clean(
                            value
                    );

            if (cleaned != null) {

                return cleaned;
            }
        }

        return null;
    }

    // =====================================================
    // CLEAN
    // =====================================================

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

    // =====================================================
    // INFORMATION ALERT
    // =====================================================

    private void showInformation(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        if (sosScene != null
                &&
                sosScene.getWindow() != null) {

            alert.initOwner(
                    sosScene.getWindow()
            );
        }

        alert.setTitle(
                "RoadGuardian"
        );

        alert.setHeaderText(
                title
        );

        alert.setContentText(
                message
        );

        alert.showAndWait();
    }

    // =====================================================
    // ERROR ALERT
    // =====================================================

    private void showError(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        if (sosScene != null
                &&
                sosScene.getWindow() != null) {

            alert.initOwner(
                    sosScene.getWindow()
            );
        }

        alert.setTitle(
                "RoadGuardian"
        );

        alert.setHeaderText(
                title
        );

        alert.setContentText(
                message
        );

        alert.showAndWait();
    }

    // =====================================================
    // SCROLLBAR STYLE
    // =====================================================

    private void addScrollbarStyle(
            Scene scene
    ) {

        if (scene == null) {

            return;
        }

        scene.getStylesheets()
                .add(
                        "data:text/css,"
                                +
                        ".scroll-bar:vertical {"
                                +
                        "-fx-background-color: #C4D9E0;"
                                +
                        "-fx-background-radius: 8;"
                                +
                        "-fx-padding: 2;"
                                +
                        "}"
                                +
                        ".scroll-bar:vertical .track {"
                                +
                        "-fx-background-color: #C4D9E0;"
                                +
                        "-fx-background-radius: 8;"
                                +
                        "}"
                                +
                        ".scroll-bar:vertical .thumb {"
                                +
                        "-fx-background-color: #A1A1AA;"
                                +
                        "-fx-background-radius: 8;"
                                +
                        "}"
                                +
                        ".scroll-bar:vertical .increment-button,"
                                +
                        ".scroll-bar:vertical .decrement-button {"
                                +
                        "-fx-background-color: transparent;"
                                +
                        "-fx-padding: 0;"
                                +
                        "}"
                                +
                        ".scroll-bar:horizontal {"
                                +
                        "-fx-opacity: 0;"
                                +
                        "-fx-max-height: 0;"
                                +
                        "-fx-pref-height: 0;"
                                +
                        "}"
                );
    }

    // =====================================================
    // FALLBACK TIMELINE OBJECT
    // =====================================================

    private static class TimelineItem {

        private final String title;

        private final String status;

        private final String time;

        private TimelineItem(
                String title,
                String status,
                String time
        ) {

            this.title =
                    title;

            this.status =
                    status;

            this.time =
                    time;
        }
    }
}