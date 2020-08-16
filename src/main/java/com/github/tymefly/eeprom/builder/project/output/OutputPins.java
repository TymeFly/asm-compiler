package com.github.tymefly.eeprom.builder.project.output;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.github.tymefly.eeprom.builder.utils.Preconditions;


/**
 * A Model of the output (data) pins for each of the EEPROMs that are defined in the project.
 */
@Immutable
public class OutputPins {
    private final Collection<Rom> roms;
    private final Map<Rom, Set<OutputPin>> byRom;
    private final Map<String, OutputPin> byName;


    OutputPins() {
        roms = new TreeSet<>();
        byRom = new HashMap<>();
        byName = new LinkedHashMap<>();
    }


    /**
     * Factory method used to create a new OutputPin
     * @param name          The unique name of the Output pin
     * @param rom           The EEPROM assigned to this pin
     * @param pin           The Data bit number
     * @param activeLow     {@literal true} if this line is active low;
     *                      {@literal false} indicates that this line is normally active high
     * @return              A new OutputPin or {@literal null} if there is a duplicate definition
     */
    @Nullable
    OutputPin define(@Nonnull String name, @Nonnull Rom rom, int pin, boolean activeLow) {
        var newPin = new OutputPin(name, rom, pin, activeLow);
        var oldPin = byName.put(name.toLowerCase(), newPin);

        roms.add(rom);
        byRom.computeIfAbsent(rom, r -> new HashSet<>()).add(newPin);

        return (oldPin == null ? newPin : null);
    }


    /**
     * Returns the {@link OutputPin} with the unique {@code name} or {@literal null} if no such pin exists
     * @param name      Name of the required pin
     * @return the {@link OutputPin} with the unique {@code name} or {@literal null} if no such pin exists
     */
    @Nullable
    public OutputPin getPin(@Nonnull String name) {
        return byName.get(name.toLowerCase());
    }


    /**
     * Returns an immutable, sorted, collection of all EEPROMs used by this project
     * @return an immutable, sorted, collection of all EEPROMs used by this project
     */
    @Nonnull
    public Collection<Rom> getRoms() {
        return Collections.unmodifiableCollection(roms);
    }


    /**
     * Returns an immutable collection describing all of the pins used by a {@code rom}
     * @param rom       One of the Rom object returned by {@link #getRoms()}
     * @return an immutable collection describing all of the pins used by a {@code rom}
     * @see #getRoms()
     */
    public Collection<OutputPin> getPins(@Nonnull Rom rom) {
        Preconditions.checkArgument(roms.contains(rom), "Invalid rom %s", rom);

        return Collections.unmodifiableCollection(byRom.get(rom));
    }


    /**
     * Returns an immutable collection describing all of the pins
     * @return an immutable collection describing all of the pins
     */
    public Collection<OutputPin> getPins() {
        return Collections.unmodifiableCollection(byName.values());
    }
}
