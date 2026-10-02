package project.dao.user;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;

import project.firebase.FirebaseConfig;

import java.util.HashMap;
import java.util.Map;

public class ProfileDAO {

    private static final String USERS = "users";
    private static final String CUSTOMERS = "customers";

    private final Firestore firestore;

    public ProfileDAO() throws Exception {
        firestore = FirebaseConfig.getFirestore();
    }

    // ============================================================
    // GET PROFILE
    // users/{email} = login source of truth
    // customers = admin compatibility / extra customer fields
    // ============================================================

    public Map<String, Object> getProfile(String email) throws Exception {

        Map<String, Object> result = new HashMap<>();

        String normalizedEmail = normalizeEmail(email);

        if (normalizedEmail == null) {
            return result;
        }

        // --------------------------------------------------------
        // 1) Registered user document
        // --------------------------------------------------------

        DocumentSnapshot userDocument = firestore
                .collection(USERS)
                .document(normalizedEmail)
                .get()
                .get();

        if (userDocument.exists() && userDocument.getData() != null) {
            result.putAll(userDocument.getData());
        }

        // --------------------------------------------------------
        // 2) Customer/admin copy. Only fill missing values so the
        //    registered users document remains authoritative.
        // --------------------------------------------------------

        DocumentSnapshot customerDocument = findCustomerByEmail(normalizedEmail);

        if (customerDocument != null
                && customerDocument.exists()
                && customerDocument.getData() != null) {

            mergeMissing(result, customerDocument.getData());
            result.put("customerId", customerDocument.getId());
        }

        result.put("email", normalizedEmail);

        if (isBlank(result.get("role"))) {
            result.put("role", "Customer");
        }

        return result;
    }

    // ============================================================
    // UPDATE PROFILE
    // Updates the registered users document and, if an Admin-side
    // customer document exists, keeps that record in sync too.
    // ============================================================

    public boolean updateProfile(
            String email,
            String name,
            String phone
    ) throws Exception {

        String normalizedEmail = normalizeEmail(email);

        if (normalizedEmail == null) {
            return false;
        }

        String cleanName = clean(name);
        String cleanPhone = clean(phone);

        Map<String, Object> updates = new HashMap<>();
        updates.put("email", normalizedEmail);
        updates.put("name", cleanName == null ? "" : cleanName);
        updates.put("phone", cleanPhone == null ? "" : cleanPhone);
        updates.put("updatedAt", String.valueOf(System.currentTimeMillis()));

        // --------------------------------------------------------
        // Registered login/profile record. set(..., merge) is used
        // instead of update() so a missing document does not crash.
        // --------------------------------------------------------

        firestore
                .collection(USERS)
                .document(normalizedEmail)
                .set(updates, SetOptions.merge())
                .get();

        // --------------------------------------------------------
        // Existing Admin customer record, if present.
        // We do not create a second customer record unnecessarily.
        // --------------------------------------------------------

        DocumentSnapshot customerDocument = findCustomerByEmail(normalizedEmail);

        if (customerDocument != null && customerDocument.exists()) {
            customerDocument
                    .getReference()
                    .set(updates, SetOptions.merge())
                    .get();
        }

        return true;
    }

    // ============================================================
    // FIND CUSTOMER BY EMAIL
    // ============================================================

    private DocumentSnapshot findCustomerByEmail(String email) throws Exception {

        if (email == null) {
            return null;
        }

        // Fast path: customers/{email}
        DocumentSnapshot direct = firestore
                .collection(CUSTOMERS)
                .document(email)
                .get()
                .get();

        if (direct.exists()) {
            return direct;
        }

        // Admin-created customer documents may use random IDs.
        QuerySnapshot query = firestore
                .collection(CUSTOMERS)
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .get();

        if (!query.isEmpty()) {
            return query.getDocuments().get(0);
        }

        return null;
    }

    // ============================================================
    // MERGE ONLY MISSING VALUES
    // ============================================================

    private void mergeMissing(
            Map<String, Object> target,
            Map<String, Object> source
    ) {

        if (target == null || source == null) {
            return;
        }

        for (Map.Entry<String, Object> entry : source.entrySet()) {

            if (entry.getKey() == null) {
                continue;
            }

            Object current = target.get(entry.getKey());

            if (isBlank(current) && !isBlank(entry.getValue())) {
                target.put(entry.getKey(), entry.getValue());
            }
        }
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private String normalizeEmail(String email) {

        String value = clean(email);

        return value == null
                ? null
                : value.toLowerCase();
    }

    private String clean(String value) {

        if (value == null) {
            return null;
        }

        String cleaned = value.trim();

        return cleaned.isEmpty()
                ? null
                : cleaned;
    }

    private boolean isBlank(Object value) {

        return value == null
                || String.valueOf(value).trim().isEmpty();
    }
}
