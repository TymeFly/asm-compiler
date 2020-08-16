package com.github.tymefly.eeprom.builder.io.source;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import com.github.tymefly.eeprom.builder.utils.Preconditions;

/**
 * A class that describes the content of one of the project source files.
 */
public class SourceFile {
    private final File projectDirectory;
    private final ProjectFile file;
    private final SourceLine header;
    private final Map<String, SourceGroup> groups;


    SourceFile(@Nonnull File projectDirectory, @Nonnull ProjectFile file, @Nonnull SourceLine header) {
        this.projectDirectory = projectDirectory;
        this.file = file;
        this.header = header;
        this.groups = new LinkedHashMap<>();
    }


    void add(@Nonnull SourceGroup group) {
        String groupName = group.getName().toUpperCase();

        Preconditions.checkState(!groups.containsKey(groupName), "group %s has already been defined", group.getName());

        groups.put(groupName, group);
        group.setFile(this);
    }


    /**
     * Returns the parent ProjectFile
     * @return the parent ProjectFile
     */
    @Nonnull
    public ProjectFile getFile() {
        return file;
    }


    /**
     * Returns a standard group in this SourceFile. If there is no group called {@code name} then an
     * empty group will be returned
     * @param name  name of the required group
     * @param <E>   Type of the predefined group
     * @return      the named group
     */
    @Nonnull
    public <E extends Enum<E>> SourceGroup getGroup(@Nonnull E name) {
        return getGroup(name.name());
    }


    /**
     * Returns a single named group in this SourceFile. If there is no group called {@code name} then an
     * empty group will be returned
     * @param name  name of the required group
     * @return      the named group
     */
    @Nonnull
    public SourceGroup getGroup(@Nonnull String name) {
        SourceGroup group = groups.get(name.toUpperCase());

        if (group == null) {                           // Don't add to groups - it would effect groups() and getGroups()
            group = new SourceGroup(name, header);
        }

        return group;
    }


    /**
     * Returns all of the groups in this SourceFile
     * @return all of the groups in this SourceFile
     */
    @Nonnull
    public Collection<SourceGroup> getGroups() {
        return groups.values();
    }


    @Override
    public String toString() {
        return "SourceFile{" +
                "projectDirectory=" + projectDirectory +
                ", file=" + file +
                ", groups Count=" + groups.size() +
                '}';
    }
}
