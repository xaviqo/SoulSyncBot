package tech.xavi.soulsync.service.main;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.configuration.constants.ConfigurationFinals;
import tech.xavi.soulsync.dto.gateway.spotify.SpotifyAlbum;
import tech.xavi.soulsync.dto.gateway.spotify.SpotifySong;
import tech.xavi.soulsync.entity.Song;
import tech.xavi.soulsync.entity.sub.SongStatus;
import tech.xavi.soulsync.repository.SongRepository;
import tech.xavi.soulsync.service.task.SearchService;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Log4j2
@RequiredArgsConstructor
@Service
public class SongService {

    private final SearchService searchService;
    private final SongRepository songRepository;

    public Song getOrCreateSong(SpotifySong spotifySong) {
        log.info("song: {}",spotifySong.toString());
        return songRepository.findBySpotifyId(spotifySong.getId())
                .orElseGet( () -> createSong(spotifySong));
    }

    public void updateSongStatus(Song song){
        log.debug("[updateSongStatus] - Song is saved in DB: {}",song.getSearchInput());
        songRepository.save(song);
    }

    public Song createSong(SpotifySong spotifySong){
        log.info("Se crea cancion {}",spotifySong.toString());
        String searchInput = searchService
                .getSongSearchInputForSlskd(spotifySong);
        Song song = Song.builder()
                .name(spotifySong.getName())
                .artists(String.join(ConfigurationFinals.ARTIST_DIVIDER,spotifySong.getArtists()))
                .searchInput(searchInput)
                .album(spotifySong.getAlbum())
                .spotifyId(spotifySong.getId())
                .status(SongStatus.WAITING)
                .attempts(0)
                .lastCheck(0)
                .added(System.currentTimeMillis())
                .build();
        songRepository.save(song);
        return song;
    }

    public Set<String> getPlaylistIdsFromSongId(String songId){
        return songRepository.findPlaylistIdsBySongId(songId);
    }

    public Optional<Set<Song>> findByStatus(SongStatus status){
        return songRepository
                .findByStatus(status);
    }

    public SpotifySong mapAlbumTrackToSpotifySong(
            String albumName,
            SpotifyAlbum.AlbumTrack albumTrack
    ){
        SpotifySong spotifySong = new SpotifySong();
        spotifySong.setTrack(new SpotifySong.Track());
        spotifySong.getTrack().setAlbum(new SpotifySong.Album(albumName));
        spotifySong.getTrack().setId(albumTrack.getId());
        spotifySong.getTrack().setName(albumTrack.getName());
        spotifySong.getTrack().setArtists(albumTrack.getArtists());
        return spotifySong;
    }

    public void checkHasIdBeforeAdding(SpotifySong spotifySong){
        if (spotifySong.getId() == null) {
            spotifySong
                    .getTrack()
                    .setId(getAlphaNumId(spotifySong.getFirstArtist(),spotifySong.getName()));
        }
    }

    private String getAlphaNumId(String str1, String str2) {
        byte[] hash;
        try {
            hash = MessageDigest
                    .getInstance("SHA-256")
                    .digest((str1 + str2).getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.substring(0, 16);
    }

}
