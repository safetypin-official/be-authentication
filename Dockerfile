FROM openjdk:21-jdk-slim

WORKDIR /app
COPY ./authentication-0.1.jar /app/app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
