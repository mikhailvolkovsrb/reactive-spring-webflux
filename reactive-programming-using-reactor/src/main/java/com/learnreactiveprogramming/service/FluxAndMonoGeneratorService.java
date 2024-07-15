package com.learnreactiveprogramming.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class FluxAndMonoGeneratorService {
    /**
     * Simple cases
     */
    //we can say this is a publisher
    public Flux<String> namesFlux() {
        //return Flux.fromArray(array)
        //return Flux.just("alex", "ben", "chloe")
        return Flux.fromIterable(List.of("alex", "ben", "chloe")); // Can be from db or remote service
    }

    public Mono<String> nameMono() {
        return Mono.just("Alex");
    }

    public Flux<String> namesFlux_map() {
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .map(String::toUpperCase);
    }

    /**
     * Immutability section
     */
    public Flux<String> namesFlux_immutability() {
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .map(String::toUpperCase); // "ALEX", "BEN", "CHLOE"
    }

    public Flux<String> namesFlux_immutability2() {
        Flux<String> nameFlux = Flux.fromIterable(List.of("alex", "ben", "chloe"));
        return nameFlux.map(String::toUpperCase); //"ALEX", "BEN", "CHLOE"
    }

    public Flux<String> namesFlux_immutabilityWrong() {
        Flux<String> nameFlux = Flux.fromIterable(List.of("alex", "ben", "chloe"));
        nameFlux.map(String::toUpperCase); //Wrong cause no any effect of map() function (Nobody get the result)
        //so here will be returned original stream
        return nameFlux;
    }


    /**
     * Map section
     */
    public Flux<String> namesFlux_map(int stringLength) {
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .map(s -> s.length() + " - " + s);
    }

    /**
     * FlatMap section
     */
    public Flux<String> namesFlux_flatmap(int stringLength) {
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .flatMap(s -> splitString(s));
//                .log(); //A-L-E-X-C-H-L-O-E
    }

    public Flux<String> splitString(String name) {
        String[] array = name.split("");
        return Flux.fromArray(array);//a,l,e,x
    }

    /**
     * FlatMap async
     */
    public Flux<String> namesFlux_flatmap_async(int stringLength) {
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .flatMap(s -> splitString_withDelay(s));
//                .log(); // A-L-E-X-C-H-L-O-E but mixed order of letters
    }

    public Flux<String> splitString_withDelay(String name) {
        String[] array = name.split("");
        long delay = new Random().nextInt(10);
        return Flux.fromArray(array).delayElements(Duration.ofMillis(delay));
    }


    /**
     * concatMap instead of flatMap for keep the order
     */
    public Flux<String> namesFlux_concatMap(int stringLength) {
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .concatMap(this::splitString_withDelay)
                .log();
    }

    /**
     * concatMap section
     */

    public Mono<List<String>> namesMono_flatMap(int stringLength) {
        return Mono.just("alex")
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .flatMap(this::splitStringMono);
    }

    /**
     * Convert some values to a Mono<List<>> object
     */
    private Mono<List<String>> splitStringMono(String s) {
        String[] charArray = s.split("");
        List<String> charList = List.of(charArray); //ALEX -> A, L, E, X
        return Mono.just(charList).log();
    }


    /**
     * Flux from Mono
     */
    public Flux<String> namesMono_flatMapMany(int stringLength) {
        return Mono.just("alex")
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .flatMapMany(this::splitString);
    }


    /**
     * Transform section
     */
    public Flux<String> namesFlux_transform(int stringLength) {

        Function<Flux<String>, Flux<String>> filtermap = name -> name.map(String::toUpperCase)
                .filter(s -> s.length() > stringLength);

        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .transform(filtermap)
                .flatMap(this::splitString)
                .defaultIfEmpty("default")
                .log();
    }

    /**
     * Switch  if empty
     * Note
     * .defaultIfEmpty - returns a default value;
     * .switchIfEmpty(defaultFlux) - provide an another default source
     */
    public Flux<String> namesFlux_transform_switchIfEmpty(int stringLength) {

        Function<Flux<String>, Flux<String>> filtermap = name -> name.map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .flatMap(s->splitString(s));

        Flux<String> defaultFlux = Flux.just("default")
                .transform(filtermap); //"D","E","F","A","U","L","T"

        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .transform(filtermap)
                .switchIfEmpty(defaultFlux)
                .log();
    }


    /**
     * Combine streams: concat / concatWith
     */
    public Flux<String> explore_concat(){
        Flux<String> abcFlux = Flux.just("A","B","C");
        Flux<String> defFlux = Flux.just("D","E","F");
        return Flux.concat(abcFlux,defFlux).log();
    }

    public Flux<String> explore_concat_with(){
        Flux<String> abcFlux = Flux.just("A","B","C");
        Flux<String> defFlux = Flux.just("D","E","F");
        return abcFlux.concatWith(defFlux).log();
    }

    public Flux<String> explore_concat_Mono(){
        Mono<String> aMono = Mono.just("A");
        Mono<String> bMono = Mono.just("B");
        return aMono.concatWith(bMono).log(); // Flux: A, B
    }

    /**
     * Combine streams: merge / mergeWith
     * The difference is that the values will be mixed instead of concat
     * If the difference in delay is not so big - the order in test can be changed.
     * but Flux.mergeSequential - warranty of order!
     */

    public Flux<String> explore_merge(){
        Flux<String> abcFlux = Flux.just("A","B","C").delayElements(Duration.ofMillis(100));
        Flux<String> defFlux = Flux.just("D","E","F").delayElements(Duration.ofMillis(125));
        return Flux.merge(abcFlux,defFlux).log(); //A-D-B-E-C-F
    }

    public Flux<String> explore_mergeWith(){
        Flux<String> abcFlux = Flux.just("A","B","C").delayElements(Duration.ofMillis(100));
        Flux<String> defFlux = Flux.just("D","E","F").delayElements(Duration.ofMillis(125));
        return abcFlux.mergeWith(defFlux);
    }
    public Flux<String> explore_mergeWithMono(){
        Mono<String> aMono = Mono.just("A");
        Mono<String> bMono = Mono.just("B");
        return aMono.mergeWith(bMono);
    }

    public Flux<String> explore_mergeSequential(){
        Flux<String> abcFlux = Flux.just("A","B","C").delayElements(Duration.ofMillis(100));
        Flux<String> defFlux = Flux.just("D","E","F").delayElements(Duration.ofMillis(100));
        return Flux.mergeSequential(abcFlux, defFlux);
    }

    /**
     * zip and zipWith allows to combine up to 8 publishers
     */
    public Flux<String> explore_zip(){
        Flux<String> abcFlux = Flux.just("A","B","C").delayElements(Duration.ofMillis(100));
        Flux<String> defFlux = Flux.just("D","E","F").delayElements(Duration.ofMillis(125));
        return Flux.zip(abcFlux,defFlux, (first, second) -> first + second); //AD, BE, CF
    }

    public Flux<String> explore_zip_1(){
        Flux<String> abcFlux = Flux.just("A","B","C").delayElements(Duration.ofMillis(100));
        Flux<String> defFlux = Flux.just("D","E","F").delayElements(Duration.ofMillis(125));
        Flux<String> _123Flux = Flux.just("1","2","3").delayElements(Duration.ofMillis(125));
        Flux<String> _456Flux = Flux.just("4","5","6").delayElements(Duration.ofMillis(125));
        return Flux.zip(abcFlux,defFlux, _123Flux, _456Flux)
                .map(t4->t4.getT1()+t4.getT2()+t4.getT3()+t4.getT4()); //AD14, BE25, CF36
    }

    public Flux<String> explore_zipWith(){
        Flux<String> abcFlux = Flux.just("A","B","C").delayElements(Duration.ofMillis(100));
        Flux<String> defFlux = Flux.just("D","E","F").delayElements(Duration.ofMillis(125));
        return abcFlux.zipWith(defFlux, (first, second) -> first + second); //AD, BE, CF
    }

    public Mono<String> explore_zipWithMono(){
        Mono<String> aMono = Mono.just("A");
        Mono<String> bMono = Mono.just("B");
        return aMono.zipWith(bMono)
                .map(t2->t2.getT1() + t2.getT2()); //AB
    }








    public static void main(String[] args) throws InterruptedException {
        FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();
        fluxAndMonoGeneratorService.namesFlux_flatmap_async(3)
                .subscribe(System.out::println);

        TimeUnit.SECONDS.sleep(10);
//
//        fluxAndMonoGeneratorService.namesFlux_flatmap_async(3)
//                .doOnNext(System.out::println)
//                .blockLast(); // Wait for the completion of the Flux
    }
}
