# Сборка проекта
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Финальный образ
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/target/cowsay-0.0.1.jar cowsay.jar
RUN mkdir data
ENTRYPOINT ["java", "-jar", "cowsay.jar"]