package tech.xavi.soulsync.entity.datafile;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import tech.xavi.soulsync.configuration.globals.DownloadPriority;
import tech.xavi.soulsync.configuration.globals.SearchInputStrategy;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@Getter
@RequiredArgsConstructor
public enum ConfigurationField {

    // OTHER
    IS_APP_INSTALLED(null,null,Section.SETUP, DataType.BOOLEAN,null,null,null,null),
    IS_DEMO_MODE(null,null,Section.SETUP, DataType.BOOLEAN,null,null,null,false),
    VERSION(null,null,Section.SETUP,DataType.TEXT,null,null,null,null),

    // API
    SPOTIFY_CLIENT_ID("spotify client id",null,Section.API, DataType.TEXT,"You can get both ID and key from the spotify developers dashboard",null,null,null),
    SPOTIFY_API_SECRET("spotify client secret",null,Section.API, DataType.TEXT,"You can get both ID and key from the spotify developers dashboard",null,null,null),
    SLSKD_USERNAME("slskd username",null,Section.API, DataType.TEXT,"Username for Slskd panel",null,null, "slskd"),
    SLSKD_PASSWORD("slskd password",null,Section.API, DataType.TEXT,"Password for Slskd panel",null,null,"slskd"),
    SLSKD_API_URL("slskd api url",null,Section.API, DataType.TEXT,"Slskd Api URL",null,null,"http://localhost:5030"),

    // SEARCH
    SRCH_MILLIS_BETWEEN_REQUESTS("Min milliseconds between requests",null,Section.SEARCH,DataType.NUMBER,"Minimum milliseconds between requests, will automatically adjust if too many requests are being made to the SoulSeek API",500,99999,null),
    SRCH_MAX_SIMULTANEOUS_THREADS("Max simultaneous threads",null,Section.SEARCH,DataType.NUMBER,"Maximum number of threads at a time",1,99,null),
    SRCH_TRACKS_PER_QUEUE("Tracks per queue",null, Section.SEARCH,DataType.NUMBER, "Max songs queued per batch",1, 99999, null),
    SRCH_SEARCH_WAITING_SECONDS("Search waiting seconds",null,Section.SEARCH,DataType.NUMBER,"Maximum seconds of waiting for a search to end",10,999,null),
    SRCH_MINUTES_TO_EVALUATE_AS_STUCK("Minutes elapsed to evaluate download as stuck",null,Section.SEARCH,DataType.NUMBER,"Every few minutes the MAINTENANCE task is executed which includes: checking download status and relocating files",1,9999,null),

    // MAINTENANCE
    APP_DATA_REFRESH_RATE("Downloads refresh rate",null,Section.MAINTENANCE,DataType.NUMBER,"Frequency of visual update of playlist data (downloads and songs)",5,420,null),
    APP_RUN_DOWNLOAD_TASK("Run download task",null,Section.MAINTENANCE,DataType.BOOLEAN,"Download task is executed if it is active",null,null,null),
    APP_RUN_MAINTENANCE_TASK("Run maintenance task",null,Section.MAINTENANCE,DataType.BOOLEAN,"Maintenance task is executed if it is active",null,null,null),
    APP_MAINTENANCE_TASK_INTERVAL_MINS("maintenance task minutes interval",null,Section.MAINTENANCE,DataType.NUMBER,"Every few minutes the maintenance task is executed which includes: checking downloads status and relocating files",1,9999,null),
    APP_IS_RELOCATE("Relocate finished downloads",null,Section.MAINTENANCE,DataType.BOOLEAN,"Copy o move completed downloads to another directory",null,null,null),
    APP_SLSKD_DOWNLOADS_PATH("Slskd download path",null,Section.MAINTENANCE,DataType.TEXT,"SLSKD Directory of Completed Downloads",null,null,null),
    APP_RELOCATED_FILES_PATH("Relocated files path",null,Section.MAINTENANCE,DataType.TEXT,"Directory to copy/move completed downloads to",null,null,null),
    APP_RELOCATE_BY("Relocation strategy",null,Section.MAINTENANCE,DataType.SELECT,"Organize directories of copied files by Playlist name or by Artist/Album",null,null,new String[]{"PLAYLIST", "ARTIST/ALBUM"}),
    APP_RENAME_RELOCATED("Rename when move/copy files",null,Section.MAINTENANCE,DataType.BOOLEAN,"Modify the name of the downloaded songs with the format [ SongName - Artists ] when they are relocated",null,null,null),
    APP_MOVE_OR_COPY("Move or copy files",null,Section.MAINTENANCE,DataType.SELECT,"Move or copy finished files when relocate",null,null,new String[]{"MOVE", "COPY"}),

    // SEARCH_POLICY
    SP_NAME("Search Policy Name","name",Section.SEARCH_POLICY, DataType.TEXT,"Name for identifying the configuration",null,null,null),
    SP_FILE_FORMATS("Accepted file formats","fileFormatsByComa",Section.SEARCH_POLICY,DataType.TEXT,"File formats by coma, sorted by preference (flac,wav,aiff,mp3...)",null,null,null),
    SP_MIN_CHARS_PER_WORD("Minimum characters per word","minCharsPerWord",Section.SEARCH_POLICY,DataType.NUMBER,"If the word does not equal or exceed the number of characters, it will not be used in the search input",1,99,null),
    SP_WORDS_TO_REMOVE("Excluded search words","wordsToRemoveByComa",Section.SEARCH_POLICY,DataType.TEXT,"Words in the name of the song/artist/album that will not be included in the search inputs (the,of,by,at...)",null,null,null),
    SP_MIN_MP3_BITRATE("Minimum MP3 bitrate","minimumMp3Bitrate",Section.SEARCH_POLICY,DataType.SELECT,"Minimum sample rate, only applicable when searching for mp3s",8,320,new Integer[]{8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320}),
    SP_MINIMUM_MINS_RETRY("Minimum minutes per retry","minimumMinutesPerRetry",Section.SEARCH_POLICY,DataType.NUMBER,"Minimum minutes for retrying a search of the same track (0 for no minimum)",0,0,null),
    SP_MAX_RETRIES("Max retries per song","maxRetries",Section.SEARCH_POLICY,DataType.NUMBER,"Minimum minutes for retrying a search of the same track (0 for no minimum)",0,0,null),
    SP_IS_AVOID_REMIX("Exclude remix versions","avoidRemix",Section.SEARCH_POLICY,DataType.BOOLEAN,"Avoid downloading remixes of the added songs as long as it is not explicitly a remix",0,0,null),
    SP_IS_AVOID_LIVE("Exclude live recordings","avoidLive",Section.SEARCH_POLICY,DataType.BOOLEAN,"Avoid downloading live recordings as long as it is not a live album",0,0,null),
    SP_IS_AVOID_RADIO_EDIT("Exclude radio edits","avoidRadioEdit",Section.SEARCH_POLICY,DataType.BOOLEAN,"Avoid downloading radio edit versions as long as it is not explicitly a radio edit.",0,0,null),
    SP_IS_AVOID_MIXED_TRACK("Exclude tracks from sets","avoidMixedTrack",Section.SEARCH_POLICY,DataType.BOOLEAN,"Avoid downloading tracks from dj sets (from albums that are dj sessions)",0,0,null),
    SP_INPUT_STRATEGY("Search input strategy","inputStrategy",Section.SEARCH_POLICY,DataType.SELECT,"Apply alternative input search logic. Sometimes it is useful for stuck searches",0,0, SearchInputStrategy.values()),
    SP_DOWNLOAD_PRIORITY("Download priority","downloadPriority",Section.SEARCH_POLICY,DataType.SELECT,"Download priority over the rest of the lists",0,0, DownloadPriority.values()),
    ;

    private final String fieldName;
    private final String objectFieldName;
    private final Section section;
    private final DataType dataType;
    private final String description;
    private final Integer min;
    private final Integer max;
    private final Object defaultValues;
    @Setter
    private JsonNode value;

    public static ConfigurationField findEnumByName(String name) {
        return Arrays.stream(values())
                .filter(e -> e.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No ConfigurationField found with name: " + name));
    }

    public static Set<ConfigurationField> getAllBySection(String section){
        return Arrays.stream(values())
                .filter( cf -> cf.section.name().equalsIgnoreCase(section) )
                .collect(Collectors.toSet());
    }

    @Getter @RequiredArgsConstructor
    public enum Section {
        SETUP,
        API,
        SEARCH_POLICY,
        SEARCH,
        MAINTENANCE
        ;

    }

    @Getter
    @RequiredArgsConstructor
    public enum DataType {
        TEXT("Value must be a text string"),
        ARRAY("It should be a list of values"),
        NUMBER("The value must be a number"),
        RANGE("The value is out of accepted range"),
        SELECT("The value is not in the list of valid options"),
        BOOLEAN("The value can only be TRUE or FALSE")
        ;
        private final String inputErrorMsg;
    }

    public String getName(){
        return this.name();
    }

    @JsonIgnore
    public Class<?> getClazz() {
        return switch (dataType) {
            case TEXT -> String.class;
            case ARRAY, SELECT -> Object.class;
            case NUMBER -> Number.class;
            case RANGE -> Integer.class;
            case BOOLEAN -> Boolean.class;
        };
    }


}
