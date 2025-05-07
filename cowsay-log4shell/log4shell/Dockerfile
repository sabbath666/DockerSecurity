FROM openjdk:8u111-jdk
EXPOSE 8080
ADD target/log4shell-rce-0.0.1.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]