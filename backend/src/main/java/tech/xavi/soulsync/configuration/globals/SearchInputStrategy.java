package tech.xavi.soulsync.configuration.globals;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor @Getter
public enum SearchInputStrategy {
    STANDARD_STRATEGY("Standard search strategy (eliminates symbols, configured words, short words...)"),
    WILDCARDS_TO_ARTIST("Standard but add wildcards to artists names (some artists are banned on the soulseek network)  [The Beatles --> *eatles]"),
    ALBUM_INSTEAD_ARTIST("Standard but add album name instead of artist");
    private final String description;
}
