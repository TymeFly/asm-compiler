package com.github.tymefly.eeprom.builder.io.source.group;

import javax.annotation.Nonnull;

import com.github.tymefly.eeprom.builder.utils.EnumUtils;

/**
 * A Group name validator for reconfigured sets of group names
 * @param <E>       Type of the enumeration class validated by this class
 */
public class EnumGroupValidator<E extends Enum<E>> implements GroupValidator {
    private final Class<E> type;


    /**
     * Constructor
     * @param type      The enumeration type that is checked by this validator
     */
    public EnumGroupValidator(@Nonnull Class<E> type) {
        this.type = type;
    }


    @Override
    public boolean isValid(@Nonnull String name) {
        E constant = EnumUtils.getEnum(name, type);

        return (constant != null);
    }
}
