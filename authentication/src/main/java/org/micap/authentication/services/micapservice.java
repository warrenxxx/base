package org.micap.authentication.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.bson.types.ObjectId;
import org.micap.authentication.Dto.*;
import org.micap.authentication.Repository.AccountRepository;
import org.micap.authentication.utils.Acces_Token;
import org.micap.authentication.utils.Oauth;
import org.micap.authentication.utils.OauthProperty;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class micapservice {

    @Autowired
    JavaMailSender javaMailSender;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    OauthProperty oauthProperty;

    @Value("${secret}")
    private String secret;

    WebClient githubClient = WebClient.create("https://github.com");
    WebClient githubClientApi = WebClient.create("https://api.github.com");

    public Mono<String> accesJwtTokenByGithub(String code) {
        String server = "github";
        Oauth oauth = oauthProperty.getOauthClient().get(server);
        Mono<Account> tmp = this
                .githubClient
                .get()
                .uri("/login/oauth/access_token?client_id=" + oauth.getClientId() + "&client_secret=" + oauth.getSecret() + "&code=" + code)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Acces_Token.class)
                .flatMap(
                        e -> githubClientApi
                                .get()
                                .uri("/user")
                                .header("Authorization", "token " + e.getAccess_token())
                                .accept(MediaType.APPLICATION_JSON)
                                .retrieve()
                                .bodyToMono(Map.class)
                )
                .map(e ->
                        new Account()
                                .set_id(new ObjectId().toString())
                                .setEmail((String) e.get("email"))
                                .setUserName(new UserName((String) e.get("login"), server))
                                .setPhoto((String) e.get("avatar_url"))
                )
                .flatMap(e ->
                        accountRepository.findFirstByUserName(e.getUserName())
                                .switchIfEmpty(accountRepository.insert(e))
                );
        return signAccount(tmp).map(LoginResponse::getToken);
    }

    private Mono<LoginResponse> signAccount(Mono<Account> mono) {
        return mono.map(e -> {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String token = JWT.create()
                    .withIssuer("auth0")
                    .withClaim("_id", e.get_id())
                    .withClaim("userName", e.getUserName().getId())
                    .sign(algorithm);
            return new LoginResponse(token, e.get_id(), e.getEmail(), e.getPerson(), e.getPhoto());
        });
    }

    public Mono<LoginResponse> resetToken(String _id) {
        return Mono.error(new Exception());
    }

    public Mono<LoginResponse> login(Mono<LoginRequest> loginRequestMono) {
        Mono<Account> tmp = loginRequestMono
                .flatMap(
                        body -> accountRepository
                                .findFirstByUserName(new UserName(body.getUserName(), "local"))
                                .filter(account -> BCrypt.checkpw(body.getPassword(), account.getPassword()))

                ).switchIfEmpty(Mono.error(new Exception("password incorrecto")));
        return signAccount(tmp);
    }


    public Mono<String> registerSendEmail(Mono<RegisterRequest> registerrequest) {
        return registerrequest
                .flatMap(e -> accountRepository.existsAccountByUserNameOrEmail(
                        new UserName(e.getUserName(), "local"),
                        e.getEmail()
                        ).flatMap(exist -> {
                            if (exist != true) {
                                return Mono.just(e);
                            } else {
                                System.out.println(exist);
                                return Mono.error(new Exception("Username o email ya exite"));
                            }
                        })
                )
                .map(e -> {
//                    RegisterRequest e = (RegisterRequest) obj;
                    Algorithm algorithm = Algorithm.HMAC256(secret);
                    String token = JWT.create()
                            .withIssuer("auth0")
                            .withClaim("userName", e.getUserName())
                            .withClaim("password", e.getPassword())
                            .withClaim("email", e.getEmail())
                            .withClaim("photo", e.getPhoto())
                            .withClaim("name", e.getName())
                            .withClaim("lastname", e.getLastname())
                            .withClaim("bithDate", e.getBithDate())
                            .sign(algorithm);
                    System.out.println(token);

                    SimpleMailMessage message = new SimpleMailMessage();
                    message.setFrom("algo@hotmial.com");
                    message.setTo(e.getEmail());
                    message.setSubject("sujeto de la empresa");
                    message.setText("paa confirmar su email haga click aqui " + oauthProperty.getHostClientBack() + "/register/" + token);
                    javaMailSender.send(message);
                    return "enviando";
                });
    }

    public Mono<LoginResponse> registerConfirmEmail(String token) {
        DecodedJWT jwt;
        try {

            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("auth0")
                    .build(); //Reusable verifier instance
            jwt = verifier.verify(token);
        } catch (JWTVerificationException exception) {
            return Mono.error(exception);
        }
        return
                this.login(
                        this.accountRepository.insert(
                                new Account()
                                        .set_id(new ObjectId().toString())
                                        .setUserName(new UserName(jwt.getClaim("userName").asString(), "local"))
                                        .setPassword(BCrypt.hashpw(jwt.getClaim("password").asString(), BCrypt.gensalt()))
                                        .setEmail(jwt.getClaim("email").asString())
                                        .setPhoto(jwt.getClaim("photo").asString())
                                        .setPerson(
                                                new Person()
                                                        .setName(jwt.getClaim("name").asString())
                                                        .setLastname(jwt.getClaim("lastname").asString())
                                                        .setBithDate(jwt.getClaim("bithDate").asDate())
                                        )
                        )
                                .map(
                                        e -> new LoginRequest(e.getUserName().getId(), jwt.getClaim("password").asString())
                                )
                );

    }
}

