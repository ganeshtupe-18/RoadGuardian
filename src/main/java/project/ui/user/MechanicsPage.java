package project.ui.user;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.Scene;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputDialog;

import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import javafx.scene.paint.Color;

import javafx.scene.shape.Circle;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import project.dao.user.ServiceCatalogDAO;
import project.dao.user.ServiceRequestDAO;
import project.dao.user.VehicleDAO;

import project.firebase.FirebaseConfig;

import project.model.Mechanic;
import project.model.RoadService;
import project.util.IconUtil;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MechanicsPage {

    // =====================================================
    // COLORS
    // =====================================================

    private static final String BG =
            "#0F0F0F";

    private static final String WHITE =
            "#1A1A1A";

    private static final String CARD =
            "#1A1A1A";

    private static final String BLUE =
            "#F59E0B";

    private static final String DARK =
            "#F3F4F6";

    private static final String GREY =
            "#A1A1AA";

    private static final String BORDER =
            "#F59E0B";

    private static final String GREEN =
            "#22C55E";

    private static final String ORANGE =
            "#F59E0B";

    // =====================================================
    // DEFAULT SIZE
    // =====================================================

    private static final double DEFAULT_WIDTH =
            1200;

    private static final double DEFAULT_HEIGHT =
            700;

    // =====================================================
    // FIREBASE
    // =====================================================

    private final Firestore firestore;

    private final VehicleDAO vehicleDAO;

    private final ServiceRequestDAO serviceRequestDAO;

    private final ServiceCatalogDAO serviceCatalogDAO;

    // =====================================================
    // UI
    // =====================================================

    private Scene scene;

    private VBox mechanicsList;

    private Label mechanicsCountLabel;

    private Label selectedMechanicNameLabel;

    private Label selectedMechanicDetailsLabel;

    private Label selectedMechanicStatusLabel;

    // =====================================================
    // DATA
    // =====================================================

    private List<Mechanic> mechanics =
            new ArrayList<>();

    private Mechanic selectedMechanic;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public MechanicsPage() {

        try {

            firestore =
                    FirebaseConfig.getFirestore();

        } catch (IOException e) {

            throw new RuntimeException(
                    "Unable to initialize Firebase.",
                    e
            );
        }

        vehicleDAO =
                new VehicleDAO();

        serviceRequestDAO =
                new ServiceRequestDAO();

        serviceCatalogDAO =
                new ServiceCatalogDAO(
                        firestore
                );

        createPage();
    }

    // =====================================================
    // GET SCENE
    // =====================================================

    public Scene getMechanicsScene() {

        return scene;
    }

    // =====================================================
    // CREATE PAGE
    // =====================================================

    private void createPage() {

        // =================================================
        // HEADER
        // =================================================

        HBox header =
                UserHeader.createHeader();

        // =================================================
        // SIDEBAR
        // =================================================

        ScrollPane sidebar =
                UserSideBar.createSidebar(
                        "Mechanics"
                );

        // =================================================
        // MAIN CONTENT
        // =================================================

        VBox content =
                new VBox(18);

        content.setPadding(
                new Insets(
                        22,
                        22,
                        28,
                        22
                )
        );

        content.setFillWidth(
                true
        );

        content.setBackground(
                new Background(
                        new BackgroundFill(
                                Color.web(BG),
                                CornerRadii.EMPTY,
                                Insets.EMPTY
                        )
                )
        );

        // =================================================
        // HEADING
        // =================================================

        Label title =
                new Label(
                        "Smart Mechanic Dispatch"
                );

        title.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        34
                )
        );

        title.setTextFill(
                Color.web(DARK)
        );

        mechanicsCountLabel =
                new Label(
                        "Loading available mechanics from Firebase..."
                );

        mechanicsCountLabel.setFont(
                Font.font(
                        "Arial",
                        15
                )
        );

        mechanicsCountLabel.setTextFill(
                Color.web(GREY)
        );

        VBox heading =
                new VBox(
                        4,
                        title,
                        mechanicsCountLabel
                );

        // =================================================
        // MAIN AREA
        // =================================================

        HBox mainArea =
                new HBox(18);

        mainArea.setAlignment(
                Pos.TOP_LEFT
        );

        mainArea.setFillHeight(
                true
        );

        // =================================================
        // LEFT SIDE
        // =================================================

        VBox dispatchPanel =
                createDispatchPanel();

        dispatchPanel.setPrefWidth(
                470
        );

        dispatchPanel.setMinWidth(
                400
        );

        // =================================================
        // RIGHT MECHANICS LIST
        // =================================================

        VBox mechanicsPanel =
                createMechanicsPanel();

        HBox.setHgrow(
                mechanicsPanel,
                Priority.ALWAYS
        );

        mainArea
                .getChildren()
                .addAll(
                        dispatchPanel,
                        mechanicsPanel
                );

        content
                .getChildren()
                .addAll(
                        heading,
                        mainArea
                );

        VBox.setVgrow(
                mainArea,
                Priority.ALWAYS
        );

        // =================================================
        // CONTENT SCROLL
        // =================================================

        ScrollPane dashboardScroll =
                new ScrollPane(
                        content
                );

        dashboardScroll.setFitToWidth(
                true
        );

        dashboardScroll.setPannable(
                true
        );

        dashboardScroll.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        dashboardScroll.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED
        );

        dashboardScroll.setStyle(
                "-fx-background-color: "
                        + BG
                        + ";"
                        + "-fx-background: "
                        + BG
                        + ";"
                        + "-fx-border-color: transparent;"
        );

        // =================================================
        // BODY
        // =================================================

        HBox body =
                new HBox();

        body.setFillHeight(
                true
        );

        body.getChildren()
                .addAll(
                        sidebar,
                        dashboardScroll
                );

        HBox.setHgrow(
                dashboardScroll,
                Priority.ALWAYS
        );

        // =================================================
        // ROOT
        // =================================================

        VBox root =
                new VBox(
                        header,
                        body
                );

        VBox.setVgrow(
                body,
                Priority.ALWAYS
        );

        // =================================================
        // SCENE SIZE
        // =================================================

        double width =
                DEFAULT_WIDTH;

        double height =
                DEFAULT_HEIGHT;

        try {

            if (UserDashboard.dashboardStage != null) {

                if (UserDashboard.dashboardStage.getWidth()
                        > 0) {

                    width =
                            UserDashboard.dashboardStage
                                    .getWidth();
                }

                if (UserDashboard.dashboardStage.getHeight()
                        > 0) {

                    height =
                            UserDashboard.dashboardStage
                                    .getHeight();
                }
            }

        } catch (Exception ignored) {
        }

        scene =
                new Scene(
                        root,
                        width,
                        height
                );

        addScrollbarStyle(
                scene
        );

        // =================================================
        // LOAD FIREBASE
        // =================================================

        loadMechanics();
    }

    // =====================================================
    // DISPATCH PANEL
    // =====================================================

    private VBox createDispatchPanel() {

        VBox panel =
                new VBox(18);

        panel.setPadding(
                new Insets(24)
        );

        panel.setStyle(
                "-fx-background-color: "
                        + WHITE
                        + ";"
                        + "-fx-background-radius: 22;"
                        + "-fx-border-color: "
                        + BORDER
                        + ";"
                        + "-fx-border-radius: 22;"
        );

        Label title =
                new Label(
                        "Dispatch Overview"
                );

        title.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        22
                )
        );

        title.setTextFill(
                Color.web(DARK)
        );

        Label info =
                new Label(
                        "Select an available mechanic and create "
                                + "a roadside service request."
                );

        info.setWrapText(
                true
        );

        info.setFont(
                Font.font(
                        "Arial",
                        13
                )
        );

        info.setTextFill(
                Color.web(GREY)
        );

        // =================================================
        // CUSTOMER
        // =================================================

        VBox customerCard =
                createInfoCard(
                        "CUSTOMER",
                        getCurrentCustomerName(),
                        getCurrentCustomerId()
                );

        // =================================================
        // SELECTED MECHANIC
        // =================================================

        VBox selectedCard =
                new VBox(8);

        selectedCard.setPadding(
                new Insets(18)
        );

        selectedCard.setStyle(
                "-fx-background-color: "
                        + CARD
                        + ";"
                        + "-fx-background-radius: 16;"
        );

        Label selectedTitle =
                new Label(
                        "SELECTED MECHANIC"
                );

        selectedTitle.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        10
                )
        );

        selectedTitle.setTextFill(
                Color.web(GREY)
        );

        selectedMechanicNameLabel =
                new Label(
                        "No mechanic selected"
                );

        selectedMechanicNameLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        18
                )
        );

        selectedMechanicNameLabel.setTextFill(
                Color.web(DARK)
        );

        selectedMechanicDetailsLabel =
                new Label(
                        "Select a mechanic from the list."
                );

        selectedMechanicDetailsLabel.setWrapText(
                true
        );

        selectedMechanicDetailsLabel.setFont(
                Font.font(
                        "Arial",
                        12
                )
        );

        selectedMechanicDetailsLabel.setTextFill(
                Color.web(GREY)
        );

        selectedMechanicStatusLabel =
                new Label(
                        "Waiting for selection"
                );

        selectedMechanicStatusLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        12
                )
        );

        selectedMechanicStatusLabel.setTextFill(
                Color.web(ORANGE)
        );

        selectedCard
                .getChildren()
                .addAll(
                        selectedTitle,
                        selectedMechanicNameLabel,
                        selectedMechanicDetailsLabel,
                        selectedMechanicStatusLabel
                );

        // =================================================
        // HOW FLOW WORKS
        // =================================================

        VBox flow =
                new VBox(9);

        Label flowTitle =
                new Label(
                        "How dispatch works"
                );

        flowTitle.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        15
                )
        );

        flowTitle.setTextFill(
                Color.web(DARK)
        );

        flow.getChildren()
                .addAll(
                        flowTitle,

                        flowStep(
                                "1",
                                "Select an available mechanic"
                        ),

                        flowStep(
                                "2",
                                "Select your vehicle"
                        ),

                        flowStep(
                                "3",
                                "Describe your problem and location"
                        ),

                        flowStep(
                                "4",
                                "Request is created as Assigned and visible to Admin"
                        ),

                        flowStep(
                                "5",
                                "Selected mechanic accepts the request and tracking begins"
                        )
                );

        Label note =
                new Label(
                        "Only real mechanics stored in Firebase are shown here. "
                                + "No demo mechanic data is used."
                );

        note.setWrapText(
                true
        );

        note.setPadding(
                new Insets(12)
        );

        note.setStyle(
                "-fx-background-color: #EEF7FF;"
                        + "-fx-background-radius: 10;"
                        + "-fx-text-fill: "
                        + BLUE
                        + ";"
                        + "-fx-font-size: 11px;"
                );

        panel
                .getChildren()
                .addAll(
                        title,
                        info,
                        customerCard,
                        selectedCard,
                        flow,
                        note
                );

        return panel;
    }

    // =====================================================
    // MECHANICS PANEL
    // =====================================================

    private VBox createMechanicsPanel() {

        VBox panel =
                new VBox(14);

        panel.setPadding(
                new Insets(0)
        );

        HBox heading =
                new HBox();

        heading.setAlignment(
                Pos.CENTER_LEFT
        );

        Label title =
                new Label(
                        "Available Mechanics"
                );

        title.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        21
                )
        );

        title.setTextFill(
                Color.web(DARK)
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Button refresh =
                new Button(
                        "Refresh"
                );

        refresh.setPadding(
                new Insets(
                        9,
                        15,
                        9,
                        15
                )
        );

        refresh.setStyle(
                "-fx-background-color: "
                        + WHITE
                        + ";"
                        + "-fx-text-fill: "
                        + DARK
                        + ";"
                        + "-fx-border-color: "
                        + BORDER
                        + ";"
                        + "-fx-border-radius: 16;"
                        + "-fx-background-radius: 16;"
                        + "-fx-font-weight: bold;"
                        + "-fx-cursor: hand;"
        );

        refresh.setOnAction(
                e -> loadMechanics()
        );

        heading
                .getChildren()
                .addAll(
                        title,
                        spacer,
                        refresh
                );

        mechanicsList =
                new VBox(14);

        mechanicsList.setFillWidth(
                true
        );

        Label loading =
                new Label(
                        "Loading mechanics from Firebase..."
                );

        loading.setPadding(
                new Insets(30)
        );

        loading.setTextFill(
                Color.web(GREY)
        );

        mechanicsList
                .getChildren()
                .add(
                        loading
                );

        panel
                .getChildren()
                .addAll(
                        heading,
                        mechanicsList
                );

        return panel;
    }

    // =====================================================
    // LOAD REAL FIREBASE MECHANICS
    // =====================================================

    private void loadMechanics() {

        if (mechanicsList == null) {

            return;
        }

        mechanicsList
                .getChildren()
                .clear();

        Label loading =
                new Label(
                        "Loading mechanics..."
                );

        loading.setPadding(
                new Insets(25)
        );

        loading.setTextFill(
                Color.web(GREY)
        );

        mechanicsList
                .getChildren()
                .add(
                        loading
                );

        try {

            QuerySnapshot snapshot =
                    firestore
                            .collection(
                                    "mechanics"
                            )
                            .get()
                            .get();

            List<Mechanic> loaded =
                    new ArrayList<>();

            for (DocumentSnapshot document :
                    snapshot.getDocuments()) {

                Mechanic mechanic =
                        convertMechanic(
                                document
                        );

                if (mechanic == null) {

                    continue;
                }

                /*
                 * Old registration may not store status.
                 * Missing status = Active.
                 */
                if (!isMechanicAvailable(
                        mechanic
                )) {

                    continue;
                }

                loaded.add(
                        mechanic
                );
            }

            loaded.sort(
                    Comparator.comparing(
                            mechanic ->
                                    safe(
                                            mechanic.getName()
                                    ).toLowerCase()
                    )
            );

            mechanics =
                    loaded;

            renderMechanics();

        } catch (Exception e) {

            mechanics =
                    new ArrayList<>();

            mechanicsList
                    .getChildren()
                    .clear();

            Label error =
                    new Label(
                            "Unable to load mechanics from Firebase."
                    );

            error.setPadding(
                    new Insets(25)
            );

            error.setTextFill(
                    Color.web("#EF4444")
            );

            mechanicsList
                    .getChildren()
                    .add(
                            error
                    );

            if (mechanicsCountLabel != null) {

                mechanicsCountLabel.setText(
                        "Unable to load mechanics"
                );
            }

            System.err.println(
                    "Mechanics load error: "
                            + e.getMessage()
            );
        }
    }

    // =====================================================
    // RENDER MECHANICS
    // =====================================================

    private void renderMechanics() {

        mechanicsList
                .getChildren()
                .clear();

        if (mechanicsCountLabel != null) {

            mechanicsCountLabel.setText(
                    mechanics.size()
                            + (
                            mechanics.size() == 1
                                    ? " active mechanic"
                                    : " active mechanics"
                    )
                            + " available in RoadGuardian"
            );
        }

        if (mechanics.isEmpty()) {

            VBox empty =
                    new VBox(8);

            empty.setAlignment(
                    Pos.CENTER
            );

            empty.setPadding(
                    new Insets(40)
            );

            Label title =
                    new Label(
                            "No active mechanics available"
                    );

            title.setFont(
                    Font.font(
                            "Arial",
                            FontWeight.BOLD,
                            16
                    )
            );

            title.setTextFill(
                    Color.web(DARK)
            );

            Label subtitle =
                    new Label(
                            "Active mechanics added by Admin "
                                    + "will appear here."
                    );

            subtitle.setTextFill(
                    Color.web(GREY)
            );

            empty.getChildren()
                    .addAll(
                            title,
                            subtitle
                    );

            mechanicsList
                    .getChildren()
                    .add(
                            empty
                    );

            return;
        }

        for (Mechanic mechanic :
                mechanics) {

            mechanicsList
                    .getChildren()
                    .add(
                            createMechanicCard(
                                    mechanic
                            )
                    );
        }
    }

    // =====================================================
    // MECHANIC CARD
    // =====================================================

    private VBox createMechanicCard(
            Mechanic mechanic
    ) {

        VBox card =
                new VBox(12);

        card.setPadding(
                new Insets(20)
        );

        card.setMaxWidth(
                Double.MAX_VALUE
        );

        card.setCursor(
                javafx.scene.Cursor.HAND
        );

        boolean selected =
                selectedMechanic != null
                        &&
                sameMechanic(
                        selectedMechanic,
                        mechanic
                );

        applyCardStyle(
                card,
                selected
        );

        card.setOnMouseClicked(
                e -> {

                    selectMechanic(
                            mechanic
                    );

                    renderMechanics();
                }
        );

        // =================================================
        // TOP
        // =================================================

        HBox top =
                new HBox(10);

        top.setAlignment(
                Pos.CENTER_LEFT
        );

        Circle avatar =
                new Circle(
                        22
                );

        avatar.setFill(
                Color.web(
                        "#E7F7EE"
                )
        );

        javafx.scene.shape.SVGPath mechanicIcon =
                IconUtil.createMechanicIcon(
                        0.75,
                        GREEN
                );

        javafx.scene.layout.StackPane avatarBox =
                new javafx.scene.layout.StackPane(
                        avatar,
                        mechanicIcon
                );

        VBox nameBox =
                new VBox(3);

        Label name =
                new Label(
                        safe(
                                mechanic.getName()
                        )
                );

        name.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        18
                )
        );

        name.setTextFill(
                Color.web(DARK)
        );

        Label specialization =
                new Label(
                        valueOr(
                                mechanic.getSpecialization(),
                                "General Mechanic"
                        )
                );

        specialization.setFont(
                Font.font(
                        "Arial",
                        12
                )
        );

        specialization.setTextFill(
                Color.web(GREY)
        );

        nameBox.getChildren()
                .addAll(
                        name,
                        specialization
                );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Label active =
                new Label(
                        "● Active"
                );

        active.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        11
                )
        );

        active.setTextFill(
                Color.web(GREEN)
        );

        top.getChildren()
                .addAll(
                        avatarBox,
                        nameBox,
                        spacer,
                        active
                );

        // =================================================
        // DETAILS
        // =================================================

        HBox details =
                new HBox(18);

        details.getChildren()
                .addAll(

                        smallInfo(
                                "City",
                                valueOr(
                                        mechanic.getCity(),
                                        "Not provided"
                                )
                        ),

                        smallInfo(
                                "Experience",
                                experienceDisplay(
                                        mechanic.getExperience()
                                )
                        ),

                        smallInfo(
                                "Phone",
                                valueOr(
                                        mechanic.getPhone(),
                                        "Not provided"
                                )
                        )
                );

        // =================================================
        // EMAIL
        // =================================================

        Label email =
                new Label(
                        valueOr(
                                mechanic.getEmail(),
                                mechanic.getMechanicId()
                        )
                );

        email.setFont(
                Font.font(
                        "Arial",
                        11
                )
        );

        email.setTextFill(
                Color.web(GREY)
        );

        // =================================================
        // SPECIALIZATION TAGS
        // =================================================

        HBox tags =
                createSpecializationTags(
                        mechanic.getSpecialization()
                );

        // =================================================
        // BUTTON
        // =================================================

        HBox bottom =
                new HBox();

        bottom.setAlignment(
                Pos.CENTER_LEFT
        );

        Label availability =
                new Label(
                        "Available for service requests"
                );

        availability.setTextFill(
                Color.web(GREEN)
        );

        availability.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        12
                )
        );

        Region bottomSpacer =
                new Region();

        HBox.setHgrow(
                bottomSpacer,
                Priority.ALWAYS
        );

        Button request =
                new Button(
                        "Request Mechanic"
                );

        request.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        12
                )
        );

        request.setTextFill(
                Color.WHITE
        );

        request.setPadding(
                new Insets(
                        10,
                        16,
                        10,
                        16
                )
        );

        request.setStyle(
                "-fx-background-color: "
                        + BLUE
                        + ";"
                        + "-fx-background-radius: 18;"
                        + "-fx-text-fill: white;"
                        + "-fx-cursor: hand;"
        );

        request.setOnAction(
                e -> {

                    e.consume();

                    selectMechanic(
                            mechanic
                    );

                    requestMechanic(
                            mechanic
                    );
                }
        );

        bottom.getChildren()
                .addAll(
                        availability,
                        bottomSpacer,
                        request
                );

        card.getChildren()
                .addAll(
                        top,
                        details,
                        email,
                        tags,
                        bottom
                );

        return card;
    }

    // =====================================================
    // SELECT MECHANIC
    // =====================================================

    private void selectMechanic(
            Mechanic mechanic
    ) {

        selectedMechanic =
                mechanic;

        if (mechanic == null) {

            return;
        }

        if (selectedMechanicNameLabel != null) {

            selectedMechanicNameLabel.setText(
                    safe(
                            mechanic.getName()
                    )
            );
        }

        if (selectedMechanicDetailsLabel != null) {

            String details =
                    valueOr(
                            mechanic.getSpecialization(),
                            "General Mechanic"
                    )
                            + "\n"
                            + valueOr(
                            mechanic.getCity(),
                            "City not provided"
                    );

            selectedMechanicDetailsLabel.setText(
                    details
            );
        }

        if (selectedMechanicStatusLabel != null) {

            selectedMechanicStatusLabel.setText(
                    "✓ Ready to create service request"
            );

            selectedMechanicStatusLabel.setTextFill(
                    Color.web(GREEN)
            );
        }
    }

    // =====================================================
    // REQUEST MECHANIC
    // =====================================================

    private void requestMechanic(
            Mechanic mechanic
    ) {

        if (mechanic == null) {

            return;
        }

        String customerId =
                getCurrentCustomerId();

        if (customerId == null) {

            showError(
                    "Login Required",
                    "Customer session was not found. "
                            + "Please login again."
            );

            return;
        }

        try {

            // =================================================
            // LOAD CUSTOMER VEHICLES
            //
            // This also migrates old nested vehicles into
            // top-level vehicles collection.
            // =================================================

            List<Map<String, Object>> vehicles =
                    vehicleDAO
                            .getVehicles(
                                    customerId
                            );

            if (vehicles == null
                    ||
                    vehicles.isEmpty()) {

                showError(
                        "Vehicle Required",
                        "Please add a vehicle before requesting a mechanic."
                );

                return;
            }

            // =================================================
            // VEHICLE SELECTION
            // =================================================

            Map<String, Map<String, Object>> vehicleOptions =
                    new LinkedHashMap<>();

            for (Map<String, Object> vehicle :
                    vehicles) {

                String display =
                        buildVehicleDisplay(
                                vehicle
                        );

                String uniqueDisplay =
                        display;

                int number =
                        2;

                while (vehicleOptions.containsKey(
                        uniqueDisplay
                )) {

                    uniqueDisplay =
                            display
                                    + " ("
                                    + number
                                    + ")";

                    number++;
                }

                vehicleOptions.put(
                        uniqueDisplay,
                        vehicle
                );
            }

            List<String> vehicleLabels =
                    new ArrayList<>(
                            vehicleOptions.keySet()
                    );

            ChoiceDialog<String> vehicleDialog =
                    new ChoiceDialog<>(
                            vehicleLabels.get(0),
                            vehicleLabels
                    );

            vehicleDialog.setTitle(
                    "Select Vehicle"
            );

            vehicleDialog.setHeaderText(
                    "Select the vehicle that needs assistance"
            );

            vehicleDialog.setContentText(
                    "Vehicle:"
            );

            Optional<String> selectedVehicleResult =
                    vehicleDialog.showAndWait();

            if (selectedVehicleResult.isEmpty()) {

                return;
            }

            Map<String, Object> vehicle =
                    vehicleOptions.get(
                            selectedVehicleResult.get()
                    );

            if (vehicle == null) {

                return;
            }

            // =================================================
            // SERVICE TYPE
            //
            // IMPORTANT DATA-FLOW RULE:
            //
            // Admin service catalog is helpful, but it must NOT
            // block a roadside request.
            //
            // 1) Active Firebase services exist -> select one.
            // 2) No active services -> customer enters service
            //    type manually and request still continues.
            // =================================================

            String serviceType =
                    null;

            List<RoadService> activeServices =
                    new ArrayList<>();

            try {

                List<RoadService> loadedServices =
                        serviceCatalogDAO
                                .getActiveServices();

                if (loadedServices != null) {

                    activeServices.addAll(
                            loadedServices
                    );
                }

            } catch (Exception serviceLoadError) {

                /*
                 * Do not stop the Customer -> Admin -> Mechanic
                 * request flow just because the optional service
                 * catalog could not be loaded.
                 */
                System.err.println(
                        "Service catalog load warning: "
                                + serviceLoadError.getMessage()
                );
            }

            Map<String, RoadService> serviceOptions =
                    new LinkedHashMap<>();

            for (RoadService service :
                    activeServices) {

                if (service == null
                        ||
                        clean(service.getName()) == null) {

                    continue;
                }

                String display =
                        service.getName();

                if (service.getBasePrice() > 0) {

                    display +=
                            " • "
                                    + service.getPriceDisplay();
                }

                String duration =
                        clean(
                                service.getEstimatedDuration()
                        );

                if (duration != null) {

                    display +=
                            " • "
                                    + duration;
                }

                String uniqueDisplay =
                        display;

                int serviceNumber =
                        2;

                while (serviceOptions.containsKey(
                        uniqueDisplay
                )) {

                    uniqueDisplay =
                            display
                                    + " ("
                                    + serviceNumber
                                    + ")";

                    serviceNumber++;
                }

                serviceOptions.put(
                        uniqueDisplay,
                        service
                );
            }

            // =================================================
            // ACTIVE ADMIN SERVICES AVAILABLE
            // =================================================

            if (!serviceOptions.isEmpty()) {

                List<String> serviceLabels =
                        new ArrayList<>(
                                serviceOptions.keySet()
                        );

                ChoiceDialog<String> serviceDialog =
                        new ChoiceDialog<>(
                                serviceLabels.get(0),
                                serviceLabels
                        );

                serviceDialog.setTitle(
                        "Service Type"
                );

                serviceDialog.setHeaderText(
                        "Select an active RoadGuardian service"
                );

                serviceDialog.setContentText(
                        "Service:"
                );

                Optional<String> serviceResult =
                        serviceDialog.showAndWait();

                if (serviceResult.isEmpty()) {

                    return;
                }

                RoadService selectedService =
                        serviceOptions.get(
                                serviceResult.get()
                        );

                if (selectedService == null) {

                    showError(
                            "Service Error",
                            "Selected service is no longer available."
                    );

                    return;
                }

                serviceType =
                        clean(
                                selectedService.getName()
                        );

            } else {

                // =============================================
                // NO ADMIN SERVICES CONFIGURED
                //
                // Allow manual service type so the emergency
                // request still reaches serviceRequests and is
                // visible to both Admin and selected Mechanic.
                // =============================================

                TextInputDialog serviceInputDialog =
                        new TextInputDialog();

                serviceInputDialog.setTitle(
                        "Service Type"
                );

                serviceInputDialog.setHeaderText(
                        "No active Admin services are configured"
                );

                serviceInputDialog.setContentText(
                        "Enter required service:"
                );

                Optional<String> manualServiceResult =
                        serviceInputDialog.showAndWait();

                if (manualServiceResult.isEmpty()) {

                    return;
                }

                serviceType =
                        clean(
                                manualServiceResult.get()
                        );
            }

            if (serviceType == null) {

                showError(
                        "Service Required",
                        "Please enter or select the required service type."
                );

                return;
            }

            // =================================================
            // PROBLEM
            // =================================================

            TextInputDialog problemDialog =
                    new TextInputDialog();

            problemDialog.setTitle(
                    "Vehicle Problem"
            );

            problemDialog.setHeaderText(
                    "Describe the vehicle problem"
            );

            problemDialog.setContentText(
                    "Problem:"
            );

            Optional<String> problemResult =
                    problemDialog.showAndWait();

            if (problemResult.isEmpty()) {

                return;
            }

            String description =
                    clean(
                            problemResult.get()
                    );

            if (description == null) {

                showError(
                        "Problem Required",
                        "Please describe the vehicle problem."
                );

                return;
            }

            // =================================================
            // LOCATION
            // =================================================

            TextInputDialog locationDialog =
                    new TextInputDialog();

            locationDialog.setTitle(
                    "Current Location"
            );

            locationDialog.setHeaderText(
                    "Enter your current vehicle location"
            );

            locationDialog.setContentText(
                    "Location:"
            );

            Optional<String> locationResult =
                    locationDialog.showAndWait();

            if (locationResult.isEmpty()) {

                return;
            }

            String location =
                    clean(
                            locationResult.get()
                    );

            if (location == null) {

                showError(
                        "Location Required",
                        "Please enter your current location."
                );

                return;
            }

            // =================================================
            // VEHICLE DATA
            // =================================================

            String vehicleId =
                    firstString(
                            vehicle,
                            "vehicleId",
                            "id"
                    );

            String vehicleNumber =
                    firstString(
                            vehicle,
                            "vehicleNumber",
                            "registrationNumber"
                    );

            if (vehicleId == null) {

                showError(
                        "Vehicle Error",
                        "Selected vehicle ID is missing."
                );

                return;
            }

            // =================================================
            // MECHANIC DATA
            // =================================================

            String mechanicId =
                    normalizeId(
                            clean(
                                    mechanic.getEmail()
                            )
                    );

            if (mechanicId == null) {

                mechanicId =
                        normalizeId(
                                clean(
                                        mechanic.getMechanicId()
                                )
                        );
            }

            if (mechanicId == null) {

                showError(
                        "Mechanic Error",
                        "Selected mechanic ID is missing."
                );

                return;
            }

            String mechanicName =
                    valueOr(
                            mechanic.getName(),
                            "Mechanic"
                    );

            // =================================================
            // CUSTOMER
            // =================================================

            String customerName =
                    getCurrentCustomerName();

            // =================================================
            // CREATE SAME SHARED SERVICE REQUEST
            // =================================================

            String requestId =
                    serviceRequestDAO
                            .createRequest(
                                    customerId,
                                    customerName,
                                    vehicleId,
                                    vehicleNumber,
                                    mechanicId,
                                    mechanicName,
                                    serviceType,
                                    description,
                                    location
                            );

            // =================================================
            // SUCCESS
            // =================================================

            Alert success =
                    new Alert(
                            Alert.AlertType.INFORMATION
                    );

            success.setTitle(
                    "RoadGuardian"
            );

            success.setHeaderText(
                    "Service Request Created"
            );

            success.setContentText(
                    "Request ID: "
                            + requestId
                            + "\n\n"
                            + "Preferred mechanic: "
                            + mechanicName
                            + "\n"
                            + "Vehicle: "
                            + valueOr(
                            vehicleNumber,
                            vehicleId
                    )
                            + "\n"
                            + "Service: "
                            + serviceType
                            + "\n"
                            + "Location: "
                            + location
                            + "\n\n"
                            + "Status: Assigned\n\n"
                            + "The selected mechanic is already linked to this request. "
                            + "Admin can monitor the same Firebase record, "
                            + "and the mechanic can now Accept it."
            );

            success.showAndWait();

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    "Request Failed",
                    "Unable to create service request.\n\n"
                            + e.getMessage()
            );
        }
    }

    // =====================================================
    // FIRESTORE -> MECHANIC
    // =====================================================

    private Mechanic convertMechanic(
            DocumentSnapshot document
    ) {

        if (document == null
                ||
                !document.exists()) {

            return null;
        }

        Mechanic mechanic =
                new Mechanic();

        String email =
                firstDocumentString(
                        document,
                        "email"
                );

        if (email == null
                &&
                document.getId().contains("@")) {

            email =
                    document.getId();
        }

        email =
                normalizeId(
                        email
                );

        String mechanicId =
                email;

        if (mechanicId == null) {

            mechanicId =
                    normalizeId(
                            firstDocumentString(
                                    document,
                                    "mechanicId"
                            )
                    );
        }

        if (mechanicId == null) {

            mechanicId =
                    normalizeId(
                            document.getId()
                    );
        }

        /*
         * IMPORTANT FIX:
         * Customer service request must store mechanic email when
         * email exists. Random Firestore document IDs do not match
         * the mechanic login session.
         */
        mechanic.setMechanicId(
                mechanicId
        );

        mechanic.setName(
                valueOr(
                        firstDocumentString(
                                document,
                                "name",
                                "fullName",
                                "mechanicName"
                        ),
                        "Mechanic"
                )
        );

        mechanic.setEmail(
                email == null
                        ? ""
                        : email.toLowerCase()
        );

        mechanic.setPhone(
                valueOr(
                        firstDocumentString(
                                document,
                                "phone",
                                "phoneNumber"
                        ),
                        ""
                )
        );

        mechanic.setAddress(
                valueOr(
                        firstDocumentString(
                                document,
                                "address"
                        ),
                        ""
                )
        );

        mechanic.setCity(
                valueOr(
                        firstDocumentString(
                                document,
                                "city"
                        ),
                        ""
                )
        );

        mechanic.setSpecialization(
                valueOr(
                        firstDocumentString(
                                document,
                                "specialization",
                                "speciality"
                        ),
                        ""
                )
        );

        /*
         * Handles Number 0 and String "0".
         */
        mechanic.setExperience(
                valueOr(
                        firstDocumentString(
                                document,
                                "experience"
                        ),
                        ""
                )
        );

        String status =
                firstDocumentString(
                        document,
                        "status"
                );

        if (status == null) {

            status =
                    "Active";
        }

        mechanic.setStatus(
                status
        );

        mechanic.setCreatedAt(
                valueOr(
                        firstDocumentString(
                                document,
                                "createdAt"
                        ),
                        ""
                )
        );

        return mechanic;
    }

    // =====================================================
    // IS AVAILABLE
    // =====================================================

    private boolean isMechanicAvailable(
            Mechanic mechanic
    ) {

        if (mechanic == null) {

            return false;
        }

        String status =
                clean(
                        mechanic.getStatus()
                );

        if (status == null) {

            return true;
        }

        return !status.equalsIgnoreCase(
                "Inactive"
        )
                &&
                !status.equalsIgnoreCase(
                        "Blocked"
                )
                &&
                !status.equalsIgnoreCase(
                        "Disabled"
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

        if (userId.contains("@")) {

            return userId.toLowerCase();
        }

        return userId;
    }

    // =====================================================
    // CURRENT CUSTOMER NAME
    // =====================================================

    private String getCurrentCustomerName() {

        String name =
                clean(
                        UserSession.getUserName()
                );

        return name == null
                ? "Customer"
                : name;
    }

    // =====================================================
    // VEHICLE DISPLAY
    // =====================================================

    private String buildVehicleDisplay(
            Map<String, Object> vehicle
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
                        "name",
                        "make"
                );

        String model =
                firstString(
                        vehicle,
                        "model"
                );

        StringBuilder display =
                new StringBuilder();

        if (number != null) {

            display.append(
                    number
            );
        }

        String vehicleName =
                joinNonBlank(
                        brand,
                        model
                );

        if (vehicleName != null) {

            if (display.length() > 0) {

                display.append(
                        " • "
                );
            }

            display.append(
                    vehicleName
            );
        }

        if (display.length() == 0) {

            String id =
                    firstString(
                            vehicle,
                            "vehicleId",
                            "id"
                    );

            display.append(
                    valueOr(
                            id,
                            "Vehicle"
                    )
            );
        }

        return display.toString();
    }

    // =====================================================
    // INFO CARD
    // =====================================================

    private VBox createInfoCard(
            String title,
            String value,
            String subtitle
    ) {

        VBox card =
                new VBox(5);

        card.setPadding(
                new Insets(16)
        );

        card.setStyle(
                "-fx-background-color: "
                        + CARD
                        + ";"
                        + "-fx-background-radius: 14;"
        );

        Label titleLabel =
                new Label(
                        title
                );

        titleLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        9
                )
        );

        titleLabel.setTextFill(
                Color.web(GREY)
        );

        Label valueLabel =
                new Label(
                        safe(
                                value
                        )
                );

        valueLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        16
                )
        );

        valueLabel.setTextFill(
                Color.web(DARK)
        );

        // UI-only: show a user silhouette for the customer representation.
        if ("CUSTOMER".equalsIgnoreCase(title)) {
            valueLabel.setGraphic(
                    IconUtil.createUserIcon(
                            0.65,
                            BLUE
                    )
            );
            valueLabel.setGraphicTextGap(8);
        }

        Label subtitleLabel =
                new Label(
                        safe(
                                subtitle
                        )
                );

        subtitleLabel.setWrapText(
                true
        );

        subtitleLabel.setFont(
                Font.font(
                        "Arial",
                        11
                )
        );

        subtitleLabel.setTextFill(
                Color.web(GREY)
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
    // FLOW STEP
    // =====================================================

    private HBox flowStep(
            String number,
            String text
    ) {

        HBox row =
                new HBox(10);

        row.setAlignment(
                Pos.CENTER_LEFT
        );

        Circle circle =
                new Circle(
                        14,
                        Color.web(BLUE)
                );

        Label numberLabel =
                new Label(
                        number
                );

        numberLabel.setTextFill(
                Color.WHITE
        );

        numberLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        10
                )
        );

        javafx.scene.layout.StackPane numberBox =
                new javafx.scene.layout.StackPane(
                        circle,
                        numberLabel
                );

        Label label =
                new Label(
                        text
                );

        label.setWrapText(
                true
        );

        label.setTextFill(
                Color.web(DARK)
        );

        label.setFont(
                Font.font(
                        "Arial",
                        12
                )
        );

        row.getChildren()
                .addAll(
                        numberBox,
                        label
                );

        return row;
    }

    // =====================================================
    // SPECIALIZATION TAGS
    // =====================================================

    private HBox createSpecializationTags(
            String specialization
    ) {

        HBox tags =
                new HBox(7);

        String value =
                clean(
                        specialization
                );

        if (value == null) {

            tags.getChildren()
                    .add(
                            tag(
                                    "General Service"
                            )
                    );

            return tags;
        }

        String[] values =
                value.split(
                        "[,;/|]"
                );

        int count =
                0;

        for (String item :
                values) {

            String cleaned =
                    clean(
                            item
                    );

            if (cleaned == null) {

                continue;
            }

            tags.getChildren()
                    .add(
                            tag(
                                    cleaned
                            )
                    );

            count++;

            if (count >= 3) {

                break;
            }
        }

        if (count == 0) {

            tags.getChildren()
                    .add(
                            tag(
                                    value
                            )
                    );
        }

        return tags;
    }

    // =====================================================
    // TAG
    // =====================================================

    private Label tag(
            String text
    ) {

        Label tag =
                new Label(
                        text
                );

        tag.setFont(
                Font.font(
                        "Arial",
                        11
                )
        );

        tag.setTextFill(
                Color.web(DARK)
        );

        tag.setPadding(
                new Insets(
                        5,
                        9,
                        5,
                        9
                )
        );

        tag.setBackground(
                new Background(
                        new BackgroundFill(
                                Color.web(
                                        "#EAF3F5"
                                ),
                                new CornerRadii(12),
                                Insets.EMPTY
                        )
                )
        );

        return tag;
    }

    // =====================================================
    // SMALL INFO
    // =====================================================

    private VBox smallInfo(
            String title,
            String value
    ) {

        VBox box =
                new VBox(2);

        Label titleLabel =
                new Label(
                        title.toUpperCase()
                );

        titleLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        8
                )
        );

        titleLabel.setTextFill(
                Color.web(GREY)
        );

        Label valueLabel =
                new Label(
                        safe(
                                value
                        )
                );

        valueLabel.setFont(
                Font.font(
                        "Arial",
                        11
                )
        );

        valueLabel.setTextFill(
                Color.web(DARK)
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
    // CARD STYLE
    // =====================================================

    private void applyCardStyle(
            VBox card,
            boolean selected
    ) {

        card.setBackground(
                new Background(
                        new BackgroundFill(
                                Color.web(CARD),
                                new CornerRadii(20),
                                Insets.EMPTY
                        )
                )
        );

        card.setBorder(
                new Border(
                        new BorderStroke(
                                selected
                                        ? Color.web(BLUE)
                                        : Color.web(BORDER),

                                BorderStrokeStyle.SOLID,

                                new CornerRadii(20),

                                new BorderWidths(
                                        selected
                                                ? 2.5
                                                : 1
                                )
                        )
                )
        );
    }

    // =====================================================
    // SAME MECHANIC
    // =====================================================

    private boolean sameMechanic(
            Mechanic first,
            Mechanic second
    ) {

        if (first == null
                ||
                second == null) {

            return false;
        }

        String firstId =
                normalizeId(
                        first.getMechanicId()
                );

        String secondId =
                normalizeId(
                        second.getMechanicId()
                );

        String firstEmail =
                normalizeId(
                        first.getEmail()
                );

        String secondEmail =
                normalizeId(
                        second.getEmail()
                );

        return idsMatch(firstId, secondId)
                || idsMatch(firstId, secondEmail)
                || idsMatch(firstEmail, secondId)
                || idsMatch(firstEmail, secondEmail);
    }

    // =====================================================
    // ID MATCH
    // =====================================================

    private boolean idsMatch(
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
    // EXPERIENCE DISPLAY
    // =====================================================

    private String experienceDisplay(
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
    // INITIAL
    // =====================================================

    private String getInitial(
            String name
    ) {

        String value =
                clean(
                        name
                );

        if (value == null) {

            return "M";
        }

        return value
                .substring(
                        0,
                        1
                )
                .toUpperCase();
    }

    // =====================================================
    // FIRST MAP STRING
    // =====================================================

    private String firstString(
            Map<String, Object> data,
            String... keys
    ) {

        if (data == null
                ||
                keys == null) {

            return null;
        }

        for (String key :
                keys) {

            Object value =
                    data.get(
                            key
                    );

            String text =
                    stringValue(
                            value
                    );

            if (text != null) {

                return text;
            }
        }

        return null;
    }

    // =====================================================
    // FIRST DOCUMENT STRING
    // =====================================================

    private String firstDocumentString(
            DocumentSnapshot document,
            String... keys
    ) {

        if (document == null
                ||
                keys == null) {

            return null;
        }

        for (String key :
                keys) {

            String text =
                    stringValue(
                            document.get(
                                    key
                            )
                    );

            if (text != null) {

                return text;
            }
        }

        return null;
    }

    // =====================================================
    // JOIN
    // =====================================================

    private String joinNonBlank(
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

        if (first == null
                &&
                second == null) {

            return null;
        }

        if (first == null) {

            return second;
        }

        if (second == null) {

            return first;
        }

        return first
                + " "
                + second;
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
    // VALUE OR
    // =====================================================

    private String valueOr(
            String value,
            String fallback
    ) {

        String cleaned =
                clean(
                        value
                );

        if (cleaned != null) {

            return cleaned;
        }

        String fallbackValue =
                clean(
                        fallback
                );

        return fallbackValue == null
                ? "-"
                : fallbackValue;
    }

    // =====================================================
    // STRING VALUE
    // =====================================================

    private String stringValue(
            Object value
    ) {

        if (value == null) {

            return null;
        }

        String text =
                String.valueOf(
                        value
                ).trim();

        return text.isEmpty()
                ? null
                : text;
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
    // SCROLLBAR STYLE
    // =====================================================

    private void addScrollbarStyle(
            Scene scene
    ) {

        scene.getStylesheets()
                .add(
                        "data:text/css,"
                                +

                        ".scroll-bar:vertical {"
                                + "-fx-background-color: #C4D9E0;"
                                + "-fx-background-radius: 8;"
                                + "-fx-padding: 2;"
                                + "}"

                                +

                        ".scroll-bar:vertical .track {"
                                + "-fx-background-color: #C4D9E0;"
                                + "-fx-background-radius: 8;"
                                + "}"

                                +

                        ".scroll-bar:vertical .thumb {"
                                + "-fx-background-color: #A1A1AA;"
                                + "-fx-background-radius: 8;"
                                + "}"

                                +

                        ".scroll-bar:vertical .thumb:hover {"
                                + "-fx-background-color: #F3F4F6;"
                                + "}"

                                +

                        ".scroll-bar:horizontal {"
                                + "-fx-opacity: 0;"
                                + "-fx-max-height: 0;"
                                + "-fx-pref-height: 0;"
                                + "}"
                );
    }
}