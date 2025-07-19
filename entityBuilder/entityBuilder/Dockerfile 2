# Use a lightweight JDK image
FROM bellsoft/liberica-openjdk-alpine-musl:21-cds

# Define a volume for temporary data
VOLUME /tmp

# Arguments for JAR file and JVM options
ARG JAR_FILE
ARG JVM_VALUES
ARG DEPENDENCY

# Copy the JAR file into the container
COPY ${JAR_FILE} /app.jar

COPY ${DEPENDENCY} /tmp


# Ensure Maven, Docker CLI, and required tools are available
RUN apk add --no-cache maven curl bash docker-cli docker-cli-buildx

RUN mkdir -p /root/.m2

# Install custom JARs into Maven local repo inside the container
RUN mvn install:install-file \
    -Dfile=/tmp/dependency/framework-datastore-0.0.1-SNAPSHOT.jar \
    -DgroupId=com.batty.forgex \
    -DartifactId=framework-datastore \
    -Dversion=0.0.1-SNAPSHOT \
    -Dpackaging=jar && \
    mvn install:install-file \
    -Dfile=/tmp/dependency/framework-utils-0.0.1-SNAPSHOT.jar \
    -DgroupId=com.batty.forgex \
    -DartifactId=framework-utils \
    -Dversion=0.0.1-SNAPSHOT \
    -Dpackaging=jar

RUN mvn install:install-file \
  -Dfile=/tmp/dependency/forgex-parent-1.0.0.pom \
  -DgroupId=com.batty.forgex \
  -DartifactId=forgex-parent \
  -Dversion=1.0.0 \
  -Dpackaging=pom


# Install OpenAPI Generator CLI
RUN curl -L https://repo1.maven.org/maven2/org/openapitools/openapi-generator-cli/7.4.0/openapi-generator-cli-7.4.0.jar -o /usr/local/bin/openapi-generator-cli.jar \
 && echo -e '#!/bin/sh\nexec java -jar /usr/local/bin/openapi-generator-cli.jar "$@"' > /usr/local/bin/openapi-generator-cli \
 && chmod +x /usr/local/bin/openapi-generator-cli





# Expose the application port
EXPOSE 8081

# Use entrypoint to run with Java runtime parameters
ENTRYPOINT ["sh", "-c", "java $JVM_VALUES -jar /app.jar"]
