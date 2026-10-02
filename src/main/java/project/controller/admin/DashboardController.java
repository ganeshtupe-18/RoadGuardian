package project.controller.admin;

import com.google.cloud.firestore.Firestore;

import project.dao.admin.DashboardDAO;
import project.dao.admin.ServiceRequestDAO;
import project.dao.admin.SOSRequestDAO;

import project.model.ServiceRequest;
import project.model.SOSRequest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardController {

    // =====================================================
    // DAO
    // =====================================================

    private final DashboardDAO dashboardDAO;

    private final ServiceRequestDAO serviceRequestDAO;

    private final SOSRequestDAO sosRequestDAO;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public DashboardController(
            Firestore firestore
    ) {

        if (firestore == null) {

            throw new IllegalArgumentException(
                    "Firestore cannot be null."
            );
        }

        dashboardDAO =
                new DashboardDAO(
                        firestore
                );

        serviceRequestDAO =
                new ServiceRequestDAO(
                        firestore
                );

        sosRequestDAO =
                new SOSRequestDAO(
                        firestore
                );
    }

    // =====================================================
    // DASHBOARD STATISTICS
    // =====================================================

    public Map<String, Integer> getDashboardStatistics() {

        try {

            Map<String, Integer> statistics =
                    dashboardDAO
                            .getStatistics();

            return statistics == null
                    ? new HashMap<>()
                    : statistics;

        } catch (Exception e) {

            logError(
                    "Unable to load dashboard statistics",
                    e
            );

            return new HashMap<>();
        }
    }

    // =====================================================
    // SAFE INTEGER VALUE
    //
    // Existing AdminDashboard method preserved.
    // =====================================================

    public int getValue(
            Map<String, Integer> data,
            String key
    ) {

        if (data == null
                ||
                key == null) {

            return 0;
        }

        Integer value =
                data.get(
                        key
                );

        return value == null
                ? 0
                : value;
    }

    // =====================================================
    // CUSTOMER COUNTS
    // =====================================================

    public int getTotalCustomers(
            Map<String, Integer> data
    ) {

        return getValue(
                data,
                "customers"
        );
    }

    public int getActiveCustomers(
            Map<String, Integer> data
    ) {

        return getValue(
                data,
                "activeCustomers"
        );
    }

    public int getInactiveCustomers(
            Map<String, Integer> data
    ) {

        return getValue(
                data,
                "inactiveCustomers"
        );
    }

    // =====================================================
    // MECHANIC COUNTS
    // =====================================================

    public int getTotalMechanics(
            Map<String, Integer> data
    ) {

        return getValue(
                data,
                "mechanics"
        );
    }

    public int getActiveMechanics(
            Map<String, Integer> data
    ) {

        return getValue(
                data,
                "activeMechanics"
        );
    }

    public int getInactiveMechanics(
            Map<String, Integer> data
    ) {

        return getValue(
                data,
                "inactiveMechanics"
        );
    }

    // =====================================================
    // VEHICLE COUNT
    // =====================================================

    public int getTotalVehicles(
            Map<String, Integer> data
    ) {

        return getValue(
                data,
                "vehicles"
        );
    }

    // =====================================================
    // SERVICE REQUEST COUNTS
    // =====================================================

    public int getTotalServiceRequests(
            Map<String, Integer> data
    ) {

        return getValue(
                data,
                "serviceRequests"
        );
    }

    public int getActiveServiceRequests(
            Map<String, Integer> data
    ) {

        return getValue(
                data,
                "activeRequests"
        );
    }

    public int getPendingServiceRequests(
            Map<String, Integer> data
    ) {

        return getValue(
                data,
                "pendingRequests"
        );
    }

    public int getAssignedServiceRequests(
            Map<String, Integer> data
    ) {

        return getValue(
                data,
                "assignedRequests"
        );
    }

    public int getAcceptedServiceRequests(
            Map<String, Integer> data
    ) {

        return getValue(
                data,
                "acceptedRequests"
        );
    }

    public int getInProgressServiceRequests(
            Map<String, Integer> data
    ) {

        return getValue(
                data,
                "inProgressRequests"
        );
    }

    public int getCompletedServiceRequests(
            Map<String, Integer> data
    ) {

        return getValue(
                data,
                "completedRequests"
        );
    }

    public int getCancelledServiceRequests(
            Map<String, Integer> data
    ) {

        return getValue(
                data,
                "cancelledRequests"
        );
    }

    // =====================================================
    // SOS COUNTS
    // =====================================================

    public int getTotalSOSRequests(
            Map<String, Integer> data
    ) {

        return getValue(
                data,
                "sosRequests"
        );
    }

    public int getActiveSOSRequests(
            Map<String, Integer> data
    ) {

        return getValue(
                data,
                "activeSOS"
        );
    }

    public int getPendingSOSRequests(
            Map<String, Integer> data
    ) {

        return getValue(
                data,
                "pendingSOS"
        );
    }

    public int getAssignedSOSRequests(
            Map<String, Integer> data
    ) {

        return getValue(
                data,
                "assignedSOS"
        );
    }

    public int getAcceptedSOSRequests(
            Map<String, Integer> data
    ) {

        return getValue(
                data,
                "acceptedSOS"
        );
    }

    public int getInProgressSOSRequests(
            Map<String, Integer> data
    ) {

        return getValue(
                data,
                "inProgressSOS"
        );
    }

    public int getResolvedSOSRequests(
            Map<String, Integer> data
    ) {

        return getValue(
                data,
                "resolvedSOS"
        );
    }

    public int getCancelledSOSRequests(
            Map<String, Integer> data
    ) {

        return getValue(
                data,
                "cancelledSOS"
        );
    }

    // =====================================================
    // RECENT SERVICE REQUESTS
    //
    // Actual serviceRequests collection.
    // No fake SR1256 etc.
    // =====================================================

    public List<ServiceRequest> getRecentServiceRequests() {

        return getRecentServiceRequests(
                5
        );
    }

    public List<ServiceRequest> getRecentServiceRequests(
            int limit
    ) {

        try {

            List<ServiceRequest> requests =
                    serviceRequestDAO
                            .getAllRequests();

            if (requests == null
                    ||
                    requests.isEmpty()) {

                return new ArrayList<>();
            }

            List<ServiceRequest> result =
                    new ArrayList<>(
                            requests
                    );

            result.sort(
                    Comparator
                            .comparingLong(
                                    this::getServiceRequestTime
                            )
                            .reversed()
            );

            return limit(
                    result,
                    limit
            );

        } catch (Exception e) {

            logError(
                    "Unable to load recent service requests",
                    e
            );

            return new ArrayList<>();
        }
    }

    // =====================================================
    // RECENT SOS REQUESTS
    // =====================================================

    public List<SOSRequest> getRecentSOSRequests() {

        return getRecentSOSRequests(
                5
        );
    }

    public List<SOSRequest> getRecentSOSRequests(
            int limit
    ) {

        try {

            List<SOSRequest> requests =
                    sosRequestDAO
                            .getAllRequests();

            if (requests == null
                    ||
                    requests.isEmpty()) {

                return new ArrayList<>();
            }

            List<SOSRequest> result =
                    new ArrayList<>(
                            requests
                    );

            result.sort(
                    Comparator
                            .comparingLong(
                                    this::getSOSRequestTime
                            )
                            .reversed()
            );

            return limitSOS(
                    result,
                    limit
            );

        } catch (Exception e) {

            logError(
                    "Unable to load recent SOS requests",
                    e
            );

            return new ArrayList<>();
        }
    }

    // =====================================================
    // LATEST SERVICE REQUEST
    // =====================================================

    public ServiceRequest getLatestServiceRequest() {

        List<ServiceRequest> requests =
                getRecentServiceRequests(
                        1
                );

        return requests.isEmpty()
                ? null
                : requests.get(0);
    }

    // =====================================================
    // LATEST SOS
    // =====================================================

    public SOSRequest getLatestSOSRequest() {

        List<SOSRequest> requests =
                getRecentSOSRequests(
                        1
                );

        return requests.isEmpty()
                ? null
                : requests.get(0);
    }

    // =====================================================
    // SERVICE DISPLAY HELPERS
    // =====================================================

    public String getServiceRequestId(
            ServiceRequest request
    ) {

        if (request == null) {

            return "-";
        }

        return firstNonBlank(
                request.getRequestId(),
                "-"
        );
    }

    public String getServiceCustomer(
            ServiceRequest request
    ) {

        if (request == null) {

            return "Customer";
        }

        return firstNonBlank(
                request.getCustomerName(),
                request.getCustomerId(),
                "Customer"
        );
    }

    public String getServiceVehicle(
            ServiceRequest request
    ) {

        if (request == null) {

            return "Vehicle not provided";
        }

        return firstNonBlank(
                request.getVehicleNumber(),
                request.getVehicleId(),
                "Vehicle not provided"
        );
    }

    public String getServiceType(
            ServiceRequest request
    ) {

        if (request == null) {

            return "Service Request";
        }

        return firstNonBlank(
                request.getServiceType(),
                "Service Request"
        );
    }

    public String getServiceMechanic(
            ServiceRequest request
    ) {

        if (request == null
                ||
                !request.hasMechanic()) {

            return "Not Assigned";
        }

        return firstNonBlank(
                request.getMechanicName(),
                request.getMechanicId(),
                "Assigned"
        );
    }

    public String getServiceStatus(
            ServiceRequest request
    ) {

        if (request == null) {

            return "Pending";
        }

        return normalizeServiceStatus(
                request.getStatus()
        );
    }

    // =====================================================
    // SOS DISPLAY HELPERS
    // =====================================================

    public String getSOSId(
            SOSRequest request
    ) {

        if (request == null) {

            return "-";
        }

        return firstNonBlank(
                request.getSosId(),
                "-"
        );
    }

    public String getSOSCustomer(
            SOSRequest request
    ) {

        if (request == null) {

            return "Customer";
        }

        return firstNonBlank(
                request.getCustomerName(),
                request.getUserName(),
                request.getCustomerId(),
                request.getUserId(),
                "Customer"
        );
    }

    public String getSOSEmergencyType(
            SOSRequest request
    ) {

        if (request == null) {

            return "Emergency SOS";
        }

        return firstNonBlank(
                request.getEmergencyType(),
                "Emergency SOS"
        );
    }

    public String getSOSLocation(
            SOSRequest request
    ) {

        if (request == null) {

            return "Location not available";
        }

        return firstNonBlank(
                request.getLocation(),
                "Location not available"
        );
    }

    public String getSOSMechanic(
            SOSRequest request
    ) {

        if (request == null
                ||
                !request.hasMechanic()) {

            return "Not Assigned";
        }

        return firstNonBlank(
                request.getMechanicName(),
                request.getResponderName(),
                request.getMechanicId(),
                request.getResponderId(),
                "Assigned"
        );
    }

    public String getSOSStatus(
            SOSRequest request
    ) {

        if (request == null) {

            return "Pending";
        }

        return normalizeSOSStatus(
                request.getStatus()
        );
    }

    // =====================================================
    // ADMIN ATTENTION COUNTS
    //
    // Service requests requiring admin/mechanic attention.
    // =====================================================

    public int getServiceAttentionCount(
            Map<String, Integer> data
    ) {

        return getPendingServiceRequests(
                data
        );
    }

    public int getSOSAttentionCount(
            Map<String, Integer> data
    ) {

        return getPendingSOSRequests(
                data
        )
                +
                getAssignedSOSRequests(
                        data
                );
    }

    // =====================================================
    // SERVICE STATUS NORMALIZATION
    // =====================================================

    private String normalizeServiceStatus(
            String status
    ) {

        status =
                clean(
                        status
                );

        if (status == null) {

            return "Pending";
        }

        if (status.equalsIgnoreCase(
                "Pending"
        )) {

            return "Pending";
        }

        if (status.equalsIgnoreCase(
                "Assigned"
        )) {

            return "Assigned";
        }

        if (status.equalsIgnoreCase(
                "Accepted"
        )) {

            return "Accepted";
        }

        if (
                status.equalsIgnoreCase(
                        "In Progress"
                )
                        ||
                status.equalsIgnoreCase(
                        "InProgress"
                )
                        ||
                status.equalsIgnoreCase(
                        "Active"
                )
                        ||
                status.equalsIgnoreCase(
                        "Started"
                )
        ) {

            return "In Progress";
        }

        if (
                status.equalsIgnoreCase(
                        "Completed"
                )
                        ||
                status.equalsIgnoreCase(
                        "Complete"
                )
                        ||
                status.equalsIgnoreCase(
                        "Done"
                )
                        ||
                status.equalsIgnoreCase(
                        "Closed"
                )
        ) {

            return "Completed";
        }

        if (
                status.equalsIgnoreCase(
                        "Cancelled"
                )
                        ||
                status.equalsIgnoreCase(
                        "Canceled"
                )
                        ||
                status.equalsIgnoreCase(
                        "Rejected"
                )
        ) {

            return "Cancelled";
        }

        return status;
    }

    // =====================================================
    // SOS STATUS NORMALIZATION
    // =====================================================

    private String normalizeSOSStatus(
            String status
    ) {

        status =
                clean(
                        status
                );

        if (status == null) {

            return "Pending";
        }

        if (
                status.equalsIgnoreCase(
                        "Pending"
                )
                        ||
                status.equalsIgnoreCase(
                        "Active"
                )
        ) {

            return "Pending";
        }

        if (status.equalsIgnoreCase(
                "Assigned"
        )) {

            return "Assigned";
        }

        if (status.equalsIgnoreCase(
                "Accepted"
        )) {

            return "Accepted";
        }

        if (
                status.equalsIgnoreCase(
                        "In Progress"
                )
                        ||
                status.equalsIgnoreCase(
                        "InProgress"
                )
                        ||
                status.equalsIgnoreCase(
                        "Started"
                )
        ) {

            return "In Progress";
        }

        if (
                status.equalsIgnoreCase(
                        "Resolved"
                )
                        ||
                status.equalsIgnoreCase(
                        "Completed"
                )
                        ||
                status.equalsIgnoreCase(
                        "Complete"
                )
        ) {

            return "Resolved";
        }

        if (
                status.equalsIgnoreCase(
                        "Cancelled"
                )
                        ||
                status.equalsIgnoreCase(
                        "Canceled"
                )
        ) {

            return "Cancelled";
        }

        return status;
    }

    // =====================================================
    // SERVICE REQUEST TIMESTAMP
    // =====================================================

    private long getServiceRequestTime(
            ServiceRequest request
    ) {

        if (request == null) {

            return 0L;
        }

        long time =
                parseTime(
                        request.getUpdatedAt()
                );

        if (time > 0) {

            return time;
        }

        time =
                parseTime(
                        request.getCompletedDate()
                );

        if (time > 0) {

            return time;
        }

        time =
                parseTime(
                        request.getCancelledDate()
                );

        if (time > 0) {

            return time;
        }

        time =
                parseTime(
                        request.getStartedDate()
                );

        if (time > 0) {

            return time;
        }

        time =
                parseTime(
                        request.getAcceptedDate()
                );

        if (time > 0) {

            return time;
        }

        time =
                parseTime(
                        request.getAssignedDate()
                );

        if (time > 0) {

            return time;
        }

        return parseTime(
                request.getRequestDate()
        );
    }

    // =====================================================
    // SOS TIMESTAMP
    // =====================================================

    private long getSOSRequestTime(
            SOSRequest request
    ) {

        if (request == null) {

            return 0L;
        }

        long time =
                parseTime(
                        objectString(
                                request.getUpdatedAt()
                        )
                );

        if (time > 0) {

            return time;
        }

        time =
                parseTime(
                        request.getResolvedDate()
                );

        if (time > 0) {

            return time;
        }

        time =
                parseTime(
                        request.getCancelledDate()
                );

        if (time > 0) {

            return time;
        }

        time =
                parseTime(
                        request.getStartedDate()
                );

        if (time > 0) {

            return time;
        }

        time =
                parseTime(
                        request.getAcceptedDate()
                );

        if (time > 0) {

            return time;
        }

        time =
                parseTime(
                        request.getAssignedDate()
                );

        if (time > 0) {

            return time;
        }

        return parseTime(
                request.getRequestDate()
        );
    }

    // =====================================================
    // LIMIT SERVICE LIST
    // =====================================================

    private List<ServiceRequest> limit(
            List<ServiceRequest> requests,
            int limit
    ) {

        if (requests == null
                ||
                requests.isEmpty()) {

            return new ArrayList<>();
        }

        if (limit <= 0
                ||
                requests.size() <= limit) {

            return new ArrayList<>(
                    requests
            );
        }

        return new ArrayList<>(
                requests.subList(
                        0,
                        limit
                )
        );
    }

    // =====================================================
    // LIMIT SOS LIST
    // =====================================================

    private List<SOSRequest> limitSOS(
            List<SOSRequest> requests,
            int limit
    ) {

        if (requests == null
                ||
                requests.isEmpty()) {

            return new ArrayList<>();
        }

        if (limit <= 0
                ||
                requests.size() <= limit) {

            return new ArrayList<>(
                    requests
            );
        }

        return new ArrayList<>(
                requests.subList(
                        0,
                        limit
                )
        );
    }

    // =====================================================
    // PARSE TIME
    // =====================================================

    private long parseTime(
            String value
    ) {

        value =
                clean(
                        value
                );

        if (value == null) {

            return 0L;
        }

        try {

            long number =
                    Long.parseLong(
                            value
                    );

            if (
                    number > 0
                            &&
                    number < 100000000000L
            ) {

                number *=
                        1000L;
            }

            return number;

        } catch (Exception e) {

            return 0L;
        }
    }

    // =====================================================
    // OBJECT -> STRING
    // =====================================================

    private String objectString(
            Object object
    ) {

        if (object == null) {

            return null;
        }

        String value =
                String.valueOf(
                        object
                ).trim();

        return value.isEmpty()
                ? null
                : value;
    }

    // =====================================================
    // FIRST NON BLANK
    // =====================================================

    private String firstNonBlank(
            String... values
    ) {

        if (values == null) {

            return null;
        }

        for (String value :
                values) {

            String cleaned =
                    clean(
                            value
                    );

            if (cleaned != null) {

                return cleaned;
            }
        }

        return null;
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

    // =====================================================
    // ERROR
    // =====================================================

    private void logError(
            String message,
            Exception e
    ) {

        System.err.println(
                message
                        + ": "
                        + (
                        e == null
                                ? "Unknown error"
                                : e.getMessage()
                )
        );

        if (e != null) {

            e.printStackTrace();
        }
    }
}