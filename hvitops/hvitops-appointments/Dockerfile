FROM gradle:8-jdk21 as builder

WORKDIR /app
COPY build.gradle settings.gradle ./
COPY src ./src

RUN gradle build -x test

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
