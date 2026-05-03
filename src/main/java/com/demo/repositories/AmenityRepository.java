package com.demo.repositories;

import com.demo.model.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AmenityRepository extends JpaRepository<Amenity, Long> {

    List<Amenity> findByName(String name);
    List<Amenity> findByNameContainingIgnoreCase(String name);

    @Query("SELECT a FROM Amenity a WHERE a.name = :name AND a.listing.id = :listingId")
    List<Amenity> findByNameAndListing_Id(@Param("name") String name, @Param("listingId") Long listingId);


    List<Amenity> findByListing_Id(Long id);

    // List<Amenity> findByNameAndListing_Id(String wifi, Long id);
}