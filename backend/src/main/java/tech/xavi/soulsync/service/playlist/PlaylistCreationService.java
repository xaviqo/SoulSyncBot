package tech.xavi.soulsync.service.playlist;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.configuration.globals.PlaylistType;
import tech.xavi.soulsync.dto.gateway.spotify.SpotifyPlaylistDto;
import tech.xavi.soulsync.entity.db.DownloadList;
import tech.xavi.soulsync.entity.db.Playlist;
import tech.xavi.soulsync.entity.db.SpotifySong;
import tech.xavi.soulsync.service.download.downloadlist.DownloadListCreationService;
import tech.xavi.soulsync.service.integration.SpotifyGatewayService;
import tech.xavi.soulsync.service.song.SongService;
import tech.xavi.soulsync.service.user.AccountService;

import java.util.Set;

@Log4j2
@Service
@RequiredArgsConstructor
public class PlaylistCreationService {

    private final SpotifyGatewayService spotifyGatewayService;
    private final PlaylistMainService playlistMainService;
    private final SongService songService;
    private final AccountService accountService;
    private final DownloadListCreationService downloadListCreationService;

    public Playlist addNewPlaylist(String spotifyId, String searchPolicyId){
        SpotifyPlaylistDto spotifyPlaylistDto = spotifyGatewayService
                .getPlaylistDetails(spotifyId);
        DownloadList downloadList = downloadListCreationService
                .createDownloadList(spotifyId,searchPolicyId);
        Set<SpotifySong> spotifySongs = songService
                .fetchSongsFromSpotify(spotifyPlaylistDto);
        String playlistCover = playlistMainService
                .getPlaylistCoverUrl(spotifyPlaylistDto.getImages());
        Playlist playlist = playlistMainService
                .savePlaylist(Playlist.builder()
                .id(spotifyId)
                .name(spotifyPlaylistDto.getName())
                .totalTracks(spotifySongs.size())
                .cover(playlistCover)
                .songs(spotifySongs)
                .owner(accountService.getCurrentUser().getUsername())
                .lastUpdate(System.currentTimeMillis())
                .playlistType(PlaylistType.PLAYLIST)
                .downloadLists(Set.of(downloadList))
                .build());

        downloadListCreationService
                .createSlskdDownloads(playlist, downloadList);

        return playlist;
    }

}
