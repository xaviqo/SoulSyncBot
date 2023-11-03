package tech.xavi.soulsync.dto.rest.form;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CfgField {
    SPOTIFY_CLIENT_ID("spotify client id","api","You can get both ID and key from the spotify developers dashboard"),
    SPOTIFY_API_SECRET("spotify client secret","api","You can get both ID and key from the spotify developers dashboard")
    ;
    private final String field;
    private final String section;
    private final String description;
    private String value;

}
