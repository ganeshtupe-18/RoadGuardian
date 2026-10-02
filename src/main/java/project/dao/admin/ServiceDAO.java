package project.dao.admin;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;

import project.model.RoadService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceDAO {

    private static final String COLLECTION = "services";
    private static final String REQUEST_COLLECTION = "serviceRequests";

    private final Firestore firestore;

    public ServiceDAO(Firestore firestore) {
        if (firestore == null) {
            throw new IllegalArgumentException("Firestore cannot be null.");
        }
        this.firestore = firestore;
    }

    public List<RoadService> getAllServices() throws Exception {
        QuerySnapshot snapshot = firestore.collection(COLLECTION).get().get();
        List<RoadService> services = new ArrayList<>();

        for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
            RoadService service = fromDocument(document);
            if (service != null) {
                services.add(service);
            }
        }

        services.sort(
                Comparator.comparing(
                        service -> safe(service.getName()).toLowerCase()
                )
        );

        return services;
    }

    public RoadService getServiceById(String serviceId) throws Exception {
        serviceId = clean(serviceId);
        if (serviceId == null) {
            return null;
        }

        DocumentSnapshot document = firestore
                .collection(COLLECTION)
                .document(serviceId)
                .get()
                .get();

        return fromDocument(document);
    }

    public String addService(RoadService service) throws Exception {
        validate(service);

        if (nameExists(service.getName(), null)) {
            throw new IllegalArgumentException("A service with this name already exists.");
        }

        DocumentReference document = firestore.collection(COLLECTION).document();
        String now = now();

        service.setServiceId(document.getId());
        service.setName(clean(service.getName()));
        service.setDescription(cleanOrEmpty(service.getDescription()));
        service.setCategory(defaultValue(service.getCategory(), "General"));
        service.setEstimatedDuration(cleanOrEmpty(service.getEstimatedDuration()));
        service.setStatus(normalizeStatus(service.getStatus()));
        service.setCreatedAt(now);
        service.setUpdatedAt(now);

        document.set(toMap(service)).get();
        return service.getServiceId();
    }

    public boolean updateService(RoadService service) throws Exception {
        validate(service);

        String serviceId = clean(service.getServiceId());
        if (serviceId == null) {
            return false;
        }

        DocumentSnapshot existing = firestore
                .collection(COLLECTION)
                .document(serviceId)
                .get()
                .get();

        if (!existing.exists()) {
            return false;
        }

        if (nameExists(service.getName(), serviceId)) {
            throw new IllegalArgumentException("A service with this name already exists.");
        }

        Map<String, Object> data = toMap(service);
        data.put("serviceId", serviceId);
        data.put("name", clean(service.getName()));
        data.put("description", cleanOrEmpty(service.getDescription()));
        data.put("category", defaultValue(service.getCategory(), "General"));
        data.put("estimatedDuration", cleanOrEmpty(service.getEstimatedDuration()));
        data.put("status", normalizeStatus(service.getStatus()));
        data.put("updatedAt", now());

        firestore
                .collection(COLLECTION)
                .document(serviceId)
                .set(data, SetOptions.merge())
                .get();

        return true;
    }

    public boolean deleteService(String serviceId) throws Exception {
        RoadService service = getServiceById(serviceId);
        if (service == null) {
            return false;
        }

        if (isUsedByServiceRequest(service)) {
            return false;
        }

        firestore
                .collection(COLLECTION)
                .document(service.getServiceId())
                .delete()
                .get();

        return true;
    }

    public boolean isUsedByServiceRequest(RoadService service) throws Exception {
        if (service == null) {
            return false;
        }

        String serviceId = clean(service.getServiceId());
        if (serviceId != null) {
            QuerySnapshot byId = firestore
                    .collection(REQUEST_COLLECTION)
                    .whereEqualTo("serviceId", serviceId)
                    .limit(1)
                    .get()
                    .get();

            if (!byId.getDocuments().isEmpty()) {
                return true;
            }
        }

        String name = clean(service.getName());
        if (name != null) {
            QuerySnapshot byName = firestore
                    .collection(REQUEST_COLLECTION)
                    .whereEqualTo("serviceType", name)
                    .limit(1)
                    .get()
                    .get();

            return !byName.getDocuments().isEmpty();
        }

        return false;
    }

    private boolean nameExists(String name, String ignoreServiceId) throws Exception {
        name = clean(name);
        if (name == null) {
            return false;
        }

        for (RoadService service : getAllServices()) {
            if (ignoreServiceId != null
                    && ignoreServiceId.equals(service.getServiceId())) {
                continue;
            }

            if (name.equalsIgnoreCase(safe(service.getName()))) {
                return true;
            }
        }

        return false;
    }

    private RoadService fromDocument(DocumentSnapshot document) {
        if (document == null || !document.exists()) {
            return null;
        }

        RoadService service = new RoadService();
        service.setServiceId(firstString(document, "serviceId", "id"));
        if (clean(service.getServiceId()) == null) {
            service.setServiceId(document.getId());
        }

        service.setName(firstString(document, "name", "serviceName", "title"));
        service.setDescription(firstString(document, "description", "details"));
        service.setCategory(firstString(document, "category", "type"));
        service.setEstimatedDuration(firstString(document, "estimatedDuration", "duration"));
        service.setStatus(readStatus(document));
        service.setCreatedAt(firstString(document, "createdAt", "createdDate"));
        service.setUpdatedAt(firstString(document, "updatedAt", "updatedDate"));
        service.setBasePrice(readDouble(document, "basePrice", "price", "startingPrice"));

        return service;
    }

    private Map<String, Object> toMap(RoadService service) {
        Map<String, Object> data = new HashMap<>();
        data.put("serviceId", service.getServiceId());
        data.put("name", clean(service.getName()));
        data.put("serviceName", clean(service.getName()));
        data.put("description", cleanOrEmpty(service.getDescription()));
        data.put("category", defaultValue(service.getCategory(), "General"));
        data.put("basePrice", Math.max(0, service.getBasePrice()));
        data.put("price", Math.max(0, service.getBasePrice()));
        data.put("estimatedDuration", cleanOrEmpty(service.getEstimatedDuration()));
        data.put("status", normalizeStatus(service.getStatus()));
        data.put("active", "Active".equalsIgnoreCase(normalizeStatus(service.getStatus())));
        data.put("createdAt", cleanOrEmpty(service.getCreatedAt()));
        data.put("updatedAt", cleanOrEmpty(service.getUpdatedAt()));
        return data;
    }

    private void validate(RoadService service) {
        if (service == null) {
            throw new IllegalArgumentException("Service cannot be null.");
        }
        if (clean(service.getName()) == null) {
            throw new IllegalArgumentException("Service name is required.");
        }
        if (service.getBasePrice() < 0) {
            throw new IllegalArgumentException("Base price cannot be negative.");
        }
    }

    private String readStatus(DocumentSnapshot document) {
        String status = firstString(document, "status");
        if (status != null) {
            return normalizeStatus(status);
        }

        Boolean active = document.getBoolean("active");
        if (active != null) {
            return active ? "Active" : "Inactive";
        }

        return "Active";
    }

    private double readDouble(DocumentSnapshot document, String... fields) {
        for (String field : fields) {
            Object value = document.get(field);
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            if (value != null) {
                try {
                    String text = value.toString().replace("₹", "").replace(",", "").trim();
                    if (!text.isBlank()) {
                        return Double.parseDouble(text);
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return 0;
    }

    private String firstString(DocumentSnapshot document, String... fields) {
        for (String field : fields) {
            Object value = document.get(field);
            if (value != null) {
                String text = clean(value.toString());
                if (text != null) {
                    return text;
                }
            }
        }
        return null;
    }

    private String normalizeStatus(String status) {
        status = clean(status);
        return status != null && status.equalsIgnoreCase("Inactive")
                ? "Inactive"
                : "Active";
    }

    private String defaultValue(String value, String fallback) {
        value = clean(value);
        return value == null ? fallback : value;
    }

    private String cleanOrEmpty(String value) {
        value = clean(value);
        return value == null ? "" : value;
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String now() {
        return String.valueOf(System.currentTimeMillis());
    }
}
