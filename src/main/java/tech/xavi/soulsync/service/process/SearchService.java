package tech.xavi.soulsync.service.process;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.dto.gateway.slskd.SlskdSearchQuery;
import tech.xavi.soulsync.dto.gateway.slskd.SlskdSearchResult;
import tech.xavi.soulsync.dto.gateway.slskd.SlskdSearchStatus;
import tech.xavi.soulsync.dto.gateway.spotify.SpotifySong;
import tech.xavi.soulsync.entity.Song;
import tech.xavi.soulsync.entity.SoulSyncConfiguration;
import tech.xavi.soulsync.exception.SeekProcessError;
import tech.xavi.soulsync.exception.SeekProcessException;
import tech.xavi.soulsync.gateway.SlskdGateway;
import tech.xavi.soulsync.service.configuration.ConfigurationService;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;

@Log4j2
@Service
public class SearchService {

    private final SoulSyncConfiguration.Finder searchConfiguration;
    private final SlskdGateway slskdGateway;
    private final AuthService authService;
    private final PauseService delayService;

    public SearchService(
            ConfigurationService configurationService,
            SlskdGateway slskdGateway,
            AuthService authService,
            PauseService delayService
    ) {
        this.searchConfiguration = configurationService.getConfiguration().finder();
        this.slskdGateway = slskdGateway;
        this.authService = authService;
        this.delayService = delayService;
    }

    public void searchSong(Song song){
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
        int maxRetries = searchConfiguration.getMaxRetriesWaitingResult();
        int retries = 1;
        do {
            try {
                if (isSearchComplete(song).isComplete())
                    return getResultArray(song);

                if (isAttemptLimitReached(retries,song))
                    break;

                pause(song,retries);
                retries++;

            } catch (SeekProcessException seekProcessException){
                log.error("[fetchResults] - {} - Search Input: {}",
                        seekProcessException.getSeekProcessError(),
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

    private SlskdSearchStatus isSearchComplete(Song song) throws SeekProcessException{
        SlskdSearchStatus searchStatus = slskdGateway.checkSearchStatus(
                song.getSearchId().toString(),
                authService.getSlskdToken().token()
        );
        if (searchStatus == null)
            throw new SeekProcessException(
                    SeekProcessError.NULL_RESULT_EXCEPTION
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
        int maxRetries = searchConfiguration.getMaxRetriesWaitingResult();
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
        List<String> wordsToRemove = Arrays.asList(searchConfiguration.getWordsToRemove());
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
}
