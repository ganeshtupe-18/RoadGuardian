package project.dao.user;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteBatch;

import project.model.Notification;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NotificationDAO {

    private static final String NOTIFICATIONS = "notifications";
    private static final String READS = "notificationReads";
    private static final String SERVICE_REQUESTS = "serviceRequests";
    private static final String SOS_REQUESTS = "SOSRequests";

    private final Firestore firestore;

    public NotificationDAO(Firestore firestore) {
        if (firestore == null) {
            throw new IllegalArgumentException("Firestore cannot be null.");
        }
        this.firestore = firestore;
    }

    public List<Notification> getNotifications(String userId, String userEmail) throws Exception {
        Set<String> identities = buildIdentities(userId, userEmail);
        String readerKey = readerKey(userId, userEmail);

        if (identities.isEmpty() || readerKey == null) {
            return new ArrayList<>();
        }

        Set<String> readIds = loadReadNotificationIds(readerKey);
        Map<String, Notification> result = new LinkedHashMap<>();

        loadAdminNotifications(result, identities);
        loadServiceNotifications(result, identities);
        loadSOSNotifications(result, identities);

        List<Notification> notifications = new ArrayList<>(result.values());

        for (Notification notification : notifications) {
            notification.setRead(readIds.contains(notification.getNotificationId()));
        }

        notifications.sort(
                Comparator.comparingLong(Notification::getCreatedAt).reversed()
        );

        return notifications;
    }

    public boolean markRead(String userId, String userEmail, String notificationId) throws Exception {
        String readerKey = readerKey(userId, userEmail);
        notificationId = clean(notificationId);

        if (readerKey == null || notificationId == null) {
            return false;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("readerKey", readerKey);
        data.put("userId", firstNonBlank(normalizeId(userId), normalizeId(userEmail), readerKey));
        data.put("userEmail", normalizeId(userEmail));
        data.put("notificationId", notificationId);
        data.put("readAt", System.currentTimeMillis());

        firestore.collection(READS)
                .document(readDocumentId(readerKey, notificationId))
                .set(data, SetOptions.merge())
                .get();

        return true;
    }

    public boolean markAllRead(
            String userId,
            String userEmail,
            List<Notification> notifications
    ) throws Exception {
        String readerKey = readerKey(userId, userEmail);

        if (readerKey == null || notifications == null || notifications.isEmpty()) {
            return true;
        }

        int count = 0;
        WriteBatch batch = firestore.batch();

        for (Notification notification : notifications) {
            if (notification == null || notification.isRead()) {
                continue;
            }

            String notificationId = clean(notification.getNotificationId());
            if (notificationId == null) {
                continue;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("readerKey", readerKey);
            data.put("userId", firstNonBlank(normalizeId(userId), normalizeId(userEmail), readerKey));
            data.put("userEmail", normalizeId(userEmail));
            data.put("notificationId", notificationId);
            data.put("readAt", System.currentTimeMillis());

            DocumentReference reference = firestore.collection(READS)
                    .document(readDocumentId(readerKey, notificationId));

            batch.set(reference, data, SetOptions.merge());
            count++;

            if (count == 400) {
                batch.commit().get();
                batch = firestore.batch();
                count = 0;
            }
        }

        if (count > 0) {
            batch.commit().get();
        }

        return true;
    }

    private void loadAdminNotifications(
            Map<String, Notification> result,
            Set<String> identities
    ) throws Exception {
        QuerySnapshot snapshot = firestore.collection(NOTIFICATIONS).get().get();

        for (DocumentSnapshot document : snapshot.getDocuments()) {
            boolean broadcast = booleanValue(document, "broadcast", "isBroadcast");
            String recipientId = normalizeId(firstNonBlank(
                    document.getString("recipientId"),
                    document.getString("customerId"),
                    document.getString("userId"),
                    document.getString("userEmail")
            ));

            if (!broadcast && (recipientId == null || !identities.contains(recipientId))) {
                continue;
            }

            Notification notification = new Notification();
            notification.setNotificationId("admin_" + document.getId());
            notification.setRecipientId(recipientId);
            notification.setRecipientName(document.getString("recipientName"));
            notification.setTitle(firstNonBlank(document.getString("title"), "RoadGuardian Update"));
            notification.setMessage(firstNonBlank(
                    document.getString("message"),
                    document.getString("description"),
                    "You have a new RoadGuardian update."
            ));
            notification.setType(firstNonBlank(document.getString("type"), "Admin"));
            notification.setSource(firstNonBlank(document.getString("source"), "Admin"));
            notification.setRelatedId(document.getString("relatedId"));
            notification.setBroadcast(broadcast);
            notification.setReminder(booleanValue(document, "reminder")
                    || "Reminder".equalsIgnoreCase(notification.getType()));
            notification.setStatus(firstNonBlank(document.getString("status"), "Sent"));
            notification.setCreatedAt(eventTime(document, "createdAt", "updatedAt", "date", "timestamp"));
            notification.setUpdatedAt(eventTime(document, "updatedAt", "createdAt"));

            putIfValid(result, notification);
        }
    }

    private void loadServiceNotifications(
            Map<String, Notification> result,
            Set<String> identities
    ) throws Exception {
        QuerySnapshot snapshot = firestore.collection(SERVICE_REQUESTS).get().get();

        for (DocumentSnapshot document : snapshot.getDocuments()) {
            if (!belongsToCustomer(document, identities)) {
                continue;
            }

            String requestId = document.getId();
            String service = firstNonBlank(
                    document.getString("serviceType"),
                    document.getString("serviceName"),
                    "service request"
            );
            String vehicle = firstNonBlank(
                    document.getString("vehicleNumber"),
                    document.getString("registrationNumber"),
                    document.getString("vehicleId"),
                    "your vehicle"
            );
            String mechanic = firstNonBlank(
                    document.getString("mechanicName"),
                    document.getString("assignedMechanicName"),
                    document.getString("mechanicId"),
                    "A mechanic"
            );
            String location = firstNonBlank(document.getString("location"), "your location");
            String status = normalizeServiceStatus(document.getString("status"));

            long created = eventTime(document, "requestDate", "createdAt", "date");
            addSystemEvent(
                    result,
                    "service_" + requestId + "_created",
                    "Service request created",
                    "Your " + service + " request for " + vehicle + " was created.",
                    "Service",
                    "Service Request",
                    requestId,
                    created
            );

            long assigned = eventTime(document, "assignedDate");
            if (assigned == 0L && document.getString("mechanicId") != null
                    && ("Assigned".equals(status) || "Accepted".equals(status)
                    || "In Progress".equals(status) || "Completed".equals(status))) {
                assigned = eventTime(document, "updatedAt", "requestDate");
            }
            addSystemEvent(
                    result,
                    "service_" + requestId + "_assigned",
                    "Mechanic assigned",
                    mechanic + " has been assigned to your " + service + " request for " + vehicle + ".",
                    "Service",
                    "Service Request",
                    requestId,
                    assigned
            );

            long accepted = eventTime(document, "acceptedDate");
            if (accepted == 0L && ("Accepted".equals(status)
                    || "In Progress".equals(status) || "Completed".equals(status))) {
                accepted = eventTime(document, "updatedAt", "assignedDate");
            }
            addSystemEvent(
                    result,
                    "service_" + requestId + "_accepted",
                    "Mechanic accepted request",
                    mechanic + " accepted your " + service + " request.",
                    "Service",
                    "Service Request",
                    requestId,
                    accepted
            );

            long navigationStarted = eventTime(document, "navigationStartedDate", "navigationStartedAt");
            if (navigationStarted == 0L
                    && "Started".equalsIgnoreCase(document.getString("navigationStatus"))) {
                navigationStarted = eventTime(document, "updatedAt", "acceptedDate");
            }
            addSystemEvent(
                    result,
                    "service_" + requestId + "_navigation",
                    "Mechanic started navigation",
                    mechanic + " started navigation to " + location + ".",
                    "Service",
                    "Navigation",
                    requestId,
                    navigationStarted
            );

            long arrived = eventTime(document, "arrivedDate", "arrivalDate", "arrivedAt");
            if (arrived == 0L
                    && "Arrived".equalsIgnoreCase(document.getString("arrivalStatus"))) {
                arrived = eventTime(document, "updatedAt", "navigationStartedDate");
            }
            addSystemEvent(
                    result,
                    "service_" + requestId + "_arrived",
                    "Mechanic arrived",
                    mechanic + " has arrived at " + location + ".",
                    "Service",
                    "Navigation",
                    requestId,
                    arrived
            );

            long started = eventTime(document, "startedDate", "inProgressDate");
            if (started == 0L && ("In Progress".equals(status) || "Completed".equals(status))) {
                started = eventTime(document, "updatedAt", "acceptedDate");
            }
            addSystemEvent(
                    result,
                    "service_" + requestId + "_progress",
                    "Service in progress",
                    "Work has started on " + vehicle + " for " + service + ".",
                    "Service",
                    "Service Request",
                    requestId,
                    started
            );

            long completed = eventTime(document, "completedDate");
            if (completed == 0L && "Completed".equals(status)) {
                completed = eventTime(document, "updatedAt", "startedDate", "requestDate");
            }
            addSystemEvent(
                    result,
                    "service_" + requestId + "_completed",
                    "Service completed",
                    "Your " + service + " request for " + vehicle + " has been completed.",
                    "Service",
                    "Service Request",
                    requestId,
                    completed
            );

            long cancelled = eventTime(document, "cancelledDate");
            if (cancelled == 0L && "Cancelled".equals(status)) {
                cancelled = eventTime(document, "updatedAt", "requestDate");
            }
            addSystemEvent(
                    result,
                    "service_" + requestId + "_cancelled",
                    "Service request cancelled",
                    "Your " + service + " request for " + vehicle + " was cancelled.",
                    "Service",
                    "Service Request",
                    requestId,
                    cancelled
            );
        }
    }

    private void loadSOSNotifications(
            Map<String, Notification> result,
            Set<String> identities
    ) throws Exception {
        QuerySnapshot snapshot = firestore.collection(SOS_REQUESTS).get().get();

        for (DocumentSnapshot document : snapshot.getDocuments()) {
            if (!belongsToCustomer(document, identities)) {
                continue;
            }

            String sosId = document.getId();
            String emergencyType = firstNonBlank(
                    document.getString("emergencyType"),
                    document.getString("type"),
                    "Emergency SOS"
            );
            String location = firstNonBlank(document.getString("location"), "your location");
            String mechanic = firstNonBlank(
                    document.getString("mechanicName"),
                    document.getString("responderName"),
                    document.getString("mechanicId"),
                    "A responder"
            );
            String status = normalizeSOSStatus(document.getString("status"));

            addSystemEvent(
                    result,
                    "sos_" + sosId + "_created",
                    "SOS activated",
                    emergencyType + " was sent from " + location + ".",
                    "SOS",
                    "SOS",
                    sosId,
                    eventTime(document, "requestDate", "createdAt", "date")
            );

            long assigned = eventTime(document, "assignedDate");
            if (assigned == 0L && document.getString("mechanicId") != null
                    && ("Assigned".equals(status) || "Accepted".equals(status)
                    || "In Progress".equals(status) || "Resolved".equals(status))) {
                assigned = eventTime(document, "updatedAt", "requestDate");
            }
            addSystemEvent(
                    result,
                    "sos_" + sosId + "_assigned",
                    "SOS responder assigned",
                    mechanic + " has been assigned to your " + emergencyType + ".",
                    "SOS",
                    "SOS",
                    sosId,
                    assigned
            );

            long accepted = eventTime(document, "acceptedDate");
            if (accepted == 0L && ("Accepted".equals(status)
                    || "In Progress".equals(status) || "Resolved".equals(status))) {
                accepted = eventTime(document, "updatedAt", "assignedDate");
            }
            addSystemEvent(
                    result,
                    "sos_" + sosId + "_accepted",
                    "SOS accepted",
                    mechanic + " accepted your emergency request.",
                    "SOS",
                    "SOS",
                    sosId,
                    accepted
            );

            long started = eventTime(document, "startedDate", "inProgressDate");
            if (started == 0L && ("In Progress".equals(status) || "Resolved".equals(status))) {
                started = eventTime(document, "updatedAt", "acceptedDate");
            }
            addSystemEvent(
                    result,
                    "sos_" + sosId + "_progress",
                    "SOS response in progress",
                    mechanic + " is responding to your emergency request.",
                    "SOS",
                    "SOS",
                    sosId,
                    started
            );

            long resolved = eventTime(document, "resolvedDate");
            if (resolved == 0L && "Resolved".equals(status)) {
                resolved = eventTime(document, "updatedAt", "startedDate", "requestDate");
            }
            addSystemEvent(
                    result,
                    "sos_" + sosId + "_resolved",
                    "SOS resolved",
                    "Your " + emergencyType + " has been marked as resolved.",
                    "SOS",
                    "SOS",
                    sosId,
                    resolved
            );

            long cancelled = eventTime(document, "cancelledDate");
            if (cancelled == 0L && "Cancelled".equals(status)) {
                cancelled = eventTime(document, "updatedAt", "requestDate");
            }
            addSystemEvent(
                    result,
                    "sos_" + sosId + "_cancelled",
                    "SOS cancelled",
                    "Your " + emergencyType + " was cancelled.",
                    "SOS",
                    "SOS",
                    sosId,
                    cancelled
            );
        }
    }

    private void addSystemEvent(
            Map<String, Notification> result,
            String id,
            String title,
            String message,
            String type,
            String source,
            String relatedId,
            long createdAt
    ) {
        if (createdAt <= 0L) {
            return;
        }

        Notification notification = new Notification();
        notification.setNotificationId(id);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setSource(source);
        notification.setRelatedId(relatedId);
        notification.setStatus("System");
        notification.setCreatedAt(createdAt);
        notification.setUpdatedAt(createdAt);
        notification.setBroadcast(false);
        notification.setReminder(false);

        putIfValid(result, notification);
    }

    private void putIfValid(Map<String, Notification> result, Notification notification) {
        if (result == null || notification == null) {
            return;
        }

        String id = clean(notification.getNotificationId());
        String title = clean(notification.getTitle());
        String message = clean(notification.getMessage());

        if (id == null || title == null || message == null) {
            return;
        }

        if (notification.getCreatedAt() <= 0L) {
            notification.setCreatedAt(System.currentTimeMillis());
        }

        result.putIfAbsent(id, notification);
    }

    private Set<String> loadReadNotificationIds(String readerKey) throws Exception {
        Set<String> readIds = new HashSet<>();
        QuerySnapshot snapshot = firestore.collection(READS).get().get();

        for (DocumentSnapshot document : snapshot.getDocuments()) {
            String storedReader = normalizeId(document.getString("readerKey"));
            if (storedReader == null || !storedReader.equals(normalizeId(readerKey))) {
                continue;
            }

            String notificationId = clean(document.getString("notificationId"));
            if (notificationId != null) {
                readIds.add(notificationId);
            }
        }

        return readIds;
    }

    private boolean belongsToCustomer(DocumentSnapshot document, Set<String> identities) {
        if (document == null || identities == null || identities.isEmpty()) {
            return false;
        }

        String[] fields = {
                "customerId",
                "userId",
                "userEmail",
                "customerEmail",
                "email"
        };

        for (String field : fields) {
            String value = normalizeId(document.getString(field));
            if (value != null && identities.contains(value)) {
                return true;
            }
        }

        return false;
    }

    private Set<String> buildIdentities(String userId, String userEmail) {
        Set<String> values = new HashSet<>();
        String id = normalizeId(userId);
        String email = normalizeId(userEmail);
        if (id != null) {
            values.add(id);
        }
        if (email != null) {
            values.add(email);
        }
        return values;
    }

    private String readerKey(String userId, String userEmail) {
        return firstNonBlank(normalizeId(userEmail), normalizeId(userId));
    }

    private String readDocumentId(String readerKey, String notificationId) {
        return safeId(readerKey) + "__" + safeId(notificationId);
    }

    private String safeId(String value) {
        value = firstNonBlank(value, "unknown");
        return value.replaceAll("[^A-Za-z0-9._@-]", "_");
    }

    private long eventTime(DocumentSnapshot document, String... fields) {
        if (document == null || fields == null) {
            return 0L;
        }
        for (String field : fields) {
            Object value = document.get(field);
            long time = toMillis(value);
            if (time > 0L) {
                return time;
            }
        }
        return 0L;
    }

    private long toMillis(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return normalizeEpoch(((Number) value).longValue());
        }
        if (value instanceof Date) {
            return ((Date) value).getTime();
        }
        if (value instanceof Timestamp) {
            return ((Timestamp) value).toSqlTimestamp().getTime();
        }
        try {
            return normalizeEpoch(Long.parseLong(String.valueOf(value).trim()));
        } catch (Exception ignored) {
            return 0L;
        }
    }

    private long normalizeEpoch(long value) {
        if (value > 0 && value < 100000000000L) {
            return value * 1000L;
        }
        return value;
    }

    private boolean booleanValue(DocumentSnapshot document, String... fields) {
        if (document == null || fields == null) {
            return false;
        }
        for (String field : fields) {
            Object value = document.get(field);
            if (value instanceof Boolean) {
                return (Boolean) value;
            }
            if (value != null && "true".equalsIgnoreCase(String.valueOf(value).trim())) {
                return true;
            }
        }
        return false;
    }

    private String normalizeServiceStatus(String status) {
        status = clean(status);
        if (status == null) {
            return "Pending";
        }
        if (status.equalsIgnoreCase("InProgress") || status.equalsIgnoreCase("Active")
                || status.equalsIgnoreCase("Started")) {
            return "In Progress";
        }
        if (status.equalsIgnoreCase("Complete") || status.equalsIgnoreCase("Done")) {
            return "Completed";
        }
        if (status.equalsIgnoreCase("Canceled")) {
            return "Cancelled";
        }
        return status;
    }

    private String normalizeSOSStatus(String status) {
        status = clean(status);
        if (status == null) {
            return "Pending";
        }
        if (status.equalsIgnoreCase("InProgress") || status.equalsIgnoreCase("Active")
                || status.equalsIgnoreCase("Started")) {
            return "In Progress";
        }
        if (status.equalsIgnoreCase("Complete") || status.equalsIgnoreCase("Completed")
                || status.equalsIgnoreCase("Done")) {
            return "Resolved";
        }
        if (status.equalsIgnoreCase("Canceled")) {
            return "Cancelled";
        }
        return status;
    }

    private String normalizeId(String value) {
        value = clean(value);
        if (value == null) {
            return null;
        }
        return value.contains("@") ? value.toLowerCase() : value;
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
