package project.controller.admin;

import com.google.cloud.firestore.Firestore;

import project.dao.admin.ServiceDAO;
import project.model.RoadService;

import java.util.ArrayList;
import java.util.List;

public class ServiceController {

    private final ServiceDAO serviceDAO;
    private String lastError;

    public ServiceController(Firestore firestore) {
        serviceDAO = new ServiceDAO(firestore);
    }

    public List<RoadService> getAllServices() {
        try {
            lastError = null;
            return serviceDAO.getAllServices();
        } catch (Exception e) {
            lastError = message(e);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<RoadService> getActiveServices() {
        List<RoadService> active = new ArrayList<>();
        for (RoadService service : getAllServices()) {
            if (service != null && service.isActive()) {
                active.add(service);
            }
        }
        return active;
    }

    public RoadService getServiceById(String serviceId) {
        try {
            lastError = null;
            return serviceDAO.getServiceById(serviceId);
        } catch (Exception e) {
            lastError = message(e);
            e.printStackTrace();
            return null;
        }
    }

    public boolean addService(RoadService service) {
        try {
            lastError = null;
            String id = serviceDAO.addService(service);
            return id != null && !id.isBlank();
        } catch (Exception e) {
            lastError = message(e);
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateService(RoadService service) {
        try {
            lastError = null;
            return serviceDAO.updateService(service);
        } catch (Exception e) {
            lastError = message(e);
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteService(RoadService service) {
        if (service == null) {
            lastError = "Service is not available.";
            return false;
        }

        try {
            lastError = null;
            if (serviceDAO.isUsedByServiceRequest(service)) {
                lastError = "This service is already used by service requests. Set it to Inactive instead of deleting it.";
                return false;
            }
            return serviceDAO.deleteService(service.getServiceId());
        } catch (Exception e) {
            lastError = message(e);
            e.printStackTrace();
            return false;
        }
    }

    public int getTotalCount() {
        return getAllServices().size();
    }

    public int getActiveCount() {
        int count = 0;
        for (RoadService service : getAllServices()) {
            if (service != null && service.isActive()) {
                count++;
            }
        }
        return count;
    }

    public String getLastError() {
        return lastError;
    }

    private String message(Exception e) {
        if (e == null || e.getMessage() == null || e.getMessage().isBlank()) {
            return "Unknown Firebase error.";
        }
        return e.getMessage();
    }
}
