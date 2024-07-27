package tech.xavi.soulsync.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.xavi.soulsync.configuration.globals.ApiRoutes;
import tech.xavi.soulsync.dto.account.AccountDto;
import tech.xavi.soulsync.dto.account.AccountWithRole;
import tech.xavi.soulsync.dto.account.SignInResponseDto;
import tech.xavi.soulsync.service.user.AccountService;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class UserController {

    private final AccountService accountService;

    @PostMapping(ApiRoutes.EP_ACC_SIGN_IN)
    public ResponseEntity<SignInResponseDto> signIn(
            HttpServletRequest request,
            @RequestBody AccountDto accountDto
    ){
        return ResponseEntity.ok(accountService.checkCredentialsAndSignIn(request, accountDto));
    }

    @GetMapping(ApiRoutes.EP_ACC_GET_USERS)
    public ResponseEntity<List<AccountWithRole>> getUsers(){
        return ResponseEntity.ok(accountService.getAccounts());
    }

    @PostMapping(ApiRoutes.EP_ACC_CREATE_USER)
    public ResponseEntity<Void> createUser(@RequestBody AccountDto dto){
        accountService.createAccount(dto,dto.getRole());
        return ResponseEntity.created(null).build();
    }

    @DeleteMapping(ApiRoutes.EP_ACC_DELETE_USER + "/{username}")
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        accountService.deleteAccount(username);
        return ResponseEntity.created(null).build();
    }

}
