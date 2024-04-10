package tech.xavi.soulsync.service.search.strategy;

import tech.xavi.soulsync.entity.SpotifySong;

import java.text.Normalizer;
import java.util.List;

public class SearchInputUtil {

    private static final String SPECIAL_CHARS_REGEX = "[^a-zA-Z0-9]";
    private static final String DIACRITICAL_ACCENT_MARKS_REGEX = "\\p{M}";
    private static final String TWO_OR_MORE_SPACES_REGEX = "\\s+";

    public static String getSongAndArtistAndAlbum(SpotifySong spotifySong){
        return String.format(
                "%s %s %s",
                spotifySong.getName(),
                spotifySong.getArtists(),
                spotifySong.getAlbum()
        );
    }

    public static String getSongAndArtist(SpotifySong spotifySong){
        return String.format(
                "%s %s",
                spotifySong.getName(),
                spotifySong.getArtists()
        );
    }

    public static String normalizeInput(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFKD)
                .replaceAll("ñ","n")
                .replaceAll("&"," ")
                .replaceAll("ç","c")
                .replaceAll(DIACRITICAL_ACCENT_MARKS_REGEX, "")
                .replaceAll(SPECIAL_CHARS_REGEX," ")
                .replaceAll(TWO_OR_MORE_SPACES_REGEX, " ");
    }

    public static String removeWordsByMinCharsThreshold(String input, int minChars){
        String[] words = input.split(" ");
        StringBuilder cleanedText = new StringBuilder();
        for (String word : words)
            if (word.length() >= minChars)
                cleanedText.append(word).append(" ");
        return String.valueOf(cleanedText);
    }

    public static String removeUnwantedWords(String input, List<String> stringsToRemove) {
        String[] words = input.split(" ");
        StringBuilder cleanedText = new StringBuilder();
        for (String word : words)
            if (!stringsToRemove.contains(word.toLowerCase()))
                cleanedText.append(word).append(" ");
        return String.valueOf(cleanedText);
    }

}
