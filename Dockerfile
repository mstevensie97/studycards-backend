# ==== Etapa 1: compilar el proyecto con Maven ====
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Primero solo el pom.xml: así Docker reutiliza la descarga de dependencias
# cuando solo cambia el código.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q package -DskipTests

# ==== Etapa 2: imagen liviana solo para ejecutar ====
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/quarkus-app/ ./quarkus-app/

# Render gratis tiene poca memoria (512 MB): limitamos el heap de Java.
ENV JAVA_OPTS="-Xmx350m"

# El puerto lo define la variable PORT que Render asigna (ver application.properties).
EXPOSE 8080
CMD ["sh", "-c", "java $JAVA_OPTS -jar quarkus-app/quarkus-run.jar"]