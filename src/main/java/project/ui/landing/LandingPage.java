package project.ui.landing;

import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class LandingPage extends Application {

    public static Stage appMainStage;

    private final StackPane root = new StackPane();

    private MediaPlayer introMediaPlayer;


    // =====================================================
    // APPLICATION START
    // =====================================================

    @Override
    public void start(Stage primaryStage) {

        appMainStage = primaryStage;

        primaryStage.setTitle(
                "RoadGuardian"
        );

        /*
         * Proper Windows application window.
         * Fullscreen nahi.
         */
        primaryStage.initStyle(
                StageStyle.DECORATED
        );

        primaryStage.setResizable(true);

        /*
         * Complete monitor cover करण्यासाठी
         * maximized window.
         */
        primaryStage.setMaximized(true);

        /*
         * Intro video.
         */
        playIntroVideo(primaryStage);
    }


    // =====================================================
    // INTRO VIDEO
    // =====================================================

    private void playIntroVideo(Stage primaryStage) {

        try {

            var videoUrl = getClass().getResource(
                    "/video/roadguardian_intro.mp4"
            );


            // =================================================
            // VIDEO NOT FOUND
            // =================================================

            if (videoUrl == null) {

                System.err.println(
                        "Intro video not found: " +
                                "/video/roadguardian_intro.mp4"
                );

                showLandingPage(
                        primaryStage
                );

                return;
            }


            // =================================================
            // MEDIA
            // =================================================

            Media media =
                    new Media(
                            videoUrl.toExternalForm()
                    );


            introMediaPlayer =
                    new MediaPlayer(
                            media
                    );


            introMediaPlayer.setVolume(
                    1.0
            );


            // =================================================
            // MEDIA VIEW
            // =================================================

            MediaView mediaView =
                    new MediaView(
                            introMediaPlayer
                    );


            /*
             * Video complete window fill करेल.
             */
            mediaView.setPreserveRatio(
                    false
            );

            mediaView.setSmooth(
                    true
            );


            // =================================================
            // VIDEO ROOT
            // =================================================

            StackPane introRoot =
                    new StackPane(
                            mediaView
                    );


            introRoot.setStyle(
                    "-fx-background-color:black;"
            );


            /*
             * Video width = window width.
             */
            mediaView
                    .fitWidthProperty()
                    .bind(
                            introRoot.widthProperty()
                    );


            /*
             * Video height = window height.
             */
            mediaView
                    .fitHeightProperty()
                    .bind(
                            introRoot.heightProperty()
                    );


            // =================================================
            // VIDEO SCENE
            // =================================================

            Scene introScene =
                    new Scene(
                            introRoot,
                            1280,
                            720,
                            Color.BLACK
                    );


            /*
             * IMPORTANT:
             *
             * Separate Stage create करत नाही.
             * Same primaryStage मध्ये video.
             */
            primaryStage.setScene(
                    introScene
            );


            /*
             * Maximized window.
             */
            primaryStage.setMaximized(
                    true
            );


            primaryStage.show();


            primaryStage.toFront();


            // =================================================
            // VIDEO FINISHED
            // =================================================

            introMediaPlayer.setOnEndOfMedia(
                    () -> {

                        try {

                            introMediaPlayer.stop();

                            introMediaPlayer.dispose();

                        } catch (Exception ignored) {

                        }


                        introMediaPlayer = null;


                        /*
                         * Video संपताच immediately
                         * Landing Page.
                         */
                        Platform.runLater(
                                () -> {

                                    showLandingPage(
                                            primaryStage
                                    );
                                }
                        );
                    }
            );


            // =================================================
            // VIDEO ERROR
            // =================================================

            introMediaPlayer.setOnError(
                    () -> {

                        System.err.println(
                                "Intro video error: " +
                                        (
                                                introMediaPlayer != null
                                                        ? introMediaPlayer.getError()
                                                        : "Unknown error"
                                        )
                        );


                        try {

                            if (
                                    introMediaPlayer != null
                            ) {

                                introMediaPlayer.stop();

                                introMediaPlayer.dispose();

                                introMediaPlayer = null;
                            }

                        } catch (Exception ignored) {

                        }


                        /*
                         * Video error झाला तरी
                         * Landing Page दाखवायची.
                         */
                        Platform.runLater(
                                () -> {

                                    showLandingPage(
                                            primaryStage
                                    );
                                }
                        );
                    }
            );


            // =================================================
            // START VIDEO
            // =================================================

            introMediaPlayer.play();


        } catch (Exception e) {

            System.err.println(
                    "Unable to play intro video: " +
                            e.getMessage()
            );


            showLandingPage(
                    primaryStage
            );
        }
    }


    // =====================================================
    // SHOW LANDING PAGE
    // =====================================================

    private void showLandingPage(
            Stage primaryStage
    ) {

        /*
         * Intro player cleanup.
         */
        if (
                introMediaPlayer != null
        ) {

            try {

                introMediaPlayer.stop();

                introMediaPlayer.dispose();

            } catch (Exception ignored) {

            }


            introMediaPlayer = null;
        }


        primaryStage.setTitle(
                "RoadGuardian"
        );


        /*
         * Landing page same window मध्ये.
         */
        primaryStage.setScene(
                createPage()
        );


        /*
         * Window maximized राहील.
         */
        primaryStage.setMaximized(
                true
        );


        primaryStage.show();


        primaryStage.toFront();
    }


    // =====================================================
    // LANDING PAGE
    // =====================================================

    public Scene createPage() {

        project.ui.theme.DarkTheme.apply(
                root
        );


        // =====================================================
        // BACKGROUND IMAGE
        // =====================================================

        Image darkCarImage = null;


        if (
                getClass().getResource(
                        "/image/landingDarkBg.jpeg"
                ) != null
        ) {

            darkCarImage =
                    new Image(
                            getClass()
                                    .getResource(
                                            "/image/landingDarkBg.jpeg"
                                    )
                                    .toExternalForm()
                    );
        }


        ImageView backgroundImage =
                new ImageView();


        if (
                darkCarImage != null
        ) {

            backgroundImage.setImage(
                    darkCarImage
            );


            backgroundImage.setPreserveRatio(
                    false
            );


            backgroundImage.setSmooth(
                    true
            );


            backgroundImage
                    .fitWidthProperty()
                    .bind(
                            root.widthProperty()
                    );


            backgroundImage
                    .fitHeightProperty()
                    .bind(
                            root.heightProperty()
                    );
        }


        backgroundImage.setMouseTransparent(
                true
        );


        // =====================================================
        // DARK OVERLAY
        // =====================================================

        Region darkOverlay =
                new Region();


        darkOverlay.setStyle(
                "-fx-background-color:rgba(0,0,0,0.42);"
        );


        darkOverlay.setMouseTransparent(
                true
        );


        darkOverlay
                .prefWidthProperty()
                .bind(
                        root.widthProperty()
                );


        darkOverlay
                .prefHeightProperty()
                .bind(
                        root.heightProperty()
                );


        // =====================================================
        // MAIN CONTENT
        // =====================================================

        BorderPane content =
                new BorderPane();


        content.setPickOnBounds(
                false
        );


        // =====================================================
        // LOGO
        // =====================================================

        Text road =
                new Text(
                        "Road"
                );


        road.setStyle(
                "-fx-font-family:'Segoe UI';" +
                        "-fx-font-size:28px;" +
                        "-fx-font-weight:bold;" +
                        "-fx-fill:#F3F4F6;"
        );


        Text guardian =
                new Text(
                        "Guardian"
                );


        guardian.setStyle(
                "-fx-font-family:'Segoe UI';" +
                        "-fx-font-size:28px;" +
                        "-fx-font-weight:bold;" +
                        "-fx-fill:#F59E0B;"
        );


        HBox logo =
                new HBox(
                        road,
                        guardian
                );


        logo.setAlignment(
                Pos.CENTER_LEFT
        );


        // =====================================================
        // LOGIN BUTTON
        // =====================================================

        Button loginButton =
                createButton(
                        "Login",
                        false,
                        14
                );


        // =====================================================
        // REGISTER BUTTON
        // =====================================================

        Button registerButton =
                createButton(
                        "Create Account",
                        true,
                        14
                );


        HBox navButtons =
                new HBox(
                        12,
                        loginButton,
                        registerButton
                );


        navButtons.setAlignment(
                Pos.CENTER_RIGHT
        );


        Region spacer =
                new Region();


        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );


        // =====================================================
        // NAVBAR
        // =====================================================

        HBox navbar =
                new HBox(
                        logo,
                        spacer,
                        navButtons
                );


        navbar.setAlignment(
                Pos.CENTER
        );


        navbar.setPadding(
                new Insets(
                        18,
                        45,
                        18,
                        45
                )
        );


        navbar.setStyle(
                "-fx-background-color:transparent;"
        );


        // =====================================================
        // HERO HEADING
        // =====================================================

        Text heading =
                new Text(
                        "Your Roadside Safety\nCompanion"
                );


        heading.setStyle(
                "-fx-font-family:'Segoe UI';" +
                        "-fx-font-size:44px;" +
                        "-fx-font-weight:bold;" +
                        "-fx-fill:#F3F4F6;"
        );


        // =====================================================
        // HEADING LINE
        // =====================================================

        Region headingLine =
                new Region();


        headingLine.setPrefHeight(
                3
        );


        headingLine.setMaxWidth(
                635
        );


        headingLine.setMinWidth(
                400
        );


        headingLine.setStyle(
                "-fx-background-color:#F59E0B;" +
                        "-fx-background-radius:5px;"
        );


        // =====================================================
        // DESCRIPTION
        // =====================================================

        Text description =
                new Text(
                        "RoadGuardian helps you get assistance when " +
                                "your vehicle breaks down, connects you with " +
                                "nearby mechanics and keeps your journey safer."
                );


        description.setWrappingWidth(
                540
        );


        description.setStyle(
                "-fx-font-family:'Segoe UI';" +
                        "-fx-font-size:17px;" +
                        "-fx-fill:#D4D4D8;"
        );


        // =====================================================
        // GET STARTED
        // =====================================================

        Button getStartedButton =
                createButton(
                        "Get Started",
                        true,
                        16
                );


        // =====================================================
        // MECHANIC BUTTON
        // =====================================================

        Button becomeMechanicButton =
                createButton(
                        "Become a Mechanic",
                        false,
                        16
                );


        HBox heroButtons =
                new HBox(
                        15,
                        getStartedButton,
                        becomeMechanicButton
                );


        heroButtons.setAlignment(
                Pos.CENTER_LEFT
        );


        // =====================================================
        // HERO TEXT
        // =====================================================

        VBox heroText =
                new VBox(
                        20,
                        heading,
                        headingLine,
                        description,
                        heroButtons
                );


        heroText.setAlignment(
                Pos.CENTER_LEFT
        );


        heroText.setMaxWidth(
                650
        );


        heroText.setMinWidth(
                500
        );


        // =====================================================
        // HERO SECTION
        // =====================================================

        StackPane heroSection =
                new StackPane(
                        heroText
                );


        heroSection.setAlignment(
                Pos.CENTER_LEFT
        );


        heroSection.setPadding(
                new Insets(
                        35,
                        40,
                        20,
                        85
                )
        );


        heroSection.setMinHeight(
                430
        );


        // =====================================================
        // FEATURE TITLE
        // =====================================================

        Text featureTitle =
                new Text(
                        "Why RoadGuardian?"
                );


        featureTitle.setStyle(
                "-fx-font-family:'Segoe UI';" +
                        "-fx-font-size:30px;" +
                        "-fx-font-weight:bold;" +
                        "-fx-fill:#F3F4F6;"
        );


        // =====================================================
        // FEATURES
        // =====================================================

        VBox sos =
                createFeature(
                        "Emergency Assistance",
                        "Get roadside help when you need it.",
                        "#EF4444"
                );


        VBox diagnosis =
                createFeature(
                        "AI Breakdown Diagnosis",
                        "Understand possible vehicle problems.",
                        "#F59E0B"
                );


        VBox mechanic =
                createFeature(
                        "Smart Mechanic Dispatch",
                        "Connect with nearby mechanics.",
                        "#22C55E"
                );


        VBox tracking =
                createFeature(
                        "Live Tracking",
                        "Track your assigned mechanic.",
                        "#F59E0B"
                );


        HBox features =
                new HBox(
                        25,
                        sos,
                        diagnosis,
                        mechanic,
                        tracking
                );


        features.setAlignment(
                Pos.CENTER_LEFT
        );


        features.setMaxWidth(
                1100
        );


        // =====================================================
        // FEATURE SECTION
        // =====================================================

        VBox featureSection =
                new VBox(
                        15,
                        featureTitle,
                        features
                );


        featureSection.setAlignment(
                Pos.CENTER_LEFT
        );


        featureSection.setPadding(
                new Insets(
                        5,
                        30,
                        10,
                        30
                )
        );


        // =====================================================
        // CENTER CONTENT
        // =====================================================

        VBox centerContent =
                new VBox(
                        0,
                        heroSection,
                        featureSection
                );


        centerContent.setFillWidth(
                true
        );


        centerContent.setStyle(
                "-fx-background-color:transparent;"
        );


        // =====================================================
        // FOOTER
        // =====================================================

        Text footerText =
                new Text(
                        "© 2026 RoadGuardian. All rights reserved."
                );


        footerText.setStyle(
                "-fx-font-family:'Segoe UI';" +
                        "-fx-font-size:13px;" +
                        "-fx-fill:#D4D4D8;"
        );


        HBox footer =
                new HBox(
                        footerText
                );


        footer.setAlignment(
                Pos.CENTER
        );


        footer.setPadding(
                new Insets(
                        8
                )
        );


        footer.setStyle(
                "-fx-background-color:rgba(10,10,10,0.72);" +
                        "-fx-border-color:rgba(245,158,11,0.20);" +
                        "-fx-border-width:1 0 0 0;"
        );


        // =====================================================
        // THANK YOU BUTTON - BOTTOM RIGHT
        // =====================================================

        Button thankYouButton =
                createButton(
                        "Thank You",
                        false,
                        14
                );

        thankYouButton.setPrefWidth(130);

        StackPane.setAlignment(
                thankYouButton,
                Pos.BOTTOM_RIGHT
        );

        StackPane.setMargin(
                thankYouButton,
                new Insets(0, 28, 22, 0)
        );

        // =====================================================
        // BUTTON ACTIONS
        // =====================================================

        loginButton.setOnAction(
                e -> openLoginPage()
        );


        registerButton.setOnAction(
                e -> openRegisterPage()
        );


        getStartedButton.setOnAction(
                e -> openLoginPage()
        );


        becomeMechanicButton.setOnAction(
                e -> openMechanicRegisterPage()
        );


        thankYouButton.setOnAction(
                e -> showThankYouPage()
        );


        // =====================================================
        // BORDERPANE
        // =====================================================

        content.setTop(
                navbar
        );


        content.setCenter(
                centerContent
        );


        content.setBottom(
                footer
        );


        content
                .prefWidthProperty()
                .bind(
                        root.widthProperty()
                );


        content
                .prefHeightProperty()
                .bind(
                        root.heightProperty()
                );


        // =====================================================
        // ROOT
        // =====================================================

        root
                .getChildren()
                .clear();


        root
                .getChildren()
                .addAll(
                        backgroundImage,
                        darkOverlay,
                        content,
                        thankYouButton
                );


        // =====================================================
        // SCENE
        // =====================================================

        return new Scene(
                root,
                Color.BLACK
        );
    }


    // =====================================================
    // THANK YOU PAGE
    // =====================================================

    private void showThankYouPage() {

        String[] imagePaths = {
                "/image/Thank_You.png",
                "/image/Thank_You.jpg",
                "/image/Thank_You.jpeg"
        };

        Image thankYouImage = null;

        for (String imagePath : imagePaths) {
            var imageUrl = getClass().getResource(imagePath);

            if (imageUrl != null) {
                thankYouImage = new Image(imageUrl.toExternalForm(), true);
                break;
            }
        }

        if (thankYouImage == null) {
            System.err.println("Thank You image not found in /image/");
            return;
        }

        final Image finalThankYouImage = thankYouImage;

        /*
         * The Thank You artwork is 3:2 while a maximized desktop window
         * is usually much wider.  Therefore it is NOT possible to both
         * show the complete artwork and fill the whole window with the
         * same image without either cropping or stretching it.
         *
         * We keep the complete original image visible in the foreground
         * and use a soft enlarged copy behind it to fill the screen.
         * This gives a full-screen presentation without distortion.
         */
        ImageView backgroundView = new ImageView(finalThankYouImage);
        backgroundView.setPreserveRatio(true);
        backgroundView.setSmooth(true);
        backgroundView.setOpacity(0.28);

        StackPane backgroundLayer = new StackPane(backgroundView);
        backgroundLayer.setStyle("-fx-background-color:#050505;");
        backgroundLayer.setAlignment(Pos.CENTER);

        Runnable resizeBackground = () -> {
            double width = backgroundLayer.getWidth();
            double height = backgroundLayer.getHeight();

            if (width <= 0 || height <= 0) {
                return;
            }

            double imageRatio = finalThankYouImage.getWidth()
                    / finalThankYouImage.getHeight();
            double containerRatio = width / height;

            if (containerRatio > imageRatio) {
                backgroundView.setFitWidth(width);
                backgroundView.setFitHeight(width / imageRatio);
            } else {
                backgroundView.setFitHeight(height);
                backgroundView.setFitWidth(height * imageRatio);
            }
        };

        backgroundLayer.widthProperty().addListener(
                (obs, oldValue, newValue) -> resizeBackground.run()
        );
        backgroundLayer.heightProperty().addListener(
                (obs, oldValue, newValue) -> resizeBackground.run()
        );

        /*
         * Foreground image: CONTAIN behaviour.
         * The complete Thank You artwork is always visible.
         * No stretching and no cropping.
         */
        ImageView imageView = new ImageView(finalThankYouImage);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        StackPane imageLayer = new StackPane(imageView);
        imageLayer.setAlignment(Pos.CENTER);
        imageLayer.setMouseTransparent(true);

        Runnable resizeForeground = () -> {
            double width = imageLayer.getWidth();
            double height = imageLayer.getHeight();

            if (width <= 0 || height <= 0) {
                return;
            }

            double imageRatio = finalThankYouImage.getWidth()
                    / finalThankYouImage.getHeight();
            double containerRatio = width / height;

            if (containerRatio > imageRatio) {
                imageView.setFitHeight(height);
                imageView.setFitWidth(height * imageRatio);
            } else {
                imageView.setFitWidth(width);
                imageView.setFitHeight(width / imageRatio);
            }
        };

        imageLayer.widthProperty().addListener(
                (obs, oldValue, newValue) -> resizeForeground.run()
        );
        imageLayer.heightProperty().addListener(
                (obs, oldValue, newValue) -> resizeForeground.run()
        );

        // Button backButton = createButton(
        //         "Back",
        //         true,
        //         14
        // );

        // backButton.setPrefWidth(100);
        // backButton.setPrefHeight(46);

        // /* Bottom-left, independent of image size. */
        // StackPane.setAlignment(backButton, Pos.BOTTOM_LEFT);
        // StackPane.setMargin(
        //         backButton,
        //         new Insets(0, 0, 22, 22)
        // );

        // backButton.setOnAction(e -> {
        //     e.consume();
        //     showLandingPage(appMainStage);
        // });

        StackPane imageRoot = new StackPane(
                backgroundLayer,
                imageLayer
                //backButton
        );
        imageRoot.setStyle("-fx-background-color:#050505;");

        Scene thankYouScene = new Scene(
                imageRoot,
                Color.BLACK
        );

        appMainStage.setScene(thankYouScene);
        appMainStage.setTitle("RoadGuardian - Thank You");
        appMainStage.setMaximized(true);
        appMainStage.show();
        appMainStage.toFront();

        Platform.runLater(() -> {
            resizeBackground.run();
            resizeForeground.run();
        });
    }

    // =====================================================
    // LOGIN PAGE
    // =====================================================

    private void openLoginPage() {

        LoginPage page =
                new LoginPage();


        Scene scene =
                new Scene(
                        page.getView()
                );


        appMainStage.setScene(
                scene
        );


        appMainStage.setTitle(
                "RoadGuardian - Login"
        );


        appMainStage.setMaximized(
                true
        );


        appMainStage.show();
    }


    // =====================================================
    // USER REGISTER PAGE
    // =====================================================

    private void openRegisterPage() {

        RegisterPage page =
                new RegisterPage();


        Scene scene =
                new Scene(
                        page.getView()
                );


        appMainStage.setScene(
                scene
        );


        appMainStage.setTitle(
                "RoadGuardian - Register"
        );


        appMainStage.setMaximized(
                true
        );


        appMainStage.show();
    }


    // =====================================================
    // MECHANIC REGISTER
    // =====================================================

    private void openMechanicRegisterPage() {

        RegisterPage page =
                new RegisterPage(
                        "Mechanic"
                );


        Scene scene =
                new Scene(
                        page.getView()
                );


        appMainStage.setScene(
                scene
        );


        appMainStage.setTitle(
                "RoadGuardian - Mechanic Registration"
        );


        appMainStage.setMaximized(
                true
        );


        appMainStage.show();
    }


    // =====================================================
    // CREATE BUTTON
    // =====================================================

    private Button createButton(
            String text,
            boolean orange,
            int fontSize
    ) {

        Button button =
                new Button(
                        text
                );


        button.setStyle(
                orange
                        ? orangeStyle(fontSize)
                        : blueStyle(fontSize)
        );


        // =================================================
        // MOUSE ENTER
        // =================================================

        button.setOnMouseEntered(
                e -> {

                    button.setStyle(
                            orange
                                    ? orangeHoverStyle(fontSize)
                                    : blueHoverStyle(fontSize)
                    );


                    ScaleTransition scale =
                            new ScaleTransition(
                                    Duration.millis(150),
                                    button
                            );


                    scale.setToX(
                            1.05
                    );


                    scale.setToY(
                            1.05
                    );


                    scale.play();
                }
        );


        // =================================================
        // MOUSE EXIT
        // =================================================

        button.setOnMouseExited(
                e -> {

                    button.setStyle(
                            orange
                                    ? orangeStyle(fontSize)
                                    : blueStyle(fontSize)
                    );


                    ScaleTransition scale =
                            new ScaleTransition(
                                    Duration.millis(150),
                                    button
                            );


                    scale.setToX(
                            1.0
                    );


                    scale.setToY(
                            1.0
                    );


                    scale.play();
                }
        );


        return button;
    }


    // =====================================================
    // ORANGE BUTTON
    // =====================================================

    private String orangeStyle(
            int size
    ) {

        return
                "-fx-background-color:#F59E0B;" +
                        "-fx-background-radius:8px;" +
                        "-fx-text-fill:white;" +
                        "-fx-font-family:'Segoe UI';" +
                        "-fx-font-size:" + size + "px;" +
                        "-fx-font-weight:bold;" +
                        "-fx-padding:12px 30px;" +
                        "-fx-cursor:hand;";
    }


    // =====================================================
    // ORANGE HOVER
    // =====================================================

    private String orangeHoverStyle(
            int size
    ) {

        return
                "-fx-background-color:#D97706;" +
                        "-fx-background-radius:8px;" +
                        "-fx-text-fill:white;" +
                        "-fx-font-family:'Segoe UI';" +
                        "-fx-font-size:" + size + "px;" +
                        "-fx-font-weight:bold;" +
                        "-fx-padding:12px 30px;" +
                        "-fx-cursor:hand;";
    }


    // =====================================================
    // BLUE / OUTLINE BUTTON
    // =====================================================

    private String blueStyle(
            int size
    ) {

        return
                "-fx-background-color:rgba(20,20,20,0.78);" +
                        "-fx-border-color:#F59E0B;" +
                        "-fx-border-width:1.5px;" +
                        "-fx-border-radius:8px;" +
                        "-fx-background-radius:8px;" +
                        "-fx-text-fill:#F59E0B;" +
                        "-fx-font-family:'Segoe UI';" +
                        "-fx-font-size:" + size + "px;" +
                        "-fx-font-weight:bold;" +
                        "-fx-padding:12px 30px;" +
                        "-fx-cursor:hand;";
    }


    // =====================================================
    // BLUE HOVER
    // =====================================================

    private String blueHoverStyle(
            int size
    ) {

        return
                "-fx-background-color:#F59E0B;" +
                        "-fx-border-color:#F59E0B;" +
                        "-fx-border-width:1.5px;" +
                        "-fx-border-radius:8px;" +
                        "-fx-background-radius:8px;" +
                        "-fx-text-fill:white;" +
                        "-fx-font-family:'Segoe UI';" +
                        "-fx-font-size:" + size + "px;" +
                        "-fx-font-weight:bold;" +
                        "-fx-padding:12px 30px;" +
                        "-fx-cursor:hand;";
    }


    // =====================================================
    // FEATURE CARD
    // =====================================================

    private VBox createFeature(
            String title,
            String description,
            String accentColor
    ) {

        Label titleLabel =
                new Label(
                        title
                );


        titleLabel.setStyle(
                "-fx-font-family:'Segoe UI';" +
                        "-fx-font-size:16px;" +
                        "-fx-font-weight:bold;" +
                        "-fx-text-fill:#F3F4F6;"
        );


        Label descriptionLabel =
                new Label(
                        description
                );


        descriptionLabel.setWrapText(
                true
        );


        descriptionLabel.setMaxWidth(
                220
        );


        descriptionLabel.setStyle(
                "-fx-font-family:'Segoe UI';" +
                        "-fx-font-size:13px;" +
                        "-fx-text-fill:#D4D4D8;"
        );


        Text accent =
                new Text(
                        "━━━━"
                );


        accent.setStyle(
                "-fx-fill:" + accentColor + ";" +
                        "-fx-font-weight:bold;"
        );


        VBox box =
                new VBox(
                        10,
                        titleLabel,
                        accent,
                        descriptionLabel
                );


        box.setAlignment(
                Pos.CENTER
        );


        box.setPrefWidth(
                230
        );


        box.setMinHeight(
                140
        );


        box.setPadding(
                new Insets(
                        18
                )
        );


        box.setStyle(
                "-fx-background-color:rgba(20,20,20,0.82);" +
                        "-fx-background-radius:12px;" +
                        "-fx-border-color:rgba(245,158,11,0.25);" +
                        "-fx-border-radius:12px;"
        );


        return box;
    }
}