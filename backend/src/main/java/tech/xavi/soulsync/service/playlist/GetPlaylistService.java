package tech.xavi.soulsync.service.playlist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.dto.playlist.GetPlaylistDto;

import java.util.Set;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class GetPlaylistService {

    private final PlaylistMainService playlistMainService;

    public Set<GetPlaylistDto> getAllPlaylists() {
        return playlistMainService
                .findAll()
                .stream()
                .map( playlist -> GetPlaylistDto.builder()
                        .id(playlist.getId())
                        .playlistType(playlist.getPlaylistType())
                        .cover(playlist.getCover())
                        .name(playlist.getName())
                        .owner(playlist.getOwner())
                        .totalTracks(playlist.getTotalTracks())
                        .build())
                .collect(Collectors.toSet());
    }
}
