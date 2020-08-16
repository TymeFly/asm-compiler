package com.github.tymefly.eeprom.builder.project.global;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import com.github.tymefly.eeprom.builder.utils.Preconditions;
import com.github.tymefly.eeprom.builder.utils.SystemLimits;

/**
 * A model for the project wide settings
 */
public class GlobalData {
    /** Max address pin on a 8Kb EEPROM (lowest address pin is A0) */
    public static final int MAX_ADDRESS_BIT = 13;

    private List<String> descriptions;
    private String name;
    private String version;
    private int maxAddressBit;

    GlobalData() {
        descriptions = new ArrayList<>();
    }


    void setName(@Nonnull String name) {
        this.name = name;
    }


    void setVersion(@Nonnull String version) {
        this.version = version;
    }


    void setMaxAddressBit(@Nonnull String maxAddressBit) {
        this.maxAddressBit = Integer.parseInt(maxAddressBit);

        boolean valid = ((this.maxAddressBit > 0) && (this.maxAddressBit <= MAX_ADDRESS_BIT));

        Preconditions.checkArgument(valid, "maxAddressBit is out of range");

        SystemLimits.setMaxAddressBit(this.maxAddressBit);
    }


    void addDescription(@Nonnull String header) {
        descriptions.add(header);
    }


    /**
     * Returns the application name
     * @return the application name
     */
    @Nonnull
    public String getName() {
        return name;
    }


    /**
     * Returns the application version number
     * @return the application version number
     */
    @Nonnull
    public String getVersion() {
        return version;
    }


    /**
     * Returns the maximum Address Bits in the EEPROM. The address bits are Address bits are
     * numbered {@literal A0} -> {@literal Amax}, so the number of address lines is one greater
     * then {@code maxAddressBit}.
     * Not all input have to be configured, although unused address bits should be tied low
     * @return the maximum number of Address Bits in the EEPROM.
     */
    public int getMaxAddressBit() {
        return maxAddressBit;
    }

    /**
     * Returns all the lines of text in the project description
     * @return all the lines of text in the project description
     */
    @Nonnull
    public List<String> getDescriptions() {
        return Collections.unmodifiableList(descriptions);
    }
}
