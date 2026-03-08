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
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
//import org.apache.commons.io;

@Service
public class FileStorageService {

    @Value("${upload.storage.directory}")
    public String STORAGE_DIRECTORY;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    public void saveFile(MultipartFile fileToSave) throws IOException {

        if (fileToSave == null) {
            throw new NullPointerException("fileToSave is null");
        }

        File targetFile = new File(STORAGE_DIRECTORY + File.separator + fileToSave.getOriginalFilename());

        Files.copy(fileToSave.getInputStream(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // Process CSV
        processCSV(targetFile);

        // Delete original file
        targetFile.delete();
    }

    private void processCSV(File csvFile) throws IOException {

        Reader reader = new FileReader(csvFile);

        CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());

        Map<String, Integer> headers = parser.getHeaderMap();

//        String tableName = "dataset_" + UUID.randomUUID().toString().replace("-", "");

        String fileName = csvFile.getName()
                .replace(".csv", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "_");

        String tableName = "dataset_" + fileName;

        Map<String, String> columnTypes = new HashMap<>();

        for (String column : headers.keySet()) {
            columnTypes.put(column, "TEXT");
        }

        for (CSVRecord record : parser) {

            for (String column : headers.keySet()) {

                String value = record.get(column);

                String detectedType = detectType(value);

                if (!detectedType.equals("TEXT")) {
                    columnTypes.put(column, detectedType);
                }
            }

            break; // just inspect first row
        }

        createTable(columnTypes, tableName);

        insertRows(parser, headers, tableName);
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

//    private void createTable(Map<String, Integer> headers, String tableName) {
//
//        StringBuilder sql = new StringBuilder("CREATE TABLE " + tableName + " (");
//
////        for (String column : headers.keySet()) {
////
////            sql.append(column).append(" TEXT,");
////        }
//
//        for (String column : headers.keySet()) {
//
//            String safeColumn = column
//                    .trim()
//                    .toLowerCase()
//                    .replaceAll("[^a-z0-9]", "_");
//
//            sql.append(safeColumn).append(" TEXT,");
//        }
//        sql.deleteCharAt(sql.length() - 1);
//
//        sql.append(")");
//
//        jdbcTemplate.execute(sql.toString());
//    }


    private void insertRows(CSVParser parser,
                            Map<String,Integer> headers,
                            String tableName) {

        for (CSVRecord record : parser) {

            StringBuilder columns = new StringBuilder();
            StringBuilder values = new StringBuilder();

            for (String column : headers.keySet()) {

                columns.append(column).append(",");
                values.append("'")
                        .append(record.get(column).replace("'", "''"))
                        .append("'")
                        .append(",");
            }

            columns.deleteCharAt(columns.length()-1);
            values.deleteCharAt(values.length()-1);

            String sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + values + ")";

            jdbcTemplate.execute(sql);
        }
    }

    private String detectType(String value) {

        if (value == null || value.isEmpty()) {
            return "TEXT";
        }

        if (value.matches("-?\\d+")) {
            return "INTEGER";
        }

        if (value.matches("-?\\d+\\.\\d+")) {
            return "DOUBLE PRECISION";
        }

        if (value.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return "DATE";
        }

        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return "BOOLEAN";
        }

        return "TEXT";
    }


}
