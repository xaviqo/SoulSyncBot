package tech.xavi.soulsync.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.dto.gateway.SlskdSearchQuery;
import tech.xavi.soulsync.dto.gateway.SlskdSearchResult;
import tech.xavi.soulsync.dto.gateway.SlskdSearchStatus;
import tech.xavi.soulsync.dto.gateway.SpotifySong;
import tech.xavi.soulsync.gateway.SlskdGateway;
import tech.xavi.soulsync.model.Song;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j2
@Service
public class SearchService {

    private final List<String> COMMON_WORDS_TO_REMOVE;
    private final long WAIT_SEC_BTW_RESULT_REQ;
    private final int MAX_RETRIES_RESULT_REQ;
    private final SlskdGateway slskdGateway;
    private final AuthService authService;
    private final ExecutorService threadExecutor;
    private final RateLimitDelayService delayService;

    public SearchService(
            @Value("${tech.xavi.soulsync.cfg.common-words-to-remove}") String commonWordsStr,
            @Value("${tech.xavi.soulsync.cfg.max-concurrent-threads}") int maxConcurrentSearches,
            @Value("${tech.xavi.soulsync.cfg.wait-sec-btw-result-req}") int waitSecBtwResReq,
            @Value("${tech.xavi.soulsync.cfg.max-retries-result-req}") int maxRetResReq,
            SlskdGateway slskdGateway,
            AuthService authService,
            RateLimitDelayService delayService
    ) {
        this.COMMON_WORDS_TO_REMOVE = Arrays.stream(commonWordsStr.split(",")).toList();
        this.WAIT_SEC_BTW_RESULT_REQ = waitSecBtwResReq;
        this.MAX_RETRIES_RESULT_REQ = maxRetResReq;
        this.threadExecutor = Executors.newFixedThreadPool(maxConcurrentSearches);
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
        log.debug("[searchSong] - Delay required. Random Milliseconds:: {}",delayMs);
        log.debug("[searchSong] - Song search is initiated: ({}) with id ({})",
                song.getSearchInput(),
                song.getSearchId()
        );
    }

    public SlskdSearchResult[] fetchResults(Song song) {
        synchronized (song) {
            int retries = 0;
            do {
                int delayMs = delayService.delay();
                log.debug("[fetchResults] - Delay required. Random Milliseconds:: {}",delayMs);
                SlskdSearchStatus searchStatus = isSearchComplete(song);
                if (searchStatus == null){
                    log.debug("[fetchResults] - Search is not available, 'isComplete' value is NULL: {}, {}", song.getSearchInput(), song.getSearchId());
                    song.notify();
                    break;
                }
                if (searchStatus.isComplete()) {
                    log.debug("[fetchResults] - Search process finished, " +
                            "a list of results is being requested | SearchInput: {} SearchId: {}", song.getSearchInput(), song.getSearchId());

                    return slskdGateway.getSearchResults(
                            song.getSearchId().toString(),
                            authService.getSlskdToken().token()
                    );
                } else {
                    if (retries == MAX_RETRIES_RESULT_REQ-1) {
                        log.debug("[fetchResults] - The search process has not been completed after {} retries. " +
                                        "It will be postponed for the next hunting | SearchInput: {} SearchId: {}",
                                MAX_RETRIES_RESULT_REQ, song.getSearchInput(), song.getSearchId());
                    } else {
                        log.debug("[fetchResults] (Attempt #{}) - The search process has not yet been completed, " +
                                        "waiting {} sec for the next attempt | SearchInput: {} SearchId: {}",
                                (retries+1), WAIT_SEC_BTW_RESULT_REQ, song.getSearchInput(), song.getSearchId());
                    }
                    retries++;

                    try {
                        song.wait(WAIT_SEC_BTW_RESULT_REQ * 1000);
                    } catch (InterruptedException e) {
                        log.error("InterruptedException occurred while waiting for the next attempt: {}, {}, {}",
                                e.getMessage() ,
                                song.getSearchInput(),
                                song.getSearchId()
                        );
                        Thread.currentThread().interrupt();
                    }
                }
            } while (retries < MAX_RETRIES_RESULT_REQ);
        }
        log.debug("[fetchResults] - No search results found for: {}, {}", song.getSearchInput(), song.getSearchId());
        return new SlskdSearchResult[0];
    }

    private SlskdSearchStatus isSearchComplete(Song song){
        return slskdGateway.checkSearchStatus(
                song.getSearchId().toString(),
                authService.getSlskdToken().token()
        );
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

    private String removeSpecialChars(String songNameAndArtist){
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
            if (!COMMON_WORDS_TO_REMOVE.contains(word.toLowerCase())) {
                cleanedText.append(word).append(" ");
            }
        }

        String finalCleanedText = cleanedText.toString().isBlank()
                ? songNameAndArtist
                : cleanedText.toString();

        return finalCleanedText.trim();
    }
}
