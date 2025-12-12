FROM eclipse-temurin:17-jdk-alpine

# --- ADICIONE ESTAS DUAS LINHAS AQUI ---
RUN apk update && apk add --no-cache postgresql-client
# ---------------------------------------

VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]