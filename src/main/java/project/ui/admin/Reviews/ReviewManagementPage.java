package project.ui.admin.Reviews;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import project.controller.admin.ReviewController;
import project.model.Review;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReviewManagementPage {

    private static final String BG = "#0F0F0F";
    private static final String SURFACE = "#1A1A1A";
    private static final String SECONDARY = "#242424";
    private static final String WHITE = "#1A1A1A";

    private static final String HEADING = "#F3F4F6";
    private static final String RECORD_TEXT = "#F3F4F6";
    private static final String TEXT = "#A1A1AA";
    private static final String MUTED = "#A1A1AA";
    private static final String BORDER = "#F59E0B";

    private static final String BLUE = "#F59E0B";
    private static final String GREEN = "#22C55E";
    private static final String ORANGE = "#F59E0B";
    private static final String RED = "#EF4444";

    private final ReviewController controller = new ReviewController();
    private final List<Review> allReviews = new ArrayList<>();

    private StackPane pageContainer;
    private VBox managementView;
    private VBox reviewCards;
    private ScrollPane recordsScrollPane;

    private TextField searchField;
    private ComboBox<String> ratingFilter;
    private ComboBox<String> sortFilter;

    private Label totalLabel;
    private Label fiveStarLabel;
    private Label averageLabel;
    private Label resultCountLabel;

    private double savedScrollPosition = 0.0;

    public VBox getView() {
        pageContainer = new StackPane();
        pageContainer.setStyle("-fx-background-color: " + BG + ";");

        managementView = createManagementView();
        pageContainer.getChildren().setAll(managementView);

        loadReviews();

        VBox wrapper = new VBox(pageContainer);
        wrapper.setStyle("-fx-background-color: " + BG + ";");
        wrapper.setFillWidth(true);
        VBox.setVgrow(pageContainer, Priority.ALWAYS);
        return wrapper;
    }

    private VBox createManagementView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30, 32, 34, 32));
        root.setStyle("-fx-background-color: " + BG + ";");

        root.getChildren().addAll(
                createHeader(),
                createStatistics(),
                createToolbar(),
                createRecordsCard()
        );

        return root;
    }

    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(5);

        Label title = new Label("Review Management");
        title.setStyle(
                "-fx-text-fill: " + HEADING + ";"
                        + "-fx-font-size: 29px;"
                        + "-fx-font-weight: bold;"
        );

        Label subtitle = new Label(
                "Monitor real customer reviews submitted after completed service requests."
        );
        subtitle.setWrapText(true);
        subtitle.setStyle(
                "-fx-text-fill: " + TEXT + ";"
                        + "-fx-font-size: 13px;"
        );

        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label live = new Label("● SYSTEM LIVE");
        live.setStyle(
                "-fx-text-fill: " + GREEN + ";"
                        + "-fx-font-size: 10px;"
                        + "-fx-font-weight: bold;"
        );

        Button refresh = createPrimaryButton("Refresh");
        refresh.setOnAction(event -> refreshPreservingScroll());

        header.getChildren().addAll(titleBox, spacer, live, refresh);
        return header;
    }

    private HBox createStatistics() {
        HBox row = new HBox(14);

        totalLabel = createValueLabel(BLUE);
        fiveStarLabel = createValueLabel(ORANGE);
        averageLabel = createValueLabel(GREEN);

        VBox total = createStatCard(
                "Total Reviews",
                totalLabel,
                "All reviews"
        );
        VBox fiveStar = createStatCard(
                "5 ★ Reviews",
                fiveStarLabel,
                "Excellent experiences"
        );
        VBox average = createStatCard(
                "Average Rating",
                averageLabel,
                "Overall customer score"
        );

        HBox.setHgrow(total, Priority.ALWAYS);
        HBox.setHgrow(fiveStar, Priority.ALWAYS);
        HBox.setHgrow(average, Priority.ALWAYS);

        row.getChildren().addAll(total, fiveStar, average);
        return row;
    }

    private Label createValueLabel(String color) {
        Label label = new Label("0");
        label.setStyle(
                "-fx-text-fill: " + color + ";"
                        + "-fx-font-size: 25px;"
                        + "-fx-font-weight: bold;"
        );
        return label;
    }

    private VBox createStatCard(String title, Label value, String subtitle) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(16));
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMinHeight(105);
        card.setStyle(
                "-fx-background-color: " + SURFACE + ";"
                        + "-fx-border-color: " + BORDER + ";"
                        + "-fx-border-radius: 12;"
                        + "-fx-background-radius: 12;"
        );

        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-text-fill: #A1A1AA;"
                        + "-fx-font-size: 12px;"
                        + "-fx-font-weight: bold;"
        );

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle(
                "-fx-text-fill: " + MUTED + ";"
                        + "-fx-font-size: 9px;"
        );

        card.getChildren().addAll(titleLabel, value, subtitleLabel);
        return card;
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("Search customer, mechanic, service, vehicle or review...");
        searchField.setPrefWidth(390);
        searchField.setPrefHeight(44);
        styleTextField(searchField);

        ratingFilter = new ComboBox<>();
        ratingFilter.getItems().addAll(
                "All Ratings",
                "5 Stars",
                "4 Stars",
                "3 Stars",
                "2 Stars",
                "1 Star"
        );
        ratingFilter.setValue("All Ratings");
        ratingFilter.setPrefWidth(145);
        ratingFilter.setPrefHeight(44);
        styleComboBox(ratingFilter);

        sortFilter = new ComboBox<>();
        sortFilter.getItems().addAll(
                "Newest First",
                "Oldest First",
                "Highest Rating",
                "Lowest Rating",
                "Customer A-Z"
        );
        sortFilter.setValue("Newest First");
        sortFilter.setPrefWidth(155);
        sortFilter.setPrefHeight(44);
        styleComboBox(sortFilter);

        searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        ratingFilter.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        sortFilter.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());

        toolbar.getChildren().addAll(searchField, ratingFilter, sortFilter);
        return toolbar;
    }

    private VBox createRecordsCard() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(20));
        card.setStyle(
                "-fx-background-color: " + SURFACE + ";"
                        + "-fx-border-color: " + BORDER + ";"
                        + "-fx-border-radius: 14;"
                        + "-fx-background-radius: 14;"
        );

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox headingBox = new VBox(4);

        Label heading = new Label("Customer Reviews");
        heading.setStyle(
                "-fx-text-fill: " + HEADING + ";"
                        + "-fx-font-size: 19px;"
                        + "-fx-font-weight: bold;"
        );

        resultCountLabel = new Label("0 reviews");
        resultCountLabel.setStyle(
                "-fx-text-fill: " + BLUE + ";"
                        + "-fx-font-size: 10px;"
                        + "-fx-font-weight: bold;"
        );

        headingBox.getChildren().addAll(heading, resultCountLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label note = new Label("No demo records");
        note.setStyle(
                "-fx-text-fill: " + GREEN + ";"
                        + "-fx-font-size: 9px;"
                        + "-fx-font-weight: bold;"
        );

        header.getChildren().addAll(headingBox, spacer, note);

        reviewCards = new VBox(12);
        reviewCards.setPadding(new Insets(3, 3, 10, 3));
        reviewCards.setFillWidth(true);

        recordsScrollPane = new ScrollPane(reviewCards);
        recordsScrollPane.setFitToWidth(true);
        recordsScrollPane.setPrefHeight(560);
        recordsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        recordsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        recordsScrollPane.setStyle(
                "-fx-background-color: transparent;"
                        + "-fx-background: transparent;"
        );

        card.getChildren().addAll(header, new Separator(), recordsScrollPane);
        return card;
    }

    private VBox createReviewCard(Review review, int number) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(17));
        card.setCursor(Cursor.HAND);
        card.setStyle(
                "-fx-background-color: #242424;"
                        + "-fx-border-color: " + BORDER + ";"
                        + "-fx-border-radius: 11;"
                        + "-fx-background-radius: 11;"
        );

        HBox top = new HBox(12);
        top.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);

        Label customer = new Label(
                "#" + number + "  " + firstNonBlank(review.getCustomerName(), "Customer")
        );
        customer.setStyle(
                "-fx-text-fill: " + RECORD_TEXT + ";"
                        + "-fx-font-size: 16px;"
                        + "-fx-font-weight: bold;"
        );

        Label requestId = new Label(
                "Request ID: " + firstNonBlank(review.getServiceRequestId(), "-")
        );
        requestId.setStyle(
                "-fx-text-fill: " + TEXT + ";"
                        + "-fx-font-size: 9px;"
        );

        titleBox.getChildren().addAll(customer, requestId);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label rating = new Label(stars(review.getRating()) + "  " + review.getRating() + "/5");
        rating.setStyle(
                "-fx-text-fill: " + ORANGE + ";"
                        + "-fx-font-size: 12px;"
                        + "-fx-font-weight: bold;"
        );

        top.getChildren().addAll(titleBox, spacer, rating);

        HBox info = new HBox(10);
        VBox service = createInfoBox("SERVICE", firstNonBlank(review.getServiceType(), "Service Request"));
        VBox mechanic = createInfoBox("MECHANIC", firstNonBlank(review.getMechanicName(), review.getMechanicId(), "Mechanic"));
        VBox vehicle = createInfoBox("VEHICLE", firstNonBlank(review.getVehicleNumber(), review.getVehicleId(), "Vehicle"));
        VBox date = createInfoBox("UPDATED", formatDate(firstNonBlank(review.getUpdatedAt(), review.getCreatedAt())));

        HBox.setHgrow(service, Priority.ALWAYS);
        HBox.setHgrow(mechanic, Priority.ALWAYS);
        HBox.setHgrow(vehicle, Priority.ALWAYS);
        HBox.setHgrow(date, Priority.ALWAYS);
        info.getChildren().addAll(service, mechanic, vehicle, date);

        Label reviewText = new Label(firstNonBlank(review.getReviewText(), "No review text."));
        reviewText.setWrapText(true);
        reviewText.setStyle(
                "-fx-text-fill: " + RECORD_TEXT + ";"
                        + "-fx-font-size: 11px;"
                        + "-fx-background-color: #1A1A1A;"
                        + "-fx-border-color: " + BORDER + ";"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
                        + "-fx-padding: 10 12 10 12;"
        );

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button details = createSecondaryButton("View Details");
        details.setOnAction(event -> {
            event.consume();
            savedScrollPosition = getScrollPosition();
            showDetails(review);
        });

        Button delete = createDangerButton("Delete");
        delete.setOnAction(event -> {
            event.consume();
            deleteReview(review);
        });

        actions.getChildren().addAll(details, delete);

        card.getChildren().addAll(
                top,
                new Separator(),
                info,
                reviewText,
                actions
        );

        card.setOnMouseClicked(event -> {
            savedScrollPosition = getScrollPosition();
            showDetails(review);
        });

        return card;
    }

    private VBox createInfoBox(String title, String value) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(10));
        box.setMinWidth(120);
        box.setMaxWidth(Double.MAX_VALUE);
        box.setStyle(
                "-fx-background-color: " + WHITE + ";"
                        + "-fx-border-color: " + BORDER + ";"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
        );

        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-text-fill: " + TEXT + ";"
                        + "-fx-font-size: 8px;"
                        + "-fx-font-weight: bold;"
        );

        Label valueLabel = new Label(firstNonBlank(value, "-"));
        valueLabel.setWrapText(true);
        valueLabel.setStyle(
                "-fx-text-fill: " + RECORD_TEXT + ";"
                        + "-fx-font-size: 10px;"
                        + "-fx-font-weight: bold;"
        );

        box.getChildren().addAll(titleLabel, valueLabel);
        return box;
    }

    private void loadReviews() {
        allReviews.clear();
        List<Review> reviews = controller.getAllReviews();
        if (reviews != null) {
            allReviews.addAll(reviews);
        }
        updateStatistics();
        applyFilters();
    }

    private void refreshPreservingScroll() {
        double position = getScrollPosition();
        loadReviews();
        Platform.runLater(() -> {
            if (recordsScrollPane != null) {
                recordsScrollPane.setVvalue(position);
            }
        });
    }

    private void applyFilters() {
        if (reviewCards == null) {
            return;
        }

        List<Review> filtered = controller.filterReviews(
                allReviews,
                searchField == null ? null : searchField.getText(),
                ratingFilter == null ? "All Ratings" : ratingFilter.getValue(),
                sortFilter == null ? "Newest First" : sortFilter.getValue()
        );

        renderReviews(filtered);
    }

    private void renderReviews(List<Review> reviews) {
        reviewCards.getChildren().clear();

        if (reviews == null || reviews.isEmpty()) {
            reviewCards.getChildren().add(createEmptyState());
            resultCountLabel.setText("0 reviews found");
            return;
        }

        int number = 1;
        for (Review review : reviews) {
            reviewCards.getChildren().add(createReviewCard(review, number++));
        }

        resultCountLabel.setText(
                reviews.size() == 1 ? "1 review" : reviews.size() + " reviews"
        );
    }

    private void updateStatistics() {
        if (totalLabel != null) {
            totalLabel.setText(String.valueOf(allReviews.size()));
        }
        if (fiveStarLabel != null) {
            fiveStarLabel.setText(String.valueOf(controller.getFiveStarCount(allReviews)));
        }
        if (averageLabel != null) {
            averageLabel.setText(String.format("%.1f", controller.getAverageRating(allReviews)));
        }
    }

    private void showDetails(Review review) {
        if (review == null || pageContainer == null) {
            return;
        }

        Review fresh = controller.getReviewById(review.getReviewId());
        if (fresh == null) {
            fresh = review;
        }

        Review selected = fresh;
        ReviewDetailsPage page = new ReviewDetailsPage(
                selected,
                this::showManagement,
                () -> deleteReview(selected)
        );

        pageContainer.getChildren().setAll(page.getView());
    }

    private void showManagement() {
        if (pageContainer == null) {
            return;
        }

        pageContainer.getChildren().setAll(managementView);
        loadReviews();

        Platform.runLater(() -> {
            if (recordsScrollPane != null) {
                recordsScrollPane.setVvalue(savedScrollPosition);
            }
        });
    }

    private void deleteReview(Review review) {
        if (review == null) {
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("RoadGuardian");
        confirmation.setHeaderText("Delete Review?");
        confirmation.setContentText(
                "Customer: " + firstNonBlank(review.getCustomerName(), "Customer")
                        + "\nRequest ID: " + firstNonBlank(review.getServiceRequestId(), "-")
                        + "\n\nThis removes the review from Firebase and clears the review fields on the same service request."
        );

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        boolean success = controller.deleteReview(review.getReviewId());
        if (!success) {
            showError("Delete Failed", "The review could not be deleted from Firebase.");
            return;
        }

        pageContainer.getChildren().setAll(managementView);
        loadReviews();
    }

    private VBox createEmptyState() {
        VBox box = new VBox(7);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(42));

        Label title = new Label("No Reviews Found");
        title.setStyle(
                "-fx-text-fill: " + HEADING + ";"
                        + "-fx-font-size: 15px;"
                        + "-fx-font-weight: bold;"
        );

        Label message = new Label(
                "Customer reviews submitted after completed services will appear here."
        );
        message.setWrapText(true);
        message.setStyle(
                "-fx-text-fill: " + TEXT + ";"
                        + "-fx-font-size: 10px;"
        );

        box.getChildren().addAll(title, message);
        return box;
    }

    private Button createPrimaryButton(String text) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: " + BLUE + ";"
                        + "-fx-text-fill: white;"
                        + "-fx-background-radius: 8;"
                        + "-fx-font-size: 10px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-padding: 9 15 9 15;"
                        + "-fx-cursor: hand;"
        );
        return button;
    }

    private Button createSecondaryButton(String text) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: #1A1A1A;"
                        + "-fx-text-fill: " + BLUE + ";"
                        + "-fx-border-color: " + BLUE + ";"
                        + "-fx-border-radius: 7;"
                        + "-fx-background-radius: 7;"
                        + "-fx-font-size: 9px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-padding: 8 12 8 12;"
                        + "-fx-cursor: hand;"
        );
        return button;
    }

    private Button createDangerButton(String text) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: " + RED + ";"
                        + "-fx-text-fill: white;"
                        + "-fx-background-radius: 7;"
                        + "-fx-font-size: 9px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-padding: 8 12 8 12;"
                        + "-fx-cursor: hand;"
        );
        return button;
    }

    private void styleTextField(TextField field) {
        field.setStyle(
                "-fx-background-color: #1A1A1A;"
                        + "-fx-text-fill: " + RECORD_TEXT + ";"
                        + "-fx-prompt-text-fill: " + MUTED + ";"
                        + "-fx-border-color: " + BORDER + ";"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
                        + "-fx-padding: 0 12 0 12;"
                        + "-fx-font-size: 11px;"
        );
    }

    private void styleComboBox(ComboBox<String> comboBox) {
        comboBox.setStyle(
                "-fx-background-color: #1A1A1A;"
                        + "-fx-border-color: " + BORDER + ";"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
                        + "-fx-font-size: 11px;"
        );
    }

    private double getScrollPosition() {
        return recordsScrollPane == null ? 0.0 : recordsScrollPane.getVvalue();
    }

    private String stars(int rating) {
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            builder.append(i <= rating ? "★" : "☆");
        }
        return builder.toString();
    }

    private String formatDate(String value) {
        String cleaned = clean(value);
        if (cleaned == null) {
            return "-";
        }

        try {
            long millis = Long.parseLong(cleaned);
            if (millis > 0 && millis < 100000000000L) {
                millis *= 1000L;
            }
            return DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")
                    .withZone(ZoneId.systemDefault())
                    .format(Instant.ofEpochMilli(millis));
        } catch (Exception e) {
            return cleaned;
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("RoadGuardian");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            String cleaned = clean(value);
            if (cleaned != null) {
                return cleaned;
            }
        }
        return null;
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }
}
