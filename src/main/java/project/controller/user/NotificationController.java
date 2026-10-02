package project.controller.user;

import project.dao.user.NotificationDAO;
import project.firebase.FirebaseConfig;
import project.model.Notification;
import project.ui.user.UserSession;

import java.util.ArrayList;
import java.util.List;

public class NotificationController {

    private final NotificationDAO notificationDAO;
    private final String userId;
    private final String userEmail;
    private String lastError;

    public NotificationController() {
        try {
            this.notificationDAO = new NotificationDAO(FirebaseConfig.getFirestore());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to initialize NotificationController.", e);
        }

        this.userId = UserSession.getUserId();
        this.userEmail = UserSession.getUserEmail();
    }

    public List<Notification> getNotifications() {
        try {
            lastError = null;
            return notificationDAO.getNotifications(userId, userEmail);
        } catch (Exception e) {
            lastError = e.getMessage();
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public boolean markRead(Notification notification) {
        if (notification == null || notification.isRead()) {
            return true;
        }

        try {
            boolean success = notificationDAO.markRead(
                    userId,
                    userEmail,
                    notification.getNotificationId()
            );
            if (success) {
                notification.setRead(true);
                lastError = null;
            }
            return success;
        } catch (Exception e) {
            lastError = e.getMessage();
            e.printStackTrace();
            return false;
        }
    }

    public boolean markAllRead(List<Notification> notifications) {
        try {
            boolean success = notificationDAO.markAllRead(userId, userEmail, notifications);
            if (success && notifications != null) {
                for (Notification notification : notifications) {
                    if (notification != null) {
                        notification.setRead(true);
                    }
                }
                lastError = null;
            }
            return success;
        } catch (Exception e) {
            lastError = e.getMessage();
            e.printStackTrace();
            return false;
        }
    }

    public int unreadCount(List<Notification> notifications) {
        int count = 0;
        if (notifications == null) {
            return count;
        }
        for (Notification notification : notifications) {
            if (notification != null && !notification.isRead()) {
                count++;
            }
        }
        return count;
    }

    public boolean isLoggedIn() {
        return UserSession.isLoggedIn();
    }

    public String getLastError() {
        return lastError;
    }
}
