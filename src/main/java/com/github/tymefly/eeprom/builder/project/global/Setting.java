package com.github.tymefly.eeprom.builder.project.global;

import java.util.function.BiConsumer;

import javax.annotation.Nonnull;

/**
 * Interface for enumerations that set project write settings
 */
interface Setting {
    /**
     * Returns the name of the key in the settings file
     * @return the name of the key in the settings file
     */
    @Nonnull
    String key();


    /**
     * Returns the setter method
     * @return the setter method
     */
    @Nonnull
    BiConsumer<GlobalData, String> setter();
}
