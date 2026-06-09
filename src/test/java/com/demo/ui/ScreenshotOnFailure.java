package com.demo.ui;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ScreenshotOnFailure implements AfterTestExecutionCallback {

    @Override
    public void afterTestExecution(ExtensionContext context) {
        if (context.getExecutionException().isEmpty()) return; // el test pasó: nada que hacer

        Object instance = context.getTestInstance().orElse(null);
        if (!(instance instanceof BaseSeleniumTest test) || test.driver == null) return;

        try {
            File src = ((TakesScreenshot) test.driver).getScreenshotAs(OutputType.FILE);
            Path dir = Path.of("target", "screenshots");
            Files.createDirectories(dir);
            String name = context.getRequiredTestClass().getSimpleName()
                    + "." + context.getRequiredTestMethod().getName() + ".png";
            Files.copy(src.toPath(), dir.resolve(name), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Captura del fallo en target/screenshots/" + name);
        } catch (Exception e) {
            System.out.println("No se pudo capturar la pantalla: " + e.getMessage());
        }
    }
}