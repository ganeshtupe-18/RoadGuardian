package project.controller.user;

import project.dao.user.SettingsDAO;
import project.ui.user.SettingsPage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsController {

    private final SettingsDAO settingsDAO;

    public SettingsController() {
        settingsDAO = new SettingsDAO();
    }

    // ============================================================
    // LOAD ALL SETTINGS
    // ============================================================

    public void loadSettings(
            String userId,
            SettingsPage page
    ) {

        if (page == null) {
            return;
        }

        if (isBlank(userId)) {
            page.showError(
                    "User session is missing. Please login again."
            );
            return;
        }

        try {

            Map<String, Object> settings = settingsDAO.getSettings(userId);
            Map<String, Object> vehicle = settingsDAO.getVehicleDetails(userId);
            List<Map<String, Object>> contacts = settingsDAO.getEmergencyContacts(userId);

            page.applyLoadedData(
                    settings == null ? new HashMap<>() : settings,
                    vehicle == null ? new HashMap<>() : vehicle,
                    contacts == null ? List.of() : contacts
            );

        } catch (Exception exception) {

            exception.printStackTrace();

            page.showError(
                    "Unable to load settings.\n\n"
                            + safeMessage(exception)
            );
        }
    }

    // ============================================================
    // SAVE PREFERENCES
    // ============================================================

    public void savePreferences(
            String userId,
            boolean darkMode,
            String language,
            boolean mechanicUpdates,
            boolean documentReminders,
            boolean serviceAlerts,
            boolean offers,
            SettingsPage page
    ) {

        if (page == null) {
            return;
        }

        if (isBlank(userId)) {
            page.showError("User session is missing.");
            return;
        }

        try {

            Map<String, Object> data = new HashMap<>();

            data.put("darkMode", darkMode);
            data.put("language", isBlank(language) ? "English" : language.trim());
            data.put("mechanicStatusUpdates", mechanicUpdates);
            data.put("documentExpiryReminders", documentReminders);
            data.put("serviceDueAlerts", serviceAlerts);
            data.put("offersAndPartnerDeals", offers);

            settingsDAO.saveSettings(userId, data);

            page.showSuccess(
                    "Settings saved successfully."
            );

        } catch (Exception exception) {

            exception.printStackTrace();

            page.showError(
                    "Unable to save settings.\n\n"
                            + safeMessage(exception)
            );
        }
    }

    // ============================================================
    // SAVE VEHICLE - BACKWARD COMPATIBILITY
    // ============================================================

    public void saveVehicle(
            String userId,
            String makeModel,
            String registration,
            String fuelType,
            String year,
            SettingsPage page
    ) {

        saveVehicle(
                userId,
                null,
                makeModel,
                registration,
                fuelType,
                year,
                page
        );
    }

    // ============================================================
    // SAVE VEHICLE - CANONICAL vehicles COLLECTION
    // ============================================================

    public void saveVehicle(
            String userId,
            String vehicleId,
            String makeModel,
            String registration,
            String fuelType,
            String year,
            SettingsPage page
    ) {

        if (page == null) {
            return;
        }

        if (isBlank(userId)) {
            page.showError("User session is missing.");
            return;
        }

        if (isBlank(makeModel)) {
            page.showError("Please enter make and model.");
            return;
        }

        if (isBlank(registration)) {
            page.showError("Please enter registration number.");
            return;
        }

        try {

            settingsDAO.saveVehicleDetails(
                    userId,
                    vehicleId,
                    makeModel,
                    registration,
                    fuelType,
                    year
            );

            page.showSuccess(
                    "Vehicle details saved successfully."
            );

            // Reload so currentVehicleId and display values stay
            // connected to the same canonical vehicle document.
            loadSettings(userId, page);

        } catch (Exception exception) {

            exception.printStackTrace();

            page.showError(
                    "Unable to save vehicle details.\n\n"
                            + safeMessage(exception)
            );
        }
    }

    // ============================================================
    // SAVE CONTACT
    // ============================================================

    public void saveContact(
            String userId,
            String contactId,
            String name,
            String phone,
            SettingsPage page
    ) {

        if (page == null) {
            return;
        }

        if (isBlank(userId)) {
            page.showError("User session is missing.");
            return;
        }

        if (isBlank(name)) {
            page.showError("Contact name is required.");
            return;
        }

        if (isBlank(phone) || !phone.trim().matches("\\d{10}")) {
            page.showError("Contact number must contain exactly 10 digits.");
            return;
        }

        try {

            settingsDAO.saveEmergencyContact(
                    userId,
                    contactId,
                    name,
                    phone
            );

            page.showSuccess(
                    "Emergency contact saved."
            );

            // Important: refresh row with the real Firestore
            // contactId so future edits update instead of duplicate.
            loadSettings(userId, page);

        } catch (Exception exception) {

            exception.printStackTrace();

            page.showError(
                    "Unable to save contact.\n\n"
                            + safeMessage(exception)
            );
        }
    }

    // ============================================================
    // DELETE CONTACT
    // ============================================================

    public void deleteContact(
            String userId,
            String contactId,
            SettingsPage page
    ) {

        if (page == null) {
            return;
        }

        if (isBlank(userId) || isBlank(contactId)) {
            page.showError("Emergency contact ID is missing.");
            return;
        }

        try {

            settingsDAO.deleteEmergencyContact(
                    userId,
                    contactId
            );

            page.showSuccess(
                    "Emergency contact removed."
            );

            loadSettings(userId, page);

        } catch (Exception exception) {

            exception.printStackTrace();

            page.showError(
                    "Unable to remove contact.\n\n"
                            + safeMessage(exception)
            );
        }
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safeMessage(Exception exception) {

        if (exception == null
                || exception.getMessage() == null
                || exception.getMessage().trim().isEmpty()) {
            return "Unknown Firebase error.";
        }

        return exception.getMessage();
    }
}
