package project.controller;

import project.dao.auth.RegisterDAO;

public class RegisterController {

    private final RegisterDAO registerDAO;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public RegisterController() {

        registerDAO =
                new RegisterDAO();
    }


    // =====================================================
    // REGISTER
    // =====================================================

    public String register(
            String name,
            String email,
            String phone,
            String role,
            String password,
            String confirmPassword
    ) {

        // -------------------------------------------------
        // NULL SAFETY
        // -------------------------------------------------

        if (name == null) {
            name = "";
        }

        if (email == null) {
            email = "";
        }

        if (phone == null) {
            phone = "";
        }

        if (role == null) {
            role = "";
        }

        if (password == null) {
            password = "";
        }

        if (confirmPassword == null) {
            confirmPassword = "";
        }


        // -------------------------------------------------
        // EMPTY VALIDATION
        // -------------------------------------------------

        if (
                name.trim().isEmpty()
                        ||
                email.trim().isEmpty()
                        ||
                phone.trim().isEmpty()
                        ||
                role.trim().isEmpty()
                        ||
                password.isEmpty()
                        ||
                confirmPassword.isEmpty()
        ) {

            return "Please fill all fields.";
        }


        // -------------------------------------------------
        // EMAIL VALIDATION
        // -------------------------------------------------

        if (!email.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        )) {

            return "Please enter a valid email address.";
        }


        // -------------------------------------------------
        // PHONE VALIDATION
        // -------------------------------------------------

        if (!phone.matches(
                "\\d{10}"
        )) {

            return "Phone number must contain exactly 10 digits.";
        }


        // -------------------------------------------------
        // PASSWORD VALIDATION
        // -------------------------------------------------

        if (password.length() < 6) {

            return "Password must contain at least 6 characters.";
        }


        // -------------------------------------------------
        // PASSWORD MATCH
        // -------------------------------------------------

        if (!password.equals(confirmPassword)) {

            return "Password and confirm password do not match.";
        }


        // -------------------------------------------------
        // NORMALIZE ROLE
        // -------------------------------------------------

        if (role.equalsIgnoreCase("Mechanic")) {

            role = "Mechanic";

        } else {

            role = "User";
        }


        try {

            // -------------------------------------------------
            // CHECK EXISTING ACCOUNT
            // -------------------------------------------------

            if (
                    registerDAO.accountExists(
                            email,
                            role
                    )
            ) {

                return
                        "An account with this email already exists as "
                                + role
                                + ".";
            }


            // -------------------------------------------------
            // CREATE ACCOUNT
            // -------------------------------------------------

            registerDAO.createAccount(
                    name,
                    email,
                    phone,
                    role,
                    password
            );


            return "SUCCESS";


        } catch (Exception exception) {

            exception.printStackTrace();

            return
                    "Unable to create account.\n\n"
                            + exception.getMessage();
        }
    }
}