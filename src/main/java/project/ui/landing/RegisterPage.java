package project.ui.landing;

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
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import project.controller.RegisterController;

public class RegisterPage {

    private final BorderPane root;
    private final String role;
    private final RegisterController controller;

    public RegisterPage() {
        this("User");
    }

    public RegisterPage(String role) {
        this.role = role == null || role.equalsIgnoreCase("Mechanic")
                ? "Mechanic" : "User";
        this.controller = new RegisterController();
        this.root = new BorderPane();
        project.ui.theme.DarkTheme.apply(root);
        createPage();
    }

    private void createPage() {

        // Background
        Image bg = new Image(
                "file:src\\main\\java\\project\\resources\\images\\register.png"
        );

        root.setBackground(new Background(new BackgroundImage(
                bg,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(100, 100, false, false, false, true)
        )));
        root.setBackground(
                new Background(
                        new BackgroundFill(
                                Color.web("#0F0F0F"),
                                CornerRadii.EMPTY,
                                Insets.EMPTY
                        )
                )
        );

        // Back
        Button back = button("← Back", 105, 40,
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #F3F4F6;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;");

        addBackHover(back);

        HBox top = new HBox(back);
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(18, 30, 5, 30));

        // Logo
        Text road = text("Road", 30, "#F3F4F6", true);
        Text guardian = text("Guardian", 30, "#F59E0B", true);

        HBox logo = new HBox(road, guardian);
        logo.setAlignment(Pos.CENTER);

        // Heading
        Text heading = text("Create Your Account", 30, "#F3F4F6", true);

        Text subtitle = text(
                role.equals("Mechanic")
                        ? "Create your RoadGuardian mechanic account."
                        : "Join RoadGuardian and make every journey safer.",
                14, "#A1A1AA", false
        );

        // Fields
        Label nameLabel = label("Full Name");
        TextField name = field("Enter your full name");

        Label emailLabel = label("Email Address");
        TextField email = field("Enter your email");

        Label phoneLabel = label("Phone Number");
        TextField phone = field("Enter your phone number");

        Label passwordLabel = label("Password");
        PasswordField password = passwordField("Create a password");

        Label confirmLabel = label("Confirm Password");
        PasswordField confirm = passwordField("Confirm your password");

        // Register button
        Button register = button(
                "Create Account",
                520,
                46,
                "-fx-background-color: #F59E0B;" +
                "-fx-background-radius: 9px;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;"
        );

        addRegisterHover(register);

        // Login
        Text loginText = text(
                "Already have an account?",
                13, "#A1A1AA", false
        );

        Button login = button(
                "Login", 70, 30,
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #F59E0B;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;"
        );

        addLinkHover(login);

        HBox loginBox = new HBox(5, loginText, login);
        loginBox.setAlignment(Pos.CENTER);

        // Form
        VBox form = new VBox(
                6,
                nameLabel, name,
                emailLabel, email,
                phoneLabel, phone,
                passwordLabel, password,
                confirmLabel, confirm,
                register,
                loginBox
        );

        form.setAlignment(Pos.CENTER);
        form.setPrefWidth(520);
        form.setMaxWidth(520);

        // Card
        VBox card = new VBox(
                14,
                logo,
                heading,
                subtitle,
                form
        );

        card.setAlignment(Pos.CENTER);
        card.setPrefSize(650, 690);
        card.setMinSize(650, 690);
        card.setMaxSize(650, 690);
        card.setPadding(new Insets(25, 55, 22, 55));
        card.setStyle(
                "-fx-background-color: rgba(26,26,26,0.94);" +
                "-fx-background-radius: 24px;"
        );

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(245, 158, 11, 0.18));
        shadow.setRadius(25);
        shadow.setSpread(0.08);
        card.setEffect(shadow);

        // Animated border
        Rectangle border = new Rectangle(650, 690);
        border.setArcWidth(48);
        border.setArcHeight(48);
        border.setFill(Color.TRANSPARENT);
        border.setStroke(Color.rgb(245, 158, 11, 0.75));
        border.setStrokeWidth(2.5);
        border.getStrokeDashArray().setAll(18.0, 9.0, 4.0, 9.0);
        border.setMouseTransparent(true);

        DropShadow glow = new DropShadow();
        glow.setColor(Color.rgb(245, 158, 11, 0.65));
        glow.setRadius(10);
        glow.setSpread(0.08);
        border.setEffect(glow);

        Timeline borderAnimation = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(border.strokeDashOffsetProperty(), 0)
                ),
                new KeyFrame(
                        Duration.seconds(2.2),
                        new KeyValue(border.strokeDashOffsetProperty(), -60)
                )
        );
        borderAnimation.setCycleCount(Timeline.INDEFINITE);
        borderAnimation.play();

        Timeline glowAnimation = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(glow.radiusProperty(), 8)
                ),
                new KeyFrame(
                        Duration.seconds(1.4),
                        new KeyValue(glow.radiusProperty(), 22)
                ),
                new KeyFrame(
                        Duration.seconds(2.8),
                        new KeyValue(glow.radiusProperty(), 8)
                )
        );
        glowAnimation.setCycleCount(Timeline.INDEFINITE);
        glowAnimation.play();

        StackPane cardStack = new StackPane(border, card);
        cardStack.setPrefSize(650, 690);
        cardStack.setMinSize(650, 690);
        cardStack.setMaxSize(650, 690);

        // Safety panel
        Text safetyTitle = text(
                "Your Safety. Our Priority.",
                27, "#F3F4F6", true
        );

        HBox safetyHeader = new HBox(
                15,
                createLine(),
                safetyTitle,
                createLine()
        );
        safetyHeader.setAlignment(Pos.CENTER);

        VBox safetyCards = new VBox(
                15,
                safetyCard("✓", "Your Safety", "Our Priority"),
                safetyCard("⚙", "Expert", "Mechanics"),
                safetyCard("◉", "24/7", "Support"),
                safetyCard("◆", "Safe &", "Reliable")
        );
        safetyCards.setAlignment(Pos.CENTER);

        VBox info = new VBox(25, safetyHeader, safetyCards);
        info.setAlignment(Pos.TOP_CENTER);
        info.setPrefWidth(260);
        info.setMaxWidth(260);
        info.setPadding(new Insets(20, 10, 20, 10));

        HBox main = new HBox(55, info, cardStack);
        main.setAlignment(Pos.CENTER);
        main.setPadding(new Insets(5, 40, 5, 40));

        // Footer
        Text footerText = text(
                "© 2026 RoadGuardian. All rights reserved.",
                12, "#A1A1AA", false
        );

        HBox footer = new HBox(footerText);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(9));
        footer.setStyle(
                "-fx-background-color: rgba(22,22,22,0.94);" +
                "-fx-border-color: rgba(51,51,51,0.95);" +
                "-fx-border-width: 1 0 0 0;"
        );

        // Actions
        register.setOnAction(e -> register(
                name.getText(),
                email.getText(),
                phone.getText(),
                password.getText(),
                confirm.getText()
        ));

        login.setOnAction(e -> openLoginPage());
        back.setOnAction(e -> goBack());

        root.setTop(top);
        root.setCenter(main);
        root.setBottom(footer);
    }

    // =====================================================
    // REGISTER
    // =====================================================

    private void register(
            String name,
            String email,
            String phone,
            String password,
            String confirm
    ) {

        String result = controller.register(
                name,
                email,
                phone,
                role,
                password,
                confirm
        );

        if ("SUCCESS".equals(result)) {

            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Registration Successful",
                    "Your RoadGuardian " + role +
                    " account has been created successfully."
            );

            openLoginPage();

        } else {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Registration",
                    result
            );
        }
    }

    // =====================================================
    // LOGIN PAGE
    // =====================================================

    private void openLoginPage() {

        LoginPage page = new LoginPage();

        Scene scene = new Scene(
                page.getView(),
                LandingPage.appMainStage.getWidth(),
                LandingPage.appMainStage.getHeight()
        );

        LandingPage.appMainStage.setScene(scene);
        LandingPage.appMainStage.setTitle("RoadGuardian - Login");
    }

    // =====================================================
    // BACK
    // =====================================================

    private void goBack() {

        LandingPage page = new LandingPage();

        Scene scene = page.createPage();

        LandingPage.appMainStage.setScene(scene);
        LandingPage.appMainStage.setTitle("RoadGuardian");
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
    // UI HELPERS
    // =====================================================

    private Text text(
            String value,
            double size,
            String color,
            boolean bold
    ) {

        Text t = new Text(value);

        t.setStyle(
                "-fx-font-family: 'Segoe UI';" +
                "-fx-font-size: " + size + "px;" +
                "-fx-font-weight: " +
                (bold ? "bold" : "normal") + ";" +
                "-fx-fill: " + color + ";"
        );

        return t;
    }

    private Label label(String value) {

        Label l = new Label(value);

        l.setStyle(
                "-fx-font-family: 'Segoe UI';" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #F3F4F6;"
        );

        return l;
    }

    private TextField field(String prompt) {

        TextField f = new TextField();
        f.setPromptText(prompt);
        styleField(f);
        return f;
    }

    private PasswordField passwordField(String prompt) {

        PasswordField f = new PasswordField();
        f.setPromptText(prompt);
        styleField(f);
        return f;
    }

    private void styleField(TextField field) {

        field.setPrefSize(520, 39);
        field.setMaxWidth(520);

        field.setStyle(
                "-fx-background-color: rgba(26,26,26,0.94);" +
                "-fx-background-radius: 8px;" +
                "-fx-border-color: #F59E0B;" +
                "-fx-border-radius: 8px;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-font-size: 14px;" +
                "-fx-text-fill: #F3F4F6;" +
                "-fx-prompt-text-fill: #A1A1AA;" +
                "-fx-padding: 0 12px;"
        );
    }

    private Button button(
            String text,
            double width,
            double height,
            String style
    ) {

        Button b = new Button(text);
        b.setPrefSize(width, height);
        b.setStyle(
                style +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-cursor: hand;"
        );

        return b;
    }

    // =====================================================
    // SAFETY CARD
    // =====================================================

    private VBox safetyCard(
            String iconText,
            String line1,
            String line2
    ) {

        Circle circle = new Circle(27);
        circle.setFill(Color.rgb(36, 36, 36, 0.75));
        circle.setStroke(Color.rgb(245, 158, 11, 0.65));
        circle.setStrokeWidth(1.8);

        Text icon = text(
                iconText,
                22,
                "#F59E0B",
                true
        );

        StackPane iconBox = new StackPane(circle, icon);

        DropShadow glow = new DropShadow();
        glow.setColor(Color.rgb(245, 158, 11, 0.75));
        glow.setRadius(7);
        glow.setSpread(0.10);
        circle.setEffect(glow);

        Timeline glowAnimation = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(glow.radiusProperty(), 6),
                        new KeyValue(circle.opacityProperty(), 0.65)
                ),
                new KeyFrame(
                        Duration.seconds(1.2),
                        new KeyValue(glow.radiusProperty(), 19),
                        new KeyValue(circle.opacityProperty(), 1)
                ),
                new KeyFrame(
                        Duration.seconds(2.4),
                        new KeyValue(glow.radiusProperty(), 6),
                        new KeyValue(circle.opacityProperty(), 0.65)
                )
        );
        glowAnimation.setCycleCount(Timeline.INDEFINITE);
        glowAnimation.play();

        TranslateTransition floating = new TranslateTransition(
                Duration.seconds(2.2),
                iconBox
        );
        floating.setToY(-4);
        floating.setAutoReverse(true);
        floating.setCycleCount(TranslateTransition.INDEFINITE);
        floating.play();

        Text first = text(line1, 14, "#F3F4F6", true);
        Text second = text(line2, 14, "#F59E0B", true);

        VBox words = new VBox(1, first, second);
        words.setAlignment(Pos.CENTER);

        VBox card = new VBox(14, iconBox, words);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefSize(240, 70);
        card.setMaxWidth(240);
        card.setPadding(new Insets(8, 12, 8, 12));

        card.setStyle(
                "-fx-background-color: rgba(36,36,36,0.82);" +
                "-fx-background-radius: 14px;" +
                "-fx-border-color: rgba(245, 158, 11,0.35);" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 14px;"
        );

        card.setOnMouseEntered(e -> scale(card, 1.04));
        card.setOnMouseExited(e -> scale(card, 1.0));

        return card;
    }

    private Region createLine() {

        Region line = new Region();

        line.setPrefSize(45, 2);
        line.setMinSize(45, 2);
        line.setMaxSize(45, 2);

        line.setStyle(
                "-fx-background-color: #F59E0B;" +
                "-fx-background-radius: 5px;"
        );

        ScaleTransition scale = new ScaleTransition(
                Duration.seconds(1.4),
                line
        );

        scale.setFromX(0.35);
        scale.setToX(1);
        scale.setAutoReverse(true);
        scale.setCycleCount(ScaleTransition.INDEFINITE);
        scale.play();

        return line;
    }

    // =====================================================
    // HOVERS
    // =====================================================

    private void scale(Region node, double value) {

        ScaleTransition scale = new ScaleTransition(
                Duration.millis(180),
                node
        );

        scale.setToX(value);
        scale.setToY(value);
        scale.play();
    }

    private void addRegisterHover(Button button) {

        button.setOnMouseEntered(e -> {
            button.setStyle(
                    "-fx-background-color: #D97706;" +
                    "-fx-background-radius: 9px;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-family: 'Segoe UI';" +
                    "-fx-font-size: 16px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-cursor: hand;"
            );
            scale(button, 1.03);
        });

        button.setOnMouseExited(e -> {
            button.setStyle(
                    "-fx-background-color: #F59E0B;" +
                    "-fx-background-radius: 9px;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-family: 'Segoe UI';" +
                    "-fx-font-size: 16px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-cursor: hand;"
            );
            scale(button, 1);
        });
    }

    private void addBackHover(Button button) {

        button.setOnMouseEntered(e -> {
            button.setStyle(
                    "-fx-background-color: #D97706;" +
                    "-fx-background-radius: 9px;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-family: 'Segoe UI';" +
                    "-fx-font-size: 14px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-cursor: hand;"
            );
            scale(button, 1.05);
        });

        button.setOnMouseExited(e -> {
            button.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-text-fill: #F3F4F6;" +
                    "-fx-font-family: 'Segoe UI';" +
                    "-fx-font-size: 14px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-cursor: hand;"
            );
            scale(button, 1);
        });
    }

    private void addLinkHover(Button button) {

        button.setOnMouseEntered(e -> {
            button.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-text-fill: #F59E0B;" +
                    "-fx-font-family: 'Segoe UI';" +
                    "-fx-font-size: 13px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-cursor: hand;"
            );
            scale(button, 1.05);
        });

        button.setOnMouseExited(e -> {
            button.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-text-fill: #F59E0B;" +
                    "-fx-font-family: 'Segoe UI';" +
                    "-fx-font-size: 13px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-cursor: hand;"
            );
            scale(button, 1);
        });
    }

    // =====================================================
    // GET VIEW
    // =====================================================

    public Parent getView() {
        return root;
    }
}
