package com.github.tymefly.eeprom.builder.io.target.report;

import java.io.File;
import java.io.Writer;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nonnull;

import com.github.tymefly.eeprom.builder.utils.StringUtils;
import com.github.tymefly.eeprom.builder.utils.SystemLimits;

/**
 * Generate a report that lists all the unused opcodes.
 * This can be used to work out where we can add new instructions
 */
class Available extends ReportFile {
    private final Set<Integer> unused;

    Available(@Nonnull File targetDirectory) {
        super(targetDirectory, "AvailableCodes.txt");

        this.unused = new TreeSet<>();
    }


    @Override
    public void unusedCode(int code) {
        unused.add(code);
    }


    @Override
    public void usedCode(int code, @Nonnull Collection<String> names) {
    }


    @Override
    public void report(@Nonnull Writer writer) {
        report(writer, "Hex\t\tDec\t\t Binary");
        unused.forEach(code -> report(writer,
                                      "0x%02x\t%03d\t\t%s",
                                      code, code, StringUtils.asBinary(code, SystemLimits.BITS_IN_BYTE)));

        report(writer, "");
        report(writer, "%d OpCodes available", unused.size());
    }
}
