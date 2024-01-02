# Dockerfile para compilar y ejecutar la aplicación Spring Boot con JDK Alpine
FROM openjdk:17 as builder

WORKDIR /app

# Copiar el código fuente y el archivo pom.xml
COPY src ./src
COPY pom.xml .
COPY mvnw .
COPY .mvn/wrapper/ .mvn/wrapper/

# Dar permisos de ejecución al script
RUN chmod +x mvnw

# Empaquetar la aplicación
RUN ./mvnw clean package -DskipTests

# Segunda etapa del Dockerfile
FROM openjdk:17-jdk-alpine

WORKDIR /app

# Copiar el JAR generado desde la etapa de compilación
COPY --from=builder /app/target/soulsyncbot.jar /app/app.jar

# Comando para ejecutar la aplicación Spring Boot
CMD ["java", "-jar", "/app/app.jar"]
