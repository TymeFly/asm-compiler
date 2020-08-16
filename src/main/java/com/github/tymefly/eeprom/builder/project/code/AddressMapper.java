package com.github.tymefly.eeprom.builder.project.code;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nonnull;

import com.github.tymefly.eeprom.builder.project.input.InputPin;
import com.github.tymefly.eeprom.builder.project.input.InputPins;
import com.github.tymefly.eeprom.builder.project.input.IrBit;
import com.github.tymefly.eeprom.builder.project.input.MCycle;
import com.github.tymefly.eeprom.builder.project.input.PinState;

/**
 * Map the {@link InputPins} to an address in the EEPROM
 */
class AddressMapper {
    private final InputPins inputPins;
    private final Map<Integer, Integer> codeCache;
    private final Map<Integer, Integer> cycleCache;
    private final Map<Set<Condition>, Integer> stateCache;


    AddressMapper(@Nonnull InputPins inputPins) {
        this.inputPins = inputPins;
        this.codeCache = new HashMap<>();
        this.cycleCache = new HashMap<>();
        this.stateCache = new HashMap<>();
    }


    int calculate(@Nonnull Set<Condition> state, int code, int mCycle) {
        int address = stateToAddress(state);
        address |= opCodeToAddress(code);
        address |= cycleToAddress(mCycle);

        return address;
    }


    private int stateToAddress(@Nonnull Set<Condition> state) {
        return stateCache.computeIfAbsent(state, k -> {
            int address = 0;

            for (var condition : state) {
                if (condition.getState() == PinState.ACTIVE) {
                    InputPin pin = condition.getPin();
                    address |= (1 << pin.getPin());
                }
            }

            return address;
        });
    }


    private int opCodeToAddress(int code) {
        return codeCache.computeIfAbsent(code, k -> toAddress(code, b -> IrBit.fromBit(b).name()));
    }


    private int cycleToAddress(int mCycle) {
        return cycleCache.computeIfAbsent(mCycle, k -> toAddress(mCycle, b -> MCycle.fromBit(b).name()));
    }


    private int toAddress(int value, @Nonnull Function<Integer, String> lookup) {
        int address = 0;
        int bit = 0;

        while (value != 0) {
            if ((value & 1) != 0) {
                String pinName = lookup.apply(bit);

                if (inputPins.hasPin(pinName)) {
                    InputPin pin = inputPins.getPin(pinName);

                    address |= (1 << pin.getPin());
                }
            }

            value >>= 1;
            bit++;
        }

        return address;
    }
}
