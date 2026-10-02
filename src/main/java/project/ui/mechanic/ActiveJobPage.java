package project.ui.mechanic;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import project.controller.mechanic.ActiveJobController;
import project.model.ServiceRequest;
import project.util.Theme;
import project.util.IconUtil;

import java.util.Optional;

public class ActiveJobPage {

    // =====================================================
    // CONTROLLER
    // =====================================================

    private final ActiveJobController controller;

    // =====================================================
    // CURRENT FIREBASE JOB
    // =====================================================

    private ServiceRequest currentJob;

    // =====================================================
    // ROOT
    // =====================================================

    private VBox mainContent;

    // =====================================================
    // SUMMARY
    // =====================================================

    private Label requestIdValueLabel;
    private Label customerValueLabel;
    private Label vehicleValueLabel;
    private Label serviceValueLabel;
    private Label problemValueLabel;
    private Label locationValueLabel;

    // =====================================================
    // STATUS
    // =====================================================

    private Label jobStatusLabel;
    private Label progressTextLabel;

    // =====================================================
    // WORK FORM
    // =====================================================

    private TextArea diagnosisField;
    private TextArea repairField;

    private TextField estimatedCostField;
    private TextField partsCostField;
    private TextField labourCostField;

    private Label totalAmountLabel;

    // =====================================================
    // BUTTONS
    // =====================================================

    private Button refreshButton;

    private Button navigationButton;
    private Button arrivedButton;
    private Button startRepairButton;

    private Button saveDetailsButton;
    private Button completeJobButton;

    // =====================================================
    // FALLBACK DATA
    //
    // Existing compatibility for:
    //
    // setJob(
    // customer,
    // vehicle,
    // problem,
    // type,
    // distance,
    // amount
    // )
    //
    // This fallback is DISPLAY ONLY.
    // Real actions always need a Firebase requestId.
    // =====================================================

    private String fallbackCustomer =
            "No Customer";

    private String fallbackVehicle =
            "No Vehicle";

    private String fallbackProblem =
            "No Service Request";

    private String fallbackType =
            "Service Request";

    private String fallbackLocation =
            "Location not available";

    private String fallbackAmount =
            "₹0";

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public ActiveJobPage() {

        controller =
                new ActiveJobController();
    }

    // =====================================================
    // GET CONTENT
    // =====================================================

    public ScrollPane getContent() {

        buildPage();

        loadActiveJob();

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

        VBox summary =
                createSummarySection();

        HBox progress =
                createProgressSection();

        HBox workArea =
                new HBox(20);

        VBox diagnosisPanel =
                createDiagnosisPanel();

        VBox costPanel =
                createCostPanel();

        HBox.setHgrow(
                diagnosisPanel,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                costPanel,
                Priority.ALWAYS
        );

        diagnosisPanel.setMaxWidth(
                Double.MAX_VALUE
        );

        costPanel.setMaxWidth(
                Double.MAX_VALUE
        );

        workArea
                .getChildren()
                .addAll(
                        diagnosisPanel,
                        costPanel
                );

        VBox completion =
                createCompletionPanel();

        mainContent
                .getChildren()
                .addAll(
                        header,
                        summary,
                        progress,
                        workArea,
                        completion
                );
    }

    // =====================================================
    // HEADER
    // =====================================================

    private VBox createHeader() {

        VBox header =
                new VBox(6);

        HBox titleRow =
                new HBox(15);

        titleRow.setAlignment(
                Pos.CENTER_LEFT
        );

        VBox text =
                new VBox(5);

        Label title =
                new Label(
                        "Active Job"
                );

        title.setStyle(
                "-fx-font-size: 29px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: "
                        + Theme.HEADING
                        + ";"
        );

        Label subtitle =
                new Label(
                        "Manage your accepted service request, repair work and final cost."
                );

        subtitle.setStyle(
                "-fx-font-size: 13px;"
                        + "-fx-text-fill: "
                        + Theme.SECONDARY_TEXT
                        + ";"
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

        refreshButton =
                new Button(
                        "Refresh"
                );

        styleSecondaryButton(
                refreshButton
        );

        refreshButton.setOnAction(
                event ->
                        loadActiveJob()
        );

        titleRow.getChildren()
                .addAll(
                        text,
                        spacer,
                        refreshButton
                );

        header.getChildren()
                .add(
                        titleRow
                );

        return header;
    }

    // =====================================================
    // SUMMARY SECTION
    // =====================================================

    private VBox createSummarySection() {

        VBox card =
                new VBox(15);

        card.setPadding(
                new Insets(20)
        );

        card.setStyle(
                cardStyle()
        );

        Label title =
                new Label(
                        "Job Information"
                );

        title.setStyle(
                "-fx-font-size: 17px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: "
                        + Theme.TEXT
                        + ";"
        );

        HBox firstRow =
                new HBox(14);

        VBox requestId =
                createSummaryItem(
                        "REQUEST ID",
                        "-"
                );

        VBox customer =
                createSummaryItem(
                        "CUSTOMER",
                        fallbackCustomer
                );

        VBox vehicle =
                createSummaryItem(
                        "VEHICLE",
                        fallbackVehicle
                );

        requestIdValueLabel =
                getSummaryValueLabel(
                        requestId
                );

        customerValueLabel =
                getSummaryValueLabel(
                        customer
                );

        vehicleValueLabel =
                getSummaryValueLabel(
                        vehicle
                );

        HBox.setHgrow(
                requestId,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                customer,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                vehicle,
                Priority.ALWAYS
        );

        firstRow.getChildren()
                .addAll(
                        requestId,
                        customer,
                        vehicle
                );

        HBox secondRow =
                new HBox(14);

        VBox service =
                createSummaryItem(
                        "SERVICE",
                        fallbackType
                );

        VBox problem =
                createSummaryItem(
                        "PROBLEM",
                        fallbackProblem
                );

        VBox location =
                createSummaryItem(
                        "LOCATION",
                        fallbackLocation
                );

        serviceValueLabel =
                getSummaryValueLabel(
                        service
                );

        problemValueLabel =
                getSummaryValueLabel(
                        problem
                );

        locationValueLabel =
                getSummaryValueLabel(
                        location
                );

        HBox.setHgrow(
                service,
                Priority.ALWAYS
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
                        service,
                        problem,
                        location
                );

        card.getChildren()
                .addAll(
                        title,
                        new Separator(),
                        firstRow,
                        secondRow
                );

        return card;
    }

    // =====================================================
    // SUMMARY ITEM
    // =====================================================

    private VBox createSummaryItem(
            String title,
            String value
    ) {

        VBox box =
                new VBox(5);

        box.setPadding(
                new Insets(11)
        );

        box.setMaxWidth(
                Double.MAX_VALUE
        );

        box.setStyle(
                "-fx-background-color: "
                        + Theme.SECONDARY_SURFACE
                        + ";"
                        + "-fx-background-radius: 9;"
        );

        Label titleLabel =
                new Label(
                        title
                );

        titleLabel.setStyle(
                "-fx-font-size: 9px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: "
                        + Theme.SECONDARY_TEXT
                        + ";"
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

        valueLabel.setStyle(
                "-fx-font-size: 12px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: "
                        + Theme.TEXT
                        + ";"
        );

        // UI-only: use shared vector icons for customer and vehicle representations.
        if ("CUSTOMER".equalsIgnoreCase(title)) {
            valueLabel.setGraphic(
                    IconUtil.createUserIcon(
                            0.55,
                            Theme.INFO
                    )
            );
            valueLabel.setGraphicTextGap(7);
        } else if ("VEHICLE".equalsIgnoreCase(title)) {
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
    // GET SUMMARY VALUE LABEL
    // =====================================================

    private Label getSummaryValueLabel(
            VBox box
    ) {

        if (box == null
                ||
                box.getChildren().size() < 2) {

            return null;
        }

        return (Label)
                box.getChildren()
                        .get(1);
    }

    // =====================================================
    // PROGRESS SECTION
    // =====================================================

    private HBox createProgressSection() {

        HBox container =
                new HBox(12);

        container.setAlignment(
                Pos.CENTER_LEFT
        );

        container.setPadding(
                new Insets(18)
        );

        container.setStyle(
                cardStyle()
        );

        VBox text =
                new VBox(5);

        jobStatusLabel =
                new Label(
                        "Loading..."
                );

        jobStatusLabel.setStyle(
                "-fx-font-size: 14px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: "
                        + Theme.INFO
                        + ";"
        );

        progressTextLabel =
                new Label(
                        "Checking Firebase for an active job..."
                );

        progressTextLabel.setWrapText(
                true
        );

        progressTextLabel.setStyle(
                "-fx-font-size: 10px;"
                        + "-fx-text-fill: "
                        + Theme.SECONDARY_TEXT
                        + ";"
        );

        text.getChildren()
                .addAll(
                        jobStatusLabel,
                        progressTextLabel
                );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        navigationButton =
                new Button(
                        "Start Navigation"
                );

        styleSecondaryButton(
                navigationButton
        );

        navigationButton.setOnAction(
                event ->
                        startNavigation()
        );

        arrivedButton =
                new Button(
                        "Mark Arrived"
                );

        styleSecondaryButton(
                arrivedButton
        );

        arrivedButton.setOnAction(
                event ->
                        markArrived()
        );

        startRepairButton =
                new Button(
                        "Start Repair"
                );

        stylePrimaryButton(
                startRepairButton
        );

        startRepairButton.setOnAction(
                event ->
                        startRepair()
        );

        container.getChildren()
                .addAll(
                        text,
                        spacer,
                        navigationButton,
                        arrivedButton,
                        startRepairButton
                );

        return container;
    }

    // =====================================================
    // DIAGNOSIS PANEL
    // =====================================================

    private VBox createDiagnosisPanel() {

        VBox panel =
                new VBox(13);

        panel.setPadding(
                new Insets(20)
        );

        panel.setStyle(
                cardStyle()
        );

        Label title =
                new Label(
                        "Diagnosis & Repair"
                );

        title.setStyle(
                "-fx-font-size: 17px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: "
                        + Theme.TEXT
                        + ";"
        );

        Label subtitle =
                new Label(
                        "Enter the actual diagnosis and work performed on the customer's vehicle."
                );

        subtitle.setWrapText(
                true
        );

        subtitle.setStyle(
                "-fx-font-size: 10px;"
                        + "-fx-text-fill: "
                        + Theme.SECONDARY_TEXT
                        + ";"
        );

        Label diagnosisLabel =
                createFieldLabel(
                        "Diagnosis"
                );

        diagnosisField =
                new TextArea();

        diagnosisField.setPromptText(
                "Example: Battery discharged, alternator charging normally..."
        );

        diagnosisField.setPrefRowCount(
                5
        );

        diagnosisField.setWrapText(
                true
        );

        styleTextArea(
                diagnosisField
        );

        Label repairLabel =
                createFieldLabel(
                        "Repair / Work Performed"
                );

        repairField =
                new TextArea();

        repairField.setPromptText(
                "Describe the repair work completed..."
        );

        repairField.setPrefRowCount(
                5
        );

        repairField.setWrapText(
                true
        );

        styleTextArea(
                repairField
        );

        panel.getChildren()
                .addAll(
                        title,
                        subtitle,
                        new Separator(),
                        diagnosisLabel,
                        diagnosisField,
                        repairLabel,
                        repairField
                );

        return panel;
    }

    // =====================================================
    // COST PANEL
    // =====================================================

    private VBox createCostPanel() {

        VBox panel =
                new VBox(13);

        panel.setPadding(
                new Insets(20)
        );

        panel.setStyle(
                cardStyle()
        );

        Label title =
                new Label(
                        "Job Cost"
                );

        title.setStyle(
                "-fx-font-size: 17px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: "
                        + Theme.TEXT
                        + ";"
        );

        Label subtitle =
                new Label(
                        "Save real repair cost in the same service request."
                );

        subtitle.setWrapText(
                true
        );

        subtitle.setStyle(
                "-fx-font-size: 10px;"
                        + "-fx-text-fill: "
                        + Theme.SECONDARY_TEXT
                        + ";"
        );

        Label estimatedLabel =
                createFieldLabel(
                        "Estimated Cost (₹)"
                );

        estimatedCostField =
                new TextField();

        estimatedCostField.setPromptText(
                "0"
        );

        styleTextField(
                estimatedCostField
        );

        Label partsLabel =
                createFieldLabel(
                        "Parts Cost (₹)"
                );

        partsCostField =
                new TextField();

        partsCostField.setPromptText(
                "0"
        );

        styleTextField(
                partsCostField
        );

        Label labourLabel =
                createFieldLabel(
                        "Labour Cost (₹)"
                );

        labourCostField =
                new TextField();

        labourCostField.setPromptText(
                "0"
        );

        styleTextField(
                labourCostField
        );

        partsCostField
                .textProperty()
                .addListener(
                        (
                                observable,
                                oldValue,
                                newValue
                        ) ->
                                updateTotal()
                );

        labourCostField
                .textProperty()
                .addListener(
                        (
                                observable,
                                oldValue,
                                newValue
                        ) ->
                                updateTotal()
                );

        VBox totalBox =
                new VBox(5);

        totalBox.setPadding(
                new Insets(14)
        );

        totalBox.setStyle(
                "-fx-background-color: "
                        + Theme.SECONDARY_SURFACE
                        + ";"
                        + "-fx-background-radius: 10;"
        );

        Label totalTitle =
                new Label(
                        "FINAL COST"
                );

        totalTitle.setStyle(
                "-fx-font-size: 9px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: "
                        + Theme.SECONDARY_TEXT
                        + ";"
        );

        totalAmountLabel =
                new Label(
                        "₹0"
                );

        totalAmountLabel.setStyle(
                "-fx-font-size: 24px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: "
                        + Theme.SUCCESS
                        + ";"
        );

        Label totalInfo =
                new Label(
                        "Parts Cost + Labour Cost"
                );

        totalInfo.setStyle(
                "-fx-font-size: 9px;"
                        + "-fx-text-fill: "
                        + Theme.SECONDARY_TEXT
                        + ";"
        );

        totalBox.getChildren()
                .addAll(
                        totalTitle,
                        totalAmountLabel,
                        totalInfo
                );

        panel.getChildren()
                .addAll(
                        title,
                        subtitle,
                        new Separator(),

                        estimatedLabel,
                        estimatedCostField,

                        partsLabel,
                        partsCostField,

                        labourLabel,
                        labourCostField,

                        totalBox
                );

        return panel;
    }

    // =====================================================
    // COMPLETION PANEL
    // =====================================================

    private VBox createCompletionPanel() {

        VBox panel =
                new VBox(13);

        panel.setPadding(
                new Insets(20)
        );

        panel.setStyle(
                cardStyle()
        );

        HBox row =
                new HBox(15);

        row.setAlignment(
                Pos.CENTER_LEFT
        );

        VBox text =
                new VBox(4);

        Label title =
                new Label(
                        "Job Actions"
                );

        title.setStyle(
                "-fx-font-size: 17px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: "
                        + Theme.TEXT
                        + ";"
        );

        Label subtitle =
                new Label(
                        "Save work during repair or complete the service when all work is finished."
                );

        subtitle.setWrapText(
                true
        );

        subtitle.setStyle(
                "-fx-font-size: 10px;"
                        + "-fx-text-fill: "
                        + Theme.SECONDARY_TEXT
                        + ";"
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

        saveDetailsButton =
                new Button(
                        "Save Work Details"
                );

        styleSecondaryButton(
                saveDetailsButton
        );

        saveDetailsButton.setOnAction(
                event ->
                        saveWorkDetails()
        );

        completeJobButton =
                new Button(
                        "Complete Job"
                );

        completeJobButton.setPadding(
                new Insets(
                        10,
                        18,
                        10,
                        18
                )
        );

        completeJobButton.setStyle(
                "-fx-background-color: "
                        + Theme.SUCCESS
                        + ";"
                        + "-fx-text-fill: white;"
                        + "-fx-background-radius: 8;"
                        + "-fx-font-size: 11px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-cursor: hand;"
        );

        completeJobButton.setOnAction(
                event ->
                        completeJob()
        );

        row.getChildren()
                .addAll(
                        text,
                        spacer,
                        saveDetailsButton,
                        completeJobButton
                );

        panel.getChildren()
                .add(
                        row
                );

        return panel;
    }

    // =====================================================
    // LOAD ACTIVE JOB
    // =====================================================

    private void loadActiveJob() {

        if (mainContent == null) {

            return;
        }

        setLoadingState();

        currentJob =
                controller
                        .refreshCurrentActiveJob();

        if (currentJob == null) {

            showNoActiveJob();

            return;
        }

        populateJob(
                currentJob
        );
    }

    // =====================================================
    // LOADING STATE
    // =====================================================

    private void setLoadingState() {

        setLabel(
                jobStatusLabel,
                "Loading..."
        );

        setLabel(
                progressTextLabel,
                "Reading latest service request from Firebase..."
        );

        if (refreshButton != null) {

            refreshButton.setDisable(
                    true
            );
        }
    }

    // =====================================================
    // POPULATE FIREBASE JOB
    // =====================================================

    private void populateJob(
            ServiceRequest job
    ) {

        if (job == null) {

            showNoActiveJob();

            return;
        }

        // =================================================
        // SUMMARY
        // =================================================

        setLabel(
                requestIdValueLabel,
                job.getRequestId()
        );

        setLabel(
                customerValueLabel,
                controller.getCustomerName(
                        job
                )
        );

        setLabel(
                vehicleValueLabel,
                controller.getVehicleDisplay(
                        job
                )
        );

        setLabel(
                serviceValueLabel,
                controller.getServiceTypeDisplay(
                        job
                )
        );

        setLabel(
                problemValueLabel,
                controller.getProblemDisplay(
                        job
                )
        );

        setLabel(
                locationValueLabel,
                controller.getLocationDisplay(
                        job
                )
        );

        // =================================================
        // FORM
        // =================================================

        if (diagnosisField != null) {

            diagnosisField.setText(
                    controller.getDiagnosis(
                            job
                    )
            );
        }

        if (repairField != null) {

            repairField.setText(
                    controller.getRepairDetails(
                            job
                    )
            );
        }

        if (estimatedCostField != null) {

            estimatedCostField.setText(
                    cleanMoneyForField(
                            job.getEstimatedCost()
                    )
            );
        }

        if (partsCostField != null) {

            partsCostField.setText(
                    controller.getPartsCostDisplay(
                            job
                    )
            );
        }

        if (labourCostField != null) {

            labourCostField.setText(
                    controller.getLabourCostDisplay(
                            job
                    )
            );
        }

        updateTotal();

        updateStatusUI(
                job
        );

        if (refreshButton != null) {

            refreshButton.setDisable(
                    false
            );
        }
    }

    // =====================================================
    // UPDATE STATUS UI
    // =====================================================

    private void updateStatusUI(
            ServiceRequest job
    ) {

        if (job == null) {

            showNoActiveJob();

            return;
        }

        String status =
                controller.getStatusDisplay(
                        job
                );

        setLabel(
                jobStatusLabel,
                status
        );

        // =================================================
        // ACCEPTED
        // =================================================

        if (controller.canStartRepair(
                job
        )) {

            if (jobStatusLabel != null) {

                jobStatusLabel.setStyle(
                        "-fx-font-size: 14px;"
                                + "-fx-font-weight: bold;"
                                + "-fx-text-fill: "
                                + Theme.INFO
                                + ";"
                );
            }

            setLabel(
                    progressTextLabel,
                    "Request accepted. Navigate to the customer or start the repair when ready."
            );

            if (startRepairButton != null) {

                startRepairButton.setDisable(
                        false
                );

                startRepairButton.setText(
                        "Start Repair"
                );
            }

            if (navigationButton != null) {

                navigationButton.setDisable(
                        false
                );
            }

            if (arrivedButton != null) {

                arrivedButton.setDisable(
                        false
                );
            }

            /*
             * Accepted job may contain pre-filled diagnosis
             * or estimate, but repair completion is locked
             * until In Progress.
             */
            setWorkFieldsDisabled(
                    false
            );

            if (saveDetailsButton != null) {

                saveDetailsButton.setDisable(
                        false
                );
            }

            if (completeJobButton != null) {

                completeJobButton.setDisable(
                        true
                );
            }

            return;
        }

        // =================================================
        // IN PROGRESS
        // =================================================

        if (controller.isRepairInProgress(
                job
        )) {

            if (jobStatusLabel != null) {

                jobStatusLabel.setStyle(
                        "-fx-font-size: 14px;"
                                + "-fx-font-weight: bold;"
                                + "-fx-text-fill: "
                                + Theme.PRIMARY
                                + ";"
                );
            }

            setLabel(
                    progressTextLabel,
                    "Repair is in progress. Save diagnosis, work details and costs before completing."
            );

            if (startRepairButton != null) {

                startRepairButton.setText(
                        "Repair Started"
                );

                startRepairButton.setDisable(
                        true
                );
            }

            if (navigationButton != null) {

                navigationButton.setDisable(
                        false
                );
            }

            if (arrivedButton != null) {

                arrivedButton.setDisable(
                        false
                );
            }

            setWorkFieldsDisabled(
                    false
            );

            if (saveDetailsButton != null) {

                saveDetailsButton.setDisable(
                        false
                );
            }

            if (completeJobButton != null) {

                completeJobButton.setDisable(
                        false
                );
            }

            return;
        }

        // =================================================
        // OTHER STATUS
        // =================================================

        setLabel(
                progressTextLabel,
                "This request is no longer an active repair job."
        );

        if (startRepairButton != null) {

            startRepairButton.setDisable(
                    true
            );
        }

        if (navigationButton != null) {

            navigationButton.setDisable(
                    true
            );
        }

        if (arrivedButton != null) {

            arrivedButton.setDisable(
                    true
            );
        }

        setWorkFieldsDisabled(
                true
        );

        if (saveDetailsButton != null) {

            saveDetailsButton.setDisable(
                    true
            );
        }

        if (completeJobButton != null) {

            completeJobButton.setDisable(
                    true
            );
        }
    }

    // =====================================================
    // NO ACTIVE JOB
    // =====================================================

    private void showNoActiveJob() {

        currentJob =
                null;

        // =================================================
        // FALLBACK DISPLAY
        // =================================================

        setLabel(
                requestIdValueLabel,
                "-"
        );

        setLabel(
                customerValueLabel,
                fallbackCustomer
        );

        setLabel(
                vehicleValueLabel,
                fallbackVehicle
        );

        setLabel(
                serviceValueLabel,
                fallbackType
        );

        setLabel(
                problemValueLabel,
                fallbackProblem
        );

        setLabel(
                locationValueLabel,
                fallbackLocation
        );

        setLabel(
                jobStatusLabel,
                "No Active Job"
        );

        if (jobStatusLabel != null) {

            jobStatusLabel.setStyle(
                    "-fx-font-size: 14px;"
                            + "-fx-font-weight: bold;"
                            + "-fx-text-fill: "
                            + Theme.SECONDARY_TEXT
                            + ";"
            );
        }

        setLabel(
                progressTextLabel,
                "Accept a service request first. Accepted requests automatically appear here."
        );

        if (diagnosisField != null) {

            diagnosisField.clear();
        }

        if (repairField != null) {

            repairField.clear();
        }

        if (estimatedCostField != null) {

            estimatedCostField.setText(
                    cleanMoneyForField(
                            fallbackAmount
                    )
            );
        }

        if (partsCostField != null) {

            partsCostField.setText(
                    "0"
            );
        }

        if (labourCostField != null) {

            labourCostField.setText(
                    "0"
            );
        }

        updateTotal();

        setWorkFieldsDisabled(
                true
        );

        if (navigationButton != null) {

            navigationButton.setDisable(
                    true
            );
        }

        if (arrivedButton != null) {

            arrivedButton.setDisable(
                    true
            );
        }

        if (startRepairButton != null) {

            startRepairButton.setDisable(
                    true
            );

            startRepairButton.setText(
                    "Start Repair"
            );
        }

        if (saveDetailsButton != null) {

            saveDetailsButton.setDisable(
                    true
            );
        }

        if (completeJobButton != null) {

            completeJobButton.setDisable(
                    true
            );
        }

        if (refreshButton != null) {

            refreshButton.setDisable(
                    false
            );
        }
    }

    // =====================================================
    // START NAVIGATION
    // =====================================================

    private void startNavigation() {

        if (!hasRealCurrentJob()) {

            showError(
                    "Navigation",
                    "No Firebase active job is selected."
            );

            return;
        }

        boolean success =
                controller.startNavigation(
                        currentJob.getRequestId()
                );

        if (success) {

            showInfo(
                    "Navigation Started",
                    "Navigation status was saved for this service request.\n\n"
                            + "Customer location: "
                            + controller.getLocationDisplay(
                            currentJob
                    )
            );

        } else {

            showError(
                    "Navigation Error",
                    "Navigation could not be started for this job."
            );
        }
    }

    // =====================================================
    // MARK ARRIVED
    // =====================================================

    private void markArrived() {

        if (!hasRealCurrentJob()) {

            showError(
                    "Arrival",
                    "No Firebase active job is selected."
            );

            return;
        }

        boolean success =
                controller.markArrived(
                        currentJob.getRequestId()
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
    // START REPAIR
    // =====================================================

    private void startRepair() {

        if (!hasRealCurrentJob()) {

            showError(
                    "Start Repair",
                    "No accepted Firebase job was found."
            );

            return;
        }

        if (controller.isRepairInProgress(
                currentJob
        )) {

            showInfo(
                    "Repair",
                    "This repair is already in progress."
            );

            return;
        }

        if (!controller.canStartRepair(
                currentJob
        )) {

            showError(
                    "Start Repair",
                    "Only an Accepted request can be started."
            );

            loadActiveJob();

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
                "Start Repair?"
        );

        confirmation.setContentText(
                "Customer: "
                        + controller.getCustomerName(
                        currentJob
                )
                        + "\nVehicle: "
                        + controller.getVehicleDisplay(
                        currentJob
                )
                        + "\n\nStatus will change from Accepted to In Progress."
        );

        Optional<ButtonType> result =
                confirmation.showAndWait();

        if (result.isEmpty()
                ||
                result.get() != ButtonType.OK) {

            return;
        }

        boolean success =
                controller.startRepair(
                        currentJob.getRequestId()
                );

        if (success) {

            showInfo(
                    "Repair Started",
                    "The job is now In Progress.\n\n"
                            + "Customer and Admin will see the updated status."
            );

            loadActiveJob();

        } else {

            showError(
                    "Unable to Start Repair",
                    "The request status may have changed. Refresh and try again."
            );

            loadActiveJob();
        }
    }

    // =====================================================
    // SAVE WORK DETAILS
    // =====================================================

    private void saveWorkDetails() {

        if (!hasRealCurrentJob()) {

            showError(
                    "Save Work",
                    "No Firebase active job was found."
            );

            return;
        }

        if (!controller.isActive(
                currentJob
        )) {

            showError(
                    "Save Work",
                    "This request is no longer an active job."
            );

            loadActiveJob();

            return;
        }

        String diagnosis =
                clean(
                        diagnosisField.getText()
                );

        String repair =
                clean(
                        repairField.getText()
                );

        String estimated =
                cleanMoneyForField(
                        estimatedCostField.getText()
                );

        String parts =
                cleanMoneyForField(
                        partsCostField.getText()
                );

        String labour =
                cleanMoneyForField(
                        labourCostField.getText()
                );

        // =================================================
        // VALIDATE MONEY
        // =================================================

        if (!controller.isValidAmount(
                estimated
        )) {

            showError(
                    "Invalid Cost",
                    "Estimated cost must be a valid positive number or 0."
            );

            estimatedCostField.requestFocus();

            return;
        }

        if (!controller.isValidAmount(
                parts
        )) {

            showError(
                    "Invalid Cost",
                    "Parts cost must be a valid positive number or 0."
            );

            partsCostField.requestFocus();

            return;
        }

        if (!controller.isValidAmount(
                labour
        )) {

            showError(
                    "Invalid Cost",
                    "Labour cost must be a valid positive number or 0."
            );

            labourCostField.requestFocus();

            return;
        }

        String finalCost =
                controller.calculateTotalAsString(
                        parts,
                        labour
                );

        /*
         * For SAVE we allow blank diagnosis / repair while
         * the mechanic is still working.
         */
        boolean diagnosisSaved =
                controller.saveDiagnosis(
                        currentJob.getRequestId(),
                        diagnosis == null
                                ? ""
                                : diagnosis
                );

        boolean repairSaved =
                controller.saveRepairDetails(
                        currentJob.getRequestId(),
                        repair == null
                                ? ""
                                : repair
                );

        boolean costsSaved =
                controller.saveCostDetails(
                        currentJob.getRequestId(),
                        zeroIfBlank(
                                estimated
                        ),
                        zeroIfBlank(
                                parts
                        ),
                        zeroIfBlank(
                                labour
                        ),
                        finalCost
                );

        if (
                diagnosisSaved
                        &&
                repairSaved
                        &&
                costsSaved
        ) {

            showInfo(
                    "Work Saved",
                    "Diagnosis, repair details and costs were saved to Firebase."
            );

            loadActiveJob();

        } else {

            showError(
                    "Save Failed",
                    "Some job details could not be saved."
            );
        }
    }

    // =====================================================
    // COMPLETE JOB
    // =====================================================

    private void completeJob() {

        if (!hasRealCurrentJob()) {

            showError(
                    "Complete Job",
                    "No Firebase active job was found."
            );

            return;
        }

        String diagnosis =
                clean(
                        diagnosisField.getText()
                );

        String repair =
                clean(
                        repairField.getText()
                );

        String estimated =
                zeroIfBlank(
                        cleanMoneyForField(
                                estimatedCostField.getText()
                        )
                );

        String parts =
                zeroIfBlank(
                        cleanMoneyForField(
                                partsCostField.getText()
                        )
                );

        String labour =
                zeroIfBlank(
                        cleanMoneyForField(
                                labourCostField.getText()
                        )
                );

        // =================================================
        // VALIDATION
        // =================================================

        String validation =
                controller.validateCompletion(
                        currentJob,
                        diagnosis,
                        repair,
                        parts,
                        labour
                );

        if (validation != null) {

            showError(
                    "Cannot Complete Job",
                    validation
            );

            return;
        }

        if (!controller.isValidAmount(
                estimated
        )) {

            showError(
                    "Invalid Estimated Cost",
                    "Enter a valid estimated cost."
            );

            return;
        }

        String finalCost =
                controller.calculateTotalAsString(
                        parts,
                        labour
                );

        Alert confirmation =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        confirmation.setTitle(
                "RoadGuardian"
        );

        confirmation.setHeaderText(
                "Complete this service job?"
        );

        confirmation.setContentText(
                "Customer: "
                        + controller.getCustomerName(
                        currentJob
                )
                        + "\nVehicle: "
                        + controller.getVehicleDisplay(
                        currentJob
                )
                        + "\n\nParts Cost: ₹"
                        + parts
                        + "\nLabour Cost: ₹"
                        + labour
                        + "\nFinal Cost: ₹"
                        + finalCost
                        + "\n\nStatus will change to Completed."
        );

        Optional<ButtonType> result =
                confirmation.showAndWait();

        if (result.isEmpty()
                ||
                result.get() != ButtonType.OK) {

            return;
        }

        completeJobButton.setDisable(
                true
        );

        boolean success =
                controller.completeJob(
                        currentJob.getRequestId(),
                        diagnosis,
                        repair,
                        estimated,
                        parts,
                        labour
                );

        if (success) {

            showInfo(
                    "Job Completed",
                    "Service job completed successfully.\n\n"
                            + "Final Cost: ₹"
                            + finalCost
                            + "\n\n"
                            + "The same record is now available in Customer history, Admin requests and Mechanic job history."
            );

            /*
             * Completed request is no longer an Active Job.
             */
            loadActiveJob();

        } else {

            completeJobButton.setDisable(
                    false
            );

            showError(
                    "Completion Failed",
                    "Job could not be completed. Make sure the repair status is In Progress."
            );

            loadActiveJob();
        }
    }

    // =====================================================
    // UPDATE TOTAL
    // =====================================================

    private void updateTotal() {

        if (totalAmountLabel == null) {

            return;
        }

        String parts =
                partsCostField == null
                        ? "0"
                        : partsCostField.getText();

        String labour =
                labourCostField == null
                        ? "0"
                        : labourCostField.getText();

        String display =
                controller.calculateTotalDisplay(
                        parts,
                        labour
                );

        totalAmountLabel.setText(
                display
        );
    }

    // =====================================================
    // DISABLE / ENABLE WORK FIELDS
    // =====================================================

    private void setWorkFieldsDisabled(
            boolean disabled
    ) {

        if (diagnosisField != null) {

            diagnosisField.setDisable(
                    disabled
            );
        }

        if (repairField != null) {

            repairField.setDisable(
                    disabled
            );
        }

        if (estimatedCostField != null) {

            estimatedCostField.setDisable(
                    disabled
            );
        }

        if (partsCostField != null) {

            partsCostField.setDisable(
                    disabled
            );
        }

        if (labourCostField != null) {

            labourCostField.setDisable(
                    disabled
            );
        }
    }

    // =====================================================
    // HAS REAL FIREBASE JOB
    // =====================================================

    private boolean hasRealCurrentJob() {

        return currentJob != null
                &&
                currentJob.getRequestId() != null
                &&
                !currentJob
                        .getRequestId()
                        .isBlank();
    }

    // =====================================================
    // EXISTING COMPATIBILITY METHOD
    //
    // DO NOT REMOVE.
    //
    // This does NOT create a Firebase job.
    //
    // Accepted requests must already exist in:
    //
    // serviceRequests/{requestId}
    // =====================================================

    public void setJob(
            String customer,
            String vehicle,
            String problem,
            String type,
            String distance,
            String amount
    ) {

        fallbackCustomer =
                valueOrDefault(
                        customer,
                        "No Customer"
                );

        fallbackVehicle =
                valueOrDefault(
                        vehicle,
                        "No Vehicle"
                );

        fallbackProblem =
                valueOrDefault(
                        problem,
                        "No Service Request"
                );

        fallbackType =
                valueOrDefault(
                        type,
                        "Service Request"
                );

        /*
         * Old UI called this field distance.
         * We retain it only as a fallback display.
         */
        fallbackLocation =
                valueOrDefault(
                        distance,
                        "Location not available"
                );

        fallbackAmount =
                valueOrDefault(
                        amount,
                        "₹0"
                );

        /*
         * Always try Firebase first.
         */
        if (mainContent != null) {

            loadActiveJob();
        }
    }

    // =====================================================
    // OPTIONAL CURRENT JOB
    // =====================================================

    public ServiceRequest getCurrentJob() {

        return currentJob;
    }

    // =====================================================
    // REFRESH FROM DASHBOARD
    // =====================================================

    public void refresh() {

        if (mainContent != null) {

            loadActiveJob();
        }
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

        label.setStyle(
                "-fx-font-size: 11px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: "
                        + Theme.TEXT
                        + ";"
        );

        return label;
    }

    // =====================================================
    // TEXT AREA STYLE
    // =====================================================

    private void styleTextArea(
            TextArea area
    ) {

        area.setStyle(
                "-fx-control-inner-background: "
                        + Theme.SURFACE
                        + ";"
                        + "-fx-background-color: "
                        + Theme.SURFACE
                        + ";"
                        + "-fx-border-color: "
                        + Theme.BORDER
                        + ";"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
                        + "-fx-font-size: 11px;"
                        + "-fx-text-fill: "
                        + Theme.TEXT
                        + ";"
        );
    }

    // =====================================================
    // TEXT FIELD STYLE
    // =====================================================

    private void styleTextField(
            TextField field
    ) {

        field.setPadding(
                new Insets(10)
        );

        field.setStyle(
                "-fx-background-color: "
                        + Theme.SURFACE
                        + ";"
                        + "-fx-border-color: "
                        + Theme.BORDER
                        + ";"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
                        + "-fx-font-size: 11px;"
                        + "-fx-text-fill: "
                        + Theme.TEXT
                        + ";"
        );
    }

    // =====================================================
    // PRIMARY BUTTON
    // =====================================================

    private void stylePrimaryButton(
            Button button
    ) {

        button.setPadding(
                new Insets(
                        10,
                        18,
                        10,
                        18
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
                        + "-fx-font-size: 11px;"
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
                        10,
                        16,
                        10,
                        16
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
    // CARD STYLE
    // =====================================================

    private String cardStyle() {

        return "-fx-background-color: "
                + Theme.CARD
                + ";"
                + "-fx-background-radius: 14;"
                + "-fx-border-color: "
                + "#FACC15"
                + ";"
                + "-fx-border-radius: 14;";
    }

    // =====================================================
    // SET LABEL
    // =====================================================

    private void setLabel(
            Label label,
            String value
    ) {

        if (label != null) {

            label.setText(
                    safe(
                            value
                    )
            );
        }
    }

    // =====================================================
    // CLEAN MONEY FOR TEXT FIELD
    // =====================================================

    private String cleanMoneyForField(
            String value
    ) {

        if (value == null
                ||
                value.isBlank()) {

            return "0";
        }

        String cleaned =
                value
                        .replace(
                                "₹",
                                ""
                        )
                        .replace(
                                ",",
                                ""
                        )
                        .trim();

        return cleaned.isBlank()
                ? "0"
                : cleaned;
    }

    // =====================================================
    // ZERO IF BLANK
    // =====================================================

    private String zeroIfBlank(
            String value
    ) {

        String cleaned =
                clean(
                        value
                );

        return cleaned == null
                ? "0"
                : cleaned;
    }

    // =====================================================
    // VALUE OR DEFAULT
    // =====================================================

    private String valueOrDefault(
            String value,
            String defaultValue
    ) {

        String cleaned =
                clean(
                        value
                );

        return cleaned == null
                ? defaultValue
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
    // INFO
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
}