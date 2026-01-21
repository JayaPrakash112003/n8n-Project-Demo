# Use official Playwright Java image
FROM mcr.microsoft.com/playwright/java:v1.40.0-focal

# Set working directory
WORKDIR /app

# Install Maven
RUN apt-get update && apt-get install -y maven

# Copy pom
COPY pom.xml ./

# Resolve dependencies
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build the application
RUN mvn package -DskipTests

# Expose the port
EXPOSE 8080

# Command to run the application
CMD ["sh", "-c", "java -jar target/zepto-automation-0.0.1-SNAPSHOT.jar"]
