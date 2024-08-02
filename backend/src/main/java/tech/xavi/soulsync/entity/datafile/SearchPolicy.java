package tech.xavi.soulsync.entity.datafile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import tech.xavi.soulsync.configuration.globals.DownloadPriority;
import tech.xavi.soulsync.configuration.globals.SearchInputStrategy;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder @NoArgsConstructor @AllArgsConstructor
public class SearchPolicy {

    @Setter
    private String id;
    private String name;
    private String fileFormatsByComa;
    private int minCharsPerWord;
    private String wordsToRemoveByComa;
    private int minimumMp3Bitrate;
    private int minimumMinutesPerRetry;
    private int maxRetries;
    private boolean avoidRemix;
    private boolean avoidLive;
    private boolean avoidRadioEdit;
    private boolean avoidMixedTrack;
    private SearchInputStrategy inputStrategy;
    private DownloadPriority downloadPriority;

    public List<String> acceptedFormats() {
        return List.of(fileFormatsByComa.split(","));
    }

    @JsonIgnore
    public boolean isMp3Accepted(){
        for (String format : acceptedFormats())
            if (format.equalsIgnoreCase("mp3")) return true;
        return false;
    }

    @JsonIgnore
    public List<String> getAvoidValues() {
        List<String> toAvoidValues = new ArrayList<>();
        if (isAvoidRemix())
            toAvoidValues.addAll(List.of("rmx","remix"));
        if (isAvoidLive())
            toAvoidValues.add("live");
        if (isAvoidRadioEdit())
            toAvoidValues.add("radio");
        if (isAvoidMixedTrack())
            toAvoidValues.addAll(List.of("mix","mixed"));
        return toAvoidValues;
    }
}