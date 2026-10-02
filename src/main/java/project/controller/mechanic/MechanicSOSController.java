package project.controller.mechanic;

import com.google.cloud.firestore.Firestore;

import project.dao.mechanic.SOSRequestDAO;
import project.model.SOSRequest;

import java.util.ArrayList;
import java.util.List;

public class MechanicSOSController {

    // =====================================================
    // DAO
    // =====================================================

    private final SOSRequestDAO sosRequestDAO;

    // =====================================================
    // DEFAULT CONSTRUCTOR
    // =====================================================

    public MechanicSOSController() {

        this.sosRequestDAO =
                new SOSRequestDAO();
    }

    // =====================================================
    // FIRESTORE CONSTRUCTOR
    // =====================================================

    public MechanicSOSController(
            Firestore firestore
    ) {

        this.sosRequestDAO =
                new SOSRequestDAO(
                        firestore
                );
    }

    // =====================================================
    // CURRENT MECHANIC
    // =====================================================

    public String getCurrentMechanicId() {

        return sosRequestDAO
                .getCurrentMechanicId();
    }

    // =====================================================
    // ALL MY SOS REQUESTS
    // =====================================================

    public List<SOSRequest> getAllMyRequests() {

        try {

            return sosRequestDAO
                    .getMySOSRequests();

        } catch (Exception e) {

            logError(
                    "Unable to load mechanic SOS requests",
                    e
            );

            return new ArrayList<>();
        }
    }

    // =====================================================
    // ASSIGNED
    //
    // Admin assigned request.
    // Mechanic still needs to Accept / Reject.
    // =====================================================

    public List<SOSRequest> getAssignedRequests() {

        try {

            return sosRequestDAO
                    .getAssignedRequests();

        } catch (Exception e) {

            logError(
                    "Unable to load assigned SOS requests",
                    e
            );

            return new ArrayList<>();
        }
    }

    // =====================================================
    // ACCEPTED
    // =====================================================

    public List<SOSRequest> getAcceptedRequests() {

        try {

            return sosRequestDAO
                    .getAcceptedRequests();

        } catch (Exception e) {

            logError(
                    "Unable to load accepted SOS requests",
                    e
            );

            return new ArrayList<>();
        }
    }

    // =====================================================
    // IN PROGRESS
    // =====================================================

    public List<SOSRequest> getInProgressRequests() {

        try {

            return sosRequestDAO
                    .getInProgressRequests();

        } catch (Exception e) {

            logError(
                    "Unable to load in-progress SOS requests",
                    e
            );

            return new ArrayList<>();
        }
    }

    // =====================================================
    // ACTIVE
    //
    // Assigned
    // Accepted
    // In Progress
    // =====================================================

    public List<SOSRequest> getActiveRequests() {

        try {

            return sosRequestDAO
                    .getActiveRequests();

        } catch (Exception e) {

            logError(
                    "Unable to load active SOS requests",
                    e
            );

            return new ArrayList<>();
        }
    }

    // =====================================================
    // RESOLVED
    // =====================================================

    public List<SOSRequest> getResolvedRequests() {

        try {

            return sosRequestDAO
                    .getResolvedRequests();

        } catch (Exception e) {

            logError(
                    "Unable to load resolved SOS requests",
                    e
            );

            return new ArrayList<>();
        }
    }

    // =====================================================
    // CANCELLED
    // =====================================================

    public List<SOSRequest> getCancelledRequests() {

        try {

            return sosRequestDAO
                    .getCancelledRequests();

        } catch (Exception e) {

            logError(
                    "Unable to load cancelled SOS requests",
                    e
            );

            return new ArrayList<>();
        }
    }

    // =====================================================
    // HISTORY
    //
    // Resolved + Cancelled
    // =====================================================

    public List<SOSRequest> getHistoryRequests() {

        try {

            return sosRequestDAO
                    .getHistoryRequests();

        } catch (Exception e) {

            logError(
                    "Unable to load SOS history",
                    e
            );

            return new ArrayList<>();
        }
    }

    // =====================================================
    // GET BY STATUS
    // =====================================================

    public List<SOSRequest> getRequestsByStatus(
            String status
    ) {

        try {

            status =
                    clean(
                            status
                    );

            if (status == null
                    ||
                    status.equalsIgnoreCase(
                            "All"
                    )) {

                return getAllMyRequests();
            }

            return sosRequestDAO
                    .getRequestsByStatus(
                            status
                    );

        } catch (Exception e) {

            logError(
                    "Unable to filter SOS requests",
                    e
            );

            return new ArrayList<>();
        }
    }

    // =====================================================
    // GET BY ID
    // =====================================================

    public SOSRequest getRequestById(
            String sosId
    ) {

        sosId =
                clean(
                        sosId
                );

        if (sosId == null) {

            return null;
        }

        try {

            return sosRequestDAO
                    .getRequestById(
                            sosId
                    );

        } catch (Exception e) {

            logError(
                    "Unable to load SOS request",
                    e
            );

            return null;
        }
    }

    // =====================================================
    // LATEST ACTIVE SOS
    // =====================================================

    public SOSRequest getLatestActiveRequest() {

        List<SOSRequest> requests =
                getActiveRequests();

        if (requests.isEmpty()) {

            return null;
        }

        /*
         * DAO already sorts latest first.
         */
        return requests.get(0);
    }

    // =====================================================
    // ACCEPT REQUEST
    //
    // Assigned -> Accepted
    // =====================================================

    public boolean acceptRequest(
            String sosId
    ) {

        sosId =
                clean(
                        sosId
                );

        if (sosId == null) {

            return false;
        }

        try {

            SOSRequest request =
                    sosRequestDAO
                            .getRequestById(
                                    sosId
                            );

            if (request == null) {

                return false;
            }

            if (!request.isAssigned()) {

                System.err.println(
                        "SOS cannot be accepted. "
                                + "Current status: "
                                + request.getStatus()
                );

                return false;
            }

            boolean success =
                    sosRequestDAO
                            .acceptRequest(
                                    sosId
                            );

            if (success) {

                System.out.println(
                        "SOS accepted: "
                                + sosId
                );
            }

            return success;

        } catch (Exception e) {

            logError(
                    "Unable to accept SOS request",
                    e
            );

            return false;
        }
    }

    // =====================================================
    // REJECT REQUEST
    //
    // Assigned -> Pending
    //
    // mechanicId is cleared.
    // Admin can assign another mechanic.
    // =====================================================

    public boolean rejectRequest(
            String sosId
    ) {

        sosId =
                clean(
                        sosId
                );

        if (sosId == null) {

            return false;
        }

        try {

            SOSRequest request =
                    sosRequestDAO
                            .getRequestById(
                                    sosId
                            );

            if (request == null
                    ||
                    !request.isAssigned()) {

                return false;
            }

            boolean success =
                    sosRequestDAO
                            .rejectRequest(
                                    sosId
                            );

            if (success) {

                System.out.println(
                        "SOS rejected: "
                                + sosId
                                + " -> Pending"
                );
            }

            return success;

        } catch (Exception e) {

            logError(
                    "Unable to reject SOS request",
                    e
            );

            return false;
        }
    }

    // =====================================================
    // START RESPONSE
    //
    // Accepted -> In Progress
    // =====================================================

    public boolean startRequest(
            String sosId
    ) {

        sosId =
                clean(
                        sosId
                );

        if (sosId == null) {

            return false;
        }

        try {

            SOSRequest request =
                    sosRequestDAO
                            .getRequestById(
                                    sosId
                            );

            if (request == null
                    ||
                    !request.isAccepted()) {

                return false;
            }

            boolean success =
                    sosRequestDAO
                            .startRequest(
                                    sosId
                            );

            if (success) {

                System.out.println(
                        "SOS response started: "
                                + sosId
                );
            }

            return success;

        } catch (Exception e) {

            logError(
                    "Unable to start SOS response",
                    e
            );

            return false;
        }
    }

    // =====================================================
    // RESOLVE REQUEST
    //
    // In Progress -> Resolved
    // =====================================================

    public boolean resolveRequest(
            String sosId
    ) {

        sosId =
                clean(
                        sosId
                );

        if (sosId == null) {

            return false;
        }

        try {

            SOSRequest request =
                    sosRequestDAO
                            .getRequestById(
                                    sosId
                            );

            if (request == null
                    ||
                    !request.isInProgress()) {

                return false;
            }

            boolean success =
                    sosRequestDAO
                            .resolveRequest(
                                    sosId
                            );

            if (success) {

                System.out.println(
                        "SOS resolved: "
                                + sosId
                );
            }

            return success;

        } catch (Exception e) {

            logError(
                    "Unable to resolve SOS request",
                    e
            );

            return false;
        }
    }

    // =====================================================
    // START NAVIGATION
    //
    // Accepted / In Progress
    // =====================================================

    public boolean startNavigation(
            String sosId
    ) {

        sosId =
                clean(
                        sosId
                );

        if (sosId == null) {

            return false;
        }

        try {

            SOSRequest request =
                    sosRequestDAO
                            .getRequestById(
                                    sosId
                            );

            if (request == null) {

                return false;
            }

            if (
                    !request.isAccepted()
                            &&
                    !request.isInProgress()
            ) {

                return false;
            }

            return sosRequestDAO
                    .startNavigation(
                            sosId
                    );

        } catch (Exception e) {

            logError(
                    "Unable to start SOS navigation",
                    e
            );

            return false;
        }
    }

    // =====================================================
    // MARK ARRIVED
    // =====================================================

    public boolean markArrived(
            String sosId
    ) {

        sosId =
                clean(
                        sosId
                );

        if (sosId == null) {

            return false;
        }

        try {

            SOSRequest request =
                    sosRequestDAO
                            .getRequestById(
                                    sosId
                            );

            if (request == null) {

                return false;
            }

            if (
                    !request.isAccepted()
                            &&
                    !request.isInProgress()
            ) {

                return false;
            }

            return sosRequestDAO
                    .markArrived(
                            sosId
                    );

        } catch (Exception e) {

            logError(
                    "Unable to mark SOS arrival",
                    e
            );

            return false;
        }
    }

    // =====================================================
    // SEARCH
    // =====================================================

    public List<SOSRequest> searchRequests(
            String searchText
    ) {

        try {

            return sosRequestDAO
                    .searchRequests(
                            searchText
                    );

        } catch (Exception e) {

            logError(
                    "Unable to search SOS requests",
                    e
            );

            return new ArrayList<>();
        }
    }

    // =====================================================
    // SEARCH + STATUS FILTER
    // =====================================================

    public List<SOSRequest> getFilteredRequests(
            String searchText,
            String status
    ) {

        List<SOSRequest> source;

        if (clean(searchText) == null) {

            source =
                    getAllMyRequests();

        } else {

            source =
                    searchRequests(
                            searchText
                    );
        }

        status =
                clean(
                        status
                );

        if (status == null
                ||
                status.equalsIgnoreCase(
                        "All"
                )) {

            return source;
        }

        String requiredStatus =
                normalizeStatus(
                        status
                );

        List<SOSRequest> result =
                new ArrayList<>();

        for (SOSRequest request :
                source) {

            if (request == null) {

                continue;
            }

            if (sameStatus(
                    request.getStatus(),
                    requiredStatus
            )) {

                result.add(
                        request
                );
            }
        }

        return result;
    }

    // =====================================================
    // COUNTS
    // =====================================================

    public int getTotalCount() {

        return getAllMyRequests()
                .size();
    }

    public int getAssignedCount() {

        try {

            return sosRequestDAO
                    .getAssignedCount();

        } catch (Exception e) {

            return 0;
        }
    }

    public int getAcceptedCount() {

        try {

            return sosRequestDAO
                    .getAcceptedCount();

        } catch (Exception e) {

            return 0;
        }
    }

    public int getInProgressCount() {

        try {

            return sosRequestDAO
                    .getInProgressCount();

        } catch (Exception e) {

            return 0;
        }
    }

    public int getActiveCount() {

        try {

            return sosRequestDAO
                    .getActiveCount();

        } catch (Exception e) {

            return 0;
        }
    }

    public int getResolvedCount() {

        try {

            return sosRequestDAO
                    .getResolvedCount();

        } catch (Exception e) {

            return 0;
        }
    }

    public int getCancelledCount() {

        return getCancelledRequests()
                .size();
    }

    public int getHistoryCount() {

        return getHistoryRequests()
                .size();
    }

    // =====================================================
    // ACTION PERMISSION HELPERS
    // =====================================================

    public boolean canAccept(
            SOSRequest request
    ) {

        return request != null
                &&
                request.isAssigned();
    }

    public boolean canReject(
            SOSRequest request
    ) {

        return request != null
                &&
                request.isAssigned();
    }

    public boolean canStart(
            SOSRequest request
    ) {

        return request != null
                &&
                request.isAccepted();
    }

    public boolean canResolve(
            SOSRequest request
    ) {

        return request != null
                &&
                request.isInProgress();
    }

    public boolean canNavigate(
            SOSRequest request
    ) {

        return request != null
                &&
                (
                        request.isAccepted()
                                ||
                        request.isInProgress()
                );
    }

    // =====================================================
    // CUSTOMER DISPLAY
    // =====================================================

    public String getCustomerDisplay(
            SOSRequest request
    ) {

        if (request == null) {

            return "Unknown Customer";
        }

        return firstNonBlank(
                request.getCustomerName(),
                request.getUserName(),
                request.getCustomerId(),
                request.getUserEmail(),
                "Unknown Customer"
        );
    }

    // =====================================================
    // CUSTOMER ID
    // =====================================================

    public String getCustomerId(
            SOSRequest request
    ) {

        if (request == null) {

            return "";
        }

        return firstNonBlank(
                request.getCustomerId(),
                request.getUserEmail(),
                request.getUserId(),
                ""
        );
    }

    // =====================================================
    // VEHICLE DISPLAY
    // =====================================================

    public String getVehicleDisplay(
            SOSRequest request
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

    // =====================================================
    // EMERGENCY TYPE
    // =====================================================

    public String getEmergencyDisplay(
            SOSRequest request
    ) {

        if (request == null) {

            return "Emergency Roadside Assistance";
        }

        return firstNonBlank(
                request.getEmergencyType(),
                request.getDescription(),
                "Emergency Roadside Assistance"
        );
    }

    // =====================================================
    // DESCRIPTION
    // =====================================================

    public String getDescriptionDisplay(
            SOSRequest request
    ) {

        if (request == null) {

            return "Emergency assistance requested";
        }

        return firstNonBlank(
                request.getDescription(),
                request.getEmergencyType(),
                "Emergency assistance requested"
        );
    }

    // =====================================================
    // LOCATION
    // =====================================================

    public String getLocationDisplay(
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

    // =====================================================
    // GPS
    // =====================================================

    public String getGPSDisplay(
            SOSRequest request
    ) {

        if (request == null) {

            return "GPS not available";
        }

        String latitude =
                clean(
                        request.getLatitude()
                );

        String longitude =
                clean(
                        request.getLongitude()
                );

        if (latitude == null
                ||
                longitude == null) {

            return "GPS not available";
        }

        return latitude
                + ", "
                + longitude;
    }

    // =====================================================
    // HAS GPS
    // =====================================================

    public boolean hasGPS(
            SOSRequest request
    ) {

        if (request == null) {

            return false;
        }

        return clean(
                request.getLatitude()
        ) != null
                &&
                clean(
                        request.getLongitude()
                ) != null;
    }

    // =====================================================
    // STATUS DISPLAY
    // =====================================================

    public String getStatusDisplay(
            SOSRequest request
    ) {

        if (request == null) {

            return "-";
        }

        return firstNonBlank(
                request.getStatus(),
                "-"
        );
    }

    // =====================================================
    // SOS ID DISPLAY
    // =====================================================

    public String getSOSIdDisplay(
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

    // =====================================================
    // REQUEST DATE
    // =====================================================

    public String getRequestDateDisplay(
            SOSRequest request
    ) {

        if (request == null) {

            return "-";
        }

        return firstNonBlank(
                request.getRequestDate(),
                "-"
        );
    }

    // =====================================================
    // MECHANIC DISPLAY
    // =====================================================

    public String getMechanicDisplay(
            SOSRequest request
    ) {

        if (request == null) {

            return "Mechanic";
        }

        return firstNonBlank(
                request.getMechanicName(),
                request.getResponderName(),
                request.getMechanicId(),
                "Mechanic"
        );
    }

    // =====================================================
    // STATUS NORMALIZATION
    // =====================================================

    private String normalizeStatus(
            String status
    ) {

        status =
                clean(
                        status
                );

        if (status == null) {

            return null;
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
                        "En Route"
                )
                        ||
                status.equalsIgnoreCase(
                        "EnRoute"
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
    // SAME STATUS
    // =====================================================

    private boolean sameStatus(
            String first,
            String second
    ) {

        first =
                normalizeStatus(
                        first
                );

        second =
                normalizeStatus(
                        second
                );

        return first != null
                &&
                second != null
                &&
                first.equalsIgnoreCase(
                        second
                );
    }

    // =====================================================
    // FIRST NON-BLANK
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