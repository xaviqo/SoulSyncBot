package tech.xavi.soulsync.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class SoulSyncException extends RuntimeException {

    private final SoulSyncError error;
    private final HttpStatus httpStatus;
    private final Object[] errorValues;

    public SoulSyncException(SoulSyncError error, HttpStatus status) {
        this.error = error;
        this.httpStatus = status;
        this.errorValues = null;
    }

    public SoulSyncException(SoulSyncError error, HttpStatus status, Object[] errorValues) {
        this.error = error;
        this.httpStatus = status;
        this.errorValues = errorValues;
    }

    public SoulSyncException(SoulSyncError error, HttpStatus status, Object value) {
        this.error = error;
        this.httpStatus = status;
        this.errorValues = new Object[]{value};
    }

    public String getUserMessage() {
        if (errorValues != null && errorValues.length > 0)
            return String.format(
                    this.error.getMessage(),
                    errorValues
            );
        return "Error message undefined";
    }

}

