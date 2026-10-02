package project.controller.user;

import project.dao.user.ComplaintDAO;
import project.model.Complaint;
import project.ui.user.UserSession;

import java.util.ArrayList;
import java.util.List;

public class ComplaintController {

    private final ComplaintDAO complaintDAO;

    public ComplaintController() {
        this.complaintDAO = new ComplaintDAO();
    }

    public boolean submitComplaint(
            String subject,
            String category,
            String description,
            String priority,
            String relatedRequestId
    ) {
        if (!UserSession.isLoggedIn()) {
            return false;
        }

        String customerId = firstNonBlank(
                UserSession.getUserEmail(),
                UserSession.getUserId()
        );

        if (clean(customerId) == null
                || clean(subject) == null
                || clean(description) == null) {
            return false;
        }

        Complaint complaint = new Complaint();
        complaint.setCustomerId(customerId);
        complaint.setCustomerEmail(UserSession.getUserEmail());
        complaint.setCustomerName(firstNonBlank(
                UserSession.getUserName(),
                "Customer"
        ));
        complaint.setSubject(subject);
        complaint.setCategory(category);
        complaint.setDescription(description);
        complaint.setPriority(priority);
        complaint.setRelatedRequestId(relatedRequestId);

        try {
            return complaintDAO.createComplaint(complaint);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Complaint> getMyComplaints() {
        String identity = firstNonBlank(
                UserSession.getUserEmail(),
                UserSession.getUserId()
        );

        if (clean(identity) == null) {
            return new ArrayList<>();
        }

        try {
            return complaintDAO.getMyComplaints(identity);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public Complaint getComplaintById(String complaintId) {
        String identity = firstNonBlank(
                UserSession.getUserEmail(),
                UserSession.getUserId()
        );

        if (clean(identity) == null) {
            return null;
        }

        try {
            return complaintDAO.getComplaintById(complaintId, identity);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getPendingCount() {
        return countByStatus("Pending");
    }

    public int getInProgressCount() {
        return countByStatus("In Progress");
    }

    public int getResolvedCount() {
        return countByStatus("Resolved");
    }

    private int countByStatus(String status) {
        int count = 0;
        for (Complaint complaint : getMyComplaints()) {
            if (complaint != null
                    && status.equalsIgnoreCase(normalizeStatus(complaint.getStatus()))) {
                count++;
            }
        }
        return count;
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
