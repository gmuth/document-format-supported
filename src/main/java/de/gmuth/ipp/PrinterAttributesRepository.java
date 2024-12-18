package de.gmuth.ipp;

/**
 * Copyright (c) 2024 Gerhard Muth
 */

import de.gmuth.log.Logging;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PrinterAttributesRepository {

    static Logger logger = Logging.getLogger(PrinterAttributesRepository.class);

    static File printersdirectory = new File("printers") {{
        if (!exists()) mkdirs();
    }};

    List<byte[]> getRawPrinterAttributesResponses() {
        return Arrays.stream(printersdirectory.list())
                .sorted()
                .filter(filename -> filename.endsWith(".bin"))
                .map(filename -> new File(printersdirectory, filename))
                .map(file -> {
                    try {
                        logger.info("Reading " + file);
                        return Files.readAllBytes(file.toPath());
                    } catch (IOException ioException) {
                        throw new RuntimeException("Failed to read " + file, ioException);
                    }
                })
                .collect(Collectors.toList());
    }

}