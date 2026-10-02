        package project.controller.mechanic;

        import com.google.cloud.firestore.Firestore;

        import project.dao.mechanic.ActiveJobDAO;
        import project.model.ServiceRequest;

        import java.util.ArrayList;
        import java.util.List;

        public class ActiveJobController {

        // =====================================================
        // DAO
        // =====================================================

        private final ActiveJobDAO activeJobDAO;

        // =====================================================
        // DEFAULT CONSTRUCTOR
        // =====================================================

        public ActiveJobController() {

                this.activeJobDAO =
                        new ActiveJobDAO();
        }

        // =====================================================
        // FIRESTORE CONSTRUCTOR
        // =====================================================

        public ActiveJobController(
                Firestore firestore
        ) {

                this.activeJobDAO =
                        new ActiveJobDAO(
                                firestore
                        );
        }

        // =====================================================
        // CURRENT MECHANIC
        // =====================================================

        public String getCurrentMechanicId() {

                return activeJobDAO
                        .getCurrentMechanicId();
        }

        // =====================================================
        // GET CURRENT ACTIVE JOB
        //
        // Priority:
        // 1. In Progress
        // 2. Accepted
        // =====================================================

        public ServiceRequest getCurrentActiveJob() {

                try {

                return activeJobDAO
                        .getCurrentActiveJob();

                } catch (Exception e) {

                logError(
                        "Unable to load current active job",
                        e
                );

                return null;
                }
        }

        // =====================================================
        // REFRESH CURRENT JOB
        // =====================================================

        public ServiceRequest refreshCurrentActiveJob() {

                return getCurrentActiveJob();
        }

        // =====================================================
        // GET ALL ACTIVE JOBS
        //
        // Accepted + In Progress
        // =====================================================

        public List<ServiceRequest> getActiveJobs() {

                try {

                return activeJobDAO
                        .getActiveJobs();

                } catch (Exception e) {

                logError(
                        "Unable to load active jobs",
                        e
                );

                return new ArrayList<>();
                }
        }

        // =====================================================
        // GET JOB BY ID
        // =====================================================

        public ServiceRequest getJobById(
                String requestId
        ) {

                requestId =
                        clean(
                                requestId
                        );

                if (requestId == null) {

                return null;
                }

                try {

                ServiceRequest request =
                        activeJobDAO
                                .getJobById(
                                        requestId
                                );

                if (request == null) {

                        return null;
                }

                // =================================================
                // OWNERSHIP CHECK
                // =================================================

                String currentMechanicId =
                        normalizeId(
                                getCurrentMechanicId()
                        );

                String requestMechanicId =
                        normalizeId(
                                request.getMechanicId()
                        );

                if (currentMechanicId == null
                        ||
                        requestMechanicId == null
                        ||
                        !currentMechanicId.equals(
                                requestMechanicId
                        )) {

                        return null;
                }

                return request;

                } catch (Exception e) {

                logError(
                        "Unable to load active job",
                        e
                );

                return null;
                }
        }

        // =====================================================
        // START REPAIR
        //
        // Accepted -> In Progress
        // =====================================================

        public boolean startRepair(
                String requestId
        ) {

                requestId =
                        clean(
                                requestId
                        );

                if (requestId == null) {

                return false;
                }

                try {

                ServiceRequest request =
                        getJobById(
                                requestId
                        );

                if (request == null) {

                        return false;
                }

                // =================================================
                // ALREADY STARTED
                // =================================================

                if (isRepairInProgress(
                        request
                )) {

                        return true;
                }

                // =================================================
                // MUST BE ACCEPTED
                // =================================================

                if (!canStartRepair(
                        request
                )) {

                        return false;
                }

                boolean success =
                        activeJobDAO
                                .startJob(
                                        requestId
                                );

                if (success) {

                        System.out.println(
                                "Repair started: "
                                        + requestId
                                        + " -> In Progress"
                        );
                }

                return success;

                } catch (Exception e) {

                logError(
                        "Unable to start repair",
                        e
                );

                return false;
                }
        }

        // =====================================================
        // SAVE DIAGNOSIS
        // =====================================================

        public boolean saveDiagnosis(
                String requestId,
                String diagnosis
        ) {

                requestId =
                        clean(
                                requestId
                        );

                if (requestId == null) {

                return false;
                }

                try {

                return activeJobDAO
                        .saveDiagnosis(
                                requestId,
                                diagnosis
                        );

                } catch (Exception e) {

                logError(
                        "Unable to save diagnosis",
                        e
                );

                return false;
                }
        }

        // =====================================================
        // SAVE REPAIR DETAILS
        // =====================================================

        public boolean saveRepairDetails(
                String requestId,
                String repairDetails
        ) {

                requestId =
                        clean(
                                requestId
                        );

                if (requestId == null) {

                return false;
                }

                try {

                return activeJobDAO
                        .saveRepairDetails(
                                requestId,
                                repairDetails
                        );

                } catch (Exception e) {

                logError(
                        "Unable to save repair details",
                        e
                );

                return false;
                }
        }

        // =====================================================
        // UPDATE ESTIMATED COST
        // =====================================================

        public boolean updateEstimatedCost(
                String requestId,
                String estimatedCost
        ) {

                requestId =
                        clean(
                                requestId
                        );

                if (requestId == null
                        ||
                        !isValidAmount(
                                estimatedCost
                        )) {

                return false;
                }

                try {

                return activeJobDAO
                        .updateEstimatedCost(
                                requestId,
                                estimatedCost
                        );

                } catch (Exception e) {

                logError(
                        "Unable to update estimated cost",
                        e
                );

                return false;
                }
        }

        // =====================================================
        // SAVE COST DETAILS
        // =====================================================

        public boolean saveCostDetails(
                String requestId,
                String estimatedCost,
                String partsCost,
                String labourCost,
                String finalCost
        ) {

                requestId =
                        clean(
                                requestId
                        );

                if (requestId == null) {

                return false;
                }

                if (!isValidAmount(
                        estimatedCost
                )) {

                return false;
                }

                if (!isValidAmount(
                        partsCost
                )) {

                return false;
                }

                if (!isValidAmount(
                        labourCost
                )) {

                return false;
                }

                if (!isValidAmount(
                        finalCost
                )) {

                return false;
                }

                try {

                return activeJobDAO
                        .saveCostDetails(
                                requestId,
                                estimatedCost,
                                partsCost,
                                labourCost,
                                finalCost
                        );

                } catch (Exception e) {

                logError(
                        "Unable to save cost details",
                        e
                );

                return false;
                }
        }

        // =====================================================
        // SAVE ALL ACTIVE JOB DETAILS
        // =====================================================

        public boolean saveJobDetails(
                String requestId,
                String diagnosis,
                String repairDetails,
                String estimatedCost,
                String partsCost,
                String labourCost,
                String finalCost
        ) {

                requestId =
                        clean(
                                requestId
                        );

                if (requestId == null) {

                return false;
                }

                if (clean(
                        diagnosis
                ) == null) {

                return false;
                }

                if (clean(
                        repairDetails
                ) == null) {

                return false;
                }

                if (!isValidAmount(
                        estimatedCost
                )) {

                return false;
                }

                if (!isValidAmount(
                        partsCost
                )) {

                return false;
                }

                if (!isValidAmount(
                        labourCost
                )) {

                return false;
                }

                if (!isValidAmount(
                        finalCost
                )) {

                return false;
                }

                try {

                boolean success =
                        activeJobDAO
                                .saveJobDetails(
                                        requestId,
                                        diagnosis,
                                        repairDetails,
                                        estimatedCost,
                                        partsCost,
                                        labourCost,
                                        finalCost
                                );

                if (success) {

                        System.out.println(
                                "Active job details saved: "
                                        + requestId
                        );
                }

                return success;

                } catch (Exception e) {

                logError(
                        "Unable to save active job details",
                        e
                );

                return false;
                }
        }

        // =====================================================
        // COMPLETE JOB
        //
        // Only:
        //
        // In Progress -> Completed
        // =====================================================

        public boolean completeJob(
                String requestId
        ) {

                requestId =
                        clean(
                                requestId
                        );

                if (requestId == null) {

                return false;
                }

                try {

                ServiceRequest request =
                        getJobById(
                                requestId
                        );

                if (request == null) {

                        return false;
                }

                // =================================================
                // ALREADY COMPLETED
                // =================================================

                if (isCompleted(
                        request
                )) {

                        return true;
                }

                // =================================================
                // MUST BE IN PROGRESS
                // =================================================

                if (!isRepairInProgress(
                        request
                )) {

                        return false;
                }

                boolean success =
                        activeJobDAO
                                .completeJob(
                                        requestId
                                );

                if (success) {

                        System.out.println(
                                "Job completed: "
                                        + requestId
                                        + " -> Completed"
                        );
                }

                return success;

                } catch (Exception e) {

                logError(
                        "Unable to complete job",
                        e
                );

                return false;
                }
        }

        // =====================================================
        // COMPLETE JOB WITH DETAILS
        //
        // Used by ActiveJobPage.
        //
        // finalCost =
        // partsCost + labourCost
        // =====================================================

        public boolean completeJob(
                String requestId,
                String diagnosis,
                String repairDetails,
                String estimatedCost,
                String partsCost,
                String labourCost
        ) {

                String validation =
                        validateCompletion(
                                getJobById(
                                        requestId
                                ),
                                diagnosis,
                                repairDetails,
                                partsCost,
                                labourCost
                        );

                if (validation != null) {

                System.err.println(
                        "Complete job validation failed: "
                                + validation
                );

                return false;
                }

                if (!isValidAmount(
                        estimatedCost
                )) {

                return false;
                }

                String finalCost =
                        calculateTotalAsString(
                                partsCost,
                                labourCost
                        );

                try {

                boolean success =
                        activeJobDAO
                                .completeJob(
                                        requestId,
                                        diagnosis,
                                        repairDetails,
                                        estimatedCost,
                                        partsCost,
                                        labourCost,
                                        finalCost
                                );

                if (success) {

                        System.out.println(
                                "Job completed with details: "
                                        + requestId
                                        + " | finalCost="
                                        + finalCost
                        );
                }

                return success;

                } catch (Exception e) {

                logError(
                        "Unable to complete active job",
                        e
                );

                return false;
                }
        }

        // =====================================================
        // START NAVIGATION
        // =====================================================

        public boolean startNavigation(
                String requestId
        ) {

                requestId =
                        clean(
                                requestId
                        );

                if (requestId == null) {

                return false;
                }

                try {

                ServiceRequest request =
                        getJobById(
                                requestId
                        );

                if (request == null) {

                        return false;
                }

                if (
                        !canStartRepair(
                                request
                        )
                                &&
                        !isRepairInProgress(
                                request
                        )
                ) {

                        return false;
                }

                boolean success =
                        activeJobDAO
                                .startNavigation(
                                        requestId
                                );

                if (success) {

                        System.out.println(
                                "Navigation started: "
                                        + requestId
                        );
                }

                return success;

                } catch (Exception e) {

                logError(
                        "Unable to start navigation",
                        e
                );

                return false;
                }
        }

        // =====================================================
        // MARK ARRIVED
        // =====================================================

        public boolean markArrived(
                String requestId
        ) {

                requestId =
                        clean(
                                requestId
                        );

                if (requestId == null) {

                return false;
                }

                try {

                ServiceRequest request =
                        getJobById(
                                requestId
                        );

                if (request == null) {

                        return false;
                }

                if (
                        !canStartRepair(
                                request
                        )
                                &&
                        !isRepairInProgress(
                                request
                        )
                ) {

                        return false;
                }

                boolean success =
                        activeJobDAO
                                .markArrived(
                                        requestId
                                );

                if (success) {

                        System.out.println(
                                "Mechanic arrived: "
                                        + requestId
                        );
                }

                return success;

                } catch (Exception e) {

                logError(
                        "Unable to mark mechanic arrived",
                        e
                );

                return false;
                }
        }

        // =====================================================
        // ACTIVE JOB COUNT
        // =====================================================

        public int getActiveJobCount() {

                try {

                return activeJobDAO
                        .getActiveJobCount();

                } catch (Exception e) {

                return 0;
                }
        }

        // =====================================================
        // HAS ACTIVE JOB?
        // =====================================================

        public boolean hasActiveJob() {

                return getCurrentActiveJob()
                        != null;
        }

        // =====================================================
        // CUSTOMER NAME
        // =====================================================

        public String getCustomerName(
                ServiceRequest request
        ) {

                if (request == null) {

                return "No Customer";
                }

                return firstNonBlank(
                        request.getCustomerName(),
                        request.getCustomerId(),
                        "Unknown Customer"
                );
        }

        // =====================================================
        // VEHICLE DISPLAY
        // =====================================================

        public String getVehicleDisplay(
                ServiceRequest request
        ) {

                if (request == null) {

                return "No Vehicle";
                }

                return firstNonBlank(
                        request.getVehicleNumber(),
                        request.getVehicleId(),
                        "Unknown Vehicle"
                );
        }

        // =====================================================
        // SERVICE TYPE
        // =====================================================

        public String getServiceTypeDisplay(
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

        // =====================================================
        // PROBLEM DISPLAY
        // =====================================================

        public String getProblemDisplay(
                ServiceRequest request
        ) {

                if (request == null) {

                return "Service Request";
                }

                return firstNonBlank(
                        request.getDescription(),
                        request.getServiceType(),
                        "Service Request"
                );
        }

        // =====================================================
        // LOCATION DISPLAY
        // =====================================================

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

        // =====================================================
        // STATUS DISPLAY
        // =====================================================

        public String getStatusDisplay(
                ServiceRequest request
        ) {

                if (request == null) {

                return "No Active Job";
                }

                return firstNonBlank(
                        normalizeStatus(
                                request.getStatus()
                        ),
                        "Accepted"
                );
        }

        // =====================================================
        // ESTIMATED COST DISPLAY
        // =====================================================

        public String getEstimatedCostDisplay(
                ServiceRequest request
        ) {

                if (request == null) {

                return "₹0";
                }

                return formatMoney(
                        request.getEstimatedCost()
                );
        }

        // =====================================================
        // PARTS COST DISPLAY
        //
        // Existing ActiveJobPage expects a numeric string.
        // =====================================================

        public String getPartsCostDisplay(
                ServiceRequest request
        ) {

                if (request == null) {

                return "0";
                }

                String cost =
                        cleanMoney(
                                request.getPartsCost()
                        );

                return cost == null
                        ? "0"
                        : cost;
        }

        // =====================================================
        // LABOUR COST DISPLAY
        // =====================================================

        public String getLabourCostDisplay(
                ServiceRequest request
        ) {

                if (request == null) {

                return "0";
                }

                String cost =
                        cleanMoney(
                                request.getLabourCost()
                        );

                return cost == null
                        ? "0"
                        : cost;
        }

        // =====================================================
        // FINAL COST DISPLAY
        // =====================================================

        public String getFinalCostDisplay(
                ServiceRequest request
        ) {

                if (request == null) {

                return "₹0";
                }

                String finalCost =
                        cleanMoney(
                                request.getFinalCost()
                        );

                if (finalCost != null) {

                return "₹"
                        + finalCost;
                }

                return calculateTotalDisplay(
                        request.getPartsCost(),
                        request.getLabourCost()
                );
        }

        // =====================================================
        // DIAGNOSIS
        // =====================================================

        public String getDiagnosis(
                ServiceRequest request
        ) {

                if (request == null) {

                return "";
                }

                String value =
                        clean(
                                request.getDiagnosis()
                        );

                return value == null
                        ? ""
                        : value;
        }

        // =====================================================
        // REPAIR DETAILS
        // =====================================================

        public String getRepairDetails(
                ServiceRequest request
        ) {

                if (request == null) {

                return "";
                }

                String value =
                        clean(
                                request.getRepairDetails()
                        );

                return value == null
                        ? ""
                        : value;
        }

        // =====================================================
        // CALCULATE TOTAL
        // =====================================================

        public double calculateTotal(
                String partsCost,
                String labourCost
        ) {

                return parseAmount(
                        partsCost
                )
                        +
                        parseAmount(
                                labourCost
                        );
        }

        // =====================================================
        // CALCULATE TOTAL STRING
        // =====================================================

        public String calculateTotalAsString(
                String partsCost,
                String labourCost
        ) {

                double total =
                        calculateTotal(
                                partsCost,
                                labourCost
                        );

                if (total == Math.rint(
                        total
                )) {

                return String.valueOf(
                        (long) total
                );
                }

                return String.format(
                        java.util.Locale.US,
                        "%.2f",
                        total
                );
        }

        // =====================================================
        // FORMATTED TOTAL
        // =====================================================

        public String calculateTotalDisplay(
                String partsCost,
                String labourCost
        ) {

                return "₹"
                        + calculateTotalAsString(
                                partsCost,
                                labourCost
                        );
        }

        // =====================================================
        // CAN START REPAIR?
        //
        // Accepted only.
        // =====================================================

        public boolean canStartRepair(
                ServiceRequest request
        ) {

                return hasStatus(
                        request,
                        "Accepted"
                );
        }

        // =====================================================
        // IN PROGRESS?
        // =====================================================

        public boolean isRepairInProgress(
                ServiceRequest request
        ) {

                return hasStatus(
                        request,
                        "In Progress"
                );
        }

        // =====================================================
        // COMPLETED?
        // =====================================================

        public boolean isCompleted(
                ServiceRequest request
        ) {

                return hasStatus(
                        request,
                        "Completed"
                );
        }

        // =====================================================
        // ACTIVE?
        // =====================================================

        public boolean isActive(
                ServiceRequest request
        ) {

                return canStartRepair(
                        request
                )
                        ||
                        isRepairInProgress(
                                request
                        );
        }

        // =====================================================
        // VALIDATE COMPLETION
        //
        // null = valid
        // String = validation error
        // =====================================================

        public String validateCompletion(
                ServiceRequest request,
                String diagnosis,
                String repairDetails,
                String partsCost,
                String labourCost
        ) {

                if (request == null) {

                return "No active job found.";
                }

                if (!isRepairInProgress(
                        request
                )) {

                return "Please start the repair before completing the job.";
                }

                if (clean(
                        diagnosis
                ) == null) {

                return "Please enter the diagnosis.";
                }

                if (clean(
                        repairDetails
                ) == null) {

                return "Please enter the repair details.";
                }

                if (!isValidAmount(
                        partsCost
                )) {

                return "Please enter a valid parts cost.";
                }

                if (!isValidAmount(
                        labourCost
                )) {

                return "Please enter a valid labour cost.";
                }

                return null;
        }

        // =====================================================
        // AMOUNT VALIDATION
        //
        // Blank = valid -> treated as 0.
        //
        // Negative = invalid.
        // Text = invalid.
        // =====================================================

        public boolean isValidAmount(
                String value
        ) {

                String cleaned =
                        cleanMoney(
                                value
                        );

                if (cleaned == null
                        ||
                        cleaned.isBlank()) {

                return true;
                }

                try {

                double number =
                        Double.parseDouble(
                                cleaned
                        );

                return Double.isFinite(
                        number
                )
                        &&
                        number >= 0;

                } catch (Exception e) {

                return false;
                }
        }

        // =====================================================
        // PARSE AMOUNT
        // =====================================================

        private double parseAmount(
                String value
        ) {

                String cleaned =
                        cleanMoney(
                                value
                        );

                if (cleaned == null
                        ||
                        cleaned.isBlank()) {

                return 0.0;
                }

                try {

                double amount =
                        Double.parseDouble(
                                cleaned
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
        // FORMAT MONEY
        // =====================================================

        private String formatMoney(
                String value
        ) {

                String cleaned =
                        cleanMoney(
                                value
                        );

                if (cleaned == null
                        ||
                        cleaned.isBlank()) {

                return "₹0";
                }

                return "₹"
                        + cleaned;
        }

        // =====================================================
        // CLEAN MONEY
        // =====================================================

        private String cleanMoney(
                String value
        ) {

                if (value == null) {

                return null;
                }

                String cleaned =
                        value
                                .trim()
                                .replace(
                                        "₹",
                                        ""
                                )
                                .replace(
                                        ",",
                                        ""
                                )
                                .trim();

                return cleaned.isEmpty()
                        ? null
                        : cleaned;
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