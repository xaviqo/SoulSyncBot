package tech.xavi.soulsync.service.relocate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.entity.datafile.ConfigurationField;
import tech.xavi.soulsync.entity.db.SlskdRequest;
import tech.xavi.soulsync.service.configuration.ConfigurationFieldService;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class RelocationService {

    private static final String ONLY_ALPHANUM_REGEX = "[^a-zA-Z0-9]";
    private static final String SPLIT_BY_FOLDERS_REGEX = "[\\\\/]";

    private final ConfigurationFieldService configurationFieldService;

    public boolean relocate(SlskdRequest slskdRequest) {
        Hibernate.initialize(slskdRequest.getSpotifySong());
        try {
            Path sourcePath = Paths.get(getCurrentFilePath(slskdRequest));
            Path destinationPath = Paths.get(getRelocationFilePath(slskdRequest));
            Files.createDirectories(destinationPath.getParent());
            if (isMoveFile())
                Files.move(sourcePath, destinationPath);
            else
                Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            slskdRequest.setCopyRoute(destinationPath.toAbsolutePath().toString());
            return Files.exists(destinationPath);
        } catch (FileAlreadyExistsException faee) {
            log.error("Error relocating file - The file to be moved/copied already exists: {}", faee.getMessage());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Error relocating file: {}", e.getMessage());
        }
        return false;
    }

    public void createRelocationFolders() {
        File directory = new File(getRelocatedFilesPath());
        if (!directory.exists()) directory.mkdirs();
    }

    public String getRelocationFilePath(SlskdRequest slskdRequest) {
        if (isRelocateByPlaylist())
            return String.format(
                    "%s/%s/%s",
                    getRelocatedFilesPath(),
                    slskdRequest.getPlaylist().getName().replaceAll(ONLY_ALPHANUM_REGEX, "").trim(),
                    getFileName(slskdRequest)
            );
        else
            return String.format(
                    "%s/%s/%s/%s",
                    getRelocatedFilesPath(),
                    slskdRequest.getArtistsNames(),
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
            boolean hasArtists = !slskdRequest.getSpotifySong().getArtists().isEmpty();

            if (hasArtists) newFileName.append(slskdRequest.getArtistsNames().replaceAll("[/\\\\]", " ").replace("?","")).append(" - ");
            newFileName.append(slskdRequest.getSpotifySong().getName().replaceAll("[/\\\\]", " ").replace("?",""));
            newFileName.append(getFileFormat(slskdRequest));

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
        String slskdDownloadsPath = configurationFieldService
                .getValue(ConfigurationField.APP_SLSKD_DOWNLOADS_PATH)
                .asText();
        return ensureTrailingSeparator(slskdDownloadsPath);
    }

    private String getRelocatedFilesPath(){
        String relocatedFilesPath = configurationFieldService
                .getValue(ConfigurationField.APP_RELOCATED_FILES_PATH)
                .asText();
        return ensureTrailingSeparator(relocatedFilesPath);
    }

    private boolean isMoveFile() {
        return configurationFieldService
                .getValue(ConfigurationField.APP_MOVE_OR_COPY)
                .asText()
                .equals("MOVE");
    }

    private boolean isRelocateByPlaylist(){
        return configurationFieldService
                .getValue(ConfigurationField.APP_RELOCATE_BY)
                .asText()
                .equals("PLAYLIST");
    }

    private String ensureTrailingSeparator(String path) {
        if (path == null || path.isEmpty()) return path;
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        return path;
    }

}
