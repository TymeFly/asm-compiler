package com.github.tymefly.eeprom.builder.io.target.eeprom;

import javax.annotation.Nonnull;


/**
 * Contract for a class that writes EEPROM data
 */
public interface ContentWriter extends AutoCloseable {
    /**
     * Returns a description of the data written by this object
     * @return a description of the data written by this object
     */
    @Nonnull
    String getDescription();

    /**
     * Initialise the output format. This may involve adding metadata to the generated file
     */
    void initialise();


    /**
     * Apply a new entry to the EEPROM
     * @param data      element to apply
     */
    void apply(@Nonnull EpromData data);


    /**
     * Flush any remaining data to disc and close the file
     */
    void close();
}
