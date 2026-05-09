package com.demo.repositories;

import com.demo.model.Booking;
import com.demo.model.Listing;
import com.demo.model.Review;
import com.demo.model.User;
import com.demo.model.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByListingId(Long listingId);
    List<Booking> findByListingIdAndStatus(Long listingId, BookingStatus status);
    @Query("SELECT b.listing FROM Booking b WHERE b.id = :bookingId")
    Listing findListingByBookingId(@Param("bookingId") Long bookingId);
    @Query("SELECT r FROM Review r WHERE r.booking.id = :bookingId")
    Optional<Review> findReviewByBookingId(@Param("bookingId") Long bookingId);
    @Query("SELECT b.listing.owner FROM Booking b WHERE b.id = :bookingId")
    Optional<User> findOwnerByBookingId(@Param("bookingId") Long bookingId);






}