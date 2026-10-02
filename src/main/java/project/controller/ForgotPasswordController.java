package project.controller;

import project.dao.auth.ForgotPasswordDAO;

public class ForgotPasswordController {

    private final ForgotPasswordDAO forgotPasswordDAO;

    public ForgotPasswordController() {
        forgotPasswordDAO = new ForgotPasswordDAO();
    }

    public String resetPassword(String email) {

        if (email == null || email.trim().isEmpty()) {
            return "Please enter your email address.";
        }

        email = email.trim().toLowerCase();

        if (!email.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return "Please enter a valid email address.";
        }

        try {

            if (!forgotPasswordDAO.accountExists(email)) {
                return "No account found with this email address.";
            }

            /*
             * Firebase Authentication password reset
             * integration can be added here.
             */

            return "SUCCESS";

        } catch (Exception e) {

            e.printStackTrace();

            return "Unable to process your request.\n\n"
                    + e.getMessage();
        }
    }
}