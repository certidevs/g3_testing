package com.demo.repositories;

import com.demo.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    //List<Message> findByIsReadFalse();
    //List<Message> findBySentAtBetween(String startDate, String endDate);
    //List<Message> findBySentAtOrderAsc();
    //List<Message> findBySentAtOrderDesc();
}
