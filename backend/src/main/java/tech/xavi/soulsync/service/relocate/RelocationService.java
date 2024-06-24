package tech.xavi.soulsync.service.relocate;

import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.entity.datafile.ConfigurationField;
import tech.xavi.soulsync.entity.db.Artist;
import tech.xavi.soulsync.entity.db.SlskdRequest;
import tech.xavi.soulsync.service.configuration.ConfigurationFieldService;

@RequiredArgsConstructor
@Service
public class RelocationService {

    private static final String PL_FOLDER_NAME_REGEX = "[^a-zA-Z0-9\\s/]";
    private static final String SPLIT_BY_FOLDERS_REGEX = "[\\\\/]";

    private final ConfigurationFieldService configurationFieldService;

    public void relocate(SlskdRequest slskdRequest) {
        Hibernate.initialize(slskdRequest.getSpotifySong());
        String currentFilePath = getCurrentFilePath(slskdRequest);
        String relocationFilePath = getRelocationFilePath(slskdRequest);

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
                    .append(String.join(", ",artists));
            return newFileName.toString();
        } else {
            if (splittedRoute.length > 1)
                return splittedRoute[splittedRoute.length - 1];
        }
        return slskdRequest.getFilename();
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
