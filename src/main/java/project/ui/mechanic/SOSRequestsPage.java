package project.ui.mechanic;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.Cursor;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import javafx.scene.paint.Color;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import project.controller.mechanic.MechanicSOSController;
import project.model.SOSRequest;
import project.util.Theme;
import project.util.IconUtil;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

public class SOSRequestsPage {

    // =====================================================
    // CONTROLLER
    // =====================================================

    private final MechanicSOSController controller;

    // =====================================================
    // UI
    // =====================================================

    private VBox pageContainer;

    private VBox requestsContainer;

    private TextField searchField;

    private ComboBox<String> statusFilter;

    // =====================================================
    // STATISTICS
    // =====================================================

    private Label assignedValueLabel;

    private Label acceptedValueLabel;

    private Label progressValueLabel;

    private Label resolvedValueLabel;

    // =====================================================
    // DATA
    // =====================================================

    private List<SOSRequest> currentRequests =
            new ArrayList<>();

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public SOSRequestsPage() {

        controller =
                new MechanicSOSController();
    }

    // =====================================================
    // GET CONTENT
    // =====================================================

    public ScrollPane getContent() {

        return createContent();
    }

    // =====================================================
    // REFRESH
    // =====================================================

    public void refresh() {

        if (pageContainer != null) {

            loadRequests();
        }
    }

    // =====================================================
    // CREATE CONTENT
    // =====================================================

    private ScrollPane createContent() {

        pageContainer =
                new VBox(20);

        pageContainer.setPadding(
                new Insets(
                        30,
                        35,
                        35,
                        35
                )
        );

        pageContainer.setStyle(
                "-fx-background-color: "
                        + Theme.BACKGROUND
                        + ";"
        );

        buildPage();

        ScrollPane scrollPane =
                new ScrollPane(
                        pageContainer
                );

        scrollPane.setFitToWidth(
                true
        );

        scrollPane.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        scrollPane.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED
        );

        scrollPane.setStyle(
                "-fx-background-color: transparent;"
                        + "-fx-background: transparent;"
        );

        return scrollPane;
    }

    // =====================================================
    // BUILD PAGE
    // =====================================================

    private void buildPage() {

        if (pageContainer == null) {

            return;
        }

        pageContainer
                .getChildren()
                .clear();

        VBox header =
                createHeader();

        HBox statistics =
                createStatistics();

        HBox filters =
                createFilters();

        VBox requestsCard =
                createRequestsCard();

        pageContainer
                .getChildren()
                .addAll(
                        header,
                        statistics,
                        filters,
                        requestsCard
                );

        loadRequests();
    }

    // =====================================================
    // HEADER
    // =====================================================

    private VBox createHeader() {

        VBox box =
                new VBox(6);

        Label title =
                new Label(
                        "Emergency SOS Requests"
                );

        title.setTextFill(
                Color.web(
                        Theme.HEADING
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
                        "View emergency requests assigned to you and update the response status."
                );

        subtitle.setTextFill(
                Color.web(
                        Theme.SECONDARY_TEXT
                )
        );

        subtitle.setFont(
                Font.font(
                        "Arial",
                        14
                )
        );

        box.getChildren()
                .addAll(
                        title,
                        subtitle
                );

        return box;
    }

    // =====================================================
    // STATISTICS
    // =====================================================

    private HBox createStatistics() {

        HBox stats =
                new HBox(14);

        assignedValueLabel =
                new Label("0");

        acceptedValueLabel =
                new Label("0");

        progressValueLabel =
                new Label("0");

        resolvedValueLabel =
                new Label("0");

        VBox assigned =
                createStatCard(
                        "Assigned",
                        assignedValueLabel,
                        "Waiting for your response",
                        Theme.INFO
                );

        VBox accepted =
                createStatCard(
                        "Accepted",
                        acceptedValueLabel,
                        "Ready to respond",
                        "#F59E0B"
                );

        VBox progress =
                createStatCard(
                        "In Progress",
                        progressValueLabel,
                        "Emergency response active",
                        "#F59E0B"
                );

        VBox resolved =
                createStatCard(
                        "Resolved",
                        resolvedValueLabel,
                        "Completed emergencies",
                        Theme.SUCCESS
                );

        HBox.setHgrow(
                assigned,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                accepted,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                progress,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                resolved,
                Priority.ALWAYS
        );

        stats.getChildren()
                .addAll(
                        assigned,
                        accepted,
                        progress,
                        resolved
                );

        return stats;
    }

    // =====================================================
    // STAT CARD
    // =====================================================

    private VBox createStatCard(
            String title,
            Label valueLabel,
            String subtitle,
            String color
    ) {

        VBox card =
                new VBox(6);

        card.setPadding(
                new Insets(17)
        );

        card.setMinHeight(
                110
        );

        card.setMaxWidth(
                Double.MAX_VALUE
        );

        card.setStyle(
                "-fx-background-color: "
                        + Theme.CARD
                        + ";"
                        + "-fx-border-color: "
                        + "#FACC15"
                        + ";"
                        + "-fx-border-radius: 12;"
                        + "-fx-background-radius: 12;"
        );

        Label titleLabel =
                new Label(
                        title
                );

        titleLabel.setTextFill(
                Color.web(
                        Theme.HEADING
                )
        );

        titleLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        13
                )
        );

        valueLabel.setTextFill(
                Color.web(
                        color
                )
        );

        valueLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        28
                )
        );

        Label subtitleLabel =
                new Label(
                        subtitle
                );

        subtitleLabel.setTextFill(
                Color.web(
                        Theme.SECONDARY_TEXT
                )
        );

        subtitleLabel.setFont(
                Font.font(
                        "Arial",
                        10
                )
        );

        card.getChildren()
                .addAll(
                        titleLabel,
                        valueLabel,
                        subtitleLabel
                );

        return card;
    }

    // =====================================================
    // FILTERS
    // =====================================================

    private HBox createFilters() {

        HBox box =
                new HBox(12);

        box.setAlignment(
                Pos.CENTER_LEFT
        );

        searchField =
                new TextField();

        searchField.setPromptText(
                "Search customer, SOS ID, vehicle, emergency or location..."
        );

        searchField.setPrefWidth(
                390
        );

        searchField.setPrefHeight(
                43
        );

        searchField.setStyle(
                "-fx-background-color: "
                        + Theme.CARD
                        + ";"
                        + "-fx-border-color: "
                        + Theme.BORDER
                        + ";"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
                        + "-fx-padding: 0 13 0 13;"
                        + "-fx-font-size: 12px;"
        );

        statusFilter =
                new ComboBox<>();

        statusFilter
                .getItems()
                .addAll(
                        "Active",
                        "All",
                        "Assigned",
                        "Accepted",
                        "In Progress",
                        "Resolved",
                        "Cancelled"
                );

        statusFilter.setValue(
                "Active"
        );

        statusFilter.setPrefWidth(
                150
        );

        statusFilter.setPrefHeight(
                43
        );

        statusFilter.setStyle(
                "-fx-background-color: "
                        + Theme.CARD
                        + ";"
                        + "-fx-border-color: "
                        + Theme.BORDER
                        + ";"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
        );

        Button refresh =
                createButton(
                        "Refresh",
                        Theme.INFO
                );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Label live =
                new Label(
                        "● Active"
                );

        live.setTextFill(
                Color.web(
                        Theme.SUCCESS
                )
        );

        live.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        10
                )
        );

        searchField
                .textProperty()
                .addListener(
                        (
                                observable,
                                oldValue,
                                newValue
                        ) -> applyFilters()
                );

        statusFilter
                .valueProperty()
                .addListener(
                        (
                                observable,
                                oldValue,
                                newValue
                        ) -> applyFilters()
                );

        refresh.setOnAction(
                event ->
                        loadRequests()
        );

        box.getChildren()
                .addAll(
                        searchField,
                        statusFilter,
                        refresh,
                        spacer,
                        live
                );

        return box;
    }

    // =====================================================
    // REQUESTS CARD
    // =====================================================

    private VBox createRequestsCard() {

        VBox card =
                new VBox(14);

        card.setPadding(
                new Insets(20)
        );

        card.setStyle(
                "-fx-background-color: "
                        + Theme.CARD
                        + ";"
                        + "-fx-border-color: "
                        + "#FACC15"
                        + ";"
                        + "-fx-border-radius: 13;"
                        + "-fx-background-radius: 13;"
        );

        Label title =
                new Label(
                        "My SOS Requests"
                );

        title.setTextFill(
                Color.web(
                        Theme.HEADING
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
                new Label(
                        "Only SOS requests assigned to your mechanic account are shown here."
                );

        description.setTextFill(
                Color.web(
                        Theme.SECONDARY_TEXT
                )
        );

        description.setFont(
                Font.font(
                        "Arial",
                        12
                )
        );

        requestsContainer =
                new VBox(12);

        card.getChildren()
                .addAll(
                        title,
                        description,
                        new Separator(),
                        requestsContainer
                );

        return card;
    }

    // =====================================================
    // LOAD REQUESTS
    // =====================================================

    private void loadRequests() {

        if (requestsContainer == null) {

            return;
        }

        requestsContainer
                .getChildren()
                .clear();

        Label loading =
                new Label(
                        "Loading SOS requests..."
                );

        loading.setTextFill(
                Color.web(
                        Theme.SECONDARY_TEXT
                )
        );

        loading.setPadding(
                new Insets(20)
        );

        requestsContainer
                .getChildren()
                .add(
                        loading
                );

        try {

            List<SOSRequest> requests =
                    controller
                            .getAllMyRequests();

            currentRequests.clear();

            if (requests != null) {

                currentRequests.addAll(
                        requests
                );
            }

            updateStatistics();

            applyFilters();

        } catch (Exception e) {

            e.printStackTrace();

            requestsContainer
                    .getChildren()
                    .clear();

            requestsContainer
                    .getChildren()
                    .add(
                            createMessage(
                                    "Unable to load SOS requests."
                            )
                    );
        }
    }

    // =====================================================
    // UPDATE STATISTICS
    // =====================================================

    private void updateStatistics() {

        int assigned =
                0;

        int accepted =
                0;

        int inProgress =
                0;

        int resolved =
                0;

        for (SOSRequest request :
                currentRequests) {

            if (request == null) {

                continue;
            }

            if (request.isAssigned()) {

                assigned++;

            } else if (request.isAccepted()) {

                accepted++;

            } else if (request.isInProgress()) {

                inProgress++;

            } else if (request.isResolved()) {

                resolved++;
            }
        }

        assignedValueLabel.setText(
                String.valueOf(
                        assigned
                )
        );

        acceptedValueLabel.setText(
                String.valueOf(
                        accepted
                )
        );

        progressValueLabel.setText(
                String.valueOf(
                        inProgress
                )
        );

        resolvedValueLabel.setText(
                String.valueOf(
                        resolved
                )
        );
    }

    // =====================================================
    // FILTER REQUESTS
    // =====================================================

    private void applyFilters() {

        if (requestsContainer == null) {

            return;
        }

        String search =
                searchField == null
                        ? ""
                        : searchField
                        .getText()
                        .trim()
                        .toLowerCase();

        String status =
                statusFilter == null
                        ? "Active"
                        : statusFilter
                        .getValue();

        List<SOSRequest> filtered =
                new ArrayList<>();

        for (SOSRequest request :
                currentRequests) {

            if (request == null) {

                continue;
            }

            if (!matchesSearch(
                    request,
                    search
            )) {

                continue;
            }

            if (!matchesStatus(
                    request,
                    status
            )) {

                continue;
            }

            filtered.add(
                    request
            );
        }

        renderRequests(
                filtered
        );
    }

    // =====================================================
    // SEARCH MATCH
    // =====================================================

    private boolean matchesSearch(
            SOSRequest request,
            String search
    ) {

        if (search == null
                ||
                search.isBlank()) {

            return true;
        }

        return contains(
                request.getSosId(),
                search
        )
                ||
                contains(
                        request.getCustomerName(),
                        search
                )
                ||
                contains(
                        request.getCustomerId(),
                        search
                )
                ||
                contains(
                        request.getVehicleNumber(),
                        search
                )
                ||
                contains(
                        request.getEmergencyType(),
                        search
                )
                ||
                contains(
                        request.getDescription(),
                        search
                )
                ||
                contains(
                        request.getLocation(),
                        search
                )
                ||
                contains(
                        request.getStatus(),
                        search
                );
    }

    // =====================================================
    // STATUS MATCH
    // =====================================================

    private boolean matchesStatus(
            SOSRequest request,
            String status
    ) {

        if (status == null
                ||
                status.equalsIgnoreCase(
                        "All"
                )) {

            return true;
        }

        if (status.equalsIgnoreCase(
                "Active"
        )) {

            return request.isAssigned()
                    ||
                    request.isAccepted()
                    ||
                    request.isInProgress();
        }

        return request.getStatus() != null
                &&
                request
                        .getStatus()
                        .equalsIgnoreCase(
                                status
                        );
    }

    // =====================================================
    // RENDER
    // =====================================================

    private void renderRequests(
            List<SOSRequest> requests
    ) {

        requestsContainer
                .getChildren()
                .clear();

        if (requests == null
                ||
                requests.isEmpty()) {

            requestsContainer
                    .getChildren()
                    .add(
                            createEmptyState()
                    );

            return;
        }

        int number =
                1;

        for (SOSRequest request :
                requests) {

            requestsContainer
                    .getChildren()
                    .add(
                            createRequestCard(
                                    request,
                                    number++
                            )
                    );
        }
    }

    // =====================================================
    // REQUEST CARD
    // =====================================================

    private VBox createRequestCard(
            SOSRequest request,
            int number
    ) {

        VBox card =
                new VBox(13);

        card.setPadding(
                new Insets(
                        18
                )
        );

        card.setStyle(
                "-fx-background-color: "
                        + Theme.SECONDARY_SURFACE
                        + ";"
                        + "-fx-border-color: "
                        + "#FACC15"
                        + ";"
                        + "-fx-border-radius: 11;"
                        + "-fx-background-radius: 11;"
        );

        // =================================================
        // TOP
        // =================================================

        HBox top =
                new HBox(12);

        top.setAlignment(
                Pos.CENTER_LEFT
        );

        Label sosIcon =
                new Label(
                        "SOS"
                );

        sosIcon.setAlignment(
                Pos.CENTER
        );

        sosIcon.setMinSize(
                48,
                48
        );

        sosIcon.setTextFill(
                Color.WHITE
        );

        sosIcon.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        11
                )
        );

        sosIcon.setStyle(
                "-fx-background-color: "
                        + Theme.ERROR
                        + ";"
                        + "-fx-background-radius: 24;"
        );

        VBox titleBox =
                new VBox(4);

        Label customer =
                new Label(
                        controller
                                .getCustomerDisplay(
                                        request
                                )
                );

        customer.setTextFill(
                Color.web(
                        Theme.HEADING
                )
        );

        customer.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        17
                )
        );

        // UI-only: show a user silhouette for the customer representation.
        customer.setGraphic(
                IconUtil.createUserIcon(
                        0.65,
                        Theme.INFO
                )
        );
        customer.setGraphicTextGap(8);

        Label requestId =
                new Label(
                        "SOS #"
                                + firstNonBlank(
                                request.getSosId(),
                                String.valueOf(number)
                        )
                );

        requestId.setTextFill(
                Color.web(
                        Theme.SECONDARY_TEXT
                )
        );

        requestId.setFont(
                Font.font(
                        "Arial",
                        11
                )
        );

        titleBox.getChildren()
                .addAll(
                        customer,
                        requestId
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

        top.getChildren()
                .addAll(
                        sosIcon,
                        titleBox,
                        spacer,
                        status
                );

        // =================================================
        // INFORMATION
        // =================================================

        HBox information =
                new HBox(10);

        VBox emergency =
                createInfoBox(
                        "EMERGENCY",
                        controller
                                .getEmergencyDisplay(
                                        request
                                )
                );

        VBox vehicle =
                createInfoBox(
                        "VEHICLE",
                        controller
                                .getVehicleDisplay(
                                        request
                                )
                );

        VBox location =
                createInfoBox(
                        "LOCATION",
                        controller
                                .getLocationDisplay(
                                        request
                                )
                );

        VBox date =
                createInfoBox(
                        "REQUESTED",
                        formatDate(
                                request.getRequestDate()
                        )
                );

        HBox.setHgrow(
                emergency,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                vehicle,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                location,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                date,
                Priority.ALWAYS
        );

        information.getChildren()
                .addAll(
                        emergency,
                        vehicle,
                        location,
                        date
                );

        // =================================================
        // DESCRIPTION
        // =================================================

        VBox descriptionBox =
                new VBox(4);

        Label descriptionTitle =
                new Label(
                        "PROBLEM / DESCRIPTION"
                );

        descriptionTitle.setTextFill(
                Color.web(
                        Theme.SECONDARY_TEXT
                )
        );

        descriptionTitle.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        9
                )
        );

        Label description =
                new Label(
                        controller
                                .getDescriptionDisplay(
                                        request
                                )
                );

        description.setWrapText(
                true
        );

        description.setTextFill(
                Color.web(
                        Theme.TEXT
                )
        );

        description.setFont(
                Font.font(
                        "Arial",
                        12
                )
        );

        descriptionBox
                .getChildren()
                .addAll(
                        descriptionTitle,
                        description
                );

        // =================================================
        // GPS
        // =================================================

        Label gps =
                new Label(
                        "GPS: "
                                + controller
                                .getGPSDisplay(
                                        request
                                )
                );

        gps.setTextFill(
                Color.web(
                        Theme.SECONDARY_TEXT
                )
        );

        gps.setFont(
                Font.font(
                        "Arial",
                        11
                )
        );

        // =================================================
        // ACTIONS
        // =================================================

        HBox actions =
                createActions(
                        request
                );

        card.getChildren()
                .addAll(
                        top,
                        new Separator(),
                        information,
                        descriptionBox,
                        gps,
                        actions
                );

        return card;
    }

    // =====================================================
    // ACTIONS
    // =====================================================

    private HBox createActions(
            SOSRequest request
    ) {

        HBox actions =
                new HBox(10);

        actions.setAlignment(
                Pos.CENTER_RIGHT
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        actions.getChildren()
                .add(
                        spacer
                );

        // =================================================
        // ASSIGNED
        //
        // ACCEPT / REJECT
        // =================================================

        if (request.isAssigned()) {

            Button reject =
                    createOutlineButton(
                            "Reject",
                            Theme.ERROR
                    );

            Button accept =
                    createButton(
                            "Accept SOS",
                            Theme.SUCCESS
                    );

            reject.setOnAction(
                    event ->
                            rejectRequest(
                                    request
                            )
            );

            accept.setOnAction(
                    event ->
                            acceptRequest(
                                    request
                            )
            );

            actions.getChildren()
                    .addAll(
                            reject,
                            accept
                    );

            return actions;
        }

        // =================================================
        // ACCEPTED
        //
        // NAVIGATION / START RESPONSE
        // =================================================

        if (request.isAccepted()) {

            Button navigation =
                    createOutlineButton(
                            "Start Navigation",
                            Theme.INFO
                    );

            Button start =
                    createButton(
                            "Start Response",
                            Theme.PRIMARY
                    );

            navigation.setOnAction(
                    event ->
                            startNavigation(
                                    request
                            )
            );

            start.setOnAction(
                    event ->
                            startResponse(
                                    request
                            )
            );

            actions.getChildren()
                    .addAll(
                            navigation,
                            start
                    );

            return actions;
        }

        // =================================================
        // IN PROGRESS
        //
        // ARRIVED / RESOLVE
        // =================================================

        if (request.isInProgress()) {

            Button arrived =
                    createOutlineButton(
                            "Mark Arrived",
                            Theme.INFO
                    );

            Button resolve =
                    createButton(
                            "Resolve SOS",
                            Theme.SUCCESS
                    );

            arrived.setOnAction(
                    event ->
                            markArrived(
                                    request
                            )
            );

            resolve.setOnAction(
                    event ->
                            resolveRequest(
                                    request
                            )
            );

            actions.getChildren()
                    .addAll(
                            arrived,
                            resolve
                    );

            return actions;
        }

        // =================================================
        // FINISHED
        // =================================================

        Label finished =
                new Label(
                        request.isResolved()
                                ? "✓ Emergency Resolved"
                                : "Request Closed"
                );

        finished.setTextFill(
                Color.web(
                        request.isResolved()
                                ? Theme.SUCCESS
                                : Theme.SECONDARY_TEXT
                )
        );

        finished.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        12
                )
        );

        actions.getChildren()
                .add(
                        finished
                );

        return actions;
    }

    // =====================================================
    // ACCEPT
    // =====================================================

    private void acceptRequest(
            SOSRequest request
    ) {

        if (request == null) {

            return;
        }

        boolean success =
                controller
                        .acceptRequest(
                                request.getSosId()
                        );

        if (success) {

            showInfo(
                    "SOS Accepted",
                    "Emergency request accepted successfully.\n\n"
                            + "The customer and Admin can now see the status as Accepted."
            );

            loadRequests();

        } else {

            showError(
                    "Unable to Accept",
                    "The SOS request could not be accepted. "
                            + "Its status may have changed."
            );

            loadRequests();
        }
    }

    // =====================================================
    // REJECT
    // =====================================================

    private void rejectRequest(
            SOSRequest request
    ) {

        if (request == null) {

            return;
        }

        Alert confirmation =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        confirmation.setTitle(
                "RoadGuardian"
        );

        confirmation.setHeaderText(
                "Reject SOS Request?"
        );

        confirmation.setContentText(
                "This request will return to the Admin Pending queue "
                        + "so another mechanic can be assigned."
        );

        confirmation
                .showAndWait()
                .ifPresent(
                        result -> {

                            if (result.getButtonData()
                                    .isCancelButton()) {

                                return;
                            }

                            boolean success =
                                    controller
                                            .rejectRequest(
                                                    request.getSosId()
                                            );

                            if (success) {

                                showInfo(
                                        "SOS Rejected",
                                        "The request was returned to the Admin queue."
                                );

                            } else {

                                showError(
                                        "Unable to Reject",
                                        "The SOS request could not be rejected."
                                );
                            }

                            loadRequests();
                        }
                );
    }

    // =====================================================
    // START RESPONSE
    // =====================================================

    private void startResponse(
            SOSRequest request
    ) {

        if (request == null) {

            return;
        }

        boolean success =
                controller
                        .startRequest(
                                request.getSosId()
                        );

        if (success) {

            showInfo(
                    "Response Started",
                    "SOS status is now In Progress."
            );

        } else {

            showError(
                    "Unable to Start",
                    "The SOS response could not be started."
            );
        }

        loadRequests();
    }

    // =====================================================
    // START NAVIGATION
    // =====================================================

    private void startNavigation(
            SOSRequest request
    ) {

        if (request == null) {

            return;
        }

        boolean success =
                controller
                        .startNavigation(
                                request.getSosId()
                        );

        if (success) {

            showInfo(
                    "Navigation Started",
                    "Navigation status has been saved to Firebase.\n\n"
                            + "Destination: "
                            + controller
                            .getLocationDisplay(
                                    request
                            )
            );

        } else {

            showError(
                    "Navigation Error",
                    "Navigation could not be started for this request."
            );
        }
    }

    // =====================================================
    // MARK ARRIVED
    // =====================================================

    private void markArrived(
            SOSRequest request
    ) {

        if (request == null) {

            return;
        }

        boolean success =
                controller
                        .markArrived(
                                request.getSosId()
                        );

        if (success) {

            showInfo(
                    "Arrival Updated",
                    "You have been marked as arrived at the customer location."
            );

        } else {

            showError(
                    "Arrival Error",
                    "Unable to update arrival status."
            );
        }
    }

    // =====================================================
    // RESOLVE
    // =====================================================

    private void resolveRequest(
            SOSRequest request
    ) {

        if (request == null) {

            return;
        }

        Alert confirmation =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        confirmation.setTitle(
                "RoadGuardian"
        );

        confirmation.setHeaderText(
                "Resolve SOS Emergency?"
        );

        confirmation.setContentText(
                "Confirm that emergency roadside assistance is completed."
        );

        confirmation
                .showAndWait()
                .ifPresent(
                        result -> {

                            if (result.getButtonData()
                                    .isCancelButton()) {

                                return;
                            }

                            boolean success =
                                    controller
                                            .resolveRequest(
                                                    request.getSosId()
                                            );

                            if (success) {

                                showInfo(
                                        "SOS Resolved",
                                        "Emergency assistance completed successfully."
                                );

                            } else {

                                showError(
                                        "Unable to Resolve",
                                        "SOS request could not be resolved."
                                );
                            }

                            loadRequests();
                        }
                );
    }

    // =====================================================
    // INFO BOX
    // =====================================================

    private VBox createInfoBox(
            String title,
            String value
    ) {

        VBox box =
                new VBox(4);

        box.setPadding(
                new Insets(10)
        );

        box.setMinWidth(
                150
        );

        box.setMaxWidth(
                Double.MAX_VALUE
        );

        box.setStyle(
                "-fx-background-color: "
                        + Theme.CARD
                        + ";"
                        + "-fx-background-radius: 8;"
        );

        Label titleLabel =
                new Label(
                        title
                );

        titleLabel.setTextFill(
                Color.web(
                        Theme.SECONDARY_TEXT
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

        valueLabel.setTextFill(
                Color.web(
                        Theme.HEADING
                )
        );

        valueLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        11
                )
        );

        valueLabel.setWrapText(
                true
        );

        // UI-only: show a car icon for vehicle information.
        if ("VEHICLE".equalsIgnoreCase(title)) {
            valueLabel.setGraphic(
                    IconUtil.createVehicleIcon(
                            0.55,
                            Theme.PRIMARY
                    )
            );
            valueLabel.setGraphicTextGap(7);
        }

        box.getChildren()
                .addAll(
                        titleLabel,
                        valueLabel
                );

        return box;
    }

    // =====================================================
    // STATUS BADGE
    // =====================================================

    private Label createStatusBadge(
            String status
    ) {

        status =
                firstNonBlank(
                        status,
                        "-"
                );

        String color =
                Theme.SECONDARY_TEXT;

        if (status.equalsIgnoreCase(
                "Assigned"
        )) {

            color =
                    Theme.INFO;

        } else if (status.equalsIgnoreCase(
                "Accepted"
        )) {

            color =
                    "#F59E0B";

        } else if (status.equalsIgnoreCase(
                "In Progress"
        )) {

            color =
                    "#F59E0B";

        } else if (status.equalsIgnoreCase(
                "Resolved"
        )) {

            color =
                    Theme.SUCCESS;

        } else if (status.equalsIgnoreCase(
                "Cancelled"
        )) {

            color =
                    Theme.ERROR;
        }

        Label label =
                new Label(
                        status
                );

        label.setTextFill(
                Color.web(
                        color
                )
        );

        label.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        11
                )
        );

        label.setPadding(
                new Insets(
                        7,
                        12,
                        7,
                        12
                )
        );

        label.setStyle(
                "-fx-background-color: "
                        + color
                        + "18;"
                        + "-fx-background-radius: 16;"
        );

        return label;
    }

    // =====================================================
    // BUTTON
    // =====================================================

    private Button createButton(
            String text,
            String color
    ) {

        Button button =
                new Button(
                        text
                );

        button.setCursor(
                Cursor.HAND
        );

        button.setTextFill(
                Color.WHITE
        );

        button.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        11
                )
        );

        button.setPadding(
                new Insets(
                        9,
                        14,
                        9,
                        14
                )
        );

        button.setStyle(
                "-fx-background-color: "
                        + color
                        + ";"
                        + "-fx-background-radius: 8;"
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

        button.setCursor(
                Cursor.HAND
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

        button.setPadding(
                new Insets(
                        9,
                        14,
                        9,
                        14
                )
        );

        button.setStyle(
                "-fx-background-color: transparent;"
                        + "-fx-border-color: "
                        + color
                        + ";"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
        );

        return button;
    }

    // =====================================================
    // EMPTY STATE
    // =====================================================

    private VBox createEmptyState() {

        VBox box =
                new VBox(8);

        box.setAlignment(
                Pos.CENTER
        );

        box.setPadding(
                new Insets(45)
        );

        Label title =
                new Label(
                        "No SOS requests found"
                );

        title.setTextFill(
                Color.web(
                        Theme.HEADING
                )
        );

        title.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        17
                )
        );

        String selected =
                statusFilter == null
                        ? "Active"
                        : statusFilter
                        .getValue();

        Label subtitle =
                new Label(
                        "Assigned".equalsIgnoreCase(selected)
                                ? "New SOS requests assigned by Admin will appear here."
                                : "There are no SOS requests for the selected filter."
                );

        subtitle.setTextFill(
                Color.web(
                        Theme.SECONDARY_TEXT
                )
        );

        subtitle.setFont(
                Font.font(
                        "Arial",
                        12
                )
        );

        box.getChildren()
                .addAll(
                        title,
                        subtitle
                );

        return box;
    }

    // =====================================================
    // MESSAGE
    // =====================================================

    private VBox createMessage(
            String text
    ) {

        VBox box =
                new VBox();

        box.setAlignment(
                Pos.CENTER
        );

        box.setPadding(
                new Insets(30)
        );

        Label label =
                new Label(
                        text
                );

        label.setTextFill(
                Color.web(
                        Theme.ERROR
                )
        );

        box.getChildren()
                .add(
                        label
                );

        return box;
    }

    // =====================================================
    // FORMAT DATE
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

            /*
             * Handle seconds if some old data contains
             * 10-digit Unix timestamp.
             */
            if (millis > 0
                    &&
                    millis < 100000000000L) {

                millis *= 1000L;
            }

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

            /*
             * Old formatted dates stay readable.
             */
            return date;
        }
    }

    // =====================================================
    // SEARCH HELPER
    // =====================================================

    private boolean contains(
            String value,
            String search
    ) {

        return value != null
                &&
                search != null
                &&
                value
                        .toLowerCase()
                        .contains(
                                search
                        );
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
    // INFO ALERT
    // =====================================================

    private void showInfo(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

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
}