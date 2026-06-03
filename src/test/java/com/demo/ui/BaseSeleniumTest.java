package com.demo.ui;

import com.demo.model.*;
import com.demo.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BaseSeleniumTest {

    @LocalServerPort
    int port;
    @Autowired
    ListingRepository listingRepository;
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ReviewRepository reviewRepository;
    @Autowired
    MessageRepository messageRepository;
    @Autowired
    ConversationRepository conversationRepository;
    @Autowired
    AmenityRepository amenityRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    String baseUrl;
    WebDriver driver;
    WebDriverWait wait;

    User currentUser,adminUser,hostUser;
    Listing listing;
    Booking booking;
    Review review;
    Message message;
    Conversation conversation;
    Amenity amenity;

    @BeforeEach
    void setUp(){
        reviewRepository.deleteAll();
        bookingRepository.deleteAll();
        listingRepository.deleteAll();
        userRepository.deleteAll();
        messageRepository.deleteAll();
        conversationRepository.deleteAll();
        amenityRepository.deleteAll();


    }

}
