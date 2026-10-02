package project.ui.admin.SOSRequests;

import com.google.cloud.firestore.Firestore;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.Cursor;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import javafx.scene.paint.Color;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import project.controller.admin.MechanicController;
import project.controller.admin.SOSRequestController;

import project.firebase.FirebaseConfig;

import project.model.Mechanic;
import project.model.SOSRequest;

import project.ui.admin.DashBoard.AdminSectionPage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AssignSOSMechanicPage
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

    private static final String BLUE =
            "#F59E0B";

    private static final String GREEN =
            "#22C55E";

    private static final String ORANGE =
            "#F59E0B";

    private static final String RED =
            "#EF4444";

    private static final String BORDER =
            "#F59E0B";

    // =====================================================
    // REQUEST
    // =====================================================

    private final SOSRequest request;

    // =====================================================
    // CONTROLLERS
    // =====================================================

    private SOSRequestController sosController;

    private MechanicController mechanicController;

    // =====================================================
    // UI
    // =====================================================

    private ComboBox<Mechanic> mechanicComboBox;

    private Label selectedMechanicLabel;

    private Label selectedMechanicDetailsLabel;

    private Label availableCountLabel;

    private Label statusMessage;

    private Button assignButton;

    // =====================================================
    // DATA
    // =====================================================

    private final List<Mechanic> activeMechanics =
            new ArrayList<>();

    // =====================================================
    // CALLBACKS
    // =====================================================

    private Runnable onBack;

    private Runnable onSuccess;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public AssignSOSMechanicPage(
            SOSRequest request
    ) {

        this.request =
                request;
    }

    // =====================================================
    // CONSTRUCTOR - BACK
    // =====================================================

    public AssignSOSMechanicPage(
            SOSRequest request,
            Runnable onBack
    ) {

        this.request =
                request;

        this.onBack =
                onBack;
    }

    // =====================================================
    // CONSTRUCTOR - BACK + SUCCESS
    // =====================================================

    public AssignSOSMechanicPage(
            SOSRequest request,
            Runnable onBack,
            Runnable onSuccess
    ) {

        this.request =
                request;

        this.onBack =
                onBack;

        this.onSuccess =
                onSuccess;
    }

    // =====================================================
    // VIEW
    // =====================================================

    @Override
    public VBox getView() {

        VBox root =
                new VBox(20);

        root.setPadding(
                new Insets(
                        30,
                        32,
                        32,
                        32
                )
        );

        root.setStyle(
                "-fx-background-color: "
                        + BG
                        + ";"
        );

        // =================================================
        // VALIDATE REQUEST
        // =================================================

        if (request == null
                ||
                clean(
                        request.getSosId()
                ) == null) {

            return createErrorView(
                    "SOS request information is missing."
            );
        }

        // =================================================
        // FIREBASE
        // =================================================

        try {

            Firestore firestore =
                    FirebaseConfig.getFirestore();

            sosController =
                    new SOSRequestController(
                            firestore
                    );

            mechanicController =
                    new MechanicController(
                            firestore
                    );

        } catch (Exception e) {

            e.printStackTrace();

            return createErrorView(
                    "Unable to connect to Firebase."
            );
        }

        // =================================================
        // BACK
        // =================================================

        Button backButton =
                new Button(
                        "← Back to SOS Details"
                );

        styleOutlineButton(
                backButton
        );

        backButton.setOnAction(
                event ->
                        goBack()
        );

        // =================================================
        // HEADER
        // =================================================

        VBox header =
                createHeader();

        // =================================================
        // CONTENT
        // =================================================

        VBox assignmentContent =
                createAssignmentContent();

        VBox.setVgrow(
                assignmentContent,
                Priority.ALWAYS
        );

        root.getChildren()
                .addAll(
                        backButton,
                        header,
                        assignmentContent
                );

        // =================================================
        // LOAD ACTIVE MECHANICS
        // =================================================

        loadMechanics();

        return root;
    }

    // =====================================================
    // BACK
    // =====================================================

    private void goBack() {

        if (onBack != null) {

            onBack.run();
        }
    }

    // =====================================================
    // HEADER
    // =====================================================

    private VBox createHeader() {

        VBox header =
                new VBox(6);

        boolean reassign =
                request.isAssigned()
                        &&
                request.hasMechanic();

        Label title =
                new Label(
                        reassign
                                ? "Reassign SOS Mechanic"
                                : "Assign SOS Mechanic"
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
                        reassign
                                ? "Select another active mechanic for this SOS emergency request."
                                : "Assign an active mechanic to this emergency roadside assistance request."
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
    // ASSIGNMENT CONTENT
    // =====================================================

    private VBox createAssignmentContent() {

        VBox content =
                new VBox(18);

        VBox requestCard =
                createRequestCard();

        VBox currentAssignment =
                createCurrentAssignmentCard();

        VBox mechanicCard =
                createMechanicSelectionCard();

        HBox actions =
                createActions();

        statusMessage =
                new Label();

        statusMessage.setWrapText(
                true
        );

        statusMessage.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        13
                )
        );

        content.getChildren()
                .add(
                        requestCard
                );

        if (currentAssignment != null) {

            content.getChildren()
                    .add(
                            currentAssignment
                    );
        }

        content.getChildren()
                .addAll(
                        mechanicCard,
                        actions,
                        statusMessage
                );

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
                "-fx-background-color: transparent;"
                        + "-fx-background: transparent;"
        );

        VBox wrapper =
                new VBox(
                        scrollPane
                );

        VBox.setVgrow(
                scrollPane,
                Priority.ALWAYS
        );

        return wrapper;
    }

    // =====================================================
    // REQUEST CARD
    // =====================================================

    private VBox createRequestCard() {

        VBox card =
                createCard();

        Label title =
                createSectionTitle(
                        "SOS Request Information"
                );

        HBox firstRow =
                new HBox(15);

        VBox requestId =
                createInfoBox(
                        "REQUEST ID",
                        request.getSosId()
                );

        VBox customer =
                createInfoBox(
                        "CUSTOMER",
                        firstNonBlank(
                                request.getCustomerName(),
                                request.getUserName(),
                                request.getCustomerId(),
                                "Customer"
                        )
                );

        VBox status =
                createInfoBox(
                        "STATUS",
                        firstNonBlank(
                                request.getStatus(),
                                "Pending"
                        )
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
                status,
                Priority.ALWAYS
        );

        firstRow.getChildren()
                .addAll(
                        requestId,
                        customer,
                        status
                );

        HBox secondRow =
                new HBox(15);

        VBox vehicle =
                createInfoBox(
                        "VEHICLE",
                        firstNonBlank(
                                request.getVehicleNumber(),
                                request.getVehicleId(),
                                "Vehicle not provided"
                        )
                );

        VBox emergency =
                createInfoBox(
                        "EMERGENCY",
                        firstNonBlank(
                                request.getEmergencyType(),
                                request.getDescription(),
                                "Emergency Roadside Assistance"
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
                vehicle,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                emergency,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                location,
                Priority.ALWAYS
        );

        secondRow.getChildren()
                .addAll(
                        vehicle,
                        emergency,
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
    // CURRENT ASSIGNMENT
    // =====================================================

    private VBox createCurrentAssignmentCard() {

        if (!request.hasMechanic()) {

            return null;
        }

        VBox card =
                createCard();

        Label title =
                createSectionTitle(
                        "Current Assignment"
                );

        Label info =
                new Label(
                        "This SOS request is currently assigned to:"
                );

        info.setTextFill(
                Color.web(
                        TEXT
                )
        );

        info.setFont(
                Font.font(
                        "Arial",
                        13
                )
        );

        HBox row =
                new HBox(15);

        VBox mechanic =
                createInfoBox(
                        "MECHANIC",
                        firstNonBlank(
                                request.getMechanicName(),
                                request.getResponderName(),
                                "Mechanic"
                        )
                );

        VBox mechanicId =
                createInfoBox(
                        "MECHANIC ID",
                        firstNonBlank(
                                request.getMechanicId(),
                                request.getResponderId(),
                                "-"
                        )
                );

        VBox assignedDate =
                createInfoBox(
                        "ASSIGNED DATE",
                        firstNonBlank(
                                request.getAssignedDate(),
                                "-"
                        )
                );

        HBox.setHgrow(
                mechanic,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                mechanicId,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                assignedDate,
                Priority.ALWAYS
        );

        row.getChildren()
                .addAll(
                        mechanic,
                        mechanicId,
                        assignedDate
                );

        Label warning =
                new Label(
                        "Selecting another mechanic will replace the current assignment."
                );

        warning.setWrapText(
                true
        );

        warning.setPadding(
                new Insets(10)
        );

        warning.setStyle(
                "-fx-background-color: #2B210D;"
                        + "-fx-background-radius: 8;"
                        + "-fx-text-fill: "
                        + ORANGE
                        + ";"
                        + "-fx-font-weight: bold;"
                        + "-fx-font-size: 11px;"
        );

        card.getChildren()
                .addAll(
                        title,
                        info,
                        new Separator(),
                        row,
                        warning
                );

        return card;
    }

    // =====================================================
    // MECHANIC SELECTION
    // =====================================================

    private VBox createMechanicSelectionCard() {

        VBox card =
                createCard();

        HBox titleRow =
                new HBox();

        titleRow.setAlignment(
                Pos.CENTER_LEFT
        );

        Label title =
                createSectionTitle(
                        "Select Active Mechanic"
                );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        availableCountLabel =
                new Label(
                        "Loading..."
                );

        availableCountLabel.setTextFill(
                Color.web(
                        GREEN
                )
        );

        availableCountLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        11
                )
        );

        titleRow.getChildren()
                .addAll(
                        title,
                        spacer,
                        availableCountLabel
                );

        Label description =
                new Label(
                        "Only active mechanics can be assigned to an SOS request."
                );

        description.setTextFill(
                Color.web(
                        TEXT
                )
        );

        description.setFont(
                Font.font(
                        "Arial",
                        13
                )
        );

        mechanicComboBox =
                new ComboBox<>();

        mechanicComboBox.setPrefHeight(
                48
        );

        mechanicComboBox.setMaxWidth(
                Double.MAX_VALUE
        );

        mechanicComboBox.setPromptText(
                "Select an active mechanic"
        );

        styleComboBox(
                mechanicComboBox
        );

        // =================================================
        // DROPDOWN CELL
        // =================================================

        mechanicComboBox.setCellFactory(
                listView ->
                        new ListCell<Mechanic>() {

                            @Override
                            protected void updateItem(
                                    Mechanic mechanic,
                                    boolean empty
                            ) {

                                super.updateItem(
                                        mechanic,
                                        empty
                                );

                                if (empty
                                        ||
                                        mechanic == null) {

                                    setText(
                                            null
                                    );

                                    return;
                                }

                                String name =
                                        firstNonBlank(
                                                mechanic.getName(),
                                                "Mechanic"
                                        );

                                String specialization =
                                        firstNonBlank(
                                                mechanic.getSpecialization(),
                                                "General Service"
                                        );

                                String city =
                                        clean(
                                                mechanic.getCity()
                                        );

                                String text =
                                        name
                                                + "  •  "
                                                + specialization;

                                if (city != null) {

                                    text +=
                                            "  •  "
                                                    + city;
                                }

                                setText(
                                        text
                                );
                            }
                        }
        );

        // =================================================
        // SELECTED CELL
        // =================================================

        mechanicComboBox.setButtonCell(
                new ListCell<Mechanic>() {

                    @Override
                    protected void updateItem(
                            Mechanic mechanic,
                            boolean empty
                    ) {

                        super.updateItem(
                                mechanic,
                                empty
                        );

                        if (empty
                                ||
                                mechanic == null) {

                            setText(
                                    "Select an active mechanic"
                            );

                            return;
                        }

                        setText(
                                firstNonBlank(
                                        mechanic.getName(),
                                        "Mechanic"
                                )
                        );
                    }
                }
        );

        // =================================================
        // DETAILS
        // =================================================

        selectedMechanicLabel =
                new Label(
                        "No mechanic selected"
                );

        selectedMechanicLabel.setTextFill(
                Color.web(
                        TEXT
                )
        );

        selectedMechanicLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        14
                )
        );

        selectedMechanicDetailsLabel =
                new Label(
                        "Select a mechanic to view details."
                );

        selectedMechanicDetailsLabel.setWrapText(
                true
        );

        selectedMechanicDetailsLabel.setTextFill(
                Color.web(
                        TEXT
                )
        );

        selectedMechanicDetailsLabel.setFont(
                Font.font(
                        "Arial",
                        12
                )
        );

        mechanicComboBox
                .valueProperty()
                .addListener(
                        (
                                observable,
                                oldMechanic,
                                newMechanic
                        ) -> updateSelectedMechanic(
                                newMechanic
                        )
                );

        card.getChildren()
                .addAll(
                        titleRow,
                        description,
                        new Separator(),
                        mechanicComboBox,
                        selectedMechanicLabel,
                        selectedMechanicDetailsLabel
                );

        return card;
    }

    // =====================================================
    // ACTIONS
    // =====================================================

    private HBox createActions() {

        HBox actions =
                new HBox(12);

        actions.setAlignment(
                Pos.CENTER_RIGHT
        );

        Button cancel =
                new Button(
                        "Cancel"
                );

        styleOutlineButton(
                cancel
        );

        cancel.setOnAction(
                event ->
                        goBack()
        );

        boolean reassign =
                request.isAssigned()
                        &&
                request.hasMechanic();

        assignButton =
                new Button(
                        reassign
                                ? "Reassign Mechanic"
                                : "Assign SOS Mechanic"
                );

        stylePrimaryButton(
                assignButton,
                BLUE
        );

        assignButton.setDisable(
                true
        );

        assignButton.setOnAction(
                event ->
                        assignMechanic()
        );

        actions.getChildren()
                .addAll(
                        cancel,
                        assignButton
                );

        return actions;
    }

    // =====================================================
    // LOAD ACTIVE MECHANICS
    // =====================================================

    private void loadMechanics() {

        if (mechanicComboBox == null) {

            return;
        }

        mechanicComboBox
                .getItems()
                .clear();

        activeMechanics.clear();

        try {

            List<Mechanic> mechanics =
                    mechanicController
                            .getAllMechanics();

            if (mechanics == null) {

                mechanics =
                        new ArrayList<>();
            }

            // =================================================
            // FILTER ACTIVE ONLY
            //
            // Missing status is treated as Active for old
            // registration documents.
            // =================================================

            for (Mechanic mechanic :
                    mechanics) {

                if (mechanic == null) {

                    continue;
                }

                if (!isActiveMechanic(
                        mechanic
                )) {

                    continue;
                }

                if (clean(
                        mechanic.getMechanicId()
                ) == null) {

                    continue;
                }

                activeMechanics.add(
                        mechanic
                );
            }

            // =================================================
            // SORT BY NAME
            // =================================================

            activeMechanics.sort(
                    Comparator.comparing(
                            mechanic ->
                                    firstNonBlank(
                                            mechanic.getName(),
                                            ""
                                    )
                                            .toLowerCase()
                    )
            );

            mechanicComboBox
                    .getItems()
                    .addAll(
                            activeMechanics
                    );

            // =================================================
            // COUNT
            // =================================================

            if (availableCountLabel != null) {

                availableCountLabel.setText(
                        activeMechanics.size()
                                + (
                                activeMechanics.size() == 1
                                        ? " active mechanic"
                                        : " active mechanics"
                        )
                );
            }

            // =================================================
            // EMPTY
            // =================================================

            if (activeMechanics.isEmpty()) {

                mechanicComboBox.setDisable(
                        true
                );

                assignButton.setDisable(
                        true
                );

                setStatus(
                        "No active mechanics are currently available.",
                        RED
                );

                return;
            }

            mechanicComboBox.setDisable(
                    false
            );

            // =================================================
            // PRESELECT CURRENT MECHANIC
            //
            // Only if currently assigned mechanic is still
            // active.
            // =================================================

            Mechanic current =
                    findCurrentAssignedMechanic();

            if (current != null) {

                mechanicComboBox.setValue(
                        current
                );

                setStatus(
                        "Current active mechanic is selected. "
                                + "Choose another mechanic only if you want to reassign.",
                        ORANGE
                );

            } else if (request.hasMechanic()) {

                setStatus(
                        "The currently assigned mechanic is not active or no longer available. "
                                + "Select another active mechanic.",
                        ORANGE
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            mechanicComboBox.setDisable(
                    true
            );

            assignButton.setDisable(
                    true
            );

            if (availableCountLabel != null) {

                availableCountLabel.setText(
                        "Unable to load"
                );
            }

            setStatus(
                    "Unable to load mechanics: "
                            + safe(
                            e.getMessage()
                    ),
                    RED
            );
        }
    }

    // =====================================================
    // CURRENT ASSIGNED MECHANIC
    // =====================================================

    private Mechanic findCurrentAssignedMechanic() {

        String assignedId =
                normalizeId(
                        firstNonBlank(
                                request.getMechanicId(),
                                request.getResponderId()
                        )
                );

        if (assignedId == null) {

            return null;
        }

        for (Mechanic mechanic :
                activeMechanics) {

            String mechanicId =
                    normalizeId(
                            mechanic.getMechanicId()
                    );

            String mechanicEmail =
                    normalizeId(
                            mechanic.getEmail()
                    );

            if (sameId(
                    assignedId,
                    mechanicId
            )
                    ||
                    sameId(
                            assignedId,
                            mechanicEmail
                    )) {

                return mechanic;
            }
        }

        return null;
    }

    // =====================================================
    // SELECTED MECHANIC DETAILS
    // =====================================================

    private void updateSelectedMechanic(
            Mechanic mechanic
    ) {

        if (mechanic == null) {

            selectedMechanicLabel.setText(
                    "No mechanic selected"
            );

            selectedMechanicLabel.setTextFill(
                    Color.web(
                            TEXT
                    )
            );

            selectedMechanicDetailsLabel.setText(
                    "Select a mechanic to view details."
            );

            assignButton.setDisable(
                    true
            );

            return;
        }

        selectedMechanicLabel.setText(
                "Selected: "
                        + firstNonBlank(
                        mechanic.getName(),
                        "Mechanic"
                )
        );

        selectedMechanicLabel.setTextFill(
                Color.web(
                        GREEN
                )
        );

        String specialization =
                firstNonBlank(
                        mechanic.getSpecialization(),
                        "General Service"
                );

        String city =
                firstNonBlank(
                        mechanic.getCity(),
                        "City not provided"
                );

        String experience =
                formatExperience(
                        mechanic.getExperience()
                );

        String phone =
                firstNonBlank(
                        mechanic.getPhone(),
                        "Phone not provided"
                );

        selectedMechanicDetailsLabel.setText(
                "Specialization: "
                        + specialization
                        + "\n"
                        + "City: "
                        + city
                        + "\n"
                        + "Experience: "
                        + experience
                        + "\n"
                        + "Phone: "
                        + phone
                        + "\n"
                        + "Status: Active"
        );

        assignButton.setDisable(
                false
        );
    }

    // =====================================================
    // ASSIGN / REASSIGN
    // =====================================================

    private void assignMechanic() {

        Mechanic selected =
                mechanicComboBox
                        .getValue();

        if (selected == null) {

            setStatus(
                    "Please select an active mechanic before assigning.",
                    RED
            );

            return;
        }

        if (!isActiveMechanic(
                selected
        )) {

            setStatus(
                    "Selected mechanic is not active.",
                    RED
            );

            return;
        }

        String mechanicId =
                normalizeId(
                        clean(
                                selected.getEmail()
                        )
                );

        if (mechanicId == null) {

            mechanicId =
                    normalizeId(
                            clean(
                                    selected.getMechanicId()
                            )
                    );
        }

        if (mechanicId == null) {

            setStatus(
                    "Selected mechanic does not have a valid ID.",
                    RED
            );

            return;
        }

        // =================================================
        // LOAD FRESH SOS BEFORE ASSIGNING
        //
        // Avoid stale page assigning a mechanic after
        // another user already Accepted / Started it.
        // =================================================

        SOSRequest freshRequest =
                sosController
                        .getRequestById(
                                request.getSosId()
                        );

        if (freshRequest == null) {

            setStatus(
                    "SOS request could not be found.",
                    RED
            );

            return;
        }

        // =================================================
        // ASSIGN ONLY:
        //
        // Pending
        // Assigned
        // =================================================

        if (!freshRequest.isPending()
                &&
                !freshRequest.isAssigned()) {

            setStatus(
                    "Assignment is no longer allowed because this SOS request is "
                            + safe(
                            freshRequest.getStatus()
                    )
                            + ".",
                    RED
            );

            assignButton.setDisable(
                    true
            );

            return;
        }

        // =================================================
        // SAME MECHANIC?
        // =================================================

        String currentMechanicId =
                normalizeId(
                        firstNonBlank(
                                freshRequest.getMechanicId(),
                                freshRequest.getResponderId()
                        )
                );

        if (freshRequest.isAssigned()
                &&
                sameId(
                        currentMechanicId,
                        mechanicId
                )) {

            setStatus(
                    "This mechanic is already assigned to the SOS request.",
                    ORANGE
            );

            return;
        }

        // =================================================
        // SAVE TO FIREBASE
        // =================================================

        assignButton.setDisable(
                true
        );

        setStatus(
                "Assigning mechanic...",
                BLUE
        );

        try {

            boolean success =
                    sosController
                            .assignMechanic(
                                    freshRequest.getSosId(),
                                    mechanicId,
                                    firstNonBlank(
                                            selected.getName(),
                                            "Mechanic"
                                    )
                            );

            if (!success) {

                setStatus(
                        "The mechanic could not be assigned. "
                                + "The SOS status may have changed.",
                        RED
                );

                assignButton.setDisable(
                        false
                );

                return;
            }

            // =================================================
            // SUCCESS
            //
            // Firebase now contains:
            //
            // mechanicId
            // mechanicName
            // status = Assigned
            // assignedDate
            // updatedAt
            // =================================================

            setStatus(
                    "Mechanic assigned successfully. SOS status is now Assigned.",
                    GREEN
            );

            if (onSuccess != null) {

                onSuccess.run();

                return;
            }

            if (onBack != null) {

                onBack.run();

                return;
            }

            assignButton.setDisable(
                    true
            );

        } catch (Exception e) {

            e.printStackTrace();

            assignButton.setDisable(
                    false
            );

            setStatus(
                    "Assignment failed: "
                            + safe(
                            e.getMessage()
                    ),
                    RED
            );
        }
    }

    // =====================================================
    // ACTIVE MECHANIC
    // =====================================================

    private boolean isActiveMechanic(
            Mechanic mechanic
    ) {

        if (mechanic == null) {

            return false;
        }

        String status =
                clean(
                        mechanic.getStatus()
                );

        /*
         * Old mechanic registration may not contain status.
         * Missing status = Active for compatibility.
         */
        if (status == null) {

            return true;
        }

        return status.equalsIgnoreCase(
                "Active"
        );
    }

    // =====================================================
    // STATUS MESSAGE
    // =====================================================

    private void setStatus(
            String message,
            String color
    ) {

        if (statusMessage == null) {

            return;
        }

        statusMessage.setText(
                message == null
                        ? ""
                        : message
        );

        statusMessage.setTextFill(
                Color.web(
                        color
                )
        );
    }

    // =====================================================
    // CARD
    // =====================================================

    private VBox createCard() {

        VBox card =
                new VBox(13);

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

        return card;
    }

    // =====================================================
    // SECTION TITLE
    // =====================================================

    private Label createSectionTitle(
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
                        20
                )
        );

        return label;
    }

    // =====================================================
    // INFO BOX
    // =====================================================

    private VBox createInfoBox(
            String title,
            String value
    ) {

        VBox box =
                new VBox(5);

        box.setPadding(
                new Insets(
                        12
                )
        );

        box.setMinWidth(
                150
        );

        box.setStyle(
                "-fx-background-color: "
                        + SECONDARY
                        + ";"
                        + "-fx-background-radius: 9;"
                        + "-fx-border-color: "
                        + BORDER
                        + ";"
                        + "-fx-border-radius: 9;"
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
                        10
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
                        13
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
    // COMBO STYLE
    // =====================================================

    private void styleComboBox(
            ComboBox<Mechanic> comboBox
    ) {

        comboBox.setStyle(
                "-fx-background-color: "
                        + SURFACE
                        + ";"
                        + "-fx-border-color: "
                        + BORDER
                        + ";"
                        + "-fx-border-radius: 9;"
                        + "-fx-background-radius: 9;"
                        + "-fx-font-size: 13px;"
        );
    }

    // =====================================================
    // OUTLINE BUTTON
    // =====================================================

    private void styleOutlineButton(
            Button button
    ) {

        button.setTextFill(
                Color.web(
                        BLUE
                )
        );

        button.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        13
                )
        );

        button.setPadding(
                new Insets(
                        10,
                        17,
                        10,
                        17
                )
        );

        button.setCursor(
                Cursor.HAND
        );

        String normal =
                "-fx-background-color: "
                        + SURFACE
                        + ";"
                        + "-fx-border-color: "
                        + BLUE
                        + ";"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;";

        button.setStyle(
                normal
        );

        button.setOnMouseEntered(
                event ->
                        button.setStyle(
                                "-fx-background-color: #172033;"
                                        + "-fx-border-color: "
                                        + BLUE
                                        + ";"
                                        + "-fx-border-radius: 8;"
                                        + "-fx-background-radius: 8;"
                        )
        );

        button.setOnMouseExited(
                event ->
                        button.setStyle(
                                normal
                        )
        );
    }

    // =====================================================
    // PRIMARY BUTTON
    // =====================================================

    private void stylePrimaryButton(
            Button button,
            String color
    ) {

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

        button.setPadding(
                new Insets(
                        10,
                        17,
                        10,
                        17
                )
        );

        button.setCursor(
                Cursor.HAND
        );

        String normal =
                "-fx-background-color: "
                        + color
                        + ";"
                        + "-fx-background-radius: 8;";

        button.setStyle(
                normal
        );

        button.setOnMouseEntered(
                event -> {

                    if (!button.isDisabled()) {

                        button.setStyle(
                                "-fx-background-color: #F59E0B;"
                                        + "-fx-background-radius: 8;"
                        );
                    }
                }
        );

        button.setOnMouseExited(
                event ->
                        button.setStyle(
                                normal
                        )
        );
    }

    // =====================================================
    // EXPERIENCE
    // =====================================================

    private String formatExperience(
            String experience
    ) {

        String value =
                clean(
                        experience
                );

        if (value == null
                ||
                value.equals("0")
                ||
                value.equals("0.0")) {

            return "Not provided";
        }

        if (value.toLowerCase()
                .contains(
                        "year"
                )) {

            return value;
        }

        return value
                + " years";
    }

    // =====================================================
    // SAME ID
    // =====================================================

    private boolean sameId(
            String first,
            String second
    ) {

        first =
                normalizeId(
                        first
                );

        second =
                normalizeId(
                        second
                );

        return first != null
                &&
                second != null
                &&
                first.equals(
                        second
                );
    }

    // =====================================================
    // NORMALIZE ID
    // =====================================================

    private String normalizeId(
            String value
    ) {

        value =
                clean(
                        value
                );

        if (value == null) {

            return null;
        }

        if (value.contains("@")) {

            return value.toLowerCase();
        }

        return value;
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

    // =====================================================
    // ERROR VIEW
    // =====================================================

    private VBox createErrorView(
            String message
    ) {

        VBox box =
                new VBox(12);

        box.setAlignment(
                Pos.CENTER
        );

        box.setPadding(
                new Insets(
                        40
                )
        );

        box.setStyle(
                "-fx-background-color: "
                        + BG
                        + ";"
        );

        Label icon =
                new Label(
                        "!"
                );

        icon.setTextFill(
                Color.web(
                        RED
                )
        );

        icon.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        34
                )
        );

        Label title =
                new Label(
                        "SOS Assignment Error"
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
                        22
                )
        );

        Label description =
                new Label(
                        safe(
                                message
                        )
                );

        description.setWrapText(
                true
        );

        description.setTextFill(
                Color.web(
                        RED
                )
        );

        description.setFont(
                Font.font(
                        "Arial",
                        13
                )
        );

        box.getChildren()
                .addAll(
                        icon,
                        title,
                        description
                );

        return box;
    }
}