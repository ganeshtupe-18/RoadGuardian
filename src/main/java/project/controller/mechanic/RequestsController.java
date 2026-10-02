package project.controller.mechanic;

import project.dao.mechanic.MechanicRequestDAO;
import project.model.ServiceRequest;

import java.util.ArrayList;
import java.util.List;

public class RequestsController {

    private final MechanicRequestDAO requestDAO;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public RequestsController() {

        this.requestDAO =
                new MechanicRequestDAO();
    }

    // =====================================================
    // GET CURRENT MECHANIC ID
    // =====================================================

    public String getCurrentMechanicId() {

        return requestDAO
                .getCurrentMechanicId();
    }

    // =====================================================
    // GET REQUESTS FOR REQUESTS PAGE
    //
    // Only Admin-assigned requests
    // =====================================================

    public List<ServiceRequest> getAssignedRequests() {

        try {

            return requestDAO
                    .getAssignedRequests();

        } catch (Exception e) {

            System.err.println(
                    "Unable to load assigned requests: "
                            + e.getMessage()
            );

            e.printStackTrace();

            return new ArrayList<>();
        }
    }

    // =====================================================
    // GET ALL MECHANIC REQUESTS
    // =====================================================

    public List<ServiceRequest> getAllMyRequests() {

        try {

            return requestDAO
                    .getMyRequests();

        } catch (Exception e) {

            System.err.println(
                    "Unable to load mechanic requests: "
                            + e.getMessage()
            );

            return new ArrayList<>();
        }
    }

    // =====================================================
    // GET ACTIVE REQUESTS
    //
    // Accepted + In Progress
    // =====================================================

    public List<ServiceRequest> getActiveRequests() {

        try {

            return requestDAO
                    .getActiveRequests();

        } catch (Exception e) {

            System.err.println(
                    "Unable to load active requests: "
                            + e.getMessage()
            );

            return new ArrayList<>();
        }
    }

    // =====================================================
    // GET COMPLETED REQUESTS
    // =====================================================

    public List<ServiceRequest> getCompletedRequests() {

        try {

            return requestDAO
                    .getCompletedRequests();

        } catch (Exception e) {

            System.err.println(
                    "Unable to load completed requests: "
                            + e.getMessage()
            );

            return new ArrayList<>();
        }
    }

    // =====================================================
    // GET REQUEST BY ID
    // =====================================================

    public ServiceRequest getRequestById(
            String requestId
    ) {

        try {

            return requestDAO
                    .getRequestById(
                            requestId
                    );

        } catch (Exception e) {

            System.err.println(
                    "Unable to load request: "
                            + e.getMessage()
            );

            return null;
        }
    }

    // =====================================================
    // ACCEPT REQUEST
    //
    // Assigned -> Accepted
    // =====================================================

    public boolean acceptRequest(
            String requestId
    ) {

        try {

            boolean accepted =
                    requestDAO
                            .acceptRequest(
                                    requestId
                            );

            if (accepted) {

                System.out.println(
                        "Request accepted successfully: "
                                + requestId
                );

            } else {

                System.err.println(
                        "Unable to accept request: "
                                + requestId
                );
            }

            return accepted;

        } catch (Exception e) {

            System.err.println(
                    "Error accepting request: "
                            + e.getMessage()
            );

            e.printStackTrace();

            return false;
        }
    }

    // =====================================================
    // REJECT REQUEST
    //
    // Assigned -> Pending
    // mechanicId cleared
    //
    // Admin ला पुन्हा request दिसेल.
    // =====================================================

    public boolean rejectRequest(
            String requestId
    ) {

        try {

            boolean rejected =
                    requestDAO
                            .rejectRequest(
                                    requestId
                            );

            if (rejected) {

                System.out.println(
                        "Request rejected successfully: "
                                + requestId
                );
            }

            return rejected;

        } catch (Exception e) {

            System.err.println(
                    "Error rejecting request: "
                            + e.getMessage()
            );

            e.printStackTrace();

            return false;
        }
    }

    // =====================================================
    // START REQUEST
    //
    // Accepted -> In Progress
    // =====================================================

    public boolean startRequest(
            String requestId
    ) {

        try {

            return requestDAO
                    .startRequest(
                            requestId
                    );

        } catch (Exception e) {

            System.err.println(
                    "Unable to start request: "
                            + e.getMessage()
            );

            e.printStackTrace();

            return false;
        }
    }

    // =====================================================
    // SEARCH ASSIGNED REQUESTS
    //
    // Customer
    // Vehicle
    // Service
    // Problem
    // Location
    // =====================================================

    public List<ServiceRequest> searchRequests(
            String searchText
    ) {

        List<ServiceRequest> requests =
                getAssignedRequests();

        if (searchText == null
                ||
                searchText.isBlank()) {

            return requests;
        }

        String search =
                searchText
                        .trim()
                        .toLowerCase();

        List<ServiceRequest> result =
                new ArrayList<>();

        for (ServiceRequest request :
                requests) {

            if (
                    contains(
                            request.getRequestId(),
                            search
                    )
                            ||
                    contains(
                            request.getCustomerId(),
                            search
                    )
                            ||
                    contains(
                            request.getCustomerName(),
                            search
                    )
                            ||
                    contains(
                            request.getVehicleId(),
                            search
                    )
                            ||
                    contains(
                            request.getVehicleNumber(),
                            search
                    )
                            ||
                    contains(
                            request.getServiceType(),
                            search
                    )
                            ||
                    contains(
                            request.getDescription(),
                            search
                    )
                            ||
                    contains(
                            request.getLocation(),
                            search
                    )
                            ||
                    contains(
                            request.getPriority(),
                            search
                    )
            ) {

                result.add(
                        request
                );
            }
        }

        return result;
    }

    // =====================================================
    // EMERGENCY REQUESTS
    // =====================================================

    public List<ServiceRequest> getEmergencyRequests() {

        List<ServiceRequest> requests =
                getAssignedRequests();

        List<ServiceRequest> result =
                new ArrayList<>();

        for (ServiceRequest request :
                requests) {

            if (isEmergency(
                    request
            )) {

                result.add(
                        request
                );
            }
        }

        return result;
    }

    // =====================================================
    // NORMAL REQUESTS
    // =====================================================

    public List<ServiceRequest> getNormalRequests() {

        List<ServiceRequest> requests =
                getAssignedRequests();

        List<ServiceRequest> result =
                new ArrayList<>();

        for (ServiceRequest request :
                requests) {

            if (!isEmergency(
                    request
            )) {

                result.add(
                        request
                );
            }
        }

        return result;
    }

    // =====================================================
    // FILTER BY TYPE
    // =====================================================

    public List<ServiceRequest> getRequestsByType(
            String type
    ) {

        if (type == null
                ||
                type.isBlank()
                ||
                type.equalsIgnoreCase(
                        "All"
                )) {

            return getAssignedRequests();
        }

        if (type.equalsIgnoreCase(
                "Emergency"
        )) {

            return getEmergencyRequests();
        }

        if (type.equalsIgnoreCase(
                "Normal"
        )) {

            return getNormalRequests();
        }

        return getAssignedRequests();
    }

    // =====================================================
    // TOTAL ASSIGNED COUNT
    // =====================================================

    public int getTotalRequestCount() {

        return getAssignedRequests()
                .size();
    }

    // =====================================================
    // EMERGENCY COUNT
    // =====================================================

    public int getEmergencyRequestCount() {

        return getEmergencyRequests()
                .size();
    }

    // =====================================================
    // NORMAL COUNT
    // =====================================================

    public int getNormalRequestCount() {

        return getNormalRequests()
                .size();
    }

    // =====================================================
    // ACTIVE COUNT
    // =====================================================

    public int getActiveRequestCount() {

        try {

            return requestDAO
                    .getActiveRequestCount();

        } catch (Exception e) {

            System.err.println(
                    "Unable to get active request count: "
                            + e.getMessage()
            );

            return 0;
        }
    }

    // =====================================================
    // COMPLETED COUNT
    // =====================================================

    public int getCompletedRequestCount() {

        try {

            return requestDAO
                    .getCompletedRequestCount();

        } catch (Exception e) {

            System.err.println(
                    "Unable to get completed request count: "
                            + e.getMessage()
            );

            return 0;
        }
    }

    // =====================================================
    // REQUEST DISPLAY TYPE
    //
    // RequestsPage सध्या Emergency / Normal दाखवतो.
    // =====================================================

    public String getRequestType(
            ServiceRequest request
    ) {

        return isEmergency(request)
                ? "Emergency"
                : "Normal";
    }

    // =====================================================
    // DISPLAY CUSTOMER
    // =====================================================

    public String getCustomerName(
            ServiceRequest request
    ) {

        if (request == null) {

            return "Unknown Customer";
        }

        String name =
                clean(
                        request.getCustomerName()
                );

        return name == null
                ? "Unknown Customer"
                : name;
    }

    // =====================================================
    // DISPLAY VEHICLE
    // =====================================================

    public String getVehicleDisplay(
            ServiceRequest request
    ) {

        if (request == null) {

            return "Vehicle";
        }

        String number =
                clean(
                        request.getVehicleNumber()
                );

        if (number != null) {

            return number;
        }

        String vehicleId =
                clean(
                        request.getVehicleId()
                );

        return vehicleId == null
                ? "Vehicle"
                : vehicleId;
    }

    // =====================================================
    // DISPLAY PROBLEM
    // =====================================================

    public String getProblemDisplay(
            ServiceRequest request
    ) {

        if (request == null) {

            return "Service Request";
        }

        String description =
                clean(
                        request.getDescription()
                );

        if (description != null) {

            return description;
        }

        String serviceType =
                clean(
                        request.getServiceType()
                );

        return serviceType == null
                ? "Service Request"
                : serviceType;
    }

    // =====================================================
    // DISPLAY COST
    // =====================================================

    public String getEstimatedCostDisplay(
            ServiceRequest request
    ) {

        if (request == null) {

            return "Not estimated";
        }

        String cost =
                clean(
                        request.getEstimatedCost()
                );

        if (cost == null
                ||
                cost.equals("0")
                ||
                cost.equals("0.0")) {

            return "Not estimated";
        }

        if (cost.startsWith("₹")) {

            return cost;
        }

        return "₹" + cost;
    }

    // =====================================================
    // DISPLAY LOCATION
    // =====================================================

    public String getLocationDisplay(
            ServiceRequest request
    ) {

        if (request == null) {

            return "Location not provided";
        }

        String location =
                clean(
                        request.getLocation()
                );

        return location == null
                ? "Location not provided"
                : location;
    }

    // =====================================================
    // EMERGENCY CHECK
    // =====================================================

    private boolean isEmergency(
            ServiceRequest request
    ) {

        if (request == null) {

            return false;
        }

        // -------------------------------------------------
        // PRIORITY
        // -------------------------------------------------

        String priority =
                clean(
                        request.getPriority()
                );

        if (priority != null
                &&
                priority.equalsIgnoreCase(
                        "Emergency"
                )) {

            return true;
        }

        // -------------------------------------------------
        // SERVICE TYPE FALLBACK
        // -------------------------------------------------

        String serviceType =
                clean(
                        request.getServiceType()
                );

        if (serviceType != null) {

            String value =
                    serviceType.toLowerCase();

            if (
                    value.contains("emergency")
                            ||
                    value.contains("sos")
                            ||
                    value.contains("accident")
            ) {

                return true;
            }
        }

        return false;
    }

    // =====================================================
    // CONTAINS
    // =====================================================

    private boolean contains(
            String value,
            String search
    ) {

        return value != null
                &&
                value
                        .toLowerCase()
                        .contains(
                                search
                        );
    }

    // =====================================================
    // CLEAN
    // =====================================================

    private String clean(
            String value
    ) {

        if (value == null) {

            return null;
        }

        String cleaned =
                value.trim();

        return cleaned.isEmpty()
                ? null
                : cleaned;
    }
}