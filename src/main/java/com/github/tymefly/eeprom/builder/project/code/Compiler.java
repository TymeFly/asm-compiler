package com.github.tymefly.eeprom.builder.project.code;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.tymefly.eeprom.builder.exception.SourceFileException;
import com.github.tymefly.eeprom.builder.io.source.FileLoader;
import com.github.tymefly.eeprom.builder.io.source.ProjectFile;
import com.github.tymefly.eeprom.builder.io.source.SourceFile;
import com.github.tymefly.eeprom.builder.io.source.SourceGroup;
import com.github.tymefly.eeprom.builder.io.source.SourceLine;
import com.github.tymefly.eeprom.builder.io.source.group.InstructionGroup;
import com.github.tymefly.eeprom.builder.project.Source;
import com.github.tymefly.eeprom.builder.project.input.InputPin;
import com.github.tymefly.eeprom.builder.project.input.InputPins;
import com.github.tymefly.eeprom.builder.project.input.InputTypes;
import com.github.tymefly.eeprom.builder.project.input.IrBit;
import com.github.tymefly.eeprom.builder.project.input.PinState;
import com.github.tymefly.eeprom.builder.project.output.OutputPin;
import com.github.tymefly.eeprom.builder.project.output.OutputPins;
import com.github.tymefly.eeprom.builder.utils.Preconditions;

/**
 * Compile the code description into Microcode.
 */
public class Compiler extends Source {
    /**
     * A builder class for the Compiler
     */
    public static class Builder {
        private final File projectDirectory;
        private InputPins inputPins;
        private OutputPins outputPins;


        /**
         * Create a new Compiler builder object.
         * @param projectDirectory  The directory that contains the project source code
         */
        @Nonnull
        public Builder(@Nonnull File projectDirectory) {
            this.projectDirectory = projectDirectory;
        }


        /**
         * Add the input (address) and output (data) pins to the compiler
         * @param inputPins     A description of all the address pins
         * @param outputPins    A description of all the data pins which may be in multiple ROMs
         * @return              A fluent interface
         */
        @Nonnull
        public Builder withPins(@Nonnull InputPins inputPins, @Nonnull OutputPins outputPins) {
            Preconditions.checkState((this.outputPins == null), "Pins have already been set");

            this.inputPins = inputPins;
            this.outputPins = outputPins;

            return this;
        }


        /**
         * Returns a new instance of the Microcode compiler as configured by this builder
         * @return a new instance of the Microcode compiler
         */
        @Nonnull
        public Compiler compile() {
            var compiler = new Compiler(this);
            compiler.compile();

            return compiler;
        }
    }

    private static final Pattern SKIP_CYCLE = Pattern.compile("\\s*-\\s*");
    private final File projectDirectory;
    private final OutputPins outputPins;
    private final InputPins inputPins;
    private final OpCodes opCodes;
    private MicroCode result;


    private Compiler(@Nonnull Builder builder) {
        projectDirectory = Preconditions.checkNotNull(builder.projectDirectory, "Project location has not been set");
        inputPins = Preconditions.checkNotNull(builder.inputPins, "Input pins have not been set");
        outputPins = Preconditions.checkNotNull(builder.outputPins, "Output pins have not been set");

        opCodes = new OpCodes(inputPins.getIrMask());
    }


    private void compile() {
        MicroCode.Builder builder = new MicroCode.Builder(inputPins, outputPins);
        SourceFile description = new FileLoader(projectDirectory, ProjectFile.CODE).read();
        List<SourceGroup> groups = new LinkedList<>(description.getGroups());
        SourceGroup fetchGroup = extractGroup(description, groups, InstructionGroup.FETCH);
        SourceGroup finalGroup = extractGroup(description, groups, InstructionGroup.FINAL);
        SourceGroup unusedGroup = extractGroup(description, groups, InstructionGroup.UNUSED);

        parseGroup(builder, builder::startFetch, fetchGroup, true);
        parseGroup(builder, builder::startFinal, finalGroup, true);
        parseGroup(builder, builder::startUnused, unusedGroup, true);

        for (var instruction : groups) {
            parseGroup(builder, () -> builder.startInstruction(instruction.getName()), instruction, false);
        }

        result = builder.build();
    }


    @Nullable
    private SourceGroup extractGroup(@Nonnull SourceFile description,
                                     @Nonnull List<SourceGroup> groups,
                                     @Nonnull InstructionGroup extract) {
        SourceGroup extracted = null;

        for (var test : groups) {
            if (test.getName().equalsIgnoreCase(extract.getGroup())) {
                extracted = test;
                break;
            }
        }

        if ((extracted == null) && extract.isRequired()) {
            throw new SourceFileException(description, "Instruction group [%s] not found", extract.getGroup());
        }

        groups.remove(extracted);

        return extracted;
    }


    private void parseGroup(@Nonnull MicroCode.Builder builder,
                            @Nonnull Runnable start,
                            @Nullable SourceGroup definition,
                            boolean specialGroup) {
        start.run();

        if (definition != null) {
            for (var line : definition.getLines()) {
                String[] columns = splitLine(line, 1, Integer.MAX_VALUE);
                String input = columns[0];

                if (input.isEmpty()) {
                    parseMCycle(builder, line, columns);
                } else if (!specialGroup && IrBit.NAME.equals(input)) {
                    parseIr(builder, line, columns);
                } else if (!specialGroup && inputPins.hasPin(input)) {
                    parseFlag(builder, line, input, columns);
                } else {
                    throw new SourceFileException(line, "Unexpected input '%s'", input);
                }
            }
        }

        builder.completeInstruction();
    }


    private void parseIr(@Nonnull MicroCode.Builder builder, @Nonnull SourceLine line, String[] columns) {
        String value;

        if (columns.length == 2) {
            value = columns[1];
        } else if (columns.length == 3) {
            value = columns[1] + columns[2];
        } else {
            throw new SourceFileException(line, "Expected a single value for the Instruction Register");
        }

        if (!OpCodes.Code.FORMAT.matcher(value).matches()) {
            throw new SourceFileException(line,
                                     "Expected a binary value for the Instruction Register, but found '%s'",
                                     value);
        }

        OpCodes.Code instruction = opCodes.build(value);

        if (instruction == null) {
            throw new SourceFileException(line, "OpCode is invalid");
        }

        boolean done = builder.setInstruction(instruction);

        if (!done) {
            throw new SourceFileException(line, "Unexpected instruction");
        }
    }


    /**
     * Parse the description of a flag
     * @param builder       Builder that will be configured with the flag description
     * @param line          Line of source code
     * @param name          This is guaranteed to be a valid flag name
     * @param columns       Text from the line of source code
     */
    private void parseFlag(@Nonnull MicroCode.Builder builder,
                           @Nonnull SourceLine line,
                           @Nonnull String name,
                           String[] columns) {
        if (columns.length != 2) {
            throw new SourceFileException(line, "Expected a single value for Flag %s", name);
        }

        PinState state = PinState.fromText(columns[1]);

        if (state == null) {
            throw new SourceFileException(line, "Expected value for the Flag %s.", name);
        }

        InputPin pin = inputPins.getPin(InputTypes.FLAG, name);
        Condition condition = Condition.of(pin, state);
        boolean done = builder.setFlag(condition);

        if (!done) {
            throw new SourceFileException(line, "Unexpected flag %s", name);
        }
    }


    private void parseMCycle(@Nonnull MicroCode.Builder builder, @Nonnull SourceLine line, String[] columns) {
        Set<OutputPin> outputPins;

        if (SKIP_CYCLE.matcher(line.getText()).matches()) {
            outputPins = Collections.emptySet();
        } else {
            outputPins = parseOutputs(line, columns, 1);
        }

        boolean done = builder.setMCycle(outputPins);

        if (!done) {
            throw new SourceFileException(line, "Too many %s", InputTypes.M_CYCLE.name());
        }
    }



    @Nonnull
    private Set<OutputPin> parseOutputs(@Nonnull SourceLine line, @Nonnull String[] names, int first) {
        Set<OutputPin> outputs = new TreeSet<>();       // TreeSet ensures elements are logged consistently
        int index = names.length;

        while (index-- != first) {
            String name = names[index];
            OutputPin pin = outputPins.getPin(name);

            if (pin == null) {
                throw new SourceFileException(line, "Unknown output pin '%s'", name);
            }

            boolean undefined = outputs.add(pin);

            if (!undefined) {
                throw new SourceFileException(line, "Duplicate definition for output pin '%s'", name);
            }
        }

        return outputs;
    }


    /**
     * Returns a model for the compiled Microcode
     * @return a model for the compiled Microcode
     */
    @Nonnull
    public MicroCode getMicroCode() {
        return result;
    }
}
