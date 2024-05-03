package tech.xavi.soulsync.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.configuration.globals.Role;
import tech.xavi.soulsync.dto.account.AccountDto;
import tech.xavi.soulsync.dto.account.SignInResponseDto;
import tech.xavi.soulsync.dto.account.TokenDto;
import tech.xavi.soulsync.dto.shared.AlertData;
import tech.xavi.soulsync.dto.shared.MessageSeverity;
import tech.xavi.soulsync.entity.datafile.Account;
import tech.xavi.soulsync.exception.SoulSyncError;
import tech.xavi.soulsync.exception.SoulSyncException;
import tech.xavi.soulsync.repository.datafile.AccountRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final JwtService jwtService;
    private final PasswordEncoder pwdEncoder;

    public UserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails)
            return (UserDetails) authentication.getPrincipal();
        throw new SoulSyncException(
                SoulSyncError.TOKEN_ERROR,
                HttpStatus.UNAUTHORIZED
        );
    }

    public void createAccount(Account account, Role role){
        boolean isUsernameTaken = accountRepository
                .contains(account.getUsername());
        if (isUsernameTaken)
            throw new SoulSyncException(
                    SoulSyncError.ACC_ALREADY_EXISTS,
                    HttpStatus.BAD_REQUEST
            );
        accountRepository.save(
                Account.builder()
                        .username(account.getUsername())
                        .password(pwdEncoder.encode(account.getPassword()))
                        .role(role)
                        .build()
        );
    }

    public SignInResponseDto checkCredentialsAndSignIn(AccountDto request){
        return Optional.ofNullable(request)
                .filter(dto -> dto.username() != null && !dto.username().isEmpty())
                .filter(dto -> dto.password() != null && !dto.password().isEmpty())
                .map(this::signIn)
                .orElseThrow( () -> new SoulSyncException(
                        SoulSyncError.ACCOUNT_INPUT_EMPTY,
                        HttpStatus.BAD_REQUEST
                ));
    }

    public SignInResponseDto signIn(AccountDto request){
        return Optional.of(accountRepository.get(request.username()))
                .filter( acc -> pwdEncoder.matches(request.password(),acc.getPassword()) )
                .map(this::getSignInPayload)
                .orElseThrow(() -> new SoulSyncException(
                        SoulSyncError.ACC_NOT_FOUND,
                        HttpStatus.BAD_REQUEST,
                        request.username()
                        )
                );
    }

    private SignInResponseDto getSignInPayload(Account account){
        String token = jwtService.generateToken(account);
        return SignInResponseDto.builder()
                .username(account.getUsername())
                .role(account.getRole())
                .token(TokenDto.builder()
                        .token(token)
                        .expirationDate(jwtService.extractExpiration(token))
                        .build())
                .alertData(AlertData.builder()
                        .severity(MessageSeverity.SUCCESS)
                        .message(String.format("User %s successfully logged in",account.getUsername()))
                        .build())
                .build();
    }

}
