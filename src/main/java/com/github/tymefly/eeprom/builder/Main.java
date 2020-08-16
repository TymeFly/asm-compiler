package com.github.tymefly.eeprom.builder;

import java.io.File;

import javax.annotation.Nonnull;

import com.github.tymefly.eeprom.builder.config.Config;
import com.github.tymefly.eeprom.builder.io.target.eeprom.Content;
import com.github.tymefly.eeprom.builder.io.target.eeprom.ContentGenerator;
import com.github.tymefly.eeprom.builder.io.target.report.Reporter;
import com.github.tymefly.eeprom.builder.project.code.Compiler;
import com.github.tymefly.eeprom.builder.project.code.MicroCode;
import com.github.tymefly.eeprom.builder.project.global.GlobalData;
import com.github.tymefly.eeprom.builder.project.global.GlobalDataFactory;
import com.github.tymefly.eeprom.builder.project.input.InputPins;
import com.github.tymefly.eeprom.builder.project.input.InputPinsFactory;
import com.github.tymefly.eeprom.builder.project.output.OutputPins;
import com.github.tymefly.eeprom.builder.project.output.OutputPinsFactory;
import com.github.tymefly.eeprom.builder.utils.IoUtils;


/**
 * Application entry point
 */
public class Main {
    private Main() {
    }


    /**
     * Application entry point
     * @param args          Command line arguments
     */
    public static void main(@Nonnull String[] args) {
        Config config = Config.parse(Main.class, args);
        boolean done;

        if (config.requestHelp()) {
            config.displayUsage();
            done = true;
        } else if (config.isValid()) {
            done = run(config);
        } else {
            done = false;
        }

        System.exit(done ? 0 : 1);
    }


    private static boolean run(@Nonnull Config config) {
        boolean done;

        try {
            File projectDirectory = config.getProject();
            File targetDirectory = new File(projectDirectory, "target");

            System.setProperty("target.dir", targetDirectory.getAbsolutePath().replace('\\', '/'));
            IoUtils.clearDirectory(targetDirectory);

            GlobalData globalData = GlobalDataFactory.parse(projectDirectory);
            InputPins inputPins = InputPinsFactory.parse(projectDirectory);
            OutputPins outputPins = OutputPinsFactory.parse(projectDirectory);
            MicroCode microCode = new Compiler.Builder(projectDirectory)
                .withPins(inputPins, outputPins)
                .compile()
                .getMicroCode();

            generateReports(targetDirectory, microCode);
            generateRoms(targetDirectory, globalData, microCode, inputPins, outputPins);

            done = true;
        } catch (Exception e) {
            System.err.println("**** ERROR ****");
            e.printStackTrace();
            done = false;
        }

        return done;
    }


    private static void generateReports(@Nonnull File targetDirectory, @Nonnull MicroCode microCode) {
        Reporter reporter = new Reporter(targetDirectory);

        microCode.report(reporter);
        reporter.close();
    }


    private static void generateRoms(@Nonnull File targetDirectory,
                                     @Nonnull GlobalData globalData,
                                     @Nonnull MicroCode microCode,
                                     @Nonnull InputPins inputPins,
                                     @Nonnull OutputPins outputPins) {
        Content generator = new ContentGenerator(targetDirectory, globalData, inputPins, outputPins);

        microCode.generate(generator);
        generator.close();
    }
}
