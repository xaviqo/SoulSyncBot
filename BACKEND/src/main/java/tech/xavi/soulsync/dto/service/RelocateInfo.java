package tech.xavi.soulsync.dto.service;

import lombok.Builder;
import lombok.Getter;
import tech.xavi.soulsync.entity.sub.PlaylistType;

@Builder
@Getter
public class RelocateInfo {
    private String playlistFolderName;
    private String downloadFileAndFolder;
    private String renamedFileName;
    private String artistName;
    private String albumName;
    private Integer albumRelease;
    private PlaylistType playlistType;

    public boolean isAlbum(){
        return playlistType.equals(PlaylistType.ALBUM);
    }
}
