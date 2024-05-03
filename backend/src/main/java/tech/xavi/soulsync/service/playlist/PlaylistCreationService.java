package tech.xavi.soulsync.service.playlist;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.configuration.globals.PlaylistType;
import tech.xavi.soulsync.configuration.globals.RequestType;
import tech.xavi.soulsync.dto.gateway.spotify.SpotifyPlaylistDto;
import tech.xavi.soulsync.dto.gateway.spotify.SpotifySongDto;
import tech.xavi.soulsync.entity.datafile.DownloadList;
import tech.xavi.soulsync.entity.db.Playlist;
import tech.xavi.soulsync.entity.db.SpotifySong;
import tech.xavi.soulsync.exception.SoulSyncError;
import tech.xavi.soulsync.exception.SoulSyncException;
import tech.xavi.soulsync.service.artist.ArtistMainService;
import tech.xavi.soulsync.service.integration.SpotifyGatewayService;
import tech.xavi.soulsync.service.playlist.downloadlist.DownloadListCreationService;
import tech.xavi.soulsync.service.song.SongMainService;
import tech.xavi.soulsync.service.user.AccountService;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Log4j2
@Service
@RequiredArgsConstructor
public class PlaylistCreationService {

    private final SpotifyGatewayService spotifyGatewayService;
    private final PlaylistMainService playlistMainService;
    private final ArtistMainService artistMainService;
    private final SongMainService songMainService;
    private final AccountService accountService;
    private final DownloadListCreationService downloadListCreationService;
    private static final int MAX_SONGS_PER_REQUEST = 20;

    public Playlist addNewPlaylist(String spotifyId, RequestType requestType, String searchPolicyId){
        SpotifyPlaylistDto spotifyPlaylistDto = spotifyGatewayService
                .getPlaylistDetails(spotifyId, requestType);
        DownloadList downloadList = downloadListCreationService
                .createDownloadList(spotifyId,searchPolicyId);
        Set<SpotifySong> spotifySongs = getTracklist(spotifyPlaylistDto,requestType);

        artistMainService
                .saveArtistsFromTracklist(spotifySongs);
        songMainService
                .saveTracklist(spotifySongs);

        Playlist playlist = playlistMainService
                .savePlaylist(Playlist.builder()
                .id(spotifyId)
                .name(spotifyPlaylistDto.getName())
                .totalTracks(spotifySongs.size())
                .cover(getPlaylistCoverUrl(spotifyPlaylistDto))
                .songs(spotifySongs)
                .owner(accountService.getCurrentUser().getUsername())
                .lastUpdate(System.currentTimeMillis())
                .playlistType(PlaylistType.getPlaylistType("playlist"))
                .downloadLists(Set.of(downloadList))
                .build());

        downloadListCreationService
                .createSlskdDownloads(playlist, downloadList);

        return playlist;
    }

    public Set<SpotifySong> getTracklist(SpotifyPlaylistDto playlistDto , RequestType requestType){
        return fetchFromSpotify(playlistDto,requestType)
                .parallel()
                .map(this::findAndReplaceNullSongAndArtistsIds)
                .map(songMainService::createSpotifySong)
                .collect(Collectors.toSet());
    }

    private Stream<SpotifySongDto> fetchFromSpotify(SpotifyPlaylistDto playlistDto, RequestType requestType) {
        int totalPageRequests = calculateTotalPageRequests(playlistDto.getTotalTracks());
        return IntStream.range(0, totalPageRequests)
                .parallel()
                .mapToObj( index -> CompletableFuture.supplyAsync( () -> {
                    int offset = index * MAX_SONGS_PER_REQUEST;
                    return spotifyGatewayService
                            .getPlaylistSongs(playlistDto.getId(), requestType, offset);
                }))
                .map(CompletableFuture::join)
                .flatMap(List::stream);
    }

    private int calculateTotalPageRequests(long totalTracks) {
        return (int) ((totalTracks + MAX_SONGS_PER_REQUEST - 1) / MAX_SONGS_PER_REQUEST);
    }

    private String getPlaylistCoverUrl(SpotifyPlaylistDto playlistDto){
        if (playlistDto.getImages().length > 0)
            return playlistDto.getImages()[0].getUrl();
        log.error(new SoulSyncException(
                SoulSyncError.COVER_NOT_FOUND,
                HttpStatus.BAD_REQUEST,
                new Object[]{
                        playlistDto.getName(),
                        playlistDto.getId()
                }).getUserMessage());
        return null;
    }

    private SpotifySongDto findAndReplaceNullSongAndArtistsIds(SpotifySongDto dto){
        if (dto.getId() == null)
            dto.setId(UUID.randomUUID().toString());

        dto.getArtists().forEach(artist -> {
            if (artist.getId() == null)
                artist.setId(UUID.randomUUID().toString());
        });

        return dto;
    }
}
