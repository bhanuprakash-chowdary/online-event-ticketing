FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/online-event-ticketing-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE ${SERVER_PORT}
ENTRYPOINT ["java", "-jar", "-Dserver.port=${SERVER_PORT}", "/app/app.jar"]