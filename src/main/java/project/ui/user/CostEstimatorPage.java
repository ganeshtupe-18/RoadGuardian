package project.ui.user;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import project.controller.user.CostEstimatorController;
import project.model.RoadService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CostEstimatorPage {

    // ============================================================
    // COLORS
    // Same visual language as AI Diagnosis Page
    // ============================================================

    private static final String BG = "#111111";
    private static final String CARD = "#181818";
    private static final String CARD_DARK = "#151515";
    private static final String SURFACE = "#202020";
    private static final String INPUT = "#242424";
    private static final String BORDER = "#343434";

    private static final String WHITE = "#F8FAFC";
    private static final String TEXT = "#E2E8F0";
    private static final String MUTED = "#94A3B8";

    private static final String BLUE = "#3B82F6";
    private static final String BLUE_HOVER = "#2563EB";

    private static final String GREEN = "#22C55E";
    private static final String GREEN_HOVER = "#16A34A";

    private static final String AMBER = "#F59E0B";
    private static final String AMBER_LIGHT = "#FBBF24";

    private static final String RED = "#EF4444";

    private static final String FONT = "Arial";

    // ============================================================
    // FIELDS
    // ============================================================

    private Scene scene;

    private CostEstimatorController controller;

    private final Map<String, Map<String, Object>> vehicleByDisplay =
            new LinkedHashMap<>();

    private final Map<String, RoadService> serviceByDisplay =
            new LinkedHashMap<>();

    private ComboBox<String> vehicleBox;
    private ComboBox<String> serviceBox;

    private TextArea notesArea;
    private TextField locationField;

    private Label selectedVehicleLabel;

    private Label priceLabel;
    private Label basePriceLabel;
    private Label partsLabel;
    private Label labourLabel;
    private Label durationLabel;
    private Label confidenceLabel;

    private Label explanationLabel;
    private Label assumptionsLabel;
    private Label recommendationLabel;
    private Label estimateSourceLabel;

    private Button calculateButton;
    private Button requestButton;

    private CostEstimatorController.EstimateResult currentEstimate;

    private Map<String, Object> currentVehicle;
    private RoadService currentService;

    private String currentEstimateId;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public CostEstimatorPage() {

        try {

            controller =
                    new CostEstimatorController();

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Unable to initialize Cost Estimator controller.",
                    e
            );
        }
    }

    // ============================================================
    // GET SCENE
    // ============================================================

    public Scene getCostEstimatorScene() {

        BorderPane root =
                new BorderPane();

        root.setStyle(
                "-fx-background-color: " + BG + ";"
        );

        // ========================================================
        // HEADER
        // ========================================================

        try {

            root.setTop(
                    UserHeader.createHeader()
            );

        } catch (Exception e) {

            System.err.println(
                    "Cost Estimator Header Error: "
                            + e.getMessage()
            );

            root.setTop(
                    createFallbackHeader()
            );
        }

        // ========================================================
        // SIDEBAR
        // ========================================================

        try {

            root.setLeft(
                    UserSideBar.createSidebar(
                            "Cost Estimator"
                    )
            );

        } catch (Exception e) {

            System.err.println(
                    "Cost Estimator Sidebar Error: "
                            + e.getMessage()
            );
        }

        // ========================================================
        // SCROLL
        // ========================================================

        ScrollPane scrollPane =
                new ScrollPane();

        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setPannable(true);

        scrollPane.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        scrollPane.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED
        );

        scrollPane.setStyle(
                "-fx-background: " + BG + ";"
                        + "-fx-background-color: " + BG + ";"
                        + "-fx-border-color: transparent;"
        );

        VBox content =
                createContent();

        scrollPane.setContent(
                content
        );

        root.setCenter(
                scrollPane
        );

        // ========================================================
        // SCENE
        // ========================================================

        scene =
                new Scene(
                        root,
                        1280,
                        800
                );

        scene.setFill(
                Color.web(BG)
        );

        // ========================================================
        // LOAD DATA
        // ========================================================

        loadVehicles();
        loadServices();

        return scene;
    }

    // ============================================================
    // SHOW
    // ============================================================

    public void show(Stage stage) {

        if (stage == null) {

            throw new IllegalArgumentException(
                    "Stage cannot be null."
            );
        }

        stage.setScene(
                getCostEstimatorScene()
        );

        stage.setTitle(
                "RoadGuardian - Cost Estimator"
        );

        stage.show();
    }

    // ============================================================
    // MAIN CONTENT
    // ============================================================

    private VBox createContent() {

        VBox main =
                new VBox(22);

        main.setPadding(
                new Insets(
                        28,
                        38,
                        45,
                        38
                )
        );

        main.setFillWidth(true);

        main.setStyle(
                "-fx-background-color: " + BG + ";"
        );

        // ========================================================
        // PAGE TITLE
        // ========================================================

        Label title =
                createLabel(
                        "Repair Cost Estimator",
                        30,
                        FontWeight.BOLD,
                        WHITE
                );

        Label subtitle =
                createLabel(
                        "Get an AI-powered repair cost range using your vehicle, "
                                + "selected service and reported problem.",
                        14,
                        FontWeight.NORMAL,
                        TEXT
                );

        Label info =
                createLabel(
                        "Important: AI provides a preliminary estimate only. "
                                + "It is not a final invoice. The mechanic confirms "
                                + "the final repair cost after physical inspection.",
                        13,
                        FontWeight.NORMAL,
                        AMBER_LIGHT
                );

        subtitle.setWrapText(true);
        info.setWrapText(true);

        VBox infoBox =
                new VBox(
                        7,
                        title,
                        subtitle
                );

        VBox notice =
                new VBox(
                        info
                );

        notice.setPadding(
                new Insets(
                        10,
                        14,
                        10,
                        14
                )
        );

        notice.setStyle(
                "-fx-background-color: #211A0A;"
                        + "-fx-border-color: #8A6208;"
                        + "-fx-border-width: 1;"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
        );

        // ========================================================
        // BODY
        // ========================================================

        HBox body =
                new HBox(20);

        body.setAlignment(
                Pos.TOP_CENTER
        );

        body.setFillHeight(true);

        VBox inputCard =
                createInputCard();

        VBox estimateCard =
                createEstimateCard();

        inputCard.setMinWidth(480);
        inputCard.setPrefWidth(560);
        inputCard.setMaxWidth(
                Double.MAX_VALUE
        );

        estimateCard.setMinWidth(480);
        estimateCard.setPrefWidth(600);
        estimateCard.setMaxWidth(
                Double.MAX_VALUE
        );

        HBox.setHgrow(
                inputCard,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                estimateCard,
                Priority.ALWAYS
        );

        body.getChildren().addAll(
                inputCard,
                estimateCard
        );

        main.getChildren().addAll(
                infoBox,
                notice,
                body
        );

        return main;
    }

    // ============================================================
    // INPUT CARD
    // ============================================================

    private VBox createInputCard() {

        VBox card =
                createCard();

        Label heading =
                createLabel(
                        "Vehicle & Problem Details",
                        21,
                        FontWeight.BOLD,
                        WHITE
                );

        Label headingNote =
                createLabel(
                        "Select your vehicle, service and describe the problem.",
                        13,
                        FontWeight.NORMAL,
                        MUTED
                );

        Separator separator =
                createSeparator();

        // ========================================================
        // VEHICLE
        // ========================================================

        Label vehicleLabel =
                createFieldLabel(
                        "Your Vehicle"
                );

        vehicleBox =
                createComboBox();

        vehicleBox.setPromptText(
                "Select your registered vehicle"
        );

        vehicleBox.setOnAction(
                event -> updateSelectedVehicle()
        );

        selectedVehicleLabel =
                createLabel(
                        "Select a vehicle to see its details.",
                        12,
                        FontWeight.NORMAL,
                        MUTED
                );

        selectedVehicleLabel.setWrapText(true);

        // ========================================================
        // SERVICE
        // ========================================================

        Label serviceLabel =
                createFieldLabel(
                        "Repair / Service"
                );

        serviceBox =
                createComboBox();

        serviceBox.setPromptText(
                "Select the service you need"
        );

        // ========================================================
        // PROBLEM
        // ========================================================

        Label problemLabel =
                createFieldLabel(
                        "Describe Your Problem"
                );

        notesArea =
                new TextArea();

        notesArea.setPromptText(
                "Example:\n"
                        + "Wiper chalat nahi\n"
                        + "Brake madhun awaaz yetoy\n"
                        + "Engine start hot nahi\n"
                        + "AC cooling kami aahe\n\n"
                        + "You can type in Marathi, Hindi, English or Hinglish."
        );

        notesArea.setWrapText(true);

        notesArea.setPrefRowCount(8);

        notesArea.setMinHeight(165);

        notesArea.setMaxWidth(
                Double.MAX_VALUE
        );

        styleTextArea(
                notesArea
        );

        // ========================================================
        // LOCATION
        // ========================================================

        Label locationLabel =
                createFieldLabel(
                        "Service Location"
                );

        locationField =
                createTextField(
                        "Enter your current/service location"
                );

        // ========================================================
        // PRICE INFO
        // ========================================================

        Label servicePriceInfo =
                createLabel(
                        "The Admin Firebase service price is used only "
                                + "as a reference for the AI estimate.",
                        12,
                        FontWeight.NORMAL,
                        MUTED
                );

        servicePriceInfo.setWrapText(true);

        // ========================================================
        // BUTTONS
        // ========================================================

        calculateButton =
                createPrimaryButton(
                        "Calculate AI Repair Cost"
                );

        calculateButton.setMaxWidth(
                Double.MAX_VALUE
        );

        calculateButton.setPrefHeight(46);

        calculateButton.setOnAction(
                event -> calculateAI()
        );

        Button resetButton =
                createSecondaryButton(
                        "Reset"
                );

        resetButton.setPrefHeight(46);

        resetButton.setOnAction(
                event -> resetForm()
        );

        HBox buttonRow =
                new HBox(12);

        HBox.setHgrow(
                calculateButton,
                Priority.ALWAYS
        );

        buttonRow.getChildren().addAll(
                calculateButton,
                resetButton
        );

        card.getChildren().addAll(
                heading,
                headingNote,
                separator,

                vehicleLabel,
                vehicleBox,
                selectedVehicleLabel,

                serviceLabel,
                serviceBox,

                problemLabel,
                notesArea,

                locationLabel,
                locationField,

                servicePriceInfo,

                buttonRow
        );

        return card;
    }

    // ============================================================
    // ESTIMATE CARD
    // ============================================================

    private VBox createEstimateCard() {

        VBox card =
                createCard();

        Label heading =
                createLabel(
                        "AI Cost Estimate",
                        21,
                        FontWeight.BOLD,
                        WHITE
                );

        Label headingNote =
                createLabel(
                        "Estimated cost based on your vehicle, service "
                                + "and reported problem.",
                        13,
                        FontWeight.NORMAL,
                        MUTED
                );

        Separator separator =
                createSeparator();

        // ========================================================
        // PRICE BOX
        // ========================================================

        Label priceTitle =
                createLabel(
                        "Estimated Total Repair Cost",
                        14,
                        FontWeight.BOLD,
                        TEXT
                );

        priceLabel =
                createLabel(
                        "₹ —",
                        32,
                        FontWeight.BOLD,
                        AMBER_LIGHT
                );

        priceLabel.setWrapText(true);

        VBox priceBox =
                new VBox(
                        8,
                        priceTitle,
                        priceLabel
                );

        priceBox.setPadding(
                new Insets(18)
        );

        priceBox.setStyle(
                "-fx-background-color: " + CARD_DARK + ";"
                        + "-fx-background-radius: 12;"
                        + "-fx-border-color: " + AMBER + ";"
                        + "-fx-border-width: 1;"
                        + "-fx-border-radius: 12;"
        );

        // ========================================================
        // DETAILS TITLE
        // ========================================================

        Label detailsTitle =
                createLabel(
                        "Estimate Details",
                        15,
                        FontWeight.BOLD,
                        WHITE
                );

        // ========================================================
        // DETAILS
        // ========================================================

        basePriceLabel =
                createValueCard(
                        "Admin Base Price",
                        "₹ —"
                );

        partsLabel =
                createValueCard(
                        "Possible Parts",
                        "₹ —"
                );

        labourLabel =
                createValueCard(
                        "Possible Labour",
                        "₹ —"
                );

        durationLabel =
                createValueCard(
                        "Estimated Duration",
                        "—"
                );

        confidenceLabel =
                createValueCard(
                        "AI Confidence",
                        "—"
                );

        VBox details =
                new VBox(
                        9,
                        detailsTitle,
                        basePriceLabel,
                        partsLabel,
                        labourLabel,
                        durationLabel,
                        confidenceLabel
                );

        // ========================================================
        // AI ANALYSIS
        // ========================================================

        explanationLabel =
                createInformationCard(
                        "AI Analysis",
                        "AI analysis will appear here after calculation."
                );

        assumptionsLabel =
                createInformationCard(
                        "Assumptions",
                        "AI assumptions will appear here."
                );

        recommendationLabel =
                createInformationCard(
                        "Recommendation",
                        "AI recommendation will appear here."
                );

        estimateSourceLabel =
                createInformationCard(
                        "Estimate Source",
                        "Waiting for AI calculation."
                );

        // ========================================================
        // WARNING
        // ========================================================

        Label warning =
                createLabel(
                        "⚠ Final cost may change after mechanic inspection, "
                                + "actual parts selection and labour.",
                        12,
                        FontWeight.NORMAL,
                        AMBER_LIGHT
                );

        warning.setWrapText(true);

        warning.setPadding(
                new Insets(10, 12, 10, 12)
        );

        warning.setStyle(
                "-fx-background-color: #2A2110;"
                        + "-fx-background-radius: 8;"
                        + "-fx-border-color: #7C5A12;"
                        + "-fx-border-width: 1;"
                        + "-fx-border-radius: 8;"
        );

        // ========================================================
        // REQUEST BUTTON
        // ========================================================

        requestButton =
                createGreenButton(
                        "Create Service Request"
                );

        requestButton.setMaxWidth(
                Double.MAX_VALUE
        );

        requestButton.setPrefHeight(46);

        requestButton.setDisable(true);

        requestButton.setOnAction(
                event -> createRequest()
        );

        // ========================================================
        // ADD ALL
        // ========================================================

        card.getChildren().addAll(
                heading,
                headingNote,
                separator,

                priceBox,

                details,

                explanationLabel,
                assumptionsLabel,
                recommendationLabel,
                estimateSourceLabel,

                warning,

                requestButton
        );

        return card;
    }

    // ============================================================
    // CALCULATE AI
    // ============================================================

    private void calculateAI() {

        String vehicleDisplay =
                vehicleBox.getValue();

        if (vehicleDisplay == null
                || vehicleDisplay.trim().isEmpty()) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Vehicle Required",
                    "Please select your vehicle first."
            );

            return;
        }

        String serviceDisplay =
                serviceBox.getValue();

        if (serviceDisplay == null
                || serviceDisplay.trim().isEmpty()) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Service Required",
                    "Please select a service first."
            );

            return;
        }

        Map<String, Object> selectedVehicle =
                vehicleByDisplay.get(
                        vehicleDisplay
                );

        RoadService selectedService =
                serviceByDisplay.get(
                        serviceDisplay
                );

        if (selectedVehicle == null) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Vehicle Error",
                    "Selected vehicle information could not be loaded."
            );

            return;
        }

        if (selectedService == null) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Service Error",
                    "Selected service information could not be loaded."
            );

            return;
        }

        String notes =
                notesArea.getText();

        if (notes == null
                || notes.trim().isEmpty()) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Problem Required",
                    "Please describe your vehicle problem."
            );

            notesArea.requestFocus();

            return;
        }

        currentVehicle =
                selectedVehicle;

        currentService =
                selectedService;

        final String finalNotes =
                notes.trim();

        setCalculatingState(true);

        Thread thread =
                new Thread(() -> {

                    try {

                        System.out.println(
                                "=================================================="
                        );

                        System.out.println(
                                "Starting AI Cost Estimation..."
                        );

                        System.out.println(
                                "Service: "
                                        + selectedService.getName()
                        );

                        System.out.println(
                                "Customer Problem: "
                                        + finalNotes
                        );

                        System.out.println(
                                "=================================================="
                        );

                        CostEstimatorController.EstimateResult estimate =
                                controller.calculateWithAI(
                                        selectedVehicle,
                                        selectedService,
                                        finalNotes
                                );

                        Platform.runLater(() -> {

                            currentEstimate =
                                    estimate;

                            renderEstimate(
                                    estimate
                            );

                            setCalculatingState(
                                    false
                            );
                        });

                    } catch (Exception e) {

                        e.printStackTrace();

                        Platform.runLater(() -> {

                            setCalculatingState(
                                    false
                            );

                            showAlert(
                                    Alert.AlertType.ERROR,
                                    "Cost Estimation Failed",
                                    extractUsefulErrorMessage(e)
                            );
                        });
                    }

                });

        thread.setName(
                "RoadGuardian-Cost"
        );

        thread.setDaemon(true);

        thread.start();
    }

    // ============================================================
    // RENDER ESTIMATE
    // ============================================================

    private void renderEstimate(
            CostEstimatorController.EstimateResult estimate) {

        if (estimate == null) {
            return;
        }

        // ========================================================
        // TOTAL PRICE
        // ========================================================

        if (estimate.hasPrice()) {

            priceLabel.setText(
                    formatMoney(
                            estimate.getMinimum()
                    )
                            + " – "
                            + formatMoney(
                            estimate.getMaximum()
                    )
            );

        } else {

            priceLabel.setText(
                    "Final cost after inspection"
            );
        }

        // ========================================================
        // DETAIL CARDS
        // ========================================================

        basePriceLabel.setText(
                "Admin Base Price\n"
                        + formatMoney(
                        estimate.getBasePrice()
                )
        );

        partsLabel.setText(
                "Possible Parts\n"
                        + formatRange(
                        estimate.getPartsMinimum(),
                        estimate.getPartsMaximum()
                )
        );

        labourLabel.setText(
                "Possible Labour\n"
                        + formatRange(
                        estimate.getLabourMinimum(),
                        estimate.getLabourMaximum()
                )
        );

        durationLabel.setText(
                "Estimated Duration\n"
                        + safe(
                        estimate.getDuration(),
                        "Confirmed after inspection"
                )
        );

        confidenceLabel.setText(
                "AI Confidence\n"
                        + safe(
                        estimate.getConfidence(),
                        "Medium"
                )
        );

        // ========================================================
        // AI ANALYSIS
        // ========================================================

        explanationLabel.setText(
                "AI Analysis\n"
                        + safe(
                        estimate.getNote(),
                        "AI estimate is based on the selected vehicle, "
                                + "service and reported problem."
                )
        );

        assumptionsLabel.setText(
                "Assumptions\n"
                        + safe(
                        estimate.getAssumptions(),
                        "No additional assumptions provided."
                )
        );

        recommendationLabel.setText(
                "Recommendation\n"
                        + safe(
                        estimate.getRecommendation(),
                        "Get the vehicle inspected by a mechanic before "
                                + "starting major repairs."
                )
        );

        // ========================================================
        // SOURCE
        // ========================================================

        if (estimate.isAiGenerated()) {

            estimateSourceLabel.setText(
                    "Estimate Source\n"
                            + " AI + Admin Service Base Price"
            );

        } else {

            estimateSourceLabel.setText(
                    "Estimate Source\n"
                            + "Admin  Service Base Price"
            );
        }

        // ========================================================
        // ENABLE REQUEST
        // ========================================================

        requestButton.setDisable(
                !estimate.hasPrice()
        );
    }

    // ============================================================
    // CREATE SERVICE REQUEST
    // ============================================================

    private void createRequest() {

        if (currentVehicle == null
                || currentService == null
                || currentEstimate == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Estimate Required",
                    "Please calculate the repair estimate first."
            );

            return;
        }

        String location =
                locationField.getText();

        if (location == null
                || location.trim().isEmpty()) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Location Required",
                    "Please enter your service location."
            );

            locationField.requestFocus();

            return;
        }

        try {

            requestButton.setDisable(true);

            currentEstimateId =
                    controller.saveEstimate(
                            currentVehicle,
                            currentService,
                            notesArea.getText(),
                            currentEstimate
                    );

            String requestId =
                    controller.createServiceRequest(
                            currentVehicle,
                            currentService,
                            notesArea.getText(),
                            location.trim(),
                            currentEstimateId,
                            currentEstimate
                    );

            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Service Request Created",
                    "Your service request has been created successfully.\n\n"
                            + "Request ID: "
                            + requestId
                            + "\n\n"
                            + "Estimated Cost: "
                            + formatMoney(
                            currentEstimate.getMinimum()
                    )
                            + " – "
                            + formatMoney(
                            currentEstimate.getMaximum()
                    )
                            + "\n\n"
                            + "Final cost will be confirmed by the mechanic "
                            + "after inspection."
            );

        } catch (Exception e) {

            e.printStackTrace();

            requestButton.setDisable(false);

            showAlert(
                    Alert.AlertType.ERROR,
                    "Request Failed",
                    extractUsefulErrorMessage(e)
            );
        }
    }

    // ============================================================
    // LOAD VEHICLES
    // ============================================================

    private void loadVehicles() {

        try {

            List<Map<String, Object>> vehicles =
                    controller.getVehicles();

            vehicleBox.getItems().clear();

            vehicleByDisplay.clear();

            if (vehicles == null
                    || vehicles.isEmpty()) {

                vehicleBox.setPromptText(
                        "No registered vehicles found"
                );

                return;
            }

            for (Map<String, Object> vehicle : vehicles) {

                if (vehicle == null) {
                    continue;
                }

                String display =
                        buildVehicleDisplay(
                                vehicle
                        );

                String finalDisplay =
                        makeUniqueDisplay(
                                display,
                                vehicleByDisplay
                        );

                vehicleByDisplay.put(
                        finalDisplay,
                        vehicle
                );

                vehicleBox.getItems().add(
                        finalDisplay
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            vehicleBox.setPromptText(
                    "Unable to load vehicles"
            );
        }
    }

    // ============================================================
    // LOAD SERVICES
    // ============================================================

    private void loadServices() {

        try {

            List<RoadService> services =
                    controller.getActiveServices();

            serviceBox.getItems().clear();

            serviceByDisplay.clear();

            if (services == null
                    || services.isEmpty()) {

                serviceBox.setPromptText(
                        "No active services available"
                );

                return;
            }

            for (RoadService service : services) {

                if (service == null
                        || !service.isActive()) {

                    continue;
                }

                String display =
                        buildServiceDisplay(
                                service
                        );

                String finalDisplay =
                        makeUniqueDisplay(
                                display,
                                serviceByDisplay
                        );

                serviceByDisplay.put(
                        finalDisplay,
                        service
                );

                serviceBox.getItems().add(
                        finalDisplay
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            serviceBox.setPromptText(
                    "Unable to load services"
            );
        }
    }

    // ============================================================
    // UPDATE SELECTED VEHICLE
    // ============================================================

    private void updateSelectedVehicle() {

        String display =
                vehicleBox.getValue();

        if (display == null) {

            selectedVehicleLabel.setText(
                    "Select a vehicle to see its details."
            );

            selectedVehicleLabel.setTextFill(
                    Color.web(MUTED)
            );

            return;
        }

        Map<String, Object> vehicle =
                vehicleByDisplay.get(
                        display
                );

        if (vehicle == null) {
            return;
        }

        currentVehicle =
                vehicle;

        String brand =
                firstString(
                        vehicle,
                        "brand",
                        "make"
                );

        String model =
                firstString(
                        vehicle,
                        "model"
                );

        String number =
                firstString(
                        vehicle,
                        "vehicleNumber",
                        "registrationNumber"
                );

        String year =
                firstString(
                        vehicle,
                        "year",
                        "manufacturingYear"
                );

        StringBuilder text =
                new StringBuilder();

        if (brand != null) {
            text.append(brand);
        }

        if (model != null) {

            if (text.length() > 0) {
                text.append(" ");
            }

            text.append(model);
        }

        if (number != null) {

            if (text.length() > 0) {
                text.append("  •  ");
            }

            text.append(number);
        }

        if (year != null) {

            if (text.length() > 0) {
                text.append("  •  ");
            }

            text.append("Year ")
                    .append(year);
        }

        if (text.length() == 0) {

            text.append(
                    "Vehicle selected"
            );
        }

        selectedVehicleLabel.setText(
                text.toString()
        );

        selectedVehicleLabel.setTextFill(
                Color.web(TEXT)
        );
    }

    // ============================================================
    // RESET
    // ============================================================

    private void resetForm() {

        vehicleBox.getSelectionModel()
                .clearSelection();

        serviceBox.getSelectionModel()
                .clearSelection();

        notesArea.clear();

        locationField.clear();

        currentVehicle = null;
        currentService = null;
        currentEstimate = null;
        currentEstimateId = null;

        selectedVehicleLabel.setText(
                "Select a vehicle to see its details."
        );

        selectedVehicleLabel.setTextFill(
                Color.web(MUTED)
        );

        priceLabel.setText(
                "₹ —"
        );

        basePriceLabel.setText(
                "Admin Base Price\n₹ —"
        );

        partsLabel.setText(
                "Possible Parts\n₹ —"
        );

        labourLabel.setText(
                "Possible Labour\n₹ —"
        );

        durationLabel.setText(
                "Estimated Duration\n—"
        );

        confidenceLabel.setText(
                "AI Confidence\n—"
        );

        explanationLabel.setText(
                "AI Analysis\n"
                        + "AI analysis will appear here after calculation."
        );

        assumptionsLabel.setText(
                "Assumptions\n"
                        + "AI assumptions will appear here."
        );

        recommendationLabel.setText(
                "Recommendation\n"
                        + "AI recommendation will appear here."
        );

        estimateSourceLabel.setText(
                "Estimate Source\n"
                        + "Waiting for AI calculation."
        );

        requestButton.setDisable(true);

        calculateButton.setDisable(false);

        calculateButton.setText(
                "Calculate AI Repair Cost"
        );
    }

    // ============================================================
    // CALCULATING STATE
    // ============================================================

    private void setCalculatingState(
            boolean calculating) {

        calculateButton.setDisable(
                calculating
        );

        vehicleBox.setDisable(
                calculating
        );

        serviceBox.setDisable(
                calculating
        );

        notesArea.setDisable(
                calculating
        );

        if (calculating) {

            calculateButton.setText(
                    "AI is calculating..."
            );

            requestButton.setDisable(true);

            priceLabel.setText(
                    "Calculating..."
            );

        } else {

            calculateButton.setText(
                    "Calculate AI Repair Cost"
            );

            vehicleBox.setDisable(false);
            serviceBox.setDisable(false);
            notesArea.setDisable(false);
        }
    }

    // ============================================================
    // CARD
    // ============================================================

    private VBox createCard() {

        VBox card =
                new VBox(14);

        card.setPadding(
                new Insets(22)
        );

        card.setFillWidth(true);

        card.setStyle(
                "-fx-background-color: " + CARD + ";"
                        + "-fx-background-radius: 14;"
                        + "-fx-border-color: " + BORDER + ";"
                        + "-fx-border-width: 1;"
                        + "-fx-border-radius: 14;"
        );

        return card;
    }

    // ============================================================
    // LABEL
    // ============================================================

    private Label createLabel(
            String text,
            double size,
            FontWeight weight,
            String color) {

        Label label =
                new Label(text);

        label.setFont(
                Font.font(
                        FONT,
                        weight,
                        size
                )
        );

        label.setTextFill(
                Color.web(color)
        );

        label.setWrapText(true);

        return label;
    }

    // ============================================================
    // FIELD LABEL
    // ============================================================

    private Label createFieldLabel(
            String text) {

        return createLabel(
                text,
                13,
                FontWeight.BOLD,
                WHITE
        );
    }

    // ============================================================
    // COMBO BOX
    // ============================================================

    private ComboBox<String> createComboBox() {

        ComboBox<String> comboBox =
                new ComboBox<>();

        comboBox.setMaxWidth(
                Double.MAX_VALUE
        );

        comboBox.setPrefHeight(46);

        comboBox.setStyle(
                "-fx-background-color: " + INPUT + ";"
                        + "-fx-border-color: " + BORDER + ";"
                        + "-fx-border-width: 1;"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
                        + "-fx-text-fill: " + WHITE + ";"
                        + "-fx-font-family: Arial;"
                        + "-fx-font-size: 13px;"
        );

        comboBox.setButtonCell(
                createComboCell()
        );

        comboBox.setCellFactory(
                list -> createComboCell()
        );

        return comboBox;
    }

    // ============================================================
    // COMBO CELL
    // ============================================================

    private ListCell<String> createComboCell() {

        return new ListCell<String>() {

            {
                setFont(
                        Font.font(
                                FONT,
                                FontWeight.NORMAL,
                                13
                        )
                );

                setTextFill(
                        Color.web(WHITE)
                );
            }

            @Override
            protected void updateItem(
                    String item,
                    boolean empty) {

                super.updateItem(
                        item,
                        empty
                );

                if (empty
                        || item == null) {

                    setText(null);

                } else {

                    setText(item);

                    setTextFill(
                            Color.web(WHITE)
                    );

                    setFont(
                            Font.font(
                                    FONT,
                                    FontWeight.NORMAL,
                                    13
                            )
                    );

                    setStyle(
                            "-fx-background-color: " + INPUT + ";"
                                    + "-fx-text-fill: " + WHITE + ";"
                                    + "-fx-font-family: Arial;"
                                    + "-fx-font-size: 13px;"
                    );
                }
            }
        };
    }

    // ============================================================
    // TEXT FIELD
    // ============================================================

    private TextField createTextField(
            String prompt) {

        TextField field =
                new TextField();

        field.setPromptText(
                prompt
        );

        field.setPrefHeight(46);

        field.setMaxWidth(
                Double.MAX_VALUE
        );

        field.setStyle(
                "-fx-background-color: " + INPUT + ";"
                        + "-fx-control-inner-background: " + INPUT + ";"
                        + "-fx-border-color: " + BORDER + ";"
                        + "-fx-border-width: 1;"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
                        + "-fx-text-fill: " + WHITE + ";"
                        + "-fx-prompt-text-fill: " + MUTED + ";"
                        + "-fx-font-family: Arial;"
                        + "-fx-font-size: 13px;"
        );

        return field;
    }

    // ============================================================
    // TEXT AREA
    // ============================================================

    private void styleTextArea(
            TextArea area) {

        area.setStyle(
                "-fx-background-color: " + INPUT + ";"
                        + "-fx-control-inner-background: " + INPUT + ";"
                        + "-fx-border-color: " + BORDER + ";"
                        + "-fx-border-width: 1;"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
                        + "-fx-text-fill: " + WHITE + ";"
                        + "-fx-prompt-text-fill: " + MUTED + ";"
                        + "-fx-font-family: Arial;"
                        + "-fx-font-size: 13px;"
        );
    }

    // ============================================================
    // PRIMARY BLUE BUTTON
    // ============================================================

    private Button createPrimaryButton(
            String text) {

        Button button =
                new Button(text);

        button.setFont(
                Font.font(
                        FONT,
                        FontWeight.BOLD,
                        13
                )
        );

        button.setTextFill(
                Color.WHITE
        );

        button.setPrefHeight(44);

        button.setPadding(
                new Insets(
                        0,
                        20,
                        0,
                        20
                )
        );

        button.setStyle(
                "-fx-background-color: " + BLUE + ";"
                        + "-fx-background-radius: 8;"
                        + "-fx-cursor: hand;"
        );

        button.setOnMouseEntered(
                event -> {

                    if (!button.isDisabled()) {

                        button.setStyle(
                                "-fx-background-color: " + BLUE_HOVER + ";"
                                        + "-fx-background-radius: 8;"
                                        + "-fx-cursor: hand;"
                        );
                    }
                }
        );

        button.setOnMouseExited(
                event -> {

                    if (!button.isDisabled()) {

                        button.setStyle(
                                "-fx-background-color: " + BLUE + ";"
                                        + "-fx-background-radius: 8;"
                                        + "-fx-cursor: hand;"
                        );
                    }
                }
        );

        return button;
    }

    // ============================================================
    // GREEN BUTTON
    // ============================================================

    private Button createGreenButton(
            String text) {

        Button button =
                new Button(text);

        button.setFont(
                Font.font(
                        FONT,
                        FontWeight.BOLD,
                        13
                )
        );

        button.setTextFill(
                Color.WHITE
        );

        button.setPrefHeight(44);

        button.setPadding(
                new Insets(
                        0,
                        20,
                        0,
                        20
                )
        );

        button.setStyle(
                "-fx-background-color: " + GREEN + ";"
                        + "-fx-background-radius: 8;"
                        + "-fx-cursor: hand;"
        );

        button.setOnMouseEntered(
                event -> {

                    if (!button.isDisabled()) {

                        button.setStyle(
                                "-fx-background-color: " + GREEN_HOVER + ";"
                                        + "-fx-background-radius: 8;"
                                        + "-fx-cursor: hand;"
                        );
                    }
                }
        );

        button.setOnMouseExited(
                event -> {

                    if (!button.isDisabled()) {

                        button.setStyle(
                                "-fx-background-color: " + GREEN + ";"
                                        + "-fx-background-radius: 8;"
                                        + "-fx-cursor: hand;"
                        );
                    }
                }
        );

        return button;
    }

    // ============================================================
    // SECONDARY BUTTON
    // ============================================================

    private Button createSecondaryButton(
            String text) {

        Button button =
                new Button(text);

        button.setFont(
                Font.font(
                        FONT,
                        FontWeight.BOLD,
                        13
                )
        );

        button.setTextFill(
                Color.web(WHITE)
        );

        button.setPrefHeight(44);

        button.setPadding(
                new Insets(
                        0,
                        20,
                        0,
                        20
                )
        );

        button.setStyle(
                "-fx-background-color: " + SURFACE + ";"
                        + "-fx-border-color: " + BORDER + ";"
                        + "-fx-border-width: 1;"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
                        + "-fx-cursor: hand;"
        );

        button.setOnMouseEntered(
                event -> {

                    if (!button.isDisabled()) {

                        button.setStyle(
                                "-fx-background-color: #292929;"
                                        + "-fx-border-color: #4A4A4A;"
                                        + "-fx-border-width: 1;"
                                        + "-fx-border-radius: 8;"
                                        + "-fx-background-radius: 8;"
                                        + "-fx-cursor: hand;"
                        );
                    }
                }
        );

        button.setOnMouseExited(
                event -> {

                    if (!button.isDisabled()) {

                        button.setStyle(
                                "-fx-background-color: " + SURFACE + ";"
                                        + "-fx-border-color: " + BORDER + ";"
                                        + "-fx-border-width: 1;"
                                        + "-fx-border-radius: 8;"
                                        + "-fx-background-radius: 8;"
                                        + "-fx-cursor: hand;"
                        );
                    }
                }
        );

        return button;
    }

    // ============================================================
    // VALUE CARD
    // ============================================================

    private Label createValueCard(
            String title,
            String value) {

        Label label =
                createLabel(
                        title
                                + "\n"
                                + value,
                        13,
                        FontWeight.NORMAL,
                        TEXT
                );

        label.setMaxWidth(
                Double.MAX_VALUE
        );

        label.setPadding(
                new Insets(12)
        );

        label.setStyle(
                "-fx-background-color: " + SURFACE + ";"
                        + "-fx-background-radius: 8;"
                        + "-fx-border-color: " + BORDER + ";"
                        + "-fx-border-width: 1;"
                        + "-fx-border-radius: 8;"
        );

        return label;
    }

    // ============================================================
    // INFORMATION CARD
    // ============================================================

    private Label createInformationCard(
            String title,
            String text) {

        Label label =
                createLabel(
                        title
                                + "\n"
                                + text,
                        13,
                        FontWeight.NORMAL,
                        TEXT
                );

        label.setMaxWidth(
                Double.MAX_VALUE
        );

        label.setPadding(
                new Insets(14)
        );

        label.setStyle(
                "-fx-background-color: " + SURFACE + ";"
                        + "-fx-background-radius: 8;"
                        + "-fx-border-color: " + BORDER + ";"
                        + "-fx-border-width: 1;"
                        + "-fx-border-radius: 8;"
        );

        return label;
    }

    // ============================================================
    // SEPARATOR
    // ============================================================

    private Separator createSeparator() {

        Separator separator =
                new Separator();

        separator.setStyle(
                "-fx-background-color: " + BORDER + ";"
        );

        return separator;
    }

    // ============================================================
    // VEHICLE DISPLAY
    // ============================================================

    private String buildVehicleDisplay(
            Map<String, Object> vehicle) {

        String brand =
                firstString(
                        vehicle,
                        "brand",
                        "make"
                );

        String model =
                firstString(
                        vehicle,
                        "model"
                );

        String number =
                firstString(
                        vehicle,
                        "vehicleNumber",
                        "registrationNumber"
                );

        StringBuilder builder =
                new StringBuilder();

        if (brand != null) {
            builder.append(brand);
        }

        if (model != null) {

            if (builder.length() > 0) {
                builder.append(" ");
            }

            builder.append(model);
        }

        if (number != null) {

            if (builder.length() > 0) {
                builder.append("  •  ");
            }

            builder.append(number);
        }

        if (builder.length() == 0) {

            String id =
                    firstString(
                            vehicle,
                            "vehicleId"
                    );

            if (id != null) {
                builder.append(id);
            }
        }

        return builder.length() == 0
                ? "Vehicle"
                : builder.toString();
    }

    // ============================================================
    // SERVICE DISPLAY
    // ============================================================

    private String buildServiceDisplay(
            RoadService service) {

        String name =
                clean(
                        service.getName()
                );

        if (name == null) {
            name = "Service";
        }

        double price =
                Math.max(
                        0,
                        service.getBasePrice()
                );

        if (price > 0) {

            return name
                    + "  •  Base "
                    + formatMoney(price);
        }

        return name
                + "  •  Price after inspection";
    }

    // ============================================================
    // UNIQUE DISPLAY
    // ============================================================

    private String makeUniqueDisplay(
            String display,
            Map<String, ?> existing) {

        if (!existing.containsKey(display)) {
            return display;
        }

        int counter = 2;

        String candidate;

        do {

            candidate =
                    display
                            + " ("
                            + counter
                            + ")";

            counter++;

        } while (
                existing.containsKey(candidate)
        );

        return candidate;
    }

    // ============================================================
    // FIRST STRING
    // ============================================================

    private String firstString(
            Map<String, Object> map,
            String... keys) {

        if (map == null
                || keys == null) {

            return null;
        }

        for (String key : keys) {

            Object value =
                    map.get(key);

            if (value == null) {
                continue;
            }

            String text =
                    String.valueOf(
                            value
                    ).trim();

            if (!text.isEmpty()) {
                return text;
            }
        }

        return null;
    }

    // ============================================================
    // FORMAT MONEY
    // ============================================================

    private String formatMoney(
            double value) {

        if (value <= 0) {
            return "₹ —";
        }

        return String.format(
                Locale.US,
                "₹%,.0f",
                value
        );
    }

    // ============================================================
    // FORMAT RANGE
    // ============================================================

    private String formatRange(
            double minimum,
            double maximum) {

        if (minimum <= 0
                && maximum <= 0) {

            return "₹ —";
        }

        if (maximum <= 0
                || maximum == minimum) {

            return formatMoney(
                    minimum
            );
        }

        return formatMoney(
                minimum
        )
                + " – "
                + formatMoney(
                maximum
        );
    }

    // ============================================================
    // SAFE
    // ============================================================

    private String safe(
            String value,
            String fallback) {

        String cleaned =
                clean(value);

        return cleaned == null
                ? fallback
                : cleaned;
    }

    // ============================================================
    // CLEAN
    // ============================================================

    private String clean(
            String value) {

        if (value == null) {
            return null;
        }

        String cleaned =
                value.trim();

        return cleaned.isEmpty()
                ? null
                : cleaned;
    }

    // ============================================================
    // ERROR MESSAGE
    // ============================================================

    private String extractUsefulErrorMessage(
            Exception exception) {

        if (exception == null) {
            return "Unable to complete the operation.";
        }

        Throwable current =
                exception;

        String bestMessage =
                null;

        while (current != null) {

            String message =
                    current.getMessage();

            if (message != null
                    && !message.trim().isEmpty()) {

                bestMessage =
                        message.trim();
            }

            current =
                    current.getCause();
        }

        if (bestMessage == null) {

            return exception
                    .getClass()
                    .getSimpleName();
        }

        return bestMessage;
    }

    // ============================================================
    // ALERT
    // ============================================================

    private void showAlert(
            Alert.AlertType type,
            String title,
            String message) {

        Alert alert =
                new Alert(type);

        alert.setTitle(
                title
        );

        alert.setHeaderText(
                null
        );

        alert.setContentText(
                message
        );

        alert.setResizable(true);

        alert.getDialogPane().setMinWidth(
                460
        );

        alert.getDialogPane().setStyle(
                "-fx-background-color: " + CARD + ";"
                        + "-fx-border-color: " + BORDER + ";"
                        + "-fx-border-width: 1;"
        );

        /*
         * Fix dark-theme Alert text visibility.
         */
        for (javafx.scene.Node node
                : alert.getDialogPane()
                .lookupAll(".label")) {

            if (node instanceof Label) {

                Label label =
                        (Label) node;

                label.setTextFill(
                        Color.web(WHITE)
                );

                label.setFont(
                        Font.font(
                                FONT,
                                FontWeight.NORMAL,
                                13
                        )
                );

                label.setWrapText(true);
            }
        }

        for (javafx.scene.Node node
                : alert.getDialogPane()
                .lookupAll(".button")) {

            if (node instanceof Button) {

                Button button =
                        (Button) node;

                button.setFont(
                        Font.font(
                                FONT,
                                FontWeight.BOLD,
                                13
                        )
                );

                button.setTextFill(
                        Color.web(WHITE)
                );

                button.setStyle(
                        "-fx-background-color: " + BLUE + ";"
                                + "-fx-background-radius: 7;"
                );
            }
        }

        alert.showAndWait();
    }

    // ============================================================
    // FALLBACK HEADER
    // ============================================================

    private HBox createFallbackHeader() {

        HBox header =
                new HBox();

        header.setAlignment(
                Pos.CENTER_LEFT
        );

        header.setPadding(
                new Insets(
                        15,
                        24,
                        15,
                        24
                )
        );

        header.setStyle(
                "-fx-background-color: #101010;"
                        + "-fx-border-color: " + AMBER + ";"
                        + "-fx-border-width: 0 0 1 0;"
        );

        Label logo =
                createLabel(
                        "RoadGuardian",
                        21,
                        FontWeight.BOLD,
                        WHITE
                );

        header.getChildren().add(
                logo
        );

        return header;
    }
}