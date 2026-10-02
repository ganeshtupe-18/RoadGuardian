package project.ui.mechanic;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import project.controller.mechanic.RequestsController;
import project.model.ServiceRequest;
import project.util.Theme;
import project.util.IconUtil;

import java.util.ArrayList;
import java.util.List;

public class RequestsPage {

    // =====================================================
    // UI
    // =====================================================

    private VBox pageContainer;
    private VBox requestsContainer;
    private TextField searchField;

    private Label totalValueLabel;
    private Label emergencyValueLabel;
    private Label normalValueLabel;

    // =====================================================
    // CONTROLLER
    // =====================================================

    private final RequestsController controller;

    // =====================================================
    // ACTIVE JOB NAVIGATION
    // =====================================================

    private final ActiveJobPage activeJobPage;
    private final Runnable openActiveJob;

    // =====================================================
    // LOCAL DISPLAY DATA
    // =====================================================

    private List<ServiceRequest> currentRequests =
            new ArrayList<>();

    private String selectedType =
            "All";

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public RequestsPage(
            ActiveJobPage activeJobPage,
            Runnable openActiveJob
    ) {

        this.activeJobPage =
                activeJobPage;

        this.openActiveJob =
                openActiveJob;

        this.controller =
                new RequestsController();
    }

    // =====================================================
    // GET CONTENT
    // =====================================================

    public ScrollPane getContent() {

        return createContent();
    }

    // =====================================================
    // MAIN CONTENT
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

        buildRequestsPage();

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

        scrollPane.setStyle(
                "-fx-background-color: transparent;"
        );

        return scrollPane;
    }

    // =====================================================
    // BUILD PAGE
    // =====================================================

    private void buildRequestsPage() {

        if (pageContainer == null) {

            return;
        }

        pageContainer
                .getChildren()
                .clear();

        // =================================================
        // HEADER
        // =================================================

        VBox header =
                createHeader();

        // =================================================
        // STATS
        // =================================================

        HBox stats =
                createStats();

        // =================================================
        // FILTERS
        // =================================================

        HBox filters =
                createFilters();

        // =================================================
        // REQUEST CARD
        // =================================================

        VBox requestsCard =
                createRequestsCard();

        pageContainer
                .getChildren()
                .addAll(
                        header,
                        stats,
                        filters,
                        requestsCard
                );

        // =================================================
        // LOAD FIREBASE DATA
        // =================================================

        loadRequests();
    }

    // =====================================================
    // HEADER
    // =====================================================

    private VBox createHeader() {

        VBox header =
                new VBox(5);

        Label title =
                new Label(
                        "Service Requests"
                );

        title.setStyle(
                "-fx-font-size: 28px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: "
                        + Theme.TEXT
                        + ";"
        );

        Label subtitle =
                new Label(
                        "Requests assigned to you by the administrator."
                );

        subtitle.setStyle(
                "-fx-font-size: 13px;"
                        + "-fx-text-fill: "
                        + Theme.SECONDARY_TEXT
                        + ";"
        );

        header
                .getChildren()
                .addAll(
                        title,
                        subtitle
                );

        return header;
    }

    // =====================================================
    // STATS
    // =====================================================

    private HBox createStats() {

        HBox stats =
                new HBox(18);

        totalValueLabel =
                new Label("0");

        emergencyValueLabel =
                new Label("0");

        normalValueLabel =
                new Label("0");

        stats
                .getChildren()
                .addAll(
                        createStatCard(
                                "Assigned Requests",
                                totalValueLabel,
                                Theme.INFO
                        ),

                        createStatCard(
                                "Emergency",
                                emergencyValueLabel,
                                Theme.ERROR
                        ),

                        createStatCard(
                                "Normal",
                                normalValueLabel,
                                Theme.SUCCESS
                        )
                );

        return stats;
    }

    // =====================================================
    // STAT CARD
    // =====================================================

    private VBox createStatCard(
            String title,
            Label valueLabel,
            String accent
    ) {

        VBox card =
                new VBox(8);

        card.setPadding(
                new Insets(18)
        );

        card.setPrefHeight(
                110
        );

        card.setStyle(
                "-fx-background-color: "
                        + Theme.CARD
                        + ";"
                        + "-fx-background-radius: 14;"
                        + "-fx-border-color: "
                        + "#FACC15"
                        + ";"
                        + "-fx-border-radius: 14;"
        );

        HBox row =
                new HBox();

        row.setAlignment(
                Pos.CENTER_LEFT
        );

        Label titleLabel =
                new Label(
                        title
                );

        titleLabel.setStyle(
                "-fx-font-size: 11px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: "
                        + Theme.SECONDARY_TEXT
                        + ";"
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Circle circle =
                new Circle(18);

        circle.setFill(
                Color.web(
                        accent
                )
        );

        row
                .getChildren()
                .addAll(
                        titleLabel,
                        spacer,
                        circle
                );

        valueLabel.setStyle(
                "-fx-font-size: 26px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: "
                        + Theme.TEXT
                        + ";"
        );

        card
                .getChildren()
                .addAll(
                        row,
                        valueLabel
                );

        HBox.setHgrow(
                card,
                Priority.ALWAYS
        );

        return card;
    }

    // =====================================================
    // FILTERS
    // =====================================================

    private HBox createFilters() {

        HBox filters =
                new HBox(12);

        filters.setAlignment(
                Pos.CENTER_LEFT
        );

        searchField =
                new TextField();

        searchField.setPromptText(
                "Search customer, vehicle, service or location..."
        );

        searchField.setPrefWidth(
                430
        );

        searchField.setPadding(
                new Insets(12)
        );

        searchField.setStyle(
                "-fx-background-color: "
                        + Theme.CARD
                        + ";"
                        + "-fx-border-color: "
                        + Theme.BORDER
                        + ";"
                        + "-fx-border-radius: 9;"
                        + "-fx-background-radius: 9;"
                        + "-fx-font-size: 11px;"
                        + "-fx-text-fill: "
                        + Theme.TEXT
                        + ";"
        );

        searchField
                .textProperty()
                .addListener(
                        (
                                observable,
                                oldValue,
                                newValue
                        ) -> {

                            applyFilters();
                        }
                );

        Button allButton =
                createFilterButton(
                        "All",
                        true
                );

        Button emergencyButton =
                createFilterButton(
                        "Emergency",
                        false
                );

        Button normalButton =
                createFilterButton(
                        "Normal",
                        false
                );

        Button refreshButton =
                createFilterButton(
                        "Refresh",
                        false
                );

        // -------------------------------------------------
        // ALL
        // -------------------------------------------------

        allButton.setOnAction(
                e -> {

                    selectedType =
                            "All";

                    searchField.clear();

                    applyFilters();
                }
        );

        // -------------------------------------------------
        // EMERGENCY
        // -------------------------------------------------

        emergencyButton.setOnAction(
                e -> {

                    selectedType =
                            "Emergency";

                    searchField.clear();

                    applyFilters();
                }
        );

        // -------------------------------------------------
        // NORMAL
        // -------------------------------------------------

        normalButton.setOnAction(
                e -> {

                    selectedType =
                            "Normal";

                    searchField.clear();

                    applyFilters();
                }
        );

        // -------------------------------------------------
        // REFRESH FIREBASE
        // -------------------------------------------------

        refreshButton.setOnAction(
                e -> loadRequests()
        );

        filters
                .getChildren()
                .addAll(
                        searchField,
                        allButton,
                        emergencyButton,
                        normalButton,
                        refreshButton
                );

        return filters;
    }

    // =====================================================
    // FILTER BUTTON
    // =====================================================

    private Button createFilterButton(
            String text,
            boolean active
    ) {

        Button button =
                new Button(
                        text
                );

        button.setPadding(
                new Insets(
                        11,
                        18,
                        11,
                        18
                )
        );

        if (active) {

            button.setStyle(
                    "-fx-background-color: "
                            + Theme.TEXT
                            + ";"
                            + "-fx-text-fill: "
                            + Theme.WHITE
                            + ";"
                            + "-fx-background-radius: 9;"
                            + "-fx-font-size: 11px;"
                            + "-fx-font-weight: bold;"
                            + "-fx-cursor: hand;"
            );

        } else {

            button.setStyle(
                    "-fx-background-color: "
                            + Theme.CARD
                            + ";"
                            + "-fx-text-fill: "
                            + Theme.TEXT
                            + ";"
                            + "-fx-border-color: "
                            + Theme.BORDER
                            + ";"
                            + "-fx-border-radius: 9;"
                            + "-fx-background-radius: 9;"
                            + "-fx-font-size: 11px;"
                            + "-fx-cursor: hand;"
            );
        }

        return button;
    }

    // =====================================================
    // REQUESTS CARD
    // =====================================================

    private VBox createRequestsCard() {

        VBox card =
                new VBox(12);

        card.setPadding(
                new Insets(22)
        );

        card.setStyle(
                "-fx-background-color: "
                        + Theme.CARD
                        + ";"
                        + "-fx-background-radius: 14;"
                        + "-fx-border-color: "
                        + "#FACC15"
                        + ";"
                        + "-fx-border-radius: 14;"
        );

        HBox heading =
                new HBox();

        heading.setAlignment(
                Pos.CENTER_LEFT
        );

        Label title =
                new Label(
                        "Assigned Service Requests"
                );

        title.setStyle(
                "-fx-font-size: 17px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: "
                        + Theme.TEXT
                        + ";"
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

        live.setStyle(
                "-fx-font-size: 10px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: "
                        + Theme.SUCCESS
                        + ";"
        );

        heading
                .getChildren()
                .addAll(
                        title,
                        spacer,
                        live
                );

        requestsContainer =
                new VBox(10);

        Label loading =
                new Label(
                        "Loading assigned requests..."
                );

        loading.setStyle(
                "-fx-font-size: 12px;"
                        + "-fx-text-fill: "
                        + Theme.SECONDARY_TEXT
                        + ";"
                        + "-fx-padding: 20;"
        );

        requestsContainer
                .getChildren()
                .add(
                        loading
                );

        card
                .getChildren()
                .addAll(
                        heading,
                        requestsContainer
                );

        return card;
    }

    // =====================================================
    // LOAD FIREBASE REQUESTS
    // =====================================================

    private void loadRequests() {

        try {

            currentRequests =
                    controller
                            .getAssignedRequests();

            if (currentRequests == null) {

                currentRequests =
                        new ArrayList<>();
            }

            updateStatistics();

            applyFilters();

        } catch (Exception e) {

            currentRequests =
                    new ArrayList<>();

            updateStatistics();

            showEmptyMessage(
                    "Unable to load service requests."
            );

            showError(
                    "Firebase Error",
                    "Could not load assigned requests.\n\n"
                            + e.getMessage()
            );
        }
    }

    // =====================================================
    // UPDATE STATS
    // =====================================================

    private void updateStatistics() {

        int total =
                currentRequests.size();

        int emergency =
                0;

        int normal =
                0;

        for (ServiceRequest request :
                currentRequests) {

            String type =
                    controller
                            .getRequestType(
                                    request
                            );

            if (type.equalsIgnoreCase(
                    "Emergency"
            )) {

                emergency++;

            } else {

                normal++;
            }
        }

        if (totalValueLabel != null) {

            totalValueLabel.setText(
                    String.valueOf(
                            total
                    )
            );
        }

        if (emergencyValueLabel != null) {

            emergencyValueLabel.setText(
                    String.valueOf(
                            emergency
                    )
            );
        }

        if (normalValueLabel != null) {

            normalValueLabel.setText(
                    String.valueOf(
                            normal
                    )
            );
        }
    }

    // =====================================================
    // APPLY SEARCH + TYPE FILTER
    // =====================================================

    private void applyFilters() {

        if (requestsContainer == null) {

            return;
        }

        List<ServiceRequest> filtered =
                new ArrayList<>();

        String search =
                searchField == null
                        ? ""
                        : searchField
                        .getText()
                        .trim()
                        .toLowerCase();

        for (ServiceRequest request :
                currentRequests) {

            // =============================================
            // TYPE
            // =============================================

            String requestType =
                    controller
                            .getRequestType(
                                    request
                            );

            if (!selectedType.equalsIgnoreCase(
                    "All"
            )
                    &&
                    !selectedType.equalsIgnoreCase(
                            requestType
                    )) {

                continue;
            }

            // =============================================
            // SEARCH
            // =============================================

            if (!search.isEmpty()
                    &&
                    !matchesSearch(
                            request,
                            search
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
            ServiceRequest request,
            String search
    ) {

        if (request == null) {

            return false;
        }

        return contains(
                request.getRequestId(),
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
                        request.getVehicleId(),
                        search
                )
                ||
                contains(
                        request.getServiceType(),
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
                );
    }

    // =====================================================
    // RENDER REQUESTS
    // =====================================================

    private void renderRequests(
            List<ServiceRequest> requests
    ) {

        if (requestsContainer == null) {

            return;
        }

        requestsContainer
                .getChildren()
                .clear();

        if (requests == null
                ||
                requests.isEmpty()) {

            showEmptyMessage(
                    "No assigned service requests found."
            );

            return;
        }

        for (ServiceRequest request :
                requests) {

            requestsContainer
                    .getChildren()
                    .add(
                            createRequestRow(
                                    request
                            )
                    );
        }
    }

    // =====================================================
    // EMPTY MESSAGE
    // =====================================================

    private void showEmptyMessage(
            String message
    ) {

        if (requestsContainer == null) {

            return;
        }

        requestsContainer
                .getChildren()
                .clear();

        VBox emptyBox =
                new VBox(8);

        emptyBox.setAlignment(
                Pos.CENTER
        );

        emptyBox.setPadding(
                new Insets(40)
        );

        Label title =
                new Label(
                        message
                );

        title.setStyle(
                "-fx-font-size: 14px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: "
                        + Theme.TEXT
                        + ";"
        );

        Label info =
                new Label(
                        "Requests assigned by Admin will appear here."
                );

        info.setStyle(
                "-fx-font-size: 11px;"
                        + "-fx-text-fill: "
                        + Theme.SECONDARY_TEXT
                        + ";"
        );

        emptyBox
                .getChildren()
                .addAll(
                        title,
                        info
                );

        requestsContainer
                .getChildren()
                .add(
                        emptyBox
                );
    }

    // =====================================================
    // REQUEST ROW
    // =====================================================

    private HBox createRequestRow(
            ServiceRequest request
    ) {

        HBox row =
                new HBox(12);

        row.setAlignment(
                Pos.CENTER_LEFT
        );

        row.setPadding(
                new Insets(14)
        );

        row.setStyle(
                "-fx-background-color: "
                        + Theme.SURFACE
                        + ";"
                        + "-fx-background-radius: 12;"
        );

        // =================================================
        // TYPE
        // =================================================

        String type =
                controller
                        .getRequestType(
                                request
                        );

        Circle avatarCircle =
                new Circle(29);

        if (type.equalsIgnoreCase(
                "Emergency"
        )) {

            avatarCircle.setFill(
                    Color.web(
                            "#2A1717"
                    )
            );

        } else {

            avatarCircle.setFill(
                    Color.web(
                            "#2B210D"
                    )
            );
        }

        // UI-only: show a user silhouette for the customer representation.
        var customerIcon =
                IconUtil.createUserIcon(
                        0.80,
                        Theme.INFO
                );

        javafx.scene.layout.StackPane avatar =
                new javafx.scene.layout.StackPane(
                        avatarCircle,
                        customerIcon
                );

        // =================================================
        // CUSTOMER
        // =================================================

        VBox customerBox =
                new VBox(5);

        HBox nameRow =
                new HBox(8);

        Label customerLabel =
                new Label(
                        controller
                                .getCustomerName(
                                        request
                                )
                );

        customerLabel.setStyle(
                "-fx-font-size: 13px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: "
                        + Theme.TEXT
                        + ";"
        );

        Label typeLabel =
                new Label(
                        type
                );

        if (type.equalsIgnoreCase(
                "Emergency"
        )) {

            typeLabel.setStyle(
                    "-fx-background-color: "
                            + Theme.ERROR_BG
                            + ";"
                            + "-fx-text-fill: "
                            + Theme.ERROR
                            + ";"
                            + "-fx-background-radius: 10;"
                            + "-fx-padding: 4 8 4 8;"
                            + "-fx-font-size: 8px;"
                            + "-fx-font-weight: bold;"
            );

        } else {

            typeLabel.setStyle(
                    "-fx-background-color: "
                            + Theme.SUCCESS_BG
                            + ";"
                            + "-fx-text-fill: "
                            + Theme.SUCCESS
                            + ";"
                            + "-fx-background-radius: 10;"
                            + "-fx-padding: 4 8 4 8;"
                            + "-fx-font-size: 8px;"
                            + "-fx-font-weight: bold;"
            );
        }

        nameRow
                .getChildren()
                .addAll(
                        customerLabel,
                        typeLabel
                );

        String vehicle =
                controller
                        .getVehicleDisplay(
                                request
                        );

        String problem =
                controller
                        .getProblemDisplay(
                                request
                        );

        Label problemLabel =
                new Label(
                        vehicle
                                + "  •  "
                                + problem
                );

        problemLabel.setStyle(
                "-fx-font-size: 10px;"
                        + "-fx-text-fill: "
                        + Theme.SECONDARY_TEXT
                        + ";"
        );

        // UI-only: use the shared vector car icon for vehicle representation.
        problemLabel.setGraphic(
                IconUtil.createVehicleIcon(
                        0.50,
                        Theme.PRIMARY
                )
        );
        problemLabel.setGraphicTextGap(7);

        customerBox
                .getChildren()
                .addAll(
                        nameRow,
                        problemLabel
                );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        // =================================================
        // LOCATION
        // =================================================

        VBox locationBox =
                new VBox(3);

        locationBox.setAlignment(
                Pos.CENTER_RIGHT
        );

        Label locationLabel =
                new Label(
                        controller
                                .getLocationDisplay(
                                        request
                                )
                );

        locationLabel.setMaxWidth(
                160
        );

        locationLabel.setWrapText(
                true
        );

        locationLabel.setStyle(
                "-fx-font-size: 10px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: "
                        + Theme.TEXT
                        + ";"
        );

        Label dateLabel =
                new Label(
                        getDateDisplay(
                                request
                        )
                );

        dateLabel.setStyle(
                "-fx-font-size: 9px;"
                        + "-fx-text-fill: "
                        + Theme.SECONDARY_TEXT
                        + ";"
        );

        locationBox
                .getChildren()
                .addAll(
                        locationLabel,
                        dateLabel
                );

        // =================================================
        // COST
        // =================================================

        VBox amountBox =
                new VBox(2);

        amountBox.setAlignment(
                Pos.CENTER_RIGHT
        );

        Label amountLabel =
                new Label(
                        controller
                                .getEstimatedCostDisplay(
                                        request
                                )
                );

        amountLabel.setStyle(
                "-fx-font-size: 11px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: "
                        + Theme.TEXT
                        + ";"
        );

        Label estimated =
                new Label(
                        "Estimated"
                );

        estimated.setStyle(
                "-fx-font-size: 8px;"
                        + "-fx-text-fill: "
                        + Theme.SECONDARY_TEXT
                        + ";"
        );

        amountBox
                .getChildren()
                .addAll(
                        amountLabel,
                        estimated
                );

        // =================================================
        // VIEW
        // =================================================

        Button viewButton =
                new Button(
                        "View"
                );

        styleSecondaryButton(
                viewButton
        );

        viewButton.setOnAction(
                e -> showRequestDetails(
                        request
                )
        );

        // =================================================
        // ACCEPT
        // =================================================

        Button acceptButton =
                new Button(
                        "Accept"
                );

        stylePrimaryButton(
                acceptButton
        );

        acceptButton.setOnAction(
                e -> acceptRequest(
                        request
                )
        );

        // =================================================
        // REJECT
        // =================================================

        Button rejectButton =
                new Button(
                        "Reject"
                );

        rejectButton.setPadding(
                new Insets(
                        9,
                        14,
                        9,
                        14
                )
        );

        rejectButton.setStyle(
                "-fx-background-color: "
                        + Theme.ERROR_BG
                        + ";"
                        + "-fx-text-fill: "
                        + Theme.ERROR
                        + ";"
                        + "-fx-background-radius: 8;"
                        + "-fx-font-size: 10px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-cursor: hand;"
        );

        rejectButton.setOnAction(
                e -> rejectRequest(
                        request
                )
        );

        row
                .getChildren()
                .addAll(
                        avatar,
                        customerBox,
                        spacer,
                        locationBox,
                        amountBox,
                        viewButton,
                        acceptButton,
                        rejectButton
                );

        return row;
    }

    // =====================================================
    // ACCEPT REQUEST
    //
    // Firebase:
    // Assigned -> Accepted
    // =====================================================

    private void acceptRequest(
            ServiceRequest request
    ) {

        if (request == null
                ||
                request.getRequestId() == null) {

            return;
        }

        boolean success =
                controller
                        .acceptRequest(
                                request.getRequestId()
                        );

        if (!success) {

            showError(
                    "Unable to Accept",
                    "The request could not be accepted. "
                            + "It may already have been updated."
            );

            loadRequests();

            return;
        }

        // =================================================
        // UPDATE LOCAL OBJECT
        // =================================================

        request.setStatus(
                "Accepted"
        );

        // =================================================
        // TEMPORARY EXISTING ACTIVE JOB UI SUPPORT
        //
        // ActiveJobPage will later be connected directly
        // to ActiveJobDAO / Firebase.
        // =================================================

        if (activeJobPage != null) {

            activeJobPage.setJob(
                    controller.getCustomerName(
                            request
                    ),

                    controller.getVehicleDisplay(
                            request
                    ),

                    controller.getProblemDisplay(
                            request
                    ),

                    controller.getRequestType(
                            request
                    ),

                    controller.getLocationDisplay(
                            request
                    ),

                    controller.getEstimatedCostDisplay(
                            request
                    )
            );
        }

        // =================================================
        // REFRESH
        //
        // Accepted request should disappear from
        // Assigned Requests.
        // =================================================

        loadRequests();

        // =================================================
        // OPEN ACTIVE JOB
        // =================================================

        if (openActiveJob != null) {

            openActiveJob.run();
        }
    }

    // =====================================================
    // REJECT REQUEST
    //
    // Firebase:
    //
    // Assigned
    //      ↓
    // Pending
    //
    // mechanicId cleared
    // Admin can assign another mechanic.
    // =====================================================

    private void rejectRequest(
            ServiceRequest request
    ) {

        if (request == null
                ||
                request.getRequestId() == null) {

            return;
        }

        Alert confirm =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        confirm.setTitle(
                "Reject Request"
        );

        confirm.setHeaderText(
                null
        );

        confirm.setContentText(
                "Reject service request from "
                        + controller.getCustomerName(
                                request
                        )
                        + "?"
        );

        confirm
                .showAndWait()
                .ifPresent(
                        buttonType -> {

                            if (buttonType
                                    == javafx.scene.control.ButtonType.OK) {

                                boolean success =
                                        controller
                                                .rejectRequest(
                                                        request.getRequestId()
                                                );

                                if (success) {

                                    loadRequests();

                                } else {

                                    showError(
                                            "Unable to Reject",
                                            "Request could not be rejected."
                                    );

                                    loadRequests();
                                }
                            }
                        }
                );
    }

    // =====================================================
    // REQUEST DETAILS PAGE
    // =====================================================

    private void showRequestDetails(
            ServiceRequest request
    ) {

        if (request == null
                ||
                pageContainer == null) {

            return;
        }

        pageContainer
                .getChildren()
                .clear();

        VBox detailsPage =
                new VBox(18);

        Label title =
                new Label(
                        "Request Details"
                );

        title.setStyle(
                "-fx-font-size: 28px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: "
                        + Theme.TEXT
                        + ";"
        );

        Label subtitle =
                new Label(
                        "Review the complete service request."
                );

        subtitle.setStyle(
                "-fx-font-size: 13px;"
                        + "-fx-text-fill: "
                        + Theme.SECONDARY_TEXT
                        + ";"
        );

        VBox card =
                new VBox(14);

        card.setPadding(
                new Insets(25)
        );

        card.setMaxWidth(
                750
        );

        card.setStyle(
                "-fx-background-color: "
                        + Theme.CARD
                        + ";"
                        + "-fx-background-radius: 14;"
                        + "-fx-border-color: "
                        + "#FACC15"
                        + ";"
                        + "-fx-border-radius: 14;"
        );

        card
                .getChildren()
                .addAll(
                        createDetailLabel(
                                "REQUEST ID",
                                safe(
                                        request.getRequestId()
                                )
                        ),

                        createDetailLabel(
                                "CUSTOMER",
                                controller
                                        .getCustomerName(
                                                request
                                        )
                        ),

                        createDetailLabel(
                                "CUSTOMER ID",
                                safe(
                                        request.getCustomerId()
                                )
                        ),

                        createDetailLabel(
                                "VEHICLE",
                                controller
                                        .getVehicleDisplay(
                                                request
                                        )
                        ),

                        createDetailLabel(
                                "SERVICE TYPE",
                                safe(
                                        request.getServiceType()
                                )
                        ),

                        createDetailLabel(
                                "PROBLEM / DESCRIPTION",
                                controller
                                        .getProblemDisplay(
                                                request
                                        )
                        ),

                        createDetailLabel(
                                "LOCATION",
                                controller
                                        .getLocationDisplay(
                                                request
                                        )
                        ),

                        createDetailLabel(
                                "REQUEST TYPE",
                                controller
                                        .getRequestType(
                                                request
                                        )
                        ),

                        createDetailLabel(
                                "STATUS",
                                safe(
                                        request.getStatus()
                                )
                        ),

                        createDetailLabel(
                                "REQUESTED",
                                getDateDisplay(
                                        request
                                )
                        ),

                        createDetailLabel(
                                "ESTIMATED COST",
                                controller
                                        .getEstimatedCostDisplay(
                                                request
                                        )
                        )
                );

        // =================================================
        // BUTTONS
        // =================================================

        HBox buttons =
                new HBox(10);

        Button acceptButton =
                new Button(
                        "Accept Request"
                );

        stylePrimaryButton(
                acceptButton
        );

        acceptButton.setOnAction(
                e -> acceptRequest(
                        request
                )
        );

        Button rejectButton =
                new Button(
                        "Reject Request"
                );

        rejectButton.setPadding(
                new Insets(
                        12,
                        25,
                        12,
                        25
                )
        );

        rejectButton.setStyle(
                "-fx-background-color: "
                        + Theme.ERROR_BG
                        + ";"
                        + "-fx-text-fill: "
                        + Theme.ERROR
                        + ";"
                        + "-fx-background-radius: 8;"
                        + "-fx-font-size: 12px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-cursor: hand;"
        );

        rejectButton.setOnAction(
                e -> {

                    rejectRequest(
                            request
                    );

                    buildRequestsPage();
                }
        );

        Button backButton =
                new Button(
                        "Back to Requests"
                );

        styleSecondaryButton(
                backButton
        );

        backButton.setOnAction(
                e -> buildRequestsPage()
        );

        buttons
                .getChildren()
                .addAll(
                        acceptButton,
                        rejectButton,
                        backButton
                );

        card
                .getChildren()
                .add(
                        buttons
                );

        detailsPage
                .getChildren()
                .addAll(
                        title,
                        subtitle,
                        card
                );

        pageContainer
                .getChildren()
                .add(
                        detailsPage
                );
    }

    // =====================================================
    // DETAIL LABEL
    // =====================================================

    private Label createDetailLabel(
            String title,
            String value
    ) {

        Label label =
                new Label(
                        title
                                + "\n"
                                + safe(value)
                );

        label.setWrapText(
                true
        );

        label.setMaxWidth(
                700
        );

        label.setStyle(
                "-fx-font-size: 12px;"
                        + "-fx-text-fill: "
                        + Theme.TEXT
                        + ";"
        );

        if ("VEHICLE".equalsIgnoreCase(title)) {
            // UI-only: use the shared vector car icon for vehicle representation.
            label.setGraphic(
                    IconUtil.createVehicleIcon(
                            0.60,
                            Theme.PRIMARY
                    )
            );
            label.setGraphicTextGap(8);
        }

        return label;
    }

    // =====================================================
    // PRIMARY BUTTON
    // =====================================================

    private void stylePrimaryButton(
            Button button
    ) {

        button.setPadding(
                new Insets(
                        9,
                        16,
                        9,
                        16
                )
        );

        button.setStyle(
                "-fx-background-color: "
                        + Theme.PRIMARY
                        + ";"
                        + "-fx-text-fill: "
                        + Theme.WHITE
                        + ";"
                        + "-fx-background-radius: 8;"
                        + "-fx-font-size: 10px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-cursor: hand;"
        );
    }

    // =====================================================
    // SECONDARY BUTTON
    // =====================================================

    private void styleSecondaryButton(
            Button button
    ) {

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
                        + Theme.CARD
                        + ";"
                        + "-fx-text-fill: "
                        + Theme.TEXT
                        + ";"
                        + "-fx-border-color: "
                        + Theme.BORDER
                        + ";"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
                        + "-fx-font-size: 10px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-cursor: hand;"
        );
    }

    // =====================================================
    // DATE DISPLAY
    // =====================================================

    private String getDateDisplay(
            ServiceRequest request
    ) {

        if (request == null) {

            return "";
        }

        String date =
                clean(
                        request.getRequestDate()
                );

        if (date == null) {

            return "Unknown time";
        }

        try {

            long requestTime =
                    Long.parseLong(
                            date
                    );

            long difference =
                    System.currentTimeMillis()
                            - requestTime;

            if (difference < 0) {

                return date;
            }

            long minutes =
                    difference
                            / (60 * 1000);

            if (minutes < 1) {

                return "Just now";
            }

            if (minutes < 60) {

                return minutes
                        + " min ago";
            }

            long hours =
                    minutes / 60;

            if (hours < 24) {

                return hours
                        + " hr ago";
            }

            long days =
                    hours / 24;

            return days
                    + " day"
                    + (days == 1
                    ? ""
                    : "s")
                    + " ago";

        } catch (Exception e) {

            /*
             * Old Firestore document may contain
             * formatted date string.
             */
            return date;
        }
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

    // =====================================================
    // SEARCH
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
    // SAFE DISPLAY
    // =====================================================

    private String safe(
            String value
    ) {

        String cleaned =
                clean(
                        value
                );

        return cleaned == null
                ? "-"
                : cleaned;
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
}