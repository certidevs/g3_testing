package com.demo.model.enums;

public enum Role {
    ROLE_ADMIN, // admin global de toda la plataforma

    ROLE_HOST, // owner de casa solo puede ver las suyas y sus reservas de casa

    ROLE_USER, // cliente no puede confirmar reservar solo reservar
}
