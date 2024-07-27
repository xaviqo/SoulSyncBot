package tech.xavi.soulsync.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @ToString
public class LoginAttempt {
    private final String ip;
    private int attempts;
    @Setter private long blockStartTime;
    @Setter private boolean isBlocked;
    @Setter private int waitingMinutes;
    @Setter private int waitingSeconds;

    public LoginAttempt(String ip) {
        this.ip = ip;
    }

    public void increaseAttempts() {
        this.attempts++;
    }
    public void resetAttempts() {
        this.attempts = 0;
    }

}
