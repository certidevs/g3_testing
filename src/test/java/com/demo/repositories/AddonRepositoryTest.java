package com.demo.repositories;

import com.demo.model.Addon;
import com.demo.model.Listing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@DataJpaTest
class AddonRepositoryTest {
    @Autowired
    AddonRepository addonRepository;

    Addon add1;
    Addon add2;
    Addon add3;

    @BeforeEach
    void setUp() {
        addonRepository.deleteAll();
        add1 = addonRepository.save(Addon.builder().title("Addon 1").price(30.0).build());
        add2 = addonRepository.save(Addon.builder().title("Addon 2").price(20.0).build());
        add3 = addonRepository.save(Addon.builder().title("Addon 3").price(30.0).build());
    }

  @Test
    void findByPrice() {


        List<Addon> addonsPrecio10= addonRepository.findByPrice(30.0);
        assertFalse(addonsPrecio10.isEmpty());
        assertEquals(2, addonsPrecio10.size());
        assertTrue(addonsPrecio10.contains(add1));
        assertTrue(addonsPrecio10.contains(add3));
    }
}