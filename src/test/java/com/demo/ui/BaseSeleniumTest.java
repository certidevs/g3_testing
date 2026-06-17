package com.demo.ui;

import com.demo.model.*;
import com.demo.model.enums.BookingStatus;
import com.demo.model.enums.City;
import com.demo.model.enums.ListingType;
import com.demo.model.enums.Role;
import com.demo.repositories.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ExtendWith(ScreenshotOnFailure.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BaseSeleniumTest {

    @LocalServerPort
    int port;

    @Autowired ListingRepository listingRepository;
    @Autowired BookingRepository bookingRepository;
    @Autowired UserRepository userRepository;
    @Autowired ReviewRepository reviewRepository;
    @Autowired MessageRepository messageRepository;
    @Autowired ConversationRepository conversationRepository;
    @Autowired AmenityRepository amenityRepository;
    @Autowired AmenityLineRepository amenityLineRepository;
    @Autowired PasswordEncoder passwordEncoder;

    String baseUrl;
    WebDriver driver;
    WebDriverWait wait;

    // Usuarios
    User currentUser, adminUser, hostUser;

    // Listings
    Listing loft;        // activo, con amenities, con booking y review
    Listing apartamento; // activo, sin amenities
    Listing chalet;      // inactivo

    // Entidades relacionadas
    Booking booking;         // CONFIRMED, futuro
    Booking bookingPasado;   // CONFIRMED, pasado
    Review review;           // sobre booking pasado
    Amenity wifi;
    Amenity heating;
    Conversation conversation;
    Message messageGuest;
    Message messageHost;

    @BeforeEach
    void setUp() {
        // --- Limpieza en orden correcto (dependencias primero) ---
        messageRepository.deleteAll();
        conversationRepository.deleteAll();
        reviewRepository.deleteAll();
        amenityLineRepository.deleteAll();
        amenityRepository.deleteAll();
        bookingRepository.deleteAll();
        listingRepository.deleteAll();
        userRepository.deleteAll();

        // --- Usuarios ---
        String encodedPass = passwordEncoder.encode("1234");

        adminUser = userRepository.save(User.builder()
                .name("Admin")
                .email("admin@openhouse.com")
                .username("admin@openhouse.com")
                .password(encodedPass)
                .role(Role.ROLE_ADMIN)
                .build());

        hostUser = userRepository.save(User.builder()
                .name("Alex Pro")
                .email("alex@pro.com")
                .username("alex@pro.com")
                .password(encodedPass)
                .role(Role.ROLE_HOST)
                .build());

        currentUser = userRepository.save(User.builder()
                .name("Sonia Lopez")
                .email("sonia@mail.com")
                .username("sonia@mail.com")
                .password(encodedPass)
                .role(Role.ROLE_USER)
                .build());

        // --- Listings ---
        loft = listingRepository.save(Listing.builder()
                .title("Loft Industrial")
                .shortDescription("Espacio abierto y moderno")
                .longDescription("Ubicado en la zona artística, con techos altos y mucha luz")
                .pricePerNight(110.0)
                .minNights(1)
                .maxNights(20)
                .maxGuests(3)
                .imageUrl("/images/1.jpg")
                .registeredAt(LocalDateTime.now())
                .owner(hostUser)
                .city(City.MADRID)
                .isActive(true)
                .type(ListingType.LOFT)
                .build());

        apartamento = listingRepository.save(Listing.builder()
                .title("Apartamento con Vistas")
                .shortDescription("Apartamento moderno con vistas al mar")
                .longDescription("Ubicado en la zona turística, con balcón y piscina")
                .pricePerNight(150.0)
                .minNights(2)
                .maxNights(15)
                .maxGuests(4)
                .imageUrl("/images/2.jpg")
                .registeredAt(LocalDateTime.now())
                .owner(hostUser)
                .city(City.ALICANTE)
                .isActive(true)
                .type(ListingType.APARTAMENTO)
                .build());

        chalet = listingRepository.save(Listing.builder()
                .title("Chalet en el Bosque")
                .shortDescription("Desconexión total")
                .longDescription("Rodeada de naturaleza, perfecta para escapadas")
                .pricePerNight(95.0)
                .minNights(2)
                .maxNights(15)
                .maxGuests(4)
                .imageUrl("/images/3.jpg")
                .registeredAt(LocalDateTime.now())
                .owner(hostUser)
                .city(City.BILBAO)
                .isActive(false)
                .type(ListingType.CHALET)
                .build());

        // --- Amenities (sobre el loft) ---
        wifi = amenityRepository.save(Amenity.builder()
                .name("Fibra Optica")
                .description("600 Mbps")
                .icon("wifi-icon")
                .build());

        heating = amenityRepository.save(Amenity.builder()
                .name("Calefaccion")
                .description("Radiadores inteligentes")
                .icon("heat-icon")
                .build());

        // --- Bookings ---
        booking = bookingRepository.save(Booking.builder()
                .checkIn(LocalDateTime.now().plusDays(1))
                .checkOut(LocalDateTime.now().plusDays(3))
                .status(BookingStatus.CONFIRMED)
                .guest(currentUser)
                .listing(loft)
                .build());

        bookingPasado = bookingRepository.save(Booking.builder()
                .checkIn(LocalDateTime.of(2026, 4, 20, 15, 30))
                .checkOut(LocalDateTime.of(2026, 4, 25, 15, 30))
                .status(BookingStatus.CONFIRMED)
                .guest(currentUser)
                .listing(apartamento)
                .build());

        // --- Conversation y Messages (sobre booking activo) ---
        conversation = conversationRepository.save(Conversation.builder()
                .booking(booking)
                .build());

        messageGuest = messageRepository.save(Message.builder()
                .content("¿Tengo acceso al código de la puerta?")
                .sender(currentUser)
                .conversation(conversation)
                .sentAt(LocalDateTime.now().minusMinutes(10))
                .isRead(true)
                .build());

        messageHost = messageRepository.save(Message.builder()
                .content("Sí, se te enviará 2 horas antes de tu llegada.")
                .sender(hostUser)
                .conversation(conversation)
                .sentAt(LocalDateTime.now().minusMinutes(7))
                .isRead(false)
                .build());

        // --- Review (sobre booking pasado) ---
        review = reviewRepository.save(Review.builder()
                .rating(5)
                .comment("Increíble lugar, muy recomendado")
                .verified(true)
                .creationDate(LocalDateTime.now())
                .booking(bookingPasado)
                .build());

        // --- Driver ---
        baseUrl = "http://localhost:" + port + "/";
        // options para GitHub Actions
        boolean ci = System.getenv("CI") != null; // GitHub Actions pone CI=True
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--window-size=1920,1080");
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.password_manager_leak_detection", false);   // ← la clave del modal
        chromeOptions.setExperimentalOption("prefs", prefs);
        chromeOptions.addArguments("--disable-features=PasswordLeakDetection");
        chromeOptions.addArguments("--force-device-scale-factor=1", "--start-maximized");

        if (ci) {
            chromeOptions.addArguments("--headless=new", "--no-sandbox", "--disable-gpu", "--disable-dev-shm-usage");
        }
        driver = new ChromeDriver(chromeOptions);
        wait = new WebDriverWait(driver, Duration.ofSeconds(30L));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
        messageRepository.deleteAll();
        conversationRepository.deleteAll();
        reviewRepository.deleteAll();
        amenityLineRepository.deleteAll();
        amenityRepository.deleteAll();
        bookingRepository.deleteAll();
        listingRepository.deleteAll();
        userRepository.deleteAll();
    }

    // --- Helpers de login ---
    void loginAdmin() {
        login("admin@openhouse.com", "1234");
    }

    void loginHost() {
        login("alex@pro.com", "1234");
    }

    void loginUser() {
        login("sonia@mail.com", "1234");
    }

    // ✅ DESPUÉS — busca por name, que es lo que tiene tu login.html
    void login(String username, String password) {
        driver.get(baseUrl + "login");
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("input[name='username']")));
        driver.findElement(By.cssSelector("input[name='username']")).sendKeys(username);
        driver.findElement(By.cssSelector("input[name='password']")).sendKeys(password);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(d -> d.getCurrentUrl().equals(baseUrl + "listings"));
    }
}