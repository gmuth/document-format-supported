package de.gmuth.md;

/**
 * Copyright (c) 2024 Gerhard Muth
 */

import de.gmuth.ipp.client.IppMessageRepository;
import de.gmuth.ipp.client.PrinterDescription;
import de.gmuth.log.Logging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GenerateReadme {

    static Logger logger = Logging.getLogger(GenerateReadme.class);
    static private IppMessageRepository ippMessageRepository;

    public void generateReadmeFile() throws Exception {
        ippMessageRepository = IppMessageRepository.getInstance();
        generatePrintersFile();
        File mdFile = new File("README.md");
        logger.info(() -> "> Generate markdown file: " + mdFile);
        OutputStream outputStream = new FileOutputStream(mdFile);
        try (outputStream) {
            copyFile("md/introduction.md", outputStream);
            copyFile("md/printers.md", outputStream);
            copyFile("md/usage.md", outputStream);
            copyFile("md/contribute.md", outputStream);
        }
    }

    protected void generatePrintersFile() throws IOException {
        File mdFile = new File("md/printers.md");
        logger.info(() -> "> Generate markdown file: " + mdFile);
        List<PrinterDescription> printerDescriptions = getPrinterDescriptions();
        logger.info("Writing " + printerDescriptions.size() + " printer descriptions:");
        MarkdownTable mdTable = new MarkdownTable();
        mdTable.add(List.of("Make and Model", "application", "image", "text"));
        printerDescriptions.forEach(printerDescription -> {
            mdTable.add(printerDescription.toList());
            logger.info(printerDescription.toString());
        });
        mdTable.addTitleSeparator(); // uses max column width!
        mdTable.writeMarkdown(mdFile);
    }

    private List<PrinterDescription> getPrinterDescriptions() throws IOException {
        Map<String, PrinterDescription> printerDescriptionMap = new LinkedHashMap<>();
        ippMessageRepository
                .allIppResponses.stream()
                .map(PrinterDescription::fromIppResponse)
                .sorted(Comparator.comparing(it -> it.makeAndModel))
                .forEach(it -> {
                    PrinterDescription replaced = printerDescriptionMap.put(it.makeAndModel, it);
                    if (replaced != null) logger.warning("Found duplicate key: " + replaced.makeAndModel);
                });
        return new ArrayList<>(printerDescriptionMap.values());
    }

    static void copyFile(String filename, OutputStream outputStream) throws IOException {
        logger.info("Copying file " + filename);
        Files.copy(Path.of(filename), outputStream);
    }

    public static void main(String[] args) {
        Logging.configure(Level.INFO, true);
        try {
            new GenerateReadme().generateReadmeFile();
        } catch (Throwable throwable) {
            logger.log(Level.SEVERE, "main failed", throwable);
        }
        Logging.flush();
    }

}