package com.github.tymefly.eeprom.builder.project.input;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import com.github.tymefly.eeprom.builder.exception.SourceFileException;
import com.github.tymefly.eeprom.builder.io.source.FileLoader;
import com.github.tymefly.eeprom.builder.io.source.ProjectFile;
import com.github.tymefly.eeprom.builder.io.source.SourceFile;
import com.github.tymefly.eeprom.builder.io.source.SourceGroup;
import com.github.tymefly.eeprom.builder.io.source.SourceLine;
import com.github.tymefly.eeprom.builder.project.Source;
import com.github.tymefly.eeprom.builder.utils.EnumUtils;
import com.github.tymefly.eeprom.builder.utils.SystemLimits;


/**
 * A factory class used to generate a model of the input (address) pins from a file in the project directory
 */
public class InputPinsFactory extends Source {
    private static final String PIN_NAME_PATTERN =
                "[a-z][a-z_]{0,20}";                        // Predefined name are upper case, flags are lowercase

    private InputPins inputPins;
    private Set<Integer> definedPins;


    private InputPinsFactory() {
        inputPins = new InputPins();
        definedPins = new HashSet<>();
    }


    /**
     * Parse the Input (address) Pin description file
     * @param projectDirectory  The directory that contains the project source code
     * @return a model that describes the input pins
     */
    @Nonnull
    public static InputPins parse(@Nonnull File projectDirectory) {
        InputPinsFactory me = new InputPinsFactory();
        SourceFile description = new FileLoader(projectDirectory, ProjectFile.INPUT_DEFINITION).read();

        return me.parse(description);
    }


    @Nonnull
    private InputPins parse(@Nonnull SourceFile description) {
        parse(description, InputTypes.IR, IrBit.class);
        parse(description, InputTypes.M_CYCLE, MCycle.class);
        parseFlags(description.getGroup(InputTypes.FLAG));

        requireState(description, InputTypes.IR);
        validateMCycle(description);
        validateIR();

        return inputPins;
    }


    private <E extends Enum<E>> void parse(@Nonnull SourceFile description,
                                           @Nonnull InputTypes inputTypes,
                                           @Nonnull Class<E> members) {
        var group = description.getGroup(inputTypes);
        var lines = group.getLines();

        for (var line : lines) {
            var columns = splitLine(line, 2, 2);
            var name = EnumUtils.getEnum(columns[0], members);
            var pin = parsePin(columns[1]);

            if (name == null) {
                throw new SourceFileException(line, "Invalid %s field '%s'", inputTypes, columns[0]);
            }

            if (!SystemLimits.isValidAddressBit(pin)) {
                throw new SourceFileException(line, "Invalid pin '%s' for '%s'", columns[1], name);
            }

            if (definedPins.contains(pin)) {
                throw new SourceFileException(line, "pin %d has been assigned multiple times", pin);
            }

            InputPin inputPin = inputPins.define(inputTypes, name.name(), pin);
            definedPins.add(pin);

            if (inputPin == null) {
                throw new SourceFileException(line, "pin %s has been defined multiple times", name.name());
            }
        }
    }


    private void parseFlags(@Nonnull SourceGroup flags) {
        var lines = flags.getLines();

        for (var line : lines) {
            var columns = splitLine(line, 2, 2);
            var name = columns[0];
            var pin = parsePin(columns[1]);

            if (!name.matches(PIN_NAME_PATTERN)) {
                throw new SourceFileException(line, "Invalid pin name '%s'", name);
            }

            if (!SystemLimits.isValidAddressBit(pin)) {
                throw new SourceFileException(line, "Invalid pin '%s' for '%s'", columns[1], name);
            }

            if (definedPins.contains(pin)) {
                throw new SourceFileException(line, "pin %d has been assigned multiple times", pin);
            }

            InputPin inputPin = inputPins.define(InputTypes.FLAG, name, pin);
            definedPins.add(pin);

            if (inputPin == null) {
                throw new SourceFileException(line, "pin %s has been defined multiple times", name);
            }
        }
    }


    // Check there isn't a missing pin
    private void validateMCycle(@Nonnull SourceFile description) {
        List<SourceLine> lines = requireState(description, InputTypes.M_CYCLE);
        SourceLine line = lines.get(lines.size() - 1);
        Collection<InputPin> pins = inputPins.getPins(InputTypes.M_CYCLE);
        MCycle[] values = MCycle.values();                                      // In the order they are declared
        MCycle last = values[pins.size() -1];                                   // The highest *expected* MCycle pin
        int lastValue = last.getValue();

        for (var pin : pins) {
            MCycle test = MCycle.valueOf(pin.getName());

            if (test.getValue() > lastValue) {
                throw new SourceFileException(line, "Non-contiguous %s pins defined", InputTypes.M_CYCLE.name());
            }
        }

        inputPins.setMaxMCycle(last.getMaxCycle());
    }


    private void validateIR() {
        int irMask = 0;
        IrBit max = IrBit.IR_0;
        Collection<InputPin> pins = inputPins.getPins(InputTypes.IR);

        for (var pin : pins) {
            String name = pin.getName();
            IrBit bit = IrBit.valueOf(name);

            irMask |= bit.getBitValue();
            max = (bit.getBitValue() > max.getBitValue()) ? bit : max;
        }

        inputPins.setIrMask(irMask);
        inputPins.setMaxIrPin(max);
    }


    @Nonnull
    private List<SourceLine> requireState(@Nonnull SourceFile description, @Nonnull InputTypes inputType) {
        List<SourceLine> lines = description.getGroup(inputType).getLines();

        if (lines.isEmpty()) {
            throw new SourceFileException(description, "No %s pins have been defined", inputType.name());
        }

        return lines;
    }
}
