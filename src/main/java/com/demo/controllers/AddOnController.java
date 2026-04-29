package com.demo.controllers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AddOnController {
    package com.demo.dto;

import lombok.*;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class AddOnDTO {
        private Long id;
        private String name;
        private String description;
        private Double price;
    }
}
