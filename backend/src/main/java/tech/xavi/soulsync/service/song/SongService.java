package tech.xavi.soulsync.service.song;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.dto.gateway.spotify.SpotifySongDto;
import tech.xavi.soulsync.entity.db.Artist;
import tech.xavi.soulsync.entity.db.SpotifySong;
import tech.xavi.soulsync.repository.db.SongRepository;

import java.util.Set;

@Service @RequiredArgsConstructor
public class SongService {

    private final SongRepository songRepository;

    public void saveTracklist(Set<SpotifySong> spotifySongs){
        songRepository.saveAll(spotifySongs);
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

}
