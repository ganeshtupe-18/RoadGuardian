package project.controller.admin;

import project.dao.admin.NotificationDAO;
import project.firebase.FirebaseConfig;
import project.model.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationController {

    private final NotificationDAO notificationDAO;
    private String lastError;

    public NotificationController() {
        try {
            this.notificationDAO = new NotificationDAO(FirebaseConfig.getFirestore());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to initialize NotificationController.", e);
        }
    }

    public List<Notification> getAllNotifications() {
        try {
            lastError = null;
            return notificationDAO.getAllNotifications();
        } catch (Exception e) {
            lastError = e.getMessage();
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public String sendNotification(
            String recipientId,
            String title,
            String message,
            String type,
            boolean broadcast
    ) {
        try {
            Notification notification = new Notification();
            notification.setRecipientId(recipientId);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setType(type);
            notification.setBroadcast(broadcast);
            notification.setReminder("Reminder".equalsIgnoreCase(type));

            String id = notificationDAO.createNotification(notification);
            if (id == null) {
                lastError = "Please enter all required notification details.";
            } else {
                lastError = null;
            }
            return id;
        } catch (Exception e) {
            lastError = e.getMessage();
            e.printStackTrace();
            return null;
        }
    }

    public boolean deleteNotification(String notificationId) {
        try {
            boolean success = notificationDAO.deleteNotification(notificationId);
            lastError = success ? null : "Unable to delete notification.";
            return success;
        } catch (Exception e) {
            lastError = e.getMessage();
            e.printStackTrace();
            return false;
        }
    }

    public String getLastError() {
        return lastError;
    }
}
