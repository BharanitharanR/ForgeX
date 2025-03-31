package com.batty.forgex.entityBuilder;

import com.batty.forgex.entityBuilder.api.EntityApi;
import com.batty.forgex.entityBuilder.datastore.DatastoreImpl;
import com.batty.forgex.entityBuilder.model.InlineResponse200;
import com.batty.forgex.entityBuilder.model.Node;
import com.fasterxml.jackson.core.JsonProcessingException;
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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;


@Component("EntityBuilderService")
@RestController
public class EntityBuilderService implements EntityApi {

    protected Logger log = LoggerFactory.getLogger(EntityBuilderService.class);
    @Autowired
    protected DatastoreImpl dbConnection;

    @Autowired
    ObjectMapper mapper = new ObjectMapper();

    Optional<InsertOneResult> responseData = Optional.empty();

    AtomicReference<String> resp =new AtomicReference<>();

    @Override
    public ResponseEntity<InlineResponse200> entityProcessPost(List<Node> node) {

        try {
            log.info("Inserting doc in entity builder...");
            org.bson.Document entityBuilderDocument = new Document();
            String json = mapper.writeValueAsString(node);
            entityBuilderDocument.put("data", node.toString());
            responseData=dbConnection.insertDataResponse(entityBuilderDocument);
            responseData.ifPresent(
                    (value) ->
                    {
                        resp.set(((BsonObjectId) value.getInsertedId().asObjectId()).getValue().toHexString());
                    });
            // Response Builder
            InlineResponse200 response200 = new InlineResponse200();
            response200.setMessage(resp.get());
            ResponseEntity<InlineResponse200> res = new ResponseEntity<>(response200, HttpStatusCode.valueOf(200));
            return res;
        }
        catch(Exception e)
        {
            return null;
        }
    }
}
