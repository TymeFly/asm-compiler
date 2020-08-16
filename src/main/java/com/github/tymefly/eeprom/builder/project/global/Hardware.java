package com.github.tymefly.eeprom.builder.project.global;

import java.util.function.BiConsumer;

import javax.annotation.Nonnull;

/**
 * Defines all of the settings in the "Hardware" section of the main project description file
 */
enum Hardware implements Setting {
    MAX_ADDRESS_BIT("MaxAddressBit", GlobalData::setMaxAddressBit);

    private final String key;
    private final BiConsumer<GlobalData, String> setter;


    Hardware(@Nonnull String key, @Nonnull BiConsumer<GlobalData, String> setter) {
        this.key = key;
        this.setter = setter;
    }


    @Nonnull
    public String key() {
        return key;
    }


    @Nonnull
    public BiConsumer<GlobalData, String> setter() {
        return setter;
    }
}
