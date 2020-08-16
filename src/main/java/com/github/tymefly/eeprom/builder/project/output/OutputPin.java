package com.github.tymefly.eeprom.builder.project.output;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * A Model for each of the Data pins in in the system.
 */
@Immutable
public class OutputPin implements Comparable<OutputPin> {
    private final String name;
    private final Rom rom;
    private final int pin;
    private final boolean activeLow;


    /**
     * Construct a new Output pin definition
     * @param name          Name of the pin with out any leading {@code /}. This must be globally unique
     * @param rom           The EEPROM that drives the PIN
     * @param pin           The data bit number
     * @param activeLow     {@code true} for active low or {@code false} for active high
     */
    OutputPin(@Nonnull String name, @Nonnull Rom rom, int pin, boolean activeLow) {
        this.name = name;
        this.rom = rom;
        this.pin = pin;
        this.activeLow = activeLow;
    }


    /**
     * Returns the unique name given to this output pin. This will not have a leading {@code /} if the
     * pin is normally active low.
     * @return the unique name given to this output pin
     * @see #isActiveLow()
     */
    @Nonnull
    public String getName() {
        return name;
    }


    /**
     * Returns the Rom associated with the Output pin
     * @return the Rom associated with the Output pin
     */
    @Nonnull
    public Rom getRom() {
        return rom;
    }


    /**
     * Returns the Data bit number associated with this output pin
     * @return the Data bit number associated with this output pin
     */
    public int getPin() {
        return pin;
    }


    /**
     * Returns {@literal true} only if this output pin is active line; {@literal false} means the line active high
     * @return {@literal true} only if this output pin is active line; {@literal false} means the line active high
     */
    public boolean isActiveLow() {
        return activeLow;
    }


    @Override
    public boolean equals(Object other) {
        boolean equals;

        if (this == other) {
            equals = true;
        } else if (other == null) {
            equals = false;
        } else if (getClass() != other.getClass()) {
            equals = false;
        } else {
            OutputPin outputPin = (OutputPin) other;

            equals = (pin == outputPin.pin);
            equals = equals && (rom == outputPin.rom);
        }

        return equals;
    }


    @Override
    public int hashCode() {
        return (rom.hashCode() << 16) + pin;
    }


    @Override
    public String toString() {
        return "OutputPin{name='" + name + '\'' + ", chip=" + rom + ", pin=" + pin + ", activeLow=" + activeLow + '}';
    }

    @Override
    public int compareTo(@Nonnull OutputPin other) {
        int result = (getRom().getRomNumber() - other.getRom().getRomNumber());

        if (result == 0) {
            result = getPin() - other.getPin();
        }

        return result;
    }
}
