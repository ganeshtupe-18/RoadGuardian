package project.dao.user;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import project.firebase.FirebaseConfig;
import project.model.Complaint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComplaintDAO {

    private static final String COLLECTION = "complaints";

    private final Firestore firestore;

    public ComplaintDAO() {
        try {
            this.firestore = FirebaseConfig.getFirestore();
        } catch (IOException e) {
            throw new RuntimeException("Unable to initialize Firebase.", e);
        }
    }

    public ComplaintDAO(Firestore firestore) {
        if (firestore == null) {
            throw new IllegalArgumentException("Firestore cannot be null.");
        }
        this.firestore = firestore;
    }

    public boolean createComplaint(Complaint complaint) throws Exception {
        if (complaint == null) {
            return false;
        }

        String customerId = normalizeId(firstNonBlank(
                complaint.getCustomerId(),
                complaint.getCustomerEmail()
        ));

        String email = normalizeEmail(firstNonBlank(
                complaint.getCustomerEmail(),
                customerId
        ));

        String subject = clean(complaint.getSubject());
        String description = clean(complaint.getDescription());

        if (customerId == null || subject == null || description == null) {
            return false;
        }

        DocumentReference reference = firestore
                .collection(COLLECTION)
                .document();

        long now = System.currentTimeMillis();

        complaint.setComplaintId(reference.getId());
        complaint.setCustomerId(customerId);
        complaint.setCustomerEmail(email);
        complaint.setCustomerName(firstNonBlank(
                complaint.getCustomerName(),
                "Customer"
        ));
        complaint.setSubject(subject);
        complaint.setDescription(description);
        complaint.setCategory(firstNonBlank(complaint.getCategory(), "Other"));
        complaint.setPriority(normalizePriority(complaint.getPriority()));
        complaint.setStatus("Pending");
        complaint.setRelatedRequestId(clean(complaint.getRelatedRequestId()));
        complaint.setAdminNote(null);
        complaint.setCreatedAt(now);
        complaint.setUpdatedAt(now);
        complaint.setResolvedAt(0L);
        complaint.setRejectedAt(0L);

        reference.set(toMap(complaint)).get();
        return true;
    }

    public List<Complaint> getMyComplaints(String customerIdentity) throws Exception {
        String identity = normalizeId(customerIdentity);
        List<Complaint> result = new ArrayList<>();

        if (identity == null) {
            return result;
        }

        QuerySnapshot snapshot = firestore
                .collection(COLLECTION)
                .get()
                .get();

        for (DocumentSnapshot document : snapshot.getDocuments()) {
            Complaint complaint = fromDocument(document);
            if (complaint == null) {
                continue;
            }

            if (sameIdentity(identity, complaint.getCustomerId())
                    || sameIdentity(identity, complaint.getCustomerEmail())) {
                result.add(complaint);
            }
        }

        result.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
        return result;
    }

    public Complaint getComplaintById(String complaintId, String customerIdentity) throws Exception {
        complaintId = clean(complaintId);
        String identity = normalizeId(customerIdentity);

        if (complaintId == null || identity == null) {
            return null;
        }

        DocumentSnapshot document = firestore
                .collection(COLLECTION)
                .document(complaintId)
                .get()
                .get();

        if (!document.exists()) {
            return null;
        }

        Complaint complaint = fromDocument(document);
        if (complaint == null) {
            return null;
        }

        if (!sameIdentity(identity, complaint.getCustomerId())
                && !sameIdentity(identity, complaint.getCustomerEmail())) {
            return null;
        }

        return complaint;
    }

    private Map<String, Object> toMap(Complaint complaint) {
        Map<String, Object> map = new HashMap<>();

        map.put("complaintId", complaint.getComplaintId());
        map.put("customerId", complaint.getCustomerId());
        map.put("customerName", complaint.getCustomerName());
        map.put("customerEmail", complaint.getCustomerEmail());
        map.put("subject", complaint.getSubject());
        map.put("category", complaint.getCategory());
        map.put("description", complaint.getDescription());
        map.put("priority", complaint.getPriority());
        map.put("status", complaint.getStatus());
        map.put("relatedRequestId", complaint.getRelatedRequestId());
        map.put("adminNote", complaint.getAdminNote());
        map.put("createdAt", complaint.getCreatedAt());
        map.put("updatedAt", complaint.getUpdatedAt());
        map.put("resolvedAt", complaint.getResolvedAt());
        map.put("rejectedAt", complaint.getRejectedAt());

        return map;
    }

    private Complaint fromDocument(DocumentSnapshot document) {
        if (document == null || !document.exists()) {
            return null;
        }

        Complaint complaint = new Complaint();
        complaint.setComplaintId(firstNonBlank(
                string(document, "complaintId"),
                document.getId()
        ));
        complaint.setCustomerId(normalizeId(firstNonBlank(
                string(document, "customerId"),
                string(document, "userId"),
                string(document, "customerEmail")
        )));
        complaint.setCustomerName(firstNonBlank(
                string(document, "customerName"),
                string(document, "userName"),
                "Customer"
        ));
        complaint.setCustomerEmail(normalizeEmail(firstNonBlank(
                string(document, "customerEmail"),
                string(document, "userEmail"),
                complaint.getCustomerId()
        )));
        complaint.setSubject(firstNonBlank(string(document, "subject"), "Complaint"));
        complaint.setCategory(firstNonBlank(string(document, "category"), "Other"));
        complaint.setDescription(firstNonBlank(string(document, "description"), "No description provided."));
        complaint.setPriority(normalizePriority(string(document, "priority")));
        complaint.setStatus(normalizeStatus(string(document, "status")));
        complaint.setRelatedRequestId(clean(firstNonBlank(
                string(document, "relatedRequestId"),
                string(document, "requestId")
        )));
        complaint.setAdminNote(clean(firstNonBlank(
                string(document, "adminNote"),
                string(document, "resolutionNote")
        )));
        complaint.setCreatedAt(longValue(document, "createdAt"));
        complaint.setUpdatedAt(longValue(document, "updatedAt"));
        complaint.setResolvedAt(longValue(document, "resolvedAt"));
        complaint.setRejectedAt(longValue(document, "rejectedAt"));

        return complaint;
    }

    private String string(DocumentSnapshot document, String field) {
        Object value = document.get(field);
        return value == null ? null : String.valueOf(value);
    }

    private long longValue(DocumentSnapshot document, String field) {
        Object value = document.get(field);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value != null) {
            try {
                return Long.parseLong(String.valueOf(value));
            } catch (Exception ignored) {
            }
        }
        return 0L;
    }

    private boolean sameIdentity(String first, String second) {
        String a = normalizeId(first);
        String b = normalizeId(second);
        return a != null && b != null && a.equalsIgnoreCase(b);
    }

    private String normalizeStatus(String status) {
        status = clean(status);
        if (status == null) {
            return "Pending";
        }
        if (status.equalsIgnoreCase("InProgress") || status.equalsIgnoreCase("Processing")) {
            return "In Progress";
        }
        if (status.equalsIgnoreCase("Closed") || status.equalsIgnoreCase("Completed")) {
            return "Resolved";
        }
        return status;
    }

    private String normalizePriority(String priority) {
        priority = clean(priority);
        if (priority == null) {
            return "Medium";
        }
        if (priority.equalsIgnoreCase("High")) {
            return "High";
        }
        if (priority.equalsIgnoreCase("Low")) {
            return "Low";
        }
        return "Medium";
    }

    private String normalizeId(String value) {
        value = clean(value);
        if (value == null) {
            return null;
        }
        return value.contains("@") ? value.toLowerCase() : value;
    }

    private String normalizeEmail(String value) {
        value = clean(value);
        if (value == null || !value.contains("@")) {
            return null;
        }
        return value.toLowerCase();
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
        return cleaned.isEmpty() ? null : cleaned;
    }
}
