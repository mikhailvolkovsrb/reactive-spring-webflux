package com.reactivespring.conroller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MoviesInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1")
@Slf4j
public class MoviesInfoController {
    private MoviesInfoService moviesInfoService;

    public MoviesInfoController(MoviesInfoService moviesInfoService) {
        this.moviesInfoService = moviesInfoService;
    }

    @GetMapping("/movieinfos")
    public Flux<MovieInfo> getAllMoviesInfos(
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "name", required = false) String name) {

        if (year != null) {
            log.info("Year is : {}", year);
            return moviesInfoService.getMovieInfoByYear(year);
        }

        if (name != null) {
            log.info("Name is : {}", name);
            return moviesInfoService.getMovieInfoByName(name);
        }

        return moviesInfoService.getAllMoviesInfos();
    }

//    @GetMapping("/movieinfos/{id}")
//    public Mono<MovieInfo> getAllMoviesInfosById(@PathVariable String id) {
//        return moviesInfoService.getMovieInfoById(id);
//    }

    @GetMapping("/movieinfos/{id}")
    public Mono<ResponseEntity<MovieInfo>> getAllMoviesInfosById(@PathVariable String id) {
        Mono<ResponseEntity<MovieInfo>> response = moviesInfoService.getMovieInfoById(id).map(movieInfo -> {
                    return ResponseEntity.ok().body(movieInfo);
                })
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
        return response;
    }

    @PostMapping("/movieinfos")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MovieInfo> addMovieInfo(@RequestBody @Valid MovieInfo movieInfo) {
        return moviesInfoService.addMovieInfo(movieInfo).log();
    }

    //    @PutMapping("/movieinfos/{id}")
//    public Mono<MovieInfo> updateMovieInfo(@RequestBody MovieInfo updatedMovieInfo, @PathVariable String id) {
//        return moviesInfoService.updateMovieInfo(updatedMovieInfo, id);
//    }
    @PutMapping("/movieinfos/{id}")
    public Mono<ResponseEntity<MovieInfo>> updateMovieInfo(@RequestBody MovieInfo updatedMovieInfo, @PathVariable String id) {
        return moviesInfoService.updateMovieInfo(updatedMovieInfo, id)
                .map(movieInfo -> {
                    return ResponseEntity.ok().body(movieInfo);
                })
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .log();
    }

    @DeleteMapping("/movieinfos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteMovieInfo(@PathVariable String id) {
        return moviesInfoService.deleteMovieInfo(id);
    }
}
