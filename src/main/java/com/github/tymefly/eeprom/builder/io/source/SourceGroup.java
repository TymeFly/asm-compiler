package com.github.tymefly.eeprom.builder.io.source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import com.github.tymefly.eeprom.builder.utils.Preconditions;

/**
 * A class that is used to describe and contain all the {@link SourceLine}s in a single group of the SourceFile
 */
public class SourceGroup {
    private final String groupName;
    private final SourceLine header;
    private final List<SourceLine> lines;
    private SourceFile parent;


    SourceGroup(@Nonnull String groupName, @Nonnull SourceLine header) {
        this.groupName = groupName;
        this.header = header;
        this.lines = new ArrayList<>();
    }


    void add(@Nonnull SourceLine line) {
        Preconditions.checkArgument(line.hasText(), "Empty SourceLine added to group: %s", line);

        lines.add(line);
        line.setGroup(this);
    }


    void setFile(@Nonnull SourceFile parent) {
        this.parent = parent;
    }


    /**
     * Returns the unique name of this source group
     * @return the unique name of this source group
     */
    @Nonnull
    public String getName() {
        return groupName;
    }


    /**
     * Returns a special case 'header' SourceLine which can be used as a place holder when a more specific line
     * does not exist in the parent source file
     * @return a special case 'header' SourceLine.
     */
    @Nonnull
    public SourceLine getHeader() {
        return header;
    }


    /**
     * Returns all the SourceLines in this group. Empty lines will have been filtered out.
     * @return all the SourceLines in this group
     */
    @Nonnull
    public List<SourceLine> getLines() {
        return Collections.unmodifiableList(lines);
    }


    /**
     * Returns the parent {@link SourceFile} that contains this group
     * @return the parent {@link SourceFile} that contains this group
     */
    @Nonnull
    public SourceFile getSourceFile() {
        return parent;
    }


    @Override
    public String toString() {
        return "SourceGroup{file=" + parent + ", groupName=" + groupName + ", entries=" + lines.size() + '}';
    }
}
