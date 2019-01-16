package org.micap.account_management.repository;

import org.micap.account_management.dto.Account;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface AccountRepositoy extends ReactiveMongoRepository<Account,String> {
}
