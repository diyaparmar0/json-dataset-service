package com.datasetapp;

import com.datasetapp.repository.DatasetRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1")
class DatasetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DatasetRecordRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    private void insert(String dataset, String json) throws Exception {
        mockMvc.perform(post("/api/dataset/" + dataset + "/record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    private void insertSampleEmployees() throws Exception {
        insert("employees", "{\"id\":1,\"name\":\"John Doe\",\"age\":30,\"department\":\"Engineering\"}");
        insert("employees", "{\"id\":2,\"name\":\"Jane Smith\",\"age\":25,\"department\":\"Engineering\"}");
        insert("employees", "{\"id\":3,\"name\":\"Alice Brown\",\"age\":28,\"department\":\"Marketing\"}");
    }

    @Test
    void insertRecordReturnsConfirmation() throws Exception {
        mockMvc.perform(post("/api/dataset/employees/record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"name\":\"John Doe\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Record added successfully"))
                .andExpect(jsonPath("$.dataset").value("employees"))
                .andExpect(jsonPath("$.recordId").value(1));
    }

    @Test
    void insertingSameIdTwiceGivesConflict() throws Exception {
        insert("employees", "{\"id\":1,\"name\":\"John\"}");

        mockMvc.perform(post("/api/dataset/employees/record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"name\":\"Someone else\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void sameIdIsAllowedInDifferentDatasets() throws Exception {
        insert("employees", "{\"id\":1,\"name\":\"John\"}");
        insert("customers", "{\"id\":1,\"name\":\"Acme\"}");
    }

    @Test
    void recordWithoutIdIsRejected() throws Exception {
        mockMvc.perform(post("/api/dataset/employees/record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"No id\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void malformedJsonIsRejected() throws Exception {
        mockMvc.perform(post("/api/dataset/employees/record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void groupByReturnsRecordsGroupedByField() throws Exception {
        insertSampleEmployees();

        mockMvc.perform(get("/api/dataset/employees/query").param("groupBy", "department"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupedRecords.Engineering.length()").value(2))
                .andExpect(jsonPath("$.groupedRecords.Marketing.length()").value(1));
    }

    @Test
    void sortByAscendingOrdersRecords() throws Exception {
        insertSampleEmployees();

        mockMvc.perform(get("/api/dataset/employees/query")
                        .param("sortBy", "age").param("order", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sortedRecords[0].id").value(2))
                .andExpect(jsonPath("$.sortedRecords[1].id").value(3))
                .andExpect(jsonPath("$.sortedRecords[2].id").value(1));
    }

    @Test
    void sortByDescendingOrdersRecords() throws Exception {
        insertSampleEmployees();

        mockMvc.perform(get("/api/dataset/employees/query")
                        .param("sortBy", "age").param("order", "desc"))
                .andExpect(jsonPath("$.sortedRecords[0].id").value(1));
    }

    @Test
    void groupByAndSortByCanBeCombined() throws Exception {
        insertSampleEmployees();

        mockMvc.perform(get("/api/dataset/employees/query")
                        .param("groupBy", "department").param("sortBy", "age"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupedRecords.Engineering[0].id").value(2))
                .andExpect(jsonPath("$.groupedRecords.Engineering[1].id").value(1));
    }

    @Test
    void unknownDatasetReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/dataset/missing/query").param("groupBy", "department"))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidOrderReturnsBadRequest() throws Exception {
        insertSampleEmployees();

        mockMvc.perform(get("/api/dataset/employees/query")
                        .param("sortBy", "age").param("order", "sideways"))
                .andExpect(status().isBadRequest());
    }
    @Test
void wrongContentTypeReturns415NotServerError() throws Exception {
    mockMvc.perform(post("/api/dataset/employees/record")
                    .contentType(MediaType.TEXT_PLAIN)
                    .content("{\"id\":1}"))
            .andExpect(status().isUnsupportedMediaType());
}
}
