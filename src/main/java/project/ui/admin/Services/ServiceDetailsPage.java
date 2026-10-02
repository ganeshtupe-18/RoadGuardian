package project.ui.admin.Services;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import project.model.RoadService;
import project.ui.admin.DashBoard.AdminSectionPage;

public class ServiceDetailsPage extends AdminSectionPage {

    private static final String BG = "#0F0F0F";
    private static final String WHITE = "#1A1A1A";
    private static final String DARK = "#F3F4F6";
    private static final String TEXT = "#A1A1AA";
    private static final String BORDER = "#F59E0B";
    private static final String BLUE = "#F59E0B";
    private static final String GREEN = "#22C55E";
    private static final String RED = "#EF4444";

    private final RoadService service;
    private final Runnable onBack;
    private final Runnable onEdit;

    public ServiceDetailsPage(RoadService service, Runnable onBack, Runnable onEdit) {
        this.service = service;
        this.onBack = onBack == null ? () -> {} : onBack;
        this.onEdit = onEdit == null ? () -> {} : onEdit;
    }

    @Override
    public VBox getView() {
        VBox root = new VBox(18);
        root.setPadding(new Insets(28, 32, 32, 32));
        root.setStyle("-fx-background-color: " + BG + ";");

        Button back = secondaryButton("← Back to Services");
        back.setOnAction(e -> onBack.run());

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox heading = new VBox(4);
        heading.getChildren().addAll(
                label("Service Details", 28, true),
                label("Actual Firebase catalog record", 12, false)
        );
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button edit = primaryButton("Edit Service");
        edit.setOnAction(e -> onEdit.run());
        header.getChildren().addAll(heading, spacer, edit);

        VBox card = new VBox(10);
        card.setPadding(new Insets(22));
        card.setStyle("-fx-background-color: #1A1A1A;-fx-border-color: " + BORDER + ";-fx-border-radius: 12;-fx-background-radius: 12;");

        String status = service != null && service.isActive() ? "Active" : "Inactive";
        Label statusBadge = new Label(status);
        String statusColor = "Active".equals(status) ? GREEN : RED;
        statusBadge.setStyle("-fx-text-fill: " + statusColor + ";-fx-font-weight: bold;-fx-background-color: " + statusColor + "18;-fx-border-color: " + statusColor + "55;-fx-border-radius: 14;-fx-background-radius: 14;-fx-padding: 5 10 5 10;");

        card.getChildren().addAll(
                row("Service ID", service == null ? "-" : service.getServiceId()),
                row("Name", service == null ? "-" : service.getName()),
                row("Category", service == null ? "-" : service.getCategory()),
                row("Base Price", service == null ? "-" : service.getPriceDisplay()),
                row("Estimated Duration", service == null ? "-" : service.getEstimatedDuration()),
                row("Created", service == null ? "-" : service.getCreatedAt()),
                row("Last Updated", service == null ? "-" : service.getUpdatedAt()),
                label("Status", 10, true),
                statusBadge,
                label("Description", 10, true),
                value(service == null ? "-" : service.getDescription())
        );

        root.getChildren().addAll(back, header, card);
        return root;
    }

    private HBox row(String key, String val) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        Label k = label(key, 11, true);
        k.setMinWidth(150);
        Label v = value(val);
        v.setWrapText(true);
        HBox.setHgrow(v, Priority.ALWAYS);
        row.getChildren().addAll(k, v);
        return row;
    }

    private Label value(String text) {
        Label label = new Label(safe(text));
        label.setStyle("-fx-text-fill: " + DARK + ";-fx-font-size: 12px;-fx-font-weight: bold;");
        return label;
    }

    private Label label(String text, int size, boolean bold) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: " + (bold ? DARK : TEXT) + ";-fx-font-size: " + size + "px;" + (bold ? "-fx-font-weight: bold;" : ""));
        return label;
    }

    private Button primaryButton(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + BLUE + ";-fx-text-fill: white;-fx-font-weight: bold;-fx-background-radius: 8;-fx-padding: 10 18 10 18;-fx-cursor: hand;");
        return b;
    }

    private Button secondaryButton(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + WHITE + ";-fx-text-fill: " + DARK + ";-fx-border-color: " + BORDER + ";-fx-border-radius: 8;-fx-background-radius: 8;-fx-padding: 9 15 9 15;-fx-cursor: hand;");
        return b;
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
