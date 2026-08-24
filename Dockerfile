# Run stage
FROM eclipse-temurin:21-jre
VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java -jar /app/app.jar"]
EXPOSE 8080
