package tech.xavi.soulsync.service.relocate;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tech.xavi.soulsync.entity.*;
import tech.xavi.soulsync.repository.PlaylistRepository;
import tech.xavi.soulsync.repository.SongRepository;
import tech.xavi.soulsync.service.configuration.ConfigurationService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
@Service
public class RelocateService {

    private static final String PL_FOLDER_NAME_REGEX = "[^a-zA-Z0-9\\s]";
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
        String copyAction = getConfiguration().getMoveOrCopyFiles().getAction().toUpperCase();
        AtomicInteger totalMoved = new AtomicInteger();
        playlistRepository
                .findAll()
                .forEach( playlist -> {
                    String plFolderName = getPlaylistFolderName(playlist);
                    playlist.getSongs().stream()
                            .filter(song -> song.getStatus().equals(SongStatus.COMPLETED) )
                            .forEach( song -> {
                                String originalFileAndFolder = getFileAndFolder(song.getFilename());
                                String finalSongName = getSongTitleWithArtists(song);
                                Path finalPath = moveFile(plFolderName,originalFileAndFolder,finalSongName);
                                if (!StringUtils.isEmpty(finalPath)){
                                    song.setStatus(SongStatus.COPIED);
                                    songRepository.save(song);
                                    totalMoved.getAndIncrement();
                                    log.debug(
                                            "[moveFinishedPlaylistsSongs] - Song '{}' has been '{}' to the directory '{}'",
                                            song.getName() + " - " + song.getArtists().get(0),
                                            copyAction,
                                            finalPath.toAbsolutePath()
                                    );
                                } else {
                                    log.debug(
                                            "[moveFinishedPlaylistsSongs] - finalPath for Song '{}' is empty",
                                            song.getName() + " - " + song.getArtists().get(0)
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

    private Path moveFile(String playlistFolder, String originalFilePath, String finalSongName) {
        boolean isRenamed = getConfiguration().isRenameCopiedFiles();
        String downloadsDir = getConfiguration().getSlskdDownloadsRoute();
        File downloadedFile = new File(downloadsDir + originalFilePath);

        if (!downloadedFile.exists()) {
            log.debug("[moveFile] - File '{}' not found in the downloads directory '{}'",
                    downloadedFile.getAbsolutePath(), downloadsDir);
            return null;
        }

        try {
            Path playlistDirectory = Paths.get(playlistFolder);
            if (!Files.exists(playlistDirectory)) {
                Files.createDirectories(playlistDirectory);
            }

            String finalFilePath = playlistFolder + finalSongName + (isRenamed ? getFileFormat(originalFilePath) : "");
            Path sourcePath = Paths.get(downloadsDir + originalFilePath);
            Path destinationPath = Paths.get(finalFilePath);

            if (getConfiguration().getMoveOrCopyFiles().equals(RelocateOption.MOVE)) {
                return Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            } else {
                return Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getSongTitleWithArtists(Song song) {
        if (getConfiguration().isRenameCopiedFiles()) {
            StringBuilder songTitleWithArtists = new StringBuilder(song.getName());
            List<String> artists = song.getArtists();
            if (artists != null && !artists.isEmpty()) {
                songTitleWithArtists.append(" - ");
                songTitleWithArtists.append(String.join(" ", artists));
            }
            return songTitleWithArtists.toString();
        }
        return getOriginalName(song.getFilename());
    }

    private String getFileAndFolder(String folderAndFile) {
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

    private String getPlaylistFolderName(Playlist playlist){
        String userFilesRoute = getConfiguration().getUserFilesRoute();
        return userFilesRoute
                +playlist
                .getName()
                .replaceAll(PL_FOLDER_NAME_REGEX, "")
                .trim()
                .toUpperCase()
                +"/";
    }


    private String getFileFormat(String fileAndFolders){
        String[] arr = fileAndFolders
                .split(SPLIT_BY_FOLDERS_REGEX);
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
