package project.dao.admin;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;

import project.model.ServiceRequest;

import java.util.ArrayList;
import java.util.List;

public class ServiceRequestDAO {

    // =====================================================
    // MAIN SHARED COLLECTION
    // =====================================================

    private static final String COLLECTION =
            "serviceRequests";

    private final Firestore firestore;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public ServiceRequestDAO(
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
    // GET ALL REQUESTS
    // ADMIN
    // =====================================================

    public List<ServiceRequest> getAllRequests()
            throws Exception {

        List<ServiceRequest> requests =
                new ArrayList<>();

        ApiFuture<QuerySnapshot> future =
                firestore
                        .collection(COLLECTION)
                        .get();

        QuerySnapshot snapshot =
                future.get();

        for (DocumentSnapshot document :
                snapshot.getDocuments()) {

            ServiceRequest request =
                    convertDocument(
                            document
                    );

            if (request != null) {

                requests.add(
                        request
                );
            }
        }

        return requests;
    }

    // =====================================================
    // GET REQUEST BY ID
    // ADMIN / CUSTOMER / MECHANIC
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
    // GET REQUESTS BY CUSTOMER
    // =====================================================

    public List<ServiceRequest> getRequestsByCustomerId(
            String customerId
    ) throws Exception {

        List<ServiceRequest> requests =
                new ArrayList<>();

        customerId =
                normalizeId(
                        customerId
                );

        if (customerId == null) {

            return requests;
        }

        QuerySnapshot snapshot =
                firestore
                        .collection(COLLECTION)
                        .whereEqualTo(
                                "customerId",
                                customerId
                        )
                        .get()
                        .get();

        for (DocumentSnapshot document :
                snapshot.getDocuments()) {

            ServiceRequest request =
                    convertDocument(
                            document
                    );

            if (request != null) {

                requests.add(
                        request
                );
            }
        }

        return requests;
    }

    // =====================================================
    // GET REQUESTS BY MECHANIC
    // =====================================================

    public List<ServiceRequest> getRequestsByMechanicId(
            String mechanicId
    ) throws Exception {

        List<ServiceRequest> requests =
                new ArrayList<>();

        mechanicId =
                normalizeId(
                        mechanicId
                );

        if (mechanicId == null) {

            return requests;
        }

        QuerySnapshot snapshot =
                firestore
                        .collection(COLLECTION)
                        .whereEqualTo(
                                "mechanicId",
                                mechanicId
                        )
                        .get()
                        .get();

        for (DocumentSnapshot document :
                snapshot.getDocuments()) {

            ServiceRequest request =
                    convertDocument(
                            document
                    );

            if (request != null) {

                requests.add(
                        request
                );
            }
        }

        return requests;
    }

    // =====================================================
    // ADD REQUEST
    // CUSTOMER / ADMIN
    // =====================================================

    public boolean addRequest(
            ServiceRequest request
    ) throws Exception {

        if (request == null) {

            return false;
        }

        // -------------------------------------------------
        // CUSTOMER ID
        // -------------------------------------------------

        String customerId =
                normalizeId(
                        request.getCustomerId()
                );

        if (customerId == null) {

            throw new IllegalArgumentException(
                    "Customer ID is required."
            );
        }

        request.setCustomerId(
                customerId
        );

        // -------------------------------------------------
        // DEFAULT STATUS
        // -------------------------------------------------

        if (clean(
                request.getStatus()
        ) == null) {

            request.setStatus(
                    "Pending"
            );

        } else {

            request.setStatus(
                    normalizeStatus(
                            request.getStatus()
                    )
            );
        }

        // -------------------------------------------------
        // REQUEST DATE
        // -------------------------------------------------

        if (clean(
                request.getRequestDate()
        ) == null) {

            request.setRequestDate(
                    currentTime()
            );
        }

        request.setUpdatedAt(
                currentTime()
        );

        // -------------------------------------------------
        // NORMALIZE MECHANIC
        // -------------------------------------------------

        if (clean(
                request.getMechanicId()
        ) != null) {

            request.setMechanicId(
                    normalizeId(
                            request.getMechanicId()
                    )
            );
        }

        // -------------------------------------------------
        // CREATE DOCUMENT
        // -------------------------------------------------

        DocumentReference document =
                firestore
                        .collection(COLLECTION)
                        .document();

        String requestId =
                document.getId();

        request.setRequestId(
                requestId
        );

        document
                .set(
                        request
                )
                .get();

        return true;
    }

    // =====================================================
    // UPDATE REQUEST
    // =====================================================

    public boolean updateRequest(
            ServiceRequest request
    ) throws Exception {

        if (request == null) {

            return false;
        }

        String requestId =
                clean(
                        request.getRequestId()
                );

        if (requestId == null) {

            return false;
        }

        DocumentReference document =
                firestore
                        .collection(COLLECTION)
                        .document(requestId);

        DocumentSnapshot existing =
                document
                        .get()
                        .get();

        if (!existing.exists()) {

            return false;
        }

        // -------------------------------------------------
        // PRESERVE CUSTOMER ID
        // -------------------------------------------------

        if (clean(
                request.getCustomerId()
        ) == null) {

            request.setCustomerId(
                    stringValue(
                            existing.get(
                                    "customerId"
                            )
                    )
            );

        } else {

            request.setCustomerId(
                    normalizeId(
                            request.getCustomerId()
                    )
            );
        }

        // -------------------------------------------------
        // NORMALIZE MECHANIC
        // -------------------------------------------------

        if (clean(
                request.getMechanicId()
        ) != null) {

            request.setMechanicId(
                    normalizeId(
                            request.getMechanicId()
                    )
            );
        }

        request.setUpdatedAt(
                currentTime()
        );

        document
                .set(
                        request,
                        SetOptions.merge()
                )
                .get();

        return true;
    }

    // =====================================================
    // GENERIC STATUS UPDATE
    // =====================================================

    public boolean updateStatus(
            String requestId,
            String status
    ) throws Exception {

        requestId =
                clean(
                        requestId
                );

        status =
                normalizeStatus(
                        status
                );

        if (requestId == null
                ||
                status == null) {

            return false;
        }

        DocumentReference document =
                firestore
                        .collection(COLLECTION)
                        .document(requestId);

        DocumentSnapshot existing =
                document
                        .get()
                        .get();

        if (!existing.exists()) {

            return false;
        }

        String now =
                currentTime();

        // =================================================
        // STATUS + CORRESPONDING DATE
        // =================================================

        switch (status) {

            case "Assigned":

                document
                        .update(
                                "status",
                                "Assigned",
                                "assignedDate",
                                now,
                                "updatedAt",
                                now
                        )
                        .get();

                break;

            case "Accepted":

                document
                        .update(
                                "status",
                                "Accepted",
                                "acceptedDate",
                                now,
                                "updatedAt",
                                now
                        )
                        .get();

                break;

            case "In Progress":

                document
                        .update(
                                "status",
                                "In Progress",
                                "startedDate",
                                now,
                                "updatedAt",
                                now
                        )
                        .get();

                break;

            case "Completed":

                document
                        .update(
                                "status",
                                "Completed",
                                "completedDate",
                                now,
                                "updatedAt",
                                now
                        )
                        .get();

                break;

            case "Cancelled":

                document
                        .update(
                                "status",
                                "Cancelled",
                                "cancelledDate",
                                now,
                                "updatedAt",
                                now
                        )
                        .get();

                releaseTowTruckIfNeeded(
                        existing,
                        requestId
                );

                break;

            default:

                document
                        .update(
                                "status",
                                status,
                                "updatedAt",
                                now
                        )
                        .get();

                break;
        }

        return true;
    }

    // =====================================================
    // ADMIN -> ASSIGN MECHANIC
    // =====================================================

    public boolean assignMechanic(
            String requestId,
            String mechanicId,
            String mechanicName
    ) throws Exception {

        requestId =
                clean(
                        requestId
                );

        mechanicId =
                normalizeId(
                        mechanicId
                );

        mechanicName =
                clean(
                        mechanicName
                );

        if (requestId == null
                ||
                mechanicId == null) {

            return false;
        }

        DocumentReference document =
                firestore
                        .collection(COLLECTION)
                        .document(requestId);

        DocumentSnapshot existing =
                document
                        .get()
                        .get();

        if (!existing.exists()) {

            return false;
        }

        String currentStatus =
                stringValue(
                        existing.get(
                                "status"
                        )
                );

        /*
         * Completed / Cancelled request पुन्हा assign
         * करू नये.
         */
        if (equalsStatus(
                currentStatus,
                "Completed"
        )
                ||
                equalsStatus(
                        currentStatus,
                        "Cancelled"
                )) {

            return false;
        }

        String now =
                currentTime();

        /*
         * IMPORTANT:
         *
         * Admin फक्त mechanic assign करतो.
         *
         * Mechanic अजून accept केलेला नाही.
         *
         * म्हणून status = Assigned
         */
        document
                .update(
                        "mechanicId",
                        mechanicId,

                        "mechanicName",
                        mechanicName == null
                                ? ""
                                : mechanicName,

                        "status",
                        "Assigned",

                        "assignedDate",
                        now,

                        "updatedAt",
                        now
                )
                .get();

        return true;
    }

    // =====================================================
    // MECHANIC -> ACCEPT REQUEST
    // =====================================================

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

        ServiceRequest request =
                getRequestById(
                        requestId
                );

        if (request == null) {

            return false;
        }

        // -------------------------------------------------
        // OWNERSHIP CHECK
        // -------------------------------------------------

        if (!sameId(
                request.getMechanicId(),
                mechanicId
        )) {

            return false;
        }

        // -------------------------------------------------
        // MUST BE ASSIGNED
        // -------------------------------------------------

        if (!equalsStatus(
                request.getStatus(),
                "Assigned"
        )) {

            return false;
        }

        return updateStatus(
                requestId,
                "Accepted"
        );
    }

    // =====================================================
    // MECHANIC -> START JOB
    // =====================================================

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

        ServiceRequest request =
                getRequestById(
                        requestId
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

        if (!equalsStatus(
                request.getStatus(),
                "Accepted"
        )) {

            return false;
        }

        return updateStatus(
                requestId,
                "In Progress"
        );
    }

    // =====================================================
    // SAVE MECHANIC WORK DETAILS
    // =====================================================

    public boolean updateRepairDetails(
            String requestId,
            String mechanicId,
            String diagnosis,
            String repairDetails,
            String estimatedCost,
            String partsCost,
            String labourCost,
            String finalCost
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

        ServiceRequest request =
                getRequestById(
                        requestId
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

        DocumentReference document =
                firestore
                        .collection(COLLECTION)
                        .document(requestId);

        document
                .update(
                        "diagnosis",
                        emptyIfNull(
                                diagnosis
                        ),

                        "repairDetails",
                        emptyIfNull(
                                repairDetails
                        ),

                        "estimatedCost",
                        emptyIfNull(
                                estimatedCost
                        ),

                        "partsCost",
                        emptyIfNull(
                                partsCost
                        ),

                        "labourCost",
                        emptyIfNull(
                                labourCost
                        ),

                        "finalCost",
                        emptyIfNull(
                                finalCost
                        ),

                        "updatedAt",
                        currentTime()
                )
                .get();

        return true;
    }

    // =====================================================
    // MECHANIC -> COMPLETE JOB
    // =====================================================

    public boolean completeRequest(
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

        ServiceRequest request =
                getRequestById(
                        requestId
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

        /*
         * Normally In Progress असायलाच हवा.
         */
        if (!equalsStatus(
                request.getStatus(),
                "In Progress"
        )) {

            return false;
        }

        return updateStatus(
                requestId,
                "Completed"
        );
    }

    // =====================================================
    // COMPLETE JOB WITH DETAILS
    // =====================================================

    public boolean completeRequest(
            String requestId,
            String mechanicId,
            String diagnosis,
            String repairDetails,
            String estimatedCost,
            String partsCost,
            String labourCost,
            String finalCost
    ) throws Exception {

        boolean detailsSaved =
                updateRepairDetails(
                        requestId,
                        mechanicId,
                        diagnosis,
                        repairDetails,
                        estimatedCost,
                        partsCost,
                        labourCost,
                        finalCost
                );

        if (!detailsSaved) {

            return false;
        }

        return completeRequest(
                requestId,
                mechanicId
        );
    }

    // =====================================================
    // CANCEL REQUEST
    // CUSTOMER / ADMIN
    // =====================================================

    public boolean cancelRequest(
            String requestId
    ) throws Exception {

        ServiceRequest request =
                getRequestById(
                        requestId
                );

        if (request == null) {

            return false;
        }

        if (request.isCompleted()) {

            return false;
        }

        if (request.isCancelled()) {

            return true;
        }

        return updateStatus(
                requestId,
                "Cancelled"
        );
    }

    // =====================================================
    // DELETE REQUEST
    // ADMIN
    // =====================================================

    public boolean deleteRequest(
            String requestId
    ) throws Exception {

        requestId =
                clean(
                        requestId
                );

        if (requestId == null) {

            return false;
        }

        DocumentReference document =
                firestore
                        .collection(COLLECTION)
                        .document(requestId);

        DocumentSnapshot existing =
                document
                        .get()
                        .get();

        if (!existing.exists()) {

            return false;
        }

        releaseTowTruckIfNeeded(
                existing,
                requestId
        );

        document
                .delete()
                .get();

        return true;
    }

    // =====================================================
    // SEARCH REQUESTS
    // ADMIN
    // =====================================================

    public List<ServiceRequest> searchRequests(
            String searchText
    ) throws Exception {

        List<ServiceRequest> allRequests =
                getAllRequests();

        if (searchText == null
                ||
                searchText.isBlank()) {

            return allRequests;
        }

        String search =
                searchText
                        .trim()
                        .toLowerCase();

        List<ServiceRequest> result =
                new ArrayList<>();

        for (ServiceRequest request :
                allRequests) {

            if (
                    contains(
                            request.getRequestId(),
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
                            request.getVehicleId(),
                            search
                    )
                            ||
                    contains(
                            request.getVehicleNumber(),
                            search
                    )
                            ||
                    contains(
                            request.getMechanicId(),
                            search
                    )
                            ||
                    contains(
                            request.getMechanicName(),
                            search
                    )
                            ||
                    contains(
                            request.getServiceType(),
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
    // FILTER BY STATUS
    // =====================================================

    public List<ServiceRequest> getRequestsByStatus(
            String status
    ) throws Exception {

        List<ServiceRequest> allRequests =
                getAllRequests();

        if (status == null
                ||
                status.isBlank()
                ||
                status.equalsIgnoreCase(
                        "All"
                )) {

            return allRequests;
        }

        String normalizedStatus =
                normalizeStatus(
                        status
                );

        List<ServiceRequest> result =
                new ArrayList<>();

        for (ServiceRequest request :
                allRequests) {

            if (equalsStatus(
                    request.getStatus(),
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
    // CUSTOMER ACTIVE REQUESTS
    // =====================================================

    public List<ServiceRequest>
    getActiveRequestsByCustomerId(
            String customerId
    ) throws Exception {

        List<ServiceRequest> allRequests =
                getRequestsByCustomerId(
                        customerId
                );

        List<ServiceRequest> result =
                new ArrayList<>();

        for (ServiceRequest request :
                allRequests) {

            if (request.isActive()) {

                result.add(
                        request
                );
            }
        }

        return result;
    }

    // =====================================================
    // CUSTOMER SERVICE HISTORY
    // =====================================================

    public List<ServiceRequest>
    getHistoryByCustomerId(
            String customerId
    ) throws Exception {

        List<ServiceRequest> allRequests =
                getRequestsByCustomerId(
                        customerId
                );

        List<ServiceRequest> result =
                new ArrayList<>();

        for (ServiceRequest request :
                allRequests) {

            if (request.isFinished()) {

                result.add(
                        request
                );
            }
        }

        return result;
    }

    // =====================================================
    // MECHANIC ASSIGNED REQUESTS
    // =====================================================

    public List<ServiceRequest>
    getAssignedRequestsByMechanicId(
            String mechanicId
    ) throws Exception {

        return filterMechanicRequests(
                mechanicId,
                "Assigned"
        );
    }

    // =====================================================
    // MECHANIC ACCEPTED REQUESTS
    // =====================================================

    public List<ServiceRequest>
    getAcceptedRequestsByMechanicId(
            String mechanicId
    ) throws Exception {

        return filterMechanicRequests(
                mechanicId,
                "Accepted"
        );
    }

    // =====================================================
    // MECHANIC IN-PROGRESS REQUESTS
    // =====================================================

    public List<ServiceRequest>
    getInProgressRequestsByMechanicId(
            String mechanicId
    ) throws Exception {

        return filterMechanicRequests(
                mechanicId,
                "In Progress"
        );
    }

    // =====================================================
    // MECHANIC ACTIVE JOBS
    // =====================================================

    public List<ServiceRequest>
    getActiveRequestsByMechanicId(
            String mechanicId
    ) throws Exception {

        List<ServiceRequest> allRequests =
                getRequestsByMechanicId(
                        mechanicId
                );

        List<ServiceRequest> result =
                new ArrayList<>();

        for (ServiceRequest request :
                allRequests) {

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
    // MECHANIC JOB HISTORY
    // =====================================================

    public List<ServiceRequest>
    getHistoryByMechanicId(
            String mechanicId
    ) throws Exception {

        List<ServiceRequest> allRequests =
                getRequestsByMechanicId(
                        mechanicId
                );

        List<ServiceRequest> result =
                new ArrayList<>();

        for (ServiceRequest request :
                allRequests) {

            if (request.isFinished()) {

                result.add(
                        request
                );
            }
        }

        return result;
    }

    // =====================================================
    // MECHANIC + STATUS HELPER
    // =====================================================

    private List<ServiceRequest>
    filterMechanicRequests(
            String mechanicId,
            String status
    ) throws Exception {

        List<ServiceRequest> requests =
                getRequestsByMechanicId(
                        mechanicId
                );

        List<ServiceRequest> result =
                new ArrayList<>();

        for (ServiceRequest request :
                requests) {

            if (equalsStatus(
                    request.getStatus(),
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
    // FIRESTORE DOCUMENT -> MODEL
    //
    // Manual conversion is intentional:
    // old Firestore documents may contain numeric cost/year
    // style values and we want safe String conversion.
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

        request.setRequestId(
                firstString(
                        document,
                        "requestId"
                )
        );

        if (clean(
                request.getRequestId()
        ) == null) {

            request.setRequestId(
                    document.getId()
            );
        }

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
                firstString(
                        document,
                        "customerName",
                        "userName"
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
                        "registrationNumber"
                )
        );

        // =================================================
        // MECHANIC
        // =================================================

        request.setMechanicId(
                normalizeId(
                        firstString(
                                document,
                                "mechanicId"
                        )
                )
        );

        request.setMechanicName(
                firstString(
                        document,
                        "mechanicName"
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
                        "issue",
                        "problem"
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
        // DATES
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
        // MECHANIC WORK
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
                        "cost"
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
    // FIRST DOCUMENT VALUE AS STRING
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
                "pending"
        )) {

            return "Pending";
        }

        if (status.equalsIgnoreCase(
                "assigned"
        )) {

            return "Assigned";
        }

        if (status.equalsIgnoreCase(
                "accepted"
        )) {

            return "Accepted";
        }

        if (
                status.equalsIgnoreCase(
                        "in progress"
                )
                        ||
                status.equalsIgnoreCase(
                        "inprogress"
                )
                        ||
                status.equalsIgnoreCase(
                        "active"
                )
        ) {

            return "In Progress";
        }

        if (status.equalsIgnoreCase(
                "completed"
        )) {

            return "Completed";
        }

        if (
                status.equalsIgnoreCase(
                        "cancelled"
                )
                        ||
                status.equalsIgnoreCase(
                        "canceled"
                )
        ) {

            return "Cancelled";
        }

        return status;
    }

    // =====================================================
    // RELEASE TOW TRUCK WHEN REQUEST ENDS
    // =====================================================

    private void releaseTowTruckIfNeeded(
            DocumentSnapshot requestDocument,
            String requestId
    ) {

        if (requestDocument == null
                || !requestDocument.exists()) {
            return;
        }

        String towTruckId =
                clean(
                        stringValue(
                                requestDocument.get("towTruckId")
                        )
                );

        if (towTruckId == null) {
            return;
        }

        try {

            DocumentReference truckReference =
                    firestore
                            .collection("towTrucks")
                            .document(towTruckId);

            DocumentSnapshot truck =
                    truckReference
                            .get()
                            .get();

            if (!truck.exists()) {
                return;
            }

            String activeRequestId =
                    clean(
                            stringValue(
                                    truck.get("activeRequestId")
                            )
                    );

            if (activeRequestId != null
                    && requestId != null
                    && !activeRequestId.equals(requestId)) {
                return;
            }

            truckReference
                    .update(
                            "status", "Available",
                            "available", true,
                            "activeRequestId", "",
                            "bookedByCustomerId", "",
                            "updatedAt", currentTime()
                    )
                    .get();

        } catch (Exception e) {

            System.err.println(
                    "Unable to release tow truck: "
                            + e.getMessage()
            );
        }
    }

    // =====================================================
    // ID NORMALIZATION
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

        /*
         * Current project uses emails as User/Mechanic IDs.
         */
        if (value.contains("@")) {

            return value
                    .toLowerCase();
        }

        return value;
    }

    // =====================================================
    // SAME ID
    // =====================================================

    private boolean sameId(
            String first,
            String second
    ) {

        String firstId =
                normalizeId(
                        first
                );

        String secondId =
                normalizeId(
                        second
                );

        return firstId != null
                &&
                secondId != null
                &&
                firstId.equals(
                        secondId
                );
    }

    // =====================================================
    // STATUS CHECK
    // =====================================================

    private boolean equalsStatus(
            String first,
            String second
    ) {

        return first != null
                &&
                second != null
                &&
                first.equalsIgnoreCase(
                        second
                );
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
    // DOUBLE VALUE
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
    // EMPTY IF NULL
    // =====================================================

    private String emptyIfNull(
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

    // =====================================================
    // TIME
    // =====================================================

    private String currentTime() {

        return String.valueOf(
                System.currentTimeMillis()
        );
    }
}