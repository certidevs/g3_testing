package com.demo.ui;

import com.demo.model.Booking;
import com.demo.model.enums.BookingStatus;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BookingSeleniumTest extends BaseSeleniumTest {

    // ------------------------------------------------------------------
    // Utilidad: hace scroll al elemento y lo pulsa con JS como fallback
    // Evita ElementClickInterceptedException cuando el botón está
    // tapado por la navbar fija o fuera del viewport
    // ------------------------------------------------------------------
    private void scrollAndClick(WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", element);
        try {
            element.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    // ------------------------------------------------------------------
    // Utilidad: rellena un input de fecha (type=date) con JS
    // porque sendKeys falla en Chrome con inputs type=date
    // ------------------------------------------------------------------
    private void setDateInput(String cssSelector, String value) {
        WebElement input = driver.findElement(By.cssSelector(cssSelector));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1];", input, value);
    }

    // =========================================================
    // GET /bookings — Lista de reservas según rol
    // =========================================================

    @Test
    void adminVeTodasLasReservas() {
        loginAdmin();
        driver.get(baseUrl + "bookings");

        List<WebElement> cards = driver.findElements(By.cssSelector("div.row div.col"));
        assertTrue(cards.size() >= 2, "El admin debe ver al menos 2 reservas");
    }

    @Test
    void userSoloVeSusReservas() {
        loginUser();
        driver.get(baseUrl + "bookings");

        List<WebElement> cards = driver.findElements(By.cssSelector("div.row div.col"));
        assertTrue(cards.size() >= 2, "El usuario debe ver sus propias reservas");

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertFalse(pageText.contains(adminUser.getName()),
                "El usuario no debe ver reservas de otros usuarios");
    }

    @Test
    void hostVeReservasDeAlojamientosQueLePertenecen() {
        loginHost();
        driver.get(baseUrl + "bookings");

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("Loft Industrial"),
                "El host debe ver las reservas de sus alojamientos");
    }

    @Test
    void sinReservasMuestraMensajeVacio() {
        // Borrar en orden correcto respetando FKs:
        // messages → conversations → reviews → bookings
        messageRepository.deleteAll();
        conversationRepository.deleteAll();
        reviewRepository.deleteAll();
        bookingRepository.deleteAll();

        loginUser();
        driver.get(baseUrl + "bookings");

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("no tienes viajes") || pageText.contains("Todavía"),
                "Debe mostrarse mensaje de lista vacía");
    }

    // =========================================================
    // GET /bookings/{id} — Detalle de reserva
    // =========================================================

    @Test
    void detalleReservaMuestraAlojamientoYPropietario() {
        loginUser();
        driver.get(baseUrl + "bookings/" + booking.getId());

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("Loft Industrial"),
                "El detalle debe mostrar el nombre del alojamiento");
        assertTrue(pageText.contains(hostUser.getName()),
                "El detalle debe mostrar el nombre del propietario");
    }

    @Test
    void detalleReservaMuestraEstadoEnBadge() {
        loginUser();
        driver.get(baseUrl + "bookings/" + booking.getId());

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("CONFIRMED") || pageText.contains("PENDING"),
                "El detalle debe mostrar el estado de la reserva");
    }

    @Test
    void detalleReservaMuestraReviewSiExiste() {
        loginUser();
        // bookingPasado tiene review con comment "Increíble lugar, muy recomendado"
        driver.get(baseUrl + "bookings/" + bookingPasado.getId());

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("Increíble lugar, muy recomendado"),
                "El detalle debe mostrar el comentario de la review");
        assertTrue(pageText.contains("5 / 5"),
                "El detalle debe mostrar la puntuación de la review");
    }

    @Test
    void detalleReservaSinReviewMuestraMensajeNingunaOpinion() {
        loginUser();
        // booking futuro no tiene review
        driver.get(baseUrl + "bookings/" + booking.getId());

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("Ninguna opinión añadida"),
                "Debe mostrarse el placeholder cuando no hay review");
    }

    // =========================================================
    // POST /bookings/{id}/confirm — Confirmar reserva
    // =========================================================

    @Test
    void adminPuedeConfirmarReservaPendiente() {
        booking.setStatus(BookingStatus.PENDING);
        bookingRepository.save(booking);

        loginAdmin();
        driver.get(baseUrl + "bookings/" + booking.getId());

        WebElement confirmBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("form[action*='/confirm'] button")));
        scrollAndClick(confirmBtn);

        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.accept();

        wait.until(ExpectedConditions.urlContains("/bookings/" + booking.getId()));

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("confirmada"),
                "Debe mostrarse mensaje de éxito al confirmar");

        Booking updated = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(BookingStatus.CONFIRMED, updated.getStatus());
    }

    @Test
    void hostPropietarioPuedeConfirmarReservaPendiente() {
        booking.setStatus(BookingStatus.PENDING);
        bookingRepository.save(booking);

        loginHost();
        driver.get(baseUrl + "bookings/" + booking.getId());

        WebElement confirmBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("form[action*='/confirm'] button")));
        scrollAndClick(confirmBtn);

        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.accept();

        wait.until(ExpectedConditions.urlContains("/bookings/" + booking.getId()));

        Booking updated = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(BookingStatus.CONFIRMED, updated.getStatus());
    }

    @Test
    void userNormalNoVeBotonConfirmarEnReservaPendiente() {
        booking.setStatus(BookingStatus.PENDING);
        bookingRepository.save(booking);

        loginUser();
        driver.get(baseUrl + "bookings/" + booking.getId());

        List<WebElement> confirmForm = driver.findElements(
                By.cssSelector("form[action*='/confirm']"));
        assertTrue(confirmForm.isEmpty(),
                "El usuario normal no debe ver el botón de confirmar");
    }

    @Test
    void reservaConfirmadaNoMuestraBotonConfirmar() {
        // booking ya está CONFIRMED — el th:if solo muestra confirm si status == PENDING
        loginAdmin();
        driver.get(baseUrl + "bookings/" + booking.getId());

        List<WebElement> confirmForm = driver.findElements(
                By.cssSelector("form[action*='/confirm']"));
        assertTrue(confirmForm.isEmpty(),
                "No debe mostrarse el botón confirmar si la reserva ya está CONFIRMED");
    }

    // =========================================================
    // POST /bookings/{id}/cancel — Cancelar reserva
    // =========================================================

    @Test
    void adminPuedeCancelarReservaConfirmada() {
        // booking está CONFIRMED por defecto
        loginAdmin();
        driver.get(baseUrl + "bookings/" + booking.getId());

        WebElement cancelBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("form[action*='/cancel'] button")));
        scrollAndClick(cancelBtn);

        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.accept();

        wait.until(ExpectedConditions.urlContains("/bookings/" + booking.getId()));

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("cancelada"),
                "Debe mostrarse mensaje de cancelación exitosa");

        Booking updated = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(BookingStatus.CANCELED, updated.getStatus());
    }

    @Test
    void cualquierUsuarioPuedeCancelarReservaPendiente() {
        // El th:if del cancel muestra el botón a cualquiera si status == PENDING
        booking.setStatus(BookingStatus.PENDING);
        bookingRepository.save(booking);

        loginUser();
        driver.get(baseUrl + "bookings/" + booking.getId());

        WebElement cancelBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("form[action*='/cancel'] button")));
        scrollAndClick(cancelBtn);

        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.accept();

        wait.until(ExpectedConditions.urlContains("/bookings/" + booking.getId()));

        Booking updated = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(BookingStatus.CANCELED, updated.getStatus());
    }

    @Test
    void reservaCanceladaNoMuestraBotonCancelar() {
        booking.setStatus(BookingStatus.CANCELED);
        bookingRepository.save(booking);

        loginAdmin();
        driver.get(baseUrl + "bookings/" + booking.getId());

        List<WebElement> cancelForm = driver.findElements(
                By.cssSelector("form[action*='/cancel']"));
        assertTrue(cancelForm.isEmpty(),
                "No debe mostrarse el botón cancelar si la reserva ya está CANCELED");
    }

    // =========================================================
    // POST /bookings/{id}/delete
    // El endpoint existe en el controller pero el botón no está
    // en el HTML actual. Se verifica el comportamiento directo
    // llamando al endpoint via navegación y verificando en BD.
    // =========================================================

    @Test
    void eliminarReservaEliminaConversacionYMensajes() {
        Long bookingId     = booking.getId();
        Long convId        = conversation.getId();

        loginAdmin();

        // Llamar al endpoint POST /bookings/{id}/delete directamente con JS
        // (el botón no existe en el HTML actual, pero el endpoint sí)
        ((JavascriptExecutor) driver).executeScript(
                "var f = document.createElement('form');" +
                        "f.method = 'POST';" +
                        "f.action = '/bookings/" + bookingId + "/delete';" +
                        "var csrf = document.querySelector('input[name=\"_csrf\"]');" +
                        "if(csrf){ var c = csrf.cloneNode(true); f.appendChild(c); }" +
                        "document.body.appendChild(f);" +
                        "f.submit();"
        );

        wait.until(ExpectedConditions.urlToBe(baseUrl + "bookings"));

        assertFalse(bookingRepository.existsById(bookingId),
                "La reserva debe haber sido eliminada");
        assertNull(conversationRepository.findByBookingId(bookingId),
                "La conversación debe haber sido eliminada");
        assertTrue(messageRepository.findByConversationId(
                        convId, org.springframework.data.domain.Sort.unsorted()).isEmpty(),
                "Los mensajes deben haber sido eliminados");
    }

    @Test
    void eliminarReservaSinConversacionFunciona() {
        Long bookingId = bookingPasado.getId();

        loginAdmin();

        ((JavascriptExecutor) driver).executeScript(
                "var f = document.createElement('form');" +
                        "f.method = 'POST';" +
                        "f.action = '/bookings/" + bookingId + "/delete';" +
                        "var csrf = document.querySelector('input[name=\"_csrf\"]');" +
                        "if(csrf){ var c = csrf.cloneNode(true); f.appendChild(c); }" +
                        "document.body.appendChild(f);" +
                        "f.submit();"
        );

        wait.until(ExpectedConditions.urlToBe(baseUrl + "bookings"));

        assertFalse(bookingRepository.existsById(bookingId),
                "La reserva sin conversación debe haberse eliminado correctamente");
    }

    // =========================================================
    // GET /bookings/new/{listingId} — Formulario de nueva reserva
    // =========================================================

    @Test
    void formularioNuevaReservaMuestraListing() {
        loginUser();
        driver.get(baseUrl + "bookings/new/" + apartamento.getId());

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("Apartamento con Vistas"),
                "El formulario debe mostrar el nombre del alojamiento");
    }

    @Test
    void formularioNuevaReservaTieneInputsDeFecha() {
        loginUser();
        driver.get(baseUrl + "bookings/new/" + loft.getId());

        assertFalse(driver.findElements(By.cssSelector("input[name='checkIn']")).isEmpty(),
                "Debe existir el campo checkIn");
        assertFalse(driver.findElements(By.cssSelector("input[name='checkOut']")).isEmpty(),
                "Debe existir el campo checkOut");
    }

    // =========================================================
    // POST /bookings — Crear reserva (casos válidos)
    // =========================================================

    @Test
    void crearReservaValidaRedirigADetalle() {
        loginUser();
        driver.get(baseUrl + "bookings/new/" + apartamento.getId());

        // apartamento: minNights=2, maxNights=15, fechas libres futuras
        setDateInput("input[name='checkIn']",  "2027-01-10");
        setDateInput("input[name='checkOut']", "2027-01-13");

        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[type='submit']")));
        scrollAndClick(submitBtn);

        wait.until(ExpectedConditions.urlMatches(".*/bookings/\\d+"));

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("creada") || pageText.contains("pendiente"),
                "Debe mostrarse mensaje de reserva creada correctamente");
    }

    @Test
    void reservaCreadaQuedaEnEstadoPending() {
        loginUser();
        driver.get(baseUrl + "bookings/new/" + apartamento.getId());

        setDateInput("input[name='checkIn']",  "2027-02-01");
        setDateInput("input[name='checkOut']", "2027-02-05");

        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[type='submit']")));
        scrollAndClick(submitBtn);

        wait.until(ExpectedConditions.urlMatches(".*/bookings/\\d+"));

        String url   = driver.getCurrentUrl();
        Long newId   = Long.parseLong(url.replaceAll(".*/bookings/(\\d+).*", "$1"));

        Booking created = bookingRepository.findById(newId).orElseThrow();
        assertEquals(BookingStatus.PENDING, created.getStatus(),
                "La nueva reserva debe quedar en estado PENDING");
    }





}