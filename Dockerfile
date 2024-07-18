FROM eclipse-temurin:21.0.3_9-jdk
LABEL authors="ANSH KUSHWAHA"

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline

COPY src ./src

CMD ["./mvnw", "spring-boot:run"]