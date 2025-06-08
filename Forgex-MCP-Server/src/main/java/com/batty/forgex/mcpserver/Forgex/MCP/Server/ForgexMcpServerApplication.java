package com.batty.forgex.mcpserver.Forgex.MCP.Server;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ForgexMcpServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ForgexMcpServerApplication.class, args);
	}
	@Bean
	public ToolCallbackProvider forgexTools(ForgexService forgexService) {
		return  MethodToolCallbackProvider.builder().toolObjects(forgexService).build();
	}

}
