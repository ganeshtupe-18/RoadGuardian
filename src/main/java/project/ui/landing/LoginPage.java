package project.ui.landing;

import project.app.WindowManager;
import project.controller.LoginController;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import javafx.scene.effect.DropShadow;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import javafx.scene.paint.Color;

import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import javafx.scene.text.Text;

import javafx.util.Duration;


public class LoginPage {

    // =====================================================
    // CARD SIZE
    // =====================================================

    private static final double CARD_WIDTH = 480;
    private static final double CARD_HEIGHT = 470;

    // =====================================================
    // ROOTS
    // =====================================================

    /*
     * pageRoot
     *   ├── Background Image
     *   └── Login UI
     *
     * Background image is always kept as the bottom layer.
     */
    private final StackPane pageRoot;

    private final BorderPane root;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public LoginPage() {

        pageRoot = new StackPane();

        root = new BorderPane();

        /*
         * IMPORTANT:
         *
         * DarkTheme.apply(root) is intentionally NOT used here.
         *
         * It can apply a background through CSS and hide
         * the actual login background image.
         */

        createPage();
    }

    // =====================================================
    // CREATE PAGE
    // =====================================================

    private void createPage() {

        // =====================================================
        // BACKGROUND IMAGE
        // =====================================================

        ImageView backgroundImageView =
                createBackgroundImage();

        // =====================================================
        // ROOT CONTENT
        // =====================================================

        root.setStyle(
                "-fx-background-color: transparent;"
        );

        /*
         * Background image = first layer
         * Login UI        = second layer
         */
        pageRoot.getChildren().addAll(
                backgroundImageView,
                root
        );

        // =====================================================
        // BACK BUTTON
        // =====================================================

        Button backButton =
                new Button("← Back");

        backButton.setPrefWidth(125);
        backButton.setPrefHeight(46);

        backButton.setStyle(
                "-fx-background-color: #F59E0B;" +
                "-fx-background-radius: 10px;" +
                "-fx-text-fill: white;" +
                "-fx-font-family: 'Arial';" +
                "-fx-font-size: 15px;" +
                "-fx-font-weight: bold;" +
                "-fx-cursor: hand;"
        );

        addBackHover(backButton);

        HBox topBar =
                new HBox(
                        backButton
                );

        topBar.setAlignment(
                Pos.TOP_LEFT
        );

        topBar.setPadding(
                new Insets(
                        20,
                        25,
                        10,
                        25
                )
        );

        topBar.setStyle(
                "-fx-background-color: transparent;"
        );

        // =====================================================
        // LOGO
        // =====================================================

        Text roadText =
                new Text("Road");

        roadText.setStyle(
                "-fx-font-family: 'Arial';" +
                "-fx-font-size: 28px;" +
                "-fx-font-weight: bold;" +
                "-fx-fill: #F8FAFC;"
        );

        Text guardianText =
                new Text("Guardian");

        guardianText.setStyle(
                "-fx-font-family: 'Arial';" +
                "-fx-font-size: 28px;" +
                "-fx-font-weight: bold;" +
                "-fx-fill: #F59E0B;"
        );

        HBox logo =
                new HBox(
                        roadText,
                        guardianText
                );

        logo.setAlignment(
                Pos.CENTER
        );

        // =====================================================
        // HEADING
        // =====================================================

        Text heading =
                new Text(
                        "Welcome Back"
                );

        heading.setStyle(
                "-fx-font-family: 'Arial';" +
                "-fx-font-size: 28px;" +
                "-fx-font-weight: bold;" +
                "-fx-fill: #F8FAFC;"
        );

        // =====================================================
        // SUBTITLE
        // =====================================================

        Text subtitle =
                new Text(
                        "Login to continue your journey with RoadGuardian"
                );

        subtitle.setStyle(
                "-fx-font-family: 'Arial';" +
                "-fx-font-size: 13px;" +
                "-fx-fill: #CBD5E1;"
        );

        // =====================================================
        // EMAIL LABEL
        // =====================================================

        Label emailLabel =
                new Label(
                        "Email Address"
                );

        styleLabel(
                emailLabel
        );

        // =====================================================
        // EMAIL FIELD
        // =====================================================

        TextField emailField =
                new TextField();

        emailField.setPromptText(
                "Enter your email"
        );

        styleField(
                emailField
        );

        // =====================================================
        // PASSWORD LABEL
        // =====================================================

        Label passwordLabel =
                new Label(
                        "Password"
                );

        styleLabel(
                passwordLabel
        );

        // =====================================================
        // PASSWORD FIELD
        // =====================================================

        PasswordField passwordField =
                new PasswordField();

        passwordField.setPromptText(
                "Enter your password"
        );

        styleField(
                passwordField
        );

        // =====================================================
        // FORGOT PASSWORD
        // =====================================================

        Button forgotPasswordButton =
                new Button(
                        "Forgot Password?"
                );

        forgotPasswordButton.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #FBBF24;" +
                "-fx-font-family: 'Arial';" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-cursor: hand;"
        );

        HBox forgotBox =
                new HBox(
                        forgotPasswordButton
                );

        forgotBox.setAlignment(
                Pos.CENTER_RIGHT
        );

        forgotBox.setPrefWidth(380);
        forgotBox.setMaxWidth(380);

        // =====================================================
        // LOGIN BUTTON
        // =====================================================

        Button loginButton =
                new Button(
                        "Login"
                );

        loginButton.setPrefWidth(380);
        loginButton.setMaxWidth(380);

        loginButton.setPrefHeight(44);

        loginButton.setStyle(
                "-fx-background-color: #F59E0B;" +
                "-fx-background-radius: 9px;" +
                "-fx-text-fill: white;" +
                "-fx-font-family: 'Arial';" +
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                "-fx-cursor: hand;"
        );

        // =====================================================
        // REGISTER TEXT
        // =====================================================

        Text accountText =
                new Text(
                        "Don't have an account?"
                );

        accountText.setStyle(
                "-fx-font-family: 'Arial';" +
                "-fx-font-size: 13px;" +
                "-fx-fill: #CBD5E1;"
        );

        // =====================================================
        // REGISTER BUTTON
        // =====================================================

        Button registerButton =
                new Button(
                        "Create Account"
                );

        registerButton.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #FBBF24;" +
                "-fx-font-family: 'Arial';" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-cursor: hand;"
        );

        HBox registerBox =
                new HBox(
                        5,
                        accountText,
                        registerButton
                );

        registerBox.setAlignment(
                Pos.CENTER
        );

        // =====================================================
        // LOGIN FORM
        // =====================================================

        VBox loginForm =
                new VBox(
                        6,

                        emailLabel,
                        emailField,

                        passwordLabel,
                        passwordField,

                        forgotBox,

                        loginButton,

                        registerBox
                );

        loginForm.setAlignment(
                Pos.CENTER_LEFT
        );

        loginForm.setPrefWidth(380);
        loginForm.setMaxWidth(380);

        // =====================================================
        // LOGIN CARD
        // =====================================================

        VBox loginCard =
                new VBox(
                        10,

                        logo,
                        heading,
                        subtitle,
                        loginForm
                );

        loginCard.setAlignment(
                Pos.CENTER
        );

        loginCard.setPrefWidth(
                CARD_WIDTH
        );

        loginCard.setMinWidth(
                CARD_WIDTH
        );

        loginCard.setMaxWidth(
                CARD_WIDTH
        );

        loginCard.setPrefHeight(
                CARD_HEIGHT
        );

        loginCard.setMinHeight(
                CARD_HEIGHT
        );

        loginCard.setMaxHeight(
                CARD_HEIGHT
        );

        loginCard.setPadding(
                new Insets(
                        18,
                        45,
                        18,
                        45
                )
        );

        loginCard.setStyle(
                "-fx-background-color: rgba(15,23,42,0.96);" +
                "-fx-background-radius: 20px;" +
                "-fx-border-color: rgba(245,158,11,0.18);" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 20px;"
        );

        // =====================================================
        // CARD SHADOW
        // =====================================================

        DropShadow cardShadow =
                new DropShadow();

        cardShadow.setColor(
                Color.rgb(
                        245,
                        158,
                        11,
                        0.20
                )
        );

        cardShadow.setRadius(25);

        cardShadow.setSpread(0.08);

        loginCard.setEffect(
                cardShadow
        );

        // =====================================================
        // ANIMATED BORDER
        // =====================================================

        Rectangle animatedBorder =
                new Rectangle(
                        CARD_WIDTH,
                        CARD_HEIGHT
                );

        animatedBorder.setArcWidth(40);

        animatedBorder.setArcHeight(40);

        animatedBorder.setFill(
                Color.TRANSPARENT
        );

        animatedBorder.setStroke(
                Color.rgb(
                        245,
                        158,
                        11,
                        0.80
                )
        );

        animatedBorder.setStrokeWidth(
                2.5
        );

        animatedBorder.getStrokeDashArray()
                .setAll(
                        18.0,
                        9.0,
                        4.0,
                        9.0
                );

        animatedBorder.setMouseTransparent(
                true
        );

        // =====================================================
        // BORDER GLOW
        // =====================================================

        DropShadow borderGlow =
                new DropShadow();

        borderGlow.setColor(
                Color.rgb(
                        245,
                        158,
                        11,
                        0.65
                )
        );

        borderGlow.setRadius(8);

        borderGlow.setSpread(0.10);

        animatedBorder.setEffect(
                borderGlow
        );

        // =====================================================
        // BORDER ANIMATION
        // =====================================================

        Timeline borderAnimation =
                new Timeline(

                        new KeyFrame(
                                Duration.ZERO,

                                new KeyValue(
                                        animatedBorder
                                                .strokeDashOffsetProperty(),
                                        0
                                )
                        ),

                        new KeyFrame(
                                Duration.seconds(2.2),

                                new KeyValue(
                                        animatedBorder
                                                .strokeDashOffsetProperty(),
                                        -60
                                )
                        )
                );

        borderAnimation.setCycleCount(
                Timeline.INDEFINITE
        );

        borderAnimation.play();

        // =====================================================
        // BORDER GLOW ANIMATION
        // =====================================================

        Timeline cardGlowAnimation =
                new Timeline(

                        new KeyFrame(
                                Duration.ZERO,

                                new KeyValue(
                                        borderGlow.radiusProperty(),
                                        7
                                )
                        ),

                        new KeyFrame(
                                Duration.seconds(1.4),

                                new KeyValue(
                                        borderGlow.radiusProperty(),
                                        18
                                )
                        ),

                        new KeyFrame(
                                Duration.seconds(2.8),

                                new KeyValue(
                                        borderGlow.radiusProperty(),
                                        7
                                )
                        )
                );

        cardGlowAnimation.setCycleCount(
                Timeline.INDEFINITE
        );

        cardGlowAnimation.play();

        // =====================================================
        // CARD STACK
        // =====================================================

        StackPane loginCardStack =
                new StackPane();

        loginCardStack.setPrefWidth(
                CARD_WIDTH
        );

        loginCardStack.setMinWidth(
                CARD_WIDTH
        );

        loginCardStack.setMaxWidth(
                CARD_WIDTH
        );

        loginCardStack.setPrefHeight(
                CARD_HEIGHT
        );

        loginCardStack.setMinHeight(
                CARD_HEIGHT
        );

        loginCardStack.setMaxHeight(
                CARD_HEIGHT
        );

        /*
         * Move login card slightly towards right.
         */
        loginCardStack.setTranslateX(85);

        loginCardStack.getChildren().addAll(
                animatedBorder,
                loginCard
        );

        // =====================================================
        // SAFETY TITLE
        // =====================================================

        Text safetyTitle =
                new Text(
                        "Your Safety. Our Priority."
                );

        safetyTitle.setStyle(
                "-fx-font-family: 'Arial';" +
                "-fx-font-size: 30px;" +
                "-fx-font-weight: bold;" +
                "-fx-fill: #F8FAFC;"
        );

        Region leftLine =
                createLine();

        Region rightLine =
                createLine();

        HBox safetyHeader =
                new HBox(
                        18,
                        leftLine,
                        safetyTitle,
                        rightLine
                );

        safetyHeader.setAlignment(
                Pos.CENTER
        );

        // =====================================================
        // SERVICE CARD 1
        // =====================================================

        VBox roadside =
                createServiceCard(
                        "⚒",
                        "Roadside",
                        "Assistance"
                );

        // =====================================================
        // SERVICE CARD 2
        // =====================================================

        VBox mechanics =
                createServiceCard(
                        "⚙",
                        "Expert",
                        "Mechanics"
                );

        // =====================================================
        // SERVICE CARD 3
        // =====================================================

        VBox safe =
                createServiceCard(
                        "✓",
                        "Safe &",
                        "Reliable"
                );

        // =====================================================
        // SERVICE CARD 4
        // =====================================================

        VBox support =
                createServiceCard(
                        "◉",
                        "24/7",
                        "Support"
                );

        HBox serviceCards =
                new HBox(
                        18,
                        roadside,
                        mechanics,
                        safe,
                        support
                );

        serviceCards.setAlignment(
                Pos.CENTER
        );

        // =====================================================
        // RIGHT CONTENT
        // =====================================================

        VBox rightContent =
                new VBox(
                        28,
                        safetyHeader,
                        serviceCards
                );

        rightContent.setAlignment(
                Pos.TOP_CENTER
        );

        rightContent.setPrefWidth(700);

        rightContent.setMaxWidth(700);

        rightContent.setTranslateY(-35);

        rightContent.setTranslateX(-15);

        rightContent.setPadding(
                new Insets(
                        10,
                        20,
                        20,
                        20
                )
        );

        // =====================================================
        // MAIN CONTENT
        // =====================================================

        HBox mainContent =
                new HBox(
                        55,
                        loginCardStack,
                        rightContent
                );

        mainContent.setAlignment(
                Pos.CENTER
        );

        mainContent.setPadding(
                new Insets(
                        5,
                        35,
                        5,
                        35
                )
        );

        // =====================================================
        // FOOTER
        // =====================================================

        Text footerText =
                new Text(
                        "© 2026 RoadGuardian. All rights reserved."
                );

        footerText.setStyle(
                "-fx-font-family: 'Arial';" +
                "-fx-font-size: 12px;" +
                "-fx-fill: #CBD5E1;"
        );

        HBox footer =
                new HBox(
                        footerText
                );

        footer.setAlignment(
                Pos.CENTER
        );

        footer.setPadding(
                new Insets(8)
        );

        footer.setStyle(
                "-fx-background-color: rgba(15,23,42,0.78);" +
                "-fx-border-color: rgba(51,65,85,0.80);" +
                "-fx-border-width: 1 0 0 0;"
        );

        // =====================================================
        // HOVERS
        // =====================================================

        addLoginHover(
                loginButton
        );

        addBackHover(
                backButton
        );

        addLinkHover(
                registerButton
        );

        addLinkHover(
                forgotPasswordButton
        );

        // =====================================================
        // LOGIN ACTION
        // =====================================================

        loginButton.setOnAction(
                e -> {

                    LoginController controller =
                            new LoginController();

                    controller.login(
                            emailField.getText(),
                            passwordField.getText(),
                            this
                    );
                }
        );

        // =====================================================
        // REGISTER NAVIGATION
        // =====================================================

        registerButton.setOnAction(
                e -> openRegisterPage()
        );

        // =====================================================
        // FORGOT PASSWORD NAVIGATION
        // =====================================================

        forgotPasswordButton.setOnAction(
                e -> openForgotPasswordPage()
        );

        // =====================================================
        // BACK NAVIGATION
        // =====================================================

        backButton.setOnAction(
                e -> goBack()
        );

        // =====================================================
        // ROOT
        // =====================================================

        root.setTop(
                topBar
        );

        root.setCenter(
                mainContent
        );

        root.setBottom(
                footer
        );
    }

    // =====================================================
    // CREATE BACKGROUND IMAGE
    // =====================================================

    private ImageView createBackgroundImage() {

        try {

            java.net.URL imageUrl =
                    getClass().getResource(
                            "/image/login_bg.png"
                    );

            if (imageUrl == null) {

                System.out.println(
                        "Login Background: IMAGE NOT FOUND"
                );

                System.out.println(
                        "Expected resource:"
                );

                System.out.println(
                        "src/main/resources/image/login_bg.png"
                );

                return createFallbackBackground();
            }

            Image image =
                    new Image(
                            imageUrl.toExternalForm()
                    );

            System.out.println(
                    "Login Background Error: "
                            + image.isError()
            );

            if (image.isError()) {

                System.out.println(
                        "Login Background: FAILED TO LOAD IMAGE"
                );

                return createFallbackBackground();
            }

            ImageView imageView =
                    new ImageView(
                            image
                    );

            /*
             * Image fills complete application window.
             */
            imageView.setPreserveRatio(
                    false
            );

            imageView.setSmooth(
                    true
            );

            imageView.setCache(
                    true
            );

            /*
             * Bind image size to StackPane size.
             *
             * This is better than fixed 1600x900 because
             * the image automatically follows window size.
             */
            imageView.fitWidthProperty()
                    .bind(
                            pageRoot.widthProperty()
                    );

            imageView.fitHeightProperty()
                    .bind(
                            pageRoot.heightProperty()
                    );

            StackPane.setAlignment(
                    imageView,
                    Pos.CENTER
            );

            /*
             * Keep image clearly visible.
             */
            imageView.setOpacity(
                    1.0
            );

            System.out.println(
                    "Login Background: IMAGE LOADED SUCCESSFULLY"
            );

            return imageView;

        } catch (Exception exception) {

            System.out.println(
                    "Login Background: ERROR WHILE LOADING"
            );

            exception.printStackTrace();

            return createFallbackBackground();
        }
    }

    // =====================================================
    // FALLBACK BACKGROUND
    // =====================================================

    private ImageView createFallbackBackground() {

        /*
         * Fallback is only used when the image is missing.
         */
        root.setStyle(
                "-fx-background-color: #0B1120;"
        );

        return new ImageView();
    }

    // =====================================================
    // OPEN REGISTER PAGE
    // =====================================================

    private void openRegisterPage() {

        RegisterPage registerPage =
                new RegisterPage();

        WindowManager.prepareStage(
                LandingPage.appMainStage
        );

        Scene scene =
                WindowManager.createScene(
                        LandingPage.appMainStage,
                        registerPage.getView()
                );

        WindowManager.show(
                LandingPage.appMainStage,
                "RoadGuardian - Register",
                scene
        );
    }

    // =====================================================
    // OPEN FORGOT PASSWORD PAGE
    // =====================================================

    private void openForgotPasswordPage() {

        ForgotPasswordPage forgotPasswordPage =
                new ForgotPasswordPage();

        WindowManager.prepareStage(
                LandingPage.appMainStage
        );

        Scene scene =
                WindowManager.createScene(
                        LandingPage.appMainStage,
                        forgotPasswordPage.getView()
                );

        WindowManager.show(
                LandingPage.appMainStage,
                "RoadGuardian - Forgot Password",
                scene
        );
    }

    // =====================================================
    // GO BACK
    // =====================================================

    private void goBack() {

        LandingPage landingPage =
                new LandingPage();

        Scene scene =
                landingPage.createPage();

        WindowManager.prepareStage(
                LandingPage.appMainStage
        );

        WindowManager.show(
                LandingPage.appMainStage,
                "RoadGuardian",
                scene
        );
    }

    // =====================================================
    // SHOW ERROR
    // =====================================================

    public void showError(
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle(
                "Login Failed"
        );

        alert.setHeaderText(
                null
        );

        alert.setContentText(
                message
        );

        alert.showAndWait();
    }

    // =====================================================
    // SHOW SUCCESS
    // =====================================================

    public void showSuccess(
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(
                "Login Successful"
        );

        alert.setHeaderText(
                null
        );

        alert.setContentText(
                message
        );

        alert.showAndWait();
    }

    // =====================================================
    // GET MAIN STAGE
    // =====================================================

    public static javafx.stage.Stage getMainStage() {

        return LandingPage.appMainStage;
    }

    // =====================================================
    // LABEL STYLE
    // =====================================================

    private void styleLabel(
            Label label
    ) {

        label.setStyle(
                "-fx-font-family: 'Arial';" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #F8FAFC;"
        );
    }

    // =====================================================
    // FIELD STYLE
    // =====================================================

    private void styleField(
            TextField field
    ) {

        field.setPrefWidth(
                380
        );

        field.setMaxWidth(
                380
        );

        field.setPrefHeight(
                40
        );

        field.setStyle(
                "-fx-background-color: rgba(15,23,42,0.98);" +
                "-fx-background-radius: 8px;" +
                "-fx-border-color: #475569;" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 8px;" +
                "-fx-font-family: 'Arial';" +
                "-fx-font-size: 14px;" +
                "-fx-text-fill: #FFFFFF;" +
                "-fx-prompt-text-fill: #94A3B8;" +
                "-fx-padding: 0 12px;"
        );
    }

    // =====================================================
    // SERVICE CARD
    // =====================================================

    private VBox createServiceCard(
            String iconText,
            String line1,
            String line2
    ) {

        Circle iconCircle =
                new Circle(
                        42
                );

        iconCircle.setFill(
                Color.rgb(
                        36,
                        36,
                        36,
                        0.65
                )
        );

        iconCircle.setStroke(
                Color.rgb(
                        245,
                        158,
                        11,
                        0.75
                )
        );

        iconCircle.setStrokeWidth(
                2
        );

        Text icon =
                new Text(
                        iconText
                );

        icon.setStyle(
                "-fx-font-family: 'Arial';" +
                "-fx-font-size: 30px;" +
                "-fx-font-weight: bold;" +
                "-fx-fill: #F59E0B;"
        );

        StackPane iconContainer =
                new StackPane(
                        iconCircle,
                        icon
                );

        // =====================================================
        // GLOW
        // =====================================================

        DropShadow glow =
                new DropShadow();

        glow.setColor(
                Color.rgb(
                        245,
                        158,
                        11,
                        0.85
                )
        );

        glow.setRadius(
                8
        );

        glow.setSpread(
                0.15
        );

        iconCircle.setEffect(
                glow
        );

        Timeline glowAnimation =
                new Timeline(

                        new KeyFrame(
                                Duration.ZERO,

                                new KeyValue(
                                        glow.radiusProperty(),
                                        7
                                ),

                                new KeyValue(
                                        iconCircle.opacityProperty(),
                                        0.65
                                )
                        ),

                        new KeyFrame(
                                Duration.seconds(1.2),

                                new KeyValue(
                                        glow.radiusProperty(),
                                        20
                                ),

                                new KeyValue(
                                        iconCircle.opacityProperty(),
                                        1.0
                                )
                        ),

                        new KeyFrame(
                                Duration.seconds(2.4),

                                new KeyValue(
                                        glow.radiusProperty(),
                                        7
                                ),

                                new KeyValue(
                                        iconCircle.opacityProperty(),
                                        0.65
                                )
                        )
                );

        glowAnimation.setCycleCount(
                Timeline.INDEFINITE
        );

        glowAnimation.play();

        // =====================================================
        // FLOATING
        // =====================================================

        TranslateTransition floating =
                new TranslateTransition(
                        Duration.seconds(2.2),
                        iconContainer
                );

        floating.setFromY(
                0
        );

        floating.setToY(
                -5
        );

        floating.setAutoReverse(
                true
        );

        floating.setCycleCount(
                TranslateTransition.INDEFINITE
        );

        floating.play();

        // =====================================================
        // FIRST TEXT
        // =====================================================

        Text firstText =
                new Text(
                        line1
                );

        firstText.setStyle(
                "-fx-font-family: 'Arial';" +
                "-fx-font-size: 15px;" +
                "-fx-font-weight: bold;" +
                "-fx-fill: #F3F4F6;"
        );

        // =====================================================
        // SECOND TEXT
        // =====================================================

        Text secondText =
                new Text(
                        line2
                );

        secondText.setStyle(
                "-fx-font-family: 'Arial';" +
                "-fx-font-size: 15px;" +
                "-fx-font-weight: bold;" +
                "-fx-fill: #F3F4F6;"
        );

        VBox textBox =
                new VBox(
                        2,
                        firstText,
                        secondText
                );

        textBox.setAlignment(
                Pos.CENTER
        );

        // =====================================================
        // SERVICE CARD
        // =====================================================

        VBox card =
                new VBox(
                        14,
                        iconContainer,
                        textBox
                );

        card.setAlignment(
                Pos.CENTER
        );

        card.setPrefWidth(
                125
        );

        card.setMinWidth(
                125
        );

        card.setMaxWidth(
                125
        );

        card.setPrefHeight(
                190
        );

        card.setMinHeight(
                190
        );

        card.setMaxHeight(
                190
        );

        card.setPadding(
                new Insets(
                        10,
                        8,
                        10,
                        8
                )
        );

        card.setStyle(
                "-fx-background-color: rgba(15,23,42,0.38);" +
                "-fx-background-radius: 16px;" +
                "-fx-border-color: rgba(245,158,11,0.45);" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 16px;"
        );

        // =====================================================
        // HOVER
        // =====================================================

        card.setOnMouseEntered(
                e -> {

                    ScaleTransition scale =
                            new ScaleTransition(
                                    Duration.millis(180),
                                    card
                            );

                    scale.setToX(
                            1.06
                    );

                    scale.setToY(
                            1.06
                    );

                    scale.play();
                }
        );

        card.setOnMouseExited(
                e -> {

                    ScaleTransition scale =
                            new ScaleTransition(
                                    Duration.millis(180),
                                    card
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

        return card;
    }

    // =====================================================
    // TITLE LINE
    // =====================================================

    private Region createLine() {

        Region line =
                new Region();

        line.setPrefWidth(
                70
        );

        line.setMinWidth(
                70
        );

        line.setMaxWidth(
                70
        );

        line.setPrefHeight(
                2
        );

        line.setMinHeight(
                2
        );

        line.setMaxHeight(
                2
        );

        line.setStyle(
                "-fx-background-color: #F59E0B;" +
                "-fx-background-radius: 5px;"
        );

        ScaleTransition scale =
                new ScaleTransition(
                        Duration.seconds(1.4),
                        line
                );

        scale.setFromX(
                0.45
        );

        scale.setToX(
                1.0
        );

        scale.setAutoReverse(
                true
        );

        scale.setCycleCount(
                ScaleTransition.INDEFINITE
        );

        scale.play();

        return line;
    }

    // =====================================================
    // LOGIN BUTTON HOVER
    // =====================================================

    private void addLoginHover(
            Button button
    ) {

        button.setOnMouseEntered(
                e -> {

                    button.setStyle(
                            "-fx-background-color: #D97706;" +
                            "-fx-background-radius: 9px;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-family: 'Arial';" +
                            "-fx-font-size: 16px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-cursor: hand;"
                    );

                    ScaleTransition scale =
                            new ScaleTransition(
                                    Duration.millis(150),
                                    button
                            );

                    scale.setToX(
                            1.03
                    );

                    scale.setToY(
                            1.03
                    );

                    scale.play();
                }
        );

        button.setOnMouseExited(
                e -> {

                    button.setStyle(
                            "-fx-background-color: #F59E0B;" +
                            "-fx-background-radius: 9px;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-family: 'Arial';" +
                            "-fx-font-size: 16px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-cursor: hand;"
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
    }

    // =====================================================
    // BACK BUTTON HOVER
    // =====================================================

    private void addBackHover(
            Button button
    ) {

        button.setOnMouseEntered(
                e -> {

                    button.setStyle(
                            "-fx-background-color: #D97706;" +
                            "-fx-background-radius: 10px;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-family: 'Arial';" +
                            "-fx-font-size: 15px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-cursor: hand;"
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

        button.setOnMouseExited(
                e -> {

                    button.setStyle(
                            "-fx-background-color: #F59E0B;" +
                            "-fx-background-radius: 10px;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-family: 'Arial';" +
                            "-fx-font-size: 15px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-cursor: hand;"
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
    }

    // =====================================================
    // LINK HOVER
    // =====================================================

    private void addLinkHover(
            Button button
    ) {

        button.setOnMouseEntered(
                e -> {

                    button.setStyle(
                            "-fx-background-color: transparent;" +
                            "-fx-text-fill: #F59E0B;" +
                            "-fx-font-family: 'Arial';" +
                            "-fx-font-size: 13px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-cursor: hand;"
                    );

                    ScaleTransition scale =
                            new ScaleTransition(
                                    Duration.millis(120),
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

        button.setOnMouseExited(
                e -> {

                    button.setStyle(
                            "-fx-background-color: transparent;" +
                            "-fx-text-fill: #FBBF24;" +
                            "-fx-font-family: 'Arial';" +
                            "-fx-font-size: 13px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-cursor: hand;"
                    );

                    ScaleTransition scale =
                            new ScaleTransition(
                                    Duration.millis(120),
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
    }

    // =====================================================
    // GET VIEW
    // =====================================================

    public Parent getView() {

        return pageRoot;
    }
}