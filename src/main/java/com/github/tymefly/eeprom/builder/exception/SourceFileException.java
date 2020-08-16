package com.github.tymefly.eeprom.builder.exception;

import javax.annotation.Nonnull;

import com.github.tymefly.eeprom.builder.io.source.SourceFile;
import com.github.tymefly.eeprom.builder.io.source.SourceLine;

/**
 * Unchecked exception that is thrown when the system notices a problem with one of the source files.
 */
public class SourceFileException extends EepromBuilderException {
    private static final long serialVersionUID = 0x01;


    /**
     * Create a new exception
     * @param file      SourceFile that is invalid
     * @param message   Error message
     * @param cause     Wrapped exception
     * @see  java.util.Formatter
     */
    public SourceFileException(@Nonnull SourceFile file, @Nonnull String message, @Nonnull Throwable cause) {
        super("[" + file.getFile().getFileName() + "] ERROR: " + message, cause);
    }


    /**
     * Create a new exception
     * @param file      SourceFile that is invalid
     * @param message   Error message template
     * @param args      Optional arguments that are applied to the template
     * @see  java.util.Formatter
     */
    public SourceFileException(@Nonnull SourceFile file, @Nonnull String message, Object... args) {
        super("[" + file.getFile().getFileName() + "] ERROR: " + String.format(message, args));
    }


    /**
     * Create a new exception
     * @param line      SourceLine that is invalid
     * @param message   Error message
     * @param cause     Wrapped exception
     * @see  java.util.Formatter
     */
    public SourceFileException(@Nonnull SourceLine line, @Nonnull String message, @Nonnull Throwable cause) {
        super("[" + line.getFile().getFileName() + ":" + line.getLineNumber() + "] ERROR: " + message, cause);
    }


    /**
     * Create a new exception
     * @param line      SourceLine that is invalid
     * @param message   Error message template
     * @param args      Optional arguments that are applied to the template
     * @see  java.util.Formatter
     */
    public SourceFileException(@Nonnull SourceLine line, @Nonnull String message, Object... args) {
       super("[" + line.getFile().getFileName() + ":" + line.getLineNumber() + "] ERROR: " +
                String.format(message, args));
    }
}
