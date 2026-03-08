package com.glitchers.backend.dto;

public class FileStorageDto {
    private String message;
    private String tablename;

    public FileStorageDto() {
    }

    public FileStorageDto(String message, String tablename) {
        this.message = message;
        this.tablename = tablename;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTablename() {
        return tablename;
    }

    public void setTablename(String tablename) {
        this.tablename = tablename;
    }
}
