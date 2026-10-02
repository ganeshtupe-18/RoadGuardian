package project;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import project.ui.landing.LandingPage;

public class Main {

    public static void main(String[] args) {
        System.out.println("Hello World!");
        Application.launch(LandingPage.class, args);
    }
}