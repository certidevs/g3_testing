package com.demo.repositories;

import com.demo.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByVerifiedTrue();
    List<Review> findByRatingBetween(int minRating, int maxRating);
    List<Review> findByVerifiedTrueAndRatingBetween(int minRating, int maxRating);
    List<Review> findByCreationDateBetween(LocalDate startDate, LocalDate endDate);
    List<Review> findByRating(int rating);
    List<Review>findByVerifiedFalse();
    List<Review>findByBooking_ListingId(Long listingId);
}