package tech.xavi.soulsync.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tech.xavi.soulsync.dto.shared.MessageSeverity;

@AllArgsConstructor @Getter
public enum SoulSyncError {

    INIT_SETUP_ERROR_CFG(MessageSeverity.ERROR,"Invalid gateway or setup already done"),
    ACC_ALREADY_EXISTS(MessageSeverity.WARN,"Account %s already exists"),
    ACC_NOT_FOUND(MessageSeverity.WARN,"Account %s not found"),
    ACCOUNT_INPUT_EMPTY(MessageSeverity.WARN,"Credentials cannot be empty"),
    TOKEN_ERROR(MessageSeverity.WARN,"Session expired or invalid"),

    GATEWAY_ERROR(MessageSeverity.ERROR,"%s occurred while calling the %s waiting a response type %s"),

    INVALID_VALUE(MessageSeverity.ERROR,"Value %s not valid for field %s, required data type %s"),

    URL_NOT_FOUND(MessageSeverity.WARN,"You must provide a Spotify URL. Value provided: %s"),
    INVALID_SPOTIFY_URL(MessageSeverity.WARN,"The provided URL (%s) does not appear to be a valid Spotify URL"),
    SPOTIFY_ID_NOT_FOUND(MessageSeverity.ERROR,"Could not get the %s Id from the requested URL (%s)"),
    COVER_NOT_FOUND(MessageSeverity.WARN,"The playlist '%S' (%s) has no associated cover"),
    PLAYLIST_ALREADY_ADDED(MessageSeverity.WARN,"The playlist '%s' is already added"),
    MINIMUM_SEARCH_POLICY(MessageSeverity.WARN, "It is not allowed to delete the last available search policy")
    ;
    private final MessageSeverity messageSeverity;
    private final String message;

}
