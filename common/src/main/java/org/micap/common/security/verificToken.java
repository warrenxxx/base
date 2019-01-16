package org.micap.common.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class verificToken {
    public static Mono<ServerResponse> verifyToken(ServerRequest req, HandlerFunction<ServerResponse> next) {
        System.out.println("tok");
        List<String> headers = req.headers().header("Authorization");
        if (headers.size() == 0) return ServerResponse.status(HttpStatus.UNAUTHORIZED).build();
        String token = headers.get(0);
        try {
            Algorithm algorithm = Algorithm.HMAC256("secretoparanoserrevelado");
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("auth0")
                    .build(); //Reusable verifier instance
            DecodedJWT jwt = verifier.verify(token);
            req.attributes().put("_id", jwt.getClaim("_id").asString());
        } catch (JWTVerificationException exception) {
            return ServerResponse.status(HttpStatus.UNAUTHORIZED).build();
        }
        return next.handle(req);
    }

    public static Mono<ServerResponse> verifyAuthorization(ServerRequest req, HandlerFunction<ServerResponse> next, List<String> roles, ReactiveMongoOperations operations) {
        String _id = req.attributes().get("_id").toString();
        return operations.findOne(new Query(Criteria.where("_id").is(new ObjectId(_id))), Account.class)
                .map(e -> {
                    AtomicBoolean acces = new AtomicBoolean(true);
                    roles.forEach(role -> {
                        if (e.getRoles().indexOf(role) == -1)
                            acces.set(false);
                    });
                    return acces.get();
                }).flatMap(
                        acces->{
                            if(acces==true)
                                return next.handle(req);
                            else
                                return ServerResponse.status(HttpStatus.UNAUTHORIZED).build();
                        }
                );
    }

}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
class Account {
    private String _id;
    private List<String> roles;
}
