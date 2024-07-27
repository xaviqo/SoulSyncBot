package tech.xavi.soulsync.service.song;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.dto.gateway.spotify.SpotifyAlbumDto;
import tech.xavi.soulsync.dto.gateway.spotify.SpotifyPlaylistDto;
import tech.xavi.soulsync.dto.gateway.spotify.SpotifySongDto;
import tech.xavi.soulsync.entity.db.Artist;
import tech.xavi.soulsync.entity.db.SpotifySong;
import tech.xavi.soulsync.repository.db.SongRepository;
import tech.xavi.soulsync.service.artist.ArtistMainService;
import tech.xavi.soulsync.service.integration.SpotifyGatewayService;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static tech.xavi.soulsync.repository.gateway.SpotifyPlaylistGateway.MAX_SONGS_PER_REQUEST;
@Log4j2 @Service @RequiredArgsConstructor
public class SongService {

    private final SongRepository songRepository;
    private final SpotifyGatewayService spotifyGatewayService;
    private final ArtistMainService artistMainService;

    public Page<SpotifySong> findByPlaylistsId(String playlistId, Pageable pageable) {
        return songRepository.findByPlaylistsId(playlistId, pageable);
    }

    public Set<SpotifySong> fetchSongsFromSpotify(SpotifyPlaylistDto playlistDto){
        Set<SpotifySong> spotifySongs = fetchFromSpotify(playlistDto)
                .parallel()
                .map(this::findAndReplaceNullSongAndArtistsIds)
                .map(this::createSpotifySong)
                .collect(Collectors.toSet());

        artistMainService
                .saveArtistsFromTracklist(spotifySongs);

        return saveSongs(spotifySongs);
    }

    public Set<SpotifySong> mapAlbumSongs(SpotifyAlbumDto spotifyAlbum) {
        return Arrays.stream(spotifyAlbum.getTracks().items())
                .map( track ->
                        SpotifySong.builder()
                                .spotifyId(track.getId())
                                .name(track.getName())
                                .album(spotifyAlbum.getName())
                                .artists(track.getArtists().stream()
                                        .map( ar -> Artist.builder()
                                                .id(ar.getId())
                                                .name(ar.getName())
                                                .build())
                                        .toList())
                                .build())
                .collect(Collectors.toSet());
    }

    private Stream<SpotifySongDto> fetchFromSpotify(SpotifyPlaylistDto playlistDto) {
        int totalPageRequests = calculateTotalPageRequests(playlistDto.getTotalTracks());
        return IntStream.range(0, totalPageRequests)
                .parallel()
                .mapToObj( index -> CompletableFuture.supplyAsync( () -> {
                    int offset = index * MAX_SONGS_PER_REQUEST;
                    return spotifyGatewayService
                            .getPlaylistSongs(playlistDto.getId(), offset);
                }))
                .map(CompletableFuture::join)
                .flatMap(List::stream);
    }

    private int calculateTotalPageRequests(long totalTracks) {
        return (int) ((totalTracks + MAX_SONGS_PER_REQUEST - 1) / MAX_SONGS_PER_REQUEST);
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

    public Set<SpotifySong> saveSongs(Set<SpotifySong> spotifySongs){
        songRepository.saveAll(spotifySongs);
        return spotifySongs;
    }

    public SpotifySong createSpotifySong(SpotifySongDto dto){
        return SpotifySong.builder()
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
                        .toList())
                .build();
    }

    public void deleteOrphanSongs() {
        songRepository.deleteOrphans();
    }

}
