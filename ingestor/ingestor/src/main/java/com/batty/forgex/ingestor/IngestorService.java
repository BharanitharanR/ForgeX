package com.batty.forgex.ingestor;


import com.batty.forgex.ingestor.api.GraphApi;
import com.batty.forgex.ingestor.datastore.DatastoreImpl;
import com.batty.forgex.ingestor.model.GraphInput;
import com.batty.forgex.ingestor.model.InlineResponse200;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

@Component("IngestorService")
@RestController
public class IngestorService implements GraphApi {

    @Autowired
    protected DatastoreImpl dbConnection;

    @Override
    public ResponseEntity<InlineResponse200> graphProcessPost(GraphInput graphInput) {
        // Find the list of Nodes
        // Nodes = services
        return null;
    }
}
