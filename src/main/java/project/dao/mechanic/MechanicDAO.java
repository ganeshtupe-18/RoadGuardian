package project.dao.mechanic;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;

import project.firebase.FirebaseConfig;
import project.model.Mechanic;
import project.ui.user.UserSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MechanicDAO {

    private static final String COLLECTION =
            "mechanics";

    private final Firestore firestore;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public MechanicDAO() {

        try {

            firestore =
                    FirebaseConfig.getFirestore();

        } catch (IOException e) {

            throw new RuntimeException(
                    "Unable to initialize Firebase.",
                    e
            );
        }
    }

    public MechanicDAO(
            Firestore firestore
    ) {

        if (firestore == null) {

            throw new IllegalArgumentException(
                    "Firestore cannot be null."
            );
        }

        this.firestore =
                firestore;
    }

    // =====================================================
    // CURRENT MECHANIC ID
    // =====================================================

    public String getCurrentMechanicId() {

        String userId =
                normalizeId(
                        UserSession.getUserId()
                );

        if (userId != null) {

            return userId;
        }

        return normalizeId(
                UserSession.getUserEmail()
        );
    }

    // =====================================================
    // CURRENT MECHANIC
    // =====================================================

    public Mechanic getCurrentMechanic()
            throws Exception {

        String mechanicId =
                getCurrentMechanicId();

        if (mechanicId == null) {

            return null;
        }

        return getMechanicById(
                mechanicId
        );
    }

    // =====================================================
    // GET MECHANIC BY ID
    // =====================================================

    public Mechanic getMechanicById(
            String mechanicId
    ) throws Exception {

        mechanicId =
                normalizeId(
                        mechanicId
                );

        if (mechanicId == null) {

            return null;
        }

        // =================================================
        // DIRECT DOCUMENT LOOKUP
        // =================================================

        DocumentSnapshot direct =
                firestore
                        .collection(COLLECTION)
                        .document(mechanicId)
                        .get()
                        .get();

        if (direct.exists()) {

            return convertDocument(
                    direct
            );
        }

        // =================================================
        // mechanicId FIELD
        // =================================================

        QuerySnapshot byId =
                firestore
                        .collection(COLLECTION)
                        .whereEqualTo(
                                "mechanicId",
                                mechanicId
                        )
                        .limit(1)
                        .get()
                        .get();

        if (!byId.isEmpty()) {

            return convertDocument(
                    byId
                            .getDocuments()
                            .get(0)
            );
        }

        // =================================================
        // EMAIL FIELD
        // =================================================

        QuerySnapshot byEmail =
                firestore
                        .collection(COLLECTION)
                        .whereEqualTo(
                                "email",
                                mechanicId
                        )
                        .limit(1)
                        .get()
                        .get();

        if (!byEmail.isEmpty()) {

            return convertDocument(
                    byEmail
                            .getDocuments()
                            .get(0)
            );
        }

        return null;
    }

    // =====================================================
    // GET BY EMAIL
    // =====================================================

    public Mechanic getMechanicByEmail(
            String email
    ) throws Exception {

        return getMechanicById(
                email
        );
    }

    // =====================================================
    // EXISTS
    // =====================================================

    public boolean currentMechanicExists() {

        try {

            return getCurrentMechanic()
                    != null;

        } catch (Exception e) {

            return false;
        }
    }

    // =====================================================
    // ACTIVE?
    // =====================================================

    public boolean isCurrentMechanicActive() {

        try {

            Mechanic mechanic =
                    getCurrentMechanic();

            if (mechanic == null) {

                return false;
            }

            String status =
                    clean(
                            mechanic.getStatus()
                    );

            /*
             * Old mechanic accounts may not contain status.
             */
            if (status == null) {

                return true;
            }

            return status.equalsIgnoreCase(
                    "Active"
            );

        } catch (Exception e) {

            System.err.println(
                    "Unable to check mechanic status: "
                            + e.getMessage()
            );

            return false;
        }
    }

    // =====================================================
    // UPDATE CURRENT PROFILE
    // =====================================================

    public boolean updateCurrentProfile(
            String name,
            String phone,
            String address,
            String city,
            String specialization,
            String experience
    ) throws Exception {

        String mechanicId =
                getCurrentMechanicId();

        if (mechanicId == null) {

            return false;
        }

        return updateProfile(
                mechanicId,
                name,
                phone,
                address,
                city,
                specialization,
                experience
        );
    }

    // =====================================================
    // UPDATE PROFILE
    // =====================================================

    public boolean updateProfile(
            String mechanicId,
            String name,
            String phone,
            String address,
            String city,
            String specialization,
            String experience
    ) throws Exception {

        mechanicId =
                normalizeId(
                        mechanicId
                );

        if (mechanicId == null) {

            return false;
        }

        DocumentReference reference =
                findMechanicReference(
                        mechanicId
                );

        if (reference == null) {

            return false;
        }

        Map<String, Object> updates =
                new HashMap<>();

        putIfNotNull(
                updates,
                "name",
                clean(name)
        );

        putIfNotNull(
                updates,
                "phone",
                clean(phone)
        );

        putIfNotNull(
                updates,
                "address",
                clean(address)
        );

        putIfNotNull(
                updates,
                "city",
                clean(city)
        );

        putIfNotNull(
                updates,
                "specialization",
                clean(specialization)
        );

        putIfNotNull(
                updates,
                "experience",
                clean(experience)
        );

        updates.put(
                "profileCompleted",
                isProfileComplete(
                        name,
                        phone,
                        specialization
                )
        );

        reference
                .set(
                        updates,
                        SetOptions.merge()
                )
                .get();

        return true;
    }

    // =====================================================
    // CURRENT NAME
    // =====================================================

    public String getCurrentMechanicName() {

        try {

            Mechanic mechanic =
                    getCurrentMechanic();

            if (mechanic != null
                    &&
                    clean(
                            mechanic.getName()
                    ) != null) {

                return mechanic.getName();
            }

        } catch (Exception ignored) {
        }

        String sessionName =
                clean(
                        UserSession.getUserName()
                );

        return sessionName == null
                ? "Mechanic"
                : sessionName;
    }

    // =====================================================
    // CURRENT EMAIL
    // =====================================================

    public String getCurrentMechanicEmail() {

        try {

            Mechanic mechanic =
                    getCurrentMechanic();

            if (mechanic != null
                    &&
                    clean(
                            mechanic.getEmail()
                    ) != null) {

                return mechanic.getEmail();
            }

        } catch (Exception ignored) {
        }

        return UserSession.getUserEmail();
    }

    // =====================================================
    // CURRENT PHONE
    // =====================================================

    public String getCurrentMechanicPhone() {

        try {

            Mechanic mechanic =
                    getCurrentMechanic();

            return mechanic == null
                    ? ""
                    : safe(
                            mechanic.getPhone()
                    );

        } catch (Exception e) {

            return "";
        }
    }

    // =====================================================
    // CURRENT SPECIALIZATION
    // =====================================================

    public String getCurrentMechanicSpecialization() {

        try {

            Mechanic mechanic =
                    getCurrentMechanic();

            return mechanic == null
                    ? ""
                    : safe(
                            mechanic.getSpecialization()
                    );

        } catch (Exception e) {

            return "";
        }
    }

    // =====================================================
    // CURRENT EXPERIENCE
    // =====================================================

    public String getCurrentMechanicExperience() {

        try {

            Mechanic mechanic =
                    getCurrentMechanic();

            return mechanic == null
                    ? ""
                    : safe(
                            mechanic.getExperience()
                    );

        } catch (Exception e) {

            return "";
        }
    }

    // =====================================================
    // CURRENT STATUS
    // =====================================================

    public String getCurrentMechanicStatus() {

        try {

            Mechanic mechanic =
                    getCurrentMechanic();

            if (mechanic == null) {

                return "";
            }

            String status =
                    clean(
                            mechanic.getStatus()
                    );

            return status == null
                    ? "Active"
                    : status;

        } catch (Exception e) {

            return "";
        }
    }

    // =====================================================
    // FIND FIRESTORE REFERENCE
    // =====================================================

    private DocumentReference findMechanicReference(
            String mechanicId
    ) throws Exception {

        DocumentReference direct =
                firestore
                        .collection(COLLECTION)
                        .document(mechanicId);

        if (direct
                .get()
                .get()
                .exists()) {

            return direct;
        }

        QuerySnapshot byId =
                firestore
                        .collection(COLLECTION)
                        .whereEqualTo(
                                "mechanicId",
                                mechanicId
                        )
                        .limit(1)
                        .get()
                        .get();

        if (!byId.isEmpty()) {

            return byId
                    .getDocuments()
                    .get(0)
                    .getReference();
        }

        QuerySnapshot byEmail =
                firestore
                        .collection(COLLECTION)
                        .whereEqualTo(
                                "email",
                                mechanicId
                        )
                        .limit(1)
                        .get()
                        .get();

        if (!byEmail.isEmpty()) {

            return byEmail
                    .getDocuments()
                    .get(0)
                    .getReference();
        }

        return null;
    }

    // =====================================================
    // FIRESTORE -> MODEL
    // =====================================================

    private Mechanic convertDocument(
            DocumentSnapshot document
    ) {

        if (document == null
                ||
                !document.exists()) {

            return null;
        }

        Mechanic mechanic =
                new Mechanic();

        String mechanicId =
                firstNonBlank(
                        stringValue(
                                document.get(
                                        "mechanicId"
                                )
                        ),
                        stringValue(
                                document.get(
                                        "email"
                                )
                        ),
                        document.getId()
                );

        mechanic.setMechanicId(
                normalizeId(
                        mechanicId
                )
        );

        mechanic.setName(
                firstNonBlank(
                        stringValue(
                                document.get(
                                        "name"
                                )
                        ),
                        stringValue(
                                document.get(
                                        "fullName"
                                )
                        ),
                        "Mechanic"
                )
        );

        mechanic.setEmail(
                firstNonBlank(
                        stringValue(
                                document.get(
                                        "email"
                                )
                        ),
                        mechanicId,
                        ""
                )
        );

        mechanic.setPhone(
                valueOrEmpty(
                        document.get(
                                "phone"
                        )
                )
        );

        mechanic.setAddress(
                valueOrEmpty(
                        document.get(
                                "address"
                        )
                )
        );

        mechanic.setCity(
                valueOrEmpty(
                        document.get(
                                "city"
                        )
                )
        );

        mechanic.setSpecialization(
                firstNonBlank(
                        stringValue(
                                document.get(
                                        "specialization"
                                )
                        ),
                        stringValue(
                                document.get(
                                        "speciality"
                                )
                        ),
                        ""
                )
        );

        mechanic.setStatus(
                firstNonBlank(
                        stringValue(
                                document.get(
                                        "status"
                                )
                        ),
                        "Active"
                )
        );

        mechanic.setExperience(
                valueOrEmpty(
                        document.get(
                                "experience"
                        )
                )
        );

        mechanic.setCreatedAt(
                valueOrEmpty(
                        document.get(
                                "createdAt"
                        )
                )
        );

        return mechanic;
    }

    // =====================================================
    // PROFILE COMPLETE
    // =====================================================

    private boolean isProfileComplete(
            String name,
            String phone,
            String specialization
    ) {

        return clean(name) != null
                &&
                clean(phone) != null
                &&
                clean(specialization) != null;
    }

    // =====================================================
    // PUT
    // =====================================================

    private void putIfNotNull(
            Map<String, Object> map,
            String key,
            Object value
    ) {

        if (value != null) {

            map.put(
                    key,
                    value
            );
        }
    }

    // =====================================================
    // FIRST NON BLANK
    // =====================================================

    private String firstNonBlank(
            String... values
    ) {

        if (values == null) {

            return null;
        }

        for (String value :
                values) {

            String cleaned =
                    clean(
                            value
                    );

            if (cleaned != null) {

                return cleaned;
            }
        }

        return null;
    }

    // =====================================================
    // VALUE OR EMPTY
    // =====================================================

    private String valueOrEmpty(
            Object value
    ) {

        String text =
                stringValue(
                        value
                );

        return text == null
                ? ""
                : text;
    }

    // =====================================================
    // NORMALIZE ID
    // =====================================================

    private String normalizeId(
            String value
    ) {

        value =
                clean(
                        value
                );

        if (value == null) {

            return null;
        }

        if (value.contains("@")) {

            return value.toLowerCase();
        }

        return value;
    }

    // =====================================================
    // STRING
    // =====================================================

    private String stringValue(
            Object value
    ) {

        if (value == null) {

            return null;
        }

        String text =
                String.valueOf(
                        value
                ).trim();

        return text.isEmpty()
                ? null
                : text;
    }

    // =====================================================
    // SAFE
    // =====================================================

    private String safe(
            String value
    ) {

        String cleaned =
                clean(
                        value
                );

        return cleaned == null
                ? ""
                : cleaned;
    }

    // =====================================================
    // CLEAN
    // =====================================================

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
}