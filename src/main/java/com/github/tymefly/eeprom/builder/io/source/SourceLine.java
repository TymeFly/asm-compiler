package com.github.tymefly.eeprom.builder.io.source;

import javax.annotation.Nonnull;

import com.github.tymefly.eeprom.builder.utils.Preconditions;

/**
 * A class that encapsulates a single line of test in a {@link SourceFile}
 */
public class SourceLine {
    /**
     * Builder for SourceLine instances.
     * <br>
     * Line numbers for SourceLines are allocated sequentially, so the calling class needs to take care not to
     * skip any input, even when it doesn't care about the line.
     * <br>
     * There is also a special case 'header' SourceLine which can be used to represent line {@literal 0} of the file.
     * The calling code can use this if a required line is missing.
     */
    static class Builder {
        private final ProjectFile file;
        private SourceLine header;
        private int lineCount = 0;


        Builder(@Nonnull ProjectFile file) {
            this.file = file;
            this.header = new SourceLine("", file, 0);
        }


        @Nonnull
        SourceLine header() {
            return header;
        }


        @Nonnull
        SourceLine build(@Nonnull String line) {
            return new SourceLine(line, file, ++lineCount);
        }
    }


    private final String text;
    private ProjectFile file;
    private final int lineNumber;
    private SourceGroup group;


    private SourceLine(@Nonnull String text, @Nonnull ProjectFile file, int lineNumber) {
        this.text = text;
        this.file = file;
        this.lineNumber = lineNumber;
    }


    boolean hasText() {
        return !text.isEmpty();
    }


    void setGroup(@Nonnull SourceGroup group) {
        Preconditions.checkState((this.group == null), "Group has already been assigned to %s", this);

        this.group = group;
    }


    /**
     * Returns a cleaned up version of the literal text from the source file. This involves removing
     * comments and trailing (not not leading) spaces and squashing multiple white space into a single space.
     * @return the text for a single line of the source file.
     */
    @Nonnull
    public String getText() {
        return text;
    }


    /**
     * Returns the line number within the source file
     * @return the line number within the source file
     */
    public int getLineNumber() {
        return lineNumber;
    }


    /**
     * Returns the parent source file
     * @return the parent source file
     */
    @Nonnull
    public ProjectFile getFile() {
        return file;
    }


    @Override
    public String toString() {
        return "SourceLine{" +
            "file=" + file.getFileName() +
            ", lineNumber=" + lineNumber +
            ", group=" + (group == null ? "<unset>" : group.getName()) +
            ", text='" + text + '\'' + '}';
    }
}
