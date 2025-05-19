FROM openjdk:17
WORKDIR /app
COPY build/libs/backend-0.0.1-SNAPSHOT.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]