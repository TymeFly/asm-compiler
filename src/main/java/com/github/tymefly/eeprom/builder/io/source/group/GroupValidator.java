package com.github.tymefly.eeprom.builder.io.source.group;

import javax.annotation.Nonnull;

/**
 * Defines the contract for checking the group names in a source file to see if they are valid
 */
public interface GroupValidator {

    /**
     * Returns {@literal true} only if the {@code name} is valid
     * @param name      Name to check
     * @return {@literal true} only if the {@code name} is valid
     */
    boolean isValid(@Nonnull String name);
}
