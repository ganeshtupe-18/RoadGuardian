package project.dao.user;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;

import project.firebase.FirebaseConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VehicleDAO {

    // =====================================================
    // CANONICAL FIRESTORE COLLECTIONS
    // =====================================================

    private static final String VEHICLE_COLLECTION =
            "vehicles";

    private static final String SERVICE_REQUEST_COLLECTION =
            "serviceRequests";

    private static final String USER_COLLECTION =
            "users";

    private static final String LEGACY_VEHICLE_COLLECTION =
            "vehicles";

    private static final String LEGACY_HISTORY_COLLECTION =
            "serviceHistory";

    private final Firestore firestore;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public VehicleDAO() {

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
    // GET ALL VEHICLES FOR CURRENT CUSTOMER
    // =====================================================

    public List<Map<String, Object>> getVehicles(
            String userEmail
    ) throws Exception {

        List<Map<String, Object>> vehicles =
                new ArrayList<>();

        String customerId =
                normalizeEmail(
                        userEmail
                );

        if (customerId == null) {

            return vehicles;
        }

        /*
         * Old project data:
         *
         * users/{email}/vehicles
         *
         * जर तिथे जुना data असेल तर top-level vehicles
         * मध्ये migrate करण्याचा प्रयत्न करतो.
         */
        migrateLegacyVehicles(
                customerId
        );

        // =================================================
        // READ TOP LEVEL vehicles
        // =================================================

        QuerySnapshot result =
                firestore
                        .collection(
                                VEHICLE_COLLECTION
                        )
                        .get()
                        .get();

        for (DocumentSnapshot document :
                result.getDocuments()) {

            if (!belongsToCustomer(
                    document,
                    customerId
            )) {

                continue;
            }

            Map<String, Object> data =
                    createVehicleMapForUser(
                            document
                    );

            if (data != null) {

                vehicles.add(
                        data
                );
            }
        }

        return vehicles;
    }

    // =====================================================
    // ADD VEHICLE
    // CUSTOMER
    // =====================================================

    public String addVehicle(
            String userEmail,
            Map<String, Object> vehicleData
    ) throws Exception {

        String customerId =
                normalizeEmail(
                        userEmail
                );

        if (customerId == null) {

            throw new IllegalArgumentException(
                    "User email is required."
            );
        }

        if (vehicleData == null
                ||
                vehicleData.isEmpty()) {

            throw new IllegalArgumentException(
                    "Vehicle data is required."
            );
        }

        // =================================================
        // CREATE TOP LEVEL DOCUMENT
        // =================================================

        DocumentReference document =
                firestore
                        .collection(
                                VEHICLE_COLLECTION
                        )
                        .document();

        String vehicleId =
                document.getId();

        Map<String, Object> finalData =
                normalizeVehicleData(
                        customerId,
                        vehicleId,
                        vehicleData,
                        true
                );

        document
                .set(
                        finalData
                )
                .get();

        System.out.println(
                "Vehicle created: vehicles/"
                        + vehicleId
        );

        System.out.println(
                "Customer ID: "
                        + customerId
        );

        return vehicleId;
    }

    // =====================================================
    // UPDATE VEHICLE
    // CUSTOMER
    // =====================================================

    public void updateVehicle(
            String userEmail,
            String vehicleId,
            Map<String, Object> vehicleData
    ) throws Exception {

        String customerId =
                normalizeEmail(
                        userEmail
                );

        vehicleId =
                clean(
                        vehicleId
                );

        if (customerId == null) {

            throw new IllegalArgumentException(
                    "User email is required."
            );
        }

        if (vehicleId == null) {

            throw new IllegalArgumentException(
                    "Vehicle ID is required."
            );
        }

        if (vehicleData == null
                ||
                vehicleData.isEmpty()) {

            return;
        }

        DocumentReference document =
                firestore
                        .collection(
                                VEHICLE_COLLECTION
                        )
                        .document(
                                vehicleId
                        );

        DocumentSnapshot existing =
                document
                        .get()
                        .get();

        // =================================================
        // LEGACY FALLBACK
        // =================================================

        if (!existing.exists()) {

            boolean migrated =
                    migrateSingleLegacyVehicle(
                            customerId,
                            vehicleId
                    );

            if (migrated) {

                existing =
                        document
                                .get()
                                .get();
            }
        }

        if (!existing.exists()) {

            throw new IllegalArgumentException(
                    "Vehicle not found."
            );
        }

        // =================================================
        // OWNERSHIP CHECK
        // =================================================

        if (!belongsToCustomer(
                existing,
                customerId
        )) {

            throw new IllegalArgumentException(
                    "This vehicle does not belong to the current customer."
            );
        }

        Map<String, Object> finalData =
                normalizeVehicleData(
                        customerId,
                        vehicleId,
                        vehicleData,
                        false
                );

        document
                .set(
                        finalData,
                        SetOptions.merge()
                )
                .get();
    }

    // =====================================================
    // DELETE VEHICLE
    // CUSTOMER
    // =====================================================

    public void deleteVehicle(
            String userEmail,
            String vehicleId
    ) throws Exception {

        String customerId =
                normalizeEmail(
                        userEmail
                );

        vehicleId =
                clean(
                        vehicleId
                );

        if (customerId == null
                ||
                vehicleId == null) {

            return;
        }

        DocumentReference document =
                firestore
                        .collection(
                                VEHICLE_COLLECTION
                        )
                        .document(
                                vehicleId
                        );

        DocumentSnapshot existing =
                document
                        .get()
                        .get();

        if (existing.exists()) {

            if (!belongsToCustomer(
                    existing,
                    customerId
            )) {

                throw new IllegalArgumentException(
                        "This vehicle does not belong to the current customer."
                );
            }

            document
                    .delete()
                    .get();
        }

        /*
         * Old nested copy असेल तर तीही delete.
         */
        DocumentReference legacyDocument =
                firestore
                        .collection(
                                USER_COLLECTION
                        )
                        .document(
                                customerId
                        )
                        .collection(
                                LEGACY_VEHICLE_COLLECTION
                        )
                        .document(
                                vehicleId
                        );

        DocumentSnapshot legacySnapshot =
                legacyDocument
                        .get()
                        .get();

        if (legacySnapshot.exists()) {

            legacyDocument
                    .delete()
                    .get();
        }
    }

    // =====================================================
    // GET SINGLE VEHICLE
    // =====================================================

    public Map<String, Object> getVehicle(
            String userEmail,
            String vehicleId
    ) throws Exception {

        String customerId =
                normalizeEmail(
                        userEmail
                );

        vehicleId =
                clean(
                        vehicleId
                );

        if (customerId == null
                ||
                vehicleId == null) {

            return null;
        }

        DocumentReference document =
                firestore
                        .collection(
                                VEHICLE_COLLECTION
                        )
                        .document(
                                vehicleId
                        );

        DocumentSnapshot snapshot =
                document
                        .get()
                        .get();

        // =================================================
        // OLD NESTED VEHICLE
        // =================================================

        if (!snapshot.exists()) {

            boolean migrated =
                    migrateSingleLegacyVehicle(
                            customerId,
                            vehicleId
                    );

            if (migrated) {

                snapshot =
                        document
                                .get()
                                .get();
            }
        }

        if (!snapshot.exists()) {

            return null;
        }

        if (!belongsToCustomer(
                snapshot,
                customerId
        )) {

            return null;
        }

        return createVehicleMapForUser(
                snapshot
        );
    }

    // =====================================================
    // GET SERVICE HISTORY
    //
    // OLD:
    // users/email/vehicles/id/serviceHistory
    //
    // NEW:
    // serviceRequests
    // customerId = user
    // vehicleId  = selected vehicle
    // status     = Completed
    // =====================================================

    public List<Map<String, Object>> getServiceHistory(
            String userEmail,
            String vehicleId
    ) throws Exception {

        List<Map<String, Object>> history =
                new ArrayList<>();

        String customerId =
                normalizeEmail(
                        userEmail
                );

        vehicleId =
                clean(
                        vehicleId
                );

        if (customerId == null
                ||
                vehicleId == null) {

            return history;
        }

        /*
         * जुनी manually stored service history असेल
         * तर ती canonical serviceRequests मध्ये आणतो.
         */
        migrateLegacyServiceHistory(
                customerId,
                vehicleId
        );

        QuerySnapshot result =
                firestore
                        .collection(
                                SERVICE_REQUEST_COLLECTION
                        )
                        .get()
                        .get();

        for (DocumentSnapshot document :
                result.getDocuments()) {

            String requestCustomerId =
                    stringValue(
                            document.get(
                                    "customerId"
                            )
                    );

            String requestVehicleId =
                    stringValue(
                            document.get(
                                    "vehicleId"
                            )
                    );

            String status =
                    stringValue(
                            document.get(
                                    "status"
                            )
                    );

            if (!sameCustomer(
                    requestCustomerId,
                    customerId
            )) {

                continue;
            }

            if (requestVehicleId == null
                    ||
                    !requestVehicleId.equals(
                            vehicleId
                    )) {

                continue;
            }

            /*
             * Service History = finished service.
             */
            if (status == null
                    ||
                    !status.equalsIgnoreCase(
                            "Completed"
                    )) {

                continue;
            }

            Map<String, Object> data =
                    createServiceHistoryMap(
                            document
                    );

            if (data != null) {

                history.add(
                        data
                );
            }
        }

        return history;
    }

    // =====================================================
    // ADD SERVICE HISTORY
    //
    // Existing VehiclePage signature कायम.
    //
    // आता separate nested serviceHistory तयार न करता
    // Completed ServiceRequest तयार होईल.
    // =====================================================

    public String addServiceHistory(
            String userEmail,
            String vehicleId,
            Map<String, Object> serviceData
    ) throws Exception {

        String customerId =
                normalizeEmail(
                        userEmail
                );

        vehicleId =
                clean(
                        vehicleId
                );

        if (customerId == null) {

            throw new IllegalArgumentException(
                    "User email is required."
            );
        }

        if (vehicleId == null) {

            throw new IllegalArgumentException(
                    "Vehicle ID is required."
            );
        }

        if (serviceData == null) {

            serviceData =
                    new HashMap<>();
        }

        /*
         * Vehicle खरंच current user ची आहे का?
         */
        Map<String, Object> vehicle =
                getVehicle(
                        customerId,
                        vehicleId
                );

        if (vehicle == null) {

            throw new IllegalArgumentException(
                    "Vehicle not found."
            );
        }

        DocumentReference document =
                firestore
                        .collection(
                                SERVICE_REQUEST_COLLECTION
                        )
                        .document();

        String requestId =
                document.getId();

        Map<String, Object> data =
                new HashMap<>(
                        serviceData
                );

        data.put(
                "requestId",
                requestId
        );

        data.put(
                "customerId",
                customerId
        );

        data.put(
                "vehicleId",
                vehicleId
        );

        // =================================================
        // CUSTOMER NAME
        // =================================================

        if (isBlankValue(
                data.get(
                        "customerName"
                )
        )) {

            String customerName =
                    getCustomerName(
                            customerId
                    );

            data.put(
                    "customerName",
                    customerName
            );
        }

        // =================================================
        // VEHICLE NUMBER
        // =================================================

        if (isBlankValue(
                data.get(
                        "vehicleNumber"
                )
        )) {

            Object vehicleNumber =
                    firstValue(
                            vehicle,
                            "vehicleNumber",
                            "registrationNumber"
                    );

            data.put(
                    "vehicleNumber",
                    vehicleNumber == null
                            ? ""
                            : vehicleNumber
            );
        }

        // =================================================
        // SERVICE TYPE ALIAS
        // =================================================

        if (isBlankValue(
                data.get(
                        "serviceType"
                )
        )) {

            Object service =
                    firstValue(
                            data,
                            "service",
                            "type",
                            "serviceName"
                    );

            data.put(
                    "serviceType",
                    service == null
                            ? "Vehicle Service"
                            : service
            );
        }

        // =================================================
        // STATUS
        // =================================================

        data.put(
                "status",
                "Completed"
        );

        // =================================================
        // REQUEST DATE
        // =================================================

        if (isBlankValue(
                data.get(
                        "requestDate"
                )
        )) {

            Object date =
                    firstValue(
                            data,
                            "date",
                            "serviceDate"
                    );

            data.put(
                    "requestDate",
                    date == null
                            ? currentTime()
                            : date
            );
        }

        // =================================================
        // COMPLETED DATE
        // =================================================

        if (isBlankValue(
                data.get(
                        "completedDate"
                )
        )) {

            data.put(
                    "completedDate",
                    currentTime()
            );
        }

        document
                .set(
                        data
                )
                .get();

        return requestId;
    }

    // =====================================================
    // VEHICLE HEALTH
    // =====================================================

    public void updateVehicleHealth(
            String userEmail,
            String vehicleId,
            int health
    ) throws Exception {

        Map<String, Object> data =
                new HashMap<>();

        data.put(
                "health",
                health
        );

        updateVehicle(
                userEmail,
                vehicleId,
                data
        );
    }

    // =====================================================
    // BATTERY
    // =====================================================

    public void updateBattery(
            String userEmail,
            String vehicleId,
            int percentage,
            String batteryName,
            String installationDate
    ) throws Exception {

        Map<String, Object> data =
                new HashMap<>();

        data.put(
                "batteryPercentage",
                percentage
        );

        data.put(
                "batteryName",
                batteryName == null
                        ? ""
                        : batteryName
        );

        data.put(
                "batteryInstallationDate",
                installationDate == null
                        ? ""
                        : installationDate
        );

        updateVehicle(
                userEmail,
                vehicleId,
                data
        );
    }

    // =====================================================
    // TYRES
    // =====================================================

    public void updateTyres(
            String userEmail,
            String vehicleId,
            Map<String, Object> tyreData
    ) throws Exception {

        if (tyreData == null) {

            return;
        }

        updateVehicle(
                userEmail,
                vehicleId,
                tyreData
        );
    }

    // =====================================================
    // NORMALIZE VEHICLE DATA BEFORE FIRESTORE SAVE
    // =====================================================

    private Map<String, Object> normalizeVehicleData(
            String customerId,
            String vehicleId,
            Map<String, Object> source,
            boolean newVehicle
    ) {

        Map<String, Object> data =
                new HashMap<>();

        if (source != null) {

            data.putAll(
                    source
            );
        }

        // =================================================
        // COMMON IDs
        // =================================================

        data.put(
                "vehicleId",
                vehicleId
        );

        /*
         * Existing VehiclePage expects id.
         */
        data.put(
                "id",
                vehicleId
        );

        data.put(
                "customerId",
                customerId
        );

        data.put(
                "userEmail",
                customerId
        );

        // =================================================
        // VEHICLE NUMBER
        //
        // USER LEGACY:
        // registrationNumber
        //
        // ADMIN:
        // vehicleNumber
        // =================================================

        String vehicleNumber =
                firstString(
                        data,
                        "vehicleNumber",
                        "registrationNumber"
                );

        if (vehicleNumber != null) {

            vehicleNumber =
                    vehicleNumber
                            .toUpperCase();

            data.put(
                    "vehicleNumber",
                    vehicleNumber
            );

            data.put(
                    "registrationNumber",
                    vehicleNumber
            );
        }

        // =================================================
        // BRAND
        //
        // USER LEGACY: name
        // ADMIN: brand
        // =================================================

        String brand =
                firstString(
                        data,
                        "brand",
                        "name",
                        "make"
                );

        if (brand != null) {

            data.put(
                    "brand",
                    brand
            );

            /*
             * Existing VehiclePage compatibility.
             */
            if (isBlankValue(
                    data.get(
                            "name"
                    )
            )) {

                data.put(
                        "name",
                        brand
                );
            }
        }

        // =================================================
        // OWNER
        //
        // USER: owner
        // ADMIN: ownerName
        // =================================================

        String ownerName =
                firstString(
                        data,
                        "ownerName",
                        "owner"
                );

        if (ownerName != null) {

            data.put(
                    "ownerName",
                    ownerName
            );

            data.put(
                    "owner",
                    ownerName
            );
        }

        // =================================================
        // VEHICLE TYPE
        // =================================================

        String vehicleType =
                firstString(
                        data,
                        "vehicleType",
                        "type"
                );

        if (vehicleType != null) {

            data.put(
                    "vehicleType",
                    vehicleType
            );
        }

        // =================================================
        // STATUS
        // =================================================

        if (isBlankValue(
                data.get(
                        "status"
                )
        )) {

            data.put(
                    "status",
                    "Active"
            );
        }

        // =================================================
        // CREATED TIME
        // =================================================

        if (newVehicle
                &&
                isBlankValue(
                        data.get(
                                "createdAt"
                        )
                )) {

            data.put(
                    "createdAt",
                    currentTime()
            );
        }

        // =================================================
        // UPDATED TIME
        // =================================================

        data.put(
                "updatedAt",
                currentTime()
        );

        return data;
    }

    // =====================================================
    // DOCUMENT -> USER VEHICLE MAP
    // =====================================================

    private Map<String, Object> createVehicleMapForUser(
            DocumentSnapshot document
    ) {

        if (document == null
                ||
                !document.exists()
                ||
                document.getData() == null) {

            return null;
        }

        Map<String, Object> data =
                new HashMap<>(
                        document.getData()
                );

        String vehicleId =
                document.getId();

        data.put(
                "id",
                vehicleId
        );

        data.put(
                "vehicleId",
                vehicleId
        );

        // =================================================
        // ADMIN -> USER FIELD ALIASES
        // =================================================

        String vehicleNumber =
                firstString(
                        data,
                        "vehicleNumber",
                        "registrationNumber"
                );

        if (vehicleNumber != null) {

            data.put(
                    "vehicleNumber",
                    vehicleNumber
            );

            data.put(
                    "registrationNumber",
                    vehicleNumber
            );
        }

        String brand =
                firstString(
                        data,
                        "brand",
                        "name",
                        "make"
                );

        if (brand != null) {

            data.put(
                    "brand",
                    brand
            );

            data.put(
                    "name",
                    brand
            );
        }

        String owner =
                firstString(
                        data,
                        "ownerName",
                        "owner"
                );

        if (owner != null) {

            data.put(
                    "ownerName",
                    owner
            );

            data.put(
                    "owner",
                    owner
            );
        }

        String status =
                firstString(
                        data,
                        "status"
                );

        if (status == null) {

            data.put(
                    "status",
                    "Active"
            );
        }

        return data;
    }

    // =====================================================
    // VEHICLE OWNERSHIP
    // =====================================================

    private boolean belongsToCustomer(
            DocumentSnapshot document,
            String customerId
    ) {

        if (document == null
                ||
                !document.exists()) {

            return false;
        }

        String savedCustomerId =
                firstDocumentString(
                        document,
                        "customerId",
                        "userEmail"
                );

        return sameCustomer(
                savedCustomerId,
                customerId
        );
    }

    // =====================================================
    // MIGRATE ALL OLD USER VEHICLES
    // =====================================================

    private void migrateLegacyVehicles(
            String customerId
    ) throws Exception {

        QuerySnapshot oldVehicles =
                firestore
                        .collection(
                                USER_COLLECTION
                        )
                        .document(
                                customerId
                        )
                        .collection(
                                LEGACY_VEHICLE_COLLECTION
                        )
                        .get()
                        .get();

        for (DocumentSnapshot oldDocument :
                oldVehicles.getDocuments()) {

            migrateLegacyVehicleDocument(
                    customerId,
                    oldDocument
            );
        }
    }

    // =====================================================
    // MIGRATE ONE OLD VEHICLE
    // =====================================================

    private boolean migrateSingleLegacyVehicle(
            String customerId,
            String vehicleId
    ) throws Exception {

        DocumentSnapshot oldDocument =
                firestore
                        .collection(
                                USER_COLLECTION
                        )
                        .document(
                                customerId
                        )
                        .collection(
                                LEGACY_VEHICLE_COLLECTION
                        )
                        .document(
                                vehicleId
                        )
                        .get()
                        .get();

        if (!oldDocument.exists()) {

            return false;
        }

        return migrateLegacyVehicleDocument(
                customerId,
                oldDocument
        );
    }

    // =====================================================
    // MIGRATE LEGACY VEHICLE DOCUMENT
    // =====================================================

    private boolean migrateLegacyVehicleDocument(
            String customerId,
            DocumentSnapshot oldDocument
    ) throws Exception {

        if (oldDocument == null
                ||
                !oldDocument.exists()
                ||
                oldDocument.getData() == null) {

            return false;
        }

        String vehicleId =
                oldDocument.getId();

        DocumentReference newDocument =
                firestore
                        .collection(
                                VEHICLE_COLLECTION
                        )
                        .document(
                                vehicleId
                        );

        DocumentSnapshot existing =
                newDocument
                        .get()
                        .get();

        /*
         * Same top-level ID आधीच दुसऱ्या customer चा
         * असेल तर overwrite करायचा नाही.
         */
        if (existing.exists()) {

            if (belongsToCustomer(
                    existing,
                    customerId
            )) {

                return true;
            }

            /*
             * Rare ID collision.
             * नवीन Firestore ID वापरतो.
             */
            newDocument =
                    firestore
                            .collection(
                                    VEHICLE_COLLECTION
                            )
                            .document();

            vehicleId =
                    newDocument.getId();
        }

        Map<String, Object> migratedData =
                normalizeVehicleData(
                        customerId,
                        vehicleId,
                        oldDocument.getData(),
                        true
                );

        migratedData.put(
                "migratedFromLegacy",
                true
        );

        newDocument
                .set(
                        migratedData
                )
                .get();

        return true;
    }

    // =====================================================
    // MIGRATE OLD SERVICE HISTORY
    // =====================================================

    private void migrateLegacyServiceHistory(
            String customerId,
            String vehicleId
    ) throws Exception {

        QuerySnapshot legacyHistory =
                firestore
                        .collection(
                                USER_COLLECTION
                        )
                        .document(
                                customerId
                        )
                        .collection(
                                LEGACY_VEHICLE_COLLECTION
                        )
                        .document(
                                vehicleId
                        )
                        .collection(
                                LEGACY_HISTORY_COLLECTION
                        )
                        .get()
                        .get();

        for (DocumentSnapshot oldDocument :
                legacyHistory.getDocuments()) {

            if (oldDocument.getData() == null) {

                continue;
            }

            /*
             * Same old document ID वापरण्याचा प्रयत्न.
             */
            DocumentReference requestDocument =
                    firestore
                            .collection(
                                    SERVICE_REQUEST_COLLECTION
                            )
                            .document(
                                    oldDocument.getId()
                            );

            DocumentSnapshot existing =
                    requestDocument
                            .get()
                            .get();

            /*
             * आधी migrate झालं असेल तर पुन्हा करू नका.
             */
            if (existing.exists()) {

                String existingCustomerId =
                        stringValue(
                                existing.get(
                                        "customerId"
                                )
                        );

                String existingVehicleId =
                        stringValue(
                                existing.get(
                                        "vehicleId"
                                )
                        );

                if (sameCustomer(
                        existingCustomerId,
                        customerId
                )
                        &&
                        vehicleId.equals(
                                existingVehicleId
                        )) {

                    continue;
                }

                /*
                 * ID collision असल्यास new ID.
                 */
                requestDocument =
                        firestore
                                .collection(
                                        SERVICE_REQUEST_COLLECTION
                                )
                                .document();
            }

            String requestId =
                    requestDocument.getId();

            Map<String, Object> data =
                    new HashMap<>(
                            oldDocument.getData()
                    );

            data.put(
                    "requestId",
                    requestId
            );

            data.put(
                    "customerId",
                    customerId
            );

            data.put(
                    "vehicleId",
                    vehicleId
            );

            data.put(
                    "status",
                    "Completed"
            );

            // ---------------------------------------------
            // CUSTOMER NAME
            // ---------------------------------------------

            if (isBlankValue(
                    data.get(
                            "customerName"
                    )
            )) {

                data.put(
                        "customerName",
                        getCustomerName(
                                customerId
                        )
                );
            }

            // ---------------------------------------------
            // VEHICLE NUMBER
            // ---------------------------------------------

            if (isBlankValue(
                    data.get(
                            "vehicleNumber"
                    )
            )) {

                Map<String, Object> vehicle =
                        getVehicle(
                                customerId,
                                vehicleId
                        );

                if (vehicle != null) {

                    Object number =
                            firstValue(
                                    vehicle,
                                    "vehicleNumber",
                                    "registrationNumber"
                            );

                    if (number != null) {

                        data.put(
                                "vehicleNumber",
                                number
                        );
                    }
                }
            }

            // ---------------------------------------------
            // SERVICE TYPE
            // ---------------------------------------------

            if (isBlankValue(
                    data.get(
                            "serviceType"
                    )
            )) {

                Object service =
                        firstValue(
                                data,
                                "service",
                                "serviceName",
                                "type"
                        );

                data.put(
                        "serviceType",
                        service == null
                                ? "Vehicle Service"
                                : service
                );
            }

            // ---------------------------------------------
            // DATE
            // ---------------------------------------------

            if (isBlankValue(
                    data.get(
                            "requestDate"
                    )
            )) {

                Object oldDate =
                        firstValue(
                                data,
                                "date",
                                "serviceDate"
                        );

                data.put(
                        "requestDate",
                        oldDate == null
                                ? currentTime()
                                : oldDate
                );
            }

            if (isBlankValue(
                    data.get(
                            "completedDate"
                    )
            )) {

                data.put(
                        "completedDate",
                        currentTime()
                );
            }

            data.put(
                    "migratedFromLegacyHistory",
                    true
            );

            requestDocument
                    .set(
                            data
                    )
                    .get();
        }
    }

    // =====================================================
    // SERVICE REQUEST -> USER HISTORY MAP
    // =====================================================

    private Map<String, Object> createServiceHistoryMap(
            DocumentSnapshot document
    ) {

        if (document == null
                ||
                !document.exists()
                ||
                document.getData() == null) {

            return null;
        }

        Map<String, Object> data =
                new HashMap<>(
                        document.getData()
                );

        data.put(
                "id",
                document.getId()
        );

        data.put(
                "requestId",
                document.getId()
        );

        // =================================================
        // DATE ALIAS
        // =================================================

        if (isBlankValue(
                data.get(
                        "date"
                )
        )) {

            Object date =
                    firstValue(
                            data,
                            "completedDate",
                            "requestDate",
                            "serviceDate"
                    );

            if (date != null) {

                data.put(
                        "date",
                        date
                );
            }
        }

        // =================================================
        // SERVICE ALIAS
        // =================================================

        if (isBlankValue(
                data.get(
                        "service"
                )
        )) {

            Object service =
                    firstValue(
                            data,
                            "serviceType",
                            "serviceName"
                    );

            if (service != null) {

                data.put(
                        "service",
                        service
                );
            }
        }

        // =================================================
        // MECHANIC ALIAS
        // =================================================

        if (isBlankValue(
                data.get(
                        "mechanic"
                )
        )) {

            Object mechanic =
                    firstValue(
                            data,
                            "mechanicName"
                    );

            if (mechanic != null) {

                data.put(
                        "mechanic",
                        mechanic
                );
            }
        }

        // =================================================
        // COST ALIAS
        // =================================================

        if (isBlankValue(
                data.get(
                        "cost"
                )
        )) {

            Object cost =
                    firstValue(
                            data,
                            "finalCost",
                            "estimatedCost",
                            "totalCost"
                    );

            if (cost != null) {

                data.put(
                        "cost",
                        cost
                );
            }
        }

        return data;
    }

    // =====================================================
    // GET CUSTOMER NAME
    // =====================================================

    private String getCustomerName(
            String customerId
    ) {

        try {

            DocumentSnapshot user =
                    firestore
                            .collection(
                                    USER_COLLECTION
                            )
                            .document(
                                    customerId
                            )
                            .get()
                            .get();

            if (user.exists()) {

                String name =
                        stringValue(
                                user.get(
                                        "name"
                                )
                        );

                if (name != null) {

                    return name;
                }
            }

            DocumentSnapshot customer =
                    firestore
                            .collection(
                                    "customers"
                            )
                            .document(
                                    customerId
                            )
                            .get()
                            .get();

            if (customer.exists()) {

                String name =
                        stringValue(
                                customer.get(
                                        "name"
                                )
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
    // DOCUMENT FIELD HELPERS
    // =====================================================

    private String firstDocumentString(
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
    // MAP STRING HELPER
    // =====================================================

    private String firstString(
            Map<String, Object> data,
            String... fields
    ) {

        Object value =
                firstValue(
                        data,
                        fields
                );

        return stringValue(
                value
        );
    }

    // =====================================================
    // FIRST NON-EMPTY MAP VALUE
    // =====================================================

    private Object firstValue(
            Map<String, Object> data,
            String... fields
    ) {

        if (data == null
                ||
                fields == null) {

            return null;
        }

        for (String field :
                fields) {

            Object value =
                    data.get(
                            field
                    );

            if (!isBlankValue(
                    value
            )) {

                return value;
            }
        }

        return null;
    }

    // =====================================================
    // SAME CUSTOMER
    // =====================================================

    private boolean sameCustomer(
            String first,
            String second
    ) {

        String firstNormalized =
                normalizeEmail(
                        first
                );

        String secondNormalized =
                normalizeEmail(
                        second
                );

        return firstNormalized != null
                &&
                secondNormalized != null
                &&
                firstNormalized.equals(
                        secondNormalized
                );
    }

    // =====================================================
    // EMAIL
    // =====================================================

    private String normalizeEmail(
            String email
    ) {

        String cleaned =
                clean(
                        email
                );

        if (cleaned == null) {

            return null;
        }

        return cleaned.toLowerCase();
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
    // BLANK VALUE
    // =====================================================

    private boolean isBlankValue(
            Object value
    ) {

        return value == null
                ||
                String.valueOf(
                        value
                ).trim().isEmpty();
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