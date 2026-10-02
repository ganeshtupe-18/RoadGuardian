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

public class TowTruckDAO {

    // ============================================================
    // CANONICAL COLLECTIONS
    // ============================================================

    private static final String TOW_TRUCKS_COLLECTION =
            "towTrucks";

    /*
     * IMPORTANT:
     * Tow booking is stored as the SAME canonical service request.
     *
     * Customer -> Admin -> Mechanic -> History
     * all read serviceRequests.
     *
     * We intentionally do NOT create a second towBookings workflow.
     */
    private static final String SERVICE_REQUESTS_COLLECTION =
            "serviceRequests";

    private final Firestore firestore;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public TowTruckDAO() {

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

    public TowTruckDAO(
            Firestore firestore
    ) {

        if (firestore == null) {

            throw new IllegalArgumentException(
                    "Firestore cannot be null."
            );
        }

        this.firestore = firestore;
    }

    // ============================================================
    // GET AVAILABLE TOW TRUCKS
    // ============================================================

    public List<Map<String, Object>> getAvailableTowTrucks()
            throws Exception {

        List<Map<String, Object>> trucks =
                new ArrayList<>();

        QuerySnapshot snapshot =
                firestore
                        .collection(TOW_TRUCKS_COLLECTION)
                        .get()
                        .get();

        for (DocumentSnapshot document :
                snapshot.getDocuments()) {

            if (!isTruckAvailable(document)) {
                continue;
            }

            Map<String, Object> data =
                    new HashMap<>();

            if (document.getData() != null) {
                data.putAll(document.getData());
            }

            data.put(
                    "truckId",
                    document.getId()
            );

            trucks.add(data);
        }

        return trucks;
    }

    // ============================================================
    // GET ALL TOW TRUCKS
    // ============================================================

    public List<Map<String, Object>> getAllTowTrucks()
            throws Exception {

        List<Map<String, Object>> trucks =
                new ArrayList<>();

        QuerySnapshot snapshot =
                firestore
                        .collection(TOW_TRUCKS_COLLECTION)
                        .get()
                        .get();

        for (DocumentSnapshot document :
                snapshot.getDocuments()) {

            Map<String, Object> data =
                    new HashMap<>();

            if (document.getData() != null) {
                data.putAll(document.getData());
            }

            data.put(
                    "truckId",
                    document.getId()
            );

            trucks.add(data);
        }

        return trucks;
    }

    // ============================================================
    // BOOK TOW TRUCK
    //
    // CANONICAL FLOW:
    //
    // Customer Tow Truck page
    //          -> serviceRequests
    //          -> Admin sees same record
    //          -> mechanic assigned / already linked
    //          -> mechanic accepts
    //          -> In Progress
    //          -> Completed
    //          -> Customer + Mechanic history
    // ============================================================

    public String bookTowTruck(
            String userId,
            String userName,
            String userEmail,
            String vehicleId,
            String vehicleNumber,
            String truckId,
            String truckName,
            String company,
            String pickupLocation,
            String destination,
            String charge,
            String mechanicId,
            String mechanicName
    ) throws Exception {

        String customerId =
                firstNonBlank(
                        normalizeId(userEmail),
                        normalizeId(userId)
                );

        userName =
                firstNonBlank(
                        clean(userName),
                        "Customer"
                );

        vehicleId = clean(vehicleId);
        vehicleNumber = clean(vehicleNumber);

        truckId = clean(truckId);

        truckName =
                firstNonBlank(
                        clean(truckName),
                        "Tow Truck"
                );

        company =
                firstNonBlank(
                        clean(company),
                        "RoadGuardian Partner"
                );

        pickupLocation = clean(pickupLocation);
        destination = clean(destination);
        charge = clean(charge);

        mechanicId = normalizeId(mechanicId);
        mechanicName = clean(mechanicName);

        // --------------------------------------------------------
        // VALIDATION
        // --------------------------------------------------------

        if (customerId == null) {

            throw new IllegalArgumentException(
                    "Customer session is missing. Please login again."
            );
        }

        if (vehicleId == null) {

            throw new IllegalArgumentException(
                    "Please select a vehicle for tow assistance."
            );
        }

        if (truckId == null) {

            throw new IllegalArgumentException(
                    "Tow truck ID is missing."
            );
        }

        if (pickupLocation == null) {

            throw new IllegalArgumentException(
                    "Pickup / breakdown location is required."
            );
        }

        if (destination == null) {

            throw new IllegalArgumentException(
                    "Drop-off destination is required."
            );
        }

        // --------------------------------------------------------
        // VERIFY TRUCK STILL AVAILABLE
        // --------------------------------------------------------

        DocumentReference truckReference =
                firestore
                        .collection(TOW_TRUCKS_COLLECTION)
                        .document(truckId);

        DocumentSnapshot truckSnapshot =
                truckReference
                        .get()
                        .get();

        if (!truckSnapshot.exists()) {

            throw new IllegalArgumentException(
                    "Selected tow truck no longer exists."
            );
        }

        if (!isTruckAvailable(truckSnapshot)) {

            throw new IllegalStateException(
                    "Selected tow truck is no longer available. Please refresh."
            );
        }

        // --------------------------------------------------------
        // OPTIONAL MECHANIC LINK FROM towTrucks RECORD
        // --------------------------------------------------------

        if (mechanicId == null) {

            mechanicId =
                    normalizeId(
                            firstString(
                                    truckSnapshot,
                                    "mechanicId",
                                    "driverId",
                                    "assignedMechanicId"
                            )
                    );
        }

        if (mechanicName == null) {

            mechanicName =
                    clean(
                            firstString(
                                    truckSnapshot,
                                    "mechanicName",
                                    "driverName",
                                    "assignedMechanicName"
                            )
                    );
        }

        // --------------------------------------------------------
        // CREATE SAME serviceRequests DOCUMENT
        // --------------------------------------------------------

        DocumentReference requestReference =
                firestore
                        .collection(SERVICE_REQUESTS_COLLECTION)
                        .document();

        String requestId =
                requestReference.getId();

        String now =
                currentTime();

        String status =
                mechanicId == null
                        ? "Pending"
                        : "Assigned";

        Map<String, Object> request =
                new HashMap<>();

        request.put("requestId", requestId);

        // Customer
        request.put("customerId", customerId);
        request.put("customerName", userName);
        request.put("userId", customerId);
        request.put("userEmail", firstNonBlank(clean(userEmail), customerId));

        // Vehicle
        request.put("vehicleId", safe(vehicleId));
        request.put("vehicleNumber", safe(vehicleNumber));

        // Mechanic
        request.put("mechanicId", safe(mechanicId));
        request.put("mechanicName", safe(mechanicName));

        // Canonical service request information
        request.put("serviceType", "Tow Truck");
        request.put(
                "description",
                buildDescription(
                        truckName,
                        company,
                        destination
                )
        );
        request.put("location", pickupLocation);
        request.put("status", status);

        // Lifecycle
        request.put("requestDate", now);
        request.put("assignedDate", mechanicId == null ? "" : now);
        request.put("acceptedDate", "");
        request.put("startedDate", "");
        request.put("completedDate", "");
        request.put("cancelledDate", "");
        request.put("updatedAt", now);

        // Cost compatibility
        request.put("estimatedCost", safe(charge));
        request.put("partsCost", "");
        request.put("labourCost", "");
        request.put("finalCost", "");

        // Flow metadata
        request.put("source", "Tow Truck");
        request.put("priority", "High");

        // Tow-specific fields remain on SAME request record.
        request.put("towTruckId", truckId);
        request.put("towTruckName", truckName);
        request.put("towCompany", company);
        request.put("towPickupLocation", pickupLocation);
        request.put("towDestination", destination);
        request.put("towCharge", safe(charge));

        requestReference
                .set(request)
                .get();

        // --------------------------------------------------------
        // RESERVE TOW TRUCK
        // --------------------------------------------------------

        Map<String, Object> truckUpdates =
                new HashMap<>();

        truckUpdates.put("status", "Booked");
        truckUpdates.put("available", false);
        truckUpdates.put("activeRequestId", requestId);
        truckUpdates.put("bookedByCustomerId", customerId);
        truckUpdates.put("updatedAt", now);

        try {

            truckReference
                    .set(
                            truckUpdates,
                            SetOptions.merge()
                    )
                    .get();

        } catch (Exception truckUpdateError) {

            // Avoid orphan service request if truck reservation fails.
            requestReference
                    .delete()
                    .get();

            throw truckUpdateError;
        }

        System.out.println(
                "Tow service request created: "
                        + requestId
                        + " | status="
                        + status
                        + " | truckId="
                        + truckId
                        + " | mechanicId="
                        + mechanicId
        );

        return requestId;
    }

    // ============================================================
    // OLD METHOD - KEPT FOR SOURCE COMPATIBILITY
    //
    // Existing callers should move to the full method above.
    // ============================================================

    public String bookTowTruck(
            String userId,
            String userName,
            String userEmail,
            String truckId,
            String truckName,
            String company,
            String destination,
            String charge
    ) throws Exception {

        throw new IllegalArgumentException(
                "Vehicle and pickup location are required for the canonical Tow Truck service flow."
        );
    }

    // ============================================================
    // CANCEL TOW REQUEST
    //
    // The request remains in serviceRequests history as Cancelled.
    // The selected truck becomes Available again.
    // ============================================================

    public void cancelBooking(
            String requestId,
            String truckId
    ) throws Exception {

        requestId = clean(requestId);
        truckId = clean(truckId);

        if (requestId == null) {
            return;
        }

        DocumentReference requestReference =
                firestore
                        .collection(SERVICE_REQUESTS_COLLECTION)
                        .document(requestId);

        DocumentSnapshot requestSnapshot =
                requestReference
                        .get()
                        .get();

        if (!requestSnapshot.exists()) {
            return;
        }

        String currentStatus =
                normalizeStatus(
                        stringValue(
                                requestSnapshot.get("status")
                        )
                );

        if ("Completed".equals(currentStatus)) {

            throw new IllegalStateException(
                    "Completed tow request cannot be cancelled."
            );
        }

        String now = currentTime();

        if (!"Cancelled".equals(currentStatus)) {

            Map<String, Object> updates =
                    new HashMap<>();

            updates.put("status", "Cancelled");
            updates.put("cancelledDate", now);
            updates.put("updatedAt", now);

            requestReference
                    .set(
                            updates,
                            SetOptions.merge()
                    )
                    .get();
        }

        if (truckId == null) {

            truckId =
                    clean(
                            stringValue(
                                    requestSnapshot.get("towTruckId")
                            )
                    );
        }

        releaseTowTruck(
                truckId,
                requestId
        );
    }

    // ============================================================
    // RELEASE TOW TRUCK
    // ============================================================

    private void releaseTowTruck(
            String truckId,
            String requestId
    ) throws Exception {

        truckId = clean(truckId);

        if (truckId == null) {
            return;
        }

        DocumentReference truckReference =
                firestore
                        .collection(TOW_TRUCKS_COLLECTION)
                        .document(truckId);

        DocumentSnapshot truckSnapshot =
                truckReference
                        .get()
                        .get();

        if (!truckSnapshot.exists()) {
            return;
        }

        String linkedRequestId =
                clean(
                        stringValue(
                                truckSnapshot.get("activeRequestId")
                        )
                );

        /*
         * Do not free a truck that has already been linked
         * to some newer request.
         */
        if (linkedRequestId != null
                && requestId != null
                && !linkedRequestId.equals(requestId)) {

            return;
        }

        Map<String, Object> updates =
                new HashMap<>();

        updates.put("status", "Available");
        updates.put("available", true);
        updates.put("activeRequestId", "");
        updates.put("bookedByCustomerId", "");
        updates.put("updatedAt", currentTime());

        truckReference
                .set(
                        updates,
                        SetOptions.merge()
                )
                .get();
    }

    // ============================================================
    // AVAILABILITY
    // ============================================================

    private boolean isTruckAvailable(
            DocumentSnapshot document
    ) {

        if (document == null
                || !document.exists()) {

            return false;
        }

        Object availableValue =
                document.get("available");

        if (availableValue instanceof Boolean
                && !((Boolean) availableValue)) {

            return false;
        }

        String status =
                clean(
                        stringValue(
                                document.get("status")
                        )
                );

        /*
         * Legacy data may have no status field.
         * Missing status is treated as available unless
         * available=false explicitly exists.
         */
        if (status == null) {
            return true;
        }

        return status.equalsIgnoreCase("Available")
                || status.equalsIgnoreCase("Active")
                || status.equalsIgnoreCase("Idle")
                || status.equalsIgnoreCase("Ready");
    }

    // ============================================================
    // DESCRIPTION
    // ============================================================

    private String buildDescription(
            String truckName,
            String company,
            String destination
    ) {

        return "Tow assistance using "
                + firstNonBlank(truckName, "Tow Truck")
                + " ("
                + firstNonBlank(company, "RoadGuardian Partner")
                + ") to "
                + firstNonBlank(destination, "selected destination")
                + ".";
    }

    // ============================================================
    // FIRESTORE STRING
    // ============================================================

    private String firstString(
            DocumentSnapshot document,
            String... keys
    ) {

        if (document == null || keys == null) {
            return null;
        }

        for (String key : keys) {

            String value =
                    clean(
                            stringValue(
                                    document.get(key)
                            )
                    );

            if (value != null) {
                return value;
            }
        }

        return null;
    }

    // ============================================================
    // NORMALIZE STATUS
    // ============================================================

    private String normalizeStatus(
            String status
    ) {

        status = clean(status);

        if (status == null) {
            return "Pending";
        }

        if (status.equalsIgnoreCase("Pending")) {
            return "Pending";
        }

        if (status.equalsIgnoreCase("Assigned")) {
            return "Assigned";
        }

        if (status.equalsIgnoreCase("Accepted")) {
            return "Accepted";
        }

        if (status.equalsIgnoreCase("In Progress")
                || status.equalsIgnoreCase("InProgress")
                || status.equalsIgnoreCase("Active")) {

            return "In Progress";
        }

        if (status.equalsIgnoreCase("Completed")
                || status.equalsIgnoreCase("Complete")
                || status.equalsIgnoreCase("Done")) {

            return "Completed";
        }

        if (status.equalsIgnoreCase("Cancelled")
                || status.equalsIgnoreCase("Canceled")) {

            return "Cancelled";
        }

        return status;
    }

    // ============================================================
    // ID NORMALIZATION
    // ============================================================

    private String normalizeId(
            String value
    ) {

        value = clean(value);

        if (value == null) {
            return null;
        }

        if (value.contains("@")) {
            return value.toLowerCase();
        }

        return value;
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private String firstNonBlank(
            String... values
    ) {

        if (values == null) {
            return null;
        }

        for (String value : values) {

            String cleaned = clean(value);

            if (cleaned != null) {
                return cleaned;
            }
        }

        return null;
    }

    private String clean(
            String value
    ) {

        if (value == null) {
            return null;
        }

        String cleaned = value.trim();

        return cleaned.isEmpty()
                ? null
                : cleaned;
    }

    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value;
    }

    private String stringValue(
            Object value
    ) {

        return value == null
                ? null
                : String.valueOf(value);
    }

    private String currentTime() {

        return String.valueOf(
                System.currentTimeMillis()
        );
    }
}
