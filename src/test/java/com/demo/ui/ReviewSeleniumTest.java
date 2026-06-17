package com.demo.ui;

import com.demo.model.Review;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ReviewSeleniumTest extends BaseSeleniumTest {

    // ------------------------------------------------------------------
    // Utilidad: scroll al elemento y click, con fallback JS
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

    // =========================================================
    // GET /reviews — Listado principal
    // =========================================================

    @Test
    void listadoMuestraTodasLasReviews() {
        loginUser();
        driver.get(baseUrl + "reviews");

        // El setUp crea review (sobre bookingPasado) → al menos 1 card
        List<WebElement> cards = driver.findElements(By.cssSelector("div.row div.col"));
        assertFalse(cards.isEmpty(), "Debe mostrarse al menos una review");
    }

    @Test
    void listadoMuestraElComentarioDeLaReview() {
        loginUser();
        driver.get(baseUrl + "reviews");

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("Increíble lugar, muy recomendado"),
                "El listado debe mostrar el comentario de la review");
    }

    @Test
    void listadoSinReviewsMuestraMensajeVacio() {
        // Borrar reviews para forzar lista vacía
        reviewRepository.deleteAll();

        loginUser();
        driver.get(baseUrl + "reviews");

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("Sin valoraciones") || pageText.contains("No se encontraron"),
                "Debe mostrarse mensaje cuando no hay reviews");
    }

    @Test
    void adminVeBotonEliminarEnCadaCard() {
        loginAdmin();
        driver.get(baseUrl + "reviews");

        // Solo ADMIN ve el form de delete en la lista (sec:authorize="hasRole('ADMIN')")
        List<WebElement> deleteForms = driver.findElements(
                By.cssSelector("form[action*='/reviews/delete/']"));
        assertFalse(deleteForms.isEmpty(),
                "El admin debe ver botones de eliminar en las cards");
    }

    @Test
    void userNormalNoVeBotonEliminarEnLista() {
        loginUser();
        driver.get(baseUrl + "reviews");

        List<WebElement> deleteForms = driver.findElements(
                By.cssSelector("form[action*='/reviews/delete/']"));
        assertTrue(deleteForms.isEmpty(),
                "El usuario normal no debe ver botones de eliminar");
    }

    // =========================================================
    // GET /reviews — Filtro por rating
    // =========================================================

    @Test
    void filtrarPorRating5MuestraSoloReviewsConPuntuacion5() {
        loginUser();
        driver.get(baseUrl + "reviews?rating=5");

        String pageText = driver.findElement(By.tagName("body")).getText();
        // La review del setUp tiene rating=5
        assertTrue(pageText.contains("Increíble lugar, muy recomendado"),
                "Debe mostrarse la review con rating 5");
    }

    @Test
    void filtrarPorRatingInexistenteMuestraMensajeVacio() {
        loginUser();
        // No hay reviews con rating=2 en el setUp
        driver.get(baseUrl + "reviews?rating=2");

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("Sin valoraciones") || pageText.contains("No se encontraron"),
                "Debe mostrarse mensaje vacío si no hay reviews con ese rating");
    }

    // =========================================================
    // GET /reviews — Filtro por orden
    // =========================================================

    @Test
    void botonRecientesEstaActivo() {
        loginUser();
        driver.get(baseUrl + "reviews?orden=reciente");

        // El botón "Recientes" tiene btn-dark cuando está activo
        WebElement botonReciente = driver.findElement(
                By.xpath("//a[contains(@class,'btn') and contains(@href,'orden=reciente') and normalize-space()='Recientes']"));
        assertTrue(botonReciente.getAttribute("class").contains("btn-dark"),
                "El botón Recientes debe aparecer activo con btn-dark");
    }

    @Test
    void botonAntiguasEstaActivo() {
        loginUser();
        driver.get(baseUrl + "reviews?orden=antiguo");

        WebElement botonAntiguo = driver.findElement(
                By.xpath("//a[contains(@class,'btn') and contains(@href,'orden=antiguo') and normalize-space()='Antiguas']"));
        assertTrue(botonAntiguo.getAttribute("class").contains("btn-dark"),
                "El botón Antiguas debe aparecer activo con btn-dark");
    }

    @Test
    void botonRestablecerLimpiaTodosLosFiltros() {
        loginUser();
        driver.get(baseUrl + "reviews?rating=5&orden=reciente");

        WebElement btnRestablecer = driver.findElement(
                By.cssSelector("a[href='/reviews']"));
        scrollAndClick(btnRestablecer);

        wait.until(ExpectedConditions.urlToBe(baseUrl + "reviews"));

        // La URL no debe tener parámetros de filtro
        assertFalse(driver.getCurrentUrl().contains("rating"),
                "Tras restablecer no debe haber filtro de rating en la URL");
    }

    // =========================================================
    // GET /reviews/{id} — Detalle de review
    // =========================================================

    @Test
    void detalleReviewMuestraComentarioYPuntuacion() {
        loginUser();
        driver.get(baseUrl + "reviews/" + review.getId());

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("Increíble lugar, muy recomendado"),
                "El detalle debe mostrar el comentario de la review");
        assertTrue(pageText.contains("5 / 5") || pageText.contains("5"),
                "El detalle debe mostrar la puntuación");
    }

    @Test
    void detalleReviewMuestraEnlaceAlListing() {
        loginUser();
        driver.get(baseUrl + "reviews/" + review.getId());

        // El HTML muestra el título del listing como enlace a /listings/{id}
        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("Apartamento con Vistas"),
                "El detalle debe mostrar el nombre del alojamiento vinculado");
    }

    @Test
    void detalleReviewMuestraEnlaceALaReserva() {
        loginUser();
        driver.get(baseUrl + "reviews/" + review.getId());

        WebElement bookingLink = driver.findElement(
                By.cssSelector("a[href*='/bookings/" + bookingPasado.getId() + "']"));
        assertNotNull(bookingLink, "Debe existir enlace a la reserva asociada");
    }

    @Test
    void adminVeBotonEliminarEnDetalle() {
        loginAdmin();
        driver.get(baseUrl + "reviews/" + review.getId());

        List<WebElement> deleteForm = driver.findElements(
                By.cssSelector("form[action*='/reviews/delete/" + review.getId() + "']"));
        assertFalse(deleteForm.isEmpty(),
                "El admin debe ver el botón de eliminar en el detalle");
    }

    @Test
    void guestAutorVeBotonEliminarEnDetalle() {
        // currentUser es el guest de bookingPasado → puede eliminar su propia review
        loginUser();
        driver.get(baseUrl + "reviews/" + review.getId());

        List<WebElement> deleteForm = driver.findElements(
                By.cssSelector("form[action*='/reviews/delete/" + review.getId() + "']"));
        assertFalse(deleteForm.isEmpty(),
                "El guest autor debe ver el botón de eliminar su propia review");
    }

    @Test
    void hostNoVeBotonEliminarEnDetalleDeReviewAjena() {
        // hostUser no es ni admin ni el guest de la review
        loginHost();
        driver.get(baseUrl + "reviews/" + review.getId());

        List<WebElement> deleteForm = driver.findElements(
                By.cssSelector("form[action*='/reviews/delete/" + review.getId() + "']"));
        assertTrue(deleteForm.isEmpty(),
                "El host no debe ver el botón de eliminar una review que no es suya");
    }

    @Test
    void detalleReviewIdInexistenteRedirigAListado() {
        loginUser();
        driver.get(baseUrl + "reviews/999999");

        wait.until(ExpectedConditions.urlToBe(baseUrl + "reviews"));
        assertEquals(baseUrl + "reviews", driver.getCurrentUrl(),
                "Una review inexistente debe redirigir al listado");
    }

    // =========================================================
    // GET /reviews/delete/{id} — Eliminar review (es GET en el controller)
    // =========================================================

    @Test
    void adminPuedeEliminarReviewDesdeListado() {
        Long reviewId = review.getId();

        loginAdmin();
        driver.get(baseUrl + "reviews");

        // El botón delete en la lista tiene confirm()
        WebElement deleteBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("form[action='/reviews/delete/" + reviewId + "'] button")));
        scrollAndClick(deleteBtn);

        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.accept();

        wait.until(ExpectedConditions.urlToBe(baseUrl + "reviews"));

        // La review ya no debe aparecer en el listado
        String pageText = driver.findElement(By.tagName("body")).getText();
        assertFalse(reviewRepository.existsById(reviewId),
                "La review debe haberse eliminado de la BD");
    }

    @Test
    void adminPuedeEliminarReviewDesdeDetalle() {
        Long reviewId = review.getId();

        loginAdmin();
        driver.get(baseUrl + "reviews/" + reviewId);

        WebElement deleteBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("form[action*='/reviews/delete/" + reviewId + "'] button")));
        scrollAndClick(deleteBtn);

        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.accept();

        wait.until(ExpectedConditions.urlToBe(baseUrl + "reviews"));

        assertFalse(reviewRepository.existsById(reviewId),
                "La review debe haberse eliminado al confirmar desde el detalle");
    }

    @Test
    void eliminarReviewMuestraMensajeExito() {
        loginAdmin();
        driver.get(baseUrl + "reviews");

        WebElement deleteBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("form[action*='/reviews/delete/'] button")));
        scrollAndClick(deleteBtn);

        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.accept();

        wait.until(ExpectedConditions.urlToBe(baseUrl + "reviews"));

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("Borrado exitosamente") || pageText.contains("exitosamente"),
                "Debe mostrarse mensaje de éxito tras eliminar");
    }

    // =========================================================
    // GET /reviews/new/{bookingId} — Formulario de nueva review
    // =========================================================

    @Test
    void formularioNuevaReviewMuestraAlojamientoYFechas() {
        loginUser();
        driver.get(baseUrl + "reviews/new/" + bookingPasado.getId());

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("Apartamento con Vistas"),
                "El formulario debe mostrar el nombre del alojamiento de la reserva");
    }

    @Test
    void formularioNuevaReviewTieneEstrellasDePuntuacion() {
        loginUser();
        driver.get(baseUrl + "reviews/new/" + bookingPasado.getId());

        List<WebElement> stars = driver.findElements(By.cssSelector(".star-btn"));
        assertEquals(5, stars.size(), "Debe haber exactamente 5 estrellas en el formulario");
    }

    @Test
    void formularioNuevaReviewTieneTextarea() {
        loginUser();
        driver.get(baseUrl + "reviews/new/" + bookingPasado.getId());

        WebElement textarea = driver.findElement(By.cssSelector("textarea#comment"));
        assertNotNull(textarea, "Debe existir el textarea de comentario");
    }

    // =========================================================
    // POST /reviews — Crear review (caso válido)
    // =========================================================

    @Disabled
    @Test
    void crearReviewValidaRedirigADetalle() {
        // Primero borramos la review existente del bookingPasado
        // para poder crear una nueva sobre ese booking
        reviewRepository.delete(review);

        loginUser();
        driver.get(baseUrl + "reviews/new/" + bookingPasado.getId());

        // Clicar la 5ª estrella (rating = 5)
        List<WebElement> stars = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector(".star-btn")));
        scrollAndClick(stars.get(4)); // índice 4 = 5ª estrella

        // Rellenar el comentario
        WebElement textarea = driver.findElement(By.cssSelector("textarea#comment"));
        textarea.sendKeys("Estancia muy agradable, volvería sin duda");

        // Scroll al botón y click nativo (CSRF deshabilitado en perfil test)
        WebElement submitBtn = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("button[type='submit']")));
        scrollAndClick(submitBtn);

        // Debe redirigir al detalle de la nueva review
        wait.until(ExpectedConditions.urlMatches(".*/reviews/\\d+"));

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("Estancia muy agradable"),
                "El detalle de la review creada debe mostrar el comentario");
    }
    @Disabled
    @Test
    void reviewCreadaQuedaGuardadaEnBD() {
        reviewRepository.delete(review);

        loginUser();
        driver.get(baseUrl + "reviews/new/" + bookingPasado.getId());

        List<WebElement> stars = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector(".star-btn")));
        scrollAndClick(stars.get(3)); // 4ª estrella → rating=4

        driver.findElement(By.cssSelector("textarea#comment"))
                .sendKeys("Muy buena experiencia en general");

        // Scroll al botón y click nativo (CSRF deshabilitado en perfil test)
        WebElement submitBtn = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("button[type='submit']")));
        scrollAndClick(submitBtn);

        wait.until(ExpectedConditions.urlMatches(".*/reviews/\\d+"));

        String url   = driver.getCurrentUrl();
        Long newId   = Long.parseLong(url.replaceAll(".*/reviews/(\\d+).*", "$1"));

        Review created = reviewRepository.findById(newId).orElseThrow();
        assertEquals(4, created.getRating(), "El rating guardado debe ser 4");
        assertEquals("Muy buena experiencia en general", created.getComment());
    }

    // =========================================================
    // POST /reviews — Crear review (casos inválidos)
    // =========================================================

    @Test
    void crearReviewSinRatingMuestraError() {
        reviewRepository.delete(review);

        loginUser();
        driver.get(baseUrl + "reviews/new/" + bookingPasado.getId());

        // No seleccionamos ninguna estrella → rating vacío
        driver.findElement(By.cssSelector("textarea#comment"))
                .sendKeys("Comentario sin puntuación");

        // Scroll al botón y click nativo (CSRF deshabilitado)
        WebElement submitBtn = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("button[type='submit']")));
        scrollAndClick(submitBtn);

        // El JS del form hace e.preventDefault() cuando no hay rating → la página NO navega.
        // Esperamos a que #ratingError sea visible (el JS lo muestra con display:block).
        // Si por algún motivo el submit llega al servidor, el controller redirige a reviews/new
        // con flash de error que también contiene "puntuación".
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(By.id("ratingError")),
                ExpectedConditions.urlContains("bookings")
        ));

        String finalUrl = driver.getCurrentUrl();
        if (finalUrl.contains("bookings")) {
            // Llegó al servidor sin rating → flash de error en bookings
            String pageText = driver.findElement(By.tagName("body")).getText();
            assertTrue(pageText.contains("puntuación") || pageText.contains("error") || pageText.contains("Error"),
                    "Debe mostrarse flash de error al llegar al servidor sin puntuación");
        } else {
            // El JS bloqueó el submit → #ratingError visible en el formulario
            WebElement ratingError = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.id("ratingError")));
            assertTrue(ratingError.isDisplayed(),
                    "Debe mostrarse el error de puntuación en el formulario");
        }
    }

    @Disabled
    @Test
    void crearReviewSinComentarioMuestraError() {
        reviewRepository.delete(review);

        loginUser();
        driver.get(baseUrl + "reviews/new/" + bookingPasado.getId());

        // Seleccionar estrella pero dejar comentario vacío
        List<WebElement> stars = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector(".star-btn")));
        scrollAndClick(stars.get(2)); // 3ª estrella

        // No escribimos nada en el textarea
        // Scroll al botón y click nativo (CSRF deshabilitado en perfil test)
        WebElement submitBtn = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("button[type='submit']")));
        scrollAndClick(submitBtn);

        // El campo tiene `required` → el navegador bloquea el submit con validación nativa
        // Si el browser no lo bloquea, el controller redirige a /bookings con error
        boolean enFormulario  = driver.getCurrentUrl().contains("reviews/new");
        boolean enBookings    = driver.getCurrentUrl().contains("bookings");
        assertTrue(enFormulario || enBookings,
                "Debe quedarse en el formulario (validación HTML) o redirigir con error");
    }

    // =========================================================
    // GET /listing/{listingId}/reviews — Reviews de un listing
    // =========================================================

    @Test
    void reviewsDeListingConReseñasLasRedirigeMostrandoLista() {
        // bookingPasado.listing = apartamento, y review está vinculada a ese booking
        loginUser();
        driver.get(baseUrl + "listing/" + apartamento.getId() + "/reviews");

        // Como hay reviews, el controller devuelve review/review-list
        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("Increíble lugar, muy recomendado"),
                "Debe mostrarse la review del listing");
    }

    @Test
    void reviewsDeListingSinReseñasRedirigAlListing() {
        // loft no tiene reviews asociadas
        loginUser();
        driver.get(baseUrl + "listing/" + loft.getId() + "/reviews");

        // El controller redirige a /listings/{id} cuando no hay reviews
        wait.until(ExpectedConditions.urlContains("/listings/" + loft.getId()));
        assertTrue(driver.getCurrentUrl().contains("/listings/" + loft.getId()),
                "Sin reviews debe redirigir a la página del listing");
    }
}