# --- Estágio 1: Build (Construção) ---
    FROM maven:3.9-eclipse-temurin-17 AS build

    WORKDIR /app
    
    COPY pom.xml .
    RUN mvn dependency:go-offline
    
    COPY src ./src
    RUN mvn clean package -DskipTests
    
    # --- Estágio 2: Run (Execução) ---
    # Usamos a versão jammy (Ubuntu) para facilitar a instalação do postgresql-client
    FROM eclipse-temurin:17-jre-jammy
    
    WORKDIR /app
    
    # Instala o cliente do PostgreSQL para permitir backup/restore
    RUN apt-get update && apt-get install -y postgresql-client && rm -rf /var/lib/apt/lists/*
    
    COPY --from=build /app/target/cantinho_emocoes-0.0.1-SNAPSHOT.jar app.jar
    
    EXPOSE 8080
    
    ENTRYPOINT ["java", "-jar", "app.jar"]