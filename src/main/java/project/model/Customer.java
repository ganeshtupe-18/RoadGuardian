package project.model;

public class Customer {

    private String customerId;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String profileImageUrl;
    private String status;
    private String createdAt;

    // =====================================================
    // LOCATION
    // =====================================================

    private Double latitude;
    private Double longitude;
    private String locationUpdatedAt;

    public Customer() {
    }

    public Customer(
            String customerId,
            String name,
            String email,
            String phone,
            String address,
            String city,
            String profileImageUrl,
            String status,
            String createdAt
    ) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.city = city;
        this.profileImageUrl = profileImageUrl;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
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

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
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
}
