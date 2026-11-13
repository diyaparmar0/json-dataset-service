package com.datasetapp.controller;

import com.datasetapp.dto.InsertRecordResponse;
import com.datasetapp.service.DatasetService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(value = "/api/dataset/{datasetName}", produces = MediaType.APPLICATION_JSON_VALUE)
public class DatasetController {

    private final DatasetService datasetService;

    public DatasetController(DatasetService datasetService) {
        this.datasetService = datasetService;
    }

    @PostMapping(value = "/record", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InsertRecordResponse> insertRecord(@PathVariable String datasetName,
                                                             @RequestBody JsonNode body) {
        InsertRecordResponse response = datasetService.insertRecord(datasetName, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/query")
    public ResponseEntity<Map<String, Object>> query(@PathVariable String datasetName,
                                                     @RequestParam(required = false) String groupBy,
                                                     @RequestParam(required = false) String sortBy,
                                                     @RequestParam(required = false, defaultValue = "asc") String order) {
        return ResponseEntity.ok(datasetService.query(datasetName, groupBy, sortBy, order));
    }
}
