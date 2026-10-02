package project.ui.admin.Customers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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

import project.model.Customer;
import project.util.IconUtil;
import project.ui.admin.DashBoard.AdminSectionPage;

public class CustomerDetailsPage extends AdminSectionPage {

    // =========================================================
    // THEME
    // =========================================================

    private static final String BG = "#0F0F0F";
    private static final String SURFACE = "#1A1A1A";
    private static final String SECONDARY = "#242424";

    private static final String BLUE = "#F59E0B";
    private static final String BLUE_HOVER = "#F59E0B";
    private static final String GREEN = "#22C55E";
    private static final String RED = "#EF4444";
    private static final String ORANGE = "#F59E0B";
    private static final String PURPLE = "#F59E0B";

    private static final String HEADING = "#F3F4F6";
    private static final String TEXT = "#A1A1AA";
    private static final String BORDER = "#F59E0B";

    // =========================================================
    // CUSTOMER DATA
    // =========================================================

    private final Customer customer;
    private final int customerNumber;

    /*
     * Back button sathi callback.
     *
     * CustomerManagementPage madhun ha callback pass karaycha.
     */
    private final Runnable backAction;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public CustomerDetailsPage(
            Customer customer,
            int customerNumber,
            Runnable backAction
    ) {

        this.customer = customer;
        this.customerNumber = customerNumber;
        this.backAction = backAction;
    }

    // =========================================================
    // MAIN VIEW
    // =========================================================

    @Override
    public VBox getView() {

        VBox main =
                new VBox();

        main.setStyle(
                "-fx-background-color: " +
                BG +
                ";"
        );

        HBox header =
                createHeader();

        VBox content =
                new VBox(20);

        content.setPadding(
                new Insets(
                        25,
                        30,
                        35,
                        30
                )
        );

        content.getChildren().addAll(

                createProfileCard(),

                createOverview(),

                createPersonalInformation(),

                createActivitySection(),

                createDocumentsSection(),

                createQuickActions()
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

        main.getChildren().addAll(
                header,
                scrollPane
        );

        return main;
    }

    // =========================================================
    // HEADER
    // =========================================================

    private HBox createHeader() {

        HBox header =
                new HBox(15);

        header.setAlignment(
                Pos.CENTER_LEFT
        );

        header.setPadding(
                new Insets(
                        16,
                        25,
                        16,
                        25
                )
        );

        header.setStyle(
                "-fx-background-color: " +
                SURFACE +
                ";" +
                "-fx-border-color: " +
                BORDER +
                ";" +
                "-fx-border-width: 0 0 1 0;"
        );

        Button backButton =
                createButton(
                        "← Back to Customers",
                        BLUE
                );

        backButton.setOnAction(
                event -> {

                    if (backAction != null) {
                        backAction.run();
                    }
                }
        );

        Label title =
                new Label(
                        "Customer Details"
                );

        title.setTextFill(
                Color.web(HEADING)
        );

        title.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        23
                )
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Label brand =
                new Label(
                        "✓ RoadGuardian"
                );

        brand.setTextFill(
                Color.web(BLUE)
        );

        brand.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        16
                )
        );

        header.getChildren().addAll(
                backButton,
                title,
                spacer,
                brand
        );

        return header;
    }

    // =========================================================
    // PROFILE CARD
    // =========================================================

    private HBox createProfileCard() {

        HBox card =
                new HBox(20);

        card.setAlignment(
                Pos.CENTER_LEFT
        );

        card.setPadding(
                new Insets(25)
        );

        card.setStyle(
                cardStyle()
        );

        // UI-only: show a user silhouette instead of customer initials.
        StackPane avatar =
                createAvatar();

        VBox information =
                new VBox(7);

        HBox nameRow =
                new HBox(10);

        nameRow.setAlignment(
                Pos.CENTER_LEFT
        );

        Label name =
                new Label(
                        safe(
                                customer.getName()
                        )
                );

        name.setTextFill(
                Color.web(HEADING)
        );

        name.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        28
                )
        );

        Label number =
                new Label(
                        "#" +
                        customerNumber
                );

        number.setTextFill(
                Color.web(BLUE)
        );

        number.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        12
                )
        );

        number.setPadding(
                new Insets(
                        5,
                        10,
                        5,
                        10
                )
        );

        number.setStyle(
                "-fx-background-color: #172033;" +
                "-fx-background-radius: 20;"
        );

        nameRow.getChildren().addAll(
                name,
                number
        );

        Label role =
                new Label(
                        "RoadGuardian Customer"
                );

        role.setTextFill(
                Color.web(TEXT)
        );

        role.setFont(
                Font.font(
                        "Arial",
                        14
                )
        );

        HBox contact =
                new HBox(20);

        Label email =
                new Label(
                        "✉  " +
                        safe(
                                customer.getEmail()
                        )
                );

        Label phone =
                new Label(
                        "☎  " +
                        safe(
                                customer.getPhone()
                        )
                );

        email.setTextFill(
                Color.web(TEXT)
        );

        phone.setTextFill(
                Color.web(TEXT)
        );

        email.setFont(
                Font.font(
                        "Arial",
                        13
                )
        );

        phone.setFont(
                Font.font(
                        "Arial",
                        13
                )
        );

        contact.getChildren().addAll(
                email,
                phone
        );

        information.getChildren().addAll(
                nameRow,
                role,
                contact
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        VBox statusBox =
                new VBox(7);

        statusBox.setAlignment(
                Pos.CENTER_RIGHT
        );

        Label status =
                createStatusBadge(
                        safe(
                                customer.getStatus()
                        )
                );

        Label customerId =
                new Label(
                        "ID: " +
                        safe(
                                customer.getCustomerId()
                        )
                );

        customerId.setTextFill(
                Color.web(TEXT)
        );

        customerId.setFont(
                Font.font(
                        "Arial",
                        12
                )
        );

        statusBox.getChildren().addAll(
                status,
                customerId
        );

        card.getChildren().addAll(
                avatar,
                information,
                spacer,
                statusBox
        );

        return card;
    }

    // =========================================================
    // OVERVIEW
    // =========================================================

    private HBox createOverview() {

        HBox row =
                new HBox(15);

        VBox vehicles =
                createStatCard(
                        "Vehicles",
                        "Registered customer vehicles",
                        "VEHICLE",
                        BLUE
                );

        VBox services =
                createStatCard(
                        "Services",
                        "Service request history",
                        "🔧",
                        GREEN
                );

        VBox sos =
                createStatCard(
                        "SOS Requests",
                        "Emergency request history",
                        "🚨",
                        RED
                );

        VBox documents =
                createStatCard(
                        "Documents",
                        "Customer documents",
                        "▣",
                        PURPLE
                );

        HBox.setHgrow(
                vehicles,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                services,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                sos,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                documents,
                Priority.ALWAYS
        );

        row.getChildren().addAll(
                vehicles,
                services,
                sos,
                documents
        );

        return row;
    }

    private VBox createStatCard(
            String title,
            String subtitle,
            String icon,
            String color
    ) {

        VBox card =
                new VBox(8);

        card.setPadding(
                new Insets(18)
        );

        card.setMinHeight(
                115
        );

        card.setStyle(
                cardStyle()
        );

        Label iconLabel =
                new Label();

        iconLabel.setTextFill(
                Color.web(color)
        );

        if ("VEHICLE".equalsIgnoreCase(icon)) {
            iconLabel.setGraphic(
                    IconUtil.createVehicleIcon(
                            0.80,
                            color
                    )
            );
        } else {
            iconLabel.setText(icon);
            iconLabel.setFont(
                    Font.font(
                            "Arial",
                            FontWeight.BOLD,
                            22
                    )
            );
        }

        Label titleLabel =
                new Label(title);

        titleLabel.setTextFill(
                Color.web(HEADING)
        );

        titleLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        16
                )
        );

        Label subtitleLabel =
                new Label(subtitle);

        subtitleLabel.setTextFill(
                Color.web(TEXT)
        );

        subtitleLabel.setFont(
                Font.font(
                        "Arial",
                        12
                )
        );

        subtitleLabel.setWrapText(
                true
        );

        card.getChildren().addAll(
                iconLabel,
                titleLabel,
                subtitleLabel
        );

        addHoverEffect(
                card
        );

        return card;
    }

    // =========================================================
    // PERSONAL INFORMATION
    // =========================================================

    private VBox createPersonalInformation() {

        VBox section =
                createSection(
                        "Personal Information",
                        "Basic information registered for this customer."
                );

        GridPane grid =
                new GridPane();

        grid.setHgap(15);
        grid.setVgap(15);

        addInfo(
                grid,
                "Customer ID",
                customer.getCustomerId(),
                0,
                0
        );

        addInfo(
                grid,
                "Customer Number",
                "#" +
                customerNumber,
                1,
                0
        );

        addInfo(
                grid,
                "Full Name",
                customer.getName(),
                2,
                0
        );

        addInfo(
                grid,
                "Email",
                customer.getEmail(),
                0,
                1
        );

        addInfo(
                grid,
                "Phone",
                customer.getPhone(),
                1,
                1
        );

        addInfo(
                grid,
                "City",
                customer.getCity(),
                2,
                1
        );

        addInfo(
                grid,
                "Address",
                customer.getAddress(),
                0,
                2
        );

        addInfo(
                grid,
                "Account Status",
                customer.getStatus(),
                1,
                2
        );

        addInfo(
                grid,
                "Profile Type",
                "Customer",
                2,
                2
        );

        for (
                int i = 0;
                i < 3;
                i++
        ) {

            ColumnConstraints column =
                    new ColumnConstraints();

            column.setPercentWidth(
                    33.33
            );

            column.setHgrow(
                    Priority.ALWAYS
            );

            grid.getColumnConstraints()
                    .add(column);
        }

        section.getChildren()
                .add(grid);

        return section;
    }

    private void addInfo(
            GridPane grid,
            String title,
            String value,
            int column,
            int row
    ) {

        grid.add(
                createInfoCard(
                        title,
                        value
                ),
                column,
                row
        );
    }

    private VBox createInfoCard(
            String title,
            String value
    ) {

        VBox card =
                new VBox(5);

        card.setPadding(
                new Insets(13)
        );

        card.setStyle(
                "-fx-background-color: " +
                SECONDARY +
                ";" +
                "-fx-border-color: " +
                BORDER +
                ";" +
                "-fx-border-radius: 9;" +
                "-fx-background-radius: 9;"
        );

        Label titleLabel =
                new Label(title);

        titleLabel.setTextFill(
                Color.web(TEXT)
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
                        safe(value)
                );

        valueLabel.setTextFill(
                Color.web(HEADING)
        );

        valueLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        14
                )
        );

        valueLabel.setWrapText(
                true
        );

        card.getChildren().addAll(
                titleLabel,
                valueLabel
        );

        return card;
    }

    // =========================================================
    // ACTIVITY
    // =========================================================

    private VBox createActivitySection() {

        VBox section =
                createSection(
                        "Customer Activity",
                        "Quick overview of customer-related activity."
                );

        HBox content =
                new HBox(15);

        VBox services =
                createActivityCard(
                        "Service Requests",
                        "View service request history",
                        BLUE
                );

        VBox sos =
                createActivityCard(
                        "SOS Requests",
                        "View emergency history",
                        RED
                );

        VBox reviews =
                createActivityCard(
                        "Reviews",
                        "Customer feedback and ratings",
                        ORANGE
                );

        VBox complaints =
                createActivityCard(
                        "Complaints",
                        "Customer complaints",
                        PURPLE
                );

        HBox.setHgrow(
                services,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                sos,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                reviews,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                complaints,
                Priority.ALWAYS
        );

        content.getChildren().addAll(
                services,
                sos,
                reviews,
                complaints
        );

        section.getChildren()
                .add(content);

        return section;
    }

    private VBox createActivityCard(
            String title,
            String subtitle,
            String color
    ) {

        VBox card =
                new VBox(8);

        card.setPadding(
                new Insets(16)
        );

        card.setStyle(
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
                new Label(title);

        titleLabel.setTextFill(
                Color.web(HEADING)
        );

        titleLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        15
                )
        );

        Label subtitleLabel =
                new Label(subtitle);

        subtitleLabel.setTextFill(
                Color.web(color)
        );

        subtitleLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        13
                )
        );

        Label info =
                new Label(
                        "Detailed records will appear here."
                );

        info.setTextFill(
                Color.web(TEXT)
        );

        info.setFont(
                Font.font(
                        "Arial",
                        11
                )
        );

        card.getChildren().addAll(
                titleLabel,
                subtitleLabel,
                info
        );

        addHoverEffect(
                card
        );

        return card;
    }

    // =========================================================
    // DOCUMENTS
    // =========================================================

    private VBox createDocumentsSection() {

        VBox section =
                createSection(
                        "Customer Documents",
                        "Important documents associated with this customer."
                );

        VBox documents =
                new VBox(10);

        documents.getChildren().addAll(

                createDocumentRow(
                        "Driving License",
                        "Customer identity document"
                ),

                createDocumentRow(
                        "ID Proof",
                        "Government identity proof"
                ),

                createDocumentRow(
                        "Vehicle Registration",
                        "RC / registration document"
                ),

                createDocumentRow(
                        "Insurance",
                        "Vehicle insurance document"
                )
        );

        section.getChildren()
                .add(documents);

        return section;
    }

    private HBox createDocumentRow(
            String name,
            String description
    ) {

        HBox row =
                new HBox(15);

        row.setAlignment(
                Pos.CENTER_LEFT
        );

        row.setPadding(
                new Insets(
                        13,
                        15,
                        13,
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
                "-fx-border-radius: 9;" +
                "-fx-background-radius: 9;"
        );

        Label icon =
                new Label("▣");

        icon.setTextFill(
                Color.web(PURPLE)
        );

        icon.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        20
                )
        );

        VBox info =
                new VBox(3);

        Label title =
                new Label(name);

        title.setTextFill(
                Color.web(HEADING)
        );

        title.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        14
                )
        );

        Label descriptionLabel =
                new Label(description);

        descriptionLabel.setTextFill(
                Color.web(TEXT)
        );

        descriptionLabel.setFont(
                Font.font(
                        "Arial",
                        11
                )
        );

        info.getChildren().addAll(
                title,
                descriptionLabel
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Label status =
                new Label(
                        "Not connected"
                );

        status.setTextFill(
                Color.web(TEXT)
        );

        status.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        11
                )
        );

        Button view =
                createSmallButton(
                        "View",
                        BLUE
                );

        view.setOnAction(
                event -> showMessage(
                        name,
                        "Document viewer will be connected with Firebase Storage."
                )
        );

        row.getChildren().addAll(
                icon,
                info,
                spacer,
                status,
                view
        );

        return row;
    }

    // =========================================================
    // QUICK ACTIONS
    // =========================================================

    private HBox createQuickActions() {

        HBox actions =
                new HBox(12);

        actions.setAlignment(
                Pos.CENTER_LEFT
        );

        Button edit =
                createButton(
                        "Edit Customer",
                        ORANGE
                );

        Button vehicles =
                createButton(
                        "View Vehicles",
                        BLUE
                );

        Button services =
                createButton(
                        "Service History",
                        GREEN
                );

        Button sos =
                createButton(
                        "SOS History",
                        RED
                );

        edit.setOnAction(
                event -> showMessage(
                        "Edit Customer",
                        "Edit Customer functionality will be connected here."
                )
        );

        vehicles.setOnAction(
                event -> showMessage(
                        "Vehicles",
                        "Vehicle page will be connected here."
                )
        );

        services.setOnAction(
                event -> showMessage(
                        "Services",
                        "Service Request page will be connected here."
                )
        );

        sos.setOnAction(
                event -> showMessage(
                        "SOS",
                        "SOS Request page will be connected here."
                )
        );

        actions.getChildren().addAll(
                edit,
                vehicles,
                services,
                sos
        );

        return actions;
    }

    // =========================================================
    // SECTION
    // =========================================================

    private VBox createSection(
            String title,
            String subtitle
    ) {

        VBox section =
                new VBox(14);

        section.setPadding(
                new Insets(20)
        );

        section.setStyle(
                cardStyle()
        );

        Label titleLabel =
                new Label(title);

        titleLabel.setTextFill(
                Color.web(HEADING)
        );

        titleLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        20
                )
        );

        Label subtitleLabel =
                new Label(subtitle);

        subtitleLabel.setTextFill(
                Color.web(TEXT)
        );

        subtitleLabel.setFont(
                Font.font(
                        "Arial",
                        13
                )
        );

        section.getChildren().addAll(
                titleLabel,
                subtitleLabel
        );

        return section;
    }

    // =========================================================
    // BUTTON
    // =========================================================

    private Button createButton(
            String text,
            String color
    ) {

        Button button =
                new Button(text);

        button.setTextFill(
                Color.WHITE
        );

        button.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        13
                )
        );

        button.setPadding(
                new Insets(
                        10,
                        16,
                        10,
                        16
                )
        );

        button.setCursor(
                Cursor.HAND
        );

        button.setStyle(
                "-fx-background-color: " +
                color +
                ";" +
                "-fx-background-radius: 9;"
        );

        button.setOnMouseEntered(
                event -> {

                    String hoverColor =
                            color.equals(
                                    BLUE
                            )
                                    ? BLUE_HOVER
                                    : color;

                    button.setStyle(
                            "-fx-background-color: " +
                            hoverColor +
                            ";" +
                            "-fx-background-radius: 9;"
                    );

                    button.setTranslateY(-1);
                }
        );

        button.setOnMouseExited(
                event -> {

                    button.setStyle(
                            "-fx-background-color: " +
                            color +
                            ";" +
                            "-fx-background-radius: 9;"
                    );

                    button.setTranslateY(0);
                }
        );

        return button;
    }

    // =========================================================
    // SMALL BUTTON
    // =========================================================

    private Button createSmallButton(
            String text,
            String color
    ) {

        Button button =
                new Button(text);

        button.setTextFill(
                Color.WHITE
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
                color +
                ";" +
                "-fx-background-radius: 7;"
        );

        return button;
    }

    // =========================================================
    // STATUS BADGE
    // =========================================================

    private Label createStatusBadge(
            String status
    ) {

        boolean active =
                status.equalsIgnoreCase(
                        "Active"
                );

        Label badge =
                new Label(
                        active
                                ? "● Active"
                                : "● Inactive"
                );

        String color =
                active
                        ? GREEN
                        : RED;

        badge.setTextFill(
                Color.web(color)
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

        badge.setStyle(
                "-fx-background-color: " +
                color +
                "18;" +
                "-fx-background-radius: 20;"
        );

        return badge;
    }

    // =========================================================
    // AVATAR
    // =========================================================

    private StackPane createAvatar() {

        StackPane avatar =
                new StackPane();

        Circle circle =
                new Circle(
                        32,
                        Color.web(BLUE)
                );

        // UI-only: shared vector user silhouette for customer representation.
        var userIcon =
                IconUtil.createUserIcon(
                        0.85,
                        Color.WHITE.toString()
                );

        avatar.getChildren().addAll(
                circle,
                userIcon
        );

        avatar.setMinSize(
                64,
                64
        );

        avatar.setPrefSize(
                64,
                64
        );

        avatar.setMaxSize(
                64,
                64
        );

        return avatar;
    }

    // =========================================================
    // HOVER
    // =========================================================

    private void addHoverEffect(
            VBox card
    ) {

        String normalStyle =
                cardStyle();

        card.setCursor(
                Cursor.HAND
        );

        card.setOnMouseEntered(
                event -> {

                    card.setTranslateY(-2);

                    card.setStyle(
                            normalStyle +
                            "-fx-effect: dropshadow(" +
                            "gaussian, rgba(37,99,235,0.16)," +
                            "10,0.12,0,3);"
                    );
                }
        );

        card.setOnMouseExited(
                event -> {

                    card.setTranslateY(0);

                    card.setStyle(
                            normalStyle
                    );
                }
        );
    }

    // =========================================================
    // CARD STYLE
    // =========================================================

    private String cardStyle() {

        return
                "-fx-background-color: " +
                SURFACE +
                ";" +
                "-fx-border-color: " +
                BORDER +
                ";" +
                "-fx-border-radius: 14;" +
                "-fx-background-radius: 14;";
    }

    // =========================================================
    // INITIALS
    // =========================================================

    private String getInitials(
            String name
    ) {

        if (
                name == null ||
                name.isBlank()
        ) {

            return "CU";
        }

        String[] parts =
                name.trim()
                        .split("\\s+");

        if (
                parts.length == 1
        ) {

            return parts[0]
                    .substring(
                            0,
                            Math.min(
                                    2,
                                    parts[0].length()
                            )
                    )
                    .toUpperCase();
        }

        return (
                parts[0].charAt(0)
                + ""
                +
                parts[
                        parts.length - 1
                ].charAt(0)
        ).toUpperCase();
    }

    // =========================================================
    // SAFE VALUE
    // =========================================================

    private String safe(
            String value
    ) {

        if (
                value == null ||
                value.isBlank()
        ) {

            return "-";
        }

        return value;
    }

    // =========================================================
    // MESSAGE
    // =========================================================

    private void showMessage(
            String title,
            String message
    ) {

        javafx.scene.control.Alert alert =
                new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.INFORMATION
                );

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}