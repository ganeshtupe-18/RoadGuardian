package project.ui.user;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import project.controller.user.ReviewController;
import project.model.Review;
import project.model.ServiceRequest;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ReviewsPage {

    private static final String BACKGROUND = "#0F0F0F";
    private static final String CARD       = "#1A1A1A";
    private static final String SECONDARY  = "#242424";
    private static final String HEADING    = "#F3F4F6";
    private static final String TEXT       = "#A1A1AA";
    private static final String MUTED      = "#71717A";
    private static final String BORDER     = "#F59E0B";
    private static final String BLUE       = "#F59E0B";
    private static final String GREEN      = "#22C55E";
    private static final String ORANGE     = "#F59E0B";
    private static final String RED        = "#EF4444";

    private final ReviewController controller;

    private final List<ServiceRequest> reviewableRequests = new ArrayList<>();

    private ComboBox<ServiceRequest> requestComboBox;
    private ComboBox<Integer> ratingComboBox;
    private TextArea reviewTextArea;
    private Button submitButton;
    private Label selectedInfoLabel;
    private VBox myReviewsContainer;
    private Label reviewCountLabel;

    public ReviewsPage() {
        controller = new ReviewController();
    }

    public Scene getReviewsScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BACKGROUND + ";");

        root.setTop(UserHeader.createHeader());
        root.setLeft(UserSideBar.createSidebar("Reviews"));

        VBox content = createContent();

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle(
                "-fx-background-color: transparent;"
                        + "-fx-background: transparent;"
        );

        root.setCenter(scrollPane);

        double width = UserDashboard.dashboardStage != null
                ? UserDashboard.dashboardStage.getWidth()
                : 1200;
        double height = UserDashboard.dashboardStage != null
                ? UserDashboard.dashboardStage.getHeight()
                : 800;

        Scene scene = new Scene(root, width, height);
        loadData();
        return scene;
    }

    private VBox createContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(28, 32, 40, 32));
        content.setStyle("-fx-background-color: " + BACKGROUND + ";");

        content.getChildren().addAll(
                createHeader(),
                createReviewForm(),
                createMyReviewsSection()
        );

        return content;
    }

    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox text = new VBox(5);

        Label title = new Label("Reviews & Ratings");
        title.setTextFill(Color.web(HEADING));
        title.setFont(Font.font("Arial", FontWeight.BOLD, 29));

        Label subtitle = new Label(
                "Rate completed RoadGuardian services. Your feedback is stored and visible to Admin."
        );
        subtitle.setWrapText(true);
        subtitle.setTextFill(Color.web(TEXT));
        subtitle.setFont(Font.font("Arial", 13));

        text.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refresh = createSecondaryButton("Refresh");
        refresh.setOnAction(event -> loadData());

        header.getChildren().addAll(text, spacer, refresh);
        return header;
    }

    private VBox createReviewForm() {
        VBox card = createCard();

        Label heading = createSectionHeading("Rate a Completed Service");

        Label helper = new Label(
                "Only completed service requests can be reviewed. One review is maintained per service request; submitting again updates the same review."
        );
        helper.setWrapText(true);
        helper.setTextFill(Color.web(TEXT));
        helper.setFont(Font.font("Arial", 11));

        Label requestLabel = fieldLabel("Completed Service");

        requestComboBox = new ComboBox<>();
        requestComboBox.setMaxWidth(Double.MAX_VALUE);
        requestComboBox.setPrefHeight(44);
        styleComboBox(requestComboBox);

        requestComboBox.setCellFactory(listView -> new ServiceRequestCell());
        requestComboBox.setButtonCell(new ServiceRequestCell());
        requestComboBox.setOnAction(event -> loadSelectedReview());

        selectedInfoLabel = new Label("Select a completed service to review.");
        selectedInfoLabel.setWrapText(true);
        selectedInfoLabel.setStyle(
                "-fx-text-fill: " + TEXT + ";"
                        + "-fx-font-size: 11px;"
                        + "-fx-background-color: " + SECONDARY + ";"
                        + "-fx-border-color: " + BORDER + ";"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
                        + "-fx-padding: 10 12 10 12;"
        );

        Label ratingLabel = fieldLabel("Rating");

        ratingComboBox = new ComboBox<>();
        ratingComboBox.getItems().addAll(1, 2, 3, 4, 5);
        ratingComboBox.setPromptText("Select 1 to 5 stars");
        ratingComboBox.setPrefWidth(220);
        ratingComboBox.setPrefHeight(44);
        styleComboBox(ratingComboBox);

        ratingComboBox.setCellFactory(listView -> new RatingCell());
        ratingComboBox.setButtonCell(new RatingCell());

        Label reviewLabel = fieldLabel("Review");

        reviewTextArea = new TextArea();
        reviewTextArea.setPromptText("Write your service experience...");
        reviewTextArea.setWrapText(true);
        reviewTextArea.setPrefRowCount(5);
        reviewTextArea.setStyle(
                "-fx-control-inner-background: #242424;"
                        + "-fx-text-fill: " + HEADING + ";"
                        + "-fx-prompt-text-fill: #9A9AA3;"
                        + "-fx-border-color: " + BORDER + ";"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
                        + "-fx-font-size: 12px;"
        );

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button clear = createSecondaryButton("Clear");
        clear.setOnAction(event -> clearForm(false));

        submitButton = createPrimaryButton("Submit Review");
        submitButton.setOnAction(event -> submitReview());

        actions.getChildren().addAll(clear, submitButton);

        card.getChildren().addAll(
                heading,
                helper,
                new Separator(),
                requestLabel,
                requestComboBox,
                selectedInfoLabel,
                ratingLabel,
                ratingComboBox,
                reviewLabel,
                reviewTextArea,
                actions
        );

        return card;
    }

    private VBox createMyReviewsSection() {
        VBox card = createCard();

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label heading = createSectionHeading("My Submitted Reviews");
        reviewCountLabel = new Label("0 reviews");
        reviewCountLabel.setStyle(
                "-fx-text-fill: " + BLUE + ";"
                        + "-fx-font-size: 10px;"
                        + "-fx-font-weight: bold;"
        );
        titleBox.getChildren().addAll(heading, reviewCountLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label firebase = new Label("Active");
        firebase.setStyle(
                "-fx-text-fill: " + GREEN + ";"
                        + "-fx-font-size: 9px;"
                        + "-fx-font-weight: bold;"
        );

        header.getChildren().addAll(titleBox, spacer, firebase);

        myReviewsContainer = new VBox(12);

        card.getChildren().addAll(
                header,
                new Separator(),
                myReviewsContainer
        );

        return card;
    }

    private void loadData() {
        loadReviewableRequests();
        loadMyReviews();
    }

    private void loadReviewableRequests() {
        reviewableRequests.clear();

        List<ServiceRequest> requests = controller.getReviewableRequests();
        if (requests != null) {
            reviewableRequests.addAll(requests);
        }

        if (requestComboBox == null) {
            return;
        }

        ServiceRequest previous = requestComboBox.getValue();
        String previousId = previous == null ? null : previous.getRequestId();

        requestComboBox.getItems().setAll(reviewableRequests);

        if (reviewableRequests.isEmpty()) {
            requestComboBox.setValue(null);
            requestComboBox.setPromptText("No completed services available");
            selectedInfoLabel.setText(
                    "A service must reach Completed status before you can submit a review."
            );
            submitButton.setDisable(true);
            return;
        }

        submitButton.setDisable(false);

        ServiceRequest restore = findRequest(previousId);
        requestComboBox.setValue(restore != null ? restore : reviewableRequests.get(0));
        loadSelectedReview();
    }

    private void loadSelectedReview() {
        if (requestComboBox == null) {
            return;
        }

        ServiceRequest request = requestComboBox.getValue();
        if (request == null) {
            clearForm(false);
            return;
        }

        Review existing = controller.getReviewForRequest(request.getRequestId());

        String mechanic = firstNonBlank(
                request.getMechanicName(),
                request.getMechanicId(),
                "Mechanic"
        );
        String vehicle = firstNonBlank(
                request.getVehicleNumber(),
                request.getVehicleId(),
                "Vehicle"
        );

        selectedInfoLabel.setText(
                "Request: " + firstNonBlank(request.getRequestId(), "-")
                        + "   •   Mechanic: " + mechanic
                        + "   •   Vehicle: " + vehicle
        );

        if (existing != null) {
            ratingComboBox.setValue(existing.getRating());
            reviewTextArea.setText(firstNonBlank(existing.getReviewText(), ""));
            submitButton.setText("Update Review");
        } else {
            ratingComboBox.setValue(null);
            reviewTextArea.clear();
            submitButton.setText("Submit Review");
        }
    }

    private void submitReview() {
        ServiceRequest request = requestComboBox == null ? null : requestComboBox.getValue();
        Integer rating = ratingComboBox == null ? null : ratingComboBox.getValue();
        String reviewText = reviewTextArea == null ? null : clean(reviewTextArea.getText());

        if (request == null) {
            showError("Select Service", "Please select a completed service request.");
            return;
        }

        if (!request.isCompleted()) {
            showError("Review Not Allowed", "Only completed services can be reviewed.");
            return;
        }

        if (rating == null || rating < 1 || rating > 5) {
            showError("Select Rating", "Please select a rating from 1 to 5 stars.");
            return;
        }

        if (reviewText == null) {
            showError("Write Review", "Please write your service experience.");
            return;
        }

        if (reviewText.length() > 1000) {
            showError("Review Too Long", "Please keep the review within 1000 characters.");
            return;
        }

        submitButton.setDisable(true);

        boolean success = controller.submitReview(request, rating, reviewText);

        submitButton.setDisable(false);

        if (!success) {
            showError(
                    "Unable to Save Review",
                    "The review could not be saved. Confirm that the service is completed and try again."
            );
            return;
        }

        showInfo(
                "Review Saved",
                "Your review is saved and is now available to Admin."
        );

        loadData();
    }

    private void loadMyReviews() {
        if (myReviewsContainer == null) {
            return;
        }

        myReviewsContainer.getChildren().clear();

        List<Review> reviews = controller.getMyReviews();

        if (reviews == null || reviews.isEmpty()) {
            reviewCountLabel.setText("0 reviews");
            myReviewsContainer.getChildren().add(
                    createEmptyState(
                            "No Reviews Yet",
                            "After a service is completed, select it above and submit your rating."
                    )
            );
            return;
        }

        reviewCountLabel.setText(
                reviews.size() == 1 ? "1 review" : reviews.size() + " reviews"
        );

        for (Review review : reviews) {
            myReviewsContainer.getChildren().add(createReviewCard(review));
        }
    }

    private VBox createReviewCard(Review review) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle(
                "-fx-background-color: " + SECONDARY + ";"
                        + "-fx-border-color: " + BORDER + ";"
                        + "-fx-border-radius: 10;"
                        + "-fx-background-radius: 10;"
        );

        HBox top = new HBox(12);
        top.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(3);

        Label service = new Label(firstNonBlank(review.getServiceType(), "Service Request"));
        service.setTextFill(Color.web(HEADING));
        service.setFont(Font.font("Arial", FontWeight.BOLD, 15));

        Label request = new Label(
                "Request ID: " + firstNonBlank(review.getServiceRequestId(), "-")
        );
        request.setTextFill(Color.web(TEXT));
        request.setFont(Font.font("Arial", 9));

        titleBox.getChildren().addAll(service, request);

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
        VBox mechanic = createInfoBox(
                "MECHANIC",
                firstNonBlank(review.getMechanicName(), review.getMechanicId(), "Mechanic")
        );
        VBox vehicle = createInfoBox(
                "VEHICLE",
                firstNonBlank(review.getVehicleNumber(), review.getVehicleId(), "Vehicle")
        );
        VBox date = createInfoBox(
                "UPDATED",
                formatDate(firstNonBlank(review.getUpdatedAt(), review.getCreatedAt()))
        );

        HBox.setHgrow(mechanic, Priority.ALWAYS);
        HBox.setHgrow(vehicle, Priority.ALWAYS);
        HBox.setHgrow(date, Priority.ALWAYS);
        info.getChildren().addAll(mechanic, vehicle, date);

        Label text = new Label(firstNonBlank(review.getReviewText(), "No review text."));
        text.setWrapText(true);
        text.setStyle(
                "-fx-text-fill: " + HEADING + ";"
                        + "-fx-font-size: 11px;"
                        + "-fx-background-color: " + CARD + ";"
                        + "-fx-background-radius: 8;"
                        + "-fx-padding: 10 12 10 12;"
        );

        Button edit = createSecondaryButton("Edit This Review");
        edit.setOnAction(event -> {
            ServiceRequest target = findRequest(review.getServiceRequestId());
            if (target == null) {
                showError(
                        "Service Not Available",
                        "The completed service request for this review could not be loaded."
                );
                return;
            }
            requestComboBox.setValue(target);
            loadSelectedReview();
        });

        HBox actions = new HBox(edit);
        actions.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(
                top,
                new Separator(),
                info,
                text,
                actions
        );

        return card;
    }

    private VBox createInfoBox(String title, String value) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(9));
        box.setMinWidth(130);
        box.setMaxWidth(Double.MAX_VALUE);
        box.setStyle(
                "-fx-background-color: " + CARD + ";"
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
                "-fx-text-fill: " + HEADING + ";"
                        + "-fx-font-size: 10px;"
                        + "-fx-font-weight: bold;"
        );

        box.getChildren().addAll(titleLabel, valueLabel);
        return box;
    }

    private VBox createCard() {
        VBox card = new VBox(13);
        card.setPadding(new Insets(20));
        card.setStyle(
                "-fx-background-color: " + CARD + ";"
                        + "-fx-border-color: " + BORDER + ";"
                        + "-fx-border-radius: 14;"
                        + "-fx-background-radius: 14;"
        );
        return card;
    }

    private Label createSectionHeading(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.web(HEADING));
        label.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        return label;
    }

    private Label fieldLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.web(HEADING));
        label.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        return label;
    }

    private Button createPrimaryButton(String text) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: " + BLUE + ";"
                        + "-fx-text-fill: white;"
                        + "-fx-font-size: 11px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-background-radius: 8;"
                        + "-fx-padding: 10 17 10 17;"
                        + "-fx-cursor: hand;"
        );
        return button;
    }

    private Button createSecondaryButton(String text) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: " + CARD + ";"
                        + "-fx-text-fill: " + BLUE + ";"
                        + "-fx-border-color: " + BLUE + ";"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
                        + "-fx-font-size: 10px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-padding: 9 15 9 15;"
                        + "-fx-cursor: hand;"
        );
        return button;
    }

    private <T> void styleComboBox(ComboBox<T> comboBox) {
        comboBox.setStyle(
                "-fx-background-color: " + CARD + ";"
                        + "-fx-text-fill: " + HEADING + ";"
                        + "-fx-prompt-text-fill: " + MUTED + ";"
                        + "-fx-border-color: " + BORDER + ";"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
                        + "-fx-font-size: 11px;"
        );
    }

    private VBox createEmptyState(String title, String message) {
        VBox box = new VBox(6);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(28));

        Label titleLabel = new Label(title);
        titleLabel.setTextFill(Color.web(HEADING));
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setTextFill(Color.web(TEXT));
        messageLabel.setFont(Font.font("Arial", 10));

        box.getChildren().addAll(titleLabel, messageLabel);
        return box;
    }

    private void clearForm(boolean clearRequest) {
        if (clearRequest && requestComboBox != null) {
            requestComboBox.setValue(null);
        }
        if (ratingComboBox != null) {
            ratingComboBox.setValue(null);
        }
        if (reviewTextArea != null) {
            reviewTextArea.clear();
        }
        if (submitButton != null) {
            submitButton.setText("Submit Review");
        }
        if (selectedInfoLabel != null && (requestComboBox == null || requestComboBox.getValue() == null)) {
            selectedInfoLabel.setText("Select a completed service to review.");
        }
    }

    private ServiceRequest findRequest(String requestId) {
        String id = clean(requestId);
        if (id == null) {
            return null;
        }

        for (ServiceRequest request : reviewableRequests) {
            if (request != null && id.equals(clean(request.getRequestId()))) {
                return request;
            }
        }
        return null;
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

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("RoadGuardian");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
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

    private class ServiceRequestCell extends ListCell<ServiceRequest> {
        @Override
        protected void updateItem(ServiceRequest item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setStyle("-fx-background-color: #1A1A1A; -fx-text-fill: " + HEADING + ";");
                return;
            }
            setText(controller.getRequestDisplay(item));
            setStyle("-fx-background-color: " + CARD + "; -fx-text-fill: " + HEADING + "; -fx-font-size: 11px;");
        }
    }

    private class RatingCell extends ListCell<Integer> {
        @Override
        protected void updateItem(Integer item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setStyle("-fx-background-color: #1A1A1A; -fx-text-fill: " + HEADING + ";");
                return;
            }
            setText(stars(item) + "  " + item + "/5");
            setStyle("-fx-background-color: " + CARD + "; -fx-text-fill: " + HEADING + "; -fx-font-size: 11px;");
        }
    }
}
