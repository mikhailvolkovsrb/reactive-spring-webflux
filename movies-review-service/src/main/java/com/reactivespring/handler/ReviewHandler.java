package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.ReviewDataException;
import com.reactivespring.exception.ReviewNotFoundException;
import com.reactivespring.repository.ReviewReactiveRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.stream.Collectors;

@Component //for automatic inject into ReviewRouter.class
@Slf4j
public class ReviewHandler {

    @Autowired
    private Validator validator;

    private ReviewReactiveRepository reviewReactiveRepository;

    public ReviewHandler(ReviewReactiveRepository reviewReactiveRepository) {
        this.reviewReactiveRepository = reviewReactiveRepository;
    }

    public Mono<ServerResponse> getReviews(ServerRequest serverRequest) {
        var movieInfoId = serverRequest.queryParam("movieInfoId");
        if (movieInfoId.isPresent()) {
            var reviewFlux = reviewReactiveRepository.findReviewsByMovieInfoId(Long.valueOf(movieInfoId.get()));
            return buildReviewResponse(reviewFlux);
        } else {
            var reviewFlux = reviewReactiveRepository.findAll();
            return buildReviewResponse(reviewFlux);
        }
    }

    private static Mono<ServerResponse> buildReviewResponse(Flux<Review> reviewFlux) {
        return ServerResponse.ok().body(reviewFlux, Review.class);
    }

    public Mono<ServerResponse> addReview(ServerRequest request) {
        return request.bodyToMono(Review.class)
                .doOnNext(this::validate)
                .flatMap(reviewReactiveRepository::save)
                .flatMap(savedReview -> {
                    return ServerResponse.status(HttpStatus.CREATED)
                            .bodyValue(savedReview);
                });
    }

    private void validate(Review review) {
        var constraintViolations = validator.validate(review);
        log.info("constraintViolations : {}", constraintViolations);
        if (!constraintViolations.isEmpty()) {
            var errorMessage = constraintViolations.stream()
                    .map(ConstraintViolation::getMessage)
                    .sorted()
                    .collect(Collectors.joining(","));
            throw new ReviewDataException(errorMessage);
        }
    }

    ;

    public Mono<ServerResponse> updateReview(ServerRequest request) {
        var reviewId = request.pathVariable("id");
        var existingReview = reviewReactiveRepository.findById(reviewId)
                // We can use it here and get verbose message in client
                // OR second way is below .switchIfEmpty(ServerResponse.notFound().build()) -- then we will see just NOT FOUND RESPONSE
                .switchIfEmpty(Mono.error(new ReviewNotFoundException("Review not found for the given Review id " + reviewId)));

        return existingReview
                .flatMap(review -> request.bodyToMono(Review.class)
                        .map(reqReview -> {
                            review.setComment(reqReview.getComment());
                            review.setRating(reqReview.getRating());
                            return reqReview;
                        })
                        .flatMap(reviewReactiveRepository::save)
                        .flatMap(savedReview -> ServerResponse.ok().bodyValue(savedReview)))
                //.switchIfEmpty(ServerResponse.notFound().build())
                .log();

    }


    public Mono<ServerResponse> deleteReview(ServerRequest request) {
        var reviewId = request.pathVariable("id");
        var existingReview = reviewReactiveRepository.findById(reviewId);

        return existingReview
                .flatMap(review -> reviewReactiveRepository.deleteById(reviewId))
                .then(ServerResponse.noContent().build());
    }
}
