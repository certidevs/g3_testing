package com.demo.repositories;

import com.demo.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Conversation findByBookingId(Long bookingId);
    List<Conversation> findByBookingGuestId(Long guestId);
    @Query("SELECT c FROM Conversation c WHERE c.booking.listing.owner.id = :hostId")
    List<Conversation> findByBookingHostId(@Param("hostId") Long hostId);

    @Query("SELECT c FROM Conversation c WHERE c.booking.listing.owner.id = :hostId OR c.booking.guest.id = :guestId")
    List<Conversation> findByBookingHostIdOrBookingGuestId(@Param("hostId") Long hostId, @Param("guestId") Long guestId);
}
