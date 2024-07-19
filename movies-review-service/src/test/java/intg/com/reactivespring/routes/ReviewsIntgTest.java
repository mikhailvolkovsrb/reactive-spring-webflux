package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactiveRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
public class ReviewsIntgTest {
    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ReviewReactiveRepository reviewReactiveRepository;

    static String REVIEW_URL = "/v1/reviews";

    @BeforeEach
    void setUp() {

        var reviewsList = List.of(
                new Review(null, 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review(null, 2L, "Excellent Movie", 8.0));
        reviewReactiveRepository.saveAll(reviewsList)
                .blockLast();
    }

    @AfterEach
    void tearDown() {
        reviewReactiveRepository.deleteAll()
                .block();
    }

    @Test
    void addReview() {
        var review = new Review(null, 1L, "Awesome Movie", 9.0);

        webTestClient
                .post()
                .uri(REVIEW_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Review.class)
                .consumeWith(result -> {
                    var savedReview = result.getResponseBody();
                    assertNotNull(savedReview);
                    assertNotNull(savedReview.getReviewId());
                });
    }

    @Test
    void getAllReviewsInfo() {
        webTestClient
                .get()
                .uri(REVIEW_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(3);
    }

    @Test
    void getAllReviewsInfo2() {
        webTestClient
                .get()
                .uri(REVIEW_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .consumeWith(result -> {
                    result.getResponseBody().stream().forEach(v -> {
                        System.out.printf("reviewId = %s, movieInfoId = %s, comment = %s, rating = %s%n", v.getReviewId(), v.getMovieInfoId(), v.getComment(), v.getRating());
                    });
                })
                .hasSize(3);
    }


    @Test
    void getAllReviewsById() {
        var uri1 = UriComponentsBuilder.fromUriString(REVIEW_URL).queryParam("movieInfoId", "1").buildAndExpand().toUri();
        var uri2 = UriComponentsBuilder.fromUriString(REVIEW_URL).queryParam("movieInfoId", "2").buildAndExpand().toUri();

        webTestClient
                .get()
                .uri(uri1)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(2);

        webTestClient
                .get()
                .uri(uri2)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(1);
    }


    @Test
    void updateReview() {
        var reviewId = reviewReactiveRepository
                .findAll()
                .elementAt(0)
                .map(Review::getReviewId)
                .block(); // transform Mono<String> to String

        var updatedReview = new Review(reviewId, 1L, "Awesome Movie Updated", 10.0);

        webTestClient
                .put()
                .uri(REVIEW_URL + "/{id}", reviewId)
                .bodyValue(updatedReview)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Review.class)
                .consumeWith(result -> {
                    var value = result.getResponseBody();
                    assertNotNull(value);
                    assertNotNull(value.getComment());
                    assertEquals("Awesome Movie Updated", value.getComment());
                    assertEquals(10.0, value.getRating());
                });
    }


    @Test
    void updateReview_authors_version() {
        //given
        var reviewOriginal = new Review(null, 1L, "Awesome Movie", 9.0);
        var savedReview = reviewReactiveRepository.save(reviewOriginal).block();
        var updatedReview = new Review(null, 1L, "Not an Awesome Movie", 8.0);
        //when
        assert savedReview != null;

        webTestClient
                .put()
                .uri(REVIEW_URL + "/{id}", savedReview.getReviewId())
                .bodyValue(updatedReview)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Review.class)
                .consumeWith(reviewResponse -> {
                    var updatedBody = reviewResponse.getResponseBody();
                    assert updatedBody != null;
                    System.out.println("updatedReview : " + updatedBody);
                    assertNotNull(savedReview.getReviewId());
                    assertEquals(8.0, updatedBody.getRating());
                    assertEquals("Not an Awesome Movie", updatedBody.getComment());
                });

    }

    @Test
    void deleteReview() {
        var recordCountBefore = reviewReactiveRepository.findAll().count().block();
        var spamReview = new Review(null, 1L, "Spam spam spam", 0.0);
        var spamReviewUpdated = reviewReactiveRepository.save(spamReview).block();
        assertNotNull(spamReviewUpdated);

        webTestClient
                .delete()
                .uri(REVIEW_URL + "/{id}", spamReviewUpdated.getReviewId())
                .exchange()
                .expectStatus()
                .isNoContent();


        assertNotNull(recordCountBefore);
        //Check response and recordCount after deletion
        webTestClient
                .get()
                .uri(REVIEW_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(recordCountBefore.intValue());

        //Check that exactly needed value was deleted
        StepVerifier.create(reviewReactiveRepository.findById(spamReviewUpdated.getReviewId()))
                .expectSubscription()
                .expectNextCount(0)
                .verifyComplete();
    }
}
