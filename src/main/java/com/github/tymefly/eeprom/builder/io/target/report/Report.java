package com.github.tymefly.eeprom.builder.io.target.report;

import java.util.Collection;

import javax.annotation.Nonnull;

import com.github.tymefly.eeprom.builder.exception.IoException;

/**
 * Create Human readable reports describing the generated EEPROM code.
 */
public interface Report {
    /**
     * Ad an unused opCode to the report
     * @param code      an OpCode
     */
    void unusedCode(int code);

    /**
     * Add a defined OpCode to the report
     * @param code      an OpCode
     * @param names     Names of the instruction(s) that use this opCode. An OpCode have have multiple named
     *                      instructions if they are uniquely differentiable by the flags
     */
    void usedCode(int code, @Nonnull Collection<String> names);

    /**
     * Complete writing the report to disc.
     * @throws IoException  if the report could not be written
     */
    void close() throws IoException;
}
