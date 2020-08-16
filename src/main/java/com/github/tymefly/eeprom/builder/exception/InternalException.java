package com.github.tymefly.eeprom.builder.exception;

import javax.annotation.Nonnull;


/**
 * Unchecked exception for internal errors that should not occur
 */
public class InternalException extends EepromBuilderException {
    private static final long serialVersionUID = 0x01;


    /**
     * Constructor for a raw message
     * @param message       Human readable (raw) message
     */
    public InternalException(@Nonnull String message) {
        super("INTERNAL ERROR " + message);
    }


    /**
     * Constructor for a formatted message
     * @param message       formatted message string
     * @param args          formatting arguments
     * @see java.util.Formatter
     */
    public InternalException(@Nonnull String message, @Nonnull Object... args) {
        super("INTERNAL ERROR " + String.format(message, args));
    }


    /**
     * Constructor for a wrapped exception
     * @param message       Human readable (raw) message
     * @param cause         Wrapped exception
     */
    public InternalException(@Nonnull String message, @Nonnull Throwable cause) {
        super("INTERNAL ERROR " + message, cause);
    }
}

