package project.ui.mechanic;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

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
import javafx.scene.shape.Circle;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import project.controller.mechanic.JobHistoryController;
import project.model.ServiceRequest;
import project.util.Theme;
import project.util.VehicleIconUtil;
import project.util.IconUtil;

import java.util.ArrayList;
import java.util.List;

public class JobHistoryPage {

    // =====================================================
    // CONTROLLER
    // =====================================================

    private final JobHistoryController controller;

    // =====================================================
    // ROOT
    // =====================================================

    private VBox mainContent;

    private VBox jobsContainer;

    // =====================================================
    // FILTERS
    // =====================================================

    private TextField searchField;

    private ComboBox<String> statusFilter;

    // =====================================================
    // STATISTICS
    // =====================================================

    private Label completedValue;

    private Label cancelledValue;

    private Label thisMonthValue;

    private Label earningsValue;

    // =====================================================
    // FIREBASE DATA
    // =====================================================

    private List<ServiceRequest> allJobs =
            new ArrayList<>();

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public JobHistoryPage() {

        controller =
                new JobHistoryController();
    }

    // =====================================================
    // GET CONTENT
    //
    // Existing public method preserved.
    // =====================================================

    public ScrollPane getContent() {

        buildPage();

        loadHistory();

        ScrollPane scrollPane =
                new ScrollPane(
                        mainContent
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

        mainContent =
                new VBox(20);

        mainContent.setPadding(
                new Insets(
                        30,
                        35,
                        40,
                        35
                )
        );

        mainContent.setStyle(
                "-fx-background-color: "
                        + Theme.BACKGROUND
                        + ";"
        );

        VBox header =
                createHeader();

        HBox statistics =
                createStatistics();

        HBox filters =
                createFilters();

        VBox historyCard =
                createHistoryCard();

        mainContent
                .getChildren()
                .addAll(
                        header,
                        statistics,
                        filters,
                        historyCard
                );
    }

    // =====================================================
    // HEADER
    // =====================================================

    private VBox createHeader() {

        VBox container =
                new VBox(5);

        HBox row =
                new HBox(15);

        row.setAlignment(
                Pos.CENTER_LEFT
        );

        VBox text =
                new VBox(5);

        Label title =
                new Label(
                        "Job History"
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
                        29
                )
        );

        Label subtitle =
                new Label(
                        "View your completed and cancelled RoadGuardian service jobs."
                );

        subtitle.setTextFill(
                Color.web(
                        Theme.SECONDARY_TEXT
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

        row.getChildren()
                .addAll(
                        text,
                        spacer,
                        refresh
                );

        container.getChildren()
                .add(
                        row
                );

        return container;
    }

    // =====================================================
    // STATISTICS
    // =====================================================

    private HBox createStatistics() {

        HBox row =
                new HBox(14);

        completedValue =
                new Label("0");

        cancelledValue =
                new Label("0");

        thisMonthValue =
                new Label("0");

        earningsValue =
                new Label("₹0");

        VBox completed =
                createStatCard(
                        "COMPLETED JOBS",
                        completedValue,
                        "Successfully finished",
                        Theme.SUCCESS
                );

        VBox cancelled =
                createStatCard(
                        "CANCELLED JOBS",
                        cancelledValue,
                        "Cancelled requests",
                        Theme.ERROR
                );

        VBox month =
                createStatCard(
                        "THIS MONTH",
                        thisMonthValue,
                        "Completed this month",
                        Theme.INFO
                );

        VBox earnings =
                createStatCard(
                        "TOTAL EARNINGS",
                        earningsValue,
                        "Completed jobs only",
                        Theme.PRIMARY
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
                month,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                earnings,
                Priority.ALWAYS
        );

        row.getChildren()
                .addAll(
                        completed,
                        cancelled,
                        month,
                        earnings
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
                105
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

        value.setTextFill(
                Color.web(
                        color
                )
        );

        value.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        22
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
                "Search customer, vehicle, service, problem, location or request ID..."
        );

        searchField.setPrefWidth(
                430
        );

        searchField.setPrefHeight(
                42
        );

        searchField.setStyle(
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
                        + "-fx-padding: 0 13 0 13;"
                        + "-fx-font-size: 11px;"
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

        statusFilter.setPrefHeight(
                42
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

        Label source =
                new Label(
                        "● Active"
                );

        source.setTextFill(
                Color.web(
                        Theme.SUCCESS
                )
        );

        source.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        9
                )
        );

        row.getChildren()
                .addAll(
                        searchField,
                        statusFilter,
                        spacer,
                        source
                );

        return row;
    }

    // =====================================================
    // HISTORY CARD
    // =====================================================

    private VBox createHistoryCard() {

        VBox card =
                new VBox(12);

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

        HBox titleRow =
                new HBox();

        titleRow.setAlignment(
                Pos.CENTER_LEFT
        );

        VBox text =
                new VBox(4);

        Label title =
                new Label(
                        "Service Job Records"
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
                        19
                )
        );

        Label subtitle =
                new Label(
                        "History is read directly from completed or cancelled serviceRequests."
                );

        subtitle.setTextFill(
                Color.web(
                        Theme.SECONDARY_TEXT
                )
        );

        subtitle.setFont(
                Font.font(
                        "Arial",
                        10
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

        titleRow.getChildren()
                .addAll(
                        text,
                        spacer
                );

        jobsContainer =
                new VBox(12);

        card.getChildren()
                .addAll(
                        titleRow,
                        new Separator(),
                        jobsContainer
                );

        return card;
    }

    // =====================================================
    // LOAD FIREBASE HISTORY
    // =====================================================

    private void loadHistory() {

        if (jobsContainer == null) {

            return;
        }

        showLoading();

        try {

            List<ServiceRequest> jobs =
                    controller.refreshHistory();

            allJobs.clear();

            if (jobs != null) {

                for (ServiceRequest job :
                        jobs) {

                    if (job != null
                            &&
                            controller.isHistoryItem(
                                    job
                            )) {

                        allJobs.add(
                                job
                        );
                    }
                }
            }

            updateStatistics();

            applyFilters();

        } catch (Exception e) {

            allJobs.clear();

            updateStatistics();

            showMessage(
                    "Unable to load job history",
                    "Check your Firebase connection and refresh again."
            );

            System.err.println(
                    "JobHistoryPage load error: "
                            + e.getMessage()
            );
        }
    }

    // =====================================================
    // UPDATE STATISTICS
    // =====================================================

    private void updateStatistics() {

        if (completedValue != null) {

            completedValue.setText(
                    String.valueOf(
                            controller.getCompletedJobCount()
                    )
            );
        }

        if (cancelledValue != null) {

            cancelledValue.setText(
                    String.valueOf(
                            controller.getCancelledJobCount()
                    )
            );
        }

        if (thisMonthValue != null) {

            thisMonthValue.setText(
                    String.valueOf(
                            controller.getThisMonthJobCount()
                    )
            );
        }

        if (earningsValue != null) {

            earningsValue.setText(
                    controller.getTotalEarningsDisplay()
            );
        }
    }

    // =====================================================
    // APPLY SEARCH + STATUS FILTER
    // =====================================================

    private void applyFilters() {

        if (jobsContainer == null) {

            return;
        }

        String search =
                searchField == null
                        ? ""
                        : clean(
                        searchField.getText()
                );

        String status =
                statusFilter == null
                        ? "All Status"
                        : statusFilter.getValue();

        List<ServiceRequest> filtered =
                new ArrayList<>();

        for (ServiceRequest job :
                allJobs) {

            if (job == null) {

                continue;
            }

            if (!matchesStatus(
                    job,
                    status
            )) {

                continue;
            }

            if (!matchesSearch(
                    job,
                    search
            )) {

                continue;
            }

            filtered.add(
                    job
            );
        }

        renderJobs(
                filtered
        );
    }

    // =====================================================
    // SEARCH MATCH
    // =====================================================

    private boolean matchesSearch(
            ServiceRequest job,
            String search
    ) {

        if (search == null
                ||
                search.isBlank()) {

            return true;
        }

        search =
                search.toLowerCase();

        return contains(
                job.getRequestId(),
                search
        )
                ||
                contains(
                        controller.getCustomerDisplay(
                                job
                        ),
                        search
                )
                ||
                contains(
                        controller.getVehicleDisplay(
                                job
                        ),
                        search
                )
                ||
                contains(
                        controller.getServiceTypeDisplay(
                                job
                        ),
                        search
                )
                ||
                contains(
                        controller.getProblemDisplay(
                                job
                        ),
                        search
                )
                ||
                contains(
                        controller.getLocationDisplay(
                                job
                        ),
                        search
                )
                ||
                contains(
                        controller.getDiagnosisDisplay(
                                job
                        ),
                        search
                )
                ||
                contains(
                        controller.getRepairDetailsDisplay(
                                job
                        ),
                        search
                );
    }

    // =====================================================
    // STATUS MATCH
    // =====================================================

    private boolean matchesStatus(
            ServiceRequest job,
            String selectedStatus
    ) {

        if (selectedStatus == null
                ||
                selectedStatus.equalsIgnoreCase(
                        "All Status"
                )
                ||
                selectedStatus.equalsIgnoreCase(
                        "All"
                )) {

            return true;
        }

        if (selectedStatus.equalsIgnoreCase(
                "Completed"
        )) {

            return controller.isCompleted(
                    job
            );
        }

        if (selectedStatus.equalsIgnoreCase(
                "Cancelled"
        )) {

            return controller.isCancelled(
                    job
            );
        }

        return false;
    }

    // =====================================================
    // RENDER JOBS
    // =====================================================

    private void renderJobs(
            List<ServiceRequest> jobs
    ) {

        jobsContainer
                .getChildren()
                .clear();

        if (jobs == null
                ||
                jobs.isEmpty()) {

            showMessage(
                    "No jobs found",
                    "Completed and cancelled jobs matching the selected filters will appear here."
            );

            return;
        }

        for (ServiceRequest job :
                jobs) {

            jobsContainer
                    .getChildren()
                    .add(
                            createJobCard(
                                    job
                            )
                    );
        }
    }

    // =====================================================
    // JOB CARD
    // =====================================================

    private VBox createJobCard(
            ServiceRequest job
    ) {

        VBox card =
                new VBox(13);

        card.setPadding(
                new Insets(17)
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

        Circle avatarCircle =
                new Circle(
                        22
                );

        avatarCircle.setFill(
                Color.web(
                        Theme.CARD
                )
        );

        // UI-only: show a user silhouette instead of customer initials.
        var userIcon =
                IconUtil.createUserIcon(
                        0.70,
                        Theme.INFO
                );

        javafx.scene.layout.StackPane avatar =
                new javafx.scene.layout.StackPane(
                        avatarCircle,
                        userIcon
                );

        VBox customerInfo =
                new VBox(3);

        Label customer =
                new Label(
                        controller.getCustomerDisplay(
                                job
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
                        15
                )
        );

        Label requestId =
                new Label(
                        "Request ID: "
                                + controller.getRequestIdDisplay(
                                job
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
                        9
                )
        );

        customerInfo.getChildren()
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
                        controller.getStatusDisplay(
                                job
                        )
                );

        top.getChildren()
                .addAll(
                        avatar,
                        customerInfo,
                        spacer,
                        status
                );

        // =================================================
        // DETAILS ROW
        // =================================================

        HBox details =
                new HBox(10);

        VBox vehicle =
                createInfoBox(
                        "VEHICLE",
                        controller.getVehicleDisplay(
                                job
                        )
                );

        VBox service =
                createInfoBox(
                        "SERVICE",
                        controller.getServiceTypeDisplay(
                                job
                        )
                );

        VBox date =
                createInfoBox(
                        controller.isCancelled(job)
                                ? "CANCELLED DATE"
                                : "COMPLETED DATE",
                        controller.getDateDisplay(
                                job
                        )
                );

        VBox amount =
                createInfoBox(
                        "FINAL AMOUNT",
                        controller.getAmountDisplay(
                                job
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
                date,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                amount,
                Priority.ALWAYS
        );

        details.getChildren()
                .addAll(
                        vehicle,
                        service,
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
                        controller.getProblemDisplay(
                                job
                        )
                );

        VBox location =
                createInfoBox(
                        "LOCATION",
                        controller.getLocationDisplay(
                                job
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
        // ACTIONS
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

        Label earningInfo =
                new Label();

        if (controller.isCompleted(
                job
        )) {

            earningInfo.setText(
                    "Earning: "
                            + controller.getAmountDisplay(
                            job
                    )
            );

            earningInfo.setTextFill(
                    Color.web(
                            Theme.SUCCESS
                    )
            );

        } else {

            earningInfo.setText(
                    "Cancelled jobs do not count toward earnings."
            );

            earningInfo.setTextFill(
                    Color.web(
                            Theme.SECONDARY_TEXT
                    )
            );
        }

        earningInfo.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        10
                )
        );

        Button detailsButton =
                createSecondaryButton(
                        "View Details"
                );

        detailsButton.setOnAction(
                event ->
                        showJobDetails(
                                job
                        )
        );

        actions.getChildren()
                .addAll(
                        earningInfo,
                        actionSpacer,
                        detailsButton
                );

        card.getChildren()
                .addAll(
                        top,
                        new Separator(),
                        details,
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
                        8
                )
        );

        Label valueLabel =
                new Label(
                        safe(
                                value
                        )
                );

        valueLabel.setWrapText(
                true
        );

        valueLabel.setTextFill(
                Color.web(
                        Theme.TEXT
                )
        );

        valueLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        10
                )
        );

        if ("VEHICLE".equalsIgnoreCase(title)) {
            valueLabel.setGraphic(
                    VehicleIconUtil.createCarIcon(
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
    // VIEW JOB DETAILS
    // =====================================================

    private void showJobDetails(
            ServiceRequest job
    ) {

        if (job == null) {

            return;
        }

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(
                "RoadGuardian"
        );

        alert.setHeaderText(
                controller.getServiceTypeDisplay(
                        job
                )
                        + " - "
                        + controller.getCustomerDisplay(
                        job
                )
        );

        StringBuilder details =
                new StringBuilder();

        details.append(
                "Request ID: "
        );

        details.append(
                controller.getRequestIdDisplay(
                        job
                )
        );

        details.append(
                "\nStatus: "
        );

        details.append(
                controller.getStatusDisplay(
                        job
                )
        );

        details.append(
                "\n\nCustomer: "
        );

        details.append(
                controller.getCustomerDisplay(
                        job
                )
        );

        details.append(
                "\nVehicle: "
        );

        details.append(
                controller.getVehicleDisplay(
                        job
                )
        );

        details.append(
                "\nService: "
        );

        details.append(
                controller.getServiceTypeDisplay(
                        job
                )
        );

        details.append(
                "\nProblem: "
        );

        details.append(
                controller.getProblemDisplay(
                        job
                )
        );

        details.append(
                "\nLocation: "
        );

        details.append(
                controller.getLocationDisplay(
                        job
                )
        );

        details.append(
                "\nDate: "
        );

        details.append(
                controller.getDateDisplay(
                        job
                )
        );

        details.append(
                "\n\nDiagnosis:\n"
        );

        details.append(
                controller.getDiagnosisDisplay(
                        job
                )
        );

        details.append(
                "\n\nRepair Details:\n"
        );

        details.append(
                controller.getRepairDetailsDisplay(
                        job
                )
        );

        details.append(
                "\n\nEstimated Cost: "
        );

        details.append(
                controller.getEstimatedCostDisplay(
                        job
                )
        );

        details.append(
                "\nParts Cost: "
        );

        details.append(
                controller.getPartsCostDisplay(
                        job
                )
        );

        details.append(
                "\nLabour Cost: "
        );

        details.append(
                controller.getLabourCostDisplay(
                        job
                )
        );

        details.append(
                "\nFinal Cost: "
        );

        details.append(
                controller.getFinalCostDisplay(
                        job
                )
        );

        if (controller.isCancelled(
                job
        )) {

            details.append(
                    "\n\nEarnings: ₹0"
            );

        } else {

            details.append(
                    "\n\nMechanic Earning: "
            );

            details.append(
                    controller.getAmountDisplay(
                            job
                    )
            );
        }

        alert.setContentText(
                details.toString()
        );

        alert.getDialogPane()
                .setPrefWidth(
                        520
                );

        alert.showAndWait();
    }

    // =====================================================
    // STATUS BADGE
    // =====================================================

    private Label createStatusBadge(
            String status
    ) {

        status =
                safe(
                        status
                );

        String textColor;
        String background;

        if (status.equalsIgnoreCase(
                "Completed"
        )) {

            textColor =
                    Theme.SUCCESS;

            background =
                    Theme.SUCCESS_BG;

        } else if (status.equalsIgnoreCase(
                "Cancelled"
        )) {

            textColor =
                    Theme.ERROR;

            background =
                    Theme.ERROR_BG;

        } else {

            textColor =
                    Theme.INFO;

            background =
                    Theme.INFO_BG;
        }

        Label label =
                new Label(
                        status
                );

        label.setPadding(
                new Insets(
                        6,
                        11,
                        6,
                        11
                )
        );

        label.setTextFill(
                Color.web(
                        textColor
                )
        );

        label.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        9
                )
        );

        label.setStyle(
                "-fx-background-color: "
                        + background
                        + ";"
                        + "-fx-background-radius: 14;"
        );

        return label;
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

        return button;
    }

    // =====================================================
    // LOADING
    // =====================================================

    private void showLoading() {

        if (jobsContainer == null) {

            return;
        }

        jobsContainer
                .getChildren()
                .clear();

        VBox box =
                new VBox(7);

        box.setAlignment(
                Pos.CENTER
        );

        box.setPadding(
                new Insets(35)
        );

        Label title =
                new Label(
                        "Loading job history..."
                );

        title.setTextFill(
                Color.web(
                        Theme.TEXT
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
                        "Reading completed and cancelled service requests from Firebase."
                );

        subtitle.setTextFill(
                Color.web(
                        Theme.SECONDARY_TEXT
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

        jobsContainer
                .getChildren()
                .add(
                        box
                );
    }

    // =====================================================
    // EMPTY / ERROR MESSAGE
    // =====================================================

    private void showMessage(
            String title,
            String subtitle
    ) {

        if (jobsContainer == null) {

            return;
        }

        jobsContainer
                .getChildren()
                .clear();

        VBox box =
                new VBox(7);

        box.setAlignment(
                Pos.CENTER
        );

        box.setPadding(
                new Insets(35)
        );

        Label titleLabel =
                new Label(
                        title
                );

        titleLabel.setTextFill(
                Color.web(
                        Theme.TEXT
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
                        Theme.SECONDARY_TEXT
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

        jobsContainer
                .getChildren()
                .add(
                        box
                );
    }

    // =====================================================
    // PUBLIC REFRESH
    //
    // Existing MechanicDashboard uses this.
    // =====================================================

    public void refresh() {

        if (jobsContainer != null) {

            loadHistory();
        }
    }

    // =====================================================
    // INITIALS
    // =====================================================

    private String getInitials(
            String name
    ) {

        String cleaned =
                clean(
                        name
                );

        if (cleaned == null) {

            return "?";
        }

        String[] parts =
                cleaned.split(
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
                                search.toLowerCase()
                        );
    }

    // =====================================================
    // SAFE
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