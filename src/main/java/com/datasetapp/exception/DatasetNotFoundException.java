package com.datasetapp.exception;

public class DatasetNotFoundException extends RuntimeException {
    public DatasetNotFoundException(String datasetName) {
        super("Dataset '" + datasetName + "' not found");
    }
}
