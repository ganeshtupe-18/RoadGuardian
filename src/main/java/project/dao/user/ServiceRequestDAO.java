package project.dao.user;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;

import project.firebase.FirebaseConfig;
import project.model.ServiceRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ServiceRequestDAO {

    // =====================================================
    // SAME COLLECTION USED BY:
    // CUSTOMER + ADMIN + MECHANIC
    // =====================================================

    private static final String COLLECTION =
            "serviceRequests";

    private final Firestore firestore;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public ServiceRequestDAO() {

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

    // =====================================================
    // OPTIONAL CONSTRUCTOR
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
    // CREATE REQUEST USING MODEL
    // =====================================================

    public String createRequest(
            ServiceRequest request
    ) throws Exception {

        if (request == null) {

            throw new IllegalArgumentException(
                    "Service request cannot be null."
            );
        }

        // =================================================
        // CUSTOMER ID
        // =================================================

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

        // =================================================
        // CUSTOMER NAME
        // =================================================

        if (clean(
                request.getCustomerName()
        ) == null) {

            request.setCustomerName(
                    getCustomerName(
                            customerId
                    )
            );
        }

        // =================================================
        // VEHICLE ID
        // =================================================

        String vehicleId =
                clean(
                        request.getVehicleId()
                );

        if (vehicleId == null) {

            throw new IllegalArgumentException(
                    "Vehicle ID is required."
            );
        }

        request.setVehicleId(
                vehicleId
        );

        // =================================================
        // VERIFY VEHICLE OWNERSHIP
        // =================================================

        DocumentSnapshot vehicleDocument =
                getCustomerVehicle(
                        customerId,
                        vehicleId
                );

        if (vehicleDocument == null) {

            throw new IllegalArgumentException(
                    "Vehicle not found for this customer."
            );
        }

        // =================================================
        // VEHICLE NUMBER
        // =================================================

        if (clean(
                request.getVehicleNumber()
        ) == null) {

            request.setVehicleNumber(
                    firstString(
                            vehicleDocument,
                            "vehicleNumber",
                            "registrationNumber"
                    )
            );
        }

        // =================================================
        // SERVICE TYPE
        // =================================================

        if (clean(
                request.getServiceType()
        ) == null) {

            throw new IllegalArgumentException(
                    "Service type is required."
            );
        }

        // =================================================
        // MECHANIC
        //
        // IMPORTANT FIX:
        //
        // Customer ने mechanic select केला असेल:
        //      status = Assigned
        //
        // Mechanic select केला नसेल:
        //      status = Pending
        //      Admin नंतर mechanic assign करेल.
        // =================================================

        String mechanicId =
                normalizeId(
                        request.getMechanicId()
                );

        String now =
                currentTime();

        if (mechanicId != null) {

            // =============================================
            // CUSTOMER SELECTED A SPECIFIC MECHANIC
            // =============================================

            request.setMechanicId(
                    mechanicId
            );

            if (clean(
                    request.getMechanicName()
            ) == null) {

                request.setMechanicName(
                        getMechanicName(
                                mechanicId
                        )
                );
            }

            request.setStatus(
                    "Assigned"
            );

            request.setAssignedDate(
                    now
            );

        } else {

            // =============================================
            // NO MECHANIC SELECTED
            // ADMIN MUST ASSIGN
            // =============================================

            request.setMechanicId(
                    null
            );

            request.setMechanicName(
                    null
            );

            request.setStatus(
                    "Pending"
            );

            request.setAssignedDate(
                    null
            );
        }

        // =================================================
        // REQUEST DATE
        // =================================================

        if (clean(
                request.getRequestDate()
        ) == null) {

            request.setRequestDate(
                    now
            );
        }

        // =================================================
        // UPDATED DATE
        // =================================================

        request.setUpdatedAt(
                now
        );

        // =================================================
        // RESET WORKFLOW DATES
        //
        // assignedDate intentionally preserved when
        // mechanic is already selected.
        // =================================================

        request.setAcceptedDate(
                null
        );

        request.setStartedDate(
                null
        );

        request.setCompletedDate(
                null
        );

        request.setCancelledDate(
                null
        );

        // =================================================
        // DEFAULT SOURCE
        // =================================================

        if (clean(
                request.getSource()
        ) == null) {

            request.setSource(
                    "Customer"
            );
        }

        // =================================================
        // DEFAULT PRIORITY
        // =================================================

        if (clean(
                request.getPriority()
        ) == null) {

            request.setPriority(
                    "Normal"
            );
        }

        // =================================================
        // CREATE FIRESTORE DOCUMENT
        // =================================================

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

        System.out.println(
                "Service request created: "
                        + requestId
                        + " | status="
                        + request.getStatus()
                        + " | mechanicId="
                        + request.getMechanicId()
        );

        return requestId;
    }

    // =====================================================
    // CREATE REQUEST
    //
    // NO MECHANIC SELECTED
    // -> Pending
    // =====================================================

    public String createRequest(
            String customerId,
            String customerName,
            String vehicleId,
            String vehicleNumber,
            String serviceType,
            String description,
            String location
    ) throws Exception {

        ServiceRequest request =
                new ServiceRequest();

        request.setCustomerId(
                customerId
        );

        request.setCustomerName(
                customerName
        );

        request.setVehicleId(
                vehicleId
        );

        request.setVehicleNumber(
                vehicleNumber
        );

        request.setServiceType(
                serviceType
        );

        request.setDescription(
                description
        );

        request.setLocation(
                location
        );

        request.setSource(
                "Customer"
        );

        request.setPriority(
                "Normal"
        );

        // No mechanic here
        // so createRequest(request) makes it Pending.

        return createRequest(
                request
        );
    }

    // =====================================================
    // CREATE REQUEST WITH SELECTED MECHANIC
    //
    // Customer MechanicsPage uses THIS overload.
    //
    // Customer selected mechanic
    // -> Assigned directly
    // -> Mechanic RequestsPage can see it.
    // =====================================================

    public String createRequest(
            String customerId,
            String customerName,
            String vehicleId,
            String vehicleNumber,
            String mechanicId,
            String mechanicName,
            String serviceType,
            String description,
            String location
    ) throws Exception {

        ServiceRequest request =
                new ServiceRequest();

        request.setCustomerId(
                customerId
        );

        request.setCustomerName(
                customerName
        );

        request.setVehicleId(
                vehicleId
        );

        request.setVehicleNumber(
                vehicleNumber
        );

        // =================================================
        // SELECTED MECHANIC
        // =================================================

        request.setMechanicId(
                normalizeId(
                        mechanicId
                )
        );

        request.setMechanicName(
                cleanOrEmpty(
                        mechanicName
                )
        );

        request.setServiceType(
                serviceType
        );

        request.setDescription(
                description
        );

        request.setLocation(
                location
        );

        request.setSource(
                "Customer"
        );

        request.setPriority(
                "Normal"
        );

        /*
         * createRequest(request) detects mechanicId
         * and automatically sets:
         *
         * status = Assigned
         * assignedDate = now
         */

        return createRequest(
                request
        );
    }

    // =====================================================
    // CREATE REQUEST WITH GPS
    //
    // No mechanic selected
    // -> Pending
    // =====================================================

    public String createRequest(
            String customerId,
            String customerName,
            String vehicleId,
            String vehicleNumber,
            String serviceType,
            String description,
            String location,
            Double latitude,
            Double longitude
    ) throws Exception {

        ServiceRequest request =
                new ServiceRequest();

        request.setCustomerId(
                customerId
        );

        request.setCustomerName(
                customerName
        );

        request.setVehicleId(
                vehicleId
        );

        request.setVehicleNumber(
                vehicleNumber
        );

        request.setServiceType(
                serviceType
        );

        request.setDescription(
                description
        );

        request.setLocation(
                location
        );

        request.setLatitude(
                latitude
        );

        request.setLongitude(
                longitude
        );

        request.setSource(
                "Customer"
        );

        request.setPriority(
                "Normal"
        );

        return createRequest(
                request
        );
    }

    // =====================================================
    // GET SINGLE REQUEST
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
    // GET ALL REQUESTS OF CUSTOMER
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

        // Latest first
        requests.sort(
                (first, second) ->
                        Long.compare(
                                parseTime(
                                        second.getRequestDate()
                                ),
                                parseTime(
                                        first.getRequestDate()
                                )
                        )
        );

        return requests;
    }

    // =====================================================
    // ACTIVE REQUESTS
    //
    // Pending
    // Assigned
    // Accepted
    // In Progress
    // =====================================================

    public List<ServiceRequest> getActiveRequests(
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

            if (
                    isStatus(
                            request,
                            "Pending"
                    )
                            ||
                    isStatus(
                            request,
                            "Assigned"
                    )
                            ||
                    isStatus(
                            request,
                            "Accepted"
                    )
                            ||
                    isStatus(
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
    // PENDING REQUESTS
    // =====================================================

    public List<ServiceRequest> getPendingRequests(
            String customerId
    ) throws Exception {

        return getRequestsByStatus(
                customerId,
                "Pending"
        );
    }

    // =====================================================
    // ASSIGNED REQUESTS
    // =====================================================

    public List<ServiceRequest> getAssignedRequests(
            String customerId
    ) throws Exception {

        return getRequestsByStatus(
                customerId,
                "Assigned"
        );
    }

    // =====================================================
    // ACCEPTED + IN PROGRESS
    // =====================================================

    public List<ServiceRequest> getInProgressRequests(
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

            if (
                    isStatus(
                            request,
                            "Accepted"
                    )
                            ||
                    isStatus(
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
    // COMPLETED SERVICE HISTORY
    // =====================================================

    public List<ServiceRequest> getServiceHistory(
            String customerId
    ) throws Exception {

        return getRequestsByStatus(
                customerId,
                "Completed"
        );
    }

    // =====================================================
    // CANCELLED REQUESTS
    // =====================================================

    public List<ServiceRequest> getCancelledRequests(
            String customerId
    ) throws Exception {

        return getRequestsByStatus(
                customerId,
                "Cancelled"
        );
    }

    // =====================================================
    // REQUESTS BY STATUS
    // =====================================================

    public List<ServiceRequest> getRequestsByStatus(
            String customerId,
            String status
    ) throws Exception {

        List<ServiceRequest> allRequests =
                getRequestsByCustomerId(
                        customerId
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

            return allRequests;
        }

        for (ServiceRequest request :
                allRequests) {

            if (isStatus(
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
    // CANCEL REQUEST
    //
    // Customer can cancel only own request.
    // Completed request cannot be cancelled.
    // =====================================================

    public boolean cancelRequest(
            String customerId,
            String requestId
    ) throws Exception {

        customerId =
                normalizeId(
                        customerId
                );

        requestId =
                clean(
                        requestId
                );

        if (customerId == null
                ||
                requestId == null) {

            return false;
        }

        ServiceRequest request =
                getRequestById(
                        requestId
                );

        if (request == null) {

            return false;
        }

        // =================================================
        // OWNERSHIP
        // =================================================

        if (!sameId(
                request.getCustomerId(),
                customerId
        )) {

            return false;
        }

        // =================================================
        // COMPLETED CANNOT CANCEL
        // =================================================

        if (isStatus(
                request,
                "Completed"
        )) {

            return false;
        }

        if (isStatus(
                request,
                "Cancelled"
        )) {

            return true;
        }

        String now =
                currentTime();

        DocumentReference requestReference =
                firestore
                        .collection(COLLECTION)
                        .document(requestId);

        DocumentSnapshot rawRequest =
                requestReference
                        .get()
                        .get();

        requestReference
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
                rawRequest,
                requestId
        );

        return true;
    }

    // =====================================================
    // UPDATE REQUEST DETAILS
    //
    // Pending / Assigned request can still be edited.
    //
    // Once mechanic accepts/starts job,
    // customer cannot modify request.
    // =====================================================

    public boolean updateRequest(
            String customerId,
            ServiceRequest request
    ) throws Exception {

        customerId =
                normalizeId(
                        customerId
                );

        if (customerId == null
                ||
                request == null) {

            return false;
        }

        String requestId =
                clean(
                        request.getRequestId()
                );

        if (requestId == null) {

            return false;
        }

        ServiceRequest existing =
                getRequestById(
                        requestId
                );

        if (existing == null) {

            return false;
        }

        // =================================================
        // CUSTOMER OWNERSHIP
        // =================================================

        if (!sameId(
                existing.getCustomerId(),
                customerId
        )) {

            return false;
        }

        // =================================================
        // LOCK AFTER MECHANIC ACCEPTS
        // =================================================

        if (
                isStatus(
                        existing,
                        "Accepted"
                )
                        ||
                isStatus(
                        existing,
                        "In Progress"
                )
                        ||
                isStatus(
                        existing,
                        "Completed"
                )
                        ||
                isStatus(
                        existing,
                        "Cancelled"
                )
        ) {

            return false;
        }

        // =================================================
        // IMPORTANT:
        //
        // Preserve workflow fields from existing document.
        // Customer edit should not accidentally remove
        // mechanic assignment/status.
        // =================================================

        request.setCustomerId(
                customerId
        );

        request.setMechanicId(
                existing.getMechanicId()
        );

        request.setMechanicName(
                existing.getMechanicName()
        );

        request.setStatus(
                existing.getStatus()
        );

        request.setRequestDate(
                existing.getRequestDate()
        );

        request.setAssignedDate(
                existing.getAssignedDate()
        );

        request.setAcceptedDate(
                existing.getAcceptedDate()
        );

        request.setStartedDate(
                existing.getStartedDate()
        );

        request.setCompletedDate(
                existing.getCompletedDate()
        );

        request.setCancelledDate(
                existing.getCancelledDate()
        );

        request.setUpdatedAt(
                currentTime()
        );

        firestore
                .collection(COLLECTION)
                .document(requestId)
                .set(
                        request,
                        SetOptions.merge()
                )
                .get();

        return true;
    }

    // =====================================================
    // GET LATEST ACTIVE REQUEST
    // =====================================================

    public ServiceRequest getLatestActiveRequest(
            String customerId
    ) throws Exception {

        List<ServiceRequest> activeRequests =
                getActiveRequests(
                        customerId
                );

        if (activeRequests.isEmpty()) {

            return null;
        }

        ServiceRequest latest =
                null;

        long latestTime =
                Long.MIN_VALUE;

        for (ServiceRequest request :
                activeRequests) {

            long time =
                    parseTime(
                            request.getRequestDate()
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
    // COUNT ACTIVE REQUESTS
    // =====================================================

    public int getActiveRequestCount(
            String customerId
    ) throws Exception {

        return getActiveRequests(
                customerId
        ).size();
    }

    // =====================================================
    // COUNT COMPLETED REQUESTS
    // =====================================================

    public int getCompletedRequestCount(
            String customerId
    ) throws Exception {

        return getServiceHistory(
                customerId
        ).size();
    }

    // =====================================================
    // VERIFY VEHICLE OWNERSHIP
    // =====================================================

    private DocumentSnapshot getCustomerVehicle(
            String customerId,
            String vehicleId
    ) throws Exception {

        DocumentSnapshot vehicle =
                firestore
                        .collection("vehicles")
                        .document(vehicleId)
                        .get()
                        .get();

        if (!vehicle.exists()) {

            return null;
        }

        String savedCustomerId =
                firstString(
                        vehicle,
                        "customerId",
                        "userEmail"
                );

        if (!sameId(
                savedCustomerId,
                customerId
        )) {

            return null;
        }

        return vehicle;
    }

    // =====================================================
    // CUSTOMER NAME
    // =====================================================

    private String getCustomerName(
            String customerId
    ) {

        try {

            // =================================================
            // users/{email}
            // =================================================

            DocumentSnapshot user =
                    firestore
                            .collection("users")
                            .document(customerId)
                            .get()
                            .get();

            if (user.exists()) {

                String name =
                        firstString(
                                user,
                                "name",
                                "fullName",
                                "userName"
                        );

                if (name != null) {

                    return name;
                }
            }

            // =================================================
            // customers/{id}
            // =================================================

            DocumentSnapshot customer =
                    firestore
                            .collection("customers")
                            .document(customerId)
                            .get()
                            .get();

            if (customer.exists()) {

                String name =
                        firstString(
                                customer,
                                "name",
                                "fullName",
                                "customerName"
                        );

                if (name != null) {

                    return name;
                }
            }

        } catch (Exception e) {

            System.err.println(
                    "Unable to load customer name: "
                            + e.getMessage()
            );
        }

        return "";
    }

    // =====================================================
    // MECHANIC NAME
    //
    // Used if mechanic ID exists but mechanicName was not
    // supplied by UI.
    // =====================================================

    private String getMechanicName(
            String mechanicId
    ) {

        mechanicId =
                normalizeId(
                        mechanicId
                );

        if (mechanicId == null) {

            return "";
        }

        try {

            // =================================================
            // DIRECT DOCUMENT ID
            // =================================================

            DocumentSnapshot mechanic =
                    firestore
                            .collection("mechanics")
                            .document(mechanicId)
                            .get()
                            .get();

            if (mechanic.exists()) {

                String name =
                        firstString(
                                mechanic,
                                "name",
                                "fullName",
                                "mechanicName"
                        );

                if (name != null) {

                    return name;
                }
            }

            // =================================================
            // mechanicId FIELD
            // =================================================

            QuerySnapshot byMechanicId =
                    firestore
                            .collection("mechanics")
                            .whereEqualTo(
                                    "mechanicId",
                                    mechanicId
                            )
                            .limit(1)
                            .get()
                            .get();

            if (!byMechanicId.isEmpty()) {

                String name =
                        firstString(
                                byMechanicId
                                        .getDocuments()
                                        .get(0),
                                "name",
                                "fullName",
                                "mechanicName"
                        );

                if (name != null) {

                    return name;
                }
            }

            // =================================================
            // EMAIL FIELD
            // =================================================

            QuerySnapshot byEmail =
                    firestore
                            .collection("mechanics")
                            .whereEqualTo(
                                    "email",
                                    mechanicId
                            )
                            .limit(1)
                            .get()
                            .get();

            if (!byEmail.isEmpty()) {

                String name =
                        firstString(
                                byEmail
                                        .getDocuments()
                                        .get(0),
                                "name",
                                "fullName",
                                "mechanicName"
                        );

                if (name != null) {

                    return name;
                }
            }

        } catch (Exception e) {

            System.err.println(
                    "Unable to load mechanic name: "
                            + e.getMessage()
            );
        }

        return "";
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
        // REQUEST ID
        // =================================================

        String requestId =
                firstString(
                        document,
                        "requestId"
                );

        if (requestId == null) {

            requestId =
                    document.getId();
        }

        request.setRequestId(
                requestId
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
        // SERVICE INFORMATION
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
        // LIVE MECHANIC GPS
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
        // MECHANIC WORK DETAILS
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
    // STATUS CHECK
    // =====================================================

    private boolean isStatus(
            ServiceRequest request,
            String status
    ) {

        if (request == null
                ||
                request.getStatus() == null
                ||
                status == null) {

            return false;
        }

        return request
                .getStatus()
                .equalsIgnoreCase(
                        status
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
    // FIRST STRING
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
    // RELEASE TOW TRUCK WHEN CUSTOMER CANCELS TOW REQUEST
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
                    "Unable to release tow truck after customer cancellation: "
                            + e.getMessage()
            );
        }
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
    // CURRENT TIME
    // =====================================================

    private String currentTime() {

        return String.valueOf(
                System.currentTimeMillis()
        );
    }

    // =====================================================
    // PARSE TIME
    // =====================================================

    private long parseTime(
            String value
    ) {

        if (value == null
                ||
                value.isBlank()) {

            return 0L;
        }

        try {

            return Long.parseLong(
                    value.trim()
            );

        } catch (Exception e) {

            return 0L;
        }
    }
}