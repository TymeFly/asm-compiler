package com.github.tymefly.eeprom.builder.io.target.eeprom.map;

import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nonnull;

import com.github.tymefly.eeprom.builder.project.input.InputPin;
import com.github.tymefly.eeprom.builder.project.input.InputPins;
import com.github.tymefly.eeprom.builder.project.input.InputTypes;
import com.github.tymefly.eeprom.builder.utils.StringUtils;

/**
 * Format the flags in the Map report
 */
class FlagFormatter {
    private final NavigableSet<InputPin> flags;
    private final int flagWidth;
    private final int formatWidth;

    FlagFormatter(@Nonnull InputPins inputPins) {
        flags = new TreeSet<>(inputPins.getPins(InputTypes.FLAG));
        flagWidth = flags.stream()
             .mapToInt(f -> f.getName().length())
             .max()
             .orElse(0) + 1;     // Add a single space as a separator
        formatWidth = (flagWidth * flags.size());
    }


    int width() {
        return formatWidth;
    }


    @Nonnull
    String format(@Nonnull Set<InputPin> active) {
        StringBuilder buffer = new StringBuilder();

        for (var flag : flags) {
            String name = (active.contains(flag) ? flag.getName() : "");

            StringUtils.append(buffer, name, flagWidth);
        }

        return buffer.toString();
    }
}
