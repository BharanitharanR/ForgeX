package com.batty.forgex.ingestor.service;

import com.batty.forgex.entityBuilder.api.DefaultApi;
import com.batty.forgex.entityBuilder.api.client.ApiClient;
import com.batty.forgex.entityBuilder.api.model.InlineResponse200;
import com.batty.forgex.entityBuilder.api.model.Node;
import com.batty.forgex.framework.tasks.Task;
import com.batty.forgex.framework.tasks.TaskRegistrationService;
import com.batty.forgex.ingestor.datastore.DatastoreImpl;
import com.batty.forgex.ingestor.pojo.MicroserviceRequest;
import com.batty.forgex.ingestor.serviceGenerator.MicroserviceManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service("EntityBuilderActor")
public class EntityBuilderActor implements Task<InlineResponse200> {


    @Autowired
    protected TaskRegistrationService taskRegistrationService;

    @Autowired
    @Qualifier("forgexAsyncExecutor")  // Injecting the custom executor
    protected Executor forgexAsyncExecutor;

    @Autowired
    protected DatastoreImpl dbConnection;

    protected Logger log = LoggerFactory.getLogger(EntityBuilderActor.class);

    ObjectMapper mapper = new ObjectMapper();
    @Value("${entityBuilder.service.hostname}")
    public String entityBuilderHostname;

    private Object obj;          // Store the object of any type
    private Class<?> objType;    // Store the object's type

    private String parentId;
    // Constructor to accept the object or use a setter method
    public EntityBuilderActor() {

    }

    @PostConstruct
    public void register()
    {
        taskRegistrationService.registerTask(this);
    }

    public String getName()
    {
        return "EntityBuilderActor";
    }
    @Override
    public <U> void setObject(U obj, Class<U> type) {
        this.obj = obj;          // Store the object
        this.objType = type;     // Store the type info for casting
    }

    @Override
    public <U> U getObject(Class<U> type) {
        if (type.isInstance(obj)) {
            return type.cast(obj);  // Safe casting using the provided type
        } else {
            throw new ClassCastException("Cannot cast " + (obj != null ? obj.getClass() : "null") + " to " + type);
        }
    }

    @Override
    public void setParentId(String id) {
        this.parentId = id;
    }

    @Override
    public String getParentId() {
        return this.parentId;
    }


    // Asynchronous execution of the task
    @Override
    @Async("forgexAsyncExecutor")
    public CompletableFuture<InlineResponse200> execute() {
        return CompletableFuture.supplyAsync(() -> {
            try {


                MicroserviceRequest.Field field1 = new MicroserviceRequest.Field("id", "UUID", true, null);
                MicroserviceRequest.Field field2 = new MicroserviceRequest.Field("name", "String", true, null);
                MicroserviceRequest.Field field3 = new MicroserviceRequest.Field("price", "Double", true, "0.0");
                List<MicroserviceRequest.Field> fields = Arrays.asList(field1, field2, field3);
                // Create MicroserviceRequest
                MicroserviceRequest request = new MicroserviceRequest();
                request.setName("POSService");
                request.setEntityName("Product");
                request.setFields(fields);
                request.setDependencies(Arrays.asList("web", "data-jpa", "postgresql"));
                request.setDatabase("PostgreSQL");
                MicroserviceManager ms = new MicroserviceManager();
                ms.createAndRunService(request);


                log.info("Executing EntityBuilderActor task...");
                log.info("data in act: {}", getObject(String.class));
                log.info("entityBuilderHostname: {}", entityBuilderHostname);

                // Set up the API client
                com.batty.forgex.entityBuilder.api.client.Configuration.setDefaultApiClient(
                        new ApiClient().setBasePath(entityBuilderHostname)
                );
                DefaultApi entityBuilderSDK = new DefaultApi();

                // Parse the input object into a list of nodes
                List<Node> nodeList = mapper.readValue(getObject(String.class), new TypeReference<List<Node>>() {});
                InlineResponse200 response = entityBuilderSDK.entityProcessPost(nodeList);


                log.info("Task completed successfully: {} {}",getParentId(), response);
                return response;

            } catch (Exception e) {
                log.error("Exception in task execution: {}", e.getMessage(), e);
                return new InlineResponse200().message("Failed with exception: " + e.getMessage());
            }
        },forgexAsyncExecutor);
    }
}
