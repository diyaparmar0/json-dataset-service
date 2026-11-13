package com.datasetapp;

import com.datasetapp.util.RecordSorter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecordSorterTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final RecordSorter sorter = new RecordSorter();

    private JsonNode json(String text) throws Exception {
        return mapper.readTree(text);
    }

    @Test
    void sortsNumbersNumericallyNotAsText() throws Exception {
        List<JsonNode> records = List.of(json("{\"id\":1,\"age\":100}"), json("{\"id\":2,\"age\":9}"));

        List<JsonNode> sorted = sorter.sortBy(records, "age", true);

        assertEquals(2, sorted.get(0).get("id").asInt());
        assertEquals(1, sorted.get(1).get("id").asInt());
    }

    @Test
    void descendingReversesOrder() throws Exception {
        List<JsonNode> records = List.of(json("{\"id\":1,\"age\":20}"), json("{\"id\":2,\"age\":30}"));

        List<JsonNode> sorted = sorter.sortBy(records, "age", false);

        assertEquals(2, sorted.get(0).get("id").asInt());
    }

    @Test
    void recordsWithoutFieldStayAtTheEnd() throws Exception {
        List<JsonNode> records = List.of(json("{\"id\":1}"), json("{\"id\":2,\"age\":5}"));

        assertEquals(2, sorter.sortBy(records, "age", true).get(0).get("id").asInt());
        assertEquals(2, sorter.sortBy(records, "age", false).get(0).get("id").asInt());
    }

    @Test
    void textIsComparedIgnoringCase() throws Exception {
        List<JsonNode> records = List.of(json("{\"id\":1,\"name\":\"bob\"}"), json("{\"id\":2,\"name\":\"Alice\"}"));

        assertEquals(2, sorter.sortBy(records, "name", true).get(0).get("id").asInt());
    }
}
