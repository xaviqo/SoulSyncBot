package tech.xavi.soulsync.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.dto.gateway.SlskdDownloadPayload;
import tech.xavi.soulsync.dto.gateway.SlskdDownloadRequest;
import tech.xavi.soulsync.dto.gateway.SlskdFile;
import tech.xavi.soulsync.dto.gateway.SlskdSearchResult;
import tech.xavi.soulsync.model.Song;

import java.util.Arrays;

@Log4j2
@Service
public class FileFinderService {

    private final String[] ACCEPTED_FORMATS;
    private final int MIN_MP3_BITRATE;
    private final boolean MP3_WANTED;

    public FileFinderService(
            @Value("${tech.xavi.soulsync.cfg.accepted-formats}") String acceptedFormats,
            @Value("${tech.xavi.soulsync.cfg.min-mp3-bitrate}") int mp3BitRate
    ) {
        this.ACCEPTED_FORMATS = acceptedFormats.split(",");
        this.MIN_MP3_BITRATE = mp3BitRate;
        this.MP3_WANTED = Arrays.asList(ACCEPTED_FORMATS).contains("mp3");

    }

    public SlskdDownloadRequest createDownloadRequest(Song song, SlskdSearchResult[] results){
        for (SlskdSearchResult result : results) {
            SlskdFile[] files = result.files();
            if (files.length < 1) break;
            SlskdFile file = checkFileAndFindProper(song,files);
            if (file != null) {
                SlskdDownloadPayload payload = SlskdDownloadPayload.builder()
                        .size(file.size())
                        .filename(file.filename())
                        .build();
                return SlskdDownloadRequest.builder()
                        .username(result.username())
                        .payload(payload)
                        .build();
            }
        }
        return null;
    }

    private SlskdFile checkFileAndFindProper(Song song, SlskdFile[] files){
        for (SlskdFile file : files) {
            if (!isDesiredFormat(file))
                break;
            if (!mp3BitRateCheck(file))
                break;
            if (!containsAllSearchKeywords(file,song))
                break;
            return file;
        }
        return null;
    }

    private boolean mp3BitRateCheck(SlskdFile file){
        String fileFormat = getFileFormat(file);
        if (MP3_WANTED && fileFormat.equals("mp3")) {
            return file.bitRate() < MIN_MP3_BITRATE;
        }
        return true;
    }

    private boolean containsAllSearchKeywords(SlskdFile file, Song song){
        String[] searchKeyWords = song.getSearchInput().split(" ");
        for (String keyword : searchKeyWords) {
            if (!file.filename().toLowerCase().contains(keyword.toLowerCase())){
                return false;
            }
        }
        return true;
    }

    private boolean isDesiredFormat(SlskdFile file){
        String fileFormat = getFileFormat(file);
        for (String format : ACCEPTED_FORMATS)
            if (fileFormat.equals(format))
                return true;
        return false;
    }

    private String getFileFormat(SlskdFile file){
        String[] split = file.filename().split("\\.");
        if (split.length > 0)
            return split[split.length - 1].toLowerCase();
        return "";
    }

}
