package tech.xavi.soulsync.service.song;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.entity.SpotifySong;
import tech.xavi.soulsync.repository.db.SongRepository;

import java.util.Set;

@Service @RequiredArgsConstructor
public class SongMainService {

    private final SongRepository songRepository;

    public void saveTracklist(Set<SpotifySong> spotifySongs){
        songRepository.saveAll(spotifySongs);
    }
}
