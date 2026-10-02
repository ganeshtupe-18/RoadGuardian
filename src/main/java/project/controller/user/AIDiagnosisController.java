package project.controller.user;

import project.dao.user.DiagnosisDAO;
import project.model.DiagnosisResult;
import project.model.RoadService;
import project.service.GeminiService;
import project.ui.user.UserSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * ============================================================
 * AI DIAGNOSIS CONTROLLER
 * ============================================================
 *
 * Flow:
 *
 * AIDiagnosisPage
 *        ↓
 * AIDiagnosisController
 *        ↓
 * GeminiService
 *        ↓
 * Google Gemini API
 *        ↓
 * DiagnosisResult
 *
 * Firebase operations are handled through DiagnosisDAO.
 */
public class AIDiagnosisController {

    private final DiagnosisDAO dao;
    private final GeminiService geminiService;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public AIDiagnosisController() throws Exception {

        this.dao = new DiagnosisDAO();
        this.geminiService = new GeminiService();
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
    // BASIC DIAGNOSIS
    // ============================================================

    public DiagnosisResult diagnose(
            List<String> symptoms,
            List<RoadService> services) {

        return diagnose(
                null,
                null,
                "",
                symptoms,
                services
        );
    }

    // ============================================================
    // MAIN GEMINI DIAGNOSIS
    // ============================================================

    public DiagnosisResult diagnose(
            Map<String, Object> vehicle,
            String lastServiceDate,
            String userQuery,
            List<String> symptoms,
            List<RoadService> services) {

        List<String> normalizedSymptoms =
                normalizeSymptoms(symptoms);

        String cleanedQuery =
                clean(userQuery);

        // --------------------------------------------------------
        // VALIDATION
        // --------------------------------------------------------

        if (normalizedSymptoms.isEmpty()
                && cleanedQuery == null) {

            throw new IllegalArgumentException(
                    "Please select at least one symptom or describe your problem."
            );
        }

        // --------------------------------------------------------
        // CALL GEMINI
        // --------------------------------------------------------

        try {

            System.out.println(
                    "=================================================="
            );

            System.out.println(
                    "Starting Gemini AI Diagnosis..."
            );

            System.out.println(
                    "Customer Query: "
                            + safe(cleanedQuery)
            );

            System.out.println(
                    "Symptoms: "
                            + normalizedSymptoms
            );

            System.out.println(
                    "Last Service Date: "
                            + safe(lastServiceDate)
            );

            System.out.println(
                    "=================================================="
            );

            DiagnosisResult aiResult =
                    geminiService.diagnose(
                            vehicle,
                            lastServiceDate,
                            cleanedQuery,
                            normalizedSymptoms,
                            services
                    );

            // ----------------------------------------------------
            // VALIDATE RESULT
            // ----------------------------------------------------

            if (aiResult == null) {

                throw new IllegalStateException(
                        "Gemini returned an empty diagnosis."
                );
            }

            // ----------------------------------------------------
            // PRESERVE SELECTED SYMPTOMS
            // ----------------------------------------------------

            aiResult.setSymptoms(
                    normalizedSymptoms
            );

            // ----------------------------------------------------
            // MATCH ROADGUARDIAN SERVICE
            // ----------------------------------------------------

            RoadService matched =
                    findBestService(
                            services,
                            aiResult.getRecommendedService(),
                            aiResult.getDiagnosis()
                    );

            if (matched != null) {

                aiResult.setMatchedServiceId(
                        matched.getServiceId()
                );

                aiResult.setRecommendedService(
                        matched.getName()
                );

                aiResult.setBasePrice(
                        matched.getBasePrice()
                );

                aiResult.setEstimatedDuration(
                        matched.getEstimatedDuration()
                );
            }

            // ----------------------------------------------------
            // LAST SERVICE RECOMMENDATION
            // ----------------------------------------------------

            if (isBlank(
                    aiResult.getLastServiceRecommendation()
            )) {

                aiResult.setLastServiceRecommendation(
                        buildLastServiceRecommendation(
                                lastServiceDate
                        )
                );
            }

            // ----------------------------------------------------
            // LOG RESULT
            // ----------------------------------------------------

            System.out.println(
                    "Gemini AI Diagnosis SUCCESS"
            );

            System.out.println(
                    "Diagnosis: "
                            + safe(
                            aiResult.getDiagnosis()
                    )
            );

            System.out.println(
                    "Severity: "
                            + safe(
                            aiResult.getSeverity()
                    )
            );

            System.out.println(
                    "Recommended Service: "
                            + safe(
                            aiResult.getRecommendedService()
                    )
            );

            System.out.println(
                    "=================================================="
            );

            return aiResult;

        } catch (Exception e) {

            System.err.println(
                    "=================================================="
            );

            System.err.println(
                    "Gemini AI Diagnosis FAILED"
            );

            System.err.println(
                    e.getMessage()
            );

            System.err.println(
                    "=================================================="
            );

            /*
             * No fake/local diagnosis.
             *
             * Real Gemini response is required.
             */
            throw new RuntimeException(
                    "Gemini AI diagnosis failed: "
                            + cleanErrorMessage(e),
                    e
            );
        }
    }

    // ============================================================
    // SAVE DIAGNOSIS
    // ============================================================

    public String saveDiagnosis(
            Map<String, Object> vehicle,
            DiagnosisResult result)
            throws Exception {

        return saveDiagnosis(
                vehicle,
                result,
                "",
                null
        );
    }

    // ============================================================
    // SAVE DIAGNOSIS WITH QUERY + LAST SERVICE
    // ============================================================

    public String saveDiagnosis(
            Map<String, Object> vehicle,
            DiagnosisResult result,
            String userQuery,
            String lastServiceDate)
            throws Exception {

        if (result == null) {

            throw new IllegalArgumentException(
                    "Diagnosis result cannot be null."
            );
        }

        return dao.saveDiagnosis(
                currentCustomerId(),
                UserSession.getUserName(),
                vehicle,
                result,
                userQuery,
                lastServiceDate
        );
    }

    // ============================================================
    // CREATE SERVICE REQUEST
    // ============================================================

    public String createServiceRequest(
            Map<String, Object> vehicle,
            String location,
            DiagnosisResult result)
            throws Exception {

        return createServiceRequest(
                vehicle,
                location,
                result,
                "",
                null
        );
    }

    // ============================================================
    // CREATE SERVICE REQUEST WITH AI DATA
    // ============================================================

    public String createServiceRequest(
            Map<String, Object> vehicle,
            String location,
            DiagnosisResult result,
            String userQuery,
            String lastServiceDate)
            throws Exception {

        if (result == null) {

            throw new IllegalArgumentException(
                    "Diagnosis result cannot be null."
            );
        }

        return dao.createServiceRequestFromDiagnosis(
                currentCustomerId(),
                UserSession.getUserName(),
                vehicle,
                location,
                result,
                userQuery,
                lastServiceDate
        );
    }

    // ============================================================
    // DEFAULT SYMPTOMS
    // ============================================================

    public List<String> getSymptoms() {

        return Arrays.asList(

                "Engine won't start",

                "Battery light ON",

                "Clicking while starting",

                "Engine overheating",

                "Smoke from engine area",

                "Unusual engine noise",

                "Loss of engine power",

                "Brake noise",

                "Brake pedal feels unusual",

                "Flat tyre / puncture",

                "Vehicle vibration",

                "Steering pulling to one side",

                "Clutch / gear shifting issue",

                "AC not cooling",

                "Electrical / lights issue"
        );
    }

    // ============================================================
    // FIND BEST SERVICE
    // ============================================================

    private RoadService findBestService(
            List<RoadService> services,
            String keyword,
            String diagnosis) {

        if (services == null
                || services.isEmpty()) {

            return null;
        }

        String key =
                clean(keyword);

        String diagnosisText =
                safe(diagnosis)
                        .toLowerCase(
                                Locale.ROOT
                        );

        // --------------------------------------------------------
        // DIRECT SERVICE NAME / CATEGORY MATCH
        // --------------------------------------------------------

        if (key != null) {

            key = key.toLowerCase(
                    Locale.ROOT
            );

            for (RoadService service : services) {

                if (service == null
                        || !service.isActive()) {

                    continue;
                }

                String serviceName =
                        safe(
                                service.getName()
                        ).toLowerCase(
                                Locale.ROOT
                        );

                String category =
                        safe(
                                service.getCategory()
                        ).toLowerCase(
                                Locale.ROOT
                        );

                String description =
                        safe(
                                service.getDescription()
                        ).toLowerCase(
                                Locale.ROOT
                        );

                if (serviceName.contains(key)
                        || category.contains(key)
                        || description.contains(key)) {

                    return service;
                }
            }
        }

        // --------------------------------------------------------
        // DIAGNOSIS MATCH
        // --------------------------------------------------------

        for (RoadService service : services) {

            if (service == null
                    || !service.isActive()) {

                continue;
            }

            String name =
                    safe(
                            service.getName()
                    ).toLowerCase(
                            Locale.ROOT
                    );

            String category =
                    safe(
                            service.getCategory()
                    ).toLowerCase(
                            Locale.ROOT
                    );

            String description =
                    safe(
                            service.getDescription()
                    ).toLowerCase(
                            Locale.ROOT
                    );

            if ((!name.isEmpty()
                    && diagnosisText.contains(name))
                    ||
                    (!category.isEmpty()
                    && diagnosisText.contains(category))
                    ||
                    (!description.isEmpty()
                    && diagnosisText.contains(description))) {

                return service;
            }
        }

        // --------------------------------------------------------
        // COMMON AUTOMOTIVE KEYWORDS
        // --------------------------------------------------------

        String[] keywordGroups = {

                "brake",
                "engine",
                "battery",
                "electrical",
                "tyre",
                "tire",
                "wheel",
                "clutch",
                "gear",
                "transmission",
                "ac",
                "air conditioning",
                "cooling",
                "suspension",
                "alignment",
                "inspection"
        };

        for (String group : keywordGroups) {

            if (!diagnosisText.contains(group)) {
                continue;
            }

            for (RoadService service : services) {

                if (service == null
                        || !service.isActive()) {

                    continue;
                }

                String serviceText =
                        (
                                safe(service.getName())
                                        + " "
                                        + safe(service.getCategory())
                                        + " "
                                        + safe(service.getDescription())
                        ).toLowerCase(
                                Locale.ROOT
                        );

                if (serviceText.contains(group)) {

                    return service;
                }
            }
        }

        return null;
    }

    // ============================================================
    // NORMALIZE SYMPTOMS
    // ============================================================

    private List<String> normalizeSymptoms(
            List<String> symptoms) {

        List<String> normalized =
                new ArrayList<>();

        if (symptoms == null) {
            return normalized;
        }

        for (String symptom : symptoms) {

            if (!isBlank(symptom)) {

                String value =
                        symptom.trim();

                if (!normalized.contains(value)) {

                    normalized.add(value);
                }
            }
        }

        return normalized;
    }

    // ============================================================
    // LAST SERVICE RECOMMENDATION
    // ============================================================

    private String buildLastServiceRecommendation(
            String lastServiceDate) {

        if (isBlank(lastServiceDate)) {

            return
                    "Last service date was not provided. "
                            + "Consider checking the vehicle service history "
                            + "and manufacturer's recommended maintenance schedule.";
        }

        return
                "Last service was recorded on "
                        + lastServiceDate
                        + ". Maintenance recommendations should consider "
                        + "this date together with vehicle age, mileage and "
                        + "manufacturer service schedule.";
    }

    // ============================================================
    // CURRENT CUSTOMER ID
    // ============================================================

    private String currentCustomerId() {

        String id =
                clean(
                        UserSession.getUserId()
                );

        if (id != null) {

            return id;
        }

        String email =
                clean(
                        UserSession.getUserEmail()
                );

        if (email != null) {

            return email.toLowerCase(
                    Locale.ROOT
            );
        }

        throw new IllegalStateException(
                "Please login as a customer first."
        );
    }

    // ============================================================
    // CLEAN
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
    // SAFE
    // ============================================================

    private String safe(
            String value) {

        return value == null
                ? ""
                : value.trim();
    }

    // ============================================================
    // BLANK CHECK
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

            return "Unknown Gemini AI error.";
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