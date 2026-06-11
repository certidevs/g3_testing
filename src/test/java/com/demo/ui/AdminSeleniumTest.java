package com.demo.ui;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

public class AdminSeleniumTest extends BaseSeleniumTest {

    @Test
    void dashboardLoadsWithStatCards() {
        loginAdmin();
        driver.get(baseUrl + "dashboard");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("stat-users")));

        assertTrue(driver.findElement(By.id("stat-users")).isDisplayed());
        assertTrue(driver.findElement(By.id("stat-listings")).isDisplayed());
        assertTrue(driver.findElement(By.id("stat-bookings")).isDisplayed());
        assertTrue(driver.findElement(By.id("stat-reviews")).isDisplayed());
    }

    @Test
    void dashboardDefaultPanelIsUsers() {
        loginAdmin();
        driver.get(baseUrl + "dashboard");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("panel-users")));

        assertTrue(driver.findElement(By.id("panel-users")).getAttribute("class").contains("visible"));
        assertFalse(driver.findElement(By.id("panel-listings")).getAttribute("class").contains("visible"));
        assertFalse(driver.findElement(By.id("panel-bookings")).getAttribute("class").contains("visible"));
        assertFalse(driver.findElement(By.id("panel-reviews")).getAttribute("class").contains("visible"));
    }

    @Test
    void clickListingsStatShowsListingsPanel() {
        loginAdmin();
        driver.get(baseUrl + "dashboard");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("stat-listings")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", driver.findElement(By.id("stat-listings")));

        wait.until(ExpectedConditions.attributeContains(By.id("panel-listings"), "class", "visible"));

        assertTrue(driver.findElement(By.id("panel-listings")).getAttribute("class").contains("visible"));
        assertFalse(driver.findElement(By.id("panel-users")).getAttribute("class").contains("visible"));
        assertTrue(driver.findElement(By.id("panel-listings")).getText().contains("Loft Industrial"));
    }

    @Test
    void clickBookingsStatShowsBookingsPanel() {
        loginAdmin();
        driver.get(baseUrl + "dashboard");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("stat-bookings")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", driver.findElement(By.id("stat-bookings")));

        wait.until(ExpectedConditions.attributeContains(By.id("panel-bookings"), "class", "visible"));

        assertTrue(driver.findElement(By.id("panel-bookings")).getAttribute("class").contains("visible"));
        assertTrue(driver.findElement(By.id("panel-bookings")).getText().contains("Sonia Lopez"));
    }

    @Test
    void clickReviewsStatShowsReviewsPanel() {
        loginAdmin();
        driver.get(baseUrl + "dashboard");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("stat-reviews")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", driver.findElement(By.id("stat-reviews")));

        wait.until(ExpectedConditions.attributeContains(By.id("panel-reviews"), "class", "visible"));

        assertTrue(driver.findElement(By.id("panel-reviews")).getAttribute("class").contains("visible"));
        assertTrue(driver.findElement(By.id("panel-reviews")).getText().contains("Increíble lugar"));
    }

    @Test
    void usersTableShowsAllUsers() {
        loginAdmin();
        driver.get(baseUrl + "dashboard");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("panel-users")));

        String panelText = driver.findElement(By.id("panel-users")).getText();
        assertTrue(panelText.contains("Sonia Lopez"));
        assertTrue(panelText.contains("Alex Pro"));
        assertTrue(panelText.contains("Admin"));
    }

    @Test
    void adminUserIsProtectedNoInspectButton() {
        loginAdmin();
        driver.get(baseUrl + "dashboard");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("panel-users")));

        String panelText = driver.findElement(By.id("panel-users")).getText();
        assertTrue(panelText.contains("Protegido"));
    }

    @Test
    void activeStatCardGetsHighlightClass() {
        loginAdmin();
        driver.get(baseUrl + "dashboard");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("stat-listings")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", driver.findElement(By.id("stat-listings")));

        wait.until(ExpectedConditions.attributeContains(By.id("stat-listings"), "class", "active-stat"));

        assertTrue(driver.findElement(By.id("stat-listings")).getAttribute("class").contains("active-stat"));
        assertFalse(driver.findElement(By.id("stat-users")).getAttribute("class").contains("active-stat"));
    }

    @Test
    void listingsPanelShowsVerLink() {
        loginAdmin();
        driver.get(baseUrl + "dashboard");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("stat-listings")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", driver.findElement(By.id("stat-listings")));

        wait.until(ExpectedConditions.attributeContains(By.id("panel-listings"), "class", "visible"));

        WebElement verLink = driver.findElement(By.cssSelector("#panel-listings .btn-outline-secondary"));
        assertTrue(verLink.isDisplayed());
        assertTrue(verLink.getAttribute("href").contains("/listings/"));
    }
}