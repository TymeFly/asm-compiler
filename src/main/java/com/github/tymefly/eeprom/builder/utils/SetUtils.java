package com.github.tymefly.eeprom.builder.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Set manipulation functions
 */
public class SetUtils {
    private SetUtils() {
    }

    /**
     * Returns the power set of the {@code elements}.
     * The power set is the set of all possible sets that can contain the {@code elements}.
     * Consequently the size of the power set will be {@code 2 ^ elements.size()}
     * @param elements      Elements that c
     * @param <T>           The type of the elements
     * @return the power set of the {@code elements}
     */
    @Nonnull
    public static <T> Set<Set<T>> powerSet(@Nonnull Collection<T> elements) {
        Set<Set<T>> result = new HashSet<>();
        List<T> list = new ArrayList<>(elements);
        long powerSize = 1 << list.size();

        for (var powerIndex = 0; powerIndex < powerSize; powerIndex++) {
            Set<T> set = new HashSet<>();

            for (var elementIndex = 0; elementIndex < list.size(); elementIndex++) {
                if ((powerIndex & (1 << elementIndex)) != 0) {
                    set.add(list.get(elementIndex));
                }
            }

            result.add(set);
        }

        return result;
    }
}
