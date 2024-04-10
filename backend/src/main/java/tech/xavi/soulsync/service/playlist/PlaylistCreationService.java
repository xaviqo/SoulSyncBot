package tech.xavi.soulsync.service.playlist;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.configuration.globals.PlaylistType;
import tech.xavi.soulsync.configuration.globals.RequestType;
import tech.xavi.soulsync.dto.gateway.spotify.SpotifyPlaylistDto;
import tech.xavi.soulsync.dto.gateway.spotify.SpotifySongDto;
import tech.xavi.soulsync.entity.Artist;
import tech.xavi.soulsync.entity.Playlist;
import tech.xavi.soulsync.entity.SpotifySong;
import tech.xavi.soulsync.exception.SoulSyncError;
import tech.xavi.soulsync.exception.SoulSyncException;
import tech.xavi.soulsync.service.artist.ArtistMainService;
import tech.xavi.soulsync.service.integration.SpotifyGatewayService;
import tech.xavi.soulsync.service.song.SongMainService;
import tech.xavi.soulsync.service.user.AccountService;

import java.util.List;
import java.util.Set;
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
    private static final int MAX_SONGS_PER_REQUEST = 20;

    public Playlist addNewPlaylist(String spotifyId, RequestType requestType){
        SpotifyPlaylistDto spotifyPlaylistDto = spotifyGatewayService.
                getPlaylistDetails(spotifyId, requestType);
        Set<SpotifySong> spotifySongs = getTracklistFromSpotify(spotifyPlaylistDto,requestType);

        artistMainService
                .saveArtistsFromTracklist(spotifySongs);
        songMainService
                .saveTracklist(spotifySongs);

        return playlistMainService.savePlaylist(
                Playlist.builder()
                        .spotifyId(spotifyId)
                        .name(spotifyPlaylistDto.getName())
                        .totalTracks(spotifySongs.size())
                        .cover(getPlaylistCoverUrl(spotifyPlaylistDto))
                        .songs(spotifySongs)
                        .owner(accountService.getCurrentUser().getUsername())
                        .lastUpdate(System.currentTimeMillis())
                        .playlistType(PlaylistType.getPlaylistType("playlist"))
                        .build()
        );
    }

    public Set<SpotifySong> getTracklistFromSpotify(SpotifyPlaylistDto playlistDto , RequestType requestType){
        return fetchFromSpotify(playlistDto,requestType)
                .parallel()
                .map( dto -> SpotifySong.builder()
                        .spotifyId(dto.getId())
                        .name(dto.getName())
                        .album(dto.getAlbum())
                        .artists(dto
                                .getTrack()
                                .getArtists()
                                .stream()
                                .parallel()
                                .map(a -> Artist.builder()
                                        .id(a.getId())
                                        .name(a.getName())
                                        .build() )
                                .collect(Collectors.toSet()))
                        .build())
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

}
