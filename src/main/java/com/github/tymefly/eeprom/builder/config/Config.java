package com.github.tymefly.eeprom.builder.config;

import java.io.File;
import java.io.PrintStream;

import javax.annotation.Nonnull;

import com.github.tymefly.eeprom.builder.io.source.ProjectFile;
import com.github.tymefly.eeprom.builder.utils.Preconditions;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ParserProperties;

import static org.kohsuke.args4j.OptionHandlerFilter.ALL;


/**
 * Command Line Argument parser.
 */
public class Config {
    private static final int SCREEN_WIDTH_CHARACTERS = 80;
    private static Config instance;

    @Argument(required = true, metaVar = "LOCATION", usage = "directory containing the project files")
    private File project;

    @Option(name = "-?", aliases = {"--help", "-h"}, help = true)
    private boolean help;

    private final String parent;
    private final CmdLineParser parser;
    private boolean isValid;


    private Config(@Nonnull Class<?> parent) {
        ParserProperties parserProperties = ParserProperties.defaults()
                .withUsageWidth(SCREEN_WIDTH_CHARACTERS)
                .withAtSyntax(false)
                .withShowDefaults(true);

        this.parent = parent.getName();
        this.parser = new CmdLineParser(this, parserProperties);
    }


    /**
     * Parse the command line arguments. This must be done before calling any other methods in this class
     * @param parent        The class that implements {@literal main(String[])}
     * @param args          The command line arguments
     * @return              The singleton instance of this class
     */
    @Nonnull
    public static Config parse(@Nonnull Class<?> parent, String... args) {
        Preconditions.checkState(instance == null, "Command line has already been set");

        instance = new Config(parent);
        instance.parse(args);

        return instance;
    }


    private void parse(String... args) {
        try {
            parser.parseArgument(args);

            isValid = validate();
        } catch (CmdLineException e) {
            isValid = false;
            System.err.println("Error: " + e.getMessage());
        }

        if (!isValid) {
            System.err.println();
            displayUsage(System.err);
        }
    }


    private boolean validate() {
        String message;

        if (!project.isDirectory()) {
            message = "Error: Invalid directory " + project.getAbsolutePath();
        } else {
            message = null;

            for (ProjectFile test : ProjectFile.values()) {
                if (!new File(project, test.getFileName()).exists()) {
                    message = "Project is missing required file '" + test.getFileName() + "'";
                    break;
                }
            }
        }

        boolean valid = (message == null);

        if (!valid) {
            System.err.printf("Error: %s%n", message);
        }

        return valid;
    }


    /**
     * Dumps the command line syntax to {@link System#out}
     */
    public void displayUsage() {
        displayUsage(System.out);
    }


    private void displayUsage(@Nonnull PrintStream stream) {
        stream.println("Usage:");
        stream.println("  java " + parent + " " + parser.printExample(ALL));
        stream.println();

        this.parser.printUsage(stream);
    }


    /**
     * Returns {@literal true} only of the command line was valid
     * @return {@literal true} only of the command line was valid
     */
    public boolean isValid() {
        return isValid;
    }


    /**
     * Returns {@literal true} only if the user asked to see the help page
     * @return {@literal true} only if the user asked to see the help page
     */
    public boolean requestHelp() {
        return help;
    }


    /**
     * Returns the location of the project directory
     * @return the location of the project directory
     */
    @Nonnull
    public File getProject() {
        return project;
    }
}
