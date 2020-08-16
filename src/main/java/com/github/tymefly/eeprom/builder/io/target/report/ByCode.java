package com.github.tymefly.eeprom.builder.io.target.report;

import java.io.File;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;

import javax.annotation.Nonnull;

import com.github.tymefly.eeprom.builder.utils.StringUtils;
import com.github.tymefly.eeprom.builder.utils.SystemLimits;

/**
 * Generate a report that shows how OpCodes map on to which instructions.
 * This can be used to map from binary to assembly
 */
class ByCode extends ReportFile {
    private final Map<Integer, Collection<String>> byCode;

    ByCode(@Nonnull File targetDirectory) {
        super(targetDirectory, "OpCodes.txt");

        this.byCode = new TreeMap<>();
    }


    @Override
    public void unusedCode(int code) {
    }

    @Override
    public void usedCode(int code, @Nonnull Collection<String> names) {
        byCode.put(code, names);
    }


    @Override
    public void report(@Nonnull Writer writer) {
        report(writer, "Hex\t\tDec\t\t Binary\t\tInstruction(s)");

        byCode.forEach((c, n) -> {
            StringJoiner joiner = new StringJoiner(", ");
            n.forEach(joiner::add);

            report(writer,
                   "0x%02x\t%03d\t\t%s\t%s",
                   c, c, StringUtils.asBinary(c, SystemLimits.BITS_IN_BYTE), joiner.toString());
        });

        report(writer, "");
        report(writer, "%d OpCodes defined", byCode.size());
    }
}
