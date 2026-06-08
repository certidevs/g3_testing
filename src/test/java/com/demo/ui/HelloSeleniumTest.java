package com.demo.ui;


import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
@Disabled
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HelloSeleniumTest {

    @LocalServerPort
    int port;

    @Test
    void listingList(){

        WebDriver driver = new ChromeDriver();
        driver.get("http://localhost:"+port+"/listings");
        driver.manage().window().maximize();


        driver.quit();

    }
}
