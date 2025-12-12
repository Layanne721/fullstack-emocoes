# --- Estágio 1: Build (Compila o código Java) ---
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copia configurações e baixa dependências
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o código e gera o .jar
COPY src ./src
RUN mvn clean package -DskipTests

# --- Estágio 2: Run (Imagem Final para rodar) ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Instala o cliente PostgreSQL (necessário para o BackupService funcionar)
RUN apk update && apk add --no-cache postgresql-client

# Pega o .jar gerado no Estágio 1 e copia para a imagem final
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]