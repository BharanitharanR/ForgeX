package com.batty.forgex.ingestor.service;

import com.batty.forgex.framework.pipeline.PipelineService;
import com.batty.forgex.ingestor.datastore.GraphInputDatastoreImpl;
import com.batty.forgex.ingestor.model.GraphInput;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.InsertOneResult;
import org.bson.BsonObjectId;
import org.bson.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ForgexService {


    @Autowired
    ObjectMapper mapper = new ObjectMapper();

    Optional<InsertOneResult> responseData = Optional.empty();

    @Autowired
    protected GraphInputDatastoreImpl dbConnection;


    @Autowired
    private PipelineService pipelineService;

    @Autowired
    protected AsyncService asyncService;



    public ForgexService() {

    }

    @Tool(description = "Deploy a forgex service")
    public String generateForgexService(String graphInput) {
        String status  = "Failed";
            try
            {
                responseData  = dbConnection.insertDataResponse(mapper.convertValue(graphInput,GraphInput.class));
                AtomicReference<String> resp =new AtomicReference<>();
                responseData.ifPresent(
                        (value) ->
                        {
                            resp.set(((BsonObjectId) value.getInsertedId().asObjectId()).getValue().toHexString());
                        });

                pipelineService.executeTasks(graphInput, GraphInput.class,resp.get());
                return(resp.get());
            }
            catch(Exception e)
            {
                return status;
            }
    }

}