package com.github.tymefly.eeprom.builder.io.target.eeprom;

import javax.annotation.Nonnull;

import com.github.tymefly.eeprom.builder.project.input.InputPin;
import com.github.tymefly.eeprom.builder.project.input.PinState;
import com.github.tymefly.eeprom.builder.project.output.Rom;


/**
 * Contract for a class that writes EEPROM content
 */
public interface Content extends AutoCloseable {
    /**
     * A single entry in the EEPROM file
     */
    interface Entry {
        /**
         * Set the OpCode associated with this entry
         * @param code      OpCode
         * @return          A fluent interface
         */
        @Nonnull
        Entry forOpCode(int code);

        /**
         * Set the MCycle associated with this entry
         * @param mCycle    Machine cycle
         * @return          A fluent interface
         */
        @Nonnull
        Entry forMCycle(int mCycle);

        /**
         * Set the flags associated with this entry
         * @param pin       Input pin signaling the flag
         * @param state     The state of the flag
         * @return          A fluent interface
         */
        @Nonnull
        Entry withFlag(@Nonnull InputPin pin, @Nonnull PinState state);

        /**
         * Set the address associated with this entry
         * @param address       the address of the entry
         * @return              A fluent interface
         */
        @Nonnull
        Entry forAddress(int address);

        /**
         * Add the data bytes in the EEPROM to this entry
         * @param rom           The EEPROM
         * @param value         value stored at the address
         * @return              A fluent interface
         */
        @Nonnull
        Entry toData(@Nonnull Rom rom, byte value);

        /**
         * Add an optional annotation to this entry
         * @param annotation    description of the entry
         * @return              A fluent interface
         */
        @Nonnull
        Entry withAnnotation(@Nonnull String annotation);

        /**
         * Add all the information in this entry to the EEPROM file.
         * After calling this method no further methods in this class can be called
         */
        void apply();
    }


    /**
     * Create a new entry in the EEPROM file
     * @return a new entry in the EEPROM file
     */
    @Nonnull
    Entry entry();

    /**
     * Flush any remaining data to disc and close the file
     */
    void close();
}
