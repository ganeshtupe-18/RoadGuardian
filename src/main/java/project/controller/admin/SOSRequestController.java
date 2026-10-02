package project.controller.admin;

import com.google.cloud.firestore.Firestore;

import project.dao.admin.SOSRequestDAO;
import project.model.SOSRequest;

import java.util.ArrayList;
import java.util.List;

public class SOSRequestController {

    // =====================================================
    // DAO
    // =====================================================

    private final SOSRequestDAO sosRequestDAO;

    // =====================================================
    // EXISTING CONSTRUCTOR
    // =====================================================

    public SOSRequestController(
            Firestore firestore
    ) {

        this.sosRequestDAO =
                new SOSRequestDAO(
                        firestore
                );
    }

    // =====================================================
    // OPTIONAL DEFAULT CONSTRUCTOR
    // =====================================================

    public SOSRequestController() {

        this.sosRequestDAO =
                new SOSRequestDAO();
    }

    // =====================================================
    // GET ALL
    // =====================================================

    public List<SOSRequest> getAllRequests() {

        try {

            return sosRequestDAO
                    .getAllRequests();

        } catch (Exception e) {

            System.err.println(
                    "Unable to load SOS requests: "
                            + e.getMessage()
            );

            e.printStackTrace();

            return new ArrayList<>();
        }
    }

    // =====================================================
    // GET BY ID
    // =====================================================

    public SOSRequest getRequestById(
            String sosId
    ) {

        try {

            sosId =
                    clean(
                            sosId
                    );

            if (sosId == null) {

                return null;
            }

            return sosRequestDAO
                    .getRequestById(
                            sosId
                    );

        } catch (Exception e) {

            System.err.println(
                    "Unable to load SOS request: "
                            + e.getMessage()
            );

            return null;
        }
    }

    // =====================================================
    // GET CUSTOMER REQUESTS
    // =====================================================

    public List<SOSRequest> getRequestsByCustomerId(
            String customerId
    ) {

        try {

            customerId =
                    clean(
                            customerId
                    );

            if (customerId == null) {

                return new ArrayList<>();
            }

            return sosRequestDAO
                    .getRequestsByCustomerId(
                            customerId
                    );

        } catch (Exception e) {

            System.err.println(
                    "Unable to load customer SOS requests: "
                            + e.getMessage()
            );

            return new ArrayList<>();
        }
    }

    // =====================================================
    // GET MECHANIC REQUESTS
    // =====================================================

    public List<SOSRequest> getRequestsByMechanicId(
            String mechanicId
    ) {

        try {

            mechanicId =
                    clean(
                            mechanicId
                    );

            if (mechanicId == null) {

                return new ArrayList<>();
            }

            return sosRequestDAO
                    .getRequestsByMechanicId(
                            mechanicId
                    );

        } catch (Exception e) {

            System.err.println(
                    "Unable to load mechanic SOS requests: "
                            + e.getMessage()
            );

            return new ArrayList<>();
        }
    }

    // =====================================================
    // ADD SOS REQUEST
    //
    // Vehicle is NOT mandatory because Customer SOS
    // currently allows emergency request without vehicle.
    // =====================================================

    public boolean addRequest(
            SOSRequest request
    ) {

        try {

            if (request == null) {

                return false;
            }

            // =================================================
            // CUSTOMER
            // =================================================

            String customerId =
                    firstNonBlank(
                            request.getCustomerId(),
                            request.getUserEmail(),
                            request.getUserId()
                    );

            if (customerId == null) {

                return false;
            }

            request.setCustomerId(
                    customerId
            );

            // =================================================
            // CUSTOMER NAME
            // =================================================

            if (clean(
                    request.getCustomerName()
            ) == null) {

                request.setCustomerName(
                        firstNonBlank(
                                request.getUserName(),
                                "Customer"
                        )
                );
            }

            // =================================================
            // EMERGENCY TYPE
            // =================================================

            if (clean(
                    request.getEmergencyType()
            ) == null) {

                request.setEmergencyType(
                        "Emergency Roadside Assistance"
                );
            }

            // =================================================
            // DESCRIPTION
            // =================================================

            if (clean(
                    request.getDescription()
            ) == null) {

                request.setDescription(
                        "Emergency SOS request"
                );
            }

            // =================================================
            // STATUS
            // =================================================

            if (clean(
                    request.getStatus()
            ) == null) {

                request.setStatus(
                        "Pending"
                );
            }

            // =================================================
            // REQUEST DATE
            // =================================================

            if (clean(
                    request.getRequestDate()
            ) == null) {

                request.setRequestDate(
                        String.valueOf(
                                System.currentTimeMillis()
                        )
                );
            }

            // =================================================
            // EMPTY LIFECYCLE DATES
            // =================================================

            if (request.getAssignedDate() == null) {

                request.setAssignedDate(
                        ""
                );
            }

            if (request.getAcceptedDate() == null) {

                request.setAcceptedDate(
                        ""
                );
            }

            if (request.getStartedDate() == null) {

                request.setStartedDate(
                        ""
                );
            }

            if (request.getResolvedDate() == null) {

                request.setResolvedDate(
                        ""
                );
            }

            if (request.getCancelledDate() == null) {

                request.setCancelledDate(
                        ""
                );
            }

            return sosRequestDAO
                    .addRequest(
                            request
                    );

        } catch (Exception e) {

            System.err.println(
                    "Unable to add SOS request: "
                            + e.getMessage()
            );

            e.printStackTrace();

            return false;
        }
    }

    // =====================================================
    // UPDATE REQUEST
    // =====================================================

    public boolean updateRequest(
            SOSRequest request
    ) {

        try {

            if (request == null
                    ||
                    clean(
                            request.getSosId()
                    ) == null) {

                return false;
            }

            return sosRequestDAO
                    .updateRequest(
                            request
                    );

        } catch (Exception e) {

            System.err.println(
                    "Unable to update SOS request: "
                            + e.getMessage()
            );

            return false;
        }
    }

    // =====================================================
    // UPDATE STATUS
    //
    // DAO handles:
    //
    // assignedDate
    // acceptedDate
    // startedDate
    // resolvedDate
    // cancelledDate
    // timeline
    // updatedAt
    // =====================================================

    public boolean updateStatus(
            String sosId,
            String status
    ) {

        try {

            sosId =
                    clean(
                            sosId
                    );

            status =
                    normalizeStatus(
                            status
                    );

            if (sosId == null
                    ||
                    status == null) {

                return false;
            }

            return sosRequestDAO
                    .updateStatus(
                            sosId,
                            status
                    );

        } catch (Exception e) {

            System.err.println(
                    "Unable to update SOS status: "
                            + e.getMessage()
            );

            e.printStackTrace();

            return false;
        }
    }

    // =====================================================
    // ASSIGN MECHANIC
    //
    // Pending -> Assigned
    //
    // IMPORTANT:
    // Admin does NOT accept request for mechanic.
    // =====================================================

    public boolean assignMechanic(
            String sosId,
            String mechanicId,
            String mechanicName
    ) {

        try {

            sosId =
                    clean(
                            sosId
                    );

            mechanicId =
                    clean(
                            mechanicId
                    );

            mechanicName =
                    clean(
                            mechanicName
                    );

            if (sosId == null
                    ||
                    mechanicId == null) {

                return false;
            }

            if (mechanicName == null) {

                mechanicName =
                        "Mechanic";
            }

            return sosRequestDAO
                    .assignMechanic(
                            sosId,
                            mechanicId,
                            mechanicName
                    );

        } catch (Exception e) {

            System.err.println(
                    "Unable to assign mechanic to SOS: "
                            + e.getMessage()
            );

            e.printStackTrace();

            return false;
        }
    }

    // =====================================================
    // ACCEPT SOS
    //
    // Normally Mechanic side will call this.
    //
    // Assigned -> Accepted
    // =====================================================

    public boolean acceptRequest(
            String sosId,
            String mechanicId
    ) {

        try {

            if (clean(sosId) == null
                    ||
                    clean(mechanicId) == null) {

                return false;
            }

            return sosRequestDAO
                    .acceptRequest(
                            sosId,
                            mechanicId
                    );

        } catch (Exception e) {

            System.err.println(
                    "Unable to accept SOS request: "
                            + e.getMessage()
            );

            return false;
        }
    }

    // =====================================================
    // START SOS
    //
    // Accepted -> In Progress
    // =====================================================

    public boolean startRequest(
            String sosId,
            String mechanicId
    ) {

        try {

            if (clean(sosId) == null
                    ||
                    clean(mechanicId) == null) {

                return false;
            }

            return sosRequestDAO
                    .startRequest(
                            sosId,
                            mechanicId
                    );

        } catch (Exception e) {

            System.err.println(
                    "Unable to start SOS assistance: "
                            + e.getMessage()
            );

            return false;
        }
    }

    // =====================================================
    // RESOLVE
    //
    // In Progress -> Resolved
    // =====================================================

    public boolean resolveRequest(
            String sosId
    ) {

        try {

            if (clean(sosId) == null) {

                return false;
            }

            return sosRequestDAO
                    .resolveRequest(
                            sosId
                    );

        } catch (Exception e) {

            System.err.println(
                    "Unable to resolve SOS request: "
                            + e.getMessage()
            );

            return false;
        }
    }

    // =====================================================
    // CANCEL
    // =====================================================

    public boolean cancelRequest(
            String sosId
    ) {

        try {

            if (clean(sosId) == null) {

                return false;
            }

            return sosRequestDAO
                    .cancelRequest(
                            sosId
                    );

        } catch (Exception e) {

            System.err.println(
                    "Unable to cancel SOS request: "
                            + e.getMessage()
            );

            return false;
        }
    }

    // =====================================================
    // DELETE
    // =====================================================

    public boolean deleteRequest(
            String sosId
    ) {

        try {

            sosId =
                    clean(
                            sosId
                    );

            if (sosId == null) {

                return false;
            }

            return sosRequestDAO
                    .deleteRequest(
                            sosId
                    );

        } catch (Exception e) {

            System.err.println(
                    "Unable to delete SOS request: "
                            + e.getMessage()
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

            System.err.println(
                    "Unable to search SOS requests: "
                            + e.getMessage()
            );

            return new ArrayList<>();
        }
    }

    // =====================================================
    // FILTER BY STATUS
    // =====================================================

    public List<SOSRequest> getRequestsByStatus(
            String status
    ) {

        try {

            if (status == null
                    ||
                    status.isBlank()
                    ||
                    status.equalsIgnoreCase(
                            "All"
                    )
                    ||
                    status.equalsIgnoreCase(
                            "All Status"
                    )) {

                return getAllRequests();
            }

            return sosRequestDAO
                    .getRequestsByStatus(
                            normalizeStatus(
                                    status
                            )
                    );

        } catch (Exception e) {

            System.err.println(
                    "Unable to filter SOS requests: "
                            + e.getMessage()
            );

            return new ArrayList<>();
        }
    }

    // =====================================================
    // PENDING
    // =====================================================

    public List<SOSRequest> getPendingRequestsList() {

        return getRequestsByStatus(
                "Pending"
        );
    }

    // =====================================================
    // ASSIGNED
    // =====================================================

    public List<SOSRequest> getAssignedRequests() {

        try {

            return sosRequestDAO
                    .getAssignedRequests();

        } catch (Exception e) {

            return new ArrayList<>();
        }
    }

    // =====================================================
    // ACCEPTED
    // =====================================================

    public List<SOSRequest> getAcceptedRequestsList() {

        try {

            return sosRequestDAO
                    .getAcceptedRequests();

        } catch (Exception e) {

            return new ArrayList<>();
        }
    }

    // =====================================================
    // IN PROGRESS
    // =====================================================

    public List<SOSRequest> getInProgressRequestsList() {

        try {

            return sosRequestDAO
                    .getInProgressRequests();

        } catch (Exception e) {

            return new ArrayList<>();
        }
    }

    // =====================================================
    // ACTIVE
    //
    // Pending + Assigned + Accepted + In Progress
    // =====================================================

    public List<SOSRequest> getActiveRequests() {

        try {

            return sosRequestDAO
                    .getActiveRequests();

        } catch (Exception e) {

            return new ArrayList<>();
        }
    }

    // =====================================================
    // SEARCH + STATUS FILTER
    //
    // Useful directly from Management Page.
    // =====================================================

    public List<SOSRequest> getFilteredRequests(
            String searchText,
            String status
    ) {

        List<SOSRequest> source;

        if (clean(searchText) == null) {

            source =
                    getAllRequests();

        } else {

            source =
                    searchRequests(
                            searchText
                    );
        }

        if (status == null
                ||
                status.isBlank()
                ||
                status.equalsIgnoreCase(
                        "All"
                )
                ||
                status.equalsIgnoreCase(
                        "All Status"
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

            if (request == null
                    ||
                    request.getStatus() == null) {

                continue;
            }

            if (request
                    .getStatus()
                    .equalsIgnoreCase(
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
    // STATISTICS
    // =====================================================

    public int getTotalRequests() {

        return getAllRequests()
                .size();
    }

    public int getPendingRequests() {

        return countByStatus(
                "Pending"
        );
    }

    // =====================================================
    // NEW ASSIGNED COUNT
    // =====================================================

    public int getAssignedRequestsCount() {

        return countByStatus(
                "Assigned"
        );
    }

    /*
     * Alternate name for UI convenience.
     */
    public int getAssignedCount() {

        return getAssignedRequestsCount();
    }

    public int getAcceptedRequests() {

        return countByStatus(
                "Accepted"
        );
    }

    public int getInProgressRequests() {

        return countByStatus(
                "In Progress"
        );
    }

    public int getResolvedRequests() {

        return countByStatus(
                "Resolved"
        );
    }

    public int getCancelledRequests() {

        return countByStatus(
                "Cancelled"
        );
    }

    // =====================================================
    // ACTIVE COUNT
    // =====================================================

    public int getActiveRequestsCount() {

        int count =
                0;

        for (SOSRequest request :
                getAllRequests()) {

            if (request != null
                    &&
                    request.isActive()) {

                count++;
            }
        }

        return count;
    }

    // =====================================================
    // MECHANIC ASSIGNED?
    // =====================================================

    public boolean hasMechanic(
            SOSRequest request
    ) {

        return request != null
                &&
                request.hasMechanic();
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
    // MECHANIC DISPLAY
    // =====================================================

    public String getMechanicDisplay(
            SOSRequest request
    ) {

        if (request == null) {

            return "Not Assigned";
        }

        return firstNonBlank(
                request.getMechanicName(),
                request.getResponderName(),
                request.getMechanicId(),
                "Not Assigned"
        );
    }

    // =====================================================
    // EMERGENCY DISPLAY
    // =====================================================

    public String getEmergencyDisplay(
            SOSRequest request
    ) {

        if (request == null) {

            return "Emergency";
        }

        return firstNonBlank(
                request.getEmergencyType(),
                request.getDescription(),
                "Emergency Roadside Assistance"
        );
    }

    // =====================================================
    // LOCATION DISPLAY
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
                "Pending"
        );
    }

    // =====================================================
    // GPS DISPLAY
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
    // CAN ASSIGN?
    // =====================================================

    public boolean canAssignMechanic(
            SOSRequest request
    ) {

        if (request == null
                ||
                request.getStatus() == null) {

            return false;
        }

        return request.isPending()
                ||
                request.isAssigned();
    }

    // =====================================================
    // CAN CANCEL?
    // =====================================================

    public boolean canCancel(
            SOSRequest request
    ) {

        return request != null
                &&
                !request.isFinished();
    }

    // =====================================================
    // COUNT BY STATUS
    // =====================================================

    private int countByStatus(
            String status
    ) {

        String requiredStatus =
                normalizeStatus(
                        status
                );

        int count =
                0;

        for (SOSRequest request :
                getAllRequests()) {

            if (request == null
                    ||
                    request.getStatus() == null) {

                continue;
            }

            if (normalizeStatus(
                    request.getStatus()
            ).equalsIgnoreCase(
                    requiredStatus
            )) {

                count++;
            }
        }

        return count;
    }

    // =====================================================
    // NORMALIZE STATUS
    // =====================================================

    private String normalizeStatus(
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
                        ||
                status.equalsIgnoreCase(
                        "Closed"
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
}