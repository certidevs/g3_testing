package com.demo.ui;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthSeleniumTest extends BaseSeleniumTest {

    @Test
    void loginSuccess() {
        driver.get(baseUrl + "login");
        driver.findElement(By.name("username")).sendKeys("sonia@mail.com");
        driver.findElement(By.name("password")).sendKeys("1234");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.urlToBe(baseUrl + "listings"));

        assertEquals(baseUrl + "listings", driver.getCurrentUrl());
    }

    @Test
    void loginFailure() {
        driver.get(baseUrl + "login");
        driver.findElement(By.name("username")).sendKeys("incorrecto@openhouse.com");
        driver.findElement(By.name("password")).sendKeys("fake");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(driver -> driver.findElement(By.className("alert-danger")).isDisplayed());
        assertTrue(driver.findElement(By.className("alert-danger")).getText().contains("incorrectos"));
    }

    @Test
    void register() {
        driver.get(baseUrl + "register");
        driver.findElement(By.id("name")).sendKeys("Test User");
        driver.findElement(By.id("username")).sendKeys("testopenhouse");
        driver.findElement(By.id("email")).sendKeys("testopenhouse@gmail.com");
        driver.findElement(By.id("password")).sendKeys("password123");
        driver.findElement(By.id("passwordConfirmed")).sendKeys("password123");

        var checkbox = driver.findElement(By.cssSelector("input[type='checkbox']"));
        if (!checkbox.isSelected()) {
            checkbox.click();
        }

        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(driver -> driver.getCurrentUrl().contains(baseUrl + "login"));
        assertTrue(driver.getCurrentUrl().contains(baseUrl + "login"));
    }
}
