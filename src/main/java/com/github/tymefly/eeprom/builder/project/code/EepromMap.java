package com.github.tymefly.eeprom.builder.project.code;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.github.tymefly.eeprom.builder.project.output.Rom;
import com.github.tymefly.eeprom.builder.utils.Preconditions;
import com.github.tymefly.eeprom.builder.utils.SystemLimits;

/**
 * A class that describes the content of each of the EEPROMs
 */
@Immutable
public class EepromMap {
    static class Eeprom {
        private final Rom rom;
        private final Builder parent;
        private final boolean buildAnnotations;

        private Byte inactive;
        private Byte[] content;
        private String[] annotations;


        private Eeprom(@Nonnull Rom rom, @Nonnull Builder parent, boolean buildAnnotations) {
            this.rom = rom;
            this.parent = parent;
            this.buildAnnotations = buildAnnotations;
        }


        @Nonnull
        Eeprom setMaxAddress(int romSize) {
            Preconditions.checkState((content == null), "Rom %s size has already been set", rom);

            content = new Byte[romSize];
            annotations = (buildAnnotations ? new String[romSize] : null);

            return this;
        }


        @Nonnull
        Eeprom setDefault(int inactive) {
            Preconditions.checkState((this.inactive == null), "Rom %s Inactive State has already been set", rom);

            this.inactive = (byte) (inactive & SystemLimits.BYTE_MASK);

            return this;
        }


        @Nonnull
        Eeprom set(int address, int data, int opCode, int cycle, @Nonnull String description) {
            Preconditions.checkState((content != null), "Rom %s size has not been set", rom);
            Preconditions.checkArgument((address < content.length), "Address 0x%04x is out of range", address);
            Preconditions.checkArgument((address >= 0), "Address 0x%04x is out of range", address);
            Preconditions.checkArgument((content[address] == null), "Address 0x%04x has already been defined", address);

            if (buildAnnotations) {
                String annotation =
                        String.format("opCode 0x%02x (%03d), cycle %02d%s%s",
                                      opCode, opCode, cycle, (description.isEmpty() ? "" : ": "), description);

                annotations[address] = annotation;
            }

            content[address] = (byte) (data & SystemLimits.BYTE_MASK);

            return this;
        }


        @Nonnull
        Builder apply() {
            int address = -1;

            while (++address < content.length) {
                if (content[address] == null) {
                    content[address] = inactive;
                }
            }

            Builder builder = parent.apply(rom, content, annotations);

            content = null;
            annotations = null;

            return builder;
        }
    }


    static class Builder {
        private final EnumMap<Rom, List<Byte>> eeproms = new EnumMap<>(Rom.class);
        private List<String> annotations = new ArrayList<>();

        private int eepromSize = -1;


        @Nonnull
        Eeprom eeprom(@Nonnull Rom rom) {
            return new Eeprom(rom, this, eeproms.isEmpty());
        }


        private Builder apply(@Nonnull Rom rom, @Nonnull Byte[] content, @Nullable String[] annotations) {
            Preconditions.checkState(!eeproms.containsKey(rom), "Rom %s in already defined", rom);
            Preconditions.checkState(((eepromSize == -1) || (eepromSize == content.length)),
                                     "Inconsistent ROM lengths");

            if (annotations != null) {
                this.annotations = Arrays.asList(annotations);
            }

            eeproms.put(rom, Arrays.asList(content));
            eepromSize = content.length;

            return this;
        }


        EepromMap build() {
            EepromMap eepromMap = new EepromMap(this);

            return eepromMap;
        }
    }


    private final List<String> annotations;
    private final EnumMap<Rom, List<Byte>> eeproms;
    private final int maxAddress;


    private EepromMap(@Nonnull Builder builder) {
        this.annotations = builder.annotations;
        this.eeproms = builder.eeproms;
        this.maxAddress = (builder.eepromSize - 1);
    }


    /**
     * Returns the highest valid address in the EEPROMs
     * @return the highest valid address in the EEPROMs
     */
    public int maxAddress() {
        return maxAddress;
    }


    /**
     * Returns the byte associated in {@code address} of the {@code rom}
     * @param rom           EEPROM to examine
     * @param address       Address in the EEPROM
     * @return the byte associated in {@code address} of the {@code rom}
     */
    public byte readByte(@Nonnull Rom rom, int address) {
        List<Byte> content = eeproms.get(rom);

        Preconditions.checkState((content != null), "Invalid Rom %s", rom);
        Preconditions.checkArgument((address < content.size()), "Address 0x%04x is out of range", address);
        Preconditions.checkArgument((address >= 0), "Address 0x%04x is out of range", address);

        return content.get(address);
    }


    /**
     * Returns the annotation associated with the {@code address}
     * @param address       Address in the EEPROM
     * @return the annotation associated with the {@code address}
     */
    @Nonnull
    public String readAnnotation(int address) {
        Preconditions.checkArgument((address < annotations.size()), "Address 0x%04x is out of range", address);
        Preconditions.checkArgument((address >= 0), "Address 0x%04x is out of range", address);

        String annotation = annotations.get(address);

        return (annotation == null ? "<invalid state>" : annotation);
    }
}
