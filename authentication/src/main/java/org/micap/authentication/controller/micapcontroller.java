package org.micap.authentication.controller;

import org.micap.authentication.Dto.LoginRequest;
import org.micap.authentication.Dto.LoginResponse;
import org.micap.authentication.Dto.RegisterRequest;
import org.micap.authentication.services.micapservice;
import org.micap.authentication.utils.OauthProperty;
import org.micap.common.security.verificToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;


@Controller
public class micapcontroller {

    @Autowired
    OauthProperty oauthProperty;

    @Bean
    RouterFunction routerFunction(micapservice micapservice) {
        return route(GET("/authentication/github"), req ->
                micapservice.accesJwtTokenByGithub(req.queryParam("code").get())
                        .flatMap(
                                e -> ServerResponse.temporaryRedirect(URI.create(oauthProperty.getHostClientFront() + "/warren/" + e))
                                        .build()
                        )
        );
    }

    @Bean
    RouterFunction resetToken(micapservice micapservice) {
        return route(GET("/ggg"), req -> ok().body(micapservice.resetToken((String) req.attributes().get("_id")), LoginResponse.class)
        )
        .filter(verificToken::verifyToken);
    }
    @Bean
    RouterFunction localAuhtentication(micapservice micapservice) {
        return route(POST("/login"), req -> ok().body(micapservice.login(req.bodyToMono(LoginRequest.class)), LoginResponse.class)
        )
                .andRoute(POST("/register"),req->ok().body(micapservice.registerSendEmail(req.bodyToMono(RegisterRequest.class)),String.class))
                .andRoute(GET("/register/{token}"),req->ok().body(micapservice.registerConfirmEmail(req.pathVariable("token")),LoginResponse.class));
    }

}
