package project.controller.user;

import com.google.cloud.firestore.Firestore;

import project.dao.user.UserDashboardDAO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UserDashboardController {

    // =====================================================
    // DAO
    // =====================================================

    private final UserDashboardDAO dashboardDAO;

    // =====================================================
    // DEFAULT CONSTRUCTOR
    // =====================================================

    public UserDashboardController() {

        try {

            dashboardDAO =
                    new UserDashboardDAO();

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to initialize UserDashboardDAO",
                    e
            );
        }
    }

    // =====================================================
    // FIRESTORE CONSTRUCTOR
    // =====================================================

    public UserDashboardController(
            Firestore firestore
    ) {

        dashboardDAO =
                new UserDashboardDAO(
                        firestore
                );
    }

    // =====================================================
    // USER PROFILE
    // =====================================================

    public Map<String, Object> getUserProfile(
            String email
    ) {

        try {

            Map<String, Object> profile =
                    dashboardDAO
                            .getUserProfile(
                                    email
                            );

            return profile == null
                    ? new HashMap<>()
                    : profile;

        } catch (Exception e) {

            logError(
                    "Unable to load user profile",
                    e
            );

            return new HashMap<>();
        }
    }

    // =====================================================
    // VEHICLES
    // =====================================================

    public List<Map<String, Object>> getVehicles(
            String email
    ) {

        try {

            List<Map<String, Object>> vehicles =
                    dashboardDAO
                            .getUserVehicles(
                                    email
                            );

            return vehicles == null
                    ? new ArrayList<>()
                    : vehicles;

        } catch (Exception e) {

            logError(
                    "Unable to load vehicles",
                    e
            );

            return new ArrayList<>();
        }
    }

    // =====================================================
    // ACTIVE SERVICE REQUESTS
    //
    // Pending
    // Assigned
    // Accepted
    // In Progress
    // =====================================================

    public List<Map<String, Object>> getActiveRequests(
            String email
    ) {

        try {

            List<Map<String, Object>> requests =
                    dashboardDAO
                            .getActiveRequests(
                                    email
                            );

            return requests == null
                    ? new ArrayList<>()
                    : requests;

        } catch (Exception e) {

            logError(
                    "Unable to load active service requests",
                    e
            );

            return new ArrayList<>();
        }
    }

    // =====================================================
    // LATEST ACTIVE REQUEST
    // =====================================================

    public Map<String, Object> getLatestActiveRequest(
            String email
    ) {

        List<Map<String, Object>> requests =
                getActiveRequests(
                        email
                );

        if (requests.isEmpty()) {

            return null;
        }

        return requests.get(0);
    }

    // =====================================================
    // SERVICE HISTORY
    //
    // Completed + Cancelled
    // =====================================================

    public List<Map<String, Object>> getServiceHistory(
            String email
    ) {

        try {

            List<Map<String, Object>> history =
                    dashboardDAO
                            .getServiceHistory(
                                    email
                            );

            return history == null
                    ? new ArrayList<>()
                    : history;

        } catch (Exception e) {

            logError(
                    "Unable to load service history",
                    e
            );

            return new ArrayList<>();
        }
    }

    // =====================================================
    // MECHANICS
    // =====================================================

    public List<Map<String, Object>> getNearbyMechanics() {

        try {

            List<Map<String, Object>> mechanics =
                    dashboardDAO
                            .getNearbyMechanics();

            return mechanics == null
                    ? new ArrayList<>()
                    : mechanics;

        } catch (Exception e) {

            logError(
                    "Unable to load mechanics",
                    e
            );

            return new ArrayList<>();
        }
    }

    // =====================================================
    // VEHICLE HEALTH
    // =====================================================

    public Map<String, Object> getVehicleHealth(
            String email,
            String vehicleId
    ) {

        try {

            Map<String, Object> health =
                    dashboardDAO
                            .getVehicleHealth(
                                    email,
                                    vehicleId
                            );

            return health == null
                    ? new HashMap<>()
                    : health;

        } catch (Exception e) {

            logError(
                    "Unable to load vehicle health",
                    e
            );

            return new HashMap<>();
        }
    }

    // =====================================================
    // CUSTOMER NAME
    // =====================================================

    public String getCustomerName(
            Map<String, Object> profile
    ) {

        if (profile == null) {

            return "Customer";
        }

        return firstNonBlank(
                value(
                        profile,
                        "name"
                ),
                value(
                        profile,
                        "customerName"
                ),
                value(
                        profile,
                        "userName"
                ),
                "Customer"
        );
    }

    // =====================================================
    // TOTAL VEHICLES
    // =====================================================

    public int getVehicleCount(
            String email
    ) {

        return getVehicles(
                email
        ).size();
    }

    // =====================================================
    // ACTIVE REQUEST COUNT
    // =====================================================

    public int getActiveRequestCount(
            String email
    ) {

        return getActiveRequests(
                email
        ).size();
    }

    // =====================================================
    // HISTORY COUNT
    // =====================================================

    public int getHistoryCount(
            String email
    ) {

        return getServiceHistory(
                email
        ).size();
    }

    // =====================================================
    // STATUS COUNTS
    // =====================================================

    public int getPendingRequestCount(
            String email
    ) {

        return countRequestsByStatus(
                getActiveRequests(
                        email
                ),
                "Pending"
        );
    }

    public int getAssignedRequestCount(
            String email
    ) {

        return countRequestsByStatus(
                getActiveRequests(
                        email
                ),
                "Assigned"
        );
    }

    public int getAcceptedRequestCount(
            String email
    ) {

        return countRequestsByStatus(
                getActiveRequests(
                        email
                ),
                "Accepted"
        );
    }

    public int getInProgressRequestCount(
            String email
    ) {

        return countRequestsByStatus(
                getActiveRequests(
                        email
                ),
                "In Progress"
        );
    }

    // =====================================================
    // COMPLETED COUNT
    // =====================================================

    public int getCompletedServiceCount(
            String email
    ) {

        return countRequestsByStatus(
                getServiceHistory(
                        email
                ),
                "Completed"
        );
    }

    // =====================================================
    // CANCELLED COUNT
    // =====================================================

    public int getCancelledServiceCount(
            String email
    ) {

        return countRequestsByStatus(
                getServiceHistory(
                        email
                ),
                "Cancelled"
        );
    }

    // =====================================================
    // AVAILABLE MECHANIC COUNT
    // =====================================================

    public int getAvailableMechanicCount() {

        return getNearbyMechanics()
                .size();
    }

    // =====================================================
    // TOTAL CUSTOMER SPEND
    //
    // Completed jobs only.
    // =====================================================

    public double getTotalServiceSpend(
            String email
    ) {

        double total =
                0.0;

        for (Map<String, Object> request :
                getServiceHistory(
                        email
                )) {

            if (!hasStatus(
                    request,
                    "Completed"
            )) {

                continue;
            }

            total +=
                    getRequestAmount(
                            request
                    );
        }

        return total;
    }

    // =====================================================
    // TOTAL SPEND DISPLAY
    // =====================================================

    public String getTotalServiceSpendDisplay(
            String email
    ) {

        return formatMoney(
                getTotalServiceSpend(
                        email
                )
        );
    }

    // =====================================================
    // ACTIVE REQUEST SERVICE
    // =====================================================

    public String getRequestService(
            Map<String, Object> request
    ) {

        if (request == null) {

            return "No Active Request";
        }

        return firstNonBlank(
                value(
                        request,
                        "serviceType"
                ),
                value(
                        request,
                        "service"
                ),
                value(
                        request,
                        "serviceName"
                ),
                "Vehicle Service"
        );
    }

    // =====================================================
    // REQUEST STATUS
    // =====================================================

    public String getRequestStatus(
            Map<String, Object> request
    ) {

        if (request == null) {

            return "None";
        }

        return normalizeStatus(
                value(
                        request,
                        "status"
                )
        );
    }

    // =====================================================
    // REQUEST MECHANIC
    // =====================================================

    public String getRequestMechanic(
            Map<String, Object> request
    ) {

        if (request == null) {

            return "Mechanic not assigned";
        }

        String mechanicName =
                clean(
                        value(
                                request,
                                "mechanicName"
                        )
                );

        String mechanicId =
                clean(
                        value(
                                request,
                                "mechanicId"
                        )
                );

        if (mechanicName != null
                &&
                !mechanicName.equalsIgnoreCase(
                        "Mechanic not assigned"
                )) {

            return mechanicName;
        }

        if (mechanicId != null) {

            return mechanicId;
        }

        return "Mechanic not assigned";
    }

    // =====================================================
    // REQUEST LOCATION
    // =====================================================

    public String getRequestLocation(
            Map<String, Object> request
    ) {

        if (request == null) {

            return "Location not available";
        }

        return firstNonBlank(
                value(
                        request,
                        "location"
                ),
                value(
                        request,
                        "address"
                ),
                "Location not available"
        );
    }

    // =====================================================
    // REQUEST VEHICLE
    // =====================================================

    public String getRequestVehicle(
            Map<String, Object> request
    ) {

        if (request == null) {

            return "Vehicle not available";
        }

        return firstNonBlank(
                value(
                        request,
                        "vehicleNumber"
                ),
                value(
                        request,
                        "registrationNumber"
                ),
                value(
                        request,
                        "vehicleId"
                ),
                "Vehicle not available"
        );
    }

    // =====================================================
    // REQUEST ID
    // =====================================================

    public String getRequestId(
            Map<String, Object> request
    ) {

        if (request == null) {

            return "-";
        }

        return firstNonBlank(
                value(
                        request,
                        "requestId"
                ),
                value(
                        request,
                        "documentId"
                ),
                "-"
        );
    }

    // =====================================================
    // CUSTOMER-FRIENDLY STATUS MESSAGE
    // =====================================================

    public String getRequestStatusMessage(
            Map<String, Object> request
    ) {

        if (request == null) {

            return "No active service request.";
        }

        String status =
                getRequestStatus(
                        request
                );

        if (status.equalsIgnoreCase(
                "Pending"
        )) {

            return "Waiting for a mechanic to be assigned.";
        }

        if (status.equalsIgnoreCase(
                "Assigned"
        )) {

            return getRequestMechanic(
                    request
            )
                    + " has been assigned to your request.";
        }

        if (status.equalsIgnoreCase(
                "Accepted"
        )) {

            return getRequestMechanic(
                    request
            )
                    + " accepted your service request.";
        }

        if (status.equalsIgnoreCase(
                "In Progress"
        )) {

            return "Your vehicle service is currently in progress.";
        }

        if (status.equalsIgnoreCase(
                "Completed"
        )) {

            return "Your service request has been completed.";
        }

        if (status.equalsIgnoreCase(
                "Cancelled"
        )) {

            return "Your service request was cancelled.";
        }

        return "Current status: "
                + status;
    }

    // =====================================================
    // SHOULD SHOW LIVE MAP?
    //
    // Before mechanic assignment there is nobody to track.
    // =====================================================

    public boolean canViewLiveMap(
            Map<String, Object> request
    ) {

        if (request == null) {

            return false;
        }

        String status =
                getRequestStatus(
                        request
                );

        return status.equalsIgnoreCase(
                "Assigned"
        )
                ||
                status.equalsIgnoreCase(
                        "Accepted"
                )
                ||
                status.equalsIgnoreCase(
                        "In Progress"
                );
    }

    // =====================================================
    // HISTORY TITLE
    // =====================================================

    public String getHistoryTitle(
            Map<String, Object> request
    ) {

        return getRequestService(
                request
        );
    }

    // =====================================================
    // HISTORY DATE
    // =====================================================

    public String getHistoryDate(
            Map<String, Object> request
    ) {

        if (request == null) {

            return "";
        }

        return firstNonBlank(
                value(
                        request,
                        "date"
                ),
                value(
                        request,
                        "serviceDate"
                ),
                value(
                        request,
                        "completedDate"
                ),
                value(
                        request,
                        "cancelledDate"
                ),
                ""
        );
    }

    // =====================================================
    // HISTORY STATUS
    // =====================================================

    public String getHistoryStatus(
            Map<String, Object> request
    ) {

        return getRequestStatus(
                request
        );
    }

    // =====================================================
    // HISTORY MECHANIC
    // =====================================================

    public String getHistoryMechanic(
            Map<String, Object> request
    ) {

        return getRequestMechanic(
                request
        );
    }

    // =====================================================
    // HISTORY AMOUNT
    // =====================================================

    public double getRequestAmount(
            Map<String, Object> request
    ) {

        if (request == null) {

            return 0.0;
        }

        if (hasStatus(
                request,
                "Cancelled"
        )) {

            return 0.0;
        }

        // =================================================
        // FINAL COST
        // =================================================

        double finalCost =
                parseMoney(
                        firstNonNull(
                                request.get(
                                        "finalCost"
                                ),
                                request.get(
                                        "totalCost"
                                ),
                                request.get(
                                        "amount"
                                ),
                                request.get(
                                        "cost"
                                )
                        )
                );

        if (finalCost > 0) {

            return finalCost;
        }

        // =================================================
        // PARTS + LABOUR
        // =================================================

        double parts =
                parseMoney(
                        request.get(
                                "partsCost"
                        )
                );

        double labour =
                parseMoney(
                        firstNonNull(
                                request.get(
                                        "labourCost"
                                ),
                                request.get(
                                        "laborCost"
                                )
                        )
                );

        double calculated =
                parts + labour;

        if (calculated > 0) {

            return calculated;
        }

        // =================================================
        // OLD DATA FALLBACK
        // =================================================

        return parseMoney(
                request.get(
                        "estimatedCost"
                )
        );
    }

    // =====================================================
    // HISTORY AMOUNT DISPLAY
    // =====================================================

    public String getHistoryAmountDisplay(
            Map<String, Object> request
    ) {

        return formatMoney(
                getRequestAmount(
                        request
                )
        );
    }

    // =====================================================
    // VEHICLE DISPLAY
    // =====================================================

    public String getVehicleDisplay(
            Map<String, Object> vehicle
    ) {

        if (vehicle == null) {

            return "Vehicle";
        }

        String name =
                firstNonBlank(
                        value(
                                vehicle,
                                "name"
                        ),
                        join(
                                value(
                                        vehicle,
                                        "brand"
                                ),
                                value(
                                        vehicle,
                                        "model"
                                )
                        ),
                        join(
                                value(
                                        vehicle,
                                        "make"
                                ),
                                value(
                                        vehicle,
                                        "model"
                                )
                        ),
                        "Vehicle"
                );

        String number =
                firstNonBlank(
                        value(
                                vehicle,
                                "vehicleNumber"
                        ),
                        value(
                                vehicle,
                                "registrationNumber"
                        ),
                        ""
                );

        if (clean(
                number
        ) == null) {

            return name;
        }

        return name
                + " · "
                + number;
    }

    // =====================================================
    // VEHICLE HEALTH DISPLAY
    //
    // IMPORTANT:
    // No fake "Good".
    // =====================================================

    public String getVehicleHealthDisplay(
            Map<String, Object> vehicle
    ) {

        if (vehicle == null) {

            return "No Data";
        }

        Object healthObject =
                vehicle.get(
                        "health"
                );

        if (healthObject instanceof Map<?, ?>) {

            Map<?, ?> health =
                    (Map<?, ?>) healthObject;

            String status =
                    clean(
                            objectValue(
                                    health.get(
                                            "status"
                                    )
                            )
                    );

            if (status != null) {

                return status;
            }

            String score =
                    clean(
                            objectValue(
                                    health.get(
                                            "score"
                                    )
                            )
                    );

            if (score != null) {

                return score.endsWith("%")
                        ? score
                        : score + "%";
            }
        }

        String status =
                clean(
                        value(
                                vehicle,
                                "healthStatus"
                        )
                );

        if (status != null) {

            return status;
        }

        String score =
                clean(
                        value(
                                vehicle,
                                "healthScore"
                        )
                );

        if (score != null) {

            return score.endsWith("%")
                    ? score
                    : score + "%";
        }

        return "No Data";
    }

    // =====================================================
    // NEXT SERVICE
    // =====================================================

    public String getNextServiceDisplay(
            Map<String, Object> vehicle
    ) {

        if (vehicle == null) {

            return "Not Set";
        }

        return firstNonBlank(
                value(
                        vehicle,
                        "nextService"
                ),
                value(
                        vehicle,
                        "nextServiceDate"
                ),
                "Not Set"
        );
    }

    // =====================================================
    // INSURANCE
    // =====================================================

    public String getInsuranceDisplay(
            Map<String, Object> vehicle
    ) {

        if (vehicle == null) {

            return "Not Set";
        }

        return firstNonBlank(
                value(
                        vehicle,
                        "insuranceStatus"
                ),
                value(
                        vehicle,
                        "insuranceExpiry"
                ),
                "Not Set"
        );
    }

    // =====================================================
    // MECHANIC DISPLAY
    //
    // No fake distance / ETA / rating.
    // =====================================================

    public String getMechanicName(
            Map<String, Object> mechanic
    ) {

        if (mechanic == null) {

            return "Mechanic";
        }

        return firstNonBlank(
                value(
                        mechanic,
                        "name"
                ),
                value(
                        mechanic,
                        "mechanicName"
                ),
                value(
                        mechanic,
                        "email"
                ),
                "Mechanic"
        );
    }

    public String getMechanicSpecialization(
            Map<String, Object> mechanic
    ) {

        if (mechanic == null) {

            return "Automobile Service";
        }

        return firstNonBlank(
                value(
                        mechanic,
                        "specialization"
                ),
                "Automobile Service"
        );
    }

    public String getMechanicId(
            Map<String, Object> mechanic
    ) {

        if (mechanic == null) {

            return "";
        }

        return firstNonBlank(
                value(
                        mechanic,
                        "mechanicId"
                ),
                value(
                        mechanic,
                        "email"
                ),
                value(
                        mechanic,
                        "documentId"
                ),
                ""
        );
    }

    // =====================================================
    // STATUS COUNT
    // =====================================================

    private int countRequestsByStatus(
            List<Map<String, Object>> requests,
            String status
    ) {

        if (requests == null
                ||
                status == null) {

            return 0;
        }

        int count =
                0;

        for (Map<String, Object> request :
                requests) {

            if (hasStatus(
                    request,
                    status
            )) {

                count++;
            }
        }

        return count;
    }

    // =====================================================
    // STATUS CHECK
    // =====================================================

    private boolean hasStatus(
            Map<String, Object> request,
            String status
    ) {

        if (request == null
                ||
                status == null) {

            return false;
        }

        return normalizeStatus(
                value(
                        request,
                        "status"
                )
        ).equalsIgnoreCase(
                normalizeStatus(
                        status
                )
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
    // FORMAT MONEY
    // =====================================================

    private String formatMoney(
            double amount
    ) {

        if (!Double.isFinite(
                amount
        )
                ||
                amount < 0) {

            amount =
                    0.0;
        }

        if (amount == Math.rint(
                amount
        )) {

            return "₹"
                    + String.format(
                            Locale.US,
                            "%,.0f",
                            amount
                    );
        }

        return "₹"
                + String.format(
                        Locale.US,
                        "%,.2f",
                        amount
                );
    }

    // =====================================================
    // PARSE MONEY
    // =====================================================

    private double parseMoney(
            Object value
    ) {

        if (value == null) {

            return 0.0;
        }

        if (value instanceof Number) {

            double number =
                    ((Number) value)
                            .doubleValue();

            return number < 0
                    ? 0.0
                    : number;
        }

        String text =
                String.valueOf(
                        value
                )
                        .replace(
                                "₹",
                                ""
                        )
                        .replace(
                                ",",
                                ""
                        )
                        .trim();

        if (text.isEmpty()) {

            return 0.0;
        }

        try {

            double amount =
                    Double.parseDouble(
                            text
                    );

            if (!Double.isFinite(
                    amount
            )
                    ||
                    amount < 0) {

                return 0.0;
            }

            return amount;

        } catch (Exception e) {

            return 0.0;
        }
    }

    // =====================================================
    // FIRST NON NULL
    // =====================================================

    private Object firstNonNull(
            Object... values
    ) {

        if (values == null) {

            return null;
        }

        for (Object value :
                values) {

            if (value == null) {

                continue;
            }

            if (value instanceof String
                    &&
                    ((String) value)
                            .trim()
                            .isEmpty()) {

                continue;
            }

            return value;
        }

        return null;
    }

    // =====================================================
    // MAP VALUE
    // =====================================================

    private String value(
            Map<String, Object> map,
            String key
    ) {

        if (map == null
                ||
                key == null) {

            return null;
        }

        return objectValue(
                map.get(
                        key
                )
        );
    }

    // =====================================================
    // OBJECT VALUE
    // =====================================================

    private String objectValue(
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
    // JOIN
    // =====================================================

    private String join(
            String first,
            String second
    ) {

        first =
                clean(
                        first
                );

        second =
                clean(
                        second
                );

        if (first == null) {

            return second;
        }

        if (second == null) {

            return first;
        }

        return first
                + " "
                + second;
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