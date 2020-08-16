package com.github.tymefly.eeprom.builder.exception;

import javax.annotation.Nonnull;


/**
 * Unchecked exception for IO issues
 */
public class IoException extends EepromBuilderException {
    private static final long serialVersionUID = 0x01;


    /**
     * Constructor for a raw message
     * @param message       Human readable (raw) message
     */
    public IoException(@Nonnull String message) {
        super(message);
    }


    /**
     * Constructor for a formatted message
     * @param message       formatted message string
     * @param args          formatting arguments
     * @see java.util.Formatter
     */
    IoException(@Nonnull String message, @Nonnull Object... args) {
        super(String.format(message, args));
    }


    /**
     * Constructor for a wrapped exception
     * @param message       Human readable (raw) message
     * @param cause         Wrapped exception
     */
    public IoException(@Nonnull String message, @Nonnull Throwable cause) {
        super(message, cause);
    }
}

