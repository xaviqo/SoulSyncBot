package tech.xavi.soulsync.repository.file;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import tech.xavi.soulsync.entity.Account;
import tech.xavi.soulsync.repository.KeyValueJsonRepository;

@Repository @RequiredArgsConstructor
public class AccountRepository {

    private final KeyValueJsonRepository<String, Account> keyValueRepo;

    public synchronized Account save(Account account){
        synchronized (keyValueRepo) {
            keyValueRepo.put(
                    account.getUsername().toUpperCase(),
                    account
            );
        }
        return account;
    }

    public synchronized Account get(String username){
        return keyValueRepo.get(username.toUpperCase(), Account.class);
    }

    public synchronized boolean contains(String username){
        return keyValueRepo.containsKey(username.toUpperCase());
    }

}
