package com.datasetapp.util;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Sorts JSON records by one field.
 * Numbers are compared as numbers, everything else as text (case-insensitive).
 * Records without the field always go to the end, whatever the order is.
 */
@Component
public class RecordSorter {

    public List<JsonNode> sortBy(List<JsonNode> records, String field, boolean ascending) {
        Comparator<JsonNode> byValue = (a, b) -> compareValues(a.get(field), b.get(field));
        if (!ascending) {
            byValue = byValue.reversed();
        }

        // Missing values are split out so they stay at the end for both asc and desc.
        List<JsonNode> present = new ArrayList<>();
        List<JsonNode> missing = new ArrayList<>();
        for (JsonNode record : records) {
            JsonNode value = record.get(field);
            if (value == null || value.isNull()) {
                missing.add(record);
            } else {
                present.add(record);
            }
        }

        present.sort(byValue);
        present.addAll(missing);
        return present;
    }

    private int compareValues(JsonNode a, JsonNode b) {
        if (a.isNumber() && b.isNumber()) {
            return a.decimalValue().compareTo(b.decimalValue());
        }
        return a.asText().compareToIgnoreCase(b.asText());
    }
}
