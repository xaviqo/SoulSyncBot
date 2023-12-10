package tech.xavi.soulsync.entity.sub;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SoulSyncConfiguration {

    Api apiCfg;
    Finder finderCfg;
    App appCfg;


    public Api api(){
        return this.apiCfg;
    }
    public Finder finder() { return this.finderCfg; }
    public App app() { return this.appCfg; }

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
        boolean avoidRemix;
        boolean avoidLive;

        public int getMaxRetriesWaitingResult() {
            return maxRetriesWaitingResult;
        }

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
        int intervalMinutesScheduledTask;
        int maxSongsDownloadingSameTime;
        int minimumMinutesBtwSongCheck;
        String slskdDownloadsRoute;
        String userFilesRoute;
        boolean relocateFiles;
        RelocateFinishedStrategy moveOrCopyFiles;
        RelocateFolderStrategy relocateFolderStrategy;
        boolean renameCopiedFiles;

        public String getSlskdDownloadsRoute() {
            return addLastSlashIfNeeded(slskdDownloadsRoute);
        }

        public String getUserFilesRoute() {
            return addLastSlashIfNeeded(userFilesRoute);
        }

        private String addLastSlashIfNeeded(String route){
            if (!route.endsWith("/") && !route.endsWith("\\"))
                return route+"/";
            else
                return route;
        }

        public boolean shouldRelocateByDiscography(){
            return getRelocateFolderStrategy().equals(RelocateFolderStrategy.DISCOGRAPHY);
        }

    }


}
