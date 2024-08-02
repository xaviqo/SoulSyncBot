package tech.xavi.soulsync.repository.datafile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;
import tech.xavi.soulsync.entity.datafile.Account;

import java.util.Map;

@Repository
public class AccountRepository extends KeyValueJsonRepository<String,Account>{

    public AccountRepository(ObjectMapper mapper) {
        super(mapper, "account");
    }

    public synchronized Account save(Account account){
        synchronized (this.data) { put(account.getUsername().toUpperCase(), account); }
        return account;
    }

    public synchronized Account get(String username){
        return get(username.toUpperCase(), Account.class);
    }

    public synchronized boolean contains(String username){
        return containsKey(username.toUpperCase());
    }

    public synchronized void delete(String username){
        remove(username.toUpperCase());
    }

    @Override
    protected TypeReference<Map<String, Account>> getTypeReference() {
        return new TypeReference<Map<String, Account>>() {};
    }
}
