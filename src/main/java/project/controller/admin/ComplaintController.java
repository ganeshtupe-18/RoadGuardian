package project.controller.admin;

import com.google.cloud.firestore.Firestore;
import project.dao.admin.ComplaintDAO;
import project.model.Complaint;

import java.util.ArrayList;
import java.util.List;

public class ComplaintController {

    private final ComplaintDAO complaintDAO;

    public ComplaintController(Firestore firestore) {
        this.complaintDAO = new ComplaintDAO(firestore);
    }

    public ComplaintController() {
        this.complaintDAO = new ComplaintDAO();
    }

    public List<Complaint> getAllComplaints() {
        try {
            List<Complaint> complaints = complaintDAO.getAllComplaints();
            return complaints == null ? new ArrayList<>() : complaints;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public Complaint getComplaintById(String complaintId) {
        try {
            return complaintDAO.getComplaintById(complaintId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateStatus(
            String complaintId,
            String status,
            String adminNote
    ) {
        try {
            return complaintDAO.updateStatus(
                    complaintId,
                    status,
                    adminNote
            );
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getTotalCount() {
        return getAllComplaints().size();
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

    public int getRejectedCount() {
        return countByStatus("Rejected");
    }

    public int getHighPriorityCount() {
        int count = 0;
        for (Complaint complaint : getAllComplaints()) {
            if (complaint != null
                    && "High".equalsIgnoreCase(complaint.getPriority())
                    && !complaint.isFinalStatus()) {
                count++;
            }
        }
        return count;
    }

    private int countByStatus(String status) {
        int count = 0;
        for (Complaint complaint : getAllComplaints()) {
            if (complaint != null
                    && status.equalsIgnoreCase(normalizeStatus(complaint.getStatus()))) {
                count++;
            }
        }
        return count;
    }

    public boolean canUpdateStatus(Complaint complaint) {
        return complaint != null && !complaint.isFinalStatus();
    }

    private String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "Pending";
        }
        if (status.equalsIgnoreCase("InProgress") || status.equalsIgnoreCase("Processing")) {
            return "In Progress";
        }
        if (status.equalsIgnoreCase("Closed") || status.equalsIgnoreCase("Completed")) {
            return "Resolved";
        }
        return status.trim();
    }
}
