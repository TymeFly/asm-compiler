package com.github.tymefly.eeprom.builder.project.code;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.github.tymefly.eeprom.builder.project.input.PinState;

/**
 * A class used to translate the binary (see {@link PinState}) definition of an OpCode into a
 * unique collection of opCodes. Multiple OpCode are returned if there are {@link PinState#EITHER} fields on
 * bits that are used in the Instruction Register
 */
@Immutable
class OpCodes {
    @Immutable
    static class Code {
        static final Pattern FORMAT = Pattern.compile("[10x]{0,4} *[10x]{1,4}");

        private final String definition;
        private final Collection<Integer> codes;

        private Code(@Nonnull String definition, @Nonnull Collection<Integer> codes) {
            this.definition = definition;
            this.codes = Collections.unmodifiableCollection(codes);
        }


        @Nonnull
        Collection<Integer> getCodes() {
            return codes;
        }


        @Override
        public String toString() {
            return "Code{definition='" + definition + '\'' + ", codes=" + codes + '}';
        }
    }


    private final int irMask;


    OpCodes(int irMask) {
        this.irMask = irMask;
    }


    /**
     * Build a new OpCode. The numeric value of the {@code definition} may map onto several different
     * values depending upon the 'don't care' bit values
     * @param definition        8 binary values, including a 'x' as 'don't care' with an optional
     *                          space between the nibbles
     * @return The generated OpCode or {@literal null} if the code isn't value
     */
    @Nullable
    Code build(@Nonnull String definition) {
        Collection<Integer> codes = buildCodes(new HashSet<>(), definition.toLowerCase().trim());
        Code retValue = (codes.isEmpty() ? null : new Code(definition, codes));

        return retValue;
    }


    @Nonnull
    private Collection<Integer> buildCodes(@Nonnull Collection<Integer> codes, @Nonnull String definition) {
        int index = definition.indexOf('x');

        if (index == -1) {
            int code = Integer.parseInt(definition, 2);

            code &= irMask;

            if (!codes.contains(code)) {
                codes.add(code);
            }
        } else {
            StringBuilder builder = new StringBuilder(definition);

            builder.setCharAt(index, '0');
            buildCodes(codes, builder.toString());

            builder.setCharAt(index, '1');
            buildCodes(codes, builder.toString());
        }

        return codes;
    }
}
