package project.model;

public class Mechanic {

    private String mechanicId;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String specialization;
    private String status;
    private String experience;
    private String createdAt;

    // =====================================================
    // LOCATION
    // =====================================================

    private Double latitude;
    private Double longitude;
    private String locationUpdatedAt;

    public Mechanic() {
    }

    public Mechanic(
            String mechanicId,
            String name,
            String email,
            String phone,
            String address,
            String city,
            String specialization,
            String status,
            String experience,
            String createdAt
    ) {

        this.mechanicId = mechanicId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.city = city;
        this.specialization = specialization;
        this.status = status;
        this.experience = experience;
        this.createdAt = createdAt;
    }

    public String getMechanicId() {
        return mechanicId;
    }

    public void setMechanicId(String mechanicId) {
        this.mechanicId = mechanicId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    // =====================================================
    // LOCATION GETTERS / SETTERS
    // =====================================================

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getLocationUpdatedAt() {
        return locationUpdatedAt;
    }

    public void setLocationUpdatedAt(String locationUpdatedAt) {
        this.locationUpdatedAt = locationUpdatedAt;
    }

    // =====================================================
    // LOCATION VALIDATION
    // =====================================================

    public boolean hasLocation() {

        return latitude != null
                && longitude != null
                && Double.isFinite(latitude)
                && Double.isFinite(longitude)
                && latitude >= -90.0
                && latitude <= 90.0
                && longitude >= -180.0
                && longitude <= 180.0;
    }

    @Override
    public String toString() {

        return "Mechanic{" +
                "mechanicId='" + mechanicId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", city='" + city + '\'' +
                ", specialization='" + specialization + '\'' +
                ", status='" + status + '\'' +
                ", experience='" + experience + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}