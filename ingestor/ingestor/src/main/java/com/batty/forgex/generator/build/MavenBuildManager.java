package com.batty.forgex.generator.build;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class MavenBuildManager implements BuildManager {
    private static final Logger log = LoggerFactory.getLogger(MavenBuildManager.class);

    @Override
    public void build(String serviceDir) {
        try {
            ProcessBuilder builder = new ProcessBuilder("mvn", "clean", "install", "-DskipTests")
                    .directory(new File(serviceDir))
                    .inheritIO();
            Process process = builder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Failed to build service!");
            }
        } catch (Exception e) {
            log.error("Failed to build service", e);
            throw new RuntimeException("Failed to build service", e);
        }
    }
} 