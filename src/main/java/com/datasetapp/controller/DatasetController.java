
package com.datasetapp.controller;

import com.datasetapp.dto.InsertRecordResponse;
import com.datasetapp.service.DatasetService;
import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(
    value = "/api/dataset/{datasetName}",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(
    name = "Dataset Controller",
    description = "APIs for inserting, grouping and sorting JSON dataset records"
)
public class DatasetController {

    private final DatasetService datasetService;

    public DatasetController(DatasetService datasetService) {
        this.datasetService = datasetService;
    }

    @Operation(
        summary = "Insert a record",
        description = "Inserts a JSON record into the specified dataset."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Record inserted successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = InsertRecordResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "message": "Record inserted successfully",
                      "dataset": "employees",
                      "recordId": 1
                    }
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid JSON or missing record ID"),
        @ApiResponse(responseCode = "409", description = "Record already exists"),
        @ApiResponse(responseCode = "415", description = "Unsupported content type")
    })
    @PostMapping(value = "/record", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InsertRecordResponse> insertRecord(
        @Parameter(description = "Name of the dataset", example = "employees")
        @PathVariable String datasetName,

        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "JSON record to insert",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = "object"),
                examples = @ExampleObject(
                    name = "Employee record",
                    value = """
                    {
                      "id": 1,
                      "name": "Diya",
                      "department": "Engineering",
                      "age": 23
                    }
                    """
                )
            )
        )
        @org.springframework.web.bind.annotation.RequestBody JsonNode body
    ) {
        InsertRecordResponse response =
            datasetService.insertRecord(datasetName, body);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "Query dataset records",
        description = "Retrieve dataset records with optional grouping and sorting."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Records retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = "object")
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid sorting order"),
        @ApiResponse(responseCode = "404", description = "Dataset not found")
    })
    @GetMapping("/query")
    public ResponseEntity<Map<String, Object>> query(

        @Parameter(description = "Name of the dataset", example = "employees")
        @PathVariable String datasetName,

        @Parameter(description = "Field used to group records", example = "department")
        @RequestParam(required = false) String groupBy,

        @Parameter(description = "Field used to sort records", example = "age")
        @RequestParam(required = false) String sortBy,

        @Parameter(description = "Sorting direction: asc or desc", example = "asc")
        @RequestParam(required = false, defaultValue = "asc") String order
    ) {
        return ResponseEntity.ok(
            datasetService.query(datasetName, groupBy, sortBy, order)
        );
    }
}
