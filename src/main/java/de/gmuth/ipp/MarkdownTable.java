package de.gmuth.ipp;

/**
 * Copyright (c) 2024 Gerhard Muth
 */

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MarkdownTable extends ArrayList<List<String>> {

    protected Map<Integer, Integer> maxWidths = new HashMap<>();

    @Override
    public boolean add(List<String> columns) {
        for (int index = 0; index < columns.size(); index++) {
            int width = columns.get(index).length();
            Integer maxWidth = this.maxWidths.get(index);
            if (maxWidth == null || maxWidth < width) this.maxWidths.put(index, width);
        }
        return super.add(columns);
    }

    void addTitleSeparator() {
        add(1, maxWidths.values().stream().map("-"::repeat).toList());
    }

    void writeMarkdown(OutputStream outputStream) {
        PrintWriter printWriter = new PrintWriter(outputStream);
        String formatString = maxWidths.values().stream()
                .map(length -> "%-" + length + "s")
                .collect(Collectors.joining("|", "|", "|"));
        forEach(row -> printWriter.println(formatString.formatted(row.toArray())));
        printWriter.flush();
    }

}