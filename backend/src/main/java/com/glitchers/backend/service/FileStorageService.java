package com.glitchers.backend.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FileStorageService {

    @Value("${upload.storage.directory}")
    public String STORAGE_DIRECTORY;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    public String saveFile(MultipartFile fileToSave) throws IOException {

        if (fileToSave == null) {
            throw new NullPointerException("fileToSave is null");
        }

        File targetFile = new File(STORAGE_DIRECTORY + File.separator + fileToSave.getOriginalFilename());

        Files.copy(fileToSave.getInputStream(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // Process CSV
        String tablename=processCSV(targetFile);

        // Delete original file
        targetFile.delete();

        return tablename;
    }

    private String processCSV(File csvFile) throws IOException {

        Reader reader = new FileReader(csvFile);

        CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());

        Map<String, Integer> headers = parser.getHeaderMap();

        String fileName = csvFile.getName()
                .replace(".csv", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "_");

        String tableName = "dataset_" + fileName;

        Map<String, String> columnTypes = new HashMap<>();

        for (String column : headers.keySet()) {
            columnTypes.put(column, "TEXT");
        }

        int sampleSize = 50;
        int count = 0;

        for (CSVRecord record : parser) {

            for (String column : headers.keySet()) {

                String value = record.get(column);
                String detectedType = detectType(value);

                String currentType = columnTypes.get(column);

                columnTypes.put(column, upgradeType(currentType, detectedType));
            }

            count++;
            if (count >= sampleSize) break;
        }

        createTable(columnTypes, tableName);


        parser.close();
        reader.close();

        reader = new FileReader(csvFile);
        parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());
        insertRows(parser, headers, tableName);

        return tableName;
    }

    private void createTable(Map<String,String> columnTypes, String tableName) {

        StringBuilder sql = new StringBuilder("CREATE TABLE " + tableName + " (");

        for (Map.Entry<String,String> entry : columnTypes.entrySet()) {

            String column = entry.getKey()
                    .trim()
                    .toLowerCase()
                    .replaceAll("[^a-z0-9]", "_");

            sql.append(column)
                    .append(" ")
                    .append(entry.getValue())
                    .append(",");
        }

        sql.deleteCharAt(sql.length()-1);
        sql.append(")");

        jdbcTemplate.execute(sql.toString());
    }

    private void insertRows(CSVParser parser,
                            Map<String,Integer> headers,
                            String tableName) {

            for (CSVRecord record : parser) {

                StringBuilder columns = new StringBuilder();
                StringBuilder values = new StringBuilder();

            for (String column : headers.keySet()) {

                columns.append(column).append(",");

                String value = record.get(column);
                value = normalizeDate(value);
                value = value.replace("'", "''");

                values.append("'")
                        .append(value)
                        .append("'")
                        .append(",");
            }

            columns.deleteCharAt(columns.length()-1);
            values.deleteCharAt(values.length()-1);

            String sql = "INSERT INTO " + tableName +
                    " (" + columns + ") VALUES (" + values + ")";

            jdbcTemplate.execute(sql);
        }
    }

    private String detectType(String value) {

        if (value == null || value.isEmpty()) {
            return "TEXT";
        }

        // INTEGER
        if (value.matches("-?\\d+")) {
            return "INTEGER";
        }

        // DOUBLE
        if (value.matches("-?\\d+(\\.\\d+)?([eE]-?\\d+)?")) {
            return "DOUBLE PRECISION";
        }

        // DATE format YYYY-MM-DD
        if (value.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return "DATE";
        }

        // DATE format MM/DD/YYYY
        if (value.matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
            return "DATE";
        }

        // BOOLEAN
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return "BOOLEAN";
        }

        return "TEXT";
    }

    private String normalizeDate(String value) {

        if (value.matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {

            String[] parts = value.split("/");

            String month = parts[0].length() == 1 ? "0" + parts[0] : parts[0];
            String day = parts[1].length() == 1 ? "0" + parts[1] : parts[1];
            String year = parts[2];

            return year + "-" + month + "-" + day;
        }

        return value;
    }

    private String upgradeType(String current, String detected) {

        if (current.equals("TEXT")) return detected;

        if (current.equals("INTEGER") && detected.equals("DOUBLE PRECISION"))
            return "DOUBLE PRECISION";

        if (detected.equals("TEXT"))
            return "TEXT";

        return current;
    }

    public List<Map<String, Object>> getTableMetadata(String tableName) {

        String sql = """
            SELECT column_name, data_type
            FROM information_schema.columns
            WHERE table_name = ?
            ORDER BY ordinal_position
            """;

        return jdbcTemplate.queryForList(sql, tableName);
    }
}
