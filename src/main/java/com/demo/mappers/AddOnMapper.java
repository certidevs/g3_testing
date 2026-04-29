package com.demo.mappers;

import com.demo.dto.AddOnDTO;
import com.demo.model.AddOn;
import org.springframework.stereotype.Component;

@Component
public class AddOnMapper {
    public AddOnDTO toDTO(AddOn entity) {
        if (entity == null) return null;
        return AddOnDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .build();
    }
}