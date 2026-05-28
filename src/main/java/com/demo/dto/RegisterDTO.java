package com.demo.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String name;
    private String username;
    private String email;
    private String password;
    private String passwordConfirmed;
    private Boolean acceptRGPD;
}
