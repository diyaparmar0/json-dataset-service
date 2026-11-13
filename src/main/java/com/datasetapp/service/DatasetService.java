package com.datasetapp.service;

import com.datasetapp.dto.InsertRecordResponse;
import com.datasetapp.exception.DatasetNotFoundException;
import com.datasetapp.exception.DuplicateRecordException;
import com.datasetapp.exception.InvalidQueryException;
import com.datasetapp.exception.InvalidRecordException;
import com.datasetapp.model.DatasetRecord;
import com.datasetapp.repository.DatasetRecordRepository;
import com.datasetapp.util.RecordGrouper;
import com.datasetapp.util.RecordSorter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class DatasetService {

    private static final Pattern VALID_DATASET_NAME = Pattern.compile("^[A-Za-z0-9_-]{1,100}$");

    private final DatasetRecordRepository repository;
    private final ObjectMapper objectMapper;
    private final RecordGrouper grouper;
    private final RecordSorter sorter;

    public DatasetService(DatasetRecordRepository repository,
                          ObjectMapper objectMapper,
                          RecordGrouper grouper,
                          RecordSorter sorter) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.grouper = grouper;
        this.sorter = sorter;
    }

    @Transactional
    public InsertRecordResponse insertRecord(String datasetName, JsonNode body) {
        validateDatasetName(datasetName);
        validateRecord(body);

        Long recordId = body.get("id").asLong();
        if (repository.existsByDatasetNameAndRecordId(datasetName, recordId)) {
            throw new DuplicateRecordException(datasetName, recordId);
        }

        repository.save(new DatasetRecord(datasetName, recordId, body.toString()));
        return new InsertRecordResponse("Record added successfully", datasetName, recordId);
    }

    /**
     * Runs the query on a dataset.
     * - only groupBy  -> {"groupedRecords": {...}}
     * - only sortBy   -> {"sortedRecords": [...]}
     * - both          -> records are sorted first, then grouped, so every group is sorted
     * - neither       -> {"records": [...]} (all records in insertion order)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> query(String datasetName, String groupBy, String sortBy, String order) {
        validateDatasetName(datasetName);
        boolean ascending = parseOrder(order);

        List<JsonNode> records = loadRecords(datasetName);
        Map<String, Object> response = new LinkedHashMap<>();

        boolean hasGroupBy = hasText(groupBy);
        boolean hasSortBy = hasText(sortBy);

        if (hasSortBy) {
            records = sorter.sortBy(records, sortBy.trim(), ascending);
        }

        if (hasGroupBy) {
            response.put("groupedRecords", grouper.groupBy(records, groupBy.trim()));
        } else if (hasSortBy) {
            response.put("sortedRecords", records);
        } else {
            response.put("records", records);
        }
        return response;
    }

    private List<JsonNode> loadRecords(String datasetName) {
        List<DatasetRecord> stored = repository.findByDatasetNameOrderByIdAsc(datasetName);
        if (stored.isEmpty()) {
            throw new DatasetNotFoundException(datasetName);
        }

        List<JsonNode> records = new ArrayList<>();
        for (DatasetRecord row : stored) {
            try {
                records.add(objectMapper.readTree(row.getJsonData()));
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Stored record " + row.getId() + " is corrupted", e);
            }
        }
        return records;
    }

    private void validateDatasetName(String datasetName) {
        if (datasetName == null || !VALID_DATASET_NAME.matcher(datasetName).matches()) {
            throw new InvalidQueryException(
                    "Dataset name must be 1-100 characters: letters, digits, '_' or '-'");
        }
    }

    private void validateRecord(JsonNode body) {
        if (body == null || !body.isObject() || body.isEmpty()) {
            throw new InvalidRecordException("Record must be a non-empty JSON object");
        }
        JsonNode id = body.get("id");
        if (id == null || !id.isIntegralNumber() || !id.canConvertToLong()) {
            throw new InvalidRecordException("Record must contain a numeric integer field 'id'");
        }
    }

    private boolean parseOrder(String order) {
        if (!hasText(order) || order.equalsIgnoreCase("asc")) {
            return true;
        }
        if (order.equalsIgnoreCase("desc")) {
            return false;
        }
        throw new InvalidQueryException("Parameter 'order' must be 'asc' or 'desc'");
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
