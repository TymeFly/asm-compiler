package com.github.tymefly.eeprom.builder.project.code;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.tymefly.eeprom.builder.project.input.InputPin;
import com.github.tymefly.eeprom.builder.project.output.OutputPin;
import com.github.tymefly.eeprom.builder.utils.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * The definition of a single instruction in the Micro Code. Each instructions contains multiple Machine Cycles
 */
class Instruction {
    private static final Logger LOGGER = LogManager.getLogger();

    private final List<Set<OutputPin>> cycles;          // The inner set must be immutable as it's exported
    private final Map<InputPin, Condition> flags;
    private final int maxMCycle;

    private String name;
    private OpCodes.Code opCodes;


    Instruction(int maxMCycle) {
        LOGGER.debug("---===| Start Instruction |===---");

        this.maxMCycle = maxMCycle;

        this.cycles = new ArrayList<>();
        this.opCodes = null;
        this.flags = new HashMap<>();
    }


    void setName(@Nonnull String name) {
        LOGGER.debug("Name: {}", name);

        this.name = name;
    }


    boolean setInstruction(@Nonnull OpCodes.Code instruction) {
        LOGGER.debug("Set Instruction: {}", instruction);

        boolean valid = (this.opCodes == null);

        if (valid) {
            this.opCodes = instruction;
        }

        return valid;
    }


    boolean setFlag(@Nonnull Condition condition) {
        LOGGER.debug("Flag: {}", condition);

        boolean valid;

        InputPin flag = condition.getPin();
        Condition old = flags.put(flag, condition);
        valid = (old == null);

        return valid;
    }


    boolean setMCycle(@Nonnull Set<OutputPin> outputPins) {
        LOGGER.debug("Index: {} => {}", cycles.size(), outputPins);

        boolean valid = (cycles.size() <= maxMCycle);

        if (valid) {
            outputPins = Collections.unmodifiableSet(new TreeSet<>(outputPins));
            cycles.add(outputPins);
        }

        return valid;
    }


    boolean appendMCycle(@Nonnull Set<OutputPin> outputPins) {
        boolean valid;

        if (cycles.isEmpty()) {
            valid = setMCycle(outputPins);
        } else {
            int index = cycles.size() - 1;

            LOGGER.debug("Index: {}+ => {} ", index, outputPins);

            Set<OutputPin> last = cycles.get(index);

            last = new TreeSet<>(last);
            last.addAll(outputPins);
            last = Collections.unmodifiableSet(last);

            cycles.set(index, last);

            valid = true;
        }

        return valid;
    }


    void validate() {
        if (isGeneralInstruction()) {
            Preconditions.checkState((opCodes != null), "%s does not have any instructions", name);
            Preconditions.checkState(!cycles.isEmpty(), "%s does not have any cycles", name);
        }
    }


    boolean isGeneralInstruction() {
        return ((name != null) && !name.startsWith("<"));    // Special instructions are in the form <description>
    }


    /**
     * Returns the instruction name or {@literal null} if the instruction has no name
     * @return the instruction name or {@literal null} if the instruction has no name
     */
    @Nullable
    String getName() {
        return name;
    }


    @Nonnull
    OpCodes.Code getOpCodes() {
        return opCodes;
    }

    @Nonnull
    List<Set<OutputPin>> getMCycles() {
        return Collections.unmodifiableList(cycles);
    }


    boolean hasFlag(@Nonnull InputPin flag) {
        return flags.containsKey(flag);
    }


    @Nonnull
    Map<InputPin, Condition> getFlags() {
        return Collections.unmodifiableMap(flags);
    }



    @Override
    public String toString() {
        return "Instruction{" +
                "name='" + (name == null ? "<special>" : name) + '\'' +
                ", instruction=" + opCodes +
                ", cycles count=" + cycles.size() +
                ", flag count=" + flags.size() +
                '}';
    }
}
