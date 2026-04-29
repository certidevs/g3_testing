package com.demo.model;

import com.demo.dto.AddOnDTO;
import com.demo.mappers.AddOnDTO;
import com.demo.mappers.AddOnMapper;
import com.demo.repositories.AddOnRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Nodes.collect;

public class AddOns {
    package com.demo.services;

import com.demo.dto.AddOnDTO;
import com.demo.mappers.AddOnMapper;
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

        // Para los clientes (solo ven lo disponible)
        public List<AddOnDTO> findAllActive() {
            return repository.findByIsActiveTrue().stream()
                    .map(mapper::toDTO)
                    .collect(Collectors.toList());
        }

        // Para el administrador (ve todo el inventario)
        public List<AddOnDTO> findAllForAdmin() {
            return repository.findAll().stream()
                    .map(mapper::toDTO)-
                    .collect(Collectors.toList());
        }
    }
}
