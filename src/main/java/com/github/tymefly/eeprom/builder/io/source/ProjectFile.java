package com.github.tymefly.eeprom.builder.io.source;

import javax.annotation.Nonnull;

import com.github.tymefly.eeprom.builder.io.source.group.EnumGroupValidator;
import com.github.tymefly.eeprom.builder.io.source.group.GroupValidator;
import com.github.tymefly.eeprom.builder.io.source.group.InstructionValidator;
import com.github.tymefly.eeprom.builder.project.global.Project;
import com.github.tymefly.eeprom.builder.project.input.InputTypes;
import com.github.tymefly.eeprom.builder.project.output.Rom;


/**
 * Enumeration of all the source files that exist in a valid project
 */
public enum ProjectFile {
    PROJECT("project.txt", new EnumGroupValidator<>(Project.class)),
    INPUT_DEFINITION("input_pins.txt", new EnumGroupValidator<>(InputTypes.class)),
    CODE("code.txt", new InstructionValidator()),
    OUTPUT_DEFINITION("output_pins.txt", new EnumGroupValidator<>(Rom.class));

    private final String fileName;
    private final GroupValidator groupValidator;


    ProjectFile(@Nonnull String fileName, @Nonnull GroupValidator groupValidator) {
        this.fileName = fileName;
        this.groupValidator = groupValidator;
    }


    /**
     * Returns the source file name
     * @return the source file name
     */
    @Nonnull
    public String getFileName() {
        return fileName;
    }


    /**
     * Returns a class used to validate group names in this source file
     * @return a class used to validate group names in this source file
     */
    @Nonnull
    public GroupValidator getGroupValidator() {
        return groupValidator;
    }
}
