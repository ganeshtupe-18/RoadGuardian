package project.dao.user;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;

import project.firebase.FirebaseConfig;

import java.io.IOException;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserDashboardDAO {

    // =====================================================
    // COLLECTIONS
    // =====================================================

    private static final String USERS =
            "users";

    private static final String CUSTOMERS =
            "customers";

    private static final String VEHICLES =
            "vehicles";

    private static final String SERVICE_REQUESTS =
            "serviceRequests";

    private static final String MECHANICS =
            "mechanics";

    // =====================================================
    // FIRESTORE
    // =====================================================

    private final Firestore firestore;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public UserDashboardDAO()
            throws IOException {

        firestore =
                FirebaseConfig.getFirestore();
    }

    public UserDashboardDAO(
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
    // USER PROFILE
    //
    // Registered Customer:
    //
    // users/{email}
    //
    // Admin compatibility:
    //
    // customers/{email}
    // =====================================================

    public Map<String, Object> getUserProfile(
            String email
    ) throws Exception {

        String customerId =
                normalizeId(
                        email
                );

        Map<String, Object> result =
                new HashMap<>();

        if (customerId == null) {

            return result;
        }

        // =================================================
        // ADMIN CUSTOMER DATA
        // =================================================

        DocumentSnapshot customerDocument =
                findCustomerDocument(
                        CUSTOMERS,
                        customerId
                );

        if (customerDocument != null
                &&
                customerDocument.exists()
                &&
                customerDocument.getData() != null) {

            result.putAll(
                    customerDocument.getData()
            );
        }

        // =================================================
        // REGISTERED USER DATA OVERRIDES
        // =================================================

        DocumentSnapshot userDocument =
                findCustomerDocument(
                        USERS,
                        customerId
                );

        if (userDocument != null
                &&
                userDocument.exists()
                &&
                userDocument.getData() != null) {

            result.putAll(
                    userDocument.getData()
            );
        }

        result.put(
                "userId",
                customerId
        );

        result.put(
                "customerId",
                customerId
        );

        result.put(
                "email",
                firstNonBlank(
                        stringValue(
                                result.get(
                                        "email"
                                )
                        ),
                        customerId
                )
        );

        if (clean(
                stringValue(
                        result.get(
                                "name"
                        )
                )
        ) == null) {

            result.put(
                    "name",
                    "Customer"
            );
        }

        return result;
    }

    // =====================================================
    // USER VEHICLES
    //
    // CANONICAL:
    //
    // vehicles/{vehicleId}
    //
    // customerId = customer@email
    //
    // Also migrates:
    //
    // users/{email}/vehicles
    // =====================================================

    public List<Map<String, Object>> getUserVehicles(
            String email
    ) throws Exception {

        String customerId =
                normalizeId(
                        email
                );

        List<Map<String, Object>> result =
                new ArrayList<>();

        if (customerId == null) {

            return result;
        }

        Set<String> added =
                new HashSet<>();

        // =================================================
        // CANONICAL TOP LEVEL
        //
        // Scan instead of only whereEqualTo so old aliases
        // and email case differences are supported.
        // =================================================

        QuerySnapshot snapshot =
                firestore
                        .collection(VEHICLES)
                        .get()
                        .get();

        for (DocumentSnapshot document :
                snapshot.getDocuments()) {

            if (!vehicleBelongsToCustomer(
                    document,
                    customerId
            )) {

                continue;
            }

            Map<String, Object> vehicle =
                    prepareVehicle(
                            document.getData(),
                            document.getId(),
                            customerId
                    );

            addVehicleIfMissing(
                    result,
                    added,
                    vehicle
            );
        }

        // =================================================
        // OLD NESTED VEHICLES
        // =================================================

        QuerySnapshot legacy =
                firestore
                        .collection(USERS)
                        .document(customerId)
                        .collection("vehicles")
                        .get()
                        .get();

        for (DocumentSnapshot document :
                legacy.getDocuments()) {

            Map<String, Object> vehicle =
                    prepareVehicle(
                            document.getData(),
                            document.getId(),
                            customerId
                    );

            addVehicleIfMissing(
                    result,
                    added,
                    vehicle
            );

            migrateLegacyVehicle(
                    document.getId(),
                    vehicle
            );
        }

        result.sort(
                (
                        first,
                        second
                ) ->
                        Long.compare(
                                getTimestamp(
                                        second
                                ),
                                getTimestamp(
                                        first
                                )
                        )
        );

        return result;
    }

    // =====================================================
    // ACTIVE SERVICE REQUESTS
    //
    // Pending
    // Assigned
    // Accepted
    // In Progress
    //
    // SAME serviceRequests collection.
    // =====================================================

    public List<Map<String, Object>> getActiveRequests(
            String email
    ) throws Exception {

        String customerId =
                normalizeId(
                        email
                );

        List<Map<String, Object>> result =
                new ArrayList<>();

        if (customerId == null) {

            return result;
        }

        Set<String> added =
                new HashSet<>();

        // =================================================
        // CANONICAL
        // =================================================

        QuerySnapshot snapshot =
                firestore
                        .collection(
                                SERVICE_REQUESTS
                        )
                        .get()
                        .get();

        for (DocumentSnapshot document :
                snapshot.getDocuments()) {

            if (!requestBelongsToCustomer(
                    document,
                    customerId
            )) {

                continue;
            }

            Map<String, Object> request =
                    prepareRequest(
                            document.getData(),
                            document.getId(),
                            customerId
                    );

            if (!isActiveStatus(
                    stringValue(
                            request.get(
                                    "status"
                            )
                    )
            )) {

                continue;
            }

            addRequestIfMissing(
                    result,
                    added,
                    request
            );
        }

        // =================================================
        // OLD NESTED REQUESTS
        //
        // users/{email}/serviceRequests
        // =================================================

        QuerySnapshot legacy =
                firestore
                        .collection(USERS)
                        .document(customerId)
                        .collection("serviceRequests")
                        .get()
                        .get();

        for (DocumentSnapshot document :
                legacy.getDocuments()) {

            Map<String, Object> request =
                    prepareRequest(
                            document.getData(),
                            document.getId(),
                            customerId
                    );

            migrateLegacyRequest(
                    document.getId(),
                    request
            );

            if (!isActiveStatus(
                    stringValue(
                            request.get(
                                    "status"
                            )
                    )
            )) {

                continue;
            }

            addRequestIfMissing(
                    result,
                    added,
                    request
            );
        }

        // =================================================
        // LATEST FIRST
        // =================================================

        result.sort(
                (
                        first,
                        second
                ) ->
                        Long.compare(
                                getTimestamp(
                                        second
                                ),
                                getTimestamp(
                                        first
                                )
                        )
        );

        return result;
    }

    // =====================================================
    // SERVICE HISTORY
    //
    // SAME serviceRequests collection.
    //
    // Completed
    // Cancelled
    // =====================================================

    public List<Map<String, Object>> getServiceHistory(
            String email
    ) throws Exception {

        String customerId =
                normalizeId(
                        email
                );

        List<Map<String, Object>> result =
                new ArrayList<>();

        if (customerId == null) {

            return result;
        }

        Set<String> added =
                new HashSet<>();

        // =================================================
        // CANONICAL SERVICE REQUESTS
        // =================================================

        QuerySnapshot snapshot =
                firestore
                        .collection(
                                SERVICE_REQUESTS
                        )
                        .get()
                        .get();

        for (DocumentSnapshot document :
                snapshot.getDocuments()) {

            if (!requestBelongsToCustomer(
                    document,
                    customerId
            )) {

                continue;
            }

            Map<String, Object> request =
                    prepareRequest(
                            document.getData(),
                            document.getId(),
                            customerId
                    );

            if (!isFinishedStatus(
                    stringValue(
                            request.get(
                                    "status"
                            )
                    )
            )) {

                continue;
            }

            prepareHistoryAliases(
                    request
            );

            addRequestIfMissing(
                    result,
                    added,
                    request
            );
        }

        // =================================================
        // OLD NESTED SERVICE REQUESTS
        // =================================================

        QuerySnapshot legacyRequests =
                firestore
                        .collection(USERS)
                        .document(customerId)
                        .collection("serviceRequests")
                        .get()
                        .get();

        for (DocumentSnapshot document :
                legacyRequests.getDocuments()) {

            Map<String, Object> request =
                    prepareRequest(
                            document.getData(),
                            document.getId(),
                            customerId
                    );

            migrateLegacyRequest(
                    document.getId(),
                    request
            );

            if (!isFinishedStatus(
                    stringValue(
                            request.get(
                                    "status"
                            )
                    )
            )) {

                continue;
            }

            prepareHistoryAliases(
                    request
            );

            addRequestIfMissing(
                    result,
                    added,
                    request
            );
        }

        // =================================================
        // VERY OLD HISTORY
        //
        // users/{email}/serviceHistory
        //
        // Migrated into serviceRequests.
        // =================================================

        QuerySnapshot oldHistory =
                firestore
                        .collection(USERS)
                        .document(customerId)
                        .collection("serviceHistory")
                        .get()
                        .get();

        for (DocumentSnapshot document :
                oldHistory.getDocuments()) {

            Map<String, Object> request =
                    prepareLegacyHistory(
                            document.getData(),
                            document.getId(),
                            customerId
                    );

            migrateLegacyRequest(
                    document.getId(),
                    request
            );

            prepareHistoryAliases(
                    request
            );

            addRequestIfMissing(
                    result,
                    added,
                    request
            );
        }

        // =================================================
        // LATEST FIRST
        // =================================================

        result.sort(
                (
                        first,
                        second
                ) ->
                        Long.compare(
                                getTimestamp(
                                        second
                                ),
                                getTimestamp(
                                        first
                                )
                        )
        );

        return result;
    }

    // =====================================================
    // AVAILABLE MECHANICS
    //
    // No fake distance / ETA / rating.
    //
    // Active mechanics only.
    // =====================================================

    public List<Map<String, Object>> getNearbyMechanics()
            throws Exception {

        List<Map<String, Object>> result =
                new ArrayList<>();

        QuerySnapshot snapshot =
                firestore
                        .collection(MECHANICS)
                        .get()
                        .get();

        for (DocumentSnapshot document :
                snapshot.getDocuments()) {

            if (document.getData() == null) {

                continue;
            }

            Map<String, Object> mechanic =
                    new HashMap<>(
                            document.getData()
                    );

            String status =
                    normalizeMechanicStatus(
                            stringValue(
                                    mechanic.get(
                                            "status"
                                    )
                            )
                    );

            if (!status.equalsIgnoreCase(
                    "Active"
            )) {

                continue;
            }

            String mechanicId =
                    normalizeId(
                            firstNonBlank(
                                    stringValue(
                                            mechanic.get(
                                                    "mechanicId"
                                            )
                                    ),
                                    stringValue(
                                            mechanic.get(
                                                    "email"
                                            )
                                    ),
                                    document.getId()
                            )
                    );

            mechanic.put(
                    "documentId",
                    document.getId()
            );

            mechanic.put(
                    "mechanicId",
                    mechanicId
            );

            mechanic.put(
                    "status",
                    "Active"
            );

            if (clean(
                    stringValue(
                            mechanic.get(
                                    "name"
                            )
                    )
            ) == null) {

                mechanic.put(
                        "name",
                        "Mechanic"
                );
            }

            if (clean(
                    stringValue(
                            mechanic.get(
                                    "specialization"
                            )
                    )
            ) == null) {

                mechanic.put(
                        "specialization",
                        "Automobile Service"
                );
            }

            result.add(
                    mechanic
            );
        }

        result.sort(
                (
                        first,
                        second
                ) ->
                        safeString(
                                first.get(
                                        "name"
                                )
                        )
                                .compareToIgnoreCase(
                                        safeString(
                                                second.get(
                                                        "name"
                                                )
                                        )
                                )
        );

        return result;
    }

    // =====================================================
    // VEHICLE HEALTH
    // =====================================================

    public Map<String, Object> getVehicleHealth(
            String email,
            String vehicleId
    ) throws Exception {

        String customerId =
                normalizeId(
                        email
                );

        vehicleId =
                clean(
                        vehicleId
                );

        Map<String, Object> result =
                new HashMap<>();

        if (customerId == null
                ||
                vehicleId == null) {

            return result;
        }

        List<Map<String, Object>> vehicles =
                getUserVehicles(
                        customerId
                );

        for (Map<String, Object> vehicle :
                vehicles) {

            String currentVehicleId =
                    firstNonBlank(
                            stringValue(
                                    vehicle.get(
                                            "vehicleId"
                                    )
                            ),
                            stringValue(
                                    vehicle.get(
                                            "id"
                                    )
                            ),
                            stringValue(
                                    vehicle.get(
                                            "documentId"
                                    )
                            )
                    );

            if (!sameId(
                    currentVehicleId,
                    vehicleId
            )) {

                continue;
            }

            Object health =
                    vehicle.get(
                            "health"
                    );

            if (health instanceof Map<?, ?>) {

                Map<?, ?> source =
                        (Map<?, ?>) health;

                for (Map.Entry<?, ?> entry :
                        source.entrySet()) {

                    if (entry.getKey() == null) {

                        continue;
                    }

                    result.put(
                            String.valueOf(
                                    entry.getKey()
                            ),
                            entry.getValue()
                    );
                }

                return result;
            }

            Object status =
                    vehicle.get(
                            "healthStatus"
                    );

            Object score =
                    vehicle.get(
                            "healthScore"
                    );

            if (status != null) {

                result.put(
                        "status",
                        status
                );
            }

            if (score != null) {

                result.put(
                        "score",
                        score
                );
            }

            return result;
        }

        return result;
    }

    // =====================================================
    // FIND CUSTOMER DOCUMENT
    // =====================================================

    private DocumentSnapshot findCustomerDocument(
            String collection,
            String customerId
    ) throws Exception {

        // =================================================
        // DIRECT DOCUMENT ID
        // =================================================

        DocumentSnapshot direct =
                firestore
                        .collection(collection)
                        .document(customerId)
                        .get()
                        .get();

        if (direct.exists()) {

            return direct;
        }

        // =================================================
        // FALLBACK SCAN
        // =================================================

        QuerySnapshot snapshot =
                firestore
                        .collection(collection)
                        .get()
                        .get();

        for (DocumentSnapshot document :
                snapshot.getDocuments()) {

            String candidate =
                    normalizeId(
                            firstNonBlank(
                                    stringValue(
                                            document.get(
                                                    "customerId"
                                            )
                                    ),
                                    stringValue(
                                            document.get(
                                                    "userId"
                                            )
                                    ),
                                    stringValue(
                                            document.get(
                                                    "email"
                                            )
                                    ),
                                    document.getId()
                            )
                    );

            if (sameId(
                    candidate,
                    customerId
            )) {

                return document;
            }
        }

        return null;
    }

    // =====================================================
    // VEHICLE OWNERSHIP
    // =====================================================

    private boolean vehicleBelongsToCustomer(
            DocumentSnapshot document,
            String customerId
    ) {

        if (document == null
                ||
                customerId == null) {

            return false;
        }

        String canonical =
                normalizeId(
                        stringValue(
                                document.get(
                                        "customerId"
                                )
                        )
                );

        String email =
                normalizeId(
                        stringValue(
                                document.get(
                                        "userEmail"
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

        return sameId(
                canonical,
                customerId
        )
                ||
                sameId(
                        email,
                        customerId
                )
                ||
                sameId(
                        userId,
                        customerId
                );
    }

    // =====================================================
    // REQUEST OWNERSHIP
    // =====================================================

    private boolean requestBelongsToCustomer(
            DocumentSnapshot document,
            String customerId
    ) {

        if (document == null
                ||
                customerId == null) {

            return false;
        }

        String canonical =
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
                canonical,
                customerId
        )
                ||
                sameId(
                        userId,
                        customerId
                )
                ||
                sameId(
                        userEmail,
                        customerId
                );
    }

    // =====================================================
    // PREPARE VEHICLE
    // =====================================================

    private Map<String, Object> prepareVehicle(
            Map<String, Object> original,
            String documentId,
            String customerId
    ) {

        Map<String, Object> vehicle =
                original == null
                        ? new HashMap<>()
                        : new HashMap<>(
                                original
                        );

        String vehicleId =
                firstNonBlank(
                        stringValue(
                                vehicle.get(
                                        "vehicleId"
                                )
                        ),
                        stringValue(
                                vehicle.get(
                                        "id"
                                )
                        ),
                        documentId
                );

        String brand =
                firstNonBlank(
                        stringValue(
                                vehicle.get(
                                        "brand"
                                )
                        ),
                        stringValue(
                                vehicle.get(
                                        "make"
                                )
                        )
                );

        String model =
                stringValue(
                        vehicle.get(
                                "model"
                        )
                );

        String vehicleNumber =
                firstNonBlank(
                        stringValue(
                                vehicle.get(
                                        "vehicleNumber"
                                )
                        ),
                        stringValue(
                                vehicle.get(
                                        "registrationNumber"
                                )
                        ),
                        stringValue(
                                vehicle.get(
                                        "registration"
                                )
                        ),
                        stringValue(
                                vehicle.get(
                                        "number"
                                )
                        )
                );

        String displayName =
                firstNonBlank(
                        stringValue(
                                vehicle.get(
                                        "name"
                                )
                        ),
                        join(
                                brand,
                                model
                        ),
                        model,
                        brand,
                        "Vehicle"
                );

        vehicle.put(
                "documentId",
                documentId
        );

        vehicle.put(
                "vehicleId",
                vehicleId
        );

        vehicle.put(
                "id",
                vehicleId
        );

        vehicle.put(
                "customerId",
                customerId
        );

        vehicle.put(
                "userEmail",
                customerId
        );

        vehicle.put(
                "name",
                displayName
        );

        if (brand != null) {

            vehicle.put(
                    "brand",
                    brand
            );

            vehicle.put(
                    "make",
                    brand
            );
        }

        if (vehicleNumber != null) {

            vehicle.put(
                    "vehicleNumber",
                    vehicleNumber
            );

            vehicle.put(
                    "registrationNumber",
                    vehicleNumber
            );

            vehicle.put(
                    "number",
                    vehicleNumber
            );
        }

        return vehicle;
    }

    // =====================================================
    // PREPARE SERVICE REQUEST
    // =====================================================

    private Map<String, Object> prepareRequest(
            Map<String, Object> original,
            String documentId,
            String customerId
    ) {

        Map<String, Object> request =
                original == null
                        ? new HashMap<>()
                        : new HashMap<>(
                                original
                        );

        String requestId =
                firstNonBlank(
                        stringValue(
                                request.get(
                                        "requestId"
                                )
                        ),
                        documentId
                );

        String actualCustomerId =
                normalizeId(
                        firstNonBlank(
                                stringValue(
                                        request.get(
                                                "customerId"
                                        )
                                ),
                                stringValue(
                                        request.get(
                                                "userEmail"
                                        )
                                ),
                                stringValue(
                                        request.get(
                                                "userId"
                                        )
                                ),
                                customerId
                        )
                );

        String customerName =
                firstNonBlank(
                        stringValue(
                                request.get(
                                        "customerName"
                                )
                        ),
                        stringValue(
                                request.get(
                                        "userName"
                                )
                        ),
                        "Customer"
                );

        String serviceType =
                firstNonBlank(
                        stringValue(
                                request.get(
                                        "serviceType"
                                )
                        ),
                        stringValue(
                                request.get(
                                        "service"
                                )
                        ),
                        stringValue(
                                request.get(
                                        "serviceName"
                                )
                        ),
                        stringValue(
                                request.get(
                                        "type"
                                )
                        ),
                        "Vehicle Service"
                );

        String mechanicId =
                normalizeId(
                        firstNonBlank(
                                stringValue(
                                        request.get(
                                                "mechanicId"
                                        )
                                ),
                                stringValue(
                                        request.get(
                                                "assignedMechanicId"
                                        )
                                ),
                                stringValue(
                                        request.get(
                                                "mechanicEmail"
                                        )
                                )
                        )
                );

        String mechanicName =
                firstNonBlank(
                        stringValue(
                                request.get(
                                        "mechanicName"
                                )
                        ),
                        stringValue(
                                request.get(
                                        "assignedMechanicName"
                                )
                        ),
                        mechanicId == null
                                ? "Mechanic not assigned"
                                : mechanicId
                );

        String vehicleNumber =
                firstNonBlank(
                        stringValue(
                                request.get(
                                        "vehicleNumber"
                                )
                        ),
                        stringValue(
                                request.get(
                                        "registrationNumber"
                                )
                        ),
                        stringValue(
                                request.get(
                                        "vehicleNo"
                                )
                        )
                );

        String status =
                normalizeRequestStatus(
                        stringValue(
                                request.get(
                                        "status"
                                )
                        )
                );

        request.put(
                "documentId",
                documentId
        );

        request.put(
                "requestId",
                requestId
        );

        request.put(
                "customerId",
                actualCustomerId
        );

        request.put(
                "userId",
                firstNonBlank(
                        stringValue(
                                request.get(
                                        "userId"
                                )
                        ),
                        actualCustomerId
                )
        );

        request.put(
                "userEmail",
                firstNonBlank(
                        stringValue(
                                request.get(
                                        "userEmail"
                                )
                        ),
                        actualCustomerId
                )
        );

        request.put(
                "customerName",
                customerName
        );

        request.put(
                "userName",
                customerName
        );

        request.put(
                "serviceType",
                serviceType
        );

        request.put(
                "service",
                serviceType
        );

        request.put(
                "status",
                status
        );

        if (mechanicId != null) {

            request.put(
                    "mechanicId",
                    mechanicId
            );
        }

        request.put(
                "mechanicName",
                mechanicName
        );

        if (vehicleNumber != null) {

            request.put(
                    "vehicleNumber",
                    vehicleNumber
            );

            request.put(
                    "registrationNumber",
                    vehicleNumber
            );
        }

        return request;
    }

    // =====================================================
    // VERY OLD HISTORY
    // =====================================================

    private Map<String, Object> prepareLegacyHistory(
            Map<String, Object> original,
            String documentId,
            String customerId
    ) {

        Map<String, Object> request =
                prepareRequest(
                        original,
                        documentId,
                        customerId
                );

        if (!isFinishedStatus(
                stringValue(
                        request.get(
                                "status"
                        )
                )
        )) {

            request.put(
                    "status",
                    "Completed"
            );
        }

        if (firstValue(
                request,
                "completedDate",
                "cancelledDate"
        ) == null) {

            Object date =
                    firstValue(
                            request,
                            "serviceDate",
                            "date",
                            "updatedAt",
                            "createdAt"
                    );

            if (date != null) {

                request.put(
                        "completedDate",
                        date
                );
            }
        }

        return request;
    }

    // =====================================================
    // DASHBOARD HISTORY ALIASES
    //
    // Existing UserDashboard expects:
    //
    // title
    // date
    // amount
    // =====================================================

    private void prepareHistoryAliases(
            Map<String, Object> request
    ) {

        if (request == null) {

            return;
        }

        String status =
                normalizeRequestStatus(
                        stringValue(
                                request.get(
                                        "status"
                                )
                        )
                );

        String title =
                firstNonBlank(
                        stringValue(
                                request.get(
                                        "serviceType"
                                )
                        ),
                        stringValue(
                                request.get(
                                        "service"
                                )
                        ),
                        "Vehicle Service"
                );

        Object date =
                firstValue(
                        request,
                        "completedDate",
                        "cancelledDate",
                        "updatedAt",
                        "requestDate",
                        "serviceDate",
                        "date",
                        "createdAt"
                );

        request.put(
                "title",
                title
        );

        request.put(
                "date",
                formatDate(
                        date
                )
        );

        request.put(
                "serviceDate",
                formatDate(
                        date
                )
        );

        String amount;

        if ("Cancelled".equalsIgnoreCase(
                status
        )) {

            amount =
                    "0";

        } else {

            amount =
                    resolveFinalAmount(
                            request
                    );
        }

        request.put(
                "amount",
                amount
        );

        request.put(
                "cost",
                amount
        );
    }

    // =====================================================
    // RESOLVE FINAL AMOUNT
    // =====================================================

    private String resolveFinalAmount(
            Map<String, Object> request
    ) {

        Object finalCost =
                firstValue(
                        request,
                        "finalCost",
                        "totalCost",
                        "cost",
                        "amount"
                );

        double finalAmount =
                parseMoney(
                        finalCost
                );

        if (finalAmount > 0) {

            return formatPlainMoney(
                    finalAmount
            );
        }

        double parts =
                parseMoney(
                        request.get(
                                "partsCost"
                        )
                );

        double labour =
                parseMoney(
                        firstValue(
                                request,
                                "labourCost",
                                "laborCost"
                        )
                );

        double calculated =
                parts + labour;

        if (calculated > 0) {

            return formatPlainMoney(
                    calculated
            );
        }

        return formatPlainMoney(
                parseMoney(
                        request.get(
                                "estimatedCost"
                        )
                )
        );
    }

    // =====================================================
    // MIGRATE LEGACY VEHICLE
    // =====================================================

    private void migrateLegacyVehicle(
            String documentId,
            Map<String, Object> vehicle
    ) {

        documentId =
                clean(
                        documentId
                );

        if (documentId == null
                ||
                vehicle == null) {

            return;
        }

        try {

            DocumentSnapshot existing =
                    firestore
                            .collection(VEHICLES)
                            .document(documentId)
                            .get()
                            .get();

            if (!existing.exists()) {

                firestore
                        .collection(VEHICLES)
                        .document(documentId)
                        .set(
                                vehicle,
                                SetOptions.merge()
                        )
                        .get();
            }

        } catch (Exception e) {

            System.err.println(
                    "Vehicle migration skipped: "
                            + e.getMessage()
            );
        }
    }

    // =====================================================
    // MIGRATE LEGACY SERVICE REQUEST
    // =====================================================

    private void migrateLegacyRequest(
            String documentId,
            Map<String, Object> request
    ) {

        documentId =
                clean(
                        documentId
                );

        if (documentId == null
                ||
                request == null) {

            return;
        }

        try {

            DocumentSnapshot existing =
                    firestore
                            .collection(
                                    SERVICE_REQUESTS
                            )
                            .document(documentId)
                            .get()
                            .get();

            if (!existing.exists()) {

                firestore
                        .collection(
                                SERVICE_REQUESTS
                        )
                        .document(documentId)
                        .set(
                                request,
                                SetOptions.merge()
                        )
                        .get();
            }

        } catch (Exception e) {

            System.err.println(
                    "Service request migration skipped: "
                            + e.getMessage()
            );
        }
    }

    // =====================================================
    // ADD VEHICLE WITHOUT DUPLICATE
    // =====================================================

    private void addVehicleIfMissing(
            List<Map<String, Object>> vehicles,
            Set<String> ids,
            Map<String, Object> vehicle
    ) {

        if (vehicle == null) {

            return;
        }

        String id =
                firstNonBlank(
                        stringValue(
                                vehicle.get(
                                        "vehicleId"
                                )
                        ),
                        stringValue(
                                vehicle.get(
                                        "id"
                                )
                        ),
                        stringValue(
                                vehicle.get(
                                        "documentId"
                                )
                        )
                );

        id =
                normalizeId(
                        id
                );

        if (id == null) {

            return;
        }

        if (!ids.add(
                id
        )) {

            return;
        }

        vehicles.add(
                vehicle
        );
    }

    // =====================================================
    // ADD REQUEST WITHOUT DUPLICATE
    // =====================================================

    private void addRequestIfMissing(
            List<Map<String, Object>> requests,
            Set<String> ids,
            Map<String, Object> request
    ) {

        if (request == null) {

            return;
        }

        String id =
                firstNonBlank(
                        stringValue(
                                request.get(
                                        "requestId"
                                )
                        ),
                        stringValue(
                                request.get(
                                        "documentId"
                                )
                        )
                );

        if (id == null) {

            return;
        }

        if (!ids.add(
                id
        )) {

            return;
        }

        requests.add(
                request
        );
    }

    // =====================================================
    // ACTIVE STATUS
    // =====================================================

    private boolean isActiveStatus(
            String status
    ) {

        String normalized =
                normalizeRequestStatus(
                        status
                );

        return normalized.equalsIgnoreCase(
                "Pending"
        )
                ||
                normalized.equalsIgnoreCase(
                        "Assigned"
                )
                ||
                normalized.equalsIgnoreCase(
                        "Accepted"
                )
                ||
                normalized.equalsIgnoreCase(
                        "In Progress"
                );
    }

    // =====================================================
    // FINISHED STATUS
    // =====================================================

    private boolean isFinishedStatus(
            String status
    ) {

        String normalized =
                normalizeRequestStatus(
                        status
                );

        return normalized.equalsIgnoreCase(
                "Completed"
        )
                ||
                normalized.equalsIgnoreCase(
                        "Cancelled"
                );
    }

    // =====================================================
    // NORMALIZE REQUEST STATUS
    // =====================================================

    private String normalizeRequestStatus(
            String status
    ) {

        status =
                clean(
                        status
                );

        if (status == null) {

            return "Pending";
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
                        ||
                status.equalsIgnoreCase(
                        "Closed"
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
                        ||
                status.equalsIgnoreCase(
                        "Rejected"
                )
        ) {

            return "Cancelled";
        }

        return status;
    }

    // =====================================================
    // MECHANIC STATUS
    // =====================================================

    private String normalizeMechanicStatus(
            String status
    ) {

        status =
                clean(
                        status
                );

        /*
         * Registered mechanics without explicit status are
         * treated as active.
         */
        if (status == null) {

            return "Active";
        }

        if (
                status.equalsIgnoreCase(
                        "Inactive"
                )
                        ||
                status.equalsIgnoreCase(
                        "Blocked"
                )
                        ||
                status.equalsIgnoreCase(
                        "Disabled"
                )
        ) {

            return "Inactive";
        }

        return "Active";
    }

    // =====================================================
    // TIMESTAMP
    // =====================================================

    private long getTimestamp(
            Map<String, Object> data
    ) {

        if (data == null) {

            return 0L;
        }

        Object value =
                firstValue(
                        data,
                        "completedDate",
                        "cancelledDate",
                        "updatedAt",
                        "startedDate",
                        "acceptedDate",
                        "assignedDate",
                        "requestDate",
                        "createdAt",
                        "date"
                );

        return toMillis(
                value
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

            if (
                    number > 0
                            &&
                    number < 100000000000L
            ) {

                number *=
                        1000L;
            }

            return number;
        }

        if (value instanceof Date) {

            return ((Date) value)
                    .getTime();
        }

        if (value instanceof Timestamp) {

            return ((Timestamp) value)
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

            if (
                    number > 0
                            &&
                    number < 100000000000L
            ) {

                number *=
                        1000L;
            }

            return number;

        } catch (Exception e) {

            return 0L;
        }
    }

    // =====================================================
    // FORMAT DATE
    // =====================================================

    private String formatDate(
            Object value
    ) {

        if (value == null) {

            return "";
        }

        long millis =
                toMillis(
                        value
                );

        if (millis <= 0) {

            return String.valueOf(
                    value
            );
        }

        try {

            DateTimeFormatter formatter =
                    DateTimeFormatter
                            .ofPattern(
                                    "dd MMM yyyy"
                            )
                            .withZone(
                                    ZoneId.systemDefault()
                            );

            return formatter.format(
                    Instant.ofEpochMilli(
                            millis
                    )
            );

        } catch (Exception e) {

            return String.valueOf(
                    value
            );
        }
    }

    // =====================================================
    // MONEY
    // =====================================================

    private double parseMoney(
            Object value
    ) {

        if (value == null) {

            return 0.0;
        }

        if (value instanceof Number) {

            double number =
                    ((Number) value)
                            .doubleValue();

            return Math.max(
                    number,
                    0.0
            );
        }

        String text =
                String.valueOf(
                        value
                )
                        .replace(
                                "₹",
                                ""
                        )
                        .replace(
                                ",",
                                ""
                        )
                        .trim();

        if (text.isEmpty()) {

            return 0.0;
        }

        try {

            double number =
                    Double.parseDouble(
                            text
                    );

            if (!Double.isFinite(
                    number
            )
                    ||
                    number < 0) {

                return 0.0;
            }

            return number;

        } catch (Exception e) {

            return 0.0;
        }
    }

    // =====================================================
    // FORMAT PLAIN MONEY
    //
    // Existing dashboard itself adds ₹.
    // =====================================================

    private String formatPlainMoney(
            double value
    ) {

        if (!Double.isFinite(
                value
        )
                ||
                value < 0) {

            value =
                    0.0;
        }

        if (value == Math.rint(
                value
        )) {

            return String.valueOf(
                    (long) value
            );
        }

        return String.format(
                java.util.Locale.US,
                "%.2f",
                value
        );
    }

    // =====================================================
    // FIRST MAP VALUE
    // =====================================================

    private Object firstValue(
            Map<String, Object> map,
            String... keys
    ) {

        if (map == null
                ||
                keys == null) {

            return null;
        }

        for (String key :
                keys) {

            Object value =
                    map.get(
                            key
                    );

            if (value == null) {

                continue;
            }

            if (value instanceof String
                    &&
                    ((String) value)
                            .trim()
                            .isEmpty()) {

                continue;
            }

            return value;
        }

        return null;
    }

    // =====================================================
    // JOIN
    // =====================================================

    private String join(
            String first,
            String second
    ) {

        first =
                clean(
                        first
                );

        second =
                clean(
                        second
                );

        if (first == null) {

            return second;
        }

        if (second == null) {

            return first;
        }

        return first
                + " "
                + second;
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
    // SAFE STRING
    // =====================================================

    private String safeString(
            Object value
    ) {

        String result =
                stringValue(
                        value
                );

        return result == null
                ? ""
                : result;
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