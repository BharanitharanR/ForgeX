# Use a lightweight JDK image
FROM bellsoft/liberica-openjdk-alpine-musl:21-cds

# Define a volume for temporary data
VOLUME /tmp

# Arguments for JAR file and JVM options
ARG JAR_FILE
ARG JVM_VALUES

# Ensure Maven is available for dynamic service creation
RUN apk add --no-cache maven docker-cli docker-cli-buildx


# Copy the JAR file into the container
COPY ${JAR_FILE} /app.jar

# Expose the application port
EXPOSE 8083

# Use entrypoint to run with Java runtime parameters
ENTRYPOINT ["sh", "-c", "java $JVM_VALUES -jar /app.jar"]
