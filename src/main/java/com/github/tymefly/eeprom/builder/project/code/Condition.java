package com.github.tymefly.eeprom.builder.project.code;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.github.tymefly.eeprom.builder.project.input.InputPin;
import com.github.tymefly.eeprom.builder.project.input.PinState;


/**
 * Defines a possible condition
 */
@Immutable
class Condition {
    private static final Map<InputPin, Map<PinState, Condition>> CACHE = new HashMap<>();

    private final InputPin pin;
    private final PinState state;

    private Condition(@Nonnull InputPin pin, @Nonnull PinState state) {
        this.pin = pin;
        this.state = state;
    }


    @Nonnull
    static Condition of(@Nonnull InputPin pin, @Nonnull PinState state) {
        Map<PinState, Condition> conditions = CACHE.computeIfAbsent(pin, x -> new EnumMap<>(PinState.class));
        Condition condition = conditions.computeIfAbsent(state, x -> new Condition(pin, state));

        return condition;
    }


    @Nonnull
    InputPin getPin() {
        return pin;
    }


    @Nonnull
    PinState getState() {
        return state;
    }


    @Override
    public boolean equals(Object o) {
        boolean equal;

        if (this == o) {
            equal = true;
        } else if (o == null ) {
            equal = false;
        } else if (getClass() != o.getClass()) {
            equal = false;
        } else {
            Condition other = (Condition) o;

            equal = (pin == other.pin) &&
                    (state == other.state);
        }

        return equal;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pin, state);
    }

    @Override
    public String toString() {
        return "Condition{pin=" + pin.getName() + ", state=" + state + '}';
    }
}
