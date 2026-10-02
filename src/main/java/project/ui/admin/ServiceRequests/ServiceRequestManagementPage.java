package project.ui.admin.ServiceRequests;

import com.google.cloud.firestore.Firestore;

import javafx.application.Platform;
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
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import project.controller.admin.ServiceRequestController;
import project.firebase.FirebaseConfig;
import project.model.ServiceRequest;
import project.util.IconUtil;
import project.ui.admin.DashBoard.AdminSectionPage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ServiceRequestManagementPage
        extends AdminSectionPage {

    // =========================================================
    // COLORS
    // =========================================================

    private static final String BG =
            "#0F0F0F";

    private static final String SURFACE =
            "#1A1A1A";

    private static final String SECONDARY =
            "#242424";

    private static final String CONTROL_SURFACE =
            "#242424";

    /*
     * IMPORTANT:
     * Admin records वर हे dark colors वापरायचे.
     */
    private static final String HEADING =
            "#F3F4F6";

    private static final String RECORD_TEXT =
            "#F3F4F6";

    private static final String TEXT =
            "#A1A1AA";

    private static final String MUTED =
            "#A1A1AA";

    private static final String BORDER =
            "#F59E0B";

    private static final String BLUE =
            "#F59E0B";

    private static final String ORANGE =
            "#F59E0B";

    private static final String GREEN =
            "#22C55E";

    private static final String RED =
            "#EF4444";

    private static final String PURPLE =
            "#F59E0B";

    private static final String CYAN =
            "#F59E0B";

    // =========================================================
    // CONTROLLER
    // =========================================================

    private ServiceRequestController controller;

    // =========================================================
    // DATA
    // =========================================================

    private final List<ServiceRequest> allRequests =
            new ArrayList<>();

    // =========================================================
    // ROOT
    // =========================================================

    private StackPane pageContainer;

    private VBox managementView;

    // =========================================================
    // RECORD LIST
    // =========================================================

    private VBox requestCards;

    private ScrollPane recordsScrollPane;

    private double savedScrollPosition =
            0.0;

    // =========================================================
    // FILTERS
    // =========================================================

    private TextField searchField;

    private ComboBox<String> statusFilter;

    private ComboBox<String> sortFilter;

    private Label resultCountLabel;

    // =========================================================
    // STATISTICS
    // =========================================================

    private Label totalLabel;

    private Label pendingLabel;

    private Label assignedLabel;

    private Label acceptedLabel;

    private Label progressLabel;

    private Label completedLabel;

    private Label cancelledLabel;

    private Label activeLabel;

    // =========================================================
    // GET VIEW
    // =========================================================

    @Override
    public VBox getView() {

        pageContainer =
                new StackPane();

        pageContainer.setStyle(
                "-fx-background-color: "
                        + BG
                        + ";"
        );

        // =====================================================
        // FIREBASE
        // =====================================================

        try {

            Firestore firestore =
                    FirebaseConfig.getFirestore();

            controller =
                    new ServiceRequestController(
                            firestore
                    );

        } catch (Exception e) {

            e.printStackTrace();

            pageContainer
                    .getChildren()
                    .setAll(
                            createErrorView(
                                    "Unable to connect to Firebase."
                            )
                    );

            VBox wrapper =
                    new VBox(
                            pageContainer
                    );

            wrapper.setStyle(
                    "-fx-background-color: "
                            + BG
                            + ";"
            );

            return wrapper;
        }

        // =====================================================
        // MANAGEMENT PAGE
        // =====================================================

        managementView =
                createManagementView();

        pageContainer
                .getChildren()
                .setAll(
                        managementView
                );

        loadRequests();

        VBox wrapper =
                new VBox(
                        pageContainer
                );

        wrapper.setFillWidth(
                true
        );

        wrapper.setStyle(
                "-fx-background-color: "
                        + BG
                        + ";"
        );

        VBox.setVgrow(
                pageContainer,
                Priority.ALWAYS
        );

        return wrapper;
    }

    // =========================================================
    // MANAGEMENT VIEW
    // =========================================================

    private VBox createManagementView() {

        VBox root =
                new VBox(22);

        root.setPadding(
                new Insets(
                        30,
                        32,
                        34,
                        32
                )
        );

        root.setStyle(
                "-fx-background-color: "
                        + BG
                        + ";"
        );

        root.getChildren()
                .addAll(
                        createHeader(),
                        createStatistics(),
                        createToolbar(),
                        createRecordsCard()
                );

        return root;
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

        VBox text =
                new VBox(5);

        // =====================================================
        // TITLE
        // =====================================================

        Label title =
                new Label(
                        "Service Request Management"
                );

        /*
         * INLINE STYLE:
         * global admin CSS white करू शकणार नाही.
         */
        title.setStyle(
                "-fx-text-fill: "
                        + HEADING
                        + ";"
                        + "-fx-font-size: 29px;"
                        + "-fx-font-weight: bold;"
        );

        Label subtitle =
                new Label(
                        "Monitor complete Customer → Admin → Mechanic service lifecycle."
                );

        subtitle.setStyle(
                "-fx-text-fill: "
                        + TEXT
                        + ";"
                        + "-fx-font-size: 13px;"
        );

        text.getChildren()
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

        Label firebase =
                new Label(
                        "● SYSTEM LIVE"
                );

        firebase.setStyle(
                "-fx-text-fill: "
                        + GREEN
                        + ";"
                        + "-fx-font-size: 10px;"
                        + "-fx-font-weight: bold;"
        );

        header.getChildren()
                .addAll(
                        text,
                        spacer,
                        firebase
                );

        return header;
    }

    // =========================================================
    // STATISTICS
    // =========================================================

    private GridPane createStatistics() {

        GridPane grid =
                new GridPane();

        grid.setHgap(
                12
        );

        grid.setVgap(
                12
        );

        for (int i = 0;
             i < 4;
             i++) {

            ColumnConstraints column =
                    new ColumnConstraints();

            column.setPercentWidth(
                    25
            );

            column.setHgrow(
                    Priority.ALWAYS
            );

            grid.getColumnConstraints()
                    .add(
                            column
                    );
        }

        totalLabel =
                createValueLabel(
                        BLUE
                );

        pendingLabel =
                createValueLabel(
                        ORANGE
                );

        assignedLabel =
                createValueLabel(
                        BLUE
                );

        acceptedLabel =
                createValueLabel(
                        PURPLE
                );

        progressLabel =
                createValueLabel(
                        CYAN
                );

        completedLabel =
                createValueLabel(
                        GREEN
                );

        cancelledLabel =
                createValueLabel(
                        RED
                );

        activeLabel =
                createValueLabel(
                        BLUE
                );

        grid.add(
                createStatCard(
                        "Total Requests",
                        totalLabel,
                        "All Firebase requests"
                ),
                0,
                0
        );

        grid.add(
                createStatCard(
                        "Pending",
                        pendingLabel,
                        "Waiting for assignment"
                ),
                1,
                0
        );

        grid.add(
                createStatCard(
                        "Assigned",
                        assignedLabel,
                        "Mechanic assigned"
                ),
                2,
                0
        );

        grid.add(
                createStatCard(
                        "Accepted",
                        acceptedLabel,
                        "Accepted by mechanic"
                ),
                3,
                0
        );

        grid.add(
                createStatCard(
                        "In Progress",
                        progressLabel,
                        "Repair underway"
                ),
                0,
                1
        );

        grid.add(
                createStatCard(
                        "Completed",
                        completedLabel,
                        "Successfully finished"
                ),
                1,
                1
        );

        grid.add(
                createStatCard(
                        "Cancelled",
                        cancelledLabel,
                        "Cancelled requests"
                ),
                2,
                1
        );

        grid.add(
                createStatCard(
                        "Active",
                        activeLabel,
                        "Pending to In Progress"
                ),
                3,
                1
        );

        return grid;
    }

    // =========================================================
    // VALUE LABEL
    // =========================================================

    private Label createValueLabel(
            String color
    ) {

        Label label =
                new Label(
                        "0"
                );

        /*
         * Important:
         * inline CSS force.
         */
        label.setStyle(
                "-fx-text-fill: "
                        + color
                        + ";"
                        + "-fx-font-size: 25px;"
                        + "-fx-font-weight: bold;"
        );

        return label;
    }

    // =========================================================
    // STAT CARD
    // =========================================================

    private VBox createStatCard(
            String title,
            Label value,
            String subtitle
    ) {

        VBox card =
                new VBox(6);

        card.setPadding(
                new Insets(
                        16
                )
        );

        card.setMaxWidth(
                Double.MAX_VALUE
        );

        card.setMinHeight(
                105
        );

        card.setStyle(
                "-fx-background-color: "
                        + SURFACE
                        + ";"
                        + "-fx-border-color: "
                        + BORDER
                        + ";"
                        + "-fx-border-width: 1;"
                        + "-fx-border-radius: 12;"
                        + "-fx-background-radius: 12;"
        );

        Label titleLabel =
                new Label(
                        title
                );

        /*
         * Before screenshot मध्ये white दिसत होतं.
         * आता force dark.
         */
        titleLabel.setStyle(
                "-fx-text-fill: #A1A1AA;"
                        + "-fx-font-size: 12px;"
                        + "-fx-font-weight: bold;"
        );

        Label subtitleLabel =
                new Label(
                        subtitle
                );

        subtitleLabel.setStyle(
                "-fx-text-fill: "
                        + MUTED
                        + ";"
                        + "-fx-font-size: 9px;"
        );

        card.getChildren()
                .addAll(
                        titleLabel,
                        value,
                        subtitleLabel
                );

        return card;
    }

    // =========================================================
    // TOOLBAR
    // =========================================================

    private HBox createToolbar() {

        HBox toolbar =
                new HBox(12);

        toolbar.setAlignment(
                Pos.CENTER_LEFT
        );

        // =====================================================
        // SEARCH
        // =====================================================

        searchField =
                new TextField();

        searchField.setPromptText(
                "Search request, customer, vehicle, mechanic, service..."
        );

        searchField.setPrefWidth(
                390
        );

        searchField.setPrefHeight(
                44
        );

        styleTextField(
                searchField
        );

        // =====================================================
        // STATUS
        // =====================================================

        statusFilter =
                new ComboBox<>();

        statusFilter
                .getItems()
                .addAll(
                        "All",
                        "Pending",
                        "Assigned",
                        "Accepted",
                        "In Progress",
                        "Completed",
                        "Cancelled"
                );

        statusFilter.setValue(
                "All"
        );

        statusFilter.setPrefWidth(
                155
        );

        statusFilter.setPrefHeight(
                44
        );

        styleComboBox(
                statusFilter
        );

        // =====================================================
        // SORT
        // =====================================================

        sortFilter =
                new ComboBox<>();

        sortFilter
                .getItems()
                .addAll(
                        "Newest First",
                        "Oldest First",
                        "Customer A-Z",
                        "Customer Z-A",
                        "Status A-Z"
                );

        sortFilter.setValue(
                "Newest First"
        );

        sortFilter.setPrefWidth(
                150
        );

        sortFilter.setPrefHeight(
                44
        );

        styleComboBox(
                sortFilter
        );

        // =====================================================
        // REFRESH
        // =====================================================

        Button refresh =
                createButton(
                        "Refresh",
                        BLUE
                );

        refresh.setPrefHeight(
                44
        );

        refresh.setOnAction(
                event ->
                        refreshRequestsPreservingScroll()
        );

        // =====================================================
        // LISTENERS
        // =====================================================

        searchField
                .textProperty()
                .addListener(
                        (
                                observable,
                                oldValue,
                                newValue
                        ) ->
                                applyFilters()
                );

        statusFilter
                .valueProperty()
                .addListener(
                        (
                                observable,
                                oldValue,
                                newValue
                        ) ->
                                applyFilters()
                );

        sortFilter
                .valueProperty()
                .addListener(
                        (
                                observable,
                                oldValue,
                                newValue
                        ) ->
                                applyFilters()
                );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        toolbar.getChildren()
                .addAll(
                        searchField,
                        statusFilter,
                        sortFilter,
                        refresh,
                        spacer
                );

        return toolbar;
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

        button.setPadding(
                new Insets(
                        0,
                        18,
                        0,
                        18
                )
        );

        button.setStyle(
                "-fx-background-color: "
                        + color
                        + ";"
                        + "-fx-text-fill: white;"
                        + "-fx-background-radius: 8;"
                        + "-fx-font-size: 11px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-cursor: hand;"
        );

        return button;
    }

    // =========================================================
    // RECORD CARD HOLDER
    // =========================================================

    private VBox createRecordsCard() {

        VBox card =
                new VBox(15);

        card.setPadding(
                new Insets(
                        20
                )
        );

        card.setStyle(
                "-fx-background-color: "
                        + SURFACE
                        + ";"
                        + "-fx-border-color: "
                        + BORDER
                        + ";"
                        + "-fx-border-width: 1;"
                        + "-fx-border-radius: 14;"
                        + "-fx-background-radius: 14;"
        );

        // =====================================================
        // HEADER
        // =====================================================

        HBox header =
                new HBox(12);

        header.setAlignment(
                Pos.CENTER_LEFT
        );

        VBox headingBox =
                new VBox(4);

        Label heading =
                new Label(
                        "Service Request Information"
                );

        heading.setStyle(
                "-fx-text-fill: "
                        + HEADING
                        + ";"
                        + "-fx-font-size: 19px;"
                        + "-fx-font-weight: bold;"
        );

        Label subtitle =
                new Label(
                        "Actual records stored in serviceRequests"
                );

        subtitle.setStyle(
                "-fx-text-fill: "
                        + TEXT
                        + ";"
                        + "-fx-font-size: 11px;"
        );

        resultCountLabel =
                new Label(
                        "0 requests"
                );

        resultCountLabel.setStyle(
                "-fx-text-fill: "
                        + BLUE
                        + ";"
                        + "-fx-font-size: 11px;"
                        + "-fx-font-weight: bold;"
        );

        headingBox.getChildren()
                .addAll(
                        heading,
                        subtitle,
                        resultCountLabel
                );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Label live =
                new Label(
                        "● Live Data"
                );

        live.setStyle(
                "-fx-text-fill: "
                        + GREEN
                        + ";"
                        + "-fx-font-size: 10px;"
                        + "-fx-font-weight: bold;"
        );

        header.getChildren()
                .addAll(
                        headingBox,
                        spacer,
                        live
                );

        // =====================================================
        // REQUEST CARDS
        // =====================================================

        requestCards =
                new VBox(12);

        requestCards.setFillWidth(
                true
        );

        requestCards.setPadding(
                new Insets(
                        3,
                        3,
                        10,
                        3
                )
        );

        recordsScrollPane =
                new ScrollPane(
                        requestCards
                );

        recordsScrollPane.setFitToWidth(
                true
        );

        recordsScrollPane.setPannable(
                true
        );

        recordsScrollPane.setPrefHeight(
                560
        );

        recordsScrollPane.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        recordsScrollPane.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED
        );

        recordsScrollPane.setStyle(
                "-fx-background-color: transparent;"
                        + "-fx-background: transparent;"
                        + "-fx-border-color: transparent;"
        );

        card.getChildren()
                .addAll(
                        header,
                        new Separator(),
                        recordsScrollPane
                );

        return card;
    }

    // =========================================================
    // REQUEST CARD
    // =========================================================

    private VBox createRequestCard(
            ServiceRequest request,
            int number
    ) {

        VBox card =
                new VBox(13);

        card.setPadding(
                new Insets(
                        17,
                        19,
                        17,
                        19
                )
        );

        card.setCursor(
                Cursor.HAND
        );

        String normalStyle =
                "-fx-background-color: #242424;"
                        + "-fx-border-color: "
                        + BORDER
                        + ";"
                        + "-fx-border-width: 1;"
                        + "-fx-border-radius: 12;"
                        + "-fx-background-radius: 12;";

        String hoverStyle =
                "-fx-background-color: #1A1A1A;"
                        + "-fx-border-color: "
                        + BLUE
                        + ";"
                        + "-fx-border-width: 1.5;"
                        + "-fx-border-radius: 12;"
                        + "-fx-background-radius: 12;"
                        + "-fx-effect: dropshadow("
                        + "gaussian,"
                        + "rgba(37,99,235,0.15),"
                        + "10,0.12,0,2"
                        + ");";

        card.setStyle(
                normalStyle
        );

        // =====================================================
        // TOP SECTION
        // =====================================================

        HBox top =
                new HBox(12);

        top.setAlignment(
                Pos.CENTER_LEFT
        );

        StackPane iconBox =
                new StackPane();

        iconBox.setPrefSize(
                42,
                42
        );

        iconBox.setMinSize(
                42,
                42
        );

        iconBox.setMaxSize(
                42,
                42
        );

        iconBox.setStyle(
                "-fx-background-color: "
                        + BLUE
                        + ";"
                        + "-fx-background-radius: 50%;"
        );

        Label icon =
                new Label(
                        "▤"
                );

        icon.setStyle(
                "-fx-text-fill: white;"
                        + "-fx-font-size: 15px;"
                        + "-fx-font-weight: bold;"
        );

        iconBox.getChildren()
                .add(
                        icon
                );

        // =====================================================
        // IDENTITY
        // =====================================================

        VBox identity =
                new VBox(3);

        HBox idRow =
                new HBox(8);

        idRow.setAlignment(
                Pos.CENTER_LEFT
        );

        Label serial =
                new Label(
                        "#"
                                + number
                );

        serial.setStyle(
                "-fx-text-fill: "
                        + BLUE
                        + ";"
                        + "-fx-font-size: 10px;"
                        + "-fx-font-weight: bold;"
        );

        Label requestId =
                new Label(
                        firstNonBlank(
                                request.getRequestId(),
                                "-"
                        )
                );

        /*
         * MAIN RECORD ID = DARK BLACK
         */
        requestId.setStyle(
                "-fx-text-fill: "
                        + RECORD_TEXT
                        + ";"
                        + "-fx-font-size: 16px;"
                        + "-fx-font-weight: bold;"
        );

        idRow.getChildren()
                .addAll(
                        serial,
                        requestId
                );

        Label customer =
                new Label(
                        "Customer: "
                                + firstNonBlank(
                                request.getCustomerName(),
                                request.getCustomerId(),
                                "Unknown Customer"
                        )
                );

        /*
         * CUSTOMER clearly visible.
         */
        customer.setStyle(
                "-fx-text-fill: #A1A1AA;"
                        + "-fx-font-size: 11px;"
                        + "-fx-font-weight: bold;"
        );

        // UI-only: show a user silhouette for the customer representation.
        customer.setGraphic(
                IconUtil.createUserIcon(
                        0.65,
                        TEXT
                )
        );
        customer.setGraphicTextGap(8);

        identity.getChildren()
                .addAll(
                        idRow,
                        customer
                );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        String requestStatus =
                normalizeStatus(
                        request.getStatus()
                );

        Label status =
                createStatusBadge(
                        requestStatus
                );

        top.getChildren()
                .addAll(
                        iconBox,
                        identity,
                        spacer,
                        status
                );

        // =====================================================
        // INFORMATION ROW 1
        // =====================================================

        HBox firstRow =
                new HBox(10);

        VBox vehicle =
                createInfoBox(
                        "VEHICLE",
                        firstNonBlank(
                                request.getVehicleNumber(),
                                request.getVehicleId(),
                                "Not provided"
                        )
                );

        VBox service =
                createInfoBox(
                        "SERVICE",
                        firstNonBlank(
                                request.getServiceType(),
                                "Service Request"
                        )
                );

        VBox mechanic =
                createInfoBox(
                        "MECHANIC",
                        getMechanicDisplay(
                                request
                        )
                );

        VBox location =
                createInfoBox(
                        "LOCATION",
                        firstNonBlank(
                                request.getLocation(),
                                "Not provided"
                        )
                );

        HBox.setHgrow(
                vehicle,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                service,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                mechanic,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                location,
                Priority.ALWAYS
        );

        firstRow.getChildren()
                .addAll(
                        vehicle,
                        service,
                        mechanic,
                        location
                );

        // =====================================================
        // INFORMATION ROW 2
        // =====================================================

        HBox secondRow =
                new HBox(10);

        VBox requested =
                createInfoBox(
                        "REQUESTED",
                        firstNonBlank(
                                request.getRequestDate(),
                                "-"
                        )
                );

        VBox assigned =
                createInfoBox(
                        "ASSIGNED",
                        firstNonBlank(
                                request.getAssignedDate(),
                                "-"
                        )
                );

        VBox updated =
                createInfoBox(
                        "LAST UPDATED",
                        firstNonBlank(
                                request.getUpdatedAt(),
                                "-"
                        )
                );

        HBox.setHgrow(
                requested,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                assigned,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                updated,
                Priority.ALWAYS
        );

        secondRow.getChildren()
                .addAll(
                        requested,
                        assigned,
                        updated
                );

        // =====================================================
        // DESCRIPTION + DETAILS BUTTON
        // =====================================================

        HBox bottom =
                new HBox(12);

        bottom.setAlignment(
                Pos.CENTER_LEFT
        );

        Label description =
                new Label(
                        shorten(
                                request.getDescription(),
                                110
                        )
                );

        description.setWrapText(
                true
        );

        /*
         * DESCRIPTION dark.
         */
        description.setStyle(
                "-fx-text-fill: #F3F4F6;"
                        + "-fx-font-size: 11px;"
        );

        Region bottomSpacer =
                new Region();

        HBox.setHgrow(
                bottomSpacer,
                Priority.ALWAYS
        );

        Button viewDetails =
                createSmallButton(
                        "View Details →",
                        BLUE
                );

        viewDetails.setOnAction(
                event -> {

                    event.consume();

                    showDetails(
                            request
                    );
                }
        );

        bottom.getChildren()
                .addAll(
                        description,
                        bottomSpacer,
                        viewDetails
                );

        // =====================================================
        // ADD TO CARD
        // =====================================================

        card.getChildren()
                .addAll(
                        top,
                        new Separator(),
                        firstRow,
                        secondRow,
                        bottom
                );

        // =====================================================
        // HOVER
        // =====================================================

        card.setOnMouseEntered(
                event -> {

                    card.setStyle(
                            hoverStyle
                    );

                    card.setTranslateY(
                            -1
                    );
                }
        );

        card.setOnMouseExited(
                event -> {

                    card.setStyle(
                            normalStyle
                    );

                    card.setTranslateY(
                            0
                    );
                }
        );

        // =====================================================
        // CLICK CARD
        // =====================================================

        card.setOnMouseClicked(
                event -> {

                    savedScrollPosition =
                            getCurrentScrollPosition();

                    showDetails(
                            request
                    );
                }
        );

        return card;
    }

    // =========================================================
    // INFO BOX
    //
    // IMPORTANT UI FIX
    // =========================================================

    private VBox createInfoBox(
            String title,
            String value
    ) {

        VBox box =
                new VBox(5);

        box.setPadding(
                new Insets(
                        11
                )
        );

        box.setMinWidth(
                125
        );

        box.setMaxWidth(
                Double.MAX_VALUE
        );

        /* Dark information surface consistent with the dashboard. */
        box.setStyle(
                "-fx-background-color: "
                        + CONTROL_SURFACE
                        + ";"
                        + "-fx-border-color: "
                        + BORDER
                        + ";"
                        + "-fx-border-width: 1;"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
        );

        // =====================================================
        // FIELD NAME
        // =====================================================

        Label titleLabel =
                new Label(
                        title
                );

        titleLabel.setStyle(
                "-fx-text-fill: "
                        + TEXT
                        + ";"
                        + "-fx-font-size: 9px;"
                        + "-fx-font-weight: bold;"
        );

        // =====================================================
        // ACTUAL RECORD VALUE
        // =====================================================

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

        /*
         * THIS IS THE MAIN FIX:
         *
         * actual Firebase value almost BLACK.
         */
        valueLabel.setStyle(
                "-fx-text-fill: "
                        + RECORD_TEXT
                        + ";"
                        + "-fx-font-size: 12px;"
                        + "-fx-font-weight: bold;"
        );

        // UI-only: use semantic vector icons for vehicle and mechanic representations.
        if ("VEHICLE".equalsIgnoreCase(title)) {
            valueLabel.setGraphic(
                    IconUtil.createVehicleIcon(
                            0.55,
                            BLUE
                    )
            );
            valueLabel.setGraphicTextGap(7);
        } else if ("MECHANIC".equalsIgnoreCase(title)) {
            valueLabel.setGraphic(
                    IconUtil.createMechanicIcon(
                            0.55,
                            GREEN
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

    // =========================================================
    // STATUS BADGE
    // =========================================================

    private Label createStatusBadge(
            String status
    ) {

        String normalized =
                normalizeStatus(
                        status
                );

        String color =
                getStatusColor(
                        normalized
                );

        Label badge =
                new Label(
                        normalized
                );

        /*
         * Status intentionally colored.
         */
        badge.setStyle(
                "-fx-text-fill: "
                        + color
                        + ";"
                        + "-fx-background-color: "
                        + color
                        + "18;"
                        + "-fx-border-color: "
                        + color
                        + "55;"
                        + "-fx-border-width: 1;"
                        + "-fx-border-radius: 15;"
                        + "-fx-background-radius: 15;"
                        + "-fx-font-size: 9px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-padding: 6 11 6 11;"
        );

        return badge;
    }

    // =========================================================
    // STATUS COLOR
    // =========================================================

    private String getStatusColor(
            String status
    ) {

        if (status == null) {

            return TEXT;
        }

        if (status.equalsIgnoreCase(
                "Pending"
        )) {

            return ORANGE;
        }

        if (status.equalsIgnoreCase(
                "Assigned"
        )) {

            return BLUE;
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
                "Completed"
        )) {

            return GREEN;
        }

        if (status.equalsIgnoreCase(
                "Cancelled"
        )) {

            return RED;
        }

        return TEXT;
    }

    // =========================================================
    // SMALL BUTTON
    // =========================================================

    private Button createSmallButton(
            String text,
            String color
    ) {

        Button button =
                new Button(
                        text
                );

        button.setStyle(
                "-fx-background-color: "
                        + color
                        + ";"
                        + "-fx-text-fill: white;"
                        + "-fx-background-radius: 7;"
                        + "-fx-font-size: 9px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-padding: 8 13 8 13;"
                        + "-fx-cursor: hand;"
        );

        return button;
    }

    // =========================================================
    // LOAD REQUESTS
    // =========================================================

    private void loadRequests() {

        try {

            List<ServiceRequest> requests =
                    controller.getAllRequests();

            allRequests.clear();

            if (requests != null) {

                for (ServiceRequest request :
                        requests) {

                    if (request != null) {

                        allRequests.add(
                                request
                        );
                    }
                }
            }

            /*
             * NO FAKE RECORDS.
             *
             * Firebase empty असेल तर
             * empty stateच दिसेल.
             */

            updateStatistics();

            applyFilters();

        } catch (Exception e) {

            e.printStackTrace();

            allRequests.clear();

            updateStatistics();

            renderRequests(
                    new ArrayList<>()
            );

            showError(
                    "Unable to Load Requests",
                    firstNonBlank(
                            e.getMessage(),
                            "Firebase request loading failed."
                    )
            );
        }
    }

    // =========================================================
    // REFRESH WITHOUT SCROLL JUMP
    // =========================================================

    private void refreshRequestsPreservingScroll() {

        double position =
                getCurrentScrollPosition();

        loadRequests();

        /*
         * Content layout complete झाल्यावर
         * old scroll position restore.
         */
        Platform.runLater(
                () -> {

                    if (recordsScrollPane != null) {

                        recordsScrollPane.setVvalue(
                                position
                        );
                    }
                }
        );
    }

    // =========================================================
    // CURRENT SCROLL
    // =========================================================

    private double getCurrentScrollPosition() {

        if (recordsScrollPane == null) {

            return 0.0;
        }

        return recordsScrollPane
                .getVvalue();
    }

    // =========================================================
    // FILTER
    // =========================================================

    private void applyFilters() {

        if (requestCards == null) {

            return;
        }

        String search =
                searchField == null
                        ? ""
                        : clean(
                        searchField.getText()
                );

        String selectedStatus =
                statusFilter == null
                        ? "All"
                        : statusFilter.getValue();

        String sort =
                sortFilter == null
                        ? "Newest First"
                        : sortFilter.getValue();

        List<ServiceRequest> filtered =
                new ArrayList<>();

        for (ServiceRequest request :
                allRequests) {

            if (request == null) {

                continue;
            }

            // =================================================
            // SEARCH
            // =================================================

            if (!matchesSearch(
                    request,
                    search
            )) {

                continue;
            }

            // =================================================
            // STATUS
            // =================================================

            if (
                    selectedStatus != null
                            &&
                    !selectedStatus.equalsIgnoreCase(
                            "All"
                    )
                            &&
                    !normalizeStatus(
                            request.getStatus()
                    ).equalsIgnoreCase(
                            normalizeStatus(
                                    selectedStatus
                            )
                    )
            ) {

                continue;
            }

            filtered.add(
                    request
            );
        }

        sortRequests(
                filtered,
                sort
        );

        renderRequests(
                filtered
        );
    }

    // =========================================================
    // SEARCH MATCH
    // =========================================================

    private boolean matchesSearch(
            ServiceRequest request,
            String search
    ) {

        if (search == null
                ||
                search.isBlank()) {

            return true;
        }

        String query =
                search.toLowerCase();

        return contains(
                request.getRequestId(),
                query
        )
                ||
                contains(
                        request.getCustomerId(),
                        query
                )
                ||
                contains(
                        request.getCustomerName(),
                        query
                )
                ||
                contains(
                        request.getVehicleId(),
                        query
                )
                ||
                contains(
                        request.getVehicleNumber(),
                        query
                )
                ||
                contains(
                        request.getMechanicId(),
                        query
                )
                ||
                contains(
                        request.getMechanicName(),
                        query
                )
                ||
                contains(
                        request.getServiceType(),
                        query
                )
                ||
                contains(
                        request.getLocation(),
                        query
                )
                ||
                contains(
                        request.getDescription(),
                        query
                )
                ||
                contains(
                        request.getStatus(),
                        query
                );
    }

    // =========================================================
    // SORT
    // =========================================================

    private void sortRequests(
            List<ServiceRequest> requests,
            String sort
    ) {

        if (requests == null) {

            return;
        }

        // =====================================================
        // CUSTOMER A-Z
        // =====================================================

        if ("Customer A-Z".equals(
                sort
        )) {

            requests.sort(
                    Comparator.comparing(
                            request ->
                                    getCustomerDisplay(
                                            request
                                    ).toLowerCase()
                    )
            );

            return;
        }

        // =====================================================
        // CUSTOMER Z-A
        // =====================================================

        if ("Customer Z-A".equals(
                sort
        )) {

            requests.sort(
                    Comparator.comparing(
                            (ServiceRequest request) ->
                                    getCustomerDisplay(
                                            request
                                    ).toLowerCase()
                    ).reversed()
            );

            return;
        }

        // =====================================================
        // STATUS A-Z
        // =====================================================

        if ("Status A-Z".equals(
                sort
        )) {

            requests.sort(
                    Comparator.comparing(
                            request ->
                                    normalizeStatus(
                                            request.getStatus()
                                    ).toLowerCase()
                    )
            );

            return;
        }

        // =====================================================
        // OLDEST
        // =====================================================

        if ("Oldest First".equals(
                sort
        )) {

            requests.sort(
                    Comparator.comparingLong(
                            this::getRequestTime
                    )
            );

            return;
        }

        // =====================================================
        // NEWEST
        // =====================================================

        requests.sort(
                Comparator
                        .comparingLong(
                                this::getRequestTime
                        )
                        .reversed()
        );
    }

    // =========================================================
    // RENDER
    // =========================================================

    private void renderRequests(
            List<ServiceRequest> requests
    ) {

        if (requestCards == null) {

            return;
        }

        requestCards
                .getChildren()
                .clear();

        if (requests == null
                ||
                requests.isEmpty()) {

            requestCards
                    .getChildren()
                    .add(
                            createEmptyState()
                    );

            if (resultCountLabel != null) {

                resultCountLabel.setText(
                        "0 requests found"
                );
            }

            return;
        }

        int number =
                1;

        for (ServiceRequest request :
                requests) {

            requestCards
                    .getChildren()
                    .add(
                            createRequestCard(
                                    request,
                                    number++
                            )
                    );
        }

        if (resultCountLabel != null) {

            resultCountLabel.setText(
                    requests.size() == 1
                            ? "1 request"
                            : requests.size()
                            + " requests"
            );
        }
    }

    // =========================================================
    // UPDATE STATISTICS
    // =========================================================

    private void updateStatistics() {

        int pending =
                0;

        int assigned =
                0;

        int accepted =
                0;

        int progress =
                0;

        int completed =
                0;

        int cancelled =
                0;

        for (ServiceRequest request :
                allRequests) {

            if (request == null) {

                continue;
            }

            String status =
                    normalizeStatus(
                            request.getStatus()
                    );

            switch (status) {

                case "Pending":

                    pending++;

                    break;

                case "Assigned":

                    assigned++;

                    break;

                case "Accepted":

                    accepted++;

                    break;

                case "In Progress":

                    progress++;

                    break;

                case "Completed":

                    completed++;

                    break;

                case "Cancelled":

                    cancelled++;

                    break;

                default:

                    break;
            }
        }

        int active =
                pending
                        +
                assigned
                        +
                accepted
                        +
                progress;

        setNumber(
                totalLabel,
                allRequests.size()
        );

        setNumber(
                pendingLabel,
                pending
        );

        setNumber(
                assignedLabel,
                assigned
        );

        setNumber(
                acceptedLabel,
                accepted
        );

        setNumber(
                progressLabel,
                progress
        );

        setNumber(
                completedLabel,
                completed
        );

        setNumber(
                cancelledLabel,
                cancelled
        );

        setNumber(
                activeLabel,
                active
        );
    }

    // =========================================================
    // SHOW DETAILS
    // =========================================================

    private void showDetails(
            ServiceRequest request
    ) {

        if (request == null) {

            return;
        }

        savedScrollPosition =
                getCurrentScrollPosition();

        ServiceRequest fresh =
                getFreshRequest(
                        request
                );

        ServiceRequestDetailsPage page =
                new ServiceRequestDetailsPage(
                        fresh,
                        this::showManagementPage,
                        () ->
                                showAssignMechanicPage(
                                        fresh
                                ),
                        () ->
                                showUpdateStatus(
                                        fresh
                                )
                );

        VBox root =
                new VBox();

        root.setFillWidth(
                true
        );

        root.setStyle(
                "-fx-background-color: "
                        + BG
                        + ";"
        );

        Button backButton =
                new Button(
                        "← Back to Service Requests"
                );

        styleBackButton(
                backButton
        );

        backButton.setOnAction(
                event ->
                        showManagementPage()
        );

        HBox topBar =
                new HBox(
                        backButton
                );

        topBar.setPadding(
                new Insets(
                        18,
                        28,
                        0,
                        28
                )
        );

        VBox detailView =
                page.getView();

        VBox.setVgrow(
                detailView,
                Priority.ALWAYS
        );

        root.getChildren()
                .addAll(
                        topBar,
                        detailView
                );

        pageContainer
                .getChildren()
                .setAll(
                        root
                );
    }

    // =========================================================
    // GET FRESH REQUEST
    // =========================================================

    private ServiceRequest getFreshRequest(
            ServiceRequest request
    ) {

        if (request == null) {

            return null;
        }

        String requestId =
                clean(
                        request.getRequestId()
                );

        if (requestId == null
                ||
                controller == null) {

            return request;
        }

        try {

            ServiceRequest fresh =
                    controller.getRequestById(
                            requestId
                    );

            return fresh == null
                    ? request
                    : fresh;

        } catch (Exception e) {

            return request;
        }
    }

    // =========================================================
    // SHOW MANAGEMENT PAGE
    // =========================================================

    private void showManagementPage() {

        pageContainer
                .getChildren()
                .setAll(
                        managementView
                );

        loadRequests();

        Platform.runLater(
                () -> {

                    if (recordsScrollPane != null) {

                        recordsScrollPane.setVvalue(
                                savedScrollPosition
                        );
                    }
                }
        );
    }

    // =========================================================
    // ASSIGN MECHANIC PAGE
    // =========================================================

    private void showAssignMechanicPage(
            ServiceRequest request
    ) {

        ServiceRequest fresh =
                getFreshRequest(
                        request
                );

        AssignMechanicPage page =
                new AssignMechanicPage(
                        fresh,
                        () ->
                                showDetails(
                                        getFreshRequest(
                                                fresh
                                        )
                                )
                );

        VBox root =
                new VBox();

        root.setStyle(
                "-fx-background-color: "
                        + BG
                        + ";"
        );

        Button back =
                new Button(
                        "← Back to Request Details"
                );

        styleBackButton(
                back
        );

        back.setOnAction(
                event ->
                        showDetails(
                                getFreshRequest(
                                        fresh
                                )
                        )
        );

        HBox top =
                new HBox(
                        back
                );

        top.setPadding(
                new Insets(
                        18,
                        28,
                        0,
                        28
                )
        );

        VBox view =
                page.getView();

        VBox.setVgrow(
                view,
                Priority.ALWAYS
        );

        root.getChildren()
                .addAll(
                        top,
                        view
                );

        pageContainer
                .getChildren()
                .setAll(
                        root
                );
    }

    // =========================================================
    // PUBLIC ASSIGN METHOD
    // =========================================================

    public void showAssignMechanic(
            ServiceRequest request
    ) {

        showAssignMechanicPage(
                request
        );
    }

    // =========================================================
    // UPDATE STATUS PAGE
    // =========================================================

    public void showUpdateStatus(
            ServiceRequest request
    ) {

        ServiceRequest fresh =
                getFreshRequest(
                        request
                );

        UpdateRequestStatusPage page =
                new UpdateRequestStatusPage(
                        fresh,
                        () ->
                                showDetails(
                                        getFreshRequest(
                                                fresh
                                        )
                                )
                );

        VBox root =
                new VBox();

        root.setStyle(
                "-fx-background-color: "
                        + BG
                        + ";"
        );

        Button back =
                new Button(
                        "← Back to Request Details"
                );

        styleBackButton(
                back
        );

        back.setOnAction(
                event ->
                        showDetails(
                                getFreshRequest(
                                        fresh
                                )
                        )
        );

        HBox top =
                new HBox(
                        back
                );

        top.setPadding(
                new Insets(
                        18,
                        28,
                        0,
                        28
                )
        );

        VBox view =
                page.getView();

        VBox.setVgrow(
                view,
                Priority.ALWAYS
        );

        root.getChildren()
                .addAll(
                        top,
                        view
                );

        pageContainer
                .getChildren()
                .setAll(
                        root
                );
    }

    // =========================================================
    // EMPTY STATE
    // =========================================================

    private VBox createEmptyState() {

        VBox box =
                new VBox(8);

        box.setAlignment(
                Pos.CENTER
        );

        box.setPadding(
                new Insets(
                        45
                )
        );

        Label title =
                new Label(
                        "No Service Requests"
                );

        title.setStyle(
                "-fx-text-fill: "
                        + HEADING
                        + ";"
                        + "-fx-font-size: 15px;"
                        + "-fx-font-weight: bold;"
        );

        Label subtitle =
                new Label(
                        "Customer service requests will appear here when they are created."
                );

        subtitle.setWrapText(
                true
        );

        subtitle.setStyle(
                "-fx-text-fill: "
                        + TEXT
                        + ";"
                        + "-fx-font-size: 11px;"
        );

        box.getChildren()
                .addAll(
                        title,
                        subtitle
                );

        return box;
    }

    // =========================================================
    // ERROR VIEW
    // =========================================================

    private VBox createErrorView(
            String message
    ) {

        VBox box =
                new VBox(10);

        box.setAlignment(
                Pos.CENTER
        );

        box.setPadding(
                new Insets(
                        50
                )
        );

        box.setStyle(
                "-fx-background-color: "
                        + BG
                        + ";"
        );

        Label title =
                new Label(
                        "Firebase Connection Error"
                );

        title.setStyle(
                "-fx-text-fill: "
                        + RED
                        + ";"
                        + "-fx-font-size: 17px;"
                        + "-fx-font-weight: bold;"
        );

        Label description =
                new Label(
                        firstNonBlank(
                                message,
                                "Unable to load data."
                        )
                );

        description.setWrapText(
                true
        );

        description.setStyle(
                "-fx-text-fill: "
                        + HEADING
                        + ";"
                        + "-fx-font-size: 12px;"
        );

        box.getChildren()
                .addAll(
                        title,
                        description
                );

        return box;
    }

    // =========================================================
    // BACK BUTTON
    // =========================================================

    private void styleBackButton(
            Button button
    ) {

        button.setStyle(
                "-fx-background-color: "
                        + CONTROL_SURFACE
                        + ";"
                        + "-fx-text-fill: "
                        + BLUE
                        + ";"
                        + "-fx-border-color: "
                        + BLUE
                        + ";"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
                        + "-fx-font-size: 10px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-padding: 9 15 9 15;"
                        + "-fx-cursor: hand;"
        );
    }

    // =========================================================
    // TEXT FIELD STYLE
    // =========================================================

    private void styleTextField(
            TextField field
    ) {

        field.setStyle(
                "-fx-background-color: "
                        + CONTROL_SURFACE
                        + ";"
                        + "-fx-control-inner-background: "
                        + CONTROL_SURFACE
                        + ";"
                        + "-fx-text-fill: "
                        + RECORD_TEXT
                        + ";"
                        + "-fx-prompt-text-fill: "
                        + MUTED
                        + ";"
                        + "-fx-border-color: "
                        + BORDER
                        + ";"
                        + "-fx-border-width: 1;"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
                        + "-fx-padding: 0 12 0 12;"
                        + "-fx-font-size: 11px;"
        );
    }

    // =========================================================
    // COMBO BOX STYLE
    // =========================================================

    private void styleComboBox(
            ComboBox<String> combo
    ) {

        combo.setStyle(
                "-fx-background-color: "
                        + CONTROL_SURFACE
                        + ";"
                        + "-fx-border-color: "
                        + BORDER
                        + ";"
                        + "-fx-border-width: 1;"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
                        + "-fx-font-size: 11px;"
        );

        /*
         * Force selected value dark.
         */
        combo.setButtonCell(
                new javafx.scene.control.ListCell<>() {

                    @Override
                    protected void updateItem(
                            String item,
                            boolean empty
                    ) {

                        super.updateItem(
                                item,
                                empty
                        );

                        if (empty
                                ||
                                item == null) {

                            setText(
                                    null
                            );

                        } else {

                            setText(
                                    item
                            );

                            setStyle(
                                    "-fx-text-fill: "
                                            + RECORD_TEXT
                                            + ";"
                                            + "-fx-font-size: 11px;"
                            );
                        }
                    }
                }
        );

        /*
         * Popup list values dark.
         */
        combo.setCellFactory(
                listView ->
                        new javafx.scene.control.ListCell<>() {

                            @Override
                            protected void updateItem(
                                    String item,
                                    boolean empty
                            ) {

                                super.updateItem(
                                        item,
                                        empty
                                );

                                if (empty
                                        ||
                                        item == null) {

                                    setText(
                                            null
                                    );

                                    setStyle(
                                            ""
                                    );

                                } else {

                                    setText(
                                            item
                                    );

                                    setStyle(
                                            "-fx-text-fill: "
                                                    + RECORD_TEXT
                                                    + ";"
                                                    + "-fx-background-color: #1A1A1A;"
                                                    + "-fx-font-size: 11px;"
                                    );
                                }
                            }
                        }
        );

        /*
         * JavaFX skins style the arrow button separately. Applying this
         * after the control is attached prevents the default white block.
         */
        Platform.runLater(
                () -> {

                    javafx.scene.Node arrowButton =
                            combo.lookup(
                                    ".arrow-button"
                            );

                    if (arrowButton != null) {

                        arrowButton.setStyle(
                                "-fx-background-color: "
                                        + CONTROL_SURFACE
                                        + ";"
                                        + "-fx-background-radius: 0 8 8 0;"
                        );
                    }

                    javafx.scene.Node arrow =
                            combo.lookup(
                                    ".arrow"
                            );

                    if (arrow != null) {

                        arrow.setStyle(
                                "-fx-background-color: "
                                        + ORANGE
                                        + ";"
                        );
                    }
                }
        );
    }

    // =========================================================
    // MECHANIC DISPLAY
    // =========================================================

    private String getMechanicDisplay(
            ServiceRequest request
    ) {

        if (request == null
                ||
                !request.hasMechanic()) {

            return "Unassigned";
        }

        return firstNonBlank(
                request.getMechanicName(),
                request.getMechanicId(),
                "Assigned"
        );
    }

    // =========================================================
    // CUSTOMER DISPLAY
    // =========================================================

    private String getCustomerDisplay(
            ServiceRequest request
    ) {

        if (request == null) {

            return "";
        }

        return firstNonBlank(
                request.getCustomerName(),
                request.getCustomerId(),
                ""
        );
    }

    // =========================================================
    // REQUEST TIME
    // =========================================================

    private long getRequestTime(
            ServiceRequest request
    ) {

        if (request == null) {

            return 0L;
        }

        long value =
                parseTime(
                        request.getUpdatedAt()
                );

        if (value > 0) {

            return value;
        }

        value =
                parseTime(
                        request.getCompletedDate()
                );

        if (value > 0) {

            return value;
        }

        value =
                parseTime(
                        request.getCancelledDate()
                );

        if (value > 0) {

            return value;
        }

        value =
                parseTime(
                        request.getStartedDate()
                );

        if (value > 0) {

            return value;
        }

        value =
                parseTime(
                        request.getAcceptedDate()
                );

        if (value > 0) {

            return value;
        }

        value =
                parseTime(
                        request.getAssignedDate()
                );

        if (value > 0) {

            return value;
        }

        return parseTime(
                request.getRequestDate()
        );
    }

    // =========================================================
    // PARSE TIME
    // =========================================================

    private long parseTime(
            String value
    ) {

        value =
                clean(
                        value
                );

        if (value == null) {

            return 0L;
        }

        try {

            long number =
                    Long.parseLong(
                            value
                    );

            /*
             * Seconds → milliseconds
             */
            if (
                    number > 0
                            &&
                    number < 100000000000L
            ) {

                number *=
                        1000L;
            }

            return number;

        } catch (Exception e) {

            return 0L;
        }
    }

    // =========================================================
    // NORMALIZE STATUS
    // =========================================================

    private String normalizeStatus(
            String status
    ) {

        status =
                clean(
                        status
                );

        if (status == null) {

            return "Pending";
        }

        if (status.equalsIgnoreCase(
                "Pending"
        )) {

            return "Pending";
        }

        if (status.equalsIgnoreCase(
                "Assigned"
        )) {

            return "Assigned";
        }

        if (status.equalsIgnoreCase(
                "Accepted"
        )) {

            return "Accepted";
        }

        if (
                status.equalsIgnoreCase(
                        "In Progress"
                )
                        ||
                status.equalsIgnoreCase(
                        "InProgress"
                )
                        ||
                status.equalsIgnoreCase(
                        "Active"
                )
                        ||
                status.equalsIgnoreCase(
                        "Started"
                )
        ) {

            return "In Progress";
        }

        if (
                status.equalsIgnoreCase(
                        "Completed"
                )
                        ||
                status.equalsIgnoreCase(
                        "Complete"
                )
                        ||
                status.equalsIgnoreCase(
                        "Done"
                )
        ) {

            return "Completed";
        }

        if (
                status.equalsIgnoreCase(
                        "Cancelled"
                )
                        ||
                status.equalsIgnoreCase(
                        "Canceled"
                )
        ) {

            return "Cancelled";
        }

        return status;
    }

    // =========================================================
    // SET STAT NUMBER
    // =========================================================

    private void setNumber(
            Label label,
            int value
    ) {

        if (label != null) {

            label.setText(
                    String.valueOf(
                            value
                    )
            );
        }
    }

    // =========================================================
    // SEARCH CONTAINS
    // =========================================================

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

    // =========================================================
    // SHORTEN DESCRIPTION
    // =========================================================

    private String shorten(
            String value,
            int max
    ) {

        value =
                clean(
                        value
                );

        if (value == null) {

            return "No description provided.";
        }

        if (value.length()
                <= max) {

            return value;
        }

        return value.substring(
                0,
                max
        )
                + "...";
    }

    // =========================================================
    // FIRST NON BLANK
    // =========================================================

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

    // =========================================================
    // CLEAN
    // =========================================================

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
                "RoadGuardian"
        );

        alert.setHeaderText(
                title
        );

        alert.setContentText(
                firstNonBlank(
                        message,
                        "Unknown error."
                )
        );

        alert.showAndWait();
    }
}