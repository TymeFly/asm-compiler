package com.github.tymefly.eeprom.builder.project.output;

import java.io.File;
import java.util.HashSet;
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
 * A factory class used to generate a model of the output (data) pins from a file in the project directory
 */
public class OutputPinsFactory extends Source {
    private OutputPinsFactory() {
    }


    /**
     * Parse the Output (data) Pin description file
     * @param projectDirectory  The directory that contains the project source code
     * @return a model that describes the output pins
     */
    @Nonnull
    public static OutputPins parse(@Nonnull File projectDirectory) {
        OutputPinsFactory me = new OutputPinsFactory();
        SourceFile description = new FileLoader(projectDirectory, ProjectFile.OUTPUT_DEFINITION).read();

        return me.parse(description);
    }


    @Nonnull
    private OutputPins parse(@Nonnull SourceFile description) {
        OutputPins outputPins = new OutputPins();

        for (var group : description.getGroups()) {
            Set<Integer> defined = new HashSet<>();

            for (var line : group.getLines()) {
                parseLine(outputPins, group, line, defined);
            }
        }

        return outputPins;
    }


    private void parseLine(@Nonnull OutputPins outputPins,
                           @Nonnull SourceGroup group,
                           @Nonnull SourceLine line,
                           @Nonnull Set<Integer> defined) {
        String[] columns = splitLine(line, 2, 2);
        String name = columns[0];
        Rom chip = EnumUtils.getEnum(group.getName(), Rom.class);
        int pin = parsePin(columns[1]);
        boolean activeLow = name.startsWith("/");

        if (activeLow) {
            name = name.substring(1);
        }

        if (chip == null) {
            throw new SourceFileException(line, "Chip name '%s' is invalid", group.getName());
        }

        if (!SystemLimits.isValidDataBit(pin)) {
            throw new SourceFileException(line, "Pin number %d is out of range", pin);
        }

        if (defined.contains(pin)) {
            throw new SourceFileException(line, "Pin %d has been redefined for chip %s", pin, chip);
        } else {
            defined.add(pin);
        }

        OutputPin dataPin = outputPins.define(name, chip, pin, activeLow);

        if (dataPin == null) {
            throw new SourceFileException(line, "Duplicate pin name '%s'", columns[0]);
        }
    }
}
