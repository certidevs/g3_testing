package com.demo.dto;

import com.demo.model.AddOn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AddOnDTO<AddOn> extends JpaRepository<AddOn, Long> {
    // Esta línea es la que busca tus extras en la base de datos
    List<AddOn> findByIsActiveTrue();
}