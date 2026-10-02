package project.controller.user;

import project.dao.mechanic.MechanicDAO;
import project.dao.user.ServiceRequestDAO;
import project.model.Mechanic;
import project.model.ServiceRequest;
import project.ui.user.UserSession;

import java.util.ArrayList;
import java.util.List;

/**
 * Customer live-tracking controller.
 *
 * IMPORTANT:
 * Tracking stays on the SAME serviceRequests document.
 * Mechanic NavigationPage writes navigationStatus / arrivalStatus
 * and this controller reads those values for the customer.
 */
public class LiveTrackingController {

    private final ServiceRequestDAO serviceRequestDAO;
    private final MechanicDAO mechanicDAO;

    public LiveTrackingController() {

        serviceRequestDAO =
                new ServiceRequestDAO();

        mechanicDAO =
                new MechanicDAO();
    }

    // =====================================================
    // CURRENT TRACKABLE REQUEST
    //
    // Track only requests where a mechanic exists:
    // Assigned / Accepted / In Progress.
    // Pending has no mechanic to track.
    // =====================================================

    public ServiceRequest getCurrentTrackingRequest() {

        ServiceRequest latest =
                null;

        long latestTime =
                Long.MIN_VALUE;

        for (String customerId :
                getCustomerCandidates()) {

            try {

                List<ServiceRequest> requests =
                        serviceRequestDAO
                                .getActiveRequests(
                                        customerId
                                );

                for (ServiceRequest request :
                        requests) {

                    if (!isTrackable(request)) {
                        continue;
                    }

                    long time =
                            requestTime(request);

                    if (latest == null
                            || time > latestTime) {

                        latest = request;
                        latestTime = time;
                    }
                }

            } catch (Exception e) {

                System.err.println(
                        "Unable to load tracking request for "
                                + customerId
                                + ": "
                                + e.getMessage()
                );
            }
        }

        return latest;
    }

    // =====================================================
    // REFRESH BY REQUEST ID
    // =====================================================

    public ServiceRequest refreshRequest(
            String requestId
    ) {

        requestId = clean(requestId);

        if (requestId == null) {
            return getCurrentTrackingRequest();
        }

        try {

            ServiceRequest request =
                    serviceRequestDAO
                            .getRequestById(
                                    requestId
                            );

            if (request == null
                    || !belongsToCurrentCustomer(request)) {

                return null;
            }

            return request;

        } catch (Exception e) {

            System.err.println(
                    "Unable to refresh tracking request: "
                            + e.getMessage()
            );

            return null;
        }
    }

    // =====================================================
    // MECHANIC PROFILE
    // =====================================================

    public Mechanic getMechanic(
            ServiceRequest request
    ) {

        if (request == null
                || clean(request.getMechanicId()) == null) {

            return null;
        }

        try {

            return mechanicDAO
                    .getMechanicById(
                            request.getMechanicId()
                    );

        } catch (Exception e) {

            System.err.println(
                    "Unable to load mechanic profile: "
                            + e.getMessage()
            );

            return null;
        }
    }

    // =====================================================
    // TRACKING STATE
    // =====================================================

    public boolean isNavigationStarted(
            ServiceRequest request
    ) {

        return request != null
                && request.isNavigationStarted();
    }

    public boolean hasArrived(
            ServiceRequest request
    ) {

        return request != null
                && request.hasArrived();
    }

    public String getTrackingHeadline(
            ServiceRequest request
    ) {

        if (request == null) {
            return "No active mechanic tracking request.";
        }

        if (request.hasArrived()) {
            return "Your mechanic has arrived at the service location.";
        }

        if (request.isNavigationStarted()) {
            return getMechanicName(request)
                    + " has started navigation to your location.";
        }

        String status =
                normalizeStatus(
                        request.getStatus()
                );

        if ("Assigned".equals(status)) {
            return getMechanicName(request)
                    + " is assigned and waiting to accept the request.";
        }

        if ("Accepted".equals(status)) {
            return getMechanicName(request)
                    + " accepted your request. Navigation has not started yet.";
        }

        if ("In Progress".equals(status)) {
            return "Repair is currently in progress.";
        }

        return "Current service status: "
                + status;
    }

    public String getMechanicName(
            ServiceRequest request
    ) {

        if (request == null) {
            return "Mechanic";
        }

        return firstNonBlank(
                request.getMechanicName(),
                request.getMechanicId(),
                "Mechanic"
        );
    }

    public String getVehicleDisplay(
            ServiceRequest request
    ) {

        if (request == null) {
            return "Not available";
        }

        return firstNonBlank(
                request.getVehicleNumber(),
                request.getVehicleId(),
                "Not available"
        );
    }

    public String getLocationDisplay(
            ServiceRequest request
    ) {

        if (request == null) {
            return "Location not available";
        }

        return firstNonBlank(
                request.getLocation(),
                "Location not available"
        );
    }

    /**
     * Returns the customer's saved latitude when it is valid.
     */
    public Double getCustomerLatitude(ServiceRequest request) {
        if (request == null) {
            return null;
        }

        Double value = request.getLatitude();
        return isValidLatitude(value) ? value : null;
    }

    /**
     * Returns the customer's saved longitude when it is valid.
     */
    public Double getCustomerLongitude(ServiceRequest request) {
        if (request == null) {
            return null;
        }

        Double value = request.getLongitude();
        return isValidLongitude(value) ? value : null;
    }

    /**
     * Returns the mechanic latitude from the service request first, then
     * falls back to the mechanic profile. This prevents the customer map
     * from depending on a single Firebase field being populated.
     */
    public Double getMechanicLatitude(ServiceRequest request) {
        if (request == null) {
            return null;
        }

        Double value = request.getMechanicLatitude();
        if (isValidLatitude(value)) {
            return value;
        }

        Mechanic mechanic = getMechanic(request);
        if (mechanic != null && mechanic.hasLocation()) {
            return mechanic.getLatitude();
        }

        return null;
    }

    /**
     * Returns the mechanic longitude from the service request first, then
     * falls back to the mechanic profile.
     */
    public Double getMechanicLongitude(ServiceRequest request) {
        if (request == null) {
            return null;
        }

        Double value = request.getMechanicLongitude();
        if (isValidLongitude(value)) {
            return value;
        }

        Mechanic mechanic = getMechanic(request);
        if (mechanic != null && mechanic.hasLocation()) {
            return mechanic.getLongitude();
        }

        return null;
    }

    /**
     * True when both customer coordinates are available and valid.
     */
    public boolean hasCustomerLocation(ServiceRequest request) {
        return getCustomerLatitude(request) != null
                && getCustomerLongitude(request) != null;
    }

    /**
     * True when both mechanic coordinates are available and valid.
     */
    public boolean hasMechanicLocation(ServiceRequest request) {
        return getMechanicLatitude(request) != null
                && getMechanicLongitude(request) != null;
    }

    /**
     * True when both endpoints required for routing are available.
     */
    public boolean hasBothLocations(ServiceRequest request) {
        return hasCustomerLocation(request)
                && hasMechanicLocation(request);
    }

    private boolean isValidLatitude(Double value) {
        return value != null
                && Double.isFinite(value)
                && value >= -90.0
                && value <= 90.0;
    }

    private boolean isValidLongitude(Double value) {
        return value != null
                && Double.isFinite(value)
                && value >= -180.0
                && value <= 180.0;
    }

    public String getCoordinatesDisplay(
            ServiceRequest request
    ) {

        Double latitude = getCustomerLatitude(request);
        Double longitude = getCustomerLongitude(request);

        if (latitude == null || longitude == null) {
            return "GPS coordinates not available";
        }

        return String.format(
                "%.6f, %.6f",
                latitude,
                longitude
        );
    }

    public String getStatusDisplay(
            ServiceRequest request
    ) {

        if (request == null) {
            return "No Active Request";
        }

        return normalizeStatus(
                request.getStatus()
        );
    }

    // =====================================================
    // OWNERSHIP
    // =====================================================

    private boolean belongsToCurrentCustomer(
            ServiceRequest request
    ) {

        if (request == null) {
            return false;
        }

        String requestCustomer =
                normalizeId(
                        request.getCustomerId()
                );

        if (requestCustomer == null) {
            return false;
        }

        for (String candidate :
                getCustomerCandidates()) {

            if (sameId(
                    requestCustomer,
                    candidate
            )) {

                return true;
            }
        }

        return false;
    }

    // =====================================================
    // TRACKABLE?
    // =====================================================

    private boolean isTrackable(
            ServiceRequest request
    ) {

        if (request == null
                || !request.hasMechanic()) {

            return false;
        }

        String status =
                normalizeStatus(
                        request.getStatus()
                );

        return "Assigned".equals(status)
                || "Accepted".equals(status)
                || "In Progress".equals(status);
    }

    // =====================================================
    // CUSTOMER CANDIDATES
    //
    // Login normally stores the normalized email as the
    // Firestore document id, but we safely try both email
    // and userId for older records.
    // =====================================================

    private List<String> getCustomerCandidates() {

        List<String> candidates =
                new ArrayList<>();

        addUnique(
                candidates,
                UserSession.getUserEmail()
        );

        addUnique(
                candidates,
                UserSession.getUserId()
        );

        return candidates;
    }

    private void addUnique(
            List<String> values,
            String value
    ) {

        String normalized =
                normalizeId(value);

        if (normalized == null) {
            return;
        }

        for (String current : values) {

            if (sameId(
                    current,
                    normalized
            )) {

                return;
            }
        }

        values.add(normalized);
    }

    // =====================================================
    // TIME
    // =====================================================

    private long requestTime(
            ServiceRequest request
    ) {

        if (request == null) {
            return 0L;
        }

        long value =
                parseTime(
                        request.getUpdatedAt()
                );

        if (value > 0) {
            return value;
        }

        value =
                parseTime(
                        request.getRequestDate()
                );

        return value;
    }

    private long parseTime(
            String value
    ) {

        value = clean(value);

        if (value == null) {
            return 0L;
        }

        try {

            long parsed =
                    Long.parseLong(value);

            if (parsed > 0
                    && parsed < 100000000000L) {

                parsed *= 1000L;
            }

            return parsed;

        } catch (Exception ignored) {

            return 0L;
        }
    }

    // =====================================================
    // STATUS
    // =====================================================

    private String normalizeStatus(
            String status
    ) {

        status = clean(status);

        if (status == null) {
            return "Pending";
        }

        if (status.equalsIgnoreCase("Pending")) {
            return "Pending";
        }

        if (status.equalsIgnoreCase("Assigned")) {
            return "Assigned";
        }

        if (status.equalsIgnoreCase("Accepted")) {
            return "Accepted";
        }

        if (status.equalsIgnoreCase("In Progress")
                || status.equalsIgnoreCase("InProgress")
                || status.equalsIgnoreCase("Active")
                || status.equalsIgnoreCase("Started")) {

            return "In Progress";
        }

        if (status.equalsIgnoreCase("Completed")
                || status.equalsIgnoreCase("Complete")
                || status.equalsIgnoreCase("Done")) {

            return "Completed";
        }

        if (status.equalsIgnoreCase("Cancelled")
                || status.equalsIgnoreCase("Canceled")) {

            return "Cancelled";
        }

        return status;
    }

    // =====================================================
    // ID / STRING HELPERS
    // =====================================================

    private boolean sameId(
            String first,
            String second
    ) {

        first = normalizeId(first);
        second = normalizeId(second);

        return first != null
                && second != null
                && first.equalsIgnoreCase(second);
    }

    private String normalizeId(
            String value
    ) {

        value = clean(value);

        if (value == null) {
            return null;
        }

        if (value.contains("@")) {
            return value.toLowerCase();
        }

        return value;
    }

    private String firstNonBlank(
            String... values
    ) {

        if (values == null) {
            return null;
        }

        for (String value : values) {

            String cleaned = clean(value);

            if (cleaned != null) {
                return cleaned;
            }
        }

        return null;
    }

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
