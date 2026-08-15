# ==========================================
# Stage 1: Build the Application using Maven & Java 21
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy pom.xml first to cache Maven dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source files and package into a JAR
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# Stage 2: Create Lightweight Runtime Image with JRE 21
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy compiled JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# API Gateway runs on port 8080
EXPOSE 8080

# Execute the application
ENTRYPOINT ["java", "-jar", "app.jar"]