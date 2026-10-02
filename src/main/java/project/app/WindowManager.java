package project.app;

import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public final class WindowManager {

    // ============================================================
    // DEFAULT WINDOW SIZE
    // ============================================================

    public static final double DEFAULT_WIDTH =
            1280;

    public static final double DEFAULT_HEIGHT =
            780;

    public static final double MIN_WIDTH =
            1100;

    public static final double MIN_HEIGHT =
            700;

    // ============================================================
    // PRIVATE CONSTRUCTOR
    // ============================================================

    private WindowManager() {
    }

    // ============================================================
    // PREPARE STAGE
    // ============================================================

    public static void prepareStage(
            Stage stage
    ) {

        if (stage == null) {
            return;
        }

        Rectangle2D bounds =
                Screen.getPrimary()
                        .getVisualBounds();

        double availableWidth =
                bounds.getWidth() - 40;

        double availableHeight =
                bounds.getHeight() - 40;

        double width =
                Math.min(
                        DEFAULT_WIDTH,
                        availableWidth
                );

        double height =
                Math.min(
                        DEFAULT_HEIGHT,
                        availableHeight
                );

        stage.setMinWidth(
                Math.min(
                        MIN_WIDTH,
                        availableWidth
                )
        );

        stage.setMinHeight(
                Math.min(
                        MIN_HEIGHT,
                        availableHeight
                )
        );

        /*
         * Do not resize a maximized/fullscreen stage.
         */

        if (
                !stage.isMaximized()
                        &&
                !stage.isFullScreen()
        ) {

            if (
                    stage.getWidth() <= 0
                            ||
                    stage.getWidth() < MIN_WIDTH
            ) {

                stage.setWidth(
                        width
                );
            }

            if (
                    stage.getHeight() <= 0
                            ||
                    stage.getHeight() < MIN_HEIGHT
            ) {

                stage.setHeight(
                        height
                );
            }
        }
    }

    // ============================================================
    // SHOW
    // ============================================================

    public static void show(
            Stage stage,
            String title,
            Scene scene
    ) {

        if (
                stage == null
                        ||
                scene == null
        ) {

            return;
        }

        boolean wasMaximized =
                stage.isMaximized();

        boolean wasFullScreen =
                stage.isFullScreen();

        double oldWidth =
                stage.getWidth();

        double oldHeight =
                stage.getHeight();

        double oldX =
                stage.getX();

        double oldY =
                stage.getY();

        stage.setTitle(
                title
        );

        stage.setScene(
                scene
        );

        stage.show();

        Platform.runLater(() -> {

            if (
                    stage.getScene() != scene
            ) {

                return;
            }

            // ----------------------------------------------------
            // FULLSCREEN
            // ----------------------------------------------------

            if (wasFullScreen) {

                stage.setFullScreen(
                        true
                );

                stage.toFront();

                return;
            }

            // ----------------------------------------------------
            // MAXIMIZED
            // ----------------------------------------------------

            if (wasMaximized) {

                stage.setMaximized(
                        true
                );

                stage.toFront();

                return;
            }

            // ----------------------------------------------------
            // NORMAL WINDOW
            // ----------------------------------------------------

            stage.setMaximized(
                    false
            );

            if (oldWidth > 0) {

                stage.setWidth(
                        Math.max(
                                MIN_WIDTH,
                                oldWidth
                        )
                );
            }

            if (oldHeight > 0) {

                stage.setHeight(
                        Math.max(
                                MIN_HEIGHT,
                                oldHeight
                        )
                );
            }

            if (
                    !Double.isNaN(oldX)
                            &&
                    !Double.isNaN(oldY)
                            &&
                    oldX != 0
                            &&
                    oldY != 0
            ) {

                stage.setX(
                        oldX
                );

                stage.setY(
                        oldY
                );
            }

            stage.show();

            stage.toFront();
        });
    }

    // ============================================================
    // CREATE SCENE
    // ============================================================

    public static Scene createScene(
            Stage stage,
            Parent root
    ) {

        if (root == null) {
            return null;
        }

        /*
         * IMPORTANT:
         *
         * Do not force dashboard dimensions.
         *
         * Scene(root) lets the Stage determine its final size.
         */

        return new Scene(
                root
        );
    }
}