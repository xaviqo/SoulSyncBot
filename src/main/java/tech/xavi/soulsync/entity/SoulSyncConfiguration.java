package tech.xavi.soulsync.entity;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SoulSyncConfiguration {

    Api apiCfg;
    Finder finderCfg;
    App appCfg;

    @Builder @Data @AllArgsConstructor @NoArgsConstructor
    public static class Api {
        String spotifyClientId;
        String spotifyClientSecret;
        String slskdUsername;
        String slskdPassword;
        String slskdUrl;
    }

    @Builder @Data @AllArgsConstructor @NoArgsConstructor
    public static class Finder {
        String[] wordsToRemove;
        String[] acceptedFormats;
        int minimumBitRate;
        boolean avoidRepeatFileWhenErrored;
        int maxRetriesWaitingResult;

        public boolean weWantMp3(){
            for (String format : acceptedFormats)
                if (format.equals("mp3")) return true;
            return false;
        }
    }

    @Builder @Data @AllArgsConstructor @NoArgsConstructor
    public static class App {
        int pausesMs;
        int totalSimultaneousProcesses;
        int waitingTimeBtwResultRequestsSec;
        int intervalMinutesScheduledTask;
        int maxSongsDownloadingSameTime;
    }

    public Api api(){
        return this.apiCfg;
    }
    public Finder finder() { return this.finderCfg; }
    public App app() { return this.appCfg; }

}
