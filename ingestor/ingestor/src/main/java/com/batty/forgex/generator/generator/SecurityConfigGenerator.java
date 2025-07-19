package com.batty.forgex.generator.generator;

import com.batty.forgex.generator.model.Entity;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SecurityConfigGenerator {
    
    public void generateSecurityConfig(Entity entity, String outputPath) throws IOException {
        String packageName = "com.batty.forgex.security";
        String className = "SecurityConfig";
        
        StringBuilder content = new StringBuilder();
        
        // Package declaration
        content.append("package ").append(packageName).append(";\n\n");
        
        // Imports
        content.append("import org.springframework.context.annotation.Bean;\n");
        content.append("import org.springframework.context.annotation.Configuration;\n");
        content.append("import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;\n");
        content.append("import org.springframework.security.config.annotation.web.builders.HttpSecurity;\n");
        content.append("import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;\n");
        content.append("import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;\n");
        content.append("import org.springframework.security.config.http.SessionCreationPolicy;\n");
        content.append("import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;\n");
        content.append("import org.springframework.security.crypto.password.PasswordEncoder;\n");
        content.append("import org.springframework.web.cors.CorsConfiguration;\n");
        content.append("import org.springframework.web.cors.CorsConfigurationSource;\n");
        content.append("import org.springframework.web.cors.UrlBasedCorsConfigurationSource;\n");
        content.append("import java.util.Arrays;\n\n");
        
        // Class declaration
        content.append("@Configuration\n");
        content.append("@EnableWebSecurity\n");
        content.append("public class ").append(className).append(" extends WebSecurityConfigurerAdapter {\n\n");
        
        // Password encoder bean
        content.append("    @Bean\n");
        content.append("    public PasswordEncoder passwordEncoder() {\n");
        content.append("        return new BCryptPasswordEncoder();\n");
        content.append("    }\n\n");
        
        // CORS configuration
        content.append("    @Bean\n");
        content.append("    public CorsConfigurationSource corsConfigurationSource() {\n");
        content.append("        CorsConfiguration configuration = new CorsConfiguration();\n");
        content.append("        configuration.setAllowedOrigins(Arrays.asList(\"*\"));\n");
        content.append("        configuration.setAllowedMethods(Arrays.asList(\"GET\", \"POST\", \"PUT\", \"DELETE\", \"OPTIONS\"));\n");
        content.append("        configuration.setAllowedHeaders(Arrays.asList(\"*\"));\n");
        content.append("        configuration.setAllowCredentials(true);\n");
        content.append("        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();\n");
        content.append("        source.registerCorsConfiguration(\"/**\", configuration);\n");
        content.append("        return source;\n");
        content.append("    }\n\n");
        
        // HTTP Security configuration
        content.append("    @Override\n");
        content.append("    protected void configure(HttpSecurity http) throws Exception {\n");
        content.append("        http\n");
        content.append("            .cors().and()\n");
        content.append("            .csrf().disable()\n");
        content.append("            .authorizeRequests()\n");
        content.append("            .antMatchers(\"/api/auth/**\").permitAll()\n");
        content.append("            .antMatchers(\"/api/").append(entity.getName().toLowerCase()).append("/**\").hasRole(\"ADMIN\")\n");
        content.append("            .anyRequest().authenticated()\n");
        content.append("            .and()\n");
        content.append("            .sessionManagement()\n");
        content.append("            .sessionCreationPolicy(SessionCreationPolicy.STATELESS);\n");
        content.append("    }\n\n");
        
        // Authentication configuration
        content.append("    @Override\n");
        content.append("    protected void configure(AuthenticationManagerBuilder auth) throws Exception {\n");
        content.append("        auth.userDetailsService(userDetailsService())\n");
        content.append("            .passwordEncoder(passwordEncoder());\n");
        content.append("    }\n\n");
        
        // Authentication manager bean
        content.append("    @Bean\n");
        content.append("    @Override\n");
        content.append("    public AuthenticationManager authenticationManagerBean() throws Exception {\n");
        content.append("        return super.authenticationManagerBean();\n");
        content.append("    }\n");
        
        content.append("}\n");
        
        // Create directory if it doesn't exist
        Path outputDir = Paths.get(outputPath);
        Path packageDir = outputDir.resolve(packageName.replace('.', '/'));
        Files.createDirectories(packageDir);
        
        // Write file
        Path filePath = packageDir.resolve(className + ".java");
        Files.writeString(filePath, content.toString());
    }
} 