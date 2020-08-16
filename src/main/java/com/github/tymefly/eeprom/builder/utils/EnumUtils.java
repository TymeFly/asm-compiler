package com.github.tymefly.eeprom.builder.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Utility functions for Enumeration types
 */
public class EnumUtils {
    private EnumUtils() {
    }


    /**
     * Returns the enumeration constant with the required {@code name}. It is assumed that at most one
     * member of the enumeration type has the correct, case insensitive name.
     * @param name      Case insensitive name
     * @param type      Type of the enumeration constant
     * @param <E>       Type of the enumeration constant
     * @return          A element in {@code E} where E.name().equalsIgnoreCase(name) return true, or
     *                  {@literal null} if no such element exists.
     */
    @Nullable
    public static <E extends Enum<E>> E getEnum(@Nonnull String name, @Nonnull Class<E> type) {
        E found = null;

        for (var element : type.getEnumConstants()) {
            if (element.name().equalsIgnoreCase(name)) {
                found = element;
                break;
            }
        }

        return found;
    }
}
