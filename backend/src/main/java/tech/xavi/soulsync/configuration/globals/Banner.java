package tech.xavi.soulsync.configuration.globals;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Banner {

    ZERO("     \n" +
            " ___ \n" +
            "|   |\n" +
            "| | |\n" +
            "|___|\n" +
            "     "),
    ONE("       \n" +
            " ___   \n" +
            "|_  |  \n" +
            " _| |_ \n" +
            "|_____|\n" +
            "       "),
    TWO("     \n" +
            " ___ \n" +
            "|_  |\n" +
            "|  _|\n" +
            "|___|\n" +
            "     "),
    THREE("     \n" +
            " ___ \n" +
            "|_  |\n" +
            "|_  |\n" +
            "|___|\n" +
            "     "),
    FOUR("     \n" +
            " ___ \n" +
            "| | |\n" +
            "|_  |\n" +
            "  |_|\n" +
            "     "),
    FIVE("     \n" +
            " ___ \n" +
            "|  _|\n" +
            "|_  |\n" +
            "|___|\n" +
            "     "),
    SIX("     \n" +
            " ___ \n" +
            "|  _|\n" +
            "| . |\n" +
            "|___|\n" +
            "     "),
    SEVEN("     \n" +
            " ___ \n" +
            "|_  |\n" +
            "  | |\n" +
            "  |_|\n" +
            "     "),
    EIGHT("     \n" +
            " ___ \n" +
            "| . |\n" +
            "| . |\n" +
            "|___|\n" +
            "     "),
    NINE("     \n" +
            " ___ \n" +
            "| . |\n" +
            "|_  |\n" +
            "|___|\n" +
            "     "),
    SOULSYNC("                                   \n" +
            " _____         _ _____             \n" +
            "|   __|___ _ _| |   __|_ _ ___ ___ \n" +
            "|__   | . | | | |__   | | |   |  _|\n" +
            "|_____|___|___|_|_____|_  |_|_|___|\n" +
            "                      |___|        \n"),
    V("     \n" +
            "     \n" +
            " _ _ \n" +
            "| | |\n" +
            " \\_/ \n" +
            "     "),
    DOT("   \n" +
            "   \n" +
            "   \n" +
            " _ \n" +
            "|_|\n" +
            "   \n"),
    SPACE("  \n" +
            "  \n" +
            "  \n" +
            "  \n" +
            "  \n" +
            "  \n");

    private final String text;

    public String getLine(int index) {
        return getLines()[index];
    }

    public String[] getLines(){
        return getText().split("\\r?\\n");
    }
}