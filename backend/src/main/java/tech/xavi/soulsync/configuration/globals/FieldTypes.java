package tech.xavi.soulsync.configuration.globals;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor @Getter
public enum FieldTypes {
    TEXT("Value must be a text string"),
    ARRAY("It should be a list of values"),
    NUMBER("The value must be a number"),
    RANGE("The value is out of accepted range"),
    SELECT("The value is not in the list of valid options"),
    BOOLEAN("The value can only be TRUE or FALSE")
    ;
    private final String inputErrorMsg;
}
