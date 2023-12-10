package tech.xavi.soulsync.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tech.xavi.soulsync.configuration.constants.EndPoint;
import tech.xavi.soulsync.dto.service.AccountRequest;
import tech.xavi.soulsync.dto.service.SignInResponse;
import tech.xavi.soulsync.service.auth.AccountService;

@RequiredArgsConstructor
@RestController
public class AccessController {

    private final AccountService accountService;

    @PostMapping(EndPoint.LOGIN)
    public ResponseEntity<SignInResponse> login(@RequestBody AccountRequest request){
        return ResponseEntity.ok(accountService.signIn(request));
    }

}
