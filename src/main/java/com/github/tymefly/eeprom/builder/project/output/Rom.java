package com.github.tymefly.eeprom.builder.project.output;

import javax.annotation.Nonnull;

/**
 * Enumeration of heading that can appear in the output description file.
 * Each section defines a single ROM chip. Not all ROM files have to be generated
 */
public enum Rom {
    ROM_1(1),
    ROM_2(2),
    ROM_3(3),
    ROM_4(4),
    ROM_5(5),
    ROM_6(6),
    ROM_7(7),
    ROM_8(8);


    private final int romNumber;
    private final String fileName;


    Rom(int romNumber) {
        this.romNumber = romNumber;
        this.fileName = "Rom" + romNumber;
    }


    /**
     * Returns the unique number of the ROM
     * @return the unique number of the ROM
     */
    public int getRomNumber() {
        return romNumber;
    }


    /**
     * Returns the file name for the generated ROM
     * @param extension     Type of the generated ROM file. This must not include a leading '.'
     * @return the file name for the generated ROM
     */
    @Nonnull
    public String fileName(@Nonnull String extension) {
        return fileName + "." + extension;
    }
}
