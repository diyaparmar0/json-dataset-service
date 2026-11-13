package com.datasetapp.util;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Groups JSON records by the value of one field.
 * Records that do not have the field are collected under the key "null".
 */
@Component
public class RecordGrouper {

    public static final String MISSING_KEY = "null";

    public Map<String, List<JsonNode>> groupBy(List<JsonNode> records, String field) {
        Map<String, List<JsonNode>> groups = new LinkedHashMap<>();
        for (JsonNode record : records) {
            String key = keyFor(record, field);
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(record);
        }
        return groups;
    }

    private String keyFor(JsonNode record, String field) {
        JsonNode value = record.get(field);
        if (value == null || value.isNull()) {
            return MISSING_KEY;
        }
        return value.isValueNode() ? value.asText() : value.toString();
    }
}
