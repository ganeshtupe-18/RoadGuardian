package project.controller.user;

import project.dao.user.ProfileDAO;

import java.util.HashMap;
import java.util.Map;

public class ProfileController {

    private final ProfileDAO profileDAO;

    public ProfileController() {

        try {
            profileDAO = new ProfileDAO();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Unable to initialize ProfileDAO",
                    e
            );
        }
    }

    // ============================================================
    // GET PROFILE
    // ============================================================

    public Map<String, Object> getProfile(String email) {

        String normalizedEmail = normalizeEmail(email);

        if (normalizedEmail == null) {
            return new HashMap<>();
        }

        try {
            return profileDAO.getProfile(normalizedEmail);

        } catch (Exception e) {

            System.err.println(
                    "Profile load error: " + e.getMessage()
            );

            e.printStackTrace();
            return new HashMap<>();
        }
    }

    // ============================================================
    // UPDATE PROFILE
    // ============================================================

    public boolean updateProfile(
            String email,
            String name,
            String phone
    ) {

        String normalizedEmail = normalizeEmail(email);

        if (normalizedEmail == null) {
            return false;
        }

        String validation = validateProfile(name, phone);

        if (validation != null) {
            return false;
        }

        try {
            return profileDAO.updateProfile(
                    normalizedEmail,
                    name.trim(),
                    phone.trim()
            );

        } catch (Exception e) {

            System.err.println(
                    "Profile update error: " + e.getMessage()
            );

            e.printStackTrace();
            return false;
        }
    }

    // ============================================================
    // VALIDATE PROFILE
    // ============================================================

    public String validateProfile(
            String name,
            String phone
    ) {

        if (name == null || name.trim().isEmpty()) {
            return "Name is required.";
        }

        if (name.trim().length() < 2) {
            return "Name must contain at least 2 characters.";
        }

        if (phone == null || phone.trim().isEmpty()) {
            return "Phone number is required.";
        }

        String cleanPhone = phone.trim();

        if (!cleanPhone.matches("\\d{10}")) {
            return "Phone number must contain exactly 10 digits.";
        }

        return null;
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private String normalizeEmail(String email) {

        if (email == null) {
            return null;
        }

        String cleaned = email.trim();

        return cleaned.isEmpty()
                ? null
                : cleaned.toLowerCase();
    }
}
