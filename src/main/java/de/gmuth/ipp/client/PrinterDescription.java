package de.gmuth.ipp.client;

/**
 * Copyright (c) 2024 Gerhard Muth
 */

import de.gmuth.ipp.core.IppAttributesGroup;
import de.gmuth.ipp.core.IppResponse;
import de.gmuth.ipp.core.IppString;
import de.gmuth.log.Logging;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PrinterDescription {
    public String makeAndModel;
    public String applicationTypes;
    public String imagesTypes;
    public String textTypes;

    public PrinterDescription(
            String makeAndModel,
            String applapplicationTypes,
            String imagesTypes,
            String textTypes
    ) {
        this.makeAndModel = makeAndModel;
        this.applicationTypes = applapplicationTypes;
        this.imagesTypes = imagesTypes;
        this.textTypes = textTypes;
    }

    static Logger logger = Logging.getLogger(PrinterDescription.class);

    public static PrinterDescription fromIppResponse(IppResponse getPrinterAttributesResponse) {
        IppAttributesGroup attributes = getPrinterAttributesResponse.getPrinterGroup();
        IppString makeAndModel = attributes.getValue("printer-make-and-model");
        List<String> documentFormatSupported = attributes.getValues("document-format-supported");
        Map<String, String> documentFormatSupportedMap = getDocumentFormatSupportedMap(documentFormatSupported);
        if (documentFormatSupportedMap.keySet().size() > 3) {
            logger.warning(String.format(
                    "Found more main types than expected: %s -> %s",
                    makeAndModel, String.join(", ", documentFormatSupportedMap.keySet())
            ));
        }
        return new PrinterDescription(
                makeAndModel.getText(),
                documentFormatSupportedMap.get("application"),
                documentFormatSupportedMap.get("image"),
                documentFormatSupportedMap.get("text")
        );
    }

    private static Map<String, String> getDocumentFormatSupportedMap(List<String> documentFormatSupported) {
        return documentFormatSupported
                .stream().sorted()
                .map(mimeType -> mimeType.split("/"))
                .collect(Collectors.groupingBy(mimeType -> mimeType[0], TreeMap::new, Collectors.toList()))
                .entrySet().stream()
                .map(entry -> Map.entry(
                                entry.getKey(),
                                String.join(", ", entry.getValue().stream().map(mimeType -> mimeType[1]).sorted().collect(Collectors.toList()))
                        )
                )
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    static String stringOrEmpty(String string) {
        return string == null ? "" : string;
    }

    public List<String> toList() {
        return List.of(
                makeAndModel,
                stringOrEmpty(applicationTypes),
                stringOrEmpty(imagesTypes),
                stringOrEmpty(textTypes)
        );
    }

    @Override
    public String toString() {
        return String.format("%-50s application: %s; image: %s; text: %s;",
                makeAndModel, applicationTypes, stringOrEmpty(imagesTypes), stringOrEmpty(textTypes)
        );
    }
}