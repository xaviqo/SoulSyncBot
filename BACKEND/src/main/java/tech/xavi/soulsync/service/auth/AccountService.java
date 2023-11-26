package tech.xavi.soulsync.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.configuration.security.SoulSyncException;
import tech.xavi.soulsync.dto.rest.AccountRequest;
import tech.xavi.soulsync.dto.rest.SignInResponse;
import tech.xavi.soulsync.entity.Account;
import tech.xavi.soulsync.entity.SoulSyncError;
import tech.xavi.soulsync.repository.AccountRepository;

import static tech.xavi.soulsync.configuration.constants.ConfigurationFinals.DEFAULT_ADMIN_PASS;
import static tech.xavi.soulsync.configuration.constants.ConfigurationFinals.DEFAULT_ADMIN_USER;

@Log4j2
@RequiredArgsConstructor
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public void createAccount(String user, String password){
        if (accountRepository.findAccountByUsername(user).isPresent()){
            throw new SoulSyncException(
                    SoulSyncError.ACCOUNT_ALREADY_EXISTS,
                    HttpStatus.BAD_REQUEST
            );
        }
        accountRepository.save(
                Account.builder()
                        .username(user)
                        .password(passwordEncoder.encode(password))
                        .build()
        );
    }

    public SignInResponse signIn(AccountRequest request){
        return accountRepository.findAccountByUsername(request.username())
                .filter( account -> passwordEncoder.matches(request.password(),account.getPassword()) )
                .map( account -> getSignInPayload(account.getUsername()))
                .orElseThrow( () -> new SoulSyncException(
                        SoulSyncError.ACCOUNT_NOT_FOUND,
                        HttpStatus.BAD_REQUEST
                ));
    }

    public void updateAccount(String user, String password){
        accountRepository.findAccountByUsername(user)
                .ifPresent( account -> {
                    account.setPassword(passwordEncoder.encode(password));
                    accountRepository.save(account);
                });
    }

    private SignInResponse getSignInPayload(String username) {
        return accountRepository.findAccountByUsername(username)
                .map( account -> SignInResponse.builder()
                            .token(jwtService.generateAccessToken(account))
                            .user(account.getUsername())
                            .build()
                )
                .orElseThrow( () -> new SoulSyncException(
                        SoulSyncError.LOGIN_ERROR,
                        HttpStatus.INTERNAL_SERVER_ERROR
                ));
    }

    public void createDefaultAdmin(){
        if (accountRepository.findAccountByUsername(DEFAULT_ADMIN_USER).isEmpty()){
            createAccount(
                    DEFAULT_ADMIN_USER,
                    DEFAULT_ADMIN_PASS
            );
            log.info("[run] - Default Admin created: '{}' - '{}'",DEFAULT_ADMIN_USER,DEFAULT_ADMIN_PASS);
        }
    }

}
