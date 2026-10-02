package project.model;

import java.util.ArrayList;
import java.util.List;

public class DiagnosisResult {

    private String diagnosisId;
    private String diagnosis;
    private String explanation;
    private String recommendedService;
    private String matchedServiceId;
    private String severity;
    private double basePrice;
    private String estimatedDuration;
    private List<String> symptoms = new ArrayList<>();

    private String aiResponse;
    private String possibleCauses;
    private String recommendedActions;
    private String thingsToAvoid;
    private String lastServiceRecommendation;

    public DiagnosisResult() {
    }

    public String getDiagnosisId() {
        return diagnosisId;
    }

    public void setDiagnosisId(String diagnosisId) {
        this.diagnosisId = diagnosisId;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getRecommendedService() {
        return recommendedService;
    }

    public void setRecommendedService(String recommendedService) {
        this.recommendedService = recommendedService;
    }

    public String getMatchedServiceId() {
        return matchedServiceId;
    }

    public void setMatchedServiceId(String matchedServiceId) {
        this.matchedServiceId = matchedServiceId;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }

    public String getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(String estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public List<String> getSymptoms() {
        return symptoms == null
                ? new ArrayList<>()
                : new ArrayList<>(symptoms);
    }

    public void setSymptoms(List<String> symptoms) {
        this.symptoms = symptoms == null
                ? new ArrayList<>()
                : new ArrayList<>(symptoms);
    }

    public boolean hasCatalogPrice() {
        return basePrice > 0;
    }

    public String getPriceDisplay() {
        if (basePrice <= 0) {
            return "Price after inspection";
        }

        return "Starting from ₹" + format(basePrice);
    }

    private String format(double value) {
        if (Math.floor(value) == value) {
            return String.format(
                    java.util.Locale.US,
                    "%,d",
                    (long) value
            );
        }

        return String.format(
                java.util.Locale.US,
                "%,.2f",
                value
        );
    }

    // =========================
    // AI / Groq Response Fields
    // =========================

    public String getAiResponse() {
        return aiResponse;
    }

    public void setAiResponse(String aiResponse) {
        this.aiResponse = aiResponse;
    }

    public String getPossibleCauses() {
        return possibleCauses;
    }

    public void setPossibleCauses(String possibleCauses) {
        this.possibleCauses = possibleCauses;
    }

    public String getRecommendedActions() {
        return recommendedActions;
    }

    public void setRecommendedActions(String recommendedActions) {
        this.recommendedActions = recommendedActions;
    }

    public String getThingsToAvoid() {
        return thingsToAvoid;
    }

    public void setThingsToAvoid(String thingsToAvoid) {
        this.thingsToAvoid = thingsToAvoid;
    }

    public String getLastServiceRecommendation() {
        return lastServiceRecommendation;
    }

    public void setLastServiceRecommendation(
            String lastServiceRecommendation) {

        this.lastServiceRecommendation = lastServiceRecommendation;
    }
}