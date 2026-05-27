package com.demo.repositories;

import com.demo.model.Booking;
import com.demo.model.Listing;
import com.demo.model.Review;
import com.demo.model.enums.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    List<Review> findByBookingId(Long bookingId);
    @Query("SELECT r.booking FROM Review r WHERE r.id = :reviewId")
    Booking findBookingByReviewId(@Param("reviewId") Long reviewId);
    @Query("SELECT r FROM Review r JOIN r.booking b WHERE b.guest.id = :userId")
    List<Review> findAllByUserId(@Param("userId") Long userId);

    // ── Ciudades con reseñas para el selector ────────────────────────────────
    @Query("SELECT DISTINCT l.city FROM Review r JOIN r.booking b JOIN b.listing l ORDER BY l.city ASC")
    List<City> findDistinctCities();

    // ── Sin filtros, solo orden ──────────────────────────────────────────────
    List<Review> findAllByOrderByCreationDateDesc();
    List<Review> findAllByOrderByCreationDateAsc();

    // ── Solo por rating ──────────────────────────────────────────────────────
    @Query("SELECT r FROM Review r WHERE r.rating = :rating ORDER BY r.creationDate DESC")
    List<Review> findByRatingOrderByDateDesc(@Param("rating") int rating);

    @Query("SELECT r FROM Review r WHERE r.rating = :rating ORDER BY r.creationDate ASC")
    List<Review> findByRatingOrderByDateAsc(@Param("rating") int rating);

    // ── Solo por ciudad ──────────────────────────────────────────────────────
    @Query("SELECT r FROM Review r JOIN r.booking b JOIN b.listing l WHERE l.city = :city ORDER BY r.creationDate DESC")
    List<Review> findByCityOrderByDateDesc(@Param("city") City city);

    @Query("SELECT r FROM Review r JOIN r.booking b JOIN b.listing l WHERE l.city = :city ORDER BY r.creationDate ASC")
    List<Review> findByCityOrderByDateAsc(@Param("city") City city);

    // ── Ciudad + rating ──────────────────────────────────────────────────────
    @Query("SELECT r FROM Review r JOIN r.booking b JOIN b.listing l WHERE l.city = :city AND r.rating = :rating ORDER BY r.creationDate DESC")
    List<Review> findByCityAndRatingOrderByDateDesc(@Param("city") City city, @Param("rating") int rating);

    @Query("SELECT r FROM Review r JOIN r.booking b JOIN b.listing l WHERE l.city = :city AND r.rating = :rating ORDER BY r.creationDate ASC")
    List<Review> findByCityAndRatingOrderByDateAsc(@Param("city") City city, @Param("rating") int rating);


}