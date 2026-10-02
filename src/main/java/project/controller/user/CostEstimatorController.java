package project.controller.user;

import project.dao.user.DiagnosisDAO;
import project.model.RoadService;
import project.service.GeminiService;
import project.ui.user.UserSession;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CostEstimatorController {

    // ============================================================
    // AI COST ESTIMATE RESULT
    // ============================================================

    public static class EstimateResult {

        private final double minimum;
        private final double maximum;

        private final double basePrice;

        private final double partsMinimum;
        private final double partsMaximum;

        private final double labourMinimum;
        private final double labourMaximum;

        private final String duration;
        private final String confidence;

        private final String note;
        private final String assumptions;
        private final String recommendation;

        private final boolean aiGenerated;

        // ========================================================
        // BASIC / COMPATIBILITY CONSTRUCTOR
        // ========================================================

        public EstimateResult(
                double minimum,
                double maximum,
                String duration,
                String note) {

            this(
                    minimum,
                    maximum,
                    0,
                    0,
                    0,
                    0,
                    0,
                    duration,
                    "Medium",
                    note,
                    "",
                    "",
                    false
            );
        }

        // ========================================================
        // FULL CONSTRUCTOR
        // ========================================================

        public EstimateResult(
                double minimum,
                double maximum,
                double basePrice,
                double partsMinimum,
                double partsMaximum,
                double labourMinimum,
                double labourMaximum,
                String duration,
                String confidence,
                String note,
                String assumptions,
                String recommendation,
                boolean aiGenerated) {

            this.minimum = minimum;
            this.maximum = maximum;

            this.basePrice = basePrice;

            this.partsMinimum = partsMinimum;
            this.partsMaximum = partsMaximum;

            this.labourMinimum = labourMinimum;
            this.labourMaximum = labourMaximum;

            this.duration = duration;
            this.confidence = confidence;

            this.note = note;
            this.assumptions = assumptions;
            this.recommendation = recommendation;

            this.aiGenerated = aiGenerated;
        }

        // ========================================================
        // BASIC PRICE
        // ========================================================

        public double getMinimum() {
            return minimum;
        }

        public double getMaximum() {
            return maximum;
        }

        public boolean hasPrice() {
            return minimum > 0 && maximum > 0;
        }

        // ========================================================
        // ADMIN BASE PRICE
        // ========================================================

        public double getBasePrice() {
            return basePrice;
        }

        // ========================================================
        // PARTS
        // ========================================================

        public double getPartsMinimum() {
            return partsMinimum;
        }

        public double getPartsMaximum() {
            return partsMaximum;
        }

        // ========================================================
        // LABOUR
        // ========================================================

        public double getLabourMinimum() {
            return labourMinimum;
        }

        public double getLabourMaximum() {
            return labourMaximum;
        }

        // ========================================================
        // OTHER INFORMATION
        // ========================================================

        public String getDuration() {
            return duration;
        }

        public String getConfidence() {
            return confidence;
        }

        public String getNote() {
            return note;
        }

        public String getAssumptions() {
            return assumptions;
        }

        public String getRecommendation() {
            return recommendation;
        }

        // ========================================================
        // AI STATUS
        // ========================================================

        public boolean isAiGenerated() {
            return aiGenerated;
        }
    }

    // ============================================================
    // FIELDS
    // ============================================================

    private final DiagnosisDAO dao;
    private final GeminiService grokAIService;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public CostEstimatorController() throws Exception {

        this.dao = new DiagnosisDAO();
        this.grokAIService = new GeminiService();
    }

    // ============================================================
    // GET CUSTOMER VEHICLES
    // ============================================================

    public List<Map<String, Object>> getVehicles()
            throws Exception {

        return dao.getCustomerVehicles(
                currentCustomerId()
        );
    }

    // ============================================================
    // GET ACTIVE SERVICES
    // ============================================================

    public List<RoadService> getActiveServices()
            throws Exception {

        return dao.getActiveServices();
    }

    // ============================================================
    // NORMAL CALCULATE
    // ============================================================

    /**
     * Normal calculation is intentionally disabled.
     *
     * Cost Estimator must use real Gemini AI.
     *
     * No hardcoded price.
     * No Firebase base-price fallback.
     * No 25% artificial calculation.
     */
    public EstimateResult calculate(
            RoadService service) {

        if (service == null) {

            throw new IllegalArgumentException(
                    "Select a service first."
            );
        }

        throw new IllegalStateException(
                "AI Cost Estimator is enabled. "
                        + "Use calculateWithAI() to generate the estimate."
        );
    }

    // ============================================================
    // AI CALCULATE
    // ============================================================

    /**
     * REAL AI COST ESTIMATION
     *
     * Flow:
     *
     * Customer Vehicle
     *        +
     * Selected Service
     *        +
     * Admin Base Price as reference
     *        +
     * Customer Problem
     *        ↓
     * Gemini AI
     *        ↓
     * AI Cost Estimate
     */
    public EstimateResult calculateWithAI(
            Map<String, Object> vehicle,
            RoadService service,
            String notes) {

        // --------------------------------------------------------
        // VALIDATE SERVICE
        // --------------------------------------------------------

        if (service == null) {

            throw new IllegalArgumentException(
                    "Select a service first."
            );
        }

        // --------------------------------------------------------
        // VALIDATE PROBLEM DESCRIPTION
        // --------------------------------------------------------

        String cleanedNotes =
                cleanOrEmpty(notes);

        if (cleanedNotes.isEmpty()) {

            throw new IllegalArgumentException(
                    "Please describe the vehicle problem."
            );
        }

        try {

            System.out.println(
                    "=================================================="
            );

            System.out.println(
                    "Starting Gemini AI Cost Estimation..."
            );

            System.out.println(
                    "Service: "
                            + safeServiceName(service)
            );

            System.out.println(
                    "Customer Problem: "
                            + cleanedNotes
            );

            System.out.println(
                    "=================================================="
            );

            // ----------------------------------------------------
            // DIRECT GEMINI CALL
            // ----------------------------------------------------

            GeminiService.CostEstimate aiEstimate =
                    grokAIService.estimateRepairCost(
                            vehicle,
                            service,
                            cleanedNotes
                    );

            // ----------------------------------------------------
            // AI RESPONSE VALIDATION
            // ----------------------------------------------------

            if (aiEstimate == null) {

                throw new IllegalStateException(
                        "Gemini returned no cost estimate."
                );
            }

            if (!aiEstimate.isAiGenerated()) {

                throw new IllegalStateException(
                        "Returned estimate is not marked as AI generated."
                );
            }

            // ----------------------------------------------------
            // GET COSTS
            // ----------------------------------------------------

            double minimum =
                    roundMoney(
                            aiEstimate.getMinimum()
                    );

            double maximum =
                    roundMoney(
                            aiEstimate.getMaximum()
                    );

            double basePrice =
                    roundMoney(
                            aiEstimate.getBasePrice()
                    );

            double partsMinimum =
                    roundMoney(
                            aiEstimate.getPartsMinimum()
                    );

            double partsMaximum =
                    roundMoney(
                            aiEstimate.getPartsMaximum()
                    );

            double labourMinimum =
                    roundMoney(
                            aiEstimate.getLabourMinimum()
                    );

            double labourMaximum =
                    roundMoney(
                            aiEstimate.getLabourMaximum()
                    );

            // ----------------------------------------------------
            // VALIDATE COST RANGE
            // ----------------------------------------------------

            if (minimum <= 0) {

                throw new IllegalStateException(
                        "Gemini returned an invalid minimum cost: "
                                + minimum
                );
            }

            if (maximum <= 0) {

                throw new IllegalStateException(
                        "Gemini returned an invalid maximum cost: "
                                + maximum
                );
            }

            if (maximum < minimum) {

                throw new IllegalStateException(
                        "Gemini returned maximum cost smaller "
                                + "than minimum cost."
                );
            }

            // ----------------------------------------------------
            // VALIDATE PARTS
            // ----------------------------------------------------

            if (partsMinimum < 0
                    || partsMaximum < 0) {

                throw new IllegalStateException(
                        "Gemini returned invalid parts cost."
                );
            }

            if (partsMaximum < partsMinimum) {

                throw new IllegalStateException(
                        "Gemini returned invalid parts range."
                );
            }

            // ----------------------------------------------------
            // VALIDATE LABOUR
            // ----------------------------------------------------

            if (labourMinimum < 0
                    || labourMaximum < 0) {

                throw new IllegalStateException(
                        "Gemini returned invalid labour cost."
                );
            }

            if (labourMaximum < labourMinimum) {

                throw new IllegalStateException(
                        "Gemini returned invalid labour range."
                );
            }

            // ----------------------------------------------------
            // DURATION
            // ----------------------------------------------------

            String duration =
                    clean(
                            aiEstimate.getDuration()
                    );

            if (duration == null) {

                duration =
                        "To be confirmed after inspection";
            }

            // ----------------------------------------------------
            // CONFIDENCE
            // ----------------------------------------------------

            String confidence =
                    clean(
                            aiEstimate.getConfidence()
                    );

            if (confidence == null) {

                confidence =
                        "Medium";
            }

            // ----------------------------------------------------
            // EXPLANATION
            // ----------------------------------------------------

            String explanation =
                    clean(
                            aiEstimate.getExplanation()
                    );

            String assumptions =
                    clean(
                            aiEstimate.getAssumptions()
                    );

            String recommendation =
                    clean(
                            aiEstimate.getRecommendation()
                    );

            // ----------------------------------------------------
            // BUILD AI NOTE
            // ----------------------------------------------------

            String note =
                    buildAINote(
                            explanation,
                            confidence,
                            assumptions
                    );

            // ----------------------------------------------------
            // FINAL RESULT
            // ----------------------------------------------------

            EstimateResult result =
                    new EstimateResult(
                            minimum,
                            maximum,
                            basePrice,
                            partsMinimum,
                            partsMaximum,
                            labourMinimum,
                            labourMaximum,
                            duration,
                            confidence,
                            note,
                            safeText(assumptions),
                            safeText(recommendation),
                            true
                    );

            System.out.println(
                    "=================================================="
            );

            System.out.println(
                    "Gemini AI Cost Estimation SUCCESS"
            );

            System.out.println(
                    "Minimum: ₹" + minimum
            );

            System.out.println(
                    "Maximum: ₹" + maximum
            );

            System.out.println(
                    "Parts: ₹"
                            + partsMinimum
                            + " - ₹"
                            + partsMaximum
            );

            System.out.println(
                    "Labour: ₹"
                            + labourMinimum
                            + " - ₹"
                            + labourMaximum
            );

            System.out.println(
                    "Duration: "
                            + duration
            );

            System.out.println(
                    "Confidence: "
                            + confidence
            );

            System.out.println(
                    "AI Generated: "
                            + result.isAiGenerated()
            );

            System.out.println(
                    "=================================================="
            );

            return result;

        } catch (Exception e) {

            // ----------------------------------------------------
            // IMPORTANT:
            // NO FALLBACK
            // ----------------------------------------------------

            System.err.println(
                    "=================================================="
            );

            System.err.println(
                    "Gemini AI Cost Estimation FAILED"
            );

            System.err.println(
                    e.getMessage()
            );

            System.err.println(
                    "No hardcoded/Firebase fallback estimate will be used."
            );

            System.err.println(
                    "=================================================="
            );

            throw new RuntimeException(
                    "AI Cost Estimation failed: "
                            + cleanErrorMessage(e),
                    e
            );
        }
    }

    // ============================================================
    // SAVE ESTIMATE
    // ============================================================

    public String saveEstimate(
            Map<String, Object> vehicle,
            RoadService service,
            String notes,
            EstimateResult estimate
    ) throws Exception {

        // --------------------------------------------------------
        // VALIDATE AI RESULT
        // --------------------------------------------------------

        validateAIResult(estimate);

        // --------------------------------------------------------
        // SAVE REAL AI ESTIMATE
        // --------------------------------------------------------

        return dao.saveCostEstimate(
                currentCustomerId(),
                UserSession.getUserName(),
                vehicle,
                service,
                notes,
                estimate.getMinimum(),
                estimate.getMaximum()
        );
    }

    // ============================================================
    // CREATE SERVICE REQUEST
    // ============================================================

    public String createServiceRequest(
            Map<String, Object> vehicle,
            RoadService service,
            String notes,
            String location,
            String estimateId,
            EstimateResult estimate
    ) throws Exception {

        // --------------------------------------------------------
        // VALIDATE AI RESULT
        // --------------------------------------------------------

        validateAIResult(estimate);

        // --------------------------------------------------------
        // CREATE SERVICE REQUEST
        // --------------------------------------------------------

        return dao.createServiceRequestFromEstimate(
                currentCustomerId(),
                UserSession.getUserName(),
                vehicle,
                service,
                notes,
                location,
                estimateId,
                estimate.getMinimum()
        );
    }

    // ============================================================
    // VALIDATE AI RESULT
    // ============================================================

    private void validateAIResult(
            EstimateResult estimate) {

        if (estimate == null) {

            throw new IllegalArgumentException(
                    "No AI estimate available."
            );
        }

        if (!estimate.isAiGenerated()) {

            throw new IllegalStateException(
                    "Only Gemini AI generated estimates can be saved."
            );
        }

        if (!estimate.hasPrice()) {

            throw new IllegalStateException(
                    "AI estimate does not contain a valid price."
            );
        }

        if (estimate.getMaximum()
                < estimate.getMinimum()) {

            throw new IllegalStateException(
                    "AI estimate contains an invalid price range."
            );
        }
    }

    // ============================================================
    // BUILD AI NOTE
    // ============================================================

    private String buildAINote(
            String explanation,
            String confidence,
            String assumptions) {

        StringBuilder builder =
                new StringBuilder();

        // --------------------------------------------------------
        // AI ANALYSIS
        // --------------------------------------------------------

        if (!isBlank(explanation)) {

            builder.append(
                    "AI Analysis: "
            );

            builder.append(
                    explanation
            );
        }

        // --------------------------------------------------------
        // CONFIDENCE
        // --------------------------------------------------------

        if (!isBlank(confidence)) {

            if (builder.length() > 0) {

                builder.append(
                        "\n\n"
                );
            }

            builder.append(
                    "AI Confidence: "
            );

            builder.append(
                    confidence
            );
        }

        // --------------------------------------------------------
        // ASSUMPTIONS
        // --------------------------------------------------------

        if (!isBlank(assumptions)) {

            if (builder.length() > 0) {

                builder.append(
                        "\n\n"
                );
            }

            builder.append(
                    "AI Assumptions: "
            );

            builder.append(
                    assumptions
            );
        }

        // --------------------------------------------------------
        // SAFETY NOTE
        // --------------------------------------------------------

        if (builder.length() > 0) {

            builder.append(
                    "\n\n"
            );
        }

        builder.append(
                "Final cost may change after physical mechanic "
                        + "inspection, actual parts selection and "
                        + "labour requirements."
        );

        return builder.toString();
    }

    // ============================================================
    // CURRENT CUSTOMER ID
    // ============================================================

    private String currentCustomerId() {

        String email =
                clean(
                        UserSession.getUserEmail()
                );

        if (email != null) {

            return email.toLowerCase(
                    Locale.ROOT
            );
        }

        String id =
                clean(
                        UserSession.getUserId()
                );

        if (id != null) {

            return id;
        }

        throw new IllegalStateException(
                "Please login as a customer first."
        );
    }

    // ============================================================
    // SERVICE NAME
    // ============================================================

    private String safeServiceName(
            RoadService service) {

        if (service == null) {
            return "Unknown Service";
        }

        String name =
                clean(
                        service.getName()
                );

        return name == null
                ? "Unnamed Service"
                : name;
    }

    // ============================================================
    // ROUND MONEY
    // ============================================================

    private double roundMoney(
            double value) {

        return Math.round(
                value * 100.0
        ) / 100.0;
    }

    // ============================================================
    // CLEAN STRING
    // ============================================================

    private String clean(
            String value) {

        if (value == null) {
            return null;
        }

        String cleaned =
                value.trim();

        return cleaned.isEmpty()
                ? null
                : cleaned;
    }

    // ============================================================
    // CLEAN OR EMPTY
    // ============================================================

    private String cleanOrEmpty(
            String value) {

        String cleaned =
                clean(value);

        return cleaned == null
                ? ""
                : cleaned;
    }

    // ============================================================
    // SAFE TEXT
    // ============================================================

    private String safeText(
            String value) {

        return value == null
                ? ""
                : value.trim();
    }

    // ============================================================
    // IS BLANK
    // ============================================================

    private boolean isBlank(
            String value) {

        return value == null
                || value.trim().isEmpty();
    }

    // ============================================================
    // ERROR MESSAGE
    // ============================================================

    private String cleanErrorMessage(
            Exception e) {

        if (e == null) {
            return "Unknown AI error.";
        }

        String message =
                e.getMessage();

        if (message == null
                || message.trim().isEmpty()) {

            return e.getClass()
                    .getSimpleName();
        }

        return message.trim();
    }
}