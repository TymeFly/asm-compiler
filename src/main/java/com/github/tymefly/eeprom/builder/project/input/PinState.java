package com.github.tymefly.eeprom.builder.project.input;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static java.util.stream.Collectors.toMap;

/**
 * Enumeration of possible values each one of the {@link com.github.tymefly.eeprom.builder.project.input.InputPin}
 * bits can take
 */
public enum PinState {
    ACTIVE('1'),
    INACTIVE('0'),
    EITHER('x');

    private static final Map<Character, PinState> FROM_TEXT =
            Stream.of(values())
                    .collect(toMap(PinState::getText, Function.identity()));

    private final char text;


    PinState(char text) {
        this.text = text;
    }


    /**
     * Returns the text description of this PinState
     * @return the text description of this PinState
     */
    public char getText() {
        return text;
    }


    /**
     * Lookup the PinState based on a text description. If the description is invalid then {@literal null} is returned
     * @param text      text description of the PinState
     * @return the PinState based on a text description
     */
    @Nullable
    public static PinState fromText(@Nonnull String text) {
        PinState found = (text.length() != 1 ? null : FROM_TEXT.get(text.toLowerCase().charAt(0)));

        return found;
    }
}
