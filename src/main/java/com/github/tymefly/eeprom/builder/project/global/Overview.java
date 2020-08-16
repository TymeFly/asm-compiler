package com.github.tymefly.eeprom.builder.project.global;

import java.util.function.BiConsumer;

import javax.annotation.Nonnull;

/**
 * Defines all of the settings in the "Overview" section of the main project description file
 */
enum Overview implements Setting {
    NAME("Name", GlobalData::setName),
    VERSION("Version", GlobalData::setVersion);

    private final String key;
    private final BiConsumer<GlobalData, String> setter;


    Overview(@Nonnull String key, @Nonnull BiConsumer<GlobalData, String> setter) {
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
