package com.github.tymefly.eeprom.builder.io.source.group;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

/**
 * A Group name validator can can be used to check instructions names are valid.
 * Since the instruction names are not fixed the requirements for a valid instruction are:
 * <ul>
 *  <li>The name is unique</li>
 *  <li>The name starts with an uppercase letter</li>
 *  <li>The name contains uppercase letters, lower case letters, spaces, dashes,
 *          underscores slashes and square brackets </li>
 * </ul>
 * In addition to this, all members of the {@link InstructionGroup} are considered valid.
 */
public class InstructionValidator implements GroupValidator {
    static final Pattern INSTRUCTION_NAME = Pattern.compile("[A-Z][A-Za-z0-9_/ \\()#,\\-\\[\\]]*");

    private Set<String> groups = new LinkedHashSet<>();


    @Override
    public boolean isValid(@Nonnull String name) {
        boolean valid = INSTRUCTION_NAME.matcher(name).matches();

        for(var standard : InstructionGroup.values()) {
            if (valid) {
                break;
            } else {
                valid = standard.getGroup().equals(name);
            }
        }

        if (valid) {
            valid = !groups.contains(name);

            if (valid) {
                groups.add(name);
            }
        }

        return (valid);
    }
}
