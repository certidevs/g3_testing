package com.demo.ui;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AmenitySeleniumTest extends BaseSeleniumTest {

    @Test
    void listAmenitiesLoadsSuccessfully() {
        loginAdmin();
        driver.get(baseUrl + "amenity");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("card")));

        List<WebElement> amenityCards = driver.findElements(By.className("card"));
        assertFalse(amenityCards.isEmpty());

        String bodyText = driver.findElement(By.tagName("body")).getText();
        assertTrue(bodyText.contains("Fibra Optica"));
        assertTrue(bodyText.contains("Calefaccion"));
    }

    @Test
    void viewAmenityDetails() {
        loginAdmin();
        driver.get(baseUrl + "amenity");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Ver Detalles")));
        driver.findElement(By.linkText("Ver Detalles")).click();

        wait.until(ExpectedConditions.urlContains("/amenity/"));

    }

    @Test
    void accessDeniedForNonAdminUsers() {
        loginUser();
        driver.get(baseUrl + "amenity");

        wait.until(ExpectedConditions.urlContains("/amenity"));
    }
}