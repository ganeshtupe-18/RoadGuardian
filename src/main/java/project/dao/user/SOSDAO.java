package project.dao.user;

import com.google.cloud.firestore.DocumentReference;
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

public class SOSDAO {

    // =====================================================
    // COLLECTIONS
    // =====================================================

    private static final String USERS_COLLECTION =
            "users";

    /*
     * ONE CANONICAL SOS COLLECTION FOR:
     *
     * Customer
     * Admin
     * Mechanic
     */
    private static final String SOS_COLLECTION =
            "SOSRequests";

    /*
     * Old collections are read only for migration.
     */
    private static final String LEGACY_COLLECTION_1 =
            "sos_requests";

    private static final String LEGACY_COLLECTION_2 =
            "sosRequests";

    // =====================================================
    // FIRESTORE
    // =====================================================

    private final Firestore firestore;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public SOSDAO() {

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

    // =====================================================
    // OPTIONAL FIRESTORE CONSTRUCTOR
    // =====================================================

    public SOSDAO(
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
    // CREATE SOS REQUEST
    // =====================================================

    public String createSOSRequest(
            String userId,
            String userName,
            String userEmail,
            String location,
            String latitude,
            String longitude
    ) throws Exception {

        String originalUserId =
                clean(
                        userId
                );

        String customerId =
                normalizeId(
                        firstNonBlank(
                                userEmail,
                                userId
                        )
                );

        if (customerId == null) {

            throw new IllegalArgumentException(
                    "Logged-in customer is not available."
            );
        }

        // =================================================
        // PREVENT MULTIPLE ACTIVE SOS
        // =================================================

        DocumentSnapshot existing =
                getActiveSOS(
                        firstNonBlank(
                                originalUserId,
                                customerId
                        )
                );

        if (existing != null) {

            return existing.getId();
        }

        // =================================================
        // CREATE DOCUMENT
        // =================================================

        DocumentReference reference =
                firestore
                        .collection(SOS_COLLECTION)
                        .document();

        String sosId =
                reference.getId();

        long now =
                System.currentTimeMillis();

        // =================================================
        // SOS DATA
        // =================================================

        Map<String, Object> data =
                new HashMap<>();

        data.put(
                "sosId",
                sosId
        );

        // =================================================
        // CANONICAL CUSTOMER FIELDS
        // =================================================

        data.put(
                "customerId",
                customerId
        );

        data.put(
                "customerName",
                valueOrEmpty(
                        userName
                )
        );

        // =================================================
        // OLD CUSTOMER COMPATIBILITY
        // =================================================

        data.put(
                "userId",
                firstNonBlank(
                        originalUserId,
                        customerId
                )
        );

        data.put(
                "userName",
                valueOrEmpty(
                        userName
                )
        );

        data.put(
                "userEmail",
                firstNonBlank(
                        normalizeId(userEmail),
                        customerId
                )
        );

        // =================================================
        // VEHICLE
        //
        // SOS can exist without selected vehicle.
        // =================================================

        data.put(
                "vehicleId",
                ""
        );

        data.put(
                "vehicleNumber",
                ""
        );

        // =================================================
        // MECHANIC
        //
        // Admin will assign later.
        // =================================================

        data.put(
                "mechanicId",
                ""
        );

        data.put(
                "mechanicName",
                ""
        );

        // =================================================
        // RESPONDER COMPATIBILITY
        // =================================================

        data.put(
                "responderId",
                ""
        );

        data.put(
                "responderName",
                ""
        );

        data.put(
                "responderStatus",
                "Waiting"
        );

        // =================================================
        // EMERGENCY
        // =================================================

        data.put(
                "emergencyType",
                "Emergency SOS"
        );

        data.put(
                "description",
                "Emergency roadside assistance requested"
        );

        // =================================================
        // LOCATION
        // =================================================

        data.put(
                "location",
                firstNonBlank(
                        location,
                        "Location not available"
                )
        );

        data.put(
                "latitude",
                valueOrEmpty(
                        latitude
                )
        );

        data.put(
                "longitude",
                valueOrEmpty(
                        longitude
                )
        );

        // =================================================
        // STATUS
        //
        // IMPORTANT:
        //
        // Customer creates:
        // Pending
        //
        // Admin assigns:
        // Assigned
        //
        // Mechanic accepts:
        // Accepted
        // =================================================

        data.put(
                "status",
                "Pending"
        );

        // =================================================
        // LIFECYCLE DATES
        // =================================================

        data.put(
                "requestDate",
                String.valueOf(now)
        );

        data.put(
                "assignedDate",
                ""
        );

        data.put(
                "acceptedDate",
                ""
        );

        data.put(
                "startedDate",
                ""
        );

        data.put(
                "resolvedDate",
                ""
        );

        data.put(
                "cancelledDate",
                ""
        );

        // =================================================
        // TIMESTAMPS
        // =================================================

        data.put(
                "createdAt",
                now
        );

        data.put(
                "updatedAt",
                now
        );

        // =================================================
        // CONTACT STATUS
        // =================================================

        data.put(
                "contactsNotified",
                true
        );

        // =================================================
        // TIMELINE
        // =================================================

        data.put(
                "timeline",
                createInitialTimeline(
                        now
                )
        );

        // =================================================
        // SAVE
        // =================================================

        reference
                .set(
                        data
                )
                .get();

        System.out.println(
                "SOS created: "
                        + sosId
                        + " | customerId="
                        + customerId
                        + " | status=Pending"
        );

        return sosId;
    }

    // =====================================================
    // GET ACTIVE SOS
    //
    // Kept as DocumentSnapshot because existing
    // SOSController uses this return type.
    // =====================================================

    public DocumentSnapshot getActiveSOS(
            String userId
    ) throws Exception {

        String lookupId =
                normalizeId(
                        userId
                );

        if (lookupId == null) {

            return null;
        }

        // =================================================
        // MIGRATE OLD DATA
        // =================================================

        migrateLegacySOSForCustomer(
                lookupId
        );

        QuerySnapshot snapshot =
                firestore
                        .collection(SOS_COLLECTION)
                        .get()
                        .get();

        DocumentSnapshot latest =
                null;

        long latestTime =
                Long.MIN_VALUE;

        for (DocumentSnapshot document :
                snapshot.getDocuments()) {

            if (!belongsToCustomer(
                    document,
                    lookupId
            )) {

                continue;
            }

            String status =
                    normalizeStatus(
                            stringValue(
                                    document.get(
                                            "status"
                                    )
                            )
                    );

            if (!isActiveStatus(
                    status
            )) {

                continue;
            }

            long time =
                    getDocumentTime(
                            document
                    );

            if (latest == null
                    ||
                    time > latestTime) {

                latest =
                        document;

                latestTime =
                        time;
            }
        }

        return latest;
    }

    // =====================================================
    // GET ACTIVE SOS MODEL
    // =====================================================

    public SOSRequest getActiveSOSRequest(
            String userId
    ) throws Exception {

        DocumentSnapshot document =
                getActiveSOS(
                        userId
                );

        return document == null
                ? null
                : convertDocument(
                        document
                );
    }

    // =====================================================
    // GET SOS BY ID
    // =====================================================

    public SOSRequest getSOSById(
            String sosId
    ) throws Exception {

        sosId =
                clean(
                        sosId
                );

        if (sosId == null) {

            return null;
        }

        // =================================================
        // CANONICAL
        // =================================================

        DocumentSnapshot document =
                firestore
                        .collection(SOS_COLLECTION)
                        .document(sosId)
                        .get()
                        .get();

        if (document.exists()) {

            return convertDocument(
                    document
            );
        }

        // =================================================
        // TRY LEGACY ID
        // =================================================

        if (migrateLegacyDocumentById(
                LEGACY_COLLECTION_1,
                sosId
        )
                ||
                migrateLegacyDocumentById(
                        LEGACY_COLLECTION_2,
                        sosId
                )) {

            document =
                    firestore
                            .collection(SOS_COLLECTION)
                            .document(sosId)
                            .get()
                            .get();

            if (document.exists()) {

                return convertDocument(
                        document
                );
            }
        }

        return null;
    }

    // =====================================================
    // GET CUSTOMER SOS REQUESTS
    // =====================================================

    public List<SOSRequest> getSOSRequests(
            String customerId
    ) throws Exception {

        customerId =
                normalizeId(
                        customerId
                );

        List<SOSRequest> requests =
                new ArrayList<>();

        if (customerId == null) {

            return requests;
        }

        migrateLegacySOSForCustomer(
                customerId
        );

        QuerySnapshot snapshot =
                firestore
                        .collection(SOS_COLLECTION)
                        .get()
                        .get();

        for (DocumentSnapshot document :
                snapshot.getDocuments()) {

            if (!belongsToCustomer(
                    document,
                    customerId
            )) {

                continue;
            }

            SOSRequest request =
                    convertDocument(
                            document
                    );

            if (request != null) {

                requests.add(
                        request
                );
            }
        }

        // =================================================
        // LATEST FIRST
        // =================================================

        requests.sort(
                (
                        first,
                        second
                ) ->
                        Long.compare(
                                getRequestTime(
                                        second
                                ),
                                getRequestTime(
                                        first
                                )
                        )
        );

        return requests;
    }

    // =====================================================
    // COMPLETE / RESOLVE SOS
    //
    // Kept for old Customer UI compatibility.
    //
    // Mechanic normally resolves the SOS.
    // =====================================================

    public boolean completeSOS(
            String sosId
    ) throws Exception {

        sosId =
                clean(
                        sosId
                );

        if (sosId == null) {

            return false;
        }

        DocumentReference reference =
                getCanonicalReference(
                        sosId
                );

        if (reference == null) {

            return false;
        }

        DocumentSnapshot document =
                reference
                        .get()
                        .get();

        if (!document.exists()) {

            return false;
        }

        String status =
                normalizeStatus(
                        stringValue(
                                document.get(
                                        "status"
                                )
                        )
                );

        if ("Cancelled".equalsIgnoreCase(
                status
        )) {

            return false;
        }

        if ("Resolved".equalsIgnoreCase(
                status
        )) {

            return true;
        }

        long now =
                System.currentTimeMillis();

        Map<String, Object> updates =
                new HashMap<>();

        updates.put(
                "status",
                "Resolved"
        );

        updates.put(
                "resolvedDate",
                String.valueOf(now)
        );

        updates.put(
                "responderStatus",
                "Completed"
        );

        updates.put(
                "updatedAt",
                now
        );

        updates.put(
                "timeline",
                updateTimeline(
                        document,
                        "Resolved",
                        now
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
    // CANCEL SOS
    // =====================================================

    public boolean cancelSOS(
            String sosId
    ) throws Exception {

        sosId =
                clean(
                        sosId
                );

        if (sosId == null) {

            return false;
        }

        DocumentReference reference =
                getCanonicalReference(
                        sosId
                );

        if (reference == null) {

            return false;
        }

        DocumentSnapshot document =
                reference
                        .get()
                        .get();

        if (!document.exists()) {

            return false;
        }

        String status =
                normalizeStatus(
                        stringValue(
                                document.get(
                                        "status"
                                )
                        )
                );

        // =================================================
        // FINAL STATES
        // =================================================

        if ("Resolved".equalsIgnoreCase(status)) {

            return false;
        }

        if ("Cancelled".equalsIgnoreCase(status)) {

            return true;
        }

        long now =
                System.currentTimeMillis();

        Map<String, Object> updates =
                new HashMap<>();

        updates.put(
                "status",
                "Cancelled"
        );

        updates.put(
                "cancelledDate",
                String.valueOf(now)
        );

        updates.put(
                "responderStatus",
                "Cancelled"
        );

        updates.put(
                "updatedAt",
                now
        );

        reference
                .set(
                        updates,
                        SetOptions.merge()
                )
                .get();

        System.out.println(
                "SOS cancelled: "
                        + sosId
        );

        return true;
    }

    // =====================================================
    // EMERGENCY CONTACTS
    // =====================================================

    public List<Map<String, Object>> getEmergencyContacts(
            String userId
    ) throws Exception {

        List<Map<String, Object>> contacts =
                new ArrayList<>();

        String id =
                clean(
                        userId
                );

        if (id == null) {

            return contacts;
        }

        // =================================================
        // DIRECT USER DOCUMENT
        // =================================================

        DocumentSnapshot user =
                firestore
                        .collection(USERS_COLLECTION)
                        .document(id)
                        .get()
                        .get();

        if (!user.exists()) {

            String normalized =
                    normalizeId(
                            id
                    );

            if (normalized != null
                    &&
                    !normalized.equals(id)) {

                user =
                        firestore
                                .collection(USERS_COLLECTION)
                                .document(normalized)
                                .get()
                                .get();
            }
        }

        // =================================================
        // SEARCH EMAIL / USER ID
        // =================================================

        if (!user.exists()) {

            QuerySnapshot users =
                    firestore
                            .collection(USERS_COLLECTION)
                            .get()
                            .get();

            String lookup =
                    normalizeId(
                            id
                    );

            for (DocumentSnapshot candidate :
                    users.getDocuments()) {

                String candidateId =
                        normalizeId(
                                firstNonBlank(
                                        stringValue(
                                                candidate.get(
                                                        "email"
                                                )
                                        ),
                                        stringValue(
                                                candidate.get(
                                                        "userId"
                                                )
                                        ),
                                        candidate.getId()
                                )
                        );

                if (sameId(
                        lookup,
                        candidateId
                )) {

                    user =
                            candidate;

                    break;
                }
            }
        }

        if (!user.exists()) {

            return contacts;
        }

        Object value =
                user.get(
                        "emergencyContacts"
                );

        if (!(value instanceof List<?>)) {

            return contacts;
        }

        for (Object item :
                (List<?>) value) {

            if (!(item instanceof Map<?, ?>)) {

                continue;
            }

            Map<String, Object> contact =
                    new HashMap<>();

            Map<?, ?> source =
                    (Map<?, ?>) item;

            for (Map.Entry<?, ?> entry :
                    source.entrySet()) {

                if (entry.getKey() == null) {

                    continue;
                }

                contact.put(
                        String.valueOf(
                                entry.getKey()
                        ),
                        entry.getValue()
                );
            }

            contacts.add(
                    contact
            );
        }

        return contacts;
    }

    // =====================================================
    // CANONICAL REFERENCE
    // =====================================================

    private DocumentReference getCanonicalReference(
            String sosId
    ) throws Exception {

        DocumentReference reference =
                firestore
                        .collection(SOS_COLLECTION)
                        .document(sosId);

        if (reference
                .get()
                .get()
                .exists()) {

            return reference;
        }

        if (migrateLegacyDocumentById(
                LEGACY_COLLECTION_1,
                sosId
        )
                ||
                migrateLegacyDocumentById(
                        LEGACY_COLLECTION_2,
                        sosId
                )) {

            return firestore
                    .collection(SOS_COLLECTION)
                    .document(sosId);
        }

        return null;
    }

    // =====================================================
    // CUSTOMER OWNERSHIP
    // =====================================================

    private boolean belongsToCustomer(
            DocumentSnapshot document,
            String customerId
    ) {

        if (document == null
                ||
                customerId == null) {

            return false;
        }

        String canonicalCustomer =
                normalizeId(
                        stringValue(
                                document.get(
                                        "customerId"
                                )
                        )
                );

        String userId =
                normalizeId(
                        stringValue(
                                document.get(
                                        "userId"
                                )
                        )
                );

        String userEmail =
                normalizeId(
                        stringValue(
                                document.get(
                                        "userEmail"
                                )
                        )
                );

        return sameId(
                customerId,
                canonicalCustomer
        )
                ||
                sameId(
                        customerId,
                        userId
                )
                ||
                sameId(
                        customerId,
                        userEmail
                );
    }

    // =====================================================
    // INITIAL TIMELINE
    // =====================================================

    private List<Map<String, Object>> createInitialTimeline(
            long now
    ) {

        List<Map<String, Object>> timeline =
                new ArrayList<>();

        timeline.add(
                createTimelineItem(
                        "SOS request received",
                        "Completed",
                        now
                )
        );

        timeline.add(
                createTimelineItem(
                        "Emergency contacts notified",
                        "Completed",
                        now
                )
        );

        timeline.add(
                createTimelineItem(
                        "Mechanic assigned",
                        "Waiting",
                        null
                )
        );

        timeline.add(
                createTimelineItem(
                        "Responder en route",
                        "Waiting",
                        null
                )
        );

        timeline.add(
                createTimelineItem(
                        "Assistance completed",
                        "Waiting",
                        null
                )
        );

        return timeline;
    }

    // =====================================================
    // TIMELINE ITEM
    // =====================================================

    private Map<String, Object> createTimelineItem(
            String title,
            String status,
            Long timestamp
    ) {

        Map<String, Object> item =
                new HashMap<>();

        item.put(
                "title",
                title
        );

        item.put(
                "status",
                status
        );

        if (timestamp != null) {

            item.put(
                    "timestamp",
                    timestamp
            );
        }

        return item;
    }

    // =====================================================
    // UPDATE TIMELINE
    // =====================================================

    private List<Map<String, Object>> updateTimeline(
            DocumentSnapshot document,
            String status,
            long now
    ) {

        List<Map<String, Object>> timeline =
                readTimeline(
                        document.get(
                                "timeline"
                        )
                );

        if (timeline.isEmpty()) {

            timeline =
                    createInitialTimeline(
                            getDocumentTime(
                                    document
                            )
                    );
        }

        status =
                normalizeStatus(
                        status
                );

        if ("Assigned".equalsIgnoreCase(status)
                ||
                "Accepted".equalsIgnoreCase(status)
                ||
                "In Progress".equalsIgnoreCase(status)
                ||
                "Resolved".equalsIgnoreCase(status)) {

            markTimelineCompleted(
                    timeline,
                    "Mechanic assigned",
                    now
            );
        }

        if ("Accepted".equalsIgnoreCase(status)
                ||
                "In Progress".equalsIgnoreCase(status)
                ||
                "Resolved".equalsIgnoreCase(status)) {

            markTimelineCompleted(
                    timeline,
                    "Responder en route",
                    now
            );
        }

        if ("Resolved".equalsIgnoreCase(status)) {

            markTimelineCompleted(
                    timeline,
                    "Assistance completed",
                    now
            );
        }

        return timeline;
    }

    // =====================================================
    // MARK TIMELINE COMPLETED
    // =====================================================

    private void markTimelineCompleted(
            List<Map<String, Object>> timeline,
            String title,
            long now
    ) {

        for (Map<String, Object> item :
                timeline) {

            String itemTitle =
                    stringValue(
                            item.get(
                                    "title"
                            )
                    );

            if (itemTitle == null
                    ||
                    !itemTitle.equalsIgnoreCase(
                            title
                    )) {

                continue;
            }

            item.put(
                    "status",
                    "Completed"
            );

            if (!item.containsKey("timestamp")) {

                item.put(
                        "timestamp",
                        now
                );
            }

            return;
        }

        timeline.add(
                createTimelineItem(
                        title,
                        "Completed",
                        now
                )
        );
    }

    // =====================================================
    // READ TIMELINE
    // =====================================================

    private List<Map<String, Object>> readTimeline(
            Object value
    ) {

        List<Map<String, Object>> result =
                new ArrayList<>();

        if (!(value instanceof List<?>)) {

            return result;
        }

        for (Object item :
                (List<?>) value) {

            if (!(item instanceof Map<?, ?>)) {

                continue;
            }

            Map<String, Object> copy =
                    new HashMap<>();

            Map<?, ?> source =
                    (Map<?, ?>) item;

            for (Map.Entry<?, ?> entry :
                    source.entrySet()) {

                if (entry.getKey() == null) {

                    continue;
                }

                copy.put(
                        String.valueOf(
                                entry.getKey()
                        ),
                        entry.getValue()
                );
            }

            result.add(
                    copy
            );
        }

        return result;
    }

    // =====================================================
    // FIRESTORE -> MODEL
    // =====================================================

    private SOSRequest convertDocument(
            DocumentSnapshot document
    ) {

        if (document == null
                ||
                !document.exists()) {

            return null;
        }

        SOSRequest request =
                new SOSRequest();

        // =================================================
        // ID
        // =================================================

        request.setSosId(
                firstNonBlank(
                        stringValue(
                                document.get(
                                        "sosId"
                                )
                        ),
                        document.getId()
                )
        );

        // =================================================
        // CUSTOMER
        // =================================================

        String customerId =
                normalizeId(
                        firstNonBlank(
                                stringValue(
                                        document.get(
                                                "customerId"
                                        )
                                ),
                                stringValue(
                                        document.get(
                                                "userEmail"
                                        )
                                ),
                                stringValue(
                                        document.get(
                                                "userId"
                                        )
                                )
                        )
                );

        String customerName =
                firstNonBlank(
                        stringValue(
                                document.get(
                                        "customerName"
                                )
                        ),
                        stringValue(
                                document.get(
                                        "userName"
                                )
                        ),
                        "Customer"
                );

        request.setCustomerId(
                customerId
        );

        request.setCustomerName(
                customerName
        );

        // =================================================
        // USER ALIASES
        // =================================================

        request.setUserId(
                firstNonBlank(
                        stringValue(
                                document.get(
                                        "userId"
                                )
                        ),
                        customerId,
                        ""
                )
        );

        request.setUserName(
                firstNonBlank(
                        stringValue(
                                document.get(
                                        "userName"
                                )
                        ),
                        customerName,
                        ""
                )
        );

        request.setUserEmail(
                firstNonBlank(
                        stringValue(
                                document.get(
                                        "userEmail"
                                )
                        ),
                        customerId,
                        ""
                )
        );

        // =================================================
        // VEHICLE
        // =================================================

        request.setVehicleId(
                valueOrEmpty(
                        document.get(
                                "vehicleId"
                        )
                )
        );

        request.setVehicleNumber(
                firstNonBlank(
                        stringValue(
                                document.get(
                                        "vehicleNumber"
                                )
                        ),
                        stringValue(
                                document.get(
                                        "registrationNumber"
                                )
                        ),
                        ""
                )
        );

        // =================================================
        // MECHANIC
        // =================================================

        String mechanicId =
                normalizeId(
                        firstNonBlank(
                                stringValue(
                                        document.get(
                                                "mechanicId"
                                        )
                                ),
                                stringValue(
                                        document.get(
                                                "responderId"
                                        )
                                )
                        )
                );

        String mechanicName =
                firstNonBlank(
                        stringValue(
                                document.get(
                                        "mechanicName"
                                )
                        ),
                        stringValue(
                                document.get(
                                        "responderName"
                                )
                        ),
                        ""
                );

        request.setMechanicId(
                mechanicId
        );

        request.setMechanicName(
                mechanicName
        );

        request.setResponderId(
                firstNonBlank(
                        stringValue(
                                document.get(
                                        "responderId"
                                )
                        ),
                        mechanicId,
                        ""
                )
        );

        request.setResponderName(
                firstNonBlank(
                        stringValue(
                                document.get(
                                        "responderName"
                                )
                        ),
                        mechanicName,
                        ""
                )
        );

        // =================================================
        // EMERGENCY
        // =================================================

        request.setEmergencyType(
                firstNonBlank(
                        stringValue(
                                document.get(
                                        "emergencyType"
                                )
                        ),
                        stringValue(
                                document.get(
                                        "type"
                                )
                        ),
                        "Emergency SOS"
                )
        );

        request.setDescription(
                firstNonBlank(
                        stringValue(
                                document.get(
                                        "description"
                                )
                        ),
                        "Emergency roadside assistance requested"
                )
        );

        // =================================================
        // LOCATION
        // =================================================

        request.setLocation(
                firstNonBlank(
                        stringValue(
                                document.get(
                                        "location"
                                )
                        ),
                        stringValue(
                                document.get(
                                        "address"
                                )
                        ),
                        "Location not available"
                )
        );

        request.setLatitude(
                valueOrEmpty(
                        document.get(
                                "latitude"
                        )
                )
        );

        request.setLongitude(
                valueOrEmpty(
                        document.get(
                                "longitude"
                        )
                )
        );

        // =================================================
        // STATUS
        // =================================================

        String status =
                normalizeStatus(
                        stringValue(
                                document.get(
                                        "status"
                                )
                        )
                );

        /*
         * Old ACTIVE document with actual mechanic
         * should become Assigned instead of Pending.
         */
        if ("Pending".equalsIgnoreCase(status)
                &&
                mechanicId != null) {

            status =
                    "Assigned";
        }

        request.setStatus(
                status
        );

        request.setResponderStatus(
                firstNonBlank(
                        stringValue(
                                document.get(
                                        "responderStatus"
                                )
                        ),
                        responderStatusFromStatus(
                                status
                        )
                )
        );

        // =================================================
        // DATES
        // =================================================

        request.setRequestDate(
                firstNonBlank(
                        stringValue(
                                document.get(
                                        "requestDate"
                                )
                        ),
                        stringValue(
                                document.get(
                                        "createdAt"
                                )
                        ),
                        ""
                )
        );

        request.setAssignedDate(
                valueOrEmpty(
                        document.get(
                                "assignedDate"
                        )
                )
        );

        request.setAcceptedDate(
                valueOrEmpty(
                        document.get(
                                "acceptedDate"
                        )
                )
        );

        request.setStartedDate(
                valueOrEmpty(
                        document.get(
                                "startedDate"
                        )
                )
        );

        request.setResolvedDate(
                valueOrEmpty(
                        document.get(
                                "resolvedDate"
                        )
                )
        );

        request.setCancelledDate(
                valueOrEmpty(
                        document.get(
                                "cancelledDate"
                        )
                )
        );

        request.setCreatedAt(
                document.get(
                        "createdAt"
                )
        );

        request.setUpdatedAt(
                document.get(
                        "updatedAt"
                )
        );

        // =================================================
        // CONTACTS
        // =================================================

        Object contactsNotified =
                document.get(
                        "contactsNotified"
                );

        if (contactsNotified instanceof Boolean) {

            request.setContactsNotified(
                    (Boolean) contactsNotified
            );
        }

        // =================================================
        // TIMELINE
        // =================================================

        request.setTimeline(
                readTimeline(
                        document.get(
                                "timeline"
                        )
                )
        );

        return request;
    }

    // =====================================================
    // MODEL -> FIRESTORE MAP
    //
    // Used during legacy migration.
    // =====================================================

    private Map<String, Object> requestToMap(
            SOSRequest request
    ) {

        Map<String, Object> data =
                new HashMap<>();

        put(
                data,
                "sosId",
                request.getSosId()
        );

        put(
                data,
                "customerId",
                request.getCustomerId()
        );

        put(
                data,
                "customerName",
                request.getCustomerName()
        );

        put(
                data,
                "userId",
                request.getUserId()
        );

        put(
                data,
                "userName",
                request.getUserName()
        );

        put(
                data,
                "userEmail",
                request.getUserEmail()
        );

        put(
                data,
                "vehicleId",
                request.getVehicleId()
        );

        put(
                data,
                "vehicleNumber",
                request.getVehicleNumber()
        );

        put(
                data,
                "mechanicId",
                request.getMechanicId()
        );

        put(
                data,
                "mechanicName",
                request.getMechanicName()
        );

        put(
                data,
                "responderId",
                request.getResponderId()
        );

        put(
                data,
                "responderName",
                request.getResponderName()
        );

        put(
                data,
                "responderStatus",
                request.getResponderStatus()
        );

        put(
                data,
                "emergencyType",
                request.getEmergencyType()
        );

        put(
                data,
                "description",
                request.getDescription()
        );

        put(
                data,
                "location",
                request.getLocation()
        );

        put(
                data,
                "latitude",
                request.getLatitude()
        );

        put(
                data,
                "longitude",
                request.getLongitude()
        );

        put(
                data,
                "status",
                normalizeStatus(
                        request.getStatus()
                )
        );

        put(
                data,
                "requestDate",
                request.getRequestDate()
        );

        put(
                data,
                "assignedDate",
                request.getAssignedDate()
        );

        put(
                data,
                "acceptedDate",
                request.getAcceptedDate()
        );

        put(
                data,
                "startedDate",
                request.getStartedDate()
        );

        put(
                data,
                "resolvedDate",
                request.getResolvedDate()
        );

        put(
                data,
                "cancelledDate",
                request.getCancelledDate()
        );

        data.put(
                "createdAt",
                request.getCreatedAt() != null
                        ? request.getCreatedAt()
                        : System.currentTimeMillis()
        );

        data.put(
                "updatedAt",
                request.getUpdatedAt() != null
                        ? request.getUpdatedAt()
                        : System.currentTimeMillis()
        );

        if (request.getContactsNotified() != null) {

            data.put(
                    "contactsNotified",
                    request.getContactsNotified()
            );
        }

        List<Map<String, Object>> timeline =
                request.getTimeline();

        if (timeline == null
                ||
                timeline.isEmpty()) {

            timeline =
                    createInitialTimeline(
                            System.currentTimeMillis()
                    );
        }

        data.put(
                "timeline",
                timeline
        );

        return data;
    }

    // =====================================================
    // MIGRATE LEGACY CUSTOMER DATA
    // =====================================================

    private void migrateLegacySOSForCustomer(
            String customerId
    ) {

        migrateLegacyCollection(
                LEGACY_COLLECTION_1,
                customerId
        );

        migrateLegacyCollection(
                LEGACY_COLLECTION_2,
                customerId
        );
    }

    // =====================================================
    // MIGRATE LEGACY COLLECTION
    // =====================================================

    private void migrateLegacyCollection(
            String collection,
            String customerId
    ) {

        try {

            QuerySnapshot snapshot =
                    firestore
                            .collection(collection)
                            .get()
                            .get();

            for (DocumentSnapshot document :
                    snapshot.getDocuments()) {

                if (!belongsToCustomer(
                        document,
                        customerId
                )) {

                    continue;
                }

                migrateDocument(
                        document
                );
            }

        } catch (Exception e) {

            System.err.println(
                    "Legacy SOS migration skipped for "
                            + collection
                            + ": "
                            + e.getMessage()
            );
        }
    }

    // =====================================================
    // MIGRATE BY DOCUMENT ID
    // =====================================================

    private boolean migrateLegacyDocumentById(
            String collection,
            String sosId
    ) {

        try {

            DocumentSnapshot document =
                    firestore
                            .collection(collection)
                            .document(sosId)
                            .get()
                            .get();

            if (!document.exists()) {

                return false;
            }

            migrateDocument(
                    document
            );

            return true;

        } catch (Exception e) {

            return false;
        }
    }

    // =====================================================
    // MIGRATE ONE DOCUMENT
    // =====================================================

    private void migrateDocument(
            DocumentSnapshot oldDocument
    ) {

        try {

            SOSRequest request =
                    convertDocument(
                            oldDocument
                    );

            if (request == null) {

                return;
            }

            String sosId =
                    firstNonBlank(
                            request.getSosId(),
                            oldDocument.getId()
                    );

            request.setSosId(
                    sosId
            );

            DocumentReference canonical =
                    firestore
                            .collection(SOS_COLLECTION)
                            .document(sosId);

            DocumentSnapshot existing =
                    canonical
                            .get()
                            .get();

            if (existing.exists()) {

                return;
            }

            canonical
                    .set(
                            requestToMap(
                                    request
                            ),
                            SetOptions.merge()
                    )
                    .get();

            System.out.println(
                    "Migrated SOS to SOSRequests: "
                            + sosId
            );

        } catch (Exception e) {

            System.err.println(
                    "Unable to migrate SOS: "
                            + e.getMessage()
            );
        }
    }

    // =====================================================
    // STATUS
    // =====================================================

    private String normalizeStatus(
            String status
    ) {

        status =
                clean(
                        status
                );

        if (status == null) {

            return "Pending";
        }

        if (status.equalsIgnoreCase("ACTIVE")
                ||
                status.equalsIgnoreCase("Pending")) {

            return "Pending";
        }

        if (status.equalsIgnoreCase("Assigned")) {

            return "Assigned";
        }

        if (status.equalsIgnoreCase("Accepted")) {

            return "Accepted";
        }

        if (status.equalsIgnoreCase("In Progress")
                ||
                status.equalsIgnoreCase("InProgress")
                ||
                status.equalsIgnoreCase("En Route")
                ||
                status.equalsIgnoreCase("EnRoute")) {

            return "In Progress";
        }

        if (status.equalsIgnoreCase("Resolved")
                ||
                status.equalsIgnoreCase("Completed")
                ||
                status.equalsIgnoreCase("COMPLETED")
                ||
                status.equalsIgnoreCase("Closed")) {

            return "Resolved";
        }

        if (status.equalsIgnoreCase("Cancelled")
                ||
                status.equalsIgnoreCase("Canceled")
                ||
                status.equalsIgnoreCase("CANCELLED")) {

            return "Cancelled";
        }

        return status;
    }

    // =====================================================
    // ACTIVE STATUS
    // =====================================================

    private boolean isActiveStatus(
            String status
    ) {

        return "Pending".equalsIgnoreCase(status)
                ||
                "Assigned".equalsIgnoreCase(status)
                ||
                "Accepted".equalsIgnoreCase(status)
                ||
                "In Progress".equalsIgnoreCase(status);
    }

    // =====================================================
    // RESPONDER STATUS
    // =====================================================

    private String responderStatusFromStatus(
            String status
    ) {

        status =
                normalizeStatus(
                        status
                );

        if ("Pending".equalsIgnoreCase(status)) {

            return "Waiting";
        }

        if ("Assigned".equalsIgnoreCase(status)) {

            return "Assigned";
        }

        if ("Accepted".equalsIgnoreCase(status)) {

            return "Accepted";
        }

        if ("In Progress".equalsIgnoreCase(status)) {

            return "In Progress";
        }

        if ("Resolved".equalsIgnoreCase(status)) {

            return "Completed";
        }

        if ("Cancelled".equalsIgnoreCase(status)) {

            return "Cancelled";
        }

        return status;
    }

    // =====================================================
    // REQUEST TIME
    // =====================================================

    private long getRequestTime(
            SOSRequest request
    ) {

        if (request == null) {

            return 0L;
        }

        long value =
                toMillis(
                        request.getUpdatedAt()
                );

        if (value > 0) {

            return value;
        }

        value =
                toMillis(
                        request.getCreatedAt()
                );

        if (value > 0) {

            return value;
        }

        return toMillis(
                request.getRequestDate()
        );
    }

    // =====================================================
    // DOCUMENT TIME
    // =====================================================

    private long getDocumentTime(
            DocumentSnapshot document
    ) {

        if (document == null) {

            return 0L;
        }

        long value =
                toMillis(
                        document.get(
                                "updatedAt"
                        )
                );

        if (value > 0) {

            return value;
        }

        value =
                toMillis(
                        document.get(
                                "createdAt"
                        )
                );

        if (value > 0) {

            return value;
        }

        return toMillis(
                document.get(
                        "requestDate"
                )
        );
    }

    // =====================================================
    // OBJECT -> MILLIS
    // =====================================================

    private long toMillis(
            Object value
    ) {

        if (value == null) {

            return 0L;
        }

        if (value instanceof Number) {

            long number =
                    ((Number) value)
                            .longValue();

            if (number > 0
                    &&
                    number < 100000000000L) {

                number *= 1000L;
            }

            return number;
        }

        if (value instanceof java.util.Date) {

            return ((java.util.Date) value)
                    .getTime();
        }

        if (value instanceof com.google.cloud.Timestamp) {

            return ((com.google.cloud.Timestamp) value)
                    .toSqlTimestamp()
                    .getTime();
        }

        try {

            long number =
                    Long.parseLong(
                            String.valueOf(
                                    value
                            ).trim()
                    );

            if (number > 0
                    &&
                    number < 100000000000L) {

                number *= 1000L;
            }

            return number;

        } catch (Exception e) {

            return 0L;
        }
    }

    // =====================================================
    // SAME ID
    // =====================================================

    private boolean sameId(
            String first,
            String second
    ) {

        first =
                normalizeId(
                        first
                );

        second =
                normalizeId(
                        second
                );

        return first != null
                &&
                second != null
                &&
                first.equals(
                        second
                );
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
    // PUT
    // =====================================================

    private void put(
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
    // FIRST NON-BLANK
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
    // STRING VALUE
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
    // VALUE OR EMPTY
    // =====================================================

    private String valueOrEmpty(
            Object value
    ) {

        String value1 =
                stringValue(
                        value   
                );

        return value1 == null
                ? ""
                : value1;
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