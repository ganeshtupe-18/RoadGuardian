package project.controller.admin;

import com.google.cloud.firestore.Firestore;
import project.dao.admin.ServiceRequestDAO;
import project.model.ServiceRequest;

import java.util.ArrayList;
import java.util.List;

public class ServiceRequestController {

    private final ServiceRequestDAO serviceRequestDAO;

    public ServiceRequestController(Firestore firestore) {
        if (firestore == null) {
            throw new IllegalArgumentException("Firestore cannot be null.");
        }
        this.serviceRequestDAO = new ServiceRequestDAO(firestore);
    }

    public List<ServiceRequest> getAllRequests() {
        try {
            List<ServiceRequest> list = serviceRequestDAO.getAllRequests();
            return list == null ? new ArrayList<>() : list;
        } catch (Exception e) {
            logError("Unable to load service requests", e);
            return new ArrayList<>();
        }
    }

    public ServiceRequest getRequestById(String requestId) {
        requestId = clean(requestId);
        if (requestId == null) return null;
        try {
            return serviceRequestDAO.getRequestById(requestId);
        } catch (Exception e) {
            logError("Unable to load service request", e);
            return null;
        }
    }

    public List<ServiceRequest> getRequestsByCustomerId(String customerId) {
        customerId = normalizeId(customerId);
        if (customerId == null) return new ArrayList<>();
        try {
            List<ServiceRequest> list = serviceRequestDAO.getRequestsByCustomerId(customerId);
            return list == null ? new ArrayList<>() : list;
        } catch (Exception e) {
            logError("Unable to load customer requests", e);
            return new ArrayList<>();
        }
    }

    public List<ServiceRequest> getRequestsByMechanicId(String mechanicId) {
        mechanicId = normalizeId(mechanicId);
        if (mechanicId == null) return new ArrayList<>();
        try {
            List<ServiceRequest> list = serviceRequestDAO.getRequestsByMechanicId(mechanicId);
            return list == null ? new ArrayList<>() : list;
        } catch (Exception e) {
            logError("Unable to load mechanic requests", e);
            return new ArrayList<>();
        }
    }

    public boolean addRequest(ServiceRequest request) {
        if (request == null) return false;

        String customerId = normalizeId(request.getCustomerId());
        if (customerId == null) return false;
        request.setCustomerId(customerId);

        if (clean(request.getVehicleId()) == null && clean(request.getVehicleNumber()) == null) {
            return false;
        }
        if (clean(request.getServiceType()) == null) return false;

        String mechanicId = normalizeId(request.getMechanicId());
        request.setMechanicId(mechanicId);

        if (mechanicId != null) {
            request.setStatus("Assigned");
            if (clean(request.getAssignedDate()) == null) {
                request.setAssignedDate(currentTime());
            }
        } else {
            request.setStatus("Pending");
            request.setMechanicName(null);
            request.setAssignedDate(null);
        }

        if (clean(request.getRequestDate()) == null) {
            request.setRequestDate(currentTime());
        }

        try {
            return serviceRequestDAO.addRequest(request);
        } catch (Exception e) {
            logError("Unable to add request", e);
            return false;
        }
    }

    /**
     * General information update only. Lifecycle state is preserved from Firestore.
     */
    public boolean updateRequest(ServiceRequest request) {
        if (request == null || clean(request.getRequestId()) == null) return false;
        try {
            ServiceRequest existing = serviceRequestDAO.getRequestById(request.getRequestId());
            if (existing == null) return false;

            request.setStatus(normalizeStatus(existing.getStatus()));
            request.setAssignedDate(existing.getAssignedDate());
            request.setAcceptedDate(existing.getAcceptedDate());
            request.setStartedDate(existing.getStartedDate());
            request.setCompletedDate(existing.getCompletedDate());
            request.setCancelledDate(existing.getCancelledDate());

            return serviceRequestDAO.updateRequest(request);
        } catch (Exception e) {
            logError("Unable to update request", e);
            return false;
        }
    }

    /**
     * Admin lifecycle guard: UI/Admin may only cancel an active request.
     * Accepted/In Progress/Completed are mechanic-owned transitions.
     */
    public boolean updateStatus(String requestId, String status) {
        requestId = clean(requestId);
        status = normalizeStatus(status);
        if (requestId == null || status == null) return false;

        if (!"Cancelled".equalsIgnoreCase(status)) {
            System.err.println("Admin status update blocked: " + status);
            return false;
        }

        try {
            ServiceRequest current = serviceRequestDAO.getRequestById(requestId);
            if (current == null) return false;
            if (current.isCompleted()) return false;
            if (current.isCancelled()) return true;
            return serviceRequestDAO.cancelRequest(requestId);
        } catch (Exception e) {
            logError("Unable to cancel request", e);
            return false;
        }
    }

    public boolean cancelRequest(String requestId) {
        return updateStatus(requestId, "Cancelled");
    }

    /** Pending -> Assigned, or Assigned -> Assigned for reassign. */
    public boolean assignMechanic(String requestId, String mechanicId, String mechanicName) {
        requestId = clean(requestId);
        mechanicId = normalizeId(mechanicId);
        mechanicName = clean(mechanicName);
        if (requestId == null || mechanicId == null) return false;

        try {
            ServiceRequest current = serviceRequestDAO.getRequestById(requestId);
            if (current == null) return false;

            String status = normalizeStatus(current.getStatus());
            if (!"Pending".equalsIgnoreCase(status) && !"Assigned".equalsIgnoreCase(status)) {
                System.err.println("Mechanic assignment blocked for status: " + status);
                return false;
            }

            if (mechanicName == null) mechanicName = mechanicId;
            return serviceRequestDAO.assignMechanic(requestId, mechanicId, mechanicName);
        } catch (Exception e) {
            logError("Unable to assign mechanic", e);
            return false;
        }
    }

    public boolean deleteRequest(String requestId) {
        requestId = clean(requestId);
        if (requestId == null) return false;
        try {
            return serviceRequestDAO.deleteRequest(requestId);
        } catch (Exception e) {
            logError("Unable to delete request", e);
            return false;
        }
    }

    public List<ServiceRequest> searchRequests(String searchText) {
        try {
            List<ServiceRequest> list = serviceRequestDAO.searchRequests(clean(searchText));
            return list == null ? new ArrayList<>() : list;
        } catch (Exception e) {
            logError("Unable to search requests", e);
            return new ArrayList<>();
        }
    }

    public List<ServiceRequest> getRequestsByStatus(String status) {
        status = clean(status);
        if (status == null || status.equalsIgnoreCase("All") || status.equalsIgnoreCase("All Status")) {
            return getAllRequests();
        }
        try {
            List<ServiceRequest> list = serviceRequestDAO.getRequestsByStatus(normalizeStatus(status));
            return list == null ? new ArrayList<>() : list;
        } catch (Exception e) {
            logError("Unable to filter requests", e);
            return new ArrayList<>();
        }
    }

    public int getTotalRequests() { return getAllRequests().size(); }
    public int getPendingRequests() { return countByStatus("Pending"); }
    public int getAssignedRequests() { return countByStatus("Assigned"); }
    public int getAcceptedRequests() { return countByStatus("Accepted"); }
    public int getInProgressRequests() { return countByStatus("In Progress"); }
    public int getCompletedRequests() { return countByStatus("Completed"); }
    public int getCancelledRequests() { return countByStatus("Cancelled"); }

    public int getActiveRequests() {
        return getPendingRequests() + getAssignedRequests() + getAcceptedRequests() + getInProgressRequests();
    }

    public boolean canAssignMechanic(ServiceRequest request) {
        if (request == null) return false;
        String status = normalizeStatus(request.getStatus());
        return "Pending".equalsIgnoreCase(status) || "Assigned".equalsIgnoreCase(status);
    }

    public boolean canCancelRequest(ServiceRequest request) {
        return request != null && !request.isCompleted() && !request.isCancelled();
    }

    public boolean isFinalRequest(ServiceRequest request) {
        return request != null && (request.isCompleted() || request.isCancelled());
    }

    public String getNextAction(ServiceRequest request) {
        if (request == null) return "Request not available";
        switch (normalizeStatus(request.getStatus())) {
            case "Pending": return "Admin must assign a mechanic.";
            case "Assigned": return "Assigned mechanic must accept the request.";
            case "Accepted": return "Mechanic must start the repair.";
            case "In Progress": return "Mechanic must complete the repair.";
            case "Completed": return "Service lifecycle completed.";
            case "Cancelled": return "Service request cancelled.";
            default: return "Current status: " + normalizeStatus(request.getStatus());
        }
    }

    private int countByStatus(String status) {
        int count = 0;
        for (ServiceRequest request : getAllRequests()) {
            if (request != null && normalizeStatus(request.getStatus()).equalsIgnoreCase(status)) count++;
        }
        return count;
    }

    private String normalizeStatus(String status) {
        status = clean(status);
        if (status == null) return "Pending";
        if (status.equalsIgnoreCase("Pending")) return "Pending";
        if (status.equalsIgnoreCase("Assigned")) return "Assigned";
        if (status.equalsIgnoreCase("Accepted")) return "Accepted";
        if (status.equalsIgnoreCase("In Progress") || status.equalsIgnoreCase("InProgress") || status.equalsIgnoreCase("Active") || status.equalsIgnoreCase("Started")) return "In Progress";
        if (status.equalsIgnoreCase("Completed") || status.equalsIgnoreCase("Complete") || status.equalsIgnoreCase("Done") || status.equalsIgnoreCase("Closed")) return "Completed";
        if (status.equalsIgnoreCase("Cancelled") || status.equalsIgnoreCase("Canceled") || status.equalsIgnoreCase("Rejected")) return "Cancelled";
        return status;
    }

    private String normalizeId(String value) {
        value = clean(value);
        if (value == null) return null;
        return value.contains("@") ? value.toLowerCase() : value;
    }

    private String currentTime() {
        return String.valueOf(System.currentTimeMillis());
    }

    private String clean(String value) {
        if (value == null) return null;
        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private void logError(String message, Exception e) {
        System.err.println(message + ": " + (e == null ? "Unknown error" : e.getMessage()));
        if (e != null) e.printStackTrace();
    }
}