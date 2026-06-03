package com.demo.repositories;

import com.demo.model.AmenityLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AmenityLineRepository extends JpaRepository<AmenityLine, Long> {
    List<AmenityLine> findByListingId(Long listingId);
}