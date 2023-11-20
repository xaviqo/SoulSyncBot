package tech.xavi.soulsync.service.bot;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.dto.gateway.slskd.SlskdSearchQuery;
import tech.xavi.soulsync.dto.gateway.slskd.SlskdSearchResult;
import tech.xavi.soulsync.dto.gateway.slskd.SlskdSearchStatus;
import tech.xavi.soulsync.dto.gateway.spotify.SpotifySong;
import tech.xavi.soulsync.entity.Song;
import tech.xavi.soulsync.entity.SongStatus;
import tech.xavi.soulsync.entity.SoulSyncConfiguration;
import tech.xavi.soulsync.entity.SoulSyncError;
import tech.xavi.soulsync.configuration.security.SoulSyncException;
import tech.xavi.soulsync.gateway.SlskdGateway;
import tech.xavi.soulsync.repository.SongRepository;
import tech.xavi.soulsync.service.configuration.ConfigurationService;
import tech.xavi.soulsync.service.auth.AuthService;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;

@Log4j2
@Service
public class SearchService {

    private final SlskdGateway slskdGateway;
    private final AuthService authService;
    private final PauseService delayService;
    private final SongRepository songRepository;

    public SearchService(
            SlskdGateway slskdGateway,
            AuthService authService,
            PauseService delayService,
            SongRepository songRepository
    ) {
        this.slskdGateway = slskdGateway;
        this.authService = authService;
        this.delayService = delayService;
        this.songRepository = songRepository;
    }

    public void searchSong(Song song){
        song.setStatus(SongStatus.SEARCHING);
        song.setLastCheck(System.currentTimeMillis());
        songRepository.save(song);
        slskdGateway.initSearch(
                SlskdSearchQuery.builder()
                        .id(song.getSearchId().toString())
                        .searchText(song.getSearchInput())
                        .build(),
                authService
                        .getSlskdToken()
                        .token()
        );
        int delayMs = delayService.delay();
        log.debug("[searchSong] - Delay required. Random Milliseconds: {}",
                delayMs
        );
        log.debug("[searchSong] - Song search is initiated: ({}) with id ({})",
                song.getSearchInput(),
                song.getSearchId()
        );
    }

    public SlskdSearchResult[] fetchResults(Song song){
        int maxRetries = getConfiguration().getMaxRetriesWaitingResult();
        int retries = 1;
        do {
            try {
                if (isSearchComplete(song).isComplete())
                    return getResultArray(song);

                if (isAttemptLimitReached(retries,song))
                    break;

                pause(song,retries);
                retries++;

            } catch (SoulSyncException soulSyncException){
                log.error("[fetchResults] - {} - Search Input: {}",
                        soulSyncException.getSoulSyncError(),
                        song.getSearchInput()
                        );
                break;
            } catch (InterruptedException interruptedException) {
                log.error("InterruptedException occurred while waiting " +
                                "for the next attempt: {}, {}, {}",
                        interruptedException.getMessage() ,
                        song.getSearchInput(),
                        song.getSearchId()
                );
                Thread.currentThread().interrupt();
                break;
            }

        } while (retries < maxRetries);
        return new SlskdSearchResult[0];
    }

    private SlskdSearchResult[] getResultArray(Song song){
        log.debug("[fetchResults] - Search process finished, " +
                        "a list of results is being requested | SearchInput: {} SearchId: {}",
                song.getSearchInput(),
                song.getSearchId()
        );
        return slskdGateway.getSearchResults(
                song.getSearchId().toString(),
                authService.getSlskdToken().token()
        );
    }

    private SlskdSearchStatus isSearchComplete(Song song) throws SoulSyncException {
        SlskdSearchStatus searchStatus = slskdGateway.checkSearchStatus(
                song.getSearchId().toString(),
                authService.getSlskdToken().token()
        );
        if (searchStatus == null)
            throw new SoulSyncException(
                    SoulSyncError.NULL_RESULT_EXCEPTION,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        return searchStatus;
    }

    private void pause(Song song, int retries) throws InterruptedException {
        int pauseSec = delayService.searchPause();
        log.debug("[fetchResults] (Attempt #{}) - Wait {} sec pause performed " +
                        "| SearchInput: {} SearchId: {}",
                retries, pauseSec, song.getSearchInput(), song.getSearchId());
    }

    public String getSongSearchInputForSlskd(SpotifySong spotifySong){
        return removeSpecialChars(
                spotifySong.getName()
                        +" "+
                        spotifySong.getFirstArtist()
        );
    }

    public void deleteSearchFromSlskd(Song song){
        slskdGateway.deleteSearch(
                song.getSearchId().toString(),
                authService.getSlskdToken().token()
        );
        log.debug("[deleteSearchFromSlskd] - Search ({}) removed from Slskd after download: {}",
                song.getSearchInput(),
                song.getSearchId()
        );
    }

    private boolean isAttemptLimitReached(int currentRetries, Song song){
        int maxRetries = getConfiguration().getMaxRetriesWaitingResult();
        boolean isReached = (currentRetries == maxRetries);
        if (isReached){
            log.debug("[fetchResults] - The search process has not been completed after {} attempts. " +
                            "Postponed to next hunting | SearchInput: {} SearchId: {}",
                    maxRetries, song.getSearchInput(), song.getSearchId());
        } else {
            log.debug("[fetchResults] (Attempt #{}) - The search process has not yet been completed " +
                            "| SearchInput: {} SearchId: {}",
                    currentRetries, song.getSearchInput(), song.getSearchId());
        }
        return isReached;
    }

    // The search fails if words of two or fewer letters are added.
    private String removeSpecialChars(String songNameAndArtist){
        List<String> wordsToRemove = Arrays.asList(getConfiguration().getWordsToRemove());
        final String SPECIAL_CHARS_REGEX = "[^a-zA-Z0-9]";
        final String DIACRITICAL_ACCENT_MARKS_REGEX = "\\p{M}";
        final String TWO_OR_MORE_SPACES_REGEX = "\\s+";

        String result = Normalizer.normalize(songNameAndArtist, Normalizer.Form.NFKD)
                .replaceAll("ñ","n")
                .replaceAll("&"," and ")
                .replaceAll("ç","c")
                .replaceAll(DIACRITICAL_ACCENT_MARKS_REGEX, "")
                .replaceAll(SPECIAL_CHARS_REGEX," ")
                .replaceAll(TWO_OR_MORE_SPACES_REGEX, " ");

        String[] words = result.split(" ");
        StringBuilder cleanedText = new StringBuilder();
        for (String word : words) {
            if (!wordsToRemove.contains(word.toLowerCase()) && word.length() > 2) {
                cleanedText.append(word).append(" ");
            }
        }

        String finalCleanedText = cleanedText.toString().isBlank()
                ? songNameAndArtist
                : cleanedText.toString();

        return finalCleanedText.trim();
    }

    private SoulSyncConfiguration.Finder getConfiguration(){
        return ConfigurationService.instance().cfg().finder();
    }
}
