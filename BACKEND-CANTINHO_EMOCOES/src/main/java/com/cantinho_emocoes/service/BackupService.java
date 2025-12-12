package com.cantinho_emocoes.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class BackupService {

    private static final Logger logger = LoggerFactory.getLogger(BackupService.class);

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    /**
     * Gera um backup SQL (Texto) para download.
     */
    public File gerarBackup() throws IOException, InterruptedException {
        DatabaseConfig config = parseDatabaseConfig(dbUrl);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File backupFile = Files.createTempFile("backup_cantinho_" + timestamp + "_", ".sql").toFile();

        ProcessBuilder pb = new ProcessBuilder(
            "pg_dump", "-h", config.host, "-p", config.port, "-U", dbUser, "-d", config.dbName,
            "--clean", "--if-exists", "--no-owner", "--no-acl", "-f", backupFile.getAbsolutePath()
        );
        
        Map<String, String> env = pb.environment();
        if (dbPassword != null && !dbPassword.isEmpty()) {
            env.put("PGPASSWORD", dbPassword);
        }

        logger.info("Iniciando geração de backup SQL: {}", backupFile.getAbsolutePath());
        executarProcesso(pb, "PG_DUMP");
        return backupFile;
    }

    /**
     * Restaura backup, detectando automaticamente se é SQL (texto) ou DUMP (binário).
     */
    public void restaurarBackup(MultipartFile file) throws IOException, InterruptedException {
        DatabaseConfig config = parseDatabaseConfig(dbUrl);
        String originalName = file.getOriginalFilename();
        if (originalName == null) originalName = "backup.dump";

        // Lógica de detecção: Se termina em .sql é texto (psql), senão é binário (pg_restore)
        boolean isSql = originalName.toLowerCase().endsWith(".sql");
        Path tempFile = Files.createTempFile("restore_", isSql ? ".sql" : ".dump");

        try {
            file.transferTo(tempFile);
            logger.info("Iniciando restauração de '{}'. Modo detectado: {}", originalName, isSql ? "PSQL (Texto)" : "PG_RESTORE (Binário)");

            ProcessBuilder pb;
            if (isSql) {
                // Modo Texto (.sql) - Usa psql
                pb = new ProcessBuilder(
                    "psql", "-h", config.host, "-p", config.port, "-U", dbUser, "-d", config.dbName, 
                    "-f", tempFile.toAbsolutePath().toString()
                );
            } else {
                // Modo Binário (.dump, .backup) - Usa pg_restore
                pb = new ProcessBuilder(
                    "pg_restore", "-h", config.host, "-p", config.port, "-U", dbUser, "-d", config.dbName,
                    "--clean", "--if-exists", "--no-owner", "--no-acl", "--verbose", 
                    tempFile.toAbsolutePath().toString()
                );
            }

            Map<String, String> env = pb.environment();
            if (dbPassword != null && !dbPassword.isEmpty()) {
                env.put("PGPASSWORD", dbPassword);
            }

            executarProcesso(pb, isSql ? "PSQL" : "PG_RESTORE");
            logger.info("Restauração concluída com sucesso.");

        } finally {
            try { Files.deleteIfExists(tempFile); } catch (Exception e) { logger.warn("Falha ao limpar temp: {}", e.getMessage()); }
        }
    }

    private void executarProcesso(ProcessBuilder pb, String nomeProcesso) throws IOException, InterruptedException {
        Process process = pb.start();
        
        // Lê logs em threads separadas para não travar
        StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream());
        StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream());
        errorGobbler.start();
        outputGobbler.start();

        boolean finished = process.waitFor(10, TimeUnit.MINUTES);
        if (!finished) {
            process.destroy();
            throw new IOException("Timeout: O processo " + nomeProcesso + " demorou muito.");
        }

        int exitCode = process.exitValue();
        // pg_restore retorna 1 para avisos (warnings) não fatais. Consideramos sucesso.
        boolean sucesso = (exitCode == 0) || (nomeProcesso.equals("PG_RESTORE") && exitCode == 1);

        if (!sucesso) {
            String erros = errorGobbler.getOutput();
            logger.error("Erro crítico no {}: {}", nomeProcesso, erros);
            throw new IOException("Falha no " + nomeProcesso + " (Exit " + exitCode + "). Detalhes: " + erros);
        }
    }

    private DatabaseConfig parseDatabaseConfig(String url) {
        try {
            String clean = url.replace("jdbc:postgresql://", "");
            if (clean.contains("?")) clean = clean.substring(0, clean.indexOf("?"));
            String[] parts = clean.split("/");
            if (parts.length < 2) return new DatabaseConfig("db", "5432", "cantinho_db");
            
            String host = parts[0];
            String port = "5432";
            if (host.contains(":")) {
                String[] hp = host.split(":");
                host = hp[0];
                port = hp[1];
            }
            return new DatabaseConfig(host, port, parts[1]);
        } catch (Exception e) {
            return new DatabaseConfig("db", "5432", "cantinho_db");
        }
    }

    private record DatabaseConfig(String host, String port, String dbName) {}

    // Classe auxiliar para ler as saídas do terminal sem travar o Java
    private static class StreamGobbler extends Thread {
        private final java.io.InputStream is;
        private final StringBuilder output = new StringBuilder();
        StreamGobbler(java.io.InputStream is) { this.is = is; }
        public void run() {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = br.readLine()) != null) output.append(line).append("\n");
            } catch (IOException e) {}
        }
        public String getOutput() { return output.toString(); }
    }
}