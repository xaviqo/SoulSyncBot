package tech.xavi.soulsync.entity.sub;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RelocateOption {

    MOVE("move"),
    COPY("copy");

    private final String action;

    public static RelocateOption getOption(String value){
        if (MOVE.getAction().equalsIgnoreCase(value))
            return MOVE;
        return COPY;
    }
}
