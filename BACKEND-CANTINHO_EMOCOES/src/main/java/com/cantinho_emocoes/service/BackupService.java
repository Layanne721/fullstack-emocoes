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
import java.util.ArrayList;
import java.util.List;
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
     * Gera um backup completo do banco de dados usando pg_dump em formato SQL (texto plano).
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
        env.put("PGPASSWORD", dbPassword);

        logger.info("Iniciando backup (SQL) do banco: {} em {}", config.dbName, config.host);
        
        executarProcesso(pb, "PG_DUMP");

        logger.info("Backup gerado com sucesso: {}", backupFile.getAbsolutePath());
        return backupFile;
    }

    /**
     * Restaura um backup, detectando se é .sql (texto) ou .dump (binário).
     */
    public void restaurarBackup(MultipartFile file) throws IOException, InterruptedException {
        DatabaseConfig config = parseDatabaseConfig(dbUrl);
        String originalName = file.getOriginalFilename();
        
        if (originalName == null) originalName = "backup.sql";
        boolean isCustomFormat = originalName.endsWith(".dump") || originalName.endsWith(".backup");

        // Mantém a extensão original para clareza no arquivo temporário
        String ext = isCustomFormat ? ".dump" : ".sql";
        Path tempFile = Files.createTempFile("restore_", ext);
        
        try {
            file.transferTo(tempFile);
            logger.info("Arquivo recebido: {} (Formato Custom/Binário: {})", originalName, isCustomFormat);

            ProcessBuilder pb;
            if (isCustomFormat) {
                // Usa pg_restore para arquivos binários (.dump)
                // --clean: limpa o banco antes
                // --if-exists: evita erro se não existir
                // -d: conecta no banco
                logger.info("Usando pg_restore para arquivo binário...");
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
            } else {
                // Usa psql para arquivos de texto (.sql)
                logger.info("Usando psql para arquivo SQL plano...");
                pb = new ProcessBuilder(
                    "psql",
                    "-h", config.host,
                    "-p", config.port,
                    "-U", dbUser,
                    "-d", config.dbName,
                    "-f", tempFile.toAbsolutePath().toString()
                );
            }

            Map<String, String> env = pb.environment();
            env.put("PGPASSWORD", dbPassword);

            executarProcesso(pb, isCustomFormat ? "PG_RESTORE" : "PSQL");
            
            logger.info("Restauração concluída com sucesso.");

        } finally {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException e) {
                logger.warn("Não foi possível deletar arquivo temporário: {}", e.getMessage());
            }
        }
    }

    /**
     * Executa o processo e trata logs e erros.
     */
    private void executarProcesso(ProcessBuilder pb, String logPrefix) throws IOException, InterruptedException {
        Process process = pb.start();
        
        readStream(process.getErrorStream(), logPrefix + "_ERR");
        readStream(process.getInputStream(), logPrefix + "_OUT");

        boolean finished = process.waitFor(10, TimeUnit.MINUTES);

        if (!finished) {
            process.destroy();
            throw new IOException("Tempo limite excedido (Timeout) no processo " + logPrefix);
        }

        // pg_restore pode retornar códigos de aviso (ex: 1) que não são erros fatais.
        // Geralmente 0 = sucesso.
        if (process.exitValue() != 0) {
            // Se for pg_restore, as vezes warnings (código 1) são aceitáveis, mas erros críticos param tudo.
            // Para segurança, vamos logar como erro, mas lançar exceção apenas se for psql ou código alto.
            logger.warn("Processo {} terminou com código de saída: {}", logPrefix, process.exitValue());
            
            if (!logPrefix.equals("PG_RESTORE") || process.exitValue() > 1) {
                 throw new IOException("Falha no processo " + logPrefix + ". Código: " + process.exitValue());
            }
        }
    }

    private void readStream(java.io.InputStream inputStream, String prefix) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Logs detalhados apenas em debug para não poluir, ou info se necessário
                    logger.debug("[{}]: {}", prefix, line);
                }
            } catch (IOException e) {
                logger.error("Erro ao ler stream ({})", prefix, e);
            }
        }).start();
    }

    private DatabaseConfig parseDatabaseConfig(String url) {
        try {
            String cleanUrl = url.replace("jdbc:postgresql://", "");
            if (cleanUrl.contains("?")) {
                cleanUrl = cleanUrl.substring(0, cleanUrl.indexOf("?"));
            }
            String[] parts = cleanUrl.split("/");
            if (parts.length < 2) return new DatabaseConfig("db", "5432", "cantinho_db");

            String hostPort = parts[0];
            String dbName = parts[1];
            String host = hostPort.contains(":") ? hostPort.split(":")[0] : hostPort;
            String port = hostPort.contains(":") ? hostPort.split(":")[1] : "5432";

            return new DatabaseConfig(host, port, dbName);
        } catch (Exception e) {
            return new DatabaseConfig("db", "5432", "cantinho_db");
        }
    }

    private record DatabaseConfig(String host, String port, String dbName) {}
}