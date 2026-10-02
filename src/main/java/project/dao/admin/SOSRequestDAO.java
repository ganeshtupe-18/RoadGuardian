package project.dao.admin;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SOSRequestDAO {

    // =====================================================
    // CANONICAL COLLECTION
    // =====================================================

    private static final String COLLECTION =
            "SOSRequests";

    // =====================================================
    // OLD COLLECTIONS
    // =====================================================

    private static final String LEGACY_COLLECTION_1 =
            "sosRequests";

    private static final String LEGACY_COLLECTION_2 =
            "sos_requests";

    private final Firestore firestore;

    // =====================================================
    // EXISTING CONSTRUCTOR
    // =====================================================

    public SOSRequestDAO(
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
    // OPTIONAL DEFAULT CONSTRUCTOR
    // =====================================================

    public SOSRequestDAO() {

        try {

            this.firestore =
                    FirebaseConfig.getFirestore();

        } catch (IOException e) {

            throw new RuntimeException(
                    "Unable to initialize Firebase.",
                    e
            );
        }
    }

    // =====================================================
    // GET ALL SOS REQUESTS
    // =====================================================

    public List<SOSRequest> getAllRequests()
            throws Exception {

        // -------------------------------------------------
        // First bring old SOS documents into canonical
        // collection.
        // -------------------------------------------------

        migrateLegacyCollections();

        List<SOSRequest> requests =
                new ArrayList<>();

        QuerySnapshot snapshot =
                firestore
                        .collection(COLLECTION)
                        .get()
                        .get();

        for (DocumentSnapshot document :
                snapshot.getDocuments()) {

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

        // Latest first
        requests.sort(
                (first, second) ->
                        Long.compare(
                                getRequestTimestamp(
                                        second
                                ),
                                getRequestTimestamp(
                                        first
                                )
                        )
        );

        return requests;
    }

    // =====================================================
    // GET SOS BY ID
    // =====================================================

    public SOSRequest getRequestById(
            String sosId
    ) throws Exception {

        sosId =
                clean(
                        sosId
                );

        if (sosId == null) {

            return null;
        }

        migrateLegacyRequestById(
                sosId
        );

        DocumentSnapshot document =
                firestore
                        .collection(COLLECTION)
                        .document(sosId)
                        .get()
                        .get();

        if (!document.exists()) {

            /*
             * Some old documents may use a different
             * document ID but contain sosId field.
             */
            QuerySnapshot query =
                    firestore
                            .collection(COLLECTION)
                            .whereEqualTo(
                                    "sosId",
                                    sosId
                            )
                            .limit(1)
                            .get()
                            .get();

            if (query.isEmpty()) {

                return null;
            }

            document =
                    query
                            .getDocuments()
                            .get(0);
        }

        return convertDocument(
                document
        );
    }

    // =====================================================
    // ADD SOS REQUEST
    // =====================================================

    public boolean addRequest(
            SOSRequest request
    ) throws Exception {

        if (request == null) {

            return false;
        }

        DocumentReference document =
                firestore
                        .collection(COLLECTION)
                        .document();

        long now =
                System.currentTimeMillis();

        request.setSosId(
                document.getId()
        );

        request.setCustomerId(
                normalizeId(
                        firstNonBlank(
                                request.getCustomerId(),
                                request.getUserEmail(),
                                request.getUserId()
                        )
                )
        );

        request.setCustomerName(
                firstNonBlank(
                        request.getCustomerName(),
                        request.getUserName(),
                        "Customer"
                )
        );

        request.setStatus(
                normalizeStatus(
                        request.getStatus()
                )
        );

        if (clean(
                request.getStatus()
        ) == null) {

            request.setStatus(
                    "Pending"
            );
        }

        if (clean(
                request.getRequestDate()
        ) == null) {

            request.setRequestDate(
                    String.valueOf(
                            now
                    )
            );
        }

        request.setCreatedAt(
                request.getCreatedAt() == null
                        ? now
                        : request.getCreatedAt()
        );

        request.setUpdatedAt(
                now
        );

        document
                .set(
                        requestToMap(
                                request
                        )
                )
                .get();

        return true;
    }

    // =====================================================
    // UPDATE REQUEST
    // =====================================================

    public boolean updateRequest(
            SOSRequest request
    ) throws Exception {

        if (request == null
                ||
                clean(
                        request.getSosId()
                ) == null) {

            return false;
        }

        String sosId =
                request
                        .getSosId()
                        .trim();

        DocumentReference reference =
                getRequestReference(
                        sosId
                );

        if (reference == null) {

            return false;
        }

        request.setUpdatedAt(
                System.currentTimeMillis()
        );

        reference
                .set(
                        requestToMap(
                                request
                        ),
                        SetOptions.merge()
                )
                .get();

        return true;
    }

    // =====================================================
    // UPDATE STATUS
    //
    // Also handles lifecycle dates.
    // =====================================================

    public boolean updateStatus(
            String sosId,
            String status
    ) throws Exception {

        sosId =
                clean(
                        sosId
                );

        status =
                normalizeStatus(
                        status
                );

        if (sosId == null
                ||
                status == null) {

            return false;
        }

        DocumentReference reference =
                getRequestReference(
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

        long now =
                System.currentTimeMillis();

        Map<String, Object> updates =
                new HashMap<>();

        updates.put(
                "status",
                status
        );

        updates.put(
                "updatedAt",
                now
        );

        // =================================================
        // PENDING
        //
        // Pending request should not remain assigned.
        // =================================================

        if (status.equalsIgnoreCase(
                "Pending"
        )) {

            updates.put(
                    "mechanicId",
                    ""
            );

            updates.put(
                    "mechanicName",
                    ""
            );

            updates.put(
                    "responderId",
                    ""
            );

            updates.put(
                    "responderName",
                    ""
            );

            updates.put(
                    "responderStatus",
                    "Waiting"
            );

            updates.put(
                    "assignedDate",
                    ""
            );

            updates.put(
                    "acceptedDate",
                    ""
            );

            updates.put(
                    "startedDate",
                    ""
            );
        }

        // =================================================
        // ASSIGNED
        // =================================================

        if (status.equalsIgnoreCase(
                "Assigned"
        )) {

            updates.put(
                    "assignedDate",
                    String.valueOf(
                            now
                    )
            );

            updates.put(
                    "responderStatus",
                    "Assigned"
            );
        }

        // =================================================
        // ACCEPTED
        // =================================================

        if (status.equalsIgnoreCase(
                "Accepted"
        )) {

            updates.put(
                    "acceptedDate",
                    String.valueOf(
                            now
                    )
            );

            updates.put(
                    "responderStatus",
                    "Accepted"
            );
        }

        // =================================================
        // IN PROGRESS
        // =================================================

        if (status.equalsIgnoreCase(
                "In Progress"
        )) {

            updates.put(
                    "startedDate",
                    String.valueOf(
                            now
                    )
            );

            updates.put(
                    "responderStatus",
                    "In Progress"
            );
        }

        // =================================================
        // RESOLVED
        // =================================================

        if (status.equalsIgnoreCase(
                "Resolved"
        )) {

            updates.put(
                    "resolvedDate",
                    String.valueOf(
                            now
                    )
            );

            updates.put(
                    "responderStatus",
                    "Completed"
            );
        }

        // =================================================
        // CANCELLED
        // =================================================

        if (status.equalsIgnoreCase(
                "Cancelled"
        )) {

            updates.put(
                    "cancelledDate",
                    String.valueOf(
                            now
                    )
            );

            updates.put(
                    "responderStatus",
                    "Cancelled"
            );
        }

        updates.put(
                "timeline",
                updateTimeline(
                        document,
                        status,
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
    // ASSIGN MECHANIC
    //
    // IMPORTANT:
    //
    // OLD:
    // Admin assign -> Accepted ❌
    //
    // NEW:
    // Admin assign -> Assigned ✅
    // =====================================================

    public boolean assignMechanic(
            String sosId,
            String mechanicId,
            String mechanicName
    ) throws Exception {

        sosId =
                clean(
                        sosId
                );

        mechanicId =
                normalizeId(
                        mechanicId
                );

        mechanicName =
                clean(
                        mechanicName
                );

        if (sosId == null
                ||
                mechanicId == null) {

            return false;
        }

        DocumentReference reference =
                getRequestReference(
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

        String currentStatus =
                normalizeStatus(
                        stringValue(
                                document.get(
                                        "status"
                                )
                        )
                );

        /*
         * Admin may assign a new mechanic while request
         * is Pending or Assigned.
         *
         * Once mechanic accepts/starts/resolves it,
         * normal assignment should not overwrite it.
         */
        if (
                !currentStatus.equalsIgnoreCase(
                        "Pending"
                )
                        &&
                !currentStatus.equalsIgnoreCase(
                        "Assigned"
                )
        ) {

            return false;
        }

        long now =
                System.currentTimeMillis();

        Map<String, Object> updates =
                new HashMap<>();

        // =================================================
        // CANONICAL
        // =================================================

        updates.put(
                "mechanicId",
                mechanicId
        );

        updates.put(
                "mechanicName",
                mechanicName == null
                        ? ""
                        : mechanicName
        );

        // =================================================
        // OLD RESPONDER ALIASES
        // =================================================

        updates.put(
                "responderId",
                mechanicId
        );

        updates.put(
                "responderName",
                mechanicName == null
                        ? ""
                        : mechanicName
        );

        updates.put(
                "responderStatus",
                "Assigned"
        );

        // =================================================
        // CORRECT STATUS
        // =================================================

        updates.put(
                "status",
                "Assigned"
        );

        updates.put(
                "assignedDate",
                String.valueOf(
                        now
                )
        );

        updates.put(
                "acceptedDate",
                ""
        );

        updates.put(
                "startedDate",
                ""
        );

        updates.put(
                "updatedAt",
                now
        );

        updates.put(
                "timeline",
                updateTimeline(
                        document,
                        "Assigned",
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
    // ACCEPT SOS
    //
    // Assigned -> Accepted
    //
    // This will be used from Mechanic side.
    // =====================================================

    public boolean acceptRequest(
            String sosId,
            String mechanicId
    ) throws Exception {

        sosId =
                clean(
                        sosId
                );

        mechanicId =
                normalizeId(
                        mechanicId
                );

        if (sosId == null
                ||
                mechanicId == null) {

            return false;
        }

        DocumentReference reference =
                getRequestReference(
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

        String assignedMechanic =
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

        String status =
                normalizeStatus(
                        stringValue(
                                document.get(
                                        "status"
                                )
                        )
                );

        if (!sameId(
                assignedMechanic,
                mechanicId
        )) {

            return false;
        }

        if (!status.equalsIgnoreCase(
                "Assigned"
        )) {

            return false;
        }

        return updateStatus(
                sosId,
                "Accepted"
        );
    }

    // =====================================================
    // START SOS ASSISTANCE
    //
    // Accepted -> In Progress
    // =====================================================

    public boolean startRequest(
            String sosId,
            String mechanicId
    ) throws Exception {

        sosId =
                clean(
                        sosId
                );

        mechanicId =
                normalizeId(
                        mechanicId
                );

        if (sosId == null
                ||
                mechanicId == null) {

            return false;
        }

        SOSRequest request =
                getRequestById(
                        sosId
                );

        if (request == null) {

            return false;
        }

        if (!sameId(
                request.getMechanicId(),
                mechanicId
        )) {

            return false;
        }

        if (!request.isAccepted()) {

            return false;
        }

        return updateStatus(
                sosId,
                "In Progress"
        );
    }

    // =====================================================
    // RESOLVE SOS
    //
    // In Progress -> Resolved
    // =====================================================

    public boolean resolveRequest(
            String sosId
    ) throws Exception {

        SOSRequest request =
                getRequestById(
                        sosId
                );

        if (request == null) {

            return false;
        }

        if (
                !request.isInProgress()
                        &&
                !request.isAccepted()
        ) {

            return false;
        }

        return updateStatus(
                sosId,
                "Resolved"
        );
    }

    // =====================================================
    // CANCEL SOS
    // =====================================================

    public boolean cancelRequest(
            String sosId
    ) throws Exception {

        SOSRequest request =
                getRequestById(
                        sosId
                );

        if (request == null
                ||
                request.isFinished()) {

            return false;
        }

        return updateStatus(
                sosId,
                "Cancelled"
        );
    }

    // =====================================================
    // GET CUSTOMER SOS REQUESTS
    // =====================================================

    public List<SOSRequest> getRequestsByCustomerId(
            String customerId
    ) throws Exception {

        customerId =
                normalizeId(
                        customerId
                );

        List<SOSRequest> result =
                new ArrayList<>();

        if (customerId == null) {

            return result;
        }

        /*
         * Use getAllRequests() so customerId / userEmail /
         * userId aliases all work.
         */
        for (SOSRequest request :
                getAllRequests()) {

            String requestCustomerId =
                    normalizeId(
                            firstNonBlank(
                                    request.getCustomerId(),
                                    request.getUserEmail(),
                                    request.getUserId()
                            )
                    );

            if (sameId(
                    requestCustomerId,
                    customerId
            )) {

                result.add(
                        request
                );
            }
        }

        return result;
    }

    // =====================================================
    // GET MECHANIC SOS REQUESTS
    // =====================================================

    public List<SOSRequest> getRequestsByMechanicId(
            String mechanicId
    ) throws Exception {

        mechanicId =
                normalizeId(
                        mechanicId
                );

        List<SOSRequest> result =
                new ArrayList<>();

        if (mechanicId == null) {

            return result;
        }

        for (SOSRequest request :
                getAllRequests()) {

            String assignedMechanic =
                    normalizeId(
                            firstNonBlank(
                                    request.getMechanicId(),
                                    request.getResponderId()
                            )
                    );

            if (sameId(
                    assignedMechanic,
                    mechanicId
            )) {

                result.add(
                        request
                );
            }
        }

        return result;
    }

    // =====================================================
    // GET REQUESTS BY STATUS
    // =====================================================

    public List<SOSRequest> getRequestsByStatus(
            String status
    ) throws Exception {

        List<SOSRequest> all =
                getAllRequests();

        if (status == null
                ||
                status.isBlank()
                ||
                status.equalsIgnoreCase(
                        "All"
                )
                ||
                status.equalsIgnoreCase(
                        "All Status"
                )) {

            return all;
        }

        String normalizedStatus =
                normalizeStatus(
                        status
                );

        List<SOSRequest> result =
                new ArrayList<>();

        for (SOSRequest request :
                all) {

            if (request.getStatus() != null
                    &&
                    request
                            .getStatus()
                            .equalsIgnoreCase(
                                    normalizedStatus
                            )) {

                result.add(
                        request
                );
            }
        }

        return result;
    }

    // =====================================================
    // ASSIGNED REQUESTS
    // =====================================================

    public List<SOSRequest> getAssignedRequests()
            throws Exception {

        return getRequestsByStatus(
                "Assigned"
        );
    }

    // =====================================================
    // ACCEPTED REQUESTS
    // =====================================================

    public List<SOSRequest> getAcceptedRequests()
            throws Exception {

        return getRequestsByStatus(
                "Accepted"
        );
    }

    // =====================================================
    // IN PROGRESS
    // =====================================================

    public List<SOSRequest> getInProgressRequests()
            throws Exception {

        return getRequestsByStatus(
                "In Progress"
        );
    }

    // =====================================================
    // ACTIVE SOS
    //
    // Pending + Assigned + Accepted + In Progress
    // =====================================================

    public List<SOSRequest> getActiveRequests()
            throws Exception {

        List<SOSRequest> result =
                new ArrayList<>();

        for (SOSRequest request :
                getAllRequests()) {

            if (request.isActive()) {

                result.add(
                        request
                );
            }
        }

        return result;
    }

    // =====================================================
    // SEARCH
    // =====================================================

    public List<SOSRequest> searchRequests(
            String searchText
    ) throws Exception {

        List<SOSRequest> all =
                getAllRequests();

        if (searchText == null
                ||
                searchText.isBlank()) {

            return all;
        }

        String search =
                searchText
                        .trim()
                        .toLowerCase();

        List<SOSRequest> result =
                new ArrayList<>();

        for (SOSRequest request :
                all) {

            if (
                    contains(
                            request.getSosId(),
                            search
                    )
                            ||
                    contains(
                            request.getCustomerId(),
                            search
                    )
                            ||
                    contains(
                            request.getCustomerName(),
                            search
                    )
                            ||
                    contains(
                            request.getVehicleNumber(),
                            search
                    )
                            ||
                    contains(
                            request.getMechanicName(),
                            search
                    )
                            ||
                    contains(
                            request.getEmergencyType(),
                            search
                    )
                            ||
                    contains(
                            request.getDescription(),
                            search
                    )
                            ||
                    contains(
                            request.getLocation(),
                            search
                    )
                            ||
                    contains(
                            request.getStatus(),
                            search
                    )
            ) {

                result.add(
                        request
                );
            }
        }

        return result;
    }

    // =====================================================
    // DELETE
    // =====================================================

    public boolean deleteRequest(
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
                getRequestReference(
                        sosId
                );

        if (reference == null) {

            return false;
        }

        reference
                .delete()
                .get();

        return true;
    }

    // =====================================================
    // GET CORRECT CANONICAL REFERENCE
    // =====================================================

    private DocumentReference getRequestReference(
            String sosId
    ) throws Exception {

        sosId =
                clean(
                        sosId
                );

        if (sosId == null) {

            return null;
        }

        migrateLegacyRequestById(
                sosId
        );

        DocumentReference direct =
                firestore
                        .collection(COLLECTION)
                        .document(sosId);

        if (direct
                .get()
                .get()
                .exists()) {

            return direct;
        }

        QuerySnapshot query =
                firestore
                        .collection(COLLECTION)
                        .whereEqualTo(
                                "sosId",
                                sosId
                        )
                        .limit(1)
                        .get()
                        .get();

        if (query.isEmpty()) {

            return null;
        }

        return query
                .getDocuments()
                .get(0)
                .getReference();
    }

    // =====================================================
    // FIRESTORE -> SOS REQUEST
    //
    // Manual conversion avoids old Firestore type errors.
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
        // VEHICLE
        // =================================================

        request.setVehicleId(
                firstNonBlank(
                        stringValue(
                                document.get(
                                        "vehicleId"
                                )
                        ),
                        ""
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
                        "Emergency Roadside Assistance"
                )
        );

        request.setDescription(
                firstNonBlank(
                        stringValue(
                                document.get(
                                        "description"
                                )
                        ),
                        stringValue(
                                document.get(
                                        "issue"
                                )
                        ),
                        "Emergency SOS request"
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
                        "Unknown"
                )
        );

        request.setLatitude(
                firstNonBlank(
                        stringValue(
                                document.get(
                                        "latitude"
                                )
                        ),
                        ""
                )
        );

        request.setLongitude(
                firstNonBlank(
                        stringValue(
                                document.get(
                                        "longitude"
                                )
                        ),
                        ""
                )
        );

        // =================================================
        // STATUS
        // =================================================

        request.setStatus(
                normalizeStatus(
                        stringValue(
                                document.get(
                                        "status"
                                )
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
        // OLD USER ALIASES
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
        // RESPONDER ALIASES
        // =================================================

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

        request.setResponderStatus(
                firstNonBlank(
                        stringValue(
                                document.get(
                                        "responderStatus"
                                )
                        ),
                        getResponderStatus(
                                request.getStatus()
                        )
                )
        );

        // =================================================
        // CONTACTS
        // =================================================

        Object contacts =
                document.get(
                        "contactsNotified"
                );

        if (contacts instanceof Boolean) {

            request.setContactsNotified(
                    (Boolean) contacts
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
    // Writes canonical + compatibility aliases.
    // =====================================================

    private Map<String, Object> requestToMap(
            SOSRequest request
    ) {

        Map<String, Object> data =
                new HashMap<>();

        putIfNotNull(
                data,
                "sosId",
                request.getSosId()
        );

        // CUSTOMER
        putIfNotNull(
                data,
                "customerId",
                normalizeId(
                        firstNonBlank(
                                request.getCustomerId(),
                                request.getUserEmail(),
                                request.getUserId()
                        )
                )
        );

        putIfNotNull(
                data,
                "customerName",
                firstNonBlank(
                        request.getCustomerName(),
                        request.getUserName()
                )
        );

        // VEHICLE
        putIfNotNull(
                data,
                "vehicleId",
                request.getVehicleId()
        );

        putIfNotNull(
                data,
                "vehicleNumber",
                request.getVehicleNumber()
        );

        // MECHANIC
        putIfNotNull(
                data,
                "mechanicId",
                normalizeId(
                        firstNonBlank(
                                request.getMechanicId(),
                                request.getResponderId()
                        )
                )
        );

        putIfNotNull(
                data,
                "mechanicName",
                firstNonBlank(
                        request.getMechanicName(),
                        request.getResponderName()
                )
        );

        // EMERGENCY
        putIfNotNull(
                data,
                "emergencyType",
                request.getEmergencyType()
        );

        putIfNotNull(
                data,
                "description",
                request.getDescription()
        );

        // LOCATION
        putIfNotNull(
                data,
                "location",
                request.getLocation()
        );

        putIfNotNull(
                data,
                "latitude",
                request.getLatitude()
        );

        putIfNotNull(
                data,
                "longitude",
                request.getLongitude()
        );

        // STATUS
        putIfNotNull(
                data,
                "status",
                normalizeStatus(
                        request.getStatus()
                )
        );

        // DATES
        putIfNotNull(
                data,
                "requestDate",
                request.getRequestDate()
        );

        putIfNotNull(
                data,
                "assignedDate",
                request.getAssignedDate()
        );

        putIfNotNull(
                data,
                "acceptedDate",
                request.getAcceptedDate()
        );

        putIfNotNull(
                data,
                "startedDate",
                request.getStartedDate()
        );

        putIfNotNull(
                data,
                "resolvedDate",
                request.getResolvedDate()
        );

        putIfNotNull(
                data,
                "cancelledDate",
                request.getCancelledDate()
        );

        if (request.getCreatedAt() != null) {

            data.put(
                    "createdAt",
                    request.getCreatedAt()
            );
        }

        if (request.getUpdatedAt() != null) {

            data.put(
                    "updatedAt",
                    request.getUpdatedAt()
            );
        }

        // =================================================
        // OLD USER ALIASES
        // =================================================

        putIfNotNull(
                data,
                "userId",
                firstNonBlank(
                        request.getUserId(),
                        request.getCustomerId()
                )
        );

        putIfNotNull(
                data,
                "userName",
                firstNonBlank(
                        request.getUserName(),
                        request.getCustomerName()
                )
        );

        putIfNotNull(
                data,
                "userEmail",
                firstNonBlank(
                        request.getUserEmail(),
                        request.getCustomerId()
                )
        );

        // =================================================
        // RESPONDER ALIASES
        // =================================================

        putIfNotNull(
                data,
                "responderId",
                firstNonBlank(
                        request.getResponderId(),
                        request.getMechanicId()
                )
        );

        putIfNotNull(
                data,
                "responderName",
                firstNonBlank(
                        request.getResponderName(),
                        request.getMechanicName()
                )
        );

        putIfNotNull(
                data,
                "responderStatus",
                firstNonBlank(
                        request.getResponderStatus(),
                        getResponderStatus(
                                request.getStatus()
                        )
                )
        );

        if (request.getContactsNotified() != null) {

            data.put(
                    "contactsNotified",
                    request.getContactsNotified()
            );
        }

        if (request.getTimeline() != null) {

            data.put(
                    "timeline",
                    request.getTimeline()
            );
        }

        return data;
    }

    // =====================================================
    // LEGACY COLLECTION MIGRATION
    // =====================================================

    private void migrateLegacyCollections() {

        migrateLegacyCollection(
                LEGACY_COLLECTION_1
        );

        migrateLegacyCollection(
                LEGACY_COLLECTION_2
        );
    }

    // =====================================================
    // MIGRATE ONE COLLECTION
    // =====================================================

    private void migrateLegacyCollection(
            String collectionName
    ) {

        try {

            QuerySnapshot snapshot =
                    firestore
                            .collection(
                                    collectionName
                            )
                            .get()
                            .get();

            for (DocumentSnapshot document :
                    snapshot.getDocuments()) {

                migrateLegacyDocument(
                        document
                );
            }

        } catch (Exception e) {

            /*
             * Old collection missing / unreadable should
             * never break Admin SOS page.
             */
            System.err.println(
                    "SOS migration skipped for "
                            + collectionName
                            + ": "
                            + e.getMessage()
            );
        }
    }

    // =====================================================
    // MIGRATE BY ID
    // =====================================================

    private void migrateLegacyRequestById(
            String sosId
    ) {

        if (clean(sosId) == null) {

            return;
        }

        try {

            DocumentSnapshot first =
                    firestore
                            .collection(
                                    LEGACY_COLLECTION_1
                            )
                            .document(sosId)
                            .get()
                            .get();

            if (first.exists()) {

                migrateLegacyDocument(
                        first
                );
            }

            DocumentSnapshot second =
                    firestore
                            .collection(
                                    LEGACY_COLLECTION_2
                            )
                            .document(sosId)
                            .get()
                            .get();

            if (second.exists()) {

                migrateLegacyDocument(
                        second
                );
            }

        } catch (Exception e) {

            System.err.println(
                    "SOS migration lookup skipped: "
                            + e.getMessage()
            );
        }
    }

    // =====================================================
    // MIGRATE DOCUMENT
    // =====================================================

    private void migrateLegacyDocument(
            DocumentSnapshot oldDocument
    ) {

        if (oldDocument == null
                ||
                !oldDocument.exists()) {

            return;
        }

        try {

            SOSRequest request =
                    convertDocument(
                            oldDocument
                    );

            if (request == null) {

                return;
            }

            String sosId =
                    clean(
                            request.getSosId()
                    );

            if (sosId == null) {

                sosId =
                        oldDocument.getId();

                request.setSosId(
                        sosId
                );
            }

            DocumentReference canonical =
                    firestore
                            .collection(COLLECTION)
                            .document(sosId);

            DocumentSnapshot existing =
                    canonical
                            .get()
                            .get();

            /*
             * Canonical data has priority.
             */
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

        } catch (Exception e) {

            System.err.println(
                    "Unable to migrate legacy SOS "
                            + oldDocument.getId()
                            + ": "
                            + e.getMessage()
            );
        }
    }

    // =====================================================
    // TIMELINE
    // =====================================================

    private List<Map<String, Object>> updateTimeline(
            DocumentSnapshot document,
            String status,
            long timestamp
    ) {

        List<Map<String, Object>> timeline =
                readTimeline(
                        document.get(
                                "timeline"
                        )
                );

        if (timeline.isEmpty()) {

            timeline.add(
                    createTimelineItem(
                            "SOS request received",
                            "Completed",
                            getDocumentTimestamp(
                                    document
                            )
                    )
            );

            timeline.add(
                    createTimelineItem(
                            "Emergency contacts notified",
                            "Completed",
                            getDocumentTimestamp(
                                    document
                            )
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
        }

        status =
                normalizeStatus(
                        status
                );

        if (
                status.equalsIgnoreCase(
                        "Assigned"
                )
                        ||
                status.equalsIgnoreCase(
                        "Accepted"
                )
                        ||
                status.equalsIgnoreCase(
                        "In Progress"
                )
                        ||
                status.equalsIgnoreCase(
                        "Resolved"
                )
        ) {

            markTimelineComplete(
                    timeline,
                    "Mechanic assigned",
                    timestamp
            );
        }

        if (
                status.equalsIgnoreCase(
                        "In Progress"
                )
                        ||
                status.equalsIgnoreCase(
                        "Resolved"
                )
        ) {

            markTimelineComplete(
                    timeline,
                    "Responder en route",
                    timestamp
            );
        }

        if (status.equalsIgnoreCase(
                "Resolved"
        )) {

            markTimelineComplete(
                    timeline,
                    "Assistance completed",
                    timestamp
            );
        }

        if (status.equalsIgnoreCase(
                "Cancelled"
        )) {

            addOrUpdateTimeline(
                    timeline,
                    "SOS cancelled",
                    "Completed",
                    timestamp
            );
        }

        return timeline;
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

            for (Map.Entry<?, ?> entry :
                    ((Map<?, ?>) item).entrySet()) {

                if (entry.getKey() != null) {

                    copy.put(
                            String.valueOf(
                                    entry.getKey()
                            ),
                            entry.getValue()
                    );
                }
            }

            result.add(
                    copy
            );
        }

        return result;
    }

    // =====================================================
    // TIMELINE COMPLETE
    // =====================================================

    private void markTimelineComplete(
            List<Map<String, Object>> timeline,
            String title,
            long timestamp
    ) {

        addOrUpdateTimeline(
                timeline,
                title,
                "Completed",
                timestamp
        );
    }

    // =====================================================
    // ADD / UPDATE TIMELINE ITEM
    // =====================================================

    private void addOrUpdateTimeline(
            List<Map<String, Object>> timeline,
            String title,
            String status,
            long timestamp
    ) {

        for (Map<String, Object> item :
                timeline) {

            String currentTitle =
                    stringValue(
                            item.get(
                                    "title"
                            )
                    );

            if (currentTitle != null
                    &&
                    currentTitle.equalsIgnoreCase(
                            title
                    )) {

                item.put(
                        "status",
                        status
                );

                item.put(
                        "timestamp",
                        timestamp
                );

                return;
            }
        }

        timeline.add(
                createTimelineItem(
                        title,
                        status,
                        timestamp
                )
        );
    }

    // =====================================================
    // CREATE TIMELINE ITEM
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
    // STATUS NORMALIZATION
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

        if (
                status.equalsIgnoreCase(
                        "Pending"
                )
                        ||
                status.equalsIgnoreCase(
                        "Active"
                )
        ) {

            return "Pending";
        }

        if (status.equalsIgnoreCase(
                "Assigned"
        )) {

            return "Assigned";
        }

        if (status.equalsIgnoreCase(
                "Accepted"
        )) {

            return "Accepted";
        }

        if (
                status.equalsIgnoreCase(
                        "In Progress"
                )
                        ||
                status.equalsIgnoreCase(
                        "InProgress"
                )
                        ||
                status.equalsIgnoreCase(
                        "En Route"
                )
                        ||
                status.equalsIgnoreCase(
                        "EnRoute"
                )
        ) {

            return "In Progress";
        }

        if (
                status.equalsIgnoreCase(
                        "Resolved"
                )
                        ||
                status.equalsIgnoreCase(
                        "Completed"
                )
                        ||
                status.equalsIgnoreCase(
                        "Closed"
                )
        ) {

            return "Resolved";
        }

        if (
                status.equalsIgnoreCase(
                        "Cancelled"
                )
                        ||
                status.equalsIgnoreCase(
                        "Canceled"
                )
        ) {

            return "Cancelled";
        }

        return status;
    }

    // =====================================================
    // RESPONDER STATUS
    // =====================================================

    private String getResponderStatus(
            String status
    ) {

        status =
                normalizeStatus(
                        status
                );

        if (status.equalsIgnoreCase(
                "Pending"
        )) {

            return "Waiting";
        }

        if (status.equalsIgnoreCase(
                "Assigned"
        )) {

            return "Assigned";
        }

        if (status.equalsIgnoreCase(
                "Accepted"
        )) {

            return "Accepted";
        }

        if (status.equalsIgnoreCase(
                "In Progress"
        )) {

            return "In Progress";
        }

        if (status.equalsIgnoreCase(
                "Resolved"
        )) {

            return "Completed";
        }

        if (status.equalsIgnoreCase(
                "Cancelled"
        )) {

            return "Cancelled";
        }

        return status;
    }

    // =====================================================
    // SORT TIMESTAMP
    // =====================================================

    private long getRequestTimestamp(
            SOSRequest request
    ) {

        if (request == null) {

            return 0L;
        }

        return firstTimestamp(
                request.getUpdatedAt(),
                request.getCreatedAt(),
                request.getRequestDate()
        );
    }

    // =====================================================
    // DOCUMENT TIMESTAMP
    // =====================================================

    private long getDocumentTimestamp(
            DocumentSnapshot document
    ) {

        if (document == null) {

            return 0L;
        }

        return firstTimestamp(
                document.get(
                        "updatedAt"
                ),
                document.get(
                        "createdAt"
                ),
                document.get(
                        "requestDate"
                )
        );
    }

    // =====================================================
    // FIRST TIMESTAMP
    // =====================================================

    private long firstTimestamp(
            Object... values
    ) {

        if (values == null) {

            return 0L;
        }

        for (Object value :
                values) {

            long timestamp =
                    toMillis(
                            value
                    );

            if (timestamp > 0) {

                return timestamp;
            }
        }

        return 0L;
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
    // PUT IF NOT NULL
    // =====================================================

    private void putIfNotNull(
            Map<String, Object> data,
            String key,
            Object value
    ) {

        if (value != null) {

            data.put(
                    key,
                    value
            );
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
    // SEARCH
    // =====================================================

    private boolean contains(
            String value,
            String search
    ) {

        return value != null
                &&
                value
                        .toLowerCase()
                        .contains(
                                search
                        );
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
    // EMPTY STRING
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