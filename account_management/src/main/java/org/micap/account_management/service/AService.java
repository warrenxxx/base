package org.micap.account_management.service;

import org.micap.account_management.repository.AccountRepositoy;
import org.micap.account_management.dto.Account;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AService {
    @Autowired
    AccountRepositoy accountRepositoy;

    public Mono<Account> insert(Mono<Account> mono){
        return mono.flatMap(accountRepositoy::insert);
    }
    public Flux<Account> readall(){
        return accountRepositoy.findAll();
    }
    public Mono<Account> readone(String id){
        return accountRepositoy.findById(id);
    }
    public Mono<Account> update(Mono<Account> mono){

        return mono.
                map(e->e.setPassword(BCrypt.hashpw(e.getPassword(), BCrypt.gensalt()))).
                flatMap(accountRepositoy::save);
    }

    public Mono<Boolean> delete(String id){
        return accountRepositoy.deleteById(id).hasElement();
    }
}
