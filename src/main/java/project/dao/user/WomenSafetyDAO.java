package project.dao.user;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;

import project.firebase.FirebaseConfig;
import project.model.SOSRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Customer Women Safety data access.
 *
 * IMPORTANT:
 * Women Safety SOS does NOT use a separate SOS collection.
 * It creates the same canonical SOSRequests document used by
 * Customer SOS, Admin SOS and Mechanic SOS.
 */
public class WomenSafetyDAO {

    private static final String USERS_COLLECTION = "users";
    private static final String CUSTOMERS_COLLECTION = "customers";
    private static final String SOS_COLLECTION = "SOSRequests";
    private static final String ACTIONS_COLLECTION = "womenSafetyActions";

    private final Firestore firestore;
    private final SOSDAO sosDAO;

    public WomenSafetyDAO() throws IOException {
        this.firestore = FirebaseConfig.getFirestore();
        this.sosDAO = new SOSDAO(firestore);
    }

    public WomenSafetyDAO(Firestore firestore) {
        if (firestore == null) {
            throw new IllegalArgumentException("Firestore cannot be null.");
        }

        this.firestore = firestore;
        this.sosDAO = new SOSDAO(firestore);
    }

    // ============================================================
    // EMERGENCY CONTACTS
    // ============================================================

    public List<Map<String, Object>> getEmergencyContacts(
            String userEmail
    ) throws Exception {

        List<Map<String, Object>> contacts = new ArrayList<>();
        String customerId = normalizeId(userEmail);

        if (customerId == null) {
            return contacts;
        }

        /*
         * First use the canonical user profile format already used by SOSDAO
         * (emergencyContacts array inside users/{id}).
         */
        try {
            contacts.addAll(sosDAO.getEmergencyContacts(customerId));
        } catch (Exception ignored) {
            // Legacy subcollection fallback below.
        }

        if (!contacts.isEmpty()) {
            return contacts;
        }

        /*
         * Legacy compatibility:
         * users/{email}/emergencyContacts/{contactId}
         */
        DocumentSnapshot userDocument = findUserDocument(customerId);

        if (userDocument == null || !userDocument.exists()) {
            return contacts;
        }

        QuerySnapshot result = userDocument.getReference()
                .collection("emergencyContacts")
                .get()
                .get();

        for (DocumentSnapshot document : result.getDocuments()) {
            Map<String, Object> source = document.getData();

            if (source == null) {
                continue;
            }

            Map<String, Object> copy = new HashMap<>(source);
            copy.put("id", document.getId());
            contacts.add(copy);
        }

        return contacts;
    }

    // ============================================================
    // WOMEN SAFETY MODE
    // ============================================================

    public boolean isSafetyModeEnabled(
            String userEmail
    ) throws Exception {

        String customerId = normalizeId(userEmail);

        if (customerId == null) {
            return false;
        }

        DocumentSnapshot document = findUserDocument(customerId);

        if (document == null || !document.exists()) {
            return false;
        }

        Boolean enabled = document.getBoolean("womenSafetyMode");
        return enabled != null && enabled;
    }

    public void updateSafetyMode(
            String userEmail,
            boolean enabled
    ) throws Exception {

        String customerId = normalizeId(userEmail);

        if (customerId == null) {
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("womenSafetyMode", enabled);
        data.put("womenSafetyUpdatedAt", System.currentTimeMillis());

        DocumentSnapshot user = findUserDocument(customerId);

        if (user != null && user.exists()) {
            user.getReference().set(data, SetOptions.merge()).get();
            return;
        }

        /*
         * Customer auth documents are normally keyed by normalized email.
         * If no document was found, create/merge only the safety fields.
         */
        firestore.collection(USERS_COLLECTION)
                .document(customerId)
                .set(data, SetOptions.merge())
                .get();
    }

    // ============================================================
    // CREATE CANONICAL WOMEN SAFETY SOS
    // ============================================================

    public String createSOSRequest(
            String userEmail
    ) throws Exception {

        String customerId = normalizeId(userEmail);

        if (customerId == null) {
            throw new IllegalArgumentException("Logged-in customer is required.");
        }

        SOSRequest active = sosDAO.getActiveSOSRequest(customerId);

        if (active != null) {
            if (isWomenSafetySOS(active)) {
                return active.getSosId();
            }

            throw new IllegalStateException(
                    "Another emergency SOS is already active. Resolve or cancel it before creating a Women Safety SOS."
            );
        }

        String customerName = resolveCustomerName(customerId);

        /*
         * Use SOSDAO so the canonical lifecycle fields, timeline,
         * customer aliases and duplicate-active-SOS protection stay identical
         * to the normal Customer SOS flow.
         */
        String sosId = sosDAO.createSOSRequest(
                customerId,
                customerName,
                customerId,
                "Location not available",
                "",
                ""
        );

        long now = System.currentTimeMillis();

        Map<String, Object> updates = new HashMap<>();
        updates.put("emergencyType", "Women Safety SOS");
        updates.put("description", "Emergency assistance requested from Women Safety mode");
        updates.put("source", "Women Safety");
        updates.put("womenSafety", true);
        updates.put("priority", "Emergency");
        updates.put("updatedAt", now);

        firestore.collection(SOS_COLLECTION)
                .document(sosId)
                .set(updates, SetOptions.merge())
                .get();

        updateSafetyMode(customerId, true);

        logSafetyEvent(
                customerId,
                sosId,
                "Women Safety SOS activated"
        );

        System.out.println(
                "Women Safety SOS created: " + sosId
                        + " | customerId=" + customerId
                        + " | collection=" + SOS_COLLECTION
                        + " | status=Pending"
        );

        return sosId;
    }

    // ============================================================
    // ACTIVE SOS
    // ============================================================

    public SOSRequest getActiveSOSRequest(
            String userEmail
    ) throws Exception {

        String customerId = normalizeId(userEmail);

        if (customerId == null) {
            return null;
        }

        return sosDAO.getActiveSOSRequest(customerId);
    }

    public boolean hasActiveSOS(
            String userEmail
    ) throws Exception {

        return getActiveSOSRequest(userEmail) != null;
    }

    public boolean cancelSOS(
            String sosId
    ) throws Exception {

        String id = clean(sosId);

        if (id == null) {
            return false;
        }

        boolean cancelled = sosDAO.cancelSOS(id);

        if (cancelled) {
            logSafetyEvent("", id, "Women Safety SOS cancelled");
        }

        return cancelled;
    }

    // ============================================================
    // CONTACT ACTION LOG
    // ============================================================

    public void logContactAction(
            String userEmail,
            String contactName,
            String action
    ) throws Exception {

        String customerId = normalizeId(userEmail);

        if (customerId == null) {
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("customerId", customerId);
        data.put("userEmail", customerId);
        data.put("contactName", valueOrEmpty(contactName));
        data.put("action", valueOrEmpty(action));
        data.put("source", "Women Safety");
        data.put("createdAt", System.currentTimeMillis());

        firestore.collection(ACTIONS_COLLECTION)
                .add(data)
                .get();
    }

    private void logSafetyEvent(
            String customerId,
            String sosId,
            String action
    ) throws Exception {

        Map<String, Object> data = new HashMap<>();
        data.put("customerId", valueOrEmpty(normalizeId(customerId)));
        data.put("sosId", valueOrEmpty(sosId));
        data.put("action", valueOrEmpty(action));
        data.put("source", "Women Safety");
        data.put("createdAt", System.currentTimeMillis());

        firestore.collection(ACTIONS_COLLECTION)
                .add(data)
                .get();
    }

    // ============================================================
    // USER LOOKUP
    // ============================================================

    private DocumentSnapshot findUserDocument(
            String customerId
    ) throws Exception {

        String lookup = normalizeId(customerId);

        if (lookup == null) {
            return null;
        }

        DocumentSnapshot direct = firestore.collection(USERS_COLLECTION)
                .document(lookup)
                .get()
                .get();

        if (direct.exists()) {
            return direct;
        }

        QuerySnapshot users = firestore.collection(USERS_COLLECTION)
                .get()
                .get();

        for (DocumentSnapshot document : users.getDocuments()) {
            if (matchesCustomer(document, lookup)) {
                return document;
            }
        }

        /*
         * Admin side may also have a top-level customers collection.
         */
        DocumentSnapshot directCustomer = firestore.collection(CUSTOMERS_COLLECTION)
                .document(lookup)
                .get()
                .get();

        if (directCustomer.exists()) {
            return directCustomer;
        }

        QuerySnapshot customers = firestore.collection(CUSTOMERS_COLLECTION)
                .get()
                .get();

        for (DocumentSnapshot document : customers.getDocuments()) {
            if (matchesCustomer(document, lookup)) {
                return document;
            }
        }

        return null;
    }

    private boolean matchesCustomer(
            DocumentSnapshot document,
            String lookup
    ) {

        if (document == null || lookup == null) {
            return false;
        }

        String documentId = normalizeId(document.getId());
        String email = normalizeId(stringValue(document.get("email")));
        String userEmail = normalizeId(stringValue(document.get("userEmail")));
        String userId = normalizeId(stringValue(document.get("userId")));
        String customerId = normalizeId(stringValue(document.get("customerId")));

        return sameId(lookup, documentId)
                || sameId(lookup, email)
                || sameId(lookup, userEmail)
                || sameId(lookup, userId)
                || sameId(lookup, customerId);
    }

    private String resolveCustomerName(
            String customerId
    ) throws Exception {

        DocumentSnapshot document = findUserDocument(customerId);

        if (document == null || !document.exists()) {
            return customerId;
        }

        return firstNonBlank(
                stringValue(document.get("name")),
                stringValue(document.get("fullName")),
                stringValue(document.get("customerName")),
                customerId
        );
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private boolean isWomenSafetySOS(
            SOSRequest request
    ) {

        if (request == null) {
            return false;
        }

        String type = clean(request.getEmergencyType());

        return type != null
                && type.equalsIgnoreCase("Women Safety SOS");
    }

    private String normalizeId(String value) {
        String cleaned = clean(value);

        if (cleaned == null) {
            return null;
        }

        return cleaned.contains("@")
                ? cleaned.toLowerCase()
                : cleaned;
    }

    private boolean sameId(String first, String second) {
        String a = normalizeId(first);
        String b = normalizeId(second);
        return a != null && b != null && a.equalsIgnoreCase(b);
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String valueOrEmpty(String value) {
        String cleaned = clean(value);
        return cleaned == null ? "" : cleaned;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }

        for (String value : values) {
            String cleaned = clean(value);
            if (cleaned != null) {
                return cleaned;
            }
        }

        return null;
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }

        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }
}
