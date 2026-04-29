package com.demo.services;

import com.demo.dto.AddOnDTO;
import com.demo.mappers.AddOnMapper;
import com.demo.model.AddOn;
import com.demo.repositories.AddOnRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddOnService {
    private final AddOnRepository repository;
    private final AddOnMapper mapper;

    public AddOnService(AddOnRepository repository, AddOnMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<com.demo.services.AddOnDTO> findAllActive() {
        return repository.findByIsActiveTrue().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }
}

//package com.demo.services;
//
//import com.demo.mappers.AddOnMapper;
//
//public class AddOnServices {
//
//    private final AddOnMapper mapper;
//
//    public AddOnServices(AddOnMapper mapper) {
//        this.mapper = mapper;
//    }
//
//    // Métodos de servicio irán aquí
//} cual seria mas sencillo?