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
import java.util.stream.Collectors;

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
     * Gera backup SQL.
     */
    public File gerarBackup() throws IOException, InterruptedException {
        DatabaseConfig config = parseDatabaseConfig(dbUrl);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File backupFile = Files.createTempFile("backup_cantinho_" + timestamp + "_", ".sql").toFile();

        ProcessBuilder pb = new ProcessBuilder(
            "pg_dump",
            "-h", config.host,
            "-p", config.port,
            "-U", dbUser,
            "-d", config.dbName,
            "--clean",
            "--if-exists",
            "--no-owner",
            "--no-acl",
            "-f", backupFile.getAbsolutePath()
        );

        Map<String, String> env = pb.environment();
        if (dbPassword != null && !dbPassword.isEmpty()) {
            env.put("PGPASSWORD", dbPassword);
        }

        logger.info("Iniciando backup em: {}", backupFile.getAbsolutePath());
        executarProcesso(pb, "PG_DUMP");

        return backupFile;
    }

    /**
     * Restaura backup (SQL ou Custom/Dump).
     */
    public void restaurarBackup(MultipartFile file) throws IOException, InterruptedException {
        DatabaseConfig config = parseDatabaseConfig(dbUrl);
        String originalName = file.getOriginalFilename();
        if (originalName == null) originalName = "backup.dump";

        // Detecção simples: se termina em .sql é texto, senão tentamos como binário (pg_restore)
        boolean isSql = originalName.toLowerCase().endsWith(".sql");
        
        Path tempFile = Files.createTempFile("restore_", isSql ? ".sql" : ".dump");

        try {
            file.transferTo(tempFile);
            logger.info("Iniciando restauração. Arquivo: {} (Modo: {})", originalName, isSql ? "PSQL (Texto)" : "PG_RESTORE (Binário)");

            ProcessBuilder pb;
            if (isSql) {
                // Modo Texto (.sql)
                pb = new ProcessBuilder(
                    "psql",
                    "-h", config.host,
                    "-p", config.port,
                    "-U", dbUser,
                    "-d", config.dbName,
                    "-f", tempFile.toAbsolutePath().toString()
                );
            } else {
                // Modo Binário/Custom (.dump, .backup, etc)
                pb = new ProcessBuilder(
                    "pg_restore",
                    "-h", config.host,
                    "-p", config.port,
                    "-U", dbUser,
                    "-d", config.dbName,
                    "--clean",
                    "--if-exists",
                    "--no-owner",
                    "--no-acl",
                    "--verbose",
                    tempFile.toAbsolutePath().toString()
                );
            }

            Map<String, String> env = pb.environment();
            if (dbPassword != null && !dbPassword.isEmpty()) {
                env.put("PGPASSWORD", dbPassword);
            }

            executarProcesso(pb, isSql ? "PSQL" : "PG_RESTORE");
            logger.info("Restauração concluída.");

        } finally {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException e) {
                logger.warn("Falha ao deletar arquivo temporário: {}", e.getMessage());
            }
        }
    }

    /**
     * Executa o processo e CAPTURA O ERRO para exibir no log/exception.
     */
    private void executarProcesso(ProcessBuilder pb, String nomeProcesso) throws IOException, InterruptedException {
        // Redireciona erro para stdout para capturar tudo junto ou processa streams separadas
        Process process = pb.start();

        // Captura as saídas em Threads separadas para não bloquear
        StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream());
        StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream());

        errorGobbler.start();
        outputGobbler.start();

        boolean finished = process.waitFor(5, TimeUnit.MINUTES);

        if (!finished) {
            process.destroy();
            throw new IOException("Timeout: O processo " + nomeProcesso + " demorou muito e foi cancelado.");
        }

        int exitCode = process.exitValue();
        
        // pg_restore retorna 0 (sucesso) ou 1 (aviso/sucesso parcial) ou >1 (erros)
        // Vamos ser tolerantes com warnings (1) no pg_restore
        boolean sucesso = (exitCode == 0) || (nomeProcesso.equals("PG_RESTORE") && exitCode == 1);

        if (!sucesso) {
            String erros = errorGobbler.getOutput();
            logger.error("Erro no {}: {}", nomeProcesso, erros);
            
            // Lança exceção com a mensagem REAL do banco de dados
            throw new IOException("Falha no " + nomeProcesso + " (Cód " + exitCode + "). Detalhes: " + erros);
        }
    }

    private DatabaseConfig parseDatabaseConfig(String url) {
        try {
            // Remove jdbc:postgresql://
            String clean = url.replace("jdbc:postgresql://", "");
            // Remove params (?...)
            if (clean.contains("?")) clean = clean.substring(0, clean.indexOf("?"));
            
            String[] parts = clean.split("/");
            if (parts.length < 2) return new DatabaseConfig("db", "5432", "cantinho_db"); // Fallback

            String hostPort = parts[0];
            String dbName = parts[1];
            
            String host = hostPort;
            String port = "5432";
            
            if (hostPort.contains(":")) {
                String[] hp = hostPort.split(":");
                host = hp[0];
                port = hp[1];
            }
            return new DatabaseConfig(host, port, dbName);
        } catch (Exception e) {
            logger.error("Erro ao processar URL JDBC: {}", url);
            return new DatabaseConfig("db", "5432", "cantinho_db");
        }
    }

    private record DatabaseConfig(String host, String port, String dbName) {}

    // Classe auxiliar para ler streams sem travar
    private static class StreamGobbler extends Thread {
        private final java.io.InputStream inputStream;
        private final StringBuilder output = new StringBuilder();

        public StreamGobbler(java.io.InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            } catch (IOException e) {
                // Ignora erro de leitura de stream fechada
            }
        }

        public String getOutput() {
            return output.toString();
        }
    }
}