package project.controller.mechanic;

import com.google.cloud.firestore.Firestore;

import project.dao.mechanic.JobHistoryDAO;
import project.model.ServiceRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class JobHistoryController {

    // =====================================================
    // DAO
    // =====================================================

    private final JobHistoryDAO jobHistoryDAO;

    // =====================================================
    // DEFAULT CONSTRUCTOR
    // =====================================================

    public JobHistoryController() {

        this.jobHistoryDAO =
                new JobHistoryDAO();
    }

    // =====================================================
    // FIRESTORE CONSTRUCTOR
    // =====================================================

    public JobHistoryController(
            Firestore firestore
    ) {

        this.jobHistoryDAO =
                new JobHistoryDAO(
                        firestore
                );
    }

    // =====================================================
    // CURRENT MECHANIC ID
    // =====================================================

    public String getCurrentMechanicId() {

        return jobHistoryDAO
                .getCurrentMechanicId();
    }

    // =====================================================
    // GET COMPLETE HISTORY
    //
    // Completed + Cancelled
    // =====================================================

    public List<ServiceRequest> getJobHistory() {

        try {

            return jobHistoryDAO
                    .getJobHistory();

        } catch (Exception e) {

            logError(
                    "Unable to load job history",
                    e
            );

            return new ArrayList<>();
        }
    }

    // =====================================================
    // REFRESH HISTORY
    // =====================================================

    public List<ServiceRequest> refreshHistory() {

        return getJobHistory();
    }

    // =====================================================
    // COMPLETED JOBS
    // =====================================================

    public List<ServiceRequest> getCompletedJobs() {

        try {

            return jobHistoryDAO
                    .getCompletedJobs();

        } catch (Exception e) {

            logError(
                    "Unable to load completed jobs",
                    e
            );

            return new ArrayList<>();
        }
    }

    // =====================================================
    // CANCELLED JOBS
    // =====================================================

    public List<ServiceRequest> getCancelledJobs() {

        try {

            return jobHistoryDAO
                    .getCancelledJobs();

        } catch (Exception e) {

            logError(
                    "Unable to load cancelled jobs",
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

            return jobHistoryDAO
                    .getJobById(
                            requestId
                    );

        } catch (Exception e) {

            logError(
                    "Unable to load history job",
                    e
            );

            return null;
        }
    }

    // =====================================================
    // SEARCH
    // =====================================================

    public List<ServiceRequest> searchHistory(
            String searchText
    ) {

        String search =
                clean(
                        searchText
                );

        if (search == null) {

            return getJobHistory();
        }

        try {

            return jobHistoryDAO
                    .searchHistory(
                            search
                    );

        } catch (Exception e) {

            logError(
                    "Unable to search history",
                    e
            );

            return new ArrayList<>();
        }
    }

    // =====================================================
    // FILTER HISTORY
    //
    // All Status
    // Completed
    // Cancelled
    // =====================================================

    public List<ServiceRequest> getFilteredHistory(
            String searchText,
            String status
    ) {

        List<ServiceRequest> source;

        if (clean(
                searchText
        ) == null) {

            source =
                    getJobHistory();

        } else {

            source =
                    searchHistory(
                            searchText
                    );
        }

        String requiredStatus =
                normalizeStatus(
                        status
                );

        if (requiredStatus == null
                ||
                requiredStatus.equalsIgnoreCase(
                        "All"
                )
                ||
                requiredStatus.equalsIgnoreCase(
                        "All Status"
                )) {

            return source;
        }

        List<ServiceRequest> result =
                new ArrayList<>();

        for (ServiceRequest job :
                source) {

            if (job == null) {

                continue;
            }

            if (sameStatus(
                    job.getStatus(),
                    requiredStatus
            )) {

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

    public int getTotalJobCount() {

        try {

            return jobHistoryDAO
                    .getTotalJobCount();

        } catch (Exception e) {

            return 0;
        }
    }

    // =====================================================
    // COMPLETED COUNT
    // =====================================================

    public int getCompletedJobCount() {

        try {

            return jobHistoryDAO
                    .getCompletedJobCount();

        } catch (Exception e) {

            return 0;
        }
    }

    // =====================================================
    // CANCELLED COUNT
    // =====================================================

    public int getCancelledJobCount() {

        try {

            return jobHistoryDAO
                    .getCancelledJobCount();

        } catch (Exception e) {

            return 0;
        }
    }

    // =====================================================
    // THIS MONTH
    //
    // Completed jobs only.
    // =====================================================

    public int getThisMonthJobCount() {

        try {

            return jobHistoryDAO
                    .getJobsCompletedThisMonth();

        } catch (Exception e) {

            logError(
                    "Unable to calculate this month's jobs",
                    e
            );

            return 0;
        }
    }

    // =====================================================
    // TOTAL EARNINGS
    //
    // Completed jobs only.
    //
    // Cancelled jobs contribute ₹0.
    // =====================================================

    public double getTotalEarnings() {

        try {

            return jobHistoryDAO
                    .getTotalEarnings();

        } catch (Exception e) {

            logError(
                    "Unable to calculate total earnings",
                    e
            );

            return 0.0;
        }
    }

    // =====================================================
    // TOTAL EARNINGS DISPLAY
    // =====================================================

    public String getTotalEarningsDisplay() {

        return formatAmount(
                getTotalEarnings()
        );
    }

    // =====================================================
    // CUSTOMER DISPLAY
    // =====================================================

    public String getCustomerDisplay(
            ServiceRequest job
    ) {

        if (job == null) {

            return "Unknown Customer";
        }

        return firstNonBlank(
                job.getCustomerName(),
                job.getCustomerId(),
                "Unknown Customer"
        );
    }

    // =====================================================
    // VEHICLE DISPLAY
    // =====================================================

    public String getVehicleDisplay(
            ServiceRequest job
    ) {

        if (job == null) {

            return "Unknown Vehicle";
        }

        return firstNonBlank(
                job.getVehicleNumber(),
                job.getVehicleId(),
                "Unknown Vehicle"
        );
    }

    // =====================================================
    // SERVICE TYPE
    // =====================================================

    public String getServiceTypeDisplay(
            ServiceRequest job
    ) {

        if (job == null) {

            return "Service Request";
        }

        return firstNonBlank(
                job.getServiceType(),
                "Service Request"
        );
    }

    // =====================================================
    // PROBLEM DISPLAY
    // =====================================================

    public String getProblemDisplay(
            ServiceRequest job
    ) {

        if (job == null) {

            return "Service Request";
        }

        return firstNonBlank(
                job.getDescription(),
                job.getServiceType(),
                "Service Request"
        );
    }

    // =====================================================
    // LOCATION DISPLAY
    // =====================================================

    public String getLocationDisplay(
            ServiceRequest job
    ) {

        if (job == null) {

            return "Location not available";
        }

        return firstNonBlank(
                job.getLocation(),
                "Location not available"
        );
    }

    // =====================================================
    // STATUS DISPLAY
    // =====================================================

    public String getStatusDisplay(
            ServiceRequest job
    ) {

        if (job == null) {

            return "-";
        }

        return firstNonBlank(
                normalizeStatus(
                        job.getStatus()
                ),
                "-"
        );
    }

    // =====================================================
    // REQUEST ID DISPLAY
    // =====================================================

    public String getRequestIdDisplay(
            ServiceRequest job
    ) {

        if (job == null) {

            return "-";
        }

        return firstNonBlank(
                job.getRequestId(),
                "-"
        );
    }

    // =====================================================
    // DATE DISPLAY
    //
    // DAO chooses:
    //
    // Completed -> completedDate
    // Cancelled -> cancelledDate
    //
    // fallback:
    // updatedAt / requestDate
    // =====================================================

    public String getDateDisplay(
            ServiceRequest job
    ) {

        if (job == null) {

            return "-";
        }

        String requestId =
                clean(
                        job.getRequestId()
                );

        if (requestId == null) {

            return fallbackDate(
                    job
            );
        }

        try {

            String date =
                    jobHistoryDAO
                            .getJobDate(
                                    requestId
                            );

            if (clean(
                    date
            ) != null) {

                return date;
            }

        } catch (Exception ignored) {
        }

        return fallbackDate(
                job
        );
    }

    // =====================================================
    // DIAGNOSIS
    // =====================================================

    public String getDiagnosisDisplay(
            ServiceRequest job
    ) {

        if (job == null) {

            return "-";
        }

        return firstNonBlank(
                job.getDiagnosis(),
                "-"
        );
    }

    // =====================================================
    // REPAIR DETAILS
    // =====================================================

    public String getRepairDetailsDisplay(
            ServiceRequest job
    ) {

        if (job == null) {

            return "-";
        }

        return firstNonBlank(
                job.getRepairDetails(),
                "-"
        );
    }

    // =====================================================
    // ESTIMATED COST
    // =====================================================

    public String getEstimatedCostDisplay(
            ServiceRequest job
    ) {

        if (job == null) {

            return "₹0";
        }

        return formatAmount(
                parseAmount(
                        job.getEstimatedCost()
                )
        );
    }

    // =====================================================
    // PARTS COST
    // =====================================================

    public String getPartsCostDisplay(
            ServiceRequest job
    ) {

        if (job == null) {

            return "₹0";
        }

        return formatAmount(
                parseAmount(
                        job.getPartsCost()
                )
        );
    }

    // =====================================================
    // LABOUR COST
    // =====================================================

    public String getLabourCostDisplay(
            ServiceRequest job
    ) {

        if (job == null) {

            return "₹0";
        }

        return formatAmount(
                parseAmount(
                        job.getLabourCost()
                )
        );
    }

    // =====================================================
    // JOB AMOUNT
    //
    // Completed -> final earnings
    // Cancelled -> 0
    // =====================================================

    public double getJobAmount(
            ServiceRequest job
    ) {

        if (job == null) {

            return 0.0;
        }

        if (isCancelled(
                job
        )) {

            return 0.0;
        }

        String requestId =
                clean(
                        job.getRequestId()
                );

        if (requestId != null) {

            try {

                return jobHistoryDAO
                        .getJobAmount(
                                requestId
                        );

            } catch (Exception ignored) {
            }
        }

        // =================================================
        // FALLBACK
        // =================================================

        double finalCost =
                parseAmount(
                        job.getFinalCost()
                );

        if (finalCost > 0) {

            return finalCost;
        }

        double calculated =
                parseAmount(
                        job.getPartsCost()
                )
                        +
                parseAmount(
                        job.getLabourCost()
                );

        if (calculated > 0) {

            return calculated;
        }

        return parseAmount(
                job.getEstimatedCost()
        );
    }

    // =====================================================
    // JOB AMOUNT DISPLAY
    // =====================================================

    public String getAmountDisplay(
            ServiceRequest job
    ) {

        return formatAmount(
                getJobAmount(
                        job
                )
        );
    }

    // =====================================================
    // FINAL COST DISPLAY
    // =====================================================

    public String getFinalCostDisplay(
            ServiceRequest job
    ) {

        return getAmountDisplay(
                job
        );
    }

    // =====================================================
    // COMPLETED?
    // =====================================================

    public boolean isCompleted(
            ServiceRequest job
    ) {

        return sameStatus(
                job == null
                        ? null
                        : job.getStatus(),
                "Completed"
        );
    }

    // =====================================================
    // CANCELLED?
    // =====================================================

    public boolean isCancelled(
            ServiceRequest job
    ) {

        return sameStatus(
                job == null
                        ? null
                        : job.getStatus(),
                "Cancelled"
        );
    }

    // =====================================================
    // HISTORY ITEM?
    // =====================================================

    public boolean isHistoryItem(
            ServiceRequest job
    ) {

        return isCompleted(
                job
        )
                ||
                isCancelled(
                        job
                );
    }

    // =====================================================
    // AVERAGE RATING
    //
    // serviceRequests currently does not contain a proper
    // customer rating field.
    //
    // त्यामुळे fake 4.8 दाखवणार नाही.
    // =====================================================

    public String getAverageRatingDisplay() {

        return "N/A";
    }

    // =====================================================
    // FALLBACK DATE
    // =====================================================

    private String fallbackDate(
            ServiceRequest job
    ) {

        if (job == null) {

            return "-";
        }

        if (isCompleted(
                job
        )) {

            return firstNonBlank(
                    job.getCompletedDate(),
                    job.getUpdatedAt(),
                    job.getRequestDate(),
                    "-"
            );
        }

        if (isCancelled(
                job
        )) {

            return firstNonBlank(
                    job.getCancelledDate(),
                    job.getUpdatedAt(),
                    job.getRequestDate(),
                    "-"
            );
        }

        return firstNonBlank(
                job.getUpdatedAt(),
                job.getRequestDate(),
                "-"
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
                "Completed"
        )
                ||
                status.equalsIgnoreCase(
                        "Complete"
                )
                ||
                status.equalsIgnoreCase(
                        "Done"
                )) {

            return "Completed";
        }

        if (status.equalsIgnoreCase(
                "Cancelled"
        )
                ||
                status.equalsIgnoreCase(
                        "Canceled"
                )) {

            return "Cancelled";
        }

        if (status.equalsIgnoreCase(
                "In Progress"
        )
                ||
                status.equalsIgnoreCase(
                        "InProgress"
                )
                ||
                status.equalsIgnoreCase(
                        "Active"
                )) {

            return "In Progress";
        }

        if (status.equalsIgnoreCase(
                "Accepted"
        )) {

            return "Accepted";
        }

        if (status.equalsIgnoreCase(
                "Assigned"
        )) {

            return "Assigned";
        }

        if (status.equalsIgnoreCase(
                "Pending"
        )) {

            return "Pending";
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

        String firstStatus =
                normalizeStatus(
                        first
                );

        String secondStatus =
                normalizeStatus(
                        second
                );

        return firstStatus != null
                &&
                secondStatus != null
                &&
                firstStatus.equalsIgnoreCase(
                        secondStatus
                );
    }

    // =====================================================
    // FORMAT AMOUNT
    // =====================================================

    private String formatAmount(
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
    // PARSE AMOUNT
    // =====================================================

    private double parseAmount(
            String value
    ) {

        String cleaned =
                clean(
                        value
                );

        if (cleaned == null) {

            return 0.0;
        }

        cleaned =
                cleaned
                        .replace(
                                "₹",
                                ""
                        )
                        .replace(
                                ",",
                                ""
                        )
                        .trim();

        try {

            double number =
                    Double.parseDouble(
                            cleaned
                    );

            if (!Double.isFinite(
                    number
            )
                    ||
                    number < 0) {

                return 0.0;
            }

            return number;

        } catch (Exception e) {

            return 0.0;
        }
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