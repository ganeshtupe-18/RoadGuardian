package project.dao.admin;

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

    public List<Complaint> getAllComplaints() throws Exception {
        List<Complaint> result = new ArrayList<>();

        QuerySnapshot snapshot = firestore
                .collection(COLLECTION)
                .get()
                .get();

        for (DocumentSnapshot document : snapshot.getDocuments()) {
            Complaint complaint = fromDocument(document);
            if (complaint != null) {
                result.add(complaint);
            }
        }

        result.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
        return result;
    }

    public Complaint getComplaintById(String complaintId) throws Exception {
        complaintId = clean(complaintId);
        if (complaintId == null) {
            return null;
        }

        DocumentSnapshot document = firestore
                .collection(COLLECTION)
                .document(complaintId)
                .get()
                .get();

        if (document.exists()) {
            return fromDocument(document);
        }

        QuerySnapshot query = firestore
                .collection(COLLECTION)
                .whereEqualTo("complaintId", complaintId)
                .limit(1)
                .get()
                .get();

        if (query.isEmpty()) {
            return null;
        }

        return fromDocument(query.getDocuments().get(0));
    }

    public boolean updateStatus(
            String complaintId,
            String newStatus,
            String adminNote
    ) throws Exception {

        complaintId = clean(complaintId);
        newStatus = normalizeStatus(newStatus);
        adminNote = clean(adminNote);

        if (complaintId == null || newStatus == null) {
            return false;
        }

        Complaint current = getComplaintById(complaintId);
        if (current == null) {
            return false;
        }

        String currentStatus = normalizeStatus(current.getStatus());

        if (isFinal(currentStatus)) {
            return currentStatus.equalsIgnoreCase(newStatus);
        }

        if (!isAllowedTransition(currentStatus, newStatus)) {
            return false;
        }

        if ("Rejected".equalsIgnoreCase(newStatus) && adminNote == null) {
            return false;
        }

        long now = System.currentTimeMillis();

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        updates.put("adminNote", adminNote);
        updates.put("updatedAt", now);

        if ("Resolved".equalsIgnoreCase(newStatus)) {
            updates.put("resolvedAt", now);
            updates.put("rejectedAt", 0L);
        } else if ("Rejected".equalsIgnoreCase(newStatus)) {
            updates.put("rejectedAt", now);
            updates.put("resolvedAt", 0L);
        }

        DocumentReference reference = firestore
                .collection(COLLECTION)
                .document(current.getComplaintId());

        reference.update(updates).get();
        return true;
    }

    private boolean isAllowedTransition(String current, String next) {
        if (current.equalsIgnoreCase(next)) {
            return true;
        }

        if ("Pending".equalsIgnoreCase(current)) {
            return "In Progress".equalsIgnoreCase(next)
                    || "Resolved".equalsIgnoreCase(next)
                    || "Rejected".equalsIgnoreCase(next);
        }

        if ("In Progress".equalsIgnoreCase(current)) {
            return "Resolved".equalsIgnoreCase(next)
                    || "Rejected".equalsIgnoreCase(next);
        }

        return false;
    }

    private boolean isFinal(String status) {
        return "Resolved".equalsIgnoreCase(status)
                || "Rejected".equalsIgnoreCase(status);
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
        if (status.equalsIgnoreCase("Declined")) {
            return "Rejected";
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
