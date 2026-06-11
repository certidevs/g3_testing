package com.demo.ui;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserSeleniumTest extends BaseSeleniumTest {

    @Test
    void viewProfileAndTabsAsHost() {
        loginHost();
        driver.get(baseUrl + "profile");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("bookings-tab")));

        assertTrue(driver.findElement(By.id("inputName")).getAttribute("value").contains("Alex Pro"));
        assertTrue(driver.findElement(By.id("inputEmail")).getAttribute("value").contains("alex@pro.com"));

        WebElement reviewsTab = driver.findElement(By.id("reviews-tab"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", reviewsTab);
        wait.until(ExpectedConditions.elementToBeClickable(reviewsTab)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("reviews-pane")));

        WebElement listingsTab = driver.findElement(By.id("listings-tab"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", listingsTab);
        wait.until(ExpectedConditions.elementToBeClickable(listingsTab)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("listings-pane")));
        assertTrue(driver.findElement(By.id("listings-pane")).getText().contains("Loft Industrial"));
    }

    @Test
    void updateProfileSuccess() {
        loginUser();
        driver.get(baseUrl + "profile");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("editToggle")));

        WebElement toggle = driver.findElement(By.id("editToggle"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", toggle);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", toggle);

        WebElement inputName = driver.findElement(By.id("inputName"));
        wait.until(ExpectedConditions.elementToBeClickable(inputName));
        inputName.clear();
        inputName.sendKeys("Sonia Lopez Editada");

        WebElement saveBtn = driver.findElement(By.cssSelector("#saveButtonContainer button"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", saveBtn);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", saveBtn);

        wait.until(ExpectedConditions.urlContains("success"));
        assertTrue(driver.getCurrentUrl().contains("success"));

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert-success")));
        assertTrue(driver.findElement(By.id("inputName")).getAttribute("value").equals("Sonia Lopez Editada"));
    }

    @Test
    void updateProfileValidationError() {
        loginUser();
        driver.get(baseUrl + "profile");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("editToggle")));

        WebElement toggle = driver.findElement(By.id("editToggle"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", toggle);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", toggle);

        WebElement inputEmail = driver.findElement(By.id("inputEmail"));
        wait.until(ExpectedConditions.elementToBeClickable(inputEmail));
        inputEmail.clear();
        inputEmail.sendKeys("admin@openhouse.com");

        ((JavascriptExecutor) driver).executeScript("document.querySelector('form').setAttribute('novalidate', 'true');");

        WebElement saveBtn = driver.findElement(By.cssSelector("#saveButtonContainer button"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", saveBtn);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", saveBtn);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert-danger")));
        assertTrue(driver.findElement(By.className("alert-danger")).getText().contains("ya está registrado"));
    }
}