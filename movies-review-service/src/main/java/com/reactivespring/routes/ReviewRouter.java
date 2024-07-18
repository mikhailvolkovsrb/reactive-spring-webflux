package com.reactivespring.routes;


import com.reactivespring.handler.ReviewHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class ReviewRouter {

    @Bean
    public RouterFunction<ServerResponse> reviewsRoute(ReviewHandler reviewHandler) {

//        return route()
//                .GET("/v1/helloworld", (request -> ServerResponse.ok().bodyValue("helloworld")))
//                .POST("/v1/reviews", reviewHandler::addReview)
//                .build();

        //or
        return route()
                .nest(path("/v1/reviews"), builder -> {
                    builder.POST("", reviewHandler::addReview)
                            .GET("", (reviewHandler::getReviews))
                            .PUT("/{id}", reviewHandler::updateReview)
                            .DELETE("/{id}", request -> reviewHandler.deleteReview(request));
                })
                .GET("/v1/helloworld", (request -> ServerResponse.ok().bodyValue("helloworld")))
                .build();
    }
}
