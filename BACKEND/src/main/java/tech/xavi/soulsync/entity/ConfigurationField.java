package tech.xavi.soulsync.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static tech.xavi.soulsync.configuration.ConfigurationFinals.BIT_RATES;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@Getter
@RequiredArgsConstructor
public enum ConfigurationField {

    // API
    SPOTIFY_CLIENT_ID("spotify client id","spotifyClientId",Section.API,Type.TEXT,"You can get both ID and key from the spotify developers dashboard",null,null,null),
    SPOTIFY_API_SECRET("spotify client secret","spotifyClientSecret",Section.API,Type.TEXT,"You can get both ID and key from the spotify developers dashboard",null,null,null),
    SLSKD_USERNAME("slskd username","slskdUsername",Section.API,Type.TEXT,"Username for Slskd panel",null,null,null),
    SLSKD_PASSWORD("slskd password","slskdPassword",Section.API,Type.TEXT,"Password for Slskd panel",null,null,null),
    SLSKD_API_URL("slskd api url","slskdUrl",Section.API,Type.TEXT,"Slskd Api URL",null,null,null),

    // FINDER
    AVOID_LIVES("Avoid unwanted lives","avoidLive",Section.FINDER,Type.BOOLEAN,"Avoid downloading live recordings as long as it is not a live album'",null,null,null),
    AVOID_REMIXES("Avoid unwanted remixes","avoidRemix",Section.FINDER,Type.BOOLEAN,"Avoid downloading remixes of the added songs as long as it is not explicitly a remix",null,null,null),
    AVOID_FAILED("Avoid failed downloads","avoidRepeatFileWhenErrored",Section.FINDER,Type.BOOLEAN,"When a download fails, avoid repeating the same file on retry.",null,null,null),
    ACCEPTED_FORMATS("desired formats","acceptedFormats",Section.FINDER,Type.ARRAY,"Desired download formats, sorted by preference",null,null,null),
    BITRATE_RANGE("Mp3 Bitrate","minimumBitRate",Section.FINDER,Type.SELECT,"Minimum sample rate when searching for mp3s",null,null,BIT_RATES),
    MAX_RETRIES_SEARCH("Search waiting time","maxRetriesWaitingResult",Section.FINDER,Type.RANGE,"Waiting seconds until the search request is completed. After this time it is discarded",10,120,null),
    WORDS_TO_REMOVE("words to remove","wordsToRemove",Section.FINDER,Type.ARRAY,"Words to remove in the bot search input",null,null,null),

    // BOT
    PAUSES_MS("Ms per pause","pausesMs",Section.BOT,Type.NUMBER,"Millisecond pause on direct requests to SoulSeek to avoid temporary API bans",null,null,null),
    MAX_PL_DOWNLOAD_AT_TIME("Playlist processes","totalSimultaneousProcesses",Section.BOT,Type.NUMBER,"Max number of playlist search processes running at the same time",null,null,null),
    TASK_INTERVAL_MIN("Task waiting min","intervalMinutesScheduledTask",Section.BOT,Type.NUMBER,"Minutes of waiting time between automatic search task",null,null,null),
    SLSKD_DW_ROUTE("Slslkd Dw Path","slskdDownloadsRoute",Section.BOT,Type.TEXT,"Path to completed downloads in Slskd",null,null,null),
    USER_DW_ROUTE("User Dw Path","userFilesRoute",Section.BOT,Type.TEXT,"Minutes of waiting time between automatic search task",null,null,null),
    MOVE_OR_COPY_FILES("Move/Copy files","moveOrCopyFiles",Section.BOT,Type.SELECT,"Move or copy the relocated files (if you move the files, do not forget to keep sharing the downloaded files)",null,null,RelocateOption.values()),
    RELOCATE_FILES("Relocate files","relocateFiles",Section.BOT,Type.BOOLEAN,"User path for storing songs divided by playlist",null,null,null),
    RENAME_COPIED_FILES("Rename moved/copied","renameCopiedFiles",Section.BOT,Type.BOOLEAN,"Modify the name of the downloaded songs with the format [ SongName - Artists ] when they are relocated",null,null,null),
    ;

    private final String fieldName;
    private final String jsonFieldName;
    private final Section section;
    private final Type type;
    private final String description;
    private JsonNode value;
    private final Integer min;
    private final Integer max;
    private final Object[] values;

    public void setValue(JsonNode value) {
        this.value = value;
    }



    @Getter @RequiredArgsConstructor
    public enum Section {
        API("apiCfg"),
        FINDER("finderCfg"),
        BOT("appCfg")
        ;
        private final String jsonCfgFieldName;
    }

    @Getter @RequiredArgsConstructor
    public enum Type{
        TEXT("Value must be a text string"),
        ARRAY("It should be a list of values"),
        NUMBER("The value must be a number"),
        RANGE("The value is out of accepted range"),
        SELECT("The value is not in the list of valid options"),
        BOOLEAN("The value can only be TRUE or FALSE")
        ;
        private final String inputErrorMsg;
    }

}
