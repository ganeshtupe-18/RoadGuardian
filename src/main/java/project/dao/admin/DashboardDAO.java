package project.dao.admin;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;

import project.firebase.FirebaseConfig;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardDAO {

    // =====================================================
    // COLLECTIONS
    // =====================================================

    private static final String USERS =
            "users";

    private static final String CUSTOMERS =
            "customers";

    private static final String MECHANICS =
            "mechanics";

    private static final String VEHICLES =
            "vehicles";

    private static final String SERVICE_REQUESTS =
            "serviceRequests";

    private static final String SOS_REQUESTS =
            "SOSRequests";

    // Old SOS compatibility
    private static final String SOS_REQUESTS_ALIAS_1 =
            "sosRequests";

    private static final String SOS_REQUESTS_ALIAS_2 =
            "sos_requests";

    // =====================================================
    // FIRESTORE
    // =====================================================

    private final Firestore firestore;

    // =====================================================
    // EXISTING CONSTRUCTOR
    //
    // DashboardController already uses this constructor.
    // =====================================================

    public DashboardDAO(
            Firestore firestore
    ) {

        if (firestore == null) {

            throw new IllegalArgumentException(
                    "Firestore cannot be null."
            );
        }

        this.firestore =
                firestore;
    }

    // =====================================================
    // OPTIONAL DEFAULT CONSTRUCTOR
    // =====================================================

    public DashboardDAO() {

        try {

            this.firestore =
                    FirebaseConfig.getFirestore();

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to initialize Firestore.",
                    e
            );
        }
    }

    // =====================================================
    // DASHBOARD STATISTICS
    // =====================================================

    public Map<String, Integer> getStatistics()
            throws Exception {

        Map<String, Integer> statistics =
                new HashMap<>();

        // =================================================
        // CUSTOMERS
        //
        // Registration:
        // users/{email}
        // role = User
        //
        // Admin:
        // customers/{email}
        //
        // Merge both and remove duplicates.
        // =================================================

        Map<String, String> customerStatuses =
                loadCustomers();

        int customers =
                customerStatuses.size();

        int activeCustomers =
                0;

        int inactiveCustomers =
                0;

        for (String status :
                customerStatuses.values()) {

            if (isInactive(
                    status
            )) {

                inactiveCustomers++;

            } else {

                activeCustomers++;
            }
        }

        // =================================================
        // MECHANICS
        // =================================================

        Map<String, String> mechanicStatuses =
                loadMechanics();

        int mechanics =
                mechanicStatuses.size();

        int activeMechanics =
                0;

        int inactiveMechanics =
                0;

        for (String status :
                mechanicStatuses.values()) {

            if (isInactive(
                    status
            )) {

                inactiveMechanics++;

            } else {

                activeMechanics++;
            }
        }

        // =================================================
        // VEHICLES
        //
        // Canonical collection only:
        // vehicles/{vehicleId}
        // =================================================

        int vehicles =
                getCollectionCount(
                        VEHICLES
                );

        // =================================================
        // SERVICE REQUESTS
        // =================================================

        ServiceRequestCounts serviceCounts =
                loadServiceRequestCounts();

        // =================================================
        // SOS
        //
        // Canonical:
        // SOSRequests
        //
        // Old aliases are temporarily merged until
        // SOS consolidation is completed.
        // =================================================

        SOSCounts sosCounts =
                loadSOSCounts();

        // =================================================
        // CUSTOMER DATA
        // =================================================

        statistics.put(
                "customers",
                customers
        );

        statistics.put(
                "activeCustomers",
                activeCustomers
        );

        statistics.put(
                "inactiveCustomers",
                inactiveCustomers
        );

        // =================================================
        // MECHANIC DATA
        // =================================================

        statistics.put(
                "mechanics",
                mechanics
        );

        statistics.put(
                "activeMechanics",
                activeMechanics
        );

        statistics.put(
                "inactiveMechanics",
                inactiveMechanics
        );

        // =================================================
        // VEHICLES
        // =================================================

        statistics.put(
                "vehicles",
                vehicles
        );

        // =================================================
        // SERVICE REQUEST DATA
        //
        // Existing AdminDashboard keys are preserved.
        // =================================================

        statistics.put(
                "serviceRequests",
                serviceCounts.total
        );

        statistics.put(
                "pendingRequests",
                serviceCounts.pending
        );

        statistics.put(
                "assignedRequests",
                serviceCounts.assigned
        );

        statistics.put(
                "acceptedRequests",
                serviceCounts.accepted
        );

        statistics.put(
                "inProgressRequests",
                serviceCounts.inProgress
        );

        statistics.put(
                "completedRequests",
                serviceCounts.completed
        );

        statistics.put(
                "cancelledRequests",
                serviceCounts.cancelled
        );

        statistics.put(
                "activeRequests",
                serviceCounts.pending
                        +
                serviceCounts.assigned
                        +
                serviceCounts.accepted
                        +
                serviceCounts.inProgress
        );

        // =================================================
        // SOS DATA
        // =================================================

        statistics.put(
                "sosRequests",
                sosCounts.total
        );

        statistics.put(
                "pendingSOS",
                sosCounts.pending
        );

        statistics.put(
                "assignedSOS",
                sosCounts.assigned
        );

        statistics.put(
                "acceptedSOS",
                sosCounts.accepted
        );

        statistics.put(
                "inProgressSOS",
                sosCounts.inProgress
        );

        statistics.put(
                "resolvedSOS",
                sosCounts.resolved
        );

        statistics.put(
                "cancelledSOS",
                sosCounts.cancelled
        );

        statistics.put(
                "activeSOS",
                sosCounts.pending
                        +
                sosCounts.assigned
                        +
                sosCounts.accepted
                        +
                sosCounts.inProgress
        );

        return statistics;
    }

    // =====================================================
    // LOAD CUSTOMERS
    //
    // Important:
    //
    // users = actual registered customers
    // customers = Admin-side customer record
    //
    // Same email is counted ONCE.
    // customers collection status gets priority.
    // =====================================================

    private Map<String, String> loadCustomers()
            throws Exception {

        Map<String, String> customers =
                new LinkedHashMap<>();

        // =================================================
        // REGISTERED USERS
        // =================================================

        QuerySnapshot usersSnapshot =
                firestore
                        .collection(USERS)
                        .get()
                        .get();

        for (DocumentSnapshot document :
                usersSnapshot.getDocuments()) {

            String role =
                    stringValue(
                            document.get(
                                    "role"
                            )
                    );

            /*
             * RegistrationController currently stores:
             *
             * role = User
             *
             * Customer also supported for compatibility.
             */
            if (!isCustomerRole(
                    role
            )) {

                continue;
            }

            String customerId =
                    getIdentity(
                            document,
                            "email",
                            "customerId",
                            "userId"
                    );

            if (customerId == null) {

                continue;
            }

            String status =
                    normalizeAccountStatus(
                            stringValue(
                                    document.get(
                                            "status"
                                    )
                            )
                    );

            customers.put(
                    customerId,
                    status
            );
        }

        // =================================================
        // ADMIN CUSTOMER RECORDS
        //
        // Put after users so Admin status overrides
        // registered user's missing/default status.
        // =================================================

        QuerySnapshot customerSnapshot =
                firestore
                        .collection(CUSTOMERS)
                        .get()
                        .get();

        for (DocumentSnapshot document :
                customerSnapshot.getDocuments()) {

            String customerId =
                    getIdentity(
                            document,
                            "email",
                            "customerId",
                            "userId"
                    );

            if (customerId == null) {

                continue;
            }

            String status =
                    normalizeAccountStatus(
                            stringValue(
                                    document.get(
                                            "status"
                                    )
                            )
                    );

            customers.put(
                    customerId,
                    status
            );
        }

        return customers;
    }

    // =====================================================
    // LOAD MECHANICS
    // =====================================================

    private Map<String, String> loadMechanics()
            throws Exception {

        Map<String, String> mechanics =
                new LinkedHashMap<>();

        QuerySnapshot snapshot =
                firestore
                        .collection(MECHANICS)
                        .get()
                        .get();

        for (DocumentSnapshot document :
                snapshot.getDocuments()) {

            String mechanicId =
                    getIdentity(
                            document,
                            "email",
                            "mechanicId",
                            "userId"
                    );

            if (mechanicId == null) {

                mechanicId =
                        normalizeId(
                                document.getId()
                        );
            }

            if (mechanicId == null) {

                continue;
            }

            String status =
                    normalizeAccountStatus(
                            stringValue(
                                    document.get(
                                            "status"
                                    )
                            )
                    );

            mechanics.put(
                    mechanicId,
                    status
            );
        }

        return mechanics;
    }

    // =====================================================
    // SERVICE REQUEST COUNTS
    //
    // Reads once, then counts all statuses in Java.
    //
    // This avoids multiple Firestore queries and also
    // handles old status spellings.
    // =====================================================

    private ServiceRequestCounts loadServiceRequestCounts()
            throws Exception {

        ServiceRequestCounts counts =
                new ServiceRequestCounts();

        QuerySnapshot snapshot =
                firestore
                        .collection(
                                SERVICE_REQUESTS
                        )
                        .get()
                        .get();

        for (DocumentSnapshot document :
                snapshot.getDocuments()) {

            counts.total++;

            String status =
                    normalizeServiceStatus(
                            stringValue(
                                    document.get(
                                            "status"
                                    )
                            )
                    );

            switch (status) {

                case "Pending" ->
                        counts.pending++;

                case "Assigned" ->
                        counts.assigned++;

                case "Accepted" ->
                        counts.accepted++;

                case "In Progress" ->
                        counts.inProgress++;

                case "Completed" ->
                        counts.completed++;

                case "Cancelled" ->
                        counts.cancelled++;

                default -> {

                    /*
                     * Unknown old status:
                     * keep it in total but don't put it
                     * into a wrong lifecycle count.
                     */
                }
            }
        }

        return counts;
    }

    // =====================================================
    // LOAD SOS COUNTS
    //
    // Temporary compatibility:
    //
    // SOSRequests
    // sosRequests
    // sos_requests
    //
    // Same request ID is counted only once.
    // =====================================================

    private SOSCounts loadSOSCounts()
            throws Exception {

        Map<String, String> requests =
                new LinkedHashMap<>();

        // =================================================
        // OLD ALIASES FIRST
        // =================================================

        mergeSOSCollection(
                requests,
                SOS_REQUESTS_ALIAS_2
        );

        mergeSOSCollection(
                requests,
                SOS_REQUESTS_ALIAS_1
        );

        // =================================================
        // CANONICAL LAST = PRIORITY
        // =================================================

        mergeSOSCollection(
                requests,
                SOS_REQUESTS
        );

        SOSCounts counts =
                new SOSCounts();

        counts.total =
                requests.size();

        for (String rawStatus :
                requests.values()) {

            String status =
                    normalizeSOSStatus(
                            rawStatus
                    );

            switch (status) {

                case "Pending" ->
                        counts.pending++;

                case "Assigned" ->
                        counts.assigned++;

                case "Accepted" ->
                        counts.accepted++;

                case "In Progress" ->
                        counts.inProgress++;

                case "Resolved" ->
                        counts.resolved++;

                case "Cancelled" ->
                        counts.cancelled++;

                default -> {
                }
            }
        }

        return counts;
    }

    // =====================================================
    // MERGE SOS COLLECTION
    // =====================================================

    private void mergeSOSCollection(
            Map<String, String> result,
            String collectionName
    ) throws Exception {

        QuerySnapshot snapshot =
                firestore
                        .collection(
                                collectionName
                        )
                        .get()
                        .get();

        for (DocumentSnapshot document :
                snapshot.getDocuments()) {

            String requestId =
                    firstNonBlank(
                            stringValue(
                                    document.get(
                                            "requestId"
                                    )
                            ),
                            stringValue(
                                    document.get(
                                            "sosId"
                                    )
                            ),
                            stringValue(
                                    document.get(
                                            "id"
                                    )
                            ),
                            document.getId()
                    );

            requestId =
                    normalizeId(
                            requestId
                    );

            if (requestId == null) {

                continue;
            }

            String status =
                    stringValue(
                            document.get(
                                    "status"
                            )
                    );

            result.put(
                    requestId,
                    status
            );
        }
    }

    // =====================================================
    // COLLECTION COUNT
    // =====================================================

    private int getCollectionCount(
            String collectionName
    ) throws Exception {

        QuerySnapshot snapshot =
                firestore
                        .collection(
                                collectionName
                        )
                        .get()
                        .get();

        return snapshot.size();
    }

    // =====================================================
    // CUSTOMER ROLE
    // =====================================================

    private boolean isCustomerRole(
            String role
    ) {

        role =
                clean(
                        role
                );

        if (role == null) {

            /*
             * Do not blindly count role-less user documents
             * because an old non-customer document may exist.
             */
            return false;
        }

        return role.equalsIgnoreCase(
                "User"
        )
                ||
                role.equalsIgnoreCase(
                        "Customer"
                );
    }

    // =====================================================
    // ACCOUNT STATUS
    //
    // Old registration does not save status.
    // Missing = Active.
    // =====================================================

    private String normalizeAccountStatus(
            String status
    ) {

        status =
                clean(
                        status
                );

        if (status == null) {

            return "Active";
        }

        if (status.equalsIgnoreCase(
                "Active"
        )) {

            return "Active";
        }

        if (
                status.equalsIgnoreCase(
                        "Inactive"
                )
                        ||
                status.equalsIgnoreCase(
                        "Disabled"
                )
                        ||
                status.equalsIgnoreCase(
                        "Blocked"
                )
        ) {

            return "Inactive";
        }

        return status;
    }

    // =====================================================
    // INACTIVE?
    // =====================================================

    private boolean isInactive(
            String status
    ) {

        String normalized =
                normalizeAccountStatus(
                        status
                );

        return normalized.equalsIgnoreCase(
                "Inactive"
        );
    }

    // =====================================================
    // SERVICE REQUEST STATUS
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
    // SOS STATUS
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
    // GET DOCUMENT IDENTITY
    // =====================================================

    private String getIdentity(
            DocumentSnapshot document,
            String... fields
    ) {

        if (document == null) {

            return null;
        }

        if (fields != null) {

            for (String field :
                    fields) {

                String value =
                        normalizeId(
                                stringValue(
                                        document.get(
                                                field
                                        )
                                )
                        );

                if (value != null) {

                    return value;
                }
            }
        }

        return normalizeId(
                document.getId()
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

        /*
         * Email is used as ID for Customer/Mechanic
         * registration.
         */
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
    // SERVICE REQUEST COUNTER
    // =====================================================

    private static class ServiceRequestCounts {

        int total;

        int pending;

        int assigned;

        int accepted;

        int inProgress;

        int completed;

        int cancelled;
    }

    // =====================================================
    // SOS COUNTER
    // =====================================================

    private static class SOSCounts {

        int total;

        int pending;

        int assigned;

        int accepted;

        int inProgress;

        int resolved;

        int cancelled;
    }
}