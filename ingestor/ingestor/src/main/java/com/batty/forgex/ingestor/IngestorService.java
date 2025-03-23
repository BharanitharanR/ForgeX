package com.batty.forgex.ingestor;


import com.batty.forgex.ingestor.actor.EntityBuilderActor;
import com.batty.forgex.ingestor.api.GraphApi;
import com.batty.forgex.ingestor.datastore.DatastoreImpl;
import com.batty.forgex.ingestor.model.GraphInput;
import com.batty.forgex.ingestor.model.InlineResponse200;
import com.batty.forgex.ingestor.model.Node;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Component("IngestorService")
@RestController
public class IngestorService implements GraphApi {

    protected Logger log = LoggerFactory.getLogger(IngestorService.class);
    @Autowired
    protected DatastoreImpl dbConnection;



    @Override
    public ResponseEntity<InlineResponse200> graphProcessPost(GraphInput graphInput) {
        try {
            // Find the list of Nodes
            Document graphInputDocument = new Document();
            graphInputDocument.put("data", graphInput.toString());
            dbConnection.insertData(graphInputDocument);
            EntityBuilderActor actor = new EntityBuilderActor();
            ObjectMapper map = new ObjectMapper();
            String jsonString = map.writeValueAsString(graphInput.getNodes());

            actor.act(jsonString);
            return null;
        }
        catch(Exception e) {
            log.error(" graphProcessPost Exception {}",e.getMessage());
            return null;
        }
    }
}
