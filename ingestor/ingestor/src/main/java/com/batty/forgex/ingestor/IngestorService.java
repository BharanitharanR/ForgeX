package com.batty.forgex.ingestor;


import com.batty.forgex.framework.pipeline.PipelineService;
import com.batty.forgex.framework.tasks.Task;
import com.batty.forgex.framework.tasks.TasksList;
import com.batty.forgex.ingestor.api.GraphApi;
import com.batty.forgex.ingestor.datastore.DatastoreImpl;
import com.batty.forgex.ingestor.model.GraphInput;
import com.batty.forgex.ingestor.model.InlineResponse200;
import com.batty.forgex.ingestor.service.AsyncService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;


@Component("IngestorService")
@RestController
public class IngestorService implements GraphApi {

    protected Logger log = LoggerFactory.getLogger(IngestorService.class);
    @Autowired
    protected DatastoreImpl dbConnection;

/*    @Autowired
    protected Task entityBuilderActor;*/


    @Autowired
    protected PipelineService<TasksList> pipelineService;

    @Autowired
    protected AsyncService asyncService;

    @Override
    public ResponseEntity<InlineResponse200> graphProcessPost(GraphInput graphInput) {
        try {
            // Find the list of Nodes
            Document graphInputDocument = new Document();
            graphInputDocument.put("data", graphInput.toString());
            dbConnection.insertData(graphInputDocument);
            ObjectMapper map = new ObjectMapper();
            String jsonString = map.writeValueAsString(graphInput.getNodes());
            //entityBuilderActor.setObject(jsonString,String.class);

            pipelineService.executeTasks();
            // Response Builder
           // log.info("Response:{}",testingCompletable("sample").getNow("Not valid"));
            InlineResponse200 response200 = new InlineResponse200();
            response200.setMessage(asyncService.testingCompletable("sample").get());
            ResponseEntity<InlineResponse200> res = new ResponseEntity<>(response200,HttpStatusCode.valueOf(200));
                    // new ResponseEntity<>((InlineResponse200) entityBuilderActor.execute().get(),HttpStatusCode.valueOf(200));
            return res;
        }
        catch(Exception e) {
            log.error(" graphProcessPost Exception {}",e.getMessage());
            return null;
        }
    }
}
