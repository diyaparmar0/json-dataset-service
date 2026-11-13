package com.datasetapp.repository;

import com.datasetapp.model.DatasetRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DatasetRecordRepository extends JpaRepository<DatasetRecord, Long> {

    List<DatasetRecord> findByDatasetNameOrderByIdAsc(String datasetName);

    boolean existsByDatasetNameAndRecordId(String datasetName, Long recordId);
}
