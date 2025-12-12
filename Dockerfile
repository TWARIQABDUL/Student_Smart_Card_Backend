# Stage 1: Build the application
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Copy maven wrapper and pom.xml first to cache dependencies
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline

# Copy the source code and build
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/student_management-0.0.1-SNAPSHOT.jar app.jar

# Render sets a PORT environment variable. 
# We expose it (defaulting to 8080 if not set) and pass it to the app.
ENV PORT=8080
EXPOSE ${PORT}

# Run the jar, forcing Spring Boot to use Render's port
ENTRYPOINT ["java", "-Dserver.port=${PORT}", "-jar", "app.jar"]