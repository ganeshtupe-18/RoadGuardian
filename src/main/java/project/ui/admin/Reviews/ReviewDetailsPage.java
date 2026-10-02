package project.ui.admin.Reviews;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import project.model.Review;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ReviewDetailsPage {

    private static final String BG = "#0F0F0F";
    private static final String SURFACE = "#1A1A1A";
    private static final String WHITE = "#1A1A1A";
    private static final String HEADING = "#F3F4F6";
    private static final String TEXT = "#A1A1AA";
    private static final String BORDER = "#F59E0B";
    private static final String BLUE = "#F59E0B";
    private static final String ORANGE = "#F59E0B";
    private static final String RED = "#EF4444";

    private final Review review;
    private final Runnable backAction;
    private final Runnable deleteAction;

    public ReviewDetailsPage(
            Review review,
            Runnable backAction,
            Runnable deleteAction
    ) {
        this.review = review;
        this.backAction = backAction == null ? () -> {} : backAction;
        this.deleteAction = deleteAction == null ? () -> {} : deleteAction;
    }

    public VBox getView() {
        VBox root = new VBox(18);
        root.setPadding(new Insets(28, 32, 34, 32));
        root.setStyle("-fx-background-color: " + BG + ";");

        if (review == null) {
            root.getChildren().add(createErrorState());
            return root;
        }

        root.getChildren().addAll(
                createHeader(),
                createOverviewCard(),
                createReviewCard(),
                createActions()
        );

        return root;
    }

    private HBox createHeader() {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);

        Label title = new Label("Review Details");
        title.setStyle(
                "-fx-text-fill: " + HEADING + ";"
                        + "-fx-font-size: 26px;"
                        + "-fx-font-weight: bold;"
        );

        Label subtitle = new Label(
                "Review ID: " + firstNonBlank(review.getReviewId(), "-")
        );
        subtitle.setStyle(
                "-fx-text-fill: " + TEXT + ";"
                        + "-fx-font-size: 11px;"
        );

        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label rating = new Label(stars(review.getRating()) + "  " + review.getRating() + "/5");
        rating.setStyle(
                "-fx-text-fill: " + ORANGE + ";"
                        + "-fx-font-size: 14px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-background-color: #1A1A1A;"
                        + "-fx-border-color: " + BORDER + ";"
                        + "-fx-border-radius: 18;"
                        + "-fx-background-radius: 18;"
                        + "-fx-padding: 7 12 7 12;"
        );

        header.getChildren().addAll(titleBox, spacer, rating);
        return header;
    }

    private VBox createOverviewCard() {
        VBox card = createCard();

        Label heading = sectionHeading("Review Information");

        HBox row1 = new HBox(10);
        VBox customer = infoBox(
                "CUSTOMER",
                firstNonBlank(review.getCustomerName(), review.getCustomerId(), "Customer")
        );
        VBox email = infoBox(
                "CUSTOMER EMAIL",
                firstNonBlank(review.getCustomerEmail(), review.getCustomerId(), "-")
        );
        VBox request = infoBox(
                "REQUEST ID",
                firstNonBlank(review.getServiceRequestId(), "-")
        );

        HBox.setHgrow(customer, Priority.ALWAYS);
        HBox.setHgrow(email, Priority.ALWAYS);
        HBox.setHgrow(request, Priority.ALWAYS);
        row1.getChildren().addAll(customer, email, request);

        HBox row2 = new HBox(10);
        VBox service = infoBox(
                "SERVICE",
                firstNonBlank(review.getServiceType(), "Service Request")
        );
        VBox mechanic = infoBox(
                "MECHANIC",
                firstNonBlank(review.getMechanicName(), review.getMechanicId(), "Mechanic")
        );
        VBox vehicle = infoBox(
                "VEHICLE",
                firstNonBlank(review.getVehicleNumber(), review.getVehicleId(), "Vehicle")
        );

        HBox.setHgrow(service, Priority.ALWAYS);
        HBox.setHgrow(mechanic, Priority.ALWAYS);
        HBox.setHgrow(vehicle, Priority.ALWAYS);
        row2.getChildren().addAll(service, mechanic, vehicle);

        HBox row3 = new HBox(10);
        VBox status = infoBox("STATUS", firstNonBlank(review.getStatus(), "Published"));
        VBox created = infoBox("CREATED", formatDate(review.getCreatedAt()));
        VBox updated = infoBox(
                "UPDATED",
                formatDate(firstNonBlank(review.getUpdatedAt(), review.getCreatedAt()))
        );

        HBox.setHgrow(status, Priority.ALWAYS);
        HBox.setHgrow(created, Priority.ALWAYS);
        HBox.setHgrow(updated, Priority.ALWAYS);
        row3.getChildren().addAll(status, created, updated);

        card.getChildren().addAll(
                heading,
                new Separator(),
                row1,
                row2,
                row3
        );

        return card;
    }

    private VBox createReviewCard() {
        VBox card = createCard();

        Label heading = sectionHeading("Customer Feedback");

        Label reviewText = new Label(firstNonBlank(review.getReviewText(), "No review text."));
        reviewText.setWrapText(true);
        reviewText.setStyle(
                "-fx-text-fill: " + HEADING + ";"
                        + "-fx-font-size: 12px;"
                        + "-fx-background-color: #1A1A1A;"
                        + "-fx-border-color: " + BORDER + ";"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
                        + "-fx-padding: 14 16 14 16;"
        );

        card.getChildren().addAll(heading, new Separator(), reviewText);
        return card;
    }

    private HBox createActions() {
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button back = new Button("← Back to Reviews");
        back.setStyle(
                "-fx-background-color: #1A1A1A;"
                        + "-fx-text-fill: " + BLUE + ";"
                        + "-fx-border-color: " + BLUE + ";"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
                        + "-fx-font-size: 10px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-padding: 9 15 9 15;"
                        + "-fx-cursor: hand;"
        );
        back.setOnAction(event -> backAction.run());

        Button delete = new Button("Delete Review");
        delete.setStyle(
                "-fx-background-color: " + RED + ";"
                        + "-fx-text-fill: white;"
                        + "-fx-background-radius: 8;"
                        + "-fx-font-size: 10px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-padding: 9 15 9 15;"
                        + "-fx-cursor: hand;"
        );
        delete.setOnAction(event -> deleteAction.run());

        actions.getChildren().addAll(back, delete);
        return actions;
    }

    private VBox createCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(18));
        card.setStyle(
                "-fx-background-color: " + SURFACE + ";"
                        + "-fx-border-color: " + BORDER + ";"
                        + "-fx-border-radius: 12;"
                        + "-fx-background-radius: 12;"
        );
        return card;
    }

    private VBox infoBox(String title, String value) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(10));
        box.setMinWidth(130);
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
                "-fx-text-fill: " + HEADING + ";"
                        + "-fx-font-size: 10px;"
                        + "-fx-font-weight: bold;"
        );

        box.getChildren().addAll(titleLabel, valueLabel);
        return box;
    }

    private Label sectionHeading(String text) {
        Label label = new Label(text);
        label.setStyle(
                "-fx-text-fill: " + HEADING + ";"
                        + "-fx-font-size: 17px;"
                        + "-fx-font-weight: bold;"
        );
        return label;
    }

    private VBox createErrorState() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));

        Label title = new Label("Review Not Available");
        title.setStyle(
                "-fx-text-fill: " + RED + ";"
                        + "-fx-font-size: 16px;"
                        + "-fx-font-weight: bold;"
        );

        Button back = new Button("Back");
        back.setOnAction(event -> backAction.run());

        box.getChildren().addAll(title, back);
        return box;
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
