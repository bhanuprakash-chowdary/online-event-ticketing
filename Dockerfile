FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/online-event-ticketing-0.0.1-SNAPSHOT.jar app.jar
# EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Dserver.port=${SERVER_PORT}", "app.jar"]