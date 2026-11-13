package com.datasetapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * One JSON record that belongs to a dataset.
 * The JSON itself is kept as text in a single column, so any shape of record can be stored.
 */
@Entity
@Table(
        name = "dataset_records",
        uniqueConstraints = @UniqueConstraint(columnNames = {"dataset_name", "record_id"}),
        indexes = @Index(name = "idx_dataset_name", columnList = "dataset_name")
)
public class DatasetRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dataset_name", nullable = false, length = 100)
    private String datasetName;

    /** The "id" field supplied by the client inside the JSON body. */
    @Column(name = "record_id", nullable = false)
    private Long recordId;

    @Column(name = "json_data", nullable = false, length = 100000)
    private String jsonData;

    protected DatasetRecord() {
        // required by JPA
    }

    public DatasetRecord(String datasetName, Long recordId, String jsonData) {
        this.datasetName = datasetName;
        this.recordId = recordId;
        this.jsonData = jsonData;
    }

    public Long getId() { return id; }
    public String getDatasetName() { return datasetName; }
    public Long getRecordId() { return recordId; }
    public String getJsonData() { return jsonData; }
}
