package com.glitchers.backend.controller;

import com.glitchers.backend.dto.ApiResponseDTO;
import com.glitchers.backend.dto.FileStorageDto;
import com.glitchers.backend.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/filesapi")
public class FileManagerController {

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/upload-file")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String tablename = fileStorageService.saveFile(file);
            return ResponseEntity.ok(new FileStorageDto("file stored successfully",tablename));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(new ApiResponseDTO("file not uploaded"));
        }
    }

    @GetMapping("/metadata/{tableName}")
    public ResponseEntity<?> getMetadata(@PathVariable String tableName) {

        try
        {
            List<Map<String, Object>> tableMetadata = fileStorageService.getTableMetadata(tableName);
            return ResponseEntity.ok(tableMetadata);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponseDTO("error "+ e.getMessage()));
        }
    }

}
