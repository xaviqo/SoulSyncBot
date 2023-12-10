package tech.xavi.soulsync.entity.sub;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static tech.xavi.soulsync.configuration.constants.ConfigurationFinals.BIT_RATES;

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
    AVOID_LIVES("Exclude live recordings","avoidLive",Section.FINDER,Type.BOOLEAN,"Avoid downloading live recordings as long as it is not a live album'",null,null,null),
    AVOID_REMIXES("Exclude remix versions","avoidRemix",Section.FINDER,Type.BOOLEAN,"Avoid downloading remixes of the added songs as long as it is not explicitly a remix",null,null,null),
    AVOID_FAILED("Skip repeating file on failed downloads","avoidRepeatFileWhenErrored",Section.FINDER,Type.BOOLEAN,"When a download fails, avoid repeating the same file on retry.",null,null,null),
    ACCEPTED_FORMATS("Accepted file formats","acceptedFormats",Section.FINDER,Type.ARRAY,"Desired download formats, sorted by preference",null,null,null),
    BITRATE_RANGE("Minimum Mp3 Bitrate","minimumBitRate",Section.FINDER,Type.SELECT,"Minimum sample rate when searching for mp3s",null,null,BIT_RATES),
    MAX_RETRIES_SEARCH("Maximum search wait","maxRetriesWaitingResult",Section.FINDER,Type.RANGE,"Waiting seconds until the search request is completed. After this time it is discarded",10,120,null),
    WORDS_TO_REMOVE("Excluded search words","wordsToRemove",Section.FINDER,Type.ARRAY,"Words to remove in the bot search input",null,null,null),

    // BOT
    MIN_MINUTES_BTW_SONG_CHECK("Min. Interval Minutes between song retry","minimumMinutesBtwSongCheck",Section.BOT,Type.NUMBER,"Minimum waiting time between checks of the same song",null,null,null),
    PAUSES_MS("Pause Interval in Ms.","pausesMs",Section.BOT,Type.NUMBER,"Millisecond pause on direct requests to SoulSeek to avoid temporary API bans",null,null,null),
    MAX_PL_DOWNLOAD_AT_TIME("Concurrent Song Downloads","totalSimultaneousProcesses",Section.BOT,Type.NUMBER,"Max number of seeks running at the same time",null,null,null),
    TASK_INTERVAL_MIN("Minutes between scheduled tasks","intervalMinutesScheduledTask",Section.BOT,Type.NUMBER,"Minutes of waiting time between automatic search task",null,null,null),
    SLSKD_DW_ROUTE("Slskd download path","slskdDownloadsRoute",Section.BOT,Type.TEXT,"Path to completed downloads in Slskd",null,null,null),
    USER_DW_ROUTE("User music path","userFilesRoute",Section.BOT,Type.TEXT,"Path where to place the songs once the process is finished",null,null,null),
    MOVE_OR_COPY_FILES("Move or Copy files","moveOrCopyFiles",Section.BOT,Type.SELECT,"Move or copy the relocated files (if you move the files, do not forget to keep sharing the downloaded files)",null,null, RelocateFinishedStrategy.values()),
    RELOCATE_FILES("Relocate files","relocateFiles",Section.BOT,Type.BOOLEAN,"User path for storing songs divided by playlist",null,null,null),
    RELOCATE_PLAYLIST_FOLDER("Relocate playlists folders by","relocateFolderStrategy",Section.BOT,Type.SELECT,"Copy downloads to folder with playlist name or by artist/album",null,null,RelocateFolderStrategy.values()),
    RENAME_COPIED_FILES("Rename moved/copied files","renameCopiedFiles",Section.BOT,Type.BOOLEAN,"Modify the name of the downloaded songs with the format [ SongName - Artists ] when they are relocated",null,null,null),
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
