package de.gmuth.ipp;

/**
 * Copyright (c) 2024 Gerhard Muth
 */

import de.gmuth.log.Logging;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WriteMarkdownFile {

    static Logger logger = Logging.getLogger(WriteMarkdownFile.class);

    public static void main(String[] args) {
        Logging.configure(Level.INFO);
        try {
            new WriteMarkdownFile().run(args);
        } catch (Throwable throwable) {
            logger.log(Level.SEVERE, "run failed", throwable);
        }
        Logging.flush();
    }

    void run(String[] args) throws Exception {
        File markdownFile = new File("README.md");
        logger.info(() -> "Write markdown file: " + markdownFile);
        OutputStream outputStream = new FileOutputStream(markdownFile);
        copyFile("introduction.md", outputStream);
        writePrinterTable(outputStream);
        copyFile("contribute.md", outputStream);
    }

    void copyFile(String filename, OutputStream outputStream) throws IOException {
        logger.info("Copying file " + filename);
        Files.copy(Path.of(filename), outputStream);
    }

    void writePrinterTable(OutputStream outputStream) {
        PrinterDescriptionRepository printerDescriptionRepository = new PrinterDescriptionRepository();
        List<PrinterDescription> printerDescriptions = printerDescriptionRepository.getPrinterDescriptions();
        logger.info("Writing " + printerDescriptions.size() + " printer descriptions:");
        new MarkdownTable() {{
            add(List.of("Make and Model", "application", "image", "text"));
            printerDescriptions.forEach(printerDescription -> {
                add(printerDescription.toList());
                logger.info(printerDescription.toString());
            });
            addTitleSeparator(); // uses max column width!
            writeMarkdown(outputStream);
        }};
    }

}