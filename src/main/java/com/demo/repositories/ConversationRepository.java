package com.demo.repositories;

import com.demo.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Conversation findByBookingId(Long bookingId);
    List<Conversation> findByBookingGuestId(Long guestId);
    List<Conversation> findByBookingHostId(Long hostId);
    List<Conversation> findByBookingHostIdOrBookingGuestId(Long hostId, Long guestId);
}
