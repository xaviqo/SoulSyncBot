package tech.xavi.soulsync.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tech.xavi.soulsync.configuration.globals.ApiRoutes;
import tech.xavi.soulsync.dto.account.AccountDto;
import tech.xavi.soulsync.dto.account.SignInResponseDto;
import tech.xavi.soulsync.service.user.AccountService;

@RequiredArgsConstructor
@RestController
public class UserController {

    private final AccountService accountService;

    @PostMapping(ApiRoutes.EP_ACC_SIGN_IN)
    public ResponseEntity<SignInResponseDto> signIn(@RequestBody AccountDto accountDto){
        return ResponseEntity.ok(accountService.checkCredentialsAndSignIn(accountDto));
    }

}
