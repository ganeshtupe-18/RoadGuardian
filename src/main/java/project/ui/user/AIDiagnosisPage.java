package project.ui.user;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import project.controller.user.AIDiagnosisController;
import project.model.DiagnosisResult;
import project.model.RoadService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AIDiagnosisPage {

    // =========================================================
    // ROADGUARDIAN DARK THEME
    // =========================================================

    private static final String BG     = "#0F0F0F";
    private static final String CARD   = "#1A1A1A";
    private static final String WHITE  = "#242424";
    private static final String BORDER = "#333333";

    private static final String DARK   = "#F3F4F6";
    private static final String TEXT   = "#D1D5DB";
    private static final String MUTED  = "#A1A1AA";

    private static final String BLUE   = "#F59E0B";
    private static final String GREEN  = "#22C55E";
    private static final String ORANGE = "#F59E0B";
    private static final String RED    = "#EF4444";
    private static final String INFO   = "#38BDF8";

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final VBox root = new VBox();

    private AIDiagnosisController controller;

    private final Map<String, Map<String, Object>> vehicleByDisplay =
            new LinkedHashMap<>();

    private List<RoadService> activeServices = new ArrayList<>();

    private final List<CheckBox> symptomChecks =
            new ArrayList<>();

    private ComboBox<String> vehicleBox;

    private DatePicker lastServiceDatePicker;

    private TextArea problemQueryArea;

    private TextField locationField;

    private Label selectedVehicleInfoLabel;

    private Label lastServiceInfoLabel;

    private Label diagnosisLabel;
    private Label serviceLabel;
    private Label severityLabel;
    private Label priceLabel;
    private Label durationLabel;
    private Label explanationLabel;
    private Label sourceNoteLabel;

    private Button requestButton;

    private DiagnosisResult currentResult;

    private Map<String, Object> currentVehicle;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public AIDiagnosisPage() {
        buildUI();
    }

    // =========================================================
    // SCENE
    // =========================================================

    public Scene getAIDiagnosisScene() {

        return new Scene(
                root,
                1280,
                800
        );
    }

    // =========================================================
    // BUILD UI
    // =========================================================

    private void buildUI() {

        root.setStyle(
                "-fx-background-color: " + BG + ";"
        );

        root.setFillWidth(true);

        // -----------------------------------------------------
        // HEADER
        // -----------------------------------------------------

        HBox header =
                UserHeader.createHeader();

        // -----------------------------------------------------
        // SIDEBAR
        // -----------------------------------------------------

        ScrollPane sidebar =
                UserSideBar.createSidebar(
                        "AI Diagnosis"
                );

        // -----------------------------------------------------
        // CONTENT
        // -----------------------------------------------------

        VBox content =
                createContent();

        ScrollPane pageScroll =
                new ScrollPane(content);

        pageScroll.setFitToWidth(true);

        pageScroll.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        pageScroll.setStyle(
                "-fx-background-color: transparent; " +
                "-fx-background: " + BG + ";"
        );

        HBox body =
                new HBox(
                        sidebar,
                        pageScroll
                );

        HBox.setHgrow(
                pageScroll,
                Priority.ALWAYS
        );

        VBox.setVgrow(
                body,
                Priority.ALWAYS
        );

        root.getChildren().addAll(
                header,
                body
        );

        initializeControllerAndData();
    }

    // =========================================================
    // MAIN CONTENT
    // =========================================================

    private VBox createContent() {

        VBox content =
                new VBox(20);

        content.setPadding(
                new Insets(
                        30,
                        34,
                        40,
                        34
                )
        );

        content.setStyle(
                "-fx-background-color: " + BG + ";"
        );

        // -----------------------------------------------------
        // TITLE
        // -----------------------------------------------------

        Label title =
                new Label(
                        "AI Vehicle Breakdown Diagnosis"
                );

        title.setStyle(
                "-fx-text-fill: " + DARK + "; " +
                "-fx-font-family: 'Arial'; " +
                "-fx-font-size: 30px; " +
                "-fx-font-weight: bold;"
        );

        // -----------------------------------------------------
        // SUBTITLE
        // -----------------------------------------------------

        Label subtitle =
                new Label(
                        "Select your vehicle, tell AI what happened, " +
                        "add your last service date and symptoms. " +
                        "You can describe the problem in any language."
                );

        subtitle.setWrapText(true);

        subtitle.setStyle(
                "-fx-text-fill: " + TEXT + "; " +
                "-fx-font-family: 'Arial'; " +
                "-fx-font-size: 14px;"
        );

        // -----------------------------------------------------
        // DISCLOSURE
        // -----------------------------------------------------

        Label disclosure =
                new Label(
                        "Important: AI provides a preliminary suggestion only. " +
                        "It is not a confirmed mechanic diagnosis. " +
                        "For serious overheating, smoke, brake or electrical issues, " +
                        "stop safely and contact a mechanic."
                );

        disclosure.setWrapText(true);

        disclosure.setPadding(
                new Insets(
                        11,
                        14,
                        11,
                        14
                )
        );

        disclosure.setStyle(
                "-fx-background-color: #241B0B; " +
                "-fx-text-fill: #FBBF24; " +
                "-fx-border-color: #F59E0B; " +
                "-fx-border-radius: 9; " +
                "-fx-background-radius: 9; " +
                "-fx-font-family: 'Arial'; " +
                "-fx-font-size: 12px;"
        );

        // -----------------------------------------------------
        // COLUMNS
        // -----------------------------------------------------

        HBox columns =
                new HBox(18);

        VBox inputCard =
                createInputCard();

        VBox resultCard =
                createResultCard();

        HBox.setHgrow(
                inputCard,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                resultCard,
                Priority.ALWAYS
        );

        columns.getChildren().addAll(
                inputCard,
                resultCard
        );

        content.getChildren().addAll(
                title,
                subtitle,
                disclosure,
                columns
        );

        return content;
    }

    // =========================================================
    // INPUT CARD
    // =========================================================

    private VBox createInputCard() {

        VBox card =
                card();

        card.setPrefWidth(620);

        // -----------------------------------------------------
        // HEADING
        // -----------------------------------------------------

        Label heading =
                sectionTitle(
                        "Vehicle & Problem Details"
                );

        // -----------------------------------------------------
        // VEHICLE
        // -----------------------------------------------------

        Label vehicleTitle =
                fieldTitle(
                        "Your vehicle"
                );

        vehicleBox =
                new ComboBox<>();

        vehicleBox.setPromptText(
                "Loading your vehicles..."
        );

        vehicleBox.setMaxWidth(
                Double.MAX_VALUE
        );

        vehicleBox.setPrefHeight(44);

        styleCombo(
                vehicleBox
        );

        // -----------------------------------------------------
        // SELECTED VEHICLE INFO
        // -----------------------------------------------------

        selectedVehicleInfoLabel =
                new Label(
                        "Select your vehicle so AI can understand " +
                        "which vehicle you are talking about."
                );

        selectedVehicleInfoLabel.setWrapText(true);

        selectedVehicleInfoLabel.setStyle(
                "-fx-text-fill: " + INFO + "; " +
                "-fx-font-family: 'Arial'; " +
                "-fx-font-size: 12px;"
        );

        vehicleBox.valueProperty().addListener(
                (obs, oldValue, newValue) -> {

                    if (newValue == null) {

                        selectedVehicleInfoLabel.setText(
                                "Select your vehicle so AI can understand " +
                                "which vehicle you are talking about."
                        );

                        return;
                    }

                    Map<String, Object> vehicle =
                            vehicleByDisplay.get(
                                    newValue
                            );

                    currentVehicle = vehicle;

                    selectedVehicleInfoLabel.setText(
                            "AI context: " +
                            vehicleContextSummary(
                                    vehicle
                            )
                    );
                }
        );

        // -----------------------------------------------------
        // LAST SERVICE DATE
        // -----------------------------------------------------

        Label lastServiceTitle =
                fieldTitle(
                        "Last service date"
                );

        lastServiceDatePicker =
                new DatePicker();

        lastServiceDatePicker.setPromptText(
                "When was this vehicle last serviced?"
        );

        lastServiceDatePicker.setMaxWidth(
                Double.MAX_VALUE
        );

        lastServiceDatePicker.setPrefHeight(
                44
        );

        styleDatePicker(
                lastServiceDatePicker
        );

        lastServiceInfoLabel =
                new Label(
                        "AI will use this date to estimate " +
                        "whether regular maintenance may be overdue."
                );

        lastServiceInfoLabel.setWrapText(true);

        lastServiceInfoLabel.setStyle(
                "-fx-text-fill: " + TEXT + "; " +
                "-fx-font-family: 'Arial'; " +
                "-fx-font-size: 12px;"
        );

        lastServiceDatePicker.valueProperty().addListener(
                (obs, oldDate, newDate) -> {

                    if (newDate == null) {

                        lastServiceInfoLabel.setText(
                                "AI will use this date to estimate " +
                                "whether regular maintenance may be overdue."
                        );

                        return;
                    }

                    long days =
                            java.time.temporal.ChronoUnit.DAYS.between(
                                    newDate,
                                    LocalDate.now()
                            );

                    if (days < 0) {

                        lastServiceInfoLabel.setText(
                                "Please select a previous service date."
                        );

                        return;
                    }

                    lastServiceInfoLabel.setText(
                            "Last service: "
                                    + newDate.format(
                                    DATE_FORMATTER
                            )
                                    + " • "
                                    + days
                                    + " days ago."
                    );
                }
        );

        // -----------------------------------------------------
        // AI QUERY
        // -----------------------------------------------------

        Label queryTitle =
                fieldTitle(
                        "Describe your problem / Ask AI"
                );

        problemQueryArea =
                new TextArea();

        problemQueryArea.setPromptText(
                "Example: माझी कार चालू असताना अचानक बंद झाली " +
                "आणि engine खूप गरम झालं आहे. आता मी काय करू?"
        );

        problemQueryArea.setWrapText(true);

        problemQueryArea.setPrefRowCount(
                4
        );

        problemQueryArea.setMinHeight(
                105
        );

        problemQueryArea.setMaxWidth(
                Double.MAX_VALUE
        );

        styleTextArea(
                problemQueryArea
        );

        Label languageHint =
                new Label(
                        "You can type in Marathi, Hindi, English, " +
                        "Hinglish or any other language."
                );

        languageHint.setWrapText(true);

        languageHint.setStyle(
                "-fx-text-fill: " + MUTED + "; " +
                "-fx-font-family: 'Arial'; " +
                "-fx-font-size: 11px;"
        );

        // -----------------------------------------------------
        // SYMPTOMS
        // -----------------------------------------------------

        Label symptomTitle =
                fieldTitle(
                        "Select symptoms"
                );

        FlowPane symptomPane =
                new FlowPane(
                        10,
                        10
                );

        String[] fallbackSymptoms = {

                "Engine won't start",

                "Battery light ON",

                "Clicking while starting",

                "Engine overheating",

                "Smoke from engine area",

                "Unusual engine noise",

                "Loss of engine power",

                "Brake noise",

                "Brake pedal feels unusual",

                "Flat tyre / puncture",

                "Vehicle vibration",

                "Steering pulling to one side",

                "Clutch / gear shifting issue",

                "AC not cooling",

                "Electrical / lights issue"
        };

        for (String symptom : fallbackSymptoms) {

            CheckBox checkBox =
                    new CheckBox(
                            symptom
                    );

            checkBox.setStyle(
                    "-fx-text-fill: " + DARK + "; " +
                    "-fx-font-family: 'Arial'; " +
                    "-fx-font-size: 12px;"
            );

            symptomChecks.add(
                    checkBox
            );

            symptomPane.getChildren().add(
                    checkBox
            );
        }

        // -----------------------------------------------------
        // LOCATION
        // -----------------------------------------------------

        Label locationTitle =
                fieldTitle(
                        "Breakdown / current location"
                );

        locationField =
                new TextField();

        locationField.setPromptText(
                "Required when creating a service request"
        );

        locationField.setPrefHeight(
                44
        );

        styleField(
                locationField
        );

        // -----------------------------------------------------
        // ACTION BUTTONS
        // -----------------------------------------------------

        HBox actions =
                new HBox(10);

        actions.setAlignment(
                Pos.CENTER_LEFT
        );

        Button diagnose =
                primaryButton(
                        "Ask AI / Diagnose",
                        BLUE
                );

        Button reset =
                secondaryButton(
                        "Reset"
                );

        diagnose.setOnAction(
                e -> runDiagnosis()
        );

        reset.setOnAction(
                e -> resetForm()
        );

        actions.getChildren().addAll(
                diagnose,
                reset
        );

        // -----------------------------------------------------
        // CONTENT
        // -----------------------------------------------------

        card.getChildren().addAll(

                heading,

                new Separator(),

                vehicleTitle,

                vehicleBox,

                selectedVehicleInfoLabel,

                lastServiceTitle,

                lastServiceDatePicker,

                lastServiceInfoLabel,

                queryTitle,

                problemQueryArea,

                languageHint,

                symptomTitle,

                symptomPane,

                locationTitle,

                locationField,

                actions
        );

        return card;
    }

    // =========================================================
    // RESULT CARD
    // =========================================================

    private VBox createResultCard() {

        VBox card =
                card();

        card.setPrefWidth(
                520
        );

        Label heading =
                sectionTitle(
                        "AI Preliminary Result"
                );

        diagnosisLabel =
                valueLabel(
                        "No diagnosis yet"
                );

        serviceLabel =
                valueLabel(
                        "-"
                );

        severityLabel =
                valueLabel(
                        "-"
                );

        priceLabel =
                valueLabel(
                        "-"
                );

        durationLabel =
                valueLabel(
                        "-"
                );

        explanationLabel =
                new Label(
                        "Select a vehicle and describe your problem, " +
                        "then press Ask AI / Diagnose."
                );

        explanationLabel.setWrapText(true);

        explanationLabel.setStyle(
                "-fx-text-fill: " + TEXT + "; " +
                "-fx-font-family: 'Arial'; " +
                "-fx-font-size: 13px;"
        );

        sourceNoteLabel =
                new Label(
                        "AI recommendations are preliminary. " +
                        "Final diagnosis, repair and final cost are confirmed by the mechanic."
                );

        sourceNoteLabel.setWrapText(true);

        sourceNoteLabel.setStyle(
                "-fx-text-fill: " + MUTED + "; " +
                "-fx-font-family: 'Arial'; " +
                "-fx-font-size: 11px;"
        );

        requestButton =
                primaryButton(
                        "Create Service Request",
                        GREEN
                );

        requestButton.setDisable(
                true
        );

        requestButton.setMaxWidth(
                Double.MAX_VALUE
        );

        requestButton.setOnAction(
                e -> createServiceRequest()
        );

        card.getChildren().addAll(

                heading,

                new Separator(),

                infoRow(
                        "Diagnosis",
                        diagnosisLabel
                ),

                infoRow(
                        "Recommended service",
                        serviceLabel
                ),

                infoRow(
                        "Priority",
                        severityLabel
                ),

                infoRow(
                        "Configured estimate",
                        priceLabel
                ),

                infoRow(
                        "Estimated duration",
                        durationLabel
                ),

                new Separator(),

                explanationLabel,

                sourceNoteLabel,

                requestButton
        );

        return card;
    }

    // =========================================================
    // INITIALIZE DATA
    // =========================================================

    private void initializeControllerAndData() {

        try {

            controller =
                    new AIDiagnosisController();

            activeServices =
                    controller.getActiveServices();

            List<Map<String, Object>> vehicles =
                    controller.getVehicles();

            vehicleByDisplay.clear();

            vehicleBox.getItems().clear();

            int index = 1;

            for (
                    Map<String, Object> vehicle :
                    vehicles
            ) {

                String display =
                        vehicleDisplay(
                                vehicle,
                                index++
                        );

                vehicleByDisplay.put(
                        display,
                        vehicle
                );

                vehicleBox.getItems().add(
                        display
                );
            }

            if (
                    !vehicleBox
                            .getItems()
                            .isEmpty()
            ) {

                vehicleBox
                        .getSelectionModel()
                        .selectFirst();

                vehicleBox.setPromptText(
                        "Select vehicle"
                );

                currentVehicle =
                        vehicleByDisplay.get(
                                vehicleBox.getValue()
                        );

                selectedVehicleInfoLabel.setText(
                        "AI context: "
                                + vehicleContextSummary(
                                currentVehicle
                        )
                );

            } else {

                vehicleBox.setPromptText(
                        "No vehicles found - add a vehicle first"
                );
            }

        } catch (Exception e) {

            showError(
                    "Unable to load diagnosis data",
                    e.getMessage()
            );

            if (vehicleBox != null) {

                vehicleBox.setPromptText(
                        "Unable to load vehicles"
                );
            }
        }
    }

    // =========================================================
    // RUN DIAGNOSIS
    // =========================================================

    private void runDiagnosis() {

        if (controller == null) {

            showError(
                    "Diagnosis unavailable",
                    "Diagnosis controller is not available."
            );

            return;
        }

        String selectedVehicle =
                vehicleBox.getValue();

        currentVehicle =
                vehicleByDisplay.get(
                        selectedVehicle
                );

        if (currentVehicle == null) {

            showError(
                    "Select vehicle",
                    "Please select one of your registered vehicles."
            );

            return;
        }

        // -----------------------------------------------------
        // COLLECT SYMPTOMS
        // -----------------------------------------------------

        List<String> diagnosisContext =
                new ArrayList<>();

        for (
                CheckBox check :
                symptomChecks
        ) {

            if (check.isSelected()) {

                diagnosisContext.add(
                        check.getText()
                );
            }
        }

        // -----------------------------------------------------
        // USER'S NATURAL LANGUAGE QUERY
        // -----------------------------------------------------

        String userQuery =
                clean(
                        problemQueryArea.getText()
                );

        if (userQuery != null) {

            diagnosisContext.add(
                    "USER PROBLEM / QUERY: "
                            + userQuery
            );
        }

        // -----------------------------------------------------
        // LAST SERVICE DATE
        // -----------------------------------------------------

        LocalDate lastServiceDate =
                lastServiceDatePicker.getValue();

        if (lastServiceDate != null) {

            long daysSinceService =
                    java.time.temporal.ChronoUnit.DAYS.between(
                            lastServiceDate,
                            LocalDate.now()
                    );

            diagnosisContext.add(
                    "LAST SERVICE DATE: "
                            + lastServiceDate.format(
                            DATE_FORMATTER
                    )
                            + " (" +
                            daysSinceService +
                            " days ago)"
            );
        }

        // -----------------------------------------------------
        // VALIDATION
        // -----------------------------------------------------

        if (
                diagnosisContext.isEmpty()
        ) {

            showError(
                    "Tell AI what happened",
                    "Please describe your problem or select at least one symptom."
            );

            return;
        }

        try {

            currentResult =
                    controller.diagnose(
                            diagnosisContext,
                            activeServices
                    );

            controller.saveDiagnosis(
                    currentVehicle,
                    currentResult
            );

            renderResult(
                    currentResult
            );

            requestButton.setDisable(
                    false
            );

        } catch (Exception e) {

            showError(
                    "Unable to diagnose",
                    e.getMessage()
            );
        }
    }

    // =========================================================
    // RENDER RESULT
    // =========================================================

    private void renderResult(
            DiagnosisResult result
    ) {

        if (result == null) {

            return;
        }

        diagnosisLabel.setText(
                nonBlank(
                        result.getDiagnosis(),
                        "Needs inspection"
                )
        );

        serviceLabel.setText(
                nonBlank(
                        result.getRecommendedService(),
                        "Vehicle Inspection"
                )
        );

        severityLabel.setText(
                nonBlank(
                        result.getSeverity(),
                        "Normal"
                )
        );

        priceLabel.setText(
                nonBlank(
                        result.getPriceDisplay(),
                        "Not configured"
                )
        );

        durationLabel.setText(
                nonBlank(
                        result.getEstimatedDuration(),
                        "Confirmed after inspection"
                )
        );

        explanationLabel.setText(
                nonBlank(
                        result.getExplanation(),
                        "Mechanic inspection is required."
                )
        );

        String severity =
                nonBlank(
                        result.getSeverity(),
                        "Medium"
                );

        String color;

        if (
                "High".equalsIgnoreCase(
                        severity
                )
        ) {

            color = RED;

        } else if (
                "Low".equalsIgnoreCase(
                        severity
                )
        ) {

            color = GREEN;

        } else {

            color = ORANGE;
        }

        severityLabel.setStyle(
                "-fx-text-fill: " + color + "; " +
                "-fx-font-family: 'Arial'; " +
                "-fx-font-size: 13px; " +
                "-fx-font-weight: bold;"
        );
    }

    // =========================================================
    // CREATE SERVICE REQUEST
    // =========================================================

    private void createServiceRequest() {

        if (
                controller == null
                        || currentVehicle == null
                        || currentResult == null
        ) {

            showError(
                    "Run diagnosis first",
                    "Please run AI diagnosis before creating a request."
            );

            return;
        }

        String location =
                clean(
                        locationField.getText()
                );

        if (location == null) {

            showError(
                    "Location required",
                    "Please enter your current / breakdown location."
            );

            return;
        }

        try {

            String requestId =
                    controller.createServiceRequest(
                            currentVehicle,
                            location,
                            currentResult
                    );

            requestButton.setDisable(
                    true
            );

            showInfo(
                    "Service request created",
                    "Request ID: "
                            + requestId
                            + "\n\nNext step: select an available mechanic " +
                            "for this request."
            );

        } catch (Exception e) {

            showError(
                    "Unable to create service request",
                    e.getMessage()
            );
        }
    }

    // =========================================================
    // RESET
    // =========================================================

    private void resetForm() {

        for (
                CheckBox check :
                symptomChecks
        ) {

            check.setSelected(
                    false
            );
        }

        if (problemQueryArea != null) {

            problemQueryArea.clear();
        }

        if (lastServiceDatePicker != null) {

            lastServiceDatePicker.setValue(
                    null
            );
        }

        if (locationField != null) {

            locationField.clear();
        }

        currentResult = null;

        currentVehicle = null;

        diagnosisLabel.setText(
                "No diagnosis yet"
        );

        serviceLabel.setText("-");

        severityLabel.setText("-");

        severityLabel.setStyle(
                "-fx-text-fill: " + DARK + "; " +
                "-fx-font-family: 'Arial'; " +
                "-fx-font-size: 13px; " +
                "-fx-font-weight: bold;"
        );

        priceLabel.setText("-");

        durationLabel.setText("-");

        explanationLabel.setText(
                "Select a vehicle and describe your problem, " +
                "then press Ask AI / Diagnose."
        );

        requestButton.setDisable(
                true
        );

        lastServiceInfoLabel.setText(
                "AI will use this date to estimate " +
                "whether regular maintenance may be overdue."
        );
    }

    // =========================================================
    // CARD
    // =========================================================

    private VBox card() {

        VBox card =
                new VBox(13);

        card.setPadding(
                new Insets(22)
        );

        card.setStyle(
                "-fx-background-color: " + CARD + "; " +
                "-fx-border-color: " + BORDER + "; " +
                "-fx-border-radius: 14; " +
                "-fx-background-radius: 14;"
        );

        return card;
    }

    // =========================================================
    // SECTION TITLE
    // =========================================================

    private Label sectionTitle(
            String text
    ) {

        Label label =
                new Label(text);

        label.setStyle(
                "-fx-text-fill: " + DARK + "; " +
                "-fx-font-family: 'Arial'; " +
                "-fx-font-size: 19px; " +
                "-fx-font-weight: bold;"
        );

        return label;
    }

    // =========================================================
    // FIELD TITLE
    // =========================================================

    private Label fieldTitle(
            String text
    ) {

        Label label =
                new Label(text);

        label.setStyle(
                "-fx-text-fill: " + DARK + "; " +
                "-fx-font-family: 'Arial'; " +
                "-fx-font-size: 13px; " +
                "-fx-font-weight: bold;"
        );

        return label;
    }

    // =========================================================
    // VALUE LABEL
    // =========================================================

    private Label valueLabel(
            String text
    ) {

        Label label =
                new Label(text);

        label.setWrapText(true);

        label.setStyle(
                "-fx-text-fill: " + DARK + "; " +
                "-fx-font-family: 'Arial'; " +
                "-fx-font-size: 13px; " +
                "-fx-font-weight: bold;"
        );

        return label;
    }

    // =========================================================
    // INFO ROW
    // =========================================================

    private HBox infoRow(
            String title,
            Label value
    ) {

        HBox row =
                new HBox(10);

        row.setAlignment(
                Pos.TOP_LEFT
        );

        Label key =
                new Label(title);

        key.setMinWidth(
                145
        );

        key.setStyle(
                "-fx-text-fill: " + TEXT + "; " +
                "-fx-font-family: 'Arial'; " +
                "-fx-font-size: 12px; " +
                "-fx-font-weight: bold;"
        );

        HBox.setHgrow(
                value,
                Priority.ALWAYS
        );

        row.getChildren().addAll(
                key,
                value
        );

        return row;
    }

    // =========================================================
    // PRIMARY BUTTON
    // =========================================================

    private Button primaryButton(
            String text,
            String color
    ) {

        Button button =
                new Button(text);

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

        button.setStyle(
                "-fx-background-color: " + color + "; " +
                "-fx-text-fill: white; " +
                "-fx-font-family: 'Arial'; " +
                "-fx-font-size: 13px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 8; " +
                "-fx-cursor: hand;"
        );

        return button;
    }

    // =========================================================
    // SECONDARY BUTTON
    // =========================================================

    private Button secondaryButton(
            String text
    ) {

        Button button =
                new Button(text);

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

        button.setStyle(
                "-fx-background-color: " + WHITE + "; " +
                "-fx-text-fill: " + DARK + "; " +
                "-fx-font-family: 'Arial'; " +
                "-fx-font-size: 13px; " +
                "-fx-font-weight: bold; " +
                "-fx-border-color: " + BORDER + "; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8; " +
                "-fx-cursor: hand;"
        );

        return button;
    }

    // =========================================================
    // COMBOBOX STYLE
    // =========================================================

    private void styleCombo(
            ComboBox<String> combo
    ) {

        combo.setStyle(
                "-fx-background-color: " + WHITE + "; " +
                "-fx-border-color: " + BORDER + "; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8; " +
                "-fx-font-family: 'Arial'; " +
                "-fx-font-size: 13px;"
        );

        combo.setButtonCell(
                createComboCell()
        );

        combo.setCellFactory(
                listView ->
                        createComboCell()
        );
    }

    // =========================================================
    // COMBOBOX CELL
    // =========================================================

    private ListCell<String> createComboCell() {

        return new ListCell<>() {

            @Override
            protected void updateItem(
                    String item,
                    boolean empty
            ) {

                super.updateItem(
                        item,
                        empty
                );

                if (
                        empty
                                || item == null
                ) {

                    setText(null);

                    setStyle(
                            "-fx-background-color: "
                                    + WHITE
                                    + ";"
                    );

                } else {

                    setText(
                            item
                    );

                    setStyle(
                            "-fx-background-color: "
                                    + WHITE
                                    + "; "
                                    + "-fx-text-fill: "
                                    + DARK
                                    + "; "
                                    + "-fx-font-family: 'Arial'; "
                                    + "-fx-font-size: 13px; "
                                    + "-fx-padding: 8 10 8 10;"
                    );
                }
            }
        };
    }

    // =========================================================
    // DATE PICKER STYLE
    // =========================================================

    private void styleDatePicker(
            DatePicker picker
    ) {

        picker.setStyle(
                "-fx-background-color: " + WHITE + "; " +
                "-fx-border-color: " + BORDER + "; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8; " +
                "-fx-font-family: 'Arial'; " +
                "-fx-font-size: 13px;"
        );

        picker.setEditable(
                false
        );
    }

    // =========================================================
    // TEXT FIELD STYLE
    // =========================================================

    private void styleField(
            TextField field
    ) {

        field.setStyle(
                "-fx-background-color: " + WHITE + "; " +
                "-fx-text-fill: " + DARK + "; " +
                "-fx-prompt-text-fill: " + MUTED + "; " +
                "-fx-border-color: " + BORDER + "; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8; " +
                "-fx-font-family: 'Arial'; " +
                "-fx-font-size: 13px; " +
                "-fx-padding: 0 12 0 12;"
        );
    }

    // =========================================================
    // TEXT AREA STYLE
    // =========================================================

    private void styleTextArea(
            TextArea area
    ) {

        area.setStyle(
                "-fx-control-inner-background: " + WHITE + "; " +
                "-fx-background-color: " + WHITE + "; " +
                "-fx-text-fill: " + DARK + "; " +
                "-fx-prompt-text-fill: " + MUTED + "; " +
                "-fx-border-color: " + BORDER + "; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8; " +
                "-fx-font-family: 'Arial'; " +
                "-fx-font-size: 13px; " +
                "-fx-padding: 10;"
        );
    }

    // =========================================================
    // VEHICLE DISPLAY
    // =========================================================

    private String vehicleDisplay(
            Map<String, Object> vehicle,
            int index
    ) {

        String number =
                firstString(
                        vehicle,
                        "vehicleNumber",
                        "registrationNumber"
                );

        String brand =
                firstString(
                        vehicle,
                        "brand",
                        "make",
                        "name"
                );

        String model =
                firstString(
                        vehicle,
                        "model"
                );

        String name =
                (
                        (brand == null
                                ? ""
                                : brand)
                                + " "
                                + (model == null
                                ? ""
                                : model)
                ).trim();

        if (name.isEmpty()) {

            name =
                    "Vehicle " + index;
        }

        return number == null
                ? name
                : name + " • " + number;
    }

    // =========================================================
    // VEHICLE CONTEXT
    // =========================================================

    private String vehicleContextSummary(
            Map<String, Object> vehicle
    ) {

        if (vehicle == null) {

            return "Vehicle not selected";
        }

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

            text.append(
                    brand
            );
        }

        if (model != null) {

            if (text.length() > 0) {

                text.append(" ");
            }

            text.append(
                    model
            );
        }

        if (number != null) {

            text.append(
                    " • "
            ).append(
                    number
            );
        }

        if (year != null) {

            text.append(
                    " • Year "
            ).append(
                    year
            );
        }

        if (text.length() == 0) {

            return "Vehicle selected";
        }

        return text.toString();
    }

    // =========================================================
    // FIRST STRING
    // =========================================================

    private String firstString(
            Map<String, Object> data,
            String... keys
    ) {

        if (data != null) {

            for (String key : keys) {

                Object value =
                        data.get(
                                key
                        );

                if (
                        value != null
                                && clean(
                                value.toString()
                        ) != null
                ) {

                    return value
                            .toString()
                            .trim();
                }
            }
        }

        return null;
    }

    // =========================================================
    // NON-BLANK
    // =========================================================

    private String nonBlank(
            String value,
            String fallback
    ) {

        return clean(
                value
        ) == null
                ? fallback
                : value.trim();
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
    // INFO ALERT
    // =========================================================

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

    // =========================================================
    // ERROR ALERT
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
                clean(
                        message
                ) == null
                        ? "Unknown error."
                        : message
        );

        alert.showAndWait();
    }
}