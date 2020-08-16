package com.github.tymefly.eeprom.builder.io.source;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.github.tymefly.eeprom.builder.exception.SourceFileException;

/**
 * A class that is used to load source files from disc and return them in a cleaned up format.
 */
public class FileLoader {
    private final File projectDirectory;
    private final ProjectFile file;
    private final Path location;


    /**
     * Create a new FileLoader
     * @param projectDirectory  directory that contains the source file
     * @param file              A description of the file that needs to be loaded
     */
    public FileLoader(@Nonnull File projectDirectory, @Nonnull ProjectFile file) {
        this.projectDirectory = projectDirectory;
        this.file = file;
        this.location = new File(projectDirectory, file.getFileName()).toPath();
    }


    /**
     * Read all of the data in the Project file and return them in a cleaned up state. This involves:
     * <ul>
     *  <li>Collecting the source lines together into uniquely named groups</li>
     *  <li>Removing comments</li>
     *  <li>Removing blank lines</li>
     *  <li>Removing trailing spaces (but not leading spaces)</li>
     *  <li>Replacing multiple whitespaces with a single space</li>
     * </ul>
     * @return The cleaned up content of the file
     * @throws SourceFileException   if the file could not be read
     */
    @Nonnull
    public SourceFile read() throws SourceFileException {
        SourceLine.Builder lineBuilder = new SourceLine.Builder(file);
        SourceLine header = lineBuilder.header();
        SourceFile sourceFile = new SourceFile(projectDirectory, file, header);

        try {
            List<SourceLine> lines = readLines(lineBuilder);

            group(sourceFile, lines);
        } catch (IOException e) {
            throw new SourceFileException(sourceFile, "Failed to load file", e);
        }

        return sourceFile;
    }


    @Nonnull
    private List<SourceLine> readLines(@Nonnull SourceLine.Builder lineBuilder) throws IOException {
        return Files.readAllLines(location, StandardCharsets.UTF_8)
             .stream()
             .map(this::removeComments)
             .map(s -> s.replaceAll("\\s+$", ""))           // Remove trailing (but not leading) spaces
             .map(s -> s.replaceAll("\\s+", " "))           // Replace multiple white spaces with a single space
             .map(lineBuilder::build)
             .filter(SourceLine::hasText)                   // Filter after build to keep the line index correct.
             .collect(Collectors.toList());
    }


    @Nonnull
    private String removeComments(@Nonnull String line) {
        int index = line.indexOf("//");

        return (index != -1 ? line.substring(0, index) : line);
    }


    private void group(@Nonnull SourceFile sourceFile, @Nonnull List<SourceLine> lines) {
        SourceGroup group = null;

        for (var line : lines) {
            String text = line.getText();

            if (text.startsWith("[")) {
                group = parseGroup(line);
                sourceFile.add(group);
            } else if (group != null) {
                group.add(line);
            } else {
                throw new SourceFileException(line, "Missing section name");
            }
        }
    }


    @Nonnull
    private SourceGroup parseGroup(@Nonnull SourceLine line) {
        boolean valid;
        String text = line.getText();
        int length = text.length();

        if (text.endsWith("]")) {
            text = text.substring(1, length - 1);
            valid = file.getGroupValidator().isValid(text);
        } else {
            valid = false;
        }

        if (!valid) {
            throw new SourceFileException(line, "Invalid section name '%s'", text);
        }

        return new SourceGroup(text, line);
    }
}
