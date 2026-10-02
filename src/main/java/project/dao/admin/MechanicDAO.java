package project.dao.admin;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;

import project.model.Mechanic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MechanicDAO {

    private final Firestore firestore;

    private static final String COLLECTION =
            "mechanics";

    public MechanicDAO(Firestore firestore) {
        this.firestore = firestore;
    }

    // =====================================================
    // GET ALL MECHANICS
    // =====================================================

    public List<Mechanic> getAllMechanics()
            throws Exception {

        List<Mechanic> mechanics =
                new ArrayList<>();

        ApiFuture<QuerySnapshot> future =
                firestore
                        .collection(COLLECTION)
                        .get();

        QuerySnapshot snapshot =
                future.get();

        for (DocumentSnapshot document :
                snapshot.getDocuments()) {

            Mechanic mechanic =
                    toMechanic(document);

            if (mechanic != null) {

                mechanics.add(mechanic);
            }
        }

        return mechanics;
    }

    // =====================================================
    // GET SINGLE MECHANIC
    // =====================================================

    public Mechanic getMechanicById(
            String mechanicId
    ) throws Exception {

        mechanicId =
                normalizeId(mechanicId);

        if (mechanicId == null) {

            return null;
        }

        // First try canonical document id.
        DocumentSnapshot document =
                firestore
                        .collection(COLLECTION)
                        .document(mechanicId)
                        .get()
                        .get();

        if (document.exists()) {

            return toMechanic(document);
        }

        // Backward compatibility for old random-id documents.
        QuerySnapshot byMechanicId =
                firestore
                        .collection(COLLECTION)
                        .whereEqualTo("mechanicId", mechanicId)
                        .limit(1)
                        .get()
                        .get();

        if (!byMechanicId.isEmpty()) {

            return toMechanic(
                    byMechanicId.getDocuments().get(0)
            );
        }

        QuerySnapshot byEmail =
                firestore
                        .collection(COLLECTION)
                        .whereEqualTo("email", mechanicId)
                        .limit(1)
                        .get()
                        .get();

        if (!byEmail.isEmpty()) {

            return toMechanic(
                    byEmail.getDocuments().get(0)
            );
        }

        return null;
    }

    // =====================================================
    // ADD MECHANIC
    //
    // IMPORTANT FIX:
    // Admin-created mechanics also use email as mechanicId.
    // This makes Customer assignment and Mechanic login match.
    // =====================================================

    public boolean addMechanic(
            Mechanic mechanic
    ) throws Exception {

        if (mechanic == null) {

            return false;
        }

        String email =
                normalizeId(mechanic.getEmail());

        if (email == null
                || !email.contains("@")) {

            return false;
        }

        mechanic.setEmail(email);
        mechanic.setMechanicId(email);

        if (clean(mechanic.getStatus()) == null) {

            mechanic.setStatus("Active");
        }

        if (clean(mechanic.getCreatedAt()) == null) {

            mechanic.setCreatedAt(currentTime());
        }

        Map<String, Object> data =
                toMap(mechanic);

        /*
         * Admin Add Mechanic page has no password field.
         * To make the created mechanic login-compatible in this
         * college project, default password = phone number.
         * Mechanic can later be moved to Firebase Auth.
         */
        data.put("role", "Mechanic");

        if (clean(stringValue(data.get("password"))) == null) {

            data.put("password", clean(mechanic.getPhone()) == null
                    ? email
                    : mechanic.getPhone().trim());
        }

        firestore
                .collection(COLLECTION)
                .document(email)
                .set(data, SetOptions.merge())
                .get();

        return true;
    }

    // =====================================================
    // UPDATE MECHANIC
    // =====================================================

    public boolean updateMechanic(
            Mechanic mechanic
    ) throws Exception {

        if (mechanic == null) {

            return false;
        }

        String email =
                normalizeId(mechanic.getEmail());

        String mechanicId =
                normalizeId(mechanic.getMechanicId());

        String canonicalId =
                email != null && email.contains("@")
                        ? email
                        : mechanicId;

        if (canonicalId == null) {

            return false;
        }

        mechanic.setMechanicId(canonicalId);

        if (email != null) {

            mechanic.setEmail(email);
        }

        Map<String, Object> data =
                toMap(mechanic);

        data.put("role", "Mechanic");

        firestore
                .collection(COLLECTION)
                .document(canonicalId)
                .set(data, SetOptions.merge())
                .get();

        // If an old random document existed, keep it harmless by
        // also updating its mechanicId/email. Do not delete silently.
        if (mechanicId != null
                && !mechanicId.equals(canonicalId)) {

            firestore
                    .collection(COLLECTION)
                    .document(mechanicId)
                    .set(data, SetOptions.merge())
                    .get();
        }

        return true;
    }

    // =====================================================
    // DELETE MECHANIC
    // =====================================================

    public boolean deleteMechanic(
            String mechanicId
    ) throws Exception {

        mechanicId =
                normalizeId(mechanicId);

        if (mechanicId == null) {

            return false;
        }

        firestore
                .collection(COLLECTION)
                .document(mechanicId)
                .delete()
                .get();

        return true;
    }

    // =====================================================
    // SEARCH MECHANICS
    // =====================================================

    public List<Mechanic> searchMechanics(
            String searchText
    ) throws Exception {

        List<Mechanic> allMechanics =
                getAllMechanics();

        List<Mechanic> result =
                new ArrayList<>();

        if (searchText == null
                || searchText.isBlank()) {

            return allMechanics;
        }

        String search =
                searchText
                        .trim()
                        .toLowerCase();

        for (Mechanic mechanic :
                allMechanics) {

            if (contains(mechanic.getMechanicId(), search)
                    || contains(mechanic.getName(), search)
                    || contains(mechanic.getEmail(), search)
                    || contains(mechanic.getPhone(), search)
                    || contains(mechanic.getCity(), search)
                    || contains(mechanic.getSpecialization(), search)) {

                result.add(mechanic);
            }
        }

        return result;
    }

    // =====================================================
    // FILTER BY STATUS
    // =====================================================

    public List<Mechanic> getMechanicsByStatus(
            String status
    ) throws Exception {

        List<Mechanic> allMechanics =
                getAllMechanics();

        List<Mechanic> result =
                new ArrayList<>();

        if (status == null
                || status.isBlank()
                || status.equalsIgnoreCase("All")) {

            return allMechanics;
        }

        for (Mechanic mechanic :
                allMechanics) {

            String mechanicStatus =
                    clean(mechanic.getStatus());

            if (mechanicStatus != null
                    && mechanicStatus.equalsIgnoreCase(status)) {

                result.add(mechanic);
            }
        }

        return result;
    }

    // =====================================================
    // DOCUMENT -> MECHANIC
    // =====================================================

    private Mechanic toMechanic(
            DocumentSnapshot document
    ) {

        if (document == null
                || !document.exists()) {

            return null;
        }

        /*
         * Registration stores experience as a number, while the current
         * Mechanic model exposes it as a String. Firestore's automatic
         * mapper throws for that schema difference and one bad document
         * previously made the controller return an empty list. Read every
         * UI field defensively so old and current records can coexist.
         */
        String email =
                normalizeId(
                        firstNonBlank(
                                stringValue(document.get("email")),
                                document.getId().contains("@")
                                        ? document.getId()
                                        : null
                        )
                );

        String id =
                email != null
                        ? email
                        : normalizeId(
                                firstNonBlank(
                                        stringValue(document.get("mechanicId")),
                                        document.getId()
                                )
                        );

        String status =
                clean(
                        stringValue(
                                document.get(
                                        "status"
                                )
                        )
                );

        if (status == null) {

            status =
                    "Active";
        }

        return new Mechanic(
                id,
                cleanOrEmpty(stringValue(document.get("name"))),
                email == null ? "" : email,
                cleanOrEmpty(stringValue(document.get("phone"))),
                cleanOrEmpty(stringValue(document.get("address"))),
                cleanOrEmpty(stringValue(document.get("city"))),
                cleanOrEmpty(stringValue(document.get("specialization"))),
                status,
                cleanOrEmpty(stringValue(document.get("experience"))),
                cleanOrEmpty(stringValue(document.get("createdAt")))
        );
    }

    // =====================================================
    // MODEL -> MAP
    // =====================================================

    private Map<String, Object> toMap(
            Mechanic mechanic
    ) {

        Map<String, Object> data =
                new HashMap<>();

        data.put("mechanicId", mechanic.getMechanicId());
        data.put("name", mechanic.getName());
        data.put("email", mechanic.getEmail());
        data.put("phone", mechanic.getPhone());
        data.put("address", mechanic.getAddress());
        data.put("city", mechanic.getCity());
        data.put("specialization", mechanic.getSpecialization());
        data.put("status", mechanic.getStatus());
        data.put("experience", mechanic.getExperience());
        data.put("createdAt", mechanic.getCreatedAt());
        data.put("updatedAt", currentTime());

        return data;
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private boolean contains(
            String value,
            String search
    ) {

        return value != null
                && search != null
                && value.toLowerCase().contains(search);
    }

    private String normalizeId(
            String value
    ) {

        value = clean(value);

        if (value == null) {

            return null;
        }

        if (value.contains("@")) {

            return value.toLowerCase();
        }

        return value;
    }

    private String firstNonBlank(
            String... values
    ) {

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

    private String stringValue(
            Object value
    ) {

        if (value == null) {

            return null;
        }

        String text =
                String.valueOf(value).trim();

        return text.isEmpty()
                ? null
                : text;
    }

    private String clean(
            String value
    ) {

        if (value == null) {

            return null;
        }

        String cleaned =
                value.trim();

        return cleaned.isEmpty()
                ? null
                : cleaned;
    }

    private String cleanOrEmpty(
            String value
    ) {

        String cleaned =
                clean(value);

        return cleaned == null
                ? ""
                : cleaned;
    }

    private String currentTime() {

        return String.valueOf(System.currentTimeMillis());
    }
}
