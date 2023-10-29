package tech.xavi.soulsync.dto.rest;

import lombok.AllArgsConstructor;
import lombok.Data;
import tech.xavi.soulsync.entity.ResponseStatus;

@AllArgsConstructor
@Data
public class ResponseWrapper<T> {

    T data;
    ResponseStatus status;
    long moment;

    public static <U> ResponseWrapper<U> wrapResponse(Object object, Class<U> responseType) {
        return new ResponseWrapper<>(
                (U) object,
                ResponseStatus.OK,
                System.currentTimeMillis()
        );
    }

    public static <U> ResponseWrapper<U> wrapResponse(Object object, ResponseStatus status, Class<U> responseType) {
        return new ResponseWrapper<>(
                (U) object,
                status,
                System.currentTimeMillis()
        );
    }
}
