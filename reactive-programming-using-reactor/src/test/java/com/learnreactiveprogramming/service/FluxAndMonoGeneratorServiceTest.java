package com.learnreactiveprogramming.service;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FluxAndMonoGeneratorServiceTest {
    FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();


    @Test
    void namesFlux() {
        Flux<String> namesFlux = fluxAndMonoGeneratorService.namesFlux();

        /* Note: create() will subscribe automatically so that's why don't need to call subscribe()*/
        StepVerifier.create(namesFlux)
                .expectNext("alex", "ben", "chloe")//check all values
                .verifyComplete();

        StepVerifier.create(namesFlux)
                .expectNextCount(3) //check the count
                .verifyComplete();

        StepVerifier.create(namesFlux)
                .expectNext("alex")//check all values
                .expectNextCount(2) //remains count
                .verifyComplete();

        StepVerifier.create(namesFlux)
                .expectNext("alex")//check first value
                .expectNext("ben")//check second value
                .expectNextCount(1) //check remains count
                .verifyComplete();
    }

    @Test
    void namesFlux_map() {
        Flux<String> namesFlux = fluxAndMonoGeneratorService.namesFlux_map();
        StepVerifier.create(namesFlux)
                .expectNext("ALEX", "BEN", "CHLOE")
                .verifyComplete();

    }

    @Test
    void namesFlux_immutability() {
        Flux<String> namesFlux = fluxAndMonoGeneratorService.namesFlux_immutability();
        StepVerifier.create(namesFlux)
                .expectNext("ALEX", "BEN", "CHLOE")
                .verifyComplete();
    }

    @Test
    void namesFlux_immutability2() {
        Flux<String> namesFlux = fluxAndMonoGeneratorService.namesFlux_immutability2();
        StepVerifier.create(namesFlux)
                .expectNext("ALEX", "BEN", "CHLOE")
                .verifyComplete();
    }

    @Test
    void namesFlux_immutabilityWrong() {
        Flux<String> namesFlux = fluxAndMonoGeneratorService.namesFlux_immutabilityWrong();
        StepVerifier.create(namesFlux)
                .expectNext("alex", "ben", "chloe")
                .verifyComplete();
    }

    @Test
    void testNamesFlux_map() {
        int stringLength = 3;
        Flux<String> namesFlux = fluxAndMonoGeneratorService.namesFlux_map(stringLength);
        StepVerifier.create(namesFlux)
                .expectNext("4 - ALEX", "5 - CHLOE")
                .verifyComplete();

    }

    @Test
    void namesFlux_flatmap() {
        int stringLength = 3;
        Flux<String> namesFlux = fluxAndMonoGeneratorService.namesFlux_flatmap(stringLength);
        StepVerifier.create(namesFlux)
                .expectNext("A","L","E","X","C","H","L","O","E")
                .verifyComplete();
    }

    @Test
    void namesFlux_flatmap_async() {
        int stringLength = 3;
        Flux<String> namesFlux = fluxAndMonoGeneratorService.namesFlux_flatmap_async(stringLength);
        StepVerifier.create(namesFlux)
//                .expectNext("A","L","E","X","C","H","L","O","E") -- will fail cause order(flatmap is async)
                .expectNextCount(9)
                .verifyComplete();
    }

    @Test
    void namesFlux_concatMap() {
        int stringLength = 3;
        Flux<String> namesFlux = fluxAndMonoGeneratorService.namesFlux_concatMap(stringLength);
        StepVerifier.create(namesFlux)
                .expectNext("A","L","E","X","C","H","L","O","E")//works fine cause concatMap()
                .verifyComplete();
    }

    @Test
    void namesMono_flatMap() {
        int stringLength = 3;
        Mono<List<String>> namesMono = fluxAndMonoGeneratorService.namesMono_flatMap(stringLength);
        StepVerifier.create(namesMono)
                .expectNext(List.of("A","L","E","X"))
                .verifyComplete();
    }

    @Test
    void namesMono_flatMapMany() {
        int stringLength = 3;
        Flux<String> namesMono = fluxAndMonoGeneratorService.namesMono_flatMapMany(stringLength);
        StepVerifier.create(namesMono)
                .expectNext("A","L","E","X")
                .verifyComplete();
    }

    @Test
    void namesFlux_transform() {
        int stringLength = 3;
        Flux<String> namesFlux = fluxAndMonoGeneratorService.namesFlux_transform(stringLength);
        StepVerifier.create(namesFlux)
                .expectNext("A","L","E","X","C","H","L","O","E")
                .verifyComplete();

    }

    @Test
    void namesFlux_transform1_expect_empty_object() {
        int stringLength = 6;
        Flux<String> namesFlux = fluxAndMonoGeneratorService.namesFlux_transform(stringLength);
        StepVerifier.create(namesFlux)
                .expectNext("default")
                .verifyComplete();

    }

    @Test
    void namesFlux_transform_switchIfEmpty() {
        int stringLength = 6;
        Flux<String> namesFlux = fluxAndMonoGeneratorService.namesFlux_transform_switchIfEmpty(stringLength);
        StepVerifier.create(namesFlux)
                .expectNext("D","E","F","A","U","L","T")
                .verifyComplete();

    }

    @Test
    void explore_concat() {
        Flux<String> concatFlux = fluxAndMonoGeneratorService.explore_concat();
        StepVerifier.create(concatFlux)
                .expectNext("A","B","C","D","E","F")
                .verifyComplete();
    }

    @Test
    void explore_merge() {
        Flux<String> value = fluxAndMonoGeneratorService.explore_merge();
        StepVerifier.create(value)
                .expectNext("A","D","B","E","C","F")
                .verifyComplete();
    }

    @RepeatedTest(10)
    void explore_merge_sequential() {
        Flux<String> value = fluxAndMonoGeneratorService.explore_mergeSequential();
        StepVerifier.create(value)
                .expectNext("A","B","C","D","E","F")
                .verifyComplete();
    }

    @Test
    void explore_zip() {
        Flux<String> value = fluxAndMonoGeneratorService.explore_zip();
        StepVerifier.create(value)
                .expectNext("AD","BE","CF")
                .verifyComplete();
    }

    @Test
    void explore_zip_1() {
        Flux<String> value = fluxAndMonoGeneratorService.explore_zip_1();
        StepVerifier.create(value)
                .expectNext("AD14","BE25","CF36")
                .verifyComplete();
    }
}