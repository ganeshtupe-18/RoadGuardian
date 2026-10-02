package project.ui.mechanic;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.Scene;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import javafx.stage.Stage;

import project.controller.mechanic.ActiveJobController;
import project.controller.mechanic.JobHistoryController;
import project.controller.mechanic.MechanicSOSController;
import project.controller.mechanic.RequestsController;

import project.dao.mechanic.MechanicDAO;

import project.model.Mechanic;
import project.model.SOSRequest;
import project.model.ServiceRequest;

import project.ui.landing.LoginPage;
import project.ui.user.UserSession;

import project.util.Theme;
import project.util.VehicleIconUtil;

import java.util.ArrayList;
import java.util.List;

public class MechanicDashboard {

    // =====================================================
    // ROOT
    // =====================================================

    private BorderPane root;

    // =====================================================
    // PAGES
    // =====================================================

    private RequestsPage requestsPage;

    private ActiveJobPage activeJobPage;

    private JobHistoryPage jobHistoryPage;

    private NavigationPage navigationPage;

    private SOSRequestsPage sosRequestsPage;

    // =====================================================
    // CONTROLLERS
    // =====================================================

    private RequestsController requestsController;

    private ActiveJobController activeJobController;

    private JobHistoryController jobHistoryController;

    private MechanicSOSController sosController;

    private MechanicDAO mechanicDAO;

    // =====================================================
    // SIDEBAR BUTTONS
    // =====================================================

    private Button dashboardButton;

    private Button requestsButton;

    private Button sosButton;

    private Button navigationButton;

    private Button activeJobButton;

    private Button historyButton;

    // =====================================================
    // PROFILE
    // =====================================================

    private Label mechanicNameLabel;

    private Label mechanicStatusLabel;

    // =====================================================
    // SCENE
    // =====================================================

    public Scene getDashboardScene() {

        // =================================================
        // ROOT
        // =================================================

        root =
                new BorderPane();

        project.ui.theme.DarkTheme.apply(root);

        root.setStyle(
                "-fx-background-color: "
                        + Theme.BACKGROUND
                        + ";"
        );

        // =================================================
        // CONTROLLERS
        // =================================================

        requestsController =
                new RequestsController();

        activeJobController =
                new ActiveJobController();

        jobHistoryController =
                new JobHistoryController();

        sosController =
                new MechanicSOSController();

        mechanicDAO =
                new MechanicDAO();

        // =================================================
        // PAGES
        // =================================================

        activeJobPage =
                new ActiveJobPage();

        requestsPage =
                new RequestsPage(
                        activeJobPage,
                        this::showActiveJob
                );

        jobHistoryPage =
                new JobHistoryPage();

        navigationPage =
                new NavigationPage();

        sosRequestsPage =
                new SOSRequestsPage();

        // =================================================
        // SIDEBAR
        // =================================================

        root.setLeft(
                createSidebar()
        );

        // =================================================
        // DEFAULT
        // =================================================

        showDashboard();

        return new Scene(
                root,
                1280,
                760
        );
    }

    // =====================================================
    // SIDEBAR
    // =====================================================

    private VBox createSidebar() {

        VBox sidebar =
                new VBox(10);

        sidebar.setPrefWidth(
                245
        );

        sidebar.setMinWidth(
                245
        );

        sidebar.setMaxWidth(
                245
        );

        sidebar.setPadding(
                new Insets(
                        28,
                        18,
                        24,
                        18
                )
        );

        sidebar.setStyle(
                "-fx-background-color: "
                        + Theme.SIDEBAR
                        + ";"
        );

        // =================================================
        // LOGO
        // =================================================

        HBox logoBox =
                new HBox(10);

        logoBox.setAlignment(
                Pos.CENTER_LEFT
        );

        logoBox.setPadding(
                new Insets(
                        0,
                        8,
                        22,
                        8
                )
        );

        Circle circle =
                new Circle(19);

        circle.setFill(
                Color.web(
                        Theme.PRIMARY
                )
        );

        Label logo =
                new Label(
                        "RoadGuardian"
                );

        logo.setTextFill(
                Color.web(
                        Theme.TEXT
                )
        );

        logo.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        20
                )
        );

        logoBox
                .getChildren()
                .addAll(
                        circle,
                        logo
                );

        // =================================================
        // ROLE
        // =================================================

        Label role =
                new Label(
                        "MECHANIC PORTAL"
                );

        role.setTextFill(
                Color.web(
                        Theme.SECONDARY_TEXT
                )
        );

        role.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        10
                )
        );

        role.setPadding(
                new Insets(
                        0,
                        8,
                        8,
                        8
                )
        );

        // =================================================
        // BUTTONS
        // =================================================

        dashboardButton =
                createMenuButton(
                        "⌂   Dashboard"
                );

        requestsButton =
                createMenuButton(
                        "▣   Service Requests"
                );

        sosButton =
                createMenuButton(
                        "SOS  Emergency SOS"
                );

        navigationButton =
                createMenuButton(
                        "➤   Navigation"
                );

        activeJobButton =
                createMenuButton(
                        "⚙   Active Job"
                );

        historyButton =
                createMenuButton(
                        "◷   Job History"
                );

        dashboardButton.setOnAction(
                event ->
                        showDashboard()
        );

        requestsButton.setOnAction(
                event ->
                        showRequests()
        );

        sosButton.setOnAction(
                event ->
                        showSOSRequests()
        );

        navigationButton.setOnAction(
                event ->
                        showNavigation()
        );

        activeJobButton.setOnAction(
                event ->
                        showActiveJob()
        );

        historyButton.setOnAction(
                event ->
                        showHistory()
        );

        // =================================================
        // SPACER
        // =================================================

        Region spacer =
                new Region();

        VBox.setVgrow(
                spacer,
                Priority.ALWAYS
        );

        // =================================================
        // PROFILE
        // =================================================

        VBox profileBox =
                createMechanicProfileBox();

        // =================================================
        // LOGOUT
        // =================================================

        Button logoutButton =
                createMenuButton(
                        "↪   Logout"
                );

        logoutButton.setOnAction(
                event ->
                        logout()
        );

        sidebar
                .getChildren()
                .addAll(
                        logoBox,
                        role,

                        dashboardButton,
                        requestsButton,
                        sosButton,
                        navigationButton,
                        activeJobButton,
                        historyButton,

                        spacer,

                        profileBox,
                        logoutButton
                );

        return sidebar;
    }

    // =====================================================
    // MENU BUTTON
    // =====================================================

    private Button createMenuButton(
            String text
    ) {

        Button button =
                new Button(
                        text
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
                        14,
                        0,
                        14
                )
        );

        setNormalStyle(
                button
        );

        return button;
    }

    // =====================================================
    // NORMAL STYLE
    // =====================================================

    private void setNormalStyle(
            Button button
    ) {

        if (button == null) {

            return;
        }

        button.setStyle(
                "-fx-background-color: transparent;"
                        + "-fx-text-fill: "
                        + Theme.TEXT
                        + ";"
                        + "-fx-background-radius: 9;"
                        + "-fx-font-size: 13px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-cursor: hand;"
        );
    }

    // =====================================================
    // ACTIVE STYLE
    // =====================================================

    private void setActiveStyle(
            Button button
    ) {

        if (button == null) {

            return;
        }

        button.setStyle(
                "-fx-background-color: "
                        + Theme.PRIMARY
                        + ";"
                        + "-fx-text-fill: "
                        + Theme.WHITE
                        + ";"
                        + "-fx-background-radius: 9;"
                        + "-fx-font-size: 13px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-cursor: hand;"
        );
    }

    // =====================================================
    // ACTIVE BUTTON
    // =====================================================

    private void setActiveButton(
            Button active
    ) {

        setNormalStyle(
                dashboardButton
        );

        setNormalStyle(
                requestsButton
        );

        setNormalStyle(
                sosButton
        );

        setNormalStyle(
                navigationButton
        );

        setNormalStyle(
                activeJobButton
        );

        setNormalStyle(
                historyButton
        );

        setActiveStyle(
                active
        );
    }

    // =====================================================
    // PROFILE
    // =====================================================

    private VBox createMechanicProfileBox() {

        VBox box =
                new VBox(5);

        box.setPadding(
                new Insets(14)
        );

        box.setStyle(
                "-fx-background-color: "
                        + Theme.SURFACE
                        + ";"
                        + "-fx-border-color: "
                        + Theme.BORDER
                        + ";"
                        + "-fx-border-radius: 10;"
                        + "-fx-background-radius: 10;"
        );

        mechanicNameLabel =
                new Label(
                        getCurrentMechanicName()
                );

        mechanicNameLabel.setTextFill(
                Color.web(
                        Theme.TEXT
                )
        );

        mechanicNameLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        13
                )
        );

        mechanicStatusLabel =
                new Label();

        updateMechanicStatusLabel();

        box.getChildren()
                .addAll(
                        mechanicNameLabel,
                        mechanicStatusLabel
                );

        return box;
    }

    // =====================================================
    // PROFILE STATUS
    // =====================================================

    private void updateMechanicStatusLabel() {

        if (mechanicStatusLabel == null) {

            return;
        }

        boolean active =
                isCurrentMechanicActive();

        mechanicStatusLabel.setText(
                active
                        ? "● Active"
                        : "● Inactive"
        );

        mechanicStatusLabel.setTextFill(
                Color.web(
                        active
                                ? Theme.SUCCESS
                                : Theme.ERROR
                )
        );

        mechanicStatusLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        10
                )
        );
    }

    // =====================================================
    // SHOW DASHBOARD
    // =====================================================

    private void showDashboard() {

        setActiveButton(
                dashboardButton
        );

        updateProfile();

        root.setCenter(
                createDashboard()
        );
    }

    // =====================================================
    // SHOW SERVICE REQUESTS
    // =====================================================

    private void showRequests() {

        setActiveButton(
                requestsButton
        );

        /*
         * getContent() reloads assigned requests from
         * Firebase.
         */

        root.setCenter(
                requestsPage
                        .getContent()
        );
    }

    // =====================================================
    // SHOW SOS
    // =====================================================

    private void showSOSRequests() {

        setActiveButton(
                sosButton
        );

        root.setCenter(
                sosRequestsPage
                        .getContent()
        );
    }

    // =====================================================
    // SHOW NAVIGATION
    // =====================================================

    private void showNavigation() {

        setActiveButton(
                navigationButton
        );

        root.setCenter(
                navigationPage
                        .getContent()
        );
    }

    // =====================================================
    // SHOW ACTIVE JOB
    // =====================================================

    private void showActiveJob() {

        setActiveButton(
                activeJobButton
        );

        activeJobPage.refresh();

        root.setCenter(
                activeJobPage
                        .getContent()
        );
    }

    // =====================================================
    // SHOW HISTORY
    // =====================================================

    private void showHistory() {

        setActiveButton(
                historyButton
        );

        jobHistoryPage.refresh();

        root.setCenter(
                jobHistoryPage
                        .getContent()
        );
    }

    // =====================================================
    // DASHBOARD
    // =====================================================

    private ScrollPane createDashboard() {

        VBox content =
                new VBox(22);

        content.setPadding(
                new Insets(
                        30,
                        35,
                        35,
                        35
                )
        );

        content.setStyle(
                "-fx-background-color: "
                        + Theme.BACKGROUND
                        + ";"
        );

        // =================================================
        // HEADER
        // =================================================

        HBox header =
                createDashboardHeader();

        // =================================================
        // DATA
        // =================================================

        int assignedRequests =
                requestsController
                        .getAssignedRequests()
                        .size();

        int activeJobs =
                activeJobController
                        .getActiveJobCount();

        int completedJobs =
                jobHistoryController
                        .getCompletedJobCount();

        int assignedSOS =
                sosController
                        .getAssignedCount();

        int activeSOS =
                sosController
                        .getActiveCount();

        String earnings =
                jobHistoryController
                        .getTotalEarningsDisplay();

        // =================================================
        // STATISTICS ROW 1
        // =================================================

        HBox statsRowOne =
                new HBox(14);

        VBox requestCard =
                createStatCard(
                        "Assigned Requests",
                        String.valueOf(
                                assignedRequests
                        ),
                        "Waiting for your response",
                        Theme.INFO,
                        this::showRequests
                );

        VBox activeJobCard =
                createStatCard(
                        "Active Jobs",
                        String.valueOf(
                                activeJobs
                        ),
                        "Accepted / In Progress",
                        Theme.PRIMARY,
                        this::showActiveJob
                );

        VBox completedCard =
                createStatCard(
                        "Completed Jobs",
                        String.valueOf(
                                completedJobs
                        ),
                        "Service history",
                        Theme.SUCCESS,
                        this::showHistory
                );

        HBox.setHgrow(
                requestCard,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                activeJobCard,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                completedCard,
                Priority.ALWAYS
        );

        statsRowOne
                .getChildren()
                .addAll(
                        requestCard,
                        activeJobCard,
                        completedCard
                );

        // =================================================
        // STATISTICS ROW 2
        // =================================================

        HBox statsRowTwo =
                new HBox(14);

        VBox assignedSOSCard =
                createStatCard(
                        "Assigned SOS",
                        String.valueOf(
                                assignedSOS
                        ),
                        "Emergency requests waiting",
                        Theme.ERROR,
                        this::showSOSRequests
                );

        VBox activeSOSCard =
                createStatCard(
                        "Active SOS",
                        String.valueOf(
                                activeSOS
                        ),
                        "Assigned / Accepted / In Progress",
                        "#F59E0B",
                        this::showSOSRequests
                );

        VBox earningsCard =
                createStatCard(
                        "Total Earnings",
                        earnings,
                        "From completed service jobs",
                        Theme.SUCCESS,
                        this::showHistory
                );

        HBox.setHgrow(
                assignedSOSCard,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                activeSOSCard,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                earningsCard,
                Priority.ALWAYS
        );

        statsRowTwo
                .getChildren()
                .addAll(
                        assignedSOSCard,
                        activeSOSCard,
                        earningsCard
                );

        // =================================================
        // SERVICE REQUESTS PREVIEW
        // =================================================

        VBox servicePreview =
                createServiceRequestPreview();

        // =================================================
        // SOS PREVIEW
        // =================================================

        VBox sosPreview =
                createSOSPreview();

        content
                .getChildren()
                .addAll(
                        header,
                        statsRowOne,
                        statsRowTwo,
                        servicePreview,
                        sosPreview
                );

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

        return scrollPane;
    }

    // =====================================================
    // DASHBOARD HEADER
    // =====================================================

    private HBox createDashboardHeader() {

        HBox header =
                new HBox();

        header.setAlignment(
                Pos.CENTER_LEFT
        );

        VBox text =
                new VBox(5);

        Label title =
                new Label(
                        "Welcome, "
                                + getCurrentMechanicName()
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
                        30
                )
        );

        Label subtitle =
                new Label(
                        "Manage service requests, emergency SOS responses and active jobs."
                );

        subtitle.setTextFill(
                Color.web(
                        Theme.SECONDARY_TEXT
                )
        );

        subtitle.setFont(
                Font.font(
                        "Arial",
                        14
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
                new Button(
                        "Refresh Dashboard"
                );

        stylePrimaryButton(
                refresh
        );

        refresh.setOnAction(
                event ->
                        showDashboard()
        );

        header
                .getChildren()
                .addAll(
                        text,
                        spacer,
                        refresh
                );

        return header;
    }

    // =====================================================
    // STAT CARD
    // =====================================================

    private VBox createStatCard(
            String title,
            String value,
            String subtitle,
            String color,
            Runnable action
    ) {

        VBox card =
                new VBox(7);

        card.setPadding(
                new Insets(18)
        );

        card.setMinHeight(
                118
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
                        + "-fx-cursor: hand;"
        );

        Label titleLabel =
                new Label(
                        title
                );

        titleLabel.setTextFill(
                Color.web(
                        Theme.HEADING
                )
        );

        titleLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        13
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
                        color
                )
        );

        valueLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        27
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

        card
                .getChildren()
                .addAll(
                        titleLabel,
                        valueLabel,
                        subtitleLabel
                );

        if (action != null) {

            card.setOnMouseClicked(
                    event ->
                            action.run()
            );
        }

        return card;
    }

    // =====================================================
    // SERVICE REQUEST PREVIEW
    // =====================================================

    private VBox createServiceRequestPreview() {

        VBox card =
                createSectionCard();

        HBox header =
                createSectionHeader(
                        "Assigned Service Requests",
                        "View All",
                        this::showRequests
                );

        card.getChildren()
                .add(
                        header
                );

        card.getChildren()
                .add(
                        new Separator()
                );

        List<ServiceRequest> requests;

        try {

            requests =
                    requestsController
                            .getAssignedRequests();

        } catch (Exception e) {

            requests =
                    new ArrayList<>();
        }

        if (requests == null
                ||
                requests.isEmpty()) {

            card.getChildren()
                    .add(
                            createEmptyLabel(
                                    "No assigned service requests."
                            )
                    );

            return card;
        }

        int limit =
                Math.min(
                        3,
                        requests.size()
                );

        for (int i = 0;
             i < limit;
             i++) {

            card.getChildren()
                    .add(
                            createServicePreviewRow(
                                    requests.get(i)
                            )
                    );
        }

        return card;
    }

    // =====================================================
    // SERVICE PREVIEW ROW
    // =====================================================

    private HBox createServicePreviewRow(
            ServiceRequest request
    ) {

        HBox row =
                new HBox(14);

        row.setAlignment(
                Pos.CENTER_LEFT
        );

        row.setPadding(
                new Insets(
                        12,
                        6,
                        12,
                        6
                )
        );

        VBox details =
                new VBox(4);

        Label customer =
                new Label(
                        firstNonBlank(
                                request.getCustomerName(),
                                "Customer"
                        )
                );

        customer.setTextFill(
                Color.web(
                        Theme.TEXT
                )
        );

        customer.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        13
                )
        );

        String service =
                firstNonBlank(
                        request.getServiceType(),
                        request.getDescription(),
                        "Service Request"
                );

        Label description =
                new Label(
                        service
                                + " • "
                                + firstNonBlank(
                                request.getVehicleNumber(),
                                "Vehicle"
                        )
                );

        description.setTextFill(
                Color.web(
                        Theme.SECONDARY_TEXT
                )
        );

        description.setFont(
                Font.font(
                        "Arial",
                        11
                )
        );

        description.setGraphic(
                VehicleIconUtil.createCarIcon(
                        0.50,
                        Theme.PRIMARY
                )
        );
        description.setGraphicTextGap(7);

        details
                .getChildren()
                .addAll(
                        customer,
                        description
                );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Label status =
                createStatusBadge(
                        request.getStatus()
                );

        Button open =
                new Button(
                        "Open"
                );

        styleSmallButton(
                open
        );

        open.setOnAction(
                event ->
                        showRequests()
        );

        row.getChildren()
                .addAll(
                        details,
                        spacer,
                        status,
                        open
                );

        return row;
    }

    // =====================================================
    // SOS PREVIEW
    // =====================================================

    private VBox createSOSPreview() {

        VBox card =
                createSectionCard();

        HBox header =
                createSectionHeader(
                        "Emergency SOS",
                        "View SOS",
                        this::showSOSRequests
                );

        card.getChildren()
                .add(
                        header
                );

        card.getChildren()
                .add(
                        new Separator()
                );

        List<SOSRequest> requests;

        try {

            requests =
                    sosController
                            .getActiveRequests();

        } catch (Exception e) {

            requests =
                    new ArrayList<>();
        }

        if (requests == null
                ||
                requests.isEmpty()) {

            card.getChildren()
                    .add(
                            createEmptyLabel(
                                    "No active SOS emergencies assigned to you."
                            )
                    );

            return card;
        }

        int limit =
                Math.min(
                        3,
                        requests.size()
                );

        for (int i = 0;
             i < limit;
             i++) {

            card.getChildren()
                    .add(
                            createSOSPreviewRow(
                                    requests.get(i)
                            )
                    );
        }

        return card;
    }

    // =====================================================
    // SOS ROW
    // =====================================================

    private HBox createSOSPreviewRow(
            SOSRequest request
    ) {

        HBox row =
                new HBox(14);

        row.setAlignment(
                Pos.CENTER_LEFT
        );

        row.setPadding(
                new Insets(
                        12,
                        6,
                        12,
                        6
                )
        );

        Label emergency =
                new Label(
                        "SOS"
                );

        emergency.setTextFill(
                Color.WHITE
        );

        emergency.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        10
                )
        );

        emergency.setAlignment(
                Pos.CENTER
        );

        emergency.setMinSize(
                40,
                40
        );

        emergency.setStyle(
                "-fx-background-color: "
                        + Theme.ERROR
                        + ";"
                        + "-fx-background-radius: 20;"
        );

        VBox details =
                new VBox(4);

        Label customer =
                new Label(
                        firstNonBlank(
                                request.getCustomerName(),
                                request.getUserName(),
                                "Customer"
                        )
                );

        customer.setTextFill(
                Color.web(
                        Theme.TEXT
                )
        );

        customer.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        13
                )
        );

        Label info =
                new Label(
                        firstNonBlank(
                                request.getEmergencyType(),
                                "Emergency"
                        )
                                + " • "
                                + firstNonBlank(
                                request.getLocation(),
                                "Location not available"
                        )
                );

        info.setTextFill(
                Color.web(
                        Theme.SECONDARY_TEXT
                )
        );

        info.setFont(
                Font.font(
                        "Arial",
                        11
                )
        );

        details
                .getChildren()
                .addAll(
                        customer,
                        info
                );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Label status =
                createStatusBadge(
                        request.getStatus()
                );

        Button open =
                new Button(
                        request.isAssigned()
                                ? "Respond"
                                : "Open"
                );

        styleSmallButton(
                open
        );

        open.setOnAction(
                event ->
                        showSOSRequests()
        );

        row.getChildren()
                .addAll(
                        emergency,
                        details,
                        spacer,
                        status,
                        open
                );

        return row;
    }

    // =====================================================
    // SECTION CARD
    // =====================================================

    private VBox createSectionCard() {

        VBox card =
                new VBox(10);

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
                        + "-fx-border-radius: 12;"
                        + "-fx-background-radius: 12;"
        );

        return card;
    }

    // =====================================================
    // SECTION HEADER
    // =====================================================

    private HBox createSectionHeader(
            String title,
            String buttonText,
            Runnable action
    ) {

        HBox box =
                new HBox();

        box.setAlignment(
                Pos.CENTER_LEFT
        );

        Label label =
                new Label(
                        title
                );

        label.setTextFill(
                Color.web(
                        Theme.HEADING
                )
        );

        label.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        19
                )
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Button button =
                new Button(
                        buttonText
                );

        styleSmallButton(
                button
        );

        button.setOnAction(
                event -> {

                    if (action != null) {

                        action.run();
                    }
                }
        );

        box.getChildren()
                .addAll(
                        label,
                        spacer,
                        button
                );

        return box;
    }

    // =====================================================
    // EMPTY LABEL
    // =====================================================

    private Label createEmptyLabel(
            String text
    ) {

        Label label =
                new Label(
                        text
                );

        label.setTextFill(
                Color.web(
                        Theme.SECONDARY_TEXT
                )
        );

        label.setPadding(
                new Insets(16)
        );

        label.setFont(
                Font.font(
                        "Arial",
                        12
                )
        );

        return label;
    }

    // =====================================================
    // STATUS BADGE
    // =====================================================

    private Label createStatusBadge(
            String status
    ) {

        status =
                firstNonBlank(
                        status,
                        "-"
                );

        String color =
                Theme.SECONDARY_TEXT;

        if (status.equalsIgnoreCase(
                "Pending"
        )) {

            color =
                    Theme.WARNING;

        } else if (status.equalsIgnoreCase(
                "Assigned"
        )) {

            color =
                    Theme.INFO;

        } else if (status.equalsIgnoreCase(
                "Accepted"
        )) {

            color =
                    "#F59E0B";

        } else if (status.equalsIgnoreCase(
                "In Progress"
        )) {

            color =
                    Theme.PRIMARY;

        } else if (
                status.equalsIgnoreCase(
                        "Completed"
                )
                        ||
                status.equalsIgnoreCase(
                        "Resolved"
                )
        ) {

            color =
                    Theme.SUCCESS;

        } else if (status.equalsIgnoreCase(
                "Cancelled"
        )) {

            color =
                    Theme.ERROR;
        }

        Label label =
                new Label(
                        status
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
                        10
                )
        );

        label.setPadding(
                new Insets(
                        6,
                        10,
                        6,
                        10
                )
        );

        label.setStyle(
                "-fx-background-color: "
                        + color
                        + "18;"
                        + "-fx-background-radius: 14;"
        );

        return label;
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
                        16,
                        10,
                        16
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
    // SMALL BUTTON
    // =====================================================

    private void styleSmallButton(
            Button button
    ) {

        button.setPadding(
                new Insets(
                        8,
                        13,
                        8,
                        13
                )
        );

        button.setStyle(
                "-fx-background-color: "
                        + Theme.SURFACE
                        + ";"
                        + "-fx-text-fill: "
                        + Theme.TEXT
                        + ";"
                        + "-fx-border-color: "
                        + Theme.BORDER
                        + ";"
                        + "-fx-border-radius: 7;"
                        + "-fx-background-radius: 7;"
                        + "-fx-font-size: 10px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-cursor: hand;"
        );
    }

    // =====================================================
    // UPDATE PROFILE
    // =====================================================

    private void updateProfile() {

        if (mechanicNameLabel != null) {

            mechanicNameLabel.setText(
                    getCurrentMechanicName()
            );
        }

        updateMechanicStatusLabel();
    }

    // =====================================================
    // MECHANIC NAME
    // =====================================================

    private String getCurrentMechanicName() {

        try {

            Mechanic mechanic =
                    mechanicDAO
                            .getCurrentMechanic();

            if (mechanic != null) {

                String name =
                        clean(
                                mechanic.getName()
                        );

                if (name != null) {

                    return name;
                }
            }

        } catch (Exception e) {

            System.err.println(
                    "Unable to load mechanic profile: "
                            + e.getMessage()
            );
        }

        return getSessionMechanicName();
    }

    // =====================================================
    // SESSION NAME
    // =====================================================

    private String getSessionMechanicName() {

        String name =
                clean(
                        UserSession.getUserName()
                );

        return name == null
                ? "Mechanic"
                : name;
    }

    // =====================================================
    // ACTIVE STATUS
    // =====================================================

    private boolean isCurrentMechanicActive() {

        try {

            return mechanicDAO
                    .isCurrentMechanicActive();

        } catch (Exception e) {

            return true;
        }
    }

    // =====================================================
    // EXISTING COMPATIBILITY METHOD
    //
    // DO NOT REMOVE.
    // =====================================================

    public void openAcceptedJob(
            String customer,
            String vehicle,
            String problem,
            String type,
            String distance,
            String amount
    ) {

        if (activeJobPage == null) {

            return;
        }

        activeJobPage.setJob(
                customer,
                vehicle,
                problem,
                type,
                distance,
                amount
        );

        showActiveJob();
    }

    // =====================================================
    // LOGOUT
    // =====================================================

    private void logout() {

        try {

            /*
             * IMPORTANT:
             *
             * Save Stage reference BEFORE changing Scene.
             *
             * Otherwise root.getScene() can become null
             * after logout and cause the NPE that occurred
             * earlier.
             */

            Stage stage =
                    null;

            if (root != null
                    &&
                    root.getScene() != null
                    &&
                    root.getScene()
                            .getWindow() instanceof Stage) {

                stage =
                        (Stage)
                                root
                                        .getScene()
                                        .getWindow();
            }

            if (stage == null) {

                stage =
                        LoginPage
                                .getMainStage();
            }

            if (stage == null) {

                System.err.println(
                        "Mechanic logout failed: Stage not available."
                );

                return;
            }

            // =================================================
            // CLEAR USER
            // =================================================

            UserSession.clear();

            // =================================================
            // LOGIN PAGE
            // =================================================

            LoginPage loginPage =
                    new LoginPage();

            Scene loginScene =
                    new Scene(
                            loginPage.getView(),
                            1200,
                            700
                    );

            stage.setScene(
                    loginScene
            );

            stage.setTitle(
                    "RoadGuardian - Login"
            );

            stage.show();

            stage.toFront();

        } catch (Exception e) {

            System.err.println(
                    "Mechanic logout error: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }
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
}
