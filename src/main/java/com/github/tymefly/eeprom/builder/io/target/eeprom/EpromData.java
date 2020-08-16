package com.github.tymefly.eeprom.builder.io.target.eeprom;

import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import com.github.tymefly.eeprom.builder.project.input.InputPin;
import com.github.tymefly.eeprom.builder.project.output.Rom;

/**
 * Access to an single EEPROM element
 * @see Content.Entry
 */
public interface EpromData {
    /**
     * Returns the OpCode for this element
     * @return the OpCode for this element
     */
    int getOpCode();

    /**
     * Returns the MCycle associated with this element
     * @return the MCycle associated with this element
     */
    int getMCycle();

    /**
     * Returns the address associated with this entry
     * @return the address associated with this entry
     */
    int getAddress();

    /**
     * Returns the immutable data bytes in the EEPROM to this entry
     * @return the immutable data bytes in the EEPROM to this entry
     */
    @Nonnull
    Map<Rom, Byte> getData();

    /**
     * Returns all of the active flags associated with this entry
     * @return all of the active flags associated with this entry
     */
    @Nonnull
    Set<InputPin> getActiveFlags();

    /**
     * Returns the annotations associated with this entry
     * @return the annotations associated with this entry
     */
    @Nonnull
    String getAnnotation();
}
