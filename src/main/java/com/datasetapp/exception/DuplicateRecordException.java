package com.datasetapp.exception;

public class DuplicateRecordException extends RuntimeException {
    public DuplicateRecordException(String datasetName, Long recordId) {
        super("Record with id " + recordId + " already exists in dataset '" + datasetName + "'");
    }
}
