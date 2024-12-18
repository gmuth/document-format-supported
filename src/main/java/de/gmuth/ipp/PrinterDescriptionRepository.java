package de.gmuth.ipp;

/**
 * Copyright (c) 2024 Gerhard Muth
 */

import de.gmuth.log.Logging;

import java.util.List;
import java.util.logging.Logger;

public class PrinterDescriptionRepository {

    static Logger logger = Logging.getLogger(PrinterDescriptionRepository.class);

    PrinterAttributesRepository printerAttributesRepository = new PrinterAttributesRepository();

    List<PrinterDescription> getPrinterDescriptions() {
        return printerAttributesRepository
                .getRawPrinterAttributesResponses().stream()
                .map(PrinterDescription::fromRawIppResponse)
                .toList();
    }

}