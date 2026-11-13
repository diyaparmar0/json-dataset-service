# JSON Dataset Service

A Spring Boot REST application that stores JSON records inside named datasets and lets you
run **group-by** and **sort-by** queries on them.

## Tech stack
- Java 17, Spring Boot 3.3
- Spring Data JPA + H2 (relational database, file based)
- JUnit 5 + MockMvc for tests

## How to run
```bash
mvn spring-boot:run
```
The app starts on `http://localhost:8080`. Data is saved in `./data/datasetdb` so it survives a restart.
H2 console: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:file:./data/datasetdb`, user `sa`, empty password).

## How to run the tests
```bash
mvn test
```

## API

### 1. Insert a record
`POST /api/dataset/{datasetName}/record`

```json
{ "id": 1, "name": "John Doe", "age": 30, "department": "Engineering" }
```
Response `201 Created`
```json
{ "message": "Record added successfully", "dataset": "employees", "recordId": 1 }
```
Rules: body must be a JSON object with an integer `id`. `id` must be unique inside a dataset.
Dataset name may contain letters, digits, `_` and `-`.

### 2. Query a dataset
`GET /api/dataset/{datasetName}/query`

| Parameter | Required | Meaning |
|-----------|----------|---------|
| `groupBy` | no | field to group records by |
| `sortBy`  | no | field to sort records by |
| `order`   | no | `asc` (default) or `desc` |

**Group by** - `GET /api/dataset/employees/query?groupBy=department`
```json
{ "groupedRecords": { "Engineering": [ {...}, {...} ], "Marketing": [ {...} ] } }
```

**Sort by** - `GET /api/dataset/employees/query?sortBy=age&order=asc`
```json
{ "sortedRecords": [ {...}, {...}, {...} ] }
```

**Both** - `?groupBy=department&sortBy=age` returns `groupedRecords`, with each group sorted.
**Neither** - returns all records as `{ "records": [...] }`.

Sorting notes: numbers are sorted as numbers, text ignores case, and records missing the field are placed last.
Grouping note: records missing the group field go under the key `"null"`.

### Errors
All errors use the same shape:
```json
{ "timestamp": "...", "status": 404, "error": "Not Found", "message": "Dataset 'x' not found" }
```
| Status | When |
|--------|------|
| 400 | invalid JSON, missing/non-integer `id`, bad dataset name, bad `order` |
| 404 | dataset has no records |
| 409 | record `id` already exists in that dataset |
| 500 | unexpected server error |

## Project structure
```
controller/  REST endpoints
service/     business logic (validation, group/sort orchestration)
repository/  Spring Data JPA repository
model/       JPA entity (one JSON record = one row)
util/        RecordGrouper, RecordSorter
dto/         response objects
exception/   custom exceptions + global handler
```

## Quick curl test
```bash
curl -X POST localhost:8080/api/dataset/employees/record -H "Content-Type: application/json" \
  -d '{"id":1,"name":"John Doe","age":30,"department":"Engineering"}'
curl "localhost:8080/api/dataset/employees/query?groupBy=department"
curl "localhost:8080/api/dataset/employees/query?sortBy=age&order=desc"
```
