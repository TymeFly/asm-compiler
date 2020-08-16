package com.github.tymefly.eeprom.builder.io.target.report;

import java.io.File;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.annotation.Nonnull;

import com.github.tymefly.eeprom.builder.utils.StringUtils;
import com.github.tymefly.eeprom.builder.utils.SystemLimits;

/**
 * Generate a report that shows how instructions map on to OpCodes.
 * This can be used to hand assemble programs
 */
class ByInstruction extends ReportFile {
    private final Map<String, Collection<Integer>> byName;

    ByInstruction(@Nonnull File targetDirectory) {
        super(targetDirectory, "Instructions.txt");

        this.byName = new HashMap<>();
    }


    @Override
    public void unusedCode(int code) {
    }


    @Override
    public void usedCode(int code, @Nonnull Collection<String> names) {
        names.forEach(n -> byName.computeIfAbsent(n, k -> new TreeSet<>()).add(code));
    }


    @Override
    public void report(@Nonnull Writer writer) {
        Map<String, Collection<Integer>> cleaned = mergeDuplicates();

        report(writer, "Name\tHex\t\tDec\t\t Binary");

        cleaned.forEach((n, c) -> {
            report(writer, n);
            c.forEach(x -> report(writer,
                                 "\t\t0x%02x\t%03d\t\t%s",
                                 x, x, StringUtils.asBinary(x, SystemLimits.BITS_IN_BYTE)));
            report(writer, "");
        });

        report(writer, "");
        report(writer, "%d instructions defined", cleaned.size());
    }


    /**
     * If two instructions have the same set of OpCodes then merge them into a single line.
     * @return  a cleaned up version of {@link #byName}, sorted by instruction name
     */
    @Nonnull
    private Map<String, Collection<Integer>> mergeDuplicates() {
        Map<String, Collection<Integer>> cleaned = new TreeMap<>();
        Map<Collection<Integer>, String> reverse = new HashMap<>();

        byName.forEach((name, opCodes) -> {
            String existingName = reverse.get(opCodes);

            if (existingName != null) {
                cleaned.remove(existingName);

                existingName = existingName + ", " + name;
            } else {
                existingName = name;
            }

            cleaned.put(existingName, opCodes);
            reverse.put(opCodes, existingName);
        });

        return cleaned;
    }
}
