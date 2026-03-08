package com.glitchers.backend.controller;

import com.glitchers.backend.dto.ApiResponseDTO;
import com.glitchers.backend.dto.FileStorageDto;
import com.glitchers.backend.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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

}
