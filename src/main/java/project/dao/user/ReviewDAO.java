package project.dao.user;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteBatch;

import project.firebase.FirebaseConfig;
import project.model.Review;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewDAO {

    public static final String REVIEWS_COLLECTION = "reviews";
    public static final String SERVICE_REQUESTS_COLLECTION = "serviceRequests";

    private final Firestore firestore;

    public ReviewDAO() {
        try {
            this.firestore = FirebaseConfig.getFirestore();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to initialize ReviewDAO.", e);
        }
    }

    public ReviewDAO(Firestore firestore) {
        if (firestore == null) {
            throw new IllegalArgumentException("Firestore cannot be null.");
        }
        this.firestore = firestore;
    }

    public boolean saveOrUpdateReview(Review review) {
        if (review == null) {
            return false;
        }

        String requestId = clean(review.getServiceRequestId());
        if (requestId == null) {
            return false;
        }

        int rating = review.getRating();
        if (rating < 1 || rating > 5) {
            return false;
        }

        String reviewText = clean(review.getReviewText());
        if (reviewText == null) {
            return false;
        }

        try {
            DocumentReference reviewRef = firestore
                    .collection(REVIEWS_COLLECTION)
                    .document(requestId);

            DocumentSnapshot existing = reviewRef.get().get();

            String now = String.valueOf(System.currentTimeMillis());
            String createdAt = existing.exists()
                    ? firstNonBlank(existing.getString("createdAt"), now)
                    : now;

            review.setReviewId(requestId);
            review.setServiceRequestId(requestId);
            review.setStatus("Published");
            review.setCreatedAt(createdAt);
            review.setUpdatedAt(now);

            Map<String, Object> reviewData = toMap(review);

            Map<String, Object> servicePatch = new HashMap<>();
            servicePatch.put("reviewId", requestId);
            servicePatch.put("customerRating", rating);
            servicePatch.put("customerReview", reviewText);
            servicePatch.put("reviewStatus", "Published");
            servicePatch.put("reviewedAt", now);
            servicePatch.put("updatedAt", now);

            WriteBatch batch = firestore.batch();
            batch.set(reviewRef, reviewData, SetOptions.merge());
            batch.set(
                    firestore.collection(SERVICE_REQUESTS_COLLECTION).document(requestId),
                    servicePatch,
                    SetOptions.merge()
            );
            batch.commit().get();

            return true;

        } catch (Exception e) {
            System.err.println("Save review error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Review getReviewByRequestId(String serviceRequestId) {
        String requestId = clean(serviceRequestId);
        if (requestId == null) {
            return null;
        }

        try {
            DocumentSnapshot document = firestore
                    .collection(REVIEWS_COLLECTION)
                    .document(requestId)
                    .get()
                    .get();

            return document.exists() ? fromDocument(document) : null;

        } catch (Exception e) {
            System.err.println("Get review error: " + e.getMessage());
            return null;
        }
    }

    public List<Review> getReviewsByCustomer(String customerId, String customerEmail) {
        List<Review> reviews = new ArrayList<>();

        String normalizedId = normalizeIdentity(customerId);
        String normalizedEmail = normalizeIdentity(customerEmail);

        try {
            QuerySnapshot snapshot = firestore
                    .collection(REVIEWS_COLLECTION)
                    .get()
                    .get();

            for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
                Review review = fromDocument(document);
                if (review == null) {
                    continue;
                }

                if (matchesIdentity(review, normalizedId, normalizedEmail)) {
                    reviews.add(review);
                }
            }

            reviews.sort(
                    Comparator.comparingLong(this::reviewTime).reversed()
            );

        } catch (Exception e) {
            System.err.println("Load customer reviews error: " + e.getMessage());
            e.printStackTrace();
        }

        return reviews;
    }

    private boolean matchesIdentity(
            Review review,
            String customerId,
            String customerEmail
    ) {
        String reviewCustomerId = normalizeIdentity(review.getCustomerId());
        String reviewEmail = normalizeIdentity(review.getCustomerEmail());

        return same(reviewCustomerId, customerId)
                || same(reviewCustomerId, customerEmail)
                || same(reviewEmail, customerId)
                || same(reviewEmail, customerEmail);
    }

    private Map<String, Object> toMap(Review review) {
        Map<String, Object> data = new HashMap<>();

        data.put("reviewId", clean(review.getReviewId()));
        data.put("serviceRequestId", clean(review.getServiceRequestId()));

        data.put("customerId", clean(review.getCustomerId()));
        data.put("customerName", clean(review.getCustomerName()));
        data.put("customerEmail", normalizeIdentity(review.getCustomerEmail()));

        data.put("mechanicId", normalizeIdentity(review.getMechanicId()));
        data.put("mechanicName", clean(review.getMechanicName()));

        data.put("vehicleId", clean(review.getVehicleId()));
        data.put("vehicleNumber", clean(review.getVehicleNumber()));
        data.put("serviceType", clean(review.getServiceType()));

        data.put("rating", review.getRating());
        data.put("reviewText", clean(review.getReviewText()));
        data.put("status", firstNonBlank(review.getStatus(), "Published"));

        data.put("createdAt", clean(review.getCreatedAt()));
        data.put("updatedAt", clean(review.getUpdatedAt()));

        return data;
    }

    private Review fromDocument(DocumentSnapshot document) {
        if (document == null || !document.exists()) {
            return null;
        }

        Review review = new Review();

        review.setReviewId(firstNonBlank(
                document.getString("reviewId"),
                document.getId()
        ));

        review.setServiceRequestId(firstNonBlank(
                document.getString("serviceRequestId"),
                document.getString("requestId"),
                document.getId()
        ));

        review.setCustomerId(firstNonBlank(
                document.getString("customerId"),
                document.getString("userId")
        ));
        review.setCustomerName(firstNonBlank(
                document.getString("customerName"),
                document.getString("userName"),
                "Customer"
        ));
        review.setCustomerEmail(firstNonBlank(
                document.getString("customerEmail"),
                document.getString("userEmail")
        ));

        review.setMechanicId(document.getString("mechanicId"));
        review.setMechanicName(firstNonBlank(
                document.getString("mechanicName"),
                document.getString("responderName")
        ));

        review.setVehicleId(document.getString("vehicleId"));
        review.setVehicleNumber(firstNonBlank(
                document.getString("vehicleNumber"),
                document.getString("registrationNumber")
        ));
        review.setServiceType(firstNonBlank(
                document.getString("serviceType"),
                document.getString("service"),
                "Service Request"
        ));

        review.setRating(readRating(document.get("rating")));
        review.setReviewText(firstNonBlank(
                document.getString("reviewText"),
                document.getString("review"),
                document.getString("feedback")
        ));
        review.setStatus(firstNonBlank(
                document.getString("status"),
                "Published"
        ));
        review.setCreatedAt(firstNonBlank(
                document.getString("createdAt"),
                document.getString("date")
        ));
        review.setUpdatedAt(firstNonBlank(
                document.getString("updatedAt"),
                review.getCreatedAt()
        ));

        return review;
    }

    private int readRating(Object value) {
        if (value == null) {
            return 0;
        }

        if (value instanceof Number number) {
            return number.intValue();
        }

        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private long reviewTime(Review review) {
        if (review == null) {
            return 0L;
        }

        long updated = parseLong(review.getUpdatedAt());
        if (updated > 0) {
            return updated;
        }

        return parseLong(review.getCreatedAt());
    }

    private long parseLong(String value) {
        try {
            return Long.parseLong(firstNonBlank(value, "0"));
        } catch (Exception e) {
            return 0L;
        }
    }

    private String normalizeIdentity(String value) {
        String cleaned = clean(value);
        if (cleaned == null) {
            return null;
        }
        return cleaned.contains("@") ? cleaned.toLowerCase() : cleaned;
    }

    private boolean same(String first, String second) {
        return first != null && second != null && first.equalsIgnoreCase(second);
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
