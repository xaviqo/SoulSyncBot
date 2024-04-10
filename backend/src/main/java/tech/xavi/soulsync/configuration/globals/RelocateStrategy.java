package tech.xavi.soulsync.configuration.globals;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum RelocateStrategy {
    BY_PLAYLIST("Relocate finished downloads by playlist"),
    BY_ARTIST("Relocate finished downloads by artist/album"),
    BY_ALBUM("Relocate finished downloads by album");

    private final String description;
}
