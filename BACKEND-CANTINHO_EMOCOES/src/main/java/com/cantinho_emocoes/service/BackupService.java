package com.cantinho_emocoes.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class BackupService {

    private static final Logger log = LoggerFactory.getLogger(BackupService.class);

    // Caminho específico para o seu Windows Local (conforme você informou)
    private static final String WINDOWS_PG_PATH = "C:\\pg18\\pgsql\\bin\\";

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    private Map<String, String> getDbInfo() {
        String cleanUrl = dbUrl.replace("jdbc:postgresql://", "");
        int queryParamIndex = cleanUrl.indexOf("?");
        if (queryParamIndex != -1) {
            cleanUrl = cleanUrl.substring(0, queryParamIndex);
        }
        
        String[] hostPortDb = cleanUrl.split("/");
        String[] hostPort = hostPortDb[0].split(":");
        String dbName = hostPortDb.length > 1 ? hostPortDb[1] : "cantinho"; // Nome padrão caso falhe o split

        return Map.of(
            "host", hostPort[0],
            "port", hostPort.length > 1 ? hostPort[1] : "5432",
            "db", dbName
        );
    }

    private String getCommand(String command) {
        String os = System.getProperty("os.name").toLowerCase();
        // Se for Windows, usa o caminho fixo. Se for Linux (VPS), usa o comando direto.
        if (os.contains("win")) {
            return WINDOWS_PG_PATH + command + ".exe";
        } else {
            return command;
        }
    }

    public File gerarBackup() throws IOException, InterruptedException {
        Map<String, String> info = getDbInfo();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File backupFile = File.createTempFile("backup_cantinho_" + timestamp + "_", ".sql");

        String pgDumpCmd = getCommand("pg_dump");

        ProcessBuilder pb = new ProcessBuilder(
            pgDumpCmd,
            "-h", info.get("host"),
            "-p", info.get("port"),
            "-U", dbUser,
            "--no-owner",
            "--no-acl",
            "--clean",
            "--if-exists",
            "-f", backupFile.getAbsolutePath(),
            info.get("db")
        );

        pb.environment().put("PGPASSWORD", dbPassword);
        pb.redirectErrorStream(true);

        log.info("Iniciando backup no OS [{}] usando: {}", System.getProperty("os.name"), pgDumpCmd);
        
        Process process = pb.start();
        
        // Lê logs para debug
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.contains("pg_dump: warning")) { // Ignora warnings simples
                    log.info("Backup Output: {}", line);
                }
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Erro no pg_dump. Código: " + exitCode + ". Verifique se o postgresql-client está instalado.");
        }

        return backupFile;
    }

    public void restaurarBackup(MultipartFile file) throws IOException, InterruptedException {
        Map<String, String> info = getDbInfo();
        
        Path tempFile = Files.createTempFile("restore_", ".sql");
        file.transferTo(tempFile.toFile());

        String psqlCmd = getCommand("psql");

        ProcessBuilder pb = new ProcessBuilder(
            psqlCmd,
            "-h", info.get("host"),
            "-p", info.get("port"),
            "-U", dbUser,
            "-d", info.get("db"),
            "-f", tempFile.toAbsolutePath().toString()
        );

        pb.environment().put("PGPASSWORD", dbPassword);
        pb.redirectErrorStream(true);

        log.info("Iniciando restore no OS [{}] usando: {}", System.getProperty("os.name"), psqlCmd);

        Process process = pb.start();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.info("Restore Output: {}", line);
            }
        }

        int exitCode = process.waitFor();
        Files.deleteIfExists(tempFile);

        if (exitCode != 0) {
            throw new RuntimeException("Erro no psql. Código: " + exitCode);
        }
    }
}