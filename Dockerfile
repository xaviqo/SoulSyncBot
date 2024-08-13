# Etapa 1: Construcción
FROM amazoncorretto:21 as build

# Establecer el directorio de trabajo en la raíz del proyecto dentro del contenedor
WORKDIR /app

# Copiar todo el proyecto al contenedor
COPY . .

# Dar permisos de ejecución a mvnw
RUN chmod +x ./mvnw

# Construir el módulo backend usando Maven Wrapper
RUN ./mvnw clean package -DskipTests

# Etapa 2: Imagen mínima para producción
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiar el JAR generado desde la etapa de construcción
COPY --from=build /app/backend/target/*.jar app.jar

# Exponer el puerto en el que corre la aplicación
EXPOSE 6743

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
