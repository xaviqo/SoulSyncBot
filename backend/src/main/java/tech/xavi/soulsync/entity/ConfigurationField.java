package tech.xavi.soulsync.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Arrays;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@Getter
@RequiredArgsConstructor
public enum ConfigurationField {

    // API
    SPOTIFY_CLIENT_ID("spotify client id",Section.API, DataType.TEXT,"You can get both ID and key from the spotify developers dashboard",null,null,null),
    SPOTIFY_API_SECRET("spotify client secret",Section.API, DataType.TEXT,"You can get both ID and key from the spotify developers dashboard",null,null,null),
    SLSKD_USERNAME("slskd username",Section.API, DataType.TEXT,"Username for Slskd panel",null,null, "slskd"),
    SLSKD_PASSWORD("slskd password",Section.API, DataType.TEXT,"Password for Slskd panel",null,null,"slskd"),
    SLSKD_API_URL("slskd api url",Section.API, DataType.TEXT,"Slskd Api URL",null,null,"http://localhost:5030"),
    IS_APP_INSTALLED(null,null, DataType.BOOLEAN,null,null,null,null)
    ;

    private final String fieldName;
    private final Section section;
    private final DataType dataType;
    private final String description;
    private final Integer min;
    private final Integer max;
    private final Object defaultValues;
    @Setter
    private JsonNode value;

    public static ConfigurationField findEnumByName(String name) {
        return Arrays.stream(values())
                .filter(e -> e.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No ConfigurationField found with name: " + name));
    }

    @Getter @RequiredArgsConstructor
    public enum Section {
        API,
        FINDER,
        BOT
        ;
    }

    @Getter
    @RequiredArgsConstructor
    public enum DataType {
        TEXT("Value must be a text string"),
        ARRAY("It should be a list of values"),
        NUMBER("The value must be a number"),
        RANGE("The value is out of accepted range"),
        SELECT("The value is not in the list of valid options"),
        BOOLEAN("The value can only be TRUE or FALSE")
        ;
        private final String inputErrorMsg;
    }

    public String getName(){
        return this.name();
    }

    @JsonIgnore
    public Class<?> getClazz() {
        return switch (dataType) {
            case TEXT -> String.class;
            case ARRAY -> Object[].class;
            case NUMBER -> Number.class;
            case RANGE -> Integer.class;
            case SELECT -> Enum.class;
            case BOOLEAN -> Boolean.class;
        };
    }

}
