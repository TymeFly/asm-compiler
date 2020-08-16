package com.github.tymefly.eeprom.builder.project;

import javax.annotation.Nonnull;

import com.github.tymefly.eeprom.builder.exception.SourceFileException;
import com.github.tymefly.eeprom.builder.io.source.SourceLine;

/**
 * Base class used by all classes that parse source files
 */
public abstract class Source {
    /**
     * Split the text in a source line into a number of columns where each column is separated by one of more
     * whitespace characters. If the line has the wrong number of columns an exception will be thrown
     * @param line          Line to be split
     * @param minColumns    The minimum number of expected columns
     * @param maxColumns    The maximum number of expected columns
     * @return              An array of the columns in the text
     * @throws SourceFileException if the wrong number of columns was found in the source text
     */
    @Nonnull
    protected String[] splitLine(@Nonnull SourceLine line, int minColumns, int maxColumns) throws SourceFileException {
        String[] columns = line.getText()
                .split("\\h+");
        int count = columns.length;

        if (count < minColumns) {
            throw new SourceFileException(line, "Missing columns");
        }

        if (count > maxColumns) {
            throw new SourceFileException(line, "Unexpected columns");
        }

        return columns;
    }


    /**
     * Returns a parsed pin number, or -1 of the description is invalid
     * @param description       A pin number expressed as a string
     * @return a parsed pin number, or -1 of the description is invalid
     */
    protected int parsePin(@Nonnull String description) {
        int value;

        try {
            value = Integer.parseInt(description);
        } catch (NumberFormatException e) {                     // Calling method will handle the error
            value = -1;
        }

        return value;
    }
}
