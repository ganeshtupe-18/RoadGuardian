package project.dao.admin;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteBatch;

import project.firebase.FirebaseConfig;
import project.model.Review;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ReviewDAO {

    public static final String REVIEWS_COLLECTION = "reviews";
    public static final String SERVICE_REQUESTS_COLLECTION = "serviceRequests";

    private final Firestore firestore;

    public ReviewDAO() {
        try {
            this.firestore = FirebaseConfig.getFirestore();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to initialize Admin ReviewDAO.", e);
        }
    }

    public ReviewDAO(Firestore firestore) {
        if (firestore == null) {
            throw new IllegalArgumentException("Firestore cannot be null.");
        }
        this.firestore = firestore;
    }

    public List<Review> getAllReviews() {
        List<Review> reviews = new ArrayList<>();

        try {
            QuerySnapshot snapshot = firestore
                    .collection(REVIEWS_COLLECTION)
                    .get()
                    .get();

            for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
                Review review = fromDocument(document);
                if (review != null) {
                    reviews.add(review);
                }
            }

            reviews.sort(
                    Comparator.comparingLong(this::reviewTime).reversed()
            );

        } catch (Exception e) {
            System.err.println("Admin review load error: " + e.getMessage());
            e.printStackTrace();
        }

        return reviews;
    }

    public Review getReviewById(String reviewId) {
        String id = clean(reviewId);
        if (id == null) {
            return null;
        }

        try {
            DocumentSnapshot document = firestore
                    .collection(REVIEWS_COLLECTION)
                    .document(id)
                    .get()
                    .get();

            return document.exists() ? fromDocument(document) : null;

        } catch (Exception e) {
            System.err.println("Admin review lookup error: " + e.getMessage());
            return null;
        }
    }

    public boolean deleteReview(String reviewId) {
        String id = clean(reviewId);
        if (id == null) {
            return false;
        }

        try {
            DocumentReference reviewRef = firestore
                    .collection(REVIEWS_COLLECTION)
                    .document(id);

            DocumentSnapshot reviewDocument = reviewRef.get().get();
            if (!reviewDocument.exists()) {
                return true;
            }

            String requestId = firstNonBlank(
                    reviewDocument.getString("serviceRequestId"),
                    reviewDocument.getString("requestId"),
                    id
            );

            WriteBatch batch = firestore.batch();
            batch.delete(reviewRef);

            if (requestId != null) {
                DocumentReference requestRef = firestore
                        .collection(SERVICE_REQUESTS_COLLECTION)
                        .document(requestId);

                DocumentSnapshot requestDocument = requestRef.get().get();

                if (requestDocument.exists()) {
                    batch.update(
                            requestRef,
                            "reviewId", FieldValue.delete(),
                            "customerRating", FieldValue.delete(),
                            "customerReview", FieldValue.delete(),
                            "reviewStatus", FieldValue.delete(),
                            "reviewedAt", FieldValue.delete(),
                            "updatedAt", String.valueOf(System.currentTimeMillis())
                    );
                }
            }

            batch.commit().get();
            return true;

        } catch (Exception e) {
            System.err.println("Admin review delete error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
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
                document.getString("responderName"),
                "Mechanic"
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
                document.getString("feedback"),
                ""
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
