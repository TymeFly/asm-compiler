package com.github.tymefly.eeprom.builder.utils;

import javax.annotation.Nonnull;

/**
 * String utility functions
 */
public class StringUtils {
    /** Maximum number of bits written by {@link #asBinary(int, int)} before a space is added */
    public static final int BINARY_SPACING = 4;

    private StringUtils() {
    }


    /**
     * Return {@code value} expressed as a formatted binary string that is {@code size} digits long.
     * @param value     Value to convert
     * @param size      Number of binary digits to return
     * @return {@code value} expressed as a formatted binary string that is {@code size} digits long.
     */
    @Nonnull
    public static String asBinary(int value, int size) {
        var spaces = (size - 1) / BINARY_SPACING;
        var buffer = new char[size + spaces];

        int space = 0;
        int index = buffer.length -1;
        while (size-- != 0) {
            buffer[index--] = ((value & 1) == 1) ? '1' : '0';
            value >>= 1;

            if ((size != 0) && (++space == BINARY_SPACING)) {
                buffer[index--] = ' ';
                space = 0;
            }
        }

        return new String(buffer);
    }


    /**
     * Append some {@code text} to the String {@code builder}, ensuring that it uses at least {@code width} characters.
     * If required, additional spaces will be added after the {@code text}
     * @param builder   StringBuilder to mutate
     * @param text      Text to add to buffer
     * @param width     minimum number of character to add
     * @return          A fluent interface
     */
    @Nonnull
    public static StringBuilder append(@Nonnull StringBuilder builder, @Nonnull String text, int width) {
        int count = width - text.length();

        builder.append(text);
        while (count-- > 0) {
            builder.append(' ');
        }

        return builder;
    }
}
