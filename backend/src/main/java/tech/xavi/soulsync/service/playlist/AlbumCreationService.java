package tech.xavi.soulsync.service.playlist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.configuration.globals.PlaylistType;
import tech.xavi.soulsync.dto.gateway.spotify.SpotifyAlbumDto;
import tech.xavi.soulsync.entity.db.DownloadList;
import tech.xavi.soulsync.entity.db.Playlist;
import tech.xavi.soulsync.entity.db.SpotifySong;
import tech.xavi.soulsync.service.artist.ArtistMainService;
import tech.xavi.soulsync.service.download.downloadlist.DownloadListCreationService;
import tech.xavi.soulsync.service.integration.SpotifyGatewayService;
import tech.xavi.soulsync.service.song.SongService;
import tech.xavi.soulsync.service.user.AccountService;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static tech.xavi.soulsync.repository.gateway.SpotifyPlaylistGateway.MAX_ALBUMS_PER_REQUEST;

@RequiredArgsConstructor
@Service
public class AlbumCreationService {

    private final SpotifyGatewayService spotifyGatewayService;
    private final DownloadListCreationService downloadListCreationService;
    private final PlaylistService playlistService;
    private final ArtistMainService artistMainService;
    private final SongService songService;
    private final AccountService accountService;

    public Playlist addNewDiscography(String artistId, String searchPolicyId) {
        List<SpotifyAlbumDto> albums = spotifyGatewayService.getArtistDiscography(artistId);
        String discographyName = getDiscographyName(albums);
        Playlist parentPlaylist = Playlist.builder()
                .name(discographyName)
                .id(artistId)
                .playlistType(PlaylistType.DISCOGRAPHY)
                .isUpdatable(false)
                .lastUpdate(System.currentTimeMillis())
                .owner(accountService.getCurrentUser().getUsername())
                .totalTracks(0)
                .build();
        playlistService.savePlaylist(parentPlaylist);

        Set<String> idsByComa = getAlbumIdsGroupedByComa(getGroupedAlbumIds(albums));
        List<SpotifyAlbumDto> albumsWithTracks = idsByComa.stream()
                .flatMap(ids -> Stream.of(spotifyGatewayService.getAlbumWithTracks(ids)))
                .toList();

        albumsWithTracks.forEach(album ->
                playlistService.findById(album.getId())
                        .orElseGet(() -> createAlbumAndDownloadList(album, parentPlaylist, searchPolicyId))
        );

        parentPlaylist.setCover(getB64DiscographyCover(discographyName));
        return parentPlaylist;
    }

    public Playlist addNewAlbum(String albumId, String searchPolicyId) {
        SpotifyAlbumDto spotifyAlbumDto = spotifyGatewayService
                .getAlbumWithTracks(albumId)[0];
        return Objects.nonNull(spotifyAlbumDto)
                ? createAlbumAndDownloadList(spotifyAlbumDto,null,searchPolicyId)
                : Playlist.builder().build();
    }

    public String getB64DiscographyCover(String artistName) {
        final int IMG_PX = 640;

        try {
            String text = artistName.replace(PlaylistType.DISCOGRAPHY.name(),"");
            BufferedImage image = new BufferedImage(IMG_PX, IMG_PX, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();

            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(0, 0, IMG_PX, 640);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Monospaced", Font.BOLD, 38));

            FontMetrics fm = g2d.getFontMetrics();
            int x = (IMG_PX - fm.stringWidth(text)) / 2;
            int y = ((IMG_PX - fm.getHeight()) / 2) + fm.getAscent();

            g2d.drawString(text, x, y);
            g2d.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();

            return String.format(
                    "data:image/png;base64,%s",
                    Base64.getEncoder().encodeToString(imageBytes)
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getDiscographyName(List<SpotifyAlbumDto> albums){
        return String.format(
                "%s %s",
                albums.getFirst().getArtists().getFirst().getName(),
                PlaylistType.DISCOGRAPHY.name()
        );
    }

    private Playlist createAlbumAndDownloadList(
            SpotifyAlbumDto album,
            Playlist parentPlaylist,
            String searchPolicyId
    ) {
        DownloadList downloadList = downloadListCreationService.getDownloadList(album.getId(), searchPolicyId);
        Playlist playlist = saveAlbum(album, parentPlaylist, downloadList);
        downloadListCreationService.createSlskdRequests(playlist, downloadList);
        return playlist;
    }

    private Playlist saveAlbum(SpotifyAlbumDto albumDto, Playlist parentPlaylist, DownloadList downloadList){
        Set<SpotifySong> playlistSongs = songService.mapAlbumSongs(albumDto);
        artistMainService.saveArtistsFromTracklist(playlistSongs);
        songService.saveSongs(playlistSongs);
        return playlistService
                .savePlaylist(Playlist.builder()
                        .id(albumDto.getId())
                        .name(albumDto.getName())
                        .totalTracks(albumDto.getTotalTracks())
                        .cover(playlistService.getPlaylistCoverUrl(albumDto.getImages()))
                        .songs(playlistSongs)
                        .owner(accountService.getCurrentUser().getUsername())
                        .lastUpdate(System.currentTimeMillis())
                        .playlistType(PlaylistType.getType(albumDto.getAlbumType()))
                        .parentPlaylist(parentPlaylist)
                        .isUpdatable(false)
                        .downloadLists(Set.of(downloadList))
                        .build());
    }

    private Stream<String> getGroupedAlbumIds(List<SpotifyAlbumDto> albums){
        return IntStream.range(0, (albums.size() + MAX_ALBUMS_PER_REQUEST - 1) / MAX_ALBUMS_PER_REQUEST)
                .mapToObj(i ->
                        albums.stream()
                        .skip((long) i * MAX_ALBUMS_PER_REQUEST)
                        .limit(MAX_ALBUMS_PER_REQUEST)
                        .map(SpotifyAlbumDto::getId)
                        .collect(Collectors.joining(","))
                );
    }

    private Set<String> getAlbumIdsGroupedByComa(Stream<String> groupedAlbumIds){
        List<String> tempList = new ArrayList<>();
        Set<String> set20wordsByComa = new HashSet<>();
        groupedAlbumIds.forEach(word -> {
            tempList.add(word);
            if (tempList.size() == 20) {
                set20wordsByComa.add(String.join(",", tempList));
                tempList.clear();
            }
        });
        if (!tempList.isEmpty())
            set20wordsByComa.add(String.join(",", tempList));
        return set20wordsByComa;
    }

}
