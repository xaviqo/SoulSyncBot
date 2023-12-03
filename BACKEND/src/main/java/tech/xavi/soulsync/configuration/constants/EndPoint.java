package tech.xavi.soulsync.configuration.constants;

public class EndPoint {

    public static final String VERSION = "/v1";

    public static final String LOGIN = VERSION+"/login";

    public static final String PLAYLIST = VERSION+"/playlist";
    public static final String PL_GET_SONGS = PLAYLIST+"/songs";
    public static final String PL_GET_SONGS_STATS = PLAYLIST+"/stats";

    public static final String CONFIGURATION = VERSION+"/configuration";
    public static final String CFG_GET_CONFIGURATION_FIELDS = CONFIGURATION+"/get";
    public static final String CFG_SAVE_SECTION_CONFIGURATION = CONFIGURATION+"/save";
    public static final String CFG_RESET_SECTION = CONFIGURATION+"/reset";
    public static final String CFG_REBOOT_BOT = CONFIGURATION+"/reboot";

    public static final String MONITOR = VERSION+"/monitor";
    public static final String MONITOR_GET_LOGS = MONITOR+"/log";
    public static final String MONITOR_GET_QUEUE = MONITOR+"/queue";

    public static final String HEALTH = VERSION+"/health";


}
