package tech.xavi.soulsync.dto.rest;

public record AddPlaylistReq(
        String playlist,
        boolean avoidDuplicate,
        boolean addToWatchlist

) {

}
