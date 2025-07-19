package com.batty.forgex.generator.generator;

import com.batty.forgex.generator.context.GenerationContext;

public interface CodeGenerator {
    void generate(String outputDir, GenerationContext context);
} 