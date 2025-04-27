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

            // 3. Compile
            compileService(serviceDir);

            // 4. Dockerize and deploy
            String imageName = "forgex/" + serviceName.toLowerCase();
            createDockerImage(serviceDir, imageName);

            int dynamicPort = getFreePort();
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

    public Optional<{{entityName}}> findOne(Document query) {
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

    private void createDockerImage(String serviceDir, String imageName) throws Exception {
        StringBuilder dockerfile = new StringBuilder("""
                FROM bellsoft/liberica-openjdk-alpine-musl:21-cds
                VOLUME /tmp
                COPY target/*.jar app.jar
                """);

        dockerfile.append(constructEntryPointForContainer(imageName));

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

    private StringBuilder constructEntryPointForContainer(String imageName)
    {
        return new StringBuilder().append("ENTRYPOINT [\"java\",")
        .append("\"-Dserver.port=9595\",")
        .append("\"-Dmongodb.atlas.connection=").append( dbConnectionString +"\",")
        .append("\"-Dmongodb.collection.name=").append( imageName+"\",")
        .append(("\"-Dmongodb.database.name=forgex\","))
        .append("\"-jar\",")
        .append("\"/app.jar\"]");
    }
}
