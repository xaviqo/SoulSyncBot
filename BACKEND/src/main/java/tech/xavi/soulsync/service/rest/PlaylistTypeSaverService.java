package tech.xavi.soulsync.service.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.configuration.security.SoulSyncException;
import tech.xavi.soulsync.dto.gateway.spotify.SpotifyAlbum;
import tech.xavi.soulsync.dto.gateway.spotify.SpotifySong;
import tech.xavi.soulsync.entity.Playlist;
import tech.xavi.soulsync.entity.sub.SoulSyncError;
import tech.xavi.soulsync.entity.sub.SpotifyPathType;
import tech.xavi.soulsync.service.main.AlbumService;
import tech.xavi.soulsync.service.main.PlaylistService;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PlaylistTypeSaverService {

    private final PlaylistService playlistService;
    private final AlbumService albumService;

    public void handleUserRequest(String id, SpotifyPathType type) {
        if (playlistService.isPlaylistInDB(id)) {
            throw new SoulSyncException(
                    SoulSyncError.PLAYLIST_ALREADY_EXISTS.buildMessage(id),
                    HttpStatus.BAD_REQUEST
            );
        }
        switch (type){
            case PLAYLIST -> handlePlaylistRequest(id);
            case ARTIST -> handleDiscographyRequest(id);
            case ALBUM -> handleAlbumRequest(id);
        }
    }

    private void handlePlaylistRequest(String playlistId){
        Playlist playlist = playlistService.savePlaylist(playlistId);
        List<SpotifySong> playlistSongs = playlistService.getPlaylistSongsFromSpotify(playlistId);
        playlistService.addSongsToPlaylist(playlist,playlistSongs);
    }

    private void handleDiscographyRequest(String artistId){
        albumService
                .getArtistDiscography(artistId)
                .getItems()
                .forEach(album -> this.handleAlbumRequest(album.getId()) );
    }

    private void handleAlbumRequest(String albumId){
        saveAlbumSongs(albumService.getAlbum(albumId));
    }

    private void saveAlbumSongs(SpotifyAlbum album){
        Playlist albumPlaylist = playlistService.saveAlbum(album);
        List<SpotifySong> spotifySongs = albumService.fromSpotifyAlbumToSpotifySongs(album);
        playlistService.addSongsToPlaylist(albumPlaylist,spotifySongs);
    }

}
