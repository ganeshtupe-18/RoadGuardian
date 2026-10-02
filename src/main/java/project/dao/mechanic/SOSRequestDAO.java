package project.dao.mechanic;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;

import project.firebase.FirebaseConfig;
import project.model.SOSRequest;
import project.ui.user.UserSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SOSRequestDAO {

    // =====================================================
    // COLLECTIONS
    // =====================================================

    private static final String COLLECTION =
            "SOSRequests";

    private static final String LEGACY_COLLECTION_1 =
            "sosRequests";

    private static final String LEGACY_COLLECTION_2 =
            "sos_requests";

    // =====================================================
    // FIRESTORE
    // =====================================================

    private final Firestore firestore;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public SOSRequestDAO() {

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
    // CURRENT MECHANIC ID
    // =====================================================

    public String getCurrentMechanicId() {

        if (!UserSession.isLoggedIn()) {

            return null;
        }

        String role =
                clean(
                        UserSession.getUserRole()
                );

        if (role != null
                &&
                !role.equalsIgnoreCase(
                        "Mechanic"
                )) {

            return null;
        }

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
    // MY ALL SOS REQUESTS
    // =====================================================

    public List<SOSRequest> getMySOSRequests()
            throws Exception {

        String mechanicId =
                getCurrentMechanicId();

        if (mechanicId == null) {

            return new ArrayList<>();
        }

        return getRequestsByMechanicId(
                mechanicId
        );
    }

    // =====================================================
    // REQUESTS BY MECHANIC
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

        // =================================================
        // MIGRATE OLD DATA FIRST
        // =================================================

        migrateLegacyCollectionsForMechanic(
                mechanicId
        );

        // =================================================
        // READ CANONICAL COLLECTION
        // =================================================

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

            if (request == null) {

                continue;
            }

            if (!isOwnedByMechanic(
                    request,
                    mechanicId
            )) {

                continue;
            }

            result.add(
                    request
            );
        }

        // =================================================
        // LATEST FIRST
        // =================================================

        result.sort(
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

    public List<SOSRequest> getAssignedRequests(
            String mechanicId
    ) throws Exception {

        return getRequestsByStatus(
                mechanicId,
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

    public List<SOSRequest> getAcceptedRequests(
            String mechanicId
    ) throws Exception {

        return getRequestsByStatus(
                mechanicId,
                "Accepted"
        );
    }

    // =====================================================
    // IN PROGRESS REQUESTS
    // =====================================================

    public List<SOSRequest> getInProgressRequests()
            throws Exception {

        return getRequestsByStatus(
                "In Progress"
        );
    }

    public List<SOSRequest> getInProgressRequests(
            String mechanicId
    ) throws Exception {

        return getRequestsByStatus(
                mechanicId,
                "In Progress"
        );
    }

    // =====================================================
    // RESOLVED REQUESTS
    // =====================================================

    public List<SOSRequest> getResolvedRequests()
            throws Exception {

        return getRequestsByStatus(
                "Resolved"
        );
    }

    // =====================================================
    // CANCELLED REQUESTS
    // =====================================================

    public List<SOSRequest> getCancelledRequests()
            throws Exception {

        return getRequestsByStatus(
                "Cancelled"
        );
    }

    // =====================================================
    // ACTIVE SOS
    //
    // Assigned
    // Accepted
    // In Progress
    //
    // Pending request is NOT mechanic-owned after reject.
    // =====================================================

    public List<SOSRequest> getActiveRequests()
            throws Exception {

        List<SOSRequest> result =
                new ArrayList<>();

        for (SOSRequest request :
                getMySOSRequests()) {

            if (request == null) {

                continue;
            }

            if (
                    request.isAssigned()
                            ||
                    request.isAccepted()
                            ||
                    request.isInProgress()
            ) {

                result.add(
                        request
                );
            }
        }

        return result;
    }

    // =====================================================
    // HISTORY
    // =====================================================

    public List<SOSRequest> getHistoryRequests()
            throws Exception {

        List<SOSRequest> result =
                new ArrayList<>();

        for (SOSRequest request :
                getMySOSRequests()) {

            if (request == null) {

                continue;
            }

            if (
                    request.isResolved()
                            ||
                    request.isCancelled()
            ) {

                result.add(
                        request
                );
            }
        }

        return result;
    }

    // =====================================================
    // STATUS FILTER - CURRENT MECHANIC
    // =====================================================

    public List<SOSRequest> getRequestsByStatus(
            String status
    ) throws Exception {

        String mechanicId =
                getCurrentMechanicId();

        if (mechanicId == null) {

            return new ArrayList<>();
        }

        return getRequestsByStatus(
                mechanicId,
                status
        );
    }

    // =====================================================
    // STATUS FILTER
    // =====================================================

    public List<SOSRequest> getRequestsByStatus(
            String mechanicId,
            String status
    ) throws Exception {

        mechanicId =
                normalizeId(
                        mechanicId
                );

        status =
                normalizeStatus(
                        status
                );

        List<SOSRequest> result =
                new ArrayList<>();

        if (mechanicId == null
                ||
                status == null) {

            return result;
        }

        for (SOSRequest request :
                getRequestsByMechanicId(
                        mechanicId
                )) {

            if (request.getStatus() != null
                    &&
                    request
                            .getStatus()
                            .equalsIgnoreCase(
                                    status
                            )) {

                result.add(
                        request
                );
            }
        }

        return result;
    }

    // =====================================================
    // GET REQUEST BY ID
    //
    // Mechanic can access only his/her assigned SOS.
    // =====================================================

    public SOSRequest getRequestById(
            String sosId
    ) throws Exception {

        String mechanicId =
                getCurrentMechanicId();

        if (mechanicId == null) {

            return null;
        }

        return getRequestById(
                sosId,
                mechanicId
        );
    }

    public SOSRequest getRequestById(
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

            return null;
        }

        DocumentReference reference =
                findCanonicalReference(
                        sosId
                );

        if (reference == null) {

            return null;
        }

        DocumentSnapshot document =
                reference
                        .get()
                        .get();

        SOSRequest request =
                convertDocument(
                        document
                );

        if (request == null) {

            return null;
        }

        if (!isOwnedByMechanic(
                request,
                mechanicId
        )) {

            return null;
        }

        return request;
    }

    // =====================================================
    // ACCEPT SOS
    //
    // Assigned -> Accepted
    // =====================================================

    public boolean acceptRequest(
            String sosId
    ) throws Exception {

        return acceptRequest(
                sosId,
                getCurrentMechanicId()
        );
    }

    public boolean acceptRequest(
            String sosId,
            String mechanicId
    ) throws Exception {

        mechanicId =
                normalizeId(
                        mechanicId
                );

        SOSRequest request =
                getRequestById(
                        sosId,
                        mechanicId
                );

        if (request == null) {

            return false;
        }

        if (!request.isAssigned()) {

            return false;
        }

        DocumentReference reference =
                findCanonicalReference(
                        sosId
                );

        if (reference == null) {

            return false;
        }

        DocumentSnapshot document =
                reference
                        .get()
                        .get();

        long now =
                System.currentTimeMillis();

        Map<String, Object> updates =
                new HashMap<>();

        updates.put(
                "status",
                "Accepted"
        );

        updates.put(
                "acceptedDate",
                String.valueOf(
                        now
                )
        );

        updates.put(
                "updatedAt",
                now
        );

        updates.put(
                "responderStatus",
                "Accepted"
        );

        updates.put(
                "timeline",
                updateTimeline(
                        document,
                        "Accepted",
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
    // REJECT SOS
    //
    // Assigned -> Pending
    //
    // Mechanic assignment is cleared so Admin can assign
    // another mechanic.
    // =====================================================

    public boolean rejectRequest(
            String sosId
    ) throws Exception {

        return rejectRequest(
                sosId,
                getCurrentMechanicId()
        );
    }

    public boolean rejectRequest(
            String sosId,
            String mechanicId
    ) throws Exception {

        mechanicId =
                normalizeId(
                        mechanicId
                );

        SOSRequest request =
                getRequestById(
                        sosId,
                        mechanicId
                );

        if (request == null
                ||
                !request.isAssigned()) {

            return false;
        }

        DocumentReference reference =
                findCanonicalReference(
                        sosId
                );

        if (reference == null) {

            return false;
        }

        DocumentSnapshot document =
                reference
                        .get()
                        .get();

        long now =
                System.currentTimeMillis();

        Map<String, Object> updates =
                new HashMap<>();

        // =================================================
        // RETURN TO ADMIN QUEUE
        // =================================================

        updates.put(
                "status",
                "Pending"
        );

        // =================================================
        // CLEAR MECHANIC
        // =================================================

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

        // =================================================
        // CLEAR ASSIGNMENT LIFECYCLE
        // =================================================

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

        updates.put(
                "updatedAt",
                now
        );

        updates.put(
                "timeline",
                resetTimelineAfterReject(
                        document
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
    // START SOS RESPONSE
    //
    // Accepted -> In Progress
    // =====================================================

    public boolean startRequest(
            String sosId
    ) throws Exception {

        return startRequest(
                sosId,
                getCurrentMechanicId()
        );
    }

    public boolean startRequest(
            String sosId,
            String mechanicId
    ) throws Exception {

        mechanicId =
                normalizeId(
                        mechanicId
                );

        SOSRequest request =
                getRequestById(
                        sosId,
                        mechanicId
                );

        if (request == null
                ||
                !request.isAccepted()) {

            return false;
        }

        DocumentReference reference =
                findCanonicalReference(
                        sosId
                );

        if (reference == null) {

            return false;
        }

        DocumentSnapshot document =
                reference
                        .get()
                        .get();

        long now =
                System.currentTimeMillis();

        Map<String, Object> updates =
                new HashMap<>();

        updates.put(
                "status",
                "In Progress"
        );

        updates.put(
                "startedDate",
                String.valueOf(
                        now
                )
        );

        updates.put(
                "updatedAt",
                now
        );

        updates.put(
                "responderStatus",
                "In Progress"
        );

        updates.put(
                "timeline",
                updateTimeline(
                        document,
                        "In Progress",
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
    // RESOLVE SOS
    //
    // In Progress -> Resolved
    // =====================================================

    public boolean resolveRequest(
            String sosId
    ) throws Exception {

        return resolveRequest(
                sosId,
                getCurrentMechanicId()
        );
    }

    public boolean resolveRequest(
            String sosId,
            String mechanicId
    ) throws Exception {

        mechanicId =
                normalizeId(
                        mechanicId
                );

        SOSRequest request =
                getRequestById(
                        sosId,
                        mechanicId
                );

        if (request == null
                ||
                !request.isInProgress()) {

            return false;
        }

        DocumentReference reference =
                findCanonicalReference(
                        sosId
                );

        if (reference == null) {

            return false;
        }

        DocumentSnapshot document =
                reference
                        .get()
                        .get();

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
                String.valueOf(
                        now
                )
        );

        updates.put(
                "updatedAt",
                now
        );

        updates.put(
                "responderStatus",
                "Completed"
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
    // START NAVIGATION
    //
    // Status is not changed here.
    // =====================================================

    public boolean startNavigation(
            String sosId
    ) throws Exception {

        String mechanicId =
                getCurrentMechanicId();

        SOSRequest request =
                getRequestById(
                        sosId,
                        mechanicId
                );

        if (request == null) {

            return false;
        }

        if (
                !request.isAccepted()
                        &&
                !request.isInProgress()
        ) {

            return false;
        }

        DocumentReference reference =
                findCanonicalReference(
                        sosId
                );

        if (reference == null) {

            return false;
        }

        long now =
                System.currentTimeMillis();

        Map<String, Object> updates =
                new HashMap<>();

        updates.put(
                "navigationStatus",
                "Started"
        );

        updates.put(
                "navigationStartedDate",
                String.valueOf(
                        now
                )
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

        return true;
    }

    // =====================================================
    // MARK ARRIVED
    // =====================================================

    public boolean markArrived(
            String sosId
    ) throws Exception {

        String mechanicId =
                getCurrentMechanicId();

        SOSRequest request =
                getRequestById(
                        sosId,
                        mechanicId
                );

        if (request == null) {

            return false;
        }

        if (
                !request.isAccepted()
                        &&
                !request.isInProgress()
        ) {

            return false;
        }

        DocumentReference reference =
                findCanonicalReference(
                        sosId
                );

        if (reference == null) {

            return false;
        }

        long now =
                System.currentTimeMillis();

        Map<String, Object> updates =
                new HashMap<>();

        updates.put(
                "arrivalStatus",
                "Arrived"
        );

        updates.put(
                "arrivedDate",
                String.valueOf(
                        now
                )
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

        return true;
    }

    // =====================================================
    // COUNTS
    // =====================================================

    public int getAssignedCount()
            throws Exception {

        return getAssignedRequests()
                .size();
    }

    public int getAcceptedCount()
            throws Exception {

        return getAcceptedRequests()
                .size();
    }

    public int getInProgressCount()
            throws Exception {

        return getInProgressRequests()
                .size();
    }

    public int getActiveCount()
            throws Exception {

        return getActiveRequests()
                .size();
    }

    public int getResolvedCount()
            throws Exception {

        return getResolvedRequests()
                .size();
    }

    // =====================================================
    // SEARCH MY SOS
    // =====================================================

    public List<SOSRequest> searchRequests(
            String searchText
    ) throws Exception {

        List<SOSRequest> requests =
                getMySOSRequests();

        String search =
                clean(
                        searchText
                );

        if (search == null) {

            return requests;
        }

        search =
                search.toLowerCase();

        List<SOSRequest> result =
                new ArrayList<>();

        for (SOSRequest request :
                requests) {

            if (
                    contains(
                            request.getSosId(),
                            search
                    )
                            ||
                    contains(
                            request.getCustomerName(),
                            search
                    )
                            ||
                    contains(
                            request.getCustomerId(),
                            search
                    )
                            ||
                    contains(
                            request.getVehicleNumber(),
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
    // OWNERSHIP CHECK
    // =====================================================

    private boolean isOwnedByMechanic(
            SOSRequest request,
            String mechanicId
    ) {

        if (request == null) {

            return false;
        }

        String assignedMechanic =
                normalizeId(
                        firstNonBlank(
                                request.getMechanicId(),
                                request.getResponderId()
                        )
                );

        return sameId(
                assignedMechanic,
                mechanicId
        );
    }

    // =====================================================
    // FIND CANONICAL REFERENCE
    // =====================================================

    private DocumentReference findCanonicalReference(
            String sosId
    ) throws Exception {

        sosId =
                clean(
                        sosId
                );

        if (sosId == null) {

            return null;
        }

        DocumentReference direct =
                firestore
                        .collection(COLLECTION)
                        .document(sosId);

        DocumentSnapshot directDocument =
                direct
                        .get()
                        .get();

        if (directDocument.exists()) {

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
        // OLD CUSTOMER ALIASES
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
                        responderStatusFromStatus(
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
    // TIMELINE UPDATE
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
                    createBaseTimeline(
                            getDocumentTimestamp(
                                    document
                            )
                    );
        }

        status =
                normalizeStatus(
                        status
                );

        if (
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

            markTimelineCompleted(
                    timeline,
                    "Mechanic assigned",
                    now
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

            markTimelineCompleted(
                    timeline,
                    "Responder en route",
                    now
            );
        }

        if (status.equalsIgnoreCase(
                "Resolved"
        )) {

            markTimelineCompleted(
                    timeline,
                    "Assistance completed",
                    now
            );
        }

        return timeline;
    }

    // =====================================================
    // RESET TIMELINE AFTER REJECT
    // =====================================================

    private List<Map<String, Object>> resetTimelineAfterReject(
            DocumentSnapshot document
    ) {

        List<Map<String, Object>> timeline =
                readTimeline(
                        document.get(
                                "timeline"
                        )
                );

        if (timeline.isEmpty()) {

            timeline =
                    createBaseTimeline(
                            getDocumentTimestamp(
                                    document
                            )
                    );
        }

        resetTimelineItem(
                timeline,
                "Mechanic assigned"
        );

        resetTimelineItem(
                timeline,
                "Responder en route"
        );

        resetTimelineItem(
                timeline,
                "Assistance completed"
        );

        return timeline;
    }

    // =====================================================
    // BASE TIMELINE
    // =====================================================

    private List<Map<String, Object>> createBaseTimeline(
            long requestTime
    ) {

        List<Map<String, Object>> timeline =
                new ArrayList<>();

        timeline.add(
                createTimelineItem(
                        "SOS request received",
                        "Completed",
                        requestTime
                )
        );

        timeline.add(
                createTimelineItem(
                        "Emergency contacts notified",
                        "Completed",
                        requestTime
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
    // MARK TIMELINE COMPLETE
    // =====================================================

    private void markTimelineCompleted(
            List<Map<String, Object>> timeline,
            String title,
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

            if (currentTitle == null
                    ||
                    !currentTitle.equalsIgnoreCase(
                            title
                    )) {

                continue;
            }

            item.put(
                    "status",
                    "Completed"
            );

            item.put(
                    "timestamp",
                    timestamp
            );

            return;
        }

        timeline.add(
                createTimelineItem(
                        title,
                        "Completed",
                        timestamp
                )
        );
    }

    // =====================================================
    // RESET TIMELINE ITEM
    // =====================================================

    private void resetTimelineItem(
            List<Map<String, Object>> timeline,
            String title
    ) {

        for (Map<String, Object> item :
                timeline) {

            String currentTitle =
                    stringValue(
                            item.get(
                                    "title"
                            )
                    );

            if (currentTitle == null
                    ||
                    !currentTitle.equalsIgnoreCase(
                            title
                    )) {

                continue;
            }

            item.put(
                    "status",
                    "Waiting"
            );

            item.remove(
                    "timestamp"
            );

            return;
        }
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

        if (timestamp != null
                &&
                timestamp > 0) {

            item.put(
                    "timestamp",
                    timestamp
            );
        }

        return item;
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
    // LEGACY MIGRATION
    // =====================================================

    private void migrateLegacyCollectionsForMechanic(
            String mechanicId
    ) {

        migrateLegacyCollection(
                LEGACY_COLLECTION_1,
                mechanicId
        );

        migrateLegacyCollection(
                LEGACY_COLLECTION_2,
                mechanicId
        );
    }

    // =====================================================
    // MIGRATE LEGACY COLLECTION
    // =====================================================

    private void migrateLegacyCollection(
            String collectionName,
            String mechanicId
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

                if (!sameId(
                        assignedMechanic,
                        mechanicId
                )) {

                    continue;
                }

                migrateLegacyDocument(
                        document
                );
            }

        } catch (Exception e) {

            System.err.println(
                    "Legacy mechanic SOS migration skipped: "
                            + e.getMessage()
            );
        }
    }

    // =====================================================
    // MIGRATE ONE DOCUMENT
    // =====================================================

    private void migrateLegacyDocument(
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

            DocumentReference canonical =
                    firestore
                            .collection(COLLECTION)
                            .document(sosId);

            DocumentSnapshot existing =
                    canonical
                            .get()
                            .get();

            if (existing.exists()) {

                return;
            }

            Map<String, Object> data =
                    requestToMap(
                            request
                    );

            canonical
                    .set(
                            data,
                            SetOptions.merge()
                    )
                    .get();

        } catch (Exception e) {

            System.err.println(
                    "Unable to migrate mechanic SOS: "
                            + e.getMessage()
            );
        }
    }

    // =====================================================
    // MODEL -> MAP
    //
    // Mainly used for old SOS migration.
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
        // COMPATIBILITY
        // =================================================

        put(
                data,
                "userId",
                firstNonBlank(
                        request.getUserId(),
                        request.getCustomerId()
                )
        );

        put(
                data,
                "userName",
                firstNonBlank(
                        request.getUserName(),
                        request.getCustomerName()
                )
        );

        put(
                data,
                "userEmail",
                firstNonBlank(
                        request.getUserEmail(),
                        request.getCustomerId()
                )
        );

        put(
                data,
                "responderId",
                firstNonBlank(
                        request.getResponderId(),
                        request.getMechanicId()
                )
        );

        put(
                data,
                "responderName",
                firstNonBlank(
                        request.getResponderName(),
                        request.getMechanicName()
                )
        );

        put(
                data,
                "responderStatus",
                firstNonBlank(
                        request.getResponderStatus(),
                        responderStatusFromStatus(
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
    // RESPONDER STATUS
    // =====================================================

    private String responderStatusFromStatus(
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
    // TIMESTAMP
    // =====================================================

    private long getRequestTimestamp(
            SOSRequest request
    ) {

        if (request == null) {

            return 0L;
        }

        long time =
                toMillis(
                        request.getUpdatedAt()
                );

        if (time > 0) {

            return time;
        }

        time =
                toMillis(
                        request.getCreatedAt()
                );

        if (time > 0) {

            return time;
        }

        return toMillis(
                request.getRequestDate()
        );
    }

    private long getDocumentTimestamp(
            DocumentSnapshot document
    ) {

        if (document == null) {

            return 0L;
        }

        long time =
                toMillis(
                        document.get(
                                "updatedAt"
                        )
                );

        if (time > 0) {

            return time;
        }

        time =
                toMillis(
                        document.get(
                                "createdAt"
                        )
                );

        if (time > 0) {

            return time;
        }

        return toMillis(
                document.get(
                        "requestDate"
                )
        );
    }

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
                search != null
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