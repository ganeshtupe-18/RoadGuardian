package project.ui.admin.Mechanics;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
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
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import project.model.Mechanic;
import project.util.IconUtil;

public class MechanicDetailsPage {

    // =========================================================
    // AQUA MIST THEME
    // =========================================================

    private static final String BG = "#0F0F0F";
    private static final String SURFACE = "#1A1A1A";
    private static final String SECONDARY = "#242424";

    private static final String HEADING = "#F3F4F6";
    private static final String TEXT = "#A1A1AA";

    private static final String BLUE = "#F59E0B";
    private static final String GREEN = "#22C55E";
    private static final String RED = "#EF4444";
   
 

    private static final String ORANGE = "#F59E0B";
 

    private static final String BORDER = "#F59E0B";

    // =========================================================
    // DATA
    // =========================================================

    private final Mechanic mechanic;

    private final Runnable backAction;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public MechanicDetailsPage(
            Mechanic mechanic,
            Runnable backAction
    ) {

        this.mechanic = mechanic;

        this.backAction = backAction;
    }

    // =========================================================
    // GET VIEW
    // =========================================================

    public VBox getView() {

        VBox root =
                new VBox();

        root.setPadding(
                new Insets(
                        28,
                        32,
                        32,
                        32
                )
        );

        root.setStyle(
                "-fx-background-color: " +
                BG +
                ";"
        );

        VBox header =
                createHeader();

        VBox profileCard =
                createProfileCard();

        VBox contactCard =
                createContactCard();

        VBox professionalCard =
                createProfessionalCard();

        VBox accountCard =
                createAccountCard();

        VBox documentsCard =
                createDocumentsCard();

        VBox bottomActions =
                createBottomActions();

        VBox content =
                new VBox(18);

        content.getChildren().addAll(
                profileCard,
                contactCard,
                professionalCard,
                accountCard,
                documentsCard
        );

        ScrollPane scrollPane =
                new ScrollPane(
                        content
                );

        scrollPane.setFitToWidth(
                true
        );

        scrollPane.setPannable(
                true
        );

        scrollPane.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        scrollPane.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED
        );

        scrollPane.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-background: transparent;"
        );

        VBox.setVgrow(
                scrollPane,
                Priority.ALWAYS
        );

        root.getChildren().addAll(
                header,
                scrollPane,
                bottomActions
        );

        return root;
    }

    // =========================================================
    // HEADER
    // =========================================================

    private VBox createHeader() {

        VBox header =
                new VBox(10);

        HBox top =
                new HBox();

        top.setAlignment(
                Pos.CENTER_LEFT
        );

        Button backButton =
                new Button(
                        "← Back to Mechanics"
                );

        styleBackButton(
                backButton
        );

        backButton.setOnAction(
                event -> {

                    if (
                            backAction != null
                    ) {

                        backAction.run();
                    }
                }
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Label pageTag =
                new Label(
                        "MECHANIC PROFILE"
                );

        pageTag.setTextFill(
                Color.web(
                        BLUE
                )
        );

        pageTag.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        12
                )
        );

        pageTag.setPadding(
                new Insets(
                        7,
                        12,
                        7,
                        12
                )
        );

        pageTag.setStyle(
                "-fx-background-color: " +
                BLUE +
                "18;" +
                "-fx-background-radius: 20;"
        );

        top.getChildren().addAll(
                backButton,
                spacer,
                pageTag
        );

        Label title =
                new Label(
                        "Mechanic Details"
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
                        "Complete profile and professional information"
                );

        subtitle.setTextFill(
                Color.web(
                        TEXT
                )
        );

        subtitle.setFont(
                Font.font(
                        "Arial",
                        15
                )
        );

        header.getChildren().addAll(
                top,
                title,
                subtitle
        );

        header.setPadding(
                new Insets(
                        0,
                        0,
                        18,
                        0
                )
        );

        return header;
    }

    // =========================================================
    // PROFILE CARD
    // =========================================================

    private VBox createProfileCard() {

        VBox card =
                createCard();

        HBox profile =
                new HBox(20);

        profile.setAlignment(
                Pos.CENTER_LEFT
        );

        StackPane avatar =
                createAvatar(
                        safe(
                                mechanic.getName()
                        )
                );

        VBox identity =
                new VBox(7);

        Label name =
                new Label(
                        safe(
                                mechanic.getName()
                        )
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
                        26
                )
        );

        Label specialization =
                new Label(
                        safe(
                                mechanic.getSpecialization()
                        )
                );

        specialization.setTextFill(
                Color.web(
                        TEXT
                )
        );

        specialization.setFont(
                Font.font(
                        "Arial",
                        15
                )
        );

        HBox badges =
                new HBox(9);

        Label status =
                createStatusBadge(
                        safe(
                                mechanic.getStatus()
                        )
                );

        Label role =
                new Label(
                        "RoadGuardian Mechanic"
                );

        role.setTextFill(
                Color.web(
                        BLUE
                )
        );

        role.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        12
                )
        );

        role.setPadding(
                new Insets(
                        7,
                        12,
                        7,
                        12
                )
        );

        role.setStyle(
                "-fx-background-color: " +
                BLUE +
                "18;" +
                "-fx-background-radius: 20;"
        );

        badges.getChildren().addAll(
                status,
                role
        );

        identity.getChildren().addAll(
                name,
                specialization,
                badges
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        VBox idBox =
                createHighlightBox(
                        "MECHANIC ID",
                        safe(
                                mechanic.getMechanicId()
                        ),
                        BLUE
                );

        profile.getChildren().addAll(
                avatar,
                identity,
                spacer,
                idBox
        );

        card.getChildren().add(
                profile
        );

        return card;
    }

    // =========================================================
    // CONTACT CARD
    // =========================================================

    private VBox createContactCard() {

        VBox card =
                createCard();

        VBox heading =
                createSectionHeading(
                        "Contact Information",
                        "Basic contact and location details"
                );

        GridPane grid =
                createDetailsGrid();

        grid.add(
                createDetailBox(
                        "EMAIL",
                        mechanic.getEmail()
                ),
                0,
                0
        );

        grid.add(
                createDetailBox(
                        "PHONE",
                        mechanic.getPhone()
                ),
                1,
                0
        );

        grid.add(
                createDetailBox(
                        "CITY",
                        mechanic.getCity()
                ),
                0,
                1
        );

        grid.add(
                createDetailBox(
                        "ADDRESS",
                        mechanic.getAddress()
                ),
                1,
                1
        );

        card.getChildren().addAll(
                heading,
                grid
        );

        return card;
    }

    // =========================================================
    // PROFESSIONAL CARD
    // =========================================================

    private VBox createProfessionalCard() {

        VBox card =
                createCard();

        VBox heading =
                createSectionHeading(
                        "Professional Information",
                        "Mechanic skills and service-related information"
                );

        GridPane grid =
                createDetailsGrid();

        grid.add(
                createDetailBox(
                        "SPECIALIZATION",
                        mechanic.getSpecialization()
                ),
                0,
                0
        );

        grid.add(
                createDetailBox(
                        "EXPERIENCE",
                        mechanic.getExperience()
                ),
                1,
                0
        );

        grid.add(
                createDetailBox(
                        "SERVICE STATUS",
                        mechanic.getStatus()
                ),
                0,
                1
        );

        grid.add(
                createDetailBox(
                        "MECHANIC ID",
                        mechanic.getMechanicId()
                ),
                1,
                1
        );

        card.getChildren().addAll(
                heading,
                grid
        );

        return card;
    }

    // =========================================================
    // ACCOUNT CARD
    // =========================================================

    private VBox createAccountCard() {

        VBox card =
                createCard();

        VBox heading =
                createSectionHeading(
                        "Account Information",
                        "RoadGuardian account registration information"
                );

        GridPane grid =
                createDetailsGrid();

        grid.add(
                createDetailBox(
                        "ACCOUNT STATUS",
                        mechanic.getStatus()
                ),
                0,
                0
        );

        grid.add(
                createDetailBox(
                        "REGISTERED ON",
                        mechanic.getCreatedAt()
                ),
                1,
                0
        );

        grid.add(
                createDetailBox(
                        "PROFILE ID",
                        mechanic.getMechanicId()
                ),
                0,
                1
        );

        grid.add(
                createDetailBox(
                        "ROLE",
                        "Mechanic"
                ),
                1,
                1
        );

        card.getChildren().addAll(
                heading,
                grid
        );

        return card;
    }

    // =========================================================
    // DOCUMENTS CARD
    // =========================================================

    private VBox createDocumentsCard() {

        VBox card =
                createCard();

        VBox heading =
                createSectionHeading(
                        "Documents & Verification",
                        "Mechanic documents and verification records"
                );

        VBox documents =
                new VBox(10);

        documents.getChildren().addAll(
                createDocumentRow(
                        "Driving License",
                        "Verification document",
                        "Available",
                        GREEN
                ),

                createDocumentRow(
                        "Mechanic Certificate",
                        "Professional qualification",
                        "Available",
                        GREEN
                ),

                createDocumentRow(
                        "Identity Proof",
                        "Government identification",
                        "Available",
                        GREEN
                ),

                createDocumentRow(
                        "Address Proof",
                        "Residential verification",
                        "Pending",
                        ORANGE
                )
        );

        Label note =
                new Label(
                        "Document file integration can be connected with Firebase Storage later."
                );

        note.setTextFill(
                Color.web(
                        TEXT
                )
        );

        note.setFont(
                Font.font(
                        "Arial",
                        13
                )
        );

        note.setWrapText(
                true
        );

        card.getChildren().addAll(
                heading,
                documents,
                note
        );

        return card;
    }

    // =========================================================
    // DOCUMENT ROW
    // =========================================================

    private HBox createDocumentRow(
            String title,
            String subtitle,
            String status,
            String statusColor
    ) {

        HBox row =
                new HBox(14);

        row.setAlignment(
                Pos.CENTER_LEFT
        );

        row.setPadding(
                new Insets(
                        14,
                        15,
                        14,
                        15
                )
        );

        row.setStyle(
                "-fx-background-color: " +
                SECONDARY +
                ";" +
                "-fx-border-color: " +
                BORDER +
                ";" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;"
        );

        Label icon =
                new Label(
                        "DOC"
                );

        icon.setTextFill(
                Color.web(
                        BLUE
                )
        );

        icon.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        11
                )
        );

        icon.setPadding(
                new Insets(
                        8,
                        9,
                        8,
                        9
                )
        );

        icon.setStyle(
                "-fx-background-color: " +
                BLUE +
                "18;" +
                "-fx-background-radius: 8;"
        );

        VBox textBox =
                new VBox(3);

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
                        14
                )
        );

        Label subtitleLabel =
                new Label(
                        subtitle
                );

        subtitleLabel.setTextFill(
                Color.web(
                        TEXT
                )
        );

        subtitleLabel.setFont(
                Font.font(
                        "Arial",
                        12
                )
        );

        textBox.getChildren().addAll(
                titleLabel,
                subtitleLabel
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Label statusLabel =
                new Label(
                        status
                );

        statusLabel.setTextFill(
                Color.web(
                        statusColor
                )
        );

        statusLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        12
                )
        );

        statusLabel.setPadding(
                new Insets(
                        7,
                        11,
                        7,
                        11
                )
        );

        statusLabel.setStyle(
                "-fx-background-color: " +
                statusColor +
                "18;" +
                "-fx-background-radius: 20;"
        );

        Button viewButton =
                new Button(
                        "View"
                );

        styleSmallButton(
                viewButton
        );

        row.getChildren().addAll(
                icon,
                textBox,
                spacer,
                statusLabel,
                viewButton
        );

        return row;
    }

    // =========================================================
    // BOTTOM ACTIONS
    // =========================================================

    private VBox createBottomActions() {

        VBox wrapper =
                new VBox();

        wrapper.setPadding(
                new Insets(
                        18,
                        0,
                        0,
                        0
                )
        );

        HBox actions =
                new HBox(12);

        actions.setAlignment(
                Pos.CENTER_RIGHT
        );

        Button back =
                new Button(
                        "← Back to Mechanics"
                );

        styleBackButton(
                back
        );

        back.setOnAction(
                event -> {

                    if (
                            backAction != null
                    ) {

                        backAction.run();
                    }
                }
        );

        actions.getChildren().add(
                back
        );

        wrapper.getChildren().add(
                actions
        );

        return wrapper;
    }

    // =========================================================
    // CARD
    // =========================================================

    private VBox createCard() {

        VBox card =
                new VBox(16);

        card.setPadding(
                new Insets(
                        21
                )
        );

        card.setStyle(
                "-fx-background-color: " +
                SURFACE +
                ";" +
                "-fx-border-color: " +
                BORDER +
                ";" +
                "-fx-border-radius: 14;" +
                "-fx-background-radius: 14;"
        );

        return card;
    }

    // =========================================================
    // SECTION HEADING
    // =========================================================

    private VBox createSectionHeading(
            String title,
            String subtitle
    ) {

        VBox box =
                new VBox(4);

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
                        19
                )
        );

        Label subtitleLabel =
                new Label(
                        subtitle
                );

        subtitleLabel.setTextFill(
                Color.web(
                        TEXT
                )
        );

        subtitleLabel.setFont(
                Font.font(
                        "Arial",
                        13
                )
        );

        box.getChildren().addAll(
                titleLabel,
                subtitleLabel
        );

        return box;
    }

    // =========================================================
    // DETAILS GRID
    // =========================================================

    private GridPane createDetailsGrid() {

        GridPane grid =
                new GridPane();

        grid.setHgap(
                14
        );

        grid.setVgap(
                14
        );

        ColumnConstraints first =
                new ColumnConstraints();

        first.setPercentWidth(
                50
        );

        ColumnConstraints second =
                new ColumnConstraints();

        second.setPercentWidth(
                50
        );

        grid.getColumnConstraints().addAll(
                first,
                second
        );

        return grid;
    }

    // =========================================================
    // DETAIL BOX
    // =========================================================

    private VBox createDetailBox(
            String title,
            String value
    ) {

        VBox box =
                new VBox(5);

        box.setPadding(
                new Insets(
                        13,
                        15,
                        13,
                        15
                )
        );

        box.setMinHeight(
                70
        );

        box.setStyle(
                "-fx-background-color: " +
                SECONDARY +
                ";" +
                "-fx-border-color: " +
                BORDER +
                ";" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;"
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
                        11
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
                        HEADING
                )
        );

        valueLabel.setFont(
                Font.font(
                        "Arial",
                        15
                )
        );

        valueLabel.setWrapText(
                true
        );

        box.getChildren().addAll(
                titleLabel,
                valueLabel
        );

        return box;
    }

    // =========================================================
    // HIGHLIGHT BOX
    // =========================================================

    private VBox createHighlightBox(
            String title,
            String value,
            String color
    ) {

        VBox box =
                new VBox(4);

        box.setAlignment(
                Pos.CENTER
        );

        box.setPadding(
                new Insets(
                        12,
                        18,
                        12,
                        18
                )
        );

        box.setStyle(
                "-fx-background-color: " +
                color +
                "12;" +
                "-fx-border-color: " +
                color +
                "40;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;"
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
                        10
                )
        );

        Label valueLabel =
                new Label(
                        value
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
                        13
                )
        );

        box.getChildren().addAll(
                titleLabel,
                valueLabel
        );

        return box;
    }

    // =========================================================
    // AVATAR
    // =========================================================

    private StackPane createAvatar(
            String name
    ) {

        Circle circle =
                new Circle(
                        34,
                        Color.web(
                                BLUE
                        )
                );

        var mechanicIcon =
                IconUtil.createMechanicIcon(
                        0.9,
                        "#FFFFFF"
                );

        StackPane avatar =
                new StackPane(
                        circle,
                        mechanicIcon
                );

        avatar.setMinSize(
                68,
                68
        );

        avatar.setPrefSize(
                68,
                68
        );

        avatar.setMaxSize(
                68,
                68
        );

        return avatar;
    }

    // =========================================================
    // STATUS
    // =========================================================

    private Label createStatusBadge(
            String status
    ) {

        Label badge =
                new Label(
                        status.equals("-")
                                ? "Unknown"
                                : status
                );

        badge.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        12
                )
        );

        badge.setPadding(
                new Insets(
                        7,
                        12,
                        7,
                        12
                )
        );

        if (
                status.equalsIgnoreCase(
                        "Active"
                )
        ) {

            badge.setTextFill(
                    Color.web(
                            GREEN
                    )
            );

            badge.setStyle(
                    "-fx-background-color: " +
                    GREEN +
                    "18;" +
                    "-fx-background-radius: 20;"
            );

        } else {

            badge.setTextFill(
                    Color.web(
                            RED
                    )
            );

            badge.setStyle(
                    "-fx-background-color: " +
                    RED +
                    "18;" +
                    "-fx-background-radius: 20;"
            );
        }

        return badge;
    }

    // =========================================================
    // BUTTON STYLE
    // =========================================================

    private void styleBackButton(
            Button button
    ) {

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

        button.setPadding(
                new Insets(
                        9,
                        15,
                        9,
                        15
                )
        );

        button.setCursor(
                Cursor.HAND
        );

        String normal =
                "-fx-background-color: " +
                SURFACE +
                ";" +
                "-fx-border-color: " +
                BORDER +
                ";" +
                "-fx-border-radius: 9;" +
                "-fx-background-radius: 9;";

        String hover =
                "-fx-background-color: " +
                SECONDARY +
                ";" +
                "-fx-border-color: " +
                BLUE +
                ";" +
                "-fx-border-radius: 9;" +
                "-fx-background-radius: 9;";

        button.setStyle(
                normal
        );

        button.setOnMouseEntered(
                event ->
                        button.setStyle(
                                hover
                        )
        );

        button.setOnMouseExited(
                event ->
                        button.setStyle(
                                normal
                        )
        );
    }

    private void styleSmallButton(
            Button button
    ) {

        button.setTextFill(
                Color.web(
                        BLUE
                )
        );

        button.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        12
                )
        );

        button.setPadding(
                new Insets(
                        7,
                        12,
                        7,
                        12
                )
        );

        button.setCursor(
                Cursor.HAND
        );

        button.setStyle(
                "-fx-background-color: " +
                SURFACE +
                ";" +
                "-fx-border-color: " +
                BORDER +
                ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;"
        );
    }

    // =========================================================
    // SAFE
    // =========================================================

    private String safe(
            String value
    ) {

        return value == null ||
                value.isBlank()
                ? "-"
                : value;
    }
}