package com.github.tymefly.eeprom.builder.utils;

/**
 * Utility class that describes the limits of the system
 */
public class SystemLimits {
    /** Make used to convert data into a byte */
    public static final int BYTE_MASK = 0xff;

    /** The number of bits in a byte */
    public static final int BITS_IN_BYTE = 8;

    /** Max data pin on a Byte oriented EEPROM */
    public static final int MAX_DATA_BIT = (BITS_IN_BYTE - 1);

    private static int maxAddressBit = -1;


    private SystemLimits() {
    }


    /**
     * Set the Maximum address bit for the system. This must be done exactly once before calling
     * {@link #isValidAddressBit}. Address bits are numbered {@literal A0} -> {@literal Amax}, so the number of
     * address lines is one greater then {@code maxAddressBit}
     * @param maxAddressBit The maximum address bit.
     */
    public static void setMaxAddressBit(int maxAddressBit) {
        Preconditions.checkState(SystemLimits.maxAddressBit == -1, "MaxAddressBit has already been set");

        SystemLimits.maxAddressBit = maxAddressBit;
    }


    /**
     * Returns the maximum address bit of the EEPROM
     * @return the maximum address bit of the EEPROM
     * @see com.github.tymefly.eeprom.builder.project.global.GlobalData#getMaxAddressBit()
     */
    public static int getMaxAddressBit() {
        return maxAddressBit;
    }


    /**
     * Returns true only if {@code pin} is in the correct range to be a valid address pin.
     * The Max Address Bit must have been set
     * @param pin       Address bit
     * @return  true only if {@code pin} is in the correct range to be a valid address pin
     * @see #setMaxAddressBit(int)
     */
    public static boolean isValidAddressBit(int pin) {
        return ((pin >= 0) && (pin <= maxAddressBit));
    }


    /**
     * Returns true only if {@code pin} is in the correct range to be a valid data pin
     * @param pin       Data bit
     * @return  true only if {@code pin} is in the correct range to be a valid data pin
     */
    public static boolean isValidDataBit(int pin) {
        return ((pin >= 0) && (pin <= SystemLimits.MAX_DATA_BIT));
    }
}
