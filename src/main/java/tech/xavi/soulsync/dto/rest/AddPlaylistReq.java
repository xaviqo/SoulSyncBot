package tech.xavi.soulsync.dto.rest;

public record AddPlaylistReq(
        String playlist,
        boolean update,
        boolean avoidDuplicates,
        boolean avoidRemix,
        boolean avoidLives
) {

}
