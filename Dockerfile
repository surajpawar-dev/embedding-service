FROM eclipse-temurin:21-jre

WORKDIR /app
COPY target/embedding-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
