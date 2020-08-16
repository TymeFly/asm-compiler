package com.github.tymefly.eeprom.builder.project.code;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.github.tymefly.eeprom.builder.project.input.InputPin;
import com.github.tymefly.eeprom.builder.project.input.InputPins;
import com.github.tymefly.eeprom.builder.project.input.InputTypes;
import com.github.tymefly.eeprom.builder.project.input.PinState;
import com.github.tymefly.eeprom.builder.utils.Preconditions;
import com.github.tymefly.eeprom.builder.utils.SetUtils;

/**
 * The CodeMap is used to defined the mapping from the {@link InputPins} to the {@link Instruction}
 * that will be executed.
 */
@Immutable
class CodeMap {
    static class Builder {
        private Map<String, Instruction> instructions;
        private Instruction nop;
        private Instruction undefined;
        private InputPins inputPins;


        @Nonnull
        Builder withInputPins(@Nonnull InputPins inputPins) {
            this.inputPins = inputPins;
            return this;
        }


        @Nonnull
        Builder withInstructions(@Nonnull Map<String, Instruction> instructions) {
            this.instructions = instructions;
            return this;
        }


        @Nonnull
        Builder withNoOperation(@Nonnull Instruction nop) {
            this.nop = nop;
            return this;
        }


        @Nonnull
        Builder withUndefinedOp(@Nonnull Instruction undefined) {
            this.undefined = undefined;
            return this;
        }


        @Nonnull
        CodeMap build() {
            Preconditions.checkState((inputPins != null), "inputPins have not been set");
            Preconditions.checkState((instructions != null), "instructions map has not been set");
            Preconditions.checkState((nop != null), "nop instruction has not been set");
            Preconditions.checkState((undefined != null), "undefined instruction has not been set");

            List<Set<Condition>> allConditions = allConditions();
            List<Map<Set<Condition>, Instruction>> codeMap = new ArrayList<>();
            Map<Set<Condition>, Instruction> unused = createUnusedCode(allConditions);

            clearCodeMap(codeMap);
            populateCodeMap(codeMap, allConditions);
            fillCodeMap(codeMap, unused);

            return new CodeMap(codeMap, allConditions, unused);
        }


        private void clearCodeMap(@Nonnull List<Map<Set<Condition>, Instruction>> codeMap) {
            int irMask = inputPins.getIrMask();
            int max = (inputPins.getMaxIrPin().getBitValue() << 1) - 1;
            int index = -1;

            while (index++ != max) {
                int code = (index & irMask);                        // The code we will use.
                boolean isValid = (code == index);
                HashMap<Set<Condition>, Instruction> map = (isValid ? new HashMap<>(): null);

                codeMap.add(map);
            }
        }


        private void populateCodeMap(@Nonnull List<Map<Set<Condition>, Instruction>> codeMap,
                                     @Nonnull Collection<Set<Condition>> allConditions) {
            for (var instruction : instructions.values()) {
                Collection<Set<Condition>> keys = match(allConditions, instruction);

                for(var code : instruction.getOpCodes().getCodes()) {
                    Map<Set<Condition>, Instruction> entry = codeMap.get(code);

                    Preconditions.checkState((entry != null), "Attempt to process invalid opCode %d", code);

                    for (var condition : allConditions) {
                        Instruction current = (keys.contains(condition) ? instruction : nop);
                        Instruction existing = entry.get(condition);

                        if ((existing == null) || (existing == nop)) {
                            entry.put(condition, current);
                        } else if (current == nop) {
                            // Do nothing - don't overwrite a good instruction with nop
                        } else {
                            throw new CompilerException("OpCode %d clashed for instructions '%s' and '%s' in state %s",
                                                        code, existing.getName(), instruction.getName(), condition);
                        }
                    }
                }
            }
        }


        private void fillCodeMap(@Nonnull List<Map<Set<Condition>, Instruction>> codeMap,
                                 @Nonnull Map<Set<Condition>, Instruction> unused) {

            int index = codeMap.size();
            while (index-- != 0) {
                Map<Set<Condition>, Instruction> entry = codeMap.get(index);

                if ((entry != null) && entry.isEmpty()) {
                    codeMap.set(index, unused);
                }
            }
        }


        @Nonnull
        private Map<Set<Condition>, Instruction> createUnusedCode(@Nonnull Collection<Set<Condition>> allConditions) {
            Map<Set<Condition>, Instruction> keys = new HashMap<>();

            for (var condition : allConditions) {
                keys.put(condition, undefined);
            }

            keys = Collections.unmodifiableMap(keys);
            return keys;
        }


        @Nonnull
        private Collection<Set<Condition>> match(@Nonnull Collection<Set<Condition>> allConditions,
                                                 @Nonnull Instruction instruction) {
            Set<Set<Condition>> result = new HashSet<>();
            Collection<Condition> flags = instruction.getFlags().values();

            for (var test : allConditions) {
                Set<Condition> key = match(test, flags);

                if (key != null) {
                    result.add(key);
                }
            }

            return result;
        }


        @Nullable
        private Set<Condition> match(@Nonnull Set<Condition> test, @Nonnull Collection<Condition> flags) {
            boolean matched = true;

            for (var flag : flags) {
                InputPin pin = flag.getPin();
                var condition = Condition.of(pin, PinState.ACTIVE);
                boolean ifSet = (flag.getState() != PinState.INACTIVE);
                boolean ifClear = (flag.getState() != PinState.ACTIVE);
                boolean isSet = test.contains(condition);

                matched = ((isSet && ifSet) || (!isSet && ifClear));

                if (!matched) {
                    break;
                }
            }

            return (matched ? test : null);
        }


        /**
         * Returns a sorted, immutable, collection of all possible conditions
         * @return a sorted, immutable, collection of all possible conditions
         */
        @Nonnull
        private List<Set<Condition>> allConditions() {
            Set<Set<InputPin>> flagCombinations = SetUtils.powerSet(inputPins.getPins(InputTypes.FLAG));
            List<Set<Condition>> result = new ArrayList<>();

            for (var combination : flagCombinations) {
                result.add(Collections.unmodifiableSet(toCondition(combination)));
            }

            result.sort((l, r) -> {
                int left = l.stream()
                        .mapToInt(e -> (e.getState() == PinState.ACTIVE ? 2 << e.getPin().getPin() : 0))
                        .sum();
                int right = r.stream()
                        .mapToInt(e -> (e.getState() == PinState.ACTIVE ? 2 << e.getPin().getPin() : 0))
                        .sum();

                return left - right;
            });

            return Collections.unmodifiableList(result);
        }


        @Nonnull
        private Set<Condition> toCondition(@Nonnull Set<InputPin> test) {
            Set<Condition> result = new HashSet<>();

            for (var x : inputPins.getPins(InputTypes.FLAG)) {
                if (test.contains(x)) {
                    result.add(Condition.of(x, PinState.ACTIVE));
                } else {
                    result.add(Condition.of(x, PinState.INACTIVE));
                }
            }

            return result;
        }
    }


    /*
     * Code map is indexed by opCode. The values in the list are:
     *   - null => The opCode is invalid because the IR Register can't map it
     *   - 'unused' => The opCode doesn't have an assigned instruction (so we've made one up)
     *   - Map of all possible flag settings to the Instruction code that need to be executed
     */

    private final List<Map<Set<Condition>, Instruction>> codeMap;
    private final List<Set<Condition>> allConditions;
    private final Map<Set<Condition>, Instruction> unused;
    private final List<Integer> allValid;


    private CodeMap(@Nonnull List<Map<Set<Condition>, Instruction>> codeMap,
                    @Nonnull List<Set<Condition>> allConditions,
                    @Nonnull Map<Set<Condition>, Instruction> unused) {
        this.codeMap = Collections.unmodifiableList(codeMap);
        this.allConditions = allConditions;
        this.unused = unused;
        this.allValid = new ArrayList<>();
    }


    /**
     * Returns the largest valid OpCode
     * @return the largest valid OpCode
     */
    int maxOpCode() {
        return codeMap.size() - 1;
    }


    /**
     * Returns all possible valid set of Conditions that are supported.
     * These are sorted according to the binary values of their {@link InputPin} bit values
     * @return all possible valid set of Conditions that are supported
     */
    @Nonnull
    public List<Set<Condition>> getAllConditions() {
        return allConditions;
    }


    /**
     * Returns {@literal true} only if {@code opCode} is valid and one or more {@link Instruction}(s) have assigned
     * @param opCode        OpCode to test
     * @return {@literal true} only if {@code opCode} is valid and one or more {@link Instruction}(s) have assigned
     */
    boolean isDefined(int opCode) {
        boolean isDefined = (opCode <= maxOpCode());

        if (isDefined) {
            Map<Set<Condition>, Instruction> entry = codeMap.get(opCode);

            isDefined = ((entry != null) && (entry != unused));
        }

        return isDefined;
    }


    /**
     * Returns {@literal true} only if {@code opCode} is valid but no {@link Instruction}(s) have assigned
     * @param opCode        OpCode to test
     * @return {@literal true} only if {@code opCode} is valid but no {@link Instruction}(s) have assigned
     */
    boolean isUnused(int opCode) {
        boolean isUnused = (opCode <= maxOpCode());

        if (isUnused) {
            Map<Set<Condition>, Instruction> entry = codeMap.get(opCode);

            isUnused = ((entry != null) && (entry == unused));
        }

        return isUnused;
    }


    /**
     * Returns {@literal true} only if {@code opCode} represents a valid OpCode. This does not imply that
     * instruction(s) have been assigned to that code, merely that it's possible for the IR Register to hold this value
     * @param opCode        OpCode to test
     * @return {@literal true} only if {@code opCode} represents a valid OpCode.
     * @see #isUnused
     * @see #isDefined
     */
    boolean isValid(int opCode) {
        boolean isValid = (opCode <= maxOpCode());
        isValid = isValid && (codeMap.get(opCode) != null);

        return isValid;
    }


    /**
     * Returns a sorted immutable collection of all valid opCodes
     * @return a sorted immutable set of all valid opCodes
     * @see #isValid(int)
     */
    @Nonnull
    Collection<Integer> allValid() {
        if (allValid.isEmpty()) {
            int maxOpCode = maxOpCode();

            for (var opCode = 0; opCode <= maxOpCode; opCode++) {
                if (isValid(opCode)) {
                    allValid.add(opCode);
                }
            }
        }

        return Collections.unmodifiableList(allValid);
    }


    /**
     * Returns all the instruction names associated with the {@code opCode}
     * @param opCode        Valid OpCode
     * @return all the instruction names associated with the {@code opCode}. This is <i>usually</i> a singleton
     *          collection.
     * @see #isValid(int)
     */
    @Nonnull
    Collection<String> getNames(int opCode) {
        Collection<String> names;

        if (!isDefined(opCode)) {
            names = Collections.emptySet();
        } else {
            names = codeMap.get(opCode)
                   .values()
                   .stream()
                   .filter(Instruction::isGeneralInstruction)
                   .map(Instruction::getName)
                   .collect(Collectors.toSet());
        }

        return names;
    }



    /**
     * Returns the Instruction for a given {@code opCode} in a given {@code state}
     * @param opCode        Valid OpCode
     * @param state         One of the values returned by {@link #getAllConditions()}
     * @return              The Instruction to be executed
     * @see #isValid(int)
     * @see #getAllConditions()
     */
    @Nonnull
    Instruction getInstruction(int opCode, @Nonnull Set<Condition> state) {
        boolean isValid = (opCode <= maxOpCode());
        Map<Set<Condition>, Instruction> entry = (isValid ? codeMap.get(opCode) : null);

        Preconditions.checkArgument((entry != null), "Invalid opCode %d", opCode);

        return entry.get(state);
    }
}
