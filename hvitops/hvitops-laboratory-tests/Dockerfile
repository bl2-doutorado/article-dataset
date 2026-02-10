FROM gradle:8-jdk17 as builder

WORKDIR /app
COPY gradle.properties build.gradle settings.gradle ./
COPY src ./src

RUN gradle build -x test

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY --from=builder /app/build/quarkus-app .
EXPOSE 8082

ENTRYPOINT ["java", "-jar", "quarkus-run.jar"]
