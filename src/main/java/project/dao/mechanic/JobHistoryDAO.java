package project.dao.mechanic;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;

import project.firebase.FirebaseConfig;
import project.model.ServiceRequest;
import project.ui.user.UserSession;

import java.io.IOException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class JobHistoryDAO {

    // =====================================================
    // SAME SHARED COLLECTION
    // =====================================================

    private static final String COLLECTION =
            "serviceRequests";

    // =====================================================
    // FIRESTORE
    // =====================================================

    private final Firestore firestore;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public JobHistoryDAO() {

        try {

            firestore =
                    FirebaseConfig.getFirestore();

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to initialize Firestore.",
                    e
            );
        }
    }

    // =====================================================
    // FIRESTORE CONSTRUCTOR
    // =====================================================

    public JobHistoryDAO(
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
    // CURRENT MECHANIC ID
    //
    // Prefer email because registered mechanic document ID
    // and serviceRequests.mechanicId normally use email.
    // =====================================================

    public String getCurrentMechanicId() {

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
    // GET ALL HISTORY
    //
    // Completed + Cancelled
    // =====================================================

    public List<ServiceRequest> getJobHistory()
            throws Exception {

        return getJobHistory(
                getRequiredMechanicId()
        );
    }

    public List<ServiceRequest> getJobHistory(
            String mechanicId
    ) throws Exception {

        List<ServiceRequest> result =
                new ArrayList<>();

        mechanicId =
                normalizeId(
                        mechanicId
                );

        if (mechanicId == null) {

            return result;
        }

        List<DocumentSnapshot> documents =
                getMechanicDocuments(
                        mechanicId
                );

        for (DocumentSnapshot document :
                documents) {

            ServiceRequest request =
                    convertDocument(
                            document
                    );

            if (request == null) {

                continue;
            }

            if (
                    hasStatus(
                            request,
                            "Completed"
                    )
                            ||
                    hasStatus(
                            request,
                            "Cancelled"
                    )
            ) {

                result.add(
                        request
                );
            }
        }

        // =================================================
        // LATEST HISTORY FIRST
        // =================================================

        result.sort(
                Comparator.comparingLong(
                        this::getHistoryTimestamp
                ).reversed()
        );

        return result;
    }

    // =====================================================
    // COMPLETED JOBS
    // =====================================================

    public List<ServiceRequest> getCompletedJobs()
            throws Exception {

        return getCompletedJobs(
                getRequiredMechanicId()
        );
    }

    public List<ServiceRequest> getCompletedJobs(
            String mechanicId
    ) throws Exception {

        return getJobsByStatus(
                mechanicId,
                "Completed"
        );
    }

    // =====================================================
    // CANCELLED JOBS
    // =====================================================

    public List<ServiceRequest> getCancelledJobs()
            throws Exception {

        return getCancelledJobs(
                getRequiredMechanicId()
        );
    }

    public List<ServiceRequest> getCancelledJobs(
            String mechanicId
    ) throws Exception {

        return getJobsByStatus(
                mechanicId,
                "Cancelled"
        );
    }

    // =====================================================
    // GET JOBS BY STATUS
    // =====================================================

    public List<ServiceRequest> getJobsByStatus(
            String mechanicId,
            String status
    ) throws Exception {

        List<ServiceRequest> result =
                new ArrayList<>();

        mechanicId =
                normalizeId(
                        mechanicId
                );

        status =
                normalizeStatus(
                        status
                );

        if (mechanicId == null
                ||
                status == null) {

            return result;
        }

        for (DocumentSnapshot document :
                getMechanicDocuments(
                        mechanicId
                )) {

            ServiceRequest request =
                    convertDocument(
                            document
                    );

            if (request == null) {

                continue;
            }

            if (hasStatus(
                    request,
                    status
            )) {

                result.add(
                        request
                );
            }
        }

        result.sort(
                Comparator.comparingLong(
                        this::getHistoryTimestamp
                ).reversed()
        );

        return result;
    }

    // =====================================================
    // GET HISTORY JOB BY ID
    //
    // Current mechanic can only read their own
    // Completed / Cancelled record.
    // =====================================================

    public ServiceRequest getJobById(
            String requestId
    ) throws Exception {

        requestId =
                clean(
                        requestId
                );

        if (requestId == null) {

            return null;
        }

        DocumentSnapshot document =
                firestore
                        .collection(COLLECTION)
                        .document(requestId)
                        .get()
                        .get();

        if (!document.exists()) {

            return null;
        }

        ServiceRequest request =
                convertDocument(
                        document
                );

        if (request == null) {

            return null;
        }

        String mechanicId =
                getRequiredMechanicId();

        // =================================================
        // OWNERSHIP
        // =================================================

        if (!sameId(
                request.getMechanicId(),
                mechanicId
        )) {

            return null;
        }

        // =================================================
        // HISTORY ONLY
        // =================================================

        if (
                !hasStatus(
                        request,
                        "Completed"
                )
                        &&
                !hasStatus(
                        request,
                        "Cancelled"
                )
        ) {

            return null;
        }

        return request;
    }

    // =====================================================
    // SEARCH HISTORY
    // =====================================================

    public List<ServiceRequest> searchHistory(
            String searchText
    ) throws Exception {

        return searchHistory(
                getRequiredMechanicId(),
                searchText
        );
    }

    public List<ServiceRequest> searchHistory(
            String mechanicId,
            String searchText
    ) throws Exception {

        List<ServiceRequest> allJobs =
                getJobHistory(
                        mechanicId
                );

        String search =
                clean(
                        searchText
                );

        if (search == null) {

            return allJobs;
        }

        search =
                search.toLowerCase();

        List<ServiceRequest> result =
                new ArrayList<>();

        for (ServiceRequest job :
                allJobs) {

            if (
                    contains(
                            job.getRequestId(),
                            search
                    )
                            ||
                    contains(
                            job.getCustomerId(),
                            search
                    )
                            ||
                    contains(
                            job.getCustomerName(),
                            search
                    )
                            ||
                    contains(
                            job.getVehicleId(),
                            search
                    )
                            ||
                    contains(
                            job.getVehicleNumber(),
                            search
                    )
                            ||
                    contains(
                            job.getServiceType(),
                            search
                    )
                            ||
                    contains(
                            job.getDescription(),
                            search
                    )
                            ||
                    contains(
                            job.getDiagnosis(),
                            search
                    )
                            ||
                    contains(
                            job.getRepairDetails(),
                            search
                    )
                            ||
                    contains(
                            job.getLocation(),
                            search
                    )
                            ||
                    contains(
                            job.getStatus(),
                            search
                    )
            ) {

                result.add(
                        job
                );
            }
        }

        return result;
    }

    // =====================================================
    // TOTAL HISTORY COUNT
    // =====================================================

    public int getTotalJobCount()
            throws Exception {

        return getJobHistory()
                .size();
    }

    // =====================================================
    // COMPLETED COUNT
    // =====================================================

    public int getCompletedJobCount()
            throws Exception {

        return getCompletedJobs()
                .size();
    }

    // =====================================================
    // CANCELLED COUNT
    // =====================================================

    public int getCancelledJobCount()
            throws Exception {

        return getCancelledJobs()
                .size();
    }

    // =====================================================
    // JOBS COMPLETED THIS MONTH
    // =====================================================

    public int getJobsCompletedThisMonth()
            throws Exception {

        LocalDate today =
                LocalDate.now();

        int count =
                0;

        for (ServiceRequest request :
                getCompletedJobs()) {

            LocalDate completedDate =
                    convertToLocalDate(
                            request.getCompletedDate()
                    );

            /*
             * Older completed records may not have
             * completedDate.
             */
            if (completedDate == null) {

                completedDate =
                        convertToLocalDate(
                                request.getUpdatedAt()
                        );
            }

            if (completedDate == null) {

                completedDate =
                        convertToLocalDate(
                                request.getRequestDate()
                        );
            }

            if (completedDate == null) {

                continue;
            }

            if (
                    completedDate.getYear()
                            == today.getYear()
                            &&
                    completedDate.getMonthValue()
                            == today.getMonthValue()
            ) {

                count++;
            }
        }

        return count;
    }

    // =====================================================
    // TOTAL EARNINGS
    //
    // IMPORTANT:
    //
    // Only Completed jobs contribute to earnings.
    //
    // Cancelled jobs = ₹0.
    // =====================================================

    public double getTotalEarnings()
            throws Exception {

        return getTotalEarnings(
                getRequiredMechanicId()
        );
    }

    public double getTotalEarnings(
            String mechanicId
    ) throws Exception {

        double total =
                0.0;

        for (ServiceRequest job :
                getCompletedJobs(
                        mechanicId
                )) {

            total += resolveJobAmount(
                    job
            );
        }

        return total;
    }

    // =====================================================
    // GET AMOUNT OF ONE HISTORY JOB
    // =====================================================

    public double getJobAmount(
            String requestId
    ) throws Exception {

        ServiceRequest job =
                getJobById(
                        requestId
                );

        if (job == null) {

            return 0.0;
        }

        /*
         * A cancelled request should never inflate
         * mechanic earnings.
         */
        if (hasStatus(
                job,
                "Cancelled"
        )) {

            return 0.0;
        }

        return resolveJobAmount(
                job
        );
    }

    // =====================================================
    // GET FORMATTED JOB DATE
    //
    // Completed -> completedDate
    // Cancelled -> cancelledDate
    // fallback -> updatedAt / requestDate
    // =====================================================

    public String getJobDate(
            String requestId
    ) throws Exception {

        ServiceRequest job =
                getJobById(
                        requestId
                );

        if (job == null) {

            return "";
        }

        Object dateValue;

        if (hasStatus(
                job,
                "Completed"
        )) {

            dateValue =
                    firstNonBlank(
                            job.getCompletedDate(),
                            job.getUpdatedAt(),
                            job.getRequestDate()
                    );

        } else {

            dateValue =
                    firstNonBlank(
                            job.getCancelledDate(),
                            job.getUpdatedAt(),
                            job.getRequestDate()
                    );
        }

        LocalDate localDate =
                convertToLocalDate(
                        dateValue
                );

        if (localDate != null) {

            return localDate.format(
                    DateTimeFormatter
                            .ofPattern(
                                    "dd MMM yyyy"
                            )
            );
        }

        String raw =
                stringValue(
                        dateValue
                );

        return raw == null
                ? ""
                : raw;
    }

    // =====================================================
    // GET CURRENT MECHANIC DOCUMENTS
    //
    // IMPORTANT:
    //
    // Old DAO used:
    //
    // whereEqualTo("mechanicId", mechanicId)
    //
    // That is case-sensitive and misses alias fields.
    //
    // Now we read serviceRequests and normalize IDs.
    // =====================================================

    private List<DocumentSnapshot> getMechanicDocuments(
            String mechanicId
    ) throws Exception {

        List<DocumentSnapshot> result =
                new ArrayList<>();

        mechanicId =
                normalizeId(
                        mechanicId
                );

        if (mechanicId == null) {

            return result;
        }

        QuerySnapshot snapshot =
                firestore
                        .collection(COLLECTION)
                        .get()
                        .get();

        for (DocumentSnapshot document :
                snapshot.getDocuments()) {

            String assignedMechanic =
                    normalizeId(
                            firstString(
                                    document,
                                    "mechanicId",
                                    "assignedMechanicId",
                                    "mechanicEmail",
                                    "responderId"
                            )
                    );

            if (!sameId(
                    assignedMechanic,
                    mechanicId
            )) {

                continue;
            }

            result.add(
                    document
            );
        }

        return result;
    }

    // =====================================================
    // FIRESTORE -> SERVICE REQUEST
    // =====================================================

    private ServiceRequest convertDocument(
            DocumentSnapshot document
    ) {

        if (document == null
                ||
                !document.exists()) {

            return null;
        }

        ServiceRequest request =
                new ServiceRequest();

        // =================================================
        // REQUEST ID
        // =================================================

        request.setRequestId(
                firstNonBlank(
                        firstString(
                                document,
                                "requestId"
                        ),
                        document.getId()
                )
        );

        // =================================================
        // CUSTOMER
        // =================================================

        request.setCustomerId(
                normalizeId(
                        firstString(
                                document,
                                "customerId",
                                "userId",
                                "userEmail"
                        )
                )
        );

        request.setCustomerName(
                firstNonBlank(
                        firstString(
                                document,
                                "customerName",
                                "userName",
                                "name"
                        ),
                        "Customer"
                )
        );

        // =================================================
        // VEHICLE
        // =================================================

        request.setVehicleId(
                firstString(
                        document,
                        "vehicleId"
                )
        );

        request.setVehicleNumber(
                firstString(
                        document,
                        "vehicleNumber",
                        "registrationNumber",
                        "vehicleNo"
                )
        );

        // =================================================
        // MECHANIC
        // =================================================

        request.setMechanicId(
                normalizeId(
                        firstString(
                                document,
                                "mechanicId",
                                "assignedMechanicId",
                                "mechanicEmail",
                                "responderId"
                        )
                )
        );

        request.setMechanicName(
                firstString(
                        document,
                        "mechanicName",
                        "assignedMechanicName",
                        "responderName"
                )
        );

        // =================================================
        // SERVICE DETAILS
        // =================================================

        request.setServiceType(
                firstString(
                        document,
                        "serviceType",
                        "service",
                        "serviceName",
                        "type"
                )
        );

        request.setDescription(
                firstString(
                        document,
                        "description",
                        "issue",
                        "problem"
                )
        );

        request.setLocation(
                firstString(
                        document,
                        "location",
                        "address"
                )
        );

        // =================================================
        // STATUS
        // =================================================

        request.setStatus(
                normalizeStatus(
                        firstString(
                                document,
                                "status"
                        )
                )
        );

        // =================================================
        // REQUEST DATE
        // =================================================

        request.setRequestDate(
                stringValue(
                        firstValue(
                                document,
                                "requestDate",
                                "createdAt",
                                "date"
                        )
                )
        );

        // =================================================
        // LIFECYCLE
        // =================================================

        request.setAssignedDate(
                stringValue(
                        firstValue(
                                document,
                                "assignedDate"
                        )
                )
        );

        request.setAcceptedDate(
                stringValue(
                        firstValue(
                                document,
                                "acceptedDate"
                        )
                )
        );

        request.setStartedDate(
                stringValue(
                        firstValue(
                                document,
                                "startedDate"
                        )
                )
        );

        request.setCompletedDate(
                stringValue(
                        firstValue(
                                document,
                                "completedDate"
                        )
                )
        );

        request.setCancelledDate(
                stringValue(
                        firstValue(
                                document,
                                "cancelledDate"
                        )
                )
        );

        request.setUpdatedAt(
                stringValue(
                        firstValue(
                                document,
                                "updatedAt"
                        )
                )
        );

        // =================================================
        // DIAGNOSIS / REPAIR
        // =================================================

        request.setDiagnosis(
                firstString(
                        document,
                        "diagnosis"
                )
        );

        request.setRepairDetails(
                firstString(
                        document,
                        "repairDetails",
                        "repair",
                        "workDone"
                )
        );

        // =================================================
        // COST
        // =================================================

        request.setEstimatedCost(
                stringValue(
                        firstValue(
                                document,
                                "estimatedCost"
                        )
                )
        );

        request.setPartsCost(
                stringValue(
                        firstValue(
                                document,
                                "partsCost"
                        )
                )
        );

        request.setLabourCost(
                stringValue(
                        firstValue(
                                document,
                                "labourCost",
                                "laborCost"
                        )
                )
        );

        request.setFinalCost(
                stringValue(
                        firstValue(
                                document,
                                "finalCost",
                                "totalCost",
                                "cost",
                                "amount"
                        )
                )
        );

        // =================================================
        // EXTRA
        // =================================================

        request.setSource(
                firstString(
                        document,
                        "source"
                )
        );

        request.setPriority(
                firstString(
                        document,
                        "priority"
                )
        );

        return request;
    }

    // =====================================================
    // RESOLVE JOB AMOUNT
    //
    // Priority:
    //
    // finalCost
    // parts + labour
    // estimatedCost fallback for old completed data
    // =====================================================

    private double resolveJobAmount(
            ServiceRequest job
    ) {

        if (job == null) {

            return 0.0;
        }

        double finalCost =
                parseCost(
                        job.getFinalCost()
                );

        if (finalCost > 0) {

            return finalCost;
        }

        double parts =
                parseCost(
                        job.getPartsCost()
                );

        double labour =
                parseCost(
                        job.getLabourCost()
                );

        double calculated =
                parts + labour;

        if (calculated > 0) {

            return calculated;
        }

        /*
         * Compatibility only for old completed records
         * where finalCost was never saved.
         */
        return parseCost(
                job.getEstimatedCost()
        );
    }

    // =====================================================
    // CURRENT MECHANIC REQUIRED
    // =====================================================

    private String getRequiredMechanicId() {

        if (!UserSession.isLoggedIn()) {

            throw new IllegalStateException(
                    "No mechanic is logged in."
            );
        }

        String role =
                clean(
                        UserSession.getUserRole()
                );

        if (role != null
                &&
                !role.equalsIgnoreCase(
                        "Mechanic"
                )) {

            throw new IllegalStateException(
                    "Current logged-in user is not a mechanic."
            );
        }

        String mechanicId =
                getCurrentMechanicId();

        if (mechanicId == null) {

            throw new IllegalStateException(
                    "Mechanic ID/email is unavailable."
            );
        }

        return mechanicId;
    }

    // =====================================================
    // STATUS CHECK
    // =====================================================

    private boolean hasStatus(
            ServiceRequest request,
            String status
    ) {

        if (request == null
                ||
                status == null) {

            return false;
        }

        String actual =
                normalizeStatus(
                        request.getStatus()
                );

        String required =
                normalizeStatus(
                        status
                );

        return actual != null
                &&
                required != null
                &&
                actual.equalsIgnoreCase(
                        required
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
        ) {

            return "Cancelled";
        }

        return status;
    }

    // =====================================================
    // HISTORY TIMESTAMP
    // =====================================================

    private long getHistoryTimestamp(
            ServiceRequest request
    ) {

        if (request == null) {

            return 0L;
        }

        long time;

        if (hasStatus(
                request,
                "Completed"
        )) {

            time =
                    toMillis(
                            request.getCompletedDate()
                    );

            if (time > 0) {

                return time;
            }
        }

        if (hasStatus(
                request,
                "Cancelled"
        )) {

            time =
                    toMillis(
                            request.getCancelledDate()
                    );

            if (time > 0) {

                return time;
            }
        }

        time =
                toMillis(
                        request.getUpdatedAt()
                );

        if (time > 0) {

            return time;
        }

        return toMillis(
                request.getRequestDate()
        );
    }

    // =====================================================
    // FIRST FIRESTORE VALUE
    // =====================================================

    private Object firstValue(
            DocumentSnapshot document,
            String... fields
    ) {

        if (document == null
                ||
                fields == null) {

            return null;
        }

        for (String field :
                fields) {

            Object value =
                    document.get(
                            field
                    );

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
    // FIRST FIRESTORE STRING
    // =====================================================

    private String firstString(
            DocumentSnapshot document,
            String... fields
    ) {

        return stringValue(
                firstValue(
                        document,
                        fields
                )
        );
    }

    // =====================================================
    // COST PARSER
    // =====================================================

    private double parseCost(
            Object value
    ) {

        if (value == null) {

            return 0.0;
        }

        if (value instanceof Number) {

            double number =
                    ((Number) value)
                            .doubleValue();

            return Math.max(
                    number,
                    0.0
            );
        }

        String text =
                String.valueOf(
                        value
                )
                        .replace("₹", "")
                        .replace(",", "")
                        .trim();

        if (text.isEmpty()) {

            return 0.0;
        }

        try {

            double number =
                    Double.parseDouble(
                            text
                    );

            return Math.max(
                    number,
                    0.0
            );

        } catch (Exception e) {

            return 0.0;
        }
    }

    // =====================================================
    // LOCAL DATE
    // =====================================================

    private LocalDate convertToLocalDate(
            Object value
    ) {

        long millis =
                toMillis(
                        value
                );

        if (millis <= 0) {

            /*
             * Try formatted String date.
             */
            String text =
                    stringValue(
                            value
                    );

            if (text == null) {

                return null;
            }

            String[] patterns = {
                    "dd MMM yyyy",
                    "dd MMMM yyyy",
                    "dd-MM-yyyy",
                    "dd/MM/yyyy",
                    "yyyy-MM-dd",
                    "MMM dd, yyyy"
            };

            for (String pattern :
                    patterns) {

                try {

                    return LocalDate.parse(
                            text,
                            DateTimeFormatter
                                    .ofPattern(
                                            pattern
                                    )
                    );

                } catch (Exception ignored) {
                }
            }

            return null;
        }

        try {

            return Instant
                    .ofEpochMilli(
                            millis
                    )
                    .atZone(
                            ZoneId.systemDefault()
                    )
                    .toLocalDate();

        } catch (Exception e) {

            return null;
        }
    }

    // =====================================================
    // OBJECT -> MILLIS
    // =====================================================

    private long toMillis(
            Object value
    ) {

        if (value == null) {

            return 0L;
        }

        // =================================================
        // FIRESTORE TIMESTAMP
        // =================================================

        if (value instanceof Timestamp) {

            Timestamp timestamp =
                    (Timestamp) value;

            return timestamp
                    .toSqlTimestamp()
                    .getTime();
        }

        // =================================================
        // JAVA DATE
        // =================================================

        if (value instanceof Date) {

            return ((Date) value)
                    .getTime();
        }

        // =================================================
        // NUMBER
        // =================================================

        if (value instanceof Number) {

            long number =
                    ((Number) value)
                            .longValue();

            if (
                    number > 0
                            &&
                    number < 100000000000L
            ) {

                number *=
                        1000L;
            }

            return number;
        }

        // =================================================
        // STRING
        // =================================================

        String text =
                stringValue(
                        value
                );

        if (text == null) {

            return 0L;
        }

        try {

            long number =
                    Long.parseLong(
                            text
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
    // SAME ID
    // =====================================================

    private boolean sameId(
            String first,
            String second
    ) {

        String firstId =
                normalizeId(
                        first
                );

        String secondId =
                normalizeId(
                        second
                );

        return firstId != null
                &&
                secondId != null
                &&
                firstId.equals(
                        secondId
                );
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
    // SEARCH MATCH
    // =====================================================

    private boolean contains(
            String value,
            String search
    ) {

        return value != null
                &&
                search != null
                &&
                value
                        .toLowerCase()
                        .contains(
                                search
                        );
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