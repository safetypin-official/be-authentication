FROM openjdk:21-jdk-slim

WORKDIR /app
COPY ./target/authentication-0.1.jar /app/app.jar
EXPOSE 8080
CMD ["java", "-Dhttp.proxyHost=http://proxy.cs.ui.ac.id", "-Dhttp.proxyPort=8080", "-Dhttps.proxyHost=http://proxy.cs.ui.ac.id", "-Dhttps.proxyPort=8080", "-jar", "app.jar"]
