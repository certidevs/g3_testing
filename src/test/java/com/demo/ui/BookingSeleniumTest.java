package com.demo.ui;

import com.demo.model.Booking;
import com.demo.model.enums.BookingStatus;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BookingSeleniumTest extends BaseSeleniumTest {

    // =========================================================
    // GET /bookings — Lista de reservas según rol
    // =========================================================

    @Test
    void adminVeTodasLasReservas() {
        loginAdmin();
        driver.get(baseUrl + "bookings");

        List<WebElement> rows = driver.findElements(By.cssSelector("[data-booking-id]"));
        // El admin ve todas las reservas (booking + bookingPasado)
        assertTrue(rows.size() >= 2, "El admin debe ver al menos 2 reservas");
    }

    @Test
    void userSoloVeSusReservas() {
        loginUser();
        driver.get(baseUrl + "bookings");

        // currentUser tiene booking + bookingPasado
        List<WebElement> rows = driver.findElements(By.cssSelector("[data-booking-id]"));
        assertTrue(rows.size() >= 2, "El usuario debe ver sus propias reservas");

        // No debe aparecer ninguna reserva de otro usuario
        String pageText = driver.findElement(By.tagName("body")).getText();
        assertFalse(pageText.contains(adminUser.getName()),
                "El usuario no debe ver reservas de otros usuarios");
    }

    @Test
    void hostVeReservasComoGuestYComoHost() {
        loginHost();
        driver.get(baseUrl + "bookings");

        // El loft pertenece a hostUser y booking es sobre el loft → aparece como host
        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("Loft Industrial"),
                "El host debe ver las reservas de sus alojamientos");
    }

    // =========================================================
    // GET /bookings/{id} — Detalle de reserva
    // =========================================================

    @Test
    void detalleReservaMuestraInformacionCorrecta() {
        loginUser();
        driver.get(baseUrl + "bookings/" + booking.getId());

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("Loft Industrial"),
                "El detalle debe mostrar el nombre del alojamiento");
        assertTrue(pageText.contains(hostUser.getName()),
                "El detalle debe mostrar el propietario");
    }

    @Test
    void detalleReservaMuestraReviewSiExiste() {
        loginUser();
        // bookingPasado tiene review asociada
        driver.get(baseUrl + "bookings/" + bookingPasado.getId());

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("Increíble lugar, muy recomendado"),
                "El detalle debe mostrar la review si existe");
    }

    @Test
    void detalleReservaSinReviewNoMuestraSeccionReview() {
        loginUser();
        // booking (futuro) no tiene review
        driver.get(baseUrl + "bookings/" + booking.getId());

        List<WebElement> reviewSection = driver.findElements(By.cssSelector("[data-review]"));
        assertTrue(reviewSection.isEmpty() ||
                        !driver.findElement(By.tagName("body")).getText().contains("Increíble lugar"),
                "No debe mostrarse review si no existe");
    }

    // =========================================================
    // POST /bookings/{id}/confirm — Confirmar reserva
    // =========================================================

    @Test
    void adminPuedeConfirmarReservaPendiente() {
        // Poner la reserva en PENDING
        booking.setStatus(BookingStatus.PENDING);
        bookingRepository.save(booking);

        loginAdmin();
        driver.get(baseUrl + "bookings/" + booking.getId());

        WebElement confirmBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("form[action*='/confirm'] button")));
        confirmBtn.click();

        wait.until(ExpectedConditions.urlContains("/bookings/" + booking.getId()));

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("confirmada") || pageText.contains("CONFIRMED"),
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
        confirmBtn.click();

        wait.until(ExpectedConditions.urlContains("/bookings/" + booking.getId()));

        Booking updated = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(BookingStatus.CONFIRMED, updated.getStatus());
    }

    @Test
    void userSinPermisosNoPuedeConfirmarReserva() {
        booking.setStatus(BookingStatus.PENDING);
        bookingRepository.save(booking);

        loginUser();
        driver.get(baseUrl + "bookings/" + booking.getId());

        // El botón de confirmar no debe ser visible para un usuario sin permisos
        List<WebElement> confirmBtn = driver.findElements(
                By.cssSelector("form[action*='/confirm'] button"));
        assertTrue(confirmBtn.isEmpty(),
                "El usuario normal no debe ver el botón de confirmar");
    }

    // =========================================================
    // POST /bookings/{id}/cancel — Cancelar reserva
    // =========================================================

    @Test
    void cancelarReservaConfirmada() {
        // booking está CONFIRMED
        loginAdmin();
        driver.get(baseUrl + "bookings/" + booking.getId());

        WebElement cancelBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("form[action*='/cancel'] button")));
        cancelBtn.click();

        wait.until(ExpectedConditions.urlContains("/bookings/" + booking.getId()));

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("cancelada") || pageText.contains("CANCELED"),
                "Debe mostrarse mensaje de cancelación");

        Booking updated = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(BookingStatus.CANCELED, updated.getStatus());
    }

    @Test
    void cancelarReservaPendiente() {
        booking.setStatus(BookingStatus.PENDING);
        bookingRepository.save(booking);

        loginAdmin();
        driver.get(baseUrl + "bookings/" + booking.getId());

        WebElement cancelBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("form[action*='/cancel'] button")));
        cancelBtn.click();

        Booking updated = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(BookingStatus.CANCELED, updated.getStatus());
    }

    // =========================================================
    // POST /bookings/{id}/delete — Eliminar reserva
    // =========================================================

    @Test
    void eliminarReservaEliminaConversacionYMensajes() {
        loginAdmin();
        driver.get(baseUrl + "bookings/" + booking.getId());

        WebElement deleteBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("form[action*='/delete'] button")));
        deleteBtn.click();

        wait.until(ExpectedConditions.urlToBe(baseUrl + "bookings"));

        // La reserva ya no debe existir
        assertFalse(bookingRepository.existsById(booking.getId()),
                "La reserva debe haber sido eliminada");

        // La conversación y mensajes también deben haberse eliminado
        assertNull(conversationRepository.findByBookingId(booking.getId()),
                "La conversación debe haber sido eliminada");
        assertTrue(messageRepository.findByConversationId(
                        conversation.getId(), org.springframework.data.domain.Sort.unsorted()).isEmpty(),
                "Los mensajes deben haber sido eliminados");
    }

    @Test
    void eliminarReservaSinConversacionFunciona() {
        // bookingPasado no tiene conversación
        loginAdmin();
        driver.get(baseUrl + "bookings/" + bookingPasado.getId());

        WebElement deleteBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("form[action*='/delete'] button")));
        deleteBtn.click();

        wait.until(ExpectedConditions.urlToBe(baseUrl + "bookings"));

        assertFalse(bookingRepository.existsById(bookingPasado.getId()),
                "La reserva debe haber sido eliminada aunque no tuviera conversación");
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
    // POST /bookings — Crear reserva
    // =========================================================

    @Test
    void crearReservaValidaRedirigADetalle() {
        loginUser();
        driver.get(baseUrl + "bookings/new/" + apartamento.getId());

        // apartamento: minNights=2, maxNights=15 → reservamos 3 noches en fechas libres
        driver.findElement(By.cssSelector("input[name='checkIn']"))
                .sendKeys("2027-01-10");
        driver.findElement(By.cssSelector("input[name='checkOut']"))
                .sendKeys("2027-01-13");

        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Debe redirigir al detalle de la nueva reserva
        wait.until(ExpectedConditions.urlMatches(".*/bookings/\\d+"));

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("Reserva creada") || pageText.contains("pendiente"),
                "Debe mostrarse mensaje de reserva creada");
    }

    @Test
    void reservaCreadaQuedaEnEstadoPending() {
        loginUser();
        driver.get(baseUrl + "bookings/new/" + apartamento.getId());

        driver.findElement(By.cssSelector("input[name='checkIn']"))
                .sendKeys("2027-02-01");
        driver.findElement(By.cssSelector("input[name='checkOut']"))
                .sendKeys("2027-02-05");

        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.urlMatches(".*/bookings/\\d+"));

        // Extraer id de la URL y verificar en BD
        String url = driver.getCurrentUrl();
        Long newId = Long.parseLong(url.replaceAll(".*/bookings/(\\d+).*", "$1"));

        Booking created = bookingRepository.findById(newId).orElseThrow();
        assertEquals(BookingStatus.PENDING, created.getStatus(),
                "La nueva reserva debe estar en estado PENDING");
    }


}