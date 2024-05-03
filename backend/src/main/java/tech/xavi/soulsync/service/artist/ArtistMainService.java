package tech.xavi.soulsync.service.artist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.entity.db.SpotifySong;
import tech.xavi.soulsync.repository.db.ArtistRepository;

import java.util.Set;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class ArtistMainService {

    private final ArtistRepository artistRepository;

    public void saveArtistsFromTracklist(Set<SpotifySong> spotifySongs) {
        artistRepository.saveAll(
                spotifySongs.stream()
                        .flatMap(song -> song.getArtists().stream())
                        .filter(artist -> !artistRepository.existsById(artist.getId()))
                        .collect(Collectors.toSet())
        );
    }

}
