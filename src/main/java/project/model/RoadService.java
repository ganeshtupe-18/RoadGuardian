package project.model;

public class RoadService {

    private String serviceId;
    private String name;
    private String description;
    private String category;
    private double basePrice;
    private String estimatedDuration;
    private String status;
    private String createdAt;
    private String updatedAt;
    
    public RoadService() {
    }
    public RoadService(
            String serviceId,
            String name,
            String description,
            String category,
            double basePrice,
            String estimatedDuration,
            String status
    ) {
        this.serviceId = serviceId;
        this.name = name;
        this.description = description;
        this.category = category;
        this.basePrice = basePrice;
        this.estimatedDuration = estimatedDuration;
        this.status = status;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isActive() {
        return status != null && status.equalsIgnoreCase("Active");
    }

    public String getPriceDisplay() {
        if (basePrice <= 0) {
            return "Price on inspection";
        }
        if (Math.floor(basePrice) == basePrice) {
            return "₹" + (long) basePrice;
        }
        return String.format("₹%.2f", basePrice);
    }
}
