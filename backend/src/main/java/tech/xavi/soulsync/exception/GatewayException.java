package tech.xavi.soulsync.exception;

public class GatewayException extends RuntimeException {
    public GatewayException(String message) {
        super(message);
    }
    public GatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}