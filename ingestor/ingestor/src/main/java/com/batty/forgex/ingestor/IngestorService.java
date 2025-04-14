package com.batty.forgex.ingestor;


import com.batty.forgex.framework.pipeline.PipelineService;
import com.batty.forgex.ingestor.api.GraphApi;
import com.batty.forgex.ingestor.datastore.DatastoreImpl;
import com.batty.forgex.ingestor.model.GraphInput;
import com.batty.forgex.ingestor.model.InlineResponse200;
import com.batty.forgex.ingestor.model.InlineResponse2001;
import com.batty.forgex.ingestor.service.AsyncService;
import com.batty.forgex.ingestor.serviceGenerator.OpenApiSpecBuilder;
import com.batty.forgex.ingestor.serviceGenerator.OpenApiSpecGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.InsertOneResult;
import org.bson.BsonObjectId;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;


@SuppressWarnings("unchecked")
@Component("IngestorService")
@RestController
public class IngestorService implements GraphApi {

    protected Logger log = LoggerFactory.getLogger(IngestorService.class);
    @Autowired
    protected DatastoreImpl dbConnection;

/*    @Autowired
    protected Task entityBuilderActor;*/


    @Autowired
    ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private PipelineService pipelineService;

    @Autowired
    protected AsyncService asyncService;

    Optional<InsertOneResult> responseData = Optional.empty();

    @Override
    public ResponseEntity<InlineResponse2001> graphIdGet(String id) {
        // Response Builder
        InlineResponse2001 response200 = new InlineResponse2001();
        response200.id(id);
        response200.data(dbConnection.findStatus(id).toString());
        ResponseEntity<InlineResponse2001> res = new ResponseEntity<>(response200,HttpStatusCode.valueOf(200));
        return res;

    }

    @Override
    public ResponseEntity<InlineResponse200> graphProcessPost(GraphInput graphInput) {
        try {
            // Find the list of Nodes
            Document graphInputDocument = new Document();
            String json = mapper.writeValueAsString(graphInput);
            graphInputDocument.put("data", json);
            responseData  = dbConnection.insertDataResponse(graphInputDocument);
            AtomicReference<String> resp =new AtomicReference<>();
            responseData.ifPresent(
                    (value) ->
                    {
                        resp.set(((BsonObjectId) value.getInsertedId().asObjectId()).getValue().toHexString());
                    });

            String jsonString = mapper.writeValueAsString(graphInput);

            pipelineService.executeTasks(graphInput,GraphInput.class,resp.get());
            // Response Builder
            InlineResponse200 response200 = new InlineResponse200();
            response200.setMessage(resp.get());
            ResponseEntity<InlineResponse200> res = new ResponseEntity<>(response200,HttpStatusCode.valueOf(200));
            return res;
        }
        catch(Exception e) {
            log.error(" graphProcessPost Exception {}",e.getMessage());
            return null;
        }
    }
}
