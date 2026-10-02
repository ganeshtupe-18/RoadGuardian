package project.controller.admin;

import com.google.cloud.firestore.Firestore;

import project.dao.admin.VehicleDAO;
import project.model.Vehicle;

import java.util.ArrayList;
import java.util.List;

public class VehicleController {

    private final VehicleDAO vehicleDAO;
    private String lastError;

    public VehicleController(Firestore firestore) {
        if (firestore == null) {
            throw new IllegalArgumentException("Firestore cannot be null.");
        }
        this.vehicleDAO = new VehicleDAO(firestore);
    }

    public List<Vehicle> getAllVehicles() {
        try {
            clearError();
            List<Vehicle> result = vehicleDAO.getAllVehicles();
            return result == null ? new ArrayList<>() : result;
        } catch (Exception e) {
            setError("Unable to load vehicles", e);
            return new ArrayList<>();
        }
    }

    public Vehicle getVehicleById(String vehicleId) {
        try {
            clearError();
            return vehicleDAO.getVehicleById(clean(vehicleId));
        } catch (Exception e) {
            setError("Unable to load vehicle", e);
            return null;
        }
    }

    public List<Vehicle> getVehiclesByCustomerId(String customerId) {
        try {
            clearError();
            List<Vehicle> result = vehicleDAO.getVehiclesByCustomerId(customerId);
            return result == null ? new ArrayList<>() : result;
        } catch (Exception e) {
            setError("Unable to load customer vehicles", e);
            return new ArrayList<>();
        }
    }

    public boolean addVehicle(Vehicle vehicle) {
        try {
            clearError();

            if (!isValidVehicle(vehicle, false)) {
                return false;
            }

            return vehicleDAO.addVehicle(vehicle);
        } catch (Exception e) {
            setError("Unable to add vehicle", e);
            return false;
        }
    }

    public boolean updateVehicle(Vehicle vehicle) {
        try {
            clearError();

            if (!isValidVehicle(vehicle, true)) {
                return false;
            }

            return vehicleDAO.updateVehicle(vehicle);
        } catch (Exception e) {
            setError("Unable to update vehicle", e);
            return false;
        }
    }

    public boolean deleteVehicle(String vehicleId) {
        try {
            clearError();

            if (clean(vehicleId) == null) {
                lastError = "Vehicle ID is required.";
                return false;
            }

            return vehicleDAO.deleteVehicle(vehicleId);
        } catch (Exception e) {
            setError("Unable to delete vehicle", e);
            return false;
        }
    }

    public List<Vehicle> searchVehicles(String searchText) {
        try {
            clearError();
            List<Vehicle> result = vehicleDAO.searchVehicles(searchText);
            return result == null ? new ArrayList<>() : result;
        } catch (Exception e) {
            setError("Unable to search vehicles", e);
            return new ArrayList<>();
        }
    }

    public List<Vehicle> getVehiclesByStatus(String status) {
        try {
            clearError();
            List<Vehicle> result = vehicleDAO.getVehiclesByStatus(status);
            return result == null ? new ArrayList<>() : result;
        } catch (Exception e) {
            setError("Unable to filter vehicles", e);
            return new ArrayList<>();
        }
    }

    public boolean customerExists(String customerId) {
        try {
            clearError();
            return vehicleDAO.customerExists(customerId);
        } catch (Exception e) {
            setError("Unable to validate customer", e);
            return false;
        }
    }

    public String getCustomerName(String customerId) {
        try {
            clearError();
            return vehicleDAO.getCustomerName(customerId);
        } catch (Exception e) {
            setError("Unable to resolve customer name", e);
            return "Customer";
        }
    }

    public boolean canDeleteVehicle(String vehicleId) {
        try {
            clearError();
            return !vehicleDAO.hasActiveServiceRequests(vehicleId);
        } catch (Exception e) {
            setError("Unable to verify vehicle usage", e);
            return false;
        }
    }

    public int getTotalVehicles() {
        return getAllVehicles().size();
    }

    public int getActiveVehicles() {
        return countStatus(getAllVehicles(), "Active");
    }

    public int getInactiveVehicles() {
        return countStatus(getAllVehicles(), "Inactive");
    }

    public int getActiveVehicles(List<Vehicle> vehicles) {
        return countStatus(vehicles, "Active");
    }

    public int getInactiveVehicles(List<Vehicle> vehicles) {
        return countStatus(vehicles, "Inactive");
    }

    public String getLastError() {
        return clean(lastError);
    }

    private boolean isValidVehicle(Vehicle vehicle, boolean requireId) {
        if (vehicle == null) {
            lastError = "Vehicle data is required.";
            return false;
        }

        if (requireId && clean(vehicle.getVehicleId()) == null) {
            lastError = "Vehicle ID is required.";
            return false;
        }

        if (clean(vehicle.getCustomerId()) == null) {
            lastError = "Customer ID is required.";
            return false;
        }

        if (clean(vehicle.getVehicleNumber()) == null) {
            lastError = "Vehicle number is required.";
            return false;
        }

        if (clean(vehicle.getBrand()) == null) {
            lastError = "Vehicle brand is required.";
            return false;
        }

        if (clean(vehicle.getModel()) == null) {
            lastError = "Vehicle model is required.";
            return false;
        }

        return true;
    }

    private int countStatus(List<Vehicle> vehicles, String status) {
        if (vehicles == null) {
            return 0;
        }

        int count = 0;

        for (Vehicle vehicle : vehicles) {
            if (vehicle == null) {
                continue;
            }

            String value = clean(vehicle.getStatus());
            if (value == null) {
                value = "Active";
            }

            if (value.equalsIgnoreCase(status)) {
                count++;
            }
        }

        return count;
    }

    private void clearError() {
        lastError = null;
    }

    private void setError(String message, Exception e) {
        String detail = e == null ? null : clean(e.getMessage());
        lastError = detail == null ? message : detail;

        System.err.println(message + ": " + lastError);
        if (e != null) {
            e.printStackTrace();
        }
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }

        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }
}