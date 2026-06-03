package com.demo.repositories;

import com.demo.model.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AmenityRepository extends JpaRepository<Amenity, Long> {

    List<Amenity> findByName(String name);
    List<Amenity> findByNameContainingIgnoreCase(String name);

}