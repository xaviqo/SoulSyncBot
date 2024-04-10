package tech.xavi.soulsync.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor @Getter
public enum SearchInputStrategyName {
    STANDARD_STRATEGY("Standard search strategy (eliminates symbols, configured words, short words...)"),
    ADD_WILDCARDS_TO_ARTIST("Some artists are banned on the soulseek network, add wildcards to the name (the beatles --> *eatles)"),
    ADD_ALBUM_INSTEAD_ARTIST("Add album name instead of artist");

    private final String description;
}
