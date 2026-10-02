package project.dao.admin;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;

import project.model.Notification;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationDAO {

    private static final String COLLECTION = "notifications";

    private final Firestore firestore;

    public NotificationDAO(Firestore firestore) {
        if (firestore == null) {
            throw new IllegalArgumentException("Firestore cannot be null.");
        }
        this.firestore = firestore;
    }

    public String createNotification(Notification notification) throws Exception {
        if (notification == null) {
            return null;
        }

        String title = clean(notification.getTitle());
        String message = clean(notification.getMessage());
        String type = firstNonBlank(notification.getType(), "General");
        String recipientId = normalizeId(notification.getRecipientId());

        if (title == null || message == null) {
            return null;
        }

        if (!notification.isBroadcast() && recipientId == null) {
            return null;
        }

        DocumentReference reference = firestore.collection(COLLECTION).document();
        long now = System.currentTimeMillis();

        Map<String, Object> data = new HashMap<>();
        data.put("notificationId", reference.getId());
        data.put("recipientId", notification.isBroadcast() ? "" : recipientId);
        data.put("customerId", notification.isBroadcast() ? "" : recipientId);
        data.put("recipientName", firstNonBlank(notification.getRecipientName(), ""));
        data.put("title", title);
        data.put("message", message);
        data.put("description", message);
        data.put("type", type);
        data.put("source", "Admin");
        data.put("relatedId", firstNonBlank(notification.getRelatedId(), ""));
        data.put("broadcast", notification.isBroadcast());
        data.put("isBroadcast", notification.isBroadcast());
        data.put("reminder", notification.isReminder() || "Reminder".equalsIgnoreCase(type));
        data.put("status", "Sent");
        data.put("createdAt", now);
        data.put("updatedAt", now);
        data.put("senderRole", "Admin");

        reference.set(data).get();
        return reference.getId();
    }

    public List<Notification> getAllNotifications() throws Exception {
        QuerySnapshot snapshot = firestore.collection(COLLECTION).get().get();
        List<Notification> notifications = new ArrayList<>();

        for (DocumentSnapshot document : snapshot.getDocuments()) {
            Notification notification = fromDocument(document);
            if (notification != null) {
                notifications.add(notification);
            }
        }

        notifications.sort(
                Comparator.comparingLong(Notification::getCreatedAt).reversed()
        );

        return notifications;
    }

    public Notification getNotificationById(String notificationId) throws Exception {
        notificationId = clean(notificationId);
        if (notificationId == null) {
            return null;
        }

        DocumentSnapshot document = firestore
                .collection(COLLECTION)
                .document(notificationId)
                .get()
                .get();

        return document.exists() ? fromDocument(document) : null;
    }

    public boolean deleteNotification(String notificationId) throws Exception {
        notificationId = clean(notificationId);
        if (notificationId == null) {
            return false;
        }

        firestore.collection(COLLECTION)
                .document(notificationId)
                .delete()
                .get();

        return true;
    }

    private Notification fromDocument(DocumentSnapshot document) {
        if (document == null || !document.exists()) {
            return null;
        }

        Notification notification = new Notification();
        notification.setNotificationId(
                firstNonBlank(document.getString("notificationId"), document.getId())
        );
        notification.setRecipientId(
                firstNonBlank(
                        document.getString("recipientId"),
                        document.getString("customerId"),
                        document.getString("userId")
                )
        );
        notification.setRecipientName(document.getString("recipientName"));
        notification.setTitle(firstNonBlank(document.getString("title"), "Notification"));
        notification.setMessage(
                firstNonBlank(document.getString("message"), document.getString("description"), "")
        );
        notification.setType(firstNonBlank(document.getString("type"), "General"));
        notification.setSource(firstNonBlank(document.getString("source"), "Admin"));
        notification.setRelatedId(document.getString("relatedId"));
        notification.setBroadcast(booleanValue(document, "broadcast", "isBroadcast"));
        notification.setReminder(booleanValue(document, "reminder"));
        notification.setStatus(firstNonBlank(document.getString("status"), "Sent"));
        notification.setCreatedAt(toMillis(firstValue(document, "createdAt", "date", "timestamp")));
        notification.setUpdatedAt(toMillis(firstValue(document, "updatedAt", "createdAt")));
        return notification;
    }

    private Object firstValue(DocumentSnapshot document, String... fields) {
        if (document == null || fields == null) {
            return null;
        }
        for (String field : fields) {
            Object value = document.get(field);
            if (value != null) {
                return value;
            }
        }
        return null;
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
            if (value != null) {
                String text = String.valueOf(value).trim();
                if ("true".equalsIgnoreCase(text)) {
                    return true;
                }
            }
        }
        return false;
    }

    private long toMillis(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            long number = ((Number) value).longValue();
            return normalizeEpoch(number);
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
