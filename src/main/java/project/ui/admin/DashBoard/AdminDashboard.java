package project.ui.admin.DashBoard;

import com.google.cloud.firestore.Firestore;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.Scene;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import javafx.scene.paint.Color;

import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import javafx.stage.Stage;

import javafx.util.Duration;

import project.controller.admin.DashboardController;

import project.firebase.FirebaseConfig;

import project.model.SOSRequest;
import project.model.ServiceRequest;

import project.ui.admin.Complaints.ComplaintManagementPage;
import project.ui.admin.Customers.CustomerManagementPage;
import project.ui.admin.Mechanics.MechanicManagementPage;
import project.ui.admin.Notifications.NotificationManagementPage;
import project.ui.admin.Reports.ReportManagementPage;
import project.ui.admin.Reviews.ReviewManagementPage;
import project.ui.admin.SOSRequests.SOSRequestManagementPage;
import project.ui.admin.ServiceRequests.ServiceRequestManagementPage;
import project.ui.admin.Services.ServiceManagementPage;
import project.ui.admin.Settings.SettingsPage;
import project.ui.admin.Vehicles.VehicleManagementPage;

import project.ui.landing.LoginPage;
import project.ui.user.UserSession;
import project.util.IconUtil;

import java.time.LocalDate;
import java.time.LocalTime;

import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminDashboard {

    // =========================================================
    // COLORS
    // =========================================================

    private static final String BG =
            "#0F0F0F";

    private static final String SIDEBAR =
            "#161616";

    private static final String SURFACE =
            "#1A1A1A";

    private static final String SECONDARY =
            "#242424";

    private static final String BLUE =
            "#F59E0B";

    private static final String BLUE_HOVER =
            "#F59E0B";

    private static final String ORANGE =
            "#F59E0B";

    private static final String ORANGE_HOVER =
            "#D97706";

    private static final String HEADING =
            "#F3F4F6";

    private static final String TEXT =
            "#A1A1AA";

    private static final String GREEN =
            "#22C55E";

    private static final String GREEN_HOVER =
            "#22C55E";

    private static final String RED =
            "#EF4444";

    private static final String RED_HOVER =
            "#EF4444";

    private static final String PURPLE =
            "#F59E0B";

    private static final String CYAN =
            "#F59E0B";

    private static final String BORDER =
            "#F59E0B";

    // =========================================================
    // STAGE
    // =========================================================

    private final Stage stage;

    // =========================================================
    // LOGOUT
    // =========================================================

    public final Runnable logOutAction;

    // =========================================================
    // FIREBASE
    // =========================================================

    private Firestore firestore;

    private DashboardController dashboardController;

    // =========================================================
    // ROOT
    // =========================================================

    private BorderPane root;

    private VBox sidebar;

    private VBox contentArea;

    private ScrollPane contentScroll;

    // =========================================================
    // NAVIGATION
    // =========================================================

    private String currentPage =
            "dashboard";

    private boolean sidebarCollapsed =
            false;

    private Button dashboardButton;

    private Button customersButton;

    private Button mechanicsButton;

    private Button vehiclesButton;

    private Button serviceRequestButton;

    private Button sosButton;

    private Button servicesButton;

    private Button reviewsButton;

    private Button complaintsButton;

    private Button notificationsButton;

    private Button reportsButton;

    private Button settingsButton;

    // =========================================================
    // REFRESH
    // =========================================================

    private Timeline autoRefreshTimeline;

    // =========================================================
    // DASHBOARD DATA
    // =========================================================

    private Map<String, Integer> dashboardData =
            new HashMap<>();

    private List<ServiceRequest> recentServiceRequests =
            new ArrayList<>();

    private List<SOSRequest> recentSOSRequests =
            new ArrayList<>();

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public AdminDashboard(
            Stage stage,
            Runnable logOutAction
    ) {

        this.stage =
                stage;

        this.logOutAction =
                logOutAction;

        initializeFirebase();
    }

    // =========================================================
    // FIREBASE
    // =========================================================

    private void initializeFirebase() {

        try {

            firestore =
                    FirebaseConfig.getFirestore();

            dashboardController =
                    new DashboardController(
                            firestore
                    );

        } catch (Exception e) {

            firestore =
                    null;

            dashboardController =
                    null;

            System.err.println(
                    "Admin Dashboard Firebase Error: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    // =========================================================
    // GET VIEW
    // =========================================================

    public BorderPane getView() {

        root =
                new BorderPane();

        project.ui.theme.DarkTheme.apply(root);

        root.setStyle(
                "-fx-background-color: "
                        + BG
                        + ";"
        );

        // =====================================================
        // SIDEBAR
        // =====================================================

        sidebar =
                createSidebar();

        root.setLeft(
                sidebar
        );

        // =====================================================
        // MAIN AREA
        // =====================================================

        VBox mainArea =
                new VBox();

        mainArea.setFillWidth(
                true
        );

        HBox topBar =
                createMainTopBar();

        contentArea =
                new VBox();

        contentArea.setFillWidth(
                true
        );

        contentScroll =
                new ScrollPane(
                        contentArea
                );

        contentScroll.setFitToWidth(
                true
        );

        contentScroll.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        contentScroll.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED
        );

        contentScroll.setStyle(
                "-fx-background-color: transparent;"
                        + "-fx-background: transparent;"
        );

        mainArea
                .getChildren()
                .addAll(
                        topBar,
                        contentScroll
                );

        VBox.setVgrow(
                contentScroll,
                Priority.ALWAYS
        );

        root.setCenter(
                mainArea
        );

        showDashboard();

        startAutoRefresh();

        return root;
    }

    // =========================================================
    // TOP BAR
    // =========================================================

    private HBox createMainTopBar() {

        HBox topBar =
                new HBox(15);

        topBar.setAlignment(
                Pos.CENTER_LEFT
        );

        topBar.setPadding(
                new Insets(
                        14,
                        24,
                        14,
                        24
                )
        );

        topBar.setStyle(
                "-fx-background-color: "
                        + SURFACE
                        + ";"
                        + "-fx-border-color: "
                        + BORDER
                        + ";"
                        + "-fx-border-width: 0 0 1 0;"
        );

        Button toggle =
                new Button(
                        "☰"
                );

        toggle.setPrefSize(
                42,
                42
        );

        toggle.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        20
                )
        );

        toggle.setTextFill(
                Color.web(
                        HEADING
                )
        );

        toggle.setStyle(
                "-fx-background-color: "
                        + SECONDARY
                        + ";"
                        + "-fx-background-radius: 9;"
                        + "-fx-cursor: hand;"
        );

        toggle.setOnAction(
                event ->
                        toggleSidebar()
        );

        Label title =
                new Label(
                        "Admin Panel"
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

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        HBox profile =
                createAdminProfile();

        topBar
                .getChildren()
                .addAll(
                        toggle,
                        title,
                        spacer,
                        profile
                );

        return topBar;
    }

    // =========================================================
    // ADMIN PROFILE
    // =========================================================

    private HBox createAdminProfile() {

        HBox profile =
                new HBox(10);

        profile.setAlignment(
                Pos.CENTER_LEFT
        );

        profile.setPadding(
                new Insets(
                        7,
                        16,
                        7,
                        8
                )
        );

        profile.setStyle(
                "-fx-background-color: "
                        + SURFACE
                        + ";"
                        + "-fx-border-color: "
                        + BORDER
                        + ";"
                        + "-fx-border-radius: 24;"
                        + "-fx-background-radius: 24;"
        );

        StackPane avatarBox =
                new StackPane();

        Circle avatar =
                new Circle(
                        20,
                        Color.web(
                                BLUE
                        )
                );

        String adminName =
                firstNonBlank(
                        UserSession.getUserName(),
                        "Admin"
                );

        // UI-only: show a user silhouette instead of the admin initial.
        var userIcon =
                IconUtil.createUserIcon(
                        0.65,
                        Color.WHITE.toString()
                );

        avatarBox
                .getChildren()
                .addAll(
                        avatar,
                        userIcon
                );

        VBox details =
                new VBox(2);

        Label name =
                new Label(
                        adminName
                );

        name.setTextFill(
                Color.web(
                        HEADING
                )
        );

        name.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        14
                )
        );

        Label role =
                new Label(
                        "Administrator"
                );

        role.setTextFill(
                Color.web(
                        TEXT
                )
        );

        role.setFont(
                Font.font(
                        "Arial",
                        11
                )
        );

        details
                .getChildren()
                .addAll(
                        name,
                        role
                );

        profile
                .getChildren()
                .addAll(
                        avatarBox,
                        details
                );

        return profile;
    }

    // =========================================================
    // SIDEBAR
    // =========================================================

    private VBox createSidebar() {

        VBox side =
                new VBox(5);

        side.setPrefWidth(
                270
        );

        side.setMinWidth(
                270
        );

        side.setMaxWidth(
                270
        );

        side.setPadding(
                new Insets(
                        16,
                        12,
                        20,
                        12
                )
        );

        side.setStyle(
                "-fx-background-color: "
                        + SIDEBAR
                        + ";"
                        + "-fx-border-color: "
                        + BORDER
                        + ";"
                        + "-fx-border-width: 0 1 0 0;"
        );

        // =====================================================
        // BRAND
        // =====================================================

        VBox brand =
                new VBox(5);

        brand.setPadding(
                new Insets(
                        4,
                        10,
                        22,
                        10
                )
        );

        Label logo =
                new Label(
                        "✓"
                );

        logo.setAlignment(
                Pos.CENTER
        );

        logo.setPrefSize(
                45,
                45
        );

        logo.setTextFill(
                Color.WHITE
        );

        logo.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        24
                )
        );

        logo.setStyle(
                "-fx-background-color: "
                        + BLUE
                        + ";"
                        + "-fx-background-radius: 50%;"
        );

        Label brandTitle =
                new Label(
                        "ROADGUARDIAN"
                );

        brandTitle.setTextFill(
                Color.web(
                        HEADING
                )
        );

        brandTitle.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        20
                )
        );

        Label brandSubtitle =
                new Label(
                        "ADMIN PANEL"
                );

        brandSubtitle.setTextFill(
                Color.web(
                        GREEN
                )
        );

        brandSubtitle.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        12
                )
        );

        brand
                .getChildren()
                .addAll(
                        logo,
                        brandTitle,
                        brandSubtitle
                );

        side
                .getChildren()
                .add(
                        brand
                );

        // =====================================================
        // NAV BUTTONS
        // =====================================================

        dashboardButton =
                createNavButton(
                        "▣",
                        "Dashboard"
                );

        customersButton =
                createNavButton(
                        "♙",
                        "Customers"
                );

        mechanicsButton =
                createNavButton(
                        "⚒",
                        "Mechanics"
                );

        vehiclesButton =
                createNavButton(
                        "▰",
                        "Vehicles"
                );

        serviceRequestButton =
                createNavButton(
                        "▤",
                        "Service Requests"
                );

        sosButton =
                createNavButton(
                        "SOS",
                        "SOS Requests"
                );

        servicesButton =
                createNavButton(
                        "⚙",
                        "Services"
                );

        reviewsButton =
                createNavButton(
                        "☆",
                        "Reviews"
                );

        complaintsButton =
                createNavButton(
                        "!",
                        "Complaints"
                );

        notificationsButton =
                createNavButton(
                        "♢",
                        "Notifications"
                );

        reportsButton =
                createNavButton(
                        "▥",
                        "Reports"
                );

        settingsButton =
                createNavButton(
                        "⚙",
                        "Settings"
                );

        dashboardButton.setOnAction(
                event ->
                        showDashboard()
        );

        customersButton.setOnAction(
                event ->
                        showCustomers()
        );

        mechanicsButton.setOnAction(
                event ->
                        showMechanics()
        );

        vehiclesButton.setOnAction(
                event ->
                        showVehicles()
        );

        serviceRequestButton.setOnAction(
                event ->
                        showServiceRequests()
        );

        sosButton.setOnAction(
                event ->
                        showSOSRequests()
        );

        servicesButton.setOnAction(
                event ->
                        showServices()
        );

        reviewsButton.setOnAction(
                event ->
                        showReviews()
        );

        complaintsButton.setOnAction(
                event ->
                        showComplaints()
        );

        notificationsButton.setOnAction(
                event ->
                        showNotifications()
        );

        reportsButton.setOnAction(
                event ->
                        showReports()
        );

        settingsButton.setOnAction(
                event ->
                        showSettings()
        );

        side
                .getChildren()
                .addAll(
                        dashboardButton,
                        customersButton,
                        mechanicsButton,
                        vehiclesButton,
                        serviceRequestButton,
                        sosButton,
                        servicesButton,
                        reviewsButton,
                        complaintsButton,
                        notificationsButton,
                        reportsButton,
                        settingsButton
                );

        // =====================================================
        // SPACER
        // =====================================================

        Region spacer =
                new Region();

        VBox.setVgrow(
                spacer,
                Priority.ALWAYS
        );

        side
                .getChildren()
                .add(
                        spacer
                );

        // =====================================================
        // DATE
        // =====================================================

        side
                .getChildren()
                .add(
                        createDateBox()
                );

        // =====================================================
        // LOGOUT
        // =====================================================

        side
                .getChildren()
                .add(
                        createLogoutButton()
                );

        return side;
    }

    // =========================================================
    // NAV BUTTON
    // =========================================================

    private Button createNavButton(
            String icon,
            String text
    ) {

        Button button =
                new Button(
                        icon
                                + "   "
                                + text
                );

        button.setMaxWidth(
                Double.MAX_VALUE
        );

        button.setPrefHeight(
                47
        );

        button.setAlignment(
                Pos.CENTER_LEFT
        );

        button.setPadding(
                new Insets(
                        0,
                        15,
                        0,
                        15
                )
        );

        button.setTextFill(
                Color.web(
                        HEADING
                )
        );

        button.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        14
                )
        );

        button.setStyle(
                "-fx-background-color: transparent;"
                        + "-fx-background-radius: 9;"
                        + "-fx-cursor: hand;"
        );

        button.setOnMouseEntered(
                event -> {

                    if (!button
                            .getStyleClass()
                            .contains(
                                    "active-nav"
                            )) {

                        button.setStyle(
                                "-fx-background-color: "
                                        + SECONDARY
                                        + ";"
                                        + "-fx-background-radius: 9;"
                                        + "-fx-cursor: hand;"
                        );
                    }
                }
        );

        button.setOnMouseExited(
                event -> {

                    if (!button
                            .getStyleClass()
                            .contains(
                                    "active-nav"
                            )) {

                        button.setStyle(
                                "-fx-background-color: transparent;"
                                        + "-fx-background-radius: 9;"
                                        + "-fx-cursor: hand;"
                        );
                    }
                }
        );

        return button;
    }

    // =========================================================
    // ACTIVE NAV
    // =========================================================

    private void setActive(
            Button active
    ) {

        Button[] buttons = {
                dashboardButton,
                customersButton,
                mechanicsButton,
                vehiclesButton,
                serviceRequestButton,
                sosButton,
                servicesButton,
                reviewsButton,
                complaintsButton,
                notificationsButton,
                reportsButton,
                settingsButton
        };

        for (Button button :
                buttons) {

            if (button == null) {

                continue;
            }

            button
                    .getStyleClass()
                    .remove(
                            "active-nav"
                    );

            button.setStyle(
                    "-fx-background-color: transparent;"
                            + "-fx-background-radius: 9;"
                            + "-fx-cursor: hand;"
            );
        }

        if (active == null) {

            return;
        }

        active
                .getStyleClass()
                .add(
                        "active-nav"
                );

        active.setStyle(
                "-fx-background-color: "
                        + BLUE
                        + ";"
                        + "-fx-text-fill: white;"
                        + "-fx-background-radius: 9;"
                        + "-fx-cursor: hand;"
        );
    }

    // =========================================================
    // SIDEBAR TOGGLE
    // =========================================================

    private void toggleSidebar() {

        sidebarCollapsed =
                !sidebarCollapsed;

        sidebar.setVisible(
                !sidebarCollapsed
        );

        sidebar.setManaged(
                !sidebarCollapsed
        );
    }

    // =========================================================
    // AUTO REFRESH
    //
    // Only dashboard is automatically rebuilt.
    //
    // We do NOT reset forms/pages while Admin is working on them.
    // =========================================================

   private void startAutoRefresh() {

    if (autoRefreshTimeline != null) {

        autoRefreshTimeline.stop();
    }

    autoRefreshTimeline =
            new Timeline(
                    new KeyFrame(
                            Duration.seconds(
                                    15
                            ),
                            event -> {

                                if ("dashboard"
                                        .equals(
                                                currentPage
                                        )) {

                                    refreshDashboardSilently();
                                }
                            }
                    )
            );

    autoRefreshTimeline.setCycleCount(
            Timeline.INDEFINITE
    );

    autoRefreshTimeline.play();
}

    // =========================================================
// SILENT DASHBOARD REFRESH
//
// Firebase + UI refresh होईल,
// पण current scroll position बदलणार नाही.
// =========================================================

private void refreshDashboardSilently() {

    if (contentArea == null
            || contentScroll == null
            || !"dashboard".equals(currentPage)) {

        return;
    }

    // Current scroll position save
    double oldVValue =
            contentScroll.getVvalue();

    double oldHValue =
            contentScroll.getHvalue();

    // Latest Firebase data
    loadDashboardData();

    // Rebuild only dashboard content
    contentArea
            .getChildren()
            .setAll(
                    createDashboard()
            );

    /*
     * JavaFX ला नवीन content layout करू दे.
     * Layout complete झाल्यावर previous scroll restore.
     */
    Platform.runLater(() -> {

        if (contentScroll == null) {

            return;
        }

        contentScroll.setVvalue(
                oldVValue
        );

        contentScroll.setHvalue(
                oldHValue
        );
    });
}

    // =========================================================
    // SHOW DASHBOARD
    // =========================================================

    private void showDashboard() {

        currentPage =
                "dashboard";

        setActive(
                dashboardButton
        );

        loadDashboardData();

        contentArea
                .getChildren()
                .setAll(
                        createDashboard()
                );

        if (contentScroll != null) {

            contentScroll.setVvalue(
                    0
            );
        }
    }

    // =========================================================
    // LOAD DASHBOARD DATA
    // =========================================================

    private void loadDashboardData() {

        dashboardData =
                new HashMap<>();

        recentServiceRequests =
                new ArrayList<>();

        recentSOSRequests =
                new ArrayList<>();

        if (dashboardController == null) {

            return;
        }

        dashboardData =
                dashboardController
                        .getDashboardStatistics();

        recentServiceRequests =
                dashboardController
                        .getRecentServiceRequests(
                                5
                        );

        recentSOSRequests =
                dashboardController
                        .getRecentSOSRequests(
                                3
                        );
    }

    // =========================================================
    // DASHBOARD PAGE
    // =========================================================

    private VBox createDashboard() {

        VBox page =
                new VBox(22);

        page.setPadding(
                new Insets(
                        28,
                        30,
                        40,
                        30
                )
        );

        page.setStyle(
                "-fx-background-color: "
                        + BG
                        + ";"
        );

        page
                .getChildren()
                .addAll(
                        createDashboardHeader(),
                        createStatistics(),
                        createMainOverview(),
                        createBottomSection()
                );

        return page;
    }

    // =========================================================
    // DASHBOARD HEADER
    // =========================================================

    private HBox createDashboardHeader() {

        HBox row =
                new HBox(15);

        row.setAlignment(
                Pos.CENTER_LEFT
        );

        VBox text =
                new VBox(6);

        Label title =
                new Label(
                        "RoadGuardian Overview"
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
                        30
                )
        );

        Label subtitle =
                new Label(
                        "Live overview of customers, mechanics, service requests and emergency SOS activity."
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

        text
                .getChildren()
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
                        dashboardController == null
                                ? "● SYSTEM ERROR"
                                : "● SYSTEM LIVE"
                );

        firebase.setTextFill(
                Color.web(
                        dashboardController == null
                                ? RED
                                : GREEN
                )
        );

        firebase.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        11
                )
        );

        Button refresh =
                createOutlineButton(
                        "Refresh"
                );

        refresh.setOnAction(
                event ->
                        showDashboard()
        );

        row
                .getChildren()
                .addAll(
                        text,
                        spacer,
                        firebase,
                        refresh
                );

        return row;
    }

    // =========================================================
    // STATISTICS
    // =========================================================

    private GridPane createStatistics() {

        GridPane grid =
                new GridPane();

        grid.setHgap(
                14
        );

        grid.setVgap(
                14
        );

        for (int i = 0;
             i < 5;
             i++) {

            ColumnConstraints column =
                    new ColumnConstraints();

            column.setPercentWidth(
                    20
            );

            column.setHgrow(
                    Priority.ALWAYS
            );

            grid
                    .getColumnConstraints()
                    .add(
                            column
                    );
        }

        int customers =
                getStatistic(
                        "customers"
                );

        int activeCustomers =
                getStatistic(
                        "activeCustomers"
                );

        int mechanics =
                getStatistic(
                        "mechanics"
                );

        int activeMechanics =
                getStatistic(
                        "activeMechanics"
                );

        int vehicles =
                getStatistic(
                        "vehicles"
                );

        int serviceRequests =
                getStatistic(
                        "serviceRequests"
                );

        int activeRequests =
                getStatistic(
                        "activeRequests"
                );

        int sosRequests =
                getStatistic(
                        "sosRequests"
                );

        int activeSOS =
                getStatistic(
                        "activeSOS"
                );

        grid.add(
                createStatCard(
                        "♙",
                        customers,
                        "Customers",
                        activeCustomers
                                + " active",
                        BLUE
                ),
                0,
                0
        );

        grid.add(
                createStatCard(
                        "⚒",
                        mechanics,
                        "Mechanics",
                        activeMechanics
                                + " active",
                        GREEN
                ),
                1,
                0
        );

        grid.add(
                createStatCard(
                        "▰",
                        vehicles,
                        "Vehicles",
                        "Registered vehicles",
                        ORANGE
                ),
                2,
                0
        );

        grid.add(
                createStatCard(
                        "▤",
                        serviceRequests,
                        "Service Requests",
                        activeRequests
                                + " active",
                        BLUE
                ),
                3,
                0
        );

        grid.add(
                createStatCard(
                        "SOS",
                        sosRequests,
                        "SOS Requests",
                        activeSOS
                                + " active",
                        activeSOS > 0
                                ? RED
                                : GREEN
                ),
                4,
                0
        );

        return grid;
    }

    // =========================================================
    // STAT CARD
    // =========================================================

    private VBox createStatCard(
            String icon,
            int value,
            String title,
            String footer,
            String accent
    ) {

        VBox card =
                new VBox(7);

        card.setPadding(
                new Insets(
                        17
                )
        );

        card.setMaxWidth(
                Double.MAX_VALUE
        );

        card.setMinHeight(
                122
        );

        card.setStyle(
                cardStyle()
        );

        HBox top =
                new HBox(10);

        top.setAlignment(
                Pos.CENTER_LEFT
        );

        StackPane iconBox =
                new StackPane();

        iconBox.setPrefSize(
                44,
                44
        );

        iconBox.setMinSize(
                44,
                44
        );

        iconBox.setStyle(
                "-fx-background-color: "
                        + accent
                        + ";"
                        + "-fx-background-radius: 10;"
        );

        Label iconLabel =
                new Label(
                        icon
                );

        iconLabel.setTextFill(
                Color.WHITE
        );

        iconLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        13
                )
        );

        iconBox
                .getChildren()
                .add(
                        iconLabel
                );

        Label valueLabel =
                new Label(
                        String.valueOf(
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
                        27
                )
        );

        top
                .getChildren()
                .addAll(
                        iconBox,
                        valueLabel
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

        Label footerLabel =
                new Label(
                        footer
                );

        footerLabel.setTextFill(
                Color.web(
                        TEXT
                )
        );

        footerLabel.setFont(
                Font.font(
                        "Arial",
                        10
                )
        );

        card
                .getChildren()
                .addAll(
                        top,
                        titleLabel,
                        footerLabel
                );

        return card;
    }

    // =========================================================
    // MAIN OVERVIEW
    // =========================================================

    private HBox createMainOverview() {

        HBox row =
                new HBox(18);

        VBox services =
                createServiceOverview();

        VBox sos =
                createSOSOverview();

        HBox.setHgrow(
                services,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                sos,
                Priority.ALWAYS
        );

        services.setMaxWidth(
                Double.MAX_VALUE
        );

        sos.setMaxWidth(
                Double.MAX_VALUE
        );

        row
                .getChildren()
                .addAll(
                        services,
                        sos
                );

        return row;
    }

    // =========================================================
    // SERVICE OVERVIEW
    // =========================================================

    private VBox createServiceOverview() {

        VBox card =
                createCard();

        HBox header =
                createSectionHeader(
                        "Service Request Lifecycle",
                        "Live"
                );

        int pending =
                getStatistic(
                        "pendingRequests"
                );

        int assigned =
                getStatistic(
                        "assignedRequests"
                );

        int accepted =
                getStatistic(
                        "acceptedRequests"
                );

        int inProgress =
                getStatistic(
                        "inProgressRequests"
                );

        int completed =
                getStatistic(
                        "completedRequests"
                );

        int cancelled =
                getStatistic(
                        "cancelledRequests"
                );

        int total =
                getStatistic(
                        "serviceRequests"
                );

        HBox body =
                new HBox(20);

        body.setAlignment(
                Pos.CENTER_LEFT
        );

        StackPane donut =
                createServiceDonut(
                        pending,
                        assigned,
                        accepted,
                        inProgress,
                        completed,
                        cancelled,
                        total
                );

        VBox lifecycle =
                new VBox(9);

        lifecycle
                .getChildren()
                .addAll(
                        statusRow(
                                "Pending",
                                pending,
                                ORANGE
                        ),
                        statusRow(
                                "Assigned",
                                assigned,
                                BLUE
                        ),
                        statusRow(
                                "Accepted",
                                accepted,
                                PURPLE
                        ),
                        statusRow(
                                "In Progress",
                                inProgress,
                                CYAN
                        ),
                        statusRow(
                                "Completed",
                                completed,
                                GREEN
                        ),
                        statusRow(
                                "Cancelled",
                                cancelled,
                                RED
                        )
                );

        HBox.setHgrow(
                lifecycle,
                Priority.ALWAYS
        );

        body
                .getChildren()
                .addAll(
                        donut,
                        lifecycle
                );

        Button manage =
                createPrimaryButton(
                        "Manage Service Requests",
                        BLUE,
                        BLUE_HOVER
                );

        manage.setOnAction(
                event ->
                        showServiceRequests()
        );

        card
                .getChildren()
                .addAll(
                        header,
                        new Separator(),
                        body,
                        manage
                );

        return card;
    }

    // =========================================================
    // DONUT
    // =========================================================

    private StackPane createServiceDonut(
            int pending,
            int assigned,
            int accepted,
            int inProgress,
            int completed,
            int cancelled,
            int total
    ) {

        StackPane chart =
                new StackPane();

        chart.setPrefSize(
                210,
                210
        );

        chart.setMinSize(
                210,
                210
        );

        if (total <= 0) {

            Circle outer =
                    new Circle(
                            72
                    );

            outer.setFill(
                    Color.web(
                            BORDER
                    )
            );

            Circle inner =
                    new Circle(
                            43
                    );

            inner.setFill(
                    Color.web(
                            SURFACE
                    )
            );

            chart
                    .getChildren()
                    .addAll(
                            outer,
                            inner,
                            createDonutCenter(
                                    0
                            )
                    );

            return chart;
        }

        double start =
                90.0;

        int[] values = {
                pending,
                assigned,
                accepted,
                inProgress,
                completed,
                cancelled
        };

        String[] colors = {
                ORANGE,
                BLUE,
                PURPLE,
                CYAN,
                GREEN,
                RED
        };

        for (int i = 0;
             i < values.length;
             i++) {

            if (values[i] <= 0) {

                continue;
            }

            double angle =
                    ((double) values[i]
                            / total)
                            * 360.0;

            Arc arc =
                    new Arc(
                            0,
                            0,
                            72,
                            72,
                            start,
                            -angle
                    );

            arc.setType(
                    ArcType.ROUND
            );

            arc.setFill(
                    Color.web(
                            colors[i]
                    )
            );

            chart
                    .getChildren()
                    .add(
                            arc
                    );

            start -=
                    angle;
        }

        Circle inner =
                new Circle(
                        43
                );

        inner.setFill(
                Color.web(
                        SURFACE
                )
        );

        chart
                .getChildren()
                .addAll(
                        inner,
                        createDonutCenter(
                                total
                        )
                );

        return chart;
    }

    // =========================================================
    // DONUT CENTER
    // =========================================================

    private VBox createDonutCenter(
            int total
    ) {

        VBox center =
                new VBox(1);

        center.setAlignment(
                Pos.CENTER
        );

        Label title =
                new Label(
                        "Total"
                );

        title.setTextFill(
                Color.web(
                        TEXT
                )
        );

        title.setFont(
                Font.font(
                        "Arial",
                        11
                )
        );

        Label value =
                new Label(
                        String.valueOf(
                                total
                        )
                );

        value.setTextFill(
                Color.web(
                        HEADING
                )
        );

        value.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        22
                )
        );

        center
                .getChildren()
                .addAll(
                        title,
                        value
                );

        return center;
    }

    // =========================================================
    // SOS OVERVIEW
    // =========================================================

    private VBox createSOSOverview() {

        VBox card =
                createCard();

        int pending =
                getStatistic(
                        "pendingSOS"
                );

        int assigned =
                getStatistic(
                        "assignedSOS"
                );

        int accepted =
                getStatistic(
                        "acceptedSOS"
                );

        int inProgress =
                getStatistic(
                        "inProgressSOS"
                );

        int resolved =
                getStatistic(
                        "resolvedSOS"
                );

        int cancelled =
                getStatistic(
                        "cancelledSOS"
                );

        int active =
                getStatistic(
                        "activeSOS"
                );

        HBox header =
                createSectionHeader(
                        "SOS Emergency Lifecycle",
                        active > 0
                                ? active + " Active"
                                : "No Active SOS"
                );

        if (active > 0) {

            card.setStyle(
                    "-fx-background-color: #2A1717;"
                            + "-fx-border-color: #FCA5A5;"
                            + "-fx-border-width: 1.5;"
                            + "-fx-border-radius: 14;"
                            + "-fx-background-radius: 14;"
            );
        }

        GridPane statuses =
                new GridPane();

        statuses.setHgap(
                9
        );

        statuses.setVgap(
                9
        );

        for (int i = 0;
             i < 3;
             i++) {

            ColumnConstraints column =
                    new ColumnConstraints();

            column.setPercentWidth(
                    33.33
            );

            column.setHgrow(
                    Priority.ALWAYS
            );

            statuses
                    .getColumnConstraints()
                    .add(
                            column
                    );
        }

        statuses.add(
                createMiniStatusCard(
                        "Pending",
                        pending,
                        ORANGE
                ),
                0,
                0
        );

        statuses.add(
                createMiniStatusCard(
                        "Assigned",
                        assigned,
                        BLUE
                ),
                1,
                0
        );

        statuses.add(
                createMiniStatusCard(
                        "Accepted",
                        accepted,
                        PURPLE
                ),
                2,
                0
        );

        statuses.add(
                createMiniStatusCard(
                        "In Progress",
                        inProgress,
                        CYAN
                ),
                0,
                1
        );

        statuses.add(
                createMiniStatusCard(
                        "Resolved",
                        resolved,
                        GREEN
                ),
                1,
                1
        );

        statuses.add(
                createMiniStatusCard(
                        "Cancelled",
                        cancelled,
                        RED
                ),
                2,
                1
        );

        VBox recent =
                new VBox(7);

        Label recentTitle =
                new Label(
                        "Recent SOS Requests"
                );

        recentTitle.setTextFill(
                Color.web(
                        HEADING
                )
        );

        recentTitle.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        12
                )
        );

        recent
                .getChildren()
                .add(
                        recentTitle
                );

        if (recentSOSRequests.isEmpty()) {

            recent
                    .getChildren()
                    .add(
                            createEmptyLabel(
                                    "No SOS requests found."
                            )
                    );

        } else {

            for (SOSRequest request :
                    recentSOSRequests) {

                recent
                        .getChildren()
                        .add(
                                createRecentSOSRow(
                                        request
                                )
                        );
            }
        }

        Button manage =
                createPrimaryButton(
                        "Open SOS Requests",
                        RED,
                        RED_HOVER
                );

        manage.setOnAction(
                event ->
                        showSOSRequests()
        );

        card
                .getChildren()
                .addAll(
                        header,
                        new Separator(),
                        statuses,
                        new Separator(),
                        recent,
                        manage
                );

        return card;
    }

    // =========================================================
    // MINI STATUS CARD
    // =========================================================

    private VBox createMiniStatusCard(
            String title,
            int count,
            String color
    ) {

        VBox card =
                new VBox(4);

        card.setPadding(
                new Insets(
                        10
                )
        );

        card.setMaxWidth(
                Double.MAX_VALUE
        );

        card.setStyle(
                "-fx-background-color: "
                        + SECONDARY
                        + ";"
                        + "-fx-background-radius: 8;"
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

        Label value =
                new Label(
                        String.valueOf(
                                count
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
                        18
                )
        );

        card
                .getChildren()
                .addAll(
                        titleLabel,
                        value
                );

        return card;
    }

    // =========================================================
    // RECENT SOS ROW
    // =========================================================

    private HBox createRecentSOSRow(
            SOSRequest request
    ) {

        HBox row =
                new HBox(10);

        row.setAlignment(
                Pos.CENTER_LEFT
        );

        row.setPadding(
                new Insets(
                        8,
                        10,
                        8,
                        10
                )
        );

        row.setStyle(
                "-fx-background-color: "
                        + SECONDARY
                        + ";"
                        + "-fx-background-radius: 8;"
                        + "-fx-cursor: hand;"
        );

        VBox details =
                new VBox(2);

        Label customer =
                new Label(
                        dashboardController
                                .getSOSCustomer(
                                        request
                                )
                );

        customer.setTextFill(
                Color.web(
                        HEADING
                )
        );

        customer.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        11
                )
        );

        Label location =
                new Label(
                        dashboardController
                                .getSOSLocation(
                                        request
                                )
                );

        location.setTextFill(
                Color.web(
                        TEXT
                )
        );

        location.setFont(
                Font.font(
                        "Arial",
                        9
                )
        );

        details
                .getChildren()
                .addAll(
                        customer,
                        location
                );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        String status =
                dashboardController
                        .getSOSStatus(
                                request
                        );

        Label badge =
                createStatusBadge(
                        status,
                        getSOSStatusColor(
                                status
                        )
                );

        row
                .getChildren()
                .addAll(
                        details,
                        spacer,
                        badge
                );

        row.setOnMouseClicked(
                event ->
                        showSOSRequests()
        );

        return row;
    }

    // =========================================================
    // BOTTOM
    // =========================================================

    private HBox createBottomSection() {

        HBox row =
                new HBox(18);

        VBox recent =
                createRecentServiceRequests();

        VBox actions =
                createQuickActions();

        HBox.setHgrow(
                recent,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                actions,
                Priority.ALWAYS
        );

        recent.setMaxWidth(
                Double.MAX_VALUE
        );

        actions.setMaxWidth(
                Double.MAX_VALUE
        );

        row
                .getChildren()
                .addAll(
                        recent,
                        actions
                );

        return row;
    }

    // =========================================================
    // RECENT SERVICE REQUESTS
    //
    // NO HARDCODED SR1256 / RAHUL SHARMA.
    // =========================================================

    private VBox createRecentServiceRequests() {

        VBox card =
                createCard();

        HBox header =
                createSectionHeader(
                        "Recent Service Requests",
                        recentServiceRequests.size()
                                + " shown"
                );

        VBox list =
                new VBox(9);

        if (recentServiceRequests.isEmpty()) {

            list
                    .getChildren()
                    .add(
                            createEmptyLabel(
                                    "No service requests found in Firebase."
                            )
                    );

        } else {

            for (ServiceRequest request :
                    recentServiceRequests) {

                list
                        .getChildren()
                        .add(
                                createRecentServiceRow(
                                        request
                                )
                        );
            }
        }

        Button viewAll =
                createOutlineButton(
                        "View All Service Requests"
                );

        viewAll.setOnAction(
                event ->
                        showServiceRequests()
        );

        card
                .getChildren()
                .addAll(
                        header,
                        new Separator(),
                        list,
                        viewAll
                );

        return card;
    }

    // =========================================================
    // RECENT SERVICE ROW
    // =========================================================

    private HBox createRecentServiceRow(
            ServiceRequest request
    ) {

        HBox row =
                new HBox(12);

        row.setAlignment(
                Pos.CENTER_LEFT
        );

        row.setPadding(
                new Insets(
                        10
                )
        );

        row.setStyle(
                "-fx-background-color: "
                        + SECONDARY
                        + ";"
                        + "-fx-background-radius: 9;"
                        + "-fx-cursor: hand;"
        );

        VBox requestBox =
                new VBox(2);

        Label id =
                new Label(
                        dashboardController
                                .getServiceRequestId(
                                        request
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
                        12
                )
        );

        Label customer =
                new Label(
                        dashboardController
                                .getServiceCustomer(
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
                        10
                )
        );

        requestBox
                .getChildren()
                .addAll(
                        id,
                        customer
                );

        VBox serviceBox =
                new VBox(2);

        Label service =
                new Label(
                        dashboardController
                                .getServiceType(
                                        request
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
                        10
                )
        );

        Label vehicle =
                new Label(
                        dashboardController
                                .getServiceVehicle(
                                        request
                                )
                );

        vehicle.setTextFill(
                Color.web(
                        TEXT
                )
        );

        vehicle.setFont(
                Font.font(
                        "Arial",
                        9
                )
        );

        serviceBox
                .getChildren()
                .addAll(
                        service,
                        vehicle
                );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        VBox right =
                new VBox(3);

        right.setAlignment(
                Pos.CENTER_RIGHT
        );

        String status =
                dashboardController
                        .getServiceStatus(
                                request
                        );

        Label statusLabel =
                createStatusBadge(
                        status,
                        getServiceStatusColor(
                                status
                        )
                );

        Label mechanic =
                new Label(
                        dashboardController
                                .getServiceMechanic(
                                        request
                                )
                );

        mechanic.setTextFill(
                Color.web(
                        TEXT
                )
        );

        mechanic.setFont(
                Font.font(
                        "Arial",
                        8
                )
        );

        right
                .getChildren()
                .addAll(
                        statusLabel,
                        mechanic
                );

        row
                .getChildren()
                .addAll(
                        requestBox,
                        serviceBox,
                        spacer,
                        right
                );

        row.setOnMouseClicked(
                event ->
                        showServiceRequests()
        );

        return row;
    }

    // =========================================================
    // QUICK ACTIONS
    // =========================================================

    private VBox createQuickActions() {

        VBox card =
                createCard();

        HBox header =
                createSectionHeader(
                        "Quick Actions",
                        "Admin tools"
                );

        GridPane grid =
                new GridPane();

        grid.setHgap(
                12
        );

        grid.setVgap(
                12
        );

        for (int i = 0;
             i < 3;
             i++) {

            ColumnConstraints column =
                    new ColumnConstraints();

            column.setPercentWidth(
                    33.33
            );

            column.setHgrow(
                    Priority.ALWAYS
            );

            grid
                    .getColumnConstraints()
                    .add(
                            column
                    );
        }

        VBox customer =
                quickAction(
                        "♙",
                        "Customers",
                        BLUE
                );

        VBox mechanic =
                quickAction(
                        "⚒",
                        "Mechanics",
                        GREEN
                );

        VBox vehicle =
                quickAction(
                        "▰",
                        "Vehicles",
                        ORANGE
                );

        VBox requests =
                quickAction(
                        "▤",
                        "Service Requests",
                        BLUE
                );

        VBox sos =
                quickAction(
                        "SOS",
                        "SOS Requests",
                        RED
                );

        VBox notifications =
                quickAction(
                        "♢",
                        "Notifications",
                        PURPLE
                );

        customer.setOnMouseClicked(
                event ->
                        showCustomers()
        );

        mechanic.setOnMouseClicked(
                event ->
                        showMechanics()
        );

        vehicle.setOnMouseClicked(
                event ->
                        showVehicles()
        );

        requests.setOnMouseClicked(
                event ->
                        showServiceRequests()
        );

        sos.setOnMouseClicked(
                event ->
                        showSOSRequests()
        );

        notifications.setOnMouseClicked(
                event ->
                        showNotifications()
        );

        grid.add(
                customer,
                0,
                0
        );

        grid.add(
                mechanic,
                1,
                0
        );

        grid.add(
                vehicle,
                2,
                0
        );

        grid.add(
                requests,
                0,
                1
        );

        grid.add(
                sos,
                1,
                1
        );

        grid.add(
                notifications,
                2,
                1
        );

        card
                .getChildren()
                .addAll(
                        header,
                        new Separator(),
                        grid
                );

        return card;
    }

    // =========================================================
    // QUICK ACTION
    // =========================================================

    private VBox quickAction(
            String icon,
            String title,
            String color
    ) {

        VBox box =
                new VBox(8);

        box.setAlignment(
                Pos.CENTER
        );

        box.setPadding(
                new Insets(
                        14
                )
        );

        box.setMinHeight(
                105
        );

        box.setMaxWidth(
                Double.MAX_VALUE
        );

        box.setStyle(
                "-fx-background-color: "
                        + SECONDARY
                        + ";"
                        + "-fx-border-color: "
                        + BORDER
                        + ";"
                        + "-fx-border-radius: 10;"
                        + "-fx-background-radius: 10;"
                        + "-fx-cursor: hand;"
        );

        Label iconLabel =
                new Label(
                        icon
                );

        iconLabel.setTextFill(
                Color.web(
                        color
                )
        );

        iconLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        24
                )
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
                        11
                )
        );

        titleLabel.setWrapText(
                true
        );

        titleLabel.setTextAlignment(
                TextAlignment.CENTER
        );

        box
                .getChildren()
                .addAll(
                        iconLabel,
                        titleLabel
                );

        return box;
    }

    // =========================================================
    // STATUS ROW
    // =========================================================

    private HBox statusRow(
            String title,
            int count,
            String color
    ) {

        HBox row =
                new HBox(9);

        row.setAlignment(
                Pos.CENTER_LEFT
        );

        Circle dot =
                new Circle(
                        5,
                        Color.web(
                                color
                        )
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
                        11
                )
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Label value =
                new Label(
                        String.valueOf(
                                count
                        )
                );

        value.setTextFill(
                Color.web(
                        HEADING
                )
        );

        value.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        12
                )
        );

        row
                .getChildren()
                .addAll(
                        dot,
                        titleLabel,
                        spacer,
                        value
                );

        return row;
    }

    // =========================================================
    // SECTION HEADER
    // =========================================================

    private HBox createSectionHeader(
            String title,
            String hint
    ) {

        HBox row =
                new HBox(10);

        row.setAlignment(
                Pos.CENTER_LEFT
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
                        17
                )
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Label hintLabel =
                new Label(
                        hint
                );

        hintLabel.setTextFill(
                Color.web(
                        TEXT
                )
        );

        hintLabel.setFont(
                Font.font(
                        "Arial",
                        10
                )
        );

        row
                .getChildren()
                .addAll(
                        titleLabel,
                        spacer,
                        hintLabel
                );

        return row;
    }

    // =========================================================
    // CARD
    // =========================================================

    private VBox createCard() {

        VBox card =
                new VBox(13);

        card.setPadding(
                new Insets(
                        19
                )
        );

        card.setStyle(
                cardStyle()
        );

        return card;
    }

    private String cardStyle() {

        return "-fx-background-color: "
                + SURFACE
                + ";"
                + "-fx-border-color: "
                + BORDER
                + ";"
                + "-fx-border-radius: 14;"
                + "-fx-background-radius: 14;";
    }

    // =========================================================
    // STATUS BADGE
    // =========================================================

    private Label createStatusBadge(
            String status,
            String color
    ) {

        Label label =
                new Label(
                        firstNonBlank(
                                status,
                                "-"
                        )
                );

        label.setPadding(
                new Insets(
                        5,
                        9,
                        5,
                        9
                )
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
                        9
                )
        );

        label.setStyle(
                "-fx-background-color: "
                        + color
                        + "18;"
                        + "-fx-background-radius: 12;"
        );

        return label;
    }

    // =========================================================
    // SERVICE STATUS COLOR
    // =========================================================

    private String getServiceStatusColor(
            String status
    ) {

        status =
                clean(
                        status
                );

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
    // SOS STATUS COLOR
    // =========================================================

    private String getSOSStatusColor(
            String status
    ) {

        if (status == null) {

            return TEXT;
        }

        if (status.equalsIgnoreCase(
                "Pending"
        )) {

            return RED;
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

            return TEXT;
        }

        return RED;
    }

    // =========================================================
    // BUTTONS
    // =========================================================

    private Button createPrimaryButton(
            String title,
            String color,
            String hover
    ) {

        Button button =
                new Button(
                        title
                );

        button.setPrefHeight(
                39
        );

        button.setMaxWidth(
                Double.MAX_VALUE
        );

        button.setPadding(
                new Insets(
                        0,
                        15,
                        0,
                        15
                )
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

        button.setStyle(
                "-fx-background-color: "
                        + color
                        + ";"
                        + "-fx-background-radius: 8;"
                        + "-fx-cursor: hand;"
        );

        button.setOnMouseEntered(
                event ->
                        button.setStyle(
                                "-fx-background-color: "
                                        + hover
                                        + ";"
                                        + "-fx-background-radius: 8;"
                                        + "-fx-cursor: hand;"
                        )
        );

        button.setOnMouseExited(
                event ->
                        button.setStyle(
                                "-fx-background-color: "
                                        + color
                                        + ";"
                                        + "-fx-background-radius: 8;"
                                        + "-fx-cursor: hand;"
                        )
        );

        return button;
    }

    private Button createOutlineButton(
            String title
    ) {

        Button button =
                new Button(
                        title
                );

        button.setPrefHeight(
                38
        );

        button.setPadding(
                new Insets(
                        0,
                        14,
                        0,
                        14
                )
        );

        button.setTextFill(
                Color.web(
                        BLUE
                )
        );

        button.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        10
                )
        );

        button.setStyle(
                "-fx-background-color: transparent;"
                        + "-fx-border-color: "
                        + BLUE
                        + ";"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
                        + "-fx-cursor: hand;"
        );

        return button;
    }

    // =========================================================
    // EMPTY
    // =========================================================

    private Label createEmptyLabel(
            String text
    ) {

        Label label =
                new Label(
                        text
                );

        label.setWrapText(
                true
        );

        label.setPadding(
                new Insets(
                        12
                )
        );

        label.setTextFill(
                Color.web(
                        TEXT
                )
        );

        label.setFont(
                Font.font(
                        "Arial",
                        11
                )
        );

        return label;
    }

    // =========================================================
    // STAT
    // =========================================================

    private int getStatistic(
            String key
    ) {

        if (dashboardController == null) {

            return 0;
        }

        return dashboardController
                .getValue(
                        dashboardData,
                        key
                );
    }

    // =========================================================
    // DATE BOX
    // =========================================================

    private VBox createDateBox() {

        VBox box =
                new VBox(3);

        box.setAlignment(
                Pos.CENTER
        );

        box.setPadding(
                new Insets(
                        12
                )
        );

        box.setStyle(
                "-fx-background-color: "
                        + SECONDARY
                        + ";"
                        + "-fx-border-color: "
                        + BORDER
                        + ";"
                        + "-fx-border-radius: 9;"
                        + "-fx-background-radius: 9;"
        );

        Label date =
                new Label(
                        LocalDate.now()
                                .format(
                                        DateTimeFormatter
                                                .ofPattern(
                                                        "dd MMM yyyy"
                                                )
                                )
                );

        date.setTextFill(
                Color.web(
                        TEXT
                )
        );

        date.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        11
                )
        );

        Label time =
                new Label(
                        LocalTime.now()
                                .format(
                                        DateTimeFormatter
                                                .ofPattern(
                                                        "hh:mm a"
                                                )
                                )
                );

        time.setTextFill(
                Color.web(
                        HEADING
                )
        );

        time.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        16
                )
        );

        box
                .getChildren()
                .addAll(
                        date,
                        time
                );

        return box;
    }

    // =========================================================
    // LOGOUT
    // =========================================================

    private Button createLogoutButton() {

        Button button =
                new Button(
                        "↪  Logout"
                );

        button.setMaxWidth(
                Double.MAX_VALUE
        );

        button.setPrefHeight(
                44
        );

        button.setAlignment(
                Pos.CENTER_LEFT
        );

        button.setPadding(
                new Insets(
                        0,
                        15,
                        0,
                        15
                )
        );

        button.setTextFill(
                Color.web(
                        RED
                )
        );

        button.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        14
                )
        );

        button.setStyle(
                "-fx-background-color: transparent;"
                        + "-fx-cursor: hand;"
        );

        button.setOnAction(
                event ->
                        confirmLogout()
        );

        return button;
    }

    // =========================================================
    // CONFIRM LOGOUT
    // =========================================================

    private void confirmLogout() {

        Alert alert =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        if (stage != null) {

            alert.initOwner(
                    stage
            );
        }

        alert.setTitle(
                "RoadGuardian"
        );

        alert.setHeaderText(
                "Logout from Admin Panel?"
        );

        alert.setContentText(
                "Your current admin session will be closed."
        );

        if (alert
                .showAndWait()
                .orElse(
                        ButtonType.CANCEL
                ) != ButtonType.OK) {

            return;
        }

        logout();
    }

    // =========================================================
    // LOGOUT
    //
    // LoginController currently may pass null callback,
    // so fallback login navigation is included.
    // =========================================================

    private void logout() {

        try {

            if (autoRefreshTimeline != null) {

                autoRefreshTimeline.stop();
            }

            UserSession.clear();

            if (logOutAction != null) {

                logOutAction.run();

                return;
            }

            Stage targetStage =
                    stage;

            if (targetStage == null
                    &&
                    root != null
                    &&
                    root.getScene() != null
                    &&
                    root.getScene()
                            .getWindow()
                            instanceof Stage) {

                targetStage =
                        (Stage)
                                root.getScene()
                                        .getWindow();
            }

            if (targetStage == null) {

                targetStage =
                        LoginPage.getMainStage();
            }

            if (targetStage == null) {

                System.err.println(
                        "Admin logout failed: Stage unavailable."
                );

                return;
            }

            LoginPage loginPage =
                    new LoginPage();

            Scene loginScene =
                    new Scene(
                            loginPage.getView(),
                            1200,
                            700
                    );

            targetStage.setScene(
                    loginScene
            );

            targetStage.setTitle(
                    "RoadGuardian - Login"
            );

            targetStage.show();

            targetStage.toFront();

        } catch (Exception e) {

            System.err.println(
                    "Admin logout error: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    // =========================================================
    // NAVIGATION PAGES
    // =========================================================

    private void showCustomers() {

        currentPage =
                "customers";

        setActive(
                customersButton
        );

        CustomerManagementPage page =
                new CustomerManagementPage();

        contentArea
                .getChildren()
                .setAll(
                        page.getView()
                );

        scrollTop();
    }

    private void showMechanics() {

        currentPage =
                "mechanics";

        setActive(
                mechanicsButton
        );

        MechanicManagementPage page =
                new MechanicManagementPage();

        contentArea
                .getChildren()
                .setAll(
                        page.getView()
                );

        scrollTop();
    }

    private void showVehicles() {

        currentPage =
                "vehicles";

        setActive(
                vehiclesButton
        );

        VehicleManagementPage page =
                new VehicleManagementPage();

        contentArea
                .getChildren()
                .setAll(
                        page.getView()
                );

        scrollTop();
    }

    private void showServiceRequests() {

        currentPage =
                "serviceRequests";

        setActive(
                serviceRequestButton
        );

        ServiceRequestManagementPage page =
                new ServiceRequestManagementPage();

        contentArea
                .getChildren()
                .setAll(
                        page.getView()
                );

        scrollTop();
    }

    private void showSOSRequests() {

        currentPage =
                "sos";

        setActive(
                sosButton
        );

        SOSRequestManagementPage page =
                new SOSRequestManagementPage();

        contentArea
                .getChildren()
                .setAll(
                        page.getView()
                );

        scrollTop();
    }

    private void showServices() {

        currentPage =
                "services";

        setActive(
                servicesButton
        );

        ServiceManagementPage page =
                new ServiceManagementPage();

        contentArea
                .getChildren()
                .setAll(
                        page.getView()
                );

        scrollTop();
    }

    private void showReviews() {

        currentPage =
                "reviews";

        setActive(
                reviewsButton
        );

        ReviewManagementPage page =
                new ReviewManagementPage();

        contentArea
                .getChildren()
                .setAll(
                        page.getView()
                );

        scrollTop();
    }

    private void showComplaints() {

        currentPage =
                "complaints";

        setActive(
                complaintsButton
        );

        ComplaintManagementPage page =
                new ComplaintManagementPage();

        contentArea
                .getChildren()
                .setAll(
                        page.getView()
                );

        scrollTop();
    }

    private void showNotifications() {

        currentPage =
                "notifications";

        setActive(
                notificationsButton
        );

        NotificationManagementPage page =
                new NotificationManagementPage();

        contentArea
                .getChildren()
                .setAll(
                        page.getView()
                );

        scrollTop();
    }

    private void showReports() {

        currentPage =
                "reports";

        setActive(
                reportsButton
        );

        ReportManagementPage page =
                new ReportManagementPage();

        contentArea
                .getChildren()
                .setAll(
                        page.getView()
                );

        scrollTop();
    }

    private void showSettings() {

        currentPage =
                "settings";

        setActive(
                settingsButton
        );

        SettingsPage page =
                new SettingsPage();

        contentArea
                .getChildren()
                .setAll(
                        page.getView()
                );

        scrollTop();
    }

    // =========================================================
    // SCROLL TOP
    // =========================================================

    private void scrollTop() {

        if (contentScroll != null) {

            contentScroll.setVvalue(
                    0
            );
        }
    }

    // =========================================================
    // STRING
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
