package project.dao.user;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;

import project.firebase.FirebaseConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsDAO {

    private static final String USER_SETTINGS = "userSettings";
    private static final String USERS = "users";

    private final Firestore firestore;
    private final VehicleDAO vehicleDAO;

    public SettingsDAO() {
        try {
            firestore = FirebaseConfig.getFirestore();
            vehicleDAO = new VehicleDAO();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to initialize Firestore",
                    e
            );
        }
    }

    // ============================================================
    // USER SETTINGS
    // ============================================================

    public Map<String, Object> getSettings(String userId)
            throws Exception {

        String key = normalizeUserKey(userId);

        if (key == null) {
            throw new IllegalArgumentException(
                    "User ID is missing."
            );
        }

        DocumentSnapshot snapshot = firestore
                .collection(USER_SETTINGS)
                .document(key)
                .get()
                .get();

        if (!snapshot.exists()) {
            return new HashMap<>();
        }

        Map<String, Object> result = new HashMap<>();

        if (snapshot.getData() != null) {
            result.putAll(snapshot.getData());
        }

        return result;
    }

    public void saveSettings(
            String userId,
            Map<String, Object> settings
    ) throws Exception {

        String key = normalizeUserKey(userId);

        if (key == null) {
            throw new IllegalArgumentException(
                    "User ID is missing."
            );
        }

        Map<String, Object> data = new HashMap<>();

        if (settings != null) {
            data.putAll(settings);
        }

        data.put("userId", key);

        if (key.contains("@")) {
            data.put("userEmail", key);
        }

        data.put("updatedAt", System.currentTimeMillis());

        firestore
                .collection(USER_SETTINGS)
                .document(key)
                .set(data, SetOptions.merge())
                .get();
    }

    // ============================================================
    // EMERGENCY CONTACTS
    // Same path is used by Women Safety DAO:
    // users/{email}/emergencyContacts/{contactId}
    // ============================================================

    public List<Map<String, Object>> getEmergencyContacts(
            String userId
    ) throws Exception {

        List<Map<String, Object>> contacts = new ArrayList<>();

        String key = normalizeUserKey(userId);

        if (key == null) {
            return contacts;
        }

        QuerySnapshot snapshot = firestore
                .collection(USERS)
                .document(key)
                .collection("emergencyContacts")
                .get()
                .get();

        for (DocumentSnapshot document : snapshot.getDocuments()) {

            Map<String, Object> data = document.getData();

            if (data == null) {
                continue;
            }

            Map<String, Object> contact = new HashMap<>(data);
            contact.put("id", document.getId());
            contact.put("contactId", document.getId());

            contacts.add(contact);
        }

        return contacts;
    }

    public String saveEmergencyContact(
            String userId,
            String contactId,
            String name,
            String phone
    ) throws Exception {

        String key = normalizeUserKey(userId);
        String cleanName = clean(name);
        String cleanPhone = clean(phone);
        String cleanContactId = clean(contactId);

        if (key == null) {
            throw new IllegalArgumentException(
                    "User ID is missing."
            );
        }

        if (cleanName == null) {
            throw new IllegalArgumentException(
                    "Contact name is required."
            );
        }

        if (cleanPhone == null) {
            throw new IllegalArgumentException(
                    "Contact number is required."
            );
        }

        if (!cleanPhone.matches("\\d{10}")) {
            throw new IllegalArgumentException(
                    "Contact number must contain exactly 10 digits."
            );
        }

        DocumentReference reference;

        if (cleanContactId != null) {

            reference = firestore
                    .collection(USERS)
                    .document(key)
                    .collection("emergencyContacts")
                    .document(cleanContactId);

        } else {

            // Avoid duplicate contact records when the same phone
            // is saved more than once.
            reference = findContactByPhone(key, cleanPhone);

            if (reference == null) {
                reference = firestore
                        .collection(USERS)
                        .document(key)
                        .collection("emergencyContacts")
                        .document();
            }
        }

        DocumentSnapshot existing = reference.get().get();

        Map<String, Object> data = new HashMap<>();
        data.put("contactId", reference.getId());
        data.put("userId", key);
        data.put("name", cleanName);
        data.put("phone", cleanPhone);
        data.put("updatedAt", System.currentTimeMillis());

        if (!existing.exists()) {
            data.put("createdAt", System.currentTimeMillis());
        }

        reference
                .set(data, SetOptions.merge())
                .get();

        return reference.getId();
    }

    public void deleteEmergencyContact(
            String userId,
            String contactId
    ) throws Exception {

        String key = normalizeUserKey(userId);
        String id = clean(contactId);

        if (key == null || id == null) {
            return;
        }

        firestore
                .collection(USERS)
                .document(key)
                .collection("emergencyContacts")
                .document(id)
                .delete()
                .get();
    }

    // ============================================================
    // VEHICLE DETAILS
    // IMPORTANT:
    // Vehicle data now comes from the canonical top-level
    // vehicles collection through VehicleDAO.
    // ============================================================

    public Map<String, Object> getVehicleDetails(
            String userId
    ) throws Exception {

        String key = normalizeUserKey(userId);

        if (key == null) {
            throw new IllegalArgumentException(
                    "User ID is missing."
            );
        }

        List<Map<String, Object>> vehicles = vehicleDAO.getVehicles(key);

        if (vehicles != null && !vehicles.isEmpty()) {
            return settingsVehicleMap(selectPrimaryVehicle(vehicles));
        }

        // --------------------------------------------------------
        // Legacy fallback:
        // Old SettingsPage stored vehicle fields inside users/{id}.
        // If found, migrate once into canonical top-level vehicles.
        // --------------------------------------------------------

        DocumentSnapshot user = firestore
                .collection(USERS)
                .document(key)
                .get()
                .get();

        if (!user.exists()) {
            return new HashMap<>();
        }

        String makeModel = firstNonBlank(
                value(user, "makeModel"),
                value(user, "brand")
        );

        String registration = firstNonBlank(
                value(user, "registration"),
                value(user, "vehicleNumber"),
                value(user, "registrationNumber")
        );

        String fuelType = value(user, "fuelType");
        String year = value(user, "year");

        if (isBlank(makeModel)
                && isBlank(registration)
                && isBlank(fuelType)
                && isBlank(year)) {

            return new HashMap<>();
        }

        Map<String, Object> vehicleData = createCanonicalVehicleData(
                makeModel,
                registration,
                fuelType,
                year
        );

        String vehicleId = vehicleDAO.addVehicle(
                key,
                vehicleData
        );

        Map<String, Object> migrated = new HashMap<>();
        migrated.put("vehicleId", vehicleId);
        migrated.put("makeModel", emptyIfNull(makeModel));
        migrated.put("registration", emptyIfNull(registration).toUpperCase());
        migrated.put("fuelType", emptyIfNull(fuelType));
        migrated.put("year", emptyIfNull(year));

        return migrated;
    }

    // Backward-compatible overload.
    public void saveVehicleDetails(
            String userId,
            String makeModel,
            String registration,
            String fuelType,
            String year
    ) throws Exception {

        saveVehicleDetails(
                userId,
                null,
                makeModel,
                registration,
                fuelType,
                year
        );
    }

    public String saveVehicleDetails(
            String userId,
            String vehicleId,
            String makeModel,
            String registration,
            String fuelType,
            String year
    ) throws Exception {

        String key = normalizeUserKey(userId);
        String id = clean(vehicleId);
        String cleanMakeModel = clean(makeModel);
        String cleanRegistration = clean(registration);

        if (key == null) {
            throw new IllegalArgumentException(
                    "User ID is missing."
            );
        }

        if (cleanMakeModel == null) {
            throw new IllegalArgumentException(
                    "Please enter make and model."
            );
        }

        if (cleanRegistration == null) {
            throw new IllegalArgumentException(
                    "Please enter registration number."
            );
        }

        cleanRegistration = cleanRegistration.toUpperCase();

        List<Map<String, Object>> vehicles = vehicleDAO.getVehicles(key);

        Map<String, Object> target = null;

        if (vehicles != null) {

            // Exact vehicle ID first.
            if (id != null) {
                for (Map<String, Object> vehicle : vehicles) {
                    if (id.equals(clean(stringValue(vehicle, "vehicleId", "id")))) {
                        target = vehicle;
                        break;
                    }
                }
            }

            // Same registration second.
            if (target == null) {
                for (Map<String, Object> vehicle : vehicles) {

                    String existingRegistration = clean(stringValue(
                            vehicle,
                            "vehicleNumber",
                            "registrationNumber"
                    ));

                    if (existingRegistration != null
                            && existingRegistration.equalsIgnoreCase(cleanRegistration)) {
                        target = vehicle;
                        break;
                    }
                }
            }

            // Settings UI represents one primary vehicle. If the
            // customer owns exactly one vehicle, update that record.
            if (target == null && vehicles.size() == 1) {
                target = vehicles.get(0);
            }
        }

        Map<String, Object> vehicleData = createCanonicalVehicleData(
                cleanMakeModel,
                cleanRegistration,
                fuelType,
                year
        );

        // Preserve fields not edited by SettingsPage.
        if (target != null) {

            copyIfPresent(target, vehicleData, "vehicleType");
            copyIfPresent(target, vehicleData, "type");
            copyIfPresent(target, vehicleData, "ownerName");
            copyIfPresent(target, vehicleData, "owner");
            copyIfPresent(target, vehicleData, "status");

            String targetId = clean(stringValue(
                    target,
                    "vehicleId",
                    "id"
            ));

            if (targetId != null) {
                vehicleDAO.updateVehicle(
                        key,
                        targetId,
                        vehicleData
                );

                return targetId;
            }
        }

        return vehicleDAO.addVehicle(
                key,
                vehicleData
        );
    }

    // ============================================================
    // CONTACT HELPER
    // ============================================================

    private DocumentReference findContactByPhone(
            String userId,
            String phone
    ) throws Exception {

        QuerySnapshot snapshot = firestore
                .collection(USERS)
                .document(userId)
                .collection("emergencyContacts")
                .get()
                .get();

        for (DocumentSnapshot document : snapshot.getDocuments()) {

            String existing = clean(document.getString("phone"));

            if (existing != null && existing.equals(phone)) {
                return document.getReference();
            }
        }

        return null;
    }

    // ============================================================
    // VEHICLE HELPERS
    // ============================================================

    private Map<String, Object> selectPrimaryVehicle(
            List<Map<String, Object>> vehicles
    ) {

        if (vehicles == null || vehicles.isEmpty()) {
            return new HashMap<>();
        }

        for (Map<String, Object> vehicle : vehicles) {

            String status = clean(stringValue(vehicle, "status"));

            if (status == null || status.equalsIgnoreCase("Active")) {
                return vehicle;
            }
        }

        return vehicles.get(0);
    }

    private Map<String, Object> settingsVehicleMap(
            Map<String, Object> vehicle
    ) {

        Map<String, Object> result = new HashMap<>();

        if (vehicle == null) {
            return result;
        }

        String brand = clean(stringValue(
                vehicle,
                "brand",
                "name",
                "make"
        ));

        String model = clean(stringValue(vehicle, "model"));

        String makeModel;

        if (brand != null && model != null
                && !brand.equalsIgnoreCase(model)) {
            makeModel = brand + " " + model;

        } else {
            makeModel = firstNonBlank(brand, model, "");
        }

        result.put("vehicleId", firstNonBlank(
                stringValue(vehicle, "vehicleId", "id"),
                ""
        ));

        result.put("makeModel", makeModel);

        result.put("registration", firstNonBlank(
                stringValue(vehicle, "vehicleNumber", "registrationNumber"),
                ""
        ));

        result.put("fuelType", firstNonBlank(
                stringValue(vehicle, "fuelType"),
                ""
        ));

        result.put("year", firstNonBlank(
                stringValue(vehicle, "year"),
                ""
        ));

        return result;
    }

    private Map<String, Object> createCanonicalVehicleData(
            String makeModel,
            String registration,
            String fuelType,
            String year
    ) {

        Map<String, Object> vehicle = new HashMap<>();

        String[] parts = splitMakeModel(makeModel);

        vehicle.put("brand", parts[0]);
        vehicle.put("name", parts[0]);
        vehicle.put("model", parts[1]);
        vehicle.put("vehicleNumber", emptyIfNull(clean(registration)).toUpperCase());
        vehicle.put("registrationNumber", emptyIfNull(clean(registration)).toUpperCase());
        vehicle.put("fuelType", emptyIfNull(clean(fuelType)));
        vehicle.put("year", emptyIfNull(clean(year)));
        vehicle.put("status", "Active");

        return vehicle;
    }

    private String[] splitMakeModel(String makeModel) {

        String value = clean(makeModel);

        if (value == null) {
            return new String[]{"", ""};
        }

        String[] parts = value.split("\\s+", 2);

        return new String[]{
                parts[0],
                parts.length > 1 ? parts[1] : ""
        };
    }

    private void copyIfPresent(
            Map<String, Object> source,
            Map<String, Object> destination,
            String key
    ) {

        if (source == null || destination == null || key == null) {
            return;
        }

        Object value = source.get(key);

        if (value != null && !String.valueOf(value).trim().isEmpty()) {
            destination.put(key, value);
        }
    }

    // ============================================================
    // GENERIC HELPERS
    // ============================================================

    private String normalizeUserKey(String userId) {

        String value = clean(userId);

        if (value == null) {
            return null;
        }

        return value.contains("@")
                ? value.toLowerCase()
                : value;
    }

    private String value(DocumentSnapshot snapshot, String field) {

        if (snapshot == null || field == null) {
            return "";
        }

        Object value = snapshot.get(field);

        return value == null
                ? ""
                : String.valueOf(value);
    }

    private String stringValue(
            Map<String, Object> data,
            String... keys
    ) {

        if (data == null || keys == null) {
            return null;
        }

        for (String key : keys) {

            Object value = data.get(key);

            if (value != null) {

                String text = String.valueOf(value).trim();

                if (!text.isEmpty()) {
                    return text;
                }
            }
        }

        return null;
    }

    private String firstNonBlank(String... values) {

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

    private String clean(String value) {

        if (value == null) {
            return null;
        }

        String cleaned = value.trim();

        return cleaned.isEmpty()
                ? null
                : cleaned;
    }

    private boolean isBlank(String value) {
        return clean(value) == null;
    }

    private String emptyIfNull(String value) {
        return value == null ? "" : value;
    }
}
