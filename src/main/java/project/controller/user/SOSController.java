package project.controller.user;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;

import project.dao.user.SOSDAO;
import project.model.SOSRequest;
import project.ui.user.UserSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SOSController {

    // =====================================================
    // DAO
    // =====================================================

    private final SOSDAO sosDAO;

    // =====================================================
    // CURRENT SOS ID
    // =====================================================

    private String currentSOSId;

    // =====================================================
    // DEFAULT CONSTRUCTOR
    // =====================================================

    public SOSController() {

        this.sosDAO =
                new SOSDAO();
    }

    // =====================================================
    // FIRESTORE CONSTRUCTOR
    // =====================================================

    public SOSController(
            Firestore firestore
    ) {

        this.sosDAO =
                new SOSDAO(
                        firestore
                );
    }

    // =====================================================
    // ACTIVATE SOS
    //
    // Customer creates:
    //
    // Pending
    //
    // If an active SOS already exists, another duplicate
    // SOS is NOT created.
    // =====================================================

    public boolean activateSOS(
            String location,
            String latitude,
            String longitude
    ) {

        try {

            // =================================================
            // LOGIN CHECK
            // =================================================

            if (!UserSession.isLoggedIn()) {

                System.err.println(
                        "SOS activation failed: No logged-in user."
                );

                return false;
            }

            String userId =
                    clean(
                            UserSession.getUserId()
                    );

            String userName =
                    clean(
                            UserSession.getUserName()
                    );

            String userEmail =
                    normalizeId(
                            UserSession.getUserEmail()
                    );

            String customerLookupId =
                    firstNonBlank(
                            userEmail,
                            userId
                    );

            if (customerLookupId == null) {

                System.err.println(
                        "SOS activation failed: Customer ID/email unavailable."
                );

                return false;
            }

            // =================================================
            // CHECK EXISTING ACTIVE SOS
            // =================================================

            DocumentSnapshot existing =
                    sosDAO.getActiveSOS(
                            customerLookupId
                    );

            if (existing != null
                    &&
                    existing.exists()) {

                currentSOSId =
                        firstNonBlank(
                                stringValue(
                                        existing.get(
                                                "sosId"
                                        )
                                ),
                                existing.getId()
                        );

                System.out.println(
                        "Existing active SOS found: "
                                + currentSOSId
                );

                return true;
            }

            // =================================================
            // CREATE NEW SOS
            // =================================================

            currentSOSId =
                    sosDAO.createSOSRequest(
                            firstNonBlank(
                                    userId,
                                    customerLookupId
                            ),
                            firstNonBlank(
                                    userName,
                                    "Customer"
                            ),
                            userEmail,
                            location,
                            latitude,
                            longitude
                    );

            if (currentSOSId == null
                    ||
                    currentSOSId.isBlank()) {

                return false;
            }

            System.out.println(
                    "Customer SOS activated: "
                            + currentSOSId
            );

            return true;

        } catch (Exception e) {

            logError(
                    "Unable to activate SOS",
                    e
            );

            return false;
        }
    }

    // =====================================================
    // OLD COMPATIBILITY METHOD
    //
    // DO NOT REMOVE.
    //
    // Existing SOSPage may still use DocumentSnapshot.
    // =====================================================

    public DocumentSnapshot getActiveSOS() {

        try {

            String customerId =
                    getCurrentCustomerId();

            if (customerId == null) {

                return null;
            }

            DocumentSnapshot document =
                    sosDAO.getActiveSOS(
                            customerId
                    );

            if (document != null
                    &&
                    document.exists()) {

                currentSOSId =
                        firstNonBlank(
                                stringValue(
                                        document.get(
                                                "sosId"
                                        )
                                ),
                                document.getId()
                        );
            }

            return document;

        } catch (Exception e) {

            logError(
                    "Unable to load active SOS",
                    e
            );

            return null;
        }
    }

    // =====================================================
    // GET ACTIVE SOS MODEL
    //
    // New SOSPage should mainly use this.
    // =====================================================

    public SOSRequest getActiveSOSRequest() {

        try {

            String customerId =
                    getCurrentCustomerId();

            if (customerId == null) {

                return null;
            }

            SOSRequest request =
                    sosDAO.getActiveSOSRequest(
                            customerId
                    );

            if (request != null) {

                currentSOSId =
                        request.getSosId();
            }

            return request;

        } catch (Exception e) {

            logError(
                    "Unable to load active SOS request",
                    e
            );

            return null;
        }
    }

    // =====================================================
    // REFRESH ACTIVE SOS
    //
    // Reads latest Firebase document every time.
    // =====================================================

    public SOSRequest refreshActiveSOS() {

        return getActiveSOSRequest();
    }

    // =====================================================
    // CURRENT SOS REQUEST
    // =====================================================

    public SOSRequest getCurrentSOSRequest() {

        try {

            // =================================================
            // WE ALREADY KNOW ID
            // =================================================

            if (clean(
                    currentSOSId
            ) != null) {

                SOSRequest request =
                        sosDAO.getSOSById(
                                currentSOSId
                        );

                if (request != null) {

                    return request;
                }
            }

            // =================================================
            // OTHERWISE FIND ACTIVE SOS
            // =================================================

            return getActiveSOSRequest();

        } catch (Exception e) {

            logError(
                    "Unable to load current SOS",
                    e
            );

            return null;
        }
    }

    // =====================================================
    // GET SOS BY ID
    // =====================================================

    public SOSRequest getSOSById(
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

            SOSRequest request =
                    sosDAO.getSOSById(
                            sosId
                    );

            if (request != null) {

                currentSOSId =
                        request.getSosId();
            }

            return request;

        } catch (Exception e) {

            logError(
                    "Unable to load SOS request",
                    e
            );

            return null;
        }
    }

    // =====================================================
    // ALL SOS REQUESTS OF CURRENT CUSTOMER
    // =====================================================

    public List<SOSRequest> getMySOSRequests() {

        try {

            String customerId =
                    getCurrentCustomerId();

            if (customerId == null) {

                return new ArrayList<>();
            }

            return sosDAO.getSOSRequests(
                    customerId
            );

        } catch (Exception e) {

            logError(
                    "Unable to load customer SOS history",
                    e
            );

            return new ArrayList<>();
        }
    }

    // =====================================================
    // ACTIVE SOS EXISTS?
    // =====================================================

    public boolean hasActiveSOS() {

        SOSRequest request =
                getActiveSOSRequest();

        return request != null
                &&
                request.isActive();
    }

    // =====================================================
    // GET CURRENT SOS ID
    //
    // Existing method preserved.
    // =====================================================

    public String getCurrentSOSId() {

        if (clean(
                currentSOSId
        ) != null) {

            return currentSOSId;
        }

        SOSRequest request =
                getActiveSOSRequest();

        if (request != null) {

            currentSOSId =
                    request.getSosId();
        }

        return currentSOSId;
    }

    // =====================================================
    // COMPLETE / RESOLVE
    //
    // Existing method preserved.
    //
    // Normally Mechanic resolves SOS.
    // Kept for compatibility with old UI.
    // =====================================================

    public boolean completeSOS() {

        try {

            String sosId =
                    resolveCurrentSOSId();

            if (sosId == null) {

                return false;
            }

            boolean success =
                    sosDAO.completeSOS(
                            sosId
                    );

            if (success) {

                SOSRequest updated =
                        sosDAO.getSOSById(
                                sosId
                        );

                if (updated == null
                        ||
                        updated.isFinished()) {

                    currentSOSId =
                            null;
                }
            }

            return success;

        } catch (Exception e) {

            logError(
                    "Unable to complete SOS",
                    e
            );

            return false;
        }
    }

    // =====================================================
    // CANCEL SOS
    //
    // Existing method preserved.
    // =====================================================

    public boolean cancelSOS() {

        try {

            String sosId =
                    resolveCurrentSOSId();

            if (sosId == null) {

                return false;
            }

            SOSRequest current =
                    sosDAO.getSOSById(
                            sosId
                    );

            if (current == null) {

                return false;
            }

            // =================================================
            // RESOLVED CANNOT BE CANCELLED
            // =================================================

            if (current.isResolved()) {

                return false;
            }

            boolean success =
                    sosDAO.cancelSOS(
                            sosId
                    );

            if (success) {

                currentSOSId =
                        null;
            }

            return success;

        } catch (Exception e) {

            logError(
                    "Unable to cancel SOS",
                    e
            );

            return false;
        }
    }

    // =====================================================
    // CANCEL SPECIFIC SOS
    // =====================================================

    public boolean cancelSOS(
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
                    sosDAO.getSOSById(
                            sosId
                    );

            if (request == null
                    ||
                    request.isResolved()) {

                return false;
            }

            boolean success =
                    sosDAO.cancelSOS(
                            sosId
                    );

            if (success
                    &&
                    sosId.equals(
                            currentSOSId
                    )) {

                currentSOSId =
                        null;
            }

            return success;

        } catch (Exception e) {

            logError(
                    "Unable to cancel SOS",
                    e
            );

            return false;
        }
    }

    // =====================================================
    // EMERGENCY CONTACTS
    //
    // Existing method preserved.
    // =====================================================

    public List<Map<String, Object>> getEmergencyContacts() {

        try {

            if (!UserSession.isLoggedIn()) {

                return new ArrayList<>();
            }

            String customerId =
                    getCurrentCustomerId();

            if (customerId == null) {

                return new ArrayList<>();
            }

            return sosDAO.getEmergencyContacts(
                    customerId
            );

        } catch (Exception e) {

            logError(
                    "Unable to load emergency contacts",
                    e
            );

            return new ArrayList<>();
        }
    }

    // =====================================================
    // STATUS
    // =====================================================

    public String getStatus(
            SOSRequest request
    ) {

        if (request == null) {

            return "No Active SOS";
        }

        return firstNonBlank(
                request.getStatus(),
                "Pending"
        );
    }

    // =====================================================
    // STATUS HELPERS
    // =====================================================

    public boolean isPending(
            SOSRequest request
    ) {

        return request != null
                &&
                request.isPending();
    }

    public boolean isAssigned(
            SOSRequest request
    ) {

        return request != null
                &&
                request.isAssigned();
    }

    public boolean isAccepted(
            SOSRequest request
    ) {

        return request != null
                &&
                request.isAccepted();
    }

    public boolean isInProgress(
            SOSRequest request
    ) {

        return request != null
                &&
                request.isInProgress();
    }

    public boolean isResolved(
            SOSRequest request
    ) {

        return request != null
                &&
                request.isResolved();
    }

    public boolean isCancelled(
            SOSRequest request
    ) {

        return request != null
                &&
                request.isCancelled();
    }

    // =====================================================
    // CAN CANCEL?
    // =====================================================

    public boolean canCancel(
            SOSRequest request
    ) {

        if (request == null) {

            return false;
        }

        return request.isPending()
                ||
                request.isAssigned()
                ||
                request.isAccepted();
    }

    // =====================================================
    // CUSTOMER DISPLAY
    // =====================================================

    public String getCustomerDisplay(
            SOSRequest request
    ) {

        if (request == null) {

            return getCurrentCustomerName();
        }

        return firstNonBlank(
                request.getCustomerName(),
                request.getUserName(),
                getCurrentCustomerName()
        );
    }

    // =====================================================
    // MECHANIC DISPLAY
    // =====================================================

    public String getMechanicDisplay(
            SOSRequest request
    ) {

        if (request == null) {

            return "Waiting for mechanic";
        }

        if (!request.hasMechanic()) {

            return "Waiting for mechanic";
        }

        return firstNonBlank(
                request.getMechanicName(),
                request.getResponderName(),
                request.getMechanicId(),
                "Assigned Mechanic"
        );
    }

    // =====================================================
    // RESPONDER STATUS
    // =====================================================

    public String getResponderStatusDisplay(
            SOSRequest request
    ) {

        if (request == null) {

            return "Waiting";
        }

        String responderStatus =
                clean(
                        request.getResponderStatus()
                );

        if (responderStatus != null) {

            return responderStatus;
        }

        if (request.isPending()) {

            return "Waiting for assignment";
        }

        if (request.isAssigned()) {

            return "Mechanic assigned";
        }

        if (request.isAccepted()) {

            return "Mechanic accepted";
        }

        if (request.isInProgress()) {

            return "Responder en route";
        }

        if (request.isResolved()) {

            return "Completed";
        }

        if (request.isCancelled()) {

            return "Cancelled";
        }

        return firstNonBlank(
                request.getStatus(),
                "Waiting"
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
    // EMERGENCY TYPE
    // =====================================================

    public String getEmergencyTypeDisplay(
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

    // =====================================================
    // DESCRIPTION
    // =====================================================

    public String getDescriptionDisplay(
            SOSRequest request
    ) {

        if (request == null) {

            return "Emergency roadside assistance requested";
        }

        return firstNonBlank(
                request.getDescription(),
                "Emergency roadside assistance requested"
        );
    }

    // =====================================================
    // VEHICLE
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
    // REQUEST DATE
    // =====================================================

    public String getRequestDate(
            SOSRequest request
    ) {

        if (request == null) {

            return "";
        }

        return firstNonBlank(
                request.getRequestDate(),
                ""
        );
    }

    // =====================================================
    // ASSIGNED DATE
    // =====================================================

    public String getAssignedDate(
            SOSRequest request
    ) {

        if (request == null) {

            return "";
        }

        return firstNonBlank(
                request.getAssignedDate(),
                ""
        );
    }

    // =====================================================
    // ACCEPTED DATE
    // =====================================================

    public String getAcceptedDate(
            SOSRequest request
    ) {

        if (request == null) {

            return "";
        }

        return firstNonBlank(
                request.getAcceptedDate(),
                ""
        );
    }

    // =====================================================
    // STARTED DATE
    // =====================================================

    public String getStartedDate(
            SOSRequest request
    ) {

        if (request == null) {

            return "";
        }

        return firstNonBlank(
                request.getStartedDate(),
                ""
        );
    }

    // =====================================================
    // RESOLVED DATE
    // =====================================================

    public String getResolvedDate(
            SOSRequest request
    ) {

        if (request == null) {

            return "";
        }

        return firstNonBlank(
                request.getResolvedDate(),
                ""
        );
    }

    // =====================================================
    // TIMELINE
    // =====================================================

    public List<Map<String, Object>> getTimeline(
            SOSRequest request
    ) {

        if (request == null
                ||
                request.getTimeline() == null) {

            return new ArrayList<>();
        }

        return new ArrayList<>(
                request.getTimeline()
        );
    }

    // =====================================================
    // CURRENT CUSTOMER ID
    // =====================================================

    private String getCurrentCustomerId() {

        if (!UserSession.isLoggedIn()) {

            return null;
        }

        String email =
                normalizeId(
                        UserSession.getUserEmail()
                );

        if (email != null) {

            return email;
        }

        return normalizeId(
                UserSession.getUserId()
        );
    }

    // =====================================================
    // CURRENT CUSTOMER NAME
    // =====================================================

    private String getCurrentCustomerName() {

        return firstNonBlank(
                UserSession.getUserName(),
                "Customer"
        );
    }

    // =====================================================
    // RESOLVE CURRENT SOS ID
    // =====================================================

    private String resolveCurrentSOSId() {

        String id =
                clean(
                        currentSOSId
                );

        if (id != null) {

            return id;
        }

        SOSRequest active =
                getActiveSOSRequest();

        if (active == null) {

            return null;
        }

        currentSOSId =
                active.getSosId();

        return currentSOSId;
    }

    // =====================================================
    // NORMALIZE ID
    // =====================================================

    private String normalizeId(
            String value
    ) {

        value =
                clean(
                        value
                );

        if (value == null) {

            return null;
        }

        if (value.contains("@")) {

            return value.toLowerCase();
        }

        return value;
    }

    // =====================================================
    // STRING VALUE
    // =====================================================

    private String stringValue(
            Object value
    ) {

        if (value == null) {

            return null;
        }

        String text =
                String.valueOf(
                        value
                ).trim();

        return text.isEmpty()
                ? null
                : text;
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