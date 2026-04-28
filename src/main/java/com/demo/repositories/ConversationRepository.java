package com.demo.repositories;

import com.demo.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Conversation findByBookingId(Long bookingId);
}
