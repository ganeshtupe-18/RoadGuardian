package project.dao.mechanic;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;

import project.firebase.FirebaseConfig;
import project.model.ServiceRequest;
import project.ui.user.UserSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MechanicRequestDAO {

    // =====================================================
    // COLLECTION
    // =====================================================

    private static final String COLLECTION =
            "serviceRequests";

    // =====================================================
    // FIRESTORE
    // =====================================================

    private final Firestore firestore;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public MechanicRequestDAO() {

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
    // FIRESTORE CONSTRUCTOR
    // =====================================================

    public MechanicRequestDAO(
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
    //
    // Registration/Login मध्ये mechanic email हा
    // canonical mechanic ID म्हणून वापरला जातो.
    // =====================================================

    public String getCurrentMechanicId() {

        if (!UserSession.isLoggedIn()) {

            return null;
        }

        String email =
                normalizeId(
                        UserSession.getUserEmail()
                );

        if (email != null) {

            return email;
        }

        return normalizeId(
                UserSession.getUserId()
        );
    }

    // =====================================================
    // CURRENT MECHANIC NAME
    // =====================================================

    public String getCurrentMechanicName() {

        String name =
                clean(
                        UserSession.getUserName()
                );

        return name == null
                ? "Mechanic"
                : name;
    }

    // =====================================================
    // GET ALL MY REQUESTS
    // =====================================================

    public List<ServiceRequest> getMyRequests()
            throws Exception {

        return getRequestsByMechanicId(
                getRequiredMechanicId()
        );
    }

    // =====================================================
    // GET REQUESTS BY MECHANIC
    //
    // IMPORTANT:
    //
    // We scan serviceRequests and normalize IDs instead
    // of relying only on Firestore whereEqualTo().
    //
    // त्यामुळे:
    //
    // mechanic@gmail.com
    // Mechanic@gmail.com
    //
    // case mismatch मुळे request गायब होणार नाही.
    // =====================================================

    public List<ServiceRequest> getRequestsByMechanicId(
            String mechanicId
    ) throws Exception {

        List<ServiceRequest> result =
                new ArrayList<>();

        mechanicId =
                normalizeId(
                        mechanicId
                );

        if (mechanicId == null) {

            return result;
        }

        QuerySnapshot snapshot =
                firestore
                        .collection(COLLECTION)
                        .get()
                        .get();

        for (DocumentSnapshot document :
                snapshot.getDocuments()) {

            ServiceRequest request =
                    convertDocument(
                            document
                    );

            if (request == null) {

                continue;
            }

            if (!belongsToMechanic(
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
                Comparator.comparingLong(
                        this::getRequestTimestamp
                ).reversed()
        );

        return result;
    }

    // =====================================================
    // ASSIGNED REQUESTS
    //
    // Mechanic Requests page
    // =====================================================

    public List<ServiceRequest> getAssignedRequests()
            throws Exception {

        return getAssignedRequests(
                getRequiredMechanicId()
        );
    }

    public List<ServiceRequest> getAssignedRequests(
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

    public List<ServiceRequest> getAcceptedRequests()
            throws Exception {

        return getAcceptedRequests(
                getRequiredMechanicId()
        );
    }

    public List<ServiceRequest> getAcceptedRequests(
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

    public List<ServiceRequest> getInProgressRequests()
            throws Exception {

        return getInProgressRequests(
                getRequiredMechanicId()
        );
    }

    public List<ServiceRequest> getInProgressRequests(
            String mechanicId
    ) throws Exception {

        return getRequestsByStatus(
                mechanicId,
                "In Progress"
        );
    }

    // =====================================================
    // COMPLETED REQUESTS
    // =====================================================

    public List<ServiceRequest> getCompletedRequests()
            throws Exception {

        return getCompletedRequests(
                getRequiredMechanicId()
        );
    }

    public List<ServiceRequest> getCompletedRequests(
            String mechanicId
    ) throws Exception {

        return getRequestsByStatus(
                mechanicId,
                "Completed"
        );
    }

    // =====================================================
    // CANCELLED REQUESTS
    // =====================================================

    public List<ServiceRequest> getCancelledRequests()
            throws Exception {

        return getCancelledRequests(
                getRequiredMechanicId()
        );
    }

    public List<ServiceRequest> getCancelledRequests(
            String mechanicId
    ) throws Exception {

        return getRequestsByStatus(
                mechanicId,
                "Cancelled"
        );
    }

    // =====================================================
    // ACTIVE REQUESTS
    //
    // Accepted + In Progress
    //
    // Assigned is NOT ActiveJob yet.
    // =====================================================

    public List<ServiceRequest> getActiveRequests()
            throws Exception {

        return getActiveRequests(
                getRequiredMechanicId()
        );
    }

    public List<ServiceRequest> getActiveRequests(
            String mechanicId
    ) throws Exception {

        List<ServiceRequest> result =
                new ArrayList<>();

        for (ServiceRequest request :
                getRequestsByMechanicId(
                        mechanicId
                )) {

            if (
                    hasStatus(
                            request,
                            "Accepted"
                    )
                            ||
                    hasStatus(
                            request,
                            "In Progress"
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
    // HISTORY
    //
    // Completed + Cancelled
    // =====================================================

    public List<ServiceRequest> getHistoryRequests()
            throws Exception {

        List<ServiceRequest> result =
                new ArrayList<>();

        for (ServiceRequest request :
                getMyRequests()) {

            if (
                    hasStatus(
                            request,
                            "Completed"
                    )
                            ||
                    hasStatus(
                            request,
                            "Cancelled"
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
    // REQUESTS BY STATUS
    // =====================================================

    public List<ServiceRequest> getRequestsByStatus(
            String mechanicId,
            String status
    ) throws Exception {

        List<ServiceRequest> all =
                getRequestsByMechanicId(
                        mechanicId
                );

        List<ServiceRequest> result =
                new ArrayList<>();

        status =
                normalizeStatus(
                        status
                );

        if (status == null
                ||
                status.equalsIgnoreCase(
                        "All"
                )) {

            return all;
        }

        for (ServiceRequest request :
                all) {

            if (hasStatus(
                    request,
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
    // =====================================================

    public ServiceRequest getRequestById(
            String requestId
    ) throws Exception {

        requestId =
                clean(
                        requestId
                );

        if (requestId == null) {

            return null;
        }

        DocumentSnapshot document =
                firestore
                        .collection(COLLECTION)
                        .document(requestId)
                        .get()
                        .get();

        if (!document.exists()) {

            return null;
        }

        return convertDocument(
                document
        );
    }

    // =====================================================
    // GET MY REQUEST BY ID
    //
    // Ownership protected.
    // =====================================================

    public ServiceRequest getMyRequestById(
            String requestId
    ) throws Exception {

        ServiceRequest request =
                getRequestById(
                        requestId
                );

        if (request == null) {

            return null;
        }

        if (!belongsToMechanic(
                request,
                getRequiredMechanicId()
        )) {

            return null;
        }

        return request;
    }

    // =====================================================
    // ACCEPT
    //
    // Assigned -> Accepted
    // =====================================================

    public boolean acceptRequest(
            String requestId
    ) throws Exception {

        return acceptRequest(
                requestId,
                getRequiredMechanicId()
        );
    }

    public boolean acceptRequest(
            String requestId,
            String mechanicId
    ) throws Exception {

        requestId =
                clean(
                        requestId
                );

        mechanicId =
                normalizeId(
                        mechanicId
                );

        if (requestId == null
                ||
                mechanicId == null) {

            return false;
        }

        DocumentReference reference =
                firestore
                        .collection(COLLECTION)
                        .document(requestId);

        DocumentSnapshot snapshot =
                reference
                        .get()
                        .get();

        if (!snapshot.exists()) {

            return false;
        }

        ServiceRequest request =
                convertDocument(
                        snapshot
                );

        if (request == null) {

            return false;
        }

        // =================================================
        // OWNERSHIP
        // =================================================

        if (!belongsToMechanic(
                request,
                mechanicId
        )) {

            System.err.println(
                    "Accept blocked: Request "
                            + requestId
                            + " is not assigned to "
                            + mechanicId
            );

            return false;
        }

        // =================================================
        // ALREADY ACCEPTED
        // =================================================

        if (hasStatus(
                request,
                "Accepted"
        )) {

            return true;
        }

        // =================================================
        // ONLY ASSIGNED -> ACCEPTED
        // =================================================

        if (!hasStatus(
                request,
                "Assigned"
        )) {

            System.err.println(
                    "Accept blocked: Current status = "
                            + request.getStatus()
            );

            return false;
        }

        String now =
                currentTime();

        reference
                .update(
                        "status",
                        "Accepted",

                        "acceptedDate",
                        now,

                        "updatedAt",
                        now
                )
                .get();

        System.out.println(
                "Service request accepted: "
                        + requestId
                        + " | mechanicId="
                        + mechanicId
        );

        return true;
    }

    // =====================================================
    // REJECT
    //
    // Assigned -> Pending
    //
    // Request returns to Admin queue.
    // =====================================================

    public boolean rejectRequest(
            String requestId
    ) throws Exception {

        return rejectRequest(
                requestId,
                getRequiredMechanicId()
        );
    }

    public boolean rejectRequest(
            String requestId,
            String mechanicId
    ) throws Exception {

        requestId =
                clean(
                        requestId
                );

        mechanicId =
                normalizeId(
                        mechanicId
                );

        if (requestId == null
                ||
                mechanicId == null) {

            return false;
        }

        DocumentReference reference =
                firestore
                        .collection(COLLECTION)
                        .document(requestId);

        DocumentSnapshot snapshot =
                reference
                        .get()
                        .get();

        if (!snapshot.exists()) {

            return false;
        }

        ServiceRequest request =
                convertDocument(
                        snapshot
                );

        if (request == null) {

            return false;
        }

        if (!belongsToMechanic(
                request,
                mechanicId
        )) {

            return false;
        }

        if (!hasStatus(
                request,
                "Assigned"
        )) {

            return false;
        }

        String now =
                currentTime();

        reference
                .update(
                        "mechanicId",
                        "",

                        "mechanicName",
                        "",

                        "status",
                        "Pending",

                        "assignedDate",
                        "",

                        "acceptedDate",
                        "",

                        "startedDate",
                        "",

                        "updatedAt",
                        now
                )
                .get();

        System.out.println(
                "Service request rejected: "
                        + requestId
                        + " -> Pending"
        );

        return true;
    }

    // =====================================================
    // START REQUEST
    //
    // Accepted -> In Progress
    // =====================================================

    public boolean startRequest(
            String requestId
    ) throws Exception {

        return startRequest(
                requestId,
                getRequiredMechanicId()
        );
    }

    public boolean startRequest(
            String requestId,
            String mechanicId
    ) throws Exception {

        requestId =
                clean(
                        requestId
                );

        mechanicId =
                normalizeId(
                        mechanicId
                );

        if (requestId == null
                ||
                mechanicId == null) {

            return false;
        }

        DocumentReference reference =
                firestore
                        .collection(COLLECTION)
                        .document(requestId);

        DocumentSnapshot snapshot =
                reference
                        .get()
                        .get();

        if (!snapshot.exists()) {

            return false;
        }

        ServiceRequest request =
                convertDocument(
                        snapshot
                );

        if (request == null) {

            return false;
        }

        if (!belongsToMechanic(
                request,
                mechanicId
        )) {

            return false;
        }

        // =================================================
        // ALREADY STARTED
        // =================================================

        if (hasStatus(
                request,
                "In Progress"
        )) {

            return true;
        }

        // =================================================
        // ONLY ACCEPTED -> IN PROGRESS
        // =================================================

        if (!hasStatus(
                request,
                "Accepted"
        )) {

            return false;
        }

        String now =
                currentTime();

        reference
                .update(
                        "status",
                        "In Progress",

                        "startedDate",
                        now,

                        "updatedAt",
                        now
                )
                .get();

        System.out.println(
                "Service request started: "
                        + requestId
                        + " -> In Progress"
        );

        return true;
    }

    // =====================================================
    // COUNTS
    // =====================================================

    public int getAssignedRequestCount()
            throws Exception {

        return getAssignedRequests()
                .size();
    }

    public int getAcceptedRequestCount()
            throws Exception {

        return getAcceptedRequests()
                .size();
    }

    public int getInProgressRequestCount()
            throws Exception {

        return getInProgressRequests()
                .size();
    }

    public int getActiveRequestCount()
            throws Exception {

        return getActiveRequests()
                .size();
    }

    public int getCompletedRequestCount()
            throws Exception {

        return getCompletedRequests()
                .size();
    }

    public int getCancelledRequestCount()
            throws Exception {

        return getCancelledRequests()
                .size();
    }

    // =====================================================
    // OWNERSHIP CHECK
    // =====================================================

    private boolean belongsToMechanic(
            ServiceRequest request,
            String mechanicId
    ) {

        if (request == null) {

            return false;
        }

        return sameId(
                request.getMechanicId(),
                mechanicId
        );
    }

    // =====================================================
    // FIRESTORE -> MODEL
    // =====================================================

    private ServiceRequest convertDocument(
            DocumentSnapshot document
    ) {

        if (document == null
                ||
                !document.exists()) {

            return null;
        }

        ServiceRequest request =
                new ServiceRequest();

        // =================================================
        // REQUEST ID
        // =================================================

        request.setRequestId(
                firstNonBlank(
                        firstString(
                                document,
                                "requestId"
                        ),
                        document.getId()
                )
        );

        // =================================================
        // CUSTOMER
        // =================================================

        request.setCustomerId(
                normalizeId(
                        firstString(
                                document,
                                "customerId",
                                "userId",
                                "userEmail"
                        )
                )
        );

        request.setCustomerName(
                firstNonBlank(
                        firstString(
                                document,
                                "customerName",
                                "userName",
                                "name"
                        ),
                        "Customer"
                )
        );

        // =================================================
        // VEHICLE
        // =================================================

        request.setVehicleId(
                firstString(
                        document,
                        "vehicleId"
                )
        );

        request.setVehicleNumber(
                firstString(
                        document,
                        "vehicleNumber",
                        "registrationNumber",
                        "vehicleNo"
                )
        );

        // =================================================
        // MECHANIC
        // =================================================

        request.setMechanicId(
                normalizeId(
                        firstString(
                                document,
                                "mechanicId",
                                "assignedMechanicId",
                                "mechanicEmail",
                                "responderId"
                        )
                )
        );

        request.setMechanicName(
                firstString(
                        document,
                        "mechanicName",
                        "assignedMechanicName",
                        "responderName"
                )
        );

        // =================================================
        // SERVICE
        // =================================================

        request.setServiceType(
                firstString(
                        document,
                        "serviceType",
                        "service",
                        "serviceName",
                        "type"
                )
        );

        request.setDescription(
                firstString(
                        document,
                        "description",
                        "problem",
                        "issue"
                )
        );

        request.setLocation(
                firstString(
                        document,
                        "location",
                        "address"
                )
        );

        // =================================================
        // GPS
        // =================================================

        request.setLatitude(
                doubleValue(
                        document.get(
                                "latitude"
                        )
                )
        );

        request.setLongitude(
                doubleValue(
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
                        firstString(
                                document,
                                "status"
                        )
                )
        );

        // =================================================
        // LIFECYCLE DATES
        // =================================================

        request.setRequestDate(
                firstString(
                        document,
                        "requestDate",
                        "createdAt",
                        "date"
                )
        );

        request.setAssignedDate(
                firstString(
                        document,
                        "assignedDate"
                )
        );

        request.setAcceptedDate(
                firstString(
                        document,
                        "acceptedDate"
                )
        );

        request.setStartedDate(
                firstString(
                        document,
                        "startedDate"
                )
        );

        request.setCompletedDate(
                firstString(
                        document,
                        "completedDate"
                )
        );

        request.setCancelledDate(
                firstString(
                        document,
                        "cancelledDate"
                )
        );

        request.setUpdatedAt(
                firstString(
                        document,
                        "updatedAt"
                )
        );

        // =================================================
        // WORK DETAILS
        // =================================================

        request.setDiagnosis(
                firstString(
                        document,
                        "diagnosis"
                )
        );

        request.setRepairDetails(
                firstString(
                        document,
                        "repairDetails",
                        "repair"
                )
        );

        // =================================================
        // COST
        // =================================================

        request.setEstimatedCost(
                firstString(
                        document,
                        "estimatedCost"
                )
        );

        request.setPartsCost(
                firstString(
                        document,
                        "partsCost"
                )
        );

        request.setLabourCost(
                firstString(
                        document,
                        "labourCost",
                        "laborCost"
                )
        );

        request.setFinalCost(
                firstString(
                        document,
                        "finalCost",
                        "totalCost",
                        "cost",
                        "amount"
                )
        );

        // =================================================
        // EXTRA
        // =================================================

        request.setSource(
                firstString(
                        document,
                        "source"
                )
        );

        request.setPriority(
                firstString(
                        document,
                        "priority"
                )
        );

        return request;
    }

    // =====================================================
    // REQUIRED MECHANIC
    // =====================================================

    private String getRequiredMechanicId() {

        if (!UserSession.isLoggedIn()) {

            throw new IllegalStateException(
                    "No user is logged in."
            );
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

            throw new IllegalStateException(
                    "Current user is not a mechanic."
            );
        }

        String mechanicId =
                getCurrentMechanicId();

        if (mechanicId == null) {

            throw new IllegalStateException(
                    "Mechanic ID/email is unavailable."
            );
        }

        return mechanicId;
    }

    // =====================================================
    // STATUS CHECK
    // =====================================================

    private boolean hasStatus(
            ServiceRequest request,
            String status
    ) {

        if (request == null
                ||
                status == null) {

            return false;
        }

        String requestStatus =
                normalizeStatus(
                        request.getStatus()
                );

        String requiredStatus =
                normalizeStatus(
                        status
                );

        return requestStatus != null
                &&
                requiredStatus != null
                &&
                requestStatus.equalsIgnoreCase(
                        requiredStatus
                );
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

            return null;
        }

        if (status.equalsIgnoreCase(
                "Pending"
        )) {

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
                        "Active"
                )
        ) {

            return "In Progress";
        }

        if (
                status.equalsIgnoreCase(
                        "Completed"
                )
                        ||
                status.equalsIgnoreCase(
                        "Complete"
                )
                        ||
                status.equalsIgnoreCase(
                        "Done"
                )
        ) {

            return "Completed";
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
    // FIRST FIRESTORE STRING
    // =====================================================

    private String firstString(
            DocumentSnapshot document,
            String... fields
    ) {

        if (document == null
                ||
                fields == null) {

            return null;
        }

        for (String field :
                fields) {

            String value =
                    stringValue(
                            document.get(
                                    field
                            )
                    );

            if (value != null) {

                return value;
            }
        }

        return null;
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
    // DOUBLE
    // =====================================================

    private Double doubleValue(
            Object value
    ) {

        if (value == null) {

            return null;
        }

        if (value instanceof Number) {

            return ((Number) value)
                    .doubleValue();
        }

        try {

            return Double.parseDouble(
                    String.valueOf(
                            value
                    ).trim()
            );

        } catch (Exception e) {

            return null;
        }
    }

    // =====================================================
    // REQUEST TIMESTAMP
    // =====================================================

    private long getRequestTimestamp(
            ServiceRequest request
    ) {

        if (request == null) {

            return 0L;
        }

        long value =
                parseTime(
                        request.getUpdatedAt()
                );

        if (value > 0) {

            return value;
        }

        value =
                parseTime(
                        request.getRequestDate()
                );

        if (value > 0) {

            return value;
        }

        return parseTime(
                request.getAssignedDate()
        );
    }

    // =====================================================
    // PARSE TIMESTAMP
    // =====================================================

    private long parseTime(
            String value
    ) {

        value =
                clean(
                        value
                );

        if (value == null) {

            return 0L;
        }

        try {

            long number =
                    Long.parseLong(
                            value
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
    // CURRENT TIME
    // =====================================================

    private String currentTime() {

        return String.valueOf(
                System.currentTimeMillis()
        );
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