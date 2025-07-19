# Use a lightweight JDK image
FROM bellsoft/liberica-openjdk-alpine-musl:21-cds

# Define a volume for temporary data
VOLUME /tmp


# Copy the JAR file into the container
COPY ${JAR_FILE} /app.jar

# Expose the application port
EXPOSE 8761

# Use entrypoint to run with Java runtime parameters
ENTRYPOINT ["sh", "-c", "java $JVM_VALUES -jar /app.jar"]