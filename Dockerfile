FROM eclipse-temurin:21.0.3_9-jdk
LABEL authors="ANSH KUSHWAHA"

ARG JAR_FILE=target/*.jar
COPY ./target/CodeTracker-0.2.0.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]