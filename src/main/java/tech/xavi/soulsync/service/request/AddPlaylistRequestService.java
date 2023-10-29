package tech.xavi.soulsync.service.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.dto.gateway.spotify.SpotifySong;
import tech.xavi.soulsync.dto.rest.AddPlaylistReq;
import tech.xavi.soulsync.entity.Playlist;
import tech.xavi.soulsync.service.process.PlaylistService;
import tech.xavi.soulsync.service.process.QueueService;
import tech.xavi.soulsync.service.process.WatchlistService;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Log4j2
@RequiredArgsConstructor
@Service
public class AddPlaylistRequestService {

    private final PlaylistService playlistService;
    private final QueueService queueService;
    private final WatchlistService watchlistService;

    public Playlist addPlaylistRequest(AddPlaylistReq request) throws URISyntaxException {

        String playlistId = obtainIdFromRequest(request);
        int totalTracks = playlistService.getTotalTracks(playlistId);

        List<SpotifySong> spotifyPlaylist = playlistService
                .getPlaylistSongsFromSpotify(playlistId, totalTracks);
        Playlist playlist = playlistService
                .createPlaylistEntity(playlistId,spotifyPlaylist);
        watchlistService
                .updateWatchlist(playlist);

        queueService.seek(playlist);

        return playlist;
    }

    private String obtainIdFromRequest(AddPlaylistReq request) throws URISyntaxException {
        String SPOTIFY_PL_PATH = "/playlist/";
        String requestInput = request.playlist();
        if (!isUrl(requestInput)) {
            return requestInput;
        }
        URI uri = new URI(requestInput);
        String path = uri.getPath();
        if (path.startsWith(SPOTIFY_PL_PATH)) {
            if (path.substring(SPOTIFY_PL_PATH.length()).contains("?")) {
                return path.substring(0, path.indexOf("?"));
            }
            return path.substring(SPOTIFY_PL_PATH.length());
        } else {
            // TODO: Exception. Not a spotify playlist URL
            return "";
        }
    }

    private boolean isUrl(String string){
        return string
                .substring(0,4)
                .equalsIgnoreCase("http");
    }
}
