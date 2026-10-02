package project.controller;

import com.google.cloud.firestore.DocumentSnapshot;

import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import project.app.WindowManager;
import project.dao.auth.AuthDAO;
import project.ui.admin.DashBoard.AdminDashboard;
import project.ui.landing.LoginPage;
import project.ui.mechanic.MechanicDashboard;
import project.ui.user.UserDashboard;
import project.ui.user.UserSession;

public class LoginController {

    private final AuthDAO authDAO;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public LoginController() {
        authDAO = new AuthDAO();
    }

    // ============================================================
    // LOGIN
    // ============================================================

    public void login(
            String email,
            String password,
            LoginPage loginPage
    ) {

        if (
                email == null ||
                email.trim().isEmpty()
        ) {

            loginPage.showError(
                    "Please enter your email."
            );

            return;
        }

        if (
                password == null ||
                password.trim().isEmpty()
        ) {

            loginPage.showError(
                    "Please enter your password."
            );

            return;
        }

        try {

            String normalizedEmail =
                    email.trim().toLowerCase();

            DocumentSnapshot account =
                    authDAO.findAccountByEmail(
                            normalizedEmail
                    );

            if (account == null) {

                loginPage.showError(
                        "No account found with this email."
                );

                return;
            }

            boolean passwordCorrect =
                    authDAO.checkPassword(
                            account,
                            password
                    );

            if (!passwordCorrect) {

                loginPage.showError(
                        "Incorrect password."
                );

                return;
            }

            String role =
                    authDAO.getRole(
                            account
                    );

            if (
                    role == null ||
                    role.trim().isEmpty()
            ) {

                loginPage.showError(
                        "Account role is missing."
                );

                return;
            }

            role = role.trim();

            String userName =
                    account.getString(
                            "name"
                    );

            String userEmail =
                    account.getString(
                            "email"
                    );

            if (
                    userEmail == null ||
                    userEmail.trim().isEmpty()
            ) {

                userEmail =
                        normalizedEmail;
            }

            userEmail =
                    userEmail.trim().toLowerCase();

            if (
                    userName == null ||
                    userName.trim().isEmpty()
            ) {

                userName = "User";
            }

            String userId;

            if (role.equalsIgnoreCase("Mechanic")
                    || role.equalsIgnoreCase("User")
                    || role.equalsIgnoreCase("Customer")) {

                /*
                 * IMPORTANT FIX:
                 * Customer/Mechanic data flow uses email as canonical ID.
                 * Do not use random Firestore document IDs in session.
                 */
                userId = userEmail;

            } else {

                userId = account.getId();
            }

            UserSession.clear();

            UserSession.setUser(
                    userId,
                    userName,
                    userEmail,
                    role
            );

            System.out.println(
                    "Logged in: role="
                            + role
                            + " | userId="
                            + userId
                            + " | email="
                            + userEmail
            );

            openDashboard(
                    role
            );

        } catch (Exception exception) {

            exception.printStackTrace();

            loginPage.showError(
                    "Unable to login.\n\n"
                            +
                    exception.getMessage()
            );
        }
    }

    // ============================================================
    // OPEN DASHBOARD
    // ============================================================

    private void openDashboard(
            String role
    ) {

        Stage stage =
                LoginPage.getMainStage();

        if (stage == null) {

            System.err.println(
                    "Login navigation error: Main Stage is null."
            );

            return;
        }

        if (
                role.equalsIgnoreCase("Admin")
        ) {

            openAdminDashboard(
                    stage
            );

            return;
        }

        if (
                role.equalsIgnoreCase("Mechanic")
        ) {

            openMechanicDashboard(
                    stage
            );

            return;
        }

        if (
                role.equalsIgnoreCase("User") ||
                role.equalsIgnoreCase("Customer")
        ) {

            openUserDashboard(
                    stage
            );

            return;
        }

        System.err.println(
                "Unknown role: " + role
        );

        UserSession.clear();
    }

    // ============================================================
    // SHOW DASHBOARD FULL SCREEN
    // ============================================================

    private void showDashboardFullScreen(
            Stage stage
    ) {

        if (stage == null) {
            return;
        }

        Platform.runLater(() -> {

            try {

                Rectangle2D bounds =
                        Screen.getPrimary()
                                .getVisualBounds();

                /*
                 * First remove old maximize state.
                 */
                stage.setMaximized(
                        false
                );

                /*
                 * Force the stage to the complete
                 * available desktop area.
                 */
                stage.setX(
                        bounds.getMinX()
                );

                stage.setY(
                        bounds.getMinY()
                );

                stage.setWidth(
                        bounds.getWidth()
                );

                stage.setHeight(
                        bounds.getHeight()
                );

                stage.show();

                /*
                 * Ask Windows/JavaFX to mark it maximized too.
                 */
                stage.setMaximized(
                        true
                );

                stage.toFront();

                /*
                 * One more pass after JavaFX layout.
                 * This handles cases where setMaximized()
                 * is ignored during the first pulse.
                 */
                Platform.runLater(() -> {

                    try {

                        if (
                                !stage.isMaximized()
                        ) {

                            Rectangle2D screenBounds =
                                    Screen.getPrimary()
                                            .getVisualBounds();

                            stage.setX(
                                    screenBounds.getMinX()
                            );

                            stage.setY(
                                    screenBounds.getMinY()
                            );

                            stage.setWidth(
                                    screenBounds.getWidth()
                            );

                            stage.setHeight(
                                    screenBounds.getHeight()
                            );
                        }

                        stage.toFront();

                    } catch (Exception ignored) {
                    }
                });

            } catch (Exception exception) {

                exception.printStackTrace();
            }
        });
    }

    // ============================================================
    // OPEN ADMIN DASHBOARD
    // ============================================================

    private void openAdminDashboard(
            Stage stage
    ) {

        try {

            AdminDashboard adminDashboard =
                    new AdminDashboard(
                            stage, null
                    );

            BorderPane root =
                    adminDashboard.getView();

            if (root == null) {

                System.err.println(
                        "Admin Dashboard error: View is null."
                );

                return;
            }

            Scene scene =
                    new Scene(
                            root
                    );

            stage.setScene(
                    scene
            );

            stage.setTitle(
                    "RoadGuardian - Admin Dashboard"
            );

            stage.show();

            showDashboardFullScreen(
                    stage
            );

        } catch (Exception exception) {

            exception.printStackTrace();

            System.err.println(
                    "Unable to open Admin Dashboard: "
                            +
                    exception.getMessage()
            );
        }
    }

    // ============================================================
    // OPEN USER DASHBOARD
    // ============================================================

    private void openUserDashboard(
            Stage stage
    ) {

        try {

            UserDashboard.dashboardStage =
                    stage;

            UserDashboard dashboard =
                    new UserDashboard();

            Scene scene =
                    dashboard.getDashboardScene();

            if (scene == null) {

                System.err.println(
                        "User Dashboard error: Scene is null."
                );

                return;
            }

            stage.setScene(
                    scene
            );

            stage.setTitle(
                    "RoadGuardian - Customer Dashboard"
            );

            stage.show();

            showDashboardFullScreen(
                    stage
            );

        } catch (Exception exception) {

            exception.printStackTrace();

            System.err.println(
                    "Unable to open User Dashboard: "
                            +
                    exception.getMessage()
            );
        }
    }

    // ============================================================
    // OPEN MECHANIC DASHBOARD
    // ============================================================

    private void openMechanicDashboard(
            Stage stage
    ) {

        try {

            MechanicDashboard dashboard =
                    new MechanicDashboard();

            Scene scene =
                    dashboard.getDashboardScene();

            if (scene == null) {

                System.err.println(
                        "Mechanic Dashboard error: Scene is null."
                );

                return;
            }

            stage.setScene(
                    scene
            );

            stage.setTitle(
                    "RoadGuardian - Mechanic Dashboard"
            );

            stage.show();

            showDashboardFullScreen(
                    stage
            );

        } catch (Exception exception) {

            exception.printStackTrace();

            System.err.println(
                    "Unable to open Mechanic Dashboard: "
                            +
                    exception.getMessage()
            );
        }
    }
}