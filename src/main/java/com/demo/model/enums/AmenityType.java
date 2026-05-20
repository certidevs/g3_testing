package com.demo.model.enums;

public enum AmenityType {

    WIFI("Wifi", "fa-wifi"),
    FIBRA_OPTICA("Fibra Óptica", "fa-globe"),
    CALEFACCION("Calefacción", "fa-fire"),
    AIRE_ACONDICIONADO("Aire Acondicionado", "fa-snowflake"),
    PISCINA("Piscina", "fa-water-ladder"),
    PARKING("Parking", "fa-square-parking"),
    TELEVISION("Televisión", "fa-tv"),
    LAVADORA("Lavadora", "fa-soap"),
    SECADORA("Secadora", "fa-wind"),
    COCINA("Cocina", "fa-kitchen-set"),
    JARDIN("Jardín", "fa-tree"),
    TERRAZA("Terraza", "fa-sun"),
    ASCENSOR("Ascensor", "fa-elevator");

    private final String label;
    private final String icon;

    AmenityType(String label, String icon) {
        this.label = label;
        this.icon = icon;
    }

    public String getLabel() {
        return label;
    }

    public String getIcon() {
        return icon;
    }
}
