package tech.xavi.soulsync.entity.sub;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RelocateFinishedStrategy {

    MOVE,
    COPY;

    public static RelocateFinishedStrategy getOption(String value){
        if (MOVE.name().equalsIgnoreCase(value))
            return MOVE;
        return COPY;
    }
}
