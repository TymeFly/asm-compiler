package com.github.tymefly.eeprom.builder.io.source.group;

import javax.annotation.Nonnull;

/**
 * Standard group names for the "code" file. All member of this class have names that start with a symbol or
 * a lower case so that they don't match anything accepted by {@link InstructionValidator#INSTRUCTION_NAME}
 */
public enum InstructionGroup {
    FETCH("fetch", true),
    FINAL("final", false),
    UNUSED("*", false);

    private final String group;
    private final boolean required;


    InstructionGroup(@Nonnull String group, boolean required) {
        this.group = group;
        this.required = required;
    }


    /**
     * Returns the unique name of this InstructionGroup
     * @return the unique name of this InstructionGroup
     */
    @Nonnull
    public String getGroup() {
        return group;
    }


    /**
     * Returns {@literal true} only if this InstructionGroup is mandatory
     * @return {@literal true} only if this InstructionGroup is mandatory
     */
    public boolean isRequired() {
        return required;
    }
}
