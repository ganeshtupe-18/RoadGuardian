package project.ui.user;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import project.app.AppNavigator;

/**
 * RoadGuardian - User Sidebar
 *
 * Handles navigation between all customer-side pages.
 *
 * Every page opened from this sidebar uses the SAME Stage.
 *
 * Window size / maximize state is handled by AppNavigator
 * and WindowManager.
 */
public class UserSideBar {

    // ============================================================
    // COLORS
    // ============================================================

    private static final String SIDEBAR          = "#161616";
    private static final String SECONDARY_SURFACE = "#242424";

    private static final String HEADING        = "#F3F4F6";
    private static final String SECONDARY_TEXT = "#A1A1AA";

    private static final String NAV_BLUE = "#F59E0B";


    // ============================================================
    // SIDEBAR WIDTH
    // ============================================================

    private static final double SIDEBAR_WIDTH = 270;


    // ============================================================
    // CREATE SIDEBAR
    // ============================================================

    public static ScrollPane createSidebar(String selectedPage) {

        // ========================================================
        // SIDEBAR CONTENT
        // ========================================================

        VBox sidebarContent = new VBox();

        sidebarContent.setPrefWidth(
                SIDEBAR_WIDTH
        );

        sidebarContent.setMinWidth(
                SIDEBAR_WIDTH
        );

        sidebarContent.setMaxWidth(
                SIDEBAR_WIDTH
        );

        sidebarContent.setPadding(
                new Insets(
                        20,
                        12,
                        30,
                        12
                )
        );

        sidebarContent.setSpacing(5);

        sidebarContent.setFillWidth(true);

        sidebarContent.setStyle(
                "-fx-background-color: "
                        + SIDEBAR
                        + ";"
        );


        // ========================================================
        // OVERVIEW
        // ========================================================

        addSidebarSection(
                sidebarContent,
                "OVERVIEW"
        );


        Button dashboard =
                createSidebarButton(
                        "▦",
                        "Dashboard",
                        selectedPage
                );


        Button sos =
                createSidebarButton(
                        "♧",
                        "Emergency SOS",
                        selectedPage
                );


        Button liveMap =
                createSidebarButton(
                        "◇",
                        "Live Map",
                        selectedPage
                );


        sidebarContent.getChildren().addAll(
                dashboard,
                sos,
                liveMap
        );


        // ========================================================
        // ASSISTANCE
        // ========================================================

        addSidebarSection(
                sidebarContent,
                "ASSISTANCE"
        );


        Button aiDiagnosis =
                createSidebarButton(
                        "♧",
                        "AI Diagnosis",
                        selectedPage
                );


        Button mechanics =
                createSidebarButton(
                        "⚒",
                        "Mechanics",
                        selectedPage
                );


        Button towTruck =
                createSidebarButton(
                        "▱",
                        "Tow Truck",
                        selectedPage
                );


        Button costEstimator =
                createSidebarButton(
                        "▤",
                        "Cost Estimator",
                        selectedPage
                );


        sidebarContent.getChildren().addAll(
                aiDiagnosis,
                mechanics,
                towTruck,
                costEstimator
        );


        // ========================================================
        // GARAGE
        // ========================================================

        addSidebarSection(
                sidebarContent,
                "GARAGE"
        );


        Button vehicles =
                createSidebarButton(
                        "▱",
                        "My Vehicles",
                        selectedPage
                );


        Button serviceHistory =
                createSidebarButton(
                        "◷",
                        "Service History",
                        selectedPage
                );


        Button reviews =
                createSidebarButton(
                        "★",
                        "Reviews",
                        selectedPage
                );


        Button documents =
                createSidebarButton(
                        "▧",
                        "Documents",
                        selectedPage
                );


        sidebarContent.getChildren().addAll(
                vehicles,
                serviceHistory,
                reviews,
                documents
        );


        // ========================================================
        // ACCOUNT
        // ========================================================

        addSidebarSection(
                sidebarContent,
                "ACCOUNT"
        );


        Button profile =
                createSidebarButton(
                        "◉",
                        "Profile",
                        selectedPage
                );


        Button womenSafety =
                createSidebarButton(
                        "♙",
                        "Women Safety",
                        selectedPage
                );


        Button notifications =
                createSidebarButton(
                        "♧",
                        "Notifications",
                        selectedPage
                );


        Button complaints =
                createSidebarButton(
                        "!",
                        "Complaints",
                        selectedPage
                );


        Button settings =
                createSidebarButton(
                        "⚙",
                        "Settings",
                        selectedPage
                );


        sidebarContent.getChildren().addAll(
                profile,
                womenSafety,
                notifications,
                complaints,
                settings
        );


        // ========================================================
        // NAVIGATION
        // ========================================================

        dashboard.setOnAction(
                e -> openPage(
                        dashboard,
                        "Dashboard"
                )
        );


        sos.setOnAction(
                e -> openPage(
                        sos,
                        "Emergency SOS"
                )
        );


        liveMap.setOnAction(
                e -> openPage(
                        liveMap,
                        "Live Map"
                )
        );


        aiDiagnosis.setOnAction(
                e -> openPage(
                        aiDiagnosis,
                        "AI Diagnosis"
                )
        );


        mechanics.setOnAction(
                e -> openPage(
                        mechanics,
                        "Mechanics"
                )
        );


        towTruck.setOnAction(
                e -> openPage(
                        towTruck,
                        "Tow Truck"
                )
        );


        costEstimator.setOnAction(
                e -> openPage(
                        costEstimator,
                        "Cost Estimator"
                )
        );


        vehicles.setOnAction(
                e -> openPage(
                        vehicles,
                        "My Vehicles"
                )
        );


        serviceHistory.setOnAction(
                e -> openPage(
                        serviceHistory,
                        "Service History"
                )
        );


        reviews.setOnAction(
                e -> openPage(
                        reviews,
                        "Reviews"
                )
        );


        documents.setOnAction(
                e -> openPage(
                        documents,
                        "Documents"
                )
        );


        profile.setOnAction(
                e -> openPage(
                        profile,
                        "Profile"
                )
        );


        womenSafety.setOnAction(
                e -> openPage(
                        womenSafety,
                        "Women Safety"
                )
        );


        notifications.setOnAction(
                e -> openPage(
                        notifications,
                        "Notifications"
                )
        );


        complaints.setOnAction(
                e -> openPage(
                        complaints,
                        "Complaints"
                )
        );


        settings.setOnAction(
                e -> openPage(
                        settings,
                        "Settings"
                )
        );


        // ========================================================
        // CREATE SCROLL PANE
        // ========================================================

        ScrollPane sidebarScroll =
                new ScrollPane(
                        sidebarContent
                );


        // ========================================================
        // FIXED SIDEBAR WIDTH
        // ========================================================

        sidebarScroll.setPrefWidth(
                SIDEBAR_WIDTH
        );

        sidebarScroll.setMinWidth(
                SIDEBAR_WIDTH
        );

        sidebarScroll.setMaxWidth(
                SIDEBAR_WIDTH
        );


        // ========================================================
        // WIDTH BEHAVIOR
        // ========================================================

        sidebarScroll.setFitToWidth(true);

        /*
         * IMPORTANT:
         *
         * Content must NOT be forced to viewport height.
         * Otherwise vertical scrolling can stop.
         */
        sidebarScroll.setFitToHeight(false);


        // ========================================================
        // SCROLLBAR SETTINGS
        // ========================================================

        sidebarScroll.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        sidebarScroll.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED
        );


        // ========================================================
        // PANNABLE
        // ========================================================

        sidebarScroll.setPannable(true);


        // ========================================================
        // IMPORTANT:
        // NEVER SET VMAX TO CONTENT PIXEL HEIGHT
        // ========================================================
        //
        // ScrollPane internally handles Vmax.
        //
        // DO NOT DO:
        //
        // sidebarScroll.setVmax(
        //     sidebarContent.getBoundsInLocal().getHeight()
        // );
        //
        // That was causing incorrect scrolling behavior.
        // ========================================================


        // ========================================================
        // MOUSE WHEEL SCROLL
        // ========================================================

        sidebarScroll.addEventFilter(
                ScrollEvent.SCROLL,
                event -> {

                    double delta =
                            event.getDeltaY();

                    if (Math.abs(delta) < 0.01) {
                        return;
                    }


                    /*
                     * JavaFX ScrollPane vvalue normally ranges
                     * between 0.0 and 1.0.
                     *
                     * Convert mouse wheel movement into
                     * normalized scroll movement.
                     */

                    double currentValue =
                            sidebarScroll.getVvalue();


                    double movement =
                            delta / 700.0;


                    double newValue =
                            currentValue - movement;


                    newValue =
                            Math.max(
                                    0.0,
                                    Math.min(
                                            1.0,
                                            newValue
                                    )
                            );


                    sidebarScroll.setVvalue(
                            newValue
                    );


                    event.consume();
                }
        );


        // ========================================================
        // SCROLL PANE STYLE
        // ========================================================

        sidebarScroll.setStyle(
                "-fx-background-color: "
                        + SIDEBAR
                        + ";"
                        + "-fx-background: "
                        + SIDEBAR
                        + ";"
                        + "-fx-control-inner-background: "
                        + SIDEBAR
                        + ";"
                        + "-fx-border-color: transparent;"
        );


        // ========================================================
        // RETURN SIDEBAR
        // ========================================================

        return sidebarScroll;
    }


    // ============================================================
    // OPEN PAGE
    // ============================================================

    private static void openPage(
            Button sourceButton,
            String pageName
    ) {

        try {

            // ====================================================
            // BUTTON CHECK
            // ====================================================

            if (sourceButton == null) {

                System.err.println(
                        "Navigation error: source button is null."
                );

                return;
            }


            // ====================================================
            // SCENE CHECK
            // ====================================================

            if (sourceButton.getScene() == null) {

                System.err.println(
                        "Navigation error: button has no Scene."
                );

                return;
            }


            // ====================================================
            // WINDOW CHECK
            // ====================================================

            if (
                    sourceButton
                            .getScene()
                            .getWindow() == null
            ) {

                System.err.println(
                        "Navigation error: Scene has no Window."
                );

                return;
            }


            // ====================================================
            // GET CURRENT STAGE
            // ====================================================

            Stage stage =
                    (Stage) sourceButton
                            .getScene()
                            .getWindow();


            if (stage == null) {
                return;
            }


            // ====================================================
            // KEEP DASHBOARD STAGE SYNCHRONIZED
            // ====================================================

            UserDashboard.dashboardStage =
                    stage;


            // ====================================================
            // CENTRAL NAVIGATION
            // ====================================================
            //
            // IMPORTANT:
            //
            // We NEVER call:
            //
            // stage.setScene(...)
            //
            // from here.
            //
            // AppNavigator + WindowManager handle:
            //
            // - same Stage
            // - scene switching
            // - maximize state
            // - normal window size
            // - position
            //
            // ====================================================

            AppNavigator.navigate(
                    stage,
                    pageName
            );


        } catch (Exception ex) {

            System.err.println(
                    "Error opening page "
                            + pageName
                            + ": "
                            + ex.getMessage()
            );

            ex.printStackTrace();
        }
    }


    // ============================================================
    // SIDEBAR SECTION
    // ============================================================

    private static void addSidebarSection(
            VBox sidebar,
            String title
    ) {

        Label section =
                new Label(title);


        section.setPadding(
                new Insets(
                        11,
                        12,
                        7,
                        12
                )
        );


        section.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        12
                )
        );


        section.setTextFill(
                Color.web(
                        SECONDARY_TEXT
                )
        );


        sidebar.getChildren().add(
                section
        );
    }


    // ============================================================
    // SIDEBAR BUTTON
    // ============================================================

    private static Button createSidebarButton(
            String icon,
            String text,
            String selectedPage
    ) {

        boolean selected =
                text.equals(selectedPage);


        Button button =
                new Button(
                        icon
                                + "    "
                                + text
                );


        button.setAlignment(
                Pos.CENTER_LEFT
        );


        button.setMaxWidth(
                Double.MAX_VALUE
        );


        button.setPrefHeight(44);

        button.setMinHeight(44);


        button.setPadding(
                new Insets(
                        0,
                        13,
                        0,
                        13
                )
        );


        button.setFont(
                Font.font(
                        "Arial",
                        selected
                                ? FontWeight.BOLD
                                : FontWeight.NORMAL,
                        15
                )
        );


        button.setCursor(
                Cursor.HAND
        );


        // ========================================================
        // SELECTED BUTTON
        // ========================================================

        if (selected) {

            button.setStyle(
                    "-fx-background-color: "
                            + NAV_BLUE
                            + ";"
                            + "-fx-text-fill: white;"
                            + "-fx-background-radius: 24;"
                            + "-fx-border-color: transparent;"
            );
        }


        // ========================================================
        // NORMAL BUTTON
        // ========================================================

        else {

            setNormalStyle(
                    button
            );


            button.setOnMouseEntered(
                    e ->
                            button.setStyle(
                                    "-fx-background-color: "
                                            + SECONDARY_SURFACE
                                            + ";"
                                            + "-fx-text-fill: "
                                            + HEADING
                                            + ";"
                                            + "-fx-background-radius: 24;"
                                            + "-fx-border-color: transparent;"
                            )
            );


            button.setOnMouseExited(
                    e ->
                            setNormalStyle(
                                    button
                            )
            );
        }


        return button;
    }


    // ============================================================
    // NORMAL BUTTON STYLE
    // ============================================================

    private static void setNormalStyle(
            Button button
    ) {

        button.setStyle(
                "-fx-background-color: transparent;"
                        + "-fx-text-fill: "
                        + HEADING
                        + ";"
                        + "-fx-background-radius: 24;"
                        + "-fx-border-color: transparent;"
        );
    }
}