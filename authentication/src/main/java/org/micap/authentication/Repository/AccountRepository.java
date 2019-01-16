package org.micap.authentication.Repository;

import org.micap.authentication.Dto.Account;
import org.micap.authentication.Dto.UserName;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface AccountRepository extends ReactiveMongoRepository<Account,String> {
    Mono<Account> findFirstByUserName(UserName userName);
    Mono<Long> countByUserName(UserName userName);
    Mono<Boolean> existsAccountByUserNameOrEmail(UserName userName,String email);
}
