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
     * Gera um backup completo do banco de dados usando pg_dump.
     * @return O arquivo .sql gerado.
     */
    public File gerarBackup() throws IOException, InterruptedException {
        DatabaseConfig config = parseDatabaseConfig(dbUrl);
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "backup_cantinho_" + timestamp + ".sql";
        File backupFile = Files.createTempFile("backup_", ".sql").toFile();

        // Comando: pg_dump -h host -p port -U user -d dbname --clean --if-exists -f arquivo.sql
        // --clean: Inclui comandos DROP antes de CREATE (bom para restore)
        // --if-exists: Evita erros se o objeto não existir ao dropar
        ProcessBuilder pb = new ProcessBuilder(
            "pg_dump",
            "-h", config.host,
            "-p", config.port,
            "-U", dbUser,
            "-d", config.dbName,
            "--clean",
            "--if-exists",
            "--no-owner", // Evita problemas de permissão ao restaurar em outro user
            "--no-acl",   // Evita problemas de privilégios
            "-f", backupFile.getAbsolutePath()
        );

        // Define a senha via variável de ambiente para não pedir interativamente
        Map<String, String> env = pb.environment();
        env.put("PGPASSWORD", dbPassword);

        logger.info("Iniciando backup do banco: {}", config.dbName);
        
        Process process = pb.start();
        
        // Captura logs de erro do processo (pg_dump escreve infos no stderr)
        readStream(process.getErrorStream());
        
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new IOException("Falha ao gerar backup. Código de saída: " + exitCode);
        }

        logger.info("Backup gerado com sucesso em: {}", backupFile.getAbsolutePath());
        return backupFile;
    }

    /**
     * Restaura um backup a partir de um arquivo SQL.
     */
    public void restaurarBackup(MultipartFile file) throws IOException, InterruptedException {
        DatabaseConfig config = parseDatabaseConfig(dbUrl);
        
        // Salva o upload em um arquivo temporário
        Path tempFile = Files.createTempFile("restore_", ".sql");
        file.transferTo(tempFile);

        // Comando: psql -h host -p port -U user -d dbname -f arquivo.sql
        ProcessBuilder pb = new ProcessBuilder(
            "psql",
            "-h", config.host,
            "-p", config.port,
            "-U", dbUser,
            "-d", config.dbName,
            "-f", tempFile.toAbsolutePath().toString()
        );

        Map<String, String> env = pb.environment();
        env.put("PGPASSWORD", dbPassword);

        logger.warn("Iniciando RESTAURAÇÃO do banco: {}. Isso pode sobrescrever dados!", config.dbName);

        Process process = pb.start();
        
        // Captura logs
        readStream(process.getErrorStream());
        readStream(process.getInputStream());

        int exitCode = process.waitFor();
        
        // Deleta o arquivo temporário
        Files.deleteIfExists(tempFile);

        if (exitCode != 0) {
            throw new IOException("Falha ao restaurar backup. Código de saída: " + exitCode);
        }
        
        logger.info("Restauração concluída com sucesso.");
    }

    // --- Utilitários ---

    private void readStream(java.io.InputStream inputStream) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.debug("[PG CMD]: {}", line);
                }
            } catch (IOException e) {
                logger.error("Erro ao ler stream do processo", e);
            }
        }).start();
    }

    private DatabaseConfig parseDatabaseConfig(String url) {
        // Exemplo JDBC: jdbc:postgresql://db:5432/cantinho_db
        // Exemplo Render: jdbc:postgresql://host.render.com:5432/db?ssl...
        
        String cleanUrl = url.replace("jdbc:postgresql://", "");
        
        // Remove parâmetros (?sslmode=...)
        if (cleanUrl.contains("?")) {
            cleanUrl = cleanUrl.substring(0, cleanUrl.indexOf("?"));
        }

        String[] parts = cleanUrl.split("/");
        String hostPort = parts[0];
        String dbName = parts[1];

        String[] hostParts = hostPort.split(":");
        String host = hostParts[0];
        String port = hostParts.length > 1 ? hostParts[1] : "5432";

        return new DatabaseConfig(host, port, dbName);
    }

    private record DatabaseConfig(String host, String port, String dbName) {}
}