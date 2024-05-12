package tech.xavi.soulsync.service.playlist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.configuration.globals.PlaylistType;
import tech.xavi.soulsync.dto.gateway.spotify.SpotifyAlbumDto;
import tech.xavi.soulsync.entity.db.DownloadList;
import tech.xavi.soulsync.entity.db.Playlist;
import tech.xavi.soulsync.service.download.downloadlist.DownloadListCreationService;
import tech.xavi.soulsync.service.integration.SpotifyGatewayService;
import tech.xavi.soulsync.service.song.SongService;
import tech.xavi.soulsync.service.user.AccountService;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static tech.xavi.soulsync.repository.gateway.SpotifyPlaylistGateway.MAX_ALBUMS_PER_REQUEST;

@RequiredArgsConstructor
@Service
public class AlbumCreationService {

    private final SpotifyGatewayService spotifyGatewayService;
    private final DownloadListCreationService downloadListCreationService;
    private final PlaylistMainService playlistMainService;
    private final SongService songService;
    private final AccountService accountService;

    public Playlist addNewDiscography(String artistId, String searchPolicyId) {
        Playlist parentPlaylist = Playlist.builder()
                .id(artistId)
                .playlistType(PlaylistType.DISCOGRAPHY)
                .isUpdatable(false)
                .lastUpdate(System.currentTimeMillis())
                .build();

        getGroupedAlbumIds(spotifyGatewayService.getArtistDiscography(artistId))
                .forEach(albumIds -> Arrays.stream(spotifyGatewayService.getAlbumWithTracks(albumIds))
                        .forEach(album -> playlistMainService.findById(album.getId())
                                .orElseGet(() -> createAndDownloadAlbum(album, parentPlaylist, searchPolicyId))));

        return playlistMainService.savePlaylist(parentPlaylist);
    }


    public Playlist addNewAlbum(String albumId, String searchPolicyId) {

        return null;
    }

    private Playlist createAndDownloadAlbum(SpotifyAlbumDto album, Playlist parentPlaylist, String searchPolicyId) {
        DownloadList downloadList = downloadListCreationService.createDownloadList(album.getId(), searchPolicyId);
        Playlist playlist = saveAlbum(album, parentPlaylist, downloadList);
        downloadListCreationService.createSlskdDownloads(playlist, downloadList);
        return playlist;
    }

    private Playlist saveAlbum(SpotifyAlbumDto albumDto, Playlist parentPlaylist, DownloadList downloadList){
        return playlistMainService
                .savePlaylist(Playlist.builder()
                        .id(albumDto.getId())
                        .name(albumDto.getName())
                        .totalTracks(albumDto.getTotalTracks())
                        .cover(playlistMainService.getPlaylistCoverUrl(albumDto.getImages()))
                        .songs(songService.mapAlbumSongs(albumDto.getTracks().items()))
                        .owner(accountService.getCurrentUser().getUsername())
                        .lastUpdate(System.currentTimeMillis())
                        .playlistType(PlaylistType.getType(albumDto.getAlbumType()))
                        .parentPlaylist(parentPlaylist)
                        .downloadLists(Set.of(downloadList))
                        .build());
    }

    private Stream<String> getGroupedAlbumIds(SpotifyAlbumDto[] albums){
        return IntStream.range(0, (albums.length + MAX_ALBUMS_PER_REQUEST - 1) / MAX_ALBUMS_PER_REQUEST)
                .mapToObj(i -> Stream.of(albums)
                        .skip((long) i * MAX_ALBUMS_PER_REQUEST)
                        .limit(MAX_ALBUMS_PER_REQUEST)
                        .map(SpotifyAlbumDto::getId)
                        .collect(Collectors.joining(",")));
    }

}
