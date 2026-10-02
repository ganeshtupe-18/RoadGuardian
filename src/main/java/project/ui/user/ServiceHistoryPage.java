package project.ui.user;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.Scene;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import javafx.scene.paint.Color;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import project.dao.user.ServiceRequestDAO;
import project.dao.user.VehicleDAO;

import project.model.ServiceRequest;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ServiceHistoryPage {

    // =====================================================
    // COLORS
    // =====================================================

    private static final String BACKGROUND =
            "#0F0F0F";

    private static final String CARD =
            "#1A1A1A";

    private static final String SECONDARY_SURFACE =
            "#242424";

    private static final String HEADING =
            "#F3F4F6";

    private static final String SECONDARY_TEXT =
            "#A1A1AA";

    private static final String BORDER =
            "#F59E0B";

    private static final String BLUE =
            "#F59E0B";

    private static final String SUCCESS =
            "#22C55E";

    private static final String ERROR =
            "#EF4444";

    private static final String ORANGE =
            "#F59E0B";

    // =====================================================
    // DAO
    // =====================================================

    private final ServiceRequestDAO serviceRequestDAO;

    private final VehicleDAO vehicleDAO;

    // =====================================================
    // SESSION
    // =====================================================

    private final String customerId;

    // =====================================================
    // DATA
    // =====================================================

    private final List<ServiceRequest> allHistory =
            new ArrayList<>();

    private final List<Map<String, Object>> vehicles =
            new ArrayList<>();

    // =====================================================
    // UI
    // =====================================================

    private Scene serviceHistoryScene;

    private VBox historyContainer;

    private Label completedCountLabel;

    private Label cancelledCountLabel;

    private Label totalSpendLabel;

    private Label totalRecordsLabel;

    private TextField searchField;

    private ComboBox<String> statusFilter;

    private ComboBox<String> vehicleFilter;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public ServiceHistoryPage() {

        serviceRequestDAO =
                new ServiceRequestDAO();

        vehicleDAO =
                new VehicleDAO();

        customerId =
                getCurrentCustomerId();
    }

    // =====================================================
    // PUBLIC SCENE
    // =====================================================

    public Scene getServiceHistoryScene() {

        BorderPane root =
                new BorderPane();

        root.setStyle(
                "-fx-background-color: "
                        + BACKGROUND
                        + ";"
        );

        root.setTop(
                UserHeader.createHeader()
        );

        root.setLeft(
                UserSideBar.createSidebar(
                        "Service History"
                )
        );

        VBox content =
                createContent();

        ScrollPane scrollPane =
                new ScrollPane(
                        content
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

        root.setCenter(
                scrollPane
        );

        double width =
                UserDashboard.dashboardStage != null
                        ? UserDashboard.dashboardStage.getWidth()
                        : 1200;

        double height =
                UserDashboard.dashboardStage != null
                        ? UserDashboard.dashboardStage.getHeight()
                        : 800;

        serviceHistoryScene =
                new Scene(
                        root,
                        width,
                        height
                );

        loadHistory();

        return serviceHistoryScene;
    }

    // =====================================================
    // CREATE CONTENT
    // =====================================================

    private VBox createContent() {

        VBox content =
                new VBox(20);

        content.setPadding(
                new Insets(
                        28,
                        32,
                        40,
                        32
                )
        );

        content.setStyle(
                "-fx-background-color: "
                        + BACKGROUND
                        + ";"
        );

        content.getChildren()
                .add(
                        createHeader()
                );

        content.getChildren()
                .add(
                        createStatistics()
                );

        content.getChildren()
                .add(
                        createFilters()
                );

        content.getChildren()
                .add(
                        createHistorySection()
                );

        return content;
    }

    // =====================================================
    // HEADER
    // =====================================================

    private HBox createHeader() {

        HBox header =
                new HBox(15);

        header.setAlignment(
                Pos.CENTER_LEFT
        );

        VBox text =
                new VBox(5);

        Label title =
                new Label(
                        "Service History"
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
                        29
                )
        );

        Label subtitle =
                new Label(
                        "View your completed and cancelled RoadGuardian service requests."
                );

        subtitle.setTextFill(
                Color.web(
                        SECONDARY_TEXT
                )
        );

        subtitle.setFont(
                Font.font(
                        "Arial",
                        13
                )
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

        Button refresh =
                createSecondaryButton(
                        "Refresh"
                );

        refresh.setOnAction(
                event ->
                        loadHistory()
        );

        header.getChildren()
                .addAll(
                        text,
                        spacer,
                        refresh
                );

        return header;
    }

    // =====================================================
    // STATISTICS
    // =====================================================

    private HBox createStatistics() {

        HBox row =
                new HBox(14);

        completedCountLabel =
                new Label("0");

        cancelledCountLabel =
                new Label("0");

        totalSpendLabel =
                new Label("₹0");

        totalRecordsLabel =
                new Label("0");

        VBox completed =
                createStatCard(
                        "COMPLETED",
                        completedCountLabel,
                        "Successfully completed",
                        SUCCESS
                );

        VBox cancelled =
                createStatCard(
                        "CANCELLED",
                        cancelledCountLabel,
                        "Cancelled requests",
                        ERROR
                );

        VBox totalSpend =
                createStatCard(
                        "TOTAL SPEND",
                        totalSpendLabel,
                        "Completed services only",
                        ORANGE
                );

        VBox totalRecords =
                createStatCard(
                        "TOTAL RECORDS",
                        totalRecordsLabel,
                        "Service history records",
                        BLUE
                );

        HBox.setHgrow(
                completed,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                cancelled,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                totalSpend,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                totalRecords,
                Priority.ALWAYS
        );

        row.getChildren()
                .addAll(
                        completed,
                        cancelled,
                        totalSpend,
                        totalRecords
                );

        return row;
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
                        + CARD
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

        value.setTextFill(
                Color.web(
                        color
                )
        );

        value.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        24
                )
        );

        Label subtitleLabel =
                new Label(
                        subtitle
                );

        subtitleLabel.setTextFill(
                Color.web(
                        SECONDARY_TEXT
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

        return card;
    }

    // =====================================================
    // FILTERS
    // =====================================================

    private HBox createFilters() {

        HBox row =
                new HBox(12);

        row.setAlignment(
                Pos.CENTER_LEFT
        );

        // =================================================
        // SEARCH
        // =================================================

        searchField =
                new TextField();

        searchField.setPromptText(
                "Search service, mechanic, vehicle, problem or request ID..."
        );

        searchField.setPrefWidth(
                360
        );

        styleField(
                searchField
        );

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

        // =================================================
        // VEHICLE FILTER
        // =================================================

        vehicleFilter =
                new ComboBox<>();

        vehicleFilter.setPrefWidth(
                220
        );

        vehicleFilter
                .getItems()
                .add(
                        "All Vehicles"
                );

        vehicleFilter.setValue(
                "All Vehicles"
        );

        vehicleFilter.setStyle(
                comboStyle()
        );

        vehicleFilter.setOnAction(
                event ->
                        applyFilters()
        );

        // =================================================
        // STATUS
        // =================================================

        statusFilter =
                new ComboBox<>();

        statusFilter
                .getItems()
                .addAll(
                        "All Status",
                        "Completed",
                        "Cancelled"
                );

        statusFilter.setValue(
                "All Status"
        );

        statusFilter.setPrefWidth(
                150
        );

        statusFilter.setStyle(
                comboStyle()
        );

        statusFilter.setOnAction(
                event ->
                        applyFilters()
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Label firebase =
                new Label(
                        ""
                );

        firebase.setTextFill(
                Color.web(
                        SUCCESS
                )
        );

        firebase.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        9
                )
        );

        row.getChildren()
                .addAll(
                        searchField,
                        vehicleFilter,
                        statusFilter,
                        spacer,
                        firebase
                );

        return row;
    }

    // =====================================================
    // HISTORY SECTION
    // =====================================================

    private VBox createHistorySection() {

        VBox card =
                new VBox(13);

        card.setPadding(
                new Insets(20)
        );

        card.setStyle(
                "-fx-background-color: "
                        + CARD
                        + ";"
                        + "-fx-border-color: "
                        + BORDER
                        + ";"
                        + "-fx-border-radius: 14;"
                        + "-fx-background-radius: 14;"
        );

        Label title =
                new Label(
                        "Service Records"
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
                        19
                )
        );

        Label subtitle =
                new Label(
                        "Records are read from the same serviceRequests collection used by Customer, Admin and Mechanic."
                );

        subtitle.setTextFill(
                Color.web(
                        SECONDARY_TEXT
                )
        );

        subtitle.setFont(
                Font.font(
                        "Arial",
                        10
                )
        );

        historyContainer =
                new VBox(12);

        card.getChildren()
                .addAll(
                        title,
                        subtitle,
                        new Separator(),
                        historyContainer
                );

        return card;
    }

    // =====================================================
    // LOAD HISTORY
    // =====================================================

    private void loadHistory() {

        if (historyContainer == null) {

            return;
        }

        showLoading();

        allHistory.clear();

        if (customerId == null) {

            showEmpty(
                    "Login Required",
                    "Please login again to view your service history."
            );

            updateStatistics();

            return;
        }

        try {

            // =================================================
            // LOAD VEHICLES
            // =================================================

            loadVehicles();

            // =================================================
            // SAME CUSTOMER SERVICE REQUESTS
            // =================================================

            List<ServiceRequest> requests =
                    serviceRequestDAO
                            .getRequestsByCustomerId(
                                    customerId
                            );

            if (requests != null) {

                for (ServiceRequest request :
                        requests) {

                    if (request == null) {

                        continue;
                    }

                    /*
                     * Customer history =
                     *
                     * Completed
                     * Cancelled
                     */
                    if (
                            request.isCompleted()
                                    ||
                            request.isCancelled()
                    ) {

                        allHistory.add(
                                request
                        );
                    }
                }
            }

            // =================================================
            // LATEST FIRST
            // =================================================

            allHistory.sort(
                    Comparator
                            .comparingLong(
                                    this::getHistoryTime
                            )
                            .reversed()
            );

            updateStatistics();

            applyFilters();

        } catch (Exception e) {

            e.printStackTrace();

            allHistory.clear();

            updateStatistics();

            showEmpty(
                    "Unable to Load History",
                    "Service history could not be loaded."
            );
        }
    }

    // =====================================================
    // LOAD VEHICLE FILTER
    // =====================================================

    private void loadVehicles() {

        vehicles.clear();

        if (vehicleFilter != null) {

            vehicleFilter
                    .getItems()
                    .clear();

            vehicleFilter
                    .getItems()
                    .add(
                            "All Vehicles"
                    );

            vehicleFilter.setValue(
                    "All Vehicles"
            );
        }

        try {

            List<Map<String, Object>> result =
                    vehicleDAO.getVehicles(
                            customerId
                    );

            if (result != null) {

                vehicles.addAll(
                        result
                );
            }

            if (vehicleFilter == null) {

                return;
            }

            for (Map<String, Object> vehicle :
                    vehicles) {

                String display =
                        createVehicleDisplay(
                                vehicle
                        );

                if (!vehicleFilter
                        .getItems()
                        .contains(
                                display
                        )) {

                    vehicleFilter
                            .getItems()
                            .add(
                                    display
                            );
                }
            }

        } catch (Exception e) {

            System.err.println(
                    "Vehicle filter load error: "
                            + e.getMessage()
            );
        }
    }

    // =====================================================
    // STATISTICS UPDATE
    // =====================================================

    private void updateStatistics() {

        int completed =
                0;

        int cancelled =
                0;

        double totalSpend =
                0.0;

        for (ServiceRequest request :
                allHistory) {

            if (request == null) {

                continue;
            }

            if (request.isCompleted()) {

                completed++;

                totalSpend +=
                        getFinalAmount(
                                request
                        );

            } else if (request.isCancelled()) {

                cancelled++;
            }
        }

        if (completedCountLabel != null) {

            completedCountLabel.setText(
                    String.valueOf(
                            completed
                    )
            );
        }

        if (cancelledCountLabel != null) {

            cancelledCountLabel.setText(
                    String.valueOf(
                            cancelled
                    )
            );
        }

        if (totalRecordsLabel != null) {

            totalRecordsLabel.setText(
                    String.valueOf(
                            allHistory.size()
                    )
            );
        }

        if (totalSpendLabel != null) {

            totalSpendLabel.setText(
                    formatMoney(
                            totalSpend
                    )
            );
        }
    }

    // =====================================================
    // APPLY FILTERS
    // =====================================================

    private void applyFilters() {

        if (historyContainer == null) {

            return;
        }

        String search =
                searchField == null
                        ? null
                        : clean(
                        searchField.getText()
                );

        String status =
                statusFilter == null
                        ? "All Status"
                        : statusFilter.getValue();

        String vehicle =
                vehicleFilter == null
                        ? "All Vehicles"
                        : vehicleFilter.getValue();

        List<ServiceRequest> filtered =
                new ArrayList<>();

        for (ServiceRequest request :
                allHistory) {

            if (request == null) {

                continue;
            }

            if (!matchesStatus(
                    request,
                    status
            )) {

                continue;
            }

            if (!matchesVehicle(
                    request,
                    vehicle
            )) {

                continue;
            }

            if (!matchesSearch(
                    request,
                    search
            )) {

                continue;
            }

            filtered.add(
                    request
            );
        }

        renderHistory(
                filtered
        );
    }

    // =====================================================
    // STATUS FILTER
    // =====================================================

    private boolean matchesStatus(
            ServiceRequest request,
            String status
    ) {

        if (status == null
                ||
                status.equalsIgnoreCase(
                        "All Status"
                )) {

            return true;
        }

        if (status.equalsIgnoreCase(
                "Completed"
        )) {

            return request.isCompleted();
        }

        if (status.equalsIgnoreCase(
                "Cancelled"
        )) {

            return request.isCancelled();
        }

        return true;
    }

    // =====================================================
    // VEHICLE FILTER
    // =====================================================

    private boolean matchesVehicle(
            ServiceRequest request,
            String selectedVehicle
    ) {

        if (selectedVehicle == null
                ||
                selectedVehicle.equalsIgnoreCase(
                        "All Vehicles"
                )) {

            return true;
        }

        String requestVehicleId =
                clean(
                        request.getVehicleId()
                );

        String requestVehicleNumber =
                clean(
                        request.getVehicleNumber()
                );

        for (Map<String, Object> vehicle :
                vehicles) {

            String display =
                    createVehicleDisplay(
                            vehicle
                    );

            if (!display.equals(
                    selectedVehicle
            )) {

                continue;
            }

            String vehicleId =
                    firstNonBlank(
                            value(
                                    vehicle,
                                    "id"
                            ),
                            value(
                                    vehicle,
                                    "vehicleId"
                            )
                    );

            String registration =
                    firstNonBlank(
                            value(
                                    vehicle,
                                    "registrationNumber"
                            ),
                            value(
                                    vehicle,
                                    "vehicleNumber"
                            )
                    );

            return sameText(
                    requestVehicleId,
                    vehicleId
            )
                    ||
                    sameText(
                            requestVehicleNumber,
                            registration
                    );
        }

        return false;
    }

    // =====================================================
    // SEARCH FILTER
    // =====================================================

    private boolean matchesSearch(
            ServiceRequest request,
            String search
    ) {

        if (search == null) {

            return true;
        }

        search =
                search.toLowerCase();

        return contains(
                request.getRequestId(),
                search
        )
                ||
                contains(
                        request.getVehicleNumber(),
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
                )
                ||
                contains(
                        request.getMechanicName(),
                        search
                )
                ||
                contains(
                        request.getDiagnosis(),
                        search
                )
                ||
                contains(
                        request.getRepairDetails(),
                        search
                );
    }

    // =====================================================
    // RENDER
    // =====================================================

    private void renderHistory(
            List<ServiceRequest> history
    ) {

        historyContainer
                .getChildren()
                .clear();

        if (history == null
                ||
                history.isEmpty()) {

            showEmpty(
                    "No Service Records",
                    "Completed or cancelled services matching the selected filters will appear here."
            );

            return;
        }

        for (ServiceRequest request :
                history) {

            historyContainer
                    .getChildren()
                    .add(
                            createHistoryCard(
                                    request
                            )
                    );
        }
    }

    // =====================================================
    // HISTORY CARD
    // =====================================================

    private VBox createHistoryCard(
            ServiceRequest request
    ) {

        VBox card =
                new VBox(13);

        card.setPadding(
                new Insets(17)
        );

        card.setStyle(
                "-fx-background-color: "
                        + SECONDARY_SURFACE
                        + ";"
                        + "-fx-border-color: "
                        + BORDER
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

        VBox titleBox =
                new VBox(4);

        Label service =
                new Label(
                        firstNonBlank(
                                request.getServiceType(),
                                "Service Request"
                        )
                );

        service.setTextFill(
                Color.web(
                        HEADING
                )
        );

        service.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        16
                )
        );

        Label requestId =
                new Label(
                        "Request ID: "
                                + firstNonBlank(
                                request.getRequestId(),
                                "-"
                        )
                );

        requestId.setTextFill(
                Color.web(
                        SECONDARY_TEXT
                )
        );

        requestId.setFont(
                Font.font(
                        "Arial",
                        9
                )
        );

        titleBox.getChildren()
                .addAll(
                        service,
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
                        request
                );

        top.getChildren()
                .addAll(
                        titleBox,
                        spacer,
                        status
                );

        // =================================================
        // DETAILS
        // =================================================

        HBox firstRow =
                new HBox(10);

        VBox vehicle =
                createInfoBox(
                        "VEHICLE",
                        firstNonBlank(
                                request.getVehicleNumber(),
                                request.getVehicleId(),
                                "Vehicle"
                        )
                );

        VBox mechanic =
                createInfoBox(
                        "MECHANIC",
                        firstNonBlank(
                                request.getMechanicName(),
                                request.getMechanicId(),
                                "Not assigned"
                        )
                );

        VBox date =
                createInfoBox(
                        request.isCompleted()
                                ? "COMPLETED"
                                : "CANCELLED",
                        formatHistoryDate(
                                request
                        )
                );

        VBox amount =
                createInfoBox(
                        "FINAL AMOUNT",
                        request.isCompleted()
                                ? formatMoney(
                                getFinalAmount(
                                        request
                                )
                        )
                                : "₹0"
                );

        HBox.setHgrow(
                vehicle,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                mechanic,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                date,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                amount,
                Priority.ALWAYS
        );

        firstRow.getChildren()
                .addAll(
                        vehicle,
                        mechanic,
                        date,
                        amount
                );

        // =================================================
        // PROBLEM + LOCATION
        // =================================================

        HBox secondRow =
                new HBox(10);

        VBox problem =
                createInfoBox(
                        "PROBLEM",
                        firstNonBlank(
                                request.getDescription(),
                                "Not provided"
                        )
                );

        VBox location =
                createInfoBox(
                        "LOCATION",
                        firstNonBlank(
                                request.getLocation(),
                                "Location not available"
                        )
                );

        HBox.setHgrow(
                problem,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                location,
                Priority.ALWAYS
        );

        secondRow.getChildren()
                .addAll(
                        problem,
                        location
                );

        // =================================================
        // ACTION
        // =================================================

        HBox actions =
                new HBox(10);

        actions.setAlignment(
                Pos.CENTER_RIGHT
        );

        Region actionSpacer =
                new Region();

        HBox.setHgrow(
                actionSpacer,
                Priority.ALWAYS
        );

        Label note =
                new Label(
                        request.isCompleted()
                                ? "Service completed successfully"
                                : "This service request was cancelled"
                );

        note.setTextFill(
                Color.web(
                        request.isCompleted()
                                ? SUCCESS
                                : ERROR
                )
        );

        note.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        10
                )
        );

        Button details =
                createSecondaryButton(
                        "View Details"
                );

        details.setOnAction(
                event ->
                        showDetails(
                                request
                        )
        );

        actions.getChildren()
                .addAll(
                        note,
                        actionSpacer,
                        details
                );

        card.getChildren()
                .addAll(
                        top,
                        new Separator(),
                        firstRow,
                        secondRow,
                        actions
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
                new Insets(10)
        );

        box.setMinWidth(
                130
        );

        box.setMaxWidth(
                Double.MAX_VALUE
        );

        box.setStyle(
                "-fx-background-color: "
                        + CARD
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
                        8
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
                        10
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
    // STATUS BADGE
    // =====================================================

    private Label createStatusBadge(
            ServiceRequest request
    ) {

        boolean completed =
                request != null
                        &&
                        request.isCompleted();

        String status =
                completed
                        ? "Completed"
                        : "Cancelled";

        String color =
                completed
                        ? SUCCESS
                        : ERROR;

        Label badge =
                new Label(
                        status
                );

        badge.setPadding(
                new Insets(
                        6,
                        11,
                        6,
                        11
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
                        10
                )
        );

        badge.setStyle(
                "-fx-background-color: "
                        + color
                        + "18;"
                        + "-fx-background-radius: 14;"
        );

        return badge;
    }

    // =====================================================
    // DETAILS
    // =====================================================

    private void showDetails(
            ServiceRequest request
    ) {

        if (request == null) {

            return;
        }

        StringBuilder text =
                new StringBuilder();

        text.append(
                "Request ID: "
        );

        text.append(
                firstNonBlank(
                        request.getRequestId(),
                        "-"
                )
        );

        text.append(
                "\nStatus: "
        );

        text.append(
                firstNonBlank(
                        request.getStatus(),
                        "-"
                )
        );

        text.append(
                "\n\nService: "
        );

        text.append(
                firstNonBlank(
                        request.getServiceType(),
                        "Service Request"
                )
        );

        text.append(
                "\nVehicle: "
        );

        text.append(
                firstNonBlank(
                        request.getVehicleNumber(),
                        request.getVehicleId(),
                        "-"
                )
        );

        text.append(
                "\nMechanic: "
        );

        text.append(
                firstNonBlank(
                        request.getMechanicName(),
                        request.getMechanicId(),
                        "Not available"
                )
        );

        text.append(
                "\nLocation: "
        );

        text.append(
                firstNonBlank(
                        request.getLocation(),
                        "Not available"
                )
        );

        text.append(
                "\n\nProblem:\n"
        );

        text.append(
                firstNonBlank(
                        request.getDescription(),
                        "-"
                )
        );

        text.append(
                "\n\nDiagnosis:\n"
        );

        text.append(
                firstNonBlank(
                        request.getDiagnosis(),
                        "-"
                )
        );

        text.append(
                "\n\nRepair Details:\n"
        );

        text.append(
                firstNonBlank(
                        request.getRepairDetails(),
                        "-"
                )
        );

        text.append(
                "\n\nEstimated Cost: "
        );

        text.append(
                formatMoney(
                        parseAmount(
                                request.getEstimatedCost()
                        )
                )
        );

        text.append(
                "\nParts Cost: "
        );

        text.append(
                formatMoney(
                        parseAmount(
                                request.getPartsCost()
                        )
                )
        );

        text.append(
                "\nLabour Cost: "
        );

        text.append(
                formatMoney(
                        parseAmount(
                                request.getLabourCost()
                        )
                )
        );

        text.append(
                "\nFinal Cost: "
        );

        text.append(
                request.isCompleted()
                        ? formatMoney(
                        getFinalAmount(
                                request
                        )
                )
                        : "₹0"
        );

        text.append(
                "\n\nDate: "
        );

        text.append(
                formatHistoryDate(
                        request
                )
        );

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(
                "RoadGuardian"
        );

        alert.setHeaderText(
                firstNonBlank(
                        request.getServiceType(),
                        "Service Details"
                )
        );

        alert.setContentText(
                text.toString()
        );

        alert.getDialogPane()
                .setPrefWidth(
                        520
                );

        alert.showAndWait();
    }

    // =====================================================
    // FINAL AMOUNT
    // =====================================================

    private double getFinalAmount(
            ServiceRequest request
    ) {

        if (request == null
                ||
                request.isCancelled()) {

            return 0.0;
        }

        double finalCost =
                parseAmount(
                        request.getFinalCost()
                );

        if (finalCost > 0) {

            return finalCost;
        }

        double parts =
                parseAmount(
                        request.getPartsCost()
                );

        double labour =
                parseAmount(
                        request.getLabourCost()
                );

        double calculated =
                parts + labour;

        if (calculated > 0) {

            return calculated;
        }

        /*
         * Older records fallback.
         */
        return parseAmount(
                request.getEstimatedCost()
        );
    }

    // =====================================================
    // HISTORY TIME
    // =====================================================

    private long getHistoryTime(
            ServiceRequest request
    ) {

        if (request == null) {

            return 0L;
        }

        if (request.isCompleted()) {

            long completed =
                    parseTime(
                            request.getCompletedDate()
                    );

            if (completed > 0) {

                return completed;
            }
        }

        if (request.isCancelled()) {

            long cancelled =
                    parseTime(
                            request.getCancelledDate()
                    );

            if (cancelled > 0) {

                return cancelled;
            }
        }

        long updated =
                parseTime(
                        request.getUpdatedAt()
                );

        if (updated > 0) {

            return updated;
        }

        return parseTime(
                request.getRequestDate()
        );
    }

    // =====================================================
    // FORMAT HISTORY DATE
    // =====================================================

    private String formatHistoryDate(
            ServiceRequest request
    ) {

        if (request == null) {

            return "-";
        }

        String date;

        if (request.isCompleted()) {

            date =
                    firstNonBlank(
                            request.getCompletedDate(),
                            request.getUpdatedAt(),
                            request.getRequestDate()
                    );

        } else {

            date =
                    firstNonBlank(
                            request.getCancelledDate(),
                            request.getUpdatedAt(),
                            request.getRequestDate()
                    );
        }

        return formatDate(
                date
        );
    }

    // =====================================================
    // FORMAT DATE
    // =====================================================

    private String formatDate(
            String value
    ) {

        String cleaned =
                clean(
                        value
                );

        if (cleaned == null) {

            return "-";
        }

        try {

            long millis =
                    Long.parseLong(
                            cleaned
                    );

            if (
                    millis > 0
                            &&
                    millis < 100000000000L
            ) {

                millis *=
                        1000L;
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
             * Old formatted Firebase values remain readable.
             */
            return cleaned;
        }
    }

    // =====================================================
    // PARSE TIME
    // =====================================================

    private long parseTime(
            String value
    ) {

        String cleaned =
                clean(
                        value
                );

        if (cleaned == null) {

            return 0L;
        }

        try {

            long number =
                    Long.parseLong(
                            cleaned
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
    // MONEY
    // =====================================================

    private double parseAmount(
            String value
    ) {

        String cleaned =
                clean(
                        value
                );

        if (cleaned == null) {

            return 0.0;
        }

        cleaned =
                cleaned
                        .replace(
                                "₹",
                                ""
                        )
                        .replace(
                                ",",
                                ""
                        )
                        .trim();

        try {

            double amount =
                    Double.parseDouble(
                            cleaned
                    );

            if (!Double.isFinite(
                    amount
            )
                    ||
                    amount < 0) {

                return 0.0;
            }

            return amount;

        } catch (Exception e) {

            return 0.0;
        }
    }

    // =====================================================
    // FORMAT MONEY
    // =====================================================

    private String formatMoney(
            double value
    ) {

        if (!Double.isFinite(
                value
        )
                ||
                value < 0) {

            value =
                    0.0;
        }

        if (value == Math.rint(
                value
        )) {

            return "₹"
                    + String.format(
                            java.util.Locale.US,
                            "%,.0f",
                            value
                    );
        }

        return "₹"
                + String.format(
                        java.util.Locale.US,
                        "%,.2f",
                        value
                );
    }

    // =====================================================
    // VEHICLE DISPLAY
    // =====================================================

    private String createVehicleDisplay(
            Map<String, Object> vehicle
    ) {

        if (vehicle == null) {

            return "Vehicle";
        }

        String name =
                firstNonBlank(
                        value(
                                vehicle,
                                "name"
                        ),
                        value(
                                vehicle,
                                "make"
                        ),
                        ""
                );

        String model =
                firstNonBlank(
                        value(
                                vehicle,
                                "model"
                        ),
                        ""
                );

        String registration =
                firstNonBlank(
                        value(
                                vehicle,
                                "registrationNumber"
                        ),
                        value(
                                vehicle,
                                "vehicleNumber"
                        ),
                        ""
                );

        String display =
                (
                        firstNonBlank(
                                name,
                                ""
                        )
                                + " "
                                + firstNonBlank(
                                model,
                                ""
                        )
                ).trim();

        if (registration != null
                &&
                !registration.isBlank()) {

            if (!display.isBlank()) {

                display +=
                        " · ";
            }

            display +=
                    registration;
        }

        return display.isBlank()
                ? "Vehicle"
                : display;
    }

    // =====================================================
    // FIELD STYLE
    // =====================================================

    private void styleField(
            TextField field
    ) {

        field.setPrefHeight(
                42
        );

        field.setStyle(
                "-fx-background-color: "
                        + CARD
                        + ";"
                        + "-fx-text-fill: "
                        + HEADING
                        + ";"
                        + "-fx-border-color: "
                        + BORDER
                        + ";"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
                        + "-fx-padding: 0 12 0 12;"
                        + "-fx-font-size: 11px;"
        );
    }

    // =====================================================
    // COMBO STYLE
    // =====================================================

    private String comboStyle() {

        return "-fx-background-color: "
                + CARD
                + ";"
                + "-fx-border-color: "
                + BORDER
                + ";"
                + "-fx-border-radius: 8;"
                + "-fx-background-radius: 8;";
    }

    // =====================================================
    // SECONDARY BUTTON
    // =====================================================

    private Button createSecondaryButton(
            String text
    ) {

        Button button =
                new Button(
                        text
                );

        button.setPadding(
                new Insets(
                        9,
                        15,
                        9,
                        15
                )
        );

        button.setStyle(
                "-fx-background-color: "
                        + CARD
                        + ";"
                        + "-fx-text-fill: "
                        + HEADING
                        + ";"
                        + "-fx-border-color: "
                        + BORDER
                        + ";"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
                        + "-fx-font-size: 10px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-cursor: hand;"
        );

        return button;
    }

    // =====================================================
    // LOADING
    // =====================================================

    private void showLoading() {

        historyContainer
                .getChildren()
                .clear();

        VBox box =
                new VBox(6);

        box.setPadding(
                new Insets(35)
        );

        box.setAlignment(
                Pos.CENTER
        );

        Label title =
                new Label(
                        "Loading service history..."
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
                        13
                )
        );

        Label subtitle =
                new Label(
                        "Reading your service requests."
                );

        subtitle.setTextFill(
                Color.web(
                        SECONDARY_TEXT
                )
        );

        subtitle.setFont(
                Font.font(
                        "Arial",
                        10
                )
        );

        box.getChildren()
                .addAll(
                        title,
                        subtitle
                );

        historyContainer
                .getChildren()
                .add(
                        box
                );
    }

    // =====================================================
    // EMPTY
    // =====================================================

    private void showEmpty(
            String title,
            String subtitle
    ) {

        if (historyContainer == null) {

            return;
        }

        historyContainer
                .getChildren()
                .clear();

        VBox box =
                new VBox(7);

        box.setPadding(
                new Insets(35)
        );

        box.setAlignment(
                Pos.CENTER
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
                        14
                )
        );

        Label subtitleLabel =
                new Label(
                        subtitle
                );

        subtitleLabel.setWrapText(
                true
        );

        subtitleLabel.setTextFill(
                Color.web(
                        SECONDARY_TEXT
                )
        );

        subtitleLabel.setFont(
                Font.font(
                        "Arial",
                        10
                )
        );

        box.getChildren()
                .addAll(
                        titleLabel,
                        subtitleLabel
                );

        historyContainer
                .getChildren()
                .add(
                        box
                );
    }

    // =====================================================
    // CURRENT CUSTOMER ID
    // =====================================================

    private String getCurrentCustomerId() {

        String email =
                clean(
                        UserSession.getUserEmail()
                );

        if (email != null) {

            return email.toLowerCase();
        }

        String userId =
                clean(
                        UserSession.getUserId()
                );

        if (userId == null) {

            return null;
        }

        return userId.contains("@")
                ? userId.toLowerCase()
                : userId;
    }

    // =====================================================
    // MAP VALUE
    // =====================================================

    private String value(
            Map<String, Object> map,
            String key
    ) {

        if (map == null
                ||
                key == null) {

            return "";
        }

        Object value =
                map.get(
                        key
                );

        if (value == null) {

            return "";
        }

        return String.valueOf(
                value
        ).trim();
    }

    // =====================================================
    // SEARCH MATCH
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
    // SAME TEXT
    // =====================================================

    private boolean sameText(
            String first,
            String second
    ) {

        first =
                clean(
                        first
                );

        second =
                clean(
                        second
                );

        return first != null
                &&
                second != null
                &&
                first.equalsIgnoreCase(
                        second
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
}