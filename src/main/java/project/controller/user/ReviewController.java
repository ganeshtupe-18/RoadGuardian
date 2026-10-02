package project.controller.user;

import project.dao.user.ReviewDAO;
import project.dao.user.ServiceRequestDAO;
import project.model.Review;
import project.model.ServiceRequest;
import project.ui.user.UserSession;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ReviewController {

    private final ReviewDAO reviewDAO;
    private final ServiceRequestDAO serviceRequestDAO;

    public ReviewController() {
        this.reviewDAO = new ReviewDAO();
        this.serviceRequestDAO = new ServiceRequestDAO();
    }

    public List<ServiceRequest> getReviewableRequests() {
        String customerId = getCurrentCustomerId();
        if (customerId == null) {
            return new ArrayList<>();
        }

        List<ServiceRequest> result = new ArrayList<>();

        try {
            List<ServiceRequest> requests = serviceRequestDAO.getRequestsByCustomerId(customerId);

            if (requests != null) {
                for (ServiceRequest request : requests) {
                    if (request != null && request.isCompleted()) {
                        result.add(request);
                    }
                }
            }

            result.sort(
                    Comparator.comparingLong(this::requestTime).reversed()
            );

        } catch (Exception e) {
            System.err.println("Load reviewable requests error: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    public Review getReviewForRequest(String requestId) {
        return reviewDAO.getReviewByRequestId(requestId);
    }

    public List<Review> getMyReviews() {
        return reviewDAO.getReviewsByCustomer(
                getCurrentCustomerId(),
                normalizeIdentity(UserSession.getUserEmail())
        );
    }

    public boolean submitReview(
            ServiceRequest request,
            int rating,
            String reviewText
    ) {
        if (request == null || !request.isCompleted()) {
            return false;
        }

        if (rating < 1 || rating > 5) {
            return false;
        }

        reviewText = clean(reviewText);
        if (reviewText == null) {
            return false;
        }

        String requestId = clean(request.getRequestId());
        if (requestId == null) {
            return false;
        }

        String currentCustomerId = getCurrentCustomerId();
        String requestCustomerId = normalizeIdentity(request.getCustomerId());

        if (currentCustomerId == null) {
            return false;
        }

        if (requestCustomerId != null
                && !sameIdentity(requestCustomerId, currentCustomerId)
                && !sameIdentity(requestCustomerId, UserSession.getUserId())
                && !sameIdentity(requestCustomerId, UserSession.getUserEmail())) {
            return false;
        }

        Review existing = reviewDAO.getReviewByRequestId(requestId);
        Review review = existing == null ? new Review() : existing;

        review.setReviewId(requestId);
        review.setServiceRequestId(requestId);

        review.setCustomerId(currentCustomerId);
        review.setCustomerName(firstNonBlank(
                UserSession.getUserName(),
                request.getCustomerName(),
                "Customer"
        ));
        review.setCustomerEmail(normalizeIdentity(UserSession.getUserEmail()));

        review.setMechanicId(normalizeIdentity(request.getMechanicId()));
        review.setMechanicName(firstNonBlank(
                request.getMechanicName(),
                request.getMechanicId(),
                "Mechanic"
        ));

        review.setVehicleId(request.getVehicleId());
        review.setVehicleNumber(firstNonBlank(
                request.getVehicleNumber(),
                request.getVehicleId()
        ));
        review.setServiceType(firstNonBlank(
                request.getServiceType(),
                "Service Request"
        ));

        review.setRating(rating);
        review.setReviewText(reviewText);
        review.setStatus("Published");

        return reviewDAO.saveOrUpdateReview(review);
    }

    public String getCurrentCustomerId() {
        String email = normalizeIdentity(UserSession.getUserEmail());
        if (email != null) {
            return email;
        }

        return normalizeIdentity(UserSession.getUserId());
    }

    public String getRequestDisplay(ServiceRequest request) {
        if (request == null) {
            return "Service Request";
        }

        String service = firstNonBlank(request.getServiceType(), "Service Request");
        String vehicle = firstNonBlank(
                request.getVehicleNumber(),
                request.getVehicleId(),
                "Vehicle"
        );
        String mechanic = firstNonBlank(
                request.getMechanicName(),
                request.getMechanicId(),
                "Mechanic"
        );

        return service + " • " + vehicle + " • " + mechanic;
    }

    private long requestTime(ServiceRequest request) {
        if (request == null) {
            return 0L;
        }

        long completed = parseLong(request.getCompletedDate());
        if (completed > 0) {
            return completed;
        }

        long updated = parseLong(request.getUpdatedAt());
        if (updated > 0) {
            return updated;
        }

        return parseLong(request.getRequestDate());
    }

    private long parseLong(String value) {
        try {
            return Long.parseLong(firstNonBlank(value, "0"));
        } catch (Exception e) {
            return 0L;
        }
    }

    private boolean sameIdentity(String first, String second) {
        String a = normalizeIdentity(first);
        String b = normalizeIdentity(second);
        return a != null && b != null && a.equalsIgnoreCase(b);
    }

    private String normalizeIdentity(String value) {
        String cleaned = clean(value);
        if (cleaned == null) {
            return null;
        }
        return cleaned.contains("@") ? cleaned.toLowerCase() : cleaned;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            String cleaned = clean(value);
            if (cleaned != null) {
                return cleaned;
            }
        }
        return null;
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }
}
