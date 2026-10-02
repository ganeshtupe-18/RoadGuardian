package project.dao.user;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import project.firebase.FirebaseConfig;
import project.model.RoadService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ServiceCatalogDAO {

    private static final String COLLECTION = "services";

    private final Firestore firestore;

    public ServiceCatalogDAO() throws IOException {
        this(FirebaseConfig.getFirestore());
    }

    public ServiceCatalogDAO(Firestore firestore) {
        if (firestore == null) {
            throw new IllegalArgumentException("Firestore cannot be null.");
        }
        this.firestore = firestore;
    }

    public List<RoadService> getActiveServices() throws Exception {
        QuerySnapshot snapshot = firestore.collection(COLLECTION).get().get();
        List<RoadService> services = new ArrayList<>();

        for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
            RoadService service = fromDocument(document);
            if (service != null && service.isActive() && clean(service.getName()) != null) {
                services.add(service);
            }
        }

        services.sort(
                Comparator.comparing(
                        service -> service.getName().toLowerCase()
                )
        );

        return services;
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
        service.setBasePrice(readDouble(document, "basePrice", "price", "startingPrice"));
        service.setStatus(readStatus(document));
        service.setCreatedAt(firstString(document, "createdAt"));
        service.setUpdatedAt(firstString(document, "updatedAt"));
        return service;
    }

    private String readStatus(DocumentSnapshot document) {
        String status = firstString(document, "status");
        if (status != null) {
            return status.equalsIgnoreCase("Inactive") ? "Inactive" : "Active";
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
                    return Double.parseDouble(
                            value.toString().replace("₹", "").replace(",", "").trim()
                    );
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

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }
}
