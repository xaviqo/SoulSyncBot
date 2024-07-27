package tech.xavi.soulsync.service.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.entity.LoginAttempt;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {

    private static final String MAX_ATTEMPTS_REACHED_MESSAGE = "You have reached the maximum number of attempts (%s). Wait %smin %ssec to try again";
    private static final String ATTEMPTS_LEFT_MESSAGE = "%s attempts left or you will be temporarily blocked";
    private final int MAX_ATTEMPTS;
    private final long BLOCK_TIME;
    private final Map<String, LoginAttempt> attemptsByIpMap;

    public LoginAttemptService(
            @Value("${tech.xavi.soulsync.cfg.login-attempt.max-try}") int maxAttempts,
            @Value("${tech.xavi.soulsync.cfg.login-attempt.block-time-min}") long blockTime
    ) {
        this.attemptsByIpMap = new ConcurrentHashMap<>();
        this.MAX_ATTEMPTS = maxAttempts;
        this.BLOCK_TIME = TimeUnit.MINUTES.toMillis(blockTime);
    }

    public void checkAttempt(String ip) {
        LoginAttempt attempt = getAttemptByIp(ip);

        if (isBlockedFirstTime(attempt))
            blockAttempt(attempt);
        else if (isAlreadyBlocked(attempt))
            setRemainingWaitingMinutes(attempt);
        else if (attempt.isBlocked())
            resetAttempt(attempt);
        else
            attempt.increaseAttempts();

        saveAttempt(attempt);
    }

    public void removeAttemptByIp(String ip) {
        attemptsByIpMap.remove(ip);
    }

    public LoginAttempt getAttemptByIp(String ip) {
        return attemptsByIpMap.computeIfAbsent(ip, LoginAttempt::new);
    }

    public String getMaxAttemptsUserMessage(LoginAttempt loginAttempt) {
        if (!loginAttempt.isBlocked() && loginAttempt.getAttempts() == MAX_ATTEMPTS)
            return String.format(
                    MAX_ATTEMPTS_REACHED_MESSAGE,
                    MAX_ATTEMPTS,
                    TimeUnit.MILLISECONDS.toMinutes(BLOCK_TIME),
                    "00"
            );
        if (loginAttempt.isBlocked())
            return String.format(
                    MAX_ATTEMPTS_REACHED_MESSAGE,
                    MAX_ATTEMPTS,
                    loginAttempt.getWaitingMinutes(),
                    loginAttempt.getWaitingSeconds()
            );
        return String.format(
                ATTEMPTS_LEFT_MESSAGE,
                (MAX_ATTEMPTS - loginAttempt.getAttempts())
        );
    }

    private boolean isAlreadyBlocked(LoginAttempt attempt) {
        return attempt.isBlocked() && (attempt.getBlockStartTime() + BLOCK_TIME) > System.currentTimeMillis();
    }

    private void setRemainingWaitingMinutes(LoginAttempt attempt) {
        long remainingTime = (attempt.getBlockStartTime() + BLOCK_TIME) - System.currentTimeMillis();
        int remainingMinutes = (int) TimeUnit.MILLISECONDS.toMinutes(remainingTime);
        int remainingSeconds = (int) (TimeUnit.MILLISECONDS.toSeconds(remainingTime) % 60);
        attempt.setWaitingMinutes(remainingMinutes);
        attempt.setWaitingSeconds(remainingSeconds);

    }

    private void resetAttempt(LoginAttempt attempt) {
        attempt.setBlocked(false);
        attempt.setBlockStartTime(0);
        attempt.setWaitingMinutes(0);
        attempt.resetAttempts();
    }

    private void blockAttempt(LoginAttempt attempt) {
        attempt.setBlocked(true);
        attempt.setBlockStartTime(System.currentTimeMillis());
        attempt.setWaitingMinutes((int) TimeUnit.MILLISECONDS.toMinutes(BLOCK_TIME));
    }

    private boolean isBlockedFirstTime(LoginAttempt attempt) {
        return !attempt.isBlocked() && attempt.getAttempts() >= MAX_ATTEMPTS;
    }

    private void saveAttempt(LoginAttempt attempt) {
        attemptsByIpMap.put(attempt.getIp(), attempt);
    }


}
