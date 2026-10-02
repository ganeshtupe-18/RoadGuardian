package project.model;

public class Vehicle {

    private String vehicleId;
    private String customerId;
    private String ownerName;
    private String vehicleNumber;
    private String brand;
    private String model;
    private String vehicleType;
    private String fuelType;
    private String year;
    private String status;
    private String createdAt;

    // Firestore साठी
    public Vehicle() {
    }

    public Vehicle(
            String vehicleId,
            String customerId,
            String ownerName,
            String vehicleNumber,
            String brand,
            String model,
            String vehicleType,
            String fuelType,
            String year,
            String status,
            String createdAt
    ) {

        this.vehicleId = vehicleId;
        this.customerId = customerId;
        this.ownerName = ownerName;
        this.vehicleNumber = vehicleNumber;
        this.brand = brand;
        this.model = model;
        this.vehicleType = vehicleType;
        this.fuelType = fuelType;
        this.year = year;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
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

    @Override
    public String toString() {

        return "Vehicle{" +
                "vehicleId='" + vehicleId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", ownerName='" + ownerName + '\'' +
                ", vehicleNumber='" + vehicleNumber + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", vehicleType='" + vehicleType + '\'' +
                ", fuelType='" + fuelType + '\'' +
                ", year='" + year + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}