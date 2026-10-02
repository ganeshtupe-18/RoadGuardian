package project.ui.user;
import java.io.IOException;

import java.time.LocalTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.Node;
import javafx.scene.Scene;
import project.util.IconUtil;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import javafx.scene.paint.Color;

import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import javafx.stage.Stage;

import project.app.AppNavigator;
import project.controller.user.UserDashboardController;
import project.ui.landing.LoginPage;

public class UserDashboard {

        // ============================================================
        // COLORS
        // ============================================================

        private static final String BACKGROUND = "#0F0F0F";

        private static final String CARD = "#1A1A1A";

        private static final String SECONDARY_SURFACE = "#242424";

        private static final String HEADING = "#F3F4F6";

        private static final String SECONDARY_TEXT = "#A1A1AA";

        private static final String NAV_BLUE = "#F59E0B";

        private static final String BORDER = "#F59E0B";

        private static final String GREEN = "#22C55E";

        private static final String ORANGE = "#F59E0B";

        private static final String RED = "#EF4444";

        private static final String PURPLE = "#A78BFA";

        private static final String CYAN = "#38BDF8";

        // ============================================================
        // WINDOW
        // ============================================================

        public static Stage dashboardStage;

        // ============================================================
        // USER
        // ============================================================

        private String userEmail;

        // ============================================================
        // CONTROLLER
        // ============================================================

        private final UserDashboardController controller;

        // ============================================================
        // DATA
        // ============================================================

        private Map<String, Object> userData = Collections.emptyMap();

        private List<Map<String, Object>> vehicles = new ArrayList<>();

        private List<Map<String, Object>> activeRequests = new ArrayList<>();

        private List<Map<String, Object>> serviceHistory = new ArrayList<>();

        private List<Map<String, Object>> mechanics = new ArrayList<>();

        // ============================================================
        // SCENE
        // ============================================================

        private Scene dashboardScene;

        // ============================================================
        // ROOT
        // ============================================================

        private BorderPane dashboardRoot;

        // ============================================================
        // CONSTRUCTORS
        // ============================================================

        public UserDashboard() {

                this(
                                UserSession.getUserEmail());
        }

        public UserDashboard(
                        String userEmail) {

                controller = new UserDashboardController();

                resolveUserEmail(
                                userEmail);

                loadDashboardData();
        }

        // ============================================================
        // RESOLVE USER EMAIL / ID
        // ============================================================

        private void resolveUserEmail(
                        String suppliedEmail) {

                String value = clean(
                                suppliedEmail);

                if (value == null) {

                        value = clean(
                                        UserSession.getUserEmail());
                }

                if (value == null) {

                        value = clean(
                                        UserSession.getUserId());
                }

                if (value == null) {

                        userEmail = "";

                        return;
                }

                userEmail = value.contains("@")
                                ? value.toLowerCase()
                                : value;
        }

        // ============================================================
        // LOAD ALL DASHBOARD DATA
        // ============================================================

        private void loadDashboardData() {

                userData = Collections.emptyMap();

                vehicles = new ArrayList<>();

                activeRequests = new ArrayList<>();

                serviceHistory = new ArrayList<>();

                mechanics = new ArrayList<>();

                if (clean(
                                userEmail) == null) {

                        return;
                }

                // ========================================================
                // PROFILE
                // ========================================================

                try {

                        Map<String, Object> profile = controller.getUserProfile(
                                        userEmail);

                        if (profile != null) {

                                userData = profile;
                        }

                } catch (Exception e) {

                        System.err.println(
                                        "Dashboard profile load error: "
                                                        + e.getMessage());
                }

                // ========================================================
                // VEHICLES
                // ========================================================

                try {

                        List<Map<String, Object>> result = controller.getVehicles(
                                        userEmail);

                        if (result != null) {

                                vehicles = result;
                        }

                } catch (Exception e) {

                        System.err.println(
                                        "Dashboard vehicle load error: "
                                                        + e.getMessage());
                }

                // ========================================================
                // ACTIVE REQUESTS
                // ========================================================

                try {

                        List<Map<String, Object>> result = controller.getActiveRequests(
                                        userEmail);

                        if (result != null) {

                                activeRequests = result;
                        }

                } catch (Exception e) {

                        System.err.println(
                                        "Dashboard active request load error: "
                                                        + e.getMessage());
                }

                // ========================================================
                // SERVICE HISTORY
                // ========================================================

                try {

                        List<Map<String, Object>> result = controller.getServiceHistory(
                                        userEmail);

                        if (result != null) {

                                serviceHistory = result;
                        }

                } catch (Exception e) {

                        System.err.println(
                                        "Dashboard service history load error: "
                                                        + e.getMessage());
                }

                // ========================================================
                // MECHANICS
                // ========================================================

                try {

                        List<Map<String, Object>> result = controller.getNearbyMechanics();

                        if (result != null) {

                                mechanics = result;
                        }

                } catch (Exception e) {

                        System.err.println(
                                        "Dashboard mechanic load error: "
                                                        + e.getMessage());
                }
        }

        // ============================================================
        // START
        // ============================================================

        public void start(
                        Stage stage) {

                if (stage == null) {

                        System.err.println(
                                        "UserDashboard: Stage is null.");

                        return;
                }

                dashboardStage = stage;

                Scene scene = getDashboardScene();

                stage.setScene(
                                scene);

                stage.setTitle(
                                "RoadGuardian - Customer Dashboard");

                stage.show();

                stage.toFront();
        }

        // ============================================================
        // GET DASHBOARD SCENE
        // ============================================================

        public Scene getDashboardScene() {

                /*
                 * Important:
                 * Refresh every time Dashboard is opened again.
                 */
                loadDashboardData();

                dashboardRoot = new BorderPane();

                dashboardRoot.setStyle(
                                "-fx-background-color: "
                                                + BACKGROUND
                                                + ";");

                // ========================================================
                // HEADER
                // ========================================================

                dashboardRoot.setTop(
                                UserHeader.createHeader());

                // ========================================================
                // SIDEBAR
                // ========================================================

                dashboardRoot.setLeft(
                                UserSideBar.createSidebar(
                                                "Dashboard"));

                // ========================================================
                // CONTENT
                // ========================================================

                ScrollPane scrollPane = createMainScrollPane();

                VBox content = createDashboardContent();

                scrollPane.setContent(
                                content);

                dashboardRoot.setCenter(
                                scrollPane);

                dashboardScene = new Scene(
                                dashboardRoot);

                dashboardScene.setFill(
                                Color.web(
                                                BACKGROUND));

                return dashboardScene;
        }

        // ============================================================
        // PUBLIC REFRESH
        // ============================================================

        public void refreshDashboard() {

                loadDashboardData();

                if (dashboardRoot == null) {

                        return;
                }

                ScrollPane scrollPane = createMainScrollPane();

                scrollPane.setContent(
                                createDashboardContent());

                dashboardRoot.setCenter(
                                scrollPane);
        }

        // ============================================================
        // MAIN SCROLL PANE
        // ============================================================

        private ScrollPane createMainScrollPane() {

                ScrollPane scrollPane = new ScrollPane();

                scrollPane.setFitToWidth(
                                true);

                scrollPane.setFitToHeight(
                                false);

                scrollPane.setHbarPolicy(
                                ScrollPane.ScrollBarPolicy.NEVER);

                scrollPane.setVbarPolicy(
                                ScrollPane.ScrollBarPolicy.AS_NEEDED);

                scrollPane.setPannable(
                                true);

                scrollPane.setCache(
                                false);

                scrollPane.setStyle(
                                "-fx-background-color: "
                                                + BACKGROUND
                                                + ";"
                                                + "-fx-background: "
                                                + BACKGROUND
                                                + ";"
                                                + "-fx-control-inner-background: "
                                                + BACKGROUND
                                                + ";"
                                                + "-fx-border-color: transparent;");

                return scrollPane;
        }

        // ============================================================
        // DASHBOARD CONTENT
        // ============================================================

        private VBox createDashboardContent() {

                VBox content = new VBox(24);

                content.setPadding(
                                new Insets(
                                                30,
                                                38,
                                                40,
                                                38));

                content.setFillWidth(
                                true);

                content.setMaxWidth(
                                Double.MAX_VALUE);

                // ========================================================
                // WELCOME
                // ========================================================

                content.getChildren()
                                .add(
                                                createWelcomeSection());

                // ========================================================
                // STATISTICS
                // ========================================================

                content.getChildren()
                                .add(
                                                createStatusGrid());

                // ========================================================
                // MAIN DASHBOARD
                // ========================================================

                content.getChildren()
                                .add(
                                                createMainGrid());

                // ========================================================
                // QUICK ACTIONS
                // ========================================================

                content.getChildren()
                                .add(
                                                createQuickActions());

                // ========================================================
                // LOGOUT
                // ========================================================

                content.getChildren()
                                .add(
                                                createLogoutSection());

                return content;
        }

        // ============================================================
        // WELCOME SECTION
        // ============================================================

        private HBox createWelcomeSection() {

                HBox row = new HBox(15);

                row.setAlignment(
                                Pos.CENTER_LEFT);

                String name = controller.getCustomerName(
                                userData);

                if (clean(
                                name) == null) {

                        name = firstNonBlank(
                                        UserSession.getUserName(),
                                        "Customer");
                }

                VBox text = new VBox(6);

                Label welcome = new Label(
                                getGreeting()
                                                + ", "
                                                + name);

                welcome.setFont(
                                Font.font(
                                                "Arial",
                                                FontWeight.BOLD,
                                                30));

                welcome.setTextFill(
                                Color.web(
                                                HEADING));

                Label subtitle = new Label(
                                getDashboardSubtitle());

                subtitle.setFont(
                                Font.font(
                                                "Arial",
                                                14));

                subtitle.setTextFill(
                                Color.web(
                                                SECONDARY_TEXT));

                text.getChildren()
                                .addAll(
                                                welcome,
                                                subtitle);

                Region spacer = new Region();

                HBox.setHgrow(
                                spacer,
                                Priority.ALWAYS);

                Button refresh = createOutlineButton(
                                "Refresh Dashboard");

                refresh.setOnAction(
                                event -> refreshDashboard());

                row.getChildren()
                                .addAll(
                                                text,
                                                spacer,
                                                refresh);

                return row;
        }

        // ============================================================
        // GREETING
        // ============================================================

        private String getGreeting() {

                int hour = LocalTime.now()
                                .getHour();

                if (hour < 12) {

                        return "Good morning";
                }

                if (hour < 17) {

                        return "Good afternoon";
                }

                return "Good evening";
        }

        // ============================================================
        // DASHBOARD SUBTITLE
        // ============================================================

        private String getDashboardSubtitle() {

                if (!activeRequests.isEmpty()) {

                        Map<String, Object> request = activeRequests.get(0);

                        return controller
                                        .getRequestStatusMessage(
                                                        request);
                }

                if (!vehicles.isEmpty()) {

                        return "Your RoadGuardian account is ready for roadside and vehicle assistance.";
                }

                return "Add your vehicle to start using RoadGuardian services.";
        }

        // ============================================================
        // STATUS GRID
        // ============================================================

        private GridPane createStatusGrid() {

                GridPane grid = new GridPane();

                grid.setHgap(
                                16);

                grid.setVgap(
                                16);

                grid.setMaxWidth(
                                Double.MAX_VALUE);

                for (int i = 0; i < 4; i++) {

                        ColumnConstraints column = new ColumnConstraints();

                        column.setPercentWidth(
                                        25);

                        column.setHgrow(
                                        Priority.ALWAYS);

                        grid.getColumnConstraints()
                                        .add(
                                                        column);
                }

                int vehicleCount = vehicles.size();

                int activeCount = activeRequests.size();

                int completedCount = controller.getCompletedServiceCount(
                                userEmail);

                String totalSpend = controller.getTotalServiceSpendDisplay(
                                userEmail);

                // ========================================================
                // VEHICLES
                // ========================================================

                grid.add(
                                createStatusCard(
                                                "My Vehicles",
                                                String.valueOf(
                                                                vehicleCount),
                                                vehicleCount == 1
                                                                ? "Registered vehicle"
                                                                : "Registered vehicles",
                                                NAV_BLUE),
                                0,
                                0);

                // ========================================================
                // ACTIVE
                // ========================================================

                String latestStatus = activeRequests.isEmpty()
                                ? "None"
                                : controller.getRequestStatus(
                                                activeRequests.get(0));

                grid.add(
                                createStatusCard(
                                                "Active Requests",
                                                activeCount == 0
                                                                ? "None"
                                                                : String.valueOf(
                                                                                activeCount),
                                                activeCount == 0
                                                                ? "No service in progress"
                                                                : "Latest: "
                                                                                + latestStatus,
                                                activeCount == 0
                                                                ? SECONDARY_TEXT
                                                                : getRequestColor(
                                                                                latestStatus)),
                                1,
                                0);

                // ========================================================
                // COMPLETED
                // ========================================================

                grid.add(
                                createStatusCard(
                                                "Completed Services",
                                                String.valueOf(
                                                                completedCount),
                                                "Completed service requests",
                                                GREEN),
                                2,
                                0);

                // ========================================================
                // TOTAL SPEND
                // ========================================================

                grid.add(
                                createStatusCard(
                                                "Total Service Spend",
                                                totalSpend,
                                                "Completed services only",
                                                ORANGE),
                                3,
                                0);

                for (Node node : grid.getChildren()) {

                        if (node instanceof Region) {

                                ((Region) node)
                                                .setMaxWidth(
                                                                Double.MAX_VALUE);
                        }

                        GridPane.setHgrow(
                                        node,
                                        Priority.ALWAYS);
                }

                return grid;
        }

        // ============================================================
        // STATUS CARD
        // ============================================================

        private VBox createStatusCard(
                        String title,
                        String value,
                        String description,
                        String accent) {

                VBox card = new VBox(8);

                card.setPadding(
                                new Insets(20));

                card.setPrefHeight(
                                125);

                card.setMaxWidth(
                                Double.MAX_VALUE);

                card.setStyle(
                                "-fx-background-color: "
                                                + CARD
                                                + ";"
                                                + "-fx-background-radius: 15;"
                                                + "-fx-border-color: "
                                                + BORDER
                                                + ";"
                                                + "-fx-border-radius: 15;");

                Label titleLabel = new Label(
                                title);

                titleLabel.setFont(
                                Font.font(
                                                "Arial",
                                                FontWeight.BOLD,
                                                12));

                titleLabel.setTextFill(
                                Color.web(
                                                SECONDARY_TEXT));

                Label valueLabel = new Label(
                                firstNonBlank(
                                                value,
                                                "-"));

                valueLabel.setFont(
                                Font.font(
                                                "Arial",
                                                FontWeight.BOLD,
                                                21));

                valueLabel.setTextFill(
                                Color.web(
                                                accent));

                Label descriptionLabel = new Label(
                                description);

                descriptionLabel.setWrapText(
                                true);

                descriptionLabel.setFont(
                                Font.font(
                                                "Arial",
                                                11));

                descriptionLabel.setTextFill(
                                Color.web(
                                                SECONDARY_TEXT));

                card.getChildren()
                                .addAll(
                                                titleLabel,
                                                valueLabel,
                                                descriptionLabel);

                return card;
        }

        // ============================================================
        // MAIN GRID
        // ============================================================

        private GridPane createMainGrid() {

                GridPane grid = new GridPane();

                grid.setHgap(
                                20);

                grid.setVgap(
                                20);

                grid.setMaxWidth(
                                Double.MAX_VALUE);

                for (int i = 0; i < 2; i++) {

                        ColumnConstraints column = new ColumnConstraints();

                        column.setPercentWidth(
                                        50);

                        column.setHgrow(
                                        Priority.ALWAYS);

                        grid.getColumnConstraints()
                                        .add(
                                                        column);
                }

                VBox activeRequestCard = createActiveRequestCard();

                VBox vehicleCard = createVehicleCard();

                VBox recentServiceCard = createRecentServiceCard();

                VBox mechanicsCard = createMechanicsCard();

                grid.add(
                                activeRequestCard,
                                0,
                                0);

                grid.add(
                                vehicleCard,
                                1,
                                0);

                grid.add(
                                recentServiceCard,
                                0,
                                1);

                grid.add(
                                mechanicsCard,
                                1,
                                1);

                for (Node node : grid.getChildren()) {

                        if (node instanceof Region) {

                                ((Region) node)
                                                .setMaxWidth(
                                                                Double.MAX_VALUE);
                        }

                        GridPane.setHgrow(
                                        node,
                                        Priority.ALWAYS);
                }

                return grid;
        }

        // ============================================================
        // ACTIVE REQUEST CARD
        // ============================================================

        private VBox createActiveRequestCard() {

                VBox card = createCard();

                HBox titleRow = new HBox(10);

                titleRow.setAlignment(
                                Pos.CENTER_LEFT);

                Label title = createCardTitle(
                                "Active Service Request");

                Region spacer = new Region();

                HBox.setHgrow(
                                spacer,
                                Priority.ALWAYS);

                if (!activeRequests.isEmpty()) {

                        Label count = createSmallBadge(
                                        activeRequests.size()
                                                        + " Active",
                                        NAV_BLUE);

                        titleRow.getChildren()
                                        .addAll(
                                                        title,
                                                        spacer,
                                                        count);

                } else {

                        titleRow.getChildren()
                                        .addAll(
                                                        title,
                                                        spacer);
                }

                card.getChildren()
                                .add(
                                                titleRow);

                // ========================================================
                // EMPTY
                // ========================================================

                if (activeRequests.isEmpty()) {

                        card.getChildren()
                                        .add(
                                                        createSecondaryLabel(
                                                                        "No active service requests."));

                        Label info = createSecondaryLabel(
                                        "Choose a mechanic to create a new service request.");

                        info.setFont(
                                        Font.font(
                                                        "Arial",
                                                        11));

                        card.getChildren()
                                        .add(
                                                        info);

                        Button requestButton = createBlueButton(
                                        "Request Mechanic");

                        requestButton.setOnAction(
                                        event -> openMechanicsPage());

                        card.getChildren()
                                        .add(
                                                        requestButton);

                        return card;
                }

                // ========================================================
                // LATEST REQUEST
                // ========================================================

                Map<String, Object> request = activeRequests.get(0);

                String service = controller.getRequestService(
                                request);

                String status = controller.getRequestStatus(
                                request);

                String mechanic = controller.getRequestMechanic(
                                request);

                String location = controller.getRequestLocation(
                                request);

                String vehicle = controller.getRequestVehicle(
                                request);

                String requestId = controller.getRequestId(
                                request);

                // ========================================================
                // SERVICE TITLE
                // ========================================================

                Label serviceLabel = new Label(
                                service);

                serviceLabel.setFont(
                                Font.font(
                                                "Arial",
                                                FontWeight.BOLD,
                                                18));

                serviceLabel.setTextFill(
                                Color.web(
                                                HEADING));

                Label requestIdLabel = new Label(
                                "Request ID: "
                                                + requestId);

                requestIdLabel.setFont(
                                Font.font(
                                                "Arial",
                                                10));

                requestIdLabel.setTextFill(
                                Color.web(
                                                SECONDARY_TEXT));

                card.getChildren()
                                .addAll(
                                                serviceLabel,
                                                requestIdLabel);

                // ========================================================
                // DETAILS
                // ========================================================

                HBox details = new HBox(10);

                VBox vehicleBox = createInfoBox(
                                "VEHICLE",
                                vehicle);

                VBox statusBox = createInfoBox(
                                "STATUS",
                                status);

                HBox.setHgrow(
                                vehicleBox,
                                Priority.ALWAYS);

                HBox.setHgrow(
                                statusBox,
                                Priority.ALWAYS);

                details.getChildren()
                                .addAll(
                                                vehicleBox,
                                                statusBox);

                card.getChildren()
                                .add(
                                                details);

                // ========================================================
                // MECHANIC
                // ========================================================

                Label mechanicLabel = new Label(
                                "Mechanic: "
                                                + mechanic);

                mechanicLabel.setWrapText(
                                true);

                mechanicLabel.setFont(
                                Font.font(
                                                "Arial",
                                                FontWeight.BOLD,
                                                13));

                mechanicLabel.setTextFill(
                                Color.web(
                                                status.equalsIgnoreCase(
                                                                "Pending")
                                                                                ? SECONDARY_TEXT
                                                                                : NAV_BLUE));

                // ========================================================
                // LOCATION
                // ========================================================

                Label locationLabel = new Label(
                                "Location: "
                                                + location);

                locationLabel.setWrapText(
                                true);

                locationLabel.setFont(
                                Font.font(
                                                "Arial",
                                                11));

                locationLabel.setTextFill(
                                Color.web(
                                                SECONDARY_TEXT));

                // ========================================================
                // STATUS MESSAGE
                // ========================================================

                Label statusMessage = new Label(
                                controller.getRequestStatusMessage(
                                                request));

                statusMessage.setWrapText(
                                true);

                statusMessage.setPadding(
                                new Insets(10));

                statusMessage.setFont(
                                Font.font(
                                                "Arial",
                                                FontWeight.BOLD,
                                                11));

                String statusColor = getRequestColor(
                                status);

                statusMessage.setTextFill(
                                Color.web(
                                                statusColor));

                statusMessage.setStyle(
                                "-fx-background-color: "
                                                + statusColor
                                                + "12;"
                                                + "-fx-background-radius: 8;");

                card.getChildren()
                                .addAll(
                                                mechanicLabel,
                                                locationLabel,
                                                statusMessage);

                // ========================================================
                // BUTTONS
                // ========================================================

                HBox buttons = new HBox(10);

                Button refresh = createOutlineButton(
                                "Refresh Status");

                refresh.setOnAction(
                                event -> refreshDashboard());

                Button mapButton = createBlueButton(
                                "View Live Map");

                boolean canViewMap = controller.canViewLiveMap(
                                request);

                mapButton.setDisable(
                                !canViewMap);

                if (canViewMap) {

                        mapButton.setOnAction(
                                        event -> openLiveMap());

                } else {

                        mapButton.setText(
                                        "Waiting for Mechanic");
                }

                HBox.setHgrow(
                                refresh,
                                Priority.ALWAYS);

                HBox.setHgrow(
                                mapButton,
                                Priority.ALWAYS);

                refresh.setMaxWidth(
                                Double.MAX_VALUE);

                mapButton.setMaxWidth(
                                Double.MAX_VALUE);

                buttons.getChildren()
                                .addAll(
                                                refresh,
                                                mapButton);

                card.getChildren()
                                .add(
                                                buttons);

                return card;
        }

        // ============================================================
        // VEHICLE CARD
        // ============================================================

        private VBox createVehicleCard() {

                VBox card = createCard();

                HBox titleRow = new HBox(10);

                titleRow.setAlignment(
                                Pos.CENTER_LEFT);

                Label title = createCardTitle(
                                "My Vehicles");

                Region spacer = new Region();

                HBox.setHgrow(
                                spacer,
                                Priority.ALWAYS);

                Label count = createSmallBadge(
                                String.valueOf(
                                                vehicles.size()),
                                NAV_BLUE);

                titleRow.getChildren()
                                .addAll(
                                                title,
                                                spacer,
                                                count);

                card.getChildren()
                                .add(
                                                titleRow);

                if (vehicles.isEmpty()) {

                        card.getChildren()
                                        .add(
                                                        createSecondaryLabel(
                                                                        "No vehicles added yet."));

                } else {

                        int limit = Math.min(
                                        vehicles.size(),
                                        3);

                        for (int i = 0; i < limit; i++) {

                                card.getChildren()
                                                .add(
                                                                createVehicleRow(
                                                                                vehicles.get(i)));
                        }
                }

                Button vehicleButton = createBlueButton(
                                vehicles.isEmpty()
                                                ? "Add Vehicle"
                                                : "Manage Vehicles");

                vehicleButton.setOnAction(
                                event -> openVehicles());

                card.getChildren()
                                .add(
                                                vehicleButton);

                return card;
        }

        // ============================================================
        // VEHICLE ROW
        // ============================================================

        private VBox createVehicleRow(
                        Map<String, Object> vehicle) {

                VBox wrapper = new VBox(8);

                wrapper.setPadding(
                                new Insets(
                                                10,
                                                0,
                                                10,
                                                0));

                HBox row = new HBox(12);

                row.setAlignment(
                                Pos.CENTER_LEFT);

                StackPane iconBox = new StackPane();

                Circle circle = new Circle(
                                20);

                circle.setFill(
                                Color.web(
                                                "#E8EFFF"));

        // UI-only: use the shared vector car icon for vehicle representations.
        SVGPath carIcon =
                IconUtil.createVehicleIcon(
                        0.85,
                        NAV_BLUE
                );

        iconBox.getChildren().addAll(
                circle,
                carIcon
        );

                String vehicleName = controller.getVehicleDisplay(
                                vehicle);

                Label name = new Label(
                                vehicleName);

                name.setWrapText(
                                true);

                name.setFont(
                                Font.font(
                                                "Arial",
                                                FontWeight.BOLD,
                                                13));

                name.setTextFill(
                                Color.web(
                                                HEADING));

                VBox details = new VBox(
                                2,
                                name);

                Region spacer = new Region();

                HBox.setHgrow(
                                spacer,
                                Priority.ALWAYS);

                Label health = createSmallBadge(
                                controller.getVehicleHealthDisplay(
                                                vehicle),
                                getHealthColor(
                                                controller.getVehicleHealthDisplay(
                                                                vehicle)));

                row.getChildren()
                                .addAll(
                                                iconBox,
                                                details,
                                                spacer,
                                                health);

                // ========================================================
                // VEHICLE SECONDARY INFO
                // ========================================================

                HBox secondary = new HBox(10);

                secondary.setPadding(
                                new Insets(
                                                0,
                                                0,
                                                0,
                                                52));

                Label service = createMiniText(
                                "Next Service: "
                                                + controller.getNextServiceDisplay(
                                                                vehicle));

                Label insurance = createMiniText(
                                "Insurance: "
                                                + controller.getInsuranceDisplay(
                                                                vehicle));

                secondary.getChildren()
                                .addAll(
                                                service,
                                                insurance);

                wrapper.getChildren()
                                .addAll(
                                                row,
                                                secondary);

                return wrapper;
        }

        // ============================================================
        // RECENT SERVICE CARD
        // ============================================================

        private VBox createRecentServiceCard() {

                VBox card = createCard();

                HBox titleRow = new HBox(10);

                titleRow.setAlignment(
                                Pos.CENTER_LEFT);

                Label title = createCardTitle(
                                "Recent Service History");

                Region spacer = new Region();

                HBox.setHgrow(
                                spacer,
                                Priority.ALWAYS);

                Label count = createSmallBadge(
                                String.valueOf(
                                                serviceHistory.size()),
                                GREEN);

                titleRow.getChildren()
                                .addAll(
                                                title,
                                                spacer,
                                                count);

                card.getChildren()
                                .add(
                                                titleRow);

                if (serviceHistory.isEmpty()) {

                        card.getChildren()
                                        .add(
                                                        createSecondaryLabel(
                                                                        "No completed or cancelled services yet."));

                } else {

                        int limit = Math.min(
                                        serviceHistory.size(),
                                        4);

                        for (int i = 0; i < limit; i++) {

                                card.getChildren()
                                                .add(
                                                                createServiceRow(
                                                                                serviceHistory.get(i)));
                        }
                }

                Button historyButton = createBlueButton(
                                "View Full History");

                historyButton.setOnAction(
                                event -> openServiceHistory());

                card.getChildren()
                                .add(
                                                historyButton);

                return card;
        }

        // ============================================================
        // SERVICE HISTORY ROW
        // ============================================================

        private HBox createServiceRow(
                        Map<String, Object> service) {

                HBox row = new HBox(10);

                row.setAlignment(
                                Pos.CENTER_LEFT);

                Label icon = new Label(
                                "◷");

                icon.setFont(
                                Font.font(
                                                "Arial",
                                                20));

                icon.setTextFill(
                                Color.web(
                                                NAV_BLUE));

                String title = controller.getHistoryTitle(
                                service);

                String date = controller.getHistoryDate(
                                service);

                String status = controller.getHistoryStatus(
                                service);

                String amount = controller.getHistoryAmountDisplay(
                                service);

                String mechanic = controller.getHistoryMechanic(
                                service);

                Label titleLabel = new Label(
                                title);

                titleLabel.setFont(
                                Font.font(
                                                "Arial",
                                                FontWeight.BOLD,
                                                13));

                titleLabel.setTextFill(
                                Color.web(
                                                HEADING));

                Label detailsLabel = new Label(
                                firstNonBlank(
                                                date,
                                                "Date unavailable")
                                                + " • "
                                                + firstNonBlank(
                                                                mechanic,
                                                                "Mechanic"));

                detailsLabel.setWrapText(
                                true);

                detailsLabel.setFont(
                                Font.font(
                                                "Arial",
                                                10));

                detailsLabel.setTextFill(
                                Color.web(
                                                SECONDARY_TEXT));

                VBox details = new VBox(
                                2,
                                titleLabel,
                                detailsLabel);

                Region spacer = new Region();

                HBox.setHgrow(
                                spacer,
                                Priority.ALWAYS);

                VBox right = new VBox(3);

                right.setAlignment(
                                Pos.CENTER_RIGHT);

                Label statusLabel = createSmallBadge(
                                status,
                                getHistoryStatusColor(
                                                status));

                Label amountLabel = new Label(
                                amount);

                amountLabel.setFont(
                                Font.font(
                                                "Arial",
                                                FontWeight.BOLD,
                                                12));

                amountLabel.setTextFill(
                                Color.web(
                                                status.equalsIgnoreCase(
                                                                "Cancelled")
                                                                                ? SECONDARY_TEXT
                                                                                : HEADING));

                right.getChildren()
                                .addAll(
                                                statusLabel,
                                                amountLabel);

                row.getChildren()
                                .addAll(
                                                icon,
                                                details,
                                                spacer,
                                                right);

                return row;
        }

        // ============================================================
        // MECHANICS CARD
        // ============================================================

        private VBox createMechanicsCard() {

                VBox card = createCard();

                HBox titleRow = new HBox(10);

                titleRow.setAlignment(
                                Pos.CENTER_LEFT);

                Label title = createCardTitle(
                                "Available Mechanics");

                Region spacer = new Region();

                HBox.setHgrow(
                                spacer,
                                Priority.ALWAYS);

                Label count = createSmallBadge(
                                String.valueOf(
                                                mechanics.size()),
                                GREEN);

                titleRow.getChildren()
                                .addAll(
                                                title,
                                                spacer,
                                                count);

                card.getChildren()
                                .add(
                                                titleRow);

                if (mechanics.isEmpty()) {

                        card.getChildren()
                                        .add(
                                                        createSecondaryLabel(
                                                                        "No active mechanics are currently available."));

                } else {

                        int limit = Math.min(
                                        mechanics.size(),
                                        3);

                        for (int i = 0; i < limit; i++) {

                                card.getChildren()
                                                .add(
                                                                createMechanicRow(
                                                                                mechanics.get(i)));
                        }
                }

                Button mechanicsButton = createBlueButton(
                                "View All Mechanics");

                mechanicsButton.setOnAction(
                                event -> openMechanicsPage());

                card.getChildren()
                                .add(
                                                mechanicsButton);

                return card;
        }

        // ============================================================
        // MECHANIC ROW
        // ============================================================

        private HBox createMechanicRow(
                        Map<String, Object> mechanic) {

                HBox row = new HBox(12);

                row.setAlignment(
                                Pos.CENTER_LEFT);

                String name = controller.getMechanicName(
                                mechanic);

                String specialization = controller
                                .getMechanicSpecialization(
                                                mechanic);

                StackPane avatarBox = new StackPane();

                avatarBox.setPrefSize(
                                40,
                                40);

                avatarBox.setMinSize(
                                40,
                                40);

                avatarBox.setMaxSize(
                                40,
                                40);

                Circle avatar = new Circle(
                                19);

                avatar.setFill(
                                Color.web(
                                                "#E8EFFF"));

                Label initial = new Label(
                                getInitial(
                                                name));

                initial.setFont(
                                Font.font(
                                                "Arial",
                                                FontWeight.BOLD,
                                                13));

                initial.setTextFill(
                                Color.web(
                                                NAV_BLUE));

                avatarBox.getChildren()
                                .addAll(
                                                avatar,
                                                initial);

                VBox details = new VBox(2);

                Label nameLabel = new Label(
                                name);

                nameLabel.setFont(
                                Font.font(
                                                "Arial",
                                                FontWeight.BOLD,
                                                13));

                nameLabel.setTextFill(
                                Color.web(
                                                HEADING));

                Label specializationLabel = new Label(
                                specialization);

                specializationLabel.setWrapText(
                                true);

                specializationLabel.setFont(
                                Font.font(
                                                "Arial",
                                                10));

                specializationLabel.setTextFill(
                                Color.web(
                                                SECONDARY_TEXT));

                details.getChildren()
                                .addAll(
                                                nameLabel,
                                                specializationLabel);

                Region spacer = new Region();

                HBox.setHgrow(
                                spacer,
                                Priority.ALWAYS);

                Label active = createSmallBadge(
                                "Active",
                                GREEN);

                row.getChildren()
                                .addAll(
                                                avatarBox,
                                                details,
                                                spacer,
                                                active);

                return row;
        }

        // ============================================================
        // QUICK ACTIONS
        // ============================================================

        private VBox createQuickActions() {

                VBox section = new VBox(12);

                section.setFillWidth(
                                true);

                Label title = createCardTitle(
                                "Quick Actions");

                HBox actions = new HBox(12);

                Button mechanic = createActionButton(
                                "⚒",
                                "Request Mechanic");

                mechanic.setOnAction(
                                event -> openMechanicsPage());

                Button tow = createActionButton(
                                "▱",
                                "Tow Truck");

                tow.setOnAction(
                                event -> openTowTruck());

                Button sos = createActionButton(
                                "SOS",
                                "Emergency SOS");

                sos.setOnAction(
                                event -> openSOS());

                Button documents = createActionButton(
                                "▧",
                                "Documents");

                documents.setOnAction(
                                event -> openDocuments());

                Button vehicle = createActionButton(
                                "▱",
                                "My Vehicles");

                vehicle.setOnAction(
                                event -> openVehicles());

                HBox.setHgrow(
                                mechanic,
                                Priority.ALWAYS);

                HBox.setHgrow(
                                tow,
                                Priority.ALWAYS);

                HBox.setHgrow(
                                sos,
                                Priority.ALWAYS);

                HBox.setHgrow(
                                documents,
                                Priority.ALWAYS);

                HBox.setHgrow(
                                vehicle,
                                Priority.ALWAYS);

                mechanic.setMaxWidth(
                                Double.MAX_VALUE);

                tow.setMaxWidth(
                                Double.MAX_VALUE);

                sos.setMaxWidth(
                                Double.MAX_VALUE);

                documents.setMaxWidth(
                                Double.MAX_VALUE);

                vehicle.setMaxWidth(
                                Double.MAX_VALUE);

                actions.getChildren()
                                .addAll(
                                                mechanic,
                                                tow,
                                                sos,
                                                documents,
                                                vehicle);

                section.getChildren()
                                .addAll(
                                                title,
                                                actions);

                return section;
        }

        // ============================================================
        // LOGOUT SECTION
        // ============================================================

        private HBox createLogoutSection() {

                HBox section = new HBox();

                section.setAlignment(
                                Pos.CENTER_RIGHT);

                section.setPadding(
                                new Insets(
                                                8,
                                                0,
                                                5,
                                                0));

                Button logoutButton = new Button(
                                "Logout");

                logoutButton.setPrefWidth(
                                120);

                logoutButton.setPrefHeight(
                                42);

                logoutButton.setFont(
                                Font.font(
                                                "Arial",
                                                FontWeight.BOLD,
                                                14));

                logoutButton.setTextFill(
                                Color.WHITE);

                logoutButton.setCursor(
                                javafx.scene.Cursor.HAND);

                logoutButton.setStyle(
                                "-fx-background-color: "
                                                + RED
                                                + ";"
                                                + "-fx-background-radius: 21;");

                logoutButton.setOnMouseEntered(
                                event -> logoutButton.setStyle(
                                                "-fx-background-color: #B91C1C;"
                                                                + "-fx-background-radius: 21;"));

                logoutButton.setOnMouseExited(
                                event -> logoutButton.setStyle(
                                                "-fx-background-color: "
                                                                + RED
                                                                + ";"
                                                                + "-fx-background-radius: 21;"));

                logoutButton.setOnAction(
                                event -> logout());

                section.getChildren()
                                .add(
                                                logoutButton);

                return section;
        }

        // ============================================================
        // LOGOUT
        // ============================================================

        private void logout() {

                try {

                        /*
                         * IMPORTANT:
                         *
                         * Capture Stage BEFORE changing Scene/root.
                         */
                        Stage stage = dashboardStage;

                        if (stage == null
                                        &&
                                        dashboardScene != null
                                        &&
                                        dashboardScene.getWindow() instanceof Stage) {

                                stage = (Stage) dashboardScene
                                                .getWindow();
                        }

                        if (stage == null) {

                                stage = LoginPage.getMainStage();
                        }

                        if (stage == null) {

                                System.err.println(
                                                "Logout error: Stage is null.");

                                return;
                        }

                        UserSession.clear();

                        LoginPage loginPage = new LoginPage();

                        Scene loginScene = new Scene(
                                        loginPage.getView(),
                                        1200,
                                        700);

                        stage.setTitle(
                                        "RoadGuardian - Login");

                        stage.setScene(
                                        loginScene);

                        stage.show();

                        stage.toFront();

                        dashboardStage = null;

                } catch (Exception e) {

                        System.err.println(
                                        "Customer logout error: "
                                                        + e.getMessage());

                        e.printStackTrace();
                }
        }

        // ============================================================
        // CREATE CARD
        // ============================================================

        private VBox createCard() {

                VBox card = new VBox(15);

                card.setPadding(
                                new Insets(22));

                card.setMinHeight(
                                250);

                card.setMaxWidth(
                                Double.MAX_VALUE);

                card.setStyle(
                                "-fx-background-color: "
                                                + CARD
                                                + ";"
                                                + "-fx-background-radius: 16;"
                                                + "-fx-border-color: "
                                                + BORDER
                                                + ";"
                                                + "-fx-border-radius: 16;");

                return card;
        }

        // ============================================================
        // CARD TITLE
        // ============================================================

        private Label createCardTitle(
                        String text) {

                Label title = new Label(
                                text);

                title.setFont(
                                Font.font(
                                                "Arial",
                                                FontWeight.BOLD,
                                                18));

                title.setTextFill(
                                Color.web(
                                                HEADING));

                return title;
        }

        // ============================================================
        // SECONDARY LABEL
        // ============================================================

        private Label createSecondaryLabel(
                        String text) {

                Label label = new Label(
                                text);

                label.setFont(
                                Font.font(
                                                "Arial",
                                                13));

                label.setTextFill(
                                Color.web(
                                                SECONDARY_TEXT));

                label.setWrapText(
                                true);

                return label;
        }

        // ============================================================
        // INFO BOX
        // ============================================================

        private VBox createInfoBox(
                        String title,
                        String value) {

                VBox box = new VBox(4);

                box.setPadding(
                                new Insets(10));

                box.setMaxWidth(
                                Double.MAX_VALUE);

                box.setStyle(
                                "-fx-background-color: "
                                                + SECONDARY_SURFACE
                                                + ";"
                                                + "-fx-background-radius: 8;");

                Label titleLabel = new Label(
                                title);

                titleLabel.setFont(
                                Font.font(
                                                "Arial",
                                                FontWeight.BOLD,
                                                8));

                titleLabel.setTextFill(
                                Color.web(
                                                SECONDARY_TEXT));

                Label valueLabel = new Label(
                                firstNonBlank(
                                                value,
                                                "-"));

                valueLabel.setWrapText(
                                true);

                valueLabel.setFont(
                                Font.font(
                                                "Arial",
                                                FontWeight.BOLD,
                                                10));

                valueLabel.setTextFill(
                                Color.web(
                                                HEADING));

                box.getChildren()
                                .addAll(
                                                titleLabel,
                                                valueLabel);

                return box;
        }

        // ============================================================
        // BLUE BUTTON
        // ============================================================

        private Button createBlueButton(
                        String text) {

                Button button = new Button(
                                text);

                button.setPrefHeight(
                                38);

                button.setPadding(
                                new Insets(
                                                0,
                                                15,
                                                0,
                                                15));

                button.setFont(
                                Font.font(
                                                "Arial",
                                                FontWeight.BOLD,
                                                12));

                button.setTextFill(
                                Color.WHITE);

                button.setCursor(
                                javafx.scene.Cursor.HAND);

                button.setStyle(
                                "-fx-background-color: "
                                                + NAV_BLUE
                                                + ";"
                                                + "-fx-background-radius: 19;");

                return button;
        }

        // ============================================================
        // OUTLINE BUTTON
        // ============================================================

        private Button createOutlineButton(
                        String text) {

                Button button = new Button(
                                text);

                button.setPrefHeight(
                                38);

                button.setPadding(
                                new Insets(
                                                0,
                                                15,
                                                0,
                                                15));

                button.setFont(
                                Font.font(
                                                "Arial",
                                                FontWeight.BOLD,
                                                11));

                button.setTextFill(
                                Color.web(
                                                NAV_BLUE));

                button.setCursor(
                                javafx.scene.Cursor.HAND);

                button.setStyle(
                                "-fx-background-color: "
                                                + CARD
                                                + ";"
                                                + "-fx-border-color: "
                                                + NAV_BLUE
                                                + ";"
                                                + "-fx-border-radius: 19;"
                                                + "-fx-background-radius: 19;");

                return button;
        }

        // ============================================================
        // ACTION BUTTON
        // ============================================================

        private Button createActionButton(
                        String icon,
                        String text) {

                Button button = new Button(
                                icon
                                                + "  "
                                                + text);

                button.setPrefHeight(
                                46);

                button.setPadding(
                                new Insets(
                                                0,
                                                18,
                                                0,
                                                18));

                button.setFont(
                                Font.font(
                                                "Arial",
                                                FontWeight.BOLD,
                                                12));

                button.setTextFill(
                                Color.web(
                                                HEADING));

                button.setCursor(
                                javafx.scene.Cursor.HAND);

                button.setStyle(
                                "-fx-background-color: "
                                                + CARD
                                                + ";"
                                                + "-fx-border-color: "
                                                + BORDER
                                                + ";"
                                                + "-fx-border-radius: 23;"
                                                + "-fx-background-radius: 23;");

                button.setOnMouseEntered(
                                event -> button.setStyle(
                                                "-fx-background-color: "
                                                                + SECONDARY_SURFACE
                                                                + ";"
                                                                + "-fx-border-color: "
                                                                + NAV_BLUE
                                                                + ";"
                                                                + "-fx-border-radius: 23;"
                                                                + "-fx-background-radius: 23;"));

                button.setOnMouseExited(
                                event -> button.setStyle(
                                                "-fx-background-color: "
                                                                + CARD
                                                                + ";"
                                                                + "-fx-border-color: "
                                                                + BORDER
                                                                + ";"
                                                                + "-fx-border-radius: 23;"
                                                                + "-fx-background-radius: 23;"));

                return button;
        }

        // ============================================================
        // SMALL BADGE
        // ============================================================

        private Label createSmallBadge(
                        String text,
                        String color) {

                Label label = new Label(
                                firstNonBlank(
                                                text,
                                                "-"));

                label.setPadding(
                                new Insets(
                                                5,
                                                9,
                                                5,
                                                9));

                label.setFont(
                                Font.font(
                                                "Arial",
                                                FontWeight.BOLD,
                                                9));

                label.setTextFill(
                                Color.web(
                                                color));

                label.setStyle(
                                "-fx-background-color: "
                                                + color
                                                + "15;"
                                                + "-fx-background-radius: 12;");

                return label;
        }

        // ============================================================
        // MINI TEXT
        // ============================================================

        private Label createMiniText(
                        String text) {

                Label label = new Label(
                                text);

                label.setWrapText(
                                true);

                label.setFont(
                                Font.font(
                                                "Arial",
                                                9));

                label.setTextFill(
                                Color.web(
                                                SECONDARY_TEXT));

                return label;
        }

        // ============================================================
        // HEALTH COLOR
        // ============================================================

        private String getHealthColor(
                        String value) {

                String text = clean(
                                value);

                if (text == null
                                ||
                                text.equalsIgnoreCase(
                                                "No Data")) {

                        return SECONDARY_TEXT;
                }

                String lower = text.toLowerCase();

                if (lower.contains(
                                "good")
                                ||
                                lower.contains(
                                                "healthy")
                                ||
                                lower.contains(
                                                "excellent")) {

                        return GREEN;
                }

                if (lower.contains(
                                "poor")
                                ||
                                lower.contains(
                                                "critical")
                                ||
                                lower.contains(
                                                "bad")) {

                        return RED;
                }

                return ORANGE;
        }

        // ============================================================
        // REQUEST COLOR
        // ============================================================

        private String getRequestColor(
                        String value) {

                String status = clean(
                                value);

                if (status == null) {

                        return SECONDARY_TEXT;
                }

                if (status.equalsIgnoreCase(
                                "Pending")) {

                        return ORANGE;
                }

                if (status.equalsIgnoreCase(
                                "Assigned")) {

                        return NAV_BLUE;
                }

                if (status.equalsIgnoreCase(
                                "Accepted")) {

                        return PURPLE;
                }

                if (status.equalsIgnoreCase(
                                "In Progress")) {

                        return CYAN;
                }

                if (status.equalsIgnoreCase(
                                "Completed")) {

                        return GREEN;
                }

                if (status.equalsIgnoreCase(
                                "Cancelled")) {

                        return RED;
                }

                return SECONDARY_TEXT;
        }

        // ============================================================
        // HISTORY STATUS COLOR
        // ============================================================

        private String getHistoryStatusColor(
                        String status) {

                if (status != null
                                &&
                                status.equalsIgnoreCase(
                                                "Completed")) {

                        return GREEN;
                }

                if (status != null
                                &&
                                status.equalsIgnoreCase(
                                                "Cancelled")) {

                        return RED;
                }

                return SECONDARY_TEXT;
        }

        // ============================================================
        // INITIAL
        // ============================================================

        private String getInitial(
                        String name) {

                String cleaned = clean(
                                name);

                if (cleaned == null) {

                        return "M";
                }

                return cleaned
                                .substring(
                                                0,
                                                1)
                                .toUpperCase();
        }

        // ============================================================
        // FIRST NON BLANK
        // ============================================================

        private String firstNonBlank(
                        String... values) {

                if (values == null) {

                        return null;
                }

                for (String value : values) {

                        String cleaned = clean(
                                        value);

                        if (cleaned != null) {

                                return cleaned;
                        }
                }

                return null;
        }

        // ============================================================
        // CLEAN
        // ============================================================

        private String clean(
                        String value) {

                if (value == null) {

                        return null;
                }

                String cleaned = value.trim();

                return cleaned.isEmpty()
                                ? null
                                : cleaned;
        }

        // ============================================================
        // NAVIGATION
        // ============================================================

        private void openVehicles() {

                navigate(
                                "My Vehicles");
        }

        private void openServiceHistory() {

                navigate(
                                "Service History");
        }

        private void openDocuments() {

                navigate(
                                "Documents");
        }

        private void openMechanicsPage() {

                navigate(
                                "Mechanics");
        }

        private void openTowTruck() {

                navigate(
                                "Tow Truck");
        }

        private void openSOS() {

                navigate(
                                "Emergency SOS");
        }

        private void openLiveMap() {

                if (activeRequests.isEmpty()) {

                        System.err.println(
                                        "Live Map blocked: No active service request.");

                        return;
                }

                Map<String, Object> request = activeRequests.get(0);

                if (!controller.canViewLiveMap(
                                request)) {

                        System.err.println(
                                        "Live Map blocked: Mechanic is not assigned yet.");

                        return;
                }

                navigate(
                                "Live Map");
        }

        // ============================================================
        // CENTRAL NAVIGATION
        // ============================================================

        private void navigate(
                        String pageName) {

                Stage stage = dashboardStage;

                if (stage == null
                                &&
                                dashboardScene != null
                                &&
                                dashboardScene.getWindow() instanceof Stage) {

                        stage = (Stage) dashboardScene
                                        .getWindow();
                }

                if (stage == null) {

                        System.err.println(
                                        "Navigation error: Dashboard stage is null.");

                        return;
                }

                /*
                 * Keep shared stage reference alive across customer pages.
                 */
                dashboardStage = stage;

                try {

                        AppNavigator.navigate(
                                        stage,
                                        pageName);

                } catch (IOException e) {

                        System.err.println(
                                        "Navigation error while opening "
                                                        + pageName
                                                        + ": "
                                                        + e.getMessage());

                        e.printStackTrace();
                }
        }
}
