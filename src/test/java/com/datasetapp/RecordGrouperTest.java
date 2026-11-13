package com.datasetapp;

import com.datasetapp.util.RecordGrouper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecordGrouperTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final RecordGrouper grouper = new RecordGrouper();

    @Test
    void groupsRecordsByFieldValue() throws Exception {
        List<JsonNode> records = List.of(
                mapper.readTree("{\"id\":1,\"department\":\"Engineering\"}"),
                mapper.readTree("{\"id\":2,\"department\":\"Marketing\"}"),
                mapper.readTree("{\"id\":3,\"department\":\"Engineering\"}"));

        Map<String, List<JsonNode>> groups = grouper.groupBy(records, "department");

        assertEquals(2, groups.size());
        assertEquals(2, groups.get("Engineering").size());
        assertEquals(1, groups.get("Marketing").size());
    }

    @Test
    void recordsWithoutFieldGoToNullGroup() throws Exception {
        List<JsonNode> records = List.of(mapper.readTree("{\"id\":1}"));

        Map<String, List<JsonNode>> groups = grouper.groupBy(records, "department");

        assertTrue(groups.containsKey(RecordGrouper.MISSING_KEY));
    }
}
