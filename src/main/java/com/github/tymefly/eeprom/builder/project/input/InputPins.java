package com.github.tymefly.eeprom.builder.project.input;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.tymefly.eeprom.builder.utils.Preconditions;


/**
 * A class used to describe all of the Input (Address Line) Pins
 */
public class InputPins {
    private final Map<InputTypes, Map<String, InputPin>> byGroup;
    private final Map<String, InputPin> byName;
    private int maxMCycle = 0;
    private int irMask = 0;
    private int maxPin = -1;
    private IrBit maxIrPin = IrBit.IR_0;

    InputPins() {
        byName = new HashMap<>();
        byGroup = new EnumMap<>(InputTypes.class);

        for (var inputType : InputTypes.values()) {
            byGroup.put(inputType, new HashMap<>());
        }
    }



    /**
     * Define a new InputPin. This method will return {@literal null} to indicate that the definition
     * is invalid; Typically this is because the pin has already been defined
     * @param inputType     The input group that the pin belongs to
     * @param name          The unique name of the pin
     * @param pin           The unique address line bit number
     * @return              A new InputPin definition, or {@literal null} if the definition was invalid.
     */
    @Nullable
    InputPin define(@Nonnull InputTypes inputType, @Nonnull String name, int pin) {
        InputPin defined;

        if (hasPin(name)) {
            defined = null;
        } else {
            defined = new InputPin(inputType, name, pin);
            maxPin = Math.max(pin, maxPin);

            byGroup.get(inputType).put(name, defined);
            byName.put(name, defined);
        }

        return defined;
    }


    // zero based
    void setMaxMCycle(int maxMCycle) {
        this.maxMCycle = maxMCycle;
    }


    /**
     * Returns the highest MCycle number. MCycle numbers are 0 based then the count of MCycles is one larger
     * then the returned value
     * @return the highest MCycle number.
     */
    public int getMaxMCycle() {
        return maxMCycle;
    }


    void setMaxIrPin(@Nonnull IrBit maxIrPin) {
        this.maxIrPin = maxIrPin;
    }


    /**
     * Returns the highest configured bit in the Instruction Register
     * @return the highest configured bit in the Instruction Register
     */
    @Nonnull
    public IrBit getMaxIrPin() {
        return maxIrPin;
    }


    void setIrMask(int irMask) {
        this.irMask = irMask;
    }


    /**
     * Returns the IR Register mask.
     * Each bit that is set represents indicates that the corresponding {@link IrBit} has been assigned
     * @return the IR Register mask.
     */
    public int getIrMask() {
        return irMask;
    }


    /**
     * Returns {@literal true} only if {@code name} is a valid InputPin name
     * @param name      case sensitive name of a potential pin
     * @return {@literal true} only if {@code name} is a valid InputPin name
     */
    public boolean hasPin(@Nonnull String name) {
        return byName.containsKey(name);
    }


    /**
     * Returns highest defined bit value of the input pins
     * @return highest defined bit value of the input pins
     */
    public int maxPin() {
        return maxPin;
    }


    /**
     * Returns the definition of the named input pin. If not such pin exists then {@literal null} is returned
     * @param inputType     The type of the required pin
     * @param name          The name of the required pin
     * @return the definition of the named input pin.
     */
    @Nullable
    public InputPin getPin(@Nonnull InputTypes inputType, @Nonnull String name) {
        return byGroup.get(inputType).get(name);
    }


    /**
     * Returns the definition of the named input pin.
     * @param name      Name of the input pin
     * @return the definition of the named input pin
     * @throws IllegalArgumentException if there is no pin with the specified name
     * @see #hasPin(String)
     */
    @Nonnull
    public InputPin getPin(@Nonnull String name) throws IllegalArgumentException{
        InputPin pin = byName.get(name);

        Preconditions.checkArgument((pin != null), "Invalid InputPin %s", pin);

        return pin;
    }


    /**
     * Returns an immutable collection of all configured input pins associated with an inputType
     * @param inputType     The type of the required Pins
     * @return an immutable collection of all configured input pins associated with an inputType
     */
    @Nonnull
    public Collection<InputPin> getPins(@Nonnull InputTypes inputType) {
        return Collections.unmodifiableCollection(byGroup.get(inputType).values());
    }
}
