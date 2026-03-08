package com.glitchers.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@Service
public class FileStorageService {

    @Value("${upload.storage.directory}")
    public String STORAGE_DIRECTORY;


    public void saveFile(MultipartFile fileToSave) throws IOException {
        if (fileToSave == null) {
            throw new NullPointerException("fileToSave is null");
        }
        var targetFile = new File(STORAGE_DIRECTORY + File.separator + fileToSave.getOriginalFilename());
        if (!Objects.equals(targetFile.getParent(), STORAGE_DIRECTORY)) {
            throw new SecurityException("Unsupported filename!");
        }
        Files.copy(fileToSave.getInputStream(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}
