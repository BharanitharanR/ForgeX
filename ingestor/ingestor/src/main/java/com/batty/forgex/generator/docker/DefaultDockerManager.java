package com.batty.forgex.generator.docker;

import com.batty.forgex.generator.context.GenerationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class DefaultDockerManager implements DockerManager {
    private static final Logger log = LoggerFactory.getLogger(DefaultDockerManager.class);

    @Override
    public String createAndRunContainer(String serviceDir, GenerationContext context) {
        try {
            String imageName = "forgex/" + context.getServiceName().toLowerCase();
            createDockerImage(serviceDir, imageName);
            return runDockerContainer(imageName);
        } catch (Exception e) {
            log.error("Failed to create and run Docker container", e);
            throw new RuntimeException("Failed to create and run Docker container", e);
        }
    }

    private void createDockerImage(String serviceDir, String imageName) throws Exception {
        String dockerfile = """
                FROM bellsoft/liberica-openjdk-alpine-musl:21-cds
                VOLUME /tmp
                COPY target/*.jar app.jar
                ENTRYPOINT ["java", "-jar", "-Dserver.port=9595", "/app.jar"]
                """;

        Path dockerPath = Paths.get(serviceDir, "Dockerfile");
        Files.writeString(dockerPath, dockerfile);

        ProcessBuilder builder = new ProcessBuilder("docker", "build", "-t", imageName, ".")
                .directory(new File(serviceDir))
                .inheritIO();
        Process process = builder.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Failed to build Docker image!");
        }
    }

    private String runDockerContainer(String imageName) throws Exception {
        int port = 9595;  // Dynamic port

        ProcessBuilder builder = new ProcessBuilder(
                "docker", "run", "-d", "--network", "forgex-integrator", "-p", port + ":9595", imageName
        ).inheritIO();
        Process process = builder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Failed to start Docker container!");
        }

        // Wait for the service to be available
        Thread.sleep(5000);  // Wait for container startup

        return "http://localhost:" + port;
    }
} 