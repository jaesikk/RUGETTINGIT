FROM openjdk:11-jdk AS build
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src
COPY src/main/resources/aws.yml src/main/resources/aws.yml
COPY src/main/resources/production.yml src/main/resources/application.yml

RUN chmod +x gradlew
RUN ["./gradlew", "clean", "build", "--stacktrace", "--exclude-task", "test"]

FROM openjdk:11-jre-slim
COPY --from=build /app/build/libs/aipjt-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT java -jar app.jar
