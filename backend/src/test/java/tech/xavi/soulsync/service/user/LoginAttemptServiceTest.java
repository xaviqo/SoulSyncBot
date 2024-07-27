package tech.xavi.soulsync.service.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.xavi.soulsync.entity.LoginAttempt;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
class LoginAttemptServiceTest {

    private LoginAttemptService loginAttemptService;

    private final int MAX_ATTEMPTS = 3;

    private final long BLOCK_TIME = 1;

    @BeforeEach
    public void setup() {
        loginAttemptService = new LoginAttemptService(MAX_ATTEMPTS, BLOCK_TIME);
    }

    @Disabled
    @Test
    public void testBlockAfterMaxAttempts() {
        String ip = "192.168.1.1";
        LoginAttempt attempt = null;
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            loginAttemptService.checkAttempt(ip);
            attempt = loginAttemptService.getAttemptByIp(ip);
        }
        assertTrue(attempt.isBlocked());
    }

    @Disabled
    @Test
    public void testUnblockAfterBlockTime() throws InterruptedException {
        String ip = "192.168.1.2";
        LoginAttempt attempt = null;

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            loginAttemptService.checkAttempt(ip);
            attempt = loginAttemptService.getAttemptByIp(ip);
        }

        assertTrue(attempt.isBlocked());

        Thread.sleep(TimeUnit.MINUTES.toMillis(BLOCK_TIME) + 1);

        assertFalse(attempt.isBlocked());
        assertEquals(0, attempt.getAttempts());
    }

    @Disabled
    @Test
    public void testSetRemainingWaitingMinutes() throws InterruptedException {
        String ip = "192.168.1.3";
        LoginAttempt attempt = null;

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            loginAttemptService.checkAttempt(ip);
        }
        Thread.sleep(TimeUnit.MINUTES.toMillis(BLOCK_TIME) / 2);

        attempt = loginAttemptService.getAttemptByIp(ip);
        assertTrue(attempt.getWaitingMinutes() > 0 && attempt.getWaitingMinutes() <= BLOCK_TIME);
    }

}