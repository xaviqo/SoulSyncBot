package tech.xavi.soulsync.exception;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum SeekProcessError {
    NULL_RESULT_EXCEPTION(100,"Search ID is not available in Slskd API. Possibly expired. 'SlskdSearchStatus' object is NULL")
    ;
    private final int code;
    private final String message;
}
