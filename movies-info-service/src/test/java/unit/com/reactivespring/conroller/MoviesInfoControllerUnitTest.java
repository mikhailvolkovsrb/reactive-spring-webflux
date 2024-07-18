package com.reactivespring.conroller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MoviesInfoService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = MoviesInfoController.class)
@AutoConfigureWebTestClient
public class MoviesInfoControllerUnitTest {

    @Autowired
    private WebTestClient webTestClient;


    @MockBean
    private MoviesInfoService moviesInfoServiceMock;


    static String MOVIES_INFO_URL = "/v1/movieinfos";

    @Test
    void getAllMoviesInfo() {
        List<MovieInfo> movieinfos = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        Flux<MovieInfo> flux = Flux.fromIterable(movieinfos);
        when(moviesInfoServiceMock.getAllMoviesInfos()).thenReturn(flux);

        webTestClient
                .get()
                .uri(MOVIES_INFO_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getMovieInfoById() {
        String movieInfoId = "abc";
        MovieInfo movie = new MovieInfo(movieInfoId, "Dark Knight Rises",
                2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));

        Mono<MovieInfo> expectedMono = Mono.just(movie);
        when(moviesInfoServiceMock.getMovieInfoById(Mockito.any())).thenReturn(expectedMono);

        webTestClient
                .get()
                .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.movieInfoId").isEqualTo(movieInfoId);
    }


    @Test
    void addMovieInfo() {
        String mockId = "mockId";
        var movieInfo = new MovieInfo(mockId, "Batman Begins1",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));
        Mono<MovieInfo> expectedMono = Mono.just(movieInfo);

        when(moviesInfoServiceMock.addMovieInfo(isA(MovieInfo.class))).thenReturn(expectedMono);
        webTestClient
                .post()
                .uri(MOVIES_INFO_URL)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(result -> {
                    var savedMovieInfo = result.getResponseBody();
                    assertNotNull(savedMovieInfo);
                    assertNotNull(savedMovieInfo.getMovieInfoId());
                    assertEquals(mockId, savedMovieInfo.getMovieInfoId());
                });
    }


    @Test
    void updateMovieInfo() {
        var movieInfo = new MovieInfo(null, "Dark Knight Rises1",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        var movieInfoId = "abc";
        var movieInfoUpdated = new MovieInfo(movieInfoId, "Dark Knight Rises1",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        Mono<MovieInfo> expectedMono = Mono.just(movieInfoUpdated);

        when(moviesInfoServiceMock.updateMovieInfo(isA(MovieInfo.class), isA(String.class))).thenReturn(expectedMono);

        webTestClient
                .put()
                .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(result -> {
                    var updatedMovieInfo = result.getResponseBody();
                    assertNotNull(updatedMovieInfo);
                    assertNotNull(updatedMovieInfo.getMovieInfoId());
                    assertEquals("Dark Knight Rises1", updatedMovieInfo.getName());
                });
    }

    /**
     * Validation
     **/
    @Test
    void addMovieInfo_validation() {
        String mockId = "mockId";
        var movieInfo = new MovieInfo(mockId, "",
                -2005, List.of(""), LocalDate.parse("2005-06-15"));
        Mono<MovieInfo> expectedMono = Mono.just(movieInfo);

//        when(moviesInfoServiceMock.addMovieInfo(isA(MovieInfo.class))).thenReturn(expectedMono);

        webTestClient
                .post()
                .uri(MOVIES_INFO_URL)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isBadRequest() //that can be enough, but we can check also additional details below
                .expectBody(String.class)
                .consumeWith(result -> {
                    var responseBody = result.getResponseBody();
                    System.out.println("responseBody=" + responseBody);
                    var expectedMessage = "movieInfo.case must be present,movieInfo.name must be presented,movieInfo.year must be a Positive value";
                    assert responseBody != null;
                    assertEquals(expectedMessage, responseBody);
                });
    }

}
