package project.service;

import project.model.DiagnosisResult;
import project.model.RoadService;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * ============================================================
 * ROADGUARDIAN GROQ AI SERVICE
 * ============================================================
 *
 * Groq API integration using the OpenAI-compatible Chat Completions API.
 *
 * Environment variable:
 * GROQ_API_KEY
 *
 * Features:
 *
 * 1. AI Vehicle Diagnosis
 * 2. AI Repair Cost Estimation
 * 3. Marathi / Hindi / English / Hinglish support
 * 4. Last service date based maintenance recommendation
 * 5. Real Groq response - no fake fallback
 * 6. Multiple Groq model fallback
 * 7. Automatic retry for temporary API failures
 *
 * Model priority:
 *
 * 1. llama-3.1-8b-instant
 * 2. llama-3.3-70b-versatile
 * 3. openai/gpt-oss-20b
 *
 * Package:
 * project.service
 */
public class GeminiService {

    // ============================================================
    // GROQ MODEL CONFIGURATION
    // ============================================================

    /**
     * Models are tried in this order.
     *
     * IMPORTANT:
     * These are NOT fake/local fallbacks.
     * Every model is a real Groq API model.
     */
    private static final String[] GROQ_MODELS = {
    "openai/gpt-oss-20b",
    "openai/gpt-oss-120b",
    "qwen/qwen3.6-27b"
};

    private static final String API_BASE_URL =
            "https://api.groq.com/openai/v1/chat/completions";

    /**
     * Number of attempts for temporary failures on each model.
     *
     * Attempt 1:
     * immediate request
     *
     * Attempt 2:
     * after retry delay
     */
    private static final int MAX_ATTEMPTS_PER_MODEL = 2;

    /**
     * Retry delays:
     *
     * First retry  -> 1 second
     * Second retry -> 2 seconds
     */
    private static final long FIRST_RETRY_DELAY_MS = 1000L;
    private static final long SECOND_RETRY_DELAY_MS = 2000L;

    private final HttpClient httpClient;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public GeminiService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    // ============================================================
    // GET GEMINI API KEY
    // ============================================================

    private String getApiKey() {

        String apiKey =
                System.getenv("GROQ_API_KEY");

        if (apiKey == null
                || apiKey.trim().isEmpty()) {

            throw new IllegalStateException(
                    "GROQ_API_KEY is not configured. "
                            + "Set the environment variable and restart VS Code."
            );
        }

        apiKey =
                apiKey.trim();

        /*
         * If the key was accidentally stored
         * with surrounding quotes, remove them.
         */
        if (apiKey.length() > 1
                && apiKey.startsWith("\"")
                && apiKey.endsWith("\"")) {

            apiKey =
                    apiKey.substring(
                            1,
                            apiKey.length() - 1
                    ).trim();
        }

        if (apiKey.isEmpty()) {

            throw new IllegalStateException(
                    "GROQ_API_KEY is empty."
            );
        }

        return apiKey;
    }

    // ============================================================
    // AI DIAGNOSIS
    // ============================================================

    public DiagnosisResult diagnose(
            Map<String, Object> vehicle,
            String lastServiceDate,
            String userQuery,
            List<String> symptoms,
            List<RoadService> services) {

        try {

            String prompt =
                    buildDiagnosisPrompt(
                            vehicle,
                            lastServiceDate,
                            userQuery,
                            symptoms,
                            services
                    );

            String aiResponse =
                    callGroq(prompt);

            if (isBlank(aiResponse)) {

                throw new IllegalStateException(
                        "Groq returned an empty diagnosis."
                );
            }

            DiagnosisResult result =
                    parseDiagnosisResponse(
                            aiResponse
                    );

            result.setAiResponse(
                    aiResponse
            );

            return result;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Groq AI diagnosis failed: "
                            + getErrorMessage(e),
                    e
            );
        }
    }

    // ============================================================
    // AI COST ESTIMATION
    // ============================================================

    /**
     * Generates a REAL repair/service cost estimate.
     *
     * IMPORTANT:
     *
     * - No hardcoded AI price.
     * - No local fallback price.
     * - Admin base price is only reference information.
     * - Final estimate comes from Groq.
     * - If one Groq model fails, another Groq model is tried.
     */
    public CostEstimate estimateRepairCost(
            Map<String, Object> vehicle,
            RoadService service,
            String problemNotes) {

        if (service == null) {

            throw new IllegalArgumentException(
                    "Service cannot be null."
            );
        }

        if (isBlank(problemNotes)) {

            throw new IllegalArgumentException(
                    "Customer problem description is required."
            );
        }

        try {

            String prompt =
                    buildCostEstimationPrompt(
                            vehicle,
                            service,
                            problemNotes
                    );

            String aiResponse =
                    callGroq(prompt);

            if (isBlank(aiResponse)) {

                throw new IllegalStateException(
                        "Groq returned an empty cost estimate."
                );
            }

            String json =
                    extractJsonObject(
                            aiResponse
                    );

            CostEstimate estimate =
                    parseCostEstimate(
                            json
                    );

            if (estimate == null) {

                throw new IllegalStateException(
                        "Groq returned an empty cost estimate."
                );
            }

            /*
             * Admin price is stored only as a reference.
             *
             * It is NOT used to calculate minimum/maximum.
             */
            estimate.setBasePrice(
                    service.getBasePrice()
            );

            estimate.setAiGenerated(
                    true
            );

            estimate.setRawResponse(
                    aiResponse
            );

            validateCostEstimate(
                    estimate
            );

            return estimate;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Groq AI cost estimation failed: "
                            + getErrorMessage(e),
                    e
            );
        }
    }

    // ============================================================
    // BUILD COST ESTIMATION PROMPT
    // ============================================================

    private String buildCostEstimationPrompt(
            Map<String, Object> vehicle,
            RoadService service,
            String problemNotes) {

        StringBuilder prompt =
                new StringBuilder();

        prompt.append(
                "You are RoadGuardian's automotive repair "
                        + "cost estimation AI.\n\n"
        );

        prompt.append(
                "Analyze the customer's vehicle, selected service "
                        + "and reported problem.\n"
        );

        prompt.append(
                "Provide a realistic repair/service cost estimate "
                        + "for India.\n\n"
        );

        prompt.append(
                "IMPORTANT RULES:\n"
        );

        prompt.append(
                "1. This is an estimate, not a final invoice.\n"
        );

        prompt.append(
                "2. Use Indian Rupees (INR).\n"
        );

        prompt.append(
                "3. Do not blindly copy the Admin base price.\n"
        );

        prompt.append(
                "4. Admin base price is only a reference.\n"
        );

        prompt.append(
                "5. Estimate parts and labour independently.\n"
        );

        prompt.append(
                "6. Consider vehicle type and vehicle age.\n"
        );

        prompt.append(
                "7. Consider selected service.\n"
        );

        prompt.append(
                "8. Consider customer reported symptoms.\n"
        );

        prompt.append(
                "9. Consider severity.\n"
        );

        prompt.append(
                "10. If uncertain, provide a reasonable wider range.\n"
        );

        prompt.append(
                "11. Never claim the estimate is guaranteed.\n"
        );

        prompt.append(
                "12. Understand Marathi, Hindi, English and Hinglish.\n"
        );

        prompt.append(
                "13. Return JSON only.\n\n"
        );

        // ========================================================
        // VEHICLE
        // ========================================================

        prompt.append(
                "VEHICLE INFORMATION:\n"
        );

        if (vehicle != null
                && !vehicle.isEmpty()) {

            for (Map.Entry<String, Object> entry
                    : vehicle.entrySet()) {

                prompt.append(
                                safe(entry.getKey())
                        )
                        .append(": ")
                        .append(
                                String.valueOf(
                                        entry.getValue()
                                )
                        )
                        .append("\n");
            }

        } else {

            prompt.append(
                    "Vehicle information not available.\n"
            );
        }

        // ========================================================
        // SERVICE
        // ========================================================

        prompt.append(
                "\nSELECTED SERVICE:\n"
        );

        prompt.append(
                "Service Name: "
        );

        prompt.append(
                safe(
                        service.getName()
                )
        );

        prompt.append("\n");

        prompt.append(
                "Service Category: "
        );

        prompt.append(
                safe(
                        service.getCategory()
                )
        );

        prompt.append("\n");

        prompt.append(
                "Service Description: "
        );

        prompt.append(
                safe(
                        service.getDescription()
                )
        );

        prompt.append("\n");

        prompt.append(
                "Admin Base Price Reference: INR "
        );

        prompt.append(
                service.getBasePrice()
        );

        prompt.append("\n");

        prompt.append(
                "Admin Estimated Duration: "
        );

        prompt.append(
                safe(
                        service.getEstimatedDuration()
                )
        );

        prompt.append("\n");

        // ========================================================
        // CUSTOMER PROBLEM
        // ========================================================

        prompt.append(
                "\nCUSTOMER REPORTED PROBLEM:\n"
        );

        prompt.append(
                safe(problemNotes)
        );

        prompt.append("\n\n");

        // ========================================================
        // JSON FORMAT
        // ========================================================

        prompt.append(
                "RETURN EXACTLY THIS JSON STRUCTURE:\n\n"
        );

        prompt.append(
                "{\n"
                        + "  \"minimum\": 0,\n"
                        + "  \"maximum\": 0,\n"
                        + "  \"partsMinimum\": 0,\n"
                        + "  \"partsMaximum\": 0,\n"
                        + "  \"labourMinimum\": 0,\n"
                        + "  \"labourMaximum\": 0,\n"
                        + "  \"duration\": \"60 minutes\",\n"
                        + "  \"confidence\": \"High\",\n"
                        + "  \"explanation\": \"Explain why this cost range is appropriate.\",\n"
                        + "  \"assumptions\": \"Mention important assumptions.\",\n"
                        + "  \"recommendation\": \"Recommended next action.\"\n"
                        + "}\n\n"
        );

        prompt.append(
                "FIELD RULES:\n"
        );

        prompt.append(
                "- minimum must be numeric.\n"
        );

        prompt.append(
                "- maximum must be numeric.\n"
        );

        prompt.append(
                "- partsMinimum must be numeric.\n"
        );

        prompt.append(
                "- partsMaximum must be numeric.\n"
        );

        prompt.append(
                "- labourMinimum must be numeric.\n"
        );

        prompt.append(
                "- labourMaximum must be numeric.\n"
        );

        prompt.append(
                "- maximum must be greater than or equal to minimum.\n"
        );

        prompt.append(
                "- partsMaximum must be greater than or equal to partsMinimum.\n"
        );

        prompt.append(
                "- labourMaximum must be greater than or equal to labourMinimum.\n"
        );

        prompt.append(
                "- Do not put INR or ₹ inside numeric fields.\n"
        );

        prompt.append(
                "- confidence must be High, Medium or Low.\n"
        );

        prompt.append(
                "- duration must be human-readable.\n"
        );

        prompt.append(
                "- explanation must be useful.\n"
        );

        prompt.append(
                "- assumptions must mention uncertainty when applicable.\n"
        );

        prompt.append(
                "- recommendation must tell the customer what to do next.\n"
        );

        prompt.append(
                "- Do not return markdown.\n"
        );

        prompt.append(
                "- Do not return code fences.\n"
        );

        return prompt.toString();
    }

    // ============================================================
    // BUILD DIAGNOSIS PROMPT
    // ============================================================

    private String buildDiagnosisPrompt(
            Map<String, Object> vehicle,
            String lastServiceDate,
            String userQuery,
            List<String> symptoms,
            List<RoadService> services) {

        StringBuilder prompt =
                new StringBuilder();

        prompt.append(
                "You are RoadGuardian's AI vehicle diagnosis assistant.\n\n"
        );

        prompt.append(
                "Analyze the customer's vehicle problem carefully.\n\n"
        );

        prompt.append(
                "You understand:\n"
        );

        prompt.append(
                "- English\n"
        );

        prompt.append(
                "- Marathi\n"
        );

        prompt.append(
                "- Hindi\n"
        );

        prompt.append(
                "- Hinglish\n"
        );

        prompt.append(
                "- Marathi-English mixed language\n"
        );

        prompt.append(
                "- Hindi-English mixed language\n\n"
        );

        prompt.append(
                "Give practical automotive guidance.\n"
        );

        prompt.append(
                "Do not claim certainty when physical inspection "
                        + "is required.\n"
        );

        prompt.append(
                "If the reported issue can be dangerous, clearly "
                        + "mention the safety concern.\n"
        );

        prompt.append(
                "Consider the last service date when recommending maintenance.\n"
        );

        prompt.append(
                "Do not invent vehicle information.\n"
        );

        prompt.append(
                "If information is insufficient, clearly mention what is unknown.\n\n"
        );

        // ========================================================
        // VEHICLE
        // ========================================================

        prompt.append(
                "VEHICLE INFORMATION:\n"
        );

        if (vehicle != null
                && !vehicle.isEmpty()) {

            for (Map.Entry<String, Object> entry
                    : vehicle.entrySet()) {

                prompt.append(
                                safe(entry.getKey())
                        )
                        .append(": ")
                        .append(
                                String.valueOf(
                                        entry.getValue()
                                )
                        )
                        .append("\n");
            }

        } else {

            prompt.append(
                    "Vehicle information not available.\n"
            );
        }

        // ========================================================
        // LAST SERVICE
        // ========================================================

        prompt.append(
                "\nLAST SERVICE DATE:\n"
        );

        if (isBlank(lastServiceDate)) {

            prompt.append(
                    "Not provided.\n"
            );

        } else {

            prompt.append(
                    lastServiceDate.trim()
            ).append("\n");
        }

        // ========================================================
        // CUSTOMER QUERY
        // ========================================================

        prompt.append(
                "\nCUSTOMER QUERY:\n"
        );

        if (isBlank(userQuery)) {

            prompt.append(
                    "No free-form query provided.\n"
            );

        } else {

            prompt.append(
                    userQuery.trim()
            ).append("\n");
        }

        // ========================================================
        // SELECTED SYMPTOMS
        // ========================================================

        prompt.append(
                "\nSELECTED SYMPTOMS:\n"
        );

        if (symptoms != null
                && !symptoms.isEmpty()) {

            for (String symptom : symptoms) {

                if (!isBlank(symptom)) {

                    prompt.append("- ")
                            .append(
                                    safe(symptom)
                            )
                            .append("\n");
                }
            }

        } else {

            prompt.append(
                    "No predefined symptoms selected.\n"
            );
        }

        // ========================================================
        // AVAILABLE SERVICES
        // ========================================================

        prompt.append(
                "\nAVAILABLE ROADGUARDIAN SERVICES:\n"
        );

        if (services != null
                && !services.isEmpty()) {

            for (RoadService service : services) {

                if (service == null) {
                    continue;
                }

                prompt.append("- ")
                        .append(
                                safe(
                                        service.getName()
                                )
                        )
                        .append(" | Service ID: ")
                        .append(
                                safe(
                                        service.getServiceId()
                                )
                        )
                        .append(" | Category: ")
                        .append(
                                safe(
                                        service.getCategory()
                                )
                        )
                        .append(" | Base Price: INR ")
                        .append(
                                service.getBasePrice()
                        )
                        .append(" | ")
                        .append(
                                safe(
                                        service.getDescription()
                                )
                        )
                        .append("\n");
            }

        } else {

            prompt.append(
                    "No service list available.\n"
            );
        }

        // ========================================================
        // RESPONSE FORMAT
        // ========================================================

        prompt.append(
                "\nRETURN THE ANSWER USING THESE EXACT HEADINGS:\n\n"
        );

        prompt.append(
                "DIAGNOSIS:\n"
        );

        prompt.append(
                "EXPLANATION:\n"
        );

        prompt.append(
                "SEVERITY:\n"
        );

        prompt.append(
                "RECOMMENDED_SERVICE:\n"
        );

        prompt.append(
                "POSSIBLE_CAUSES:\n"
        );

        prompt.append(
                "RECOMMENDED_ACTIONS:\n"
        );

        prompt.append(
                "THINGS_TO_AVOID:\n"
        );

        prompt.append(
                "LAST_SERVICE_RECOMMENDATION:\n\n"
        );

        prompt.append(
                "Severity should normally be one of:\n"
                        + "LOW\n"
                        + "MEDIUM\n"
                        + "HIGH\n"
                        + "CRITICAL\n\n"
        );

        prompt.append(
                "If immediate professional inspection is required, "
                        + "clearly say so.\n"
        );

        prompt.append(
                "Keep the response practical and easy to understand.\n"
        );

        prompt.append(
                "Do not return markdown tables.\n"
        );

        return prompt.toString();
    }

    // ============================================================
    // CALL GEMINI API WITH MULTIPLE MODEL FALLBACK
    // ============================================================

    /**
     * Calls Groq using multiple models.
     *
     * Flow:
     *
     * llama-3.3-70b-versatile
     *       ↓ fail temporary
     * retry same model
     *       ↓ fail
     * llama-3.1-8b-instant
     *       ↓ fail temporary
     * retry same model
     *       ↓ fail
     * openai/gpt-oss-20b
     *       ↓
     * Groq fallback model
     *
     * Only temporary/server-side errors trigger fallback.
     *
     * Permanent errors such as invalid API key are immediately
     * reported instead of unnecessarily trying every model.
     */
    private String callGroq(
            String prompt)
            throws IOException, InterruptedException {

        String apiKey =
                getApiKey();

        RuntimeException lastError = null;

        for (String model : GROQ_MODELS) {

            System.out.println(
                    "=================================================="
            );

            System.out.println(
                    "Trying Groq Model: "
                            + model
            );

            System.out.println(
                    "=================================================="
            );

            for (int attempt = 1;
                 attempt <= MAX_ATTEMPTS_PER_MODEL;
                 attempt++) {

                try {

                    String response =
                            callGroqModel(
                                    model,
                                    prompt,
                                    apiKey
                            );

                    System.out.println(
                            "Groq SUCCESS using model: "
                                    + model
                    );

                    return response;

                } catch (RuntimeException e) {

                    lastError = e;

                    int statusCode =
                            extractHttpStatusCode(
                                    e.getMessage()
                            );

                    System.out.println(
                            "Groq model "
                                    + model
                                    + " failed. Attempt "
                                    + attempt
                                    + "/"
                                    + MAX_ATTEMPTS_PER_MODEL
                                    + " | HTTP "
                                    + statusCode
                    );

                    if (!isRetryableStatus(statusCode)) {
                        throw e;
                    }

                    if (attempt < MAX_ATTEMPTS_PER_MODEL) {

                        long delay =
                                getRetryDelay(attempt);

                        System.out.println(
                                "Temporary Groq error. Retrying "
                                        + model
                                        + " after "
                                        + delay
                                        + " ms..."
                        );

                        try {

                            Thread.sleep(delay);

                        } catch (InterruptedException interrupted) {

                            Thread.currentThread().interrupt();

                            throw interrupted;
                        }
                    }
                }
            }

            System.out.println(
                    "Model "
                            + model
                            + " unavailable. Moving to next Groq model..."
            );
        }

        if (lastError != null) {
            throw lastError;
        }

        throw new RuntimeException(
                "All configured Groq models failed."
        );
    }

    // ============================================================
    // CALL ONE GROQ MODEL
    // ============================================================

    private String callGroqModel(
            String model,
            String prompt,
            String apiKey)
            throws IOException, InterruptedException {

        String requestBody =
                "{"
                        + "\"model\":\""
                        + escapeJson(model)
                        + "\","
                        + "\"messages\":["
                        + "{"
                        + "\"role\":\"user\","
                        + "\"content\":\""
                        + escapeJson(prompt)
                        + "\""
                        + "}"
                        + "],"
                        + "\"temperature\":0.2,"
                        + "\"max_completion_tokens\":4096"
                        + "}";

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        API_BASE_URL
                                )
                        )
                        .timeout(
                                Duration.ofSeconds(60)
                        )
                        .header(
                                "Content-Type",
                                "application/json"
                        )
                        .header(
                                "Authorization",
                                "Bearer " + apiKey
                        )
                        .POST(
                                HttpRequest.BodyPublishers.ofString(
                                        requestBody
                                )
                        )
                        .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        int statusCode =
                response.statusCode();

        String responseBody =
                response.body();

        if (statusCode < 200
                || statusCode >= 300) {

            throw new RuntimeException(
                    "Groq API request failed. HTTP "
                            + statusCode
                            + ": "
                            + responseBody
            );
        }

        String text =
                extractGroqText(
                        responseBody
                );

        if (isBlank(text)) {

            throw new RuntimeException(
                    "Groq returned an empty response. "
                            + "Response body: "
                            + responseBody
            );
        }

        return text.trim();
    }

    // ============================================================
    // EXTRACT GROQ CHAT COMPLETION TEXT
    // ============================================================

    private String extractGroqText(
            String json) {

        if (isBlank(json)) {
            return "";
        }

        /*
         * Groq Chat Completions response:
         *
         * {
         *   "choices": [
         *     {
         *       "message": {
         *         "role": "assistant",
         *         "content": "..."
         *       }
         *     }
         *   ]
         * }
         */

        int choicesIndex =
                json.indexOf(
                        "\"choices\""
                );

        if (choicesIndex < 0) {
            return "";
        }

        int messageIndex =
                json.indexOf(
                        "\"message\"",
                        choicesIndex
                );

        if (messageIndex < 0) {
            return "";
        }

        int contentIndex =
                json.indexOf(
                        "\"content\"",
                        messageIndex
                );

        if (contentIndex < 0) {
            return "";
        }

        int colon =
                json.indexOf(
                        ':',
                        contentIndex
                );

        if (colon < 0) {
            return "";
        }

        int quoteStart =
                findOpeningQuote(
                        json,
                        colon + 1
                );

        if (quoteStart < 0) {
            return "";
        }

        return readJsonString(
                json,
                quoteStart
        );
    }

    // ============================================================
    // RETRYABLE STATUS CHECK
    // ============================================================

    private boolean isRetryableStatus(
            int statusCode) {

        /*
         * 429 = Too Many Requests / rate limit
         * 500 = Internal Server Error
         * 502 = Bad Gateway
         * 503 = Service Unavailable
         * 504 = Gateway Timeout
         */
        return statusCode == 429
                || statusCode == 500
                || statusCode == 502
                || statusCode == 503
                || statusCode == 504;
    }

    // ============================================================
    // EXTRACT HTTP STATUS CODE
    // ============================================================

    private int extractHttpStatusCode(
            String message) {

        if (message == null) {
            return -1;
        }

        String prefix =
                "HTTP ";

        int index =
                message.indexOf(prefix);

        if (index < 0) {
            return -1;
        }

        int start =
                index + prefix.length();

        int end =
                start;

        while (end < message.length()
                && Character.isDigit(
                message.charAt(end)
        )) {

            end++;
        }

        if (end <= start) {
            return -1;
        }

        try {

            return Integer.parseInt(
                    message.substring(
                            start,
                            end
                    )
            );

        } catch (NumberFormatException e) {

            return -1;
        }
    }

    // ============================================================
    // RETRY DELAY
    // ============================================================

    private long getRetryDelay(
            int attempt) {

        if (attempt == 1) {

            return FIRST_RETRY_DELAY_MS;
        }

        return SECOND_RETRY_DELAY_MS;
    }

    // ============================================================
    // EXTRACT GEMINI TEXT
    // ============================================================

    private String extractGroqText1(
            String json) {

        if (isBlank(json)) {
            return "";
        }

        int candidatesIndex =
                json.indexOf(
                        "\"candidates\""
                );

        if (candidatesIndex < 0) {
            return "";
        }

        StringBuilder result =
                new StringBuilder();

        int searchFrom =
                candidatesIndex;

        while (searchFrom < json.length()) {

            int textIndex =
                    json.indexOf(
                            "\"text\"",
                            searchFrom
                    );

            if (textIndex < 0) {
                break;
            }

            int colon =
                    json.indexOf(
                            ':',
                            textIndex
                    );

            if (colon < 0) {
                break;
            }

            int quoteStart =
                    findOpeningQuote(
                            json,
                            colon + 1
                    );

            if (quoteStart < 0) {
                break;
            }

            String part =
                    readJsonString(
                            json,
                            quoteStart
                    );

            if (!part.isEmpty()) {

                if (result.length() > 0) {

                    result.append("\n");
                }

                result.append(part);
            }

            searchFrom =
                    quoteStart + 1;
        }

        return result.toString();
    }

    // ============================================================
    // PARSE DIAGNOSIS
    // ============================================================

    private DiagnosisResult parseDiagnosisResponse(
            String response) {

        DiagnosisResult result =
                new DiagnosisResult();

        result.setDiagnosis(
                extractSection(
                        response,
                        "DIAGNOSIS:"
                )
        );

        result.setExplanation(
                extractSection(
                        response,
                        "EXPLANATION:"
                )
        );

        result.setSeverity(
                extractSection(
                        response,
                        "SEVERITY:"
                )
        );

        result.setRecommendedService(
                extractSection(
                        response,
                        "RECOMMENDED_SERVICE:"
                )
        );

        result.setPossibleCauses(
                extractSection(
                        response,
                        "POSSIBLE_CAUSES:"
                )
        );

        result.setRecommendedActions(
                extractSection(
                        response,
                        "RECOMMENDED_ACTIONS:"
                )
        );

        result.setThingsToAvoid(
                extractSection(
                        response,
                        "THINGS_TO_AVOID:"
                )
        );

        result.setLastServiceRecommendation(
                extractSection(
                        response,
                        "LAST_SERVICE_RECOMMENDATION:"
                )
        );

        return result;
    }

    // ============================================================
    // PARSE COST ESTIMATE
    // ============================================================

    private CostEstimate parseCostEstimate(
            String json) {

        if (isBlank(json)) {
            return null;
        }

        CostEstimate estimate =
                new CostEstimate();

        estimate.setMinimum(
                jsonNumber(
                        json,
                        "minimum"
                )
        );

        estimate.setMaximum(
                jsonNumber(
                        json,
                        "maximum"
                )
        );

        estimate.setPartsMinimum(
                jsonNumber(
                        json,
                        "partsMinimum"
                )
        );

        estimate.setPartsMaximum(
                jsonNumber(
                        json,
                        "partsMaximum"
                )
        );

        estimate.setLabourMinimum(
                jsonNumber(
                        json,
                        "labourMinimum"
                )
        );

        estimate.setLabourMaximum(
                jsonNumber(
                        json,
                        "labourMaximum"
                )
        );

        estimate.setDuration(
                jsonString(
                        json,
                        "duration"
                )
        );

        estimate.setConfidence(
                jsonString(
                        json,
                        "confidence"
                )
        );

        estimate.setExplanation(
                jsonString(
                        json,
                        "explanation"
                )
        );

        estimate.setAssumptions(
                jsonString(
                        json,
                        "assumptions"
                )
        );

        estimate.setRecommendation(
                jsonString(
                        json,
                        "recommendation"
                )
        );

        return estimate;
    }

    // ============================================================
    // VALIDATE COST ESTIMATE
    // ============================================================

    private void validateCostEstimate(
            CostEstimate estimate) {

        if (estimate == null) {

            throw new IllegalStateException(
                    "Cost estimate is null."
            );
        }

        if (Double.isNaN(
                estimate.getMinimum()
        )
                || Double.isInfinite(
                estimate.getMinimum()
        )
                || estimate.getMinimum() <= 0) {

            throw new IllegalStateException(
                    "Groq returned invalid minimum cost."
            );
        }

        if (Double.isNaN(
                estimate.getMaximum()
        )
                || Double.isInfinite(
                estimate.getMaximum()
        )
                || estimate.getMaximum() <= 0) {

            throw new IllegalStateException(
                    "Groq returned invalid maximum cost."
            );
        }

        if (estimate.getMaximum()
                < estimate.getMinimum()) {

            throw new IllegalStateException(
                    "Groq returned maximum cost smaller than minimum cost."
            );
        }

        if (estimate.getPartsMinimum() < 0
                || estimate.getPartsMaximum() < 0) {

            throw new IllegalStateException(
                    "Groq returned invalid parts cost."
            );
        }

        if (estimate.getLabourMinimum() < 0
                || estimate.getLabourMaximum() < 0) {

            throw new IllegalStateException(
                    "Groq returned invalid labour cost."
            );
        }

        if (estimate.getPartsMaximum()
                < estimate.getPartsMinimum()) {

            throw new IllegalStateException(
                    "Groq returned invalid parts range."
            );
        }

        if (estimate.getLabourMaximum()
                < estimate.getLabourMinimum()) {

            throw new IllegalStateException(
                    "Groq returned invalid labour range."
            );
        }

        String confidence =
                safe(
                        estimate.getConfidence()
                );

        if (!confidence.isEmpty()
                && !confidence.equalsIgnoreCase("High")
                && !confidence.equalsIgnoreCase("Medium")
                && !confidence.equalsIgnoreCase("Low")) {

            estimate.setConfidence(
                    "Medium"
            );
        }
    }

    // ============================================================
    // EXTRACT JSON OBJECT
    // ============================================================

    private String extractJsonObject(
            String response) {

        if (response == null) {

            throw new IllegalStateException(
                    "Groq response is null."
            );
        }

        String cleaned =
                response.trim();

        // --------------------------------------------------------
        // Remove markdown code fence
        // --------------------------------------------------------

        if (cleaned.startsWith("```")) {

            int firstNewLine =
                    cleaned.indexOf('\n');

            if (firstNewLine >= 0) {

                cleaned =
                        cleaned.substring(
                                firstNewLine + 1
                        );
            }

            int lastFence =
                    cleaned.lastIndexOf(
                            "```"
                    );

            if (lastFence >= 0) {

                cleaned =
                        cleaned.substring(
                                0,
                                lastFence
                        );
            }

            cleaned =
                    cleaned.trim();
        }

        int start =
                cleaned.indexOf('{');

        int end =
                cleaned.lastIndexOf('}');

        if (start < 0
                || end < start) {

            throw new IllegalStateException(
                    "Groq did not return valid JSON. "
                            + "Response: "
                            + cleaned
            );
        }

        return cleaned.substring(
                start,
                end + 1
        );
    }

    // ============================================================
    // JSON NUMBER
    // ============================================================

    private double jsonNumber(
            String json,
            String key) {

        if (json == null
                || key == null) {

            return 0;
        }

        int keyIndex =
                json.indexOf(
                        "\"" + key + "\""
                );

        if (keyIndex < 0) {
            return 0;
        }

        int colon =
                json.indexOf(
                        ':',
                        keyIndex
                );

        if (colon < 0) {
            return 0;
        }

        int start =
                colon + 1;

        while (start < json.length()
                && Character.isWhitespace(
                json.charAt(start)
        )) {

            start++;
        }

        int end =
                start;

        while (end < json.length()) {

            char c =
                    json.charAt(end);

            if (Character.isDigit(c)
                    || c == '.'
                    || c == '-'
                    || c == '+'
                    || c == 'e'
                    || c == 'E') {

                end++;

            } else {

                break;
            }
        }

        if (end <= start) {
            return 0;
        }

        String value =
                json.substring(
                        start,
                        end
                ).trim();

        try {

            return Double.parseDouble(
                    value
            );

        } catch (NumberFormatException e) {

            return 0;
        }
    }

    // ============================================================
    // JSON STRING
    // ============================================================

    private String jsonString(
            String json,
            String key) {

        if (json == null
                || key == null) {

            return "";
        }

        int keyIndex =
                json.indexOf(
                        "\"" + key + "\""
                );

        if (keyIndex < 0) {
            return "";
        }

        int colon =
                json.indexOf(
                        ':',
                        keyIndex
                );

        if (colon < 0) {
            return "";
        }

        int quoteStart =
                findOpeningQuote(
                        json,
                        colon + 1
                );

        if (quoteStart < 0) {
            return "";
        }

        return readJsonString(
                json,
                quoteStart
        );
    }

    // ============================================================
    // FIND OPENING QUOTE
    // ============================================================

    private int findOpeningQuote(
            String json,
            int startIndex) {

        for (int i = startIndex;
             i < json.length();
             i++) {

            char c =
                    json.charAt(i);

            if (Character.isWhitespace(c)) {
                continue;
            }

            if (c == '"') {
                return i;
            }

            return -1;
        }

        return -1;
    }

    // ============================================================
    // READ JSON STRING
    // ============================================================

    private String readJsonString(
            String json,
            int openingQuote) {

        if (json == null
                || openingQuote < 0
                || openingQuote >= json.length()
                || json.charAt(openingQuote) != '"') {

            return "";
        }

        StringBuilder result =
                new StringBuilder();

        boolean escaped =
                false;

        for (int i = openingQuote + 1;
             i < json.length();
             i++) {

            char c =
                    json.charAt(i);

            if (escaped) {

                switch (c) {

                    case 'n':
                        result.append('\n');
                        break;

                    case 'r':
                        result.append('\r');
                        break;

                    case 't':
                        result.append('\t');
                        break;

                    case 'b':
                        result.append('\b');
                        break;

                    case 'f':
                        result.append('\f');
                        break;

                    case '"':
                        result.append('"');
                        break;

                    case '\\':
                        result.append('\\');
                        break;

                    case '/':
                        result.append('/');
                        break;

                    case 'u':

                        if (i + 4 < json.length()) {

                            String hex =
                                    json.substring(
                                            i + 1,
                                            i + 5
                                    );

                            try {

                                char unicodeChar =
                                        (char) Integer.parseInt(
                                                hex,
                                                16
                                        );

                                result.append(
                                        unicodeChar
                                );

                                i += 4;

                            } catch (
                                    NumberFormatException e) {

                                result.append('u');
                            }

                        } else {

                            result.append('u');
                        }

                        break;

                    default:

                        result.append(c);

                        break;
                }

                escaped = false;

            } else if (c == '\\') {

                escaped = true;

            } else if (c == '"') {

                break;

            } else {

                result.append(c);
            }
        }

        return result.toString();
    }

    // ============================================================
    // EXTRACT DIAGNOSIS SECTION
    // ============================================================

    private String extractSection(
            String text,
            String heading) {

        if (isBlank(text)
                || isBlank(heading)) {

            return "";
        }

        int start =
                text.indexOf(
                        heading
                );

        if (start < 0) {

            return "";
        }

        start +=
                heading.length();

        String[] headings = {

                "DIAGNOSIS:",
                "EXPLANATION:",
                "SEVERITY:",
                "RECOMMENDED_SERVICE:",
                "POSSIBLE_CAUSES:",
                "RECOMMENDED_ACTIONS:",
                "THINGS_TO_AVOID:",
                "LAST_SERVICE_RECOMMENDATION:"
        };

        int end =
                text.length();

        for (String nextHeading
                : headings) {

            if (nextHeading.equals(
                    heading
            )) {

                continue;
            }

            int next =
                    text.indexOf(
                            nextHeading,
                            start
                    );

            if (next >= 0
                    && next < end) {

                end = next;
            }
        }

        if (end < start) {

            return "";
        }

        return text
                .substring(
                        start,
                        end
                )
                .trim();
    }

    // ============================================================
    // ESCAPE JSON
    // ============================================================

    private String escapeJson(
            String value) {

        if (value == null) {
            return "";
        }

        StringBuilder result =
                new StringBuilder();

        for (int i = 0;
             i < value.length();
             i++) {

            char c =
                    value.charAt(i);

            switch (c) {

                case '"':
                    result.append("\\\"");
                    break;

                case '\\':
                    result.append("\\\\");
                    break;

                case '\b':
                    result.append("\\b");
                    break;

                case '\f':
                    result.append("\\f");
                    break;

                case '\n':
                    result.append("\\n");
                    break;

                case '\r':
                    result.append("\\r");
                    break;

                case '\t':
                    result.append("\\t");
                    break;

                default:

                    if (c < 0x20) {

                        result.append(
                                String.format(
                                        "\\u%04x",
                                        (int) c
                                )
                        );

                    } else {

                        result.append(c);
                    }

                    break;
            }
        }

        return result.toString();
    }

    // ============================================================
    // SAFE STRING
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

    private String getErrorMessage(
            Exception e) {

        if (e == null) {

            return "Unknown error.";
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

    // ============================================================
    // COST ESTIMATE MODEL
    // ============================================================

    public static class CostEstimate {

        private double minimum;
        private double maximum;

        private double basePrice;

        private double partsMinimum;
        private double partsMaximum;

        private double labourMinimum;
        private double labourMaximum;

        private String duration;
        private String confidence;

        private String explanation;
        private String assumptions;
        private String recommendation;

        private boolean aiGenerated;

        private String rawResponse;

        // --------------------------------------------------------
        // CONSTRUCTOR
        // --------------------------------------------------------

        public CostEstimate() {
        }

        // --------------------------------------------------------
        // MINIMUM
        // --------------------------------------------------------

        public double getMinimum() {
            return minimum;
        }

        public void setMinimum(
                double minimum) {

            this.minimum = minimum;
        }

        // --------------------------------------------------------
        // MAXIMUM
        // --------------------------------------------------------

        public double getMaximum() {
            return maximum;
        }

        public void setMaximum(
                double maximum) {

            this.maximum = maximum;
        }

        // --------------------------------------------------------
        // BASE PRICE
        // --------------------------------------------------------

        public double getBasePrice() {
            return basePrice;
        }

        public void setBasePrice(
                double basePrice) {

            this.basePrice = basePrice;
        }

        // --------------------------------------------------------
        // PARTS MINIMUM
        // --------------------------------------------------------

        public double getPartsMinimum() {
            return partsMinimum;
        }

        public void setPartsMinimum(
                double partsMinimum) {

            this.partsMinimum = partsMinimum;
        }

        // --------------------------------------------------------
        // PARTS MAXIMUM
        // --------------------------------------------------------

        public double getPartsMaximum() {
            return partsMaximum;
        }

        public void setPartsMaximum(
                double partsMaximum) {

            this.partsMaximum = partsMaximum;
        }

        // --------------------------------------------------------
        // LABOUR MINIMUM
        // --------------------------------------------------------

        public double getLabourMinimum() {
            return labourMinimum;
        }

        public void setLabourMinimum(
                double labourMinimum) {

            this.labourMinimum = labourMinimum;
        }

        // --------------------------------------------------------
        // LABOUR MAXIMUM
        // --------------------------------------------------------

        public double getLabourMaximum() {
            return labourMaximum;
        }

        public void setLabourMaximum(
                double labourMaximum) {

            this.labourMaximum = labourMaximum;
        }

        // --------------------------------------------------------
        // DURATION
        // --------------------------------------------------------

        public String getDuration() {
            return duration;
        }

        public void setDuration(
                String duration) {

            this.duration = duration;
        }

        // --------------------------------------------------------
        // CONFIDENCE
        // --------------------------------------------------------

        public String getConfidence() {
            return confidence;
        }

        public void setConfidence(
                String confidence) {

            this.confidence = confidence;
        }

        // --------------------------------------------------------
        // EXPLANATION
        // --------------------------------------------------------

        public String getExplanation() {
            return explanation;
        }

        public void setExplanation(
                String explanation) {

            this.explanation = explanation;
        }

        // --------------------------------------------------------
        // ASSUMPTIONS
        // --------------------------------------------------------

        public String getAssumptions() {
            return assumptions;
        }

        public void setAssumptions(
                String assumptions) {

            this.assumptions = assumptions;
        }

        // --------------------------------------------------------
        // RECOMMENDATION
        // --------------------------------------------------------

        public String getRecommendation() {
            return recommendation;
        }

        public void setRecommendation(
                String recommendation) {

            this.recommendation = recommendation;
        }

        // --------------------------------------------------------
        // AI GENERATED
        // --------------------------------------------------------

        public boolean isAiGenerated() {
            return aiGenerated;
        }

        public void setAiGenerated(
                boolean aiGenerated) {

            this.aiGenerated = aiGenerated;
        }

        // --------------------------------------------------------
        // RAW RESPONSE
        // --------------------------------------------------------

        public String getRawResponse() {
            return rawResponse;
        }

        public void setRawResponse(
                String rawResponse) {

            this.rawResponse = rawResponse;
        }
    }
}