package tech.xavi.soulsync.service.bot;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.dto.gateway.slskd.SlskdDownloadPayload;
import tech.xavi.soulsync.dto.gateway.slskd.SlskdDownloadRequest;
import tech.xavi.soulsync.dto.gateway.slskd.SlskdFile;
import tech.xavi.soulsync.dto.gateway.slskd.SlskdSearchResult;
import tech.xavi.soulsync.entity.Song;
import tech.xavi.soulsync.entity.SoulSyncConfiguration;
import tech.xavi.soulsync.service.configuration.ConfigurationService;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
public class FileFinderService {

    private final SoulSyncConfiguration.Finder finderConfiguration;

    public FileFinderService(ConfigurationService configurationService) {
        this.finderConfiguration = configurationService.getConfiguration().finder();
    }

    public SlskdDownloadRequest createDownloadRequest(Song song, SlskdSearchResult[] results){
        for (SlskdSearchResult result : results) {
            SlskdFile[] files = result.files();
            if (files.length < 1) break;
            SlskdFile file = strictSearch(song, files);
            if (file != null) return createDownloadRequestPayload(result,file);
        }
        for (SlskdSearchResult result : results) {
            SlskdFile[] files = result.files();
            if (files.length < 1) break;
            SlskdFile file = flexibleSearch(song, files);
            if (file != null) return createDownloadRequestPayload(result,file);
        }
        return null;
    }

    private SlskdDownloadRequest createDownloadRequestPayload(SlskdSearchResult result, SlskdFile file){
        SlskdDownloadPayload payload = SlskdDownloadPayload.builder()
                .size(file.size())
                .filename(file.filename())
                .build();
        return SlskdDownloadRequest.builder()
                .username(result.username())
                .payload(payload)
                .build();
    }

    private SlskdFile strictSearch(Song song, SlskdFile[] files){
        for (SlskdFile file : files) {
            if (fileMeetsCriteria(song,file)){
                if (containsAllOriginalSongKeywords(file,song)){
                    log.debug("[strictSearch] - Found file for input '{}' with strict search",
                            song.getSearchInput()
                    );
                    return file;
                }
            }
        }
        return null;
    }

    private SlskdFile flexibleSearch(Song song, SlskdFile[] files){
        for (SlskdFile file : files) {
            if (fileMeetsCriteria(song,file)) {
                if (containsAllSearchInputKeywords(file,song)) {
                    log.debug("[checkFileAndFindProper] - Found file for input '{}' with flexible search",
                            song.getSearchInput()
                    );
                    return file;
                }
            }
        }
        return null;
    }

    private boolean fileMeetsCriteria(Song song, SlskdFile file){
        return isDifferentFileFromLastAttempt(song,file)
                && isDesiredFormat(file)
                && isMp3BitrateOk(file);
    }

    private boolean isMp3BitrateOk(SlskdFile file){
        int minimumBitrate = finderConfiguration.getMinimumBitRate();
        boolean weWantMp3 = finderConfiguration.weWantMp3();
        String fileFormat = getFileFormat(file);
        if (weWantMp3 && fileFormat.equals("mp3")) {
            return file.bitRate() >= minimumBitrate;
        }
        return true;
    }

    private boolean containsAllOriginalSongKeywords(SlskdFile file, Song song){
        List<String> nameAndArtists = new ArrayList<>(song.getArtists());
        nameAndArtists.add(song.getName());
        for (String keyword : nameAndArtists) {
            if (!file.filename().toLowerCase().contains(keyword.toLowerCase())){
                return false;
            }
        }
        return true;
    }

    private boolean containsAllSearchInputKeywords(SlskdFile file, Song song){
        String[] searchKeyWords = song.getSearchInput().split(" ");
        for (String keyword : searchKeyWords) {
            if (!file.filename().toLowerCase().contains(keyword.toLowerCase())){
                return false;
            }
        }
        return true;
    }

    private boolean isDesiredFormat(SlskdFile file){
        String[] acceptedFormats = finderConfiguration.getAcceptedFormats();
        String fileFormat = getFileFormat(file);
        for (String format : acceptedFormats)
            if (fileFormat.equals(format))
                return true;
        return false;
    }

    private boolean isDifferentFileFromLastAttempt(Song song, SlskdFile file) {
        boolean avoidRepeatErroredFile = finderConfiguration.isAvoidRepeatFileWhenErrored();
        if (avoidRepeatErroredFile) {
            String lastFileName = song.getFilename();
            if (lastFileName == null) return true;
            return !lastFileName.equals(file.filename());
        }
        return true;
    }

    private String getFileFormat(SlskdFile file){
        String[] split = file.filename().split("\\.");
        if (split.length > 0)
            return split[split.length - 1].toLowerCase();
        return "";
    }


}
