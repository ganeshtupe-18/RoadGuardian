package project.dao.admin;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;

import project.model.Vehicle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VehicleDAO {

    private static final String VEHICLE_COLLECTION = "vehicles";
    private static final String USER_COLLECTION = "users";
    private static final String CUSTOMER_COLLECTION = "customers";
    private static final String SERVICE_REQUEST_COLLECTION = "serviceRequests";

    private final Firestore firestore;

    public VehicleDAO(Firestore firestore) {
        if (firestore == null) {
            throw new IllegalArgumentException("Firestore cannot be null.");
        }
        this.firestore = firestore;
    }

    public List<Vehicle> getAllVehicles() throws Exception {
        List<Vehicle> vehicles = new ArrayList<>();

        QuerySnapshot snapshot = firestore
                .collection(VEHICLE_COLLECTION)
                .get()
                .get();

        for (DocumentSnapshot document : snapshot.getDocuments()) {
            Vehicle vehicle = convertDocument(document);
            if (vehicle != null) {
                vehicles.add(vehicle);
            }
        }

        return vehicles;
    }

    public Vehicle getVehicleById(String vehicleId) throws Exception {
        vehicleId = clean(vehicleId);
        if (vehicleId == null) {
            return null;
        }

        DocumentSnapshot direct = firestore
                .collection(VEHICLE_COLLECTION)
                .document(vehicleId)
                .get()
                .get();

        if (direct.exists()) {
            return convertDocument(direct);
        }

        QuerySnapshot snapshot = firestore
                .collection(VEHICLE_COLLECTION)
                .get()
                .get();

        for (DocumentSnapshot document : snapshot.getDocuments()) {
            String candidate = firstString(document, "vehicleId", "id");
            if (sameId(candidate, vehicleId)) {
                return convertDocument(document);
            }
        }

        return null;
    }

    public List<Vehicle> getVehiclesByCustomerId(String customerId) throws Exception {
        List<Vehicle> vehicles = new ArrayList<>();

        customerId = normalizeCustomerId(customerId);
        if (customerId == null) {
            return vehicles;
        }

        QuerySnapshot snapshot = firestore
                .collection(VEHICLE_COLLECTION)
                .get()
                .get();

        for (DocumentSnapshot document : snapshot.getDocuments()) {
            String ownerId = normalizeCustomerId(
                    firstString(document, "customerId", "userEmail", "userId")
            );

            if (!sameId(ownerId, customerId)) {
                continue;
            }

            Vehicle vehicle = convertDocument(document);
            if (vehicle != null) {
                vehicles.add(vehicle);
            }
        }

        return vehicles;
    }

    public boolean addVehicle(Vehicle vehicle) throws Exception {
        if (vehicle == null) {
            return false;
        }

        normalizeAndValidateVehicle(vehicle, true);

        if (!customerExists(vehicle.getCustomerId())) {
            throw new IllegalArgumentException(
                    "Customer not found. Use a valid Customer ID / customer email."
            );
        }

        if (vehicleNumberExists(vehicle.getVehicleNumber(), null)) {
            throw new IllegalArgumentException(
                    "A vehicle with this vehicle number is already registered."
            );
        }

        String resolvedOwner = resolveCustomerName(vehicle.getCustomerId());
        if (clean(vehicle.getOwnerName()) == null && resolvedOwner != null) {
            vehicle.setOwnerName(resolvedOwner);
        }

        DocumentReference document = firestore
                .collection(VEHICLE_COLLECTION)
                .document();

        vehicle.setVehicleId(document.getId());

        if (clean(vehicle.getCreatedAt()) == null) {
            vehicle.setCreatedAt(currentTime());
        }

        Map<String, Object> data = toFirestoreMap(vehicle, true);

        document.set(data).get();
        return true;
    }

    public boolean updateVehicle(Vehicle vehicle) throws Exception {
        if (vehicle == null) {
            return false;
        }

        String vehicleId = clean(vehicle.getVehicleId());
        if (vehicleId == null) {
            return false;
        }

        DocumentReference document = findVehicleDocument(vehicleId);
        if (document == null) {
            return false;
        }

        DocumentSnapshot existing = document.get().get();
        if (!existing.exists()) {
            return false;
        }

        Vehicle existingVehicle = convertDocument(existing);

        if (clean(vehicle.getCustomerId()) == null && existingVehicle != null) {
            vehicle.setCustomerId(existingVehicle.getCustomerId());
        }

        normalizeAndValidateVehicle(vehicle, false);

        if (!customerExists(vehicle.getCustomerId())) {
            throw new IllegalArgumentException(
                    "Customer not found. Use a valid Customer ID / customer email."
            );
        }

        if (vehicleNumberExists(vehicle.getVehicleNumber(), document.getId())) {
            throw new IllegalArgumentException(
                    "Another vehicle already uses this vehicle number."
            );
        }

        String resolvedOwner = resolveCustomerName(vehicle.getCustomerId());
        if (clean(vehicle.getOwnerName()) == null && resolvedOwner != null) {
            vehicle.setOwnerName(resolvedOwner);
        }

        if (clean(vehicle.getCreatedAt()) == null) {
            vehicle.setCreatedAt(
                    firstString(existing, "createdAt")
            );
        }

        vehicle.setVehicleId(document.getId());

        Map<String, Object> data = toFirestoreMap(vehicle, false);
        document.set(data, SetOptions.merge()).get();

        return true;
    }

    public boolean deleteVehicle(String vehicleId) throws Exception {
        vehicleId = clean(vehicleId);
        if (vehicleId == null) {
            return false;
        }

        DocumentReference document = findVehicleDocument(vehicleId);
        if (document == null) {
            return false;
        }

        DocumentSnapshot existing = document.get().get();
        if (!existing.exists()) {
            return false;
        }

        Vehicle vehicle = convertDocument(existing);

        if (vehicle != null && hasActiveServiceRequests(vehicle)) {
            throw new IllegalStateException(
                    "Vehicle cannot be deleted while an active service request exists."
            );
        }

        document.delete().get();
        return true;
    }

    public List<Vehicle> searchVehicles(String searchText) throws Exception {
        List<Vehicle> all = getAllVehicles();

        String search = clean(searchText);
        if (search == null) {
            return all;
        }

        search = search.toLowerCase();
        List<Vehicle> result = new ArrayList<>();

        for (Vehicle vehicle : all) {
            if (contains(vehicle.getVehicleId(), search)
                    || contains(vehicle.getCustomerId(), search)
                    || contains(vehicle.getOwnerName(), search)
                    || contains(vehicle.getVehicleNumber(), search)
                    || contains(vehicle.getBrand(), search)
                    || contains(vehicle.getModel(), search)
                    || contains(vehicle.getVehicleType(), search)
                    || contains(vehicle.getFuelType(), search)
                    || contains(vehicle.getYear(), search)
                    || contains(vehicle.getStatus(), search)) {

                result.add(vehicle);
            }
        }

        return result;
    }

    public List<Vehicle> getVehiclesByStatus(String status) throws Exception {
        status = normalizeStatus(status);

        if (status == null || status.equalsIgnoreCase("All")) {
            return getAllVehicles();
        }

        List<Vehicle> result = new ArrayList<>();

        for (Vehicle vehicle : getAllVehicles()) {
            if (vehicle != null
                    && normalizeStatus(vehicle.getStatus())
                    .equalsIgnoreCase(status)) {

                result.add(vehicle);
            }
        }

        return result;
    }

    public boolean customerExists(String customerId) throws Exception {
        customerId = normalizeCustomerId(customerId);
        if (customerId == null) {
            return false;
        }

        if (findCustomerDocument(USER_COLLECTION, customerId) != null) {
            return true;
        }

        return findCustomerDocument(CUSTOMER_COLLECTION, customerId) != null;
    }

    public String getCustomerName(String customerId) throws Exception {
        String name = resolveCustomerName(customerId);
        return clean(name) == null ? "Customer" : name;
    }

    public boolean hasActiveServiceRequests(String vehicleId) throws Exception {
        Vehicle vehicle = getVehicleById(vehicleId);
        return vehicle != null && hasActiveServiceRequests(vehicle);
    }

    private boolean hasActiveServiceRequests(Vehicle vehicle) throws Exception {
        QuerySnapshot snapshot = firestore
                .collection(SERVICE_REQUEST_COLLECTION)
                .get()
                .get();

        for (DocumentSnapshot request : snapshot.getDocuments()) {
            String status = normalizeServiceStatus(
                    stringValue(request.get("status"))
            );

            if (!(status.equalsIgnoreCase("Pending")
                    || status.equalsIgnoreCase("Assigned")
                    || status.equalsIgnoreCase("Accepted")
                    || status.equalsIgnoreCase("In Progress"))) {
                continue;
            }

            String requestVehicleId = firstString(
                    request,
                    "vehicleId"
            );

            String requestVehicleNumber = firstString(
                    request,
                    "vehicleNumber",
                    "registrationNumber"
            );

            if (sameId(requestVehicleId, vehicle.getVehicleId())
                    || sameVehicleNumber(requestVehicleNumber, vehicle.getVehicleNumber())) {
                return true;
            }
        }

        return false;
    }

    private void normalizeAndValidateVehicle(Vehicle vehicle, boolean newVehicle) {
        String customerId = normalizeCustomerId(vehicle.getCustomerId());
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID is required.");
        }
        vehicle.setCustomerId(customerId);

        String vehicleNumber = normalizeVehicleNumber(vehicle.getVehicleNumber());
        if (vehicleNumber == null) {
            throw new IllegalArgumentException("Vehicle number is required.");
        }
        vehicle.setVehicleNumber(vehicleNumber);

        String brand = clean(vehicle.getBrand());
        if (brand == null) {
            throw new IllegalArgumentException("Vehicle brand is required.");
        }
        vehicle.setBrand(brand);

        String model = clean(vehicle.getModel());
        if (model == null) {
            throw new IllegalArgumentException("Vehicle model is required.");
        }
        vehicle.setModel(model);

        vehicle.setOwnerName(clean(vehicle.getOwnerName()));
        vehicle.setVehicleType(clean(vehicle.getVehicleType()));
        vehicle.setFuelType(clean(vehicle.getFuelType()));
        vehicle.setYear(clean(vehicle.getYear()));
        vehicle.setStatus(normalizeStatus(vehicle.getStatus()));

        if (vehicle.getStatus() == null) {
            vehicle.setStatus("Active");
        }

        if (newVehicle && clean(vehicle.getCreatedAt()) == null) {
            vehicle.setCreatedAt(currentTime());
        }
    }

    private Map<String, Object> toFirestoreMap(Vehicle vehicle, boolean newVehicle) {
        Map<String, Object> data = new HashMap<>();

        String vehicleId = clean(vehicle.getVehicleId());
        String customerId = normalizeCustomerId(vehicle.getCustomerId());
        String vehicleNumber = normalizeVehicleNumber(vehicle.getVehicleNumber());
        String ownerName = clean(vehicle.getOwnerName());
        String brand = clean(vehicle.getBrand());
        String model = clean(vehicle.getModel());
        String type = clean(vehicle.getVehicleType());
        String fuel = clean(vehicle.getFuelType());
        String year = clean(vehicle.getYear());
        String status = normalizeStatus(vehicle.getStatus());
        String createdAt = clean(vehicle.getCreatedAt());

        data.put("vehicleId", vehicleId);
        data.put("id", vehicleId);

        data.put("customerId", customerId);
        data.put("userEmail", customerId);

        if (ownerName != null) {
            data.put("ownerName", ownerName);
            data.put("owner", ownerName);
        }

        data.put("vehicleNumber", vehicleNumber);
        data.put("registrationNumber", vehicleNumber);

        data.put("brand", brand);
        data.put("name", brand);
        data.put("make", brand);

        data.put("model", model);

        if (type != null) {
            data.put("vehicleType", type);
            data.put("type", type);
        }

        if (fuel != null) {
            data.put("fuelType", fuel);
        }

        if (year != null) {
            data.put("year", year);
        }

        data.put("status", status == null ? "Active" : status);

        if (createdAt != null) {
            data.put("createdAt", createdAt);
        } else if (newVehicle) {
            data.put("createdAt", currentTime());
        }

        data.put("updatedAt", currentTime());

        return data;
    }

    private Vehicle convertDocument(DocumentSnapshot document) throws Exception {
        if (document == null || !document.exists()) {
            return null;
        }

        Vehicle vehicle = new Vehicle();

        String vehicleId = firstString(document, "vehicleId", "id");
        if (clean(vehicleId) == null) {
            vehicleId = document.getId();
        }
        vehicle.setVehicleId(vehicleId);

        String customerId = normalizeCustomerId(
                firstString(document, "customerId", "userEmail", "userId")
        );
        vehicle.setCustomerId(customerId);

        String ownerName = firstString(document, "ownerName", "owner");
        if (clean(ownerName) == null && customerId != null) {
            ownerName = resolveCustomerName(customerId);
        }
        vehicle.setOwnerName(ownerName);

        vehicle.setVehicleNumber(
                normalizeVehicleNumber(
                        firstString(document, "vehicleNumber", "registrationNumber", "number")
                )
        );

        vehicle.setBrand(
                firstString(document, "brand", "name", "make")
        );

        vehicle.setModel(
                firstString(document, "model")
        );

        vehicle.setVehicleType(
                firstString(document, "vehicleType", "type")
        );

        vehicle.setFuelType(
                firstString(document, "fuelType", "fuel")
        );

        vehicle.setYear(
                stringValue(document.get("year"))
        );

        String status = normalizeStatus(
                firstString(document, "status")
        );
        vehicle.setStatus(status == null ? "Active" : status);

        vehicle.setCreatedAt(
                stringValue(document.get("createdAt"))
        );

        return vehicle;
    }

    private DocumentReference findVehicleDocument(String vehicleId) throws Exception {
        DocumentReference direct = firestore
                .collection(VEHICLE_COLLECTION)
                .document(vehicleId);

        if (direct.get().get().exists()) {
            return direct;
        }

        QuerySnapshot snapshot = firestore
                .collection(VEHICLE_COLLECTION)
                .get()
                .get();

        for (DocumentSnapshot document : snapshot.getDocuments()) {
            String candidate = firstString(document, "vehicleId", "id");
            if (sameId(candidate, vehicleId)) {
                return firestore.collection(VEHICLE_COLLECTION).document(document.getId());
            }
        }

        return null;
    }

    private boolean vehicleNumberExists(String vehicleNumber, String exceptDocumentId)
            throws Exception {

        vehicleNumber = normalizeVehicleNumber(vehicleNumber);
        if (vehicleNumber == null) {
            return false;
        }

        QuerySnapshot snapshot = firestore
                .collection(VEHICLE_COLLECTION)
                .get()
                .get();

        for (DocumentSnapshot document : snapshot.getDocuments()) {
            if (exceptDocumentId != null
                    && document.getId().equals(exceptDocumentId)) {
                continue;
            }

            String existing = firstString(
                    document,
                    "vehicleNumber",
                    "registrationNumber"
            );

            if (sameVehicleNumber(existing, vehicleNumber)) {
                return true;
            }
        }

        return false;
    }

    private String resolveCustomerName(String customerId) throws Exception {
        customerId = normalizeCustomerId(customerId);
        if (customerId == null) {
            return null;
        }

        DocumentSnapshot user = findCustomerDocument(USER_COLLECTION, customerId);
        if (user != null) {
            String role = stringValue(user.get("role"));
            if (role == null || role.equalsIgnoreCase("Customer") || role.equalsIgnoreCase("User")) {
                String name = firstString(user, "name", "customerName", "userName");
                if (clean(name) != null) {
                    return name;
                }
            }
        }

        DocumentSnapshot customer = findCustomerDocument(CUSTOMER_COLLECTION, customerId);
        if (customer != null) {
            String name = firstString(customer, "name", "customerName", "userName");
            if (clean(name) != null) {
                return name;
            }
        }

        return null;
    }

    private DocumentSnapshot findCustomerDocument(String collection, String customerId)
            throws Exception {

        DocumentSnapshot direct = firestore
                .collection(collection)
                .document(customerId)
                .get()
                .get();

        if (direct.exists()) {
            return direct;
        }

        QuerySnapshot snapshot = firestore
                .collection(collection)
                .get()
                .get();

        for (DocumentSnapshot document : snapshot.getDocuments()) {
            String candidate = normalizeCustomerId(
                    firstString(
                            document,
                            "customerId",
                            "userId",
                            "email"
                    )
            );

            if (candidate == null) {
                candidate = normalizeCustomerId(document.getId());
            }

            if (sameId(candidate, customerId)) {
                return document;
            }
        }

        return null;
    }

    private String firstString(DocumentSnapshot document, String... fields) {
        if (document == null || fields == null) {
            return null;
        }

        for (String field : fields) {
            String value = stringValue(document.get(field));
            if (clean(value) != null) {
                return value;
            }
        }

        return null;
    }

    private String normalizeCustomerId(String customerId) {
        customerId = clean(customerId);
        if (customerId == null) {
            return null;
        }

        return customerId.contains("@")
                ? customerId.toLowerCase()
                : customerId;
    }

    private String normalizeVehicleNumber(String vehicleNumber) {
        vehicleNumber = clean(vehicleNumber);
        if (vehicleNumber == null) {
            return null;
        }

        return vehicleNumber.toUpperCase();
    }

    private String normalizeStatus(String status) {
        status = clean(status);
        if (status == null) {
            return null;
        }

        if (status.equalsIgnoreCase("Active")) {
            return "Active";
        }

        if (status.equalsIgnoreCase("Inactive")
                || status.equalsIgnoreCase("Disabled")
                || status.equalsIgnoreCase("Blocked")) {
            return "Inactive";
        }

        return status;
    }

    private String normalizeServiceStatus(String status) {
        status = clean(status);
        if (status == null) {
            return "Pending";
        }

        if (status.equalsIgnoreCase("InProgress")
                || status.equalsIgnoreCase("Active")
                || status.equalsIgnoreCase("Started")) {
            return "In Progress";
        }

        if (status.equalsIgnoreCase("Complete")
                || status.equalsIgnoreCase("Done")) {
            return "Completed";
        }

        if (status.equalsIgnoreCase("Canceled")) {
            return "Cancelled";
        }

        return status;
    }

    private boolean sameId(String first, String second) {
        first = normalizeCustomerId(first);
        second = normalizeCustomerId(second);

        return first != null && second != null && first.equals(second);
    }

    private boolean sameVehicleNumber(String first, String second) {
        first = normalizeVehicleNumber(first);
        second = normalizeVehicleNumber(second);

        return first != null && second != null && first.equals(second);
    }

    private String stringValue(Object value) {
        if (value == null) {
            return null;
        }

        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }

        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private boolean contains(String value, String search) {
        return value != null
                && search != null
                && value.toLowerCase().contains(search);
    }

    private String currentTime() {
        return String.valueOf(System.currentTimeMillis());
    }
}