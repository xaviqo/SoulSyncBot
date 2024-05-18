package tech.xavi.soulsync.service.playlist;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.configuration.globals.RequestType;
import tech.xavi.soulsync.dto.playlist.AddPlaylistDto;
import tech.xavi.soulsync.dto.playlist.AddResponseDto;
import tech.xavi.soulsync.dto.shared.AlertData;
import tech.xavi.soulsync.dto.shared.MessageSeverity;
import tech.xavi.soulsync.entity.db.Playlist;
import tech.xavi.soulsync.exception.SoulSyncError;
import tech.xavi.soulsync.exception.SoulSyncException;
import tech.xavi.soulsync.service.user.AccountService;

import java.net.URI;
import java.util.Arrays;

@Service @Log4j2 @RequiredArgsConstructor
public class AddPlaylistService {

    private final PlaylistCreationService playlistCreationService;
    private final AlbumCreationService albumCreationService;
    private final PlaylistMainService playlistMainService;
    private final AccountService accountService;

    public AddResponseDto handleAddPlaylistRequest(AddPlaylistDto request){
        RequestType requestType = getRequestType(request);
        Playlist playlist = handleByRequestType(request,requestType);

        return AddResponseDto.builder()
                .id(playlist.getId())
                .name(playlist.getName())
                .playlistType(playlist.getPlaylistType())
                .cover(playlist.getCover())
                .totalTracks(playlist.getTotalTracks())
                .owner(accountService.getCurrentUser().getUsername())
                .alertData(AlertData.builder()
                        .message(getSuccessMessageByRequestType(requestType,playlist))
                        .severity(MessageSeverity.SUCCESS)
                        .build())
                .build();
    }

    private Playlist handleByRequestType(AddPlaylistDto request, RequestType requestType){
        String spotifyId = getSpotifyIdFromURL(request.url(),requestType);
        if (playlistMainService.existsById(spotifyId))
            throw new SoulSyncException(
                    SoulSyncError.PLAYLIST_ALREADY_ADDED,
                    HttpStatus.BAD_REQUEST,
                    spotifyId
            );

        return switch (requestType){
            case PLAYLIST -> playlistCreationService.addNewPlaylist(
                    spotifyId,
                    request.searchPolicy()
            );
            case ALBUM -> albumCreationService.addNewAlbum(
                    spotifyId,
                    request.searchPolicy()
            );
            case ARTIST -> albumCreationService.addNewDiscography(
                    spotifyId,
                    request.searchPolicy()
            );
        };
    }

    private RequestType getRequestType(AddPlaylistDto request) {
        return Arrays.stream(RequestType.values())
                .filter(type -> {
                    try {
                        return request.url().toURL().getPath().contains(type.getPath());
                    } catch (Exception exception) {
                        SoulSyncException soulSyncException = new SoulSyncException(
                                SoulSyncError.URL_NOT_FOUND,
                                HttpStatus.BAD_REQUEST,
                                request.url()
                        );
                        log.error(soulSyncException.getUserMessage(),exception);
                        throw soulSyncException;
                    }
                })
                .findFirst()
                .orElseThrow(() -> {
                    SoulSyncException soulSyncException = new SoulSyncException(
                            SoulSyncError.INVALID_SPOTIFY_URL,
                            HttpStatus.BAD_REQUEST,
                            request.url()
                    );
                    log.error(soulSyncException.getUserMessage());
                    return soulSyncException;
                });
    }

    private String getSpotifyIdFromURL(URI uri, RequestType requestType){
        String typePath = requestType.getPath();
        String path = uri.getPath();
        int typeIndex = path.indexOf(typePath);
        if (typeIndex != -1) {
            int startIndex = typeIndex + typePath.length();
            int endIndex = path.indexOf("/", startIndex);
            return (endIndex == -1)
                    ? path.substring(startIndex)
                    : path.substring(startIndex, endIndex);
        }
        SoulSyncException soulSyncException = new SoulSyncException(
                SoulSyncError.INVALID_SPOTIFY_URL,
                HttpStatus.BAD_REQUEST,
                new Object[] {
                    requestType.name(),
                    uri.getPath()
                }
        );
        log.error(soulSyncException.getUserMessage());
        throw soulSyncException;
    }

    private String getSuccessMessageByRequestType(RequestType requestType, Playlist playlist){
        return switch (requestType) {
            case PLAYLIST -> String.format(
                        RequestType.PLAYLIST.getUserMessage(),
                        playlist.getName(),
                        playlist.getTotalTracks()
                );
            case ARTIST -> String.format(
                        RequestType.ARTIST.getUserMessage(),
                        playlist.getName()
                );
            case ALBUM -> String.format(
                    RequestType.ALBUM.getUserMessage(),
                    playlist.getName()
            );
        };
    }

}
