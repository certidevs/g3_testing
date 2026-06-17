package com.demo.ui;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite E2E con Selenium para la entidad Listing.
 *
 * Reutiliza BaseSeleniumTest tal cual (fixtures loft/apartamento/chalet,
 * usuarios admin/host/user y helpers loginAdmin/loginHost/loginUser).
 *
 * Fixtures relevantes (creados en BaseSeleniumTest.setUp):
 *   - loft        -> ACTIVO,  MADRID,   LOFT,        owner=host, tiene booking CONFIRMED (futuro)
 *   - apartamento -> ACTIVO,  ALICANTE, APARTAMENTO, owner=host, tiene booking CONFIRMED (pasado) + review
 *   - chalet      -> INACTIVO,BILBAO,   CHALET,      owner=host, SIN reservas (borrable)
 *   - hostUser=alex@pro.com / 1234 (ROLE_HOST, dueno de los tres)
 *   - currentUser=sonia@mail.com / 1234 (ROLE_USER)
 *   - adminUser=admin@openhouse.com / 1234 (ROLE_ADMIN)
 *
 * Nota: baseUrl YA termina en "/", por eso se navega con baseUrl + "listings".
 */
public class ListingSeleniumTest extends BaseSeleniumTest {

    // ------------------------------------------------------------------
    // Utilidades
    // ------------------------------------------------------------------

    /** Hace scroll al elemento (evita que la navbar fija lo tape) y lo pulsa. */
    private void scrollAndClick(WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", element);
        try {
            element.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    /** Envia un formulario por JS, evitando el confirm() nativo del boton de borrado. */
    private void submitFormByJs(WebElement form) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].submit();", form);
    }

    /** Devuelve true si un input HTML5 es valido (checkValidity()). */
    private boolean isFieldValid(WebElement input) {
        return (Boolean) ((JavascriptExecutor) driver)
                .executeScript("return arguments[0].checkValidity();", input);
    }

    private String bodyText() {
        return driver.findElement(By.tagName("body")).getText();
    }

    // =========================================================
    // GET /listings  Listado (visibilidad por rol y filtros)
    // =========================================================

    @Test
    @DisplayName("LIST-01 | Anonimo ve solo listings activos (no ve el chalet inactivo)")
    void anonimoVeSoloListingsActivos() {
        driver.get(baseUrl + "listings");
        wait.until(ExpectedConditions.titleContains("Alojamientos disponibles"));

        String texto = bodyText();
        assertTrue(texto.contains("Loft Industrial"), "Debe verse el loft activo");
        assertTrue(texto.contains("Apartamento con Vistas"), "Debe verse el apartamento activo");
        assertFalse(texto.contains("Chalet en el Bosque"),
                "El anonimo NO debe ver el chalet (inactivo)");
    }

    @Test
    @DisplayName("LIST-02 | Anonimo no ve controles de edicion ni de activar/pausar")
    void anonimoNoVeControlesDeGestion() {
        driver.get(baseUrl + "listings");
        wait.until(ExpectedConditions.titleContains("Alojamientos disponibles"));

        assertTrue(driver.findElements(By.cssSelector("a[href*='/listings/edit/']")).isEmpty(),
                "El anonimo no debe ver enlaces de edicion");
        assertTrue(driver.findElements(By.cssSelector("form[action*='/listings/toggle/']")).isEmpty(),
                "El anonimo no debe ver formularios de activar/pausar");
    }

    @Test
    @DisplayName("LIST-03 | El host ve su propio listing inactivo con el badge 'Inactivo' y los controles")
    void hostVeSuListingInactivoConControles() {
        loginHost();
        driver.get(baseUrl + "listings");

        String texto = bodyText();
        assertTrue(texto.contains("Chalet en el Bosque"),
                "El host debe ver su propio listing inactivo");
        assertTrue(texto.contains("Inactivo"), "Debe mostrarse el badge 'Inactivo'");
        assertFalse(driver.findElements(By.cssSelector("a[href*='/listings/edit/']")).isEmpty(),
                "El host debe ver enlaces de edicion sobre sus listings");
    }

    @Test
    @DisplayName("LIST-04 | Filtro por ciudad MADRID devuelve solo el loft")
    void filtroPorCiudadDevuelveSoloMadrid() {
        driver.get(baseUrl + "listings?city=MADRID");
        wait.until(ExpectedConditions.titleContains("Alojamientos disponibles"));

        String texto = bodyText();
        assertTrue(texto.contains("Loft Industrial"), "MADRID debe incluir el loft");
        assertFalse(texto.contains("Apartamento con Vistas"),
                "MADRID no debe incluir el apartamento (ALICANTE)");
    }

    @Test
    @DisplayName("LIST-05 | minPrice > maxPrice muestra aviso y no devuelve resultados")
    void rangoDePrecioInvalidoMuestraAviso() {
        driver.get(baseUrl + "listings?minPrice=300&maxPrice=50");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-warning")));

        WebElement aviso = driver.findElement(By.cssSelector(".alert-warning"));
        assertTrue(aviso.getText().contains("no puede ser mayor"),
                "Debe avisar que el precio minimo no puede superar al maximo");
        assertTrue(driver.findElements(By.cssSelector("main div.row div.col .card")).isEmpty(),
                "No debe mostrar tarjetas de listing con rango invalido");
    }

    @Test
    @DisplayName("LIST-06 | Orden por precio ascendente coloca el loft (110 EUR) antes que el apartamento (150 EUR)")
    void ordenPorPrecioAscendente() {
        driver.get(baseUrl + "listings?sort=priceAsc");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("main div.row div.col h5")));

        List<WebElement> titulos = driver.findElements(By.cssSelector("main div.row div.col h5"));
        assertFalse(titulos.isEmpty(), "Debe haber listings");
        assertTrue(titulos.get(0).getText().contains("Loft Industrial"),
                "Con priceAsc el primero debe ser el mas barato (loft, 110 EUR)");
    }

    // =========================================================
    // GET /listings/{id}  Detalle (regresion, reglas de negocio)
    // =========================================================

    @Test
    @DisplayName("DETAIL-01 | (Regresion) Anonimo abre el detalle de un listing activo sin HTTP 500")
    void anonimoVeDetalleDeListingActivo() {
        driver.get(baseUrl + "listings/" + loft.getId());
        wait.until(ExpectedConditions.titleContains("Detalle del Alojamiento"));

        WebElement titulo = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("main h1")));
        assertEquals("Loft Industrial", titulo.getText().trim());
        // El bloque de reserva es visible para no-admin no-propietario (incluye anonimo)
        assertFalse(driver.findElements(By.cssSelector("main a.btn-airbnb")).isEmpty(),
                "El anonimo debe ver el boton 'Reservar ahora'");
    }

    @Test
    @DisplayName("DETAIL-02 | El detalle de un listing activo muestra el badge 'Disponible'")
    void detalleActivoMuestraBadgeDisponible() {
        driver.get(baseUrl + "listings/" + loft.getId());
        wait.until(ExpectedConditions.titleContains("Detalle del Alojamiento"));

        assertTrue(bodyText().contains("Disponible"),
                "Un listing activo debe mostrar el badge 'Disponible'");
    }

    @Test
    @DisplayName("DETAIL-03 | El propietario no puede reservar su propio alojamiento")
    void propietarioNoPuedeReservarSuPropioListing() {
        loginHost(); // alex@pro.com es dueno del loft
        driver.get(baseUrl + "listings/" + loft.getId());
        wait.until(ExpectedConditions.titleContains("Detalle del Alojamiento"));

        assertTrue(bodyText().contains("No puedes reservar tu propio alojamiento"),
                "El dueno debe ver el aviso de que no puede reservar");
        assertTrue(driver.findElements(By.cssSelector("main a.btn-airbnb")).isEmpty(),
                "El dueno no debe ver el boton 'Reservar ahora'");
    }

    @Test
    @DisplayName("DETAIL-04 | Un listing inactivo NO es visible para un anonimo (404)")
    void listingInactivoNoVisibleParaAnonimo() {
        driver.get(baseUrl + "listings/" + chalet.getId());

        // El controller lanza 404; nunca debe renderizarse el detalle real del chalet
        assertFalse(bodyText().contains("Rodeada de naturaleza"),
                "El anonimo no debe ver el contenido de un listing inactivo");
    }

    @Test
    @DisplayName("DETAIL-05 | El admin si puede ver el detalle de un listing inactivo")
    void adminVeDetalleDeListingInactivo() {
        loginAdmin();
        driver.get(baseUrl + "listings/" + chalet.getId());
        wait.until(ExpectedConditions.titleContains("Detalle del Alojamiento"));

        WebElement titulo = driver.findElement(By.cssSelector("main h1"));
        assertEquals("Chalet en el Bosque", titulo.getText().trim());
        assertTrue(bodyText().contains("No disponible"),
                "El listing inactivo debe mostrar el badge 'No disponible'");
    }

    // =========================================================
    // GET/POST /listings  Formulario (crear, editar, validar)
    // =========================================================

    @Test
    @DisplayName("FORM-01 | El host crea un listing y queda visible y persistido")
    void hostCreaListingCorrectamente() {
        loginHost();
        driver.get(baseUrl + "listings/new");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("title")));

        String nuevoTitulo = "Villa con piscina en Madrid";
        driver.findElement(By.id("title")).sendKeys(nuevoTitulo);
        new Select(driver.findElement(By.id("type"))).selectByValue("APARTAMENTO");
        new Select(driver.findElement(By.id("city"))).selectByValue("MADRID");
        driver.findElement(By.id("pricePerNight")).sendKeys("180");
        driver.findElement(By.id("minNights")).sendKeys("2");
        driver.findElement(By.id("maxNights")).sendKeys("10");
        driver.findElement(By.id("maxGuests")).sendKeys("6");
        driver.findElement(By.id("shortDescription"))
                .sendKeys("Espectacular villa para vacaciones familiares.");
        driver.findElement(By.id("longDescription"))
                .sendKeys("Amplia propiedad con jardin, piscina y todas las comodidades.");

        WebElement activar = driver.findElement(By.id("isActive"));
        if (!activar.isSelected()) scrollAndClick(activar);

        scrollAndClick(driver.findElement(By.cssSelector("main form button[type='submit']")));

        // El controller redirige a /listings
        wait.until(ExpectedConditions.urlToBe(baseUrl + "listings"));
        assertTrue(bodyText().contains(nuevoTitulo),
                "El nuevo listing debe verse en el listado");

        // Verificacion a nivel de datos (estilo del profesor: UI + repositorio)
        boolean persistido = listingRepository.findAll().stream()
                .anyMatch(l -> nuevoTitulo.equals(l.getTitle()));
        assertTrue(persistido, "El listing debe quedar persistido en BD");
    }

    @Test
    @DisplayName("FORM-02 | El formulario de edicion llega precargado con los datos del listing")
    void formularioEdicionVienePrecargado() {
        loginHost();
        driver.get(baseUrl + "listings/edit/" + loft.getId());
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("title")));

        assertEquals("Loft Industrial",
                driver.findElement(By.id("title")).getAttribute("value"),
                "El titulo debe venir precargado");
        assertEquals("MADRID",
                new Select(driver.findElement(By.id("city")))
                        .getFirstSelectedOption().getAttribute("value"),
                "La ciudad debe venir seleccionada");
    }

    @Test
    @DisplayName("FORM-03 | El host edita el titulo de su listing y el cambio se refleja")
    void hostEditaTituloDeSuListing() {
        loginHost();
        driver.get(baseUrl + "listings/edit/" + loft.getId());
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("title")));

        WebElement title = driver.findElement(By.id("title"));
        title.clear();
        String tituloEditado = "Loft Industrial Reformado";
        title.sendKeys(tituloEditado);

        scrollAndClick(driver.findElement(By.cssSelector("main form button[type='submit']")));

        wait.until(ExpectedConditions.urlToBe(baseUrl + "listings"));
        assertTrue(bodyText().contains(tituloEditado),
                "El titulo editado debe verse en el listado");
    }

    @Test
    @DisplayName("FORM-04 | Validacion HTML5: precio negativo deja el campo invalido y no envia")
    void precioNegativoNoEnviaFormulario() {
        loginHost();
        driver.get(baseUrl + "listings/new");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("title")));

        // Todos los obligatorios validos salvo el precio (min="1")
        driver.findElement(By.id("title")).sendKeys("Listing con precio invalido");
        new Select(driver.findElement(By.id("type"))).selectByValue("LOFT");
        new Select(driver.findElement(By.id("city"))).selectByValue("BILBAO");
        driver.findElement(By.id("minNights")).sendKeys("1");
        driver.findElement(By.id("maxGuests")).sendKeys("2");
        driver.findElement(By.id("shortDescription")).sendKeys("desc corta");
        driver.findElement(By.id("longDescription")).sendKeys("desc larga");

        WebElement precio = driver.findElement(By.id("pricePerNight"));
        precio.sendKeys("-50");

        scrollAndClick(driver.findElement(By.cssSelector("main form button[type='submit']")));

        assertTrue(driver.getCurrentUrl().contains("/listings/new"),
                "Con precio invalido no debe redirigir; sigue en el formulario");
        assertFalse(isFieldValid(precio),
                "El campo de precio debe quedar invalido por la restriccion min='1'");
    }

    @Test
    @DisplayName("FORM-05 | Validacion HTML5: enviar el formulario vacio no redirige (campos required)")
    void formularioVacioNoEnvia() {
        loginHost();
        driver.get(baseUrl + "listings/new");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("title")));

        scrollAndClick(driver.findElement(By.cssSelector("main form button[type='submit']")));

        assertTrue(driver.getCurrentUrl().contains("/listings/new"),
                "Un formulario vacio no debe enviarse");
        assertFalse(isFieldValid(driver.findElement(By.id("title"))),
                "El titulo es obligatorio y debe quedar invalido");
    }

    // =========================================================
    // Acciones con efectos: toggle y soft-delete (reglas de negocio)
    // =========================================================

    @Test
    @DisplayName("ACTION-01 | El host pausa su listing activo (toggle) y pasa a 'No disponible'")
    void hostPausaSuListing() {
        loginHost();
        driver.get(baseUrl + "listings");
        wait.until(ExpectedConditions.titleContains("Alojamientos disponibles"));

        WebElement botonPausar = driver.findElement(By.cssSelector(
                "form[action$='/listings/toggle/" + loft.getId() + "'] button[type='submit']"));
        scrollAndClick(botonPausar);

        // toggle redirige al detalle /listings/{id}
        wait.until(ExpectedConditions.urlContains("listings/" + loft.getId()));
        assertTrue(bodyText().contains("No disponible"),
                "Tras pausar, el detalle debe mostrar 'No disponible'");

        // Verificacion a nivel de datos
        assertFalse(listingRepository.findById(loft.getId()).orElseThrow().getIsActive(),
                "El listing debe quedar inactivo en BD");
    }

    @Test
    @DisplayName("ACTION-02 | El admin hace soft-delete de un listing sin reservas")
    void adminBorraListingSinReservas() {
        loginAdmin();
        driver.get(baseUrl + "listings/" + chalet.getId()); // chalet no tiene reservas
        wait.until(ExpectedConditions.titleContains("Detalle del Alojamiento"));

        WebElement formBorrado = driver.findElement(
                By.cssSelector("form[action$='/listings/" + chalet.getId() + "/delete']"));
        submitFormByJs(formBorrado); // evita el confirm() nativo

        wait.until(ExpectedConditions.urlToBe(baseUrl + "listings"));
        assertFalse(bodyText().contains("Chalet en el Bosque"),
                "El listing borrado no debe aparecer en el listado");

        // Verificacion a nivel de datos: soft delete
        assertTrue(listingRepository.findById(chalet.getId()).orElseThrow().getDeleted(),
                "El listing debe quedar marcado como deleted=true en BD");
    }

    @Test
    @DisplayName("ACTION-03 | No se puede borrar un listing con reservas activas (regla de negocio)")
    void noSeBorraListingConReservasActivas() {
        loginAdmin();
        driver.get(baseUrl + "listings/" + loft.getId()); // loft tiene booking CONFIRMED
        wait.until(ExpectedConditions.titleContains("Detalle del Alojamiento"));

        WebElement formBorrado = driver.findElement(
                By.cssSelector("form[action$='/listings/" + loft.getId() + "/delete']"));
        submitFormByJs(formBorrado);

        // Redirige de vuelta al detalle con mensaje de error
        wait.until(ExpectedConditions.urlContains("listings/" + loft.getId()));
        WebElement error = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
        assertTrue(error.getText().contains("reservas activas"),
                "Debe avisar que no se puede borrar por reservas activas");

        // No se aplico el borrado
        assertFalse(listingRepository.findById(loft.getId()).orElseThrow().getDeleted(),
                "El listing con reservas activas NO debe quedar borrado");
    }

    // =========================================================
    // Seguridad (Spring Security)
    // =========================================================

    @Test
    @DisplayName("SEC-01 | Un anonimo que intenta acceder al formulario de creacion es redirigido a login")
    void anonimoEnNewRedirigeALogin() {
        driver.get(baseUrl + "listings/new");
        wait.until(ExpectedConditions.urlContains("/login"));
        assertTrue(driver.getCurrentUrl().contains("/login"),
                "El acceso anonimo a /listings/new debe redirigir a /login");
    }

    @Test
    @DisplayName("SEC-02 | Un USER no puede editar listings (403): el formulario no se renderiza")
    void userNoPuedeEditar() {
        loginUser();
        driver.get(baseUrl + "listings/edit/" + loft.getId());

        assertTrue(driver.findElements(By.id("title")).isEmpty(),
                "Un USER no debe poder ver el formulario de edicion (403)");
    }

    // =========================================================
    // Ampliacion: filtros y orden del listado
    // =========================================================

    @Test
    @DisplayName("LIST-07 | Filtro por tipo LOFT devuelve solo el loft")
    void filtroPorTipoLoft() {
        driver.get(baseUrl + "listings?type=LOFT");
        wait.until(ExpectedConditions.titleContains("Alojamientos disponibles"));

        String texto = bodyText();
        assertTrue(texto.contains("Loft Industrial"), "El tipo LOFT debe incluir el loft");
        assertFalse(texto.contains("Apartamento con Vistas"),
                "El tipo LOFT no debe incluir el apartamento");
    }

    @Test
    @DisplayName("LIST-08 | Filtro maxPrice=120 devuelve solo el loft (110), no el apartamento (150)")
    void filtroPorPrecioMaximo() {
        driver.get(baseUrl + "listings?maxPrice=120");
        wait.until(ExpectedConditions.titleContains("Alojamientos disponibles"));

        String texto = bodyText();
        assertTrue(texto.contains("Loft Industrial"), "maxPrice=120 debe incluir el loft (110)");
        assertFalse(texto.contains("Apartamento con Vistas"),
                "maxPrice=120 no debe incluir el apartamento (150)");
    }

    @Test
    @DisplayName("LIST-09 | Combinacion de filtros type=APARTAMENTO + city=ALICANTE devuelve el apartamento")
    void filtroCombinadoTipoYCiudad() {
        driver.get(baseUrl + "listings?type=APARTAMENTO&city=ALICANTE");
        wait.until(ExpectedConditions.titleContains("Alojamientos disponibles"));

        String texto = bodyText();
        assertTrue(texto.contains("Apartamento con Vistas"),
                "La combinacion debe devolver el apartamento");
        assertFalse(texto.contains("Loft Industrial"),
                "La combinacion no debe devolver el loft");
    }

    @Test
    @DisplayName("LIST-10 | Orden por precio descendente coloca el apartamento (150) antes que el loft (110)")
    void ordenPorPrecioDescendente() {
        driver.get(baseUrl + "listings?sort=priceDesc");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("main div.row div.col h5")));

        List<WebElement> titulos = driver.findElements(By.cssSelector("main div.row div.col h5"));
        assertFalse(titulos.isEmpty(), "Debe haber listings");
        assertTrue(titulos.get(0).getText().contains("Apartamento con Vistas"),
                "Con priceDesc el primero debe ser el mas caro (apartamento, 150)");
    }

    // =========================================================
    // Ampliacion: toggle inverso (reactivar) y amenities en formulario
    // =========================================================

    @Test
    @DisplayName("ACTION-04 | El host reactiva un listing pausado (toggle inverso) y pasa a 'Disponible'")
    void hostReactivaListingPausado() {
        loginHost();
        driver.get(baseUrl + "listings");
        wait.until(ExpectedConditions.titleContains("Alojamientos disponibles"));

        // El chalet es del host y esta inactivo -> el boton dice "Activar"
        WebElement botonActivar = driver.findElement(By.cssSelector(
                "form[action$='/listings/toggle/" + chalet.getId() + "'] button[type='submit']"));
        scrollAndClick(botonActivar);

        // toggle redirige al detalle /listings/{id}
        wait.until(ExpectedConditions.urlContains("listings/" + chalet.getId()));
        assertTrue(bodyText().contains("Disponible"),
                "Tras reactivar, el detalle debe mostrar 'Disponible'");

        // Verificacion a nivel de datos
        assertTrue(listingRepository.findById(chalet.getId()).orElseThrow().getIsActive(),
                "El listing debe quedar activo en BD");
    }

    @Test
    @DisplayName("FORM-06 | El host crea un listing con amenities y estas aparecen en el detalle")
    void hostCreaListingConAmenities() {
        loginHost();
        driver.get(baseUrl + "listings/new");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("title")));

        String nuevoTitulo = "Atico con servicios premium";
        driver.findElement(By.id("title")).sendKeys(nuevoTitulo);
        new Select(driver.findElement(By.id("type"))).selectByValue("APARTAMENTO");
        new Select(driver.findElement(By.id("city"))).selectByValue("VALENCIA");
        driver.findElement(By.id("pricePerNight")).sendKeys("200");
        driver.findElement(By.id("minNights")).sendKeys("1");
        driver.findElement(By.id("maxNights")).sendKeys("12");
        driver.findElement(By.id("maxGuests")).sendKeys("4");
        driver.findElement(By.id("shortDescription")).sendKeys("Atico con todas las comodidades.");
        driver.findElement(By.id("longDescription")).sendKeys("Atico de lujo con fibra optica y calefaccion.");

        WebElement activar = driver.findElement(By.id("isActive"));
        if (!activar.isSelected()) scrollAndClick(activar);

        // Seleccionar amenities: el input btn-check esta oculto, se pulsa la etiqueta (label)
        scrollAndClick(driver.findElement(By.cssSelector("label[for='amenity-" + wifi.getId() + "']")));
        scrollAndClick(driver.findElement(By.cssSelector("label[for='amenity-" + heating.getId() + "']")));

        scrollAndClick(driver.findElement(By.cssSelector("main form button[type='submit']")));
        wait.until(ExpectedConditions.urlToBe(baseUrl + "listings"));

        // Localizar el listing recien creado y abrir su detalle
        Long nuevoId = listingRepository.findAll().stream()
                .filter(l -> nuevoTitulo.equals(l.getTitle()))
                .findFirst().orElseThrow()
                .getId();

        driver.get(baseUrl + "listings/" + nuevoId);
        wait.until(ExpectedConditions.titleContains("Detalle del Alojamiento"));

        String texto = bodyText();
        assertTrue(texto.contains("Fibra Optica"),
                "El detalle debe mostrar la amenity 'Fibra Optica'");
        assertTrue(texto.contains("Calefaccion"),
                "El detalle debe mostrar la amenity 'Calefaccion'");
    }
}
