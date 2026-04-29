package com.demo.repositories;

import com.demo.model.Addon;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddonRepository extends JpaRepository<Addon, Long> {
    List<Addon> findByPrice(Double price);

    List<Addon> findByTitle(String title);
    List<Addon> findByDescription(String description);

    @NonNull
    List<Addon> findAll();
}