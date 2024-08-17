package tech.xavi.soulsync.service.playlist;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.configuration.globals.PlaylistType;
import tech.xavi.soulsync.dto.playlist.PlaylistOverviewDto;
import tech.xavi.soulsync.entity.db.Playlist;
import tech.xavi.soulsync.exception.SoulSyncError;
import tech.xavi.soulsync.exception.SoulSyncException;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class GetPlaylistService {

    private final PlaylistService playlistService;
    private final AlbumCreationService albumCreationService;

    @Transactional
    public Set<PlaylistOverviewDto> getPlaylistDiscography(String parentPlaylistId) throws SoulSyncException {
        return playlistService
                .findAllByParentId(parentPlaylistId)
                .map(this::mapPlaylistOverviewDto)
                .collect(Collectors.toSet());
    }

    public PlaylistOverviewDto getPlaylist(String id) {
        return playlistService
                .findById(id)
                .map(this::mapPlaylistOverviewDto)
                .orElseThrow( () -> new SoulSyncException(
                        SoulSyncError.PLAYLIST_NOT_FOUND,
                        HttpStatus.BAD_REQUEST,
                        id
                ));
    }

    public Set<PlaylistOverviewDto> getAllPlaylists() {
        return playlistService
                .findAll()
                .stream()
                .filter( pl -> Objects.isNull(pl.getParentPlaylist()) )
                .map(this::mapPlaylistOverviewDto)
                .collect(Collectors.toSet());
    }

    private String getPlaylistCover(Playlist playlist){
        if (PlaylistType.DISCOGRAPHY.equals(playlist.getPlaylistType()))
            return albumCreationService
                    .getB64DiscographyCover(playlist.getName());
        else
            return playlist
                    .getCover();
    }

    private PlaylistOverviewDto mapPlaylistOverviewDto(Playlist playlist){
        return PlaylistOverviewDto.builder()
                .id(playlist.getId())
                .playlistType(playlist.getPlaylistType())
                .cover(getPlaylistCover(playlist))
                .name(playlist.getName())
                .owner(playlist.getOwnerRole())
                .totalTracks(playlist.getTotalTracks())
                .build();
    }
}
