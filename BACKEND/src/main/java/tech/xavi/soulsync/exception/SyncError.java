package tech.xavi.soulsync.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SyncError {

    ERROR_READING_JSON_CFG(700,"Error reading JSON configuration"),
    CFG_SECTION_NOT_FOUND(701,"Section '%s' is not valid"),
    CFG_FIELD_NOT_FOUND(702,"Field '%s' is not valid"),
    FIELD_VALUE_NOT_VALID(703,"Value '%s' is not valid for field '%s'. %s"),

    NULL_RESULT_EXCEPTION(800,"Slskd search ID is not available. Possibly expired."),

    PLAYLIST_ID_NOT_VALID(900,"The provided spotify playlist is not valid."),
    PLAYLIST_ALREADY_EXISTS(901,"Playlist '%s' already added in database")
    ;

    private final int code;
    private String message;

    SyncError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public SyncError buildMessage(String... values) {
        if (values != null && values.length > 0) {
            this.message = String.format(
                    this.message,
                    values
            );
        }
        return this;
    }
}
