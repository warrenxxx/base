package org.micap.account_management.controller;

import org.micap.account_management.service.AService;
import org.micap.account_management.dto.Account;
import org.micap.common.security.verificToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.Arrays;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Controller
public class AController {
    @Autowired
    AService service;
    @Autowired
    ReactiveMongoOperations operations;

    @Bean
    RouterFunction<ServerResponse> routerFunctiona() {
        return route(GET("/"), req -> ok().body(service.readall(), Account.class))
                .filter((serverRequest, handlerFunction) -> {
                    return verificToken.verifyAuthorization(serverRequest, handlerFunction, Arrays.asList("rolea", "roleb"), operations);
                }).filter((serverRequest, handlerFunction) -> {
                    return verificToken.verifyToken(serverRequest, handlerFunction);
                });

    }

    @Bean
    RouterFunction<ServerResponse> routerFunctionb() {
        return route(GET("/{id}"), req -> ok().body(service.readone(req.pathVariable("id")), Account.class))
                .filter((serverRequest, handlerFunction) -> {
                    return verificToken.verifyAuthorization(serverRequest, handlerFunction, Arrays.asList("rolec"), operations);

                }).filter((serverRequest, handlerFunction) -> {
                    return verificToken.verifyToken(serverRequest, handlerFunction);
                });

    }

    @Bean
    RouterFunction<ServerResponse> routerFunctionc() {
        return route(POST("/"), req -> ok().body(service.insert(req.bodyToMono(Account.class)), Account.class))
                .filter((serverRequest, handlerFunction) -> {
                    return verificToken.verifyAuthorization(serverRequest, handlerFunction, Arrays.asList(), operations);
                }).filter((serverRequest, handlerFunction) -> {
                    return verificToken.verifyToken(serverRequest, handlerFunction);
                });
    }
    @Bean
    RouterFunction<ServerResponse> routerFunctiond() {
        return route(PUT("/"), req -> ok().body(service.update(req.bodyToMono(Account.class)), Account.class))
                .filter((serverRequest, handlerFunction) -> {
                    return verificToken.verifyAuthorization(serverRequest, handlerFunction, Arrays.asList("roled"), operations);
                }).filter((serverRequest, handlerFunction) -> {
                    return verificToken.verifyToken(serverRequest, handlerFunction);

                });
    }

    @Bean
    RouterFunction<ServerResponse> routerFunctione() {
        return route(DELETE("/"), req -> ok().body(service.delete(req.pathVariable("id")), Boolean.class))
                .filter((serverRequest, handlerFunction) -> {
                    return verificToken.verifyAuthorization(serverRequest, handlerFunction, Arrays.asList("rolea", "roleb"), operations);
                }).filter((serverRequest, handlerFunction) -> {
                    return verificToken.verifyToken(serverRequest, handlerFunction);
                });

    }


}
