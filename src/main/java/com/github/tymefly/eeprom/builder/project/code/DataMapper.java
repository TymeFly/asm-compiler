package com.github.tymefly.eeprom.builder.project.code;

import java.util.Collection;
import java.util.Set;

import javax.annotation.Nonnull;

import com.github.tymefly.eeprom.builder.project.output.OutputPin;
import com.github.tymefly.eeprom.builder.project.output.OutputPins;
import com.github.tymefly.eeprom.builder.project.output.Rom;


/**
 * Map the {@link OutputPins} to a value in the EEPROM
 */
class DataMapper {
    private final OutputPins outputPins;

    DataMapper(@Nonnull OutputPins outputPins) {
        this.outputPins = outputPins;
    }


    int calculate(@Nonnull Rom rom, @Nonnull Set<OutputPin> pins) {
        Collection<OutputPin> allPins = outputPins.getPins(rom);
        int data = 0;

        for (var pin : allPins) {
            boolean enable = pins.contains(pin) ^ pin.isActiveLow();

            if (enable) {
                data |= (1 << pin.getPin());
            }
        }

        return data;
    }
}
