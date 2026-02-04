# שלב בנייה
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# הורדת תלויות מראש לשיפור מהירות
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# שלב הרצה
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Environment variables will be passed at runtime
# Required: GEMINI_APIKEY
# Optional: SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]