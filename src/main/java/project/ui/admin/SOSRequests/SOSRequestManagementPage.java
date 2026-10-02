package project.ui.admin.SOSRequests;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;

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

import project.controller.admin.SOSRequestController;
import project.firebase.FirebaseConfig;
import project.model.SOSRequest;
import project.ui.admin.DashBoard.AdminSectionPage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class SOSRequestManagementPage
        extends AdminSectionPage {

    // =====================================================
    // THEME
    // =====================================================

    private static final String BG =
            "#0F0F0F";

    private static final String SURFACE =
            "#1A1A1A";

    private static final String SECONDARY =
            "#242424";

    private static final String HEADING =
            "#F3F4F6";

    private static final String TEXT =
            "#A1A1AA";

    private static final String BORDER =
            "#F59E0B";

    private static final String BLUE =
            "#F59E0B";

    private static final String PURPLE =
            "#F59E0B";

    private static final String CYAN =
            "#F59E0B";

    private static final String ORANGE =
            "#F59E0B";

    private static final String GREEN =
            "#22C55E";

    private static final String RED =
            "#EF4444";

    // =====================================================
    // CONTROLLER
    // =====================================================

    private SOSRequestController controller;

    // =====================================================
    // ROOT
    // =====================================================

    private VBox root;

    // =====================================================
    // DATA
    // =====================================================

    private final List<SOSRequest> allRequests =
            new ArrayList<>();

    // =====================================================
    // UI
    // =====================================================

    private VBox requestCards;

    private TextField searchField;

    private ComboBox<String> statusFilter;

    private ComboBox<String> sortFilter;

    private Label resultCountLabel;

    // =====================================================
    // STATISTICS
    // =====================================================

    private Label totalLabel;

    private Label pendingLabel;

    private Label assignedLabel;

    private Label acceptedLabel;

    private Label progressLabel;

    private Label resolvedLabel;

    // =====================================================
    // GET VIEW
    // =====================================================

    @Override
    public VBox getView() {

        root =
                new VBox();

        root.setStyle(
                "-fx-background-color: "
                        + BG
                        + ";"
        );

        try {

            Firestore firestore =
                    FirebaseConfig.getFirestore();

            controller =
                    new SOSRequestController(
                            firestore
                    );

        } catch (Exception e) {

            e.printStackTrace();

            return createErrorView(
                    "Unable to connect to Firestore."
            );
        }

        showManagementPage();

        return root;
    }

    // =====================================================
    // MANAGEMENT PAGE
    // =====================================================

    private void showManagementPage() {

        root.getChildren()
                .clear();

        root.setPadding(
                new Insets(
                        30,
                        32,
                        32,
                        32
                )
        );

        root.setSpacing(
                20
        );

        root.setStyle(
                "-fx-background-color: "
                        + BG
                        + ";"
        );

        VBox header =
                createHeader();

        HBox statistics =
                createStatistics();

        HBox toolbar =
                createToolbar();

        VBox records =
                createRecordsCard();

        root.getChildren()
                .addAll(
                        header,
                        statistics,
                        toolbar,
                        records
                );

        VBox.setVgrow(
                records,
                Priority.ALWAYS
        );

        loadRequests();
    }

    // =====================================================
    // HEADER
    // =====================================================

    private VBox createHeader() {

        VBox header =
                new VBox(6);

        Label title =
                new Label(
                        "SOS Emergency Requests"
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
                        32
                )
        );

        Label subtitle =
                new Label(
                        "Monitor customer emergencies, assign mechanics and track the complete SOS lifecycle."
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

        header.getChildren()
                .addAll(
                        title,
                        subtitle
                );

        return header;
    }

    // =====================================================
    // STATISTICS
    // =====================================================

    private HBox createStatistics() {

        HBox statistics =
                new HBox(12);

        statistics.setFillHeight(
                true
        );

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

        resolvedLabel =
                createValueLabel(
                        GREEN
                );

        VBox total =
                createStatCard(
                        "Total SOS",
                        totalLabel,
                        "All requests",
                        BLUE
                );

        VBox pending =
                createStatCard(
                        "Pending",
                        pendingLabel,
                        "Needs assignment",
                        ORANGE
                );

        VBox assigned =
                createStatCard(
                        "Assigned",
                        assignedLabel,
                        "Waiting for mechanic",
                        BLUE
                );

        VBox accepted =
                createStatCard(
                        "Accepted",
                        acceptedLabel,
                        "Mechanic accepted",
                        PURPLE
                );

        VBox progress =
                createStatCard(
                        "In Progress",
                        progressLabel,
                        "Responder active",
                        CYAN
                );

        VBox resolved =
                createStatCard(
                        "Resolved",
                        resolvedLabel,
                        "Emergency completed",
                        GREEN
                );

        List<VBox> cards =
                List.of(
                        total,
                        pending,
                        assigned,
                        accepted,
                        progress,
                        resolved
                );

        for (VBox card : cards) {

            HBox.setHgrow(
                    card,
                    Priority.ALWAYS
            );
        }

        statistics
                .getChildren()
                .addAll(
                        cards
                );

        return statistics;
    }

    // =====================================================
    // STAT VALUE
    // =====================================================

    private Label createValueLabel(
            String color
    ) {

        Label label =
                new Label(
                        "0"
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
                        27
                )
        );

        return label;
    }

    // =====================================================
    // STAT CARD
    // =====================================================

    private VBox createStatCard(
            String title,
            Label value,
            String subtitle,
            String color
    ) {

        VBox card =
                new VBox(6);

        card.setPadding(
                new Insets(
                        17
                )
        );

        card.setMinHeight(
                112
        );

        card.setMaxWidth(
                Double.MAX_VALUE
        );

        card.setStyle(
                "-fx-background-color: "
                        + SURFACE
                        + ";"
                        + "-fx-border-color: "
                        + BORDER
                        + ";"
                        + "-fx-border-radius: 13;"
                        + "-fx-background-radius: 13;"
        );

        Label titleLabel =
                new Label(
                        title
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
                        13
                )
        );

        Label subtitleLabel =
                new Label(
                        subtitle
                );

        subtitleLabel.setTextFill(
                Color.web(
                        TEXT
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
                        value,
                        subtitleLabel
                );

        card.setOnMouseEntered(
                event ->
                        card.setStyle(
                                "-fx-background-color: #1A1A1A;"
                                        + "-fx-border-color: "
                                        + color
                                        + ";"
                                        + "-fx-border-radius: 13;"
                                        + "-fx-background-radius: 13;"
                        )
        );

        card.setOnMouseExited(
                event ->
                        card.setStyle(
                                "-fx-background-color: "
                                        + SURFACE
                                        + ";"
                                        + "-fx-border-color: "
                                        + BORDER
                                        + ";"
                                        + "-fx-border-radius: 13;"
                                        + "-fx-background-radius: 13;"
                        )
        );

        return card;
    }

    // =====================================================
    // TOOLBAR
    // =====================================================

    private HBox createToolbar() {

        HBox toolbar =
                new HBox(12);

        toolbar.setAlignment(
                Pos.CENTER_LEFT
        );

        // =================================================
        // SEARCH
        // =================================================

        searchField =
                new TextField();

        searchField.setPromptText(
                "Search SOS, customer, mechanic, vehicle, emergency or location..."
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

        // =================================================
        // STATUS
        // =================================================

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
                        "Resolved",
                        "Cancelled"
                );

        statusFilter.setValue(
                "All"
        );

        statusFilter.setPrefWidth(
                145
        );

        statusFilter.setPrefHeight(
                44
        );

        styleComboBox(
                statusFilter
        );

        // =================================================
        // SORT
        // =================================================

        sortFilter =
                new ComboBox<>();

        sortFilter
                .getItems()
                .addAll(
                        "Newest First",
                        "Oldest First",
                        "Customer A-Z",
                        "Customer Z-A",
                        "Emergency A-Z"
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

        Button refresh =
                createButton(
                        "Refresh",
                        BLUE
                );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
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

        sortFilter
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

    // =====================================================
    // RECORDS CARD
    // =====================================================

    private VBox createRecordsCard() {

        VBox card =
                new VBox(14);

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
                        + "-fx-border-radius: 14;"
                        + "-fx-background-radius: 14;"
        );

        // =================================================
        // HEADER
        // =================================================

        HBox header =
                new HBox();

        header.setAlignment(
                Pos.CENTER_LEFT
        );

        VBox headingBox =
                new VBox(4);

        Label title =
                new Label(
                        "Emergency SOS Information"
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
                        "Live SOS data from the canonical SOSRequests collection"
                );

        subtitle.setTextFill(
                Color.web(
                        TEXT
                )
        );

        subtitle.setFont(
                Font.font(
                        "Arial",
                        13
                )
        );

        resultCountLabel =
                new Label(
                        "0 requests"
                );

        resultCountLabel.setTextFill(
                Color.web(
                        BLUE
                )
        );

        resultCountLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        12
                )
        );

        headingBox
                .getChildren()
                .addAll(
                        title,
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
                        "● Latest Update"
                );

        live.setTextFill(
                Color.web(
                        GREEN
                )
        );

        live.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        11
                )
        );

        header.getChildren()
                .addAll(
                        headingBox,
                        spacer,
                        live
                );

        // =================================================
        // REQUEST LIST
        // =================================================

        requestCards =
                new VBox(12);

        requestCards.setFillWidth(
                true
        );

        ScrollPane scrollPane =
                new ScrollPane(
                        requestCards
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
                "-fx-background-color: transparent;"
                        + "-fx-background: transparent;"
        );

        VBox.setVgrow(
                scrollPane,
                Priority.ALWAYS
        );

        card.getChildren()
                .addAll(
                        header,
                        scrollPane
                );

        return card;
    }

    // =====================================================
    // SOS CARD
    // =====================================================

    private VBox createSOSCard(
            SOSRequest request,
            int number
    ) {

        VBox card =
                new VBox(13);

        card.setPadding(
                new Insets(
                        18,
                        20,
                        18,
                        20
                )
        );

        card.setCursor(
                Cursor.HAND
        );

        String normalStyle =
                "-fx-background-color: "
                        + SECONDARY
                        + ";"
                        + "-fx-border-color: "
                        + BORDER
                        + ";"
                        + "-fx-border-radius: 13;"
                        + "-fx-background-radius: 13;";

        card.setStyle(
                normalStyle
        );

        // =================================================
        // TOP
        // =================================================

        HBox top =
                new HBox(12);

        top.setAlignment(
                Pos.CENTER_LEFT
        );

        Label emergencyIcon =
                new Label(
                        "SOS"
                );

        emergencyIcon.setAlignment(
                Pos.CENTER
        );

        emergencyIcon.setMinSize(
                48,
                48
        );

        emergencyIcon.setTextFill(
                Color.WHITE
        );

        emergencyIcon.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        12
                )
        );

        emergencyIcon.setStyle(
                "-fx-background-color: "
                        + RED
                        + ";"
                        + "-fx-background-radius: 24;"
        );

        VBox identity =
                new VBox(4);

        HBox idRow =
                new HBox(8);

        idRow.setAlignment(
                Pos.CENTER_LEFT
        );

        Label serial =
                new Label(
                        "#" + number
                );

        serial.setTextFill(
                Color.web(
                        RED
                )
        );

        serial.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        11
                )
        );

        Label id =
                new Label(
                        safe(
                                request.getSosId()
                        )
                );

        id.setTextFill(
                Color.web(
                        HEADING
                )
        );

        id.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        17
                )
        );

        idRow.getChildren()
                .addAll(
                        serial,
                        id
                );

        Label customer =
                new Label(
                        "Customer: "
                                + controller
                                .getCustomerDisplay(
                                        request
                                )
                );

        customer.setTextFill(
                Color.web(
                        TEXT
                )
        );

        customer.setFont(
                Font.font(
                        "Arial",
                        12
                )
        );

        identity
                .getChildren()
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

        Label status =
                createStatusBadge(
                        controller
                                .getStatusDisplay(
                                        request
                                )
                );

        top.getChildren()
                .addAll(
                        emergencyIcon,
                        identity,
                        spacer,
                        status
                );

        // =================================================
        // INFORMATION
        // =================================================

        HBox infoRow =
                new HBox(10);

        VBox vehicle =
                createInfoBox(
                        "VEHICLE",
                        controller
                                .getVehicleDisplay(
                                        request
                                )
                );

        VBox emergency =
                createInfoBox(
                        "EMERGENCY",
                        controller
                                .getEmergencyDisplay(
                                        request
                                )
                );

        VBox mechanic =
                createInfoBox(
                        "MECHANIC",
                        controller
                                .getMechanicDisplay(
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

        HBox.setHgrow(
                vehicle,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                emergency,
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

        infoRow.getChildren()
                .addAll(
                        vehicle,
                        emergency,
                        mechanic,
                        location
                );

        // =================================================
        // DESCRIPTION + ACTIONS
        // =================================================

        HBox bottom =
                new HBox(10);

        bottom.setAlignment(
                Pos.CENTER_LEFT
        );

        Label description =
                new Label(
                        shorten(
                                safe(
                                        request.getDescription()
                                ),
                                90
                        )
                );

        description.setTextFill(
                Color.web(
                        TEXT
                )
        );

        description.setFont(
                Font.font(
                        "Arial",
                        11
                )
        );

        description.setWrapText(
                true
        );

        Region bottomSpacer =
                new Region();

        HBox.setHgrow(
                bottomSpacer,
                Priority.ALWAYS
        );

        // =================================================
        // ASSIGN / REASSIGN
        // =================================================

        if (controller.canAssignMechanic(
                request
        )) {

            boolean alreadyAssigned =
                    request.isAssigned()
                            &&
                    request.hasMechanic();

            Button assign =
                    createSmallButton(
                            alreadyAssigned
                                    ? "Reassign Mechanic"
                                    : "Assign Mechanic",
                            BLUE
                    );

            assign.setOnAction(
                    event -> {

                        event.consume();

                        showAssignMechanicPage(
                                request
                        );
                    }
            );

            bottom.getChildren()
                    .add(
                            assign
                    );
        }

        Button view =
                createSmallButton(
                        "View Emergency →",
                        RED
                );

        view.setOnAction(
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
                        bottomSpacer
                );

        /*
         * Buttons were already temporarily added above,
         * so rebuild order cleanly.
         */
        List<javafx.scene.Node> actionNodes =
                new ArrayList<>();

        if (controller.canAssignMechanic(
                request
        )) {

            boolean alreadyAssigned =
                    request.isAssigned()
                            &&
                    request.hasMechanic();

            Button assign =
                    createSmallButton(
                            alreadyAssigned
                                    ? "Reassign Mechanic"
                                    : "Assign Mechanic",
                            BLUE
                    );

            assign.setOnAction(
                    event -> {

                        event.consume();

                        showAssignMechanicPage(
                                request
                        );
                    }
            );

            actionNodes.add(
                    assign
            );
        }

        actionNodes.add(
                view
        );

        bottom.getChildren()
                .clear();

        bottom.getChildren()
                .addAll(
                        description,
                        bottomSpacer
                );

        bottom.getChildren()
                .addAll(
                        actionNodes
                );

        card.getChildren()
                .addAll(
                        top,
                        new Separator(),
                        infoRow,
                        bottom
                );

        // =================================================
        // HOVER
        // =================================================

        card.setOnMouseEntered(
                event ->
                        card.setStyle(
                                "-fx-background-color: #242424;"
                                        + "-fx-border-color: "
                                        + RED
                                        + ";"
                                        + "-fx-border-radius: 13;"
                                        + "-fx-background-radius: 13;"
                        )
        );

        card.setOnMouseExited(
                event ->
                        card.setStyle(
                                normalStyle
                        )
        );

        // =================================================
        // DOUBLE CLICK
        // =================================================

        card.setOnMouseClicked(
                event -> {

                    if (event.getClickCount()
                            == 2) {

                        showDetails(
                                request
                        );
                    }
                }
        );

        return card;
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
                new Insets(
                        10
                )
        );

        box.setMinWidth(
                150
        );

        box.setStyle(
                "-fx-background-color: "
                        + SURFACE
                        + ";"
                        + "-fx-background-radius: 9;"
        );

        Label titleLabel =
                new Label(
                        title
                );

        titleLabel.setTextFill(
                Color.web(
                        TEXT
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
                        safe(
                                value
                        )
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

        valueLabel.setWrapText(
                true
        );

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

        String value =
                safe(
                        status
                );

        String color =
                getStatusColor(
                        value
                );

        Label badge =
                new Label(
                        value
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

        badge.setPadding(
                new Insets(
                        7,
                        12,
                        7,
                        12
                )
        );

        badge.setStyle(
                "-fx-background-color: "
                        + color
                        + "18;"
                        + "-fx-background-radius: 18;"
        );

        return badge;
    }

    // =====================================================
    // STATUS COLOR
    // =====================================================

    private String getStatusColor(
            String status
    ) {

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
                "Resolved"
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

    // =====================================================
    // FILTER
    // =====================================================

    private void applyFilters() {

        if (requestCards == null) {

            return;
        }

        String search =
                searchField == null
                        ? ""
                        : searchField
                        .getText()
                        .trim()
                        .toLowerCase();

        String selectedStatus =
                statusFilter == null
                        ? "All"
                        : statusFilter
                        .getValue();

        String selectedSort =
                sortFilter == null
                        ? "Newest First"
                        : sortFilter
                        .getValue();

        List<SOSRequest> filtered =
                new ArrayList<>();

        for (SOSRequest request :
                allRequests) {

            if (!matchesSearch(
                    request,
                    search
            )) {

                continue;
            }

            if (
                    selectedStatus != null
                            &&
                    !selectedStatus.equalsIgnoreCase(
                            "All"
                    )
                            &&
                    (
                            request.getStatus() == null
                                    ||
                            !request
                                    .getStatus()
                                    .equalsIgnoreCase(
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
                selectedSort
        );

        renderRequests(
                filtered
        );
    }

    // =====================================================
    // SEARCH
    // =====================================================

    private boolean matchesSearch(
            SOSRequest request,
            String search
    ) {

        if (request == null) {

            return false;
        }

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
                        request.getMechanicName(),
                        search
                )
                ||
                contains(
                        request.getMechanicId(),
                        search
                )
                ||
                contains(
                        request.getStatus(),
                        search
                );
    }

    // =====================================================
    // SORT
    // =====================================================

    private void sortRequests(
            List<SOSRequest> requests,
            String sort
    ) {

        if (requests == null) {

            return;
        }

        if ("Customer A-Z".equals(
                sort
        )) {

            requests.sort(
                    Comparator.comparing(
                            request ->
                                    safe(
                                            request
                                                    .getCustomerName()
                                    )
                                            .toLowerCase()
                    )
            );

            return;
        }

        if ("Customer Z-A".equals(
                sort
        )) {

            requests.sort(
                    Comparator.comparing(
                                    (SOSRequest request) ->
                                            safe(
                                                    request
                                                            .getCustomerName()
                                            )
                                                    .toLowerCase()
                            )
                            .reversed()
            );

            return;
        }

        if ("Emergency A-Z".equals(
                sort
        )) {

            requests.sort(
                    Comparator.comparing(
                            request ->
                                    safe(
                                            request
                                                    .getEmergencyType()
                                    )
                                            .toLowerCase()
                    )
            );

            return;
        }

        Comparator<SOSRequest> dateComparator =
                Comparator.comparingLong(
                        this::getRequestTimestamp
                );

        if ("Oldest First".equals(
                sort
        )) {

            requests.sort(
                    dateComparator
            );

        } else {

            requests.sort(
                    dateComparator.reversed()
            );
        }
    }

    // =====================================================
    // LOAD REQUESTS
    // =====================================================

    private void loadRequests() {

        if (requestCards != null) {

            requestCards
                    .getChildren()
                    .clear();

            Label loading =
                    new Label(
                            "Loading SOS requests from Firebase..."
                    );

            loading.setPadding(
                    new Insets(
                            25
                    )
            );

            loading.setTextFill(
                    Color.web(
                            TEXT
                    )
            );

            requestCards
                    .getChildren()
                    .add(
                            loading
                    );
        }

        try {

            List<SOSRequest> requests =
                    controller
                            .getAllRequests();

            allRequests.clear();

            if (requests != null) {

                allRequests.addAll(
                        requests
                );
            }

            updateStatistics();

            applyFilters();

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    "Unable to Load SOS Requests",
                    safe(
                            e.getMessage()
                    )
            );
        }
    }

    // =====================================================
    // RENDER REQUESTS
    // =====================================================

    private void renderRequests(
            List<SOSRequest> requests
    ) {

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

            resultCountLabel.setText(
                    "0 requests found"
            );

            return;
        }

        int number =
                1;

        for (SOSRequest request :
                requests) {

            requestCards
                    .getChildren()
                    .add(
                            createSOSCard(
                                    request,
                                    number++
                            )
                    );
        }

        resultCountLabel.setText(
                requests.size()
                        + (
                        requests.size() == 1
                                ? " request"
                                : " requests"
                )
        );
    }

    // =====================================================
    // STATISTICS UPDATE
    // =====================================================

    private void updateStatistics() {

        int pending =
                0;

        int assigned =
                0;

        int accepted =
                0;

        int progress =
                0;

        int resolved =
                0;

        for (SOSRequest request :
                allRequests) {

            if (request == null
                    ||
                    request.getStatus() == null) {

                continue;
            }

            String status =
                    request
                            .getStatus()
                            .trim();

            if (status.equalsIgnoreCase(
                    "Pending"
            )) {

                pending++;

            } else if (status.equalsIgnoreCase(
                    "Assigned"
            )) {

                assigned++;

            } else if (status.equalsIgnoreCase(
                    "Accepted"
            )) {

                accepted++;

            } else if (status.equalsIgnoreCase(
                    "In Progress"
            )) {

                progress++;

            } else if (status.equalsIgnoreCase(
                    "Resolved"
            )) {

                resolved++;
            }
        }

        totalLabel.setText(
                String.valueOf(
                        allRequests.size()
                )
        );

        pendingLabel.setText(
                String.valueOf(
                        pending
                )
        );

        assignedLabel.setText(
                String.valueOf(
                        assigned
                )
        );

        acceptedLabel.setText(
                String.valueOf(
                        accepted
                )
        );

        progressLabel.setText(
                String.valueOf(
                        progress
                )
        );

        resolvedLabel.setText(
                String.valueOf(
                        resolved
                )
        );
    }

    // =====================================================
    // DETAILS
    //
    // Always reload the document so page never displays
    // stale Pending/Assigned data after assignment.
    // =====================================================

    private void showDetails(
            SOSRequest request
    ) {

        if (request == null) {

            return;
        }

        showDetailsById(
                request.getSosId()
        );
    }

    private void showDetailsById(
            String sosId
    ) {

        SOSRequest freshRequest =
                controller
                        .getRequestById(
                                sosId
                        );

        if (freshRequest == null) {

            showError(
                    "SOS Request",
                    "The SOS request could not be found."
            );

            showManagementPage();

            return;
        }

        SOSRequestDetailsPage page =
                new SOSRequestDetailsPage(

                        freshRequest,

                        this::showManagementPage,

                        () ->
                                showAssignMechanicPage(
                                        freshRequest
                                )
                );

        root.getChildren()
                .clear();

        root.setPadding(
                new Insets(0)
        );

        root.getChildren()
                .add(
                        page.getView()
                );
    }

    // =====================================================
    // ASSIGN MECHANIC
    // =====================================================

    private void showAssignMechanicPage(
            SOSRequest request
    ) {

        if (request == null) {

            return;
        }

        SOSRequest freshRequest =
                controller
                        .getRequestById(
                                request.getSosId()
                        );

        if (freshRequest == null) {

            showError(
                    "Assignment Error",
                    "SOS request was not found."
            );

            return;
        }

        // =================================================
        // ONLY:
        //
        // Pending  -> Assign
        // Assigned -> Reassign
        //
        // NOT Accepted / In Progress / Resolved / Cancelled
        // =================================================

        if (!controller.canAssignMechanic(
                freshRequest
        )) {

            showError(
                    "Assignment Not Allowed",
                    "This SOS request is already "
                            + controller
                            .getStatusDisplay(
                                    freshRequest
                            )
                            + ". A mechanic can only be assigned "
                            + "while the request is Pending or Assigned."
            );

            showDetailsById(
                    freshRequest.getSosId()
            );

            return;
        }

        String sosId =
                freshRequest
                        .getSosId();

        AssignSOSMechanicPage page =
                new AssignSOSMechanicPage(

                        freshRequest,

                        () ->
                                showDetailsById(
                                        sosId
                                ),

                        () ->
                                showDetailsById(
                                        sosId
                                )
                );

        root.getChildren()
                .clear();

        root.setPadding(
                new Insets(0)
        );

        root.getChildren()
                .add(
                        page.getView()
                );
    }

    // =====================================================
    // EMPTY
    // =====================================================

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
                        "No SOS requests found"
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
                        17
                )
        );

        Label subtitle =
                new Label(
                        "Customer SOS requests will appear here automatically."
                );

        subtitle.setTextFill(
                Color.web(
                        TEXT
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

        button.setTextFill(
                Color.WHITE
        );

        button.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        13
                )
        );

        button.setPrefHeight(
                44
        );

        button.setPadding(
                new Insets(
                        0,
                        18,
                        0,
                        18
                )
        );

        button.setCursor(
                Cursor.HAND
        );

        button.setStyle(
                "-fx-background-color: "
                        + color
                        + ";"
                        + "-fx-background-radius: 9;"
        );

        return button;
    }

    // =====================================================
    // SMALL BUTTON
    // =====================================================

    private Button createSmallButton(
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
                        11
                )
        );

        button.setPadding(
                new Insets(
                        8,
                        13,
                        8,
                        13
                )
        );

        button.setCursor(
                Cursor.HAND
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
    // TEXT FIELD
    // =====================================================

    private void styleTextField(
            TextField field
    ) {

        field.setStyle(
                "-fx-background-color: "
                        + SURFACE
                        + ";"
                        + "-fx-text-fill: "
                        + HEADING
                        + ";"
                        + "-fx-prompt-text-fill: "
                        + TEXT
                        + ";"
                        + "-fx-border-color: "
                        + BORDER
                        + ";"
                        + "-fx-border-radius: 9;"
                        + "-fx-background-radius: 9;"
                        + "-fx-padding: 0 14 0 14;"
                        + "-fx-font-size: 13px;"
        );
    }

    // =====================================================
    // COMBOBOX
    // =====================================================

    private void styleComboBox(
            ComboBox<String> combo
    ) {

        combo.setStyle(
                "-fx-background-color: "
                        + SURFACE
                        + ";"
                        + "-fx-border-color: "
                        + BORDER
                        + ";"
                        + "-fx-border-radius: 9;"
                        + "-fx-background-radius: 9;"
                        + "-fx-font-size: 12px;"
        );
    }

    // =====================================================
    // REQUEST TIMESTAMP
    // =====================================================

    private long getRequestTimestamp(
            SOSRequest request
    ) {

        if (request == null) {

            return 0L;
        }

        long timestamp =
                toMillis(
                        request.getUpdatedAt()
                );

        if (timestamp > 0) {

            return timestamp;
        }

        timestamp =
                toMillis(
                        request.getCreatedAt()
                );

        if (timestamp > 0) {

            return timestamp;
        }

        return toMillis(
                request.getRequestDate()
        );
    }

    // =====================================================
    // OBJECT -> MILLIS
    // =====================================================

    private long toMillis(
            Object value
    ) {

        if (value == null) {

            return 0L;
        }

        if (value instanceof Number) {

            long number =
                    ((Number) value)
                            .longValue();

            if (
                    number > 0
                            &&
                    number < 100000000000L
            ) {

                number *=
                        1000L;
            }

            return number;
        }

        if (value instanceof Date) {

            return ((Date) value)
                    .getTime();
        }

        if (value instanceof Timestamp) {

            return ((Timestamp) value)
                    .toSqlTimestamp()
                    .getTime();
        }

        try {

            long number =
                    Long.parseLong(
                            String.valueOf(
                                    value
                            ).trim()
                    );

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
    // SHORTEN
    // =====================================================

    private String shorten(
            String value,
            int max
    ) {

        value =
                safe(
                        value
                );

        if (value.length()
                <= max) {

            return value;
        }

        return value.substring(
                0,
                max - 3
        ) + "...";
    }

    // =====================================================
    // SAFE
    // =====================================================

    private String safe(
            String value
    ) {

        if (value == null
                ||
                value.trim().isEmpty()) {

            return "-";
        }

        return value.trim();
    }

    // =====================================================
    // ERROR
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

    // =====================================================
    // ERROR VIEW
    // =====================================================

    private VBox createErrorView(
            String message
    ) {

        VBox error =
                new VBox(12);

        error.setAlignment(
                Pos.CENTER
        );

        error.setPadding(
                new Insets(
                        50
                )
        );

        error.setStyle(
                "-fx-background-color: "
                        + BG
                        + ";"
        );

        Label title =
                new Label(
                        "SOS Requests"
                );

        title.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        25
                )
        );

        title.setTextFill(
                Color.web(
                        HEADING
                )
        );

        Label description =
                new Label(
                        message
                );

        description.setTextFill(
                Color.web(
                        RED
                )
        );

        error.getChildren()
                .addAll(
                        title,
                        description
                );

        return error;
    }
}