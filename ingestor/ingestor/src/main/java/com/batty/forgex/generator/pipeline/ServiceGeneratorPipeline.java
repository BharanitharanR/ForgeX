package com.batty.forgex.generator.pipeline;

import com.batty.forgex.generator.build.BuildManager;
import com.batty.forgex.generator.context.GenerationContext;
import com.batty.forgex.generator.docker.DockerManager;
import com.batty.forgex.generator.generator.CodeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Component
public class ServiceGeneratorPipeline {
    private static final Logger log = LoggerFactory.getLogger(ServiceGeneratorPipeline.class);
    private final List<CodeGenerator> generators;
    private final BuildManager buildManager;
    private final DockerManager dockerManager;

    @Autowired
    public ServiceGeneratorPipeline(List<CodeGenerator> generators, BuildManager buildManager, DockerManager dockerManager) {
        this.generators = generators;
        this.buildManager = buildManager;
        this.dockerManager = dockerManager;
    }

    public String generateService(GenerationContext context) {
        try {
            String serviceDir = createServiceDirectory(context.getServiceName());
            
            // Generate code
            for (CodeGenerator generator : generators) {
                generator.generate(serviceDir, context);
            }
            
            // Build service
            buildManager.build(serviceDir);
            
            // Create and run Docker container
            return dockerManager.createAndRunContainer(serviceDir, context);
        } catch (Exception e) {
            log.error("Failed to generate service", e);
            throw new RuntimeException("Failed to generate service", e);
        }
    }

    private String createServiceDirectory(String serviceName) throws Exception {
        String serviceDir = "/tmp/microservices/" + serviceName + "-" + UUID.randomUUID();
        Path path = Paths.get(serviceDir);
        Files.createDirectories(path);
        return serviceDir;
    }
} 