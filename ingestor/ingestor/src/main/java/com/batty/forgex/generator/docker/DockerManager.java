package com.batty.forgex.generator.docker;

import com.batty.forgex.generator.context.GenerationContext;

public interface DockerManager {
    String createAndRunContainer(String serviceDir, GenerationContext context);
} 