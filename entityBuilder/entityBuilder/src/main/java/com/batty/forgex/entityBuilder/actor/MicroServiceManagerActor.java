package com.batty.forgex.entityBuilder.actor;

import com.batty.forgex.entityBuilder.util.PortProvider;
import com.batty.forgex.framework.async.AsyncExecutor;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class MicroServiceManagerActor {

    private static  String BASE_DIR = "/tmp/";

    @Autowired
    protected PortProvider portProvider;
    @Autowired
    protected AsyncExecutor asyncExecutor;
    private synchronized void  setPath(String path)
    {
        BASE_DIR = BASE_DIR.concat(path).concat("/");
    }
    private synchronized String  getPath()
    {
        return BASE_DIR;
    }

    @Value("${mongodb.atlas.connection}")
    public String dbConnectionString;

    @Value("${mongodb.collection.name}")
    public String collectionName;

    public List<String> handleZipAndGenerateServices(InputStream zipInputStream,String path) throws Exception {
        Path tempZipPath = Files.createTempFile(path+ "openapi-", ".zip");
        setPath(path);
        Files.copy(zipInputStream, tempZipPath, StandardCopyOption.REPLACE_EXISTING);

        Path extractDir = Files.createTempDirectory("openapi-specs-");

        List<CompletableFuture<String>> futures = new ArrayList<>();

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(tempZipPath.toFile()))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path outputFile = extractDir.resolve(entry.getName());
                Files.createDirectories(outputFile.getParent());
                Files.copy(zis, outputFile, StandardCopyOption.REPLACE_EXISTING);
            }
        }

        List<String> serviceUrls = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(extractDir, "*.yaml")) {
            for (Path yamlPath : stream) {
                CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                    String serviceName = yamlPath.getFileName().toString().replace("-service.yaml", "");
                    // addLocalMavenDependencies(getPath());
                    return createAndRunService(serviceName, yamlPath);
                }).exceptionally(throwable -> { new RuntimeException("Exception");
                    return null;
                } );
                futures.add(future);
            }
        }

        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }


    private void generateController(String serviceDir) {
        try{

            Path datastoreDir = Paths.get(serviceDir, "src/main/java/com/batty/forgex");
            if( Files.exists(Path.of(datastoreDir + "/OpenApiGeneratorApplication.java")))
                Files.delete(Path.of(datastoreDir + "/OpenApiGeneratorApplication.java"));
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(
                    Path.of(String.valueOf(datastoreDir), "api"), "*ApiController.java")) {
                for (Path entry : stream) {
                    Files.delete(entry);
                }
            } catch (IOException e) {
                e.printStackTrace(); // Or handle appropriately
            }
            String controllerContent = """
                    package com.batty.forgex;
                    import com.fasterxml.jackson.databind.Module;
                    import org.openapitools.jackson.nullable.JsonNullableModule;
                    import org.springframework.boot.SpringApplication;
                    import org.springframework.boot.autoconfigure.SpringBootApplication;
                    import org.springframework.context.annotation.Bean;
                    import org.springframework.context.annotation.ComponentScan;
                    import org.springframework.context.annotation.FilterType;
                    import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;
                                        
                    @SpringBootApplication(
                        nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class
                    )
                    @ComponentScan(
                        basePackages = {"com.batty.forgex", "com.batty.forgex.api" , "org.openapitools.configuration"}
                    )
                    public class OpenApiGeneratorApplication {
                                        
                        public static void main(String[] args) {
                            SpringApplication.run(OpenApiGeneratorApplication.class, args);
                        }
                                        
                        @Bean(name = "com.batty.forgex.OpenApiGeneratorApplication.jsonNullableModule")
                        public Module jsonNullableModule() {
                            return new JsonNullableModule();
                        }
                                        
                    }
                    """;


            Path implFile = datastoreDir.resolve("OpenApiGeneratorApplication.java");
            Files.writeString(implFile, controllerContent);
        }
        catch(Exception e)
        {

        }
    }
    public String createAndRunService(String serviceName, Path openApiYamlPath) {
        try {
            String serviceDir = getPath() + serviceName + "-" + UUID.randomUUID();
            Files.createDirectories(Paths.get(serviceDir));

            // 0. Generate Mustache
            createTemplateStoreImpls(serviceDir);



            // 1. Generate project
            generateFromOpenAPI(serviceName, openApiYamlPath.toAbsolutePath().toString(), serviceDir);

            // 1.A Replace controller
            generateController(serviceDir);

            // 2. Inject DatastoreTemplate implementations
            generateDatastoreImpls(serviceDir);

            generateControllers(serviceDir);
            // 3. Compile
            compileService(serviceDir);

            int dynamicPort = getFreePort();
            // 4. Dockerize and deploy
            String imageName = "forgex/" + serviceName.toLowerCase();
            createDockerImage(serviceDir, imageName,dynamicPort);


            return runDockerContainer(imageName, dynamicPort);
        }
                catch(Exception e)
            {
                    return null;
            }
    }




    private void generateFromOpenAPI(String serviceName, String yamlPath, String outputDir) throws Exception
    {
            String templateDir = outputDir + "/templates";

            ProcessBuilder pb = new ProcessBuilder(
                    "openapi-generator-cli",
                    "generate",
                    "-g", "spring",
                    "-i", yamlPath,
                    "-o", outputDir,
                    "--template-dir", templateDir,
                    "--additional-properties=useSpringBoot3=true,java8=true,apiPackage=com.batty.forgex.api,modelPackage=com.batty.forgex.model,basePackage=com.batty.forgex,extraScanPackages=com.batty.forgex.framework,com.example.stuff"
            ).inheritIO();

            Process process = pb.start();
            if (process.waitFor() != 0)
            {
                throw new RuntimeException("OpenAPI code generation failed for: " + yamlPath);
            }

    }

    private void createTemplateStoreImpls(String serviceDir) throws IOException {
        Path datastoreDir = Paths.get(serviceDir, "templates/spring/");
        Files.createDirectories(datastoreDir);

        Path controllerDir = Paths.get(serviceDir, "templates/controller/");
        Files.createDirectories(controllerDir);
        String mustTemplate = """
package {{packageName}};

import com.batty.forgex.framework.datastore.interfaces.GenericDatastore;
import com.batty.forgex.framework.datastore.provider.DatastoreProvider;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.batty.forgex.model.{{entityName}};

import java.util.Optional;

@Component("{{className}}")
public class {{className}} {

    private static final Logger log = LoggerFactory.getLogger({{className}}.class);
    private final GenericDatastore<{{entityName}}> datastore;

    @Autowired
    public {{className}}(DatastoreProvider provider) {
        this.datastore = provider.getDatastore({{entityName}}.class);
    }

    public Optional<{{entityName}}> findStatus(String objId) {
        Document query = new Document("_id", new ObjectId(objId));
        return datastore.findOneFromKey(query, "data");
    }

    public Optional<InsertOneResult> insertDataResponse({{entityName}} doc) {
        return datastore.insertWithResponse(doc);
    }

    public Optional<{{entityName}}> findOne(String objId) {
        Document query = new Document("{{entityNameLower}}Id", objId);
        return datastore.findOne(query);
    }

    @SuppressWarnings("unchecked")
    public UpdateResult updateRecord(Document query, Document update) {
        return datastore.update(query, update);
    }
}
                """;

        Path implFile = datastoreDir.resolve("DataStoreImpl.mustache");
        Files.writeString(implFile, mustTemplate);


        String controllerTemplate = """
                package {{packageName}};
                                
                import com.batty.forgex.model.{{entityName}};
                import com.batty.forgex.datastore.{{entityName}}DatastoreImpl;
                import org.springframework.beans.factory.annotation.Autowired;
                import org.springframework.web.bind.annotation.*;
                import java.util.Optional;
                import com.batty.forgex.model.{{entityName}};
                import io.swagger.v3.oas.annotations.ExternalDocumentation;
                import io.swagger.v3.oas.annotations.Operation;
                import io.swagger.v3.oas.annotations.Parameter;
                import io.swagger.v3.oas.annotations.Parameters;
                import io.swagger.v3.oas.annotations.media.ArraySchema;
                import io.swagger.v3.oas.annotations.media.Content;
                import io.swagger.v3.oas.annotations.media.Schema;
                import io.swagger.v3.oas.annotations.responses.ApiResponse;
                import io.swagger.v3.oas.annotations.security.SecurityRequirement;
                import io.swagger.v3.oas.annotations.tags.Tag;
                import io.swagger.v3.oas.annotations.enums.ParameterIn;
                import org.springframework.http.HttpStatus;
                import org.springframework.http.MediaType;
                import org.springframework.http.ResponseEntity;
                import org.springframework.validation.annotation.Validated;
                import org.springframework.web.bind.annotation.*;
                import org.springframework.web.context.request.NativeWebRequest;
                import org.springframework.web.multipart.MultipartFile;
                import org.springframework.stereotype.Component;
                import jakarta.validation.Valid;
                import jakarta.validation.constraints.*;
                import java.util.List;
                import java.util.Map;
                import java.util.Optional;
                import jakarta.annotation.Generated;
                import com.mongodb.client.result.InsertOneResult;
                                
                @Component("{{entityName}}Service")
                @RestController
                public class {{entityName}}Controller implements {{entityName}}Api{
                                
                    private final {{entityName}}DatastoreImpl datastore;
                                
                    @Autowired
                    public {{entityName}}Controller({{entityName}}DatastoreImpl datastore) {
                        this.datastore = datastore;
                    }
                                
                    @Override
                    public ResponseEntity<Void> {{entityNameLower}}Post(@RequestBody {{entityName}} doc) {
                        Optional<InsertOneResult> result = datastore.insertDataResponse(doc);
                            if (result.isPresent()) {
                                return ResponseEntity.status(HttpStatus.CREATED).build();
                            } else {
                                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                            }
                    }
                                
                    @Override
                    public ResponseEntity<{{entityName}}> {{entityNameLower}}Get(@PathVariable String id) {
                        Optional<{{entityName}}> {{entityNameLower}} = datastore.findOne(id);
                        return {{entityNameLower}}
                                .map(ResponseEntity::ok)
                                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
                    }
                }
                                
                                
                """;
        implFile = controllerDir.resolve("serviceController.mustache");
        Files.writeString(implFile, controllerTemplate);


    }

    private void generateControllers(String serviceDir) throws IOException {
        Path modelDir = Paths.get(serviceDir, "src/main/java/com/batty/forgex/model");
        if (!Files.exists(modelDir)) return;

        Path outputDir = Paths.get(serviceDir, "src/main/java/com/batty/forgex/api");
        Files.createDirectories(outputDir);

        Path templatePath = Paths.get(serviceDir, "templates/controller/serviceController.mustache");
        Files.createDirectories(outputDir);
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(Files.newBufferedReader(templatePath), templatePath.getFileName().toString());

        List<Path> entityFiles = Files.walk(modelDir)
                .filter(f -> f.toString().endsWith(".java"))
                .collect(Collectors.toList());

        for (Path modelFile : entityFiles) {
            String entityName = modelFile.getFileName().toString().replace(".java", "");
            String packageName = "com.batty.forgex.api";
            String className = entityName + "Controller";

            Map<String, String> context = new HashMap<>();
            context.put("packageName", packageName);
            context.put("entityName", entityName);
            context.put("entityNameLower", entityName.toLowerCase());

            Path outputFile = outputDir.resolve(className + ".java");
            try (Writer writer = new FileWriter(outputFile.toFile())) {
                mustache.execute(writer, context).flush();
            }
        }
    }

    private void generateDatastoreImpls(String serviceDir) throws IOException {
        Path modelDir = Paths.get(serviceDir, "src/main/java/com/batty/forgex/model");
        if (!Files.exists(modelDir)) return;

        Path outputDir = Paths.get(serviceDir, "src/main/java/com/batty/forgex/datastore");
        Files.createDirectories(outputDir);

        Path templatePath = Paths.get(serviceDir, "templates/spring/DataStoreImpl.mustache");
        createDatastoreImpls(String.valueOf(templatePath));

        List<Path> entityFiles = Files.walk(modelDir)
                .filter(f -> f.toString().endsWith(".java"))
                .collect(Collectors.toList());

        for (Path modelFile : entityFiles) {
            String entityName = modelFile.getFileName().toString().replace(".java", "");
            String className = entityName + "DatastoreImpl";
            String packageName = "com.batty.forgex.datastore";

            Map<String, String> context = new HashMap<>();
            context.put("packageName", packageName);
            context.put("entityName", entityName);
            context.put("className", className);
            context.put("entityNameLower", entityName.toLowerCase());

            Path outputFilePath = outputDir.resolve(className + ".java");

            MustacheFactory mf = new DefaultMustacheFactory();
            Mustache mustache = mf.compile(Files.newBufferedReader(templatePath), templatePath.getFileName().toString());

            try (Writer writer = new FileWriter(outputFilePath.toFile())) {
                mustache.execute(writer, context).flush();
            }
        }
    }

    private void createDatastoreImpls(String serviceDir) throws IOException {
        Path modelDir = Paths.get(serviceDir, "src/main/java/com/batty/forgex/model");
        if (!Files.exists(modelDir)) return;

        Path datastoreDir = Paths.get(serviceDir, "src/main/java/com/batty/forgex/datastore");
        Files.createDirectories(datastoreDir);

        List<Path> entityFiles = Files.walk(modelDir)
                .filter(f -> f.toString().endsWith(".java"))
                .collect(Collectors.toList());

        for (Path modelFile : entityFiles) {
            String entityName = modelFile.getFileName().toString().replace(".java", "");
            String implName = entityName + "DatastoreImpl";

            String classContent = """
                    package com.forgex.datastore;
    
                    import com.batty.forgex.framework.datastore.DatastoreTemplate;
                    import com.forgex.model.%s;
                    import org.springframework.stereotype.Component;
    
                    @Component("%s")
                    public class %s {

                    private final GenericDatastore<%s> datastore;

                    @Autowired
                    public %s(DatastoreProvider provider) {
                        this.datastore = provider.getDatastore(%s.class);
                    }
                
                
                    public Optional<%s> findStatus(String objId) {
                        Document query = new Document("_id", new ObjectId(objId));
                        return datastore.findOneFromKey(query, "data");
                    }

                    public Optional<InsertOneResult> insertDataResponse(%s doc) {
                        return datastore.insertWithResponse(doc);
                    }

                    public Optional<%s> findOne(Document query) {
                        return datastore.findOne(query);
                    }

                        @SuppressWarnings(value = "unchecked")
                        public UpdateResult updateRecord(Document query, Document update) {
                        return datastore.update(query,update);
                    }
                }
                """.formatted(entityName,implName, implName, entityName,implName, entityName, implName, entityName);

            Path implFile = datastoreDir.resolve(implName + ".java");
            Files.writeString(implFile, classContent);
        }
    }


    private void addLocalMavenDependencies(String serviceDir)  {
        try {
            Path pomPath = Paths.get(serviceDir, "pom.xml");
            injectSpringBootPlugin(pomPath.toString());
            injectDependencies(pomPath.toString());
            ProcessBuilder builder = new ProcessBuilder("mvn install:install-file -Dfile=" + serviceDir + "/framework-utils-0.0.1-SNAPSHOT.jar \\\n" +
                    "    -DgroupId=com.batty.forgex \\\n" +
                    "    -DartifactId=framework-utils \\\n" +
                    "    -Dversion=0.0.1-SNAPSHOT \\\n" +
                    "    -Dpackaging=jar")
                    .directory(new File(serviceDir))
                    .inheritIO();
            Process process = builder.start();
            if (process.waitFor() != 0) {
                throw new RuntimeException("Failed to compile the service!");
            }
            builder = new ProcessBuilder("mvn install:install-file -Dfile=" + serviceDir + "/framework-datastore-0.0.1-SNAPSHOT.jar \\\n" +
                    "    -DgroupId=com.batty.forgex \\\n" +
                    "    -DartifactId=framework-datastore \\\n" +
                    "    -Dversion=0.0.1-SNAPSHOT \\\n" +
                    "    -Dpackaging=jar");
            process = builder.start();
            if (process.waitFor() != 0) {
                throw new RuntimeException("Failed to compile the service!");
            }
        }
        catch(Exception e)
        {

        }

    }

    private void serviceController(String serviceDir) throws Exception {
        Path serviceControllerDir = Paths.get(serviceDir, "src/main/java/com/batty/forgex/service");
        Files.createDirectories(serviceControllerDir);

        Path templatePath = Paths.get(serviceDir, "templates/controller/ServiceController.mustache");
        createDatastoreImpls(String.valueOf(templatePath));


    }
    private void compileService(String serviceDir) throws Exception {
        Path pomPath = Paths.get(serviceDir, "pom.xml");
        injectSpringBootPlugin(pomPath.toString());
        injectDependencies(pomPath.toString());
        ProcessBuilder builder = new ProcessBuilder("mvn", "clean", "install", "-DskipTests")
                .directory(new File(serviceDir))
                .inheritIO();
        Process process = builder.start();
        if (process.waitFor() != 0) {
            throw new RuntimeException("Failed to compile the service!");
        }
    }

    private void createDockerImage(String serviceDir, String imageName,long port) throws Exception {
        StringBuilder dockerfile = new StringBuilder("""
                FROM bellsoft/liberica-openjdk-alpine-musl:21-cds
                VOLUME /tmp
                COPY target/*.jar app.jar
            
                ENTRYPOINT ["java",
                    "-Dspring.application.name=${SERVICE_NAME}",
                    "-Deureka.client.serviceUrl.defaultZone=http://eureka:8761/eureka",
                    "-Deureka.instance.hostname=${SERVICE_HOSTNAME}",
                    "-Djava.security.egd=file:/dev/./urandom",
                    "-jar", "/app.jar"]
                """);


        dockerfile.append(constructEntryPointForContainer(imageName,port));

        Path dockerPath = Paths.get(serviceDir + "/Dockerfile");
        Files.writeString(dockerPath, dockerfile);

        ProcessBuilder builder = new ProcessBuilder("docker", "build", "-t", imageName, ".")
                .directory(new File(serviceDir))
                .inheritIO();
        Process process = builder.start();
        if (process.waitFor() != 0) {
            throw new RuntimeException("Failed to build Docker image!");
        }
    }

    private void injectDependencies(String pomPath) throws IOException {
        Path path = Paths.get(pomPath);
        String content = Files.readString(path);

        if (content.contains("framework-datastore")) return;

        String depXml = """
                <dependency>
                    <groupId>com.batty.forgex</groupId>
                    <artifactId>framework-datastore</artifactId>
                    <version>0.0.1-SNAPSHOT</version>
                </dependency>
                <dependency>
                    <groupId>com.batty.forgex</groupId>
                    <artifactId>framework-utils</artifactId>
                    <version>0.0.1-SNAPSHOT</version>
                </dependency>
                <dependency>
                  <groupId>org.springframework.boot</groupId>
                  <artifactId>spring-boot-starter-data-mongodb</artifactId>
                </dependency>
                <dependency>
                	<groupId>org.springdoc</groupId>
                	<artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
                	<version>2.1.0</version> <!-- Use latest -->
                </dependency>
                """;

        String updated;
        if (content.contains("<dependencies>")) {
            updated = content.replaceFirst("</dependencies>", depXml + "\n</dependencies>");
        } else {
            updated = "<dependencies>\n" + depXml + "\n</dependencies>";
        }

        Files.writeString(path, updated);
    }

    private void injectSpringBootPlugin(String pomPath) throws IOException {
        Path path = Paths.get(pomPath);
        String content = Files.readString(path);

        if (content.contains("spring-boot-maven-plugin")) return;

        String pluginXml = """
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <executions>
                    <execution>
                        <goals>
                            <goal>repackage</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
            """;

        String updated;
        if (content.contains("<plugins>")) {
            updated = content.replaceFirst("</plugins>", pluginXml + "\n</plugins>");
        } else if (content.contains("</build>")) {
            updated = content.replaceFirst("</build>", "<plugins>\n" + pluginXml + "\n</plugins>\n</build>");
        } else {
            updated = content.replaceFirst("</project>", "<build><plugins>" + pluginXml + "</plugins></build>\n</project>");
        }

        Files.writeString(path, updated);
    }

    private String runDockerContainer(String imageName, int port) throws Exception {
        ProcessBuilder builder = new ProcessBuilder(
                "docker", "run", "-d",
                "--network", "forgex-integrator",
                "-p", port + ":" + port,
                imageName
        ).inheritIO();

        Process process = builder.start();
        if (process.waitFor() != 0) {
            throw new RuntimeException("Failed to start Docker container!");
        }

        Thread.sleep(5000);
        return "http://localhost:" + port;
    }

    private int getFreePort() {
            return PortProvider.getPort();
    }

    private StringBuilder constructEntryPointForContainer(String imageName,long port)
    {
        return new StringBuilder().append("ENTRYPOINT [\"java\",")
        .append("\"-Dserver.port=").append(port +"\",")
        .append("\"-Dmongodb.atlas.connection=").append( dbConnectionString +"\",")
        .append("\"-Dmongodb.collection.name=").append( imageName+"\",")
        .append(("\"-Dmongodb.database.name=forgex\","))
        .append("\"-jar\",")
        .append("\"/app.jar\"]");
    }
}
