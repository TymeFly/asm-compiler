package com.github.tymefly.eeprom.builder.io.target.eeprom.srec;

import java.io.File;
import java.time.LocalDateTime;

import javax.annotation.Nonnull;

import com.github.tymefly.eeprom.builder.io.target.eeprom.ContentWriter;
import com.github.tymefly.eeprom.builder.io.target.eeprom.EpromData;
import com.github.tymefly.eeprom.builder.project.global.GlobalData;
import com.github.tymefly.eeprom.builder.project.output.Rom;
import com.github.tymefly.srec.SWriter;


/**
 * A RomWrite class that generates S-Record files
 */
public class SRecWriter implements ContentWriter {
    private static final LocalDateTime TIME_STAMP = LocalDateTime.now();
    private static final int SREC_LENGTH = 16;

    private final Rom rom;
    private final GlobalData globalData;
    private final SWriter writer;
    private final byte[] buffer;


    /**
     * Create a new class to write the data for a single EEPROM
     * @param targetDirectory       Directory to write data to
     * @param globalData            Access to the global data configuration
     * @param rom                   The ROM that needs to be generated
     */
    public SRecWriter(@Nonnull File targetDirectory, @Nonnull GlobalData globalData, @Nonnull Rom rom) {
        File destination = new File(targetDirectory, rom.fileName("srec"));

        this.rom = rom;
        this.globalData = globalData;
        this.writer = new SWriter(destination);
        this.buffer = new byte[2 << globalData.getMaxAddressBit()];
    }


    @Nonnull
    @Override
    public String getDescription() {
        return rom.toString();
    }

    @Override
    public void initialise() {
        writer.withHeader("Name: " + globalData.getName());
        writer.withHeader("Version: " + globalData.getVersion());
        writer.withHeader("ROM: " + rom.getRomNumber());
        writer.withHeader(timeStamp());

        for (var header : globalData.getDescriptions()) {
            writer.withHeader(header);
        }
    }


    @Override
    public void apply(@Nonnull EpromData data) {
        int address = data.getAddress();

        buffer[address] = data.getData().get(rom);
    }


    @Override
    public void close() {
        int address = 0;

        while (address < buffer.length) {
            int size = Math.min(buffer.length - address, SREC_LENGTH);

            writer.withData(address, buffer, address, size);
            address += size;
        }


        writer.close();
    }


    @Nonnull
    private String timeStamp() {
        return String.format("Generated at %tF %tR", TIME_STAMP, TIME_STAMP);
    }


    @Override
    public String toString() {
        return "SRecWriter{rom=" + rom + '}';
    }
}
