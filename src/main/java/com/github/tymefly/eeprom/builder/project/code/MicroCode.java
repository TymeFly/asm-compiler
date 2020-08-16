package com.github.tymefly.eeprom.builder.project.code;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nonnull;

import com.github.tymefly.eeprom.builder.exception.InternalException;
import com.github.tymefly.eeprom.builder.io.target.eeprom.Content;
import com.github.tymefly.eeprom.builder.io.target.report.Report;
import com.github.tymefly.eeprom.builder.project.input.InputPin;
import com.github.tymefly.eeprom.builder.project.input.InputPins;
import com.github.tymefly.eeprom.builder.project.input.InputTypes;
import com.github.tymefly.eeprom.builder.project.input.PinState;
import com.github.tymefly.eeprom.builder.project.output.OutputPin;
import com.github.tymefly.eeprom.builder.project.output.OutputPins;
import com.github.tymefly.eeprom.builder.project.output.Rom;
import com.github.tymefly.eeprom.builder.utils.Preconditions;
import com.github.tymefly.eeprom.builder.utils.StringUtils;
import com.github.tymefly.eeprom.builder.utils.SystemLimits;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A model which describe the microcode for the compiled assembly language.
 */
public class MicroCode {
    private static final Logger LOGGER = LogManager.getFormatterLogger();

    /**
     * Instructions must be defined in the following order:
     * <ul>
     *     <li>{@link #startFetch()} - initial steps of every instruction - the load a new instruction</li>
     *     <li>{@link #startFinal()} - final steps of every instruction</li>
     *     <li>{@link #startUnused()} - instruction to use if nothing else is defined</li>
     *     <li>{@link #startInstruction(String)} - general instructions</li>
     * </ul>
     */
    static class Builder {
        private final InputPins inputPins;
        private final OutputPins outputPins;
        private final Map<String, Instruction> instructions;
        private final Collection<InputPin> flags;
        private final Set<OutputPin> unusedOutputs;
        private final int maxMCycle;

        private Instruction current = null;
        private Instruction fetchStep = null;
        private Instruction finalStep = null;
        private Instruction unusedCode = null;
        private Instruction nop = null;
        private Set<OutputPin> halt;


        /**
         * Constructor
         * @param inputPins     A description of all the address pins
         * @param outputPins    A description of all the data pins which may be in multiple ROMs
         */
        Builder(@Nonnull InputPins inputPins, @Nonnull OutputPins outputPins) {
            this.inputPins = inputPins;
            this.outputPins = outputPins;
            this.instructions = new LinkedHashMap<>();
            this.maxMCycle = inputPins.getMaxMCycle();
            this.flags = inputPins.getPins(InputTypes.FLAG);
            this.unusedOutputs = new TreeSet<>(outputPins.getPins());
        }


        /**
         * Start generating code for the Fetch phase
         * @see #completeInstruction
         */
        void startFetch() {
            Preconditions.checkState((fetchStep == null), "Fetch step has already been defined");

            start(maxMCycle);
            fetchStep = current;

            LOGGER.debug("Name: <common-fetch>");
        }

        /**
         * Start generating code for the final phase - terminating the instruction
         * @see #completeInstruction
         */
        void startFinal() {
            Preconditions.checkState((finalStep == null), "Final step has already been defined");
            Preconditions.checkState((fetchStep != null), "Fetch step has not been set");

            start(maxMCycle);
            finalStep = current;

            LOGGER.debug("Name: <common-final>");
        }


        /**
         * Start generating code for undefined opCodes
         * @see #completeInstruction
         */
        void startUnused() {
            Preconditions.checkState((unusedCode == null), "Unused step has already been defined");
            Preconditions.checkState((fetchStep != null), "Fetch step has not been set");
            Preconditions.checkState((finalStep != null), "Final step has not been set");

            int maxCycles = fetchStep.getMCycles().size();

            start(maxCycles);
            addFetchCycles();
            unusedCode = current;
            unusedCode.setName("<unused-code>");
        }


        /**
         * Start generating code for named instructions
         * @see #completeInstruction
         */
        void startInstruction(@Nonnull String name) {
            Preconditions.checkState((fetchStep != null), "Fetch step has not been set");
            Preconditions.checkState((finalStep != null), "Final step has not been set");
            Preconditions.checkState((unusedCode != null), "Undefined step has not been set");

            start(maxMCycle);
            current.setName(name);
            addFetchCycles();
        }


        /**
         * Set the opCode(s) for this instruction
         * @param instruction   OpCode for this instruction
         * @return              {@literal true} only if the OpCode(s) are valid
         */
        boolean setInstruction(@Nonnull OpCodes.Code instruction) {
            Preconditions.checkState((current != null), "Instruction has not been started");

            return current.setInstruction(instruction);
        }


        /**
         * Set the output pins for the next MCycle
         * @param outputPins    The output pins that must be asserted for the next MCycle
         * @return              {@literal true} only if the Cycle was set.
         *                      {@literal false} may indicate that  too many machine cycles has been defined
         */
        boolean setMCycle(@Nonnull Set<OutputPin> outputPins) {
            Preconditions.checkState((current != null), "Instruction has not been started");

            unusedOutputs.removeAll(outputPins);

            return current.setMCycle(outputPins);
        }


        /**
         * Sets the conditions that must be true for this instruction to do anything;
         * if they are false then the code will move on to the next instruction
         * @param condition     Condition that must be true to execute the instruction
         * @return              {@literal true} only if the flag was set.
         */
        boolean setFlag(@Nonnull Condition condition) {
            Preconditions.checkState((current != null), "Instruction has not been started");

            return current.setFlag(condition);
        }


        /**
         * Complete a the current instruction. A new instruction must be started before
         * Flags any MCycles can be set.
         * @see #startFetch
         * @see #startFinal
         * @see #startUnused
         * @see #startInstruction
         */
        void completeInstruction() {
            Preconditions.checkState((current != null), "Instruction has not been started");

            boolean generalInstruction = current.isGeneralInstruction();

            if (generalInstruction) {
                boolean done = addFinalCycles();

                Preconditions.checkState(done, "Too many 'final' cycles for %s",current.getName());

                for (var flag : flags) {
                    if (!current.hasFlag(flag)) {
                        setFlag(Condition.of(flag, PinState.EITHER));
                    }
                }
            }

            current.validate();

            if (generalInstruction) {
                instructions.put(current.getName(), current);
            }

            current = null;
        }


        // Common code to start a new group
        private void start(int maxMCycle) {
            if (current != null) {
                completeInstruction();
            }

            current = new Instruction(maxMCycle);
        }


        private void addFetchCycles() {
            for (var init: fetchStep.getMCycles()) {
                current.setMCycle(init);
            }
        }


        private boolean addFinalCycles() {
            boolean valid;
            List<Set<OutputPin>> finalCycles = finalStep.getMCycles();

            if (finalCycles.isEmpty()) {
                valid = true;
            } else {
                Set<OutputPin> cycle = finalCycles.get(0);

                valid = current.appendMCycle(cycle);

                for (int index = 1; index < finalCycles.size(); index++) {
                    cycle = finalCycles.get(index);
                    valid = valid && current.setMCycle(cycle);
                }
            }

            return valid;
        }


        /**
         * Build the MicroCode for all the instructions that have been configured in this builder
         * @return a new Microcode interpreter.
         */
        @Nonnull
        MicroCode build() {
            buildNop();
            buildHalt();

            return new MicroCode(this);
        }


        // Synthesise a default "no nothing" instruction
        private void buildNop() {
            start(maxMCycle);
            nop = current;
            nop.setName("<no-op>");
            addFetchCycles();
            completeInstruction();
        }


        // Synthesise a halt cycle
        private void buildHalt() {
            List<Set<OutputPin>> undefinedCycles = unusedCode.getMCycles();
            int difference = undefinedCycles.size() - fetchStep.getMCycles().size();

            if (difference == 1) {
                this.halt = undefinedCycles.get(undefinedCycles.size() - 1);
            } else if (difference == 0) {
                this.halt = Collections.emptySet();
            } else {
                throw new InternalException("Unexpected 'undefined' instruction count");
            }
        }
    }


    private final int maxCycle;
    private final Set<OutputPin> halt;                      // The only non-fetch cycle in the "undefined" instruction
    private final InputPins inputPins;
    private final OutputPins outputPins;
    private final AddressMapper addressMapper;
    private final DataMapper dataMapper;
    private final CodeMap codeMap;
    private EepromMap eepromMap;


    private MicroCode(@Nonnull Builder builder) {
        this.maxCycle = builder.maxMCycle;
        this.halt = builder.halt;
        this.inputPins = builder.inputPins;
        this.outputPins = builder.outputPins;
        this.addressMapper = new AddressMapper(builder.inputPins);
        this.dataMapper = new DataMapper(builder.outputPins);
        this.codeMap = new CodeMap.Builder()
                .withInputPins(builder.inputPins)
                .withInstructions(builder.instructions)
                .withNoOperation(builder.nop)
                .withUndefinedOp(builder.unusedCode)
                .build();

        builder.unusedOutputs.forEach(
            o -> LOGGER.warn("Output Pin %s (Rom %s, pin %d) is not used", o.getName(), o.getRom(), o.getPin()));
    }


    /**
     * Generate a text report describing the Microcode
     * @param reporter      A text reporter visitor.
     */
    public void report(@Nonnull Report reporter) {
        int max = codeMap.maxOpCode();

        for (var code = 0; code <= max; code++) {
            if (codeMap.isDefined(code)) {
                reporter.usedCode(code, codeMap.getNames(code));
            } else if (codeMap.isUnused(code)) {
                reporter.unusedCode(code);
            } else {
                // Do nothing - this is an unmapped opCode
            }
        }
    }


    /**
     * Generate the content for this microcode
     * @param writer       A content writer visitor.
     */
    public void generate(@Nonnull Content writer) {
        Collection<Rom> roms = outputPins.getRoms();
        Collection<Set<Condition>> allConditions = codeMap.getAllConditions();
        EepromMap eepromMap = getEepromMap();

        for (var code : codeMap.allValid()) {
            for (var state : allConditions) {
                for (int mCycle = 0; mCycle <= maxCycle; mCycle++) {
                    int address = addressMapper.calculate(state, code, mCycle);
                    String annotation = eepromMap.readAnnotation(address);
                    Content.Entry entry = writer.entry()
                            .forOpCode(code)
                            .forMCycle(mCycle);

                    for (var flag : state) {
                        entry = entry.withFlag(flag.getPin(), flag.getState());
                    }

                    for (var rom : roms) {
                        entry = entry.toData(rom, eepromMap.readByte(rom, address));
                    }

                    entry.forAddress(address)
                         .withAnnotation(annotation)
                         .apply();
                }
            }
        }
    }


    /**
     * Returns an object that describes the content of each EEPROM
     * @return an object that describes the content of each EEPROM
     */
    @Nonnull
    private EepromMap getEepromMap() {
        if (eepromMap == null) {
            EepromMap.Builder builder = new EepromMap.Builder();

            for (var rom : outputPins.getRoms()) {
                builder = buildEeprom(builder, rom);
            }

            eepromMap = builder.build();
        }

        return eepromMap;
    }


    @Nonnull
    private EepromMap.Builder buildEeprom(@Nonnull EepromMap.Builder builder, @Nonnull Rom rom) {
        EepromMap.Eeprom eeprom = builder.eeprom(rom);
        int maxCode = codeMap.maxOpCode();
        int addressPinCount = SystemLimits.getMaxAddressBit() + 1;
        int romSize = 1 << (addressPinCount);
        Collection<Set<Condition>> allConditions = codeMap.getAllConditions();
        int inactive = dataMapper.calculate(rom, halt);
        int irMask = inputPins.getIrMask();

        eeprom = eeprom.setMaxAddress(romSize)
                       .setDefault(inactive);

        for (var code : codeMap.allValid()) {
            String description = getDescription(irMask, code);

            LOGGER.debug("Rom %s: Generate OpCode 0x%02x (%03d) => %s", rom, code, code, description);

            for (var state : allConditions) {
                Instruction instruction = codeMap.getInstruction(code, state);
                List<Set<OutputPin>> cycles = instruction.getMCycles();

                for (int mCycle = 0; mCycle < cycles.size(); mCycle++) {
                    Set<OutputPin> cycle = cycles.get(mCycle);
                    int data = dataMapper.calculate(rom, cycle);
                    int address = addressMapper.calculate(state, code, mCycle);

                    LOGGER.trace("Address: 0x%04x   %05d   %s => 0x%02x",
                            address,
                            address,
                            StringUtils.asBinary(address, addressPinCount),
                            data);

                    eeprom = eeprom.set(address, data, code, mCycle, description);
                }
            }
        }

        return eeprom.apply();
    }


    @Nonnull
    private String getDescription(int irMask, int code) {
        String description;
        int effectiveCode = code & irMask;

        if (codeMap.isUnused(effectiveCode)) {
            description = "<unused>";
        } else {
            description = String.join(", ", codeMap.getNames(effectiveCode));
        }

        return description;
    }
}
