package com.github.tymefly.eeprom.builder.io.target.report;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import com.github.tymefly.eeprom.builder.exception.IoException;


/**
 * An Aggregator class for the different reports in this package
 */
public class Reporter implements Report {
    private final List<ReportFile> reports = new ArrayList<>();


    /**
     * Constructor
     * @param targetDirectory   Directory the reports will be written in
     */
    public Reporter(@Nonnull File targetDirectory)  {
        reports.add(new Available(targetDirectory));
        reports.add(new ByCode(targetDirectory));
        reports.add(new ByInstruction(targetDirectory));
    }


    @Override
    public void unusedCode(int code) {
        reports.forEach(r -> r.unusedCode(code));
    }


    @Override
    public void usedCode(int code, @Nonnull Collection<String> names) {
        reports.forEach(r -> r.usedCode(code, names));
    }


    @Override
    public void close() throws IoException {
        reports.forEach(ReportFile::close);
        reports.clear();
    }
}
