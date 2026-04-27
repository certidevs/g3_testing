package com.demo.repositories;

import com.demo.model.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ReviewRepositoryTest {

    @Autowired
    ReviewRepository reviewRepository;

    Review rev1;
    Review rev2;

    @BeforeEach
    void setUp(){
        reviewRepository.deleteAll();

        rev1 = Review.builder().comment("Muy buen restaurante").rating(4).verified(true).creationDate(LocalDate.now()).build();
        rev2 = Review.builder().comment("No me ha gustado").rating(1).verified(false).creationDate(LocalDate.of(2019,2,3)).build();
        List<Review>reviews= List.of(rev1, rev2);
        reviewRepository.saveAll(reviews);

    }

    @Test
    @DisplayName("Buscar todas las reviews")
    void findAll(){

        List<Review>lista = reviewRepository.findAll();
        assertNotNull(lista);
        assertEquals(2, lista.size());
        for(Review r : lista){
            System.out.println(r.toString());
        }


    }

    @Test
    @DisplayName("Buscar solo los verificados con true")
    void verificationTrue(){
        List<Review> lista = reviewRepository.findByVerifiedTrue();
        assertNotNull(lista);
        assertEquals(1, lista.size());
        for(Review r : lista){
            System.out.println(r.toString());
        }
    }

    @Test
    @DisplayName("Buscar por solo un review")
    void findByRaiting(){

        List<Review>lista = reviewRepository.findByRating(4);
        assertNotNull(lista);
        assertEquals(1, lista.size());
    }

    @Test
    @DisplayName("Buscar review entre rating")
    void findByRatingBetween(){
        List<Review>lista= reviewRepository.findByRatingBetween(2,5);
        assertNotNull(lista);
        assertEquals(1, lista.size());
        for(Review r : lista){
            System.out.println(r.toString());
        }
    }

    @Test
    @DisplayName("Buscar review entre rating y verificados")
    void findByRatingBetweenAndVerifiedTrue(){
        List<Review>lista= reviewRepository.findByVerifiedTrueAndRatingBetween(2,5);
        assertNotNull(lista);
        assertEquals(1, lista.size());
    }

    @Test
    @DisplayName("Buscar review entre fechas")
    void findByDateBetween(){
        List<Review>lista = reviewRepository.findByCreationDateBetween(LocalDate.of(2020,2,2),LocalDate.now());
        assertNotNull(lista);
        assertEquals(1, lista.size());
    }

    @Test
    @DisplayName("Buscar por verified false")
    void findByVerifiedFalse(){
        List<Review>lista = reviewRepository.findByVerifiedFalse();
        assertNotNull(lista);
        assertEquals(1, lista.size());
    }




    //buscar entre diferentes raiting.
    // buscar solo los review que este verificados
    // buscar review entre fechas



}