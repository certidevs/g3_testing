package com.demo.ui;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ConversationSeleniumTest extends BaseSeleniumTest {

    @Test
    void searchConversation() {
        loginUser();
        driver.get(baseUrl + "conversation");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("search")));
        driver.findElement(By.name("search")).sendKeys("Loft");
        driver.findElement(By.cssSelector("main form input[name='search'] + button")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("card-conversation")));
        assertTrue(driver.findElement(By.className("card-conversation")).getText().contains("Loft Industrial"));
    }

    @Test
    void searchConversationNoResults() {
        loginUser();
        driver.get(baseUrl + "conversation");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("search")));
        driver.findElement(By.name("search")).sendKeys("xxxxxxxxxinexistente");
        driver.findElement(By.cssSelector("main form input[name='search'] + button")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("main .text-center")));
        assertTrue(driver.findElements(By.className("card-conversation")).isEmpty());
    }

    @Test
    void sendMessageInChatRoom() {
        loginUser();
        driver.get(baseUrl + "conversation/" + conversation.getId());

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("chat-container")));

        int initialCount = driver.findElements(By.className("message-row")).size();

        String testMessage = "Mensaje nuevo desde Selenium";
        driver.findElement(By.name("content")).sendKeys(testMessage);
        driver.findElement(By.cssSelector("form[action*='/send'] button[type='submit']")).click();

        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.className("message-row"), initialCount));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("chat-container"), testMessage));
        assertTrue(driver.findElement(By.id("chat-container")).getText().contains(testMessage));
    }

    private void clickBtnOptionWithJS(WebElement btnOption) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", btnOption);
        ((JavascriptExecutor) driver).executeScript("arguments[0].style.visibility='visible'; arguments[0].style.display='block'; arguments[0].style.opacity='1';", btnOption);
        wait.until(ExpectedConditions.elementToBeClickable(btnOption)).click();
    }

    @Test
    void hostMessageIsNotEditable() {
        loginUser();
        driver.get(baseUrl + "conversation/" + conversation.getId());

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("chat-container")));

        WebElement hostMessageDiv = driver.findElement(By.id("content-" + messageHost.getId()));
        WebElement hostWrapper = hostMessageDiv.findElement(By.xpath("./ancestor::div[contains(@class,'mw-75')]"));

        List<WebElement> options = hostWrapper.findElements(By.className("message-options"));
        assertTrue(options.isEmpty());
    }
    @Test
    void editMessageViaModal() {
        loginUser();
        driver.get(baseUrl + "conversation/" + conversation.getId());

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("chat-container")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("content-" + messageGuest.getId())));

        WebElement messageDiv = driver.findElement(By.id("content-" + messageGuest.getId()));
        WebElement messageWrapper = messageDiv.findElement(By.xpath("./ancestor::div[contains(@class,'mw-75')]"));
//        WebElement btnOption = messageWrapper.findElements(By.className("btn-option")).get(0);
        WebElement btnEdit = messageWrapper.findElement(By.cssSelector(".btn-option:not(.delete)"));

        new Actions(driver)
                .moveToElement(messageWrapper)
//                .pause(Duration.ofMillis(500))
                .moveToElement(btnEdit)
                .click().perform();

//        clickBtnOptionWithJS(btnOption);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("editModal")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("editContent")));

        WebElement editContent = driver.findElement(By.id("editContent"));
        editContent.clear();
        editContent.sendKeys("Mensaje editado por automatizacion");

        WebElement submitBtn = driver.findElement(By.cssSelector("#editForm button[type='submit']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submitBtn);

        wait.until(ExpectedConditions.urlContains("/conversation/" + conversation.getId()));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("chat-container"), "Mensaje editado por automatizacion"));
        assertTrue(driver.findElement(By.id("chat-container")).getText().contains("Mensaje editado por automatizacion"));
    }

    @Test
    void deleteMessage() {
        loginUser();
        driver.get(baseUrl + "conversation/" + conversation.getId());

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("chat-container")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("content-" + messageGuest.getId())));

        int initialMessagesCount = driver.findElements(By.className("message-row")).size();

        WebElement messageDiv = driver.findElement(By.id("content-" + messageGuest.getId()));
        WebElement messageWrapper = messageDiv.findElement(By.xpath("./ancestor::div[contains(@class,'mw-75')]"));

        List<WebElement> btnOptions = messageWrapper.findElements(By.className("btn-option"));
        WebElement btnDelete = btnOptions.stream()
                .filter(b -> b.findElements(By.className("fa-trash-can")).size() > 0)
                .findFirst()
                .orElseThrow();

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", btnDelete);
        ((JavascriptExecutor) driver).executeScript("arguments[0].style.visibility='visible'; arguments[0].style.display='block'; arguments[0].style.opacity='1';", btnDelete);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btnDelete);

        wait.until(ExpectedConditions.numberOfElementsToBeLessThan(By.className("message-row"), initialMessagesCount));
        assertEquals(initialMessagesCount - 1, driver.findElements(By.className("message-row")).size());
    }

    @Test
    void deletedMessageContentNoLongerVisible() {
        loginUser();
        driver.get(baseUrl + "conversation/" + conversation.getId());

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("chat-container")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("content-" + messageGuest.getId())));

        String originalContent = messageGuest.getContent();
        String messageContentId = "content-" + messageGuest.getId();

        WebElement messageDiv = driver.findElement(By.id(messageContentId));
        WebElement messageWrapper = messageDiv.findElement(By.xpath("./ancestor::div[contains(@class,'mw-75')]"));

        List<WebElement> btnOptions = messageWrapper.findElements(By.className("btn-option"));
        WebElement btnDelete = btnOptions.stream()
                .filter(b -> b.findElements(By.className("fa-trash-can")).size() > 0)
                .findFirst()
                .orElseThrow();

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", btnDelete);
        ((JavascriptExecutor) driver).executeScript("arguments[0].style.visibility='visible'; arguments[0].style.display='block'; arguments[0].style.opacity='1';", btnDelete);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btnDelete);

        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id(messageContentId)));
        assertFalse(driver.findElement(By.id("chat-container")).getText().contains(originalContent));
    }

    @Test
    void editedMessagePersistsAfterReload() {
        loginUser();
        driver.get(baseUrl + "conversation/" + conversation.getId());

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("chat-container")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("content-" + messageGuest.getId())));

        WebElement messageDiv = driver.findElement(By.id("content-" + messageGuest.getId()));
        WebElement messageWrapper = messageDiv.findElement(By.xpath("./ancestor::div[contains(@class,'mw-75')]"));
//        WebElement btnOption = messageWrapper.findElements(By.className("btn-option")).get(0);
//
//        clickBtnOptionWithJS(btnOption);
        WebElement btnEdit = messageWrapper.findElement(By.cssSelector(".btn-option:not(.delete)"));

        new Actions(driver)
                .moveToElement(messageWrapper)
//                .pause(Duration.ofMillis(500))
                .moveToElement(btnEdit)
                .click().perform();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("editModal")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("editContent")));

        WebElement editContent = driver.findElement(By.id("editContent"));
        editContent.clear();
        editContent.sendKeys("Persistencia tras recarga");

        WebElement submitBtn = driver.findElement(By.cssSelector("#editForm button[type='submit']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submitBtn);

        wait.until(ExpectedConditions.urlContains("/conversation/" + conversation.getId()));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("chat-container"), "Persistencia tras recarga"));

        driver.navigate().refresh();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("chat-container")));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("chat-container"), "Persistencia tras recarga"));
        assertTrue(driver.findElement(By.id("chat-container")).getText().contains("Persistencia tras recarga"));
    }
}