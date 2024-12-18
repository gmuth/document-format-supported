package de.gmuth.ipp;

/**
 * Copyright (c) 2024 Gerhard Muth
 */

import de.gmuth.ipp.core.IppAttributesGroup;
import de.gmuth.ipp.core.IppResponse;
import de.gmuth.ipp.core.IppString;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class PrinterDescription {
    String makeAndModel;
    String applicationTypes;
    String imagesTypes;
    String textTypes;

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

    static PrinterDescription fromRawIppResponse(byte[] rawIppBytes) {
        IppResponse response = new IppResponse();
        response.decode(rawIppBytes);
        return fromIppResponse(response);
    }

    static PrinterDescription fromIppResponse(IppResponse getPrinterAttributesResponse) {
        IppAttributesGroup attributes = getPrinterAttributesResponse.getPrinterGroup();
        IppString makeAndModel = attributes.getValue("printer-make-and-model");
        List<String> documentFormatSupported = attributes.getValues("document-format-supported");
        Map<String, String> documentFormatSupportedMap = getDocumentFormatSupportedMap(documentFormatSupported);
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
                                String.join(", ", entry.getValue().stream().map(mimeType -> mimeType[1]).sorted().toList())
                        )
                )
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    static String stringOrEmpty(String string) {
        return string == null ? "" : string;
    }

    List<String> toList() {
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