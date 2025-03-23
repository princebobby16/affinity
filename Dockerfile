FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# Copy Gradle wrapper and build files first for caching dependencies
COPY gradle ./gradle
COPY src ./
COPY build.gradle ./
COPY settings.gradle ./
COPY gradlew ./

# Grant execute permission to Gradle wrapper
RUN chmod +x ./gradlew

# Download dependencies
RUN ./gradlew dependencies --no-daemon

# Build the application
RUN ./gradlew clean bootJar --no-daemon --stacktrace

# Use a minimal JDK runtime for production
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose application port
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
