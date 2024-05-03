package tech.xavi.soulsync.service.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.configuration.globals.SearchInputStrategy;
import tech.xavi.soulsync.entity.datafile.SearchPolicy;
import tech.xavi.soulsync.entity.db.SpotifySong;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class SearchInputService {

    private static final String SPECIAL_CHARS_REGEX = "[^a-zA-Z0-9]";
    private static final String DIACRITICAL_ACCENT_MARKS_REGEX = "\\p{M}";
    private static final String TWO_OR_MORE_SPACES_REGEX = "\\s+";


    public String getSearchInput(SpotifySong spotifySong, SearchPolicy searchPolicy){
        SearchInputStrategy inputStrategy = searchPolicy.getInputStrategy();
        String cleanSongName = getCleanString(spotifySong.getName(),searchPolicy) + " ";
        return switch (inputStrategy) {
            case STANDARD_STRATEGY ->
                    cleanSongName + getCleanString(spotifySong.getFirstArtistName(), searchPolicy);
            case WILDCARDS_TO_ARTIST ->
                    cleanSongName + getArtistWithWildcard(spotifySong);
            case ALBUM_INSTEAD_ARTIST ->
                    cleanSongName + getCleanString(spotifySong.getAlbum(), searchPolicy);
        };
    }

    private String normalizeInput(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFKD)
                .replaceAll("ñ","n")
                .replaceAll("&"," ")
                .replaceAll("ç","c")
                .replaceAll(DIACRITICAL_ACCENT_MARKS_REGEX, "")
                .replaceAll(SPECIAL_CHARS_REGEX," ")
                .replaceAll(TWO_OR_MORE_SPACES_REGEX, " ");
    }

    private String removeWordsByMinCharsThreshold(String input, int minChars){
        String[] words = input.split(" ");
        StringBuilder cleanedText = new StringBuilder();
        for (String word : words)
            if (word.length() >= minChars)
                cleanedText.append(word).append(" ");
        return String.valueOf(cleanedText);
    }

    private String removeWordsFromList(String input, List<String> stringsToRemove) {
        String[] words = input.split(" ");
        StringBuilder cleanedText = new StringBuilder();
        for (String word : words)
            if (!stringsToRemove.contains(word.toLowerCase()))
                cleanedText.append(word).append(" ");
        return String.valueOf(cleanedText);
    }

    private String getArtistWithWildcard(SpotifySong spotifySong) {
        String normalizedArtistName = normalizeInput(spotifySong.getFirstArtistName());
        char[] artistCharArray = normalizedArtistName.toCharArray();
        artistCharArray[artistCharArray.length - 1] = '*';
        return new String(artistCharArray);
    }

    private String getCleanString(String string, SearchPolicy searchPolicy){
        int charsThreshold = searchPolicy
                .getMinCharsPerWord();

        List<String> stringsToRemove = Arrays
                .stream(searchPolicy.getWordsToRemoveByComa().split(","))
                .toList();

        String cleanString = Stream.of(string)
                .map(this::normalizeInput)
                .map( str -> this.removeWordsFromList(str,stringsToRemove) )
                .map( str -> this.removeWordsByMinCharsThreshold(str,charsThreshold) )
                .collect(Collectors.joining(" "))
                .trim();

        return cleanString.isEmpty()
                ? string
                : cleanString;
    }
}
