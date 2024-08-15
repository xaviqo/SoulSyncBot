FROM amazoncorretto:21 as build

WORKDIR /app

COPY . .

RUN chmod +x ./mvnw

RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/backend/target/*.jar app.jar

EXPOSE 6743

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
