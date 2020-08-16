package com.github.tymefly.eeprom.builder.project.input;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.github.tymefly.eeprom.builder.utils.Preconditions;

import static java.util.stream.Collectors.toMap;

/**
 * An enumeration of all the bits in the Machine Cycle counter.
 * Not all of the bits have to be used, but there can't be any gaps in the bits assigned
 */
public enum MCycle {
    M0(0),
    M1(1),
    M2(2),
    M3(3),
    M4(4),
    M5(5),
    M6(6),
    M7(7);

    private static final Map<Integer, MCycle> FROM_BIT =
            Stream.of(values())
                    .collect(toMap(b -> b.bit, Function.identity()));

    private final int bit;
    private final int value;
    private final int maxCycle;


    MCycle(int bit) {
        this.bit = bit;
        this.value = (1 << bit);
        this.maxCycle = (1 << (bit + 1)) - 1;
    }


    int getValue() {
        return value;
    }


    /**
     * If this is the highest defined counter bit, then this method return the max value of the counter
     * @return the highest value the counter can take using this as highest bit
     */
    int getMaxCycle() {
        return maxCycle;
    }


    /**
     * Look up from the counter bit number to the enumeration constant
     * @param bit       bit to look up
     * @return          The associated enumeration constant
     */
    @Nonnull
    public static MCycle fromBit(int bit) {
        MCycle mCycle = FROM_BIT.get(bit);

        Preconditions.checkArgument((mCycle != null), "Invalid bit %d", bit);

        return mCycle;
    }
}
