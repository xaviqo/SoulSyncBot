package tech.xavi.soulsync.service.files;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tech.xavi.soulsync.dto.service.RelocateInfo;
import tech.xavi.soulsync.entity.Song;
import tech.xavi.soulsync.entity.sub.RelocateFinishedStrategy;
import tech.xavi.soulsync.entity.sub.SongStatus;
import tech.xavi.soulsync.entity.sub.SoulSyncConfiguration;
import tech.xavi.soulsync.repository.PlaylistRepository;
import tech.xavi.soulsync.repository.SongRepository;
import tech.xavi.soulsync.service.config.ConfigurationService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
@Service
public class RelocateService {

    private static final String PL_FOLDER_NAME_REGEX = "[^a-zA-Z0-9\\s/]";
    private static final String SPLIT_BY_FOLDERS_REGEX = "[\\\\/]";
    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;

    public RelocateService(
            PlaylistRepository playlistRepository,
            SongRepository songRepository
    ) {
        this.playlistRepository = playlistRepository;
        this.songRepository = songRepository;
    }

    public void moveFinishedPlaylistsSongs(){
        String copyAction = getConfiguration().getMoveOrCopyFiles().name();
        AtomicInteger totalMoved = new AtomicInteger();
        playlistRepository
                .findAll()
                .forEach( playlist -> {
                    playlist.getSongs().stream()
                            .filter( song -> song.getStatus().equals(SongStatus.COMPLETED) )
                            .forEach( song -> {
                                RelocateInfo relocateInfo = RelocateInfo.builder()
                                        .playlistFolderName(getFormattedRoute(playlist.getName()))
                                        .downloadFileAndFolder(getDownloadFileAndFolder(song.getFilename()))
                                        .renamedFileName(getRenamedFileName(song))
                                        .playlistType(playlist.getType())
                                        .artistName(song.getArtists()[0])
                                        .albumRelease(playlist.getReleaseYear())
                                        .albumName(song.getAlbum())
                                        .build();
                                Path finalPath = moveFile(relocateInfo);
                                if (!StringUtils.isEmpty(finalPath)){
                                    song.setStatus(SongStatus.COPIED);
                                    song.setCopyRoute(finalPath.toAbsolutePath().toString());
                                    songRepository.save(song);
                                    totalMoved.getAndIncrement();
                                    log.debug(
                                            "[moveFinishedPlaylistsSongs] - Song '{}' has been '{}' to the directory '{}'",
                                            song.getName() + " - " + song.getArtists()[0],
                                            copyAction,
                                            finalPath.toAbsolutePath()
                                    );
                                } else {
                                    log.debug(
                                            "[moveFinishedPlaylistsSongs] - finalPath for Song '{}' is empty",
                                            song.getName() + " - " + song.getArtists()[0]
                                    );
                                }
                            });
                });
        log.debug(
                "[moveFinishedPlaylistsSongs] - A total of {} songs have been {} to the directory '{}'",
                totalMoved.get(),
                copyAction,
                getConfiguration().getUserFilesRoute()
        );
    }

    private Path moveFile(RelocateInfo relocateInfo) {
        boolean isRenamed = getConfiguration().isRenameCopiedFiles();
        String downloadsDir = getConfiguration().getSlskdDownloadsRoute();
        File downloadedFile = new File(downloadsDir + relocateInfo.getDownloadFileAndFolder());

        if (!downloadedFile.exists()) {
            log.debug("[moveFile] - File '{}' not found in the downloads directory '{}'",
                    downloadedFile.getAbsolutePath(), downloadsDir);
            return null;
        }

        try {
            String targetCopyDirectory = getTargetCopyDirectory(relocateInfo);
            Path targetCopyPath = Paths.get(targetCopyDirectory);
            if (!Files.exists(targetCopyPath)) {
                Files.createDirectories(targetCopyPath);
            }

            String finalFilePath = targetCopyDirectory + relocateInfo.getRenamedFileName() + (isRenamed ? getFileFormat(relocateInfo.getDownloadFileAndFolder()) : "");
            Path sourcePath = Paths.get(downloadsDir + relocateInfo.getDownloadFileAndFolder());
            Path destinationPath = Paths.get(finalFilePath);

            if (getConfiguration().getMoveOrCopyFiles().equals(RelocateFinishedStrategy.MOVE)) {
                return Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            } else {
                return Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getTargetCopyDirectory(RelocateInfo relocateInfo){
        boolean relocateByAlbum = relocateInfo.isAlbum() || getConfiguration().shouldRelocateByDiscography();
        if (relocateByAlbum) {
            String albumYear = relocateInfo.getAlbumRelease() != null
                    ? relocateInfo.getAlbumRelease() + "/"
                    : "";
            return getFormattedRoute(
              relocateInfo.getArtistName() + "/" +
                      albumYear+relocateInfo.getAlbumName() + "/"
            );
        }
        return relocateInfo.getPlaylistFolderName();
    }

    private String getRenamedFileName(Song song) {
        if (getConfiguration().isRenameCopiedFiles()) {
            StringBuilder songTitleWithArtists = new StringBuilder(song.getName());
            String[] artists = song.getArtists();
            if (artists != null && artists.length > 0) {
                songTitleWithArtists.append(" - ");
                songTitleWithArtists.append(String.join(" ", artists));
            }
            return songTitleWithArtists.toString();
        }
        return getOriginalName(song.getFilename());
    }

    private String getDownloadFileAndFolder(String folderAndFile) {
        String[] arr = folderAndFile.split(SPLIT_BY_FOLDERS_REGEX);
        if (arr.length > 0)
            return arr[arr.length - 2]+"/"+arr[arr.length - 1]+"/";
        else
            return folderAndFile;
    }

    private String getOriginalName(String folderAndFile) {
        String[] arr = folderAndFile.split(SPLIT_BY_FOLDERS_REGEX);
        if (arr.length > 0)
            return arr[arr.length - 1];
        else
            return folderAndFile;
    }

    private String getFormattedRoute(String folders){
        String userFilesRoute = getConfiguration().getUserFilesRoute();
        return userFilesRoute+folders.replaceAll(PL_FOLDER_NAME_REGEX, "")
                .trim()
                .toUpperCase()
                +"/";
    }

    private String getFileFormat(String fileAndFolders){
        String[] arr = fileAndFolders.split(SPLIT_BY_FOLDERS_REGEX);
        if (arr.length > 0) {
            String file = arr[arr.length -1];
            if (file.contains(".")){
                String[] split = file.split("\\.");
                return "."+split[split.length -1];
            }
        }
        return "";
    }

    private SoulSyncConfiguration.App getConfiguration(){
        return ConfigurationService.instance().cfg().app();
    }
}
