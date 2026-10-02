package project.ui.user;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Window;

/**
 * Presentation-only helper for ComboBox controls used by User pages.
 * Keeps the popup, popup cells and selected value readable in the shared
 * RoadGuardian dark theme without changing any data or navigation logic.
 */
public final class UserDarkComboBox {

    private static final String SURFACE = "#242424";
    private static final String SURFACE_DARK = "#1A1A1A";
    private static final String HOVER = "#3A2A0C";
    private static final String TEXT = "#F3F4F6";
    private static final String ACCENT = "#FBBF24";
    private static final String BORDER = "#F59E0B";

    private UserDarkComboBox() {
    }

    public static <T> void style(ComboBox<T> comboBox) {
        if (comboBox == null) {
            return;
        }

        comboBox.setStyle(
                "-fx-background-color: " + SURFACE + ";" +
                "-fx-control-inner-background: " + SURFACE + ";" +
                "-fx-text-fill: " + TEXT + ";" +
                "-fx-prompt-text-fill: #9A9AA3;" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-mark-color: " + TEXT + ";"
        );

        comboBox.setCellFactory(listView -> createCell());
        comboBox.setButtonCell(createCell());

        comboBox.setOnShown(event ->
                Platform.runLater(() -> stylePopup(comboBox))
        );
    }

    private static <T> ListCell<T> createCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.valueOf(item));
                }

                setStyle(
                        "-fx-background-color: " + SURFACE_DARK + ";" +
                        "-fx-text-fill: " + TEXT + ";" +
                        "-fx-font-size: 11px;" +
                        "-fx-padding: 7 10 7 10;"
                );
            }
        };
    }

    private static void stylePopup(ComboBox<?> comboBox) {
        for (Window window : Window.getWindows()) {
            if (!window.isShowing() || window == comboBox.getScene().getWindow()) {
                continue;
            }

            if (window.getScene() == null) {
                continue;
            }

            Parent root = window.getScene().getRoot();
            ListView<?> listView = findListView(root);

            if (listView == null) {
                continue;
            }

            listView.setStyle(
                    "-fx-background-color: " + SURFACE_DARK + ";" +
                    "-fx-control-inner-background: " + SURFACE_DARK + ";" +
                    "-fx-border-color: " + BORDER + ";"
            );

            for (Node node : listView.lookupAll(".list-cell")) {
                if (node instanceof ListCell<?> cell) {
                    cell.setStyle(
                            "-fx-background-color: " + SURFACE_DARK + ";" +
                            "-fx-text-fill: " + TEXT + ";" +
                            "-fx-font-size: 11px;"
                    );
                }
            }
        }
    }

    private static ListView<?> findListView(Node node) {
        if (node instanceof ListView<?> listView) {
            return listView;
        }

        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                ListView<?> result = findListView(child);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }
}
