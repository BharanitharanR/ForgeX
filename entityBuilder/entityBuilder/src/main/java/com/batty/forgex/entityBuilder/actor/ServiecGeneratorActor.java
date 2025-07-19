package com.batty.forgex.entityBuilder.actor;

import com.batty.forgex.entityBuilder.model.InlineResponse200;
import com.batty.forgex.framework.tasks.Task;
import com.batty.forgex.framework.tasks.TaskRegistrationService;
import com.batty.forgex.framework.utils.FolderUnzipper;
import com.batty.forgex.framework.utils.FolderZipper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service("ServiecGeneratorActor")
public class ServiecGeneratorActor implements Task<InlineResponse200> {

    @Autowired
    protected FolderUnzipper unzipper;

    @Autowired
    protected MicroServiceManagerActor microServiceManagerActor;

    @Autowired
    protected TaskRegistrationService taskRegistrationService;

    @Autowired
    @Qualifier("forgexAsyncExecutor")  // Injecting the custom executor
    protected Executor forgexAsyncExecutor;


    @Autowired
    protected FolderZipper zipper;

    protected Logger log = LoggerFactory.getLogger(ServiecGeneratorActor.class);

    ObjectMapper mapper = new ObjectMapper();

    private Object obj;          // Store the object of any type
    private Class<?> objType;    // Store the object's type

    private String parentId;


    // Constructor to accept the object or use a setter method
    public ServiecGeneratorActor() {

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

    @Override
    public CompletableFuture<InlineResponse200> execute()
    {
        try {
            unzipper.unzip(Path.of("/tmp/" + getParentId() + "/openapiSpec.zip"), Path.of("/tmp/" + getParentId()));
            List<String> urls = microServiceManagerActor.handleZipAndGenerateServices(new FileInputStream(new File("/tmp/" + getParentId() + "/openapiSpec.zip")), getParentId());
        }
        catch(Exception e)
        {
            log.error("Exception during generation {}",e);
        }
        //  return ResponseEntity.ok();
        return null;
    }

}
