// package project.controller;

// import javafx.scene.Scene;
// import javafx.stage.Stage;

// import project.ui.landing.LandingPage;
// import project.ui.landing.LoginPage;
// import project.ui.landing.RegisterPage;
// import project.ui.landing.MechanicRegisterPage;

// public class LandingController {

//     private final Stage stage;

// //     public LandingController(Stage stage) {
// //         this.stage = stage;
// //     }

//     // =====================================================
//     // LOGIN
//     // =====================================================

//     public void openLoginPage() {

//         LoginPage loginPage =
//                 new LoginPage(stage);

//         Scene scene =
//                 new Scene(
//                         loginPage.getView(),
//                         1400,
//                         800
//                 );

//         stage.setScene(scene);
//         stage.setTitle("RoadGuardian - Login");
//         stage.show();
//     }

//     // =====================================================
//     // CREATE ACCOUNT
//     // =====================================================

//     public void openRegisterPage() {

//         System.out.println("Create Account clicked");

//         RegisterPage registerPage =
//                 new RegisterPage(stage);

//         Scene scene =
//                 new Scene(
//                         registerPage.getView(),
//                         1400,
//                         800
//                 );

//         stage.setScene(scene);
//         stage.setTitle("RoadGuardian - Create Account");
//         stage.show();
//     }

//     // =====================================================
//     // BECOME A MECHANIC
//     // =====================================================

//     public void openMechanicRegisterPage() {

//         System.out.println("Become a Mechanic clicked");

//         MechanicRegisterPage mechanicRegisterPage =
//                 new MechanicRegisterPage(stage);

//         Scene scene =
//                 new Scene(
//                         mechanicRegisterPage.getView(),
//                         1400,
//                         800
//                 );

//         stage.setScene(scene);

//         stage.setTitle(
//                 "RoadGuardian - Mechanic Registration"
//         );

//         stage.show();
//     }
// }