package com.github.tymefly.eeprom.builder.project.input;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.github.tymefly.eeprom.builder.utils.Preconditions;

import static java.util.stream.Collectors.toMap;


/**
 * Enumeration of all possible Instruction Register Bits. While it is required that at least one bit is configured
 * it is not required that the bits have to be assigned in any particular project. There is also no requirement
 * that the assigned bits are contagious
 */
public enum IrBit {
    IR_0(0),
    IR_1(1),
    IR_2(2),
    IR_3(3),
    IR_4(4),
    IR_5(5),
    IR_6(6),
    IR_7(7);

    /** Name of the register - all the previous constants represent single lines in this register */
    public static final String NAME = "IR";

    private static final Map<Integer, IrBit> FROM_BIT =
            Stream.of(values())
                  .collect(toMap(b -> b.bit, Function.identity()));

    private final int bit;
    private final int bitValue;

    
    IrBit(int bit) {
        this.bit = bit;
        this.bitValue = 1 << bit;
    }


    /**
     * Returns the value associated with this bit.
     * bit 0 ({@link #IR_0}} will return 1, bit 1 ({@link #IR_1}} will return 2 etc.
     * @return the value associated with this bit.
     */
    public int getBitValue() {
        return bitValue;
    }


    /**
     * Mapping from the bit number to the enumeration constant
     * @param bit       bit to lookup
     * @return          The constant associated with the {@code bit}
     */
    @Nonnull
    public static IrBit fromBit(int bit) {
        IrBit irRegister = FROM_BIT.get(bit);

        Preconditions.checkArgument((irRegister != null), "Invalid bit %d", bit);

        return irRegister;
    }
}
