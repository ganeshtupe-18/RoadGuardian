package project.ui.user;

/**
 * Stores the currently logged-in customer's session data.
 *
 * This class does not communicate with Firebase.
 * LoginController sets the session after successful login.
 *
 * Other customer-side pages can read the logged-in user's
 * basic information through this class.
 */
public final class UserSession {

    // ============================================================
    // CURRENT USER DATA
    // ============================================================

    private static String userId;
    private static String userName;
    private static String userEmail;
    private static String userRole;

    // ============================================================
    // PRIVATE CONSTRUCTOR
    // ============================================================

    private UserSession() {
        // Utility class
    }

    // ============================================================
    // SET USER SESSION
    // ============================================================

    public static void setUser(
            String id,
            String name,
            String email,
            String role
    ) {

        userEmail = normalizeEmail(email);

        if (userEmail != null
                && role != null
                && (role.equalsIgnoreCase("Mechanic")
                || role.equalsIgnoreCase("User")
                || role.equalsIgnoreCase("Customer"))) {

            userId = userEmail;

        } else {

            userId = clean(id);
        }

        userName = clean(name);
        userRole = clean(role);
    }

    // ============================================================
    // USER ID
    // ============================================================

    public static String getUserId() {

        return userId;
    }

    // ============================================================
    // USER NAME
    // ============================================================

    public static String getUserName() {

        return userName;
    }

    // ============================================================
    // USER EMAIL
    // ============================================================

    public static String getUserEmail() {

        return userEmail;
    }

    // ============================================================
    // USER ROLE
    // ============================================================

    public static String getUserRole() {

        return userRole;
    }

    // ============================================================
    // CHECK LOGIN
    // ============================================================

    public static boolean isLoggedIn() {

        return userId != null
                && !userId.trim().isEmpty();
    }

    // ============================================================
    // NORMALIZE EMAIL
    // ============================================================

    private static String normalizeEmail(String email) {

        String cleaned = clean(email);

        if (cleaned == null) {
            return null;
        }

        return cleaned.toLowerCase();
    }

    // ============================================================
    // CLEAN
    // ============================================================

    private static String clean(String value) {

        if (value == null) {
            return null;
        }

        String cleaned = value.trim();

        return cleaned.isEmpty()
                ? null
                : cleaned;
    }

    // ============================================================
    // CLEAR SESSION
    // ============================================================

    public static void clear() {

        userId = null;
        userName = null;
        userEmail = null;
        userRole = null;
    }
}