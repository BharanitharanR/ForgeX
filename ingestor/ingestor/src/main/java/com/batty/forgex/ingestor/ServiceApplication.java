package com.batty.forgex.ingestor;

import com.batty.forgex.ingestor.service.ForgexService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

@Component("ServiceBean")
@ComponentScan(basePackages =  "com.batty")
@SpringBootApplication
@EnableAsync
public class ServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceApplication.class, args);
	}

	@Bean
	public ToolCallbackProvider weatherTools(ForgexService forgexService) {
		return  MethodToolCallbackProvider.builder().toolObjects(forgexService).build();
	}
}
