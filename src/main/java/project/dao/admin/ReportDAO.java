package project.dao.admin;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import project.model.Report;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ReportDAO {

    private static final String REPORTS = "reports";
    private static final String USERS = "users";
    private static final String CUSTOMERS = "customers";
    private static final String MECHANICS = "mechanics";
    private static final String VEHICLES = "vehicles";
    private static final String SERVICE_REQUESTS = "serviceRequests";
    private static final String SOS_REQUESTS = "SOSRequests";
    private static final String LEGACY_SOS_REQUESTS_1 = "sosRequests";
    private static final String LEGACY_SOS_REQUESTS_2 = "sos_requests";
    private static final String COMPLAINTS = "complaints";
    private static final String REVIEWS = "reviews";

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    .withLocale(Locale.ENGLISH)
                    .withZone(ZoneId.systemDefault());

    private final Firestore firestore;

    public ReportDAO(Firestore firestore) {
        if (firestore == null) {
            throw new IllegalArgumentException("Firestore cannot be null.");
        }
        this.firestore = firestore;
    }

    // =========================================================
    // REPORT CRUD
    // =========================================================

    public List<Report> getAllReports() throws Exception {
        List<Report> result = new ArrayList<>();

        QuerySnapshot snapshot = firestore
                .collection(REPORTS)
                .get()
                .get();

        for (DocumentSnapshot document : snapshot.getDocuments()) {
            Report report = document.toObject(Report.class);
            if (report == null) {
                continue;
            }
            if (blank(report.getReportId())) {
                report.setReportId(document.getId());
            }
            result.add(report);
        }

        result.sort(
                Comparator.comparingLong(Report::getGeneratedAt)
                        .reversed()
        );

        return result;
    }

    public Report getReportById(String reportId) throws Exception {
        String id = clean(reportId);
        if (id == null) {
            return null;
        }

        DocumentSnapshot document = firestore
                .collection(REPORTS)
                .document(id)
                .get()
                .get();

        if (!document.exists()) {
            return null;
        }

        Report report = document.toObject(Report.class);
        if (report != null && blank(report.getReportId())) {
            report.setReportId(document.getId());
        }
        return report;
    }

    public Report generateReport(
            String reportName,
            String reportType,
            String generatedBy,
            String generatedByEmail
    ) throws Exception {

        String name = clean(reportName);
        String type = normalizeReportType(reportType);

        if (name == null) {
            throw new IllegalArgumentException("Report name is required.");
        }

        long now = System.currentTimeMillis();
        String reportId = createReportId();

        SnapshotData snapshot = buildSnapshot(type);

        Report report = new Report();
        report.setReportId(reportId);
        report.setName(name);
        report.setType(type);
        report.setGeneratedBy(firstNonBlank(generatedBy, "Admin"));
        report.setGeneratedByEmail(clean(generatedByEmail));
        report.setGeneratedDate(DATE_FORMAT.format(Instant.ofEpochMilli(now)));
        report.setGeneratedAt(now);
        report.setRecords(snapshot.records);
        report.setStatus("Generated");
        report.setSummary(snapshot.summary);
        report.setMetrics(snapshot.metrics);

        firestore
                .collection(REPORTS)
                .document(reportId)
                .set(report)
                .get();

        return report;
    }

    public boolean deleteReport(String reportId) throws Exception {
        String id = clean(reportId);
        if (id == null) {
            return false;
        }

        DocumentReference reference = firestore
                .collection(REPORTS)
                .document(id);

        if (!reference.get().get().exists()) {
            return false;
        }

        reference.delete().get();
        return true;
    }

    // =========================================================
    // SNAPSHOT BUILDER
    // =========================================================

    private SnapshotData buildSnapshot(String type) throws Exception {
        switch (normalizeReportType(type)) {
            case "SOS":
                return buildSOSSnapshot();
            case "Customer":
                return buildCustomerSnapshot();
            case "Mechanic":
                return buildMechanicSnapshot();
            case "Financial":
                return buildFinancialSnapshot();
            case "System":
                return buildSystemSnapshot();
            case "Service":
            default:
                return buildServiceSnapshot();
        }
    }

    private SnapshotData buildServiceSnapshot() throws Exception {
        List<DocumentSnapshot> services = getDocuments(SERVICE_REQUESTS);
        Map<String, String> metrics = new LinkedHashMap<>();

        int pending = countStatus(services, "Pending");
        int assigned = countStatus(services, "Assigned");
        int accepted = countStatus(services, "Accepted");
        int inProgress = countStatus(services, "In Progress");
        int completed = countStatus(services, "Completed");
        int cancelled = countStatus(services, "Cancelled");
        double revenue = completedRevenue(services);

        metrics.put("Total Service Requests", String.valueOf(services.size()));
        metrics.put("Pending", String.valueOf(pending));
        metrics.put("Assigned", String.valueOf(assigned));
        metrics.put("Accepted", String.valueOf(accepted));
        metrics.put("In Progress", String.valueOf(inProgress));
        metrics.put("Completed", String.valueOf(completed));
        metrics.put("Cancelled", String.valueOf(cancelled));
        metrics.put("Completed Service Revenue", formatMoney(revenue));

        return new SnapshotData(
                services.size(),
                "Live service-request snapshot from the canonical serviceRequests collection.",
                metrics
        );
    }

    private SnapshotData buildSOSSnapshot() throws Exception {
        List<DocumentSnapshot> sos = getMergedDocuments(
                SOS_REQUESTS,
                LEGACY_SOS_REQUESTS_1,
                LEGACY_SOS_REQUESTS_2
        );

        Map<String, String> metrics = new LinkedHashMap<>();
        metrics.put("Total SOS Requests", String.valueOf(sos.size()));
        metrics.put("Pending", String.valueOf(countStatus(sos, "Pending")));
        metrics.put("Assigned", String.valueOf(countStatus(sos, "Assigned")));
        metrics.put("Accepted", String.valueOf(countStatus(sos, "Accepted")));
        metrics.put("In Progress", String.valueOf(countStatus(sos, "In Progress")));
        metrics.put("Resolved", String.valueOf(countStatus(sos, "Resolved")));
        metrics.put("Cancelled", String.valueOf(countStatus(sos, "Cancelled")));

        return new SnapshotData(
                sos.size(),
                "Emergency-request snapshot using canonical SOSRequests data with legacy aliases deduplicated for reporting.",
                metrics
        );
    }

    private SnapshotData buildCustomerSnapshot() throws Exception {
        int customers = countUniqueCustomers();
        List<DocumentSnapshot> vehicles = getDocuments(VEHICLES);
        List<DocumentSnapshot> services = getDocuments(SERVICE_REQUESTS);
        List<DocumentSnapshot> complaints = getDocuments(COMPLAINTS);
        List<DocumentSnapshot> reviews = getDocuments(REVIEWS);

        Map<String, String> metrics = new LinkedHashMap<>();
        metrics.put("Registered Customers", String.valueOf(customers));
        metrics.put("Registered Vehicles", String.valueOf(vehicles.size()));
        metrics.put("Service Requests", String.valueOf(services.size()));
        metrics.put("Completed Services", String.valueOf(countStatus(services, "Completed")));
        metrics.put("Complaints", String.valueOf(complaints.size()));
        metrics.put("Resolved Complaints", String.valueOf(countStatus(complaints, "Resolved")));
        metrics.put("Reviews Submitted", String.valueOf(reviews.size()));
        metrics.put("Average Review Rating", formatRating(averageRating(reviews)));

        return new SnapshotData(
                customers,
                "Customer activity snapshot across users/customers, vehicles, service requests, complaints and reviews.",
                metrics
        );
    }

    private SnapshotData buildMechanicSnapshot() throws Exception {
        List<DocumentSnapshot> mechanics = getDocuments(MECHANICS);
        List<DocumentSnapshot> services = getDocuments(SERVICE_REQUESTS);
        List<DocumentSnapshot> reviews = getDocuments(REVIEWS);

        int available = 0;
        for (DocumentSnapshot mechanic : mechanics) {
            Object availableValue = mechanic.get("available");
            String status = string(mechanic, "status");

            if (Boolean.TRUE.equals(availableValue)
                    || "Available".equalsIgnoreCase(status)
                    || "Active".equalsIgnoreCase(status)) {
                available++;
            }
        }

        Map<String, String> metrics = new LinkedHashMap<>();
        metrics.put("Registered Mechanics", String.valueOf(mechanics.size()));
        metrics.put("Available / Active", String.valueOf(available));
        metrics.put("Assigned Requests", String.valueOf(countStatus(services, "Assigned")));
        metrics.put("Accepted Requests", String.valueOf(countStatus(services, "Accepted")));
        metrics.put("Jobs In Progress", String.valueOf(countStatus(services, "In Progress")));
        metrics.put("Completed Jobs", String.valueOf(countStatus(services, "Completed")));
        metrics.put("Customer Reviews", String.valueOf(reviews.size()));
        metrics.put("Average Customer Rating", formatRating(averageRating(reviews)));

        return new SnapshotData(
                mechanics.size(),
                "Mechanic operations snapshot based on registered mechanics, service lifecycle and customer reviews.",
                metrics
        );
    }

    private SnapshotData buildFinancialSnapshot() throws Exception {
        List<DocumentSnapshot> services = getDocuments(SERVICE_REQUESTS);

        int completed = countStatus(services, "Completed");
        int cancelled = countStatus(services, "Cancelled");
        double revenue = completedRevenue(services);
        double average = completed == 0 ? 0.0 : revenue / completed;
        double towRevenue = completedRevenueByType(services, "Tow Truck");

        Map<String, String> metrics = new LinkedHashMap<>();
        metrics.put("Completed Paid Jobs", String.valueOf(completed));
        metrics.put("Cancelled Requests", String.valueOf(cancelled));
        metrics.put("Recorded Service Revenue", formatMoney(revenue));
        metrics.put("Average Completed Job Value", formatMoney(average));
        metrics.put("Recorded Tow Truck Revenue", formatMoney(towRevenue));

        return new SnapshotData(
                completed,
                "Financial snapshot calculated only from completed serviceRequests records that contain a stored cost/amount value.",
                metrics
        );
    }

    private SnapshotData buildSystemSnapshot() throws Exception {
        int customers = countUniqueCustomers();
        List<DocumentSnapshot> mechanics = getDocuments(MECHANICS);
        List<DocumentSnapshot> vehicles = getDocuments(VEHICLES);
        List<DocumentSnapshot> services = getDocuments(SERVICE_REQUESTS);
        List<DocumentSnapshot> sos = getMergedDocuments(
                SOS_REQUESTS,
                LEGACY_SOS_REQUESTS_1,
                LEGACY_SOS_REQUESTS_2
        );
        List<DocumentSnapshot> complaints = getDocuments(COMPLAINTS);
        List<DocumentSnapshot> reviews = getDocuments(REVIEWS);

        int totalRecords = customers
                + mechanics.size()
                + vehicles.size()
                + services.size()
                + sos.size()
                + complaints.size()
                + reviews.size();

        Map<String, String> metrics = new LinkedHashMap<>();
        metrics.put("Customers", String.valueOf(customers));
        metrics.put("Mechanics", String.valueOf(mechanics.size()));
        metrics.put("Vehicles", String.valueOf(vehicles.size()));
        metrics.put("Service Requests", String.valueOf(services.size()));
        metrics.put("Completed Services", String.valueOf(countStatus(services, "Completed")));
        metrics.put("SOS Requests", String.valueOf(sos.size()));
        metrics.put("Resolved SOS", String.valueOf(countStatus(sos, "Resolved")));
        metrics.put("Complaints", String.valueOf(complaints.size()));
        metrics.put("Reviews", String.valueOf(reviews.size()));
        metrics.put("Recorded Revenue", formatMoney(completedRevenue(services)));

        return new SnapshotData(
                totalRecords,
                "Complete RoadGuardian operational snapshot across customers, mechanics, vehicles, service requests, SOS, complaints and reviews.",
                metrics
        );
    }

    // =========================================================
    // FIRESTORE HELPERS
    // =========================================================

    private List<DocumentSnapshot> getDocuments(String collection) throws Exception {
        List<DocumentSnapshot> result = new ArrayList<>();
        QuerySnapshot snapshot = firestore.collection(collection).get().get();
        result.addAll(snapshot.getDocuments());
        return result;
    }

    private List<DocumentSnapshot> getMergedDocuments(String... collections) throws Exception {
        Map<String, DocumentSnapshot> merged = new LinkedHashMap<>();

        if (collections == null) {
            return new ArrayList<>();
        }

        for (String collection : collections) {
            if (blank(collection)) {
                continue;
            }
            QuerySnapshot snapshot = firestore.collection(collection).get().get();
            for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
                merged.putIfAbsent(document.getId(), document);
            }
        }

        return new ArrayList<>(merged.values());
    }

    private int countUniqueCustomers() throws Exception {
        Set<String> customerKeys = new LinkedHashSet<>();

        for (DocumentSnapshot user : getDocuments(USERS)) {
            String role = string(user, "role");
            if (role != null
                    && !role.equalsIgnoreCase("User")
                    && !role.equalsIgnoreCase("Customer")) {
                continue;
            }
            customerKeys.add(identityKey(user));
        }

        for (DocumentSnapshot customer : getDocuments(CUSTOMERS)) {
            customerKeys.add(identityKey(customer));
        }

        customerKeys.removeIf(value -> value == null || value.isBlank());
        return customerKeys.size();
    }

    private String identityKey(DocumentSnapshot document) {
        String value = firstNonBlank(
                string(document, "email"),
                string(document, "userEmail"),
                string(document, "customerId"),
                string(document, "userId"),
                document == null ? null : document.getId()
        );
        return value == null ? null : value.toLowerCase(Locale.ENGLISH);
    }

    // =========================================================
    // CALCULATIONS
    // =========================================================

    private int countStatus(List<DocumentSnapshot> documents, String wantedStatus) {
        int count = 0;
        for (DocumentSnapshot document : documents) {
            String status = normalizeStatus(string(document, "status"));
            if (status.equalsIgnoreCase(normalizeStatus(wantedStatus))) {
                count++;
            }
        }
        return count;
    }

    private double completedRevenue(List<DocumentSnapshot> services) {
        double total = 0.0;
        for (DocumentSnapshot service : services) {
            if (!"Completed".equalsIgnoreCase(normalizeStatus(string(service, "status")))) {
                continue;
            }
            total += serviceAmount(service);
        }
        return total;
    }

    private double completedRevenueByType(List<DocumentSnapshot> services, String wantedType) {
        double total = 0.0;
        for (DocumentSnapshot service : services) {
            if (!"Completed".equalsIgnoreCase(normalizeStatus(string(service, "status")))) {
                continue;
            }
            String type = firstNonBlank(
                    string(service, "serviceType"),
                    string(service, "type"),
                    string(service, "source")
            );
            if (type != null && type.equalsIgnoreCase(wantedType)) {
                total += serviceAmount(service);
            }
        }
        return total;
    }

    private double serviceAmount(DocumentSnapshot document) {
        String[] fields = {
                "finalCost",
                "totalCost",
                "amount",
                "cost",
                "estimatedCost",
                "towCharge"
        };

        for (String field : fields) {
            double value = number(document.get(field));
            if (value > 0.0) {
                return value;
            }
        }

        double parts = number(document.get("partsCost"));
        double labour = number(document.get("labourCost"));
        return Math.max(0.0, parts + labour);
    }

    private double averageRating(List<DocumentSnapshot> reviews) {
        double total = 0.0;
        int count = 0;

        for (DocumentSnapshot review : reviews) {
            double rating = number(review.get("rating"));
            if (rating <= 0.0) {
                rating = number(review.get("customerRating"));
            }
            if (rating > 0.0) {
                total += rating;
                count++;
            }
        }

        return count == 0 ? 0.0 : total / count;
    }

    private double number(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value == null) {
            return 0.0;
        }
        try {
            String text = String.valueOf(value)
                    .replace("₹", "")
                    .replace("INR", "")
                    .replace(",", "")
                    .trim();
            return Double.parseDouble(text);
        } catch (Exception ignored) {
            return 0.0;
        }
    }

    // =========================================================
    // NORMALIZATION
    // =========================================================

    private String normalizeReportType(String type) {
        String cleaned = clean(type);
        if (cleaned == null) {
            return "Service";
        }
        if (cleaned.equalsIgnoreCase("SOS")) return "SOS";
        if (cleaned.equalsIgnoreCase("Customer")) return "Customer";
        if (cleaned.equalsIgnoreCase("Mechanic")) return "Mechanic";
        if (cleaned.equalsIgnoreCase("Financial")) return "Financial";
        if (cleaned.equalsIgnoreCase("System")
                || cleaned.equalsIgnoreCase("Full System")) return "System";
        return "Service";
    }

    private String normalizeStatus(String status) {
        String cleaned = clean(status);
        if (cleaned == null) return "Pending";
        if (cleaned.equalsIgnoreCase("InProgress")
                || cleaned.equalsIgnoreCase("Active")
                || cleaned.equalsIgnoreCase("Started")) return "In Progress";
        if (cleaned.equalsIgnoreCase("Complete")
                || cleaned.equalsIgnoreCase("Done")
                || cleaned.equalsIgnoreCase("Closed")) return "Completed";
        if (cleaned.equalsIgnoreCase("Canceled")) return "Cancelled";
        return cleaned;
    }

    private String string(DocumentSnapshot document, String field) {
        if (document == null || field == null) {
            return null;
        }
        Object value = document.get(field);
        return value == null ? null : clean(String.valueOf(value));
    }

    private String createReportId() {
        return "RPT-" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 10)
                .toUpperCase(Locale.ENGLISH);
    }

    private String formatMoney(double value) {
        return String.format(Locale.ENGLISH, "INR %,.2f", Math.max(0.0, value));
    }

    private String formatRating(double value) {
        if (value <= 0.0) {
            return "No ratings yet";
        }
        return String.format(Locale.ENGLISH, "%.2f / 5", value);
    }

    private String firstNonBlank(String... values) {
        if (values == null) return null;
        for (String value : values) {
            String cleaned = clean(value);
            if (cleaned != null) return cleaned;
        }
        return null;
    }

    private String clean(String value) {
        if (value == null) return null;
        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private boolean blank(String value) {
        return clean(value) == null;
    }

    private static final class SnapshotData {
        private final int records;
        private final String summary;
        private final Map<String, String> metrics;

        private SnapshotData(
                int records,
                String summary,
                Map<String, String> metrics
        ) {
            this.records = Math.max(0, records);
            this.summary = summary;
            this.metrics = metrics == null
                    ? new LinkedHashMap<>()
                    : new LinkedHashMap<>(metrics);
        }
    }
}
