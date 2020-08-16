package com.github.tymefly.eeprom.builder.io.target.eeprom.map;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collection;

import javax.annotation.Nonnull;

import com.github.tymefly.eeprom.builder.exception.IoException;
import com.github.tymefly.eeprom.builder.io.target.eeprom.ContentWriter;
import com.github.tymefly.eeprom.builder.io.target.eeprom.EpromData;
import com.github.tymefly.eeprom.builder.project.global.GlobalData;
import com.github.tymefly.eeprom.builder.project.input.InputPins;
import com.github.tymefly.eeprom.builder.project.output.OutputPins;
import com.github.tymefly.eeprom.builder.project.output.Rom;
import com.github.tymefly.eeprom.builder.utils.StringUtils;

/**
 * Write the content of the EEPROM as a map file
 */
public class MapWriter implements ContentWriter {
    private static final int BITS_IN_BYTE = 8;
    private static final int ROM_DATA_WIDTH = 11;
    private static final int OPCODE_WIDTH = 15;
    private static final int ADDRESS_HEX_WIDTH = 6;
    private static final String FILE_NAME = "Eeprom_map.txt";
    private static final int SECTION_SIZE = 48;

    private final File destination;
    private final FlagFormatter flagFormatter;
    private final int addressBits;
    private final Writer writer;
    private final String header;

    private int lineCount;                  // Count up
    private int headerCount;                // Count down


    /**
     * Constructor
     * @param targetDirectory   Directory to write map file in
     * @param globalData        Access to the application configuration
     * @param inputPins         A description of all the address pins
     * @param outputPins        A description of all the data pins which may be in multiple ROMs
     */
    public MapWriter(@Nonnull File targetDirectory,
                     @Nonnull GlobalData globalData,
                     @Nonnull InputPins inputPins,
                     @Nonnull OutputPins outputPins) {
        destination = new File(targetDirectory, FILE_NAME);
        flagFormatter = new FlagFormatter(inputPins);
        addressBits = globalData.getMaxAddressBit();

        Collection<Rom> roms = outputPins.getRoms();

        try {
            writer = new BufferedWriter(new FileWriter(destination, StandardCharsets.UTF_8));
            header = generateHeader(roms);
            lineCount = 0;
            headerCount = 0;
        } catch (IOException e) {
            throw new IoException("Failed to generate Map file " + destination.getAbsolutePath(), e);
        }
    }


    @Nonnull
    private String generateHeader(@Nonnull Collection<Rom> roms) {
        int flagWidth = flagFormatter.width() + 2;
        int addressBinaryWidth = addressBits +
                ((addressBits + (StringUtils.BINARY_SPACING - 1)) / StringUtils.BINARY_SPACING);
        StringBuilder buffer = new StringBuilder();

        StringUtils.append(buffer, "OpCode", OPCODE_WIDTH);
        StringUtils.append(buffer, "Flags", flagWidth);
        buffer.append("M    ");
        StringUtils.append(buffer, "Address", addressBinaryWidth + ADDRESS_HEX_WIDTH);
        buffer.append("   ");
        roms.forEach(r -> StringUtils.append(buffer, r.name(), ROM_DATA_WIDTH).append("      "));
        buffer.append("Note");

        return buffer.toString();
    }


    @Nonnull
    @Override
    public String getDescription() {
        return FILE_NAME;
    }

    @Override
    public void initialise() {
    }


    @Override
    public void apply(@Nonnull EpromData data) {
        StringBuilder dataBytes = new StringBuilder();

        for (var dataValue : data.getData().values()) {
            dataBytes.append(StringUtils.asBinary(dataValue, BITS_IN_BYTE))
                     .append(" (")
                     .append(String.format("%02x", dataValue))
                     .append(")   ");
        }

        if (headerCount-- == 0) {
            if (lineCount != 0) {
                report("");
            }

            report(header);
            headerCount = SECTION_SIZE - 1;
        }

        lineCount++;

        report("%s (%02x) %s %2d    %s (%04x)   %s%s",
                StringUtils.asBinary(data.getOpCode(), BITS_IN_BYTE),
                data.getOpCode(),
                flagFormatter.format(data.getActiveFlags()),
                data.getMCycle(),
                StringUtils.asBinary(data.getAddress(), addressBits),
                data.getAddress(),
                dataBytes,
                data.getAnnotation());
    }


    @Override
    public void close() {
        LocalDateTime timeStamp = LocalDateTime.now();

        report("");
        report("");
        report("Generated at %tF %tR", timeStamp, timeStamp);

        try {
            writer.close();
        } catch (IOException e) {
            throw new IoException("Failed to write report " + destination, e);
        }
    }


    /**
     * Write a single line to the report
     * @param message       The format of the line of text
     * @param args          Details of the text
     * @throws IoException  if the report could not be written
     */
    private void report(@Nonnull String message, Object... args) throws IoException {
        try {
            String line = String.format(message, args);

            writer.write(line);
            writer.write(System.lineSeparator());
        } catch (IOException e) {
            throw new IoException("Failed to write report " + destination, e);
        }
    }
}
