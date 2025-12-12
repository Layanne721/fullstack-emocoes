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

    public File gerarBackup() throws IOException, InterruptedException {
        DatabaseConfig config = parseDatabaseConfig(dbUrl);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File backupFile = Files.createTempFile("backup_cantinho_" + timestamp + "_", ".sql").toFile();

        ProcessBuilder pb = new ProcessBuilder(
            "pg_dump", "-h", config.host, "-p", config.port, "-U", dbUser, "-d", config.dbName,
            "--clean", "--if-exists", "--no-owner", "--no-acl", "-f", backupFile.getAbsolutePath()
        );
        Map<String, String> env = pb.environment();
        if (dbPassword != null) env.put("PGPASSWORD", dbPassword);

        logger.info("Backup SQL iniciado: {}", backupFile.getAbsolutePath());
        executarProcesso(pb, "PG_DUMP");
        return backupFile;
    }

    public void restaurarBackup(MultipartFile file) throws IOException, InterruptedException {
        DatabaseConfig config = parseDatabaseConfig(dbUrl);
        String originalName = file.getOriginalFilename();
        if (originalName == null) originalName = "backup.dump";

        // Se terminar com .sql é texto, caso contrário é binário (dump)
        boolean isSql = originalName.toLowerCase().endsWith(".sql");
        Path tempFile = Files.createTempFile("restore_", isSql ? ".sql" : ".dump");

        try {
            file.transferTo(tempFile);
            logger.info("Restaurando {} (Modo: {})", originalName, isSql ? "PSQL" : "PG_RESTORE");

            ProcessBuilder pb;
            if (isSql) {
                pb = new ProcessBuilder("psql", "-h", config.host, "-p", config.port, "-U", dbUser, "-d", config.dbName, "-f", tempFile.toAbsolutePath().toString());
            } else {
                pb = new ProcessBuilder("pg_restore", "-h", config.host, "-p", config.port, "-U", dbUser, "-d", config.dbName,
                        "--clean", "--if-exists", "--no-owner", "--no-acl", "--verbose", tempFile.toAbsolutePath().toString());
            }

            Map<String, String> env = pb.environment();
            if (dbPassword != null) env.put("PGPASSWORD", dbPassword);

            executarProcesso(pb, isSql ? "PSQL" : "PG_RESTORE");
            logger.info("Restauração concluída.");
        } finally {
            try { Files.deleteIfExists(tempFile); } catch (Exception e) {}
        }
    }

    private void executarProcesso(ProcessBuilder pb, String nome) throws IOException, InterruptedException {
        Process process = pb.start();
        
        // Lê as saídas de erro e padrão em threads separadas para não travar
        StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream());
        StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream());
        errorGobbler.start();
        outputGobbler.start();

        boolean finished = process.waitFor(10, TimeUnit.MINUTES);
        if (!finished) {
            process.destroy();
            throw new IOException("Timeout: O processo " + nome + " demorou muito.");
        }

        // pg_restore retorna 1 em caso de Warnings (avisos), o que é aceitável.
        int exit = process.exitValue();
        if (exit != 0 && !(nome.equals("PG_RESTORE") && exit == 1)) {
            String erros = errorGobbler.getOutput();
            logger.error("Erro no {}: {}", nome, erros);
            // Lança o erro real para o usuário ver no frontend
            throw new IOException("Falha no " + nome + " (Exit " + exit + "): " + erros);
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