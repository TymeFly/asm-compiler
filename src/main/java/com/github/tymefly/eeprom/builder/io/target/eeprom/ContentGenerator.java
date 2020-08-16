package com.github.tymefly.eeprom.builder.io.target.eeprom;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nonnull;

import com.github.tymefly.eeprom.builder.io.target.eeprom.map.MapWriter;
import com.github.tymefly.eeprom.builder.io.target.eeprom.srec.SRecWriter;
import com.github.tymefly.eeprom.builder.project.global.GlobalData;
import com.github.tymefly.eeprom.builder.project.input.InputPin;
import com.github.tymefly.eeprom.builder.project.input.InputPins;
import com.github.tymefly.eeprom.builder.project.input.PinState;
import com.github.tymefly.eeprom.builder.project.output.OutputPins;
import com.github.tymefly.eeprom.builder.project.output.Rom;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Implementation of a {@link Content}
 */
public class ContentGenerator implements Content {
    private static class OutputEntry implements Entry, EpromData {
        private final ContentGenerator parent;
        private int opCode = -1;
        private int mCycle = -1;
        private int address = -1;
        private EnumMap<Rom, Byte> data = new EnumMap<>(Rom.class);
        private Set<InputPin> activeFlags = new TreeSet<>();
        private String annotation = "";


        OutputEntry(@Nonnull ContentGenerator parent) {
            this.parent = parent;
        }


        @Nonnull
        @Override
        public Entry forOpCode(int opCode) {
            this.opCode = opCode;

            return this;
        }

        @Nonnull
        @Override
        public Entry forMCycle(int mCycle) {
            this.mCycle = mCycle;

            return this;
        }

        @Nonnull
        @Override
        public Entry withFlag(@Nonnull InputPin pin, @Nonnull PinState state) {
            if (state == PinState.ACTIVE) {
                activeFlags.add(pin);
            }

            return this;
        }

        @Nonnull
        @Override
        public Entry forAddress(int address) {
            this.address = address;

            return this;
        }

        @Nonnull
        @Override
        public Entry toData(@Nonnull Rom rom, byte value) {
            this.data.put(rom, value);

            return this;
        }


        @Nonnull
        @Override
        public Entry withAnnotation(@Nonnull String annotation) {
            this.annotation = annotation;

            return this;
        }


        @Override
        public void apply() {
            parent.apply(this);
        }


        @Override
        public int getOpCode() {
            return opCode;
        }

        @Override
        public int getMCycle() {
            return mCycle;
        }

        @Override
        public int getAddress() {
            return address;
        }

        @Override
        @Nonnull
        public Map<Rom, Byte> getData() {
            return Collections.unmodifiableMap(data);
        }

        @Override
        @Nonnull
        public Set<InputPin> getActiveFlags() {
            return Collections.unmodifiableSet(activeFlags);
        }

        @Override
        @Nonnull
        public String getAnnotation() {
            return annotation;
        }
    }


    private static final Logger LOGGER = LogManager.getFormatterLogger();
    private final Collection<ContentWriter> targets;


    /**
     * Constructor
     * @param targetDirectory   Directory to write map file in
     * @param globalData        Access to the application configuration
     * @param inputPins         A description of all the address pins
     * @param outputPins        A description of all the data pins which may be in multiple ROMs
     */
    public ContentGenerator(@Nonnull File targetDirectory,
                            @Nonnull GlobalData globalData,
                            @Nonnull InputPins inputPins,
                            @Nonnull OutputPins outputPins) {
        targets = new ArrayList<>();

        targets.add(new MapWriter(targetDirectory, globalData, inputPins, outputPins));

        for (var rom : outputPins.getRoms()) {
            targets.add(new SRecWriter(targetDirectory, globalData, rom));
        }

        targets.forEach(ContentWriter::initialise);
    }


    @Nonnull
    @Override
    public Entry entry() {
        return new OutputEntry(this);
    }


    private void apply(@Nonnull OutputEntry entry) {
        targets.forEach(t -> t.apply(entry));
    }


    @Override
    public void close() {

        for (ContentWriter target : targets) {
            String description = target.getDescription();
            String pad = "-".repeat(description.length());

            LOGGER.info("/------%s---------\\", pad);
            LOGGER.info("|  Generating %s  |", description);
            LOGGER.info("\\------%s---------/", pad);
            target.close();
        }
    }
}
