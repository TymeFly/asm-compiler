package com.github.tymefly.eeprom.builder.project.global;

import java.io.File;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.github.tymefly.eeprom.builder.exception.SourceFileException;
import com.github.tymefly.eeprom.builder.io.source.FileLoader;
import com.github.tymefly.eeprom.builder.io.source.ProjectFile;
import com.github.tymefly.eeprom.builder.io.source.SourceFile;
import com.github.tymefly.eeprom.builder.io.source.SourceGroup;
import com.github.tymefly.eeprom.builder.io.source.SourceLine;
import com.github.tymefly.eeprom.builder.project.Source;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A factory class used to generate a model of the project wide settings
 */
public class GlobalDataFactory extends Source {
    private static final Logger LOGGER = LogManager.getLogger();

    private GlobalDataFactory() {
    }


    /**
     * Parse the main project description file
     * @param projectDirectory  The directory that contains the project source code
     * @return a model that describes the global settings
     */
    @Nonnull
    public static GlobalData parse(@Nonnull File projectDirectory) {
        GlobalDataFactory me = new GlobalDataFactory();
        SourceFile description = new FileLoader(projectDirectory, ProjectFile.PROJECT).read();

        return me.parse(description);
    }


    private GlobalData parse(@Nonnull SourceFile description) {
        GlobalData globalData = new GlobalData();

        parseSection(globalData, description, Project.OVERVIEW, Overview.class);
        parseSection(globalData, description, Project.HARDWARE, Hardware.class);
        parseDescriptions(globalData, description);

        LOGGER.info("Building Application {} version {}", globalData.getName(), globalData.getVersion());

        return globalData;
    }


    private <T extends Enum<T> & Setting> void parseSection(@Nonnull GlobalData globalData,
                              @Nonnull SourceFile description,
                              @Nonnull Project section,
                              @Nonnull Class<T> settings) {
        SourceGroup group = description.getGroup(section);
        Map<String, String> config = group.getLines()
             .stream()
             .map(SourceLine::getText)
             .map(l -> l.replaceAll("\\s*=\\s*", "="))
             .map(l -> l.split("=", 2))
             .collect(Collectors.toMap(s -> s[0].toUpperCase(), s -> s[1]));

        for (var setting : settings.getEnumConstants()) {
            String key = setting.key();
            String value = config.get(key.toUpperCase());

            if (value == null) {
                throw new SourceFileException(group.getHeader(), "Setting '%s' in Overview was not defined", key);
            }

            try {
                setting.setter().accept(globalData, value);
            } catch (RuntimeException e) {
                throw new SourceFileException(group.getHeader(),
                                         "Setting '" + key + "' has invalid value '" + value + "'",
                                         e);
            }
        }
    }


    private void parseDescriptions(@Nonnull GlobalData globalData, @Nonnull SourceFile description) {
        SourceGroup group = description.getGroup(Project.DESCRIPTION);

        for (var line : group.getLines()) {
            globalData.addDescription(line.getText());
        }
    }
}
