package project.model;

public class ServiceRequest {

    // =====================================================
    // REQUEST IDENTIFICATION
    // =====================================================

    private String requestId;

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
    // SERVICE INFORMATION
    // =====================================================

    private String serviceType;
    private String description;
    private String location;

    // =====================================================
    // OPTIONAL LOCATION COORDINATES
    // =====================================================

    private Double latitude;
    private Double longitude;

    // =====================================================
    // MECHANIC LIVE LOCATION
    // =====================================================

    private Double mechanicLatitude;
    private Double mechanicLongitude;
    private String mechanicLocationUpdatedAt;
    private String locationUpdatedAt;

    // =====================================================
    // LOCATION HELPERS
    // =====================================================

    public boolean hasCustomerLocation() {
        return latitude != null && longitude != null
                && Double.isFinite(latitude) && Double.isFinite(longitude)
                && latitude >= -90.0 && latitude <= 90.0
                && longitude >= -180.0 && longitude <= 180.0;
    }

    public boolean hasMechanicLocation() {
        return mechanicLatitude != null && mechanicLongitude != null
                && Double.isFinite(mechanicLatitude) && Double.isFinite(mechanicLongitude)
                && mechanicLatitude >= -90.0 && mechanicLatitude <= 90.0
                && mechanicLongitude >= -180.0 && mechanicLongitude <= 180.0;
    }

    public boolean hasBothLocations() {
        return hasCustomerLocation() && hasMechanicLocation();
    }

        // =====================================================
    // STATUS
    //
    // Pending
    // Assigned
    // Accepted
    // In Progress
    // Completed
    // Cancelled
    // =====================================================

    private String status;

    // =====================================================
    // REQUEST LIFECYCLE DATES
    // =====================================================

    private String requestDate;
    private String assignedDate;
    private String acceptedDate;
    private String startedDate;
    private String completedDate;
    private String cancelledDate;
    private String updatedAt;

    // =====================================================
    // LIVE TRACKING / NAVIGATION
    //
    // These fields live on the SAME serviceRequests document.
    // Mechanic NavigationPage updates them and Customer
    // LiveMapPage reads them. No separate tracking collection.
    // =====================================================

    private String navigationStatus;
    private String navigationStartedDate;
    private String arrivalStatus;
    private String arrivedDate;

    // =====================================================
    // MECHANIC WORK DETAILS
    // =====================================================

    private String diagnosis;
    private String repairDetails;

    // =====================================================
    // COST DETAILS
    //
    // String ठेवले आहेत कारण current UI TextField based आहे
    // आणि Firestore old/new numeric formatting mismatch टळेल.
    // =====================================================

    private String estimatedCost;
    private String partsCost;
    private String labourCost;
    private String finalCost;

    // =====================================================
    // EXTRA FLOW INFORMATION
    // =====================================================

    /*
     * Example:
     *
     * Customer
     * Tow Truck
     * AI Diagnosis
     * Admin
     */
    private String source;

    /*
     * Normal / High / Emergency
     */
    private String priority;

    // =====================================================
    // FIRESTORE EMPTY CONSTRUCTOR
    // =====================================================

    public ServiceRequest() {
    }

    // =====================================================
    // EXISTING CONSTRUCTOR
    //
    // IMPORTANT:
    // जुना constructor intentionally ठेवला आहे.
    // Existing project code break होऊ नये.
    // =====================================================

    public ServiceRequest(
            String requestId,
            String customerId,
            String customerName,
            String vehicleId,
            String vehicleNumber,
            String mechanicId,
            String mechanicName,
            String serviceType,
            String description,
            String location,
            String status,
            String requestDate,
            String completedDate
    ) {

        this.requestId = requestId;

        this.customerId = customerId;
        this.customerName = customerName;

        this.vehicleId = vehicleId;
        this.vehicleNumber = vehicleNumber;

        this.mechanicId = mechanicId;
        this.mechanicName = mechanicName;

        this.serviceType = serviceType;
        this.description = description;
        this.location = location;

        this.status = status;

        this.requestDate = requestDate;
        this.completedDate = completedDate;
    }

    // =====================================================
    // REQUEST ID
    // =====================================================

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(
            String requestId
    ) {
        this.requestId = requestId;
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
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(
            String customerName
    ) {
        this.customerName = customerName;
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
        this.vehicleId = vehicleId;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(
            String vehicleNumber
    ) {
        this.vehicleNumber = vehicleNumber;
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
        this.mechanicId = mechanicId;
    }

    public String getMechanicName() {
        return mechanicName;
    }

    public void setMechanicName(
            String mechanicName
    ) {
        this.mechanicName = mechanicName;
    }

    // =====================================================
    // SERVICE
    // =====================================================

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(
            String serviceType
    ) {
        this.serviceType = serviceType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(
            String description
    ) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(
            String location
    ) {
        this.location = location;
    }

    // =====================================================
    // LATITUDE / LONGITUDE
    // =====================================================

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(
            Double latitude
    ) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(
            Double longitude
    ) {
        this.longitude = longitude;
    }

    // =====================================================
    // MECHANIC LIVE LOCATION
    // =====================================================

    public Double getMechanicLatitude() {
        return mechanicLatitude;
    }

    public void setMechanicLatitude(Double mechanicLatitude) {
        this.mechanicLatitude = mechanicLatitude;
    }

    public Double getMechanicLongitude() {
        return mechanicLongitude;
    }

    public void setMechanicLongitude(Double mechanicLongitude) {
        this.mechanicLongitude = mechanicLongitude;
    }

    public String getLocationUpdatedAt() {
        return locationUpdatedAt;
    }

    public void setLocationUpdatedAt(String locationUpdatedAt) {
        this.locationUpdatedAt = locationUpdatedAt;
    }

    public String getMechanicLocationUpdatedAt() {
        return mechanicLocationUpdatedAt;
    }

    public void setMechanicLocationUpdatedAt(String mechanicLocationUpdatedAt) {
        this.mechanicLocationUpdatedAt = mechanicLocationUpdatedAt;
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
        this.status = status;
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
        this.requestDate = requestDate;
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
        this.assignedDate = assignedDate;
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
        this.acceptedDate = acceptedDate;
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
        this.startedDate = startedDate;
    }

    // =====================================================
    // COMPLETED DATE
    // =====================================================

    public String getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(
            String completedDate
    ) {
        this.completedDate = completedDate;
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
        this.cancelledDate = cancelledDate;
    }

    // =====================================================
    // UPDATED AT
    // =====================================================

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(
            String updatedAt
    ) {
        this.updatedAt = updatedAt;
    }

    // =====================================================
    // LIVE TRACKING / NAVIGATION
    // =====================================================

    public String getNavigationStatus() {
        return navigationStatus;
    }

    public void setNavigationStatus(
            String navigationStatus
    ) {
        this.navigationStatus = navigationStatus;
    }

    public String getNavigationStartedDate() {
        return navigationStartedDate;
    }

    public void setNavigationStartedDate(
            String navigationStartedDate
    ) {
        this.navigationStartedDate = navigationStartedDate;
    }

    public String getArrivalStatus() {
        return arrivalStatus;
    }

    public void setArrivalStatus(
            String arrivalStatus
    ) {
        this.arrivalStatus = arrivalStatus;
    }

    public String getArrivedDate() {
        return arrivedDate;
    }

    public void setArrivedDate(
            String arrivedDate
    ) {
        this.arrivedDate = arrivedDate;
    }

    public boolean isNavigationStarted() {

        return navigationStatus != null
                && navigationStatus.equalsIgnoreCase(
                        "Started"
                );
    }

    public boolean hasArrived() {

        return arrivalStatus != null
                && arrivalStatus.equalsIgnoreCase(
                        "Arrived"
                );
    }

    // =====================================================
    // DIAGNOSIS
    // =====================================================

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(
            String diagnosis
    ) {
        this.diagnosis = diagnosis;
    }

    // =====================================================
    // REPAIR DETAILS
    // =====================================================

    public String getRepairDetails() {
        return repairDetails;
    }

    public void setRepairDetails(
            String repairDetails
    ) {
        this.repairDetails = repairDetails;
    }

    // =====================================================
    // ESTIMATED COST
    // =====================================================

    public String getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(
            String estimatedCost
    ) {
        this.estimatedCost = estimatedCost;
    }

    // =====================================================
    // PARTS COST
    // =====================================================

    public String getPartsCost() {
        return partsCost;
    }

    public void setPartsCost(
            String partsCost
    ) {
        this.partsCost = partsCost;
    }

    // =====================================================
    // LABOUR COST
    // =====================================================

    public String getLabourCost() {
        return labourCost;
    }

    public void setLabourCost(
            String labourCost
    ) {
        this.labourCost = labourCost;
    }

    // =====================================================
    // FINAL COST
    // =====================================================

    public String getFinalCost() {
        return finalCost;
    }

    public void setFinalCost(
            String finalCost
    ) {
        this.finalCost = finalCost;
    }

    // =====================================================
    // SOURCE
    // =====================================================

    public String getSource() {
        return source;
    }

    public void setSource(
            String source
    ) {
        this.source = source;
    }

    // =====================================================
    // PRIORITY
    // =====================================================

    public String getPriority() {
        return priority;
    }

    public void setPriority(
            String priority
    ) {
        this.priority = priority;
    }

    // =====================================================
    // STATUS HELPERS
    // =====================================================

    public boolean isPending() {

        return equalsStatus(
                "Pending"
        );
    }

    public boolean isAssigned() {

        return equalsStatus(
                "Assigned"
        );
    }

    public boolean isAccepted() {

        return equalsStatus(
                "Accepted"
        );
    }

    public boolean isInProgress() {

        return equalsStatus(
                "In Progress"
        );
    }

    public boolean isCompleted() {

        return equalsStatus(
                "Completed"
        );
    }

    public boolean isCancelled() {

        return equalsStatus(
                "Cancelled"
        );
    }

    // =====================================================
    // ACTIVE REQUEST
    // =====================================================

    public boolean isActive() {

        return isPending()
                || isAssigned()
                || isAccepted()
                || isInProgress();
    }

    // =====================================================
    // FINISHED REQUEST
    // =====================================================

    public boolean isFinished() {

        return isCompleted()
                || isCancelled();
    }

    // =====================================================
    // HAS MECHANIC
    // =====================================================

    public boolean hasMechanic() {

        return mechanicId != null
                &&
                !mechanicId.isBlank();
    }

    // =====================================================
    // STATUS HELPER
    // =====================================================

    private boolean equalsStatus(
            String expectedStatus
    ) {

        return status != null
                &&
                expectedStatus != null
                &&
                status.equalsIgnoreCase(
                        expectedStatus
                );
    }

    // =====================================================
    // TO STRING
    // =====================================================

    @Override
    public String toString() {

        return "ServiceRequest{" +
                "requestId='" + requestId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", customerName='" + customerName + '\'' +
                ", vehicleId='" + vehicleId + '\'' +
                ", vehicleNumber='" + vehicleNumber + '\'' +
                ", mechanicId='" + mechanicId + '\'' +
                ", mechanicName='" + mechanicName + '\'' +
                ", serviceType='" + serviceType + '\'' +
                ", location='" + location + '\'' +
                ", status='" + status + '\'' +
                ", navigationStatus='" + navigationStatus + '\'' +
                ", arrivalStatus='" + arrivalStatus + '\'' +
                ", requestDate='" + requestDate + '\'' +
                '}';
    }
}