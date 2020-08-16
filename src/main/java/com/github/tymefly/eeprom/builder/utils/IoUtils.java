package com.github.tymefly.eeprom.builder.utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.github.tymefly.eeprom.builder.exception.IoException;


/**
 * IO Utility functions
 */
public class IoUtils {
    private IoUtils() {
    }


    /**
     * Ensure that the {@code directory} exists and is empty
     * @param directory     Directory that is required to exist but be empty
     */
    public static void clearDirectory(@Nonnull File directory) {
        final boolean[] done = { true };

        if (directory.exists()) {
            try (Stream<Path> walk = Files.walk(directory.toPath())) {
                walk.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(f -> done[0] &= f.delete());
            } catch (Exception e) {
                throw new IoException("Failed to delete directory " + directory.getAbsolutePath(), e);
            }
        }

        done[0] &= directory.mkdirs();

        if (!done[0]) {
            throw new IoException("Failed to create target directory " + directory.getAbsolutePath());
        }
    }
}
