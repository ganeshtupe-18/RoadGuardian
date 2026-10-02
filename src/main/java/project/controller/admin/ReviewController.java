package project.controller.admin;

import project.dao.admin.ReviewDAO;
import project.model.Review;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ReviewController {

    private final ReviewDAO reviewDAO;

    public ReviewController() {
        this.reviewDAO = new ReviewDAO();
    }

    public List<Review> getAllReviews() {
        List<Review> reviews = reviewDAO.getAllReviews();
        return reviews == null ? new ArrayList<>() : reviews;
    }

    public Review getReviewById(String reviewId) {
        return reviewDAO.getReviewById(reviewId);
    }

    public boolean deleteReview(String reviewId) {
        return reviewDAO.deleteReview(reviewId);
    }

    public List<Review> filterReviews(
            List<Review> source,
            String search,
            String ratingFilter,
            String sort
    ) {
        List<Review> filtered = new ArrayList<>();
        String query = clean(search);
        if (query != null) {
            query = query.toLowerCase();
        }

        Integer requiredRating = parseRatingFilter(ratingFilter);

        if (source != null) {
            for (Review review : source) {
                if (review == null) {
                    continue;
                }

                if (requiredRating != null && review.getRating() != requiredRating) {
                    continue;
                }

                if (query != null && !matchesSearch(review, query)) {
                    continue;
                }

                filtered.add(review);
            }
        }

        sortReviews(filtered, sort);
        return filtered;
    }

    public int getFiveStarCount(List<Review> reviews) {
        int count = 0;
        if (reviews != null) {
            for (Review review : reviews) {
                if (review != null && review.getRating() == 5) {
                    count++;
                }
            }
        }
        return count;
    }

    public double getAverageRating(List<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }

        int count = 0;
        int total = 0;

        for (Review review : reviews) {
            if (review == null || review.getRating() <= 0) {
                continue;
            }
            total += review.getRating();
            count++;
        }

        return count == 0 ? 0.0 : (double) total / count;
    }

    private boolean matchesSearch(Review review, String query) {
        return contains(review.getReviewId(), query)
                || contains(review.getServiceRequestId(), query)
                || contains(review.getCustomerId(), query)
                || contains(review.getCustomerName(), query)
                || contains(review.getCustomerEmail(), query)
                || contains(review.getMechanicId(), query)
                || contains(review.getMechanicName(), query)
                || contains(review.getVehicleId(), query)
                || contains(review.getVehicleNumber(), query)
                || contains(review.getServiceType(), query)
                || contains(review.getReviewText(), query)
                || contains(review.getStatus(), query);
    }

    private void sortReviews(List<Review> reviews, String sort) {
        String selected = clean(sort);

        if ("Highest Rating".equals(selected)) {
            reviews.sort(Comparator.comparingInt(Review::getRating).reversed());
            return;
        }

        if ("Lowest Rating".equals(selected)) {
            reviews.sort(Comparator.comparingInt(Review::getRating));
            return;
        }

        if ("Customer A-Z".equals(selected)) {
            reviews.sort(
                    Comparator.comparing(
                            review -> firstNonBlank(review.getCustomerName(), "").toLowerCase()
                    )
            );
            return;
        }

        if ("Oldest First".equals(selected)) {
            reviews.sort(Comparator.comparingLong(this::reviewTime));
            return;
        }

        reviews.sort(Comparator.comparingLong(this::reviewTime).reversed());
    }

    private Integer parseRatingFilter(String value) {
        String cleaned = clean(value);
        if (cleaned == null || cleaned.equalsIgnoreCase("All Ratings")) {
            return null;
        }

        for (int i = 1; i <= 5; i++) {
            if (cleaned.startsWith(String.valueOf(i))) {
                return i;
            }
        }

        return null;
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

    private boolean contains(String value, String query) {
        return value != null
                && query != null
                && value.toLowerCase().contains(query);
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
