package project.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class Report {

    private String reportId;
    private String name;
    private String type;
    private String generatedBy;
    private String generatedByEmail;
    private String generatedDate;
    private long generatedAt;
    private int records;
    private String status;
    private String summary;
    private Map<String, String> metrics = new LinkedHashMap<>();

    public Report() {
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGeneratedBy() {
        return generatedBy;
    }

    public void setGeneratedBy(String generatedBy) {
        this.generatedBy = generatedBy;
    }

    public String getGeneratedByEmail() {
        return generatedByEmail;
    }

    public void setGeneratedByEmail(String generatedByEmail) {
        this.generatedByEmail = generatedByEmail;
    }

    public String getGeneratedDate() {
        return generatedDate;
    }

    public void setGeneratedDate(String generatedDate) {
        this.generatedDate = generatedDate;
    }

    public long getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(long generatedAt) {
        this.generatedAt = generatedAt;
    }

    public int getRecords() {
        return records;
    }

    public void setRecords(int records) {
        this.records = records;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Map<String, String> getMetrics() {
        if (metrics == null) {
            metrics = new LinkedHashMap<>();
        }
        return metrics;
    }

    public void setMetrics(Map<String, String> metrics) {
        this.metrics = metrics == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(metrics);
    }

    public String getMetric(String key) {
        if (key == null || metrics == null) {
            return null;
        }
        return metrics.get(key);
    }
}
