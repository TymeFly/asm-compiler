package com.github.tymefly.eeprom.builder.project.code;

import javax.annotation.Nonnull;

import com.github.tymefly.eeprom.builder.exception.EepromBuilderException;


/**
 * Unchecked exception for internal errors that should not occur
 */
public class CompilerException extends EepromBuilderException {
    private static final long serialVersionUID = 0x01;


    /**
     * Constructor for a raw message
     * @param message       Human readable (raw) message
     */
    public CompilerException(@Nonnull String message) {
        super(message);
    }


    /**
     * Constructor for a formatted message
     * @param message       formatted message string
     * @param args          formatting arguments
     * @see java.util.Formatter
     */
    CompilerException(@Nonnull String message, @Nonnull Object... args) {
        super(String.format(message, args));
    }


    /**
     * Constructor for a wrapped exception
     * @param message       Human readable (raw) message
     * @param cause         Wrapped exception
     */
    public CompilerException(@Nonnull String message, @Nonnull Throwable cause) {
        super(message, cause);
    }
}

