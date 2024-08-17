package tech.xavi.soulsync.service.user;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.dto.account.AccountDto;
import tech.xavi.soulsync.dto.account.AccountWithRole;
import tech.xavi.soulsync.dto.account.SignInResponseDto;
import tech.xavi.soulsync.dto.account.TokenDto;
import tech.xavi.soulsync.dto.shared.AlertData;
import tech.xavi.soulsync.dto.shared.MessageSeverity;
import tech.xavi.soulsync.entity.LoginAttempt;
import tech.xavi.soulsync.entity.Role;
import tech.xavi.soulsync.entity.datafile.Account;
import tech.xavi.soulsync.exception.SoulSyncError;
import tech.xavi.soulsync.exception.SoulSyncException;
import tech.xavi.soulsync.repository.datafile.AccountRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final LoginAttemptService loginAttemptService;
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

    public String getCurrentUserRole() {
        return ((Account) getCurrentUser()).getRole().name();
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

    public SignInResponseDto checkCredentialsAndSignIn(
            HttpServletRequest request,
            AccountDto creds
    ){
        LoginAttempt loginAttempt = loginAttemptService
                .getAttemptByIp(request.getRemoteAddr());
        String attemptMessage = loginAttemptService
                .getMaxAttemptsUserMessage(loginAttempt);
        return Optional.ofNullable(creds)
                .filter(this::checkCredentialsNotEmpty)
                .map(acc -> this.signIn(acc, attemptMessage))
                .orElseThrow( () -> new SoulSyncException(
                        SoulSyncError.ACCOUNT_INPUT_EMPTY,
                        HttpStatus.BAD_REQUEST,
                        attemptMessage
                ));
    }

    private boolean checkCredentialsNotEmpty(AccountDto dto) {
        return dto.getUsername() != null
                && !dto.getUsername().isEmpty()
                && dto.getPassword() != null
                && !dto.getPassword().isEmpty();
    }

    public SignInResponseDto signIn(AccountDto accountDto, String attemptMessage){
        return Optional.ofNullable(accountRepository.get(accountDto.getUsername()))
                .filter( acc -> pwdEncoder.matches(accountDto.getPassword(),acc.getPassword()) )
                .map(this::getSignInPayload)
                .orElseThrow(() -> new SoulSyncException(
                        SoulSyncError.ACC_NOT_FOUND,
                        HttpStatus.BAD_REQUEST,
                        new String[]{ accountDto.getUsername(), attemptMessage }
                        )
                );
    }

    public void deleteAccount(String username) {
        accountRepository.delete(username);
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

    public List<AccountWithRole> getAccounts() {
        return accountRepository
                .getAll()
                .stream()
                .map( acc -> AccountWithRole.builder()
                        .username(acc.getUsername())
                        .role(acc.getRole())
                        .build())
                .toList();
    }

}
