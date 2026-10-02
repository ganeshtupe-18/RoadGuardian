package project.dao.user;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;

import project.firebase.FirebaseConfig;
import project.model.DiagnosisResult;
import project.model.RoadService;
import project.model.ServiceRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiagnosisDAO {

    private static final String DIAGNOSIS_COLLECTION = "diagnosisRecords";
    private static final String ESTIMATE_COLLECTION = "costEstimates";

    private final Firestore firestore;
    private final VehicleDAO vehicleDAO;
    private final ServiceCatalogDAO serviceCatalogDAO;
    private final ServiceRequestDAO serviceRequestDAO;

    // =========================================================
    // CONSTRUCTORS
    // =========================================================

    public DiagnosisDAO() throws IOException {
        this(FirebaseConfig.getFirestore());
    }

    public DiagnosisDAO(Firestore firestore) {

        if (firestore == null) {
            throw new IllegalArgumentException(
                    "Firestore cannot be null."
            );
        }

        this.firestore = firestore;

        this.vehicleDAO =
                new VehicleDAO();

        this.serviceCatalogDAO =
                new ServiceCatalogDAO(firestore);

        this.serviceRequestDAO =
                new ServiceRequestDAO(firestore);
    }

    // =========================================================
    // VEHICLES
    // =========================================================

    public List<Map<String, Object>> getCustomerVehicles(
            String customerEmail
    ) throws Exception {

        List<Map<String, Object>> result =
                vehicleDAO.getVehicles(
                        customerEmail
                );

        return result == null
                ? new ArrayList<>()
                : result;
    }

    // =========================================================
    // ACTIVE SERVICES
    // =========================================================

    public List<RoadService> getActiveServices()
            throws Exception {

        List<RoadService> result =
                serviceCatalogDAO.getActiveServices();

        return result == null
                ? new ArrayList<>()
                : result;
    }

    // =========================================================
    // SAVE DIAGNOSIS
    // =========================================================
    //
    // Existing method kept for backward compatibility.
    //
    // =========================================================

    public String saveDiagnosis(
            String customerId,
            String customerName,
            Map<String, Object> vehicle,
            DiagnosisResult result
    ) throws Exception {

        return saveDiagnosis(
                customerId,
                customerName,
                vehicle,
                result,
                null,
                null
        );
    }

    // =========================================================
    // SAVE AI DIAGNOSIS WITH USER CONTEXT
    // =========================================================

    public String saveDiagnosis(
            String customerId,
            String customerName,
            Map<String, Object> vehicle,
            DiagnosisResult result,
            String userQuery,
            String lastServiceDate
    ) throws Exception {

        if (
                clean(customerId) == null
                        || vehicle == null
                        || result == null
        ) {

            throw new IllegalArgumentException(
                    "Diagnosis data is incomplete."
            );
        }

        DocumentReference document =
                firestore
                        .collection(
                                DIAGNOSIS_COLLECTION
                        )
                        .document();

        String diagnosisId =
                document.getId();

        String now =
                currentTime();

        Map<String, Object> data =
                new HashMap<>();

        // -----------------------------------------------------
        // BASIC IDENTIFICATION
        // -----------------------------------------------------

        data.put(
                "diagnosisId",
                diagnosisId
        );

        data.put(
                "customerId",
                normalizeId(customerId)
        );

        data.put(
                "customerName",
                clean(customerName)
        );

        // -----------------------------------------------------
        // VEHICLE INFORMATION
        // -----------------------------------------------------

        data.put(
                "vehicleId",
                firstString(
                        vehicle,
                        "vehicleId",
                        "id"
                )
        );

        data.put(
                "vehicleNumber",
                firstString(
                        vehicle,
                        "vehicleNumber",
                        "registrationNumber"
                )
        );

        data.put(
                "vehicleBrand",
                firstString(
                        vehicle,
                        "brand",
                        "make",
                        "name"
                )
        );

        data.put(
                "vehicleModel",
                firstString(
                        vehicle,
                        "model"
                )
        );

        data.put(
                "vehicleType",
                firstString(
                        vehicle,
                        "vehicleType",
                        "type"
                )
        );

        data.put(
                "vehicleYear",
                firstString(
                        vehicle,
                        "year",
                        "manufacturingYear"
                )
        );

        // -----------------------------------------------------
        // USER AI CONTEXT
        // -----------------------------------------------------

        data.put(
                "userQuery",
                clean(userQuery)
        );

        data.put(
                "lastServiceDate",
                clean(lastServiceDate)
        );

        // -----------------------------------------------------
        // SYMPTOMS
        // -----------------------------------------------------

        data.put(
                "symptoms",
                result.getSymptoms()
        );

        // -----------------------------------------------------
        // AI RESULT
        // -----------------------------------------------------

        data.put(
                "preliminaryDiagnosis",
                clean(
                        result.getDiagnosis()
                )
        );

        data.put(
                "explanation",
                clean(
                        result.getExplanation()
                )
        );

        data.put(
                "recommendedService",
                clean(
                        result.getRecommendedService()
                )
        );

        data.put(
                "matchedServiceId",
                clean(
                        result.getMatchedServiceId()
                )
        );

        data.put(
                "severity",
                clean(
                        result.getSeverity()
                )
        );

        data.put(
                "catalogBasePrice",
                result.getBasePrice()
        );

        data.put(
                "estimatedDuration",
                clean(
                        result.getEstimatedDuration()
                )
        );

        // -----------------------------------------------------
        // RECORD STATUS
        // -----------------------------------------------------

        data.put(
                "status",
                "Diagnosed"
        );

        data.put(
                "source",
                "AI Diagnosis"
        );

        data.put(
                "aiProvider",
                "Grok"
        );

        data.put(
                "serviceRequestId",
                ""
        );

        // -----------------------------------------------------
        // TIMESTAMPS
        // -----------------------------------------------------

        data.put(
                "createdAt",
                now
        );

        data.put(
                "updatedAt",
                now
        );

        document
                .set(data)
                .get();

        result.setDiagnosisId(
                diagnosisId
        );

        return diagnosisId;
    }

    // =========================================================
    // CREATE SERVICE REQUEST
    // =========================================================
    //
    // Existing method preserved.
    //
    // =========================================================

    public String createServiceRequestFromDiagnosis(
            String customerId,
            String customerName,
            Map<String, Object> vehicle,
            String location,
            DiagnosisResult result
    ) throws Exception {

        return createServiceRequestFromDiagnosis(
                customerId,
                customerName,
                vehicle,
                location,
                result,
                null,
                null
        );
    }

    // =========================================================
    // CREATE SERVICE REQUEST WITH AI CONTEXT
    // =========================================================

    public String createServiceRequestFromDiagnosis(
            String customerId,
            String customerName,
            Map<String, Object> vehicle,
            String location,
            DiagnosisResult result,
            String userQuery,
            String lastServiceDate
    ) throws Exception {

        if (
                result == null
                        || vehicle == null
        ) {

            throw new IllegalArgumentException(
                    "Run diagnosis before creating a service request."
            );
        }

        if (
                clean(customerId) == null
        ) {

            throw new IllegalArgumentException(
                    "Customer is not logged in."
            );
        }

        String vehicleId =
                firstString(
                        vehicle,
                        "vehicleId",
                        "id"
                );

        String vehicleNumber =
                firstString(
                        vehicle,
                        "vehicleNumber",
                        "registrationNumber"
                );

        if (
                clean(vehicleId) == null
        ) {

            throw new IllegalArgumentException(
                    "Selected vehicle is invalid."
            );
        }

        // -----------------------------------------------------
        // CREATE SERVICE REQUEST MODEL
        // -----------------------------------------------------

        ServiceRequest request =
                new ServiceRequest();

        request.setCustomerId(
                normalizeId(customerId)
        );

        request.setCustomerName(
                clean(customerName)
        );

        request.setVehicleId(
                vehicleId
        );

        request.setVehicleNumber(
                vehicleNumber
        );

        request.setServiceType(
                firstNonBlank(
                        result.getRecommendedService(),
                        "Vehicle Inspection"
                )
        );

        request.setDescription(
                buildDiagnosisDescription(
                        result,
                        userQuery,
                        lastServiceDate
                )
        );

        request.setLocation(
                clean(location)
        );

        request.setSource(
                "AI Diagnosis"
        );

        request.setPriority(
                "High".equalsIgnoreCase(
                        result.getSeverity()
                )
                        ? "High"
                        : "Normal"
        );

        if (
                result.getBasePrice() > 0
        ) {

            request.setEstimatedCost(
                    String.valueOf(
                            result.getBasePrice()
                    )
            );
        }

        // -----------------------------------------------------
        // SAVE SERVICE REQUEST
        // -----------------------------------------------------

        String requestId =
                serviceRequestDAO.createRequest(
                        request
                );

        // -----------------------------------------------------
        // LINK DIAGNOSIS -> REQUEST
        // -----------------------------------------------------

        String diagnosisId =
                clean(
                        result.getDiagnosisId()
                );

        if (
                diagnosisId != null
        ) {

            Map<String, Object> update =
                    new HashMap<>();

            update.put(
                    "serviceRequestId",
                    requestId
            );

            update.put(
                    "status",
                    "Service Requested"
            );

            update.put(
                    "updatedAt",
                    currentTime()
            );

            firestore
                    .collection(
                            DIAGNOSIS_COLLECTION
                    )
                    .document(
                            diagnosisId
                    )
                    .set(
                            update,
                            SetOptions.merge()
                    )
                    .get();
        }

        return requestId;
    }

    // =========================================================
    // BUILD AI SERVICE REQUEST DESCRIPTION
    // =========================================================

    private String buildDiagnosisDescription(
            DiagnosisResult result,
            String userQuery,
            String lastServiceDate
    ) {

        String symptoms =
                result.getSymptoms().isEmpty()
                        ? "Not provided"
                        : String.join(
                                ", ",
                                result.getSymptoms()
                        );

        StringBuilder description =
                new StringBuilder();

        description.append(
                "AI Vehicle Breakdown Diagnosis"
        );

        description.append(
                "\n\nCustomer problem/query: "
        );

        description.append(
                firstNonBlank(
                        userQuery,
                        "Not provided"
                )
        );

        description.append(
                "\n\nLast service date: "
        );

        description.append(
                firstNonBlank(
                        lastServiceDate,
                        "Not provided"
                )
        );

        description.append(
                "\n\nSymptoms: "
        );

        description.append(
                symptoms
        );

        description.append(
                "\n\nPreliminary diagnosis: "
        );

        description.append(
                firstNonBlank(
                        result.getDiagnosis(),
                        "Needs inspection"
                )
        );

        description.append(
                "\n\nAI explanation: "
        );

        description.append(
                firstNonBlank(
                        result.getExplanation(),
                        "Mechanic inspection required."
                )
        );

        description.append(
                "\n\nRecommended service: "
        );

        description.append(
                firstNonBlank(
                        result.getRecommendedService(),
                        "Vehicle Inspection"
                )
        );

        description.append(
                "\n\nPriority: "
        );

        description.append(
                firstNonBlank(
                        result.getSeverity(),
                        "Normal"
                )
        );

        description.append(
                "\n\nEstimated duration: "
        );

        description.append(
                firstNonBlank(
                        result.getEstimatedDuration(),
                        "Confirmed after inspection"
                )
        );

        description.append(
                "\n\nNote: This is a preliminary AI "
                        + "diagnostic suggestion. Final diagnosis "
                        + "and repair must be confirmed by the mechanic."
        );

        return description.toString();
    }

    // =========================================================
    // COST ESTIMATE
    // =========================================================

    public String saveCostEstimate(
            String customerId,
            String customerName,
            Map<String, Object> vehicle,
            RoadService service,
            String notes,
            double minimum,
            double maximum
    ) throws Exception {

        if (
                clean(customerId) == null
                        || vehicle == null
                        || service == null
        ) {

            throw new IllegalArgumentException(
                    "Estimate data is incomplete."
            );
        }

        DocumentReference document =
                firestore
                        .collection(
                                ESTIMATE_COLLECTION
                        )
                        .document();

        String estimateId =
                document.getId();

        String now =
                currentTime();

        Map<String, Object> data =
                new HashMap<>();

        data.put(
                "estimateId",
                estimateId
        );

        data.put(
                "customerId",
                normalizeId(customerId)
        );

        data.put(
                "customerName",
                clean(customerName)
        );

        data.put(
                "vehicleId",
                firstString(
                        vehicle,
                        "vehicleId",
                        "id"
                )
        );

        data.put(
                "vehicleNumber",
                firstString(
                        vehicle,
                        "vehicleNumber",
                        "registrationNumber"
                )
        );

        data.put(
                "vehicleBrand",
                firstString(
                        vehicle,
                        "brand",
                        "make",
                        "name"
                )
        );

        data.put(
                "vehicleModel",
                firstString(
                        vehicle,
                        "model"
                )
        );

        data.put(
                "vehicleType",
                firstString(
                        vehicle,
                        "vehicleType",
                        "type"
                )
        );

        data.put(
                "serviceId",
                clean(
                        service.getServiceId()
                )
        );

        data.put(
                "serviceName",
                clean(
                        service.getName()
                )
        );

        data.put(
                "serviceCategory",
                clean(
                        service.getCategory()
                )
        );

        data.put(
                "catalogBasePrice",
                service.getBasePrice()
        );

        data.put(
                "estimatedMinimum",
                minimum
        );

        data.put(
                "estimatedMaximum",
                maximum
        );

        data.put(
                "estimatedDuration",
                clean(
                        service.getEstimatedDuration()
                )
        );

        data.put(
                "notes",
                clean(notes)
        );

        data.put(
                "source",
                "Cost Estimator"
        );

        data.put(
                "status",
                "Estimated"
        );

        data.put(
                "serviceRequestId",
                ""
        );

        data.put(
                "createdAt",
                now
        );

        data.put(
                "updatedAt",
                now
        );

        document
                .set(data)
                .get();

        return estimateId;
    }

    // =========================================================
    // CREATE REQUEST FROM COST ESTIMATE
    // =========================================================

    public String createServiceRequestFromEstimate(
            String customerId,
            String customerName,
            Map<String, Object> vehicle,
            RoadService service,
            String notes,
            String location,
            String estimateId,
            double minimum
    ) throws Exception {

        if (
                vehicle == null
                        || service == null
        ) {

            throw new IllegalArgumentException(
                    "Select vehicle and service first."
            );
        }

        String vehicleId =
                firstString(
                        vehicle,
                        "vehicleId",
                        "id"
                );

        String vehicleNumber =
                firstString(
                        vehicle,
                        "vehicleNumber",
                        "registrationNumber"
                );

        ServiceRequest request =
                new ServiceRequest();

        request.setCustomerId(
                normalizeId(customerId)
        );

        request.setCustomerName(
                clean(customerName)
        );

        request.setVehicleId(
                vehicleId
        );

        request.setVehicleNumber(
                vehicleNumber
        );

        request.setServiceType(
                firstNonBlank(
                        service.getName(),
                        "Vehicle Service"
                )
        );

        request.setDescription(
                buildEstimateDescription(
                        service,
                        notes
                )
        );

        request.setLocation(
                clean(location)
        );

        request.setSource(
                "Cost Estimator"
        );

        request.setPriority(
                "Normal"
        );

        if (
                minimum > 0
        ) {

            request.setEstimatedCost(
                    String.valueOf(
                            minimum
                    )
            );
        }

        String requestId =
                serviceRequestDAO.createRequest(
                        request
                );

        if (
                clean(estimateId) != null
        ) {

            Map<String, Object> update =
                    new HashMap<>();

            update.put(
                    "serviceRequestId",
                    requestId
            );

            update.put(
                    "status",
                    "Service Requested"
            );

            update.put(
                    "updatedAt",
                    currentTime()
            );

            firestore
                    .collection(
                            ESTIMATE_COLLECTION
                    )
                    .document(
                            estimateId
                    )
                    .set(
                            update,
                            SetOptions.merge()
                    )
                    .get();
        }

        return requestId;
    }

    // =========================================================
    // ESTIMATE DESCRIPTION
    // =========================================================

    private String buildEstimateDescription(
            RoadService service,
            String notes
    ) {

        String description =
                "Requested from Cost Estimator. Service: "
                        + firstNonBlank(
                        service.getName(),
                        "Vehicle Service"
                )
                        + ". Final cost will be confirmed "
                        + "after mechanic inspection.";

        if (
                clean(notes) != null
        ) {

            description +=
                    "\nCustomer notes: "
                            + notes.trim();
        }

        return description;
    }

    // =========================================================
    // FIRST STRING
    // =========================================================

    private String firstString(
            Map<String, Object> data,
            String... fields
    ) {

        if (
                data == null
                        || fields == null
        ) {

            return null;
        }

        for (String field : fields) {

            Object value =
                    data.get(field);

            if (
                    value != null
                            && clean(
                            value.toString()
                    ) != null
            ) {

                return value
                        .toString()
                        .trim();
            }
        }

        return null;
    }

    // =========================================================
    // FIRST NON-BLANK
    // =========================================================

    private String firstNonBlank(
            String... values
    ) {

        if (values != null) {

            for (String value : values) {

                if (
                        clean(value) != null
                ) {

                    return value.trim();
                }
            }
        }

        return null;
    }

    // =========================================================
    // NORMALIZE ID
    // =========================================================

    private String normalizeId(
            String value
    ) {

        value =
                clean(value);

        if (
                value == null
        ) {

            return null;
        }

        return value.contains("@")
                ? value.toLowerCase()
                : value;
    }

    // =========================================================
    // CLEAN
    // =========================================================

    private String clean(
            String value
    ) {

        if (
                value == null
        ) {

            return null;
        }

        String cleaned =
                value.trim();

        return cleaned.isEmpty()
                ? null
                : cleaned;
    }

    // =========================================================
    // CURRENT TIME
    // =========================================================

    private String currentTime() {

        return String.valueOf(
                System.currentTimeMillis()
        );
    }
}