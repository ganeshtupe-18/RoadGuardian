package project.ui.user;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import project.ui.landing.LoginPage;

public final class UserHeader {

    // ============================================================
    // COLORS
    // ============================================================

    private static final String MAIN_BACKGROUND   = "#0F0F0F";
    private static final String BORDER            = "#F59E0B";
    private static final String HEADING           = "#F3F4F6";
    private static final String NAV_BLUE          = "#F59E0B";
    private static final String SECONDARY_TEXT    = "#A1A1AA";
    private static final String SECONDARY_SURFACE = "#242424";
    private static final String EMERGENCY         = "#EF4444";
    private static final String LOGOUT_RED        = "#EF4444";


    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    private UserHeader() {
    }


    // ============================================================
    // CREATE HEADER
    // ============================================================

    public static HBox createHeader() {

        HBox topBar = new HBox();

        topBar.setAlignment(
                Pos.CENTER_LEFT
        );

        topBar.setPadding(
                new Insets(
                        10,
                        28,
                        10,
                        28
                )
        );

        topBar.setSpacing(14);

        topBar.setMinHeight(76);
        topBar.setPrefHeight(76);
        topBar.setMaxHeight(76);

        topBar.setStyle(
                "-fx-background-color: "
                        + MAIN_BACKGROUND
                        + ";"
                        + "-fx-border-color: "
                        + BORDER
                        + ";"
                        + "-fx-border-width: 0 0 1 0;"
        );


        // ========================================================
        // LOGO
        // ========================================================

        StackPane logo =
                new StackPane();


        Circle logoCircle =
                new Circle(21);

        logoCircle.setFill(
                Color.web(
                        NAV_BLUE
                )
        );


        Label shield =
                new Label("✓");

        shield.setTextFill(
                Color.WHITE
        );

        shield.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        20
                )
        );

        shield.setTranslateY(-1);


        logo.getChildren().addAll(
                logoCircle,
                shield
        );


        logo.setMinSize(
                44,
                44
        );

        logo.setPrefSize(
                44,
                44
        );

        logo.setMaxSize(
                44,
                44
        );


        // ========================================================
        // BRAND
        // ========================================================

        HBox brand =
                new HBox(0);

        brand.setAlignment(
                Pos.CENTER_LEFT
        );


        Label road =
                new Label("Road");

        road.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        21
                )
        );

        road.setTextFill(
                Color.web(
                        HEADING
                )
        );

        road.setPadding(
                new Insets(0)
        );


        Label guardian =
                new Label("Guardian");

        guardian.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        21
                )
        );

        guardian.setTextFill(
                Color.web(
                        NAV_BLUE
                )
        );

        guardian.setPadding(
                new Insets(0)
        );


        brand.getChildren().addAll(
                road,
                guardian
        );


        HBox logoSection =
                new HBox(
                        11,
                        logo,
                        brand
                );

        logoSection.setAlignment(
                Pos.CENTER_LEFT
        );

        logoSection.setMinHeight(46);
        logoSection.setPrefHeight(46);


        // ========================================================
        // SPACER
        // ========================================================

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );


        // ========================================================
        // SEARCH BOX
        // ========================================================

        HBox searchBox =
                new HBox();

        searchBox.setAlignment(
                Pos.CENTER_LEFT
        );

        searchBox.setPrefWidth(420);

        searchBox.setMinWidth(300);

        searchBox.setMaxWidth(420);

        searchBox.setMinHeight(42);

        searchBox.setPrefHeight(42);

        searchBox.setMaxHeight(42);

        searchBox.setPadding(
                new Insets(
                        0,
                        15,
                        0,
                        15
                )
        );

        searchBox.setSpacing(9);

        searchBox.setStyle(
                "-fx-background-color: #1A1A1A;"
                        + "-fx-border-color: "
                        + BORDER
                        + ";"
                        + "-fx-border-radius: 24;"
                        + "-fx-background-radius: 24;"
        );


        Label searchIcon =
                new Label("⌕");

        searchIcon.setFont(
                Font.font(
                        "Arial",
                        24
                )
        );

        searchIcon.setTextFill(
                Color.web(
                        SECONDARY_TEXT
                )
        );


        Label searchText =
                new Label(
                        "Search mechanics, invoices, vehicles..."
                );

        searchText.setFont(
                Font.font(15)
        );

        searchText.setTextFill(
                Color.web(
                        SECONDARY_TEXT
                )
        );


        searchBox.getChildren().addAll(
                searchIcon,
                searchText
        );


        // ========================================================
        // NOTIFICATION
        // ========================================================

        StackPane notification =
                new StackPane();

        notification.setPrefSize(
                28,
                42
        );


        Label bell =
                new Label("🔔");

        bell.setFont(
                Font.font(
                        "Arial",
                        22
                )
        );

        bell.setTextFill(
                Color.web(
                        HEADING
                )
        );


        Circle notificationDot =
                new Circle(
                        5,
                        Color.web(
                                EMERGENCY
                        )
                );


        StackPane.setAlignment(
                notificationDot,
                Pos.TOP_RIGHT
        );

        notificationDot.setTranslateX(-1);

        notificationDot.setTranslateY(10);


        notification.getChildren().addAll(
                bell,
                notificationDot
        );


        // ========================================================
        // USER DATA
        // ========================================================

        String userName =
                UserSession.getUserName();

        String userRole =
                UserSession.getUserRole();


        if (userName == null
                || userName.trim().isEmpty()) {

            userName = "User";
        }


        if (userRole == null
                || userRole.trim().isEmpty()) {

            userRole = "Customer";
        }


        userName =
                userName.trim();

        userRole =
                userRole.trim();


        // ========================================================
        // INITIALS
        // ========================================================

        String initials =
                getInitials(
                        userName
                );


        // ========================================================
        // PROFILE
        // ========================================================

        HBox profile =
                new HBox(9);

        profile.setAlignment(
                Pos.CENTER_LEFT
        );

        profile.setPadding(
                new Insets(
                        5,
                        12,
                        5,
                        6
                )
        );

        profile.setStyle(
                "-fx-background-color: #1A1A1A;"
                        + "-fx-border-color: "
                        + BORDER
                        + ";"
                        + "-fx-border-radius: 25;"
                        + "-fx-background-radius: 25;"
        );


        // ========================================================
        // AVATAR
        // ========================================================

        StackPane avatar =
                new StackPane();


        Circle avatarCircle =
                new Circle(19);

        avatarCircle.setFill(
                Color.web(
                        SECONDARY_SURFACE
                )
        );


        Label initialsLabel =
                new Label(
                        initials
                );

        initialsLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        13
                )
        );

        initialsLabel.setTextFill(
                Color.web(
                        NAV_BLUE
                )
        );


        avatar.getChildren().addAll(
                avatarCircle,
                initialsLabel
        );


        // ========================================================
        // PROFILE TEXT
        // ========================================================

        VBox profileText =
                new VBox(1);


        Label name =
                new Label(
                        userName
                );

        name.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        13
                )
        );

        name.setTextFill(
                Color.web(
                        HEADING
                )
        );


        Label role =
                new Label(
                        userRole
                );

        role.setFont(
                Font.font(11)
        );

        role.setTextFill(
                Color.web(
                        SECONDARY_TEXT
                )
        );


        profileText.getChildren().addAll(
                name,
                role
        );


        profile.getChildren().addAll(
                avatar,
                profileText
        );


        // ========================================================
        // LOGOUT BUTTON
        // ========================================================

        Button logoutButton =
                new Button(
                        "Logout"
                );


        logoutButton.setPrefWidth(82);

        logoutButton.setMinWidth(82);

        logoutButton.setMaxWidth(82);


        logoutButton.setPrefHeight(42);

        logoutButton.setMinHeight(42);

        logoutButton.setMaxHeight(42);


        logoutButton.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        13
                )
        );


        logoutButton.setCursor(
                javafx.scene.Cursor.HAND
        );


        setLogoutNormalStyle(
                logoutButton
        );


        // ========================================================
        // LOGOUT HOVER
        // ========================================================

        logoutButton.setOnMouseEntered(
                e ->
                        logoutButton.setStyle(
                                "-fx-background-color: "
                                        + LOGOUT_RED
                                        + ";"
                                        + "-fx-text-fill: white;"
                                        + "-fx-background-radius: 22;"
                                        + "-fx-border-color: "
                                        + LOGOUT_RED
                                        + ";"
                                        + "-fx-border-radius: 22;"
                        )
        );


        logoutButton.setOnMouseExited(
                e ->
                        setLogoutNormalStyle(
                                logoutButton
                        )
        );


        // ========================================================
        // LOGOUT ACTION
        // ========================================================

        logoutButton.setOnAction(
                e -> logout()
        );


        // ========================================================
        // ADD ALL HEADER COMPONENTS
        // ========================================================

        topBar.getChildren().addAll(
                logoSection,
                spacer,
                searchBox,
                notification,
                profile,
                logoutButton
        );


        return topBar;
    }


    // ============================================================
    // LOGOUT
    // ============================================================

    private static void logout() {

        try {

            // ====================================================
            // GET CURRENT STAGE
            // ====================================================

            Stage currentStage =
                    UserDashboard.dashboardStage;


            if (currentStage == null) {

                currentStage =
                        LoginPage.getMainStage();
            }


            if (currentStage == null) {

                System.err.println(
                        "Logout error: Stage is null."
                );

                return;
            }


            // ====================================================
            // IMPORTANT:
            // FINAL STAGE FOR PLATFORM.RUNLATER
            // ====================================================

            final Stage stage =
                    currentStage;


            // ====================================================
            // REMEMBER CURRENT WINDOW STATE
            // ====================================================

            final boolean wasMaximized =
                    stage.isMaximized();

            final boolean wasFullScreen =
                    stage.isFullScreen();


            // ====================================================
            // CLEAR USER SESSION
            // ====================================================

            UserSession.clear();


            // ====================================================
            // CREATE LOGIN PAGE
            // ====================================================

            LoginPage loginPage =
                    new LoginPage();


            Scene loginScene =
                    new Scene(
                            loginPage.getView()
                    );


            // ====================================================
            // SET LOGIN SCENE
            // ====================================================

            stage.setScene(
                    loginScene
            );

            stage.setTitle(
                    "RoadGuardian - Login"
            );


            // ====================================================
            // SHOW SAME STAGE
            // ====================================================

            stage.show();


            // ====================================================
            // RESTORE WINDOW STATE AFTER LAYOUT
            // ====================================================

            Platform.runLater(() -> {

                try {

                    // ============================================
                    // FULL SCREEN
                    // ============================================

                    if (wasFullScreen) {

                        stage.setFullScreen(
                                true
                        );

                        stage.toFront();

                        return;
                    }


                    // ============================================
                    // MAXIMIZED
                    // ============================================

                    if (wasMaximized) {

                        stage.setMaximized(
                                true
                        );

                        stage.toFront();

                        return;
                    }


                    // ============================================
                    // NORMAL WINDOW
                    // ============================================

                    stage.setMaximized(
                            false
                    );

                    stage.toFront();


                } catch (Exception exception) {

                    exception.printStackTrace();
                }
            });


        } catch (Exception exception) {

            exception.printStackTrace();
        }
    }


    // ============================================================
    // LOGOUT NORMAL STYLE
    // ============================================================

    private static void setLogoutNormalStyle(
            Button button
    ) {

        button.setStyle(
                "-fx-background-color: white;"
                        + "-fx-text-fill: "
                        + LOGOUT_RED
                        + ";"
                        + "-fx-background-radius: 22;"
                        + "-fx-border-color: "
                        + LOGOUT_RED
                        + ";"
                        + "-fx-border-radius: 22;"
        );
    }


    // ============================================================
    // GET INITIALS
    // ============================================================

    private static String getInitials(
            String name
    ) {

        if (name == null
                || name.trim().isEmpty()) {

            return "U";
        }


        String cleanName =
                name.trim();


        String[] parts =
                cleanName.split(
                        "\\s+"
                );


        // ========================================================
        // SINGLE NAME
        // ========================================================

        if (parts.length == 1) {

            return parts[0]
                    .substring(
                            0,
                            1
                    )
                    .toUpperCase();
        }


        // ========================================================
        // FIRST + LAST NAME
        // ========================================================

        String first =
                parts[0]
                        .substring(
                                0,
                                1
                        )
                        .toUpperCase();


        String last =
                parts[parts.length - 1]
                        .substring(
                                0,
                                1
                        )
                        .toUpperCase();


        return first + last;
    }
}