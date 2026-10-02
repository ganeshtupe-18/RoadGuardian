package project.controller.user;

import project.dao.user.TowTruckDAO;
import project.dao.user.VehicleDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TowTruckController {

    private final TowTruckDAO towTruckDAO;
    private final VehicleDAO vehicleDAO;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public TowTruckController() {

        towTruckDAO =
                new TowTruckDAO();

        vehicleDAO =
                new VehicleDAO();
    }

    // ============================================================
    // LOAD AVAILABLE TOW TRUCKS
    // ============================================================

    public List<Map<String, Object>>
    getAvailableTowTrucks() throws Exception {

        List<Map<String, Object>> trucks =
                towTruckDAO
                        .getAvailableTowTrucks();

        return trucks == null
                ? new ArrayList<>()
                : trucks;
    }

    // ============================================================
    // LOAD ALL TOW TRUCKS
    // ============================================================

    public List<Map<String, Object>>
    getAllTowTrucks() throws Exception {

        List<Map<String, Object>> trucks =
                towTruckDAO
                        .getAllTowTrucks();

        return trucks == null
                ? new ArrayList<>()
                : trucks;
    }

    // ============================================================
    // CUSTOMER VEHICLES
    // ============================================================

    public List<Map<String, Object>> getCustomerVehicles(
            String userEmail
    ) throws Exception {

        userEmail = clean(userEmail);

        if (userEmail == null) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> vehicles =
                vehicleDAO
                        .getVehicles(
                                userEmail
                        );

        return vehicles == null
                ? new ArrayList<>()
                : vehicles;
    }

    // ============================================================
    // BOOK TOW TRUCK - CANONICAL SERVICE REQUEST
    // ============================================================

    public String bookTowTruck(
            String userId,
            String userName,
            String userEmail,
            String vehicleId,
            String vehicleNumber,
            String truckId,
            String truckName,
            String company,
            String pickupLocation,
            String destination,
            String charge,
            String mechanicId,
            String mechanicName
    ) throws Exception {

        if (clean(userId) == null
                && clean(userEmail) == null) {

            throw new IllegalArgumentException(
                    "User session is missing. Please login again."
            );
        }

        if (clean(vehicleId) == null) {

            throw new IllegalArgumentException(
                    "Please select your vehicle."
            );
        }

        if (clean(truckId) == null) {

            throw new IllegalArgumentException(
                    "Tow truck ID is missing."
            );
        }

        if (clean(pickupLocation) == null) {

            throw new IllegalArgumentException(
                    "Pickup / breakdown location is required."
            );
        }

        if (clean(destination) == null) {

            throw new IllegalArgumentException(
                    "Drop-off destination is required."
            );
        }

        return towTruckDAO.bookTowTruck(
                userId,
                userName,
                userEmail,
                vehicleId,
                vehicleNumber,
                truckId,
                truckName,
                company,
                pickupLocation,
                destination,
                charge,
                mechanicId,
                mechanicName
        );
    }

    // ============================================================
    // OLD OVERLOAD - SOURCE COMPATIBILITY
    // ============================================================

    public String bookTowTruck(
            String userId,
            String userName,
            String userEmail,
            String truckId,
            String truckName,
            String company,
            String destination,
            String charge
    ) throws Exception {

        return towTruckDAO.bookTowTruck(
                userId,
                userName,
                userEmail,
                truckId,
                truckName,
                company,
                destination,
                charge
        );
    }

    // ============================================================
    // CANCEL BOOKING / REQUEST
    // ============================================================

    public void cancelBooking(
            String requestId,
            String truckId
    ) throws Exception {

        towTruckDAO.cancelBooking(
                requestId,
                truckId
        );
    }

    // ============================================================
    // CLEAN
    // ============================================================

    private String clean(
            String value
    ) {

        if (value == null) {
            return null;
        }

        String cleaned = value.trim();

        return cleaned.isEmpty()
                ? null
                : cleaned;
    }
}
