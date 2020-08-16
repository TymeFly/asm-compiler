package com.github.tymefly.eeprom.builder.exception;

import javax.annotation.Nonnull;


/**
 * Base class of all exceptions in this package
 */
public abstract class EepromBuilderException extends RuntimeException {
    /**
     * Constructor for a raw message
     * @param message       Human readable (raw) message
     */
    protected EepromBuilderException(@Nonnull String message) {
        super(message);
    }


    /**
     * Constructor for a formatted message
     * @param message       formatted message string
     * @param args          formatting arguments
     * @see java.util.Formatter
     */
    protected EepromBuilderException(@Nonnull String message, @Nonnull Object... args) {
        super(String.format(message, args));
    }


    /**
     * Constructor for a wrapped exception
     * @param message       Human readable (raw) message
     * @param cause         Wrapped exception
     */
    protected EepromBuilderException(@Nonnull String message, @Nonnull Throwable cause) {
        super(message, cause);
    }
}

