package tech.xavi.soulsync.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SoulSyncError {

    ACCOUNT_ALREADY_EXISTS(600,"An account with that username already exists"),
    ACCOUNT_NOT_FOUND(601,"No account has been found with the provided credentials"),
    LOGIN_ERROR(602,"Fatal error occurred creating the user session"),
    TOKEN_NOT_VALID(603,"Token not valid or expired"),

    CONFIGURATION_EXPECTED(700,"SoulSync must be configured before performing this action"),
    ERROR_READING_JSON_CFG(701,"Error reading JSON configuration"),
    CFG_SECTION_NOT_FOUND(702,"Section '%s' is not valid"),
    CFG_FIELD_NOT_FOUND(703,"Field '%s' is not valid"),
    FIELD_VALUE_NOT_VALID(704,"Value '%s' is not valid for field '%s'. %s"),

    GATEWAY_FATAL_ERROR(800,"A fatal error has occurred while calling the %s API"),
    GATEWAY_CALL_ERROR(801,"An error occurred getting a response from the %s API"),
    GATEWAY_JSON_DESERIALIZATION_ERROR(802,"An error occurred while deserializing the JSON response from %s API"),
    SPOTIFY_TOKEN_CALL_ERROR(803,"An error occurred obtaining the Spotify token"),
    NULL_RESULT_EXCEPTION(804,"Slskd search ID is not available. Possibly expired."),


    PLAYLIST_ID_NOT_VALID(900,"The provided spotify playlist is not valid."),
    PLAYLIST_ALREADY_EXISTS(901,"Playlist '%s' already added in database")
    ;

    private final int code;
    private String message;

    SoulSyncError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public SoulSyncError buildMessage(String... values) {
        if (values != null && values.length > 0) {
            this.message = String.format(
                    this.message,
                    values
            );
        }
        return this;
    }
}
