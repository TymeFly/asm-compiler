package com.github.tymefly.eeprom.builder.project.input;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;


/**
 * A class that describes the configuration of an Input (Address line) Pin.
 */
@Immutable
public class InputPin implements Comparable<InputPin> {
    private final InputTypes inputTypes;
    private final String name;
    private final int pin;

    InputPin(@Nonnull InputTypes inputTypes, @Nonnull String name, int pin) {
        this.inputTypes = inputTypes;
        this.name = name;
        this.pin = pin;
    }


    /**
     * Returns the input group that the pin belongs to
     * @return the input group that the pin belongs to
     */
    @Nonnull
    public InputTypes getInputTypes() {
        return inputTypes;
    }


    /**
     * Returns the unique name of this InputPin
     * @return the unique name of this InputPin
     */
    @Nonnull
    public String getName() {
        return name;
    }


    /**
     * Returns the unique Address line bit number
     * @return the unique Address line bit number
     */
    public int getPin() {
        return pin;
    }


    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (other == null) {
            return false;
        } else if(getClass() != other.getClass()) {
            return false;
        } else {
            InputPin otherPin = (InputPin) other;

            return pin == otherPin.pin;
        }
    }

    @Override
    public int hashCode() {
        return pin;
    }


    @Override
    public int compareTo(@Nonnull InputPin other) {
        return (other.pin - pin);
    }


    @Override
    public String toString() {
        return "InputPin{group=" + getInputTypes() + ", name='" + getName() + '\'' + ", pin=" + getPin() + '}';
    }
}
