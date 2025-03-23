package com.batty.forgex.entityBuilder;

import com.batty.forgex.entityBuilder.api.EntityApi;
import com.batty.forgex.entityBuilder.datastore.DatastoreImpl;
import com.batty.forgex.entityBuilder.model.InlineResponse200;
import com.batty.forgex.entityBuilder.model.Node;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@Component("EntityBuilderService")
@RestController
public class EntityBuilderService implements EntityApi {
    @Autowired
    protected DatastoreImpl dbConnection;

    @Override
    public ResponseEntity<InlineResponse200> entityProcessPost(List<Node> node) {
        org.bson.Document entityBuilderDocument =  new Document();
        entityBuilderDocument.put("data",node.toString());
        dbConnection.insertData(entityBuilderDocument);
        return null;
    }
}
