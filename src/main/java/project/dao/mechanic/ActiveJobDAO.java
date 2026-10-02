package project.dao.mechanic;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;

import project.firebase.FirebaseConfig;
import project.model.ServiceRequest;
import project.ui.user.UserSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActiveJobDAO {

    // =====================================================
    // ONE SHARED COLLECTION
    // =====================================================

    private static final String COLLECTION =
            "serviceRequests";

    private final Firestore firestore;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public ActiveJobDAO() {

        try {

            firestore =
                    FirebaseConfig.getFirestore();

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to initialize Firestore.",
                    e
            );
        }
    }

    public ActiveJobDAO(
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
    // CURRENT MECHANIC
    //
    // Email first because serviceRequests mechanicId
    // normally stores mechanic email.
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
    // GET ACTIVE JOBS
    //
    // Accepted
    // In Progress
    // =====================================================

    public List<ServiceRequest> getActiveJobs()
            throws Exception {

        return getActiveJobs(
                getRequiredMechanicId()
        );
    }

    // =====================================================
    // ACTIVE JOBS BY MECHANIC
    //
    // We intentionally read and normalize IDs rather than
    // depending only on whereEqualTo.
    // =====================================================

    public List<ServiceRequest> getActiveJobs(
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

            // =================================================
            // ONLY CURRENT MECHANIC
            // =================================================

            if (!sameId(
                    request.getMechanicId(),
                    mechanicId
            )) {

                continue;
            }

            // =================================================
            // ACTIVE JOB STATUS
            // =================================================

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
    // CURRENT ACTIVE JOB
    //
    // Priority:
    //
    // 1. In Progress
    // 2. Accepted
    // =====================================================

    public ServiceRequest getCurrentActiveJob()
            throws Exception {

        List<ServiceRequest> jobs =
                getActiveJobs();

        if (jobs.isEmpty()) {

            return null;
        }

        ServiceRequest inProgress =
                findLatestByStatus(
                        jobs,
                        "In Progress"
                );

        if (inProgress != null) {

            return inProgress;
        }

        return findLatestByStatus(
                jobs,
                "Accepted"
        );
    }

    // =====================================================
    // GET JOB BY ID
    // =====================================================

    public ServiceRequest getJobById(
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
    // START JOB
    //
    // Accepted -> In Progress
    // =====================================================

    public boolean startJob(
            String requestId
    ) throws Exception {

        return startJob(
                requestId,
                getRequiredMechanicId()
        );
    }

    public boolean startJob(
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

        DocumentSnapshot document =
                reference
                        .get()
                        .get();

        if (!document.exists()) {

            return false;
        }

        ServiceRequest request =
                convertDocument(
                        document
                );

        if (request == null) {

            return false;
        }

        // =================================================
        // OWNERSHIP
        // =================================================

        if (!sameId(
                request.getMechanicId(),
                mechanicId
        )) {

            System.err.println(
                    "Start job blocked: request is not assigned to current mechanic."
            );

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
        // ONLY ACCEPTED CAN START
        // =================================================

        if (!hasStatus(
                request,
                "Accepted"
        )) {

            System.err.println(
                    "Start job blocked. Current status: "
                            + request.getStatus()
            );

            return false;
        }

        String now =
                currentTime();

        Map<String, Object> updates =
                new HashMap<>();

        updates.put(
                "status",
                "In Progress"
        );

        updates.put(
                "startedDate",
                now
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
                "Active job started: "
                        + requestId
                        + " -> In Progress"
        );

        return true;
    }

    // =====================================================
    // SAVE DIAGNOSIS
    // =====================================================

    public boolean saveDiagnosis(
            String requestId,
            String diagnosis
    ) throws Exception {

        String mechanicId =
                getRequiredMechanicId();

        if (!canModifyJob(
                requestId,
                mechanicId
        )) {

            return false;
        }

        Map<String, Object> updates =
                new HashMap<>();

        updates.put(
                "diagnosis",
                cleanOrEmpty(
                        diagnosis
                )
        );

        updates.put(
                "updatedAt",
                currentTime()
        );

        firestore
                .collection(COLLECTION)
                .document(requestId)
                .set(
                        updates,
                        SetOptions.merge()
                )
                .get();

        return true;
    }

    // =====================================================
    // SAVE REPAIR DETAILS
    // =====================================================

    public boolean saveRepairDetails(
            String requestId,
            String repairDetails
    ) throws Exception {

        String mechanicId =
                getRequiredMechanicId();

        if (!canModifyJob(
                requestId,
                mechanicId
        )) {

            return false;
        }

        Map<String, Object> updates =
                new HashMap<>();

        updates.put(
                "repairDetails",
                cleanOrEmpty(
                        repairDetails
                )
        );

        updates.put(
                "updatedAt",
                currentTime()
        );

        firestore
                .collection(COLLECTION)
                .document(requestId)
                .set(
                        updates,
                        SetOptions.merge()
                )
                .get();

        return true;
    }

    // =====================================================
    // SAVE COST DETAILS
    // =====================================================

    public boolean saveCostDetails(
            String requestId,
            String estimatedCost,
            String partsCost,
            String labourCost,
            String finalCost
    ) throws Exception {

        String mechanicId =
                getRequiredMechanicId();

        if (!canModifyJob(
                requestId,
                mechanicId
        )) {

            return false;
        }

        Map<String, Object> updates =
                new HashMap<>();

        updates.put(
                "estimatedCost",
                cleanCost(
                        estimatedCost
                )
        );

        updates.put(
                "partsCost",
                cleanCost(
                        partsCost
                )
        );

        updates.put(
                "labourCost",
                cleanCost(
                        labourCost
                )
        );

        String calculatedFinalCost =
                resolveFinalCost(
                        finalCost,
                        partsCost,
                        labourCost
                );

        updates.put(
                "finalCost",
                calculatedFinalCost
        );

        // Compatibility aliases
        updates.put(
                "totalCost",
                calculatedFinalCost
        );

        updates.put(
                "updatedAt",
                currentTime()
        );

        firestore
                .collection(COLLECTION)
                .document(requestId)
                .set(
                        updates,
                        SetOptions.merge()
                )
                .get();

        return true;
    }

    // =====================================================
    // SAVE COMPLETE JOB DETAILS
    // =====================================================

    public boolean saveJobDetails(
            String requestId,
            String diagnosis,
            String repairDetails,
            String estimatedCost,
            String partsCost,
            String labourCost,
            String finalCost
    ) throws Exception {

        String mechanicId =
                getRequiredMechanicId();

        if (!canModifyJob(
                requestId,
                mechanicId
        )) {

            return false;
        }

        Map<String, Object> updates =
                new HashMap<>();

        // =================================================
        // WORK
        // =================================================

        updates.put(
                "diagnosis",
                cleanOrEmpty(
                        diagnosis
                )
        );

        updates.put(
                "repairDetails",
                cleanOrEmpty(
                        repairDetails
                )
        );

        // =================================================
        // COST
        // =================================================

        updates.put(
                "estimatedCost",
                cleanCost(
                        estimatedCost
                )
        );

        updates.put(
                "partsCost",
                cleanCost(
                        partsCost
                )
        );

        updates.put(
                "labourCost",
                cleanCost(
                        labourCost
                )
        );

        String calculatedFinalCost =
                resolveFinalCost(
                        finalCost,
                        partsCost,
                        labourCost
                );

        updates.put(
                "finalCost",
                calculatedFinalCost
        );

        updates.put(
                "totalCost",
                calculatedFinalCost
        );

        updates.put(
                "updatedAt",
                currentTime()
        );

        firestore
                .collection(COLLECTION)
                .document(requestId)
                .set(
                        updates,
                        SetOptions.merge()
                )
                .get();

        System.out.println(
                "Active job details saved: "
                        + requestId
        );

        return true;
    }

    // =====================================================
    // COMPLETE JOB
    //
    // In Progress -> Completed
    // =====================================================

    public boolean completeJob(
            String requestId
    ) throws Exception {

        return completeJob(
                requestId,
                getRequiredMechanicId()
        );
    }

    public boolean completeJob(
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

        DocumentSnapshot document =
                reference
                        .get()
                        .get();

        if (!document.exists()) {

            return false;
        }

        ServiceRequest request =
                convertDocument(
                        document
                );

        if (request == null) {

            return false;
        }

        // =================================================
        // OWNERSHIP
        // =================================================

        if (!sameId(
                request.getMechanicId(),
                mechanicId
        )) {

            System.err.println(
                    "Complete job blocked: request belongs to another mechanic."
            );

            return false;
        }

        // =================================================
        // ALREADY COMPLETED
        // =================================================

        if (hasStatus(
                request,
                "Completed"
        )) {

            return true;
        }

        // =================================================
        // ONLY IN PROGRESS CAN COMPLETE
        // =================================================

        if (!hasStatus(
                request,
                "In Progress"
        )) {

            System.err.println(
                    "Complete job blocked. Current status: "
                            + request.getStatus()
            );

            return false;
        }

        String now =
                currentTime();

        String finalCost =
                resolveFinalCost(
                        request.getFinalCost(),
                        request.getPartsCost(),
                        request.getLabourCost()
                );

        Map<String, Object> updates =
                new HashMap<>();

        updates.put(
                "status",
                "Completed"
        );

        updates.put(
                "completedDate",
                now
        );

        updates.put(
                "updatedAt",
                now
        );

        // =================================================
        // FINAL COST
        // =================================================

        updates.put(
                "finalCost",
                finalCost
        );

        updates.put(
                "totalCost",
                finalCost
        );

        // =================================================
        // NAVIGATION FINAL STATE
        // =================================================

        updates.put(
                "jobStatus",
                "Completed"
        );

        reference
                .set(
                        updates,
                        SetOptions.merge()
                )
                .get();

        /*
         * If this job came from the Tow Truck flow, the reserved
         * tow truck must become available again after completion.
         */
        releaseTowTruckIfNeeded(
                document,
                requestId
        );

        System.out.println(
                "Service request completed: "
                        + requestId
                        + " | finalCost="
                        + finalCost
        );

        return true;
    }

    // =====================================================
    // COMPLETE JOB WITH DETAILS
    // =====================================================

    public boolean completeJob(
            String requestId,
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

        if (requestId == null) {

            return false;
        }

        // =================================================
        // MUST BE CURRENT MECHANIC'S ACTIVE JOB
        // =================================================

        String mechanicId =
                getRequiredMechanicId();

        ServiceRequest current =
                getJobById(
                        requestId
                );

        if (current == null
                ||
                !sameId(
                        current.getMechanicId(),
                        mechanicId
                )) {

            return false;
        }

        if (hasStatus(
                current,
                "Completed"
        )) {

            return true;
        }

        if (!hasStatus(
                current,
                "In Progress"
        )) {

            return false;
        }

        // =================================================
        // SAVE FINAL DETAILS
        // =================================================

        boolean saved =
                saveJobDetails(
                        requestId,
                        diagnosis,
                        repairDetails,
                        estimatedCost,
                        partsCost,
                        labourCost,
                        finalCost
                );

        if (!saved) {

            return false;
        }

        // =================================================
        // COMPLETE SAME DOCUMENT
        // =================================================

        return completeJob(
                requestId,
                mechanicId
        );
    }

    // =====================================================
    // UPDATE ESTIMATED COST
    // =====================================================

    public boolean updateEstimatedCost(
            String requestId,
            String estimatedCost
    ) throws Exception {

        String mechanicId =
                getRequiredMechanicId();

        if (!canModifyJob(
                requestId,
                mechanicId
        )) {

            return false;
        }

        Map<String, Object> updates =
                new HashMap<>();

        updates.put(
                "estimatedCost",
                cleanCost(
                        estimatedCost
                )
        );

        updates.put(
                "updatedAt",
                currentTime()
        );

        firestore
                .collection(COLLECTION)
                .document(requestId)
                .set(
                        updates,
                        SetOptions.merge()
                )
                .get();

        return true;
    }

    // =====================================================
    // MARK ARRIVED
    //
    // Does not change repair status.
    // =====================================================

    public boolean markArrived(
            String requestId
    ) throws Exception {

        String mechanicId =
                getRequiredMechanicId();

        ServiceRequest request =
                getJobById(
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

        if (
                !hasStatus(
                        request,
                        "Accepted"
                )
                        &&
                !hasStatus(
                        request,
                        "In Progress"
                )
        ) {

            return false;
        }

        String now =
                currentTime();

        Map<String, Object> updates =
                new HashMap<>();

        updates.put(
                "arrivalStatus",
                "Arrived"
        );

        updates.put(
                "arrivedDate",
                now
        );

        updates.put(
                "updatedAt",
                now
        );

        firestore
                .collection(COLLECTION)
                .document(requestId)
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
    // Accepted / In Progress only.
    // =====================================================

    public boolean startNavigation(
            String requestId
    ) throws Exception {

        String mechanicId =
                getRequiredMechanicId();

        ServiceRequest request =
                getJobById(
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

        if (
                !hasStatus(
                        request,
                        "Accepted"
                )
                        &&
                !hasStatus(
                        request,
                        "In Progress"
                )
        ) {

            return false;
        }

        String now =
                currentTime();

        Map<String, Object> updates =
                new HashMap<>();

        updates.put(
                "navigationStatus",
                "Started"
        );

        // Starting navigation means the mechanic is now on the way.
        // Keep the dedicated navigationStatus and also advance the
        // main service lifecycle so both sides show In Progress.
        updates.put(
                "status",
                "In Progress"
        );

        updates.put(
                "navigationStartedDate",
                now
        );

        updates.put(
                "updatedAt",
                now
        );

        firestore
                .collection(COLLECTION)
                .document(requestId)
                .set(
                        updates,
                        SetOptions.merge()
                )
                .get();

        return true;
    }

    // =====================================================
    // SAVE / CONFIRM MECHANIC LOCATION
    //
    // Used by both GPS and manual map selection.
    // Keeping this as a public wrapper lets NavigationPage
    // use the exact same Firebase validation/update path.
    // =====================================================

    public boolean saveMechanicLocation(
            String requestId,
            double latitude,
            double longitude
    ) throws Exception {

        return updateMechanicLocation(
                requestId,
                latitude,
                longitude
        );
    }

    // =====================================================
    // UPDATE MECHANIC LIVE LOCATION
    //
    // Saves the mechanic's latest GPS position on the SAME
    // serviceRequests/{requestId} document.
    // Existing job/navigation functionality is not changed.
    // =====================================================

    public boolean updateMechanicLocation(
            String requestId,
            double latitude,
            double longitude
    ) throws Exception {

        requestId =
                clean(
                        requestId
                );

        if (requestId == null) {
            return false;
        }

        // Valid GPS range check.
        if (!Double.isFinite(latitude)
                || !Double.isFinite(longitude)
                || latitude < -90.0
                || latitude > 90.0
                || longitude < -180.0
                || longitude > 180.0) {

            return false;
        }

        String mechanicId =
                getRequiredMechanicId();

        ServiceRequest request =
                getJobById(
                        requestId
                );

        if (request == null) {
            return false;
        }

        // Only the mechanic assigned to this request may update
        // the live location.
        if (!sameId(
                request.getMechanicId(),
                mechanicId
        )) {
            return false;
        }

        // Location tracking is allowed only while the job is
        // Accepted or In Progress.
        if (!hasStatus(
                request,
                "Accepted"
        )
                && !hasStatus(
                        request,
                        "In Progress"
                )) {

            return false;
        }

        Map<String, Object> updates =
                new HashMap<>();

        updates.put(
                "mechanicLatitude",
                latitude
        );

        updates.put(
                "mechanicLongitude",
                longitude
        );

        updates.put(
                "mechanicLocationUpdatedAt",
                currentTime()
        );

        firestore
                .collection(COLLECTION)
                .document(requestId)
                .set(
                        updates,
                        SetOptions.merge()
                )
                .get();

        return true;
    }

    // =====================================================
    // ACTIVE JOB COUNT
    // =====================================================

    public int getActiveJobCount()
            throws Exception {

        return getActiveJobs()
                .size();
    }

    // =====================================================
    // CAN MODIFY
    //
    // Accepted:
    // estimate/diagnosis may be prepared.
    //
    // In Progress:
    // repair details/costs can be saved.
    // =====================================================

    private boolean canModifyJob(
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
                getJobById(
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

        return hasStatus(
                request,
                "Accepted"
        )
                ||
                hasStatus(
                        request,
                        "In Progress"
                );
    }

    // =====================================================
    // FIND LATEST BY STATUS
    // =====================================================

    private ServiceRequest findLatestByStatus(
            List<ServiceRequest> requests,
            String status
    ) {

        ServiceRequest latest =
                null;

        long latestTime =
                Long.MIN_VALUE;

        for (ServiceRequest request :
                requests) {

            if (!hasStatus(
                    request,
                    status
            )) {

                continue;
            }

            long time =
                    getRequestTimestamp(
                            request
                    );

            if (latest == null
                    ||
                    time > latestTime) {

                latest =
                        request;

                latestTime =
                        time;
            }
        }

        return latest;
    }

    // =====================================================
    // FIRESTORE -> SERVICE REQUEST
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
        // ID
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
        // MECHANIC LIVE GPS
        // =================================================

        request.setMechanicLatitude(
                doubleValue(
                        document.get(
                                "mechanicLatitude"
                        )
                )
        );

        request.setMechanicLongitude(
                doubleValue(
                        document.get(
                                "mechanicLongitude"
                        )
                )
        );

        request.setMechanicLocationUpdatedAt(
                firstString(
                        document,
                        "mechanicLocationUpdatedAt"
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
        // NAVIGATION / ARRIVAL STATUS
        // =================================================

        request.setNavigationStatus(
                firstString(
                        document,
                        "navigationStatus"
                )
        );

        request.setNavigationStartedDate(
                firstString(
                        document,
                        "navigationStartedDate"
                )
        );

        request.setArrivalStatus(
                firstString(
                        document,
                        "arrivalStatus"
                )
        );

        request.setArrivedDate(
                firstString(
                        document,
                        "arrivedDate"
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

        String actual =
                normalizeStatus(
                        request.getStatus()
                );

        String required =
                normalizeStatus(
                        status
                );

        return actual != null
                &&
                required != null
                &&
                actual.equalsIgnoreCase(
                        required
                );
    }

    // =====================================================
    // NORMALIZE STATUS
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
    // FINAL COST
    // =====================================================

    private String resolveFinalCost(
            String finalCost,
            String partsCost,
            String labourCost
    ) {

        String finalValue =
                clean(
                        finalCost
                );

        if (finalValue != null) {

            return cleanCost(
                    finalValue
            );
        }

        double parts =
                parseAmount(
                        partsCost
                );

        double labour =
                parseAmount(
                        labourCost
                );

        double total =
                parts + labour;

        if (total == Math.floor(total)) {

            return String.valueOf(
                    (long) total
            );
        }

        return String.valueOf(
                total
        );
    }

    // =====================================================
    // PARSE MONEY
    // =====================================================

    private double parseAmount(
            String value
    ) {

        String cleaned =
                clean(
                        value
                );

        if (cleaned == null) {

            return 0.0;
        }

        cleaned =
                cleaned
                        .replace("₹", "")
                        .replace(",", "")
                        .trim();

        try {

            return Double.parseDouble(
                    cleaned
            );

        } catch (Exception e) {

            return 0.0;
        }
    }

    // =====================================================
    // COST CLEAN
    // =====================================================

    private String cleanCost(
            String value
    ) {

        String cleaned =
                clean(
                        value
                );

        if (cleaned == null) {

            return "0";
        }

        cleaned =
                cleaned
                        .replace("₹", "")
                        .replace(",", "")
                        .trim();

        if (cleaned.isEmpty()) {

            return "0";
        }

        try {

            double amount =
                    Double.parseDouble(
                            cleaned
                    );

            if (amount < 0) {

                return "0";
            }

            if (amount == Math.floor(amount)) {

                return String.valueOf(
                        (long) amount
                );
            }

            return String.valueOf(
                    amount
            );

        } catch (Exception e) {

            return "0";
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

        long time =
                parseTime(
                        request.getUpdatedAt()
                );

        if (time > 0) {

            return time;
        }

        time =
                parseTime(
                        request.getStartedDate()
                );

        if (time > 0) {

            return time;
        }

        time =
                parseTime(
                        request.getAcceptedDate()
                );

        if (time > 0) {

            return time;
        }

        time =
                parseTime(
                        request.getAssignedDate()
                );

        if (time > 0) {

            return time;
        }

        return parseTime(
                request.getRequestDate()
        );
    }

    // =====================================================
    // PARSE TIME
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

                number *=
                        1000L;
            }

            return number;

        } catch (Exception e) {

            return 0L;
        }
    }

    // =====================================================
    // RELEASE TOW TRUCK AFTER COMPLETION
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
                    "Unable to release tow truck after completion: "
                            + e.getMessage()
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
    // CLEAN OR EMPTY
    // =====================================================

    private String cleanOrEmpty(
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

    // =====================================================
    // CURRENT TIME
    // =====================================================

    private String currentTime() {

        return String.valueOf(
                System.currentTimeMillis()
        );
    }
}   