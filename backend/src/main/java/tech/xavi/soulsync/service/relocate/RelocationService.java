package tech.xavi.soulsync.service.relocate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.entity.datafile.ConfigurationField;
import tech.xavi.soulsync.entity.db.Artist;
import tech.xavi.soulsync.entity.db.SlskdRequest;
import tech.xavi.soulsync.service.configuration.ConfigurationFieldService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
@Service
public class RelocationService {

    private static final String SPLIT_BY_FOLDERS_REGEX = "[\\\\/]";

    private final ConfigurationFieldService configurationFieldService;

    public boolean relocate(SlskdRequest slskdRequest) {
        Hibernate.initialize(slskdRequest.getSpotifySong());
        try {
            Path sourcePath = Paths.get(getCurrentFilePath(slskdRequest));
            Path destinationPath = Paths.get(getRelocationFilePath(slskdRequest));
            Files.createDirectories(destinationPath.getParent());
            Files.move(sourcePath, destinationPath);
            slskdRequest.setCopyRoute(destinationPath.toAbsolutePath().toString());
            return Files.exists(destinationPath);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Error relocating file: {}", e.getMessage());
        }
        return false;
    }

    public void createRelocationFolders() {
        Arrays.stream(getRelocatedFilesPath()
                .split(SPLIT_BY_FOLDERS_REGEX))
                .toList()
                .forEach( path -> {
                    File directory = new File(path);
                    if (!directory.exists()) directory.mkdirs();
                });
    }

    private String getRelocationFilePath(SlskdRequest slskdRequest) {
        if (isRelocateByPlaylist())
            return String.format(
                    "%s/%s/%s",
                    getRelocatedFilesPath(),
                    slskdRequest.getPlaylist().getName().trim(),
                    getFileName(slskdRequest)
            );
        else
            return String.format(
                    "%s/%s/%s/%s",
                    getRelocatedFilesPath(),
                    slskdRequest.getSpotifySong().getArtists().get(0).getName(),
                    slskdRequest.getSpotifySong().getAlbum(),
                    getFileName(slskdRequest)
            );
    }

    private String getFileName(SlskdRequest slskdRequest) {
        String[] splittedRoute = slskdRequest
                .getFilename()
                .split(SPLIT_BY_FOLDERS_REGEX);
        if (shouldRenameRelocated()) {
            StringBuilder newFileName = new StringBuilder();
            String[] artists = slskdRequest
                    .getSpotifySong()
                    .getArtists()
                    .stream()
                    .map(Artist::getName)
                    .toArray(String[]::new);
            newFileName
                    .append(slskdRequest.getSpotifySong().getName())
                    .append(" - ")
                    .append(String.join(", ",artists))
                    .append(getFileFormat(slskdRequest));
            return newFileName.toString();
        } else {
            if (splittedRoute.length > 1)
                return splittedRoute[splittedRoute.length - 1] + getFileFormat(slskdRequest);
        }
        return slskdRequest.getFilename() + getFileFormat(slskdRequest);
    }

    private String getCurrentFilePath(SlskdRequest slskdRequest){
        String[] splittedRoute = slskdRequest
                .getFilename()
                .split(SPLIT_BY_FOLDERS_REGEX);
        if (splittedRoute.length >= 2)
            return String.format(
                    "%s/%s/%s",
                    getSlskdDownloadsPath(),
                    splittedRoute[splittedRoute.length - 2],
                    splittedRoute[splittedRoute.length - 1]
            );
        else
            return slskdRequest
                    .getFilename();
    }

    private String getFileFormat(SlskdRequest slskdRequest) {
        int lastIndex = slskdRequest.getFilename().lastIndexOf(".");
        if (lastIndex != -1)
            return "." + slskdRequest.getFilename().substring(lastIndex + 1);
        return "";
    }

    private boolean shouldRenameRelocated(){
        return configurationFieldService
                .getValue(ConfigurationField.APP_RENAME_RELOCATED)
                .asBoolean();
    }

    private String getSlskdDownloadsPath() {
        return configurationFieldService
                .getValue(ConfigurationField.APP_SLSKD_DOWNLOADS_PATH)
                .asText();
    }

    private String getRelocatedFilesPath(){
        return configurationFieldService
                .getValue(ConfigurationField.APP_RELOCATED_FILES_PATH)
                .asText();
    }

    private boolean isRelocateByPlaylist(){
        return configurationFieldService
                .getValue(ConfigurationField.APP_RELOCATE_BY)
                .asText()
                .equals("PLAYLIST");
    }

}
