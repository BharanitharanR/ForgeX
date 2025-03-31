package com.batty.forgex.ingestor.serviceGenerator;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.UUID;

import com.batty.forgex.ingestor.pojo.MicroserviceRequest;
import org.springframework.stereotype.Service;

@Service
public class MicroserviceManager {

    private static final String BASE_DIR = "/tmp/microservices/";

    public String createAndRunService(MicroserviceRequest request) throws Exception {
        String serviceName = request.getName();
        String serviceDir = BASE_DIR + serviceName + "-" + UUID.randomUUID();

        // 1. Generate Project
        generateSpringBootService(serviceName, serviceDir);

        // 2. Generate Entity, Service, and Controller
        generateEntity(serviceDir, request);
        generateController(serviceDir, request);

        // 3. Compile and Package JAR
        compileService(serviceDir);

        // 4. Create Docker Image and Run
        String imageName = "forgex/" + serviceName.toLowerCase();
        createDockerImage(serviceDir, imageName);
        String serviceUrl = runDockerContainer(imageName);

        return serviceUrl;
    }

    private void generateSpringBootService(String serviceName, String outputDir) throws Exception {
        String url = "https://start.spring.io/starter.zip?" +
                "type=maven-project&language=java&bootVersion=3.3.0&" +
                "baseDir=" + serviceName + "&groupId=com.forgex&" +
                "artifactId=" + serviceName + "&name=" + serviceName +
                "&dependencies=web";

        // Create directories
        Files.createDirectories(Paths.get(outputDir));

        // Download and unzip
        try (InputStream in = new URL(url).openStream()) {

                Path zipPath = Paths.get(outputDir + ".zip");
                Files.copy(in, zipPath, StandardCopyOption.REPLACE_EXISTING);

                // 2. Unzip the project
                Process unzipProcess = new ProcessBuilder("unzip", zipPath.toString(), "-d", outputDir)
                        .inheritIO()
                        .start();
                unzipProcess.waitFor();

                // 3. Move the project contents to the correct directory
                String nestedDir = outputDir + "/" + serviceName;  // Nested directory where files are extracted
                if (Files.exists(Paths.get(nestedDir))) {
                    Files.walk(Paths.get(nestedDir))
                            .forEach(source -> {
                                try {
                                    Path destination = Paths.get(outputDir).resolve(Paths.get(nestedDir).relativize(source));
                                    if (Files.isDirectory(source)) {
                                        Files.createDirectories(destination);
                                    } else {
                                        Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
                                    }
                                } catch (Exception e) {
                                    throw new RuntimeException("Failed to move project files: " + e.getMessage(), e);
                                }
                            });

                    // 4. Delete the nested directory
                    Files.walk(Paths.get(nestedDir))
                            .sorted((a, b) -> b.compareTo(a))  // Delete children first
                            .forEach(path -> {
                                try {
                                    Files.delete(path);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                }

                // 5. Clean up ZIP file
                Files.delete(zipPath);
            }
    }

    private void generateEntity(String serviceDir, MicroserviceRequest request) throws IOException {
        String entityCode = """
                package com.forgex.POSService;
                                
                import org.springframework.boot.SpringApplication;
                import org.springframework.boot.autoconfigure.SpringBootApplication;
                @SpringBootApplication(scanBasePackages = "com.forgex")
                public class PosServiceApplication {
                            
                    public static void main(String[] args) {
                        SpringApplication.run(PosServiceApplication.class, args);
                    }
                }
                """.formatted(request.getEntityName());

        Path entityPath = Paths.get(serviceDir + "/src/main/java/com/forgex/POSService/");
        Files.createDirectories(entityPath);
        Files.writeString(entityPath.resolve(  "PosServiceApplication.java"), entityCode);
    }

    private void generateController(String serviceDir, MicroserviceRequest request) throws IOException {
        String className = request.getEntityName().substring(0, 1).toUpperCase() + request.getEntityName().substring(1);
        String controllerCode = """
                package com.forgex.controller;
                import org.springframework.web.bind.annotation.*;
                @RestController
                @RequestMapping("/api/Product")
                public class ProductController {

                    @GetMapping
                    public String getAll() {
                        return "hello";
                    }
                }
                """.formatted(request.getEntityName(), request.getEntityName().toLowerCase(),
                request.getEntityName(), request.getEntityName(), request.getEntityName());

        Path controllerPath = Paths.get(serviceDir + "/src/main/java/com/forgex/controller/");
        Files.createDirectories(controllerPath);
        Files.writeString(controllerPath.resolve("ProductController.java"), controllerCode);
    }

    private void compileService(String serviceDir) throws Exception {
        ProcessBuilder builder = new ProcessBuilder("mvn", "clean", "install","-DskipTests")
                .directory(new File(serviceDir))
                .inheritIO();
        Process process = builder.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Failed to compile the service!");
        }
    }

    private void createDockerImage(String serviceDir, String imageName) throws Exception {
        // Create Dockerfile
        String dockerfile = """
                FROM bellsoft/liberica-openjdk-alpine-musl:21-cds
                VOLUME /tmp
                COPY target/*.jar app.jar
                ENTRYPOINT ["java", "-jar", "-Dserver.port=9595","/app.jar"]
                """;

        Path dockerPath = Paths.get(serviceDir + "/Dockerfile");
        Files.writeString(dockerPath, dockerfile);

        // Build Docker Image
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
        int port = 9595 ;  // Dynamic port

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
