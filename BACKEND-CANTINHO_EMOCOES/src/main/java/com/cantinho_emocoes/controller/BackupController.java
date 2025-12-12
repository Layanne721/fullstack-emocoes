package com.cantinho_emocoes.controller;

import com.cantinho_emocoes.service.BackupService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/backup")
public class BackupController {

    private final BackupService backupService;

    public BackupController(BackupService backupService) {
        this.backupService = backupService;
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadBackup() {
        try {
            File backupFile = backupService.gerarBackup();
            FileSystemResource resource = new FileSystemResource(backupFile);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + backupFile.getName());
            headers.add(HttpHeaders.CONTENT_TYPE, "application/sql");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(backupFile.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/restore")
    public ResponseEntity<?> restoreBackup(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Arquivo vazio."));
        }
        
        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".sql") && !fileName.endsWith(".dum") && !fileName.endsWith(".dump"))) {
             return ResponseEntity.badRequest().body(Map.of("error", "Formato inválido. Use .sql ou .dum"));
        }

        try {
            backupService.restaurarBackup(file);
            return ResponseEntity.ok(Map.of("message", "Banco restaurado com sucesso!"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Erro ao restaurar: " + e.getMessage()));
        }
    }
}