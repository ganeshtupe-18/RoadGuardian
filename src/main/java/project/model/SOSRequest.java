package project.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SOSRequest {

    // =====================================================
    // SOS ID
    // =====================================================

    private String sosId;

    // =====================================================
    // CUSTOMER
    // =====================================================

    private String customerId;
    private String customerName;

    // =====================================================
    // VEHICLE
    // =====================================================

    private String vehicleId;
    private String vehicleNumber;

    // =====================================================
    // MECHANIC
    // =====================================================

    private String mechanicId;
    private String mechanicName;

    // =====================================================
    // EMERGENCY
    // =====================================================

    private String emergencyType;
    private String description;

    // =====================================================
    // LOCATION
    // =====================================================

    private String location;
    private String latitude;
    private String longitude;

    // =====================================================
    // STATUS
    //
    // Pending
    // Assigned
    // Accepted
    // In Progress
    // Resolved
    // Cancelled
    // =====================================================

    private String status;

    // =====================================================
    // LIFECYCLE DATES
    // =====================================================

    private String requestDate;

    private String assignedDate;

    private String acceptedDate;

    private String startedDate;

    private String resolvedDate;

    private String cancelledDate;

    /*
     * User SOSDAO currently stores updatedAt as a Long.
     * Object keeps Firestore mapping safe for both
     * old String and new numeric timestamps.
     */
    private Object updatedAt;

    private Object createdAt;

    // =====================================================
    // OLD CUSTOMER-SIDE COMPATIBILITY FIELDS
    // =====================================================

    private String userId;
    private String userName;
    private String userEmail;

    // =====================================================
    // OLD RESPONDER COMPATIBILITY
    // =====================================================

    private String responderId;
    private String responderName;
    private String responderStatus;

    // =====================================================
    // SOS EXTRA DATA
    // =====================================================

    private Boolean contactsNotified;

    private List<Map<String, Object>> timeline;

    // =====================================================
    // FIRESTORE EMPTY CONSTRUCTOR
    // =====================================================

    public SOSRequest() {
    }

    // =====================================================
    // OLD CONSTRUCTOR
    //
    // DO NOT REMOVE.
    //
    // Existing project compatibility.
    // =====================================================

    public SOSRequest(
            String sosId,
            String customerId,
            String customerName,
            String vehicleId,
            String vehicleNumber,
            String mechanicId,
            String mechanicName,
            String emergencyType,
            String description,
            String location,
            String latitude,
            String longitude,
            String status,
            String requestDate,
            String resolvedDate
    ) {

        this.sosId =
                sosId;

        this.customerId =
                customerId;

        this.customerName =
                customerName;

        this.vehicleId =
                vehicleId;

        this.vehicleNumber =
                vehicleNumber;

        this.mechanicId =
                mechanicId;

        this.mechanicName =
                mechanicName;

        this.emergencyType =
                emergencyType;

        this.description =
                description;

        this.location =
                location;

        this.latitude =
                latitude;

        this.longitude =
                longitude;

        this.status =
                status;

        this.requestDate =
                requestDate;

        this.resolvedDate =
                resolvedDate;
    }

    // =====================================================
    // FULL CONSTRUCTOR
    // =====================================================

    public SOSRequest(
            String sosId,
            String customerId,
            String customerName,
            String vehicleId,
            String vehicleNumber,
            String mechanicId,
            String mechanicName,
            String emergencyType,
            String description,
            String location,
            String latitude,
            String longitude,
            String status,
            String requestDate,
            String assignedDate,
            String acceptedDate,
            String startedDate,
            String resolvedDate,
            String cancelledDate,
            Object updatedAt
    ) {

        this.sosId =
                sosId;

        this.customerId =
                customerId;

        this.customerName =
                customerName;

        this.vehicleId =
                vehicleId;

        this.vehicleNumber =
                vehicleNumber;

        this.mechanicId =
                mechanicId;

        this.mechanicName =
                mechanicName;

        this.emergencyType =
                emergencyType;

        this.description =
                description;

        this.location =
                location;

        this.latitude =
                latitude;

        this.longitude =
                longitude;

        this.status =
                status;

        this.requestDate =
                requestDate;

        this.assignedDate =
                assignedDate;

        this.acceptedDate =
                acceptedDate;

        this.startedDate =
                startedDate;

        this.resolvedDate =
                resolvedDate;

        this.cancelledDate =
                cancelledDate;

        this.updatedAt =
                updatedAt;
    }

    // =====================================================
    // SOS ID
    // =====================================================

    public String getSosId() {

        return sosId;
    }

    public void setSosId(
            String sosId
    ) {

        this.sosId =
                sosId;
    }

    // =====================================================
    // CUSTOMER
    // =====================================================

    public String getCustomerId() {

        return customerId;
    }

    public void setCustomerId(
            String customerId
    ) {

        this.customerId =
                customerId;
    }

    public String getCustomerName() {

        return customerName;
    }

    public void setCustomerName(
            String customerName
    ) {

        this.customerName =
                customerName;
    }

    // =====================================================
    // VEHICLE
    // =====================================================

    public String getVehicleId() {

        return vehicleId;
    }

    public void setVehicleId(
            String vehicleId
    ) {

        this.vehicleId =
                vehicleId;
    }

    public String getVehicleNumber() {

        return vehicleNumber;
    }

    public void setVehicleNumber(
            String vehicleNumber
    ) {

        this.vehicleNumber =
                vehicleNumber;
    }

    // =====================================================
    // MECHANIC
    // =====================================================

    public String getMechanicId() {

        return mechanicId;
    }

    public void setMechanicId(
            String mechanicId
    ) {

        this.mechanicId =
                mechanicId;
    }

    public String getMechanicName() {

        return mechanicName;
    }

    public void setMechanicName(
            String mechanicName
    ) {

        this.mechanicName =
                mechanicName;
    }

    // =====================================================
    // EMERGENCY
    // =====================================================

    public String getEmergencyType() {

        return emergencyType;
    }

    public void setEmergencyType(
            String emergencyType
    ) {

        this.emergencyType =
                emergencyType;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(
            String description
    ) {

        this.description =
                description;
    }

    // =====================================================
    // LOCATION
    // =====================================================

    public String getLocation() {

        return location;
    }

    public void setLocation(
            String location
    ) {

        this.location =
                location;
    }

    public String getLatitude() {

        return latitude;
    }

    public void setLatitude(
            String latitude
    ) {

        this.latitude =
                latitude;
    }

    public String getLongitude() {

        return longitude;
    }

    public void setLongitude(
            String longitude
    ) {

        this.longitude =
                longitude;
    }

    // =====================================================
    // STATUS
    // =====================================================

    public String getStatus() {

        return status;
    }

    public void setStatus(
            String status
    ) {

        this.status =
                normalizeStatus(
                        status
                );
    }

    // =====================================================
    // REQUEST DATE
    // =====================================================

    public String getRequestDate() {

        return requestDate;
    }

    public void setRequestDate(
            String requestDate
    ) {

        this.requestDate =
                requestDate;
    }

    // =====================================================
    // ASSIGNED DATE
    // =====================================================

    public String getAssignedDate() {

        return assignedDate;
    }

    public void setAssignedDate(
            String assignedDate
    ) {

        this.assignedDate =
                assignedDate;
    }

    // =====================================================
    // ACCEPTED DATE
    // =====================================================

    public String getAcceptedDate() {

        return acceptedDate;
    }

    public void setAcceptedDate(
            String acceptedDate
    ) {

        this.acceptedDate =
                acceptedDate;
    }

    // =====================================================
    // STARTED DATE
    // =====================================================

    public String getStartedDate() {

        return startedDate;
    }

    public void setStartedDate(
            String startedDate
    ) {

        this.startedDate =
                startedDate;
    }

    // =====================================================
    // RESOLVED DATE
    // =====================================================

    public String getResolvedDate() {

        return resolvedDate;
    }

    public void setResolvedDate(
            String resolvedDate
    ) {

        this.resolvedDate =
                resolvedDate;
    }

    // =====================================================
    // CANCELLED DATE
    // =====================================================

    public String getCancelledDate() {

        return cancelledDate;
    }

    public void setCancelledDate(
            String cancelledDate
    ) {

        this.cancelledDate =
                cancelledDate;
    }

    // =====================================================
    // UPDATED AT
    // =====================================================

    public Object getUpdatedAt() {

        return updatedAt;
    }

    public void setUpdatedAt(
            Object updatedAt
    ) {

        this.updatedAt =
                updatedAt;
    }

    // =====================================================
    // CREATED AT
    // =====================================================

    public Object getCreatedAt() {

        return createdAt;
    }

    public void setCreatedAt(
            Object createdAt
    ) {

        this.createdAt =
                createdAt;
    }

    // =====================================================
    // OLD USER ALIASES
    // =====================================================

    public String getUserId() {

        return userId;
    }

    public void setUserId(
            String userId
    ) {

        this.userId =
                userId;
    }

    public String getUserName() {

        return userName;
    }

    public void setUserName(
            String userName
    ) {

        this.userName =
                userName;
    }

    public String getUserEmail() {

        return userEmail;
    }

    public void setUserEmail(
            String userEmail
    ) {

        this.userEmail =
                userEmail;
    }

    // =====================================================
    // OLD RESPONDER ALIASES
    // =====================================================

    public String getResponderId() {

        return responderId;
    }

    public void setResponderId(
            String responderId
    ) {

        this.responderId =
                responderId;
    }

    public String getResponderName() {

        return responderName;
    }

    public void setResponderName(
            String responderName
    ) {

        this.responderName =
                responderName;
    }

    public String getResponderStatus() {

        return responderStatus;
    }

    public void setResponderStatus(
            String responderStatus
    ) {

        this.responderStatus =
                responderStatus;
    }

    // =====================================================
    // CONTACTS NOTIFIED
    // =====================================================

    public Boolean getContactsNotified() {

        return contactsNotified;
    }

    public void setContactsNotified(
            Boolean contactsNotified
    ) {

        this.contactsNotified =
                contactsNotified;
    }

    // =====================================================
    // TIMELINE
    // =====================================================

    public List<Map<String, Object>> getTimeline() {

        if (timeline == null) {

            timeline =
                    new ArrayList<>();
        }

        return timeline;
    }

    public void setTimeline(
            List<Map<String, Object>> timeline
    ) {

        this.timeline =
                timeline;
    }

    // =====================================================
    // STATUS HELPERS
    // =====================================================

    public boolean isPending() {

        return hasStatus(
                "Pending"
        );
    }

    public boolean isAssigned() {

        return hasStatus(
                "Assigned"
        );
    }

    public boolean isAccepted() {

        return hasStatus(
                "Accepted"
        );
    }

    public boolean isInProgress() {

        return hasStatus(
                "In Progress"
        );
    }

    public boolean isResolved() {

        return hasStatus(
                "Resolved"
        );
    }

    public boolean isCancelled() {

        return hasStatus(
                "Cancelled"
        );
    }

    // =====================================================
    // ACTIVE?
    // =====================================================

    public boolean isActive() {

        return isPending()
                ||
                isAssigned()
                ||
                isAccepted()
                ||
                isInProgress();
    }

    // =====================================================
    // FINISHED?
    // =====================================================

    public boolean isFinished() {

        return isResolved()
                ||
                isCancelled();
    }

    // =====================================================
    // HAS MECHANIC?
    // =====================================================

    public boolean hasMechanic() {

        return mechanicId != null
                &&
                !mechanicId
                        .trim()
                        .isEmpty();
    }

    // =====================================================
    // STATUS CHECK
    // =====================================================

    private boolean hasStatus(
            String expected
    ) {

        return status != null
                &&
                expected != null
                &&
                status.equalsIgnoreCase(
                        expected
                );
    }

    // =====================================================
    // STATUS NORMALIZATION
    // =====================================================

    private String normalizeStatus(
            String value
    ) {

        if (value == null
                ||
                value.trim().isEmpty()) {

            return value;
        }

        String status =
                value.trim();

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
    // TO STRING
    // =====================================================

    @Override
    public String toString() {

        return "SOSRequest{" +

                "sosId='"
                + sosId
                + '\'' +

                ", customerName='"
                + customerName
                + '\'' +

                ", vehicleNumber='"
                + vehicleNumber
                + '\'' +

                ", mechanicName='"
                + mechanicName
                + '\'' +

                ", emergencyType='"
                + emergencyType
                + '\'' +

                ", location='"
                + location
                + '\'' +

                ", status='"
                + status
                + '\'' +

                '}';

    }
}