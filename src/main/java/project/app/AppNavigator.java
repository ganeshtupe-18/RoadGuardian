package project.app;

import java.io.IOException;

import javafx.scene.Scene;
import javafx.stage.Stage;

import project.ui.user.AIDiagnosisPage;
import project.ui.user.CostEstimatorPage;
import project.ui.user.ComplaintsPage;
import project.ui.user.DocumentsPage;
import project.ui.user.LiveMapPage;
import project.ui.user.MechanicsPage;
import project.ui.user.NotificationsPage;
import project.ui.user.ProfilePage;
import project.ui.user.ReviewsPage;
import project.ui.user.ServiceHistoryPage;
import project.ui.user.SettingsPage;
import project.ui.user.SOSPage;
import project.ui.user.TowTruckPage;
import project.ui.user.UserDashboard;
import project.ui.user.VehiclePage;
import project.ui.user.WomenSafety;

public final class AppNavigator {

    private AppNavigator() {
    }


    // ============================================================
    // NAVIGATE
    // ============================================================

    public static void navigate(
            Stage dashboardStage,
            String pageName
    ) throws IOException {

        // ========================================================
        // STAGE CHECK
        // ========================================================

        if (dashboardStage == null) {

            System.err.println(
                    "Navigation error: Stage is null."
            );

            return;
        }


        // ========================================================
        // PAGE CHECK
        // ========================================================

        if (pageName == null
                || pageName.trim().isEmpty()) {

            System.err.println(
                    "Navigation error: Page name is empty."
            );

            return;
        }


        // ========================================================
        // KEEP DASHBOARD STAGE SYNCHRONIZED
        // ========================================================

        UserDashboard.dashboardStage =
                dashboardStage;


        // ========================================================
        // PREPARE WINDOW
        // ========================================================

        WindowManager.prepareStage(
                dashboardStage
        );


        // ========================================================
        // NAVIGATION
        // ========================================================

        switch (pageName) {


            // ====================================================
            // DASHBOARD
            // ====================================================

            case "Dashboard" -> {

                showDashboard(
                        dashboardStage
                );
            }


            // ====================================================
            // EMERGENCY SOS
            // ====================================================

            case "Emergency SOS" -> {

                show(
                        dashboardStage,
                        "RoadGuardian - Emergency SOS",

                        new SOSPage(() -> {

                            try {

                                navigate(
                                        dashboardStage,
                                        "Dashboard"
                                );

                            } catch (IOException e) {

                                e.printStackTrace();
                            }

                        }).getSOSScene()
                );
            }


            // ====================================================
            // LIVE MAP
            // ====================================================

            case "Live Map" -> {

                show(
                        dashboardStage,
                        "RoadGuardian - Live Tracking",

                        new LiveMapPage()
                                .getLiveMapScene()
                );
            }


            // ====================================================
            // AI DIAGNOSIS
            // ====================================================

            case "AI Diagnosis" -> {

                show(
                        dashboardStage,
                        "RoadGuardian - AI Diagnosis",

                        new AIDiagnosisPage()
                                .getAIDiagnosisScene()
                );
            }


            // ====================================================
            // MECHANICS
            // ====================================================

            case "Mechanics" -> {

                show(
                        dashboardStage,
                        "RoadGuardian - Mechanics",

                        new MechanicsPage()
                                .getMechanicsScene()
                );
            }


            // ====================================================
            // TOW TRUCK
            // ====================================================

            case "Tow Truck" -> {

                show(
                        dashboardStage,
                        "RoadGuardian - Tow Truck",

                        new TowTruckPage()
                                .getTowTruckScene()
                );
            }


            // ====================================================
            // COST ESTIMATOR
            // ====================================================

            case "Cost Estimator" -> {

                show(
                        dashboardStage,
                        "RoadGuardian - Cost Estimator",

                        new CostEstimatorPage()
                                .getCostEstimatorScene()
                );
            }


            // ====================================================
            // MY VEHICLES
            // ====================================================

            case "My Vehicles" -> {

                show(
                        dashboardStage,
                        "RoadGuardian - My Vehicles",

                        new VehiclePage()
                                .getMyVehiclesScene()
                );
            }


            // ====================================================
            // SERVICE HISTORY
            // ====================================================

            case "Service History" -> {

                show(
                        dashboardStage,
                        "RoadGuardian - Service History",

                        new ServiceHistoryPage()
                                .getServiceHistoryScene()
                );
            }


            // ====================================================
            // DOCUMENTS
            // ====================================================

            case "Documents" -> {

                show(
                        dashboardStage,
                        "RoadGuardian - Documents",

                        new DocumentsPage()
                                .getDocumentsScene()
                );
            }


            // ====================================================
            // PROFILE
            // ====================================================

            case "Profile" -> {

                show(
                        dashboardStage,
                        "RoadGuardian - My Profile",

                        new ProfilePage()
                                .getProfileScene()
                );
            }


            // ====================================================
            // WOMEN SAFETY
            // ====================================================

            case "Women Safety" -> {

                show(
                        dashboardStage,
                        "RoadGuardian - Women Safety",

                        new WomenSafety()
                                .getWomenSafetyScene()
                );
            }


            // ====================================================
            // NOTIFICATIONS
            // ====================================================

            case "Notifications" -> {

                show(
                        dashboardStage,
                        "RoadGuardian - Notifications",

                        new NotificationsPage()
                                .getNotificationsScene()
                );
            }


            // ====================================================
            // COMPLAINTS
            // ====================================================

            case "Complaints" -> {

                show(
                        dashboardStage,
                        "RoadGuardian - Complaints",

                        new ComplaintsPage()
                                .getComplaintsScene()
                );
            }


            // ====================================================
            // REVIEWS
            // ====================================================

            case "Reviews" -> {

                show(
                        dashboardStage,
                        "RoadGuardian - Reviews & Ratings",

                        new ReviewsPage()
                                .getReviewsScene()
                );
            }


            // ====================================================
            // SETTINGS
            // ====================================================

            case "Settings" -> {

                show(
                        dashboardStage,
                        "RoadGuardian - Settings",

                        new SettingsPage()
                                .getSettingsScene()
                );
            }


            // ====================================================
            // UNKNOWN PAGE
            // ====================================================

            default -> {

                throw new IllegalArgumentException(
                        "Unknown page: " + pageName
                );
            }
        }
    }


    // ============================================================
    // SHOW DASHBOARD
    // ============================================================

    private static void showDashboard(
            Stage stage
    ) {

        if (stage == null) {
            return;
        }


        // ========================================================
        // KEEP SESSION STAGE
        // ========================================================

        UserDashboard.dashboardStage =
                stage;


        // ========================================================
        // CREATE DASHBOARD
        // ========================================================

        UserDashboard dashboard =
                new UserDashboard();


        Scene scene =
                dashboard.getDashboardScene();


        if (scene == null) {

            System.err.println(
                    "Dashboard navigation error: Scene is null."
            );

            return;
        }


        // ========================================================
        // USE CENTRAL WINDOW MANAGER
        // ========================================================

        WindowManager.show(
                stage,
                "RoadGuardian - Customer Dashboard",
                scene
        );
    }


    // ============================================================
    // SHOW ANY PAGE
    // ============================================================

    private static void show(
            Stage stage,
            String title,
            Scene scene
    ) {

        // ========================================================
        // STAGE CHECK
        // ========================================================

        if (stage == null) {

            System.err.println(
                    "Navigation error: Stage is null."
            );

            return;
        }


        // ========================================================
        // SCENE CHECK
        // ========================================================

        if (scene == null) {

            System.err.println(
                    "Navigation error: Scene is null for "
                            + title
            );

            return;
        }


        // ========================================================
        // KEEP DASHBOARD STAGE SYNCHRONIZED
        // ========================================================

        UserDashboard.dashboardStage =
                stage;


        // ========================================================
        // CENTRAL WINDOW MANAGER
        // ========================================================

        WindowManager.show(
                stage,
                title,
                scene
        );
    }
}