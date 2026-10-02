package project.ui.landing;

import project.controller.ForgotPasswordController;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class ForgotPasswordPage {

    private final BorderPane root;
    private final ForgotPasswordController controller;

    public ForgotPasswordPage() {
        root = new BorderPane();
        project.ui.theme.DarkTheme.apply(root);
        controller = new ForgotPasswordController();
        createPage();
    }

    private void createPage() {

        Image backgroundImage = new Image(
                "file:src\\main\\java\\project\\resources\\images\\carBg.png"
        );

        BackgroundImage background = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(
                        1.0, 1.0, true, true, false, true
                )
        );

        root.setBackground(new Background(background));
        root.setBackground(
                new Background(
                        new javafx.scene.layout.BackgroundFill(
                                Color.web("#0F0F0F"),
                                javafx.scene.layout.CornerRadii.EMPTY,
                                Insets.EMPTY
                        )
                )
        );

        // ================= BACK =================

        Button backButton = new Button("← Back");

        backButton.setPrefSize(115, 44);

        backButton.setStyle(
                "-fx-background-color:#F59E0B;" +
                "-fx-background-radius:10px;" +
                "-fx-text-fill:white;" +
                "-fx-font-family:'Segoe UI';" +
                "-fx-font-size:15px;" +
                "-fx-font-weight:bold;" +
                "-fx-cursor:hand;"
        );

        addBackHover(backButton);

        HBox topBar = new HBox(backButton);
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPadding(new Insets(20, 25, 10, 25));

        // ================= LOGO =================

        Text road = new Text("Road");
        road.setStyle(
                "-fx-font-family:'Segoe UI';" +
                "-fx-font-size:30px;" +
                "-fx-font-weight:bold;" +
                "-fx-fill:#F3F4F6;"
        );

        Text guardian = new Text("Guardian");
        guardian.setStyle(
                "-fx-font-family:'Segoe UI';" +
                "-fx-font-size:30px;" +
                "-fx-font-weight:bold;" +
                "-fx-fill:#F59E0B;"
        );

        HBox logo = new HBox(road, guardian);
        logo.setAlignment(Pos.CENTER);

        // ================= HEADING =================

        Text heading = new Text("Forgot Password?");
        heading.setStyle(
                "-fx-font-family:'Segoe UI';" +
                "-fx-font-size:30px;" +
                "-fx-font-weight:bold;" +
                "-fx-fill:#F3F4F6;"
        );

        Text subtitle = new Text(
                "Enter your registered email to reset your password."
        );

        subtitle.setStyle(
                "-fx-font-family:'Segoe UI';" +
                "-fx-font-size:14px;" +
                "-fx-fill:#A1A1AA;"
        );

        // ================= EMAIL =================

        Label emailLabel = new Label("Email Address");

        emailLabel.setStyle(
                "-fx-font-family:'Segoe UI';" +
                "-fx-font-size:14px;" +
                "-fx-font-weight:bold;" +
                "-fx-text-fill:#F3F4F6;"
        );

        TextField emailField = new TextField();

        emailField.setPromptText(
                "Enter your registered email"
        );

        emailField.setPrefSize(400, 44);

        emailField.setStyle(
                "-fx-background-color:rgba(36,36,36,0.97);" +
                "-fx-background-radius:8px;" +
                "-fx-border-color:#F59E0B;" +
                "-fx-border-radius:8px;" +
                "-fx-font-family:'Segoe UI';" +
                "-fx-font-size:14px;" +
                "-fx-text-fill:#F3F4F6;" +
                "-fx-prompt-text-fill:#A1A1AA;" +
                "-fx-padding:0 12px;"
        );

        // ================= RESET =================

        Button resetButton = new Button("Send Reset Link");

        resetButton.setPrefSize(400, 46);

        resetButton.setStyle(
                "-fx-background-color:#F59E0B;" +
                "-fx-background-radius:9px;" +
                "-fx-text-fill:white;" +
                "-fx-font-family:'Segoe UI';" +
                "-fx-font-size:16px;" +
                "-fx-font-weight:bold;" +
                "-fx-cursor:hand;"
        );

        addResetHover(resetButton);

        // ================= LOGIN =================

        Text rememberText = new Text(
                "Remember your password?"
        );

        rememberText.setStyle(
                "-fx-font-family:'Segoe UI';" +
                "-fx-font-size:13px;" +
                "-fx-fill:#A1A1AA;"
        );

        Button loginButton = new Button("Login");

        loginButton.setStyle(
                "-fx-background-color:transparent;" +
                "-fx-text-fill:#F59E0B;" +
                "-fx-font-family:'Segoe UI';" +
                "-fx-font-size:13px;" +
                "-fx-font-weight:bold;" +
                "-fx-cursor:hand;"
        );

        addLinkHover(loginButton);

        HBox loginBox = new HBox(
                5,
                rememberText,
                loginButton
        );

        loginBox.setAlignment(Pos.CENTER);

        // ================= FORM =================

        VBox form = new VBox(
                10,
                emailLabel,
                emailField,
                resetButton,
                loginBox
        );

        form.setAlignment(Pos.CENTER);
        form.setPrefWidth(400);
        form.setMaxWidth(400);

        // ================= CARD =================

        VBox card = new VBox(
                20,
                logo,
                heading,
                subtitle,
                form
        );

        card.setAlignment(Pos.CENTER);
        card.setPrefSize(500, 500);
        card.setMinSize(500, 500);
        card.setMaxSize(500, 500);

        card.setPadding(
                new Insets(40, 50, 40, 50)
        );

        card.setStyle(
                "-fx-background-color:rgba(26,26,26,0.94);" +
                "-fx-background-radius:22px;"
        );

        DropShadow cardShadow = new DropShadow();

        cardShadow.setColor(
                Color.rgb(245, 158, 11, 0.20)
        );

        cardShadow.setRadius(25);
        cardShadow.setSpread(0.08);

        card.setEffect(cardShadow);

        // ================= BORDER =================

        Rectangle border = new Rectangle(500, 500);

        border.setArcWidth(44);
        border.setArcHeight(44);
        border.setFill(Color.TRANSPARENT);

        border.setStroke(
                Color.rgb(245, 158, 11, 0.75)
        );

        border.setStrokeWidth(2.5);

        border.getStrokeDashArray().setAll(
                18.0, 9.0, 4.0, 9.0
        );

        border.setMouseTransparent(true);

        DropShadow glow = new DropShadow();

        glow.setColor(
                Color.rgb(245, 158, 11, 0.65)
        );

        glow.setRadius(10);
        glow.setSpread(0.08);

        border.setEffect(glow);

        Timeline borderAnimation = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(
                                border.strokeDashOffsetProperty(),
                                0
                        )
                ),
                new KeyFrame(
                        Duration.seconds(2.2),
                        new KeyValue(
                                border.strokeDashOffsetProperty(),
                                -60
                        )
                )
        );

        borderAnimation.setCycleCount(
                Timeline.INDEFINITE
        );

        borderAnimation.play();

        Timeline glowAnimation = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(
                                glow.radiusProperty(),
                                8
                        )
                ),
                new KeyFrame(
                        Duration.seconds(1.4),
                        new KeyValue(
                                glow.radiusProperty(),
                                20
                        )
                ),
                new KeyFrame(
                        Duration.seconds(2.8),
                        new KeyValue(
                                glow.radiusProperty(),
                                8
                        )
                )
        );

        glowAnimation.setCycleCount(
                Timeline.INDEFINITE
        );

        glowAnimation.play();

        StackPane cardStack = new StackPane(
                border,
                card
        );

        cardStack.setPrefSize(500, 500);
        cardStack.setMinSize(500, 500);
        cardStack.setMaxSize(500, 500);

        // ================= CENTER =================

        HBox center = new HBox(cardStack);

        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(20));

        // ================= FOOTER =================

        Text footerText = new Text(
                "© 2026 RoadGuardian. All rights reserved."
        );

        footerText.setStyle(
                "-fx-font-family:'Segoe UI';" +
                "-fx-font-size:12px;" +
                "-fx-fill:#A1A1AA;"
        );

        HBox footer = new HBox(footerText);

        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(10));

        footer.setStyle(
                "-fx-background-color:rgba(22,22,22,0.94);" +
                "-fx-border-color:rgba(51,51,51,0.95);" +
                "-fx-border-width:1 0 0 0;"
        );

        // ================= ACTIONS =================

        resetButton.setOnAction(
                e -> resetPassword(emailField.getText())
        );

        loginButton.setOnAction(
                e -> openLoginPage()
        );

        backButton.setOnAction(
                e -> goBack()
        );

        root.setTop(topBar);
        root.setCenter(center);
        root.setBottom(footer);
    }

    // =====================================================
    // RESET PASSWORD
    // =====================================================

    private void resetPassword(String email) {

        String result = controller.resetPassword(email);

        if ("SUCCESS".equals(result)) {

            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Password Reset",
                    "Your account was found successfully.\n\n"
                            + "Password reset functionality will be "
                            + "connected with Firebase Authentication."
            );

            openLoginPage();

        } else {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Password Reset",
                    result
            );
        }
    }

    // =====================================================
    // ALERT
    // =====================================================

    private void showAlert(
            Alert.AlertType type,
            String title,
            String message
    ) {

        Alert alert = new Alert(type);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    // =====================================================
    // LOGIN PAGE
    // =====================================================

    private void openLoginPage() {

        LoginPage loginPage = new LoginPage();

        Scene scene = new Scene(
                loginPage.getView(),
                LandingPage.appMainStage.getWidth(),
                LandingPage.appMainStage.getHeight()
        );

        LandingPage.appMainStage.setScene(scene);

        LandingPage.appMainStage.setTitle(
                "RoadGuardian - Login"
        );
    }

    // =====================================================
    // LANDING PAGE
    // =====================================================

    private void goBack() {

        LandingPage landingPage =
                new LandingPage();

        Scene scene =
                landingPage.createPage();

        LandingPage.appMainStage.setScene(scene);

        LandingPage.appMainStage.setTitle(
                "RoadGuardian"
        );
    }

    // =====================================================
    // RESET HOVER
    // =====================================================

    private void addResetHover(Button button) {

        button.setOnMouseEntered(e -> {

            button.setStyle(
                    "-fx-background-color:#D97706;" +
                    "-fx-background-radius:9px;" +
                    "-fx-text-fill:white;" +
                    "-fx-font-family:'Segoe UI';" +
                    "-fx-font-size:16px;" +
                    "-fx-font-weight:bold;" +
                    "-fx-cursor:hand;"
            );

            ScaleTransition scale =
                    new ScaleTransition(
                            Duration.millis(150),
                            button
                    );

            scale.setToX(1.03);
            scale.setToY(1.03);
            scale.play();
        });

        button.setOnMouseExited(e -> {

            button.setStyle(
                    "-fx-background-color:#F59E0B;" +
                    "-fx-background-radius:9px;" +
                    "-fx-text-fill:white;" +
                    "-fx-font-family:'Segoe UI';" +
                    "-fx-font-size:16px;" +
                    "-fx-font-weight:bold;" +
                    "-fx-cursor:hand;"
            );

            ScaleTransition scale =
                    new ScaleTransition(
                            Duration.millis(150),
                            button
                    );

            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });
    }

    // =====================================================
    // BACK HOVER
    // =====================================================

    private void addBackHover(Button button) {

        button.setOnMouseEntered(e -> {

            button.setStyle(
                    "-fx-background-color:#D97706;" +
                    "-fx-background-radius:9px;" +
                    "-fx-text-fill:white;" +
                    "-fx-font-family:'Segoe UI';" +
                    "-fx-font-size:14px;" +
                    "-fx-font-weight:bold;" +
                    "-fx-cursor:hand;"
            );

            ScaleTransition scale =
                    new ScaleTransition(
                            Duration.millis(150),
                            button
                    );

            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();
        });

        button.setOnMouseExited(e -> {

            button.setStyle(
                    "-fx-background-color:#F59E0B;" +
                    "-fx-background-radius:10px;" +
                    "-fx-text-fill:white;" +
                    "-fx-font-family:'Segoe UI';" +
                    "-fx-font-size:15px;" +
                    "-fx-font-weight:bold;" +
                    "-fx-cursor:hand;"
            );

            ScaleTransition scale =
                    new ScaleTransition(
                            Duration.millis(150),
                            button
                    );

            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });
    }

    // =====================================================
    // LINK HOVER
    // =====================================================

    private void addLinkHover(Button button) {

        button.setOnMouseEntered(e -> {

            button.setStyle(
                    "-fx-background-color:transparent;" +
                    "-fx-text-fill:#F59E0B;" +
                    "-fx-font-family:'Segoe UI';" +
                    "-fx-font-size:13px;" +
                    "-fx-font-weight:bold;" +
                    "-fx-cursor:hand;"
            );

            ScaleTransition scale =
                    new ScaleTransition(
                            Duration.millis(120),
                            button
                    );

            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();
        });

        button.setOnMouseExited(e -> {

            button.setStyle(
                    "-fx-background-color:transparent;" +
                    "-fx-text-fill:#F59E0B;" +
                    "-fx-font-family:'Segoe UI';" +
                    "-fx-font-size:13px;" +
                    "-fx-font-weight:bold;" +
                    "-fx-cursor:hand;"
            );

            ScaleTransition scale =
                    new ScaleTransition(
                            Duration.millis(120),
                            button
                    );

            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });
    }

    // =====================================================
    // GET VIEW
    // =====================================================

    public Parent getView() {
        return root;
    }
}
